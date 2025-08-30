package com.ecommerce.cart.client;

import com.ecommerce.cart.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ProductServiceClientFallback implements ProductServiceClient {

    @Override
    public ProductDTO getProduct(Long id) {
        log.warn("Product service is unavailable, returning null for product ID: {}", id);
        return null;
    }

    @Override
    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        log.warn("Product service is unavailable, returning empty list for product IDs: {}", ids);
        return Collections.emptyList();
    }
}
