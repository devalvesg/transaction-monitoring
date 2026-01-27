package com.devalvesg.fraud_analysis_service.adapters.controllers;

import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistRequest;
import com.devalvesg.fraud_analysis_service.adapters.dto.BlacklistResponse;
import com.devalvesg.fraud_analysis_service.application.services.BlacklistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blacklist")
@RequiredArgsConstructor
@Slf4j
public class BlacklistController {

    private final BlacklistService blacklistService;

    @PostMapping
    public ResponseEntity<BlacklistResponse> addToBlacklist(@Valid @RequestBody BlacklistRequest request) {
        log.info("POST /blacklist - Adding address: {}", request.getAddress());
        BlacklistResponse response = blacklistService.addToBlacklist(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BlacklistResponse>> getAllBlacklisted() {
        log.info("GET /blacklist - Retrieving all blacklisted addresses");
        List<BlacklistResponse> responses = blacklistService.getAllBlacklisted();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlacklistResponse> getById(@PathVariable Long id) {
        log.info("GET /blacklist/{} - Retrieving blacklisted address", id);
        return blacklistService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isBlacklisted(@RequestParam String address) {
        log.info("GET /blacklist/check?address={} - Checking if address is blacklisted", address);
        boolean isBlacklisted = blacklistService.isBlacklisted(address);
        return ResponseEntity.ok(isBlacklisted);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromBlacklist(@PathVariable Long id) {
        log.info("DELETE /blacklist/{} - Removing address from blacklist", id);
        boolean removed = blacklistService.removeFromBlacklist(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
