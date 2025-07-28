package com.skiresort.auth.repository;

import com.skiresort.auth.entity.UserCredential;
import com.skiresort.auth.entity.CredentialStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.time.LocalDateTime;

/**
 * ユーザー認証情報リポジトリ実装
 */
@ApplicationScoped
@Transactional
public class UserCredentialRepositoryImpl implements UserCredentialRepository {
    
    @PersistenceContext(unitName = "authPU")
    private EntityManager entityManager;
    
    @Override
    public Optional<UserCredential> findById(UUID id) {
        UserCredential credential = entityManager.find(UserCredential.class, id);
        return Optional.ofNullable(credential);
    }
    
    @Override
    public Optional<UserCredential> findByUserId(UUID userId) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.userId = :userId", 
            UserCredential.class
        );
        query.setParameter("userId", userId);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<UserCredential> findByUsername(String username) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.username = :username", 
            UserCredential.class
        );
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<UserCredential> findByEmail(String email) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.email = :email", 
            UserCredential.class
        );
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<UserCredential> findByEmailVerificationToken(String token) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.emailVerificationToken = :token", 
            UserCredential.class
        );
        query.setParameter("token", token);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<UserCredential> findByPasswordResetToken(String token) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.passwordResetToken = :token", 
            UserCredential.class
        );
        query.setParameter("token", token);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<UserCredential> findByMfaSecretKey(String secretKey) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.mfaSecretKey = :secretKey", 
            UserCredential.class
        );
        query.setParameter("secretKey", secretKey);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public List<UserCredential> findByStatus(CredentialStatus status) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.status = :status", 
            UserCredential.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.createdAt BETWEEN :startDate AND :endDate", 
            UserCredential.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.lastLoginAt BETWEEN :startDate AND :endDate", 
            UserCredential.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findLockedAccounts() {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.accountLockedUntil IS NOT NULL AND uc.accountLockedUntil > :now", 
            UserCredential.class
        );
        query.setParameter("now", LocalDateTime.now());
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findUnverifiedEmails() {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.emailVerified = false", 
            UserCredential.class
        );
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findMfaEnabled() {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.mfaEnabled = true", 
            UserCredential.class
        );
        return query.getResultList();
    }
    
    @Override
    public UserCredential save(UserCredential userCredential) {
        if (userCredential.getId() == null) {
            entityManager.persist(userCredential);
            return userCredential;
        } else {
            return entityManager.merge(userCredential);
        }
    }
    
    @Override
    public UserCredential update(UserCredential userCredential) {
        return entityManager.merge(userCredential);
    }
    
    @Override
    public void deleteById(UUID id) {
        UserCredential credential = entityManager.find(UserCredential.class, id);
        if (credential != null) {
            entityManager.remove(credential);
        }
    }
    
    @Override
    public boolean existsByUsername(String username) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(uc) FROM UserCredential uc WHERE uc.username = :username", 
            Long.class
        );
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }
    
    @Override
    public boolean existsByEmail(String email) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(uc) FROM UserCredential uc WHERE uc.email = :email", 
            Long.class
        );
        query.setParameter("email", email);
        return query.getSingleResult() > 0;
    }
    
    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(uc) FROM UserCredential uc", 
            Long.class
        );
        return query.getSingleResult();
    }
    
    @Override
    public long countByStatus(CredentialStatus status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(uc) FROM UserCredential uc WHERE uc.status = :status", 
            Long.class
        );
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    @Override
    public List<UserCredential> findExpiredEmailVerificationTokens(LocalDateTime expirationTime) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.emailVerificationExpiresAt IS NOT NULL AND uc.emailVerificationExpiresAt < :expirationTime", 
            UserCredential.class
        );
        query.setParameter("expirationTime", expirationTime);
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findExpiredPasswordResetTokens(LocalDateTime expirationTime) {
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.passwordResetExpiresAt IS NOT NULL AND uc.passwordResetExpiresAt < :expirationTime", 
            UserCredential.class
        );
        query.setParameter("expirationTime", expirationTime);
        return query.getResultList();
    }
    
    @Override
    public List<UserCredential> findInactiveUsers(int daysSinceLastLogin) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysSinceLastLogin);
        TypedQuery<UserCredential> query = entityManager.createQuery(
            "SELECT uc FROM UserCredential uc WHERE uc.lastLoginAt IS NOT NULL AND uc.lastLoginAt < :threshold", 
            UserCredential.class
        );
        query.setParameter("threshold", threshold);
        return query.getResultList();
    }
}
