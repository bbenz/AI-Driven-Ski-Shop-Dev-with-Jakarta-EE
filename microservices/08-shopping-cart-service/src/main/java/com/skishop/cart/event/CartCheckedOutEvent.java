package com.skishop.cart.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartCheckedOutEvent(
    String eventId,
    String cartId,
    UUID customerId,
    BigDecimal totalAmount,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_CHECKED_OUT";
    }
}
