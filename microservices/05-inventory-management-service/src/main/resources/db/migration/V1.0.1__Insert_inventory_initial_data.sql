-- 在庫管理サービス初期データ投入
-- V1.0.1__Insert_inventory_initial_data.sql

-- 店舗・倉庫マスタデータ
INSERT INTO locations (code, name, location_type, address, phone, manager_name, is_active, created_at) VALUES
    ('MAIN_STORE', 'メインストア', 'STORE', '長野県白馬村北城1234-1', '0261-85-1234', '田中一郎', TRUE, NOW()),
    ('RENTAL_COUNTER', 'レンタルカウンター', 'STORE', '長野県白馬村北城1234-2', '0261-85-1235', '佐藤花子', TRUE, NOW()),
    ('WAREHOUSE_A', 'メイン倉庫A', 'WAREHOUSE', '長野県白馬村北城1234-10', '0261-85-1240', '鈴木次郎', TRUE, NOW()),
    ('WAREHOUSE_B', 'サブ倉庫B', 'WAREHOUSE', '長野県白馬村北城1234-11', '0261-85-1241', '高橋三郎', TRUE, NOW()),
    ('REPAIR_SHOP', 'メンテナンス工房', 'REPAIR_SHOP', '長野県白馬村北城1234-20', '0261-85-1250', '山田修', TRUE, NOW());

