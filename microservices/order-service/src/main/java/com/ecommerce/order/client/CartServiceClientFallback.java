package com.ecommerce.order.client;

import com.ecommerce.order.dto.CartDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartServiceClientFallback implements CartServiceClient {

    @Override
    public CartDTO getCartForOrder(Long userId) {
        log.warn("Cart service is unavailable, returning null for user ID: {}", userId);
        return null;
    }
}
