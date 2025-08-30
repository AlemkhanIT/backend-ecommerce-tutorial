package com.ecommerce.order.dto;

import com.ecommerce.order.model.Order;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    
    @NotNull
    private Long userId;
    
    @NotBlank
    private String address;
    
    @NotBlank
    private String phoneNumber;
    
    private Order.OrderStatus status;
    
    @NotNull
    private BigDecimal totalAmount;
    
    private String stripePaymentIntentId;
    private String stripeClientSecret;
    
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
