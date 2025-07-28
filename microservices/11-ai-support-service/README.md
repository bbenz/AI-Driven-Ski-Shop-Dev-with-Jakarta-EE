# AI Support Service

LangChain4j 1.1.0 + Azure OpenAI を使用したAIサポートサービス

## 概要

このサービスは、Quarkus フレームワークを使用して構築されたAIサポートマイクロサービスです。
LangChain4j 1.1.0 と Azure OpenAI (GPT-4o) を統合し、以下の機能を提供します：

- **チャットボット機能**: 顧客サポート、商品推薦、技術的アドバイス
- **商品推薦システム**: プロファイルベース、行動ベース、協調フィルタリング
- **検索機能拡張**: クエリ拡張、同義語展開、結果再ランキング

## 技術スタック

- **フレームワーク**: Quarkus 3.15.1
- **言語**: Java 21 LTS (プレビュー機能有効)
- **AI統合**: LangChain4j 1.1.0
- **AIプロバイダー**: Azure OpenAI GPT-4o
- **コンテナ**: Docker & Docker Compose

## Java 21 機能の活用

本プロジェクトでは、最新のJava 21機能を積極的に活用しています：

### 1. Records (データ転送オブジェクト)
```java
public record ChatMessageRequest(
    @NotBlank String userId,
    @NotBlank String content,
    String conversationId,
    String sessionId,
    Map<String, Object> context
) {
    // バリデーションと変換のファクトリーメソッド付き
}
```

### 2. Sealed Classes/Interfaces (型安全な階層)
```java
public sealed interface Intent 
    permits Intent.ProductRecommendation, Intent.TechnicalAdvice, 
            Intent.OrderSupport, Intent.GeneralInquiry {
    
    record ProductRecommendation(String category, String preferences) implements Intent {}
    record TechnicalAdvice(String topic, String skillLevel) implements Intent {}
    // ...
}
```

### 3. Switch Expressions (パターンマッチング)
```java
return switch (intent) {
    case Intent.ProductRecommendation(var category, var preferences) -> 
        productRecommendationAssistant.recommend(message, category, preferences);
    case Intent.TechnicalAdvice(var topic, var skillLevel) -> 
        customerSupportAssistant.provideTechnicalAdvice(message, topic, skillLevel);
    // ...
};
```

### 4. Text Blocks (複数行文字列)
```java
String systemMessage = """
    あなたは親しみやすいスキーショップの店員です。
    以下の点に注意して回答してください：
    - 丁寧で親しみやすい口調
    - 具体的で実践的なアドバイス
    - 安全性を最優先に考慮
    """;
```

## API エンドポイント

### チャット API
- `POST /api/v1/chat/message` - 一般的なチャットメッセージ処理
- `POST /api/v1/chat/recommend` - 商品推薦特化チャット
- `POST /api/v1/chat/advice` - 技術的アドバイス特化チャット
- `GET /api/v1/chat/conversations/{userId}` - 会話履歴取得
- `DELETE /api/v1/chat/conversations/{conversationId}` - 会話削除

### 推薦 API
- `POST /api/v1/recommendations/profile-based` - プロファイルベース推薦
- `POST /api/v1/recommendations/behavior-based` - 行動ベース推薦
- `POST /api/v1/recommendations/collaborative` - 協調フィルタリング推薦
- `POST /api/v1/recommendations/bundle` - 商品組み合わせ推薦

### 検索拡張 API
- `POST /api/v1/search/enhance-query` - クエリ拡張
- `POST /api/v1/search/expand-synonyms` - 同義語展開
- `POST /api/v1/search/rerank` - 検索結果再ランキング
- `POST /api/v1/search/analyze-intent` - 検索意図分析
- `POST /api/v1/search/personalized` - パーソナライズド検索

## 設定

### Azure OpenAI 設定
```yaml
azure:
  openai:
    endpoint: ${AZURE_OPENAI_ENDPOINT:https://your-openai-resource.openai.azure.com/}
    api-key: ${AZURE_OPENAI_API_KEY:your-api-key}
    deployment:
      chat: ${AZURE_OPENAI_CHAT_DEPLOYMENT:gpt-4o}
      embedding: ${AZURE_OPENAI_EMBEDDING_DEPLOYMENT:text-embedding-3-small}
```

### データベース設定
```yaml
quarkus:
  datasource:
    db-kind: postgresql
    username: ${DB_USERNAME:ai_support}
    password: ${DB_PASSWORD:password}
    jdbc:
      url: ${DB_URL:jdbc:postgresql://localhost:5432/ai_support_db}
```

## 実行方法

### Docker環境での起動（推奨）

#### 1. 環境変数設定

