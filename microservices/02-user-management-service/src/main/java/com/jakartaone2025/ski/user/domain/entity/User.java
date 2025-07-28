package com.jakartaone2025.ski.user.domain.entity;

import com.jakartaone2025.ski.user.domain.valueobject.PersonalInfo;
import com.jakartaone2025.ski.user.domain.valueobject.AccountStatus;
import com.jakartaone2025.ski.user.domain.valueobject.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ユーザーエンティティ
 * スキーリゾートショップのユーザー情報を管理するメインエンティティ
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email", unique = true),
    @Index(name = "idx_users_status", columnList = "status"),
    @Index(name = "idx_users_created_at", columnList = "created_at")
})
@NamedQueries({
    @NamedQuery(
        name = "User.findByEmail",
        query = "SELECT u FROM User u WHERE u.email = :email"
    ),
    @NamedQuery(
        name = "User.findActiveUsers",
        query = "SELECT u FROM User u WHERE u.accountStatus.status = :status"
    ),
    @NamedQuery(
        name = "User.searchUsers",
        query = """
            SELECT u FROM User u WHERE
            (:searchTerm IS NULL OR 
             LOWER(u.personalInfo.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(u.personalInfo.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            AND (:statuses IS NULL OR u.accountStatus.status IN :statuses)
            ORDER BY u.createdAt DESC
            """
    )
})
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    
    @Email(message = "有効なメールアドレスを入力してください")
    @NotBlank(message = "メールアドレスは必須です")
    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;
    
    @NotBlank(message = "パスワードハッシュは必須です")
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Valid
    @Embedded
    @NotNull(message = "個人情報は必須です")
    private PersonalInfo personalInfo;
    
    @Valid
    @Embedded
    @NotNull(message = "アカウント状態は必須です")
    private AccountStatus accountStatus;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<UserPreference> preferences = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    // Constructors
    protected User() {
        // JPAのため
    }
    
    public User(String email, String passwordHash, PersonalInfo personalInfo) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.personalInfo = personalInfo;
        this.accountStatus = new AccountStatus(
            UserStatus.PENDING_VERIFICATION,
            false,
            false,
            null,
            0,
            null
        );
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
    public void updatePersonalInfo(PersonalInfo newPersonalInfo) {
        this.personalInfo = newPersonalInfo;
    }
    
    public void activateAccount() {
        this.accountStatus = this.accountStatus.withStatus(UserStatus.ACTIVE)
                                              .withEmailVerified(true);
    }
    
    public void suspendAccount() {
        this.accountStatus = this.accountStatus.withStatus(UserStatus.SUSPENDED);
    }
    
    public void deactivateAccount() {
        this.accountStatus = this.accountStatus.withStatus(UserStatus.DEACTIVATED);
    }
    
    public void recordLoginFailure() {
        int newFailureCount = this.accountStatus.loginFailureCount() + 1;
        LocalDateTime lockoutUntil = null;
        
        // 5回失敗したら30分ロックアウト
        if (newFailureCount >= 5) {
            lockoutUntil = LocalDateTime.now().plusMinutes(30);
        }
        
        this.accountStatus = this.accountStatus
            .withLoginFailureCount(newFailureCount)
            .withLockoutUntil(lockoutUntil);
    }
    
    public void recordSuccessfulLogin() {
        this.accountStatus = this.accountStatus
            .withLastLoginAt(LocalDateTime.now())
            .withLoginFailureCount(0)
            .withLockoutUntil(null);
    }
    
    public void addAddress(Address address) {
        address.setUser(this);
        this.addresses.add(address);
    }
    
    public void removeAddress(Address address) {
        this.addresses.remove(address);
        address.setUser(null);
    }
    
    public void addPreference(UserPreference preference) {
        preference.setUser(this);
        this.preferences.add(preference);
    }
    
    public boolean isAccountLocked() {
        return this.accountStatus.isLocked();
    }
    
    public boolean isActive() {
        return this.accountStatus.isActive();
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public PersonalInfo getPersonalInfo() {
        return personalInfo;
    }
    
    public void setPersonalInfo(PersonalInfo personalInfo) {
        this.personalInfo = personalInfo;
    }
    
    public AccountStatus getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public List<Address> getAddresses() {
        return new ArrayList<>(addresses);
    }
    
    public List<UserPreference> getPreferences() {
        return new ArrayList<>(preferences);
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
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("User{id=%s, email='%s', status=%s}", 
            id, email, accountStatus.status());
    }
}
