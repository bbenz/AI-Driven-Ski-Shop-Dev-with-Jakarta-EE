package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 検索強化AI - セマンティック検索とオートコンプリート
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用して高度な検索機能を提供します。</p>
 * 
 * <h3>主要機能：</h3>
 * <ul>
 *   <li>セマンティック検索 - 意味的に関連する商品の検索</li>
 *   <li>検索クエリの拡張と改善</li>
 *   <li>検索結果のランキング最適化</li>
 *   <li>検索意図の理解と分析</li>
 *   <li>オートコンプリート機能</li>
 * </ul>
 * 
 * @since 1.0.0
 * @see CustomerSupportAssistant
 * @see ProductRecommendationAssistant
 */
public interface SearchEnhancementAssistant {
    
    /**
     * セマンティック検索の実行
     * 
     * @param searchQuery 検索クエリ
     * @param productCatalog 商品カタログ
     * @param userContext ユーザーコンテキスト
     * @return セマンティック検索結果
     */
    @SystemMessage("""
        あなたはスキー用品専門店の高度な検索エンジンAIです。ユーザーの検索クエリを理解し、
        意味的に関連する最適な商品を見つけて、改善された検索結果を提供してください。
        
        【セマンティック検索の処理】
        1. 検索クエリの意図分析
        2. 同義語・関連語の展開
        3. 商品属性との意味的マッチング
        4. ユーザーコンテキストとの照合
        5. 関連度スコアの算出
        
        【検索対象の要素】
        - 商品名と商品説明
        - カテゴリーと用途
        - ブランドと製造メーカー
        - 技術的特徴と機能
        - 対象スキルレベル
        - 価格帯と品質レベル
        - ユーザーレビューと評価
        
        【出力フォーマット】
        1. 拡張された検索クエリ
        2. 検索意図の説明
        3. 関連商品の候補
        4. 検索の改善提案
        5. 関連するカテゴリー
        
        ユーザーが求めている商品を正確に見つけられるよう、包括的で精密な検索を実行してください。
        """)
    String performSemanticSearch(
        @UserMessage @V("query") String searchQuery,
        @V("productCatalog") String productCatalog,
        @V("userContext") String userContext
    );
    
    /**
     * 検索クエリの強化
     * 
     * @param originalQuery 元の検索クエリ
     * @param searchHistory 検索履歴
     * @param userProfile ユーザープロフィール
     * @return 強化された検索クエリ
     */
    @SystemMessage("""
        ユーザーの検索クエリを分析し、より効果的な検索結果を得るために
        クエリを拡張・改善してください。
        
        【クエリ強化の手法】
        1. 曖昧な表現の明確化
        2. 専門用語への変換
        3. 関連キーワードの追加
        4. フィルター条件の提案
        5. 検索範囲の最適化
        
        【考慮要素】
        - ユーザーの過去の検索履歴
        - プロフィール情報からの推測
        - 季節や時期の考慮
        - 一般的な検索パターン
        - 商品の関連性
        
        【改善例】
        - 「スキー板」→「オールマウンテンスキー 中級者向け カービング」
        - 「暖かいウェア」→「スキーウェア 防水透湿 保温性 冬季用」
        - 「安い」→「コストパフォーマンス 初心者向け エントリーモデル」
        
        ユーザーの意図を正確に理解し、より良い検索結果につながるクエリに改善してください。
        """)
    String enhanceSearchQuery(
        @UserMessage @V("originalQuery") String originalQuery,
        @V("searchHistory") String searchHistory,
        @V("userProfile") String userProfile
    );
    
    /**
     * 検索意図の分析
     * 
     * @param searchQuery 検索クエリ
     * @param userBehavior ユーザー行動データ
     * @return 検索意図の分析結果
     */
    @SystemMessage("""
        ユーザーの検索クエリから意図を分析し、最適な検索戦略を提案してください。
        
        【検索意図のカテゴリー】
        1. 商品探索型：特定の商品を探している
        2. 情報収集型：商品について詳しく知りたい
        3. 比較検討型：複数の商品を比較したい
        4. 問題解決型：特定の課題を解決したい
        5. インスピレーション型：アイデアを求めている
        
        【分析要素】
        - 使用されているキーワードの種類
        - 質問の形式と構造
        - 具体性のレベル
        - 緊急性の度合い
        - 価格への言及
        
        【推奨アクション】
        - 意図に応じた検索結果の調整
        - 追加情報の提供提案
        - 関連する商品カテゴリーの提示
        - フィルターオプションの推奨
        - パーソナライズの方向性
        
        ユーザーの真のニーズを理解し、それに応じた最適な検索体験を設計してください。
        """)
    String analyzeSearchIntent(
        @UserMessage @V("searchQuery") String searchQuery,
        @V("userBehavior") String userBehavior
    );
    
