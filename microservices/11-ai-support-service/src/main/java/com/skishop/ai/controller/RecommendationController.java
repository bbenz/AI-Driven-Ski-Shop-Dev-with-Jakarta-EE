package com.skishop.ai.controller;

import com.skishop.ai.service.ProductRecommendationAssistant;
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
 * Product Recommendation API Controller
 * 
 * <p>LangChain4j 1.1.0 + Azure OpenAI を使用した商品推薦機能</p>
 * 
 * @since 1.0.0
 */
@Path("/api/v1/recommendations")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Recommendation API", description = "AI Product Recommendation API")
public class RecommendationController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);
    
    private final ProductRecommendationAssistant recommendationAssistant;
    
    /**
     * コンストラクタ
     * 
     * @param recommendationAssistant 商品推薦アシスタント
     */
    @Inject
    public RecommendationController(ProductRecommendationAssistant recommendationAssistant) {
        this.recommendationAssistant = recommendationAssistant;
    }
    
    /**
     * ユーザープロファイルベース商品推薦
     */
    @POST
    @Path("/profile-based")
    @Operation(summary = "Profile-based recommendation", description = "Recommend products based on user profile")
    public Response getProfileBasedRecommendations(
            @QueryParam("userId") String userId,
            Map<String, Object> profileData) {
        
        logger.info("Generating profile-based recommendations for user: {}", userId);
        
        try {
            // プロファイルデータを文字列に変換
            String profileSummary = buildProfileSummary(profileData);
            
            // AI推薦エンジンで推薦商品を取得
            String recommendations = recommendationAssistant.recommendForProfile(
                profileSummary, 
                "スキー用品", 
                5
            );
            
            // レスポンス構築
            var response = Map.of(
                "userId", userId,
                "recommendations", recommendations,
                "type", "PROFILE_BASED",
                "profileData", profileData
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error generating profile-based recommendations for user: {}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "推薦の生成中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 行動ベース商品推薦
     */
    @POST
    @Path("/behavior-based")
    @Operation(summary = "Behavior-based recommendation", description = "Recommend products based on user behavior")
    public Response getBehaviorBasedRecommendations(
            @QueryParam("userId") String userId,
            Map<String, Object> behaviorData) {
        
        logger.info("Generating behavior-based recommendations for user: {}", userId);
        
        try {
            // 行動データを文字列に変換
            String behaviorSummary = buildBehaviorSummary(behaviorData);
            
            // AI推薦エンジンで推薦商品を取得
            String recommendations = recommendationAssistant.recommendForBehavior(
                behaviorSummary, 
                "最近の閲覧・購入履歴", 
                5
            );
            
            // レスポンス構築
            var response = Map.of(
                "userId", userId,
                "recommendations", recommendations,
                "type", "BEHAVIOR_BASED",
                "behaviorData", behaviorData
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error generating behavior-based recommendations for user: {}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "推薦の生成中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 類似ユーザーベース商品推薦
     */
    @POST
    @Path("/collaborative")
    @Operation(summary = "Collaborative filtering recommendation", description = "Recommend products based on similar users")
    public Response getCollaborativeRecommendations(
            @QueryParam("userId") String userId,
            @QueryParam("similarUserIds") List<String> similarUserIds) {
        
        logger.info("Generating collaborative recommendations for user: {}", userId);
        
        try {
            // 類似ユーザー情報を文字列に変換
            String similarUsersInfo = String.join(", ", similarUserIds);
            
            // AI推薦エンジンで推薦商品を取得
            String recommendations = recommendationAssistant.recommendForSimilarUsers(
                similarUsersInfo, 
                "類似ユーザーの購入履歴", 
                5
            );
            
            // レスポンス構築
            var response = Map.of(
                "userId", userId,
                "recommendations", recommendations,
                "type", "COLLABORATIVE_FILTERING",
                "similarUserIds", similarUserIds
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error generating collaborative recommendations for user: {}", userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "推薦の生成中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * 商品組み合わせ推薦
     */
    @POST
    @Path("/bundle")
    @Operation(summary = "Product bundle recommendation", description = "Recommend product bundles")
    public Response getBundleRecommendations(
            @QueryParam("baseProductId") String baseProductId,
            @QueryParam("userId") String userId) {
        
        logger.info("Generating bundle recommendations for product: {} and user: {}", baseProductId, userId);
        
        try {
            // ベース商品情報（実際の実装では商品情報を取得）
            String baseProductInfo = "商品ID: " + baseProductId;
            
            // AI推薦エンジンで商品組み合わせを取得
            String bundleRecommendations = recommendationAssistant.recommendBundles(
                baseProductInfo, 
                "関連商品・アクセサリー", 
                3
            );
            
            // レスポンス構築
            var response = Map.of(
                "baseProductId", baseProductId,
                "userId", userId,
                "bundleRecommendations", bundleRecommendations,
                "type", "PRODUCT_BUNDLE"
            );
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.error("Error generating bundle recommendations for product: {} and user: {}", baseProductId, userId, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "推薦の生成中にエラーが発生しました"))
                    .build();
        }
    }
    
    /**
     * プロファイルデータサマリー構築
     */
    private String buildProfileSummary(Map<String, Object> profileData) {
        return """
            ユーザープロファイル:
            - スキーレベル: %s
            - 好みのスタイル: %s
            - 予算: %s
            - 体型: %s
            """.formatted(
                profileData.getOrDefault("skillLevel", "不明"),
                profileData.getOrDefault("skiStyle", "不明"),
                profileData.getOrDefault("budget", "不明"),
                profileData.getOrDefault("physique", "不明")
            );
    }
    
    /**
     * 行動データサマリー構築
     */
    private String buildBehaviorSummary(Map<String, Object> behaviorData) {
        return """
            ユーザー行動データ:
            - 最近閲覧した商品: %s
            - 過去の購入履歴: %s
            - 検索履歴: %s
            - お気に入り商品: %s
            """.formatted(
                behaviorData.getOrDefault("recentViews", "なし"),
                behaviorData.getOrDefault("purchaseHistory", "なし"),
                behaviorData.getOrDefault("searchHistory", "なし"),
                behaviorData.getOrDefault("favorites", "なし")
            );
    }
}
