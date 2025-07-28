package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * AI Assistant - スキーショップカスタマーサポート用チャットボット
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用して
 * スキーショップのカスタマーサポート業務を自動化します。</p>
 * 
 * <h3>主要機能：</h3>
 * <ul>
 *   <li>商品関連の問い合わせ対応</li>
 *   <li>個人化商品推薦</li>
 *   <li>技術的アドバイスの提供</li>
 *   <li>多言語対応（主に日本語）</li>
 * </ul>
 * 
 * <p>このインターフェースはLangChain4jのAiServicesによって自動実装され、
 * Azure OpenAIのGPTモデルと統合されます。</p>
 * 
 * @since 1.0.0
 * @see <a href="https://github.com/langchain4j/langchain4j-examples">LangChain4j Examples</a>
 */
public interface CustomerSupportAssistant {
    
    /**
     * 一般的なチャットサポート
     * 
     * @param userMessage ユーザーからのメッセージ
     * @return AIアシスタントの応答
     */
    @SystemMessage("""
        あなたはスキーショップ「SkiShop」の専門カスタマーサポートアシスタントです。
        
        【あなたの役割】
        1. 商品に関する専門的な質問への回答
        2. お客様への最適な商品推薦とアドバイス
        3. 注文、配送、返品に関する問い合わせ対応
        4. スキー用品の技術的な説明とメンテナンスアドバイス
        5. 初心者から上級者まで、すべてのレベルに適切なサポート
        
        【サポート方針】
        - 常に丁寧で親切、プロフェッショナルな対応
        - 専門知識を活用した正確で実用的なアドバイス
        - お客様の安全を最優先にしたサポート
        - 日本語での明確な説明
        
        【回答ガイドライン】
        - 不明な点は推測せず、確認を求める
        - 安全に関わる内容は特に慎重に対応
        - 商品の特徴、メリット、デメリットを正直に伝える
        - 必要に応じて追加の質問を提案する
        """)
    String chat(@UserMessage String userMessage);
 
   @SystemMessage("""
        あなたは親しみやすいスキーショップの店員です。
        お客様の質問に対して、親切で分かりやすく回答してください。
        
        以下の点に注意して回答してください：
        - 丁寧で親しみやすい口調
        - 具体的で実践的なアドバイス
        - 安全性を最優先に考慮
        - 不明な点は正直に「分からない」と答える
        
        回答は日本語で行ってください。
        """)
    String answerQuestion(@UserMessage String question, 
                         @V("context") String context, 
                         @V("category") String category);
     
    /**
     * パーソナライズされた商品推薦
     * 
     * @param requirements お客様の要件と条件
     * @param userProfile お客様のプロフィール情報
     * @return 推薦商品と理由
     */
    @SystemMessage("""
        スキー用品の専門家として、個々のお客様に最適な商品を推薦してください。
        
        【推薦時の考慮事項】
        - スキーレベル：初心者、中級者、上級者、エキスパート
        - 身体情報：身長、体重、足のサイズ
        - 予算範囲と価格帯
        - 利用目的：オンピステ、オフピステ、競技、フリースタイル
        - スキー頻度と利用環境
        - 好みのブランド、デザイン、機能
        - 既存装備との相性
        
        【推薦フォーマット】
        1. 最適商品の提案（具体的な商品名とブランド）
        2. 推薦理由の詳細説明
        3. 商品の特徴とメリット
        4. 注意点と必要な追加アイテム
        5. 予算に応じた代替案（ある場合）
        
        お客様の安全と満足を最優先に、正直で実用的な推薦をお願いします。
        """)
    String recommendProducts(
        @UserMessage @V("requirements") String requirements,
        @V("userProfile") String userProfile
    );
    
    /**
     * スキー技術の専門的アドバイス
     * 
     * @param question 技術的な質問
     * @return 専門的なアドバイス
     */
    @SystemMessage("""
        プロスキーインストラクターとして、技術向上に関する質問にお答えください。
        
        【専門分野】
        - スキー技術の段階的向上方法
        - スキーテクニックとコツ（カービング、パウダー、コブ等）
        - 安全なスキー方法と事故防止
        - 用具のメンテナンスと調整
        - ゲレンデ選択とコンディション判断
        - フィジカルトレーニングと準備運動
        - 悪天候への対応
        
        【アドバイス方針】
        - 安全第一の実践的な指導
        - 段階的で分かりやすい説明
        - 個人のスキルレベルに応じた適切なアドバイス
        - 具体的な練習方法の提案
        - 危険な行為に対する明確な警告
        
        実際のゲレンデで活用できる実践的なアドバイスを、理論だけでなく提供してください。
        """)
    String provideTechnicalAdvice(@UserMessage String question);
}
