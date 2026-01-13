-- Create fraud_rule_violations table to store detailed fraud detection results
CREATE TABLE IF NOT EXISTS fraud_rule_violations (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    rule_name VARCHAR(100) NOT NULL,
    rule_description VARCHAR(500) NOT NULL,
    detail_message TEXT,
    detected_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_violation_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES transactions(id)
        ON DELETE CASCADE
);

-- Create indexes for query performance
CREATE INDEX IF NOT EXISTS idx_violation_transaction_id
    ON fraud_rule_violations(transaction_id);

CREATE INDEX IF NOT EXISTS idx_violation_rule_name
    ON fraud_rule_violations(rule_name);

CREATE INDEX IF NOT EXISTS idx_violation_detected_at
    ON fraud_rule_violations(detected_at);

CREATE INDEX IF NOT EXISTS idx_violation_transaction_rule
    ON fraud_rule_violations(transaction_id, rule_name);
