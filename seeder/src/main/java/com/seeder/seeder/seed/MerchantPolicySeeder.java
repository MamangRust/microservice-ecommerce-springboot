package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain merchant_policy (merchant-policy-db). Idempotent via ON CONFLICT (title).
 */
@Component
public class MerchantPolicySeeder implements Seeder {

    @Override
    public String domain() { return "merchant_policy"; }

    @Override
    public int order() { return 25; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("merchant_policy");
        jdbc.update("""
            INSERT INTO merchant_policies (merchant_id, policy_type, title, description) VALUES
            (1, 'REFUND', 'Kebijakan Refund', 'Refund maksimal 7 hari setelah pembelian'),
            (1, 'SHIPPING', 'Kebijakan Pengiriman', 'Pengiriman 1-3 hari kerja')
            ON CONFLICT (title) DO NOTHING
            """);
        ctx.log().info("Seeded merchant policies (idempotent)");
    }
}