# Inventory Management Service

在庫管理サービス（Inventory Management Service）は、スキーショップの販売・レンタル商品在庫管理を担当するマイクロサービスです。**Event-Driven Architecture**を採用し、Product Catalog Serviceとの疎結合な連携を実現しています。

## 概要

このサービスは以下の在庫管理機能を提供します：

- **在庫レベル管理**: スキー板、ブーツ、ストック、ヘルメット、ウェアなどの在庫数管理
- **レンタル料金管理**: 商品タイプ別のレンタル料金計算とビジネスロジック
- **在庫予約管理**: レンタル予約、在庫引当、可用性チェック
- **在庫移動管理**: 複数店舗、倉庫間の在庫移動履歴
- **Product Catalog連携**: イベント駆動による商品情報の自動同期
- **リアルタイム在庫**: Kafkaベースの即座の在庫状況更新

## 技術スタック

- **Quarkus 3.8**: 高性能Javaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **Jakarta EE 11**: エンタープライズJavaAPI
- **PostgreSQL**: メインデータベース
- **Redis**: 在庫キャッシュ・リアルタイム更新
- **Apache Kafka**: イベントストリーミング
- **MicroProfile**: 設定管理・ヘルスチェック・メトリクス
- **Docker**: コンテナ化
- **Docker Compose**: ローカル開発環境

## Event-Driven Architecture

このサービスは、データ重複問題を解決するためEvent-Driven Architectureを採用しています：

```text
┌─────────────────────┐     Events     ┌─────────────────────┐
│ Product Catalog     │─────────────→│ Apache Kafka        │
│ Service             │               │ (Message Broker)    │
│ (Source of Truth)   │               │                     │
└─────────────────────┘               └─────────────────────┘
                                                │
                                                │ Subscribe
                                                ▼
                                    ┌─────────────────────┐
                                    │ Inventory Mgmt      │
                                    │ Service             │
                                    │ (Event Consumer)    │
                                    └─────────────────────┘
```

### データ責任分離

| サービス | 責任 | 保持データ |
|---------|------|-----------|
| **Product Catalog** | 商品マスタ管理 | ・商品情報（名前、SKU、価格）<br>・カテゴリ・ブランド情報<br>・商品仕様・画像 |
| **Inventory Management** | 在庫管理専門 | ・在庫数・場所・状態<br>・レンタル料金（ビジネスロジック）<br>・予約・移動履歴 |

### イベント処理

- **Product Created**: 新商品の在庫エントリ自動作成
- **Product Updated**: 商品情報のキャッシュ更新
- **Product Deleted**: 該当在庫の無効化

## アーキテクチャ

```text
┌─────────────────────────────────────────────────────────┐
│              Inventory Management Service                │
├─────────────────────────────────────────────────────────┤
│  Event Layer (Kafka Consumer)                          │
│  ├─ ProductEventConsumer                               │
│  └─ EventOrderingService                               │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ InventoryResource                                   │
│  ├─ ReservationResource                                 │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ EquipmentService                                    │
│  ├─ StockReservationService                             │
│  └─ InventoryEventPublisher                             │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ EquipmentRepository                                 │
│  ├─ StockReservationRepository                          │
│  └─ StockMovementRepository                             │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ Equipment (Event-driven Cache)                      │
│  ├─ StockReservation                                    │
│  └─ StockMovement                                       │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計（Event-Driven対応）

### Equipment (設備マスタ) - Product Catalog連携

- **product_id**: Product Catalogサービスへの参照（UUID）
- **daily_rate**: ビジネス固有のレンタル料金
- **is_rental_available**: レンタル可能フラグ
- **warehouse_id**: 倉庫場所
- **available_quantity**: 利用可能在庫数
- **reserved_quantity**: 予約済み在庫数

#### キャッシュフィールド（Product Catalogからイベント同期）

- **cached_sku**: 商品SKU（キャッシュ）
- **cached_name**: 商品名（キャッシュ）
- **cached_category**: カテゴリ（キャッシュ）
- **cached_brand**: ブランド（キャッシュ）
- **cached_equipment_type**: 設備タイプ（キャッシュ）
- **cached_base_price**: 基本価格（キャッシュ）
- **cache_updated_at**: キャッシュ更新日時

### StockReservation (在庫予約)

- 予約ID、商品ID、顧客ID
- 予約期間（開始日時、終了日時）
- ステータス（予約中、確定、キャンセル、期限切れ）
- 予約数量

### StockMovement (在庫移動履歴)

- 移動ID、商品ID、移動タイプ
- 数量、理由、作成日時
- 参照ID（注文ID等）

## API エンドポイント（Event-Driven対応）

### 在庫管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/inventory/equipment` | レンタル設備一覧取得 |
| GET | `/inventory/equipment/{productId}` | Product ID指定での設備詳細取得 |
| GET | `/inventory/equipment/{productId}/availability` | 在庫状況取得 |
| POST | `/inventory/equipment/{productId}/stock` | 在庫数更新 |
| GET | `/inventory/equipment/by-sku/{sku}` | SKU指定での設備検索（キャッシュ利用） |

