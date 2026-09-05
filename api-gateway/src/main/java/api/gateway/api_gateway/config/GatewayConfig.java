package api.gateway.api_gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import api.gateway.api_gateway.filter.JwtAuthFilter;

@Configuration
public class GatewayConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public GatewayConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("auth", r -> r.path("/auth/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://auth-service"))
            .route("user", r -> r.path("/users/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://user"))
            .route("product", r -> r.path("/products/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://product"))
            .route("order", r -> r.path("/orders/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://order"))
            .route("payment", r -> r.path("/payments/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://payment"))
            .route("notification", r -> r.path("/notifications/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://notification-service"))
            .route("file-storage", r -> r.path("/files/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://file-storage-service"))
            .route("role", r -> r.path("/roles/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://role-service"))
            .route("merchant", r -> r.path("/merchants/**", "/merchant-documents/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://merchant-service"))
            .route("merchant-award", r -> r.path("/merchant-awards/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://merchant-award-service"))
            .route("merchant-business", r -> r.path("/merchant-businesses/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://merchant-business-service"))
            .route("merchant-detail", r -> r.path("/merchant-details/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://merchant-detail-service"))
            .route("merchant-policy", r -> r.path("/merchant-policies/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://merchant-policy-service"))
            .route("category", r -> r.path("/categories/**", "/category/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://category-service"))
            .route("banner", r -> r.path("/banners/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://banner-service"))
            .route("slider", r -> r.path("/sliders/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://slider-service"))
            .route("cart", r -> r.path("/carts/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://cart-service"))
            .route("order-item", r -> r.path("/order-items/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://order-item-service"))
            .route("transaction", r -> r.path("/transactions/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://transaction-service"))
            .route("review", r -> r.path("/reviews/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://review-service"))
            .route("review-detail", r -> r.path("/review-details/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://review-detail-service"))
            .route("shipping-address", r -> r.path("/shipping-addresses/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://shipping-address-service"))
            .route("stats", r -> r.path("/stats/**")
                .filters(f -> f.filter(jwtAuthFilter))
                .uri("lb://stats-reader"))
            .build();
    }
}
