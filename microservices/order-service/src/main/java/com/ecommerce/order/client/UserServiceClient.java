package com.ecommerce.order.client;

import com.ecommerce.order.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    
    @GetMapping("/api/auth/user/{id}")
    UserDTO getUserById(@PathVariable Long id);
}
