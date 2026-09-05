package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain merchant_detail (merchant-detail-db). Idempotent via ON CONFLICT (merchant_id).
 */
@Component
public class MerchantDetailSeeder implements Seeder {

    @Override
    public String domain() { return "merchant_detail"; }

    @Override
    public int order() { return 25; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("merchant_detail");
        jdbc.update("""
            INSERT INTO merchant_details (merchant_id, display_name, cover_image_url, logo_url, short_description, website_url) VALUES
            (1, 'Toko Elektronik Jaya', 'https://files.local/cover-1.jpg', 'https://files.local/logo-1.png', 'Pusat elektronik terpercaya', 'https://jayashop.com'),
            (2, 'Fashion Kita', 'https://files.local/cover-2.jpg', 'https://files.local/logo-2.png', 'Tren fashion terkini', 'https://fashionkita.com')
            ON CONFLICT (merchant_id) DO NOTHING
            """);
        ctx.log().info("Seeded merchant details (idempotent)");
    }
}