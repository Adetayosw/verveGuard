CREATE TABLE IF NOT EXISTS blacklisted_cards (
    id BIGSERIAL PRIMARY KEY,                                            card_number_hash VARCHAR(255) NOT NULL UNIQUE,
    card_last_four VARCHAR(4),
    reason VARCHAR(255),
    blacklisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );
