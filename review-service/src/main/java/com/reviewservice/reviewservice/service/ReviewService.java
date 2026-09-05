package com.reviewservice.reviewservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import com.reviewservice.reviewservice.dto.ReviewMapper;
import com.reviewservice.reviewservice.dto.ReviewRequest;
import com.reviewservice.reviewservice.entity.Review;
import com.reviewservice.reviewservice.repository.ReviewRepository;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public ReviewService(ReviewRepository repository, ReviewMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("review-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("review-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<Review> getAll() {
        Span span = tracer.spanBuilder("getAllReviews").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); }
    }

    public List<Review> getByProductId(Long productId) { return repository.findByProductId(productId); }
    public Review getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Review not found")); }
    public Review create(ReviewRequest req) { return repository.save(mapper.toEntity(req)); }

    public Review update(Long id, ReviewRequest req) {
        Review e = getById(id);
        e.setName(req.name()); e.setComment(req.comment()); e.setRating(req.rating());
        return repository.save(e);
    }

    public void delete(Long id) { repository.deleteById(id); }
}