package com.devalvesg.fraud_analysis_service.application.services;

import com.devalvesg.fraud_analysis_service.adapters.dto.TransactionEvent;
import com.devalvesg.fraud_analysis_service.adapters.persistence.BlacklistedAddressRepository;
import com.devalvesg.fraud_analysis_service.adapters.persistence.TransactionRepository;
import com.devalvesg.fraud_analysis_service.application.config.FraudDetectionProperties;
import com.devalvesg.fraud_analysis_service.application.dto.FraudAnalysisResult;
import com.devalvesg.fraud_analysis_service.domain.models.entities.BlacklistedAddress;
import com.devalvesg.fraud_analysis_service.domain.models.entities.TransactionEntity;
import com.devalvesg.fraud_analysis_service.domain.models.enums.FraudRule;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class FraudAnalysisService {

    private final TransactionRepository transactionRepository;
    private final BlacklistedAddressRepository blackListRepository;
    private final BlacklistService blacklistService;
    private final FraudRuleEngine ruleEngine;
    private final FraudDetectionProperties properties;
    private final Counter fraudAnalysisCompletedCounter;
    private final Counter fraudDetectedCounter;
    private final Counter cleanTransactionsCounter;
    private final Timer fraudAnalysisTimer;
    private final DistributionSummary riskScoreDistribution;
    private final Counter blacklistAddCounter;
    private final AtomicInteger blacklistSizeHolder;
    private final MeterRegistry meterRegistry;
    private final Map<String, Long> blacklistedAddressesMap;

    public FraudAnalysisService(
            TransactionRepository transactionRepository,
            BlacklistedAddressRepository blackListRepository,
            BlacklistService blacklistService,
            FraudRuleEngine ruleEngine,
            FraudDetectionProperties properties,
            Counter fraudAnalysisCompletedCounter,
            Counter fraudDetectedCounter,
            Counter cleanTransactionsCounter,
            Timer fraudAnalysisTimer,
            DistributionSummary riskScoreDistribution,
            Counter blacklistAddCounter,
            AtomicInteger blacklistSizeHolder,
            MeterRegistry meterRegistry,
            Map<String, Long> blacklistedAddressesMap) {
        this.transactionRepository = transactionRepository;
        this.blackListRepository = blackListRepository;
        this.blacklistService = blacklistService;
        this.ruleEngine = ruleEngine;
        this.properties = properties;
        this.fraudAnalysisCompletedCounter = fraudAnalysisCompletedCounter;
        this.fraudDetectedCounter = fraudDetectedCounter;
        this.cleanTransactionsCounter = cleanTransactionsCounter;
        this.fraudAnalysisTimer = fraudAnalysisTimer;
        this.riskScoreDistribution = riskScoreDistribution;
        this.blacklistAddCounter = blacklistAddCounter;
        this.blacklistSizeHolder = blacklistSizeHolder;
        this.meterRegistry = meterRegistry;
        this.blacklistedAddressesMap = blacklistedAddressesMap;
    }

    /**
     * Main method to analyze a transaction for fraud
     * This is stateless - results are returned, not persisted
     *
     * @param event Transaction event from Kafka
     * @return Fraud analysis result with risk score and triggered rules
     */
    @Transactional
    public FraudAnalysisResult analyzeTransaction(TransactionEvent event) {
        return fraudAnalysisTimer.record(() -> {
            log.debug("Starting fraud analysis for transaction: {}", event.getTransactionId());

            TransactionEntity transaction = saveTransactionLocally(event);

            int txCount = transactionRepository.countByFromAddress(event.getFromAddress());
            log.debug("Address {} has {} total transactions", event.getFromAddress(), txCount);

            Map<FraudRule, String> violations = new HashMap<>();
            evaluateAllRules(event, txCount, violations);

            for (FraudRule rule : violations.keySet()) {
                meterRegistry.counter("fraud.rules.triggered",
                        "rule", rule.name(),
                        "network", event.getNetwork().name()).increment();
            }

            int totalScore = violations.keySet().stream()
                    .mapToInt(FraudRule::getDefaultWeight)
                    .sum();

            if (totalScore > 100) {
                if (!blacklistService.isBlacklisted(transaction.getFromAddress())) {
                    BlacklistedAddress blacklistedAddress = BlacklistedAddress.builder()
                            .addedAt(Instant.now())
                            .address(transaction.getFromAddress())
                            .reason("The minimum score limit has been exceeded")
                            .build();

                    BlacklistedAddress saved = blackListRepository.save(blacklistedAddress);

                    blacklistedAddressesMap.put(saved.getAddress(), saved.getAddedAt().getEpochSecond());
                    io.micrometer.core.instrument.Gauge.builder("blacklist_address_timestamp", blacklistedAddressesMap,
                            map -> map.getOrDefault(saved.getAddress(), 0L))
                            .tag("address", saved.getAddress())
                            .tag("reason", saved.getReason() != null ? saved.getReason() : "unknown")
                            .description("Blacklisted address with timestamp")
                            .register(meterRegistry);

                    blacklistAddCounter.increment();
                    blacklistSizeHolder.incrementAndGet();

                    log.info("Address {} auto-blacklisted with score {}", saved.getAddress(), totalScore);
                }
            }

            BigDecimal riskScore = calculateRiskScore(violations.keySet());
            riskScoreDistribution.record(riskScore.doubleValue());

            boolean isFraud = riskScore.compareTo(new BigDecimal(properties.getFraudScoreThreshold())) >= 0;

            fraudAnalysisCompletedCounter.increment();
            if (isFraud) {
                fraudDetectedCounter.increment();
                meterRegistry.counter("fraud.detected.by.network",
                        "network", event.getNetwork().name()).increment();
            } else {
                cleanTransactionsCounter.increment();
            }

            log.info("Fraud analysis completed for {}: fraud={}, score={}, rules={}",
                    event.getTransactionId(), isFraud, riskScore, violations.size());

            return FraudAnalysisResult.builder()
                    .transactionId(event.getId())
                    .transactionUuid(event.getTransactionId())
                    .riskScore(riskScore)
                    .flaggedAsFraud(isFraud)
                    .triggeredRules(new ArrayList<>(violations.keySet()))
                    .ruleDetails(violations)
                    .analyzedAt(Instant.now())
                    .build();
        });
    }

    private TransactionEntity saveTransactionLocally(TransactionEvent event) {
        Optional<TransactionEntity> existing = transactionRepository.findByTransactionId(event.getTransactionId());
        if (existing.isPresent()) {
            log.debug("Transaction {} already exists in local database", event.getTransactionId());
            return existing.get();
        }

        TransactionEntity transaction = TransactionEntity.builder()
                .transactionId(event.getTransactionId())
                .network(event.getNetwork())
                .networkVersion(event.getNetworkVersion())
                .fromAddress(event.getFromAddress())
                .toAddress(event.getToAddress())
                .fromLabel(event.getFromLabel())
                .toLabel(event.getToLabel())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .realizedAt(event.getRealizedAt())
                .status(event.getStatus())
                .type(event.getType())
                .flaggedAsFraud(event.getFlaggedAsFraud())
                .riskScore(event.getRiskScore())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();

        TransactionEntity saved = transactionRepository.save(transaction);
        log.debug("Transaction {} saved to local database with ID {}", saved.getTransactionId(), saved.getId());
        return saved;
    }

    private void evaluateAllRules(TransactionEvent event, int txCount, Map<FraudRule, String> violations) {
        // Rule 1: HIGH_VALUE_AT_NIGHT
        ruleEngine.checkHighValueAtNight(event).ifPresent(reason ->
                violations.put(FraudRule.HIGH_VALUE_AT_NIGHT, reason));

        // Rule 2: NEW_ADDRESS_HIGH_VALUE
        ruleEngine.checkNewAddressHighValue(event, txCount).ifPresent(reason ->
                violations.put(FraudRule.NEW_ADDRESS_HIGH_VALUE, reason));

        // Rule 6: FIRST_TIME_TRANSACTION
        ruleEngine.checkFirstTimeTransaction(txCount).ifPresent(reason ->
                violations.put(FraudRule.FIRST_TIME_TRANSACTION, reason));

        // Rule 8: SUSPICIOUS_LABELS
        ruleEngine.checkSuspiciousLabels(event).ifPresent(reason ->
                violations.put(FraudRule.SUSPICIOUS_LABELS, reason));

        // Rule 9: RANDOM_OR_MEANINGLESS_LABEL
        ruleEngine.checkRandomOrMeaninglessLabel(event).ifPresent(reason ->
                violations.put(FraudRule.RANDOM_OR_MEANINGLESS_LABEL, reason));

        // Rule 10: ADDRESS_IN_BLACKLIST
        ruleEngine.checkAddressInBlacklist(event.getFromAddress(), event.getToAddress()).ifPresent(reason ->
                violations.put(FraudRule.ADDRESS_IN_BLACKLIST, reason));

        // Rule 12: PENDING_TOO_LONG
        ruleEngine.checkPendingTooLong(event).ifPresent(reason ->
                violations.put(FraudRule.PENDING_TOO_LONG, reason));

        log.debug("Rule evaluation complete: {} violations detected", violations.size());
    }

    private BigDecimal calculateRiskScore(Set<FraudRule> triggeredRules) {
        if (triggeredRules.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int totalScore = triggeredRules.stream()
                .mapToInt(rule -> properties.getRuleWeight(rule.name()))
                .sum();

        BigDecimal score = new BigDecimal(totalScore);

        BigDecimal maxScore = new BigDecimal("999.99");
        if (score.compareTo(maxScore) > 0) {
            log.warn("Risk score {} exceeds maximum, capping at {}", score, maxScore);
            return maxScore;
        }

        return score;
    }
}
