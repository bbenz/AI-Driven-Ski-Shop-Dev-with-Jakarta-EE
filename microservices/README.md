# スキーリゾート管理システム - マイクロサービスアーキテクチャ

## 概要

Jakarta EE 11、Java 21 LTS、WildFly 31.0.1を使用して構築された包括的なマイクロサービス型スキーリゾート管理システムです。このシステムは、ユーザー管理、設備レンタル、リフト券、レッスン、決済、カスタマーサポートを含む、スキーリゾート運営のすべての側面を処理するように設計されています。

## アーキテクチャ

システムは以下のコンポーネントでマイクロサービスアーキテクチャパターンに従っています：

### 🚪 01. APIゲートウェイサービス (Port 8080)

- **目的**: すべてのクライアントリクエストの中央エントリーポイント
- **機能**: リクエストルーティング、認証、レート制限、CORS処理
- **技術**: Jakarta EE 11, JAX-RS, JWT, MicroProfile
- **エンドポイント**: パスプレフィックスに基づいてすべての下流サービスにルーティング

### 👥 02. ユーザー管理サービス (Port 8081)

- **目的**: ユーザー登録、プロファイル管理、認証データ
- **機能**: ユーザーCRUD、プロファイル管理、ロール管理、スキルレベル追跡
- **技術**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **エンティティ**: User, UserProfile, UserPreferences

### 🎿 03. プロダクトカタログサービス (Port 8082)

- **目的**: 設備カタログ、リフト券、レッスンパッケージ
- **機能**: 商品管理、価格設定、在庫状況、カテゴリ
- **技術**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **エンティティ**: Product, Category, PriceRule, ProductVariant

### 🔐 04. 認証サービス (Port 8083)

- **目的**: ユーザー認証、JWTトークン管理
- **機能**: ログイン/ログアウト、トークン更新、セッション管理
- **技術**: Jakarta EE 11, JWT, BCrypt, セッションストレージ用Redis
- **機能**: 多要素認証、パスワードリセット、アカウント確認

### 📦 05. 在庫管理サービス (Port 8084)

- **目的**: 設備在庫、可用性追跡
- **機能**: 在庫管理、予約、メンテナンス追跡
- **技術**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **エンティティ**: Equipment, InventoryItem, Reservation, MaintenanceRecord

### 🛒 06. 注文管理サービス (Port 8085)

- **目的**: 注文処理、予約管理
- **機能**: 注文ライフサイクル、予約確認、キャンセル
- **技術**: Jakarta EE 11, JPA/Hibernate, イベントソーシング
- **エンティティ**: Order, OrderItem, Booking, Reservation

### 💳 07. 決済サービス (Port 8086)

- **目的**: 決済処理、取引管理
- **機能**: 複数決済方法、返金、決済追跡
- **技術**: Jakarta EE 11, 決済ゲートウェイ統合, PCI準拠
- **エンティティ**: Payment, Transaction, PaymentMethod, Refund

### 🛍️ 08. ショッピングカートサービス (Port 8087)

- **目的**: ショッピングカート管理、セッション処理
- **機能**: カート永続化、アイテム管理、価格計算
- **技術**: Jakarta EE 11, カートストレージ用Redis, リアルタイム更新
- **機能**: カート共有、保存済みカート、自動クリーンアップ

### 🎫 09. クーポン・割引サービス (Port 8088)

- **目的**: プロモーションコード、割引管理
- **機能**: クーポン検証、割引計算、使用状況追跡
- **技術**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **エンティティ**: Coupon, Discount, PromotionRule, UsageRecord

### ⭐ 10. ポイント・ロイヤルティサービス (Port 8089)

- **目的**: 顧客ロイヤルティプログラム、ポイント管理
- **機能**: ポイント獲得、償還、ティア管理
- **技術**: Jakarta EE 11, JPA/Hibernate, PostgreSQL
- **エンティティ**: LoyaltyAccount, PointsTransaction, LoyaltyTier

### 🤖 11. AIサポートサービス (Port 8090)

- **目的**: インテリジェントカスタマーサポート、チャットボット
- **機能**: 自然言語処理、自動応答、エスカレーション
- **技術**: Jakarta EE 11, AI/ML統合, リアルタイムチャット用WebSocket
- **機能**: 多言語サポート、感情分析

### 🖥️ 12. フロントエンドサービス (Port 8091)

- **目的**: Webフロントエンド、顧客・管理者インターフェース
- **機能**: レスポンシブUI、リアルタイム更新、PWA機能
- **技術**: モダンWeb技術、WebSocket、Service Worker
- **インターフェース**: 顧客ポータル、管理ダッシュボード、スタッフインターフェース

## 技術スタック

### コア技術

- **Java 21 LTS** - 仮想スレッドとモダン言語機能
- **Jakarta EE 11** - エンタープライズ機能
- **WildFly 31.0.1** - アプリケーションサーバー
- **MicroProfile 6.1** - マイクロサービス仕様

### データベース・永続化

- **PostgreSQL** - プライマリデータストレージ
- **Hibernate 6.4.1** - ORM
- **Redis** - キャッシュとセッションストレージ
- **H2** - テスト用

### セキュリティ

- **JWT** - 認証トークン
- **BCrypt** - パスワードハッシュ化
- **HTTPS/TLS** - セキュア通信
- **OAuth2/OpenID Connect** - 外部認証

### 通信

- **JAX-RS** - REST API
- **WebSocket** - リアルタイム通信
- **JMS** - 非同期メッセージング
- **gRPC** - 高性能サービス間通信

### 監視・可観測性

