package com.devalvesg.transaction_service.domain.contracts;

import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;

import java.util.List;

public interface ITransactionService {
    public TransactionEntity findById(Long userId);
    public List<TransactionEntity> findAll();
    public TransactionEntity createTransaction(TransactionEntity transactionEntity);
    public TransactionEntity updateTransaction(TransactionEntity transactionEntity);
}