### 予約管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/inventory/reservations` | 在庫予約作成 |
| GET | `/inventory/reservations/{reservationId}` | 予約詳細取得 |
| POST | `/inventory/reservations/{reservationId}/confirm` | 予約確定 |
| POST | `/inventory/reservations/{reservationId}/cancel` | 予約キャンセル |
| GET | `/inventory/reservations/order/{orderId}` | 注文別予約一覧 |

### Event処理API（内部用）

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/inventory/health/data-sync` | Product Catalogとの同期状況確認 |
| POST | `/inventory/sync/manual` | 手動データ同期実行 |

## 環境設定

### Kafka設定

```properties
# Product Catalog連携
mp.messaging.incoming.product-events.connector=smallrye-kafka
mp.messaging.incoming.product-events.topic=product-events
mp.messaging.incoming.product-events.group.id=inventory-management-group

# 在庫イベント発行
mp.messaging.outgoing.inventory-events-out.connector=smallrye-kafka
mp.messaging.outgoing.inventory-events-out.topic=inventory-events
```

### データベース設定

```properties
# PostgreSQL設定
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=${DB_USER:skiresort}
quarkus.datasource.password=${DB_PASSWORD:skiresort}
quarkus.datasource.jdbc.url=${DB_URL:jdbc:postgresql://localhost:5432/skiresortdb}

# Redis設定（在庫キャッシュ）
quarkus.redis.hosts=${REDIS_URL:redis://localhost:6379}
```

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
| `QUARKUS_HTTP_PORT` | サービスポート | `8085` |
| `DB_URL` | データベース接続URL | `jdbc:postgresql://localhost:5432/skiresortdb` |
| `DB_USER` | データベースユーザー | `skiresort` |
| `DB_PASSWORD` | データベースパスワード | `skiresort` |
| `REDIS_URL` | RedisURL | `redis://localhost:6379` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafkaサーバー | `localhost:9092` |
| `INVENTORY_CACHE_TTL` | 在庫キャッシュTTL | `300` |
| `INVENTORY_CART_RESERVATION_TIMEOUT_MINUTES` | カート予約タイムアウト | `30` |

## データベース設定

### PostgreSQL設定

```sql
-- 商品マスタテーブル（商品カタログサービスと連携）
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
- Docker & Docker Compose
- PostgreSQL 15+ (Dockerで提供)
- Redis 7+ (Dockerで提供)
- Apache Kafka 3.0+ (Dockerで提供)

### ローカル開発環境

#### Docker Composeでの起動

```bash
# 全サービス起動（データベース、Redis、Kafka含む）
docker-compose up -d

# ログ確認
docker-compose logs -f inventory-management-service

# サービス停止
docker-compose down
```

#### Quarkus開発モードでの起動

```bash
# 依存サービスのみ起動
docker-compose up -d postgres redis kafka

# Quarkus開発モード起動（ホットリロード有効）
./mvnw compile quarkus:dev -Dquarkus.http.port=8085
```

### ビルド

```bash
# Maven ビルド
./mvnw clean compile

# テスト実行
./mvnw test

# 実行可能JAR作成
./mvnw package

# Nativeイメージ作成（本番用）
./mvnw package -Pnative
```

### デプロイ

#### Docker実行

```bash
# Dockerイメージ作成
docker build -t skiresort/inventory-management-service .

# コンテナ起動
docker run -p 8085:8085 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/skiresortdb \
  -e REDIS_URL=redis://host.docker.internal:6379 \
  skiresort/inventory-management-service
```

#### 本番環境デプロイ

```bash
# 本番ビルド実行
./build-production.sh

# Kubernetes デプロイ（例）
kubectl apply -f k8s/deployment.yaml
```

## API使用例

### 在庫状況確認

```bash
curl -X GET "http://localhost:8085/api/v1/inventory/variants/ski-rossignol-x1/availability?color=red&size=170cm&warehouseId=uuid" \
  -H "Authorization: Bearer your_jwt_token"
```

### バリアント在庫予約

```bash
curl -X POST http://localhost:8085/api/v1/inventory/cart/reserve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "cartId": "cart-uuid",
    "customerId": "customer-uuid",
    "variantSku": "ski-rossignol-x1-red-170",
    "warehouseId": "warehouse-uuid",
    "quantity": 1,
    "unitPrice": 89000
  }'
```

### 価格計算

```bash
curl -X POST http://localhost:8085/api/v1/inventory/pricing/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "variantSku": "ski-rossignol-x1-red-170",
    "quantity": 2,
    "customerId": "customer-uuid",
    "promoCodes": ["WINTER2024", "FIRSTBUY"]
  }'
