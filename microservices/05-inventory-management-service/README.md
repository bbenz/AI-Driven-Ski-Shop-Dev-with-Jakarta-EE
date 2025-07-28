# Inventory Management Service

在庫管理サービス（Inventory Management Service）は、スキーリゾート管理システムの設備在庫管理を担当するマイクロサービスです。

## 概要

このサービスは以下の在庫管理機能を提供します：

- **設備在庫管理**: スキー、ブーツ、ポール、ヘルメットなどの在庫追跡
- **予約管理**: レンタル予約、在庫引当、可用性チェック
- **メンテナンス管理**: 設備の定期点検、修理、交換スケジュール
- **場所管理**: 複数店舗、倉庫間の在庫移動
- **サイズ管理**: サイズ別在庫、適合性チェック
- **リアルタイム在庫**: 即座の在庫状況更新

## 技術スタック

- **Jakarta EE 11**: エンタープライズJavaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **WildFly 31.0.1**: アプリケーションサーバー
- **PostgreSQL**: メインデータベース
- **Redis**: 在庫キャッシュ・リアルタイム更新
- **Apache Kafka**: イベントストリーミング
- **MicroProfile Config**: 設定管理
- **MicroProfile Health**: ヘルスチェック

## アーキテクチャ

```text
┌─────────────────────────────────────────────────────────┐
│              Inventory Management Service                │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ InventoryResource                                   │
│  ├─ ReservationResource                                 │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ InventoryService                                    │
│  ├─ ReservationService                                  │
│  └─ MaintenanceService                                  │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ EquipmentRepository                                 │
│  ├─ InventoryItemRepository                             │
│  └─ ReservationRepository                               │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ Equipment                                           │
│  ├─ InventoryItem                                       │
│  └─ Reservation                                         │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計

### Equipment (設備マスタ)

- 設備ID、設備名、カテゴリ、ブランド
- 設備仕様（サイズ、重量、適用レベル）
- メンテナンス情報（点検間隔、交換時期）
- 画像URL、説明

### InventoryItem (在庫アイテム)

- 在庫ID、設備ID、シリアル番号
- 状態（利用可能、レンタル中、メンテナンス中、廃棄）
- 場所（店舗、倉庫、エリア）
- サイズ、コンディション

### Reservation (予約情報)

- 予約ID、顧客ID、設備ID
- 予約期間（開始日時、終了日時）
- ステータス（予約中、確定、キャンセル、完了）
- 引当在庫ID

## API エンドポイント

### 在庫管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/inventory/equipment` | 設備一覧取得 |
| GET | `/inventory/equipment/{equipmentId}` | 設備詳細取得 |
| GET | `/inventory/equipment/{equipmentId}/availability` | 在庫状況取得 |
| PUT | `/inventory/items/{itemId}/status` | 在庫ステータス更新 |
| POST | `/inventory/items/{itemId}/maintenance` | メンテナンス記録 |

### 予約管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/inventory/reservations` | 予約作成 |
| GET | `/inventory/reservations/{reservationId}` | 予約詳細取得 |
| PUT | `/inventory/reservations/{reservationId}/confirm` | 予約確定 |
| DELETE | `/inventory/reservations/{reservationId}` | 予約キャンセル |

### 可用性チェックAPI

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/inventory/check-availability` | 複数アイテムの可用性チェック |
| GET | `/inventory/equipment/{equipmentId}/available-dates` | 利用可能日取得 |

## セキュリティ機能

### アクセス制御

- ロールベースアクセス制御（スタッフ、管理者）
- 在庫操作権限の細分化
- 店舗別アクセス制限

### データ整合性

- 楽観的ロックによる同時更新制御
- トランザクション管理
- 在庫数整合性チェック

### 監査ログ

- すべての在庫変更の記録
- 予約操作の履歴
- メンテナンス記録

## 設定

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `DATABASE_URL` | データベース接続URL | `jdbc:postgresql://localhost:5432/skiresortdb` |
| `DATABASE_USER` | データベースユーザー | `skiresort` |
| `DATABASE_PASSWORD` | データベースパスワード | `skiresort` |
| `REDIS_HOST` | Redisホスト | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafkaサーバー | `localhost:9092` |
| `INVENTORY_CACHE_TTL` | 在庫キャッシュTTL | `300` |
| `RESERVATION_TIMEOUT` | 予約タイムアウト | `PT30M` |

## データベース設定

### PostgreSQL設定

```sql
-- 設備マスタテーブル（商品カタログサービスと連携）
CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL, -- 商品カタログサービスとの連携
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    category VARCHAR(100) NOT NULL,
    brand VARCHAR(100) NOT NULL,
    equipment_type VARCHAR(50) NOT NULL, -- SKI_BOARD, BOOT, HELMET, etc.
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
```

## イベント統合

### パブリッシュされるイベント

- `InventoryUpdatedEvent` - 在庫状況変更時
- `ReservationCreatedEvent` - 予約作成時
- `ReservationConfirmedEvent` - 予約確定時
- `EquipmentMaintenanceEvent` - メンテナンス実施時

### 消費されるイベント

- 注文管理サービスからの予約要求
- ユーザー管理サービスからの顧客情報更新
- 決済サービスからの支払い完了通知

## ビルドと実行

### 前提条件

- Java 21 LTS
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- Apache Kafka 3.0+
- WildFly 31.0.1

### ビルド

```bash
# Maven ビルド
mvn clean compile

# テスト実行
mvn test

# パッケージ作成
mvn package
```

### デプロイ

