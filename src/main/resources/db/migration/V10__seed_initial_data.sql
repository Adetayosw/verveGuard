    -- Seed merchant categories
INSERT INTO merchant_categories (category_name, max_amount, description)
VALUES
    ('BASIC',    100000.00,  'Basic tier for small businesses. Max single transaction: ₦100,000'),
    ('STANDARD', 500000.00,  'Standard tier for medium businesses. Max single transaction: ₦500,000'),
    ('PREMIUM',  5000000.00, 'Premium tier for large businesses. Max single transaction: ₦5,000,000')
    ON CONFLICT (category_name) DO NOTHING;

-- Seed customers (5 realistic Nigerian names)
INSERT INTO customers (first_name, last_name, email, phone)
VALUES
    ('Tunde',  'Balogun', 'tunde.balogun@example.com', '+2348012345671'),
    ('Amaka',  'Okafor',  'amaka.okafor@example.com',  '+2348012345672'),
    ('Seyi',   'Adesina', 'seyi.adesina@example.com',  '+2348012345673'),
    ('Halima', 'Bello',   'halima.bello@example.com',  '+2348012345674'),
    ('Kunle',  'Oladipo', 'kunle.oladipo@example.com', '+2348012345675')
    ON CONFLICT (email) DO NOTHING;

-- Assume customer IDs are 1..5 in order

-- Seed customer cards (10 cards, 2 per customer)
-- Card 1: 5061234567890123
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (1, '9dc1ab33bb52c14962974d2dbef934f9203f5f907065577c4aa4aea9aecb8317', '0123', TRUE);

-- Card 2: 5062234567890124
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (1, '02f0bdaf59c6a92eb5f211a7b72201be5d763b02448f061dee52e8b431b88d34', '0124', TRUE);

-- Card 3: 5063234567890125
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (2, '512fbe0aa6dbafca1ef8dd7f8bf1f9a9b7d50158f00bd2440c0843ec14436969', '0125', TRUE);

-- Card 4: 5064234567890126
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (2, '4d62594ab431793b171bf8eda8212b681c495649ad89c2cda47c976a3b87c64c', '0126', TRUE);

-- Card 5: 5065234567890127
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (3, '567f8cf033ff7765da8a395f31996ce8852abd43c9c2077d4742f205c530c03e', '0127', TRUE);

-- Card 6: 5066234567890128
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (3, '2fb59e5e8cf73797eaa09a15619de3a15b3c80a1b8ab220dcc8f3c10207cde1b', '0128', TRUE);

-- Card 7: 5067234567890129
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (4, '61d4db36fcb64397a76491f75f0012f2f1c932a03fc95197596b6a0c8cf3a9b6', '0129', TRUE);

-- Card 8: 5068234567890130
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (4, '8c914f94660029032e0eee02a231878c0d02b07e31a6a83eb4a9b2b4c44c398c', '0130', TRUE);

-- Card 9: 5069234567890131
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (5, '5f25d1dcde641b4a76847b558367522a23b81c3d0950dfa6bd8d68b52d4168f3', '0131', TRUE);

-- Card 10: 5070234567890132
INSERT INTO customer_cards (customer_id, card_number_hash, card_last_four, is_active)
VALUES (5, '50d8e5b8d8045f4adc028ebc4ce46fbb37df2c7c3c3ce9c05e3ed8d854d88241', '0132', TRUE);

-- Seed card spending profiles for all 10 cards
-- Low avg (5k–20k), medium (50k–100k), high (200k–500k)

-- Card 1: low avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('9dc1ab33bb52c14962974d2dbef934f9203f5f907065577c4aa4aea9aecb8317',  5000.00, 15000.00,  30, CURRENT_TIMESTAMP);

-- Card 2: low/medium avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('02f0bdaf59c6a92eb5f211a7b72201be5d763b02448f061dee52e8b431b88d34', 20000.00, 50000.00,  40, CURRENT_TIMESTAMP);

-- Card 3: medium avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('512fbe0aa6dbafca1ef8dd7f8bf1f9a9b7d50158f00bd2440c0843ec14436969',  50000.00, 150000.00, 50, CURRENT_TIMESTAMP);

-- Card 4: medium/high avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('4d62594ab431793b171bf8eda8212b681c495649ad89c2cda47c976a3b87c64c', 100000.00, 300000.00, 60, CURRENT_TIMESTAMP);

-- Card 5: high avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('567f8cf033ff7765da8a395f31996ce8852abd43c9c2077d4742f205c530c03e', 200000.00, 600000.00, 70, CURRENT_TIMESTAMP);

-- Card 6: high avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('2fb59e5e8cf73797eaa09a15619de3a15b3c80a1b8ab220dcc8f3c10207cde1b', 300000.00, 900000.00, 80, CURRENT_TIMESTAMP);

-- Card 7: very high avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('61d4db36fcb64397a76491f75f0012f2f1c932a03fc95197596b6a0c8cf3a9b6', 400000.00, 1200000.00, 90, CURRENT_TIMESTAMP);

-- Card 8: very high avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('8c914f94660029032e0eee02a231878c0d02b07e31a6a83eb4a9b2b4c44c398c', 500000.00, 1500000.00, 100, CURRENT_TIMESTAMP);

-- Card 9: low/medium avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('5f25d1dcde641b4a76847b558367522a23b81c3d0950dfa6bd8d68b52d4168f3', 15000.00, 40000.00,  35, CURRENT_TIMESTAMP);

-- Card 10: medium avg
INSERT INTO card_spending_profile (card_number_hash, avg_transaction_amount, max_seen_amount, transaction_count, last_updated)
VALUES ('50d8e5b8d8045f4adc028ebc4ce46fbb37df2c7c3c3ce9c05e3ed8d854d88241', 80000.00, 250000.00, 55, CURRENT_TIMESTAMP);

-- Seed blacklisted merchants (5 entries for testing)
INSERT INTO blacklisted_merchants (merchant_id, merchant_category, reason, blacklisted_by)
VALUES
    ('MERCH_BL_001', 'BASIC',    'Chargeback spike detected',          'admin'),
    ('MERCH_BL_002', 'STANDARD', 'Regulatory compliance issues',       'admin'),
    ('MERCH_BL_003', 'PREMIUM',  'Fraudulent transaction patterns',    'admin'),
    ('MERCH_BL_004', NULL,       'Suspicious behaviour under review',  'admin'),
    ('MERCH_BL_005', 'BASIC',    'Multiple customer complaints',       'admin')
    ON CONFLICT (merchant_id) DO NOTHING;