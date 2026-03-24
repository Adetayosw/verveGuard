CREATE TABLE IF NOT EXISTS customer_cards(
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    card_number_hash VARCHAR(255) NOT NULL,
    card_last_four VARCHAR(4) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_customer_cards_customer
    FOREIGN KEY (customer_id) REFERENCES customers (id)
    ON DELETE CASCADE

)