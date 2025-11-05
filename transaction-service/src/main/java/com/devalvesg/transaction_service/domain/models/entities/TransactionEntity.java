package com.devalvesg.transaction_service.domain.models.entities;

import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import com.devalvesg.transaction_service.domain.models.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_network", columnList = "network"),
        @Index(name = "idx_transaction_id", columnList = "transactionId"),
        @Index(name = "idx_realized_at", columnList = "realizedAt"),
        @Index(name = "idx_from_address", columnList = "fromAddress"),
        @Index(name = "idx_to_address", columnList = "toAddress"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_flagged", columnList = "flaggedAsFraud")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 128)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PaymentNetwork network;

    @Column(length = 20)
    private String networkVersion;

    @Column(nullable = false, length = 256)
    private String fromAddress;

    @Column(nullable = false, length = 256)
    private String toAddress;

    @Column(length = 256)
    private String fromLabel;

    @Column(length = 256)
    private String toLabel;

    @Column(nullable = false, precision = 36, scale = 18)
    private BigDecimal amount;

    @Column(nullable = false, length = 20)
    private String currency;

    @Column(nullable = false)
    private Instant realizedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TransactionType type;

    @Column(nullable = false)
    @Builder.Default
    private Boolean flaggedAsFraud = false;

    @Column(precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
