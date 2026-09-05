package com.reviewdetailservice.reviewdetailservice.controller;

import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailMapper;
import com.reviewdetailservice.reviewdetailservice.dto.ReviewDetailRequest;
import com.reviewdetailservice.reviewdetailservice.service.ReviewDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/review-details")
@Tag(name = "Review Detail Management")
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewDetailController {
    private final ReviewDetailService service;
    private final ReviewDetailMapper mapper;
    public ReviewDetailController(ReviewDetailService service, ReviewDetailMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping @Operation(summary = "Get all review details") public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll().stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/review/{reviewId}") @Operation(summary = "Get details by review") public ResponseEntity<?> getByReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(service.getByReviewId(reviewId).stream().map(mapper::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/{id}") @Operation(summary = "Get detail by ID") public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(mapper.toResponse(service.getById(id))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @PostMapping @Operation(summary = "Create review detail") public ResponseEntity<?> create(@Valid @RequestBody ReviewDetailRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.create(req)));
    }

    @PutMapping("/{id}") @Operation(summary = "Update review detail") public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ReviewDetailRequest req) {
        try { return ResponseEntity.ok(mapper.toResponse(service.update(id, req))); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }

    @DeleteMapping("/{id}") @Operation(summary = "Delete review detail") public ResponseEntity<?> delete(@PathVariable Long id) {
        try { service.delete(id); return ResponseEntity.ok("Review detail deleted"); }
        catch (RuntimeException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); }
    }
}