package com.skiresort.order.exception;

/**
 * 注文が見つからない例外
 */
public class OrderNotFoundException extends RuntimeException {
    
    private final String orderId;
    private final String orderNumber;
    
    public OrderNotFoundException(String message) {
        super(message);
        this.orderId = null;
        this.orderNumber = null;
    }
    
    public OrderNotFoundException(String message, String orderId) {
        super(message);
        this.orderId = orderId;
        this.orderNumber = null;
    }
    
    public OrderNotFoundException(String message, String orderId, String orderNumber) {
        super(message);
        this.orderId = orderId;
        this.orderNumber = orderNumber;
    }
    
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.orderId = null;
        this.orderNumber = null;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getOrderNumber() {
        return orderNumber;
    }
    
    public static OrderNotFoundException forId(String orderId) {
        return new OrderNotFoundException(
            String.format("注文が見つかりません。ID: %s", orderId), orderId);
    }
    
    public static OrderNotFoundException forOrderNumber(String orderNumber) {
        return new OrderNotFoundException(
            String.format("注文が見つかりません。注文番号: %s", orderNumber), null, orderNumber);
    }
}
