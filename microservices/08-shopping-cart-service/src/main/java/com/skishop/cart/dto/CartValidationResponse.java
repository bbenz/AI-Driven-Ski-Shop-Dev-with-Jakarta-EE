package com.skishop.cart.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Response DTO for cart validation
 */
public record CartValidationResponse(
    @JsonProperty("valid")
    boolean valid,
    
    @JsonProperty("errors")
    List<String> errors
) {}
