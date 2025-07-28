package com.skiresort.payment.repository.impl;

import com.skiresort.payment.model.Payment;
import com.skiresort.payment.model.PaymentStatus;
import com.skiresort.payment.repository.PaymentRepository;
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
 * 決済リポジトリ実装クラス
 */
@ApplicationScoped
@Transactional
public class PaymentRepositoryImpl implements PaymentRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    @Override
    public Payment save(Payment payment) {
        if (payment.getId() == null) {
            em.persist(payment);
            return payment;
        } else {
            return em.merge(payment);
        }
    }
    
    @Override
    public Optional<Payment> findById(UUID id) {
        Payment payment = em.find(Payment.class, id);
        return Optional.ofNullable(payment);
    }
    
    @Override
    public Optional<Payment> findByPaymentId(String paymentId) {
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.paymentId = :paymentId", Payment.class);
            query.setParameter("paymentId", paymentId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        TypedQuery<Payment> query = em.createNamedQuery("Payment.findByOrderId", Payment.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> findByCustomerId(UUID customerId) {
        TypedQuery<Payment> query = em.createNamedQuery("Payment.findByCustomerId", Payment.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        TypedQuery<Payment> query = em.createNamedQuery("Payment.findByStatus", Payment.class);
        query.setParameter("status", status);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> findByProvider(String provider) {
        TypedQuery<Payment> query = em.createNamedQuery("Payment.findByProvider", Payment.class);
        query.setParameter("provider", provider);
        return query.getResultList();
    }
    
    @Override
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        TypedQuery<Payment> query = em.createNamedQuery("Payment.findByDateRange", Payment.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }
    
    @Override
    public Optional<Payment> findByProviderTransactionId(String providerTransactionId) {
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.providerTransactionId = :providerTransactionId", 
                Payment.class);
            query.setParameter("providerTransactionId", providerTransactionId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<Payment> findExpiredPayments() {
        TypedQuery<Payment> query = em.createQuery(
            "SELECT p FROM Payment p WHERE p.expiresAt IS NOT NULL AND p.expiresAt < :now " +
            "AND p.status IN (:statuses)", Payment.class);
        query.setParameter("now", LocalDateTime.now());
        query.setParameter("statuses", List.of(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED));
        return query.getResultList();
    }
    
    @Override
    public List<Payment> findFailedPaymentsForRetry(LocalDateTime before) {
        TypedQuery<Payment> query = em.createQuery(
            "SELECT p FROM Payment p WHERE p.status = :status AND p.failedAt < :before " +
            "ORDER BY p.failedAt ASC", Payment.class);
        query.setParameter("status", PaymentStatus.FAILED);
        query.setParameter("before", before);
        query.setMaxResults(100); // 一度に処理する最大件数
        return query.getResultList();
    }
    
    @Override
    public PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate) {
        // 総件数と総額
        TypedQuery<Object[]> totalQuery = em.createQuery(
            "SELECT COUNT(p), COALESCE(SUM(p.amount.amount), 0) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate", Object[].class);
        totalQuery.setParameter("startDate", startDate);
        totalQuery.setParameter("endDate", endDate);
        Object[] totalResult = totalQuery.getSingleResult();
        
        // 成功件数と成功額
        TypedQuery<Object[]> successQuery = em.createQuery(
            "SELECT COUNT(p), COALESCE(SUM(p.amount.amount), 0) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = :status", 
            Object[].class);
        successQuery.setParameter("startDate", startDate);
        successQuery.setParameter("endDate", endDate);
        successQuery.setParameter("status", PaymentStatus.CAPTURED);
        Object[] successResult = successQuery.getSingleResult();
        
        // 失敗件数
        TypedQuery<Long> failedQuery = em.createQuery(
            "SELECT COUNT(p) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status = :status", 
            Long.class);
        failedQuery.setParameter("startDate", startDate);
        failedQuery.setParameter("endDate", endDate);
        failedQuery.setParameter("status", PaymentStatus.FAILED);
        Long failedCount = failedQuery.getSingleResult();
        
        // 保留件数
        TypedQuery<Long> pendingQuery = em.createQuery(
            "SELECT COUNT(p) FROM Payment p " +
            "WHERE p.createdAt BETWEEN :startDate AND :endDate AND p.status IN :statuses", 
            Long.class);
        pendingQuery.setParameter("startDate", startDate);
        pendingQuery.setParameter("endDate", endDate);
        pendingQuery.setParameter("statuses", List.of(PaymentStatus.PENDING, PaymentStatus.AUTHORIZED));
        Long pendingCount = pendingQuery.getSingleResult();
        
        return new PaymentStats(
            ((Number) totalResult[0]).longValue(),
            ((Number) successResult[0]).longValue(),
            failedCount,
            pendingCount,
            (BigDecimal) totalResult[1],
            (BigDecimal) successResult[1]
        );
    }
    
    @Override
    public void delete(Payment payment) {
        Payment managed = em.contains(payment) ? payment : em.merge(payment);
        em.remove(managed);
    }
}
