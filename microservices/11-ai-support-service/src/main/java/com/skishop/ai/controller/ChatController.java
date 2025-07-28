package com.skishop.ai.controller;

import com.skishop.ai.dto.ChatMessageRequest;
import com.skishop.ai.dto.ChatMessageResponse;
import com.skishop.ai.service.ChatService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chatbot API Controller
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用したAIチャット機能</p>
 * 
 * @since 1.0.0
 */
@Path("/api/v1/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Chat API", description = "AI Chatbot API")
public class ChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatService chatService;
    
    /**
     * コンストラクタ
     * 
     * @param chatService チャットサービス
     */
    @Inject
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }
    
    /**
     * チャットメッセージ送信
     */
    @POST
    @Path("/message")
    @Operation(summary = "Send chat message", description = "Process user messages and return AI responses")
    public Response sendMessage(@Valid ChatMessageRequest request) {
        
        logger.info("Received chat message from user: {}", request.userId());
        
        // エラーハンドリングはChatServiceで既に実装済みのため、
        // コントローラーはそのまま結果を返すだけ
        // AiServiceExceptionはGlobalExceptionHandlerで処理される
        ChatMessageResponse response = chatService.processMessage(request);
        return Response.ok(response).build();
    }
    
    /**
     * 商品推薦チャット
     */
    @POST
    @Path("/recommend")
    @Operation(summary = "Product recommendation chat", description = "AI chat specialized for product recommendations")
    public Response recommendProducts(@Valid ChatMessageRequest request) {
        
        logger.info("Processing product recommendation for user: {}", request.userId());
        
        // 推薦フラグをリクエストに設定 - Recordはイミュータブルなので新しいインスタンスを作成
        var contextWithIntent = new HashMap<String, Object>();
        if (request.context() != null) {
            contextWithIntent.putAll(request.context());
        }
        contextWithIntent.put("forcedIntent", "PRODUCT_RECOMMENDATION");
        
        var enhancedRequest = new ChatMessageRequest(
            request.userId(),
            request.content(),
            request.conversationId(),
            request.sessionId(),
            contextWithIntent
        );
        
        ChatMessageResponse response = chatService.processMessage(enhancedRequest);
        return Response.ok(response).build();
    }
    
    /**
     * 技術的アドバイスチャット
     */
    @POST
    @Path("/advice")
    @Operation(summary = "Technical advice chat", description = "AI chat specialized for skiing technique advice")
    public Response provideTechnicalAdvice(@Valid ChatMessageRequest request) {
        
        logger.info("Processing technical advice for user: {}", request.userId());
        
        // 技術的アドバイスフラグをリクエストに設定 - Recordはイミュータブルなので新しいインスタンスを作成
        var contextWithIntent = new HashMap<String, Object>();
        if (request.context() != null) {
            contextWithIntent.putAll(request.context());
        }
        contextWithIntent.put("forcedIntent", "TECHNICAL_ADVICE");
        
        var enhancedRequest = new ChatMessageRequest(
            request.userId(),
            request.content(),
            request.conversationId(),
            request.sessionId(),
            contextWithIntent
        );
        
        ChatMessageResponse response = chatService.processMessage(enhancedRequest);
        return Response.ok(response).build();
    }
    
    /**
     * 会話履歴取得
     */
    @GET
    @Path("/conversations/{userId}")
    @Operation(summary = "Get conversation history", description = "Get conversation history for specified user")
    public Response getConversationHistory(
            @PathParam("userId") String userId,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        
        logger.info("Getting conversation history for user: {}", userId);
        
        // 実際の実装では、会話履歴サービスから取得
        // 現在はモックレスポンスを返す
        var mockHistory = Map.of(
            "conversations", List.of(),
            "totalCount", 0,
            "userId", userId,
            "limit", limit
        );
        
        return Response.ok(mockHistory).build();
    }
    
    /**
     * 会話削除
     */
    @DELETE
    @Path("/conversations/{conversationId}")
    @Operation(summary = "Delete conversation", description = "Delete specified conversation")
    public Response deleteConversation(@PathParam("conversationId") String conversationId) {
        
        logger.info("Deleting conversation: {}", conversationId);
        
        // 実際の実装では、会話セッションを削除
        // 現在は成功レスポンスを返す
        return Response.noContent().build();
    }
    
    /**
     * チャットフィードバック送信
     */
    @POST
    @Path("/feedback")
    @Operation(summary = "Chat feedback", description = "Submit user feedback for chat")
    public Response submitFeedback(
            @QueryParam("conversationId") String conversationId,
            @QueryParam("rating") int rating,
            @QueryParam("comment") String comment) {
        
        logger.info("Submitting feedback for conversation: {} with rating: {}", conversationId, rating);
        
        // 実際の実装では、フィードバックをデータベースに保存
        // 現在は成功レスポンスを返す
        return Response.noContent().build();
    }
}
