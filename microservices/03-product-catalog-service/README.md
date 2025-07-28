# Product Catalog Service

スキー用品ショップの商品カタログ管理サービス

## 概要

Product Catalog Serviceは、スキー用品販売ショップサイトの商品カタログ管理を担当するマイクロサービスです。

### 主要機能

- 商品情報の管理（登録、更新、削除）
- カテゴリとブランドの階層管理
- 商品検索とフィルタリング
- 商品詳細情報の提供
- 価格管理

## 技術仕様

- **Java**: 21 LTS
- **Framework**: Quarkus 3.8.1
- **Database**: PostgreSQL 16
- **Build Tool**: Maven
- **Container**: Docker

## 開発環境セットアップ

### 前提条件

- Java 21
- Docker & Docker Compose
- Maven 3.9+

### 起動方法

#### 1. Docker Composeでの起動

```bash
# プロジェクトルートで実行
docker-compose up -d

# ログの確認
docker-compose logs -f product-catalog-service
```

#### 2. ローカル開発モード

```bash
# PostgreSQLのみDocker Composeで起動
docker-compose up -d postgres

# Quarkus開発モードで起動
./mvnw quarkus:dev
```

### アクセス情報

- **API**: http://localhost:8083
- **OpenAPI UI**: http://localhost:8083/q/swagger-ui/
- **Health Check**: http://localhost:8083/q/health
- **Metrics**: http://localhost:8083/q/metrics

## API エンドポイント

### 商品管理

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/products` | 商品一覧・検索 |
| GET | `/api/v1/products/{id}` | 商品詳細取得 |
| POST | `/api/v1/products` | 商品登録 |
| PUT | `/api/v1/products/{id}` | 商品更新 |
| DELETE | `/api/v1/products/{id}` | 商品削除 |

### カテゴリ管理

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/categories` | カテゴリ一覧取得 |
| GET | `/api/v1/categories/{id}` | カテゴリ詳細取得 |
| POST | `/api/v1/categories` | カテゴリ登録 |
| PUT | `/api/v1/categories/{id}` | カテゴリ更新 |

### ブランド管理

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/brands` | ブランド一覧取得 |
| GET | `/api/v1/brands/{id}` | ブランド詳細取得 |
| POST | `/api/v1/brands` | ブランド登録 |

## テスト実行

```bash
# 単体テスト
mvn test

# 統合テスト
mvn verify

# テストカバレッジレポート生成
mvn jacoco:report
```

## ビルド・デプロイ

### JARファイル生成

```bash
mvn clean package
```

### Docker イメージビルド

```bash
mvn clean package -Dquarkus.container-image.build=true
```

### Native イメージビルド

```bash
mvn clean package -Pnative
```

## 設定

アプリケーション設定は `src/main/resources/application.yml` で管理されています。

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `QUARKUS_DATASOURCE_JDBC_URL` | PostgreSQL接続URL | `jdbc:postgresql://localhost:5432/product_catalog` |
| `QUARKUS_DATASOURCE_USERNAME` | DB ユーザー名 | `postgres` |
| `QUARKUS_DATASOURCE_PASSWORD` | DB パスワード | `postgres` |
| `QUARKUS_HTTP_PORT` | HTTP ポート | `8083` |

## ライセンス

MIT License
