CREATE TABLE IF NOT EXISTS blacklisted_merchants(
    id BIGSERIAL PRIMARY KEY,
    merchant_id VARCHAR(100) NOT NULL UNIQUE,
    merchant_category VARCHAR(50),
    reason VARCHAR(255),
    blacklisted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    blacklisted_by VARCHAR(100)
)