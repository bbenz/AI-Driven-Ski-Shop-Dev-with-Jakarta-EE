package com.skishop.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error response DTO
 */
public record ErrorResponse(
    @JsonProperty("errorCode")
    String errorCode,
    
    @JsonProperty("message")
    String message
) {}
