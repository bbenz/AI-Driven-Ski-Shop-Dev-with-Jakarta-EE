-- Sample data for development and testing
-- import.sql

-- Sample shopping carts
INSERT INTO shopping_carts (id, cart_id, customer_id, session_id, status, currency, subtotal_amount, tax_amount, shipping_amount, discount_amount, total_amount, expires_at) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'cart-001', '550e8400-e29b-41d4-a716-446655440101', null, 'ACTIVE', 'JPY', 15000.00, 1500.00, 0.00, 1000.00, 15500.00, CURRENT_TIMESTAMP + INTERVAL '8 hours'),
('550e8400-e29b-41d4-a716-446655440002', 'cart-002', null, 'session-12345', 'ACTIVE', 'JPY', 8000.00, 800.00, 500.00, 0.00, 9300.00, CURRENT_TIMESTAMP + INTERVAL '8 hours'),
('550e8400-e29b-41d4-a716-446655440003', 'cart-003', '550e8400-e29b-41d4-a716-446655440102', null, 'ACTIVE', 'JPY', 0.00, 0.00, 0.00, 0.00, 0.00, CURRENT_TIMESTAMP + INTERVAL '8 hours');

-- Sample cart items
INSERT INTO cart_items (id, cart_id, product_id, sku, product_name, product_image_url, unit_price, quantity, total_price) VALUES
('550e8400-e29b-41d4-a716-446655440201', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440301', 'SKI-BOOT-001', 'Alpine Ski Boots - Size 27.0', '/images/ski-boot-001.jpg', 12000.00, 1, 12000.00),
('550e8400-e29b-41d4-a716-446655440202', '550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440302', 'SKI-HELMET-001', 'Safety Ski Helmet - Large', '/images/helmet-001.jpg', 3000.00, 1, 3000.00),
('550e8400-e29b-41d4-a716-446655440203', '550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440303', 'SKI-GLOVE-001', 'Waterproof Ski Gloves - Medium', '/images/glove-001.jpg', 4000.00, 2, 8000.00);

-- Sample applied coupons
INSERT INTO applied_coupons (id, cart_id, coupon_code, coupon_name, discount_type, discount_amount) VALUES
('550e8400-e29b-41d4-a716-446655440401', '550e8400-e29b-41d4-a716-446655440001', 'WINTER10', 'Winter Sale 10% Off', 'PERCENTAGE', 1000.00);
