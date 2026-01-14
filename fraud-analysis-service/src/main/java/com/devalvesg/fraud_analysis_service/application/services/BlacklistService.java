package com.devalvesg.fraud_analysis_service.application.services;

import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistRequest;
import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistResponse;
import com.devalvesg.fraud_analysis_service.adapters.persistence.BlacklistedAddressRepository;
import com.devalvesg.fraud_analysis_service.domain.models.entities.BlacklistedAddress;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlacklistService {

    private final BlacklistedAddressRepository blacklistedAddressRepository;
    private final MeterRegistry meterRegistry;
    private final Map<String, Long> blacklistedAddressesMap;

    public BlacklistService(
            BlacklistedAddressRepository blacklistedAddressRepository,
            MeterRegistry meterRegistry,
            Map<String, Long> blacklistedAddressesMap) {
        this.blacklistedAddressRepository = blacklistedAddressRepository;
        this.meterRegistry = meterRegistry;
        this.blacklistedAddressesMap = blacklistedAddressesMap;
    }

    @PostConstruct
    public void initializeBlacklistMetrics() {
        log.info("Initializing blacklist metrics from database");
        List<BlacklistedAddress> addresses = blacklistedAddressRepository.findAll();
        for (BlacklistedAddress address : addresses) {
            registerBlacklistMetric(address);
        }
        log.info("Initialized {} blacklisted addresses in metrics", addresses.size());
    }

    private void registerBlacklistMetric(BlacklistedAddress address) {
        blacklistedAddressesMap.put(address.getAddress(), address.getAddedAt().getEpochSecond());

        Gauge.builder("blacklist_address_timestamp", blacklistedAddressesMap,
                map -> map.getOrDefault(address.getAddress(), 0L))
                .tag("address", address.getAddress())
                .tag("reason", address.getReason() != null ? address.getReason() : "unknown")
                .description("Blacklisted address with timestamp")
                .register(meterRegistry);

        log.debug("Registered blacklist metric for address: {}", address.getAddress());
    }

    private void removeBlacklistMetric(String address) {
        blacklistedAddressesMap.remove(address);
        meterRegistry.remove(
            Objects.requireNonNull(meterRegistry.find("blacklist_address_timestamp")
                            .tag("address", address)
                            .gauge())
                .getId()
        );
        log.debug("Removed blacklist metric for address: {}", address);
    }

    @Transactional
    public BlacklistResponse addToBlacklist(BlacklistRequest request) {
        log.info("Adding address to blacklist: {}", request.getAddress());

        if (blacklistedAddressRepository.existsByAddress(request.getAddress())) {
            log.warn("Address {} already exists in blacklist", request.getAddress());
            throw new IllegalArgumentException("Address already exists in blacklist");
        }

        BlacklistedAddress entity = BlacklistedAddress.builder()
                .address(request.getAddress())
                .reason(request.getReason())
                .addedAt(Instant.now())
                .build();

        BlacklistedAddress saved = blacklistedAddressRepository.save(entity);
        registerBlacklistMetric(saved);
        log.info("Address {} added to blacklist with ID {}", saved.getAddress(), saved.getId());

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BlacklistResponse> getAllBlacklisted() {
        log.debug("Retrieving all blacklisted addresses");
        return blacklistedAddressRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<BlacklistResponse> getById(Long id) {
        log.debug("Retrieving blacklisted address by ID: {}", id);
        return blacklistedAddressRepository.findById(id)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String address) {
        return blacklistedAddressRepository.existsByAddress(address);
    }

    @Transactional
    public boolean removeFromBlacklist(Long id) {
        log.info("Removing address from blacklist with ID: {}", id);

        Optional<BlacklistedAddress> addressOpt = blacklistedAddressRepository.findById(id);
        if (addressOpt.isPresent()) {
            String address = addressOpt.get().getAddress();
            blacklistedAddressRepository.deleteById(id);
            removeBlacklistMetric(address);
            log.info("Address {} removed from blacklist with ID {}", address, id);
            return true;
        }

        log.warn("Blacklist entry with ID {} not found", id);
        return false;
    }

    private BlacklistResponse toResponse(BlacklistedAddress entity) {
        return BlacklistResponse.builder()
                .id(entity.getId())
                .address(entity.getAddress())
                .reason(entity.getReason())
                .addedAt(entity.getAddedAt())
                .build();
    }
}
