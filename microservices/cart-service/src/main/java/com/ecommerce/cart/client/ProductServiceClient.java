package com.ecommerce.cart.client;

import com.ecommerce.cart.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", fallback = ProductServiceClientFallback.class)
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ProductDTO getProduct(@PathVariable Long id);
    
    @GetMapping("/api/products/ids")
    List<ProductDTO> getProductsByIds(@RequestParam List<Long> ids);
}
