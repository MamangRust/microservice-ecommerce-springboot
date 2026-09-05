package com.seeder.seeder.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Registers one DataSource per domain database so seeders can write to each
 * service's own PostgreSQL instance.
 */
@Configuration
public class SeederDataSourceConfig {

    private static final Logger log = LoggerFactory.getLogger(SeederDataSourceConfig.class);

    @Value("${seed.db.identity-url:jdbc:postgresql://user-db:5432/user_service}")
    private String identityUrl;

    @Value("${seed.db.role-url:jdbc:postgresql://role-db:5432/role_service}")
    private String roleUrl;

    @Value("${seed.db.merchant-url:jdbc:postgresql://merchant-db:5432/merchant_service}")
    private String merchantUrl;

    @Value("${seed.db.merchant-award-url:jdbc:postgresql://merchant-award-db:5432/merchant_award_service}")
    private String merchantAwardUrl;

    @Value("${seed.db.merchant-business-url:jdbc:postgresql://merchant-business-db:5432/merchant_business_service}")
    private String merchantBusinessUrl;

    @Value("${seed.db.merchant-detail-url:jdbc:postgresql://merchant-detail-db:5432/merchant_detail_service}")
    private String merchantDetailUrl;

    @Value("${seed.db.merchant-policy-url:jdbc:postgresql://merchant-policy-db:5432/merchant_policy_service}")
    private String merchantPolicyUrl;

    @Value("${seed.db.category-url:jdbc:postgresql://category-db:5432/category_service}")
    private String categoryUrl;

    @Value("${seed.db.banner-url:jdbc:postgresql://banner-db:5432/banner_service}")
    private String bannerUrl;

    @Value("${seed.db.slider-url:jdbc:postgresql://slider-db:5432/slider_service}")
    private String sliderUrl;

    @Value("${seed.db.product-url:jdbc:postgresql://product-db:5432/product_service}")
    private String productUrl;

    @Value("${seed.db.cart-url:jdbc:postgresql://cart-db:5432/cart_service}")
    private String cartUrl;

    @Value("${seed.db.order-url:jdbc:postgresql://order-db:5432/spring-order-service}")
    private String orderUrl;

    @Value("${seed.db.order-item-url:jdbc:postgresql://order-item-db:5432/order_item_service}")
    private String orderItemUrl;

    @Value("${seed.db.transaction-url:jdbc:postgresql://transaction-db:5432/transaction_service}")
    private String transactionUrl;

    @Value("${seed.db.review-url:jdbc:postgresql://review-db:5432/review_service}")
    private String reviewUrl;

    @Value("${seed.db.review-detail-url:jdbc:postgresql://review-detail-db:5432/review_detail_service}")
    private String reviewDetailUrl;

    @Value("${seed.db.shipping-address-url:jdbc:postgresql://shipping-address-db:5432/shipping_address_service}")
    private String shippingAddressUrl;

    @Value("${seed.db.username:postgres}")
    private String username;

    @Value("${seed.db.password:password}")
    private String password;

    @Bean
    public Map<String, DataSource> seedDataSources() {
        Map<String, DataSource> map = new HashMap<>();
        map.put("identity", dataSource(identityUrl));
        map.put("role", dataSource(roleUrl));
        map.put("merchant", dataSource(merchantUrl));
        map.put("merchant_award", dataSource(merchantAwardUrl));
        map.put("merchant_business", dataSource(merchantBusinessUrl));
        map.put("merchant_detail", dataSource(merchantDetailUrl));
        map.put("merchant_policy", dataSource(merchantPolicyUrl));
        map.put("category", dataSource(categoryUrl));
        map.put("banner", dataSource(bannerUrl));
        map.put("slider", dataSource(sliderUrl));
        map.put("product", dataSource(productUrl));
        map.put("cart", dataSource(cartUrl));
        map.put("order", dataSource(orderUrl));
        map.put("order_item", dataSource(orderItemUrl));
        map.put("transaction", dataSource(transactionUrl));
        map.put("review", dataSource(reviewUrl));
        map.put("review_detail", dataSource(reviewDetailUrl));
        map.put("shipping_address", dataSource(shippingAddressUrl));
        log.info("Registered {} seed data sources", map.size());
        return map;
    }

    private DataSource dataSource(String url) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        return ds;
    }
}