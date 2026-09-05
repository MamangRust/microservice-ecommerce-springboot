package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeder untuk domain cart (cart-db). Idempotent: skip jika sudah ada cart.
 */
@Component
public class CartSeeder implements Seeder {

    @Override
    public String domain() { return "cart"; }

    @Override
    public int order() { return 45; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("cart");
        Integer count = jdbc.queryForObject("SELECT count(*) FROM carts", Integer.class);
        if (count != null && count > 0) {
            ctx.log().info("Carts already exist, skipping seed");
            return;
        }
        jdbc.update("""
            INSERT INTO carts (user_id, product_id, name, price, quantity) VALUES
            (2, 1, 'Smartphone X', 5000000, 1),
            (2, 2, 'Kemeja Polos', 150000, 2)
            """);
        ctx.log().info("Seeded carts");
    }
}