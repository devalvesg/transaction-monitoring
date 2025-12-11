package com.devalvesg.fraud_analysis_service.adapters.messaging;

import com.devalvesg.fraud_analysis_service.adapters.dto.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {

    @KafkaListener(
            topics = "${spring.kafka.topics.transactions-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionCreated(
            @Payload TransactionEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition,
            @Header(KafkaHeaders.OFFSET) Long offset
    ) {
        log.info("Received transaction created event from topic: {}, partition: {}, offset: {}",
                topic, partition, offset);
        log.info("Transaction details - ID: {}, TransactionId: {}, From: {}, To: {}, Amount: {} {}, Network: {}",
                event.getId(),
                event.getTransactionId(),
                event.getFromAddress(),
                event.getToAddress(),
                event.getAmount(),
                event.getCurrency(),
                event.getNetwork());

        processTransactionEvent(event);

        log.info("Successfully processed transaction created event for transactionId: {}", event.getTransactionId());
    }

    private void processTransactionEvent(TransactionEvent event) {
        log.info("Processing transaction for fraud analysis: {}", event.getTransactionId());
    }
}
