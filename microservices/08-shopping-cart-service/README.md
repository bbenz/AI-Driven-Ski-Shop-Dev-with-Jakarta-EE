# ショッピングカートサービス

スキー用品Eコマースプラットフォームでショッピングカート機能を管理するQuarkusベースのマイクロサービスです。

## 機能

- **カート管理**: ショッピングカートの作成、更新、管理
- **商品操作**: 商品の追加、削除、数量更新
- **セッションサポート**: セッション追跡によるゲストカート管理
- **カートマージ**: ログイン時のゲストカートとユーザーカートの統合
- **リアルタイム更新**: ライブカート更新用WebSocketサポート
- **キャッシュ**: パフォーマンス向上のためのRedisベースキャッシュ
- **イベント駆動**: マイクロサービス通信用Kafka統合
- **レジリエンス**: サーキットブレーカー、リトライ、フォールバックパターン
- **監視**: Prometheusメトリクスと分散トレーシング

## 技術スタック

- **フレームワーク**: Quarkus 3.15.1（Jakarta EE 11対応）
- **ランタイム**: Java 21 LTS
- **データベース**: PostgreSQL 16（Flyway マイグレーション）
- **キャッシュ**: Redis 7.2
- **メッセージング**: Apache Kafka
- **監視**: Prometheus、Jaeger トレーシング
- **API**: OpenAPI文書付きREST
- **リアルタイム**: WebSocket エンドポイント

## クイックスタート

### 前提条件

- Java 21 LTS
- Maven 3.9+
- Docker と Docker Compose

### 開発モード

1. インフラストラクチャサービスを開始：

```bash
docker-compose up -d postgres redis kafka zookeeper
```

1. 開発モードでアプリケーションを実行：

```bash
./mvnw quarkus:dev
```

サービスは以下で利用可能です：

- **API**: <http://localhost:8088>
- **Swagger UI**: <http://localhost:8088/swagger-ui>
- **ヘルスチェック**: <http://localhost:8088/q/health>
- **メトリクス**: <http://localhost:8088/metrics>

### 本番モード

1. Docker Composeでビルドして実行：

```bash
docker-compose up --build
```

1. またはJARファイルをビルドして実行：

```bash
./mvnw clean package
java -jar target/shopping-cart-service-1.0.0-SNAPSHOT-runner.jar
```

## APIエンドポイント

### カート管理

- `GET /api/v1/carts/{cartId}` - IDでカートを取得
- `GET /api/v1/carts/session/{sessionId}` - セッションでカートを取得/作成
- `GET /api/v1/carts/customer/{customerId}` - 顧客でカートを取得/作成

### 商品操作

- `POST /api/v1/carts/{cartId}/items` - カートに商品を追加
- `PUT /api/v1/carts/{cartId}/items/{sku}/quantity` - 商品数量を更新
- `DELETE /api/v1/carts/{cartId}/items/{sku}` - 商品を削除
- `DELETE /api/v1/carts/{cartId}/items` - カートをクリア

### 高度な操作

- `POST /api/v1/carts/{guestCartId}/merge/{customerId}` - ゲストカートをマージ
- `POST /api/v1/carts/{cartId}/validate` - カートを検証

### WebSocket

- `WS /api/v1/carts/ws/{cartId}` - リアルタイムカート更新

## 設定

`application.properties`の主要設定プロパティ：

```properties
# データベース
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5433/cartdb
quarkus.datasource.username=cartuser
quarkus.datasource.password=cartpass

# Redis
quarkus.redis.hosts=redis://localhost:6379

# Kafka
kafka.bootstrap.servers=localhost:9092

# 外部サービス
quarkus.rest-client.product-catalog-service.url=http://localhost:8081
quarkus.rest-client.inventory-management-service.url=http://localhost:8082
```

## データベーススキーマ

サービスは以下の主要テーブルでPostgreSQLを使用します：

- `shopping_carts` - カートメタデータと合計
- `cart_items` - 個別カート商品
- `applied_coupons` - 適用された割引とクーポン
- `cart_events` - カートアクティビティのイベントソーシング

マイグレーションはFlywayで管理され、`src/main/resources/db/migration/`に配置されています。

## イベント統合

### パブリッシュされるイベント

- `CartCreatedEvent` - 新しいカートが作成された時
- `CartItemAddedEvent` - 商品が追加された時
- `CartItemRemovedEvent` - 商品が削除された時
- `CartItemQuantityUpdatedEvent` - 数量が変更された時
- `CartMergedEvent` - カートがマージされた時
- `CartCheckedOutEvent` - チェックアウトが開始された時

### 消費されるイベント

