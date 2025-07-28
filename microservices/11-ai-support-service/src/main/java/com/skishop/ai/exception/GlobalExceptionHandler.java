package com.skishop.ai.exception;

import com.skishop.ai.exception.AiServiceException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global Exception Handler
 * 
 * <p>アプリケーション全体の例外ハンドリング</p>
 * 
 * @since 1.0.0
 */
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @Override
    public Response toResponse(Exception exception) {
        
        // AIサービス例外の場合
        if (exception instanceof AiServiceException aiException) {
            return handleAiServiceException(aiException);
        }
        
        // バリデーション例外の場合
        if (exception instanceof jakarta.validation.ValidationException validationException) {
            return handleValidationException(validationException);
        }
        
        // 一般的な例外の場合
        logger.error("Unexpected error occurred", exception);
        return handleGenericException(exception);
    }
    
    /**
     * AIサービス例外ハンドリング
     */
    private Response handleAiServiceException(AiServiceException exception) {
        logger.error("AI service error: {}", exception.getMessage(), exception);
        
        var errorResponse = Map.of(
            "error", "AI_SERVICE_ERROR",
            "message", exception.getMessage(),
            "timestamp", LocalDateTime.now().toString(),
            "type", exception.getErrorType().toString()
        );
        
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
    
    /**
     * バリデーション例外ハンドリング
     */
    private Response handleValidationException(jakarta.validation.ValidationException exception) {
        logger.warn("Validation error: {}", exception.getMessage());
        
        var errorResponse = Map.of(
            "error", "VALIDATION_ERROR",
            "message", "入力データが不正です",
            "details", exception.getMessage(),
            "timestamp", LocalDateTime.now().toString()
        );
        
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
    
    /**
     * 一般例外ハンドリング
     */
    private Response handleGenericException(Exception exception) {
        logger.error("Unexpected error occurred: {}", exception.getMessage(), exception);
        
        var errorResponse = Map.of(
            "error", "INTERNAL_SERVER_ERROR",
            "message", "内部サーバーエラーが発生しました",
            "timestamp", LocalDateTime.now().toString()
        );
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
