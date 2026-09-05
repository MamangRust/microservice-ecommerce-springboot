-- ClickHouse init schema — ecommerce_stats
CREATE DATABASE IF NOT EXISTS ecommerce_stats;

-- Order daily aggregation (ReplacingMergeTree dedup by event_version)
CREATE TABLE IF NOT EXISTS ecommerce_stats.order_daily (
    event_id        String,
    event_version   UInt64,
    occurred_at     DateTime,
    order_id        String,
    product_id      String,
    user_id         String,
    quantity        UInt64,
    status          String
) ENGINE = ReplacingMergeTree(event_version)
PARTITION BY toYYYYMM(occurred_at)
ORDER BY (order_id);

-- Transaction daily aggregation
CREATE TABLE IF NOT EXISTS ecommerce_stats.transaction_daily (
    event_id        String,
    event_version   UInt64,
    occurred_at     DateTime,
    transaction_id  UInt64,
    order_id        UInt64,
    merchant_id     UInt64,
    payment_method  String,
    status          String,
    amount          UInt64
) ENGINE = ReplacingMergeTree(event_version)
PARTITION BY toYYYYMM(occurred_at)
ORDER BY (merchant_id, transaction_id);
