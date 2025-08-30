package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductEvent;
import com.ecommerce.product.dto.ProductListDTO;
import com.ecommerce.product.exception.InsufficientStockException;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.mapper.ProductMapper;
import com.ecommerce.product.model.Product;
import com.ecommerce.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);
        
        ProductDTO savedProductDTO = productMapper.toDTO(savedProduct);
        
        // Publish product created event
        ProductEvent event = ProductEvent.productCreated(savedProductDTO);
        kafkaTemplate.send("product-events", event);
        
        log.info("Product created: {}", savedProduct.getName());
        return savedProductDTO;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setStock(productDTO.getStock());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setImageUrl(productDTO.getImageUrl());

        Product updatedProduct = productRepository.save(existingProduct);
        ProductDTO updatedProductDTO = productMapper.toDTO(updatedProduct);
        
        // Publish product updated event
        ProductEvent event = ProductEvent.productUpdated(updatedProductDTO);
        kafkaTemplate.send("product-events", event);
        
        log.info("Product updated: {}", updatedProduct.getName());
        return updatedProductDTO;
    }

    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        productRepository.delete(product);
        
        // Publish product deleted event
        ProductEvent event = ProductEvent.productDeleted(id);
        kafkaTemplate.send("product-events", event);
        
        log.info("Product deleted: {}", product.getName());
    }

    @Cacheable(value = "products", key = "#id")
    public ProductDTO getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDTO(product);
    }

    @Cacheable(value = "products", key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductListDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(productMapper::toListDTO);
    }

    @Cacheable(value = "products", key = "'category_' + #category + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductListDTO> getProductsByCategory(String category, Pageable pageable) {
        Page<Product> products = productRepository.findByCategory(category, pageable);
        return products.map(productMapper::toListDTO);
    }

    @Cacheable(value = "products", key = "'search_' + #searchTerm + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductListDTO> searchProducts(String searchTerm, Pageable pageable) {
        Page<Product> products = productRepository.findByNameOrDescriptionContaining(searchTerm, searchTerm, pageable);
        return products.map(productMapper::toListDTO);
    }

    @Cacheable(value = "products", key = "'price_' + #minPrice + '_' + #maxPrice + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductListDTO> getProductsByPriceRange(java.math.BigDecimal minPrice, 
                                                        java.math.BigDecimal maxPrice, 
                                                        Pageable pageable) {
        Page<Product> products = productRepository.findByPriceRange(minPrice, maxPrice, pageable);
        return products.map(productMapper::toListDTO);
    }

    @Cacheable(value = "products", key = "'available_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<ProductListDTO> getAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAvailableProducts(pageable);
        return products.map(productMapper::toListDTO);
    }

    @Cacheable(value = "categories")
    public List<String> getAllCategories() {
        return productRepository.findAllCategories();
    }

    public List<ProductDTO> getProductsByIds(List<Long> ids) {
        List<Product> products = productRepository.findByIds(ids);
        return productMapper.toDTOList(products);
    }

    @Transactional
    public void updateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
        }

        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
        
        // Publish stock updated event
        ProductEvent event = ProductEvent.stockUpdated(productId, product.getStock());
        kafkaTemplate.send("product-events", event);
        
        log.info("Stock updated for product {}: {}", product.getName(), product.getStock());
    }

    @Transactional
    public void restoreStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
        
        // Publish stock updated event
        ProductEvent event = ProductEvent.stockUpdated(productId, product.getStock());
        kafkaTemplate.send("product-events", event);
        
        log.info("Stock restored for product {}: {}", product.getName(), product.getStock());
    }

    public List<ProductDTO> getLowStockProducts(Integer threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return productMapper.toDTOList(products);
    }
}
