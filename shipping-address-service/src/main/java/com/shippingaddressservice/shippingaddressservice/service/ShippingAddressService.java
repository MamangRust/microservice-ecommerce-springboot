package com.shippingaddressservice.shippingaddressservice.service;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import org.springframework.stereotype.Service;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapper;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressRequest;
import com.shippingaddressservice.shippingaddressservice.entity.ShippingAddress;
import com.shippingaddressservice.shippingaddressservice.repository.ShippingAddressRepository;
import java.util.List;

@Service
public class ShippingAddressService {
    private final ShippingAddressRepository repository;
    private final ShippingAddressMapper mapper;
    private final Tracer tracer;
    public ShippingAddressService(ShippingAddressRepository repository, ShippingAddressMapper mapper, OpenTelemetry openTelemetry) {
        this.repository = repository;
        this.mapper = mapper;
        this.tracer = openTelemetry.getTracer("shipping-address-service", "1.0.0");
        Meter meter = openTelemetry.getMeter("shipping-address-service");
        meter.counterBuilder("requests_total").build();
        meter.histogramBuilder("requests_duration_seconds").setUnit("s").build();
    }
    public List<ShippingAddress> getAll() { Span span = tracer.spanBuilder("getAll").startSpan(); try (Scope s = span.makeCurrent()) { return repository.findAll(); } finally { span.end(); } }
    public List<ShippingAddress> getByOrderId(Long orderId) { return repository.findByOrderId(orderId); }
    public ShippingAddress getById(Long id) { return repository.findById(id).orElseThrow(() -> new RuntimeException("Address not found")); }
    public ShippingAddress create(ShippingAddressRequest req) { return repository.save(mapper.toEntity(req)); }
    public ShippingAddress update(Long id, ShippingAddressRequest req) {
        ShippingAddress e = getById(id);
        e.setAlamat(req.alamat()); e.setProvinsi(req.provinsi()); e.setNegara(req.negara());
        e.setKota(req.kota()); e.setCourier(req.courier()); e.setShippingMethod(req.shippingMethod()); e.setShippingCost(req.shippingCost());
        return repository.save(e);
    }
    public void delete(Long id) { repository.deleteById(id); }
}