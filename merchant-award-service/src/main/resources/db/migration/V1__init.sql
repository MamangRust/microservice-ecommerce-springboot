CREATE TABLE merchant_certifications_and_awards (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    issued_by VARCHAR(255),
    issue_date DATE,
    expiry_date DATE,
    certificate_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);
CREATE INDEX idx_merchant_award_merchant ON merchant_certifications_and_awards(merchant_id);