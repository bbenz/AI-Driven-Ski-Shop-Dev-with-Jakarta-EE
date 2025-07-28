package com.skishop.ai.controller;

import com.skishop.ai.service.SearchEnhancementAssistant;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Search Enhancement API Controller
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用した検索機能拡張</p>
 * 
 * @since 1.0.0
 */
@Path("/api/v1/search")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Search API", description = "AI-Enhanced Search API")
public class SearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
    
    private final SearchEnhancementAssistant searchAssistant;
    
    /**
     * コンストラクタ
     * 
     * @param searchAssistant 検索拡張アシスタント
     */
    @Inject
    public SearchController(SearchEnhancementAssistant searchAssistant) {
        this.searchAssistant = searchAssistant;
    }
    
    /**
     * クエリ拡張
     */
    @POST
    @Path("/enhance-query")
    @Operation(summary = "Enhance search query", description = "Use AI to enhance and expand search queries")
    public Response enhanceQuery(@QueryParam("query") String query) {
        
        logger.info("Enhancing search query: {}", query);
        
        try {
            // AI検索アシスタントでクエリを拡張
            String enhancedQuery = searchAssistant.enhanceQuery(query, "スキー用品", "default_user");
            
            // レスポンス構築
            var response = Map.of(
                "originalQuery", query,
                "enhancedQuery", enhancedQuery,
                "suggestions", parseEnhancedQuery(enhancedQuery)
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error enhancing query: {}", query, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "クエリ拡張中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 同義語展開
     */
    @POST
    @Path("/expand-synonyms")
    @Operation(summary = "Expand synonyms", description = "Expand search terms with synonyms using AI")
    public Response expandSynonyms(@QueryParam("term") String term) {
        
        logger.info("Expanding synonyms for term: {}", term);
        
        try {
            // AI検索アシスタントで同義語を展開
            String synonyms = searchAssistant.expandSynonyms(term, "スキー・スノーボード用語", 10);
            
            // レスポンス構築
            var response = Map.of(
                "originalTerm", term,
                "synonyms", synonyms,
                "expandedTerms", parseSynonyms(synonyms)
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error expanding synonyms for term: {}", term, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "同義語展開中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 検索結果の再ランキング
     */
    @POST
    @Path("/rerank")
    @Operation(summary = "Rerank search results", description = "Rerank search results using AI")
    public Response rerankResults(
            @QueryParam("query") String query,
            Map<String, Object> searchResults) {
        
        logger.info("Reranking search results for query: {}", query);
        
        try {
            // 検索結果を文字列に変換
            String resultsString = convertResultsToString(searchResults);
            
            // AI検索アシスタントで結果を再ランキング
            String rerankedResults = searchAssistant.rerankResults(
                query, 
                resultsString, 
                "関連性・人気度・評価"
            );
            
            // レスポンス構築
            var response = Map.of(
                "query", query,
                "originalResults", searchResults,
                "rerankedResults", rerankedResults,
                "rankingFactors", List.of("関連性", "人気度", "評価", "在庫状況")
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error reranking results for query: {}", query, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "検索結果の再ランキング中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 検索意図分析
     */
    @POST
    @Path("/analyze-intent")
    @Operation(summary = "Analyze search intent", description = "Analyze user search intent using AI")
    public Response analyzeSearchIntent(@QueryParam("query") String query) {
        
        logger.info("Analyzing search intent for query: {}", query);
        
        try {
            // AI検索アシスタントで検索意図を分析
            String intentAnalysis = searchAssistant.analyzeIntent(
                query, 
                "商品検索・情報検索・比較検索・技術相談",
                "empty_session"
            );
            
            // レスポンス構築
            var response = Map.of(
                "query", query,
                "intentAnalysis", intentAnalysis,
                "suggestedFilters", generateSuggestedFilters(intentAnalysis),
                "recommendedCategories", generateRecommendedCategories(intentAnalysis)
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error analyzing search intent for query: {}", query, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "検索意図分析中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * パーソナライズド検索
     */
    @POST
    @Path("/personalized")
    @Operation(summary = "Personalized search", description = "Personalize search results using AI")
    public Response personalizedSearch(
            @QueryParam("query") String query,
            @QueryParam("userId") String userId,
            Map<String, Object> userProfile) {
        
        logger.info("Performing personalized search for user: {} with query: {}", userId, query);
        
        try {
            // ユーザープロファイルを文字列に変換
            String profileString = convertProfileToString(userProfile);
            
            // AI検索アシスタントでパーソナライズド検索
            String personalizedResults = searchAssistant.personalizeSearch(
                query, 
                profileString, 
                "ユーザー好み・過去の行動・スキルレベル"
            );
            
            // レスポンス構築
            var response = Map.of(
                "query", query,
                "userId", userId,
                "personalizedResults", personalizedResults,
                "personalizationFactors", userProfile,
                "type", "PERSONALIZED_SEARCH"
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error performing personalized search for user: {} with query: {}", userId, query, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "パーソナライズド検索中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 拡張クエリをパース
     */
    private List<String> parseEnhancedQuery(String enhancedQuery) {
        // 実際の実装では、AIの応答をパースして検索候補を抽出
        return List.of(
            enhancedQuery.split("\\n")[0].replace("拡張クエリ: ", "").trim(),
            "関連キーワード候補1",
            "関連キーワード候補2"
        );
    }
    
    /**
     * 同義語をパース
     */
    private List<String> parseSynonyms(String synonyms) {
        // 実際の実装では、AIの応答をパースして同義語リストを抽出
        return List.of(
            synonyms.split("\\n")[0].trim(),
            "同義語候補1",
            "同義語候補2"
        );
    }
    
    /**
     * 検索結果を文字列に変換
     */
    private String convertResultsToString(Map<String, Object> searchResults) {
        return """
            検索結果:
            - 商品数: %s
            - カテゴリ: %s
            - 価格帯: %s
            """.formatted(
                searchResults.getOrDefault("totalCount", "不明"),
                searchResults.getOrDefault("categories", "不明"),
                searchResults.getOrDefault("priceRange", "不明")
            );
    }
    
    /**
     * ユーザープロファイルを文字列に変換
     */
    private String convertProfileToString(Map<String, Object> userProfile) {
        return """
            ユーザープロファイル:
            - スキーレベル: %s
            - 好みのブランド: %s
            - 予算: %s
            - 過去の購入履歴: %s
            """.formatted(
                userProfile.getOrDefault("skillLevel", "不明"),
                userProfile.getOrDefault("preferredBrands", "不明"),
                userProfile.getOrDefault("budget", "不明"),
                userProfile.getOrDefault("purchaseHistory", "不明")
            );
    }
    
    /**
     * 推奨フィルターを生成
     */
    private List<String> generateSuggestedFilters(String intentAnalysis) {
        // 実際の実装では、意図分析結果に基づいてフィルターを生成
        return List.of("価格帯", "ブランド", "レベル", "用途");
    }
    
    /**
     * 推奨カテゴリを生成
     */
    private List<String> generateRecommendedCategories(String intentAnalysis) {
        // 実際の実装では、意図分析結果に基づいてカテゴリを生成
        return List.of("スキー板", "スキーブーツ", "スキーウェア", "アクセサリー");
    }
}
