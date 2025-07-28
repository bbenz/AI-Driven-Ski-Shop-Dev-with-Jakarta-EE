# Payment Service

決済サービス（Payment Service）は、スキーリゾート管理システムの決済処理を担当するマイクロサービスです。

## 概要

このサービスは以下の決済機能を提供します：

- **決済処理**: クレジットカード、電子マネー、QRコード決済
- **請求管理**: 利用料金の計算、明細作成、領収書発行
- **返金処理**: キャンセル時の返金、部分返金
- **分割払い**: クレジットカード分割、後払いサービス
- **売上管理**: 日次売上、月次レポート、分析データ
- **セキュリティ**: PCI DSS準拠、不正検知

## 技術スタック

- **Jakarta EE 11**: エンタープライズJavaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **WildFly 31.0.1**: アプリケーションサーバー
- **PostgreSQL**: メインデータベース
- **Redis**: セッション管理・キャッシュ
- **Apache Kafka**: イベントストリーミング
- **MicroProfile Config**: 設定管理
- **MicroProfile Health**: ヘルスチェック

## アーキテクチャ

```text
┌─────────────────────────────────────────────────────────┐
│                  Payment Service                        │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ PaymentResource                                     │
│  ├─ RefundResource                                      │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ PaymentService                                      │
│  ├─ RefundService                                       │
│  └─ FraudDetectionService                               │
├─────────────────────────────────────────────────────────┤
│  Payment Gateway Layer                                  │
│  ├─ CreditCardProcessor                                 │
│  ├─ QRCodeProcessor                                     │
│  └─ ElectronicMoneyProcessor                            │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ PaymentRepository                                   │
│  ├─ TransactionRepository                               │
│  └─ RefundRepository                                    │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ Payment                                             │
│  ├─ Transaction                                         │
│  └─ RefundRecord                                        │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計

### Payment (決済情報)

- 決済ID、注文ID、顧客ID
- 決済金額、通貨、決済方法
- ステータス（処理中、成功、失敗、キャンセル）
- 決済時刻、更新時刻

### Transaction (取引詳細)

- 取引ID、決済ID、外部取引ID
- 決済プロバイダー、認証コード
- 手数料、税額
- レスポンスコード、レスポンスメッセージ

### RefundRecord (返金記録)

- 返金ID、決済ID、返金金額
- 返金理由、返金ステータス
- 返金日時、処理者ID

## API エンドポイント

### 決済処理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/payment/process` | 決済処理 |
| GET | `/payment/{paymentId}` | 決済詳細取得 |
| GET | `/payment/{paymentId}/status` | 決済ステータス確認 |
| POST | `/payment/{paymentId}/cancel` | 決済キャンセル |

### 返金処理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/payment/{paymentId}/refund` | 返金処理 |
| GET | `/payment/{paymentId}/refunds` | 返金履歴取得 |
| GET | `/refund/{refundId}` | 返金詳細取得 |

### 売上管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/payment/sales/daily` | 日次売上取得 |
| GET | `/payment/sales/monthly` | 月次売上取得 |
| GET | `/payment/transactions` | 取引履歴検索 |

## セキュリティ機能

### PCI DSS準拠

- カード情報の暗号化
- セキュアな通信（TLS 1.3）
- アクセスログの記録
- 定期的なセキュリティ監査

### 不正検知

- 異常な取引パターンの検知
- 地理的異常の検知
- 金額異常の検知
- ブラックリストチェック

### データ保護

- 個人情報の仮名化
- 決済データの暗号化
- アクセス権限の制御
- 監査証跡の保持

## 設定

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `DATABASE_URL` | データベース接続URL | `jdbc:postgresql://localhost:5432/skiresortdb` |
| `DATABASE_USER` | データベースユーザー | `skiresort` |
| `DATABASE_PASSWORD` | データベースパスワード | `skiresort` |
| `REDIS_HOST` | Redisホスト | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafkaサーバー | `localhost:9092` |
| `PAYMENT_GATEWAY_URL` | 決済ゲートウェイURL | `https://api.payment-gateway.com` |
| `PAYMENT_GATEWAY_API_KEY` | 決済ゲートウェイAPIキー | `dummy_api_key` |
| `FRAUD_DETECTION_THRESHOLD` | 不正検知閾値 | `0.8` |

## データベース設定

