-- 在庫管理サービス初期テーブル作成
-- V1.0.0__Create_inventory_tables.sql

-- PostgreSQL UUID拡張を有効化
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 設備マスタテーブル（商品カタログサービスと連携）
CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL, -- 商品カタログサービスの products.id と連携
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    equipment_type VARCHAR(50) NOT NULL, -- SKI_BOARD, BINDING, POLE, BOOT, HELMET, etc.
    size_range VARCHAR(50),
    difficulty_level VARCHAR(20),
    daily_rate DECIMAL(10,2) NOT NULL,
    description TEXT,
    image_url VARCHAR(500),
    is_rental_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_equipment_product_id ON equipment(product_id);
CREATE INDEX idx_equipment_sku ON equipment(sku);
CREATE INDEX idx_equipment_category ON equipment(category);
CREATE INDEX idx_equipment_brand ON equipment(brand);
CREATE INDEX idx_equipment_type ON equipment(equipment_type);
CREATE INDEX idx_equipment_rental_available ON equipment(is_rental_available, is_active);

-- 在庫アイテムテーブル
CREATE TABLE inventory_items (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    serial_number VARCHAR(100) UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, RENTED, MAINTENANCE, RETIRED
    location VARCHAR(100) NOT NULL DEFAULT 'MAIN_STORE',
    size VARCHAR(20),
    condition_rating INTEGER DEFAULT 5 CHECK (condition_rating >= 1 AND condition_rating <= 5),
    purchase_date DATE,
    last_maintenance_date DATE,
    next_maintenance_date DATE,
    total_rental_count INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_items_equipment ON inventory_items(equipment_id);
CREATE INDEX idx_inventory_items_status ON inventory_items(status);
CREATE INDEX idx_inventory_items_location ON inventory_items(location);
CREATE INDEX idx_inventory_items_size ON inventory_items(size);
CREATE INDEX idx_inventory_items_condition ON inventory_items(condition_rating);
CREATE INDEX idx_inventory_items_serial ON inventory_items(serial_number);

-- 予約テーブル
CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    inventory_item_id BIGINT REFERENCES inventory_items(id),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED
    total_amount DECIMAL(10,2),
    deposit_amount DECIMAL(10,2),
    size_requested VARCHAR(20),
    special_requests TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    confirmed_at TIMESTAMP,
    completed_at TIMESTAMP,
    cancelled_at TIMESTAMP
);

CREATE INDEX idx_reservations_customer ON reservations(customer_id);
CREATE INDEX idx_reservations_equipment ON reservations(equipment_id);
CREATE INDEX idx_reservations_item ON reservations(inventory_item_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_dates ON reservations(start_date, end_date);
CREATE INDEX idx_reservations_created_at ON reservations(created_at);

-- 在庫移動履歴テーブル
CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id),
    movement_type VARCHAR(20) NOT NULL, -- RENTAL, RETURN, MAINTENANCE, TRANSFER, PURCHASE, RETIRE
    from_location VARCHAR(100),
    to_location VARCHAR(100),
    from_status VARCHAR(20),
    to_status VARCHAR(20),
    customer_id BIGINT,
    reservation_id BIGINT REFERENCES reservations(id),
    notes TEXT,
    movement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_by VARCHAR(100)
);

CREATE INDEX idx_inventory_movements_item ON inventory_movements(inventory_item_id);
CREATE INDEX idx_inventory_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_inventory_movements_date ON inventory_movements(movement_date);
CREATE INDEX idx_inventory_movements_customer ON inventory_movements(customer_id);
CREATE INDEX idx_inventory_movements_reservation ON inventory_movements(reservation_id);

