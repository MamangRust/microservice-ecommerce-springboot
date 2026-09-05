package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain banner (banner-db). Idempotent via ON CONFLICT (name).
 */
@Component
public class BannerSeeder implements Seeder {

    @Override
    public String domain() { return "banner"; }

    @Override
    public int order() { return 35; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("banner");
        jdbc.update("""
            INSERT INTO banners (name, start_date, end_date, is_active) VALUES
            ('Promo Tahun Baru', '2026-01-01', '2026-01-31', true),
            ('Flash Sale', '2026-02-14', '2026-02-20', true)
            ON CONFLICT (name) DO NOTHING
            """);
        ctx.log().info("Seeded banners (idempotent)");
    }
}