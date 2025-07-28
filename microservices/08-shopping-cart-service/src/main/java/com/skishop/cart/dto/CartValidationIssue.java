package com.skishop.cart.dto;

public record CartValidationIssue(
    String sku,
    String type,
    String message
) {}