-- メンテナンス記録テーブル
CREATE TABLE maintenance_records (
    id BIGSERIAL PRIMARY KEY,
    inventory_item_id BIGINT NOT NULL REFERENCES inventory_items(id),
    maintenance_type VARCHAR(50) NOT NULL, -- ROUTINE, REPAIR, REPLACEMENT, INSPECTION
    description TEXT NOT NULL,
    maintenance_date DATE NOT NULL,
    performed_by VARCHAR(100),
    cost DECIMAL(10,2),
    parts_replaced TEXT,
    condition_before INTEGER CHECK (condition_before >= 1 AND condition_before <= 5),
    condition_after INTEGER CHECK (condition_after >= 1 AND condition_after <= 5),
    next_maintenance_date DATE,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_maintenance_records_item ON maintenance_records(inventory_item_id);
CREATE INDEX idx_maintenance_records_type ON maintenance_records(maintenance_type);
CREATE INDEX idx_maintenance_records_date ON maintenance_records(maintenance_date);
CREATE INDEX idx_maintenance_records_completed ON maintenance_records(is_completed);

-- 在庫統計テーブル（サマリー情報）
CREATE TABLE inventory_summary (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    location VARCHAR(100) NOT NULL,
    total_count INTEGER NOT NULL DEFAULT 0,
    available_count INTEGER NOT NULL DEFAULT 0,
    rented_count INTEGER NOT NULL DEFAULT 0,
    maintenance_count INTEGER NOT NULL DEFAULT 0,
    retired_count INTEGER NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(equipment_id, location)
);

CREATE INDEX idx_inventory_summary_equipment ON inventory_summary(equipment_id);
CREATE INDEX idx_inventory_summary_location ON inventory_summary(location);
CREATE INDEX idx_inventory_summary_updated ON inventory_summary(last_updated);

-- 店舗・倉庫マスタテーブル
CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    location_type VARCHAR(20) NOT NULL, -- STORE, WAREHOUSE, REPAIR_SHOP
    address TEXT,
    phone VARCHAR(20),
    manager_name VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_locations_code ON locations(code);
CREATE INDEX idx_locations_type ON locations(location_type);
CREATE INDEX idx_locations_active ON locations(is_active);

-- 在庫アラートテーブル
CREATE TABLE inventory_alerts (
    id BIGSERIAL PRIMARY KEY,
    equipment_id BIGINT NOT NULL REFERENCES equipment(id),
    location VARCHAR(100) NOT NULL,
    alert_type VARCHAR(20) NOT NULL, -- LOW_STOCK, NO_STOCK, MAINTENANCE_DUE, HIGH_DEMAND
    severity VARCHAR(10) NOT NULL, -- LOW, MEDIUM, HIGH, CRITICAL
    message TEXT NOT NULL,
    current_count INTEGER,
    threshold_count INTEGER,
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_inventory_alerts_equipment ON inventory_alerts(equipment_id);
CREATE INDEX idx_inventory_alerts_type ON inventory_alerts(alert_type);
CREATE INDEX idx_inventory_alerts_severity ON inventory_alerts(severity);
CREATE INDEX idx_inventory_alerts_resolved ON inventory_alerts(is_resolved);
CREATE INDEX idx_inventory_alerts_created ON inventory_alerts(created_at);

-- トリガー関数：在庫統計の自動更新
CREATE OR REPLACE FUNCTION update_inventory_summary()
RETURNS TRIGGER AS $$
BEGIN
    -- 在庫統計を更新
    INSERT INTO inventory_summary (equipment_id, location, total_count, available_count, rented_count, maintenance_count, retired_count, last_updated)
    SELECT 
        NEW.equipment_id,
        NEW.location,
        COUNT(*) as total_count,
        COUNT(*) FILTER (WHERE status = 'AVAILABLE') as available_count,
        COUNT(*) FILTER (WHERE status = 'RENTED') as rented_count,
        COUNT(*) FILTER (WHERE status = 'MAINTENANCE') as maintenance_count,
        COUNT(*) FILTER (WHERE status = 'RETIRED') as retired_count,
        CURRENT_TIMESTAMP
    FROM inventory_items 
    WHERE equipment_id = NEW.equipment_id AND location = NEW.location
    GROUP BY equipment_id, location
    ON CONFLICT (equipment_id, location) 
    DO UPDATE SET 
        total_count = EXCLUDED.total_count,
        available_count = EXCLUDED.available_count,
        rented_count = EXCLUDED.rented_count,
        maintenance_count = EXCLUDED.maintenance_count,
        retired_count = EXCLUDED.retired_count,
        last_updated = EXCLUDED.last_updated;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- トリガー：在庫アイテムの変更時に統計を更新
CREATE TRIGGER trigger_update_inventory_summary
    AFTER INSERT OR UPDATE OR DELETE ON inventory_items
    FOR EACH ROW EXECUTE FUNCTION update_inventory_summary();

-- 在庫更新時のタイムスタンプ自動更新関数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- タイムスタンプ自動更新トリガー
CREATE TRIGGER trigger_equipment_updated_at 
    BEFORE UPDATE ON equipment 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_inventory_items_updated_at 
    BEFORE UPDATE ON inventory_items 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_reservations_updated_at 
    BEFORE UPDATE ON reservations 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
