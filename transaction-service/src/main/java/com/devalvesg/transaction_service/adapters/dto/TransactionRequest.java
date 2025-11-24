package com.devalvesg.transaction_service.adapters.dto;

import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import com.devalvesg.transaction_service.domain.models.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    private String transactionId;
    private PaymentNetwork network;
    private String networkVersion;
    private String fromAddress;
    private String toAddress;
    private String fromLabel;
    private String toLabel;
    private BigDecimal amount;
    private String currency;
    private Instant realizedAt;
    private TransactionStatus status;
    private TransactionType type;
}