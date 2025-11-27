package com.devalvesg.transaction_service.domain.models.enums;

public enum TransactionType {
    // Transferências básicas
    TRANSFER,

    // DeFi
    SWAP,
    LIQUIDITY_ADD,
    LIQUIDITY_REMOVE,
    STAKE,
    UNSTAKE,
    CLAIM_REWARDS,

    // NFTs
    NFT_MINT,
    NFT_TRANSFER,
    NFT_SALE,

    // Smart Contracts
    CONTRACT_DEPLOYMENT,
    CONTRACT_CALL,

    // Tokens
    TOKEN_MINT,
    TOKEN_BURN,
    TOKEN_APPROVAL,

    // Bridge/Cross-chain
    BRIDGE_TRANSFER,

    // Tradicionais
    PAYMENT,
    REFUND,
    WITHDRAWAL,
    DEPOSIT,

    // Outros
    OTHER
}
