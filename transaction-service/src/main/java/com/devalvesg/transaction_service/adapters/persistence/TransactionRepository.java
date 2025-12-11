package com.devalvesg.transaction_service.adapters.persistence;

import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> findAllByToAddress(String toAddress);

    List<TransactionEntity> findAllByFromAddress(String fromAddress);

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    List<TransactionEntity> findAllByStatus(TransactionStatus status);

    List<TransactionEntity> findAllByNetwork(PaymentNetwork network);

    @Query("SELECT t FROM TransactionEntity t WHERE t.flaggedAsFraud = true")
    List<TransactionEntity> findAllFlaggedAsFraud();
}
