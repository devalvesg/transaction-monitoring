package com.devalvesg.transaction_service.adapters.dto;

import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import com.devalvesg.transaction_service.domain.models.enums.TransactionType;
import jakarta.validation.constraints.*;
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

    @NotBlank(message = "Transaction ID is required")
    @Size(max = 128, message = "Transaction ID must not exceed 128 characters")
    private String transactionId;

    @NotNull(message = "Payment network is required")
    private PaymentNetwork network;

    @Size(max = 20, message = "Network version must not exceed 20 characters")
    private String networkVersion;

    @NotBlank(message = "From address is required")
    @Size(max = 256, message = "From address must not exceed 256 characters")
    private String fromAddress;

    @NotBlank(message = "To address is required")
    @Size(max = 256, message = "To address must not exceed 256 characters")
    private String toAddress;

    @Size(max = 256, message = "From label must not exceed 256 characters")
    private String fromLabel;

    @Size(max = 256, message = "To label must not exceed 256 characters")
    private String toLabel;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00000000000000001", inclusive = false, message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(max = 20, message = "Currency must not exceed 20 characters")
    private String currency;

    @NotNull(message = "Realized at is required")
    private Instant realizedAt;

    private TransactionStatus status;

    private TransactionType type;
}