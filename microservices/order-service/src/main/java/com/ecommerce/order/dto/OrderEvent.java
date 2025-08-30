package com.ecommerce.order.dto;

import com.ecommerce.order.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String eventType;
    private Long orderId;
    private Long userId;
    private Order.OrderStatus status;
    private BigDecimal totalAmount;
    private String stripePaymentIntentId;
    private List<OrderItemDTO> items;
    private LocalDateTime timestamp;
    
    public static OrderEvent orderCreated(OrderDTO order) {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_CREATED");
        event.setOrderId(order.getId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setStripePaymentIntentId(order.getStripePaymentIntentId());
        event.setItems(order.getItems());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static OrderEvent orderPaymentConfirmed(OrderDTO order) {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_PAYMENT_CONFIRMED");
        event.setOrderId(order.getId());
        event.setUserId(order.getUserId());
        event.setStatus(order.getStatus());
        event.setTotalAmount(order.getTotalAmount());
        event.setStripePaymentIntentId(order.getStripePaymentIntentId());
        event.setItems(order.getItems());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static OrderEvent orderStatusUpdated(Long orderId, Order.OrderStatus status) {
        OrderEvent event = new OrderEvent();
        event.setEventType("ORDER_STATUS_UPDATED");
        event.setOrderId(orderId);
        event.setStatus(status);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
