package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeder untuk domain order_item (order-item-db). Idempotent: skip jika sudah ada.
 */
@Component
public class OrderItemSeeder implements Seeder {

    @Override
    public String domain() { return "order_item"; }

    @Override
    public int order() { return 45; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("order_item");
        Integer count = jdbc.queryForObject("SELECT count(*) FROM order_items", Integer.class);
        if (count != null && count > 0) {
            ctx.log().info("Order items already exist, skipping seed");
            return;
        }
        jdbc.update("""
            INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
            (1, 1, 2, 5000000),
            (1, 2, 1, 150000)
            """);
        ctx.log().info("Seeded order items");
    }
}