### PostgreSQL設定

```sql
-- 決済情報テーブル
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'JPY',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    gateway_transaction_id VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 取引詳細テーブル
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    external_transaction_id VARCHAR(200),
    payment_provider VARCHAR(50),
    authorization_code VARCHAR(100),
    processing_fee DECIMAL(10,2),
    tax_amount DECIMAL(10,2),
    response_code VARCHAR(10),
    response_message TEXT,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 返金記録テーブル
CREATE TABLE refund_records (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT NOT NULL REFERENCES payments(id),
    refund_amount DECIMAL(10,2) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processed_by BIGINT,
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## イベント統合

### パブリッシュされるイベント

- `PaymentProcessedEvent` - 決済処理完了時
- `PaymentFailedEvent` - 決済失敗時
- `RefundProcessedEvent` - 返金処理完了時
- `FraudDetectedEvent` - 不正検知時

### 消費されるイベント

- 注文管理サービスからの決済要求
- ユーザー管理サービスからの顧客情報更新
- ショッピングカートサービスからの決済開始

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
cp target/payment-service.war $WILDFLY_HOME/standalone/deployments/
```

### Docker実行

```bash
# Docker Compose で実行
docker-compose up payment-service
```

## API使用例

### 決済処理

```bash
curl -X POST http://localhost:8086/payment/process \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "orderId": 12345,
    "customerId": 678,
    "amount": 15000,
    "currency": "JPY",
    "paymentMethod": "CREDIT_CARD",
    "cardDetails": {
      "number": "4111111111111111",
      "expiryMonth": 12,
      "expiryYear": 2025,
      "cvv": "123",
      "holderName": "TARO YAMADA"
    }
  }'
```

### 決済ステータス確認

```bash
curl -X GET "http://localhost:8086/payment/pay-12345/status" \
  -H "Authorization: Bearer your_jwt_token"
```

### 返金処理

```bash
curl -X POST http://localhost:8086/payment/pay-12345/refund \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "amount": 5000,
    "reason": "キャンセルによる返金",
    "refundMethod": "ORIGINAL"
  }'
```

### 売上レポート取得

```bash
curl -X GET "http://localhost:8086/payment/sales/daily?date=2024-01-15" \
  -H "Authorization: Bearer your_jwt_token"
```

## 決済プロバイダー統合

### クレジットカード決済

- Stripe API統合
- Square API統合
- PayPal API統合
- 国内決済代行サービス

### 電子マネー決済

- 交通系ICカード
- 楽天Edy
- iD、QUICPay
- PayPay、LINE Pay

### QRコード決済

- PayPay
- 楽天ペイ
- d払い
- au PAY

## 監視とロギング

### ヘルスチェック

- `/health` エンドポイントでサービス状態確認
- データベース接続状態
- 決済ゲートウェイ接続状態
- Redis・Kafka接続状態

### メトリクス

- 決済成功率
- 平均処理時間
- 返金率
- 不正検知率

### ログ

- 決済処理ログ
- 返金処理ログ
- 不正検知ログ
- エラーログ

## セキュリティ考慮事項

### 本番環境での推奨設定

1. **カード情報の保護**
   - PCI DSS準拠
   - カード情報の非保持化

2. **通信セキュリティ**
   - TLS 1.3使用
   - 証明書ピニング

3. **不正防止**
   - 3Dセキュア対応
   - リスクベース認証

## トラブルシューティング

### よくある問題

1. **決済失敗**
   - ネットワーク接続確認
   - APIキーの有効性確認

2. **パフォーマンス問題**
   - 決済ゲートウェイ応答時間確認
   - データベース接続プール設定

3. **不正検知誤検知**
   - 閾値設定の調整
   - ホワイトリスト設定

## 今後の拡張予定

- [ ] 暗号通貨決済対応
- [ ] 分割払いサービス
- [ ] リカーリング決済
- [ ] API決済（Open Banking）
- [ ] AIによる不正検知強化

## 開発者向け情報

### コード構成

```text
src/main/java/
├── com/skiresort/payment/
│   ├── entity/          # JPA エンティティ
│   ├── service/         # ビジネスロジック
│   ├── repository/      # データアクセス
│   ├── resource/        # REST エンドポイント
│   ├── gateway/         # 決済ゲートウェイ統合
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
- 決済ゲートウェイ SDK

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。
