package com.devalvesg.fraud_analysis_service.adapters.messaging;

import com.devalvesg.fraud_analysis_service.adapters.dto.FraudAlert;
import com.devalvesg.fraud_analysis_service.adapters.dto.TransactionEvent;
import com.devalvesg.fraud_analysis_service.application.dto.FraudAnalysisResult;
import com.devalvesg.fraud_analysis_service.application.services.FraudAnalysisService;
import com.devalvesg.fraud_analysis_service.domain.models.enums.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {

    private final FraudAnalysisService fraudAnalysisService;
    private final FraudAlertProducer fraudAlertProducer;

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
        try {
            log.info("Processing transaction for fraud analysis: {}", event.getTransactionId());

            FraudAnalysisResult result = fraudAnalysisService.analyzeTransaction(event);

            FraudAlert alert = FraudAlert.builder()
                    .eventType("FRAUD_ANALYSIS_COMPLETED")
                    .eventTimestamp(Instant.now())
                    .transactionId(result.getTransactionId())
                    .transactionUuid(result.getTransactionUuid())
                    .flaggedAsFraud(result.getFlaggedAsFraud())
                    .riskScore(result.getRiskScore())
                    .triggeredRules(result.getTriggeredRules().stream()
                            .map(FraudRule::name)
                            .collect(Collectors.toList()))
                    .fraudReason(buildFraudReason(result))
                    .build();

            fraudAlertProducer.sendFraudAlert(alert);

            log.info("Transaction {} analyzed: fraud={}, score={}, rules={}",
                    event.getTransactionId(),
                    result.getFlaggedAsFraud(),
                    result.getRiskScore(),
                    result.getTriggeredRules().size());

        } catch (Exception e) {
            log.error("Error analyzing transaction {}: {}",
                    event.getTransactionId(), e.getMessage(), e);
            throw e;
        }
    }

    private String buildFraudReason(FraudAnalysisResult result) {
        if (result.getRuleDetails().isEmpty()) {
            return "No fraud indicators detected";
        }

        return result.getRuleDetails().entrySet().stream()
                .map(entry -> entry.getKey().getDescription() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
    }
}
