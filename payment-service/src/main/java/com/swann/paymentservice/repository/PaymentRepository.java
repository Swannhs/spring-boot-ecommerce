package com.swann.paymentservice.repository;

import com.swann.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    /**
     * Find payment by order ID
     * 
     * @param orderId the order ID
     * @return optional payment
     */
    Optional<Payment> findByOrderId(UUID orderId);
}