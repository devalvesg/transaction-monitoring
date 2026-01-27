package com.devalvesg.fraud_analysis_service.adapters.messaging;

import com.devalvesg.fraud_analysis_service.adapters.dto.FailedMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueProducer {

    private final KafkaTemplate<String, FailedMessageEvent> dlqKafkaTemplate;

    @Value("${spring.kafka.topics.dead-letter-queue}")
    private String deadLetterQueueTopic;

    public void sendToDeadLetterQueue(ConsumerRecord<?, ?> consumerRecord, Exception exception, int retryAttempts) {
        log.warn("Sending message to DLQ after {} retry attempts. Original topic: {}, partition: {}, offset: {}",
                retryAttempts, consumerRecord.topic(), consumerRecord.partition(), consumerRecord.offset());

        FailedMessageEvent failedMessage = FailedMessageEvent.builder()
                .originalTopic(consumerRecord.topic())
                .originalPartition(consumerRecord.partition())
                .originalOffset(consumerRecord.offset())
                .messageKey(consumerRecord.key() != null ? consumerRecord.key().toString() : null)
                .originalPayload(consumerRecord.value())
                .errorMessage(exception.getMessage())
                .exceptionClass(exception.getClass().getName())
                .stackTrace(getStackTraceAsString(exception))
                .failedAt(Instant.now())
                .retryAttempts(retryAttempts)
                .build();

        sendToDLQ(failedMessage);
    }

    private void sendToDLQ(FailedMessageEvent event) {
        Message<FailedMessageEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, deadLetterQueueTopic)
                .setHeader(KafkaHeaders.KEY, event.getMessageKey())
                .build();

        CompletableFuture<SendResult<String, FailedMessageEvent>> future = dlqKafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("CRITICAL: Failed to send message to DLQ topic {}: {}",
                        deadLetterQueueTopic, ex.getMessage(), ex);
                log.error("Original failed message details: topic={}, partition={}, offset={}",
                        event.getOriginalTopic(), event.getOriginalPartition(), event.getOriginalOffset());
            } else {
                log.info("Successfully sent message to DLQ topic {} with offset {}. Original message from topic: {}, partition: {}, offset: {}",
                        deadLetterQueueTopic,
                        result.getRecordMetadata().offset(),
                        event.getOriginalTopic(),
                        event.getOriginalPartition(),
                        event.getOriginalOffset());
            }
        });
    }

    private String getStackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}
