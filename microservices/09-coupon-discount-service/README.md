# Coupon Discount Service

クーポン・割引サービス（Coupon Discount Service）は、スキーリゾート管理システムのクーポンと割引機能を担当するマイクロサービスです。

## 概要

このサービスは以下のクーポン・割引機能を提供します：

- **クーポン管理**: クーポンの作成、配布、利用、期限管理
- **割引ルール**: 条件付き割引、時期別割引、グループ割引
- **プロモーション**: キャンペーン管理、特別イベント割引
- **利用制限**: 利用回数制限、ユーザー制限、併用制限
- **効果分析**: クーポン利用率、売上効果、顧客行動分析
- **自動配布**: 誕生日クーポン、リピーター特典

## 技術スタック

- **Jakarta EE 11**: エンタープライズJavaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **WildFly 31.0.1**: アプリケーションサーバー
- **PostgreSQL**: メインデータベース
- **Redis**: キャッシュ・セッション管理
- **Apache Kafka**: イベントストリーミング
- **MicroProfile Config**: 設定管理
- **MicroProfile Health**: ヘルスチェック

## アーキテクチャ

```text
┌─────────────────────────────────────────────────────────┐
│               Coupon Discount Service                   │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ CouponResource                                      │
│  ├─ DiscountResource                                    │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ CouponService                                       │
│  ├─ DiscountService                                     │
│  └─ PromotionService                                    │
├─────────────────────────────────────────────────────────┤
│  Rule Engine Layer                                      │
│  ├─ DiscountRuleEngine                                  │
│  ├─ EligibilityChecker                                  │
│  └─ UsageLimitValidator                                 │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ CouponRepository                                    │
│  ├─ DiscountRuleRepository                              │
│  └─ UsageHistoryRepository                              │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ Coupon                                              │
│  ├─ DiscountRule                                        │
│  └─ UsageHistory                                        │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計

### Coupon (クーポン)

- クーポンID、クーポンコード、クーポン名
- 割引タイプ（固定額、割合、無料）、割引値
- 有効期間（開始日、終了日）
- 利用制限（回数、ユーザー、併用可否）

### DiscountRule (割引ルール)

- ルールID、ルール名、ルールタイプ
- 適用条件（商品、金額、日時、ユーザー属性）
- 割引計算方法
- 優先度、アクティブ状態

### UsageHistory (利用履歴)

- 利用ID、クーポンID、ユーザーID、注文ID
- 利用日時、割引金額
- 利用状況（適用、キャンセル）

## API エンドポイント

### クーポン管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/coupons` | クーポン一覧取得 |
| POST | `/coupons` | クーポン作成 |
| GET | `/coupons/{couponId}` | クーポン詳細取得 |
| PUT | `/coupons/{couponId}` | クーポン更新 |
| DELETE | `/coupons/{couponId}` | クーポン削除 |

### 割引計算API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/discounts/calculate` | 割引金額計算 |
| POST | `/discounts/apply` | 割引適用 |
| GET | `/discounts/eligible` | 適用可能割引取得 |

### クーポン利用API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/coupons/{couponCode}/validate` | クーポン有効性確認 |
| POST | `/coupons/{couponCode}/use` | クーポン利用 |
| GET | `/users/{userId}/coupons` | ユーザークーポン取得 |

## セキュリティ機能

### クーポンコードセキュリティ

- ランダムコード生成
- 推測困難なコード形式
- 利用回数制限
- 不正利用検知

### アクセス制御

- 管理者権限（クーポン作成・削除）
- スタッフ権限（クーポン配布）
- ユーザー権限（クーポン利用）

### 監査機能

- クーポン利用履歴
- 割引適用ログ
- 不正利用検知ログ

## 設定

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `DATABASE_URL` | データベース接続URL | `jdbc:postgresql://localhost:5432/skiresortdb` |
| `DATABASE_USER` | データベースユーザー | `skiresort` |
| `DATABASE_PASSWORD` | データベースパスワード | `skiresort` |
| `REDIS_HOST` | Redisホスト | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafkaサーバー | `localhost:9092` |
| `COUPON_CODE_LENGTH` | クーポンコード長 | `8` |
| `MAX_DISCOUNT_PERCENTAGE` | 最大割引率 | `50` |
| `CACHE_TTL_MINUTES` | キャッシュTTL | `30` |

## データベース設定

### PostgreSQL設定

