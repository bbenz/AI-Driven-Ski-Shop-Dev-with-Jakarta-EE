# Authentication Service

認証サービス（Authentication Service）は、スキーリゾート管理システムの中央認証を担当するマイクロサービスです。

## 概要

このサービスは以下の認証機能を提供します：

- **ローカル認証**: ユーザー名/パスワードによる認証
- **OAuth2認証**: Google、Facebook、Twitterによるソーシャル認証
- **多要素認証（MFA）**: SMS、Email、TOTPによる追加認証
- **JWT トークン管理**: アクセストークンとリフレッシュトークンの発行・検証
- **パスワード管理**: パスワードリセット、強度チェック
- **アカウント管理**: アカウントロック、メール確認

## 技術スタック

- **Jakarta EE 11**: エンタープライズJavaフレームワーク
- **Java 21 LTS**: プログラミング言語
- **WildFly 31.0.1**: アプリケーションサーバー
- **PostgreSQL**: メインデータベース
- **Redis**: トークンキャッシュ・セッション管理
- **JWT**: JSON Web Token実装（Nimbus JOSE + JWT）
- **BCrypt**: パスワードハッシュ化
- **MicroProfile Config**: 設定管理
- **MicroProfile JWT**: JWT認証

## アーキテクチャ

```
┌─────────────────────────────────────────────────────────┐
│                Authentication Service                    │
├─────────────────────────────────────────────────────────┤
│  REST Layer (JAX-RS)                                   │
│  ├─ AuthenticationResource                              │
│  └─ Exception Handlers                                  │
├─────────────────────────────────────────────────────────┤
│  Service Layer                                          │
│  ├─ AuthenticationService                               │
│  ├─ JwtService                                          │
│  └─ OAuth2Service                                       │
├─────────────────────────────────────────────────────────┤
│  Repository Layer                                       │
│  ├─ UserCredentialRepository                            │
│  └─ OAuth2CredentialRepository                          │
├─────────────────────────────────────────────────────────┤
│  Entity Layer (JPA)                                     │
│  ├─ UserCredential                                      │
│  └─ OAuth2Credential                                    │
└─────────────────────────────────────────────────────────┘
```

## エンティティ設計

### UserCredential (ユーザー認証情報)
- ユーザーID、ユーザー名、メール、パスワード
- MFA設定（SMS、Email、TOTP）
- アカウントステータス（アクティブ、ロック、無効）
- パスワードリセット、メール確認トークン
- ログイン試行回数、最終ログイン時刻

### OAuth2Credential (OAuth2認証情報)
- OAuth2プロバイダー情報（Google、Facebook、Twitter）
- アクセストークン、リフレッシュトークン
- プロバイダーユーザーID、プロフィール情報
- トークン有効期限、最終同期時刻

## API エンドポイント

### 認証API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/auth/register` | ユーザー新規登録 |
| POST | `/auth/login` | ユーザー名/パスワード認証 |
| POST | `/auth/mfa/verify` | MFA コード検証 |
| POST | `/auth/oauth2/authenticate` | OAuth2 認証 |
| POST | `/auth/refresh` | トークンリフレッシュ |
| POST | `/auth/revoke` | トークン取り消し |

### パスワード管理API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| POST | `/auth/password/reset-request` | パスワードリセット要求 |
| POST | `/auth/password/reset` | パスワードリセット実行 |

### メール確認API

| メソッド | エンドポイント | 説明 |
|---------|---------------|------|
| GET | `/auth/email/verify` | メールアドレス確認 |

## セキュリティ機能

### パスワードセキュリティ
- BCryptによる強力なハッシュ化
- パスワード強度チェック（大文字、小文字、数字、特殊文字）
- パスワード履歴管理（再利用防止）

### アカウント保護
- ログイン試行回数制限（デフォルト5回）
- 一時的なアカウントロック（デフォルト30分）
- IPアドレスベースのレート制限

### 多要素認証（MFA）
- **SMS認証**: AWS SNS経由でSMSコード送信
- **Email認証**: SMTP経由でメールコード送信
- **TOTP認証**: Google Authenticatorなどのアプリ対応

### OAuth2セキュリティ
- PKCE (Proof Key for Code Exchange) サポート
- State パラメータによるCSRF攻撃防止
- スコープベースのアクセス制御

## JWT トークン設計

### アクセストークン
- **有効期限**: 15分（設定可能）
- **クレーム**: ユーザーID、ロール、権限
- **用途**: API認証

### リフレッシュトークン
- **有効期限**: 7日（設定可能）
- **クレーム**: ユーザーID、ロール
- **用途**: アクセストークン更新

## 設定

### 環境変数

