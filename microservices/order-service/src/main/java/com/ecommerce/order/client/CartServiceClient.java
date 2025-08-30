package com.ecommerce.order.client;

import com.ecommerce.order.dto.CartDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "cart-service", fallback = CartServiceClientFallback.class)
public interface CartServiceClient {
    
    @GetMapping("/api/cart/order")
    CartDTO getCartForOrder(@RequestHeader("X-User-Id") Long userId);
}
