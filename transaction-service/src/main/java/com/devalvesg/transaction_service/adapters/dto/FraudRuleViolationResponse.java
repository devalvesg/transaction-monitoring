package com.devalvesg.transaction_service.adapters.dto;

import com.devalvesg.transaction_service.domain.models.enums.FraudRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudRuleViolationResponse {

    private Long id;
    private FraudRule ruleName;
    private String ruleDescription;
    private String detailMessage;
    private Instant detectedAt;
    private Instant createdAt;
}
