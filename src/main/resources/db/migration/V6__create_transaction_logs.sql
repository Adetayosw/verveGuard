CREATE TABLE IF NOT EXISTS transaction_logs(
    id BIGSERIAL PRIMARY KEY,
    card_number_hash VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4),
    merchant_id VARCHAR(100),
    amount NUMERIC(19,4),
    ip_address VARCHAR(45),
    status VARCHAR(20),
    fraud_reason VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)