- プロダクトカタログサービスからの商品価格更新
- 在庫管理サービスからの在庫変更
- ユーザー管理サービスからのユーザープロファイル更新

## 監視と可観測性

### ヘルスチェック

- **Liveness**: `/q/health/live`
- **Readiness**: `/q/health/ready`

### メトリクス

- **Prometheus**: `/metrics`
- カート固有のカスタムメトリクス：
  - カート作成率
  - 商品追加/削除率
  - キャッシュヒット率
  - WebSocket接続数

### 分散トレーシング

- **Jaeger**: すべてのリクエストの自動トレース収集
- 外部サービス呼び出し用のカスタムスパン
- マイクロサービス間の相関関係

## キャッシュ戦略

Redisは以下の用途で使用されます：

- **カートサマリー**: 頻繁にアクセスされるカートデータ（1時間TTL）
- **セッションマッピング**: ゲストカートからセッションへのマッピング（8時間TTL）
- **一時操作**: 同時操作ロック（1分TTL）

## レジリエンスパターン

- **サーキットブレーカー**: 外部サービス障害からの保護
- **リトライ**: 一時的障害の自動リトライ
- **フォールバック**: サービス利用不可時の適切な機能低下
- **タイムアウト**: ハングリクエストの防止

## テスト

### 単体テスト

```bash
./mvnw test
```

### 統合テスト

```bash
./mvnw verify
```

### TestContainers

統合テストは以下のTestContainersを使用：

- PostgreSQL データベース
- Redis キャッシュ
- Kafka メッセージング

## コンテナデプロイメント

### Docker ビルド

```bash
docker build -t shopping-cart-service .
```

### ネイティブビルド（GraalVM）

```bash
./mvnw package -Pnative -Dquarkus.native.container-build=true
docker build -f Dockerfile.native -t shopping-cart-service:native .
```

## 外部サービス統合

サービスは以下と統合します：

1. **プロダクトカタログサービス**（port 8081）
   - 商品検証と価格設定
   - 商品在庫状況チェック

2. **在庫管理サービス**（port 8082）
   - 在庫状況確認
   - 在庫予約

3. **ユーザー管理サービス**（port 8083）
   - ユーザー認証と認可
   - ユーザープロファイル情報

4. **APIゲートウェイサービス**（port 8080）
   - リクエストルーティングとロードバランシング
   - 認証とレート制限

## 開発

### 開発モード機能

- **ライブリロード**: コード変更時の自動再起動
- **開発UI**: `/q/dev`で利用可能
- **H2コンソール**: 開発モードでのデータベース検査
- **デバッグログ**: 開発用の詳細ログ

### コード構造

```text
src/main/java/com/skishop/cart/
├── entity/          # JPA エンティティ
├── dto/             # データ転送オブジェクト
├── resource/        # REST エンドポイント
├── service/         # ビジネスロジック
├── event/           # イベント定義
├── exception/       # カスタム例外
└── health/          # ヘルスチェック実装
```

## トラブルシューティング

### よくある問題

1. **データベース接続問題**
   - PostgreSQLが動作していることを確認
   - 接続パラメータをチェック
   - Flywayマイグレーションログを確認

2. **Redis接続問題**
   - Redisがアクセス可能であることを確認
   - Redis設定をチェック
   - ネットワーク接続を確認

3. **Kafka問題**
   - KafkaとZookeeperが動作していることを確認
   - トピック作成をチェック
   - コンシューマーグループ設定を確認

### ログ

アプリケーションログには以下が含まれます：

- リクエスト/レスポンス詳細
- キャッシュ操作
- イベントパブリッシュ/消費
- 外部サービス呼び出し
- 相関IDを含むエラートレース

## パフォーマンス考慮事項

- **非同期処理**: ノンブロッキング操作用のCompletableFuture
- **コネクションプール**: データベースとRedis接続の最適化
- **キャッシュ戦略**: 頻繁にアクセスされるデータの多層キャッシュ
- **イベントストリーミング**: 疎結合でスケーラブルな通信用Kafka
- **リソース制限**: JVMとコンテナの制限設定

## セキュリティ

- **JWT認証**: Bearerトークン認証のサポート
- **入力検証**: すべてのリクエストパラメータのJakarta Validation
- **SQLインジェクション防止**: JPAでのパラメータ化クエリ
- **CORSサポート**: 設定可能なクロスオリジンリソース共有

## 今後の拡張

- [ ] カート推奨エンジン統合
- [ ] 高度なキャッシュ戦略（マルチリージョン）
- [ ] カート放棄予測の機械学習
- [ ] 拡張リアルタイムアナリティクス
- [ ] GraphQL API サポート
- [ ] 高度なセキュリティ機能（OAuth2、レート制限）
