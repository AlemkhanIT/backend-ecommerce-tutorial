package com.ecommerce.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;
}
