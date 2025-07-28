package com.jakartaone2025.ski.gateway.resource;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.*;
import com.jakartaone2025.ski.gateway.routing.RoutingService;
import java.util.Enumeration;
import java.util.logging.Logger;

/**
 * Gateway Resource - すべてのリクエストをプロキシ
 */
@Path("/")
@RequestScoped
public class GatewayResource {
    
    private static final Logger logger = Logger.getLogger(GatewayResource.class.getName());
    
    private final RoutingService routingService;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private UriInfo uriInfo;
    
    @Context
    private HttpHeaders headers;
    
    @Inject
    public GatewayResource(RoutingService routingService) {
        this.routingService = routingService;
    }
    
    @GET
    @Path("{path: .*}")
    public Response proxyGet(@PathParam("path") String path) {
        return proxyRequest("GET", path, null);
    }
    
    @POST
    @Path("{path: .*}")
    public Response proxyPost(@PathParam("path") String path, String body) {
        return proxyRequest("POST", path, body);
    }
    
    @PUT
    @Path("{path: .*}")
    public Response proxyPut(@PathParam("path") String path, String body) {
        return proxyRequest("PUT", path, body);
    }
    
    @DELETE
    @Path("{path: .*}")
    public Response proxyDelete(@PathParam("path") String path) {
        return proxyRequest("DELETE", path, null);
    }
    
    @PATCH
    @Path("{path: .*}")
    public Response proxyPatch(@PathParam("path") String path, String body) {
        return proxyRequest("PATCH", path, body);
    }
    
    private Response proxyRequest(String method, String path, String body) {
        try {
            // パスの正規化
            String normalizedPath = java.nio.file.Paths.get("/", path).toString().replace('\\', '/');
            
            logger.info(String.format("Proxying %s request to path: %s", method, normalizedPath));
            
            // サービスURLを解決
            String serviceUrl = routingService.resolveServiceUrl(normalizedPath);
            
            // サービスのヘルス状態を確認
            if (!routingService.isServiceHealthy(serviceUrl)) {
                logger.warning(String.format("Service %s is unhealthy", serviceUrl));
                return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\": \"Service Unavailable\", \"message\": \"The requested service is currently unavailable\"}")
                    .build();
            }
            
            // HTTPクライアントでリクエストを転送
            Client client = routingService.getHttpClient();
            WebTarget target = client.target(serviceUrl).path(normalizedPath);
            
            // クエリパラメータを追加
            MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
            for (var entry : queryParams.entrySet()) {
                String key = entry.getKey();
                for (String value : entry.getValue()) {
                    target = target.queryParam(key, value);
                }
            }
            
            // リクエストビルダーを作成
            Invocation.Builder requestBuilder = target.request();
            
            // ヘッダーを転送（一部除外）
            copyHeaders(requestBuilder);
            
            // リクエストを実行
            Response response;
            if (body != null && !body.isEmpty()) {
                String contentType = headers.getHeaderString("Content-Type");
                if (contentType == null) {
                    contentType = MediaType.APPLICATION_JSON;
                }
                response = requestBuilder.method(method, Entity.entity(body, contentType));
            } else {
                response = requestBuilder.method(method);
            }
            
            // レスポンスを返す
            return Response.status(response.getStatus())
                .entity(response.readEntity(String.class))
                .replaceAll(copyResponseHeaders(response))
                .build();
                
        } catch (IllegalArgumentException e) {
            logger.warning("Unknown service path: " + path);
            return Response.status(Response.Status.NOT_FOUND)
                .entity("{\"error\": \"Not Found\", \"message\": \"The requested resource was not found\"}")
                .build();
        } catch (Exception e) {
            logger.severe(String.format("Error proxying request to %s: %s", path, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Internal Server Error\", \"message\": \"An unexpected error occurred\"}")
                .build();
        }
    }
    
    private void copyHeaders(Invocation.Builder requestBuilder) {
        // 転送するヘッダー
        String[] headersToForward = {
            "Authorization", "Content-Type", "Accept", "Accept-Language",
            "User-Agent", "X-Forwarded-For", "X-Real-IP"
        };
        
        for (String headerName : headersToForward) {
            String headerValue = headers.getHeaderString(headerName);
            if (headerValue != null) {
                requestBuilder.header(headerName, headerValue);
            }
        }
        
        // カスタムヘッダー（X-で始まるもの）も転送
        for (String headerName : headers.getRequestHeaders().keySet()) {
            if (headerName.startsWith("X-") && !headerName.equals("X-Forwarded-For") && !headerName.equals("X-Real-IP")) {
                String headerValue = headers.getHeaderString(headerName);
                if (headerValue != null) {
                    requestBuilder.header(headerName, headerValue);
                }
            }
        }
        
        // 認証情報をリクエストプロパティから取得して転送
        String userId = (String) request.getAttribute("auth.userId");
        String username = (String) request.getAttribute("auth.username");
        
        if (userId != null) {
            requestBuilder.header("X-User-ID", userId);
        }
        if (username != null) {
            requestBuilder.header("X-Username", username);
        }
    }
    
    private MultivaluedMap<String, Object> copyResponseHeaders(Response response) {
        MultivaluedMap<String, Object> responseHeaders = new MultivaluedHashMap<>();
        
        // 転送するレスポンスヘッダー
        String[] headersToForward = {
            "Content-Type", "Content-Length", "Cache-Control", "ETag",
            "Last-Modified", "Location", "Set-Cookie"
        };
        
        for (String headerName : headersToForward) {
            Object headerValue = response.getHeaders().getFirst(headerName);
            if (headerValue != null) {
                responseHeaders.add(headerName, headerValue);
            }
        }
        
        return responseHeaders;
    }
}