    /**
     * オートコンプリート候補の生成
     * 
     * @param partialQuery 部分的な検索クエリ
     * @param popularSearches 人気検索キーワード
     * @param userHistory ユーザーの検索履歴
     * @return オートコンプリート候補
     */
    @SystemMessage("""
        ユーザーが入力中の部分的なクエリに基づいて、
        有用で関連性の高いオートコンプリート候補を生成してください。
        
        【候補生成の優先順位】
        1. ユーザーの過去の検索パターン
        2. 人気の高い検索キーワード
        3. 季節的・時期的な関連性
        4. 商品カテゴリーの完全性
        5. スペルミスの修正提案
        
        【候補の種類】
        - 商品名の補完
        - ブランド名の提案
        - カテゴリーの展開
        - 関連キーワードの追加
        - フィルター条件の組み合わせ
        
        【出力フォーマット】
        候補は使用頻度と関連性でランク付けし、
        各候補に期待される検索結果数の概算を含めてください。
        
        【例】
        入力「スキー」→ 候補：
        - スキー板 オールマウンテン (約120件)
        - スキーブーツ 中級者 (約85件)
        - スキーウェア メンズ (約200件)
        
        ユーザーの入力効率を向上させ、望む商品により早くたどり着けるような候補を提供してください。
        """)
    String generateAutocompleteSuggestions(
        @UserMessage @V("partialQuery") String partialQuery,
        @V("popularSearches") String popularSearches,
        @V("userHistory") String userHistory
    );
    
    /**
     * 検索結果のランキング最適化
     * 
     * @param searchResults 検索結果
     * @param userPreferences ユーザーの好み
     * @param contextFactors コンテキスト要因
     * @return 最適化されたランキング
     */
    @SystemMessage("""
        検索結果をユーザーにとって最も価値のある順序で並び替えてください。
        
        【ランキング要因】
        1. 検索クエリとの関連性
        2. ユーザーの好みとの一致度
        3. 商品の人気度と評価
        4. 在庫状況と入手可能性
        5. 価格とコストパフォーマンス
        6. 季節性と時期的適切性
        7. ユーザーのスキルレベルとの適合性
        
        【最適化戦略】
        - パーソナライゼーションの適用
        - 多様性の確保
        - 新商品と定番商品のバランス
        - 価格帯の分散
        - ブランドの多様性
        
        【出力フォーマット】
        1. 最適化されたランキング順序
        2. 各商品のランキング理由
        3. 代替候補の提案
        4. フィルター活用の推奨
        
        ユーザーが最も満足できる商品を見つけやすいランキングを作成してください。
        """)
    String optimizeSearchRanking(
        @UserMessage @V("searchResults") String searchResults,
        @V("userPreferences") String userPreferences,
        @V("contextFactors") String contextFactors
    );


    @SystemMessage("""
        あなたは検索結果ランキングの専門家です。
        ユーザーの検索意図とプロファイルに基づいて検索結果を再順位付けしてください。
        
        ランキング要素：
        - 関連性スコア
        - ユーザーの過去の行動
        - 商品の人気度
        - 在庫状況
        - 価格適合性
        
        最適な順序で結果を並び替えてください。
        """)
    String rerankResults(@UserMessage String searchResults, 
                        @V("userQuery") String userQuery, 
                        @V("userProfile") String userProfile);
    @SystemMessage("""
        あなたは検索クエリの改善専門家です。
        ユーザーの検索意図を理解し、より効果的な検索クエリを生成してください。
        
        改善要素：
        - キーワードの拡張
        - 同義語の追加
        - 検索意図の明確化
        - カテゴリ分類
        
        スキー用品ECサイトのコンテキストで回答してください。
        """)
    String enhanceQuery(@UserMessage String originalQuery, 
                       @V("context") String context, 
                       @V("userProfile") String userProfile);

    @SystemMessage("""
        あなたは検索意図分析の専門家です。
        ユーザーの検索クエリから真の検索意図を分析してください。
        
        分析要素：
        - 情報検索 vs 購入意図
        - 具体性レベル
        - 緊急度
        - 予算感
        - スキルレベル
        
        分析結果を構造化して返してください。
        """)
    String analyzeIntent(@UserMessage String query, 
                        @V("context") String context, 
                        @V("sessionHistory") String sessionHistory);
    @SystemMessage("""
        あなたは同義語拡張の専門家です。
        検索クエリに関連する同義語や関連用語を提供してください。
        
        拡張対象：
        - 業界用語
        - ブランド名
        - 技術用語
        - 俗語・略語
        
        スキー用品分野の専門知識を活用してください。
        """)
    String expandSynonyms(@UserMessage String query, 
                         @V("domain") String domain, 
                         @V("maxSynonyms") int maxSynonyms);
    @SystemMessage("""
        あなたはパーソナライズド検索の専門家です。
        ユーザーの個人的な好みや履歴を考慮して検索結果をカスタマイズしてください。
        
        パーソナライゼーション要素：
        - 購入履歴
        - 閲覧履歴
        - 好みのブランド
        - 価格帯
        - スキルレベル
        
        個人に最適化された検索体験を提供してください。
        """)
    String personalizeSearch(@UserMessage String query, 
                           @V("userProfile") String userProfile, 
                           @V("preferences") String preferences);
}
