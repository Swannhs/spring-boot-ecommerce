package com.swann.productservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ObjectMapper objectMapper;

    private UUID productId;
    private ProductResponse productResponse;
    private CreateProductRequest createProductRequest;
    private UpdateProductStockRequest updateProductStockRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
        
        productId = UUID.randomUUID();
        
        productResponse = ProductResponse.builder()
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
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(productResponse);

        mockMvc.perform(post("/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createProductRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void getProductById_ShouldReturnProduct() throws Exception {
        when(productService.getProductById(productId)).thenReturn(productResponse);

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.getProductById(productId)).thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(get("/products/{productId}", productId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateProductStock_ShouldReturnUpdatedProduct() throws Exception {
        ProductResponse updatedResponse = ProductResponse.builder()
                .productId(productId)
                .name("Test Product")
                .price(new BigDecimal("19.99"))
                .stock(90)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(productService.updateProductStock(eq(productId), any(UpdateProductStockRequest.class)))
                .thenReturn(updatedResponse);

        mockMvc.perform(put("/products/{productId}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductStockRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(19.99))
                .andExpect(jsonPath("$.stock").value(90));
    }

    @Test
    void updateProductStock_WhenProductNotFound_ShouldReturnNotFound() throws Exception {
        when(productService.updateProductStock(eq(productId), any(UpdateProductStockRequest.class)))
                .thenThrow(new EntityNotFoundException("Product not found"));

        mockMvc.perform(put("/products/{productId}/stock", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateProductStockRequest)))
                .andExpect(status().isNotFound());
    }
}