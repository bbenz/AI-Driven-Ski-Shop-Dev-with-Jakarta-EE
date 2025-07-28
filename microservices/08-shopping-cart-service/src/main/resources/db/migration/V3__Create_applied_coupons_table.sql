-- V3__Create_applied_coupons_table.sql
CREATE TABLE applied_coupons (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    coupon_code VARCHAR(50) NOT NULL,
    coupon_name VARCHAR(255) NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL,
    applied_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_applied_coupons_cart_id ON applied_coupons(cart_id);
CREATE INDEX idx_applied_coupons_coupon_code ON applied_coupons(coupon_code);
