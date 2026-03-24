CREATE TABLE IF NOT EXISTS card_spending_profile (
    id BIGSERIAL PRIMARY KEY,
    card_number_hash VARCHAR(255) NOT NULL UNIQUE,
    avg_transaction_amount NUMERIC(19,4),
    max_seen_amount NUMERIC(19,4),
    transaction_count INT DEFAULT 0,
    last_updated TIMESTAMP
)