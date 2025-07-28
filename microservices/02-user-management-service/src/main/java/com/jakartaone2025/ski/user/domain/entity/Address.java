package com.jakartaone2025.ski.user.domain.entity;

import com.jakartaone2025.ski.user.domain.valueobject.AddressType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 住所エンティティ
 */
@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_addresses_user_id", columnList = "user_id"),
    @Index(name = "idx_addresses_type", columnList = "type"),
    @Index(name = "idx_addresses_default", columnList = "is_default")
})
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "ユーザーは必須です")
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    @NotNull(message = "住所タイプは必須です")
    private AddressType type;
    
    @Pattern(regexp = "^\\d{3}-\\d{4}$", message = "郵便番号は000-0000の形式で入力してください")
    @Column(name = "postal_code", length = 8)
    private String postalCode;
    
    @NotBlank(message = "都道府県は必須です")
    @Size(max = 10, message = "都道府県は10文字以内で入力してください")
    @Column(name = "prefecture", nullable = false, length = 10)
    private String prefecture;
    
    @NotBlank(message = "市区町村は必須です")
    @Size(max = 50, message = "市区町村は50文字以内で入力してください")
    @Column(name = "city", nullable = false, length = 50)
    private String city;
    
    @NotBlank(message = "住所1は必須です")
    @Size(max = 100, message = "住所1は100文字以内で入力してください")
    @Column(name = "address_line1", nullable = false, length = 100)
    private String addressLine1;
    
    @Size(max = 100, message = "住所2は100文字以内で入力してください")
    @Column(name = "address_line2", length = 100)
    private String addressLine2;
    
    @Size(max = 100, message = "建物名は100文字以内で入力してください")
    @Column(name = "building", length = 100)
    private String building;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    protected Address() {
        // JPAのため
    }
    
    public Address(User user, AddressType type, String postalCode, String prefecture, 
                   String city, String addressLine1) {
        this.user = user;
        this.type = type;
        this.postalCode = postalCode;
        this.prefecture = prefecture;
        this.city = city;
        this.addressLine1 = addressLine1;
        this.isDefault = false;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public String getFullAddress() {
        StringBuilder builder = new StringBuilder();
        
        if (postalCode != null && !postalCode.isBlank()) {
            builder.append("〒").append(postalCode).append(" ");
        }
        
        builder.append(prefecture)
               .append(city)
               .append(addressLine1);
        
        if (addressLine2 != null && !addressLine2.isBlank()) {
            builder.append(" ").append(addressLine2);
        }
        
        if (building != null && !building.isBlank()) {
            builder.append(" ").append(building);
        }
        
        return builder.toString();
    }
    
    public void setAsDefault() {
        this.isDefault = true;
    }
    
    public void unsetAsDefault() {
        this.isDefault = false;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public AddressType getType() {
        return type;
    }
    
    public void setType(AddressType type) {
        this.type = type;
    }
    
    public String getPostalCode() {
        return postalCode;
    }
    
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }
    
    public String getPrefecture() {
        return prefecture;
    }
    
    public void setPrefecture(String prefecture) {
        this.prefecture = prefecture;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getAddressLine1() {
        return addressLine1;
    }
    
    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }
    
    public String getAddressLine2() {
        return addressLine2;
    }
    
    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }
    
    public String getBuilding() {
        return building;
    }
    
    public void setBuilding(String building) {
        this.building = building;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Address address = (Address) obj;
        return id != null && id.equals(address.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("Address{id=%s, type=%s, fullAddress='%s'}", 
            id, type, getFullAddress());
    }
}
