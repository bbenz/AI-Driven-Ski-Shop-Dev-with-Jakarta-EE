package com.skiresort.inventory.dto;

import com.skiresort.inventory.model.ReservationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 在庫予約応答DTO
 */
public record StockReservationResponse(
    UUID id,
    UUID inventoryItemId,
    String sku,
    UUID orderId,
    UUID customerId,
    Integer reservedQuantity,
    ReservationStatus status,
    LocalDateTime expiresAt,
    LocalDateTime createdAt,
    LocalDateTime confirmedAt,
    LocalDateTime cancelledAt,
    String cancellationReason,
    boolean isExpired,
    boolean canConfirm,
    boolean canCancel
) {
    public static StockReservationResponse from(com.skiresort.inventory.model.StockReservation reservation) {
        return new StockReservationResponse(
            reservation.getId(),
            reservation.getInventoryItem().getId(),
            reservation.getInventoryItem().getSku(),
            reservation.getOrderId(),
            reservation.getCustomerId(),
            reservation.getReservedQuantity(),
            reservation.getStatus(),
            reservation.getExpiresAt(),
            reservation.getCreatedAt(),
            reservation.getConfirmedAt(),
            reservation.getCancelledAt(),
            reservation.getCancellationReason(),
            reservation.isExpired(),
            reservation.canConfirm(),
            reservation.canCancel()
        );
    }
}
