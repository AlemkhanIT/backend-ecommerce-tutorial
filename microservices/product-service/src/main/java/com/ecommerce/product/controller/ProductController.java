package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDTO;
import com.ecommerce.product.dto.ProductListDTO;
import com.ecommerce.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Products", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, 
                                                   @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProduct(@PathVariable Long id) {
        ProductDTO product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<Page<ProductListDTO>> getAllProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductListDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<Page<ProductListDTO>> getProductsByCategory(
            @PathVariable String category,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductListDTO> products = productService.getProductsByCategory(category, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products")
    public ResponseEntity<Page<ProductListDTO>> searchProducts(
            @RequestParam String q,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductListDTO> products = productService.searchProducts(q, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    public ResponseEntity<Page<ProductListDTO>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductListDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    @Operation(summary = "Get available products (in stock)")
    public ResponseEntity<Page<ProductListDTO>> getAvailableProducts(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProductListDTO> products = productService.getAvailableProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all product categories")
    public ResponseEntity<List<String>> getAllCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/ids")
    @Operation(summary = "Get products by IDs")
    public ResponseEntity<List<ProductDTO>> getProductsByIds(@RequestParam List<Long> ids) {
        List<ProductDTO> products = productService.getProductsByIds(ids);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get products with low stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        List<ProductDTO> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/update-stock")
    @Operation(summary = "Update product stock")
    public ResponseEntity<Void> updateStock(@RequestBody Map<String, Object> stockUpdateRequest) {
        Long productId = Long.valueOf(stockUpdateRequest.get("productId").toString());
        Integer quantity = Integer.valueOf(stockUpdateRequest.get("quantity").toString());
        String operation = stockUpdateRequest.get("operation").toString();
        
        if ("decrease".equals(operation)) {
            productService.updateStock(productId, quantity);
        } else if ("increase".equals(operation)) {
            productService.restoreStock(productId, quantity);
        }
        
        return ResponseEntity.ok().build();
    }
}
