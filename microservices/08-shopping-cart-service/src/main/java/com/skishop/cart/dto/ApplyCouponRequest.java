package com.skishop.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplyCouponRequest(
    @NotBlank @Size(max = 50) String couponCode
) {}
