package com.skiresort.order.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 注文番号生成サービス
 */
@ApplicationScoped
public class OrderNumberService {
    
    private static final String ORDER_PREFIX = "ORD";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    
    /**
     * 注文番号を生成
     * フォーマット: ORD-YYYYMMDD-XXXXXXXX
     */
    public String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DATE_FORMAT);
        long sequence = SEQUENCE.getAndIncrement();
        
        // シーケンス番号を8桁にパディング
        String sequencePart = String.format("%08d", sequence);
        
        return String.format("%s-%s-%s", ORDER_PREFIX, datePart, sequencePart);
    }
    
    /**
     * 注文番号のバリデーション
     */
    public boolean isValidOrderNumber(String orderNumber) {
        if (orderNumber == null || orderNumber.trim().isEmpty()) {
            return false;
        }
        
        // フォーマット: ORD-YYYYMMDD-XXXXXXXX (20文字)
        String pattern = "^ORD-\\d{8}-\\d{8}$";
        return orderNumber.matches(pattern);
    }
    
    /**
     * 注文番号から日付部分を抽出
     */
    public String extractDateFromOrderNumber(String orderNumber) {
        if (!isValidOrderNumber(orderNumber)) {
            throw new IllegalArgumentException("Invalid order number format: " + orderNumber);
        }
        
        String[] parts = orderNumber.split("-");
        return parts[1]; // YYYYMMDD部分
    }
    
    /**
     * 注文番号からシーケンス部分を抽出
     */
    public String extractSequenceFromOrderNumber(String orderNumber) {
        if (!isValidOrderNumber(orderNumber)) {
            throw new IllegalArgumentException("Invalid order number format: " + orderNumber);
        }
        
        String[] parts = orderNumber.split("-");
        return parts[2]; // XXXXXXXX部分
    }
    
    /**
     * 次のシーケンス番号を取得（テスト用）
     */
    public long getNextSequence() {
        return SEQUENCE.get();
    }
    
    /**
     * シーケンス番号をリセット（テスト用）
     */
    public void resetSequence() {
        SEQUENCE.set(1);
    }
}
