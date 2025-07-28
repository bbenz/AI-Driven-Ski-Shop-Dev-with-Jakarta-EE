# Points Loyalty Service

ポイント・ロイヤルティサービス（Points Loyalty Service）は、スキーリゾート管理システムの顧客ロイヤルティプログラムを担当するマイクロサービスです。

## 概要

このサービスは以下のロイヤルティ機能を提供します：

- **ポイント管理**: ポイントの付与、利用、残高管理、履歴追跡
- **会員ランク**: ランク判定、特典管理、ランクアップ通知
- **特典管理**: ランク別特典、期間限定特典、個人化特典
- **キャンペーン**: ボーナスポイント、イベント連動、紹介プログラム
- **分析機能**: 顧客生涯価値、利用パターン、効果測定
- **連携機能**: 外部ポイントサービス、提携企業との連携

## 技術スタック

- **Jakarta EE 11**: エンタープライズJavaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **WildFly 31.0.1**: アプリケーションサーバー
- **PostgreSQL**: メインデータベース
- **Redis**: キャッシュ・リアルタイム更新
- **Apache Kafka**: イベントストリーミング
- **MicroProfile Config**: 設定管理
- **MicroProfile Health**: ヘルスチェック

## アーキテクチャ

```text
┌─────────────────────────────────────────────────────────┐
│                Points Loyalty Service                   │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ PointsResource                                      │
│  ├─ LoyaltyResource                                     │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ PointsService                                       │
│  ├─ LoyaltyService                                      │
│  └─ RewardService                                       │
├─────────────────────────────────────────────────────────┤
│  Business Logic Layer                                   │
│  ├─ PointCalculationEngine                              │
│  ├─ TierEvaluationEngine                                │
│  └─ RewardEligibilityEngine                             │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ PointsAccountRepository                             │
│  ├─ TransactionRepository                               │
│  └─ LoyaltyTierRepository                               │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ PointsAccount                                       │
│  ├─ PointsTransaction                                   │
│  └─ LoyaltyTier                                         │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計

### PointsAccount (ポイントアカウント)

- アカウントID、ユーザーID
- 総ポイント、利用可能ポイント、保留ポイント
- ランク、ランクアップ日、次回評価日
- 累計購入金額、最終利用日

### PointsTransaction (ポイント取引)

- 取引ID、アカウントID、関連注文ID
- 取引タイプ（獲得、利用、失効、調整）
- ポイント数、取引日時
- 説明、有効期限

### LoyaltyTier (ロイヤルティランク)

- ランクID、ランク名、ランクレベル
- 必要条件（購入金額、ポイント、回数）
- 特典内容（ポイント倍率、割引率、特別サービス）
- アイコン、説明

## API エンドポイント

### ポイント管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/points/accounts/{userId}` | ポイント残高取得 |
| POST | `/points/accounts/{userId}/earn` | ポイント付与 |
| POST | `/points/accounts/{userId}/redeem` | ポイント利用 |
| GET | `/points/accounts/{userId}/transactions` | ポイント履歴取得 |

### ロイヤルティ管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/loyalty/tiers` | ランク一覧取得 |
| GET | `/loyalty/users/{userId}/tier` | ユーザーランク取得 |
| GET | `/loyalty/users/{userId}/benefits` | 利用可能特典取得 |
| POST | `/loyalty/users/{userId}/evaluate` | ランク再評価 |

### 特典管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/rewards/available` | 利用可能特典一覧 |
| POST | `/rewards/{rewardId}/claim` | 特典利用 |
| GET | `/rewards/history/{userId}` | 特典利用履歴 |

## セキュリティ機能

### ポイントセキュリティ

- ポイント取引の暗号化
- 不正利用検知
- 取引ログの改ざん防止
- 二重処理防止

### アクセス制御

- ユーザー本人のみアクセス可能
- 管理者権限（ポイント調整）
- 監査ログの記録

### データ整合性

- ポイント残高の整合性チェック
- 取引履歴の完全性保証
- 定期的な残高照合

## 設定

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `DATABASE_URL` | データベース接続URL | `jdbc:postgresql://localhost:5432/skiresortdb` |
| `DATABASE_USER` | データベースユーザー | `skiresort` |
| `DATABASE_PASSWORD` | データベースパスワード | `skiresort` |
| `REDIS_HOST` | Redisホスト | `localhost` |
| `KAFKA_BOOTSTRAP_SERVERS` | Kafkaサーバー | `localhost:9092` |
| `POINTS_EXPIRY_MONTHS` | ポイント有効期限 | `12` |
| `BASE_POINTS_RATE` | 基本ポイント付与率 | `0.01` |
| `TIER_EVALUATION_SCHEDULE` | ランク評価スケジュール | `0 0 1 * *` |

## データベース設定

### PostgreSQL設定

