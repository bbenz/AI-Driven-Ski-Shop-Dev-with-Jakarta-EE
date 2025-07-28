-- テスト用データ
-- import-test.sql

INSERT INTO brands (id, name, description, country, is_featured, is_active, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'Test Brand', 'テスト用ブランド', 'Japan', true, true, CURRENT_TIMESTAMP);

INSERT INTO categories (id, name, description, parent_id, path, level, sort_order, is_active, created_at)
VALUES
    ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Test Category', 'テスト用カテゴリ', NULL, '/test', 0, 1, true, CURRENT_TIMESTAMP);

INSERT INTO products (id, sku, name, description, category_id, brand_id, material, ski_type, difficulty_level, 
                     publish_status, is_active, is_featured, base_price, created_at)
VALUES
    ('00000001-0000-0000-0000-000000000001', 'TEST-SKI-001', 'Test Ski 1', 'テスト用スキー1', 
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 
     'COMPOSITE', 'ALL_MOUNTAIN', 'INTERMEDIATE', 'PUBLISHED', true, true, 50000.00, CURRENT_TIMESTAMP),
    
    ('00000002-0000-0000-0000-000000000002', 'TEST-SKI-002', 'Test Ski 2', 'テスト用スキー2', 
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 
     'CARBON', 'CARVING', 'ADVANCED', 'PUBLISHED', true, true, 75000.00, CURRENT_TIMESTAMP),
     
    ('00000003-0000-0000-0000-000000000003', 'TEST-SKI-003', 'Test Ski 3', 'テスト用スキー3', 
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 
     'WOOD', 'FREESTYLE', 'BEGINNER', 'PUBLISHED', true, true, 60000.00, CURRENT_TIMESTAMP),
     
    ('00000004-0000-0000-0000-000000000004', 'ROX-CARV-165', 'Rossignol Carving', 'テスト用カービング', 
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 
     'COMPOSITE', 'CARVING', 'INTERMEDIATE', 'PUBLISHED', true, true, 80000.00, CURRENT_TIMESTAMP),
     
    ('00000005-0000-0000-0000-000000000005', 'TEST-SKI-005', 'Test Ski 5', 'テスト用スキー5', 
     'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '11111111-1111-1111-1111-111111111111', 
     'TITANIUM', 'RACING', 'EXPERT', 'PUBLISHED', true, true, 120000.00, CURRENT_TIMESTAMP);