```

### 配送可能性確認

```bash
curl -X GET "http://localhost:8085/api/v1/inventory/shipping/availability?variantSku=ski-rossignol-x1-red-170&postalCode=106-0032&quantity=1" \
  -H "Authorization: Bearer your_jwt_token"
```

### 代替商品提案

```bash
curl -X GET "http://localhost:8085/api/v1/inventory/alternatives/ski-rossignol-x1-red-170?warehouseId=warehouse-uuid" \
  -H "Authorization: Bearer your_jwt_token"
```

## 監視とロギング

### ヘルスチェック

- `/health` エンドポイントでサービス状態確認
- `/health/ready` 準備状態確認
- `/health/live` 生存状態確認
- データベース接続状態
- Redis・Kafka接続状態
- 外部サービス接続状態

### メトリクス

- 在庫回転率
- 予約成功率
- カート予約タイムアウト率
- API レスポンス時間
- データベース性能指標

### ログ

- 在庫変更ログ
- 予約操作ログ
- 価格計算ログ
- エラーログ

## スキーショップ販売サイト向け拡張機能

### 商品バリエーション管理

- **カラー・サイズ別在庫**: 商品の各バリエーション（カラー×サイズ）ごとの個別在庫管理
- **SKU階層管理**: 親SKU（商品）- 子SKU（バリエーション）の階層構造
- **バリエーション検索**: カラー・サイズ指定での在庫確認・予約機能

### 価格・プロモーション管理

- **動的価格計算**: 在庫レベルに応じた価格調整機能
- **プロモーション適用**: 割引クーポン、セール価格の自動適用
- **価格履歴管理**: 価格変動の履歴追跡と分析

### オンライン販売特化機能

- **ショッピングカート一時予約**: カート追加時の30分間在庫確保
- **入荷予定通知**: 在庫切れ商品の入荷時自動通知
- **代替商品提案**: 在庫切れ時の類似商品推薦
- **リアルタイム在庫表示**: Webサイト上でのリアルタイム在庫状況表示

### 配送・受取管理

- **配送エリア別在庫管理**: 配送可能エリアごとの在庫確保
- **店舗受取対応**: 店舗受取用在庫の別管理
- **配送可否判定**: 郵便番号ベースの配送可能性判定

## Docker環境

### ローカル開発環境構成

- **アプリケーション**: Quarkus (ポート: 8085)
- **データベース**: PostgreSQL 15 (ポート: 5432)
- **キャッシュ**: Redis 7 (ポート: 6379)
- **メッセージング**: Apache Kafka + Zookeeper (ポート: 9092, 2181)

### Docker Compose利用

```bash
# サービス起動
docker-compose up -d

# 特定サービスのみ起動
docker-compose up -d postgres redis

# ログ確認
docker-compose logs -f inventory-management-service

# サービス停止・削除
docker-compose down -v
```

### Quarkus開発機能

- **ホットリロード**: コード変更時の自動再起動
- **開発UI**: <http://localhost:8085/q/dev> でアクセス
- **Swagger UI**: <http://localhost:8085/q/swagger-ui>
- **ヘルスチェック**: <http://localhost:8085/q/health>

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

### 商品データ（30種類）

商品カタログサービスと連携して、以下の商品が管理されています：

#### スキー板（13種類）

- **レーシングGS**: Rossignol Hero Athlete FIS GS、Atomic Redster X9 WC GS
- **レーシングSL**: Atomic Redster G9 FIS SL、Salomon S/Race Rush SL
- **パウダー**: K2 Mindbender 108Ti、Volkl Mantra M6、Nordica Enforcer 110 Free
- **モーグル**: K2 Mogul Ski
- **初級・中級向け**: Salomon S/Max 8、Rossignol Experience 78 Ti、Atomic Vantage 75 C
- **ジュニア**: Rossignol Experience Pro Jr、Salomon QST Max Jr

#### その他商品（17種類）

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

各商品に対して複数の在庫アイテムが作成されており、以下の状況を含みます：

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

- **Quarkus Core**: RESTEasy Reactive、Jackson、Hibernate ORM Panache
- **データベース**: PostgreSQL JDBC Driver
- **キャッシュ**: Redis Client
- **メッセージング**: Kafka Reactive Messaging
- **MicroProfile**: Health、Metrics、OpenAPI
- **テスト**: JUnit 5、REST Assured、Testcontainers

## 在庫数取得API

### 基本的な在庫情報取得

特定商品の在庫情報を取得するには、以下のAPIエンドポイントを使用します：

```http
GET /api/v1/inventory/equipment/{productId}
```

**レスポンス例:**

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "sku": "ski-rossignol-x1",
  "name": "Rossignol Hero Athlete FIS GS",
  "category": "SKI_BOARD",
  "totalQuantity": 15,
  "availableQuantity": 12,
  "reservedQuantity": 3,
  "maintenanceQuantity": 0,
  "dailyRate": 8900,
  "lastUpdated": "2024-12-15T10:30:00Z"
}
```

