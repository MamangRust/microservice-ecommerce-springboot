package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Seeder untuk domain product (product-db). E-commerce product menggunakan UUID id.
 * Idempotent via ON CONFLICT (name).
 */
@Component
public class ProductSeeder implements Seeder {

    @Override
    public String domain() {
        return "product";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("product");
        jdbc.update("""
            INSERT INTO products (id, name, description, price, quantity) VALUES
            (?, 'Smartphone X', 'Smartphone flagship', 5000000, 20),
            (?, 'Kemeja Polos', 'Kemeja katun polos', 150000, 50)
            ON CONFLICT (name) DO NOTHING
            """, UUID.randomUUID(), UUID.randomUUID());
        ctx.log().info("Seeded products (idempotent)");
    }
}