# APIゲートウェイサービス

これはスキーリゾート管理システムの中央APIゲートウェイです。すべてのマイクロサービスに対して、ルーティング、認証、レート制限、その他の横断的関心事を提供します。

## 機能

- **リクエストルーティング**: 受信リクエストを適切な下流マイクロサービスにルーティング
- **認証**: JWTトークン検証とユーザーコンテキスト抽出
- **レート制限**: 設定可能な制限によるクライアント毎のリクエストレート制限
- **ヘルス監視**: すべての下流サービスのヘルスチェック
- **CORSサポート**: クロスオリジンリソース共有設定
- **サービス発見**: サービス可用性に基づく動的ルーティング

## 技術スタック

- Jakarta EE 11
- Java 21 LTS（仮想スレッド対応）
- WildFly 31.0.1 アプリケーションサーバー
- MicroProfile 6.1（Config, Health, Metrics, OpenAPI）
- JWT（JSON Web Tokens）認証
- Maven ビルド管理

## 設定

設定は`META-INF/microprofile-config.properties`のMicroProfile Configで管理されています：

```properties
# サーバー設定
server.host=0.0.0.0
server.port=8080

# JWT設定
jwt.secret=your-256-bit-secret-key
jwt.expiration.hours=24

# レート制限
gateway.ratelimit.default.requests=100
gateway.ratelimit.auth.requests=200
gateway.ratelimit.window.seconds=60

# サービスURL
services.user.url=http://localhost:8081
services.product.url=http://localhost:8082
# ... その他のサービス
```

## APIルート

ゲートウェイはパスプレフィックスに基づいてリクエストをルーティングします：

- `/users/*` → ユーザー管理サービス (port 8081)
- `/api/v1/products/*` → プロダクトカタログサービス (port 8083)
- `/api/v1/categories/*` → プロダクトカタログサービス (port 8083)
- `/products/*` → プロダクトカタログサービス (port 8083)（非推奨パス）
- `/categories/*` → プロダクトカタログサービス (port 8083)（非推奨パス）
- `/auth/*` → 認証サービス (port 8084)
- `/inventory/*`, `/api/v1/inventory/*` → 在庫管理サービス (port 8085)
- `/orders/*` → 注文管理サービス (port 8086)
- `/payments/*` → 決済サービス (port 8087)
- `/cart/*` → ショッピングカートサービス (port 8088)
- `/coupons/*`, `/discounts/*` → クーポン・割引サービス (port 8089)
- `/points/*`, `/loyalty/*` → ポイント・ロイヤルティサービス (port 8090)
- `/ai/*`, `/support/*` → AIサポートサービス (port 8091)

## レート制限

レート制限は以下の機能で実装されています：

- クライアント毎の追跡（ユーザーIDまたはIPアドレス）
- エンドポイントカテゴリ毎の設定可能な制限
- スライディングウィンドウアルゴリズム
- レスポンスのレート制限ヘッダー

## 認証

JWT認証は以下を含みます：

- 保護されたエンドポイントでのトークン検証
- ユーザーコンテキスト抽出と転送
- パブリックエンドポイント（ヘルスチェック、認証エンドポイント）
- 下流サービスへの認証ヘッダー転送

## プロダクトカタログサービスエンドポイント

APIゲートウェイは以下のプロダクトカタログサービスエンドポイントをルーティングします：

### カテゴリ管理（10エンドポイント）

- `GET /api/v1/categories` - 全カテゴリ一覧取得
- `GET /api/v1/categories/root` - ルートカテゴリ一覧取得
- `GET /api/v1/categories/main` - メインカテゴリ一覧取得
- `GET /api/v1/categories/path` - パスでカテゴリ取得
- `GET /api/v1/categories/{categoryId}` - カテゴリ詳細取得
- `GET /api/v1/categories/{categoryId}/children` - 子カテゴリ一覧取得
- `GET /api/v1/categories/level/{level}` - レベル別カテゴリ取得
- `GET /api/v1/categories/{categoryId}/subcategories` - サブカテゴリ一覧取得
- `GET /api/v1/categories/{categoryId}/products` - カテゴリの商品一覧取得
- `GET /api/v1/categories/{categoryId}/subcategories/products` - サブカテゴリ毎の商品一覧取得

### 商品管理（9エンドポイント）

- `GET /api/v1/products` - 商品一覧・検索
- `GET /api/v1/products/featured` - 注目商品一覧
- `GET /api/v1/products/{productId}` - 商品詳細取得
- `GET /api/v1/products/sku/{sku}` - SKUで商品取得
- `GET /api/v1/products/category/{categoryId}` - カテゴリ別商品一覧
- `GET /api/v1/products/brand/{brandId}` - ブランド別商品一覧
- `POST /api/v1/products` - 商品登録
- `PUT /api/v1/products/{productId}` - 商品更新
- `DELETE /api/v1/products/{productId}` - 商品削除

### システムエンドポイント

- `GET /q/health` - ヘルスチェック
- `GET /q/openapi` - OpenAPI仕様書取得

## ヘルスチェック

複数のヘルスチェックエンドポイント：

- `/health` - MicroProfile Healthチェック
- `/health/services` - すべての下流サービスの詳細ステータス
- LivenessとReadinessプローブ

## ビルドと実行

```bash
# プロジェクトをビルド
mvn clean compile

# テスト実行
mvn test

# WARファイルをパッケージ化
mvn package

# WildFlyにデプロイ
mvn wildfly:deploy
```

## 開発

サービスには包括的な単体テストが含まれており、Jakarta EEのベストプラクティスに従っています：

- フィールドインジェクションではなくコンストラクターインジェクション
- 適切な例外処理とログ記録
- 設定可能なタイムアウトとリトライロジック
- 関心事の明確な分離

## 監視

ゲートウェイは以下による メトリクスと監視を提供します：

- MicroProfile Metrics
- 構造化ログ
- サービスヘルス追跡
- リクエスト/レスポンス時間測定

## セキュリティ考慮事項

- JWTシークレットキーは本番環境で変更必須
- 本番環境ではHTTPSを強制すべき
- レート制限により悪用を防止
- プロキシされるすべてのリクエストでの入力検証
- Webクライアント用の適切なCORS設定
