package com.swann.productservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "product_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductReservation {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "reservation_id", updatable = false, nullable = false)
    private UUID reservationId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}