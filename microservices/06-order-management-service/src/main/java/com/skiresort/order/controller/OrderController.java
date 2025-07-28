package com.skiresort.order.controller;

import com.skiresort.order.model.*;
import com.skiresort.order.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 注文管理RESTコントローラー
 */
@Path("/api/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderController {
    
    private static final Logger LOGGER = Logger.getLogger(OrderController.class.getName());
    
    @Inject
    private OrderService orderService;
    
    /**
     * 注文作成
     */
    @POST
    public Response createOrder(@Valid Order order) {
        try {
            Order createdOrder = orderService.createOrder(order);
            LOGGER.info("注文が作成されました: " + createdOrder.getOrderNumber());
            return Response.status(Response.Status.CREATED).entity(createdOrder).build();
        } catch (Exception e) {
            LOGGER.severe("注文作成エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("注文作成に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文取得（ID）
     */
    @GET
    @Path("/{orderId}")
    public Response getOrder(@PathParam("orderId") String orderIdStr) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            Order order = orderService.findOrderById(orderId);
            return Response.ok(order).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な注文ID形式です"))
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("注文が見つかりません"))
                .build();
        }
    }
    
    /**
     * 注文取得（注文番号）
     */
    @GET
    @Path("/number/{orderNumber}")
    public Response getOrderByNumber(@PathParam("orderNumber") String orderNumber) {
        try {
            Order order = orderService.findOrderByNumber(orderNumber);
            return Response.ok(order).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("注文が見つかりません"))
                .build();
        }
    }
    
    /**
     * 顧客の注文一覧取得
     */
    @GET
    @Path("/customer/{customerId}")
    public Response getOrdersByCustomer(
            @PathParam("customerId") String customerIdStr,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        try {
            UUID customerId = UUID.fromString(customerIdStr);
            List<Order> orders;
            
            if (size > 0) {
                orders = orderService.findOrdersByCustomerId(customerId, page, size);
            } else {
                orders = orderService.findOrdersByCustomerId(customerId);
            }
            
            return Response.ok(orders).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な顧客ID形式です"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("顧客注文取得エラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("注文取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * ステータス別注文一覧取得
     */
    @GET
    @Path("/status/{status}")
    public Response getOrdersByStatus(@PathParam("status") String statusStr) {
        try {
            OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
            List<Order> orders = orderService.findOrdersByStatus(status);
            return Response.ok(orders).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効なステータスです: " + statusStr))
                .build();
        } catch (Exception e) {
            LOGGER.severe("ステータス別注文取得エラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("注文取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 注文ステータス変更
     */
    @PUT
    @Path("/{orderId}/status")
    public Response changeOrderStatus(
            @PathParam("orderId") String orderIdStr,
            @Valid StatusChangeRequest request) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            
            Order updatedOrder = orderService.changeOrderStatus(
                orderId, newStatus, request.getChangedBy(), request.getReason());
            
            LOGGER.info(String.format("注文ステータス変更: %s -> %s", 
                       orderId, newStatus));
            
            return Response.ok(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効なパラメータです: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            LOGGER.severe("ステータス変更エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("ステータス変更に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 決済ステータス変更
     */
    @PUT
    @Path("/{orderId}/payment-status")
    public Response changePaymentStatus(
            @PathParam("orderId") String orderIdStr,
            @Valid PaymentStatusChangeRequest request) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            PaymentStatus newPaymentStatus = PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase());
            
            Order updatedOrder = orderService.changePaymentStatus(
                orderId, newPaymentStatus, request.getReason());
            
            LOGGER.info(String.format("決済ステータス変更: %s -> %s", 
                       orderId, newPaymentStatus));
            
            return Response.ok(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効なパラメータです: " + e.getMessage()))
                .build();
        } catch (Exception e) {
            LOGGER.severe("決済ステータス変更エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("決済ステータス変更に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文キャンセル
     */
    @PUT
    @Path("/{orderId}/cancel")
    public Response cancelOrder(
            @PathParam("orderId") String orderIdStr,
            @Valid CancelOrderRequest request) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            
            Order cancelledOrder = orderService.cancelOrder(
                orderId, request.getReason(), request.getCancelledBy());
            
            LOGGER.info("注文がキャンセルされました: " + orderId);
            
            return Response.ok(cancelledOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な注文IDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文キャンセルエラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("注文キャンセルに失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文明細追加
     */
    @POST
    @Path("/{orderId}/items")
    public Response addOrderItem(
            @PathParam("orderId") String orderIdStr,
            @Valid OrderItem orderItem) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            Order updatedOrder = orderService.addOrderItem(orderId, orderItem);
            
            LOGGER.info("注文明細が追加されました: " + orderId);
            
            return Response.status(Response.Status.CREATED).entity(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な注文IDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文明細追加エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("注文明細追加に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文明細更新
     */
    @PUT
    @Path("/{orderId}/items/{itemId}")
    public Response updateOrderItem(
            @PathParam("orderId") String orderIdStr,
            @PathParam("itemId") String itemIdStr,
            @Valid OrderItem orderItem) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            UUID itemId = UUID.fromString(itemIdStr);
            
            Order updatedOrder = orderService.updateOrderItem(orderId, itemId, orderItem);
            
            LOGGER.info("注文明細が更新されました: " + itemId);
            
            return Response.ok(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効なIDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文明細更新エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("注文明細更新に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文明細削除
     */
    @DELETE
    @Path("/{orderId}/items/{itemId}")
    public Response removeOrderItem(
            @PathParam("orderId") String orderIdStr,
            @PathParam("itemId") String itemIdStr) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            UUID itemId = UUID.fromString(itemIdStr);
            
            Order updatedOrder = orderService.removeOrderItem(orderId, itemId);
            
            LOGGER.info("注文明細が削除されました: " + itemId);
            
            return Response.ok(updatedOrder).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効なIDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文明細削除エラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("注文明細削除に失敗しました: " + e.getMessage()))
                .build();
        }
    }
    
    /**
     * 注文明細一覧取得
     */
    @GET
    @Path("/{orderId}/items")
    public Response getOrderItems(@PathParam("orderId") String orderIdStr) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            List<OrderItem> items = orderService.getOrderItems(orderId);
            return Response.ok(items).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な注文IDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文明細取得エラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("注文明細取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 注文履歴取得
     */
    @GET
    @Path("/{orderId}/history")
    public Response getOrderHistory(@PathParam("orderId") String orderIdStr) {
        try {
            UUID orderId = UUID.fromString(orderIdStr);
            List<OrderStatusHistory> history = orderService.getOrderHistory(orderId);
            return Response.ok(history).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("無効な注文IDです"))
                .build();
        } catch (Exception e) {
            LOGGER.severe("注文履歴取得エラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("注文履歴取得に失敗しました"))
                .build();
        }
    }
    
    // DTOクラス
    public static class StatusChangeRequest {
        private String status;
        private String changedBy;
        private String reason;
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getChangedBy() { return changedBy; }
        public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class PaymentStatusChangeRequest {
        private String paymentStatus;
        private String reason;
        
        // Getters and Setters
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    public static class CancelOrderRequest {
        private String reason;
        private String cancelledBy;
        
        // Getters and Setters
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getCancelledBy() { return cancelledBy; }
        public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }
    }
    
    public static class ErrorResponse {
        private String message;
        private long timestamp;
        
        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}
