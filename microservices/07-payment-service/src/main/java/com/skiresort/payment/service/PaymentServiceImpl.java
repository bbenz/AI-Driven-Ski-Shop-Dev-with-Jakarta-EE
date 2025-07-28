package com.skiresort.payment.service;

import com.skiresort.payment.exception.InvalidPaymentStateException;
import com.skiresort.payment.exception.PaymentNotFoundException;
import com.skiresort.payment.exception.PaymentProcessingException;
import com.skiresort.payment.model.*;
import com.skiresort.payment.repository.PaymentRepository;
import com.skiresort.payment.repository.RefundRepository;
import com.skiresort.payment.repository.PaymentRepository.PaymentStats;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 決済サービス実装クラス
 */
@ApplicationScoped
@Transactional
public class PaymentServiceImpl implements PaymentService {
    
    private static final Logger logger = Logger.getLogger(PaymentServiceImpl.class.getName());
    
    @Inject
    private PaymentRepository paymentRepository;
    
    @Inject
    private RefundRepository refundRepository;
    
    @Inject
    private EncryptionService encryptionService;
    
    @Override
    public Payment createPayment(UUID orderId, UUID customerId, PaymentAmount amount,
                                PaymentMethod paymentMethod, String provider, PaymentDetails paymentDetails) {
        String paymentId = generatePaymentId();
        
        // 暗号化されたPaymentDetailsを作成
        PaymentDetails encryptedDetails = null;
        if (paymentDetails != null) {
            String encryptedCardNumber = paymentDetails.cardNumber() != null ? 
                encryptionService.encrypt(paymentDetails.cardNumber()) : null;
            String encryptedCvv = paymentDetails.cvv() != null ? 
                encryptionService.encrypt(paymentDetails.cvv()) : null;
                
            encryptedDetails = new PaymentDetails(
                encryptedCardNumber,
                paymentDetails.expiryMonth(),
                paymentDetails.expiryYear(),
                encryptedCvv,
                paymentDetails.cardHolderName(),
                paymentDetails.billingAddress(),
                paymentDetails.isStored()
            );
        }
        
        Payment payment = new Payment(paymentId, orderId, customerId, amount, paymentMethod, provider);
        payment.setPaymentDetails(encryptedDetails);
        
        // 有効期限を設定（30分後）
        payment.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        
        Payment savedPayment = paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createStatusChangeEvent(
            savedPayment, null, "PENDING", "決済が作成されました");
        savedPayment.addEvent(event);
        
        logger.info("決済が作成されました: " + paymentId);
        return savedPayment;
    }
    
    @Override
    public Payment authorizePayment(String paymentId, String authCode, String providerTransactionId) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        
        if (!payment.canBeAuthorized()) {
            throw new InvalidPaymentStateException(
                "決済を認証できません。現在のステータス: " + payment.getStatus());
        }
        
        payment.authorize(authCode, providerTransactionId);
        paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createPaymentAuthorizedEvent(payment, authCode);
        payment.addEvent(event);
        