```bash
# WildFlyにデプロイ
cp target/inventory-management-service.war $WILDFLY_HOME/standalone/deployments/
```

### Docker実行

```bash
# Docker Compose で実行
docker-compose up inventory-management-service
```

## API使用例

### 在庫状況確認

```bash
curl -X GET "http://localhost:8084/inventory/equipment/ski-rossignol-x1/availability?startDate=2024-01-15&endDate=2024-01-17" \
  -H "Authorization: Bearer your_jwt_token"
```

### 予約作成

```bash
curl -X POST http://localhost:8084/inventory/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "customerId": 123,
    "equipmentId": "ski-rossignol-x1",
    "startDate": "2024-01-15T09:00:00Z",
    "endDate": "2024-01-17T17:00:00Z",
    "size": "170cm"
  }'
```

### 在庫ステータス更新

```bash
curl -X PUT http://localhost:8084/inventory/items/item-12345/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "status": "MAINTENANCE",
    "reason": "定期点検",
    "expectedAvailableDate": "2024-01-20"
  }'
```

## 監視とロギング

### ヘルスチェック

- `/health` エンドポイントでサービス状態確認
- データベース接続状態
- Redis・Kafka接続状態
- 外部サービス接続状態

### メトリクス

- 在庫回転率
- 予約成功率
- メンテナンス頻度
- レスポンス時間

### ログ

- 在庫変更ログ
- 予約操作ログ
- メンテナンス記録
- エラーログ

## セキュリティ考慮事項

### 本番環境での推奨設定

1. **在庫データの保護**
   - データベース暗号化
   - アクセス権限の最小化

2. **同時アクセス制御**
   - 楽観的ロック
   - 分散ロック（Redis）

3. **監査証跡**
   - 全操作の記録
   - 変更履歴の保持

## トラブルシューティング

### よくある問題

1. **在庫不整合**
   - キャッシュとDBの同期確認
   - トランザクション境界の確認

2. **予約競合**
   - ロック機構の確認
   - リトライ設定の調整

3. **パフォーマンス問題**
   - キャッシュヒット率の確認
   - クエリ最適化

## 作成済みデータ概要

### 設備データ（30種類）

商品カタログサービスと連携して、以下の設備が管理されています：

#### スキー板（13種類）
- **レーシングGS**: Rossignol Hero Athlete FIS GS、Atomic Redster X9 WC GS
- **レーシングSL**: Atomic Redster G9 FIS SL、Salomon S/Race Rush SL
- **パウダー**: K2 Mindbender 108Ti、Volkl Mantra M6、Nordica Enforcer 110 Free
- **モーグル**: K2 Mogul Ski
- **初級・中級向け**: Salomon S/Max 8、Rossignol Experience 78 Ti、Atomic Vantage 75 C
- **ジュニア**: Rossignol Experience Pro Jr、Salomon QST Max Jr

#### その他設備（17種類）
- **ストック**: Leki World Cup Racing GS、Atomic AMT SL、Black Diamond Traverse Pro
- **スキーブーツ**: Salomon S/Pro 120、Atomic Hawx Ultra 130 S、Rossignol Pure Pro 80、Salomon Team T2
- **ヘルメット**: POC Obex SPIN、Smith Vantage MIPS、Giro Launch Jr
- **スキーウェア**: Patagonia Powder Bowl Jacket
- **ゴーグル**: Oakley Flight Deck、Smith I/O MAG XL
- **グローブ**: Hestra Army Leather Heli Ski
- **バッグ**: Thule RoundTrip Ski Roller
- **ワックス**: Swix CH7X Yellow
- **チューンナップ**: Swix Economy Ski Vise

### 在庫データ（52アイテム）

各設備に対して複数の在庫アイテムが作成されており、以下の状況を含みます：

- **利用可能**: 45アイテム（87%）
- **レンタル中**: 7アイテム（13%）
- **メンテナンス中**: 0アイテム
- **廃棄済み**: 0アイテム

### 店舗・倉庫（5箇所）

- **メインストア**: 白馬村のメイン店舗
- **レンタルカウンター**: レンタル専用カウンター
- **メイン倉庫A**: 主要在庫保管場所
- **サブ倉庫B**: 補助在庫保管場所
- **メンテナンス工房**: 修理・点検専用施設

### 現在のレンタル状況

7件のアクティブなレンタルが進行中：
- 中級者向けスキー板（2件）
- ジュニア向けスキー板（1件）
- スキーブーツ（1件）
- ヘルメット（1件）
- ゴーグル（1件）
- グローブ（1件）

### 在庫アラート（5件）

- レーシングスキーの在庫不足（2件）
- ジュニア向けスキーの高需要（1件）
- スキーブーツのメンテナンス時期（1件）
- ヘルメットXLサイズの在庫不足（1件）

### データ連携

- **商品カタログサービス**: `product_id`により30商品と連携
- **ユーザー管理サービス**: `customer_id`により顧客と連携
- **注文管理サービス**: 予約・注文との連携
- **決済サービス**: レンタル料金・デポジットとの連携

## 開発者向け情報

### コード構成

```text
src/main/java/
├── com/skiresort/inventory/
│   ├── entity/          # JPA エンティティ
│   ├── service/         # ビジネスロジック
│   ├── repository/      # データアクセス
│   ├── resource/        # REST エンドポイント
│   ├── event/           # イベント処理
│   └── exception/       # 例外クラス
```

### 依存関係

- Jakarta EE 11 API
- MicroProfile Config
- MicroProfile Health
- Apache Kafka Client
- PostgreSQL JDBC
- Redis Client

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。
