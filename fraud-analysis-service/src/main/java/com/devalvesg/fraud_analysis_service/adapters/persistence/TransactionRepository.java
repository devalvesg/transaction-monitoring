package com.devalvesg.fraud_analysis_service.adapters.persistence;

import com.devalvesg.fraud_analysis_service.domain.models.entities.TransactionEntity;
import com.devalvesg.fraud_analysis_service.domain.models.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    int countByFromAddress(String fromAddress);
    Optional<TransactionEntity> findByTransactionId(String transactionId);
    boolean existsByTransactionId(String transactionId);
}
