package com.skiresort.payment.controller;

import com.skiresort.payment.dto.CreatePaymentRequest;
import com.skiresort.payment.dto.PaymentResponse;
import com.skiresort.payment.model.*;
import com.skiresort.payment.service.EncryptionService;
import com.skiresort.payment.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 決済RESTコントローラー
 */
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentController {
    
    private static final Logger logger = Logger.getLogger(PaymentController.class.getName());
    
    @Inject
    private PaymentService paymentService;
    
    @Inject
    private EncryptionService encryptionService;
    
    /**
     * 決済を作成する
     */
    @POST
    public Response createPayment(@Valid CreatePaymentRequest request) {
        try {
            PaymentAmount amount = new PaymentAmount(
                request.amount(), 
                request.currency(), 
                request.taxAmount() != null ? request.taxAmount() : BigDecimal.ZERO,
                BigDecimal.ZERO // 手数料は0で初期化
            );
            
            PaymentDetails paymentDetails = null;
            if (request.paymentDetails() != null) {
                var details = request.paymentDetails();
                paymentDetails = new PaymentDetails(
                    details.cardNumber(),
                    details.expiryMonth(),
                    details.expiryYear(),
                    details.cvv(),
                    details.cardHolderName(),
                    details.billingAddress(),
                    details.isStored()
                );
            }
            
            Payment payment = paymentService.createPayment(
                request.orderId(),
                request.customerId(),
                amount,
                request.paymentMethod(),
                request.provider(),
                paymentDetails
            );
            
            PaymentResponse response = toPaymentResponse(payment);
            logger.info("決済が作成されました: " + payment.getPaymentId());
            
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            logger.severe("決済作成エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("決済作成に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 決済を認証する
     */
    @POST
    @Path("/{paymentId}/authorize")
    public Response authorizePayment(@PathParam("paymentId") String paymentId,
                                   AuthorizePaymentRequest request) {
        try {
            Payment payment = paymentService.authorizePayment(
                paymentId, 
                request.authCode(), 
                request.providerTransactionId()
            );
            
            PaymentResponse response = toPaymentResponse(payment);
            logger.info("決済が認証されました: " + paymentId);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("決済認証エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("決済認証に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 決済を確定する
     */
    @POST
    @Path("/{paymentId}/capture")
    public Response capturePayment(@PathParam("paymentId") String paymentId) {
        try {
            Payment payment = paymentService.capturePayment(paymentId);
            PaymentResponse response = toPaymentResponse(payment);
            logger.info("決済が確定されました: " + paymentId);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("決済確定エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("決済確定に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 決済をキャンセルする
     */
    @POST
    @Path("/{paymentId}/cancel")
    public Response cancelPayment(@PathParam("paymentId") String paymentId) {
        try {
            Payment payment = paymentService.cancelPayment(paymentId);
            PaymentResponse response = toPaymentResponse(payment);
            logger.info("決済がキャンセルされました: " + paymentId);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("決済キャンセルエラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("決済キャンセルに失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 決済IDで決済を取得する
     */
    @GET
    @Path("/{paymentId}")
    public Response getPayment(@PathParam("paymentId") String paymentId) {
        return paymentService.getPaymentByPaymentId(paymentId)
                .map(this::toPaymentResponse)
                .map(response -> Response.ok(response).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("決済が見つかりません", paymentId))
                        .build());
    }
    
    /**
     * 注文IDで決済一覧を取得する
     */
    @GET
    @Path("/order/{orderId}")
    public Response getPaymentsByOrderId(@PathParam("orderId") UUID orderId) {
        List<Payment> payments = paymentService.getPaymentsByOrderId(orderId);
        List<PaymentResponse> responses = payments.stream()
                .map(this::toPaymentResponse)
                .toList();
        
        return Response.ok(responses).build();
    }
    
    /**
     * 顧客IDで決済一覧を取得する
     */
    @GET
    @Path("/customer/{customerId}")
    public Response getPaymentsByCustomerId(@PathParam("customerId") UUID customerId) {
        List<Payment> payments = paymentService.getPaymentsByCustomerId(customerId);
        List<PaymentResponse> responses = payments.stream()
                .map(this::toPaymentResponse)
                .toList();
        
        return Response.ok(responses).build();
    }
    
    /**
     * 決済統計を取得する
     */
    @GET
    @Path("/stats")
    public Response getPaymentStats(@QueryParam("startDate") String startDate,
                                   @QueryParam("endDate") String endDate) {
        try {
            LocalDateTime start = startDate != null ? 
                LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
            LocalDateTime end = endDate != null ? 
                LocalDateTime.parse(endDate) : LocalDateTime.now();
            
            var stats = paymentService.getPaymentStats(start, end);
            
            // 成功率を計算
            BigDecimal successRate = stats.totalCount() > 0 ? 
                BigDecimal.valueOf(stats.successCount())
                    .divide(BigDecimal.valueOf(stats.totalCount()), 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)) : 
                BigDecimal.ZERO;
            
            var response = new PaymentResponse.PaymentStatsResponse(
                stats.totalCount(),
                stats.successCount(),
                stats.failedCount(),
                stats.pendingCount(),
                stats.totalAmount(),
                stats.successAmount(),
                successRate
            );
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("統計取得エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("統計取得に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * PaymentをPaymentResponseに変換する
     */
    private PaymentResponse toPaymentResponse(Payment payment) {
        String maskedCardNumber = null;
        if (payment.getPaymentDetails() != null && 
            payment.getPaymentDetails().cardNumber() != null) {
            try {
                String decryptedCardNumber = encryptionService.decrypt(
                    payment.getPaymentDetails().cardNumber());
                maskedCardNumber = encryptionService.maskCardNumber(decryptedCardNumber);
            } catch (Exception e) {
                maskedCardNumber = "**** **** **** ****";
            }
        }
        
        return new PaymentResponse(
            payment.getId(),
            payment.getPaymentId(),
            payment.getOrderId(),
            payment.getCustomerId(),
            payment.getAmount().amount(),
            payment.getAmount().currency(),
            payment.getAmount().taxAmount(),
            payment.getPaymentMethod(),
            payment.getStatus(),
            payment.getProvider(),
            payment.getProviderTransactionId(),
            payment.getAuthorizationCode(),
            payment.getCreatedAt(),
            payment.getUpdatedAt(),
            payment.getAuthorizedAt(),
            payment.getCapturedAt(),
            payment.getExpiresAt(),
            payment.getFailureReason(),
            maskedCardNumber
        );
    }
    
    /**
     * 認証リクエストDTO
     */
    public record AuthorizePaymentRequest(
        String authCode,
        String providerTransactionId
    ) {}
    
    /**
     * エラーレスポンスDTO
     */
    public record ErrorResponse(
        String message,
        String details
    ) {}
}
