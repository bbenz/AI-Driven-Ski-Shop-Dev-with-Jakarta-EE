import httpClient from '@/services/http/client';
import type {
  Product,
  Category,
  Brand,
  ProductSearchParams,
  ProductSearchResponse,
  ProductReview,
} from '@/types/product';
import type { PaginatedResponse } from '@/types/api';

// API レスポンス用の型定義
interface ApiProduct {
  id: string;
  sku: string;
  name: string;
  shortDescription?: string;
  category: {
    id: string;
    name: string;
    path: string;
  };
  brand: {
    id: string;
    name?: string;
    logoUrl?: string;
    country?: string;
  };
  currentPrice: number;
  basePrice: number;
  discountPercentage: number;
  primaryImageUrl: string;
  inStock: boolean;
  featured: boolean;
  rating?: number;
  reviewCount: number;
  tags: string[];
  createdAt: string;
  onSale: boolean;
}

// API レスポンスをフロントエンド用型に変換する関数
function mapApiProductToProduct(apiProduct: ApiProduct): Product {
  return {
    id: apiProduct.id,
    name: apiProduct.name,
    description: apiProduct.shortDescription || '',
    shortDescription: apiProduct.shortDescription,
    price: apiProduct.currentPrice,
    originalPrice: apiProduct.basePrice,
    discountPercentage: apiProduct.discountPercentage,
    sku: apiProduct.sku,
    categoryId: apiProduct.category.id,
    category: {
      id: apiProduct.category.id,
      name: apiProduct.category.name,
      slug: apiProduct.category.path.replace(/^\//, '').replace(/\//g, '-'),
      description: '',
      children: [],
      isActive: true,
      sortOrder: 0,
      productCount: 0,
    },
    brandId: apiProduct.brand.id,
    brand: apiProduct.brand.name ? {
      id: apiProduct.brand.id,
      name: apiProduct.brand.name,
      slug: apiProduct.brand.name.toLowerCase().replace(/\s+/g, '-'),
      description: '',
      logo: apiProduct.brand.logoUrl,
      isActive: true,
    } : undefined,
    images: apiProduct.primaryImageUrl ? [{
      id: `${apiProduct.id}-primary`,
      url: apiProduct.primaryImageUrl,
      alt: apiProduct.name,
      isPrimary: true,
      sortOrder: 0,
    }] : [],
    attributes: [],
    variants: [],
    inventory: {
      quantity: apiProduct.inStock ? 10 : 0,
      reservedQuantity: 0,
      availableQuantity: apiProduct.inStock ? 10 : 0,
      lowStockThreshold: 5,
      status: apiProduct.inStock ? 'in_stock' : 'out_of_stock' as const,
    },
    specifications: [],
    reviews: [],
    rating: apiProduct.rating || 0,
    reviewCount: apiProduct.reviewCount,
    tags: apiProduct.tags,
    isActive: true,
    isFeatured: apiProduct.featured,
    isOnSale: apiProduct.onSale,
    createdAt: apiProduct.createdAt,
    updatedAt: apiProduct.createdAt,
  };
}

/**
 * 商品関連API
 */
export const productsAPI = {
  /**
   * 商品一覧取得
   */
  async getProducts(params?: ProductSearchParams): Promise<ProductSearchResponse> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params });
    const products = apiProducts.map(mapApiProductToProduct);
    
    return {
      products,
      totalCount: products.length,
      totalPages: 1,
      currentPage: 1,
      filters: [],
      aggregations: {
        minPrice: products.length > 0 ? Math.min(...products.map(p => p.price)) : 0,
        maxPrice: products.length > 0 ? Math.max(...products.map(p => p.price)) : 0,
        avgRating: products.length > 0 ? products.reduce((sum, p) => sum + p.rating, 0) / products.length : 0,
        totalProducts: products.length,
        brands: [],
        categories: []
      }
    };
  },

  /**
   * 商品詳細取得
   */
  async getProduct(id: string): Promise<Product> {
    return await httpClient.get<Product>(`/api/v1/products/${id}`);
  },

  /**
   * 商品検索
   */
  async searchProducts(params: ProductSearchParams): Promise<ProductSearchResponse> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params });
    const products = apiProducts.map(mapApiProductToProduct);
    
    return {
      products,
      totalCount: products.length,
      totalPages: 1,
      currentPage: 1,
      filters: [],
      aggregations: {
        minPrice: products.length > 0 ? Math.min(...products.map(p => p.price)) : 0,
        maxPrice: products.length > 0 ? Math.max(...products.map(p => p.price)) : 0,
        avgRating: products.length > 0 ? products.reduce((sum, p) => sum + p.rating, 0) / products.length : 0,
        totalProducts: products.length,
        brands: [],
        categories: []
      }
    };
  },

  /**
   * 注目商品取得
   */
  async getFeaturedProducts(limit = 10): Promise<Product[]> {
    try {
      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8083'}/api/v1/products/featured`
      );
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      
      const apiProducts: ApiProduct[] = await response.json();
      // limitが指定されている場合は結果を制限
      const limitedProducts = limit && limit < apiProducts.length ? apiProducts.slice(0, limit) : apiProducts;
      return limitedProducts.map(mapApiProductToProduct);
    } catch (error) {
      console.error('getFeaturedProducts error:', error);
      throw error;
    }
  },

  /**
   * SKUで商品取得
   */
  async getProductBySku(sku: string): Promise<Product> {
    return await httpClient.get<Product>(`/api/v1/products/sku/${sku}`);
  },

  /**
   * カテゴリ別商品一覧
   */
  async getProductsByCategory(categoryId: string): Promise<Product[]> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params: { categoryId } });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * ブランド別商品一覧
   */
  async getProductsByBrand(brandId: string): Promise<Product[]> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params: { brandId } });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * 商品推奨取得
   */
  async getRecommendedProducts(productId?: string, limit = 10): Promise<Product[]> {
    const params = productId ? { productId, limit, featured: true } : { limit, featured: true };
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * 人気商品取得
   */
  async getPopularProducts(limit = 10): Promise<Product[]> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params: { limit, featured: true } });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * 新着商品取得
   */
  async getNewProducts(limit = 10): Promise<Product[]> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params: { limit, sort: 'created_desc' } });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * セール商品取得
   */
  async getSaleProducts(limit = 10): Promise<Product[]> {
    const apiProducts = await httpClient.get<ApiProduct[]>('/api/v1/products', { params: { limit, onSale: true } });
    return apiProducts.map(mapApiProductToProduct);
  },

  /**
   * カテゴリ一覧取得
   */
  async getCategories(): Promise<Category[]> {
    return await httpClient.get<Category[]>('/api/v1/categories');
  },

  /**
   * ルートカテゴリ取得
   */
  async getRootCategories(): Promise<Category[]> {
    return await httpClient.get<Category[]>('/api/v1/categories/root');
  },

  /**
   * メインカテゴリ取得
   */
  async getMainCategories(): Promise<Category[]> {
    return await httpClient.get<Category[]>('/api/v1/categories/main');
  },

  /**
   * カテゴリ詳細取得
   */
  async getCategory(id: string): Promise<Category> {
    return await httpClient.get<Category>(`/api/v1/categories/${id}`);
  },

  /**
   * カテゴリの子要素取得
   */
  async getCategoryChildren(categoryId: string): Promise<Category[]> {
    return await httpClient.get<Category[]>(`/api/v1/categories/${categoryId}/children`);
  },

  /**
   * カテゴリのサブカテゴリ取得
   */
  async getCategorySubcategories(categoryId: string): Promise<Category[]> {
    return await httpClient.get<Category[]>(`/api/v1/categories/${categoryId}/subcategories`);
  },

  /**
   * レベル別カテゴリ取得
   */
  async getCategoriesByLevel(level: number): Promise<Category[]> {
    return await httpClient.get<Category[]>(`/api/v1/categories/level/${level}`);
  },

  /**
   * パスでカテゴリ取得
   */
  async getCategoryByPath(path: string): Promise<Category> {
    return await httpClient.get<Category>('/api/v1/categories/path', { params: { path } });
  },

  /**
   * カテゴリ内の商品取得
   */
  async getCategoryProducts(categoryId: string): Promise<{ category: Category; products: Product[] }> {
    return await httpClient.get<{ category: Category; products: Product[] }>(`/api/v1/categories/${categoryId}/products`);
  },

  /**
   * サブカテゴリ毎の商品一覧取得
   */
  async getSubcategoryProducts(categoryId: string): Promise<Array<{ category: Category; products: Product[] }>> {
    return await httpClient.get<Array<{ category: Category; products: Product[] }>>(`/api/v1/categories/${categoryId}/subcategories/products`);
  },

  /**
   * ブランド一覧取得
   */
  async getBrands(): Promise<Brand[]> {
    return await httpClient.get<Brand[]>('/products/brands');
  },

  /**
   * ブランド詳細取得
   */
  async getBrand(id: string): Promise<Brand> {
    return await httpClient.get<Brand>(`/products/brands/${id}`);
  },

  /**
   * 商品レビュー取得
   */
  async getProductReviews(
    productId: string,
    page = 1,
    pageSize = 10
  ): Promise<PaginatedResponse<ProductReview>> {
    return await httpClient.get<PaginatedResponse<ProductReview>>(
      `/products/${productId}/reviews`,
      { params: { page, pageSize } }
    );
  },

  /**
   * 商品レビュー投稿
   */
  async createProductReview(
    productId: string,
    review: {
      rating: number;
      title: string;
      comment: string;
      images?: File[];
    }
  ): Promise<ProductReview> {
    const formData = new FormData();
    formData.append('rating', review.rating.toString());
    formData.append('title', review.title);
    formData.append('comment', review.comment);
    
    if (review.images) {
      review.images.forEach((image, index) => {
        formData.append(`images[${index}]`, image);
      });
    }

    return await httpClient.upload<ProductReview>(
      `/products/${productId}/reviews`,
      formData
    );
  },

  /**
   * 商品レビュー更新
   */
  async updateProductReview(
    productId: string,
    reviewId: string,
    updates: Partial<{
      rating: number;
      title: string;
      comment: string;
    }>
  ): Promise<ProductReview> {
    return await httpClient.patch<ProductReview>(
      `/products/${productId}/reviews/${reviewId}`,
      updates
    );
  },

  /**
   * 商品レビュー削除
   */
  async deleteProductReview(productId: string, reviewId: string): Promise<void> {
    return await httpClient.delete<void>(`/products/${productId}/reviews/${reviewId}`);
  },

  /**
   * レビューに「役に立った」投票
   */
  async voteReviewHelpful(
    productId: string,
    reviewId: string,
    helpful: boolean
  ): Promise<void> {
    return await httpClient.post<void>(
      `/products/${productId}/reviews/${reviewId}/vote`,
      { helpful }
    );
  },

  /**
   * 商品比較
   */
  async compareProducts(productIds: string[]): Promise<{
    products: Product[];
    comparison: Record<string, unknown>;
  }> {
    return await httpClient.post<{
      products: Product[];
      comparison: Record<string, unknown>;
    }>('/products/compare', { productIds });
  },

  /**
   * 商品在庫確認
   */
  async checkStock(productId: string, variantId?: string): Promise<{
    inStock: boolean;
    quantity: number;
    status: string;
  }> {
    const params = variantId ? { variantId } : {};
    return await httpClient.get<{
      inStock: boolean;
      quantity: number;
      status: string;
    }>(`/products/${productId}/stock`, { params });
  },

  /**
   * 商品価格履歴取得
   */
  async getPriceHistory(productId: string, days = 30): Promise<Array<{
    date: string;
    price: number;
  }>> {
    return await httpClient.get<Array<{
      date: string;
      price: number;
    }>>(`/products/${productId}/price-history`, { params: { days } });
  },

  /**
   * 商品の関連商品取得
   */
  async getRelatedProducts(productId: string, limit = 10): Promise<Product[]> {
    return await httpClient.get<Product[]>(`/products/${productId}/related`, {
      params: { limit },
    });
  },

  /**
   * ウィッシュリストに追加
   */
  async addToWishlist(productId: string): Promise<void> {
    return await httpClient.post<void>('/products/wishlist', { productId });
  },

  /**
   * ウィッシュリストから削除
   */
  async removeFromWishlist(productId: string): Promise<void> {
    return await httpClient.delete<void>(`/products/wishlist/${productId}`);
  },

  /**
   * ウィッシュリスト取得
   */
  async getWishlist(): Promise<Product[]> {
    return await httpClient.get<Product[]>('/products/wishlist');
  },

  /**
   * 商品閲覧履歴取得
   */
  async getViewHistory(limit = 20): Promise<Product[]> {
    return await httpClient.get<Product[]>('/products/view-history', {
      params: { limit },
    });
  },

  /**
   * 商品閲覧記録
   */
  async recordView(productId: string): Promise<void> {
    return await httpClient.post<void>('/products/view-history', { productId });
  },
};
