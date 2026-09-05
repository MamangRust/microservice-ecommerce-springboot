package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain review_detail (review-detail-db). Idempotent: skip jika sudah ada.
 */
@Component
public class ReviewDetailSeeder implements Seeder {

    @Override
    public String domain() { return "review_detail"; }

    @Override
    public int order() { return 45; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("review_detail");
        Integer count = jdbc.queryForObject("SELECT count(*) FROM review_details", Integer.class);
        if (count != null && count > 0) {
            ctx.log().info("Review details already exist, skipping seed");
            return;
        }
        jdbc.update("""
            INSERT INTO review_details (review_id, type, url, caption) VALUES
            (1, 'PHOTO', 'https://files.local/review-1.jpg', 'Foto produk diterima')
            """);
        ctx.log().info("Seeded review details");
    }
}