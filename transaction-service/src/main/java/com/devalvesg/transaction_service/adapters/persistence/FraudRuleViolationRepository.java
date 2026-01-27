package com.devalvesg.transaction_service.adapters.persistence;

import com.devalvesg.transaction_service.domain.models.entities.FraudRuleViolationEntity;
import com.devalvesg.transaction_service.domain.models.enums.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudRuleViolationRepository extends JpaRepository<FraudRuleViolationEntity, Long> {

    List<FraudRuleViolationEntity> findByTransactionId(Long transactionId);

    List<FraudRuleViolationEntity> findByRuleName(FraudRule ruleName);

    void deleteByTransactionId(Long transactionId);
}
