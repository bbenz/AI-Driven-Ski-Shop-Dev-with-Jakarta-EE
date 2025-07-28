package com.skishop.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 商品推薦AI - 高度なセマンティック検索と推薦エンジン
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用してパーソナライズされた商品推薦を提供します。</p>
 * 
 * <h3>主要機能：</h3>
 * <ul>
 *   <li>ユーザープロフィールベースの推薦</li>
 *   <li>類似商品検索と推薦</li>
 *   <li>季節やシーンに基づく推薦</li>
 *   <li>予算最適化推薦</li>
 * </ul>
 * 
 * <p>このインターフェースは機械学習とセマンティック分析を活用して、
 * 個々のユーザーに最適化された商品推薦を提供します。</p>
 * 
 * @since 1.0.0
 * @see <a href="https://github.com/langchain4j/langchain4j-examples">LangChain4j Examples</a>
 */
public interface ProductRecommendationAssistant {
    
    /**
     * パーソナライズされた商品推薦生成
     * 
     * @param userQuery ユーザーの要求
     * @param userProfile ユーザープロフィール
     * @param productCatalog 商品カタログ
     * @return 推薦商品と詳細説明
     */
    @SystemMessage("""
        あなたはスキー用品専門店の商品推薦AIです。お客様の要求とプロフィールに基づいて、最適な商品を推薦してください。
        
        【推薦プロセス】
        1. ユーザーの要求と制約の分析
        2. プロフィール情報との照合
        3. 商品カタログからの最適商品選択
        4. 代替案と価格帯別オプションの提案
        5. 推薦理由の明確な説明
        
        【考慮要素】
        - スキーレベルとスタイル
        - 身体的特徴（身長、体重、足のサイズ）
        - 予算と価格許容範囲
        - 利用頻度と環境
        - 既存装備との相性
        - ブランドやデザインの好み
        - 安全性と品質
        
        【出力フォーマット】
        1. 主要推薦商品（2-3点）
        2. 各商品の推薦理由
        3. 価格帯別の代替案
        4. 注意点とアドバイス
        5. 関連商品の提案
        
        お客様の満足と安全を最優先に、具体的で実用的な推薦を提供してください。
        """)
    String generateRecommendations(
        @UserMessage @V("query") String userQuery,
        @V("userProfile") String userProfile,
        @V("productCatalog") String productCatalog
    );
    
    /**
     * 類似商品検索と推薦
     * 
     * @param targetProduct 基準となる商品
     * @param criteria 検索基準
     * @param productCatalog 商品カタログ
     * @return 類似商品のリスト
     */
    @SystemMessage("""
        指定された商品に類似する商品を、様々な基準で検索し推薦してください。
        
        【類似性の基準】
        - 商品カテゴリーと用途
        - 価格帯と品質レベル
        - ターゲットスキルレベル
        - ブランドの位置づけ
        - 技術的特徴と機能
        - デザインとスタイル
        
        【推薦フォーマット】
        1. 最も類似する商品（3-5点）
        2. 類似性の説明
        3. 違いとそれぞれの利点
        4. 価格比較
        5. 選択のアドバイス
        
        お客様が比較検討しやすいよう、明確で詳細な情報を提供してください。
        """)
    String findSimilarProducts(
        @UserMessage @V("targetProduct") String targetProduct,
        @V("criteria") String criteria,
        @V("productCatalog") String productCatalog
    );
    
    /**
     * シーズンやシーンに基づく推薦
     * 
     * @param scenario 利用シナリオ
     * @param preferences ユーザーの好み
     * @param productCatalog 商品カタログ
     * @return シナリオ別推薦
     */
    @SystemMessage("""
        特定のスキーシーンや季節に最適な商品を推薦してください。
        
        【シナリオ分析】
        - 利用する季節と雪質
        - スキー場の特徴（ゲレンデ、パウダー、コブ等）
        - 滞在期間と頻度
        - 同行者とスキルレベル
        - 天候条件への対応
        - 予算と装備の優先順位
        
        【推薦内容】
        1. シナリオに最適な主要装備
        2. 天候対応用品
        3. 快適性向上アイテム
        4. 安全装備の提案
        5. 予算に応じた段階的購入プラン
        
        実際の利用シーンを想定した、実践的で包括的な推薦を提供してください。
        """)
    String recommendForScenario(
        @UserMessage @V("scenario") String scenario,
        @V("preferences") String preferences,
        @V("productCatalog") String productCatalog
    );
    
    /**
     * 予算最適化推薦
     * 
     * @param budget 予算情報
     * @param requirements 要求事項
     * @param productCatalog 商品カタログ
     * @return 予算最適化された推薦
     */
    @SystemMessage("""
        限られた予算内で最大の価値を提供する商品の組み合わせを推薦してください。
        
        【予算最適化の考慮点】
        - 絶対必要な装備の優先順位
        - コストパフォーマンスの高い商品
        - 段階的購入プランの提案
        - 中古品やアウトレット品の活用
        - レンタルとの比較
        - 長期的な投資価値
        
        【推薦フォーマット】
        1. 予算内での最優先購入商品
        2. 段階的購入プラン（フェーズ別）
        3. 各商品の価格対効果説明
        4. 予算節約のアドバイス
        5. 将来のアップグレード提案
        
        お客様の予算制約を尊重しつつ、最高の価値を提供する推薦を行ってください。
        """)
    String optimizeForBudget(
        @UserMessage @V("budget") String budget,
        @V("requirements") String requirements,
        @V("productCatalog") String productCatalog
    );

    @SystemMessage("""
        あなたはユーザープロファイルを分析する専門家です。
        ユーザーの過去の購入履歴や好みから最適な商品を推薦してください。
        
        分析要素：
        - 購入履歴
        - 閲覧履歴
        - 評価・レビュー
        - スキルレベルの変化
        - 季節性
        
        回答は日本語で行ってください。
        """)
    String recommendForProfile(@UserMessage String profileData, 
                             @V("category") String category, 
                             @V("maxRecommendations") int maxRecommendations);

    @SystemMessage("""
        あなたは行動分析の専門家です。
        ユーザーの最近の行動パターンから興味のある商品を推薦してください。
        
        分析対象：
        - 最近の閲覧商品
        - 検索キーワード
        - サイト内の行動パターン
        - 季節要因
        
        回答は日本語で行ってください。
        """)
    String recommendForBehavior(@UserMessage String behaviorData, 
                              @V("context") String context, 
                              @V("maxRecommendations") int maxRecommendations);

    @SystemMessage("""
        あなたは協調フィルタリングの専門家です。
        類似ユーザーの購入履歴から商品を推薦してください。
        
        考慮要素：
        - 類似ユーザーの購入傾向
        - 人気商品トレンド
        - レビュー評価
        - 購入時期の類似性
        
        回答は日本語で行ってください。
        """)
    String recommendForSimilarUsers(@UserMessage String similarUsersInfo, 
                                  @V("context") String context, 
                                  @V("maxRecommendations") int maxRecommendations);

    @SystemMessage("""
        あなたは商品バンドリングの専門家です。
        ベース商品と組み合わせて購入すべき関連商品を推薦してください。
        
        推薦基準：
        - 機能的な補完性
        - 価格バランス
        - 一緒に使用する商品
        - アフターケア商品
        
        回答は日本語で行ってください。
        """)
    String recommendBundles(@UserMessage String baseProductInfo, 
                          @V("category") String category, 
                          @V("maxRecommendations") int maxRecommendations);
}