-- 商品カタログサービスと連携する設備マスタデータ（30商品すべて）
INSERT INTO equipment (product_id, sku, name, category, brand, equipment_type, size_range, difficulty_level, daily_rate, description, image_url, is_rental_available, is_active, created_at, updated_at) VALUES
    -- スキー板
    ('00000001-0000-0000-0000-000000000001'::uuid, 'ROX-HA-GS-2024', 'Rossignol Hero Athlete FIS GS', 'レーシング GS', 'Rossignol', 'SKI_BOARD', '180-190cm', 'EXPERT', 8000, 'FIS公認ジャイアントスラローム用レーシングスキー', '/images/products/rossignol-hero-athlete-gs-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000002-0000-0000-0000-000000000002'::uuid, 'ATO-X9-GS-2024', 'Atomic Redster X9 WC GS', 'レーシング GS', 'Atomic', 'SKI_BOARD', '175-185cm', 'EXPERT', 8500, 'ワールドカップ仕様GS板', '/images/products/atomic-x9-gs-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000003-0000-0000-0000-000000000003'::uuid, 'ATO-RG9-SL-2024', 'Atomic Redster G9 FIS SL', 'レーシング SL', 'Atomic', 'SKI_BOARD', '155-165cm', 'EXPERT', 7500, 'FIS公認スラローム用レーシングスキー', '/images/products/atomic-redster-g9-sl-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000004-0000-0000-0000-000000000004'::uuid, 'SAL-RR-SL-2024', 'Salomon S/Race Rush SL', 'レーシング SL', 'Salomon', 'SKI_BOARD', '155-165cm', 'EXPERT', 7000, 'レーシングスラローム板', '/images/products/salomon-race-rush-sl-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000005-0000-0000-0000-000000000005'::uuid, 'K2-MB108TI-2024', 'K2 Mindbender 108Ti', 'パウダー', 'K2', 'SKI_BOARD', '172-188cm', 'ADVANCED', 6000, 'パウダースノー対応オールマウンテンスキー', '/images/products/k2-mindbender-108ti-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000006-0000-0000-0000-000000000006'::uuid, 'VOL-M6-2024', 'Volkl Mantra M6', 'パウダー', 'Volkl', 'SKI_BOARD', '170-185cm', 'ADVANCED', 5800, 'パウダー・オールマウンテンスキー', '/images/products/volkl-mantra-m6-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000007-0000-0000-0000-000000000007'::uuid, 'NOR-E110-2024', 'Nordica Enforcer 110 Free', 'パウダー', 'Nordica', 'SKI_BOARD', '172-188cm', 'ADVANCED', 6200, 'フリーライド・パウダースキー', '/images/products/nordica-enforcer-110-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000008-0000-0000-0000-000000000008'::uuid, 'K2-MOG-2024', 'K2 Mogul Ski', 'モーグル', 'K2', 'SKI_BOARD', '160-170cm', 'ADVANCED', 5500, 'モーグル専用スキー板', '/images/products/k2-mogul-ski-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000009-0000-0000-0000-000000000009'::uuid, 'SAL-SMAX8-2024', 'Salomon S/Max 8', '初級・中級向け', 'Salomon', 'SKI_BOARD', '150-170cm', 'INTERMEDIATE', 4000, '中級者向けカービングスキー', '/images/products/salomon-smax8-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000010-0000-0000-0000-000000000010'::uuid, 'ROX-EX78-2024', 'Rossignol Experience 78 Ti', '初級・中級向け', 'Rossignol', 'SKI_BOARD', '150-170cm', 'INTERMEDIATE', 3800, '初中級者向けオールマウンテン', '/images/products/rossignol-experience-78ti-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000011-0000-0000-0000-000000000011'::uuid, 'ATO-V75C-2024', 'Atomic Vantage 75 C', '初級・中級向け', 'Atomic', 'SKI_BOARD', '140-160cm', 'BEGINNER', 3200, '初級者向けカービングスキー', '/images/products/atomic-vantage-75c-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000012-0000-0000-0000-000000000012'::uuid, 'ROX-EXP-JR-2024', 'Rossignol Experience Pro Jr', 'ジュニア', 'Rossignol', 'SKI_BOARD', '120-150cm', 'BEGINNER', 2500, 'ジュニア向けオールマウンテンスキー', '/images/products/rossignol-experience-pro-jr-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000013-0000-0000-0000-000000000013'::uuid, 'SAL-QST-JR-2024', 'Salomon QST Max Jr', 'ジュニア', 'Salomon', 'SKI_BOARD', '120-150cm', 'BEGINNER', 2300, 'ジュニア向けオールマウンテン', '/images/products/salomon-qst-max-jr-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- ストック
    ('00000014-0000-0000-0000-000000000014'::uuid, 'LEK-WC-GS-2024', 'Leki World Cup Racing GS', 'GS用ストック', 'Leki', 'POLE', '120-130cm', 'EXPERT', 1200, 'ワールドカップ仕様GSストック', '/images/products/leki-world-cup-gs-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000015-0000-0000-0000-000000000015'::uuid, 'ATO-AMT-SL-2024', 'Atomic AMT SL', 'SL用ストック', 'Atomic', 'POLE', '110-125cm', 'EXPERT', 1000, 'スラローム用レーシングストック', '/images/products/atomic-amt-sl-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000016-0000-0000-0000-000000000016'::uuid, 'BD-TRAV-2024', 'Black Diamond Traverse Pro', '伸縮式ストック', 'Black Diamond', 'POLE', '100-140cm', 'INTERMEDIATE', 800, '伸縮式ツーリングストック', '/images/products/bd-traverse-pro-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- スキーブーツ
    ('00000017-0000-0000-0000-000000000017'::uuid, 'SAL-SP120-2024', 'Salomon S/Pro 120', '全般', 'Salomon', 'BOOT', '25.0-29.0cm', 'ADVANCED', 3500, 'ハイパフォーマンススキーブーツ', '/images/products/salomon-spro-120-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000018-0000-0000-0000-000000000018'::uuid, 'ATO-HU130-2024', 'Atomic Hawx Ultra 130 S', '全般', 'Atomic', 'BOOT', '25.0-29.0cm', 'EXPERT', 3800, 'レーシング・パフォーマンスブーツ', '/images/products/atomic-hawx-ultra-130-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000019-0000-0000-0000-000000000019'::uuid, 'ROX-PP80-W-2024', 'Rossignol Pure Pro 80', 'レディース用', 'Rossignol', 'BOOT', '22.0-26.0cm', 'INTERMEDIATE', 3000, 'レディース用オールマウンテンブーツ', '/images/products/rossignol-pure-pro-80-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000020-0000-0000-0000-000000000020'::uuid, 'SAL-T2-JR-2024', 'Salomon Team T2', 'ジュニア用', 'Salomon', 'BOOT', '18.0-24.0cm', 'BEGINNER', 2000, 'ジュニア用スキーブーツ', '/images/products/salomon-team-t2-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- ヘルメット
    ('00000021-0000-0000-0000-000000000021'::uuid, 'POC-OBX-SPIN-2024', 'POC Obex SPIN', '全般', 'POC', 'HELMET', 'S-XL', 'BEGINNER', 1500, '高性能スキーヘルメット', '/images/products/poc-obex-spin-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000022-0000-0000-0000-000000000022'::uuid, 'SMI-VAN-MIPS-2024', 'Smith Vantage MIPS', '全般', 'Smith', 'HELMET', 'S-XL', 'BEGINNER', 1800, 'MIPS搭載スキーヘルメット', '/images/products/smith-vantage-mips-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000023-0000-0000-0000-000000000023'::uuid, 'GIR-LAU-JR-2024', 'Giro Launch Jr', 'ジュニア用', 'Giro', 'HELMET', 'XS-M', 'BEGINNER', 800, 'ジュニア用スキーヘルメット', '/images/products/giro-launch-jr-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- スキーウェア
    ('00000024-0000-0000-0000-000000000024'::uuid, 'PAT-PB-JAC-2024', 'Patagonia Powder Bowl Jacket', '全般', 'Patagonia', 'WEAR', 'S-XL', 'INTERMEDIATE', 2500, 'プレミアムスキージャケット', '/images/products/patagonia-powder-bowl-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- ゴーグル
    ('00000025-0000-0000-0000-000000000025'::uuid, 'OAK-FD-2024', 'Oakley Flight Deck', 'ゴーグル全般', 'Oakley', 'GOGGLE', 'ONE_SIZE', 'BEGINNER', 1000, 'プリズムレンズスキーゴーグル', '/images/products/oakley-flight-deck-1.jpg', TRUE, TRUE, NOW(), NOW()),
    ('00000026-0000-0000-0000-000000000026'::uuid, 'SMI-IO-MAG-2024', 'Smith I/O MAG XL', 'ゴーグル全般', 'Smith', 'GOGGLE', 'ONE_SIZE', 'BEGINNER', 1200, '交換レンズ付きゴーグル', '/images/products/smith-io-mag-xl-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- グローブ
    ('00000027-0000-0000-0000-000000000027'::uuid, 'HES-ALH-2024', 'Hestra Army Leather Heli Ski', '全般', 'Hestra', 'GLOVE', 'S-XL', 'INTERMEDIATE', 600, 'プレミアムスキーグローブ', '/images/products/hestra-army-leather-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- バッグ/スキーケース
    ('00000028-0000-0000-0000-000000000028'::uuid, 'THU-RTS-2024', 'Thule RoundTrip Ski Roller', 'バッグ/スキーケース', 'Thule', 'BAG', 'ONE_SIZE', 'BEGINNER', 1500, 'ローラー付きスキーバッグ', '/images/products/thule-roundtrip-ski-1.jpg', TRUE, TRUE, NOW(), NOW()),
    
    -- ワックス
    ('00000029-0000-0000-0000-000000000029'::uuid, 'SWI-CH7X-2024', 'Swix CH7X Yellow', 'ワックス', 'Swix', 'WAX', 'ONE_SIZE', 'BEGINNER', 200, '万能ハードワックス', '/images/products/swix-ch7x-yellow-1.jpg', FALSE, TRUE, NOW(), NOW()),
    
    -- チューンナップ用品
    ('00000030-0000-0000-0000-000000000030'::uuid, 'SWI-VISE-2024', 'Swix Economy Ski Vise', 'チューンナップ用品', 'Swix', 'TUNING', 'ONE_SIZE', 'INTERMEDIATE', 800, 'スキーバイス・チューンナップ台', '/images/products/swix-economy-vise-1.jpg', FALSE, TRUE, NOW(), NOW());

-- 在庫アイテムデータ（各設備に対して複数のアイテムを作成）
-- レーシングスキー（高級品は少なめ）
INSERT INTO inventory_items (equipment_id, serial_number, status, location, size, condition_rating, purchase_date, last_maintenance_date, next_maintenance_date, total_rental_count, created_at, updated_at) VALUES
    -- Rossignol Hero Athlete FIS GS (equipment_id: 1)
    (1, 'ROX-HA-GS-001', 'AVAILABLE', 'MAIN_STORE', '185cm', 5, '2024-01-15', '2024-01-15', '2024-04-15', 0, NOW(), NOW()),
    (1, 'ROX-HA-GS-002', 'AVAILABLE', 'MAIN_STORE', '185cm', 5, '2024-01-15', '2024-01-15', '2024-04-15', 0, NOW(), NOW()),
    (1, 'ROX-HA-GS-003', 'AVAILABLE', 'RENTAL_COUNTER', '180cm', 5, '2024-01-15', '2024-01-15', '2024-04-15', 0, NOW(), NOW()),
    (1, 'ROX-HA-GS-004', 'AVAILABLE', 'WAREHOUSE_A', '190cm', 5, '2024-01-15', '2024-01-15', '2024-04-15', 0, NOW(), NOW()),
    (1, 'ROX-HA-GS-005', 'AVAILABLE', 'WAREHOUSE_A', '190cm', 5, '2024-01-15', '2024-01-15', '2024-04-15', 0, NOW(), NOW()),
    
    -- Atomic Redster X9 WC GS (equipment_id: 2)
    (2, 'ATO-X9-GS-001', 'AVAILABLE', 'MAIN_STORE', '180cm', 5, '2024-01-20', '2024-01-20', '2024-04-20', 0, NOW(), NOW()),
    (2, 'ATO-X9-GS-002', 'AVAILABLE', 'MAIN_STORE', '180cm', 5, '2024-01-20', '2024-01-20', '2024-04-20', 0, NOW(), NOW()),
    (2, 'ATO-X9-GS-003', 'AVAILABLE', 'RENTAL_COUNTER', '175cm', 5, '2024-01-20', '2024-01-20', '2024-04-20', 0, NOW(), NOW()),
    (2, 'ATO-X9-GS-004', 'AVAILABLE', 'WAREHOUSE_A', '185cm', 5, '2024-01-20', '2024-01-20', '2024-04-20', 0, NOW(), NOW()),
    
    -- 初級・中級向けスキー（在庫多め）
    -- Salomon S/Max 8 (equipment_id: 9)
    (9, 'SAL-SMAX8-001', 'AVAILABLE', 'MAIN_STORE', '160cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-002', 'AVAILABLE', 'MAIN_STORE', '160cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-003', 'AVAILABLE', 'MAIN_STORE', '165cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-004', 'AVAILABLE', 'RENTAL_COUNTER', '160cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-005', 'AVAILABLE', 'RENTAL_COUNTER', '165cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-006', 'AVAILABLE', 'RENTAL_COUNTER', '150cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-007', 'AVAILABLE', 'WAREHOUSE_A', '170cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-008', 'AVAILABLE', 'WAREHOUSE_A', '165cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    (9, 'SAL-SMAX8-009', 'RENTED', 'RENTAL_COUNTER', '160cm', 4, '2024-01-10', '2024-01-10', '2024-04-10', 3, NOW(), NOW()),
    (9, 'SAL-SMAX8-010', 'AVAILABLE', 'WAREHOUSE_B', '150cm', 5, '2024-01-10', '2024-01-10', '2024-04-10', 0, NOW(), NOW()),
    
    -- Rossignol Experience 78 Ti (equipment_id: 10)
    (10, 'ROX-EX78-001', 'AVAILABLE', 'MAIN_STORE', '160cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-002', 'AVAILABLE', 'MAIN_STORE', '165cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-003', 'AVAILABLE', 'RENTAL_COUNTER', '160cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-004', 'AVAILABLE', 'RENTAL_COUNTER', '155cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-005', 'AVAILABLE', 'WAREHOUSE_A', '170cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-006', 'AVAILABLE', 'WAREHOUSE_A', '165cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    (10, 'ROX-EX78-007', 'RENTED', 'RENTAL_COUNTER', '160cm', 4, '2024-01-12', '2024-01-12', '2024-04-12', 2, NOW(), NOW()),
    (10, 'ROX-EX78-008', 'AVAILABLE', 'WAREHOUSE_B', '150cm', 5, '2024-01-12', '2024-01-12', '2024-04-12', 0, NOW(), NOW()),
    
    -- ジュニア向けスキー
    -- Rossignol Experience Pro Jr (equipment_id: 12)
    (12, 'ROX-EXP-JR-001', 'AVAILABLE', 'MAIN_STORE', '130cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 0, NOW(), NOW()),
    (12, 'ROX-EXP-JR-002', 'AVAILABLE', 'MAIN_STORE', '140cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 0, NOW(), NOW()),
    (12, 'ROX-EXP-JR-003', 'AVAILABLE', 'RENTAL_COUNTER', '130cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 0, NOW(), NOW()),
    (12, 'ROX-EXP-JR-004', 'AVAILABLE', 'RENTAL_COUNTER', '135cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 0, NOW(), NOW()),
    (12, 'ROX-EXP-JR-005', 'AVAILABLE', 'RENTAL_COUNTER', '120cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 0, NOW(), NOW()),
    (12, 'ROX-EXP-JR-006', 'RENTED', 'RENTAL_COUNTER', '140cm', 5, '2024-01-05', '2024-01-05', '2024-04-05', 1, NOW(), NOW()),
    
    -- スキーブーツ
    -- Salomon S/Pro 120 (equipment_id: 17)
    (17, 'SAL-SP120-001', 'AVAILABLE', 'MAIN_STORE', '27.0cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-002', 'AVAILABLE', 'MAIN_STORE', '27.0cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-003', 'AVAILABLE', 'RENTAL_COUNTER', '26.5cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-004', 'AVAILABLE', 'RENTAL_COUNTER', '27.5cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-005', 'AVAILABLE', 'WAREHOUSE_A', '26.0cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-006', 'AVAILABLE', 'WAREHOUSE_A', '28.0cm', 5, '2024-01-08', '2024-01-08', '2024-07-08', 0, NOW(), NOW()),
    (17, 'SAL-SP120-007', 'RENTED', 'RENTAL_COUNTER', '27.0cm', 4, '2024-01-08', '2024-01-08', '2024-07-08', 2, NOW(), NOW()),
    
    -- ヘルメット
    -- POC Obex SPIN (equipment_id: 21)
    (21, 'POC-OBX-001', 'AVAILABLE', 'MAIN_STORE', 'M', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-002', 'AVAILABLE', 'MAIN_STORE', 'L', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-003', 'AVAILABLE', 'RENTAL_COUNTER', 'M', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-004', 'AVAILABLE', 'RENTAL_COUNTER', 'S', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-005', 'AVAILABLE', 'RENTAL_COUNTER', 'L', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-006', 'AVAILABLE', 'WAREHOUSE_A', 'XL', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    (21, 'POC-OBX-007', 'RENTED', 'RENTAL_COUNTER', 'M', 5, '2024-01-03', NULL, NULL, 1, NOW(), NOW()),
    (21, 'POC-OBX-008', 'AVAILABLE', 'WAREHOUSE_B', 'S', 5, '2024-01-03', NULL, NULL, 0, NOW(), NOW()),
    
    -- ゴーグル
    -- Oakley Flight Deck (equipment_id: 25)
    (25, 'OAK-FD-001', 'AVAILABLE', 'MAIN_STORE', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 0, NOW(), NOW()),
    (25, 'OAK-FD-002', 'AVAILABLE', 'MAIN_STORE', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 0, NOW(), NOW()),
    (25, 'OAK-FD-003', 'AVAILABLE', 'RENTAL_COUNTER', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 0, NOW(), NOW()),
    (25, 'OAK-FD-004', 'AVAILABLE', 'RENTAL_COUNTER', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 0, NOW(), NOW()),
    (25, 'OAK-FD-005', 'AVAILABLE', 'WAREHOUSE_A', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 0, NOW(), NOW()),
    (25, 'OAK-FD-006', 'RENTED', 'RENTAL_COUNTER', 'ONE_SIZE', 5, '2024-01-06', NULL, NULL, 1, NOW(), NOW()),
    
    -- ストック
    -- Leki World Cup Racing GS (equipment_id: 14)
    (14, 'LEK-WC-GS-001', 'AVAILABLE', 'MAIN_STORE', '125cm', 5, '2024-01-07', '2024-01-07', '2024-07-07', 0, NOW(), NOW()),
    (14, 'LEK-WC-GS-002', 'AVAILABLE', 'MAIN_STORE', '125cm', 5, '2024-01-07', '2024-01-07', '2024-07-07', 0, NOW(), NOW()),
    (14, 'LEK-WC-GS-003', 'AVAILABLE', 'RENTAL_COUNTER', '120cm', 5, '2024-01-07', '2024-01-07', '2024-07-07', 0, NOW(), NOW()),
    (14, 'LEK-WC-GS-004', 'AVAILABLE', 'RENTAL_COUNTER', '130cm', 5, '2024-01-07', '2024-01-07', '2024-07-07', 0, NOW(), NOW()),
    (14, 'LEK-WC-GS-005', 'AVAILABLE', 'WAREHOUSE_A', '125cm', 5, '2024-01-07', '2024-01-07', '2024-07-07', 0, NOW(), NOW()),
    
    -- グローブ
    -- Hestra Army Leather Heli Ski (equipment_id: 27)
    (27, 'HES-ALH-001', 'AVAILABLE', 'MAIN_STORE', 'M', 5, '2024-01-04', NULL, NULL, 0, NOW(), NOW()),
    (27, 'HES-ALH-002', 'AVAILABLE', 'MAIN_STORE', 'L', 5, '2024-01-04', NULL, NULL, 0, NOW(), NOW()),
    (27, 'HES-ALH-003', 'AVAILABLE', 'RENTAL_COUNTER', 'M', 5, '2024-01-04', NULL, NULL, 0, NOW(), NOW()),
    (27, 'HES-ALH-004', 'AVAILABLE', 'RENTAL_COUNTER', 'S', 5, '2024-01-04', NULL, NULL, 0, NOW(), NOW()),
    (27, 'HES-ALH-005', 'AVAILABLE', 'WAREHOUSE_A', 'XL', 5, '2024-01-04', NULL, NULL, 0, NOW(), NOW()),
    (27, 'HES-ALH-006', 'RENTED', 'RENTAL_COUNTER', 'L', 4, '2024-01-04', NULL, NULL, 2, NOW(), NOW());

-- 予約データ（現在アクティブな予約とサンプル予約）
INSERT INTO reservations (customer_id, equipment_id, inventory_item_id, start_date, end_date, status, total_amount, deposit_amount, size_requested, special_requests, created_at, updated_at, confirmed_at) VALUES
    (101, 9, 13, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 8000, 2000, '160cm', 'なし', '2024-01-14 10:00:00', '2024-01-15 08:30:00', '2024-01-14 15:00:00'),
    (102, 10, 19, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 7600, 2000, '160cm', 'なし', '2024-01-14 11:00:00', '2024-01-15 08:30:00', '2024-01-14 16:00:00'),
    (103, 12, 24, '2024-01-15 09:00:00', '2024-01-16 17:00:00', 'ACTIVE', 2500, 1000, '140cm', 'ジュニア用', '2024-01-14 14:00:00', '2024-01-15 08:30:00', '2024-01-14 18:00:00'),
    (104, 17, 31, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 7000, 2000, '27.0cm', 'なし', '2024-01-14 12:00:00', '2024-01-15 08:30:00', '2024-01-14 17:00:00'),
    (105, 21, 39, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 3000, 1000, 'M', 'なし', '2024-01-14 13:00:00', '2024-01-15 08:30:00', '2024-01-14 18:30:00'),
    (106, 25, 44, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 2000, 500, 'ONE_SIZE', 'なし', '2024-01-14 16:00:00', '2024-01-15 08:30:00', '2024-01-14 20:00:00'),
    (107, 27, 52, '2024-01-15 09:00:00', '2024-01-17 17:00:00', 'ACTIVE', 1200, 300, 'L', 'なし', '2024-01-14 17:00:00', '2024-01-15 08:30:00', '2024-01-14 21:00:00'),
    
    -- 今後の予約
    (201, 1, NULL, '2024-01-18 09:00:00', '2024-01-20 17:00:00', 'CONFIRMED', 16000, 4000, '185cm', 'レーシング用', '2024-01-16 10:00:00', '2024-01-16 15:00:00', '2024-01-16 15:00:00'),
    (202, 9, NULL, '2024-01-19 09:00:00', '2024-01-21 17:00:00', 'CONFIRMED', 8000, 2000, '165cm', 'なし', '2024-01-17 11:00:00', '2024-01-17 16:00:00', '2024-01-17 16:00:00'),
    (203, 12, NULL, '2024-01-20 09:00:00', '2024-01-22 17:00:00', 'PENDING', 5000, 1500, '130cm', 'ジュニア向け', '2024-01-18 14:00:00', '2024-01-18 14:00:00', NULL);

-- 在庫移動履歴データ
INSERT INTO inventory_movements (inventory_item_id, movement_type, from_location, to_location, from_status, to_status, customer_id, reservation_id, notes, movement_date, processed_by) VALUES
    (13, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 101, 1, '顧客への貸出', '2024-01-15 09:00:00', 'staff001'),
    (19, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 102, 2, '顧客への貸出', '2024-01-15 09:15:00', 'staff001'),
    (24, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 103, 3, 'ジュニア向け貸出', '2024-01-15 09:30:00', 'staff002'),
    (31, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 104, 4, '顧客への貸出', '2024-01-15 09:45:00', 'staff001'),
    (39, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 105, 5, 'ヘルメット貸出', '2024-01-15 10:00:00', 'staff002'),
    (44, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 106, 6, 'ゴーグル貸出', '2024-01-15 10:15:00', 'staff001'),
    (52, 'RENTAL', 'RENTAL_COUNTER', 'RENTAL_COUNTER', 'AVAILABLE', 'RENTED', 107, 7, 'グローブ貸出', '2024-01-15 10:30:00', 'staff002'),
    
    -- 倉庫移動履歴
    (1, 'TRANSFER', 'WAREHOUSE_A', 'MAIN_STORE', 'AVAILABLE', 'AVAILABLE', NULL, NULL, '店舗への移動', '2024-01-14 14:00:00', 'staff003'),
    (2, 'TRANSFER', 'WAREHOUSE_A', 'MAIN_STORE', 'AVAILABLE', 'AVAILABLE', NULL, NULL, '店舗への移動', '2024-01-14 14:15:00', 'staff003'),
    (25, 'TRANSFER', 'WAREHOUSE_A', 'RENTAL_COUNTER', 'AVAILABLE', 'AVAILABLE', NULL, NULL, 'レンタルカウンターへの移動', '2024-01-13 16:00:00', 'staff004');

-- メンテナンス記録データ
INSERT INTO maintenance_records (inventory_item_id, maintenance_type, description, maintenance_date, performed_by, cost, parts_replaced, condition_before, condition_after, next_maintenance_date, is_completed, created_at) VALUES
    (1, 'ROUTINE', 'シーズン前点検・ワックス塗布', '2024-01-15', 'maint001', 1500, 'エッジチューン', 5, 5, '2024-04-15', TRUE, NOW()),
    (2, 'ROUTINE', 'シーズン前点検・ワックス塗布', '2024-01-15', 'maint001', 1500, 'エッジチューン', 5, 5, '2024-04-15', TRUE, NOW()),
    (13, 'ROUTINE', '定期メンテナンス', '2024-01-10', 'maint002', 800, 'なし', 5, 4, '2024-04-10', TRUE, NOW()),
    (19, 'ROUTINE', '定期メンテナンス', '2024-01-12', 'maint001', 800, 'なし', 5, 4, '2024-04-12', TRUE, NOW()),
    (31, 'ROUTINE', 'ブーツクリーニング・調整', '2024-01-08', 'maint003', 500, 'インソール交換', 5, 4, '2024-07-08', TRUE, NOW()),
    (39, 'INSPECTION', 'ヘルメット安全点検', '2024-01-03', 'maint002', 200, 'なし', 5, 5, NULL, TRUE, NOW()),
    (52, 'ROUTINE', 'グローブクリーニング', '2024-01-04', 'maint003', 300, 'なし', 5, 4, NULL, TRUE, NOW());

-- 在庫アラートデータ
INSERT INTO inventory_alerts (equipment_id, location, alert_type, severity, message, current_count, threshold_count, is_resolved, created_at) VALUES
    (1, 'MAIN_STORE', 'LOW_STOCK', 'MEDIUM', 'Rossignol Hero Athlete FIS GS の在庫が少なくなっています', 2, 5, FALSE, NOW()),
    (2, 'RENTAL_COUNTER', 'LOW_STOCK', 'MEDIUM', 'Atomic Redster X9 WC GS の在庫が少なくなっています', 1, 3, FALSE, NOW()),
    (12, 'RENTAL_COUNTER', 'HIGH_DEMAND', 'HIGH', 'ジュニア向けスキーの需要が高まっています', 5, 8, FALSE, NOW()),
    (17, 'RENTAL_COUNTER', 'MAINTENANCE_DUE', 'MEDIUM', 'スキーブーツのメンテナンス時期です', 6, 0, FALSE, '2024-01-14 10:00:00'),
    (21, 'WAREHOUSE_A', 'LOW_STOCK', 'LOW', 'ヘルメットXLサイズの在庫が少なくなっています', 1, 3, FALSE, '2024-01-13 15:00:00');
