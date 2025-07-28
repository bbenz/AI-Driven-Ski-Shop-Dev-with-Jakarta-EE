package com.skishop.cart.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CartMergedEvent(
    String eventId,
    String sourceCartId,
    String targetCartId,
    UUID customerId,
    LocalDateTime timestamp
) implements CartEvent {
    @Override
    public String getEventType() {
        return "CART_MERGED";
    }
}
