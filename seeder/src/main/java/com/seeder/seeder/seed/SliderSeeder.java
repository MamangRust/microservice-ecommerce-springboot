package com.seeder.seeder.seed;

import com.common.seed.SeedContext;
import com.common.seed.Seeder;
import org.springframework.stereotype.Component;

/**
 * Seeder untuk domain slider (slider-db). Idempotent via ON CONFLICT (name).
 */
@Component
public class SliderSeeder implements Seeder {

    @Override
    public String domain() { return "slider"; }

    @Override
    public int order() { return 35; }

    @Override
    public void seed(SeedContext ctx) {
        var jdbc = ctx.jdbc("slider");
        jdbc.update("""
            INSERT INTO sliders (name, image) VALUES
            ('Slider Hero 1', 'https://files.local/slider-1.jpg'),
            ('Slider Hero 2', 'https://files.local/slider-2.jpg')
            ON CONFLICT (name) DO NOTHING
            """);
        ctx.log().info("Seeded sliders (idempotent)");
    }
}