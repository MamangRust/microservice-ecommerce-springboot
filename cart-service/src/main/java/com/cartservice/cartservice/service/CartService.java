package com.cartservice.cartservice.service;

import com.cartservice.cartservice.dto.CartMapper;
import com.cartservice.cartservice.dto.CartRequest;
import com.cartservice.cartservice.entity.Cart;
import com.cartservice.cartservice.repository.CartRepository;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartService {
    private final CartRepository repository;
    private final CartMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public CartService(CartRepository repository, CartMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("cart-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("cart-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public Page<Cart> getByUserId(Long userId, Pageable pageable) {
        Span span = tracer.spanBuilder("getUserCart").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) { return repository.findByUserId(userId, pageable); }
        finally { span.end(); }
    }

    public Cart addItem(CartRequest req) {
        return repository.save(mapper.toEntity(req));
    }

    public void removeItem(Long cartId, Long userId) {
        Cart cart = repository.findById(cartId).orElseThrow(() -> new RuntimeException("Cart item not found"));
        if (!cart.getUserId().equals(userId)) throw new RuntimeException("Unauthorized");
        repository.delete(cart);
    }

    public void clearCart(Long userId, List<Long> cartIds) {
        repository.deleteByUserIdAndIdIn(userId, cartIds);
    }
}