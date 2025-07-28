# AI サポートチャット機能

このプロジェクトに統合されたAIサポートチャット機能です。

## 機能概要

- **商品推薦**: ユーザーのニーズに基づいてスキー用品を推薦
- **技術アドバイス**: スキー技術に関する専門的なアドバイス
- **一般チャット**: スキーに関する様々な質問対応
- **会話履歴**: 複数の会話を管理・保存
- **リアルタイム応答**: AIサポートサービスとのリアルタイム通信

## アーキテクチャ

```
12-frontend-service/
├── src/
│   ├── components/chat/           # チャット関連コンポーネント
│   │   ├── ChatInterface.tsx      # メインチャットインターフェース
│   │   ├── ConversationSidebar.tsx # 会話リストサイドバー
│   │   ├── MessageBubble.tsx      # メッセージ表示コンポーネント
│   │   └── ChatInput.tsx          # メッセージ入力コンポーネント
│   ├── services/api/
│   │   └── aiSupportApi.ts        # AIサポートAPI通信
│   ├── stores/
│   │   └── chatStore.ts           # チャット状態管理（Zustand）
│   ├── types/
│   │   └── ai-chat.ts             # チャット関連型定義
│   └── app/
│       └── chat/                  # チャットページ
│           ├── page.tsx           # メインチャットページ
│           └── demo/page.tsx      # デモページ
```

## API エンドポイント

AIサポートサービス（11-ai-support-service）の以下のエンドポイントを使用：

- `POST /api/v1/chat/recommend` - 商品推薦
- `POST /api/v1/chat/advice` - 技術アドバイス  
- `POST /api/v1/chat/message` - 一般チャット

## 使用方法

### 1. AIサポートサービスの起動

```bash
cd 11-ai-support-service
./mvnw quarkus:dev
```

### 2. フロントエンドサービスの起動

```bash
cd 12-frontend-service
npm run dev
```

### 3. チャット機能へのアクセス

- メインページのヘッダーから「AIサポート」をクリック
- または直接 `http://localhost:3000/chat` にアクセス
- デモページ: `http://localhost:3000/chat/demo`

## CORS設定（開発環境）

AIサポートサービス側でCORS設定が必要な場合は、以下を `application.properties` に追加：

```properties
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000
quarkus.http.cors.headers=accept, authorization, content-type, x-requested-with
quarkus.http.cors.methods=GET, POST, PUT, DELETE, OPTIONS
```

## 主要コンポーネント

### ChatInterface
- メインのチャットUIコンポーネント
- 会話リスト、メッセージ表示、入力フォームを統合

### MessageBubble
- ユーザーとAIのメッセージを表示
- Markdown形式のAI応答をHTMLに変換

### ChatInput
- メッセージ入力フォーム
- チャットモード選択（一般・推薦・アドバイス）

### ConversationSidebar
- 会話履歴の管理
- 新規会話作成、会話切り替え、削除機能

## 状態管理

Zustandを使用してチャット状態を管理：

- 会話リスト
- 現在の会話ID
- メッセージ履歴
- ローディング状態
- エラー状態

## 型定義

TypeScriptの型定義で型安全性を確保：

- `ChatMessage`: メッセージの構造
- `ChatConversation`: 会話の構造
- `ChatMode`: チャットモード（recommend/advice/message）
- `ChatStore`: 状態管理ストアの型

## カスタマイズ

### メッセージフォーマット
`MessageBubble.tsx` の `formatContent` 関数でMarkdown表示をカスタマイズ可能

### チャットモード
新しいチャットモードを追加する場合：
1. `ChatMode` 型に新しいモードを追加
2. `aiSupportApi.ts` でエンドポイントを追加
3. `ChatInput.tsx` でモード選択UIを更新

### スタイリング
TailwindCSSクラスでスタイルをカスタマイズ可能

## トラブルシューティング

### AIサポートサービスに接続できない場合
1. AIサポートサービスが起動していることを確認
2. CORS設定が正しいことを確認
3. ファイアウォール設定を確認

### メッセージが表示されない場合
1. ブラウザの開発者ツールでネットワークエラーを確認
2. AIサポートサービスのログを確認
3. APIレスポンス形式が期待通りであることを確認
