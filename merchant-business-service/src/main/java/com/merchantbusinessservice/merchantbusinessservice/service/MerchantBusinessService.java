package com.merchantbusinessservice.merchantbusinessservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapper;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessRequest;
import com.merchantbusinessservice.merchantbusinessservice.entity.MerchantBusinessInformation;
import com.merchantbusinessservice.merchantbusinessservice.repository.MerchantBusinessInformationRepository;

import java.util.List;

@Service
public class MerchantBusinessService {
    private final MerchantBusinessInformationRepository repository;
    private final MerchantBusinessMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public MerchantBusinessService(MerchantBusinessInformationRepository repository,
                                   MerchantBusinessMapper mapper,
                                   OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("merchant-business-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("merchant-business-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<MerchantBusinessInformation> getAll() {
        Span span = tracer.spanBuilder("getAllBusiness").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) {
            return repository.findAll();
        } finally {
            span.end();
        }
    }

    public MerchantBusinessInformation getByMerchantId(Long merchantId) {
        return repository.findByMerchantId(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant business not found"));
    }

    public MerchantBusinessInformation getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Merchant business not found"));
    }

    public MerchantBusinessInformation create(MerchantBusinessRequest req) {
        return repository.save(mapper.toEntity(req));
    }

    public MerchantBusinessInformation update(Long id, MerchantBusinessRequest req) {
        MerchantBusinessInformation entity = getById(id);
        entity.setBusinessType(req.businessType());
        entity.setTaxId(req.taxId());
        entity.setEstablishedYear(req.establishedYear());
        entity.setNumberOfEmployees(req.numberOfEmployees());
        entity.setWebsiteUrl(req.websiteUrl());
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}