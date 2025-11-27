package com.devalvesg.transaction_service.application.services;

import com.devalvesg.transaction_service.adapters.persistence.TransactionRepository;
import com.devalvesg.transaction_service.domain.contracts.ITransactionService;
import com.devalvesg.transaction_service.domain.exceptions.CustomException;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import org.springframework.stereotype.Service;

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

    public TransactionEntity findByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new CustomException("Invalid transaction identifier");
        }
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new CustomException("Transaction not found"));
    }

    public List<TransactionEntity> findByFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByFromAddress(address);
    }

    public List<TransactionEntity> findByToAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new CustomException("Invalid address");
        }
        return transactionRepository.findAllByToAddress(address);
    }

    public List<TransactionEntity> findByStatus(TransactionStatus status) {
        if (status == null) {
            throw new CustomException("Invalid status");
        }
        return transactionRepository.findAllByStatus(status);
    }

    public List<TransactionEntity> findByNetwork(PaymentNetwork network) {
        if (network == null) {
            throw new CustomException("Invalid payment network");
        }
        return transactionRepository.findAllByNetwork(network);
    }

    public List<TransactionEntity> findFlaggedAsFraud() {
        return transactionRepository.findAllFlaggedAsFraud();
    }

    @Override
    public TransactionEntity createTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity.getFromAddress().equals(transactionEntity.getToAddress())) {
            throw new CustomException("From address and to address cannot be the same");
        }

        transactionEntity.setFlaggedAsFraud(null);
        transactionEntity.setRiskScore(null);

            transactionEntity.setStatus(TransactionStatus.PENDING);

        return transactionRepository.save(transactionEntity);
    }

    @Override
    public TransactionEntity updateTransaction(TransactionEntity transactionEntity) {
        if (transactionEntity == null || transactionEntity.getId() == null) {
            throw new CustomException("Transaction identifier is required");
        }

        // Verificar se transação existe
        TransactionEntity existingTransaction = findById(transactionEntity.getId());

        // Permitir atualização de campos específicos
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

    public void deleteTransaction(Long id) {
        if (id == null || id <= 0) {
            throw new CustomException("Invalid identifier");
        }

        if (!transactionRepository.existsById(id)) {
            throw new CustomException("Transaction not found");
        }

        transactionRepository.deleteById(id);
    }
}
