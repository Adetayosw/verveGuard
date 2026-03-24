CREATE TABLE IF NOT EXISTS flagged_attempts (
    id BIGSERIAL PRIMARY KEY,
    card_number_hash VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4),
    merchant_id VARCHAR(100) NOT NULL,
    amount NUMERIC(19,4),
    ip_address VARCHAR(45),
    fraud_signal VARCHAR(50),
    fraud_message VARCHAR(255),
    resolution VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);