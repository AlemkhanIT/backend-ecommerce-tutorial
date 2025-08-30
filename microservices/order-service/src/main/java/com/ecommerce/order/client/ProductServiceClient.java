package com.ecommerce.order.client;

import com.ecommerce.order.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {
    
    @PostMapping("/api/products/update-stock")
    void updateStock(@RequestBody Map<String, Object> stockUpdateRequest);
}
