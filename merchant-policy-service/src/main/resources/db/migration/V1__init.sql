CREATE TABLE merchant_policies (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    policy_type VARCHAR(100),
    title VARCHAR(255),
    description TEXT,
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);
CREATE INDEX idx_merchant_policy_merchant ON merchant_policies(merchant_id);