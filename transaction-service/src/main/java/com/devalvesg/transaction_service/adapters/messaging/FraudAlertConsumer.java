package com.devalvesg.transaction_service.adapters.messaging;

import com.devalvesg.transaction_service.adapters.dto.FraudAlert;
import com.devalvesg.transaction_service.application.services.TransactionService;
import com.devalvesg.transaction_service.domain.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudAlertConsumer {

    private final TransactionService transactionService;

    /**
     * Consumes fraud alerts from Kafka and updates the corresponding transaction
     * with fraud detection results (flaggedAsFraud and riskScore).
     *
     * @param fraudAlert The fraud alert message from Kafka
     * @param partition Kafka partition
     * @param offset Kafka offset
     * @param acknowledgment Manual acknowledgment for error handling
     */
    @KafkaListener(
            topics = "${spring.kafka.topics.fraud-alerts}",
            containerFactory = "fraudAlertKafkaListenerContainerFactory"
    )
    public void consumeFraudAlert(
            @Payload FraudAlert fraudAlert,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received fraud alert for transaction {} from partition {} at offset {}",
                fraudAlert.getTransactionUuid(), partition, offset);

        try {
            if (fraudAlert.getTransactionId() == null) {
                log.error("Fraud alert missing transactionId: {}", fraudAlert);
                acknowledgment.acknowledge();
                return;
            }

            transactionService.updateTransactionFraudDetails(
                    fraudAlert.getTransactionId(),
                    fraudAlert.getFlaggedAsFraud(),
                    fraudAlert.getRiskScore()
            );

            log.info("Successfully processed fraud alert for transaction {} (fraud={}, score={})",
                    fraudAlert.getTransactionUuid(),
                    fraudAlert.getFlaggedAsFraud(),
                    fraudAlert.getRiskScore());

            acknowledgment.acknowledge();

        } catch (CustomException e) {
            log.error("Business error processing fraud alert for transaction {}: {}",
                    fraudAlert.getTransactionUuid(), e.getMessage());
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Unexpected error processing fraud alert for transaction {}: {}",
                    fraudAlert.getTransactionUuid(), e.getMessage(), e);
            throw e;
        }
    }
}