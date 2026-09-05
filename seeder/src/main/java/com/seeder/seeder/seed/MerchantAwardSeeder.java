package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain merchant_award (merchant-award-db). Idempotent via ON CONFLICT (title).
 */
@Component
public class MerchantAwardSeeder implements Seeder {

    @Override
    public String domain() { return "merchant_award"; }

    @Override
    public int order() { return 25; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("merchant_award");
        jdbc.update("""
            INSERT INTO merchant_certifications_and_awards (merchant_id, title, description, issued_by, issue_date, expiry_date, certificate_url) VALUES
            (1, 'Sertifikat Halal', 'Sertifikasi halal MUI', 'MUI', '2024-01-01', '2026-01-01', 'https://files.local/halal-1.pdf'),
            (2, 'Penghargaan UMKM', 'Penghargaan UMKM Teladan', 'Kemenkop', '2023-05-01', NULL, 'https://files.local/umkm-2.pdf')
            ON CONFLICT (title) DO NOTHING
            """);
        ctx.log().info("Seeded merchant awards (idempotent)");
    }
}