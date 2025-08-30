package com.ecommerce.product.mapper;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductListDTO;
import com.ecommerce.product.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    
    ProductDTO toDTO(Product product);
    
    Product toEntity(ProductDTO productDTO);
    
    ProductListDTO toListDTO(Product product);
    
    List<ProductDTO> toDTOList(List<Product> products);
    
    List<ProductListDTO> toListDTOList(List<Product> products);
}
