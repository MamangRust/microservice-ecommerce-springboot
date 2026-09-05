package com.merchantdetailservice.merchantdetailservice.controller;

import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailMapper;
import com.merchantdetailservice.merchantdetailservice.dto.MerchantDetailRequest;
import com.merchantdetailservice.merchantdetailservice.service.MerchantDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/merchant-details")
@Tag(name = "Merchant Detail Management")
@SecurityRequirement(name = "Bearer Authentication")
public class MerchantDetailController {
    private final MerchantDetailService service;
    private final MerchantDetailMapper mapper;
    public MerchantDetailController(MerchantDetailService service, MerchantDetailMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all merchant details")
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/merchant/{merchantId}") @Operation(summary = "Get detail by merchant ID")
    public ResponseEntity<?> getByMerchantId(@PathVariable Long merchantId) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getByMerchantId(merchantId))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @GetMapping("/{id}") @Operation(summary = "Get detail by ID")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create merchant detail")
    public ResponseEntity<?> create(@Valid @RequestBody MerchantDetailRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update merchant detail")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MerchantDetailRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete merchant detail")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Merchant detail deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}