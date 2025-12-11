-- Create transactions table with proper nullable columns
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(128) NOT NULL UNIQUE,
    network VARCHAR(50) NOT NULL,
    network_version VARCHAR(20),
    from_address VARCHAR(256) NOT NULL,
    to_address VARCHAR(256) NOT NULL,
    from_label VARCHAR(256),
    to_label VARCHAR(256),
    amount NUMERIC(36, 18) NOT NULL,
    currency VARCHAR(20) NOT NULL,
    realized_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL,
    type VARCHAR(50),
    flagged_as_fraud BOOLEAN,
    risk_score NUMERIC(5, 2),
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_network ON transactions(network);
CREATE INDEX IF NOT EXISTS idx_transaction_id ON transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_realized_at ON transactions(realized_at);
CREATE INDEX IF NOT EXISTS idx_from_address ON transactions(from_address);
CREATE INDEX IF NOT EXISTS idx_to_address ON transactions(to_address);
CREATE INDEX IF NOT EXISTS idx_status ON transactions(status);
CREATE INDEX IF NOT EXISTS idx_flagged ON transactions(flagged_as_fraud);
