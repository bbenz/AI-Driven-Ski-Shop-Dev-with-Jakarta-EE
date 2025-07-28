-- 完全修正済みサンプルデータの挿入
-- V1.0.4__Insert_final_sample_data.sql

-- ブランドマスタデータ
INSERT INTO brands (id, name, description, country, is_featured, is_active, created_at, updated_at)
VALUES
    ('b0000001-0000-0000-0000-000000000001'::uuid, 'Rossignol', 'フランスの老舗スキーメーカー', 'France', true, true, NOW(), NOW()),
    ('b0000002-0000-0000-0000-000000000002'::uuid, 'Salomon', 'アルペンスポーツのリーディングブランド', 'France', true, true, NOW(), NOW()),
    ('b0000003-0000-0000-0000-000000000003'::uuid, 'K2', 'アメリカ発祥のスキーブランド', 'USA', true, true, NOW(), NOW()),
    ('b0000004-0000-0000-0000-000000000004'::uuid, 'Völkl', 'ドイツ製高品質スキー', 'Germany', true, true, NOW(), NOW()),
    ('b0000005-0000-0000-0000-000000000005'::uuid, 'Atomic', 'オーストリアの技術革新ブランド', 'Austria', true, true, NOW(), NOW());

-- カテゴリマスタデータ
INSERT INTO categories (id, name, description, parent_id, path, level, sort_order, is_active, created_at, updated_at)
VALUES
    ('c0000001-0000-0000-0000-000000000001'::uuid, 'スキー', 'スキー用品全般', NULL, '/ski', 0, 1, true, NOW(), NOW()),
    ('c0000002-0000-0000-0000-000000000002'::uuid, 'カービングスキー', '高速カービングターン用', 'c0000001-0000-0000-0000-000000000001'::uuid, '/ski/carving', 1, 1, true, NOW(), NOW()),
    ('c0000003-0000-0000-0000-000000000003'::uuid, 'オールマウンテンスキー', 'あらゆる雪面対応', 'c0000001-0000-0000-0000-000000000001'::uuid, '/ski/allmountain', 1, 2, true, NOW(), NOW()),
    ('c0000004-0000-0000-0000-000000000004'::uuid, 'レーシングスキー', '競技用高性能スキー', 'c0000001-0000-0000-0000-000000000001'::uuid, '/ski/racing', 1, 3, true, NOW(), NOW()),
    ('c0000005-0000-0000-0000-000000000005'::uuid, '初心者向けスキー', '初心者用扱いやすいスキー', 'c0000001-0000-0000-0000-000000000001'::uuid, '/ski/beginner', 1, 4, true, NOW(), NOW());

-- 商品マスタデータ
INSERT INTO products (id, name, description, brand_id, category_id, sku, base_price, material, ski_type, difficulty_level, flex, publish_status, published_at, created_at, updated_at, is_active)
VALUES
    ('00000001-0000-0000-0000-000000000001'::uuid, 'Rossignol Experience 86 Basalt', '中上級者向けオールマウンテンスキー', 'b0000001-0000-0000-0000-000000000001'::uuid, 'c0000003-0000-0000-0000-000000000003'::uuid, 'ROX-EXP86-BAS', 82800, 'WOOD_CORE', 'ALL_MOUNTAIN', 'INTERMEDIATE', 'MEDIUM', 'PUBLISHED', NOW(), NOW(), NOW(), true),
    ('00000002-0000-0000-0000-000000000002'::uuid, 'Salomon S/Force Bold', '高性能カービングスキー', 'b0000002-0000-0000-0000-000000000002'::uuid, 'c0000002-0000-0000-0000-000000000002'::uuid, 'SAL-SFB-2024', 98000, 'CARBON_FIBER', 'CARVING', 'ADVANCED', 'HARD', 'PUBLISHED', NOW(), NOW(), NOW(), true),
    ('00000003-0000-0000-0000-000000000003'::uuid, 'K2 Mindbender 99Ti', 'チタン層搭載オールマウンテンスキー', 'b0000003-0000-0000-0000-000000000003'::uuid, 'c0000003-0000-0000-0000-000000000003'::uuid, 'K2-MB99TI', 108000, 'TITANIUM', 'ALL_MOUNTAIN', 'EXPERT', 'HARD', 'PUBLISHED', NOW(), NOW(), NOW(), true);
