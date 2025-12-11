package com.devalvesg.transaction_service.adapters.messaging;

import com.devalvesg.transaction_service.adapters.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Value("${spring.kafka.topics.transactions-created}")
    private String transactionsCreatedTopic;

    @Value("${spring.kafka.topics.transactions-updated}")
    private String transactionsUpdatedTopic;

    public void sendTransactionCreatedEvent(TransactionEvent event) {
        log.info("Sending transaction created event for transactionId: {}", event.getTransactionId());
        sendEvent(transactionsCreatedTopic, event);
    }

    public void sendTransactionUpdatedEvent(TransactionEvent event) {
        log.info("Sending transaction updated event for transactionId: {}", event.getTransactionId());
        sendEvent(transactionsUpdatedTopic, event);
    }

    private void sendEvent(String topic, TransactionEvent event) {
        Message<TransactionEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .setHeader(KafkaHeaders.KEY, event.getTransactionId())
                .build();

        CompletableFuture<SendResult<String, TransactionEvent>> future = kafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event to topic {}: {}", topic, ex.getMessage(), ex);
            } else {
                log.info("Successfully sent event to topic {} with offset {}",
                         topic, result.getRecordMetadata().offset());
            }
        });
    }
}
