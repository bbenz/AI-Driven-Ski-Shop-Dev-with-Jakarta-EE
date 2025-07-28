package com.skiresort.inventory.controller;

import com.skiresort.inventory.dto.*;
import com.skiresort.inventory.service.InventoryService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 在庫管理REST API コントローラー
 */
@RequestScoped
@Path("/api/v1/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryController {
    
    private static final Logger logger = Logger.getLogger(InventoryController.class.getName());
    
    @Inject
    private InventoryService inventoryService;
    
    /**
     * 在庫アイテムを作成する
     */
    @POST
    @Path("/items")
    public Response createInventoryItem(@Valid CreateInventoryItemRequest request) {
        try {
            logger.info("在庫アイテム作成リクエスト: SKU=" + request.sku());
            
            InventoryItemResponse response = inventoryService.createInventoryItem(request);
            
            return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.warning("在庫アイテム作成でバリデーションエラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("VALIDATION_ERROR", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.severe("在庫アイテム作成で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫アイテムの作成に失敗しました"))
                .build();
        }
    }
    
    /**
     * SKUで在庫アイテムを取得する
     */
    @GET
    @Path("/items/sku/{sku}")
    public Response getInventoryItemBySku(@PathParam("sku") @NotBlank String sku) {
        try {
            logger.info("在庫アイテム取得リクエスト: SKU=" + sku);
            
            InventoryItemResponse response = inventoryService.getInventoryItemBySku(sku);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            logger.info("指定されたSKUの在庫アイテムが見つかりません: " + sku);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.severe("在庫アイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫アイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * IDで在庫アイテムを取得する
     */
    @GET
    @Path("/items/{id}")
    public Response getInventoryItemById(@PathParam("id") @NotNull UUID id) {
        try {
            logger.info("在庫アイテム取得リクエスト: ID=" + id);
            
            InventoryItemResponse response = inventoryService.getInventoryItemById(id);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            logger.info("指定されたIDの在庫アイテムが見つかりません: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.severe("在庫アイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫アイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 倉庫IDで在庫アイテム一覧を取得する
     */
    @GET
    @Path("/items/warehouse/{warehouseId}")
    public Response getInventoryItemsByWarehouse(@PathParam("warehouseId") @NotNull UUID warehouseId) {
        try {
            logger.info("倉庫別在庫アイテム取得リクエスト: 倉庫ID=" + warehouseId);
            
            List<InventoryItemResponse> response = inventoryService.getInventoryItemsByWarehouse(warehouseId);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("倉庫別在庫アイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫アイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 全ての在庫アイテムを取得する
     */
    @GET
    @Path("/items")
    public Response getAllInventoryItems() {
        try {
            logger.info("全在庫アイテム取得リクエスト");
            
            List<InventoryItemResponse> response = inventoryService.getAllInventoryItems();
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("全在庫アイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫アイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 低在庫アイテムを取得する
     */
    @GET
    @Path("/items/low-stock")
    public Response getLowStockItems() {
        try {
            logger.info("低在庫アイテム取得リクエスト");
            
            List<InventoryItemResponse> response = inventoryService.getLowStockItems();
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("低在庫アイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "低在庫アイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 在庫切れアイテムを取得する
     */
    @GET
    @Path("/items/out-of-stock")
    public Response getOutOfStockItems() {
        try {
            logger.info("在庫切れアイテム取得リクエスト");
            
            List<InventoryItemResponse> response = inventoryService.getOutOfStockItems();
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("在庫切れアイテム取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫切れアイテムの取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 在庫を予約する
     */
    @POST
    @Path("/reservations")
    public Response reserveInventory(@Valid ReserveInventoryRequest request) {
        try {
            logger.info("在庫予約リクエスト: SKU=" + request.sku() + ", 数量=" + request.quantity());
            
            StockReservationResponse response = inventoryService.reserveInventory(request);
            
            return Response.status(Response.Status.CREATED)
                .entity(response)
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.warning("在庫予約でバリデーションエラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("VALIDATION_ERROR", e.getMessage()))
                .build();
        } catch (Exception e) {
            if (e.getMessage().contains("在庫不足")) {
                logger.info("在庫予約で在庫不足: " + e.getMessage());
                return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_STOCK", e.getMessage()))
                    .build();
            }
            logger.severe("在庫予約で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫予約に失敗しました"))
                .build();
        }
    }
    
    /**
     * 在庫予約を確定する
     */
    @PUT
    @Path("/reservations/{reservationId}/confirm")
    public Response confirmReservation(@PathParam("reservationId") @NotNull UUID reservationId) {
        try {
            logger.info("在庫予約確定リクエスト: 予約ID=" + reservationId);
            
            StockReservationResponse response = inventoryService.confirmReservation(reservationId);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            logger.info("指定された予約が見つかりません: " + reservationId);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            logger.warning("予約確定で状態エラー: " + e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("INVALID_STATE", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.severe("予約確定で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "予約確定に失敗しました"))
                .build();
        }
    }
    
    /**
     * 在庫予約をキャンセルする
     */
    @PUT
    @Path("/reservations/{reservationId}/cancel")
    public Response cancelReservation(
            @PathParam("reservationId") @NotNull UUID reservationId,
            @QueryParam("reason") String reason) {
        try {
            logger.info("在庫予約キャンセルリクエスト: 予約ID=" + reservationId);
            
            String cancellationReason = reason != null ? reason : "キャンセル理由未指定";
            StockReservationResponse response = inventoryService.cancelReservation(reservationId, cancellationReason);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            logger.info("指定された予約が見つかりません: " + reservationId);
            return Response.status(Response.Status.NOT_FOUND)
                .entity(new ErrorResponse("NOT_FOUND", e.getMessage()))
                .build();
        } catch (IllegalStateException e) {
            logger.warning("予約キャンセルで状態エラー: " + e.getMessage());
            return Response.status(Response.Status.CONFLICT)
                .entity(new ErrorResponse("INVALID_STATE", e.getMessage()))
                .build();
        } catch (Exception e) {
            logger.severe("予約キャンセルで予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "予約キャンセルに失敗しました"))
                .build();
        }
    }
    
    /**
     * 在庫を更新する
     */
    @PUT
    @Path("/items/update-stock")
    public Response updateStock(@Valid UpdateStockRequest request) {
        try {
            logger.info("在庫更新リクエスト: SKU=" + request.sku() + ", タイプ=" + request.movementType());
            
            InventoryItemResponse response = inventoryService.updateStock(request);
            
            return Response.ok(response).build();
            
        } catch (IllegalArgumentException e) {
            logger.warning("在庫更新でバリデーションエラー: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse("VALIDATION_ERROR", e.getMessage()))
                .build();
        } catch (Exception e) {
            if (e.getMessage().contains("在庫不足")) {
                logger.info("在庫更新で在庫不足: " + e.getMessage());
                return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("INSUFFICIENT_STOCK", e.getMessage()))
                    .build();
            }
            logger.severe("在庫更新で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "在庫更新に失敗しました"))
                .build();
        }
    }
    
    /**
     * 注文IDで予約を取得する
     */
    @GET
    @Path("/reservations/order/{orderId}")
    public Response getReservationsByOrderId(@PathParam("orderId") @NotNull UUID orderId) {
        try {
            logger.info("注文別予約取得リクエスト: 注文ID=" + orderId);
            
            List<StockReservationResponse> response = inventoryService.getReservationsByOrderId(orderId);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("注文別予約取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "予約の取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 顧客IDで予約を取得する
     */
    @GET
    @Path("/reservations/customer/{customerId}")
    public Response getReservationsByCustomerId(@PathParam("customerId") @NotNull UUID customerId) {
        try {
            logger.info("顧客別予約取得リクエスト: 顧客ID=" + customerId);
            
            List<StockReservationResponse> response = inventoryService.getReservationsByCustomerId(customerId);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.severe("顧客別予約取得で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "予約の取得に失敗しました"))
                .build();
        }
    }
    
    /**
     * 期限切れ予約を処理する
     */
    @POST
    @Path("/reservations/process-expired")
    public Response processExpiredReservations() {
        try {
            logger.info("期限切れ予約処理リクエスト");
            
            int processedCount = inventoryService.processExpiredReservations();
            
            return Response.ok(new ProcessExpiredReservationsResponse(processedCount, 
                "期限切れ予約を処理しました")).build();
            
        } catch (Exception e) {
            logger.severe("期限切れ予約処理で予期しないエラー: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse("INTERNAL_ERROR", "期限切れ予約の処理に失敗しました"))
                .build();
        }
    }
    
    /**
     * エラーレスポンスDTO
     */
    public record ErrorResponse(String code, String message) {}
    
    /**
     * 期限切れ予約処理レスポンスDTO
     */
    public record ProcessExpiredReservationsResponse(int processedCount, String message) {}
}
