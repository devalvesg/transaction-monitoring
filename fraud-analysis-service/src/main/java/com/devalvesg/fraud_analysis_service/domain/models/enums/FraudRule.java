package com.devalvesg.fraud_analysis_service.domain.models.enums;

import lombok.Getter;

@Getter
public enum FraudRule {
    HIGH_VALUE_AT_NIGHT("High value transaction during night hours", 40),
    NEW_ADDRESS_HIGH_VALUE("New address sending high value", 35),
    FIRST_TIME_TRANSACTION("First transaction from address", 15),
    SUSPICIOUS_LABELS("Suspicious label keywords detected", 60),
    RANDOM_OR_MEANINGLESS_LABEL("Random or meaningless label", 25),
    ADDRESS_IN_BLACKLIST("Address found in blacklist", 100),
    PENDING_TOO_LONG("Transaction pending for excessive time", 20),
    SAME_FROM_AND_TO_ADDRESS("Sender and recipient are identical", 50),

    // History-dependent rules (Phase 2 - not implemented yet)
    VALUE_ANOMALY_FROM_HISTORY("Value significantly different from historical average", 50),
    STRUCTURED_SMALL_TRANSFERS("Smurfing pattern detected - structured small transfers", 65),
    BURSTING_MULTIPLE_TRANSACTIONS("Multiple transactions in short time window", 45),
    TRANSFER_TO_UNUSUAL_RECIPIENT("Transfer to new or unusual recipient", 35),
    UNUSUAL_OPERATING_HOURS("Operating outside normal hours for this address", 30);

    private final String description;
    private final int defaultWeight;

    FraudRule(String description, int defaultWeight) {
        this.description = description;
        this.defaultWeight = defaultWeight;
    }
}
