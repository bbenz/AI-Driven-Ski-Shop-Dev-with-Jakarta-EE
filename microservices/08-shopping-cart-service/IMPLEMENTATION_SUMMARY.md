# Shopping Cart Service - Implementation Summary

## 実装完了項目

### ✅ 完了した機能

#### 1. コアアーキテクチャ
- **Quarkus 3.15.1** ベースのマイクロサービス
- **Java 21 LTS** での開発
- **Jakarta EE 11** 準拠の実装
- **Panache ORM** による効率的なデータアクセス

#### 2. データベース設計
- **PostgreSQL 16** による永続化
- **Flyway** マイグレーションによるスキーマ管理
- 以下のテーブル構造:
  - `shopping_carts` - カートメタデータ
  - `cart_items` - カート商品アイテム
  - `applied_coupons` - 適用クーポン
  - `cart_events` - イベントソーシング

#### 3. REST API エンドポイント
- **カート管理**: 作成、取得、更新、削除
- **商品操作**: 追加、削除、数量変更
- **セッション管理**: ゲストカート対応
- **カートマージ**: ゲスト→ユーザーカート統合
- **OpenAPI/Swagger** ドキュメント自動生成

#### 4. WebSocket サポート
- リアルタイムカート更新通知
- 価格変更通知
- 在庫状況通知
- 商品廃止通知

#### 5. キャッシュ戦略
- **Redis** 分散キャッシュ
- カートサマリキャッシュ (1時間 TTL)
- セッションマッピング (8時間 TTL)
- 排他制御用ロック

#### 6. イベント駆動アーキテクチャ
- **Apache Kafka** 統合
- カートイベント発行:
  - CartCreatedEvent
  - CartItemAddedEvent
  - CartItemRemovedEvent
  - CartItemQuantityUpdatedEvent
  - CartMergedEvent

#### 7. 外部サービス統合
- **Product Catalog Service** 連携
- **Inventory Management Service** 連携
- **User Management Service** 連携
- **Circuit Breaker, Retry, Fallback** パターン実装

#### 8. 可観測性・監視
- **Prometheus** メトリクス
- **Jaeger** 分散トレーシング
- **Health Check** エンドポイント
- 構造化ログ出力

#### 9. コンテナ化
- **Docker** マルチステージビルド
- **Docker Compose** 開発環境
- **健全性チェック** 設定
- **JVM最適化** 設定

#### 10. 開発ツール
- **Quarkus Dev Mode** サポート
- **Live Reload** 機能
- **Dev UI** 統合
- **テストコンテナ** サポート

## 技術仕様詳細

### アーキテクチャパターン
- **マイクロサービス**: 疎結合、独立デプロイ可能
- **イベント駆動**: 非同期メッセージング
- **CQRS要素**: 読み書き分離の基礎実装
- **キャッシュファースト**: パフォーマンス最適化

### セキュリティ
- **JWT認証**: ベアラートークンサポート準備
- **入力検証**: Jakarta Validation
- **SQLインジェクション対策**: パラメータ化クエリ
- **CORS設定**: クロスオリジンサポート

### パフォーマンス
- **非同期処理**: CompletableFuture活用
- **コネクションプール**: DB、Redis最適化
- **多層キャッシュ**: メモリ + Redis
- **Virtual Threads**: Java 21の並行性活用

### 耐障害性
- **Circuit Breaker**: 外部サービス保護
- **Retry**: 一時的障害自動復旧
- **Fallback**: 段階的機能縮退
- **Timeout**: リクエストタイムアウト制御

## ディレクトリ構造

```
08-shopping-cart-service/
├── src/main/java/com/skishop/cart/
│   ├── entity/           # JPA エンティティ
│   ├── dto/              # データ転送オブジェクト
│   ├── resource/         # REST エンドポイント
│   ├── service/          # ビジネスロジック
│   ├── event/            # イベント定義
│   ├── exception/        # 例外クラス
│   └── health/           # ヘルスチェック
├── src/main/resources/
│   ├── db/migration/     # Flyway マイグレーション
│   ├── application.properties
│   └── import.sql        # サンプルデータ
├── src/test/java/        # テストコード
├── docker/
│   └── prometheus/       # Prometheus設定
├── Dockerfile            # コンテナビルド
├── docker-compose.yml    # 開発環境
├── pom.xml              # Maven設定
├── run.sh               # 起動スクリプト
└── README.md            # ドキュメント
```

## 起動方法

### 開発モード
```bash
# インフラ起動
docker-compose up -d postgres redis kafka zookeeper

# アプリケーション起動
./mvnw quarkus:dev
```

### 本番モード
```bash
# 全サービス起動
./run.sh

# または
docker-compose up --build
```

## エンドポイント

### 主要API
- `GET /api/v1/carts/{cartId}` - カート取得
- `POST /api/v1/carts/{cartId}/items` - 商品追加
- `PUT /api/v1/carts/{cartId}/items/{sku}/quantity` - 数量更新
- `DELETE /api/v1/carts/{cartId}/items/{sku}` - 商品削除

### 管理・監視
- `GET /q/health` - ヘルスチェック
- `GET /metrics` - Prometheusメトリクス
- `GET /swagger-ui` - API仕様書
- `WS /api/v1/carts/ws/{cartId}` - WebSocket

## 外部サービス依存

### 必須依存
1. **PostgreSQL** (port 5432) - データ永続化
2. **Redis** (port 6379) - キャッシュ
3. **Kafka** (port 9092) - イベント配信

### オプション依存
1. **Product Catalog Service** (port 8081) - 商品情報
2. **Inventory Service** (port 8082) - 在庫管理
3. **User Service** (port 8083) - ユーザー情報

### 監視サービス
1. **Jaeger** (port 16686) - 分散トレーシング
2. **Prometheus** (port 9090) - メトリクス収集

## 12-frontend-service との統合

### REST API 統合
フロントエンドサービスは以下のエンドポイントを使用:
- カート操作: REST API経由
- リアルタイム更新: WebSocket経由

### データフロー
1. **ユーザー操作** → Frontend Service
2. **API呼び出し** → Cart Service (HTTP/REST)
3. **リアルタイム通知** → Frontend Service (WebSocket)
4. **UI更新** → ユーザー画面

### セッション管理
- **ゲストユーザー**: セッションIDベース
- **ログインユーザー**: カスタマーIDベース
- **自動マージ**: ログイン時のカート統合

## テスト

### 単体テスト
```bash
./mvnw test
```

### 統合テスト
```bash
./mvnw verify
```

### 手動テスト
```bash
# カート作成
curl -X GET http://localhost:8088/api/v1/carts/session/test-session

# 商品追加
curl -X POST http://localhost:8088/api/v1/carts/{cartId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId":"550e8400-e29b-41d4-a716-446655440301","sku":"SKI-BOOT-001","productName":"Test Boots","unitPrice":15000.00,"quantity":1}'
```

## 今後の拡張予定

- [ ] 高度なキャッシュ戦略
- [ ] ML基盤カート放棄予測
- [ ] GraphQL APIサポート
- [ ] セキュリティ強化（OAuth2、レート制限）
- [ ] マルチリージョン対応

## 品質指標

- **テストカバレッジ**: 85%以上目標
- **API応答時間**: 95%ile < 200ms
- **可用性**: 99.9%目標
- **データ整合性**: ACID準拠
