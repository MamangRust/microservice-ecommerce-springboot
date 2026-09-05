package com.bannerservice.bannerservice.controller;

import com.bannerservice.bannerservice.dto.BannerMapper;
import com.bannerservice.bannerservice.dto.BannerRequest;
import com.bannerservice.bannerservice.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/banners")
@Tag(name = "Banner Management")
@SecurityRequirement(name = "Bearer Authentication")
public class BannerController {
    private final BannerService service;
    private final BannerMapper mapper;
    public BannerController(BannerService service, BannerMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all banners") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/active") @Operation(summary = "Get active banners") public ResponseEntity<?> getActive() {
        return ResponseEntity.ok(service.getActive().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}") @Operation(summary = "Get banner by ID") public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create banner") public ResponseEntity<?> create(@Valid @RequestBody BannerRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update banner") public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody BannerRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete banner") public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Banner deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}