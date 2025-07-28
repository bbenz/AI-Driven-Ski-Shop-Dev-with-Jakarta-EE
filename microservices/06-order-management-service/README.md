# 注文管理サービス (Order Management Service)

スキーリゾート管理システムの注文処理を担当するマイクロサービスです。

## 概要

このサービスは以下の機能を提供します：

- 注文作成・管理
- 注文明細の追加・更新・削除
- 注文ステータスの管理（保留中→確認済み→支払済み→処理中→発送済み→配送完了）
- 決済ステータスの管理
- 注文履歴の記録
- 注文キャンセル処理

## 技術スタック

- **Java**: 21 LTS
- **Jakarta EE**: 11
- **JPA**: 3.2
- **JAX-RS**: 4.0
- **CDI**: 4.1
- **Bean Validation**: 3.1
- **MicroProfile**: Health Check
- **データベース**: PostgreSQL 16
- **アプリケーションサーバー**: WildFly 31.0.1

## アーキテクチャ

### パッケージ構成

```
com.skiresort.order/
├── model/               # エンティティクラス
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── PaymentStatus.java
│   ├── OrderAmount.java
│   ├── ShippingAddress.java
│   └── OrderStatusHistory.java
├── repository/          # データアクセス層
│   ├── OrderRepository.java
│   ├── OrderItemRepository.java
│   └── OrderStatusHistoryRepository.java
├── service/            # ビジネスロジック層
│   ├── OrderService.java
│   └── OrderNumberService.java
├── controller/         # REST API層
│   └── OrderController.java
├── exception/          # 例外クラス
│   ├── OrderNotFoundException.java
│   └── InvalidOrderStateException.java
└── health/            # ヘルスチェック
    ├── OrderServiceLivenessCheck.java
    └── OrderServiceReadinessCheck.java
```

### データベース設計

#### orders テーブル
- 注文の基本情報
- 金額情報（小計、割引、税額、送料、合計）
- 配送先・請求先住所
- 各種日時情報

#### order_items テーブル
- 注文明細情報
- 商品情報、数量、価格

#### order_status_history テーブル
- 注文ステータス変更履歴
- 変更者、変更理由の記録

## API エンドポイント

### 注文管理

| HTTP Method | Endpoint | 説明 |
|-------------|----------|------|
| POST | `/api/orders` | 注文作成 |
| GET | `/api/orders/{orderId}` | 注文取得 |
| GET | `/api/orders/number/{orderNumber}` | 注文番号による取得 |
| GET | `/api/orders/customer/{customerId}` | 顧客の注文一覧 |
| GET | `/api/orders/status/{status}` | ステータス別注文一覧 |

### 注文ステータス管理

| HTTP Method | Endpoint | 説明 |
|-------------|----------|------|
| PUT | `/api/orders/{orderId}/status` | 注文ステータス変更 |
| PUT | `/api/orders/{orderId}/payment-status` | 決済ステータス変更 |
| PUT | `/api/orders/{orderId}/cancel` | 注文キャンセル |

### 注文明細管理

| HTTP Method | Endpoint | 説明 |
|-------------|----------|------|
| POST | `/api/orders/{orderId}/items` | 明細追加 |
| PUT | `/api/orders/{orderId}/items/{itemId}` | 明細更新 |
| DELETE | `/api/orders/{orderId}/items/{itemId}` | 明細削除 |
| GET | `/api/orders/{orderId}/items` | 明細一覧取得 |

### 注文履歴

| HTTP Method | Endpoint | 説明 |
|-------------|----------|------|
| GET | `/api/orders/{orderId}/history` | 注文履歴取得 |

## ビジネスルール

### 注文ステータス遷移

```
PENDING → CONFIRMED → PAID → PROCESSING → SHIPPED → DELIVERED
    ↓         ↓         ↓         ↓         ↓         ↓
CANCELLED  CANCELLED  CANCELLED  CANCELLED  CANCELLED  RETURNED
```

### 明細変更ルール

- 明細の変更は `PENDING` および `CONFIRMED` ステータスでのみ可能
- 注文確定後は基本的に明細変更不可

### キャンセルルール

- `DELIVERED` および `RETURNED` 以外のステータスでキャンセル可能
- キャンセル後の状態変更は不可

## 設定

### データベース接続設定

```properties
# PostgreSQL接続設定
spring.datasource.url=jdbc:postgresql://localhost:5432/skiresort_order
spring.datasource.username=skiresort_user
spring.datasource.password=skiresort_pass
```

### アプリケーション設定

```properties
# アプリケーション設定
app.order.number.prefix=ORD
app.order.expiry.hours=24
```

## ビルド・デプロイ

### ビルド

```bash
mvn clean compile
mvn package -DskipTests
```

### デプロイ

WildFly 31.0.1にデプロイ：

```bash
# WildFly CLI
[standalone@localhost:9990 /] deploy target/order-management-service.war
```

## ヘルスチェック

### Liveness Check

```bash
curl http://localhost:8080/health/live
```

### Readiness Check

```bash
curl http://localhost:8080/health/ready
```

## 使用例

### 注文作成

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440001",
    "orderItems": [{
      "productId": "550e8400-e29b-41d4-a716-446655440301",
      "sku": "SKI-LIFT-001",
      "productName": "スキーリフト1日券",
      "unitPrice": 5000.00,
      "quantity": 2
    }],
    "shippingAddress": {
      "name": "山田太郎",
      "postalCode": "100-0001",
      "address1": "東京都千代田区千代田1-1",
      "city": "千代田区",
      "country": "Japan"
    }
  }'
```

### 注文ステータス変更

```bash
curl -X PUT http://localhost:8080/api/orders/{orderId}/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED",
    "changedBy": "admin",
    "reason": "在庫確認完了"
  }'
```

## 開発者向け情報

### Java 21の新機能活用

- **Record Classes**: `OrderAmount`, `ShippingAddress`で値オブジェクトを定義
- **Pattern Matching**: ステータス遷移ロジックでswitch式を活用
- **Text Blocks**: 長いSQL文を読みやすく記述

### Jakarta EE 11の新機能

- **JPA 3.2**: UUID型の自動生成、改良されたクエリ機能
- **Bean Validation 3.1**: より細かいバリデーション制御
- **CDI 4.1**: 強化された依存性注入機能

## トラブルシューティング

### よくある問題

1. **データベース接続エラー**
   - PostgreSQLサーバーが起動しているか確認
   - 接続設定が正しいか確認

2. **注文ステータス変更エラー**
   - 無効なステータス遷移でないか確認
   - 注文の現在のステータスを確認

3. **明細変更エラー**
   - 注文ステータスが変更可能状態か確認
   - 明細IDが正しいか確認

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。

## 貢献

プルリクエストや Issue の報告は歓迎します。

## サポート

質問や問題がある場合は、GitHub Issues を使用してください。
