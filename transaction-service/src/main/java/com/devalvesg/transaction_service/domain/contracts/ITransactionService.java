package com.devalvesg.transaction_service.domain.contracts;

import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public interface ITransactionService {
    TransactionEntity findById(Long id);
    List<TransactionEntity> findAll();
    TransactionEntity findByTransactionId(String transactionId);
    List<TransactionEntity> findByFromAddress(String address);
    List<TransactionEntity> findByToAddress(String address);
    List<TransactionEntity> findByStatus(TransactionStatus status);
    List<TransactionEntity> findByNetwork(PaymentNetwork network);
    List<TransactionEntity> findFlaggedAsFraud();

    TransactionEntity createTransaction(TransactionEntity entity);
    TransactionEntity updateTransaction(TransactionEntity entity);
    TransactionEntity updateTransactionFraudDetails(
            Long transactionId,
            Boolean flaggedAsFraud,
            BigDecimal riskScore,
            List<String> triggeredRuleNames,
            Instant detectedAt
    );

}
