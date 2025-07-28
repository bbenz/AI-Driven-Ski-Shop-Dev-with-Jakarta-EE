package com.ski.shop.catalog.rest;

import com.ski.shop.catalog.domain.DifficultyLevel;
import com.ski.shop.catalog.domain.SkiType;
import com.ski.shop.catalog.dto.ProductCreateRequest;
import com.ski.shop.catalog.dto.ProductResponse;
import com.ski.shop.catalog.dto.ProductSummaryResponse;
import com.ski.shop.catalog.service.ProductService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 商品管理REST API
 */
@Path("/api/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "商品管理API")
public class ProductResource {

    @Inject
    ProductService productService;

    @GET
    @Operation(summary = "商品一覧・検索", description = "検索条件に基づいて商品一覧を取得します")
    @APIResponse(
        responseCode = "200",
        description = "商品一覧",
        content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ProductSummaryResponse.class))
    )
    public List<ProductSummaryResponse> searchProducts(
            @Parameter(description = "検索キーワード")
            @QueryParam("q") String keyword,
            
            @Parameter(description = "検索キーワード（searchパラメータ）")
            @QueryParam("search") String searchKeyword,
            
            @Parameter(description = "カテゴリID")
            @QueryParam("categoryId") UUID categoryId,
            
            @Parameter(description = "カテゴリIDリスト（カンマ区切り）")
            @QueryParam("categoryIds") String categoryIds,
            
            @Parameter(description = "サブカテゴリを含めるかどうか")
            @QueryParam("includeSubcategories") @DefaultValue("false") boolean includeSubcategories,
            
            @Parameter(description = "ブランドID") 
            @QueryParam("brandId") UUID brandId,
            
            @Parameter(description = "スキータイプ")
            @QueryParam("skiType") SkiType skiType,
            
            @Parameter(description = "難易度レベル")
            @QueryParam("difficultyLevel") DifficultyLevel difficultyLevel,
            
            @Parameter(description = "最低価格")
            @QueryParam("minPrice") Double minPrice,
            
            @Parameter(description = "最高価格")
            @QueryParam("maxPrice") Double maxPrice,
            
            @Parameter(description = "ソート順", schema = @Schema(enumeration = {"name_asc", "name_desc", "price_asc", "price_desc", "created_desc", "popularity"}))
            @QueryParam("sort") @DefaultValue("created_desc") String sort,
            
            @Parameter(description = "ソートフィールド", schema = @Schema(enumeration = {"name", "price", "created", "popularity"}))
            @QueryParam("sortBy") String sortBy,
            
            @Parameter(description = "ソート方向", schema = @Schema(enumeration = {"asc", "desc"}))
            @QueryParam("sortOrder") String sortOrder,
            
            @Parameter(description = "ページ番号")
            @QueryParam("page") @DefaultValue("0") int page,
            
            @Parameter(description = "1ページあたりの件数")
            @QueryParam("size") @DefaultValue("20") int size) {

        // keywordとsearchKeywordのどちらかが指定されていれば使用
        String effectiveKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword : searchKeyword;
        
        // sortByとsortOrderからsort文字列を構築
        String effectiveSort = sort;
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            String order = (sortOrder != null && sortOrder.equalsIgnoreCase("asc")) ? "asc" : "desc";
            effectiveSort = sortBy + "_" + order;
        }
        
        // categoryIdsを解析してUUIDリストに変換
        List<UUID> categoryIdList = new ArrayList<>();
        if (categoryIds != null && !categoryIds.trim().isEmpty()) {
            String[] idArray = categoryIds.split(",");
            for (String id : idArray) {
                try {
                    categoryIdList.add(UUID.fromString(id.trim()));
                } catch (IllegalArgumentException e) {
                    // 無効なUUIDは無視
                }
            }
        }
        
        return productService.searchProducts(effectiveKeyword, categoryId, categoryIdList, includeSubcategories, brandId, skiType, difficultyLevel, minPrice, maxPrice, effectiveSort, page, size);
    }

    @GET
    @Path("/featured")
    @Operation(summary = "注目商品一覧", description = "注目商品の一覧を取得します")
    @APIResponse(
        responseCode = "200",
        description = "注目商品一覧",
        content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ProductSummaryResponse.class))
    )
    public List<ProductSummaryResponse> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    @GET
    @Path("/{productId}")
    @Operation(summary = "商品詳細取得", description = "指定されたIDの商品詳細を取得します")
    @APIResponse(
        responseCode = "200",
        description = "商品詳細",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @APIResponse(responseCode = "404", description = "商品が見つかりません")
    public ProductResponse getProduct(
            @Parameter(description = "商品ID", required = true)
            @PathParam("productId") UUID productId) {
        return productService.getProduct(productId);
    }

    @GET
    @Path("/sku/{sku}")
    @Operation(summary = "SKUで商品取得", description = "指定されたSKUの商品詳細を取得します")
    @APIResponse(
        responseCode = "200",
        description = "商品詳細",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @APIResponse(responseCode = "404", description = "商品が見つかりません")
    public ProductResponse getProductBySku(
            @Parameter(description = "商品SKU", required = true)
            @PathParam("sku") String sku) {
        return productService.getProductBySku(sku);
    }

    @GET
    @Path("/category/{categoryId}")
    @Operation(summary = "カテゴリ別商品一覧", description = "指定されたカテゴリの商品一覧を取得します")
    @APIResponse(
        responseCode = "200",
        description = "カテゴリ別商品一覧",
        content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ProductSummaryResponse.class))
    )
    public List<ProductSummaryResponse> getProductsByCategory(
            @Parameter(description = "カテゴリID", required = true)
            @PathParam("categoryId") UUID categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GET
    @Path("/brand/{brandId}")
    @Operation(summary = "ブランド別商品一覧", description = "指定されたブランドの商品一覧を取得します")
    @APIResponse(
        responseCode = "200",
        description = "ブランド別商品一覧",
        content = @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = ProductSummaryResponse.class))
    )
    public List<ProductSummaryResponse> getProductsByBrand(
            @Parameter(description = "ブランドID", required = true)
            @PathParam("brandId") UUID brandId) {
        return productService.getProductsByBrand(brandId);
    }

    @POST
    @Operation(summary = "商品登録", description = "新しい商品を登録します")
    @APIResponse(
        responseCode = "201",
        description = "商品登録成功",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @APIResponse(responseCode = "400", description = "リクエストが不正です")
    @APIResponse(responseCode = "409", description = "SKUが重複しています")
    public Response createProduct(@Valid ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return Response.created(URI.create("/api/v1/products/" + response.id()))
                .entity(response)
                .build();
    }

    @PUT
    @Path("/{productId}")
    @Operation(summary = "商品更新", description = "指定されたIDの商品を更新します")
    @APIResponse(
        responseCode = "200",
        description = "商品更新成功",
        content = @Content(schema = @Schema(implementation = ProductResponse.class))
    )
    @APIResponse(responseCode = "400", description = "リクエストが不正です")
    @APIResponse(responseCode = "404", description = "商品が見つかりません")
    @APIResponse(responseCode = "409", description = "SKUが重複しています")
    public ProductResponse updateProduct(
            @Parameter(description = "商品ID", required = true)
            @PathParam("productId") UUID productId,
            @Valid ProductCreateRequest request) {
        return productService.updateProduct(productId, request);
    }

    @DELETE
    @Path("/{productId}")
    @Operation(summary = "商品削除", description = "指定されたIDの商品を削除します")
    @APIResponse(responseCode = "204", description = "商品削除成功")
    @APIResponse(responseCode = "404", description = "商品が見つかりません")
    public Response deleteProduct(
            @Parameter(description = "商品ID", required = true)
            @PathParam("productId") UUID productId) {
        productService.deleteProduct(productId);
        return Response.noContent().build();
    }
}
