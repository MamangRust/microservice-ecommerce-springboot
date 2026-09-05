package com.merchantbusinessservice.merchantbusinessservice.controller;

import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessMapper;
import com.merchantbusinessservice.merchantbusinessservice.dto.MerchantBusinessRequest;
import com.merchantbusinessservice.merchantbusinessservice.service.MerchantBusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/merchant-businesses")
@Tag(name = "Merchant Business Management")
@SecurityRequirement(name = "Bearer Authentication")
public class MerchantBusinessController {
    private final MerchantBusinessService service;
    private final MerchantBusinessMapper mapper;
    public MerchantBusinessController(MerchantBusinessService service, MerchantBusinessMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all merchant businesses")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/merchant/{merchantId}") @Operation(summary = "Get business by merchant ID")
    public ResponseEntity<?> getByMerchantId(@PathVariable Long merchantId) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getByMerchantId(merchantId))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @GetMapping("/{id}") @Operation(summary = "Get business by ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create merchant business")
    public ResponseEntity<?> create(@Valid @RequestBody MerchantBusinessRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update merchant business")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MerchantBusinessRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete merchant business")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Merchant business deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}