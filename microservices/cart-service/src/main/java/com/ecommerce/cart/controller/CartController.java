package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDTO;
import com.ecommerce.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get user's cart")
    public ResponseEntity<CartDTO> getCart(@RequestHeader("X-User-Id") Long userId) {
        CartDTO cart = cartService.getCart(userId);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartDTO> addToCart(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam @NotNull Long productId,
            @RequestParam @Min(1) Integer quantity) {
        CartDTO cart = cartService.addToCart(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<CartDTO> removeFromCart(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId) {
        CartDTO cart = cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{productId}/quantity")
    @Operation(summary = "Update item quantity in cart")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long productId,
            @RequestParam @Min(0) Integer quantity) {
        CartDTO cart = cartService.updateCartItemQuantity(userId, productId, quantity);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    @Operation(summary = "Clear user's cart")
    public ResponseEntity<Void> clearCart(@RequestHeader("X-User-Id") Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/order")
    @Operation(summary = "Get cart for order processing")
    public ResponseEntity<CartDTO> getCartForOrder(@RequestHeader("X-User-Id") Long userId) {
        CartDTO cart = cartService.getCartForOrder(userId);
        return ResponseEntity.ok(cart);
    }
}
