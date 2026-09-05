CREATE TABLE sliders (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    image VARCHAR(255),
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);