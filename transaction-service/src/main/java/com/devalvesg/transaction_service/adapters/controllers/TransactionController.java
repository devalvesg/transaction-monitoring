package com.devalvesg.transaction_service.adapters.controllers;

import com.devalvesg.transaction_service.adapters.dto.TransactionRequest;
import com.devalvesg.transaction_service.adapters.dto.TransactionResponse;
import com.devalvesg.transaction_service.adapters.mappers.TransactionMapper;
import com.devalvesg.transaction_service.application.services.TransactionService;
import com.devalvesg.transaction_service.domain.models.entities.TransactionEntity;
import com.devalvesg.transaction_service.domain.models.enums.PaymentNetwork;
import com.devalvesg.transaction_service.domain.models.enums.TransactionStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    public TransactionController(TransactionService transactionService, TransactionMapper transactionMapper) {
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionEntity entity = transactionMapper.toEntity(request);
        TransactionEntity createdEntity = transactionService.createTransaction(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toResponse(createdEntity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable Long id) {
        TransactionEntity entity = transactionService.findById(id);
        return ResponseEntity.ok(transactionMapper.toResponse(entity));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionEntity> entities = transactionService.findAll();
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        TransactionEntity entity = transactionMapper.toEntity(request);
        entity.setId(id);
        TransactionEntity updatedEntity = transactionService.updateTransaction(entity);
        return ResponseEntity.ok(transactionMapper.toResponse(updatedEntity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/by-transaction-id/{transactionId}")
    public ResponseEntity<TransactionResponse> getByTransactionId(@PathVariable String transactionId) {
        TransactionEntity entity = transactionService.findByTransactionId(transactionId);
        return ResponseEntity.ok(transactionMapper.toResponse(entity));
    }

    @GetMapping("/search/by-from-address")
    public ResponseEntity<List<TransactionResponse>> getByFromAddress(@RequestParam String address) {
        List<TransactionEntity> entities = transactionService.findByFromAddress(address);
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }

    @GetMapping("/search/by-to-address")
    public ResponseEntity<List<TransactionResponse>> getByToAddress(@RequestParam String address) {
        List<TransactionEntity> entities = transactionService.findByToAddress(address);
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }

    @GetMapping("/search/by-status")
    public ResponseEntity<List<TransactionResponse>> getByStatus(@RequestParam TransactionStatus status) {
        List<TransactionEntity> entities = transactionService.findByStatus(status);
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }

    @GetMapping("/search/by-network")
    public ResponseEntity<List<TransactionResponse>> getByNetwork(@RequestParam PaymentNetwork network) {
        List<TransactionEntity> entities = transactionService.findByNetwork(network);
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }

    @GetMapping("/search/flagged-as-fraud")
    public ResponseEntity<List<TransactionResponse>> getFlaggedAsFraud() {
        List<TransactionEntity> entities = transactionService.findFlaggedAsFraud();
        return ResponseEntity.ok(entities.stream()
                .map(transactionMapper::toResponse)
                .toList());
    }
}
