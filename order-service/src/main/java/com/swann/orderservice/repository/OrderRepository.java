package com.swann.orderservice.repository;

import com.swann.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    
    /**
     * Find all orders for a specific customer
     * 
     * @param customerId the customer ID
     * @return list of orders for the customer
     */
    List<Order> findByCustomerId(UUID customerId);
}