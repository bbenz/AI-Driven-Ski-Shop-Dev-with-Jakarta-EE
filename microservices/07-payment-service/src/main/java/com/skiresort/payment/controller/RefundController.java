package com.skiresort.payment.controller;

import com.skiresort.payment.dto.PaymentResponse;
import com.skiresort.payment.model.Refund;
import com.skiresort.payment.service.PaymentService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 返金RESTコントローラー
 */
@Path("/refunds")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RefundController {
    
    private static final Logger logger = Logger.getLogger(RefundController.class.getName());
    
    @Inject
    private PaymentService paymentService;
    
    /**
     * 返金を作成する
     */
    @POST
    public Response createRefund(@Valid CreateRefundRequest request) {
        try {
            Refund refund = paymentService.createRefund(
                request.paymentId(),
                request.amount(),
                request.reason(),
                request.requestedBy()
            );
            
            RefundResponse response = toRefundResponse(refund);
            logger.info("返金が作成されました: " + refund.getRefundId());
            
            return Response.status(Response.Status.CREATED).entity(response).build();
        } catch (Exception e) {
            logger.severe("返金作成エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PaymentController.ErrorResponse("返金作成に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 返金を処理する
     */
    @POST
    @Path("/{refundId}/process")
    public Response processRefund(@PathParam("refundId") String refundId,
                                ProcessRefundRequest request) {
        try {
            Refund refund = paymentService.processRefund(refundId, request.providerRefundId());
            RefundResponse response = toRefundResponse(refund);
            logger.info("返金処理を開始しました: " + refundId);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("返金処理エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PaymentController.ErrorResponse("返金処理に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 返金を完了する
     */
    @POST
    @Path("/{refundId}/complete")
    public Response completeRefund(@PathParam("refundId") String refundId) {
        try {
            Refund refund = paymentService.completeRefund(refundId);
            RefundResponse response = toRefundResponse(refund);
            logger.info("返金が完了しました: " + refundId);
            
            return Response.ok(response).build();
        } catch (Exception e) {
            logger.severe("返金完了エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PaymentController.ErrorResponse("返金完了に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * 返金IDで返金を取得する
     */
    @GET
    @Path("/{refundId}")
    public Response getRefund(@PathParam("refundId") String refundId) {
        return paymentService.getRefundByRefundId(refundId)
                .map(this::toRefundResponse)
                .map(response -> Response.ok(response).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity(new PaymentController.ErrorResponse("返金が見つかりません", refundId))
                        .build());
    }
    
    /**
     * 決済IDで返金一覧を取得する
     */
    @GET
    @Path("/payment/{paymentId}")
    public Response getRefundsByPaymentId(@PathParam("paymentId") String paymentId) {
        try {
            List<Refund> refunds = paymentService.getRefundsByPaymentId(paymentId);
            List<RefundResponse> responses = refunds.stream()
                    .map(this::toRefundResponse)
                    .toList();
            
            return Response.ok(responses).build();
        } catch (Exception e) {
            logger.severe("返金一覧取得エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new PaymentController.ErrorResponse("返金一覧取得に失敗しました", e.getMessage()))
                    .build();
        }
    }
    
    /**
     * RefundをRefundResponseに変換する
     */
    private RefundResponse toRefundResponse(Refund refund) {
        return new RefundResponse(
            refund.getId(),
            refund.getRefundId(),
            refund.getPayment().getPaymentId(),
            refund.getAmount(),
            refund.getCurrency(),
            refund.getStatus(),
            refund.getReason(),
            refund.getFailureReason(),
            refund.getProviderRefundId(),
            refund.getRequestedBy(),
            refund.getCreatedAt(),
            refund.getUpdatedAt(),
            refund.getProcessedAt(),
            refund.getCompletedAt()
        );
    }
    
    /**
     * 返金作成リクエストDTO
     */
    public record CreateRefundRequest(
        String paymentId,
        BigDecimal amount,
        String reason,
        UUID requestedBy
    ) {}
    
    /**
     * 返金処理リクエストDTO
     */
    public record ProcessRefundRequest(
        String providerRefundId
    ) {}
    
    /**
     * 返金レスポンスDTO
     */
    public record RefundResponse(
        UUID id,
        String refundId,
        String paymentId,
        BigDecimal amount,
        String currency,
        com.skiresort.payment.model.RefundStatus status,
        String reason,
        String failureReason,
        String providerRefundId,
        UUID requestedBy,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt,
        java.time.LocalDateTime processedAt,
        java.time.LocalDateTime completedAt
    ) {}
}
