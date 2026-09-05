package com.reviewdetailservice.reviewdetailservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapper;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailRequest;
import com.reviewdetailservice.reviewdetailservice.entity.ReviewDetail;
import com.reviewdetailservice.reviewdetailservice.repository.ReviewDetailRepository;
import java.util.List;

@Service
public class ReviewDetailService {
    private final ReviewDetailRepository repository;
    private final ReviewDetailMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public ReviewDetailService(ReviewDetailRepository repository, ReviewDetailMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("review-detail-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("review-detail-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<ReviewDetail> getAll() {
        Span span = tracer.spanBuilder("getAllReviewDetails").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); }
    }

    public List<ReviewDetail> getByReviewId(Long reviewId) { return repository.findByReviewId(reviewId); }
    public ReviewDetail getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Review detail not found")); }
    public ReviewDetail create(ReviewDetailRequest req) { return repository.save(mapper.toEntity(req)); }

    public ReviewDetail update(Long id, ReviewDetailRequest req) {
        ReviewDetail e = getById(id);
        e.setType(req.type()); e.setUrl(req.url()); e.setCaption(req.caption());
        return repository.save(e);
    }

    public void delete(Long id) { repository.deleteById(id); }
}