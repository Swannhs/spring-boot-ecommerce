package com.swann.paymentservice.consumer;

import com.swann.paymentservice.config.KafkaConfig;
import com.swann.paymentservice.event.OrderCreatedEvent;
import com.swann.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = KafkaConfig.ORDER_CREATED_TOPIC, groupId = "payment-service-group")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Received order created event for order: {}", event.getOrderId());
        
        try {
            // Process payment for the order
            paymentService.processPayment(
                    event.getOrderId(),
                    event.getCustomerId(),
                    event.getTotalAmount()
            );
            
            log.info("Successfully processed payment for order: {}", event.getOrderId());
        } catch (Exception e) {
            log.error("Failed to process payment for order: {}", event.getOrderId(), e);
            // In a real application, we would implement retry logic or dead letter queue
        }
    }
}