package com.devalvesg.transaction_service.domain.models.entities;

import com.devalvesg.transaction_service.domain.models.enums.FraudRule;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "fraud_rule_violations", indexes = {
        @Index(name = "idx_violation_transaction_id", columnList = "transaction_id"),
        @Index(name = "idx_violation_rule_name", columnList = "ruleName"),
        @Index(name = "idx_violation_detected_at", columnList = "detectedAt"),
        @Index(name = "idx_violation_transaction_rule", columnList = "transaction_id,ruleName")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudRuleViolationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private TransactionEntity transaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_name", nullable = false, length = 100)
    private FraudRule ruleName;

    @Column(name = "rule_description", nullable = false, length = 500)
    private String ruleDescription;

    @Column(name = "detail_message", columnDefinition = "TEXT")
    private String detailMessage;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
