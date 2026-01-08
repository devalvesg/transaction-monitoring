package com.devalvesg.fraud_analysis_service.application.dto;

import com.devalvesg.fraud_analysis_service.domain.models.enums.FraudRule;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class FraudAnalysisResult {

    private Long transactionId;
    private String transactionUuid;
    private BigDecimal riskScore;
    private Boolean flaggedAsFraud;
    private List<FraudRule> triggeredRules;
    private Map<FraudRule, String> ruleDetails;
    private Instant analyzedAt;
}
