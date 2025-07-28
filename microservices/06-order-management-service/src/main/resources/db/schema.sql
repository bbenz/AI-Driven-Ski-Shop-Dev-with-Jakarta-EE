-- 注文管理サービス用データベーススキーマ

-- 注文テーブル
CREATE TABLE orders (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number VARCHAR(50) UNIQUE NOT NULL,
    customer_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    
    -- 金額情報
    subtotal_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) DEFAULT 0.00,
    tax_amount DECIMAL(10,2) DEFAULT 0.00,
    shipping_amount DECIMAL(8,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    
    -- 配送先住所
    shipping_address_name VARCHAR(100),
    shipping_address_postal_code VARCHAR(20),
    shipping_address_address1 VARCHAR(200),
    shipping_address_address2 VARCHAR(200),
    shipping_address_city VARCHAR(100),
    shipping_address_state VARCHAR(100),
    shipping_address_country VARCHAR(100),
    shipping_address_phone VARCHAR(20),
    
    -- 請求先住所
    billing_address_name VARCHAR(100),
    billing_address_postal_code VARCHAR(20),
    billing_address_address1 VARCHAR(200),
    billing_address_address2 VARCHAR(200),
    billing_address_city VARCHAR(100),
    billing_address_state VARCHAR(100),
    billing_address_country VARCHAR(100),
    billing_address_phone VARCHAR(20),
    
    -- 注文メモ
    order_notes TEXT,
    special_instructions TEXT,
    
    -- 日時情報
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    paid_at TIMESTAMP,
    
    CONSTRAINT chk_orders_status CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_orders_payment_status CHECK (payment_status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED'))
);

-- 注文明細テーブル
CREATE TABLE order_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    discount_amount DECIMAL(8,2) DEFAULT 0.00,
    tax_amount DECIMAL(8,2) DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    reservation_id UUID,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 注文ステータス履歴テーブル
CREATE TABLE order_status_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    changed_by VARCHAR(100) NOT NULL,
    reason VARCHAR(500),
    notes VARCHAR(1000),
    automatic_change BOOLEAN DEFAULT FALSE,
    system_reference VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- インデックス作成
CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_payment_status ON orders(payment_status);
CREATE INDEX idx_orders_order_number ON orders(order_number);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_confirmed_at ON orders(confirmed_at);
CREATE INDEX idx_orders_shipped_at ON orders(shipped_at);
CREATE INDEX idx_orders_delivered_at ON orders(delivered_at);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
CREATE INDEX idx_order_items_sku ON order_items(sku);
CREATE INDEX idx_order_items_reservation_id ON order_items(reservation_id);

CREATE INDEX idx_order_status_history_order_id ON order_status_history(order_id);
CREATE INDEX idx_order_status_history_new_status ON order_status_history(new_status);
CREATE INDEX idx_order_status_history_changed_by ON order_status_history(changed_by);
CREATE INDEX idx_order_status_history_created_at ON order_status_history(created_at);

-- 更新時間自動更新トリガー
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_orders_updated_at 
    BEFORE UPDATE ON orders 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_order_items_updated_at 
    BEFORE UPDATE ON order_items 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- サンプルデータ（開発・テスト用）
INSERT INTO orders (
    id, order_number, customer_id, status, payment_status,
    subtotal_amount, tax_amount, total_amount,
    shipping_address_name, shipping_address_postal_code, 
    shipping_address_address1, shipping_address_city, shipping_address_country,
    created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'ORD-20241201-00000001',
    '550e8400-e29b-41d4-a716-446655440101',
    'PENDING',
    'PENDING',
    15000.00,
    1500.00,
    16500.00,
    '山田太郎',
    '100-0001',
    '東京都千代田区千代田1-1',
    '千代田区',
    'Japan',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO order_items (
    id, order_id, product_id, sku, product_name,
    unit_price, quantity, tax_amount, total_amount,
    created_at, updated_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440201',
    '550e8400-e29b-41d4-a716-446655440001',
    '550e8400-e29b-41d4-a716-446655440301',
    'SKI-LIFT-001',
    'スキーリフト1日券',
    5000.00,
    3,
    1500.00,
    16500.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO order_status_history (
    id, order_id, previous_status, new_status, changed_by, reason,
    automatic_change, system_reference, created_at
) VALUES (
    '550e8400-e29b-41d4-a716-446655440401',
    '550e8400-e29b-41d4-a716-446655440001',
    NULL,
    'PENDING',
    'SYSTEM',
    '注文作成',
    TRUE,
    'ORDER_CREATED',
    CURRENT_TIMESTAMP
);
