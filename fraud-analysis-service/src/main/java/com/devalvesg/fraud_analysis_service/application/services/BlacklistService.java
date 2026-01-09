package com.devalvesg.fraud_analysis_service.application.services;

import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistRequest;
import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistResponse;
import com.devalvesg.fraud_analysis_service.adapters.persistence.BlacklistedAddressRepository;
import com.devalvesg.fraud_analysis_service.domain.models.entities.BlacklistedAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlacklistService {

    private final BlacklistedAddressRepository blacklistedAddressRepository;

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

        if (blacklistedAddressRepository.existsById(id)) {
            blacklistedAddressRepository.deleteById(id);
            log.info("Address removed from blacklist with ID {}", id);
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
