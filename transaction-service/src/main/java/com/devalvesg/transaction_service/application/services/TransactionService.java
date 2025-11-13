package com.devalvesg.transaction_service.application.services;

import com.devalvesg.transaction_service.adapters.persistence.TransactionRepository;
import com.devalvesg.transaction_service.domain.contracts.ITransactionService;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService implements ITransactionService {

    private TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository repository) {
        this.transactionRepository = repository;
    }

    @Override
    public List<TransactionEntity> findAllByUserId(Long userId) {
        return transactionRepository.findAllByUserId(userId);
    }

    @Override
    public TransactionEntity findById(Long id) {
        return transactionRepository.findById(id).orElseThrow(() -> new RuntimeException("Transaction not found"));
    }

    @Override
    public List<TransactionEntity> findAll() {
        return List.of();
    }

    @Override
    public TransactionEntity CreateTransaction(TransactionEntity transactionEntity) {
        return null;
    }

    @Override
    public TransactionEntity UpdateTransaction(TransactionEntity transactionEntity) {
        return null;
    }
}
