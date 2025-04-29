package com.swann.productservice.service;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.model.Product;
import com.swann.productservice.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());
        
        // Create product entity
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .build();
        
        // Save product
        Product savedProduct = productRepository.save(product);
        
        return mapToProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID productId) {
        log.info("Getting product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProductStock(UUID productId, UpdateProductStockRequest request) {
        log.info("Updating stock for product with ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        
        // Update stock
        product.setStock(request.getStock());
        
        // Save updated product
        Product updatedProduct = productRepository.save(product);
        
        return mapToProductResponse(updatedProduct);
    }
    
    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .productId(product.getProductId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}