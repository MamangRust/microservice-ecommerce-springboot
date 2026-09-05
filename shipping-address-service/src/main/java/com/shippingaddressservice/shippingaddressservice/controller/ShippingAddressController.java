package com.shippingaddressservice.shippingaddressservice.controller;

import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressMapper;
import com.shippingaddressservice.shippingaddressservice.dto.ShippingAddressRequest;
import com.shippingaddressservice.shippingaddressservice.service.ShippingAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/shipping-addresses")
@Tag(name = "Shipping Address Management")
@SecurityRequirement(name = "Bearer Authentication")
public class ShippingAddressController {
    private final ShippingAddressService service;
    private final ShippingAddressMapper mapper;
    public ShippingAddressController(ShippingAddressService service, ShippingAddressMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }
    @GetMapping("/order/{orderId}") @Operation(summary = "Get by order") public ResponseEntity<?> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(service.getByOrderId(orderId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }
    @GetMapping("/{id}") @Operation(summary = "Get by ID") public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
    @PostMapping @Operation(summary = "Create") public ResponseEntity<?> create(@Valid @RequestBody ShippingAddressRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }
    @PutMapping("/{id}") @Operation(summary = "Update") public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ShippingAddressRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
    @DeleteMapping("/{id}") @Operation(summary = "Delete") public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}