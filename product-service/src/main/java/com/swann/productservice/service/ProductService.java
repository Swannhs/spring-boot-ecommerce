package com.swann.productservice.service;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;

import java.util.UUID;

public interface ProductService {
    
    /**
     * Create a new product
     * 
     * @param request the product creation request
     * @return the created product response
     */
    ProductResponse createProduct(CreateProductRequest request);
    
    /**
     * Get a product by ID
     * 
     * @param productId the product ID
     * @return the product response
     */
    ProductResponse getProductById(UUID productId);
    
    /**
     * Update product stock
     * 
     * @param productId the product ID
     * @param request the stock update request
     * @return the updated product response
     */
    ProductResponse updateProductStock(UUID productId, UpdateProductStockRequest request);
}