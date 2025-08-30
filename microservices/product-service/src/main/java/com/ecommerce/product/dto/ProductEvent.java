package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    private String eventType;
    private Long productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private LocalDateTime timestamp;
    
    public static ProductEvent productCreated(ProductDTO product) {
        ProductEvent event = new ProductEvent();
        event.setEventType("PRODUCT_CREATED");
        event.setProductId(product.getId());
        event.setName(product.getName());
        event.setPrice(product.getPrice());
        event.setStock(product.getStock());
        event.setCategory(product.getCategory());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static ProductEvent productUpdated(ProductDTO product) {
        ProductEvent event = new ProductEvent();
        event.setEventType("PRODUCT_UPDATED");
        event.setProductId(product.getId());
        event.setName(product.getName());
        event.setPrice(product.getPrice());
        event.setStock(product.getStock());
        event.setCategory(product.getCategory());
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static ProductEvent productDeleted(Long productId) {
        ProductEvent event = new ProductEvent();
        event.setEventType("PRODUCT_DELETED");
        event.setProductId(productId);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static ProductEvent stockUpdated(Long productId, Integer newStock) {
        ProductEvent event = new ProductEvent();
        event.setEventType("STOCK_UPDATED");
        event.setProductId(productId);
        event.setStock(newStock);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
}
