package com.devalvesg.transaction_service.domain.models.enums;

public enum PaymentNetwork {
    // Blockchains Principais
    BITCOIN("Bitcoin", true, "BTC"),
    ETHEREUM("Ethereum", true, "ETH"),
    BINANCE_SMART_CHAIN("Binance Smart Chain", true, "BNB"),
    POLYGON("Polygon", true, "MATIC"),
    SOLANA("Solana", true, "SOL"),
    AVALANCHE("Avalanche", true, "AVAX"),
    CARDANO("Cardano", true, "ADA"),
    POLKADOT("Polkadot", true, "DOT"),
    COSMOS("Cosmos", true, "ATOM"),
    TRON("Tron", true, "TRX"),

    // Layer 2
    ARBITRUM("Arbitrum", true, "ETH"),
    OPTIMISM("Optimism", true, "ETH"),
    BASE("Base", true, "ETH"),

    // Stablecoins Networks
    STELLAR("Stellar", true, "XLM"),
    RIPPLE("Ripple", true, "XRP"),

    // Sistemas Tradicionais (preparado para futuro)
    PIX("PIX Brasil", false, "BRL"),
    SWIFT("SWIFT Network", false, null),
    SEPA("SEPA Europe", false, "EUR"),
    FEDWIRE("Fedwire US", false, "USD"),
    ACH("ACH US", false, "USD"),

    // Carteiras e Exchanges
    LIGHTNING_NETWORK("Lightning Network", true, "BTC"),

    // Outros
    OTHER("Other", false, null);

    private final String displayName;
    private final boolean blockchain;
    private final String nativeCurrency;

    PaymentNetwork(String displayName, boolean blockchain, String nativeCurrency) {
        this.displayName = displayName;
        this.blockchain = blockchain;
        this.nativeCurrency = nativeCurrency;
    }

    public String getDisplayName() { return displayName; }
    public boolean isBlockchain() { return blockchain; }
    public String getNativeCurrency() { return nativeCurrency; }
}
