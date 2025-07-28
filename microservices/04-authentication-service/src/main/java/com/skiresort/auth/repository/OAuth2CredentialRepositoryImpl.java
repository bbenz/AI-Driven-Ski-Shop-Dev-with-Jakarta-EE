package com.skiresort.auth.repository;

import com.skiresort.auth.entity.OAuth2Credential;
import com.skiresort.auth.entity.OAuth2Status;
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
 * OAuth2認証情報リポジトリ実装
 */
@ApplicationScoped
@Transactional
public class OAuth2CredentialRepositoryImpl implements OAuth2CredentialRepository {
    
    @PersistenceContext(unitName = "authPU")
    private EntityManager entityManager;
    
    @Override
    public Optional<OAuth2Credential> findById(UUID id) {
        OAuth2Credential credential = entityManager.find(OAuth2Credential.class, id);
        return Optional.ofNullable(credential);
    }
    
    @Override
    public List<OAuth2Credential> findByUserId(UUID userId) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.userId = :userId", 
            OAuth2Credential.class
        );
        query.setParameter("userId", userId);
        return query.getResultList();
    }
    
    @Override
    public Optional<OAuth2Credential> findByUserIdAndProvider(UUID userId, String provider) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.userId = :userId AND oc.provider = :provider", 
            OAuth2Credential.class
        );
        query.setParameter("userId", userId);
        query.setParameter("provider", provider);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<OAuth2Credential> findByProviderUserIdAndProvider(String providerUserId, String provider) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.providerUserId = :providerUserId AND oc.provider = :provider", 
            OAuth2Credential.class
        );
        query.setParameter("providerUserId", providerUserId);
        query.setParameter("provider", provider);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public Optional<OAuth2Credential> findByProviderEmailAndProvider(String email, String provider) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.email = :email AND oc.provider = :provider", 
            OAuth2Credential.class
        );
        query.setParameter("email", email);
        query.setParameter("provider", provider);
        return query.getResultStream().findFirst();
    }
    
    @Override
    public List<OAuth2Credential> findByProvider(String provider) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.provider = :provider", 
            OAuth2Credential.class
        );
        query.setParameter("provider", provider);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findByStatus(OAuth2Status status) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.status = :status", 
            OAuth2Credential.class
        );
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.createdAt BETWEEN :startDate AND :endDate", 
            OAuth2Credential.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findByLastLoginBetween(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.lastAuthenticatedAt BETWEEN :startDate AND :endDate", 
            OAuth2Credential.class
        );
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findExpiredTokens(LocalDateTime expirationTime) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.accessTokenExpiresAt IS NOT NULL AND oc.accessTokenExpiresAt < :expirationTime", 
            OAuth2Credential.class
        );
        query.setParameter("expirationTime", expirationTime);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findExpiredRefreshTokens(LocalDateTime expirationTime) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.accessTokenExpiresAt IS NOT NULL AND oc.accessTokenExpiresAt < :expirationTime AND oc.refreshToken IS NOT NULL", 
            OAuth2Credential.class
        );
        query.setParameter("expirationTime", expirationTime);
        return query.getResultList();
    }
    
    @Override
    public OAuth2Credential save(OAuth2Credential oauth2Credential) {
        if (oauth2Credential.getId() == null) {
            entityManager.persist(oauth2Credential);
            return oauth2Credential;
        } else {
            return entityManager.merge(oauth2Credential);
        }
    }
    
    @Override
    public OAuth2Credential update(OAuth2Credential oauth2Credential) {
        return entityManager.merge(oauth2Credential);
    }
    
    @Override
    public void deleteById(UUID id) {
        OAuth2Credential credential = entityManager.find(OAuth2Credential.class, id);
        if (credential != null) {
            entityManager.remove(credential);
        }
    }
    
    @Override
    public void deleteByUserId(UUID userId) {
        entityManager.createQuery("DELETE FROM OAuth2Credential oc WHERE oc.userId = :userId")
                    .setParameter("userId", userId)
                    .executeUpdate();
    }
    
    @Override
    public void deleteByUserIdAndProvider(UUID userId, String provider) {
        entityManager.createQuery("DELETE FROM OAuth2Credential oc WHERE oc.userId = :userId AND oc.provider = :provider")
                    .setParameter("userId", userId)
                    .setParameter("provider", provider)
                    .executeUpdate();
    }
    
    @Override
    public boolean existsByProviderUserIdAndProvider(String providerUserId, String provider) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc WHERE oc.providerUserId = :providerUserId AND oc.provider = :provider", 
            Long.class
        );
        query.setParameter("providerUserId", providerUserId);
        query.setParameter("provider", provider);
        return query.getSingleResult() > 0;
    }
    
    @Override
    public boolean existsByUserIdAndProvider(UUID userId, String provider) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc WHERE oc.userId = :userId AND oc.provider = :provider", 
            Long.class
        );
        query.setParameter("userId", userId);
        query.setParameter("provider", provider);
        return query.getSingleResult() > 0;
    }
    
    @Override
    public long count() {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc", 
            Long.class
        );
        return query.getSingleResult();
    }
    
    @Override
    public long countByProvider(String provider) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc WHERE oc.provider = :provider", 
            Long.class
        );
        query.setParameter("provider", provider);
        return query.getSingleResult();
    }
    
    @Override
    public long countByStatus(OAuth2Status status) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc WHERE oc.status = :status", 
            Long.class
        );
        query.setParameter("status", status);
        return query.getSingleResult();
    }
    
    public Optional<OAuth2Credential> findByProviderAndProviderUserId(String provider, String providerUserId) {
        return findByProviderUserIdAndProvider(providerUserId, provider);
    }
    
    @Override
    public List<OAuth2Credential> findInactiveCredentials(int daysSinceLastLogin) {
        LocalDateTime threshold = LocalDateTime.now().minusDays(daysSinceLastLogin);
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.lastAuthenticatedAt IS NOT NULL AND oc.lastAuthenticatedAt < :threshold", 
            OAuth2Credential.class
        );
        query.setParameter("threshold", threshold);
        return query.getResultList();
    }
    
    @Override
    public List<OAuth2Credential> findActiveByUserId(UUID userId) {
        TypedQuery<OAuth2Credential> query = entityManager.createQuery(
            "SELECT oc FROM OAuth2Credential oc WHERE oc.userId = :userId AND oc.status = :status", 
            OAuth2Credential.class
        );
        query.setParameter("userId", userId);
        query.setParameter("status", OAuth2Status.ACTIVE);
        return query.getResultList();
    }
    
    @Override
    public boolean isTokenValid(String accessToken) {
        TypedQuery<Long> query = entityManager.createQuery(
            "SELECT COUNT(oc) FROM OAuth2Credential oc WHERE oc.accessToken = :accessToken AND oc.status = :status AND (oc.accessTokenExpiresAt IS NULL OR oc.accessTokenExpiresAt > :now)", 
            Long.class
        );
        query.setParameter("accessToken", accessToken);
        query.setParameter("status", OAuth2Status.ACTIVE);
        query.setParameter("now", LocalDateTime.now());
        return query.getSingleResult() > 0;
    }
}