```bash
# 環境変数テンプレートをコピー
cp .env.template .env

# .envファイルを編集してAzure OpenAI設定を入力
vim .env
```

.envファイルの設定例：

```bash
AZURE_OPENAI_ENDPOINT=https://your-resource-name.openai.azure.com/
AZURE_OPENAI_API_KEY=your-api-key-here
AZURE_OPENAI_CHAT_DEPLOYMENT_NAME=gpt-4o
AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME=text-embedding-3-large
```

#### 2. サービス起動

**簡単起動（推奨）**:

```bash
# 起動スクリプトを使用（自動でヘルスチェックまで実行）
./run-docker.sh
```

**手動起動**:

```bash
# バックグラウンドで起動
docker-compose up -d

# ログを確認
docker-compose logs -f ai-support-service

# ヘルスチェック
curl http://localhost:8091/q/health
```

#### 3. 動作確認

```bash
# API ドキュメント（Swagger UI）
open http://localhost:8091/q/swagger-ui

# チャット機能テスト
curl -X POST http://localhost:8091/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "content": "スキー板のおすすめを教えてください",
    "conversationId": "conv456"
  }'
```

#### 4. サービス停止

```bash
# サービス停止
docker-compose down

# ボリュームも削除（完全クリーンアップ）
docker-compose down -v
```

### ローカル開発環境での起動

#### 必要条件

- Java 21 LTS (プレビュー機能有効)
- Maven 3.9以上
- Azure OpenAIサービス（APIキーとエンドポイント）

## Java 21 機能の活用

このサービスはJava 21の最新機能を活用して実装されています：

- **Record Patterns**: 不変データ構造とパターンマッチング
- **Sealed Classes**: 型安全な階層構造
- **Switch Expressions**: 強化されたswitch文とパターンマッチング
- **Text Blocks**: 複数行文字列のクリーンな記述
- **Virtual Threads**: 高性能な並行処理（Quarkusで有効）

#### 環境変数の設定

```bash
export AZURE_OPENAI_ENDPOINT="https://your-resource-name.openai.azure.com/"
export AZURE_OPENAI_API_KEY="your-api-key-here"
export AZURE_OPENAI_CHAT_DEPLOYMENT_NAME="gpt-4o"
export AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME="text-embedding-3-large"
```

### 開発モード

```bash
./mvnw compile quarkus:dev
```

### 本番ビルド

```bash
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

### ネイティブビルド

```bash
./mvnw package -Dnative
./target/ai-support-service-1.0.0-SNAPSHOT-runner
```

## 検証方法

本プロジェクトの機能は以下の手順で検証できます。

### 前提条件

Azure OpenAIの環境変数を設定してください：

```bash
export AZURE_OPENAI_API_KEY="your-api-key"
export AZURE_OPENAI_ENDPOINT="https://your-openai-resource.cognitiveservices.azure.com/"
export AZURE_OPENAI_CHAT_DEPLOYMENT_NAME="gpt-4o"
export AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME="text-embedding-3-small"
```

### 1. アプリケーション起動

簡易モード（データベース・外部サービス不要）で起動：

```bash
mvn quarkus:dev -DskipTests=true -Dquarkus.http.port=8091
```

### 2. ヘルスチェック

アプリケーションの正常性を確認：

```bash
curl http://localhost:8091/q/health
```

期待される結果：`"status": "UP"`

### 3. チャット機能のテスト

一般的なチャット機能をテスト：

```bash
curl -X POST http://localhost:8091/api/v1/chat/message \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "content": "こんにちは！スキーについて教えてください。",
    "conversationId": "test-conversation"
  }'
```

期待される結果：Azure OpenAIからの日本語レスポンス

### 4. 商品推薦機能のテスト

商品推薦特化チャットをテスト：

```bash
curl -X POST http://localhost:8091/api/v1/chat/recommend \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "content": "初心者向けのスキー板を推薦してください。身長170cm、スキー経験はほぼありません。",
    "conversationId": "recommend-conversation"
  }'
```

期待される結果：詳細な商品推薦と価格帯別の提案

### 5. 技術アドバイス機能のテスト

技術的なアドバイス機能をテスト：

```bash
curl -X POST http://localhost:8085/api/v1/chat/advice \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "test-user",
    "content": "パラレルターンがうまくできません。どのように練習すれば良いでしょうか？",
    "conversationId": "advice-conversation"
  }'
```

期待される結果：段階的な練習方法と具体的なアドバイス

### 6. 検索強化機能のテスト

検索クエリの拡張機能をテスト：

```bash
curl -X POST "http://localhost:8091/api/v1/search/enhance-query" \
  -G --data-urlencode "query=スキー板 初心者"
