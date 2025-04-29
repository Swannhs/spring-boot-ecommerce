package com.swann.paymentservice.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedEvent extends BaseEvent {
    private UUID orderId;
    private UUID customerId;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;

    @Data
    @NoArgsConstructor
    public static class OrderItemDto {
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;
    }
}