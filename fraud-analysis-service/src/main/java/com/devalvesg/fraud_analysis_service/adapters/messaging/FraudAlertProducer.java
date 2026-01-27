package com.devalvesg.fraud_analysis_service.adapters.messaging;

import com.devalvesg.fraud_analysis_service.adapters.dto.FraudAlert;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudAlertProducer {

    private final KafkaTemplate<String, FraudAlert> fraudAlertKafkaTemplate;

    @Value("${spring.kafka.topics.fraud-alerts}")
    private String fraudAlertsTopic;

    /**
     * Send fraud alert to Kafka topic
     * Uses async sending with CompletableFuture callbacks
     *
     * @param alert Fraud alert to send
     */
    public void sendFraudAlert(FraudAlert alert) {
        Message<FraudAlert> message = MessageBuilder
                .withPayload(alert)
                .setHeader(KafkaHeaders.TOPIC, fraudAlertsTopic)
                .setHeader(KafkaHeaders.KEY, alert.getTransactionUuid())
                .build();

        CompletableFuture<SendResult<String, FraudAlert>> future = fraudAlertKafkaTemplate.send(message);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send fraud alert for transaction {}: {}",
                        alert.getTransactionUuid(), ex.getMessage(), ex);
            } else {
                log.info("Fraud alert sent for transaction {} (fraud={}, score={}) to topic {} at offset {}",
                        alert.getTransactionUuid(),
                        alert.getFlaggedAsFraud(),
                        alert.getRiskScore(),
                        fraudAlertsTopic,
                        result.getRecordMetadata().offset());
            }
        });
    }
}