```

期待される結果：検索クエリの分析と改善提案

### 7. API仕様の確認

OpenAPI仕様を確認：

```bash
curl http://localhost:8091/q/openapi
```

Swagger UIでブラウザから確認：

```text
http://localhost:8085/q/swagger-ui
```

### 検証項目チェックリスト

- [ ] アプリケーションが正常に起動する（約5-10秒）
- [ ] ヘルスチェックが`UP`ステータスを返す
- [ ] チャット機能が日本語で適切に応答する
- [ ] 商品推薦が詳細で実用的な内容を提供する
- [ ] 技術アドバイスが段階的で安全性を考慮している
- [ ] 検索強化機能がクエリを適切に分析・拡張する
- [ ] OpenAPI仕様が15以上のエンドポイントを表示する
- [ ] Swagger UIが正常に表示される

### トラブルシューティング

#### Dockerエラーが発生する場合

Dev Servicesが無効になっているため、Dockerは不要です。エラーメッセージは無視できます。

#### ポート競合が発生する場合

別のポートを指定して起動：

```bash
mvn quarkus:dev -Dquarkus.http.port=8086
```

#### Azure OpenAI接続エラーが発生する場合

- 環境変数が正しく設定されているか確認
- Azure OpenAIのAPI Key、Endpoint、デプロイメント名が正確か確認
- Azure OpenAIのクォータが残っているか確認

## Docker実行

```bash
# イメージビルド
docker build -f src/main/docker/Dockerfile.jvm -t ai-support-service .

# コンテナ実行
docker run -i --rm -p 8091:8091 ai-support-service
```

## ヘルスチェック

- **Liveness**: `GET /q/health/live`
- **Readiness**: `GET /q/health/ready`

## API ドキュメント

開発モードでは、以下のURLでSwagger UIにアクセスできます：

- <http://localhost:8091/q/swagger-ui>

## 主要な実装クラス

### エンティティ

- `ConversationSession` - 会話セッション管理
- `ChatMessage` - チャットメッセージ
- `ConversationAnalysis` - 会話分析結果
- `KnowledgeBaseEntry` - 知識ベースエントリ

### サービス

- `ChatService` - メインチャットサービス
- `CustomerSupportAssistant` - 顧客サポートAI
- `ProductRecommendationAssistant` - 商品推薦AI
- `SearchEnhancementAssistant` - 検索拡張AI

### コントローラー

- `ChatController` - チャット API
- `RecommendationController` - 推薦 API
- `SearchController` - 検索拡張 API

### 設定・例外処理

- `LangChain4jConfig` - LangChain4j設定
- `AiServiceException` - AI関連例外処理
- `GlobalExceptionHandler` - グローバル例外ハンドリング

## 特徴

1. **最新技術の活用**
   - LangChain4j 1.1.0の最新機能
   - Java 21の新機能（Records, Sealed Classes, Switch Expressions, Text Blocks）
   - Quarkus 3.15.1によるクラウドネイティブ対応

2. **堅牢な設計**
   - 包括的なエラーハンドリング
   - リトライ機構とフォールバック
   - 型安全なAPI設計

3. **高性能**
   - Quarkusによる高速起動
   - Redis キャッシング
   - 非同期処理対応

4. **拡張性**
   - マイクロサービスアーキテクチャ
   - プラガブルAIサービス
   - 柔軟な設定システム

## ライセンス

本プロジェクトはMITライセンスの下で公開されています。

## TODO

現在、開発環境での検証を容易にするため永続化やキャッシュ機能は無効化しています。
実際には、PostgreSQL や Redis などの設定を行うことでより本番環境で堅牢なシステムを作ることができます。

## Docker環境でのトラブルシューティング

### よくある問題

1. **Azure OpenAI接続エラー**

   ```bash
   # 設定確認
   docker-compose exec ai-support-service env | grep AZURE
   
   # ログ確認
   docker-compose logs ai-support-service
   ```

2. **メモリ不足エラー**

   ```bash
   # リソース使用量確認
   docker stats ai-support-service
   
   # docker-compose.ymlのメモリ制限を調整
   ```

3. **ポート競合**

   ```bash
   # ポート使用状況確認
   lsof -i :8091
   
   # docker-compose.ymlのポート番号を変更
   ports:
     - "8092:8091"  # 8092ポートで公開
   ```

### ログ確認

```bash
# リアルタイムログ
docker-compose logs -f ai-support-service

# 過去のログ
docker-compose logs --tail=100 ai-support-service

# エラーログのみ
docker-compose logs ai-support-service 2>&1 | grep ERROR
```

### 監視・メトリクス

```bash
# ヘルスチェック
curl http://localhost:8091/q/health

# メトリクス（Prometheusフォーマット）
curl http://localhost:8091/q/metrics

# アプリケーション情報
curl http://localhost:8091/q/info
```
