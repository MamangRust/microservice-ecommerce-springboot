package com.merchantawardservice.merchantawardservice.controller;

import com.merchantawardservice.merchantawardservice.dto.MerchantAwardMapper;
import com.merchantawardservice.merchantawardservice.dto.MerchantAwardRequest;
import com.merchantawardservice.merchantawardservice.service.MerchantAwardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/merchant-awards")
@Tag(name = "Merchant Award Management")
@SecurityRequirement(name = "Bearer Authentication")
public class MerchantAwardController {
    private final MerchantAwardService service;
    private final MerchantAwardMapper mapper;
    public MerchantAwardController(MerchantAwardService service, MerchantAwardMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all merchant awards")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/merchant/{merchantId}") @Operation(summary = "Get awards by merchant ID")
    public ResponseEntity<?> getByMerchantId(@PathVariable Long merchantId) {
        return ResponseEntity.ok(service.getByMerchantId(merchantId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}") @Operation(summary = "Get award by ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create merchant award")
    public ResponseEntity<?> create(@Valid @RequestBody MerchantAwardRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update merchant award")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MerchantAwardRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete merchant award")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Merchant award deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}