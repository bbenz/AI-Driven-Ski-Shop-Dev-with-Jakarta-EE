package com.skishop.cart.dto;

public record WebSocketMessage(
    String type,
    Object data
) {}