```sql
-- クーポンテーブル
CREATE TABLE coupons (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    discount_type VARCHAR(20) NOT NULL, -- FIXED_AMOUNT, PERCENTAGE, FREE_ITEM
    discount_value DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2),
    max_discount_amount DECIMAL(10,2),
    usage_limit INTEGER,
    used_count INTEGER DEFAULT 0,
    per_user_limit INTEGER DEFAULT 1,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 割引ルールテーブル
CREATE TABLE discount_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    rule_type VARCHAR(50) NOT NULL, -- TIME_BASED, QUANTITY_BASED, USER_BASED
    conditions JSONB NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10,2) NOT NULL,
    priority INTEGER DEFAULT 0,
    is_combinable BOOLEAN DEFAULT false,
    is_active BOOLEAN DEFAULT true,
    valid_from TIMESTAMP,
    valid_until TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 利用履歴テーブル
CREATE TABLE usage_history (
    id BIGSERIAL PRIMARY KEY,
    coupon_id BIGINT REFERENCES coupons(id),
    rule_id BIGINT REFERENCES discount_rules(id),
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    discount_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## イベント統合

### パブリッシュされるイベント

- `CouponUsedEvent` - クーポン利用時
- `DiscountAppliedEvent` - 割引適用時
- `CouponExpiredEvent` - クーポン期限切れ時
- `PromotionStartedEvent` - プロモーション開始時

### 消費されるイベント

- 注文管理サービスからの割引計算要求
- ユーザー管理サービスからの誕生日通知
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
cp target/coupon-discount-service.war $WILDFLY_HOME/standalone/deployments/
```

### Docker実行

```bash
# Docker Compose で実行
docker-compose up coupon-discount-service
```

## API使用例

### クーポン作成

```bash
curl -X POST http://localhost:8088/coupons \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "name": "新春キャンペーン20%OFF",
    "description": "新春特別キャンペーン",
    "discountType": "PERCENTAGE",
    "discountValue": 20,
    "minOrderAmount": 5000,
    "maxDiscountAmount": 3000,
    "usageLimit": 100,
    "perUserLimit": 1,
    "validFrom": "2024-01-01T00:00:00Z",
    "validUntil": "2024-01-31T23:59:59Z"
  }'
```

### 割引計算

```bash
curl -X POST http://localhost:8088/discounts/calculate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "userId": 123,
    "items": [
      {
        "productId": "lift-ticket-daily",
        "quantity": 2,
        "price": 5000
      }
    ],
    "couponCode": "NEWYEAR20",
    "totalAmount": 10000
  }'
```

### クーポン有効性確認

```bash
curl -X POST http://localhost:8088/coupons/NEWYEAR20/validate \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "userId": 123,
    "orderAmount": 8000
  }'
```

### ユーザークーポン取得

```bash
curl -X GET "http://localhost:8088/users/123/coupons?status=available" \
  -H "Authorization: Bearer your_jwt_token"
```

## 割引ルールエンジン

### ルールタイプ

1. **時期別割引**
   - 早期予約割引
   - シーズンオフ割引
   - 平日割引

2. **数量割引**
   - 複数購入割引
   - グループ割引
   - まとめ買い割引

3. **ユーザー属性割引**
   - 学生割引
   - シニア割引
   - 会員ランク別割引

### ルール評価順序

1. 優先度の高いルールから評価
2. 併用可能ルールの組み合わせ最適化
3. 最大割引額の制限適用

## 監視とロギング

### ヘルスチェック

- `/health` エンドポイントでサービス状態確認
- データベース接続状態
- Redis・Kafka接続状態
- ルールエンジン状態

### メトリクス

- クーポン利用率
- 割引効果（売上増加率）
- 不正利用検知数
- 人気クーポンランキング

### ログ

- クーポン利用ログ
- 割引計算ログ
- 不正利用検知ログ
- エラーログ

## セキュリティ考慮事項

### 本番環境での推奨設定

1. **クーポンコード保護**
   - 推測困難なコード生成
   - 利用制限の厳格な適用

2. **不正利用防止**
   - 異常パターン検知
   - レート制限

3. **データ保護**
   - 利用履歴の暗号化
   - アクセス権限の制御

## トラブルシューティング

### よくある問題

1. **クーポン利用エラー**
   - 有効期限確認
   - 利用制限確認

2. **割引計算エラー**
   - ルール条件の確認
   - 併用制限の確認

3. **パフォーマンス問題**
   - キャッシュヒット率確認
   - ルール評価の最適化

## 今後の拡張予定

- [ ] AI による個人化クーポン
- [ ] 位置情報連動クーポン
- [ ] ソーシャルメディア連携
- [ ] A/Bテスト機能
- [ ] 動的価格調整

## 開発者向け情報

### コード構成

```text
src/main/java/
├── com/skiresort/coupon/
│   ├── entity/          # JPA エンティティ
│   ├── service/         # ビジネスロジック
│   ├── repository/      # データアクセス
│   ├── resource/        # REST エンドポイント
│   ├── rule/            # ルールエンジン
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
- JSON-B

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。
