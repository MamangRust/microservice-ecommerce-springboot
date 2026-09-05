package com.sliderservice.sliderservice.controller;

import com.sliderservice.sliderservice.dto.SliderMapper;
import com.sliderservice.sliderservice.dto.SliderRequest;
import com.sliderservice.sliderservice.service.SliderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sliders")
@Tag(name = "Slider Management")
@SecurityRequirement(name = "Bearer Authentication")
public class SliderController {
    private final SliderService service;
    private final SliderMapper mapper;
    public SliderController(SliderService service, SliderMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all sliders") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }
    @GetMapping("/{id}") @Operation(summary = "Get by ID") public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
    @PostMapping @Operation(summary = "Create slider") public ResponseEntity<?> create(@Valid @RequestBody SliderRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }
    @PutMapping("/{id}") @Operation(summary = "Update slider") public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SliderRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
    @DeleteMapping("/{id}") @Operation(summary = "Delete slider") public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Slider deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}