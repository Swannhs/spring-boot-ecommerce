package com.swann.paymentservice.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PaymentProcessedEvent extends BaseEvent {
    private UUID paymentId;
    private UUID orderId;
    private BigDecimal amount;
    private String status;

    public PaymentProcessedEvent(UUID paymentId, UUID orderId, BigDecimal amount, String status) {
        super("PAYMENT_PROCESSED");
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
    }
}