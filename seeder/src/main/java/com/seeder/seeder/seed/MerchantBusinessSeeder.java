package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain merchant_business (merchant-business-db). Idempotent via ON CONFLICT (merchant_id).
 */
@Component
public class MerchantBusinessSeeder implements Seeder {

    @Override
    public String domain() { return "merchant_business"; }

    @Override
    public int order() { return 25; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("merchant_business");
        jdbc.update("""
            INSERT INTO merchant_business_information (merchant_id, business_type, tax_id, established_year, number_of_employees, website_url) VALUES
            (1, 'RETAIL', '1234567890', 2015, 25, 'https://jayashop.com'),
            (2, 'FASHION', '0987654321', 2018, 10, 'https://fashionkita.com')
            ON CONFLICT (merchant_id) DO NOTHING
            """);
        ctx.log().info("Seeded merchant businesses (idempotent)");
    }
}