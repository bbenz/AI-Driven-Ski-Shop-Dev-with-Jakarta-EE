package com.skiresort.payment.repository.impl;

import com.skiresort.payment.model.Refund;
import com.skiresort.payment.model.RefundStatus;
import com.skiresort.payment.repository.RefundRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 返金リポジトリ実装クラス
 */
@ApplicationScoped
@Transactional
public class RefundRepositoryImpl implements RefundRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Refund save(Refund refund) {
        if (refund.getId() == null) {
            em.persist(refund);
            return refund;
        } else {
            return em.merge(refund);
        }
    }
    
    @Override
    public Optional<Refund> findById(UUID id) {
        Refund refund = em.find(Refund.class, id);
        return Optional.ofNullable(refund);
    }
    
    @Override
    public Optional<Refund> findByRefundId(String refundId) {
        try {
            TypedQuery<Refund> query = em.createQuery(
                "SELECT r FROM Refund r WHERE r.refundId = :refundId", Refund.class);
            query.setParameter("refundId", refundId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Refund> findByPaymentId(UUID paymentId) {
        TypedQuery<Refund> query = em.createNamedQuery("Refund.findByPaymentId", Refund.class);
        query.setParameter("paymentId", paymentId);
        return query.getResultList();
    }
    
    @Override
    public List<Refund> findByStatus(RefundStatus status) {
        TypedQuery<Refund> query = em.createNamedQuery("Refund.findByStatus", Refund.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<Refund> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Refund> query = em.createNamedQuery("Refund.findByDateRange", Refund.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public List<Refund> findPendingRefunds() {
        TypedQuery<Refund> query = em.createNamedQuery("Refund.findPendingRefunds", Refund.class);
        return query.getResultList();
    }
    
    @Override
    public Optional<Refund> findByProviderRefundId(String providerRefundId) {
        try {
            TypedQuery<Refund> query = em.createQuery(
                "SELECT r FROM Refund r WHERE r.providerRefundId = :providerRefundId", 
                Refund.class);
            query.setParameter("providerRefundId", providerRefundId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public RefundStats getRefundStats(LocalDateTime startDate, LocalDateTime endDate) {
        // 総件数と総額
        TypedQuery<Object[]> totalQuery = em.createQuery(
            "SELECT COUNT(r), COALESCE(SUM(r.amount), 0) FROM Refund r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate", Object[].class);
        totalQuery.setParameter("startDate", startDate);
        totalQuery.setParameter("endDate", endDate);
        Object[] totalResult = totalQuery.getSingleResult();
        
        // 完了件数と完了額
        TypedQuery<Object[]> completedQuery = em.createQuery(
            "SELECT COUNT(r), COALESCE(SUM(r.amount), 0) FROM Refund r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.status = :status", 
            Object[].class);
        completedQuery.setParameter("startDate", startDate);
        completedQuery.setParameter("endDate", endDate);
        completedQuery.setParameter("status", RefundStatus.COMPLETED);
        Object[] completedResult = completedQuery.getSingleResult();
        
        // 失敗件数
        TypedQuery<Long> failedQuery = em.createQuery(
            "SELECT COUNT(r) FROM Refund r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.status = :status", 
            Long.class);
        failedQuery.setParameter("startDate", startDate);
        failedQuery.setParameter("endDate", endDate);
        failedQuery.setParameter("status", RefundStatus.FAILED);
        Long failedCount = failedQuery.getSingleResult();
        
        // 保留件数
        TypedQuery<Long> pendingQuery = em.createQuery(
            "SELECT COUNT(r) FROM Refund r " +
            "WHERE r.createdAt BETWEEN :startDate AND :endDate AND r.status IN :statuses", 
            Long.class);
        pendingQuery.setParameter("startDate", startDate);
        pendingQuery.setParameter("endDate", endDate);
        pendingQuery.setParameter("statuses", List.of(RefundStatus.PENDING, RefundStatus.PROCESSING));
        Long pendingCount = pendingQuery.getSingleResult();
        
        return new RefundStats(
            ((Number) totalResult[0]).longValue(),
            ((Number) completedResult[0]).longValue(),
            failedCount,
            pendingCount,
            (BigDecimal) totalResult[1],
            (BigDecimal) completedResult[1]
        );
    }
    
    @Override
    public void delete(Refund refund) {
        Refund managed = em.contains(refund) ? refund : em.merge(refund);
        em.remove(managed);
    }
}
