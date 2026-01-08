package com.devalvesg.transaction_service.adapters.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {

    private String eventType;
    private Instant eventTimestamp;
    private Long transactionId;
    private String transactionUuid;
    private Boolean flaggedAsFraud;
    private BigDecimal riskScore;
    private List<String> triggeredRules;
    private String fraudReason;
}