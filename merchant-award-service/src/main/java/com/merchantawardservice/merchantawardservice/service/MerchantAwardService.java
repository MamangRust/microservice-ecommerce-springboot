package com.merchantawardservice.merchantawardservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapper;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardRequest;
import com.merchantawardservice.merchantawardservice.entity.MerchantCertificationAndAward;
import com.merchantawardservice.merchantawardservice.repository.MerchantCertificationAndAwardRepository;

import java.util.List;

@Service
public class MerchantAwardService {
    private final MerchantCertificationAndAwardRepository repository;
    private final MerchantAwardMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public MerchantAwardService(MerchantCertificationAndAwardRepository repository,
                                MerchantAwardMapper mapper,
                                OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("merchant-award-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("merchant-award-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<MerchantCertificationAndAward> getAll() {
        Span span = tracer.spanBuilder("getAllAwards").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            return repository.findAll();
        } finally {
            span.end();
        }
    }

    public List<MerchantCertificationAndAward> getByMerchantId(Long merchantId) {
        return repository.findByMerchantId(merchantId);
    }

    public MerchantCertificationAndAward getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Merchant award not found"));
    }

    public MerchantCertificationAndAward create(MerchantAwardRequest req) {
        return repository.save(mapper.toEntity(req));
    }

    public MerchantCertificationAndAward update(Long id, MerchantAwardRequest req) {
        MerchantCertificationAndAward entity = getById(id);
        entity.setTitle(req.title());
        entity.setDescription(req.description());
        entity.setIssuedBy(req.issuedBy());
        entity.setIssueDate(req.issueDate());
        entity.setExpiryDate(req.expiryDate());
        entity.setCertificateUrl(req.certificateUrl());
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}