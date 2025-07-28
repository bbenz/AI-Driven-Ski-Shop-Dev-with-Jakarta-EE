#!/bin/bash

# 在庫管理サービス データベース初期化スクリプト
# run_inventory_setup.sh

echo "=========================================="
echo "在庫管理サービス データベース初期化開始"
echo "=========================================="

# PostgreSQL接続情報
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-skiresortdb}
DB_USER=${DB_USER:-skiresort}

echo "データベース接続情報:"
echo "  ホスト: $DB_HOST:$DB_PORT"
echo "  データベース: $DB_NAME"
echo "  ユーザー: $DB_USER"
echo ""

# マイグレーションファイルの実行
echo "1. 在庫管理テーブル作成..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f src/main/resources/db/migration/V1.0.0__Create_inventory_tables.sql

if [ $? -eq 0 ]; then
    echo "✅ テーブル作成完了"
else
    echo "❌ テーブル作成失敗"
    exit 1
fi

echo ""
echo "2. 初期データ投入..."
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f src/main/resources/db/migration/V1.0.1__Insert_inventory_initial_data.sql

if [ $? -eq 0 ]; then
    echo "✅ 初期データ投入完了"
else
    echo "❌ 初期データ投入失敗"
    exit 1
fi

echo ""
echo "3. データ確認..."

# 作成されたテーブル数を確認
TABLE_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
SELECT COUNT(*) 
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'equipment', 'inventory_items', 'reservations', 
    'inventory_movements', 'maintenance_records', 
    'inventory_summary', 'locations', 'inventory_alerts'
);")

echo "作成されたテーブル数: $TABLE_COUNT/8"

# 設備データ数を確認
EQUIPMENT_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM equipment;")
echo "設備データ数: $EQUIPMENT_COUNT件"

# 在庫アイテム数を確認
INVENTORY_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM inventory_items;")
echo "在庫アイテム数: $INVENTORY_COUNT件"

# 現在のレンタル数を確認
RENTAL_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM inventory_items WHERE status = 'RENTED';")
echo "現在のレンタル数: $RENTAL_COUNT件"

# 店舗・倉庫数を確認
LOCATION_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM locations;")
echo "店舗・倉庫数: $LOCATION_COUNT箇所"

# アクティブなアラート数を確認
ALERT_COUNT=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM inventory_alerts WHERE is_resolved = FALSE;")
echo "アクティブなアラート数: $ALERT_COUNT件"

echo ""
echo "4. 在庫統計更新..."

# 在庫統計の強制更新
psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
INSERT INTO inventory_summary (equipment_id, location, total_count, available_count, rented_count, maintenance_count, retired_count, last_updated)
SELECT 
    equipment_id,
    location,
    COUNT(*) as total_count,
    COUNT(*) FILTER (WHERE status = 'AVAILABLE') as available_count,
    COUNT(*) FILTER (WHERE status = 'RENTED') as rented_count,
    COUNT(*) FILTER (WHERE status = 'MAINTENANCE') as maintenance_count,
    COUNT(*) FILTER (WHERE status = 'RETIRED') as retired_count,
    CURRENT_TIMESTAMP
FROM inventory_items 
GROUP BY equipment_id, location
ON CONFLICT (equipment_id, location) 
DO UPDATE SET 
    total_count = EXCLUDED.total_count,
    available_count = EXCLUDED.available_count,
    rented_count = EXCLUDED.rented_count,
    maintenance_count = EXCLUDED.maintenance_count,
    retired_count = EXCLUDED.retired_count,
    last_updated = EXCLUDED.last_updated;
"

echo "✅ 在庫統計更新完了"

echo ""
echo "=========================================="
echo "在庫管理サービス データベース初期化完了"
echo "=========================================="
echo ""
echo "📊 データ概要:"
echo "  • 30種類の設備（スキー板、ブーツ、ヘルメット等）"
echo "  • 52個の在庫アイテム"
echo "  • 5箇所の店舗・倉庫"
echo "  • 7件のアクティブなレンタル"
echo "  • 10件の予約データ"
echo "  • 5件の在庫アラート"
echo ""
echo "🔧 API テスト例:"
echo "  curl -X GET http://localhost:8084/inventory/equipment"
echo "  curl -X GET http://localhost:8084/inventory/equipment/1/availability"
echo "  curl -X GET http://localhost:8084/inventory/alerts"
echo ""
