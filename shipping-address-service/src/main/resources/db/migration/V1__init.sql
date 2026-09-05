CREATE TABLE shipping_addresses (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    alamat TEXT,
    provinsi VARCHAR(255),
    negara VARCHAR(255),
    kota VARCHAR(255),
    courier VARCHAR(100),
    shipping_method VARCHAR(255),
    shipping_cost INTEGER,
    created_at TIMESTAMP DEFAULT now(), updated_at TIMESTAMP DEFAULT now(), deleted_at TIMESTAMP
);
CREATE INDEX idx_shipping_order ON shipping_addresses(order_id);