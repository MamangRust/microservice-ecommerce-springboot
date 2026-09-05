package com.cartservice.cartservice.controller;

import com.cartservice.cartservice.dto.CartMapper;
import com.cartservice.cartservice.dto.CartRequest;
import com.cartservice.cartservice.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/carts")
@Tag(name = "Cart Management")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {
    private final CartService service;
    private final CartMapper mapper;
    public CartController(CartService service, CartMapper mapper) { this.service = service; this.mapper = mapper; }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user cart (paginated)")
    public ResponseEntity<?> getUserCart(@PathVariable Long userId,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(service.getByUserId(userId, PageRequest.of(page, size))
            .map(mapper::toResponse));
    }

    @PostMapping
    @Operation(summary = "Add item to cart")
    public ResponseEntity<?> addItem(@Valid @RequestBody CartRequest req) {
        return ResponseEntity.ok(mapper.toResponse(service.addItem(req)));
    }

    @DeleteMapping("/{cartId}/user/{userId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<?> removeItem(@PathVariable Long cartId, @PathVariable Long userId) {
        try { service.removeItem(cartId, userId); return ResponseEntity.ok("Item removed"); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Clear cart (delete selected items)")
    public ResponseEntity<?> clearCart(@PathVariable Long userId, @RequestParam List<Long> cartIds) {
        service.clearCart(userId, cartIds);
        return ResponseEntity.ok("Cart cleared");
    }
}