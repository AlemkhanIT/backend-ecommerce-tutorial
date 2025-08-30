package com.ecommerce.order.client;

import com.ecommerce.order.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserServiceClientFallback implements UserServiceClient {

    @Override
    public UserDTO getUserById(Long id) {
        log.warn("User service is unavailable, returning null for user ID: {}", id);
        return null;
    }
}
