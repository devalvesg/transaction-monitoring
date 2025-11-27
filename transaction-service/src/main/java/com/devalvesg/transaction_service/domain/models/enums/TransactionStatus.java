package com.devalvesg.transaction_service.domain.models.enums;

public enum TransactionStatus {
    PENDING,
    CONFIRMED,
    FAILED,
    REVERTED,
    UNDER_REVIEW,
    BLOCKED,
    CANCELLED
}