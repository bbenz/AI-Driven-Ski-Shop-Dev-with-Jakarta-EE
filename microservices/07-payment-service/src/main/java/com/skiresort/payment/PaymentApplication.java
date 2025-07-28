package com.skiresort.payment;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.Set;

/**
 * Payment Service JAX-RSアプリケーション
 */
@ApplicationPath("/api")
public class PaymentApplication extends Application {
    
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
            com.skiresort.payment.controller.PaymentController.class,
            com.skiresort.payment.controller.RefundController.class,
            // CORS フィルター
            CorsFilter.class,
            // 例外ハンドラー
            PaymentExceptionMapper.class
        );
    }
    
    /**
     * CORSフィルター
     */
    @jakarta.ws.rs.ext.Provider
    public static class CorsFilter implements jakarta.ws.rs.container.ContainerResponseFilter {
        
        @Override
        public void filter(jakarta.ws.rs.container.ContainerRequestContext requestContext,
                          jakarta.ws.rs.container.ContainerResponseContext responseContext) {
            responseContext.getHeaders().add("Access-Control-Allow-Origin", "*");
            responseContext.getHeaders().add("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization");
            responseContext.getHeaders().add("Access-Control-Allow-Methods", 
                "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            responseContext.getHeaders().add("Access-Control-Max-Age", "1209600");
        }
    }
    
    /**
     * 例外マッパー
     */
    @jakarta.ws.rs.ext.Provider
    public static class PaymentExceptionMapper 
            implements jakarta.ws.rs.ext.ExceptionMapper<Exception> {
        
        @Override
        public jakarta.ws.rs.core.Response toResponse(Exception exception) {
            var errorResponse = new ErrorResponse(
                "Internal Server Error", 
                exception.getMessage()
            );
            
            return jakarta.ws.rs.core.Response
                    .status(jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }
    
    /**
     * エラーレスポンス
     */
    public record ErrorResponse(String message, String details) {}
}
