package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductServiceClient;
import com.ecommerce.cart.dto.*;
import com.ecommerce.cart.exception.CartNotFoundException;
import com.ecommerce.cart.exception.InsufficientStockException;
import com.ecommerce.cart.exception.ProductNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProductServiceClient productServiceClient;
    private final KafkaTemplate<String, CartEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final long CART_EXPIRY_HOURS = 24;

    public CartDTO getCart(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        String cartJson = redisTemplate.opsForValue().get(cartKey);
        
        if (cartJson == null) {
            return createEmptyCart(userId);
        }

        try {
            CartDTO cart = objectMapper.readValue(cartJson, CartDTO.class);
            return enrichCartWithProductDetails(cart);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing cart for user {}: {}", userId, e.getMessage());
            return createEmptyCart(userId);
        }
    }

    public CartDTO addToCart(Long userId, Long productId, Integer quantity) {
        ProductDTO product = productServiceClient.getProduct(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        CartDTO cart = getCart(userId);
        
        // Check if product already exists in cart
        CartItemDTO existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Add new item
            CartItemDTO newItem = new CartItemDTO();
            newItem.setProductId(productId);
            newItem.setProductName(product.getName());
            newItem.setPrice(product.getPrice());
            newItem.setQuantity(quantity);
            newItem.setImageUrl(product.getImageUrl());
            cart.getItems().add(newItem);
        }

        cart.setLastUpdated(LocalDateTime.now());
        saveCart(userId, cart);

        // Publish cart updated event
        CartEvent event = CartEvent.cartUpdated(userId, cart);
        kafkaTemplate.send("cart-events", event);

        log.info("Added {} units of product {} to cart for user {}", quantity, product.getName(), userId);
        return cart;
    }

    public CartDTO removeFromCart(Long userId, Long productId) {
        CartDTO cart = getCart(userId);
        
        cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        cart.setLastUpdated(LocalDateTime.now());
        
        saveCart(userId, cart);

        // Publish cart updated event
        CartEvent event = CartEvent.cartUpdated(userId, cart);
        kafkaTemplate.send("cart-events", event);

        log.info("Removed product {} from cart for user {}", productId, userId);
        return cart;
    }

    public CartDTO updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }

        CartDTO cart = getCart(userId);
        
        CartItemDTO item = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartNotFoundException("Product not found in cart"));

        // Check stock availability
        ProductDTO product = productServiceClient.getProduct(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product not found with id: " + productId);
        }

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        item.setQuantity(quantity);
        cart.setLastUpdated(LocalDateTime.now());
        
        saveCart(userId, cart);

        // Publish cart updated event
        CartEvent event = CartEvent.cartUpdated(userId, cart);
        kafkaTemplate.send("cart-events", event);

        log.info("Updated quantity of product {} to {} in cart for user {}", productId, quantity, userId);
        return cart;
    }

    public void clearCart(Long userId) {
        String cartKey = CART_KEY_PREFIX + userId;
        redisTemplate.delete(cartKey);

        // Publish cart cleared event
        CartEvent event = CartEvent.cartCleared(userId);
        kafkaTemplate.send("cart-events", event);

        log.info("Cleared cart for user {}", userId);
    }

    public CartDTO getCartForOrder(Long userId) {
        CartDTO cart = getCart(userId);
        
        // Validate all items are still available
        List<CartItemDTO> validItems = new ArrayList<>();
        for (CartItemDTO item : cart.getItems()) {
            ProductDTO product = productServiceClient.getProduct(item.getProductId());
            if (product != null && product.getStock() >= item.getQuantity()) {
                validItems.add(item);
            } else {
                log.warn("Product {} is no longer available or has insufficient stock", item.getProductId());
            }
        }
        
        cart.setItems(validItems);
        return cart;
    }

    private CartDTO createEmptyCart(Long userId) {
        CartDTO cart = new CartDTO();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setLastUpdated(LocalDateTime.now());
        return cart;
    }

    private CartDTO enrichCartWithProductDetails(CartDTO cart) {
        if (cart.getItems().isEmpty()) {
            return cart;
        }

        List<Long> productIds = cart.getItems().stream()
                .map(CartItemDTO::getProductId)
                .toList();

        List<ProductDTO> products = productServiceClient.getProductsByIds(productIds);
        
        // Update cart items with current product information
        for (CartItemDTO item : cart.getItems()) {
            ProductDTO product = products.stream()
                    .filter(p -> p.getId().equals(item.getProductId()))
                    .findFirst()
                    .orElse(null);
            
            if (product != null) {
                item.setProductName(product.getName());
                item.setPrice(product.getPrice());
                item.setImageUrl(product.getImageUrl());
            }
        }

        return cart;
    }

    private void saveCart(Long userId, CartDTO cart) {
        String cartKey = CART_KEY_PREFIX + userId;
        try {
            String cartJson = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(cartKey, cartJson, CART_EXPIRY_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            log.error("Error serializing cart for user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to save cart", e);
        }
    }
}
