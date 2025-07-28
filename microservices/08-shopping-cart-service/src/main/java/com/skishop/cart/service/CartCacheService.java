package com.skishop.cart.service;

import com.skishop.cart.dto.CartResponse;
import com.skishop.cart.dto.CartSummaryDto;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;

@ApplicationScoped
public class CartCacheService {
    
    private static final Logger LOG = Logger.getLogger(CartCacheService.class);
    
    private static final String CART_CACHE_KEY_PREFIX = "cart:cache:";
    private static final String CART_SUMMARY_KEY_PREFIX = "cart:summary:";
    private static final String CART_SESSION_KEY_PREFIX = "cart:session:";
    private static final String GUEST_CART_KEY_PREFIX = "guest:cart:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    private static final Duration SESSION_TTL = Duration.ofHours(8);
    
    @Inject
    RedisDataSource redis;
    
    @Inject
    ObjectMapper objectMapper;
    
    private ValueCommands<String, String> valueCommands() {
        return redis.value(String.class);
    }
    
    public void cacheCart(String cartId, CartResponse cart) {
        try {
            String key = CART_CACHE_KEY_PREFIX + cartId;
            String cartJson = objectMapper.writeValueAsString(cart);
            valueCommands().setex(key, CACHE_TTL.getSeconds(), cartJson);
            
            LOG.debugf("Cached cart: %s", cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to cache cart: %s", cartId);
        }
    }
    
    public CartResponse getCachedCart(String cartId) {
        try {
            String key = CART_CACHE_KEY_PREFIX + cartId;
            String cartJson = valueCommands().get(key);
            
            if (cartJson != null) {
                CartResponse cart = objectMapper.readValue(cartJson, CartResponse.class);
                LOG.debugf("Retrieved cart from cache: %s", cartId);
                return cart;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve cart from cache: %s", cartId);
        }
        
        return null;
    }
    
    public void cacheCartSummary(String cartId, String customerId, CartSummaryDto summary) {
        try {
            String key = CART_SUMMARY_KEY_PREFIX + cartId + ":" + (customerId != null ? customerId : "anonymous");
            String summaryJson = objectMapper.writeValueAsString(summary);
            valueCommands().setex(key, CACHE_TTL.getSeconds(), summaryJson);
            
            LOG.debugf("Cached cart summary: %s", cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to cache cart summary: %s", cartId);
        }
    }
    
    public CartSummaryDto getCachedCartSummary(String cartId, String customerId) {
        try {
            String key = CART_SUMMARY_KEY_PREFIX + cartId + ":" + (customerId != null ? customerId : "anonymous");
            String summaryJson = valueCommands().get(key);
            
            if (summaryJson != null) {
                CartSummaryDto summary = objectMapper.readValue(summaryJson, CartSummaryDto.class);
                LOG.debugf("Retrieved cart summary from cache: %s", cartId);
                return summary;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve cart summary from cache: %s", cartId);
        }
        
        return null;
    }
    
    public void invalidateCart(String cartId) {
        try {
            String key = CART_CACHE_KEY_PREFIX + cartId;
            redis.key().del(key);
            
            LOG.debugf("Invalidated cart cache: %s", cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to invalidate cart cache: %s", cartId);
        }
    }
    
    // Session-based guest cart management
    public void storeGuestCartSession(String sessionId, String cartId) {
        try {
            String key = CART_SESSION_KEY_PREFIX + sessionId;
            valueCommands().setex(key, SESSION_TTL.getSeconds(), cartId);
            
            LOG.debugf("Stored guest cart session: %s -> %s", sessionId, cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to store guest cart session: %s", sessionId);
        }
    }
    
    public String getGuestCartBySession(String sessionId) {
        try {
            String key = CART_SESSION_KEY_PREFIX + sessionId;
            String cartId = valueCommands().get(key);
            
            if (cartId != null) {
                LOG.debugf("Retrieved guest cart by session: %s -> %s", sessionId, cartId);
                return cartId;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve guest cart by session: %s", sessionId);
        }
        
        return null;
    }
    
    public void removeGuestCartSession(String sessionId) {
        try {
            String key = CART_SESSION_KEY_PREFIX + sessionId;
            redis.key().del(key);
            
            LOG.debugf("Removed guest cart session: %s", sessionId);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to remove guest cart session: %s", sessionId);
        }
    }
    
    // Cart lock for concurrent operations
    public boolean acquireCartLock(String cartId, Duration lockDuration) {
        try {
            String lockKey = "cart:lock:" + cartId;
            String lockValue = String.valueOf(System.currentTimeMillis());
            
            // Use SET with NX and EX options for atomic lock acquisition
            var result = redis.execute("SET", lockKey, lockValue, "NX", "EX", String.valueOf(lockDuration.getSeconds()));
            
            boolean acquired = "OK".equals(result.toString());
            if (acquired) {
                LOG.debugf("Acquired cart lock: %s", cartId);
            } else {
                LOG.debugf("Failed to acquire cart lock: %s", cartId);
            }
            
            return acquired;
        } catch (Exception e) {
            LOG.errorf(e, "Error acquiring cart lock: %s", cartId);
            return false;
        }
    }
    
    public void releaseCartLock(String cartId) {
        try {
            String lockKey = "cart:lock:" + cartId;
            redis.key().del(lockKey);
            
            LOG.debugf("Released cart lock: %s", cartId);
        } catch (Exception e) {
            LOG.errorf(e, "Error releasing cart lock: %s", cartId);
        }
    }
    
    // Temporary item operations for concurrent add/remove
    public void storeTempItem(String cartId, String sku, int quantity) {
        try {
            String key = "cart:temp:" + cartId + ":" + sku;
            valueCommands().setex(key, Duration.ofMinutes(5).getSeconds(), String.valueOf(quantity));
            
            LOG.debugf("Stored temp item: %s/%s -> %d", cartId, sku, quantity);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to store temp item: %s/%s", cartId, sku);
        }
    }
    
    public Integer getTempItem(String cartId, String sku) {
        try {
            String key = "cart:temp:" + cartId + ":" + sku;
            String quantityStr = valueCommands().get(key);
            
            if (quantityStr != null) {
                int quantity = Integer.parseInt(quantityStr);
                LOG.debugf("Retrieved temp item: %s/%s -> %d", cartId, sku, quantity);
                return quantity;
            }
        } catch (Exception e) {
            LOG.errorf(e, "Failed to retrieve temp item: %s/%s", cartId, sku);
        }
        
        return null;
    }
    
    public void removeTempItem(String cartId, String sku) {
        try {
            String key = "cart:temp:" + cartId + ":" + sku;
            redis.key().del(key);
            
            LOG.debugf("Removed temp item: %s/%s", cartId, sku);
        } catch (Exception e) {
            LOG.errorf(e, "Failed to remove temp item: %s/%s", cartId, sku);
        }
    }
    
    // Cache statistics and monitoring
    public long getCacheSize() {
        try {
            return redis.key().keys(CART_CACHE_KEY_PREFIX + "*").size();
        } catch (Exception e) {
            LOG.errorf(e, "Failed to get cache size");
            return -1;
        }
    }
    
    public void clearAllCaches() {
        try {
            var cartKeys = redis.key().keys(CART_CACHE_KEY_PREFIX + "*");
            var sessionKeys = redis.key().keys(CART_SESSION_KEY_PREFIX + "*");
            var guestKeys = redis.key().keys(GUEST_CART_KEY_PREFIX + "*");
            
            if (!cartKeys.isEmpty()) {
                redis.key().del(cartKeys.toArray(new String[0]));
            }
            if (!sessionKeys.isEmpty()) {
                redis.key().del(sessionKeys.toArray(new String[0]));
            }
            if (!guestKeys.isEmpty()) {
                redis.key().del(guestKeys.toArray(new String[0]));
            }
            
            LOG.info("Cleared all cart caches");
        } catch (Exception e) {
            LOG.errorf(e, "Failed to clear all caches");
        }
    }
}
