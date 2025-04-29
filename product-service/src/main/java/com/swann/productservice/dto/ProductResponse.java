package com.swann.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private UUID productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}