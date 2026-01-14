package com.devalvesg.transaction_service.application.services;

import com.devalvesg.transaction_service.adapters.dto.TransactionEvent;
import com.devalvesg.transaction_service.adapters.mappers.TransactionMapper;
import com.devalvesg.transaction_service.adapters.messaging.TransactionEventProducer;
import com.devalvesg.transaction_service.adapters.persistence.TransactionRepository;
import com.devalvesg.transaction_service.domain.contracts.ITransactionService;
import com.devalvesg.transaction_service.domain.exceptions.CustomException;
import com.devalvesg.transaction_service.domain.models.entities.FraudRuleViolationEntity;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.FraudRule;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;
    private final TransactionMapper transactionMapper;
    private final Counter transactionCreatedCounter;
    private final Timer transactionCreationTimer;
    private final Counter fraudAlertReceivedCounter;
    private final Counter fraudDetectedCounter;
    private final DistributionSummary transactionAmountSummary;
    private final DistributionSummary riskScoreSummary;
    private final AtomicInteger pendingTransactionCount;
    private final MeterRegistry meterRegistry;

    public TransactionService(
            TransactionRepository repository,
            TransactionEventProducer eventProducer,
            TransactionMapper transactionMapper,
            Counter transactionCreatedCounter,
            Timer transactionCreationTimer,
            Counter fraudAlertReceivedCounter,
            Counter fraudDetectedCounter,
            DistributionSummary transactionAmountSummary,
            DistributionSummary riskScoreSummary,
            AtomicInteger pendingTransactionCount,
            MeterRegistry meterRegistry) {
        this.transactionRepository = repository;
        this.eventProducer = eventProducer;
        this.transactionMapper = transactionMapper;
        this.transactionCreatedCounter = transactionCreatedCounter;
        this.transactionCreationTimer = transactionCreationTimer;
        this.fraudAlertReceivedCounter = fraudAlertReceivedCounter;
        this.fraudDetectedCounter = fraudDetectedCounter;
        this.transactionAmountSummary = transactionAmountSummary;
        this.riskScoreSummary = riskScoreSummary;
        this.pendingTransactionCount = pendingTransactionCount;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public TransactionEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new CustomException("Invalid identifier");
        }
        return transactionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Transaction not found"));
    }

    @Override
    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public TransactionEntity findByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new CustomException("Invalid transaction identifier");
        }
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException("Transaction not found"));
    }

    @Override
    public List<TransactionEntity> findByFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByFromAddress(address);
    }

    @Override
    public List<TransactionEntity> findByToAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByToAddress(address);
    }

    @Override
    public List<TransactionEntity> findByStatus(TransactionStatus status) {
        if (status == null) {
            throw new CustomException("Invalid status");
        }
        return transactionRepository.findAllByStatus(status);
    }

    @Override
    public List<TransactionEntity> findByNetwork(PaymentNetwork network) {
        if (network == null) {
            throw new CustomException("Invalid payment network");
        }
        return transactionRepository.findAllByNetwork(network);
    }

    @Override
    public List<TransactionEntity> findFlaggedAsFraud() {
        return transactionRepository.findAllFlaggedAsFraud();
    }

    @Override
    @Transactional
    public TransactionEntity createTransaction(TransactionEntity transactionEntity) {
        return transactionCreationTimer.record(() -> {
            if (transactionEntity.getFromAddress().equals(transactionEntity.getToAddress())) {
                throw new CustomException("From address and to address cannot be the same");
            }

            transactionEntity.setStatus(TransactionStatus.PENDING);
            TransactionEntity savedTransaction = transactionRepository.save(transactionEntity);

            transactionCreatedCounter.increment();
            transactionAmountSummary.record(savedTransaction.getAmount().doubleValue());
            meterRegistry.counter("transactions.created.by.network",
                    "network", savedTransaction.getNetwork().name()).increment();
            pendingTransactionCount.incrementAndGet();

            try {
                TransactionEvent event = transactionMapper.toEvent(savedTransaction);
                event.setEventType("TRANSACTION_CREATED");
                event.setEventTimestamp(Instant.now());
                eventProducer.sendTransactionCreatedEvent(event);
            } catch (Exception e) {
                log.error("Failed to publish transaction created event for transactionId: {}",
                         savedTransaction.getTransactionId(), e);
            }

            return savedTransaction;
        });
    }

    @Override
    @Transactional
    public TransactionEntity updateTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity == null || transactionEntity.getId() == null) {
            throw new CustomException("Transaction identifier is required");
        }

        TransactionEntity existingTransaction = findById(transactionEntity.getId());

        if (transactionEntity.getStatus() != null) {
            existingTransaction.setStatus(transactionEntity.getStatus());
        }
        if (transactionEntity.getFlaggedAsFraud() != null) {
            existingTransaction.setFlaggedAsFraud(transactionEntity.getFlaggedAsFraud());
        }
        if (transactionEntity.getRiskScore() != null) {
            existingTransaction.setRiskScore(transactionEntity.getRiskScore());
        }
        if (transactionEntity.getFromLabel() != null) {
            existingTransaction.setFromLabel(transactionEntity.getFromLabel());
        }
        if (transactionEntity.getToLabel() != null) {
            existingTransaction.setToLabel(transactionEntity.getToLabel());
        }

        TransactionEntity updatedTransaction = transactionRepository.save(existingTransaction);

        try {
            TransactionEvent event = transactionMapper.toEvent(updatedTransaction);
            event.setEventType("TRANSACTION_UPDATED");
            event.setEventTimestamp(Instant.now());
            eventProducer.sendTransactionUpdatedEvent(event);
        } catch (Exception e) {
            log.error("Failed to publish transaction updated event for transactionId: {}",
                     updatedTransaction.getTransactionId(), e);
        }

        return updatedTransaction;
    }

    @Override
    @Transactional
    public TransactionEntity updateTransactionFraudDetails(
            Long transactionId,
            Boolean flaggedAsFraud,
            BigDecimal riskScore,
            List<String> triggeredRuleNames,
            Instant detectedAt) {

        fraudAlertReceivedCounter.increment();

        if (transactionId == null || transactionId <= 0) {
            throw new CustomException("Invalid transaction identifier");
        }

        TransactionEntity existingTransaction = findById(transactionId);

        existingTransaction.setFlaggedAsFraud(flaggedAsFraud);
        existingTransaction.setRiskScore(riskScore);

        if (flaggedAsFraud != null && flaggedAsFraud) {
            fraudDetectedCounter.increment();
        }

        if (riskScore != null) {
            riskScoreSummary.record(riskScore.doubleValue());
        }

        if (existingTransaction.getStatus() == TransactionStatus.PENDING) {
            pendingTransactionCount.decrementAndGet();
        }

        existingTransaction.clearViolations();

        if (triggeredRuleNames != null && !triggeredRuleNames.isEmpty()) {
            for (String ruleName : triggeredRuleNames) {
                FraudRule rule = FraudRule.fromName(ruleName);

                if (rule == null) {
                    log.warn("Unknown fraud rule name received: {}. Skipping.", ruleName);
                    continue;
                }

                meterRegistry.counter("fraud.rules.violated",
                        "rule", ruleName).increment();

                FraudRuleViolationEntity violation = FraudRuleViolationEntity.builder()
                        .ruleName(rule)
                        .ruleDescription(rule.getDescription())
                        .detailMessage(null)
                        .detectedAt(detectedAt != null ? detectedAt : Instant.now())
                        .build();

                existingTransaction.addViolation(violation);
            }

            log.info("Added {} fraud rule violations to transaction {}",
                    existingTransaction.getFraudViolations().size(),
                    transactionId);
        }

        return updateTransaction(existingTransaction);
    }
}
