package com.devalvesg.fraud_analysis_service.application.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "fraud-detection")
@Data
public class FraudDetectionProperties {

    private BigDecimal highValueThreshold = new BigDecimal("10000");
    private int nightHourStart = 0;
    private int nightHourEnd = 5;
    private int newAddressTransactionThreshold = 5;
    private long pendingTimeoutHours = 24;
    private int fraudScoreThreshold = 70;
    private Map<String, Integer> ruleWeights = defaultWeights();

    private Map<String, Integer> defaultWeights() {
        Map<String, Integer> weights = new HashMap<>();
        weights.put("HIGH_VALUE_AT_NIGHT", 40);
        weights.put("NEW_ADDRESS_HIGH_VALUE", 35);
        weights.put("FIRST_TIME_TRANSACTION", 15);
        weights.put("SUSPICIOUS_LABELS", 60);
        weights.put("RANDOM_OR_MEANINGLESS_LABEL", 25);
        weights.put("ADDRESS_IN_BLACKLIST", 100);
        weights.put("PENDING_TOO_LONG", 20);
        weights.put("SAME_FROM_AND_TO_ADDRESS", 50);
        return weights;
    }

    public int getRuleWeight(String ruleName) {
        return ruleWeights.getOrDefault(ruleName, 0);
    }
}
