package com.devalvesg.transaction_service.adapters.controllers;

import com.devalvesg.transaction_service.domain.contracts.ITransactionService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transaction")
public class TransactionController {

    public TransactionController(ITransactionService transactionService) {
    }
}
