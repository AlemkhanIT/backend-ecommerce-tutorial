package com.ecommerce.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartEvent {
    private String eventType;
    private Long userId;
    private List<CartItemDTO> items;
    private BigDecimal totalAmount;
    private LocalDateTime timestamp;
    
    public static CartEvent cartUpdated(Long userId, CartDTO cart) {
        CartEvent event = new CartEvent();
        event.setEventType("CART_UPDATED");
        event.setUserId(userId);
        event.setItems(cart.getItems());
        event.setTotalAmount(cart.getTotalAmount());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static CartEvent cartCleared(Long userId) {
        CartEvent event = new CartEvent();
        event.setEventType("CART_CLEARED");
        event.setUserId(userId);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
