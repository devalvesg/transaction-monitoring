package com.devalvesg.transaction_service.adapters.persistence;

import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    public List<TransactionEntity> findAllByToAddress(String toAddress);
    public List<TransactionEntity> findAllByFromAddress(String fromAddress);
    public Optional<TransactionEntity> findByTransactionId(String transactionId);
}
