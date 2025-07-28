package com.skiresort.order.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * 配送先住所エンベッダブルクラス
 */
@Embeddable
public record ShippingAddress(
    @Column(name = "shipping_first_name", length = 50)
    String firstName,
    
    @Column(name = "shipping_last_name", length = 50)
    String lastName,
    
    @Column(name = "shipping_postal_code", length = 10)
    String postalCode,
    
    @Column(name = "shipping_prefecture", length = 20)
    String prefecture,
    
    @Column(name = "shipping_city", length = 100)
    String city,
    
    @Column(name = "shipping_address_line1", length = 200)
    String addressLine1,
    
    @Column(name = "shipping_address_line2", length = 200)
    String addressLine2,
    
    @Column(name = "shipping_phone_number", length = 20)
    String phoneNumber
) {
    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }
    
    public String getFullAddress() {
        var builder = new StringBuilder();
        
        if (postalCode != null && !postalCode.isBlank()) {
            builder.append("〒").append(postalCode).append(" ");
        }
        
        if (prefecture != null) {
            builder.append(prefecture);
        }
        
        if (city != null) {
            builder.append(city);
        }
        
        if (addressLine1 != null) {
            builder.append(addressLine1);
        }
            
        if (addressLine2 != null && !addressLine2.isBlank()) {
            builder.append(" ").append(addressLine2);
        }
        
        return builder.toString();
    }
    
    public boolean isComplete() {
        return firstName != null && !firstName.isBlank() &&
               lastName != null && !lastName.isBlank() &&
               postalCode != null && !postalCode.isBlank() &&
               prefecture != null && !prefecture.isBlank() &&
               city != null && !city.isBlank() &&
               addressLine1 != null && !addressLine1.isBlank() &&
               phoneNumber != null && !phoneNumber.isBlank();
    }
}
