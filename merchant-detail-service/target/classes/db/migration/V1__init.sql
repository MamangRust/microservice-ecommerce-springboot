CREATE TABLE merchant_details (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL UNIQUE,
    display_name VARCHAR(255),
    cover_image_url VARCHAR(255),
    logo_url VARCHAR(255),
    short_description TEXT,
    website_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);