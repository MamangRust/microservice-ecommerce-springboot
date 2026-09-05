package com.merchantpolicyservice.merchantpolicyservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapper;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyRequest;
import com.merchantpolicyservice.merchantpolicyservice.entity.MerchantPolicy;
import com.merchantpolicyservice.merchantpolicyservice.repository.MerchantPolicyRepository;

import java.util.List;

@Service
public class MerchantPolicyService {
    private final MerchantPolicyRepository repository;
    private final MerchantPolicyMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public MerchantPolicyService(MerchantPolicyRepository repository, MerchantPolicyMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("merchant-policy-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("merchant-policy-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<MerchantPolicy> getAll() {
        Span span = tracer.spanBuilder("getAllPolicies").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); }
    }

    public List<MerchantPolicy> getByMerchantId(Long merchantId) { return repository.findByMerchantId(merchantId); }

    public MerchantPolicy getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Policy not found")); }

    public MerchantPolicy create(MerchantPolicyRequest req) { return repository.save(mapper.toEntity(req)); }

    public MerchantPolicy update(Long id, MerchantPolicyRequest req) {
        MerchantPolicy e = getById(id);
        e.setPolicyType(req.policyType()); e.setTitle(req.title()); e.setDescription(req.description());
        return repository.save(e);
    }

    public void delete(Long id) { repository.deleteById(id); }
}