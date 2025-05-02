package com.swann.productservice.integration;

import com.swann.productservice.dto.CreateProductRequest;
import com.swann.productservice.dto.ProductResponse;
import com.swann.productservice.dto.UpdateProductStockRequest;
import com.swann.productservice.model.Product;
import com.swann.productservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(com.swann.productservice.TestcontainersConfiguration.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1)
@DirtiesContext
@Testcontainers
public class ProductServiceIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/products";
        
        // Clean up the database before each test
        productRepository.deleteAll();
    }

    @Test
    void createProduct_ShouldCreateProductAndReturnResponse() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                new BigDecimal("19.99"),
                100
        );

        // When
        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(baseUrl, request, ProductResponse.class);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductResponse productResponse = response.getBody();
        assertNotNull(productResponse.getProductId());
        assertEquals("Test Product", productResponse.getName());
        assertEquals(new BigDecimal("19.99"), productResponse.getPrice());
        assertEquals(100, productResponse.getStock());

        // Verify the product was saved to the database
        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());
        Product savedProduct = products.get(0);
        assertEquals("Test Product", savedProduct.getName());
        assertEquals(new BigDecimal("19.99"), savedProduct.getPrice());
        assertEquals(100, savedProduct.getStock());
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        // Given
        CreateProductRequest request = new CreateProductRequest(
                "Test Product",
                new BigDecimal("19.99"),
                100
        );

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(baseUrl, request, ProductResponse.class);
        UUID productId = createResponse.getBody().getProductId();

        // When
        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(baseUrl + "/" + productId, ProductResponse.class);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductResponse productResponse = response.getBody();
        assertEquals(productId, productResponse.getProductId());
        assertEquals("Test Product", productResponse.getName());
        assertEquals(new BigDecimal("19.99"), productResponse.getPrice());
        assertEquals(100, productResponse.getStock());
    }

    @Test
    void getProductById_WhenProductNotFound_ShouldReturnNotFound() {
        // When
        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(baseUrl + "/" + UUID.randomUUID(), ProductResponse.class);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateProductStock_ShouldUpdateStockAndReturnUpdatedProduct() {
        // Given
        CreateProductRequest createRequest = new CreateProductRequest(
                "Test Product",
                new BigDecimal("19.99"),
                100
        );

        ResponseEntity<ProductResponse> createResponse = restTemplate.postForEntity(baseUrl, createRequest, ProductResponse.class);
        UUID productId = createResponse.getBody().getProductId();

        UpdateProductStockRequest updateRequest = new UpdateProductStockRequest(90);

        // When
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                baseUrl + "/" + productId + "/stock",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ProductResponse.class
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProductResponse productResponse = response.getBody();
        assertEquals(productId, productResponse.getProductId());
        assertEquals("Test Product", productResponse.getName());
        assertEquals(new BigDecimal("19.99"), productResponse.getPrice());
        assertEquals(90, productResponse.getStock());

        // Verify the product was updated in the database
        Product updatedProduct = productRepository.findById(productId).orElse(null);
        assertNotNull(updatedProduct);
        assertEquals(90, updatedProduct.getStock());
    }

    @Test
    void updateProductStock_WhenProductNotFound_ShouldReturnNotFound() {
        // Given
        UpdateProductStockRequest updateRequest = new UpdateProductStockRequest(90);

        // When
        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                baseUrl + "/" + UUID.randomUUID() + "/stock",
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest),
                ProductResponse.class
        );

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}