package com.swann.productservice.controller;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
@Tag(name = "Product Management", description = "APIs for managing products and inventory in the e-commerce system")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Create a new product", description = "Creates a new product in the catalog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        log.info("Received request to create product with name: {}", request.getName());
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get product by ID", description = "Retrieves a specific product by its unique identifier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found and returned"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProductById(@Parameter(description = "Unique identifier of the product") @PathVariable UUID productId) {
        log.info("Received request to get product with ID: {}", productId);
        try {
            ProductResponse response = productService.getProductById(productId);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            log.warn("Product not found with ID: {}", productId);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Update product stock", description = "Updates the stock quantity for a specific product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product stock updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{productId}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @Parameter(description = "Unique identifier of the product") @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductStockRequest request) {
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