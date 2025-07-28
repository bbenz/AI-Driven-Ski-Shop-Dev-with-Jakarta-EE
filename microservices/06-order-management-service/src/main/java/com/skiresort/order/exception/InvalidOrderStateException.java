package com.skiresort.order.exception;

import com.skiresort.order.model.OrderStatus;

/**
 * 無効な注文状態例外
 */
public class InvalidOrderStateException extends RuntimeException {
    
    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;
    
    public InvalidOrderStateException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }
    
    public InvalidOrderStateException(String message, OrderStatus currentStatus) {
        super(message);
        this.currentStatus = currentStatus;
        this.targetStatus = null;
    }
    
    public InvalidOrderStateException(String message, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(message);
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
        this.currentStatus = null;
        this.targetStatus = null;
    }
    
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
    
    public static InvalidOrderStateException cannotTransition(OrderStatus from, OrderStatus to) {
        return new InvalidOrderStateException(
            String.format("注文ステータスを %s から %s に変更できません", 
                from.getDescription(), to.getDescription()), 
            from, to
        );
    }
    
    public static InvalidOrderStateException cannotCancel(OrderStatus currentStatus) {
        return new InvalidOrderStateException(
            String.format("現在のステータス %s ではキャンセルできません", 
                currentStatus.getDescription()), 
            currentStatus
        );
    }
    
    public static InvalidOrderStateException cannotModify(OrderStatus currentStatus) {
        return new InvalidOrderStateException(
            String.format("現在のステータス %s では変更できません", 
                currentStatus.getDescription()), 
            currentStatus
        );
    }
}
