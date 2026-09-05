package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain shipping_address (shipping-address-db). Idempotent: skip jika sudah ada.
 */
@Component
public class ShippingAddressSeeder implements Seeder {

    @Override
    public String domain() { return "shipping_address"; }

    @Override
    public int order() { return 45; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("shipping_address");
        Integer count = jdbc.queryForObject("SELECT count(*) FROM shipping_addresses", Integer.class);
        if (count != null && count > 0) {
            ctx.log().info("Shipping addresses already exist, skipping seed");
            return;
        }
        jdbc.update("""
            INSERT INTO shipping_addresses (order_id, alamat, provinsi, negara, kota, courier, shipping_method, shipping_cost) VALUES
            (1, 'Jl. Melati No. 5', 'DKI Jakarta', 'Indonesia', 'Jakarta Selatan', 'JNE', 'REG', 10000)
            """);
        ctx.log().info("Seeded shipping addresses");
    }
}