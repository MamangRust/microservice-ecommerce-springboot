package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain review (review-db). Idempotent: skip jika sudah ada review.
 */
@Component
public class ReviewSeeder implements Seeder {

    @Override
    public String domain() { return "review"; }

    @Override
    public int order() { return 45; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("review");
        Integer count = jdbc.queryForObject("SELECT count(*) FROM reviews", Integer.class);
        if (count != null && count > 0) {
            ctx.log().info("Reviews already exist, skipping seed");
            return;
        }
        jdbc.update("""
            INSERT INTO reviews (user_id, product_id, name, comment, rating) VALUES
            (2, 1, 'Budi', 'Produk bagus dan cepat sampai', 5),
            (2, 2, 'Siti', 'Bahan nyaman, ukuran pas', 4)
            """);
        ctx.log().info("Seeded reviews");
    }
}