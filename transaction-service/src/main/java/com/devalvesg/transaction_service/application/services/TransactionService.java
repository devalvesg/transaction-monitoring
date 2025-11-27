package com.devalvesg.transaction_service.application.services;

import com.devalvesg.transaction_service.adapters.persistence.TransactionRepository;
import com.devalvesg.transaction_service.domain.contracts.ITransactionService;
import com.devalvesg.transaction_service.domain.exceptions.CustomException;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository repository) {
        this.transactionRepository = repository;
    }

    @Override
    public TransactionEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new CustomException("Invalid identifier");
        }
        return transactionRepository.findById(id)
                .orElseThrow(() -> new CustomException("Transaction not found"));
    }

    @Override
    public List<TransactionEntity> findAll() {
        return transactionRepository.findAll();
    }

    @Override
    public TransactionEntity findByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new CustomException("Invalid transaction identifier");
        }
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException("Transaction not found"));
    }

    @Override
    public List<TransactionEntity> findByFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByFromAddress(address);
    }

    @Override
    public List<TransactionEntity> findByToAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByToAddress(address);
    }

    @Override
    public List<TransactionEntity> findByStatus(TransactionStatus status) {
        if (status == null) {
            throw new CustomException("Invalid status");
        }
        return transactionRepository.findAllByStatus(status);
    }

    @Override
    public List<TransactionEntity> findByNetwork(PaymentNetwork network) {
        if (network == null) {
            throw new CustomException("Invalid payment network");
        }
        return transactionRepository.findAllByNetwork(network);
    }

    @Override
    public List<TransactionEntity> findFlaggedAsFraud() {
        return transactionRepository.findAllFlaggedAsFraud();
    }

    @Override
    @Transactional
    public TransactionEntity createTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity.getFromAddress().equals(transactionEntity.getToAddress())) {
            throw new CustomException("From address and to address cannot be the same");
        }

        transactionEntity.setStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transactionEntity);
    }

    @Override
    @Transactional
    public TransactionEntity updateTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity == null || transactionEntity.getId() == null) {
            throw new CustomException("Transaction identifier is required");
        }

        TransactionEntity existingTransaction = findById(transactionEntity.getId());

        if (transactionEntity.getStatus() != null) {
            existingTransaction.setStatus(transactionEntity.getStatus());
        }
        if (transactionEntity.getFlaggedAsFraud() != null) {
            existingTransaction.setFlaggedAsFraud(transactionEntity.getFlaggedAsFraud());
        }
        if (transactionEntity.getRiskScore() != null) {
            existingTransaction.setRiskScore(transactionEntity.getRiskScore());
        }
        if (transactionEntity.getFromLabel() != null) {
            existingTransaction.setFromLabel(transactionEntity.getFromLabel());
        }
        if (transactionEntity.getToLabel() != null) {
            existingTransaction.setToLabel(transactionEntity.getToLabel());
        }

        return transactionRepository.save(existingTransaction);
    }
}
