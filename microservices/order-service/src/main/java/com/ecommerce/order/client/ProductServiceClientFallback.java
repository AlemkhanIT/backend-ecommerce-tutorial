package com.ecommerce.order.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public void updateStock(Map<String, Object> stockUpdateRequest) {
        log.warn("Product service is unavailable, cannot update stock for request: {}", stockUpdateRequest);
    }
}
