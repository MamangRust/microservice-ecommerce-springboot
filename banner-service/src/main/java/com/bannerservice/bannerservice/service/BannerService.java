package com.bannerservice.bannerservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;

import com.bannerservice.bannerservice.dto.BannerMapper;
import com.bannerservice.bannerservice.dto.BannerRequest;
import com.bannerservice.bannerservice.entity.Banner;
import com.bannerservice.bannerservice.repository.BannerRepository;

import java.util.List;

@Service
public class BannerService {
    private final BannerRepository repository;
    private final BannerMapper mapper;
    private final Tracer tracer;
    private final LongCounter requestsTotal;
    private final DoubleHistogram requestsDuration;

    public BannerService(BannerRepository repository, BannerMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("banner-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("banner-service");
        this.requestsTotal = meter.counterBuilder("requests_total").setDescription("Total requests").setUnit("1").build();
        this.requestsDuration = meter.histogramBuilder("requests_duration_seconds").setDescription("Duration").setUnit("s").build();
    }

    public List<Banner> getAll() {
        Span span = tracer.spanBuilder("getAllBanners").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope scope = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); }
    }

    public List<Banner> getActive() { return repository.findByIsActiveTrue(); }

    public Banner getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Banner not found")); }

    public Banner create(BannerRequest req) { return repository.save(mapper.toEntity(req)); }

    public Banner update(Long id, BannerRequest req) {
        Banner e = getById(id);
        e.setName(req.name()); e.setStartDate(req.startDate()); e.setEndDate(req.endDate());
        e.setStartTime(req.startTime()); e.setEndTime(req.endTime());
        if (req.isActive() != null) e.setIsActive(req.isActive());
        return repository.save(e);
    }

    public void delete(Long id) { repository.deleteById(id); }
}