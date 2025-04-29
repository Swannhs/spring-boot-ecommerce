package com.swann.orderservice.event;

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

    public OrderCreatedEvent(UUID orderId, UUID customerId, BigDecimal totalAmount, 
                            String status, List<OrderItemDto> items) {
        super("ORDER_CREATED");
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.status = status;
        this.items = items;
    }

    @Data
    @NoArgsConstructor
    public static class OrderItemDto {
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;

        public OrderItemDto(UUID productId, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}