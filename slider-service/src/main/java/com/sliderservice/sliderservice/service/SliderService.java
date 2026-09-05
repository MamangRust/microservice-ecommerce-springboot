package com.sliderservice.sliderservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import com.sliderservice.sliderservice.dto.SliderMapper;
import com.sliderservice.sliderservice.dto.SliderRequest;
import com.sliderservice.sliderservice.entity.Slider;
import com.sliderservice.sliderservice.repository.SliderRepository;
import java.util.List;

@Service
public class SliderService {
    private final SliderRepository repository;
    private final SliderMapper mapper;
    private final Tracer tracer;
    public SliderService(SliderRepository repository, SliderMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("slider-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("slider-service");
        meter.counterBuilder("requests_total").build();
        meter.histogramBuilder("requests_duration_seconds").setUnit("s").build();
    }
    public List<Slider> getAll() { Span span = tracer.spanBuilder("getAllSliders").setSpanKind(SpanKind.SERVER).startSpan(); try (Scope s = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); } }
    public Slider getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Slider not found")); }
    public Slider create(SliderRequest req) { return repository.save(mapper.toEntity(req)); }
    public Slider update(Long id, SliderRequest req) {
        Slider e = getById(id);
        e.setName(req.name()); e.setImage(req.image());
        return repository.save(e);
    }
    public void delete(Long id) { repository.deleteById(id); }
}