        logger.info("決済が認証されました: " + paymentId);
        return payment;
    }
    
    @Override
    public Payment capturePayment(String paymentId) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        
        if (!payment.canBeCaptured()) {
            throw new InvalidPaymentStateException(
                "決済を確定できません。現在のステータス: " + payment.getStatus());
        }
        
        payment.capture();
        paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createPaymentCapturedEvent(payment);
        payment.addEvent(event);
        
        logger.info("決済が確定されました: " + paymentId);
        return payment;
    }
    
    @Override
    public Payment cancelPayment(String paymentId) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        
        if (!payment.canBeCancelled()) {
            throw new InvalidPaymentStateException(
                "決済をキャンセルできません。現在のステータス: " + payment.getStatus());
        }
        
        PaymentStatus previousStatus = payment.getStatus();
        payment.cancel();
        paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createStatusChangeEvent(
            payment, previousStatus.toString(), "CANCELLED", "決済がキャンセルされました");
        payment.addEvent(event);
        
        logger.info("決済がキャンセルされました: " + paymentId);
        return payment;
    }
    
    @Override
    public Payment failPayment(String paymentId, String reason) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        
        payment.fail(reason);
        paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createPaymentFailedEvent(payment, reason);
        payment.addEvent(event);
        
        logger.warning("決済が失敗しました: " + paymentId + ", 理由: " + reason);
        return payment;
    }
    
    @Override
    public Refund createRefund(String paymentId, BigDecimal amount, String reason, UUID requestedBy) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        
        if (!payment.canBeRefunded()) {
            throw new InvalidPaymentStateException(
                "返金できません。現在のステータス: " + payment.getStatus());
        }
        
        BigDecimal refundableAmount = payment.getRefundableAmount();
        if (amount.compareTo(refundableAmount) > 0) {
            throw new PaymentProcessingException(
                "返金額が返金可能額を超えています。返金可能額: " + refundableAmount);
        }
        
        String refundId = generateRefundId();
        Refund refund = new Refund(refundId, payment, amount, 
                                  payment.getAmount().currency(), reason, requestedBy);
        
        refundRepository.save(refund);
        payment.addRefund(refund);
        paymentRepository.save(payment);
        
        // イベントを記録
        PaymentEvent event = PaymentEvent.createRefundRequestedEvent(payment, refundId);
        payment.addEvent(event);
        
        logger.info("返金が作成されました: " + refundId);
        return refund;
    }
    
    @Override
    public Refund processRefund(String refundId, String providerRefundId) {
        Refund refund = getRefundByRefundId(refundId)
            .orElseThrow(() -> new PaymentNotFoundException("返金が見つかりません: " + refundId));
        
        if (!refund.canBeProcessed()) {
            throw new InvalidPaymentStateException(
                "返金を処理できません。現在のステータス: " + refund.getStatus());
        }
        
        refund.process(providerRefundId);
        refundRepository.save(refund);
        
        logger.info("返金の処理を開始しました: " + refundId);
        return refund;
    }
    
    @Override
    public Refund completeRefund(String refundId) {
        Refund refund = getRefundByRefundId(refundId)
            .orElseThrow(() -> new PaymentNotFoundException("返金が見つかりません: " + refundId));
        
        if (!refund.canBeCompleted()) {
            throw new InvalidPaymentStateException(
                "返金を完了できません。現在のステータス: " + refund.getStatus());
        }
        
        refund.complete();
        refundRepository.save(refund);
        
        logger.info("返金が完了しました: " + refundId);
        return refund;
    }
    
    @Override
    public Refund failRefund(String refundId, String reason) {
        Refund refund = getRefundByRefundId(refundId)
            .orElseThrow(() -> new PaymentNotFoundException("返金が見つかりません: " + refundId));
        
        refund.fail(reason);
        refundRepository.save(refund);
        
        logger.warning("返金が失敗しました: " + refundId + ", 理由: " + reason);
        return refund;
    }
    
    @Override
    public Optional<Payment> getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId);
    }
    
    @Override
    public List<Payment> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<Payment> getPaymentsByCustomerId(UUID customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }
    
    @Override
    public List<Payment> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status);
    }
    
    @Override
    public List<Refund> getRefundsByPaymentId(String paymentId) {
        Payment payment = getPaymentByPaymentId(paymentId)
            .orElseThrow(() -> new PaymentNotFoundException("決済が見つかりません: " + paymentId));
        return refundRepository.findByPaymentId(payment.getId());
    }
    
    @Override
    public Optional<Refund> getRefundByRefundId(String refundId) {
        return refundRepository.findByRefundId(refundId);
    }
    
    @Override
    public void processExpiredPayments() {
        List<Payment> expiredPayments = paymentRepository.findExpiredPayments();
        
        for (Payment payment : expiredPayments) {
            try {
                if (payment.canBeCancelled()) {
                    payment.cancel();
                    paymentRepository.save(payment);
                    
                    // イベントを記録
                    PaymentEvent event = new PaymentEvent(payment, "EXPIRED", "決済が期限切れでキャンセルされました");
                    payment.addEvent(event);
                    
                    logger.info("期限切れ決済をキャンセルしました: " + payment.getPaymentId());
                }
            } catch (Exception e) {
                logger.severe("期限切れ決済の処理に失敗しました: " + payment.getPaymentId() + ", " + e.getMessage());
            }
        }
        
        logger.info("期限切れ決済処理完了: " + expiredPayments.size() + "件");
    }
    
    @Override
    public void retryFailedPayments() {
        LocalDateTime retryThreshold = LocalDateTime.now().minusHours(1);
        List<Payment> failedPayments = paymentRepository.findFailedPaymentsForRetry(retryThreshold);
        
        // ここでは失敗した決済をログに記録するのみ
        // 実際の再試行処理は別のバッチジョブで実行
        for (Payment payment : failedPayments) {
            logger.info("再試行候補の決済: " + payment.getPaymentId() + 
                       ", 失敗理由: " + payment.getFailureReason());
        }
        
        logger.info("失敗決済再試行候補: " + failedPayments.size() + "件");
    }
    
    @Override
    public PaymentStats getPaymentStats(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.getPaymentStats(startDate, endDate);
    }
    
    @Override
    public String generatePaymentId() {
        return "PAY_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
    
    @Override
    public String generateRefundId() {
        return "REF_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}
