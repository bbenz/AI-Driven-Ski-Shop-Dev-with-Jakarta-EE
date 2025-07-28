-- V1__Create_shopping_carts_table.sql
CREATE TABLE shopping_carts (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    cart_id VARCHAR(100) UNIQUE NOT NULL,
    customer_id UUID,
    session_id VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    currency VARCHAR(3) NOT NULL DEFAULT 'JPY',
    subtotal_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    shipping_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE,
    converted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes
CREATE INDEX idx_shopping_carts_customer_id ON shopping_carts(customer_id);
CREATE INDEX idx_shopping_carts_session_id ON shopping_carts(session_id);
CREATE INDEX idx_shopping_carts_status ON shopping_carts(status);
CREATE INDEX idx_shopping_carts_expires_at ON shopping_carts(expires_at);
CREATE INDEX idx_shopping_carts_created_at ON shopping_carts(created_at);
