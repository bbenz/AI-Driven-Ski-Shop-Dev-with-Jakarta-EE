package com.skiresort.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * 注文金額エンベッダブルクラス
 */
@Embeddable
public record OrderAmount(
    @Column(name = "subtotal_amount", precision = 12, scale = 2)
    BigDecimal subtotalAmount,
    
    @Column(name = "discount_amount", precision = 12, scale = 2)
    BigDecimal discountAmount,
    
    @Column(name = "tax_amount", precision = 12, scale = 2)
    BigDecimal taxAmount,
    
    @Column(name = "shipping_amount", precision = 12, scale = 2)
    BigDecimal shippingAmount,
    
    @Column(name = "total_amount", precision = 12, scale = 2)
    BigDecimal totalAmount,
    
    @Column(name = "currency", length = 3)
    String currency
) {
    public static OrderAmount create(BigDecimal subtotal, BigDecimal discount, 
                                   BigDecimal tax, BigDecimal shipping) {
        var total = subtotal.subtract(discount != null ? discount : BigDecimal.ZERO)
                           .add(tax != null ? tax : BigDecimal.ZERO)
                           .add(shipping != null ? shipping : BigDecimal.ZERO);
        return new OrderAmount(
            subtotal, 
            discount != null ? discount : BigDecimal.ZERO, 
            tax != null ? tax : BigDecimal.ZERO, 
            shipping != null ? shipping : BigDecimal.ZERO, 
            total, 
            "JPY"
        );
    }
    
    public static OrderAmount zero() {
        return new OrderAmount(
            BigDecimal.ZERO, 
            BigDecimal.ZERO, 
            BigDecimal.ZERO, 
            BigDecimal.ZERO, 
            BigDecimal.ZERO, 
            "JPY"
        );
    }
}