### 期間指定での在庫可用性確認

レンタル期間を指定して在庫の可用性を確認する場合：

```http
GET /api/v1/inventory/equipment/{productId}/availability?startDate=2024-12-20&endDate=2024-12-27&quantity=2
```

**レスポンス例:**

```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "totalQuantity": 15,
  "availableQuantity": 12,
  "isAvailable": true,
  "availabilityByDate": [
    {
      "date": "2024-12-20",
      "availableQuantity": 12
    },
    {
      "date": "2024-12-21",
      "availableQuantity": 10
    }
  ],
  "nextAvailableDate": "2024-12-20T09:00:00Z"
}
```

### 実装例（JavaScript）

```javascript
// 在庫情報取得
async function getProductStock(productId) {
  const response = await fetch(`/api/v1/inventory/equipment/${productId}`, {
    headers: {
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    }
  });
  
  if (response.ok) {
    const data = await response.json();
    return data;
  }
  throw new Error('在庫情報の取得に失敗しました');
}

// 期間指定での可用性確認
async function checkAvailability(productId, startDate, endDate, quantity = 1) {
  const params = new URLSearchParams({
    startDate: startDate,
    endDate: endDate,
    quantity: quantity.toString()
  });
  
  const response = await fetch(
    `/api/v1/inventory/equipment/${productId}/availability?${params}`,
    { headers: { 'Authorization': 'Bearer ' + token } }
  );
  
  return response.ok ? (await response.json()).isAvailable : false;
}
```

## 同時アクセス制御

### 同時購入リクエストへの対応

在庫数が少ない商品に対する同時購入リクエストは、以下の仕組みで制御されます：

#### 責任分離

| サービス | 責任範囲 |
|---------|---------|
| **Order Management Service** | 注文プロセス管理、顧客対応、代替商品提案 |
| **Inventory Management Service** | 原子的在庫操作、同時アクセス制御、データ整合性 |

#### 同時制御の仕組み

```text
【在庫1個に対する同時購入要求の処理】

1. 顧客A・Bが同時に購入リクエスト
   ↓
2. Order Management Service → Inventory Management Service
   POST /api/v1/inventory/reservations (並行実行)
   ↓
3. Inventory Management Service での制御:
   - 楽観的ロック（version_field）による制御
   - 原子的なCAS（Compare-And-Swap）操作
   - 先着順での在庫確保
   ↓
4. 処理結果:
   - 先着者: 予約成功 (200 OK)
   - 後着者: 在庫不足エラー (409 Conflict)
```

#### データベース制御

**楽観的ロック制御:**

```sql
-- Equipment テーブルにバージョンフィールド追加
ALTER TABLE equipment ADD COLUMN version_field BIGINT DEFAULT 1;

-- 原子的在庫更新
UPDATE equipment 
SET available_quantity = available_quantity - ?,
    reserved_quantity = reserved_quantity + ?,
    version_field = version_field + 1
WHERE product_id = ? 
  AND version_field = ?
  AND available_quantity >= ?;
```

#### 競合エラーハンドリング

在庫競合が発生した場合のレスポンス例：

```json
{
  "error": "STOCK_CONFLICT",
  "message": "在庫が他の顧客により確保されました",
  "errorCode": "INV_409_CONCURRENT_RESERVATION",
  "suggestions": [
    {
      "type": "ALTERNATIVE_PRODUCT",
      "productId": "alternative-uuid",
      "message": "類似商品をご提案します"
    },
    {
      "type": "WAITLIST",
      "estimatedAvailability": "2024-12-25T10:00:00Z",
      "message": "キャンセル待ちリストへの登録が可能です"
    }
  ]
}
```

### 高負荷時の追加制御

**Redis分散ロック:**

```text
1. Redis分散ロック取得 (Key: "inventory:lock:{productId}", TTL: 30秒)
2. ロック取得成功時のみDB操作実行
3. 処理完了後のロック解放
4. ロック取得失敗時は即座にエラーレスポンス
```

**Kafkaキューイング:**

```text
1. 大量同時アクセス時は予約要求をKafkaキューに投入
2. 単一コンシューマによる順次処理
3. 処理結果の非同期通知
4. Order Management Serviceでの結果ハンドリング
```

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。
