#!/bin/bash

# AI Support Service Docker 起動スクリプト

set -e

echo "🚀 AI Support Service Docker 起動スクリプト (Java 21)"
echo "====================================================="

# 環境変数ファイルのチェック
if [ ! -f ".env" ]; then
    echo "❌ .envファイルが見つかりません。"
    echo "📝 .env.templateをコピーして.envファイルを作成してください:"
    echo "   cp .env.template .env"
    echo "   vim .env"
    exit 1
fi

echo "✅ .envファイルが見つかりました"

# Azure OpenAI設定の確認
if ! grep -q "AZURE_OPENAI_ENDPOINT=" .env || ! grep -q "AZURE_OPENAI_API_KEY=" .env; then
    echo "⚠️  Azure OpenAI設定が不完全です。.envファイルを確認してください。"
    echo "   必要な設定:"
    echo "   - AZURE_OPENAI_ENDPOINT"
    echo "   - AZURE_OPENAI_API_KEY"
    echo "   - AZURE_OPENAI_CHAT_DEPLOYMENT_NAME"
    echo "   - AZURE_OPENAI_EMBEDDING_DEPLOYMENT_NAME"
fi

# Dockerの確認
if ! command -v docker &> /dev/null; then
    echo "❌ Dockerがインストールされていません"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Composeがインストールされていません"
    exit 1
fi

echo "✅ Docker環境が利用可能です"

# サービス起動
echo "🐳 Dockerサービスを起動中..."
docker-compose up -d

echo "⏳ サービスの起動を待機中..."
sleep 10

# ヘルスチェック
echo "🔍 ヘルスチェック実行中..."
for i in {1..30}; do
    if curl -f http://localhost:8091/q/health > /dev/null 2>&1; then
        echo "✅ サービスが正常に起動しました！"
        echo ""
        echo "🌐 利用可能なエンドポイント:"
        echo "   - ヘルスチェック: http://localhost:8091/q/health"
        echo "   - API ドキュメント: http://localhost:8091/q/swagger-ui"
        echo "   - メトリクス: http://localhost:8091/q/metrics"
        echo ""
        echo "🧪 テスト用コマンド:"
        echo "   curl -X POST http://localhost:8091/api/v1/chat/message \\"
        echo "     -H \"Content-Type: application/json\" \\"
        echo "     -d '{\"userId\":\"test\",\"content\":\"こんにちは\",\"conversationId\":\"test\"}'"
        echo ""
        echo "📊 ログ確認: docker-compose logs -f ai-support-service"
        echo "🛑 停止コマンド: docker-compose down"
        exit 0
    fi
    echo "   試行 $i/30: サービス起動待機中..."
    sleep 2
done

echo "❌ サービスの起動に失敗しました"
echo "📊 ログを確認してください: docker-compose logs ai-support-service"
exit 1