```sql
-- ポイントアカウントテーブル
CREATE TABLE points_accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT UNIQUE NOT NULL,
    total_points BIGINT DEFAULT 0,
    available_points BIGINT DEFAULT 0,
    pending_points BIGINT DEFAULT 0,
    tier_id BIGINT REFERENCES loyalty_tiers(id),
    tier_achieved_date DATE,
    next_tier_evaluation_date DATE,
    lifetime_purchase_amount DECIMAL(12,2) DEFAULT 0,
    last_activity_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ポイント取引テーブル
CREATE TABLE points_transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES points_accounts(id),
    transaction_type VARCHAR(20) NOT NULL, -- EARN, REDEEM, EXPIRE, ADJUST
    points_amount BIGINT NOT NULL,
    order_id BIGINT,
    description VARCHAR(500),
    expiry_date DATE,
    transaction_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100)
);

-- ロイヤルティランクテーブル
CREATE TABLE loyalty_tiers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    level_order INTEGER NOT NULL,
    min_purchase_amount DECIMAL(12,2) NOT NULL,
    min_points BIGINT,
    min_transactions INTEGER,
    points_multiplier DECIMAL(3,2) DEFAULT 1.00,
    discount_percentage DECIMAL(5,2) DEFAULT 0,
    special_benefits JSONB,
    icon_url VARCHAR(500),
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## イベント統合

### パブリッシュされるイベント

- `PointsEarnedEvent` - ポイント獲得時
- `PointsRedeemedEvent` - ポイント利用時
- `TierUpgradedEvent` - ランクアップ時
- `PointsExpiredEvent` - ポイント失効時

### 消費されるイベント

- 注文管理サービスからの購入完了通知
- 決済サービスからの支払い完了通知
- ユーザー管理サービスからの会員登録通知

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
cp target/points-loyalty-service.war $WILDFLY_HOME/standalone/deployments/
```

### Docker実行

```bash
# Docker Compose で実行
docker-compose up points-loyalty-service
```

## API使用例

### ポイント残高取得

```bash
curl -X GET "http://localhost:8089/points/accounts/123" \
  -H "Authorization: Bearer your_jwt_token"
```

### ポイント付与

```bash
curl -X POST http://localhost:8089/points/accounts/123/earn \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "points": 500,
    "orderId": 12345,
    "description": "リフト券購入",
    "expiryDate": "2025-01-15"
  }'
```

### ポイント利用

```bash
curl -X POST http://localhost:8089/points/accounts/123/redeem \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_jwt_token" \
  -d '{
    "points": 1000,
    "orderId": 12346,
    "description": "割引適用"
  }'
```

### ランク情報取得

```bash
curl -X GET "http://localhost:8089/loyalty/users/123/tier" \
  -H "Authorization: Bearer your_jwt_token"
```

### 利用可能特典取得

```bash
curl -X GET "http://localhost:8089/loyalty/users/123/benefits" \
  -H "Authorization: Bearer your_jwt_token"
```

## ポイント付与ルール

### 基本付与ルール

- 購入金額の1%をポイント付与
- ランクに応じたボーナス倍率
- 特別キャンペーン時の追加ポイント

### ボーナスポイント

- 初回利用ボーナス
- 誕生日ボーナス
- 連続利用ボーナス
- 友人紹介ボーナス

### ポイント有効期限

- 通常ポイント：12ヶ月
- ボーナスポイント：6ヶ月
- キャンペーンポイント：3ヶ月

## ロイヤルティランク

### ランク体系

1. **ブロンズ** (0円～)
   - ポイント倍率: 1.0倍
   - 特典: なし

2. **シルバー** (10万円～)
   - ポイント倍率: 1.2倍
   - 特典: 5%割引

3. **ゴールド** (30万円～)
   - ポイント倍率: 1.5倍
   - 特典: 10%割引、優先予約

4. **プラチナ** (100万円～)
   - ポイント倍率: 2.0倍
   - 特典: 15%割引、専用ラウンジ、無料アップグレード

### ランク判定条件

- 過去12ヶ月の累計購入金額
- 月次で自動評価・更新
- ランクダウンは年1回のみ

## 監視とロギング

### ヘルスチェック

- `/health` エンドポイントでサービス状態確認
- データベース接続状態
- Redis・Kafka接続状態
- ポイント残高整合性チェック

### メトリクス

- ポイント付与率
- ポイント利用率
- ランク分布
- 特典利用率

### ログ

- ポイント取引ログ
- ランク変更ログ
- 特典利用ログ
- エラーログ

## セキュリティ考慮事項

### 本番環境での推奨設定

1. **ポイントデータ保護**
   - 取引データの暗号化
   - 改ざん防止機能

2. **不正利用防止**
   - 異常取引検知
   - レート制限

3. **監査機能**
   - 全取引の記録
   - 管理者操作の追跡

## トラブルシューティング

### よくある問題

1. **ポイント残高不一致**
   - 取引履歴の確認
   - 定期整合性チェック実行

2. **ランク更新遅延**
   - 評価スケジュール確認
   - バッチ処理ログ確認

3. **パフォーマンス問題**
   - キャッシュ設定確認
   - インデックス最適化

## 今後の拡張予定

- [ ] AIによる個人化特典推奨
- [ ] ブロックチェーンポイント管理
- [ ] 外部ポイントサービス連携拡大
- [ ] ゲーミフィケーション要素追加
- [ ] リアルタイム通知機能

## 開発者向け情報

### コード構成

```text
src/main/java/
├── com/skiresort/loyalty/
│   ├── entity/          # JPA エンティティ
│   ├── service/         # ビジネスロジック
│   ├── repository/      # データアクセス
│   ├── resource/        # REST エンドポイント
│   ├── engine/          # ポイント・ランク計算エンジン
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
