package com.devalvesg.fraud_analysis_service.adapters.persistence;

import com.devalvesg.fraud_analysis_service.domain.models.entities.BlacklistedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistedAddressRepository extends JpaRepository<BlacklistedAddress, Long> {
    boolean existsByAddress(String address);
    Optional<BlacklistedAddress> findByAddress(String address);
}
