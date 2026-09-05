CREATE TABLE merchant_business_information (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL UNIQUE,
    business_type VARCHAR(100),
    tax_id VARCHAR(50),
    established_year INTEGER,
    number_of_employees INTEGER,
    website_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);