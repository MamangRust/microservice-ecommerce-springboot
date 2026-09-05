package com.merchantpolicyservice.merchantpolicyservice.controller;

import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyMapper;
import com.merchantpolicyservice.merchantpolicyservice.dto.MerchantPolicyRequest;
import com.merchantpolicyservice.merchantpolicyservice.service.MerchantPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/merchant-policies")
@Tag(name = "Merchant Policy Management")
@SecurityRequirement(name = "Bearer Authentication")
public class MerchantPolicyController {
    private final MerchantPolicyService service;
    private final MerchantPolicyMapper mapper;
    public MerchantPolicyController(MerchantPolicyService service, MerchantPolicyMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all policies") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/merchant/{merchantId}") @Operation(summary = "Get policies by merchant") public ResponseEntity<?> getByMerchant(@PathVariable Long merchantId) {
        return ResponseEntity.ok(service.getByMerchantId(merchantId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}") @Operation(summary = "Get policy by ID") public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create policy") public ResponseEntity<?> create(@Valid @RequestBody MerchantPolicyRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update policy") public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody MerchantPolicyRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete policy") public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Policy deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}