package com.skishop.cart.dto;

import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(
    @NotBlank String sourceCartId
) {}
