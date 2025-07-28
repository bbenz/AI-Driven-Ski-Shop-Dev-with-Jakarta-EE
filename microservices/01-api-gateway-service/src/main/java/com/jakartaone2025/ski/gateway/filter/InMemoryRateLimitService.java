package com.jakartaone2025.ski.gateway.filter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * In-Memory Rate Limit Service Implementation
 * 本番環境では Redis などの分散キャッシュを使用することを推奨
 */
@ApplicationScoped
public class InMemoryRateLimitService implements RateLimitService {
    
    private static final Logger logger = Logger.getLogger(InMemoryRateLimitService.class.getName());
    
    private final int defaultRequestsPerMinute;
    private final int authRequestsPerMinute;
    private final int windowSeconds;
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitMap = new ConcurrentHashMap<>();
    
    @Inject
    public InMemoryRateLimitService(
            @ConfigProperty(name = "gateway.ratelimit.default.requests", defaultValue = "100") int defaultRequestsPerMinute,
            @ConfigProperty(name = "gateway.ratelimit.auth.requests", defaultValue = "200") int authRequestsPerMinute,
            @ConfigProperty(name = "gateway.ratelimit.window.seconds", defaultValue = "60") int windowSeconds) {
        this.defaultRequestsPerMinute = defaultRequestsPerMinute;
        this.authRequestsPerMinute = authRequestsPerMinute;
        this.windowSeconds = windowSeconds;
    }
    
    @Override
    public boolean isRequestAllowed(String clientId, String path) {
        int limit = getLimit(path);
        String key = generateKey(clientId, path);
        
        rateLimitMap.compute(key, (k, entry) -> {
            Instant now = Instant.now();
            
            if (entry == null) {
                return new RateLimitEntry(now, new AtomicInteger(1), limit);
            }
            
            // ウィンドウがリセットされた場合
            if (now.isAfter(entry.windowStart.plus(Duration.ofSeconds(windowSeconds)))) {
                return new RateLimitEntry(now, new AtomicInteger(1), limit);
            }
            
            // 現在のウィンドウ内でのリクエスト
            entry.requestCount.incrementAndGet();
            return entry;
        });
        
        RateLimitEntry entry = rateLimitMap.get(key);
        return entry.requestCount.get() <= limit;
    }
    
    @Override
    public int getLimit(String path) {
        if (path.startsWith("/auth")) {
            return authRequestsPerMinute;
        }
        return defaultRequestsPerMinute;
    }
    
    @Override
    public int getRemaining(String clientId, String path) {
        String key = generateKey(clientId, path);
        RateLimitEntry entry = rateLimitMap.get(key);
        
        if (entry == null) {
            return getLimit(path);
        }
        
        int remaining = entry.limit - entry.requestCount.get();
        return Math.max(0, remaining);
    }
    
    @Override
    public long getResetTime(String clientId, String path) {
        String key = generateKey(clientId, path);
        RateLimitEntry entry = rateLimitMap.get(key);
        
        if (entry == null) {
            return Instant.now().plus(Duration.ofSeconds(windowSeconds)).getEpochSecond();
        }
        
        return entry.windowStart.plus(Duration.ofSeconds(windowSeconds)).getEpochSecond();
    }
    
    private String generateKey(String clientId, String path) {
        return String.format("%s:%s:%d", 
            clientId, 
            normalizePathForRateLimit(path),
            Instant.now().getEpochSecond() / windowSeconds
        );
    }
    
    private String normalizePathForRateLimit(String path) {
        // パスを正規化（例：/users/123 -> /users/{id}）
        if (path.matches("/users/\\d+.*")) {
            return "/users/{id}";
        }
        if (path.matches("/products/\\d+.*")) {
            return "/products/{id}";
        }
        if (path.matches("/orders/\\d+.*")) {
            return "/orders/{id}";
        }
        return path;
    }
    
    /**
     * Rate Limit Entry
     */
    private static class RateLimitEntry {
        final Instant windowStart;
        final AtomicInteger requestCount;
        final int limit;
        
        RateLimitEntry(Instant windowStart, AtomicInteger requestCount, int limit) {
            this.windowStart = windowStart;
            this.requestCount = requestCount;
            this.limit = limit;
        }
    }
    
    /**
     * Clean up expired entries (実運用では定期実行)
     */
    public void cleanupExpiredEntries() {
        Instant cutoff = Instant.now().minus(Duration.ofSeconds((long) windowSeconds * 2));
        
        rateLimitMap.entrySet().removeIf(entry -> {
            RateLimitEntry rateLimitEntry = entry.getValue();
            return rateLimitEntry.windowStart.isBefore(cutoff);
        });
        
        logger.info("Cleaned up expired rate limit entries. Current size: " + rateLimitMap.size());
    }
}
