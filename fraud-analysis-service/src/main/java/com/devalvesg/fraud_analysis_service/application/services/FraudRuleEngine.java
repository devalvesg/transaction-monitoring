package com.devalvesg.fraud_analysis_service.application.services;

import com.devalvesg.fraud_analysis_service.adapters.dto.TransactionEvent;
import com.devalvesg.fraud_analysis_service.adapters.persistence.BlacklistedAddressRepository;
import com.devalvesg.fraud_analysis_service.application.config.FraudDetectionProperties;
import com.devalvesg.fraud_analysis_service.domain.models.enums.TransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class FraudRuleEngine {

    private final FraudDetectionProperties properties;
    private final BlacklistedAddressRepository blacklistedAddressRepository;

    private static final Set<String> SUSPICIOUS_KEYWORDS = new HashSet<>(Arrays.asList(
            "mix", "mixer", "bot", "tmp", "temp", "scam", "scammer", "hack", "hacker",
            "phish", "phishing", "fraud", "stolen", "launder", "anonymous", "anon"
    ));

    private static final Pattern MEANINGLESS_PATTERN = Pattern.compile(
            "^(aaa+|xxx+|test|tmp|temp|user\\d+|wallet\\d+|address\\d+)$",
            Pattern.CASE_INSENSITIVE
    );

    public Optional<String> checkHighValueAtNight(TransactionEvent event) {
        if (event.getAmount().compareTo(properties.getHighValueThreshold()) < 0) {
            return Optional.empty();
        }

        int hour = event.getRealizedAt().atZone(ZoneOffset.UTC).getHour();
        if (hour >= properties.getNightHourStart() && hour < properties.getNightHourEnd()) {
            return Optional.of(String.format(
                    "High value transaction (%s %s) during night hours (%02d:00 UTC)",
                    event.getAmount(), event.getCurrency(), hour
            ));
        }

        return Optional.empty();
    }

    public Optional<String> checkNewAddressHighValue(TransactionEvent event, int transactionCount) {
        if (transactionCount >= properties.getNewAddressTransactionThreshold()) {
            return Optional.empty();
        }

        if (event.getAmount().compareTo(properties.getHighValueThreshold()) >= 0) {
            return Optional.of(String.format(
                    "New address (%d transactions) sending high value (%s %s)",
                    transactionCount, event.getAmount(), event.getCurrency()
            ));
        }

        return Optional.empty();
    }

    public Optional<String> checkFirstTimeTransaction(int transactionCount) {
        if (transactionCount == 1) {
            return Optional.of("First transaction from this address");
        }
        return Optional.empty();
    }

    public Optional<String> checkSuspiciousLabels(TransactionEvent event) {
        StringBuilder suspiciousLabels = new StringBuilder();

        if (event.getFromLabel() != null && containsSuspiciousKeyword(event.getFromLabel())) {
            suspiciousLabels.append("From label: '").append(event.getFromLabel()).append("' ");
        }

        if (event.getToLabel() != null && containsSuspiciousKeyword(event.getToLabel())) {
            suspiciousLabels.append("To label: '").append(event.getToLabel()).append("'");
        }

        if (!suspiciousLabels.isEmpty()) {
            return Optional.of("Suspicious keywords detected in labels - " + suspiciousLabels.toString().trim());
        }

        return Optional.empty();
    }

    public Optional<String> checkRandomOrMeaninglessLabel(TransactionEvent event) {
        StringBuilder issues = new StringBuilder();

        if (event.getFromLabel() != null && isMeaninglessLabel(event.getFromLabel())) {
            issues.append("From label: '").append(event.getFromLabel()).append("' ");
        }

        if (event.getToLabel() != null && isMeaninglessLabel(event.getToLabel())) {
            issues.append("To label: '").append(event.getToLabel()).append("'");
        }

        if (!issues.isEmpty()) {
            return Optional.of("Meaningless or random labels detected - " + issues.toString().trim());
        }

        return Optional.empty();
    }

    public Optional<String> checkAddressInBlacklist(String fromAddress, String toAddress) {
        boolean fromBlacklisted = blacklistedAddressRepository.existsByAddress(fromAddress);
        boolean toBlacklisted = blacklistedAddressRepository.existsByAddress(toAddress);

        if (fromBlacklisted && toBlacklisted) {
            return Optional.of("Both sender and recipient addresses are blacklisted");
        } else if (fromBlacklisted) {
            return Optional.of("Sender address is blacklisted");
        } else if (toBlacklisted) {
            return Optional.of("Recipient address is blacklisted");
        }

        return Optional.empty();
    }

    public Optional<String> checkPendingTooLong(TransactionEvent event) {
        if (event.getStatus() != TransactionStatus.PENDING) {
            return Optional.empty();
        }

        Duration pendingDuration = Duration.between(event.getCreatedAt(), Instant.now());
        long pendingHours = pendingDuration.toHours();

        if (pendingHours > properties.getPendingTimeoutHours()) {
            return Optional.of(String.format(
                    "Transaction pending for %d hours (threshold: %d hours)",
                    pendingHours, properties.getPendingTimeoutHours()
            ));
        }

        return Optional.empty();
    }

    private boolean containsSuspiciousKeyword(String label) {
        String lowerLabel = label.toLowerCase();
        return SUSPICIOUS_KEYWORDS.stream().anyMatch(lowerLabel::contains);
    }

    private boolean isMeaninglessLabel(String label) {
        if (label.length() < 3) {
            return true;
        }

        if (MEANINGLESS_PATTERN.matcher(label).matches()) {
            return true;
        }

        return label.chars().distinct().count() == 1;
    }
}
