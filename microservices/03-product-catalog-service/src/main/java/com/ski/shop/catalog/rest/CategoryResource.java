package com.ski.shop.catalog.rest;

import com.ski.shop.catalog.dto.CategoryResponse;
import com.ski.shop.catalog.dto.CategoryWithProductCountResponse;
import com.ski.shop.catalog.dto.CategoryWithProductsResponse;
import com.ski.shop.catalog.service.CategoryService;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.UUID;

/**
 * カテゴリRESTエンドポイント
 */
@Path("/api/v1/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Categories", description = "カテゴリ管理API")
@Blocking
public class CategoryResource {

    @Inject
    CategoryService categoryService;

    @GET
    @Operation(
        summary = "全カテゴリ一覧取得",
        description = "全カテゴリの一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "カテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getAllCategories() {
        return categoryService.getAllCategoriesWithProductCount();
    }

    @GET
    @Path("/root")
    @Operation(
        summary = "ルートカテゴリ一覧取得",
        description = "ルートレベルのカテゴリ一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "ルートカテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getRootCategories() {
        return categoryService.getRootCategoriesWithProductCount();
    }

    @GET
    @Path("/main")
    @Operation(
        summary = "メインカテゴリ一覧取得", 
        description = "メインカテゴリ（レベル0）の一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "メインカテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getMainCategories() {
        return categoryService.getMainCategories();
    }

    @GET
    @Path("/{categoryId}/subcategories")
    @Operation(
        summary = "サブカテゴリ一覧取得",
        description = "指定されたカテゴリのサブカテゴリ一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "サブカテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getSubCategories(@PathParam("categoryId") UUID categoryId) {
        return categoryService.getSubCategories(categoryId);
    }

    @GET
    @Path("/level/{level}")
    @Operation(
        summary = "レベル別カテゴリ取得",
        description = "指定されたレベルのカテゴリ一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "指定レベルのカテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getCategoriesByLevel(@PathParam("level") Integer level) {
        return categoryService.getCategoriesByLevel(level);
    }    @GET
    @Path("/{categoryId}")
    @Operation(
        summary = "カテゴリ詳細取得",
        description = "指定されたIDのカテゴリ詳細を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "カテゴリ詳細",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryResponse.class)
        )
    )
    @APIResponse(responseCode = "404", description = "カテゴリが見つかりません")
    public CategoryResponse getCategory(@PathParam("categoryId") UUID categoryId) {
        return categoryService.getCategory(categoryId);
    }

    @GET
    @Path("/path")
    @Operation(
        summary = "パスでカテゴリ取得",
        description = "指定されたパスのカテゴリ詳細を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "カテゴリ詳細",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryResponse.class)
        )
    )
    @APIResponse(responseCode = "404", description = "カテゴリが見つかりません")
    @APIResponse(responseCode = "400", description = "パスパラメータが必須です")
    public CategoryResponse getCategoryByPath(@QueryParam("path") String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new BadRequestException("Path parameter is required");
        }
        return categoryService.getCategoryByPath(path);
    }

    @GET
    @Path("/{categoryId}/children")
    @Operation(
        summary = "子カテゴリ一覧取得",
        description = "指定されたカテゴリの子カテゴリ一覧を商品数と共に取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "子カテゴリ一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductCountResponse.class)
        )
    )
    public List<CategoryWithProductCountResponse> getChildCategories(
            @PathParam("categoryId") UUID categoryId) {
        return categoryService.getChildCategoriesWithProductCount(categoryId);
    }

    @GET
    @Path("/{categoryId}/products")
    @Operation(
        summary = "カテゴリの商品一覧取得",
        description = "指定されたカテゴリに属する商品一覧を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "カテゴリと商品一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductsResponse.class)
        )
    )
    @APIResponse(responseCode = "404", description = "カテゴリが見つかりません")
    public CategoryWithProductsResponse getCategoryWithProducts(
            @PathParam("categoryId") UUID categoryId,
            @QueryParam("limit") @DefaultValue("10") int limit) {
        return categoryService.getCategoryWithProducts(categoryId, limit);
    }

    @GET
    @Path("/{categoryId}/subcategories/products")
    @Operation(
        summary = "サブカテゴリ毎の商品一覧取得",
        description = "指定されたカテゴリのサブカテゴリ毎に商品一覧を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "サブカテゴリ毎の商品一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductsResponse.class)
        )
    )
    @APIResponse(responseCode = "404", description = "カテゴリが見つかりません")
    public List<CategoryWithProductsResponse> getSubCategoriesWithProducts(
            @PathParam("categoryId") UUID categoryId,
            @QueryParam("limit") @DefaultValue("5") int limit) {
        return categoryService.getSubCategoriesWithProducts(categoryId, limit);
    }

    @GET
    @Path("/{categoryId}/all-products")
    @Operation(
        summary = "カテゴリとサブカテゴリの全商品取得",
        description = "指定されたカテゴリとそのサブカテゴリの全商品一覧を取得します"
    )
    @APIResponse(
        responseCode = "200",
        description = "カテゴリとサブカテゴリの全商品一覧",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = CategoryWithProductsResponse.class)
        )
    )
    @APIResponse(responseCode = "404", description = "カテゴリが見つかりません")
    public CategoryWithProductsResponse getCategoryWithAllProducts(
            @PathParam("categoryId") UUID categoryId,
            @QueryParam("limit") @DefaultValue("50") int limit) {
        return categoryService.getCategoryWithAllProducts(categoryId, limit);
    }
}
