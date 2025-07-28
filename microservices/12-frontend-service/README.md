# 12-frontend-service

これはNext.jsとTypeScriptで構築されたスキー販売システムのフロントエンドサービスです。顧客がスキー用品を管理し、情報を閲覧し、AIサポート機能にアクセスするためのユーザーインターフェースを提供します。

## 🔧 環境設定

### 前提条件

- Node.js 18+ 
- npm または yarn

### 環境変数の設定

1. 環境変数テンプレートをコピー：

```bash
cp .env.example .env.local
```

2. `.env.local`を編集して設定を変更：

```bash
# 開発環境用設定
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
NEXT_PUBLIC_USE_API_GATEWAY=false
NEXT_PUBLIC_DISABLE_DUMMY_USER=false
```

**⚠️ セキュリティ警告**: 

- `.env.local`をバージョン管理にコミットしない
- 本番環境では`NEXT_PUBLIC_DISABLE_DUMMY_USER=true`に設定
- 本番環境ではAPIゲートウェイ（`NEXT_PUBLIC_USE_API_GATEWAY=true`）を使用

## はじめに

まず、開発サーバーを起動します：

```bash
npx next dev --turbopack --port 3000
```

ブラウザで [http://localhost:3000](http://localhost:3000) を開いて結果を確認してください。

## デモユーザー（開発専用）

テスト目的で、開発モードでデモユーザーが利用可能です：

- **メールアドレス**: `demo@skiresort.com`
- **パスワード**: `demo123`
- **機能**: AIサポートとすべての顧客機能へのアクセス

### デモユーザーの操作

- デモユーザーは開発モードで自動的に有効化されます
- 開発者コントロールパネル（左上角）でデモユーザーのオン/オフを切り替え可能
- デモユーザーは本番環境では自動的に無効化されます
- 手動で無効にするには`NEXT_PUBLIC_DISABLE_DUMMY_USER=true`を環境変数に設定

### ログイン手順

1. `/login`にアクセス
2. デモ認証情報を使用するか「デモアカウント情報を入力」ボタンをクリック
3. ログインをクリックしてAIサポートを含むすべての機能にアクセス

`app/page.tsx`を編集してページを変更できます。ファイルを編集すると、ページが自動更新されます。

このプロジェクトは[`next/font`](https://nextjs.org/docs/app/building-your-application/optimizing/fonts)を使用してVercelの新しいフォントファミリー[Geist](https://vercel.com/font)を自動的に最適化・読み込みしています。

## 詳細情報

Next.jsについて詳しく学ぶには、以下のリソースを参照してください：

- [Next.js Documentation](https://nextjs.org/docs) - Next.jsの機能とAPIについて学習
- [Learn Next.js](https://nextjs.org/learn) - インタラクティブなNext.jsチュートリアル

[Next.js GitHubリポジトリ](https://github.com/vercel/next.js)もチェックしてみてください - フィードバックやコントリビューションを歓迎します！

## Vercelでのデプロイ

Next.jsアプリをデプロイする最も簡単な方法は、Next.jsの開発元である[Vercel Platform](https://vercel.com/new?utm_medium=default-template&filter=next.js&utm_source=create-next-app&utm_campaign=create-next-app-readme)を使用することです。

詳細については、[Next.jsデプロイメント文書](https://nextjs.org/docs/app/building-your-application/deploying)をご確認ください。
