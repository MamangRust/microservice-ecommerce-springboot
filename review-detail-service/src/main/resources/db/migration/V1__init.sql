CREATE TABLE review_details (
    id BIGSERIAL PRIMARY KEY,
    review_id BIGINT NOT NULL,
    type VARCHAR(20),
    url VARCHAR(255),
    caption VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);
CREATE INDEX idx_review_details_review ON review_details(review_id);