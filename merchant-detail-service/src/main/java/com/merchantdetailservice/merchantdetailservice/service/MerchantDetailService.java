package com.merchantdetailservice.merchantdetailservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapper;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailRequest;
import com.merchantdetailservice.merchantdetailservice.entity.MerchantDetail;
import com.merchantdetailservice.merchantdetailservice.repository.MerchantDetailRepository;

import java.util.List;

@Service
public class MerchantDetailService {
    private final MerchantDetailRepository repository;
    private final MerchantDetailMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public MerchantDetailService(MerchantDetailRepository repository,
                                 MerchantDetailMapper mapper,
                                 OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("merchant-detail-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("merchant-detail-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<MerchantDetail> getAll() {
        Span span = tracer.spanBuilder("getAllDetails").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            return repository.findAll();
        } finally {
            span.end();
        }
    }

    public MerchantDetail getByMerchantId(Long merchantId) {
        return repository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant detail not found"));
    }

    public MerchantDetail getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Merchant detail not found"));
    }

    public MerchantDetail create(MerchantDetailRequest req) {
        return repository.save(mapper.toEntity(req));
    }

    public MerchantDetail update(Long id, MerchantDetailRequest req) {
        MerchantDetail entity = getById(id);
        entity.setDisplayName(req.displayName());
        entity.setCoverImageUrl(req.coverImageUrl());
        entity.setLogoUrl(req.logoUrl());
        entity.setShortDescription(req.shortDescription());
        entity.setWebsiteUrl(req.websiteUrl());
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}