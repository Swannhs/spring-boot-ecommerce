package com.swann.productservice.service;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.model.Product;
import com.swann.productservice.repository.ProductRepository;
import com.swann.productservice.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Captor
    private ArgumentCaptor<Product> productCaptor;

    private UUID productId;
    private Product product;
    private CreateProductRequest createProductRequest;
    private UpdateProductStockRequest updateProductStockRequest;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        product = Product.builder()
                .productId(productId)
                .name("Test Product")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createProductRequest = new CreateProductRequest(
                "Test Product",
                new BigDecimal("19.99"),
                100
        );

        updateProductStockRequest = new UpdateProductStockRequest(90);
    }

    @Test
    void createProduct_ShouldCreateProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product savedProduct = invocation.getArgument(0);
            savedProduct.setProductId(productId); // Simulate ID generation
            return savedProduct;
        });

        // When
        ProductResponse response = productService.createProduct(createProductRequest);

        // Then
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertEquals("Test Product", capturedProduct.getName());
        assertEquals(new BigDecimal("19.99"), capturedProduct.getPrice());
        assertEquals(100, capturedProduct.getStock());

        // Verify response
        assertEquals(productId, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("19.99"), response.getPrice());
        assertEquals(100, response.getStock());
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductResponse response = productService.getProductById(productId);

        // Then
        verify(productRepository).findById(productId);
        assertEquals(productId, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("19.99"), response.getPrice());
        assertEquals(100, response.getStock());
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.getProductById(productId));
        verify(productRepository).findById(productId);
    }

    @Test
    void updateProductStock_ShouldUpdateStock() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponse response = productService.updateProductStock(productId, updateProductStockRequest);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).save(productCaptor.capture());
        Product capturedProduct = productCaptor.getValue();
        assertEquals(90, capturedProduct.getStock());

        // Verify response
        assertEquals(productId, response.getProductId());
        assertEquals("Test Product", response.getName());
        assertEquals(new BigDecimal("19.99"), response.getPrice());
        assertEquals(90, response.getStock());
    }

    @Test
    void updateProductStock_WhenProductNotFound_ShouldThrowException() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> productService.updateProductStock(productId, updateProductStockRequest));
        verify(productRepository).findById(productId);
    }
}