- **MicroProfile Health** - ヘルスチェック
- **MicroProfile Metrics** - アプリケーションメトリクス
- **MicroProfile OpenTracing** - 分散トレーシング
- **構造化ログ** - JSON形式

## はじめに

### 前提条件

- Java 21 LTS
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
- WildFly 31.0.1

### プロジェクトのビルド

```bash
# リポジトリをクローン
git clone https://github.com/jakartaone2025/ski-resort-system.git
cd ski-resort-system/microservices

# すべてのサービスをビルド
mvn clean compile

# テスト実行
mvn test

# すべてのサービスをパッケージ化
mvn package
```

### サービスの実行

#### オプション1: 個別サービス

```bash
# APIゲートウェイ
cd 01-api-gateway-service
mvn wildfly:deploy

# ユーザー管理
cd 02-user-management-service
mvn wildfly:deploy
```

#### オプション2: Docker Compose

```bash
# 依存関係を含むすべてのサービスを開始
docker-compose up -d
```

### サービスURL

- APIゲートウェイ: <http://localhost:8080>
- ユーザー管理: <http://localhost:8081>
- プロダクトカタログ: <http://localhost:8083>
- 認証: <http://localhost:8084>
- 在庫管理: <http://localhost:8085>
- 注文管理: <http://localhost:8086>
- 決済サービス: <http://localhost:8087>
- ショッピングカート: <http://localhost:8088>
- クーポン・割引: <http://localhost:8089>
- ポイント・ロイヤルティ: <http://localhost:8090>
- AIサポート: <http://localhost:8091>
- フロントエンド: <http://localhost:8092>

## API文書

各サービスは以下でOpenAPI文書を提供しています：

- `http://localhost:<port>/<service-name>/api/openapi`

## 設定

設定はMicroProfile Configで管理されています：

- **環境変数** - 本番デプロイ用
- **プロパティファイル** - 開発用
- **Kubernetes ConfigMaps** - コンテナ環境用

## データベーススキーマ

各サービスは独自のデータベーススキーマを管理しています：

- **ユーザー管理**: ユーザープロファイル、認証データ
- **プロダクトカタログ**: 商品、カテゴリ、価格設定
- **在庫**: 設備、在庫レベル、予約
- **注文**: 注文履歴、予約、フルフィルメント
- **決済**: 取引、決済方法
- **ロイヤルティ**: ポイント、ティア、報酬

## セキュリティ・認証

### 認証フロー

1. ユーザーが認証サービス経由で認証
2. ユーザークレーム付きJWTトークンを発行
3. APIゲートウェイがすべてのリクエストでトークンを検証
4. サービス間通信はJWT転送を使用

### 認可

- **ロールベースアクセス制御（RBAC）**
- **リソースレベル権限**
- **サービス間通信用APIキー認証**

## 監視

### ヘルスチェック

- `/health` - MicroProfile Healthエンドポイント
- `/health/live` - Livenessプローブ
- `/health/ready` - Readinessプローブ

### メトリクス

- `/metrics` - Prometheus互換メトリクス
- 各サービス用カスタムビジネスメトリクス

### ログ

- 構造化JSONログ
- リクエストトレーシング用相関ID
- 集約型ログ収集

## 開発ガイドライン

### コーディング規約

- Java 21ベストプラクティス
- フィールドインジェクションよりコンストラクターインジェクション
- 包括的エラーハンドリング
- 単体・統合テスト

### テスト戦略

- **単体テスト** - JUnit 5とMockito
- **統合テスト** - TestContainers
- **契約テスト** - Pact
- **E2Eテスト** - REST Assured

### CI/CDパイプライン

- **ビルド**: Mavenコンパイルとテスト
- **品質**: SonarQubeコード解析
- **セキュリティ**: OWASP依存関係スキャン
- **デプロイ**: ブルーグリーンデプロイ戦略

## スケーラビリティ・パフォーマンス

### 水平スケーリング

- ステートレスサービス設計
- データベースコネクションプール
- HAProxy/NGINXでのロードバランシング

### キャッシュ戦略

- **Redis** - セッションと頻繁にアクセスされるデータ
- **CDN** - 静的コンテンツ
- **アプリケーションレベル** - Caffeineでのキャッシュ

### パフォーマンス最適化

- **Java 21仮想スレッド** - 同時実行性向上
- **コネクションプール** - データベース効率化
- **非同期処理** - 長時間実行オペレーション

## デプロイ

### コンテナ戦略

- **Docker** - 各サービス用コンテナ
- **マルチステージビルド** - 最適化されたイメージ
- **ヘルスチェック** - コンテナに統合

### Kubernetes

- **Helmチャート** - デプロイテンプレート
- **ConfigMaps** - 設定管理
- **Secrets** - 機密データ
- **Ingress** - 外部アクセス

### 本番環境での監視

- **Prometheus** - メトリクス収集
- **Grafana** - ダッシュボード
- **ELKスタック** - ログ解析
- **Jaeger** - 分散トレーシング

## 貢献方法

1. リポジトリをフォーク
2. フィーチャーブランチを作成
3. テスト付きで変更を実装
4. プルリクエストを提出

## ライセンス

このプロジェクトはMITライセンスの下でライセンスされています - 詳細はLICENSEファイルを参照してください。

## サポート

質問やサポートについては：

- **文書**: [Wiki](https://github.com/jakartaone2025/ski-resort-system/wiki)
- **Issues**: [GitHub Issues](https://github.com/jakartaone2025/ski-resort-system/issues)
- **ディスカッション**: [GitHub Discussions](https://github.com/jakartaone2025/ski-resort-system/discussions)