| 変数名 | 説明 | デフォルト値 |
|--------|------|-------------|
| `JWT_SECRET` | JWT署名キー | `your-256-bit-secret-key-here-change-in-production` |
| `JWT_ACCESS_TOKEN_EXPIRATION` | アクセストークン有効期限 | `PT15M` |
| `JWT_REFRESH_TOKEN_EXPIRATION` | リフレッシュトークン有効期限 | `P7D` |
| `MAX_LOGIN_ATTEMPTS` | 最大ログイン試行回数 | `5` |
| `LOCKOUT_DURATION` | アカウントロック期間 | `PT30M` |
| `MFA_ENABLED` | MFA有効化 | `true` |
| `REDIS_HOST` | Redisホスト | `localhost` |
| `REDIS_PORT` | Redisポート | `6379` |

### OAuth2設定

各OAuth2プロバイダーの設定：

```properties
# Google OAuth2
oauth2.google.client-id=${OAUTH2_GOOGLE_CLIENT_ID}
oauth2.google.client-secret=${OAUTH2_GOOGLE_CLIENT_SECRET}

# Facebook OAuth2
oauth2.facebook.client-id=${OAUTH2_FACEBOOK_CLIENT_ID}
oauth2.facebook.client-secret=${OAUTH2_FACEBOOK_CLIENT_SECRET}

# Twitter OAuth2
oauth2.twitter.client-id=${OAUTH2_TWITTER_CLIENT_ID}
oauth2.twitter.client-secret=${OAUTH2_TWITTER_CLIENT_SECRET}
```

## データベース設定

### PostgreSQL設定

```sql
-- データベース作成
CREATE DATABASE ski_resort_auth;

-- ユーザー作成
CREATE USER auth_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ski_resort_auth TO auth_user;
```

### Redis設定

```
# redis.conf
maxmemory 256mb
maxmemory-policy allkeys-lru
```

## ビルドと実行

### 前提条件
- Java 21 LTS
- Maven 3.9+
- PostgreSQL 15+
- Redis 7+
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
cp target/authentication-service.war $WILDFLY_HOME/standalone/deployments/
```

### Docker実行

```bash
# Docker Compose で実行
docker-compose up authentication-service
```

## API使用例

### ユーザー登録

```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass123!"
  }'
```

### ログイン

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "SecurePass123!"
  }'
```

### OAuth2認証（Google）

```bash
curl -X POST http://localhost:8080/auth/oauth2/authenticate \
  -H "Content-Type: application/json" \
  -d '{
    "provider": "google",
    "authorizationCode": "authorization_code_from_google",
    "redirectUri": "http://localhost:3000/callback"
  }'
```

### トークンリフレッシュ

```bash
curl -X POST http://localhost:8080/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "your_refresh_token"
  }'
```

## 監視とロギング

### ヘルスチェック
- `/health` エンドポイントで サービス状態確認
- データベース接続状態
- Redis接続状態

### メトリクス
- 認証成功/失敗率
- MFA使用率
- OAuth2プロバイダー別統計
- レスポンス時間

### ログ
- 認証イベント（成功、失敗、MFA）
- セキュリティイベント（アカウントロック、不正アクセス）
- システムエラー

## セキュリティ考慮事項

### 本番環境での推奨設定

1. **JWT署名キーの変更**
   ```
   JWT_SECRET=your-strong-256-bit-secret-key-here
   ```

2. **HTTPS必須**
   - 全ての通信をHTTPS化
   - Secure Cookieの使用

3. **レート制限の強化**
   - IPアドレスベースの制限
   - ユーザーベースの制限

4. **監視の強化**
   - 異常なログインパターンの検知
   - セキュリティアラートの設定

## トラブルシューティング

### よくある問題

1. **JWT署名エラー**
   - JWT_SECRETが正しく設定されているか確認
   - 時刻同期の確認

2. **OAuth2認証失敗**
   - クライアントIDとシークレットの確認
   - リダイレクトURIの確認

3. **MFA失敗**
   - 時刻同期の確認（TOTP）
   - SMSプロバイダーの設定確認

## 今後の拡張予定

- [ ] WebAuthn/FIDO2サポート
- [ ] リスクベース認証
- [ ] シングルサインオン（SSO）
- [ ] セッション管理の改善
- [ ] AI/ML による不正検知

## 開発者向け情報

### コード構成
```
src/main/java/
├── com/skiresort/auth/
│   ├── entity/          # JPA エンティティ
│   ├── service/         # ビジネスロジック
│   ├── repository/      # データアクセス
│   ├── resource/        # REST エンドポイント
│   └── exception/       # 例外クラス
```

### 依存関係
- Jakarta EE 11 API
- MicroProfile Config
- MicroProfile JWT
- Nimbus JOSE + JWT
- BCrypt
- PostgreSQL JDBC
- Redis Client

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。
