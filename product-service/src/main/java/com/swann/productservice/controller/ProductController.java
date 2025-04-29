package com.swann.productservice.controller;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product
     * 
     * @param request the product creation request
     * @return the created product response with 201 Created status
     */
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody CreateProductRequest request) {
        log.info("Received request to create product with name: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a product by ID
     * 
     * @param productId the product ID
     * @return the product response with 200 OK status, or 404 Not Found if product doesn't exist
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID productId) {
        log.info("Received request to get product with ID: {}", productId);
        try {
            ProductResponse response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Product not found with ID: {}", productId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Update product stock
     * 
     * @param productId the product ID
     * @param request the stock update request
     * @return the updated product response with 200 OK status, or 404 Not Found if product doesn't exist
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @PathVariable UUID productId,
            @RequestBody UpdateProductStockRequest request) {
        log.info("Received request to update stock for product with ID: {}", productId);
        try {
            ProductResponse response = productService.updateProductStock(productId, request);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Product not found with ID: {}", productId);
            return ResponseEntity.notFound().build();
        }
    }
}