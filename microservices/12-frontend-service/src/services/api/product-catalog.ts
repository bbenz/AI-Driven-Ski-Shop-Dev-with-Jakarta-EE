/**
 * Product Catalog Service API Client
 * 
 * Product Catalog Serviceへの直接アクセス用APIクライアント
 * 将来的にAPI Gateway経由に切り替える際は、BASE_URLの変更だけで対応可能
 */

import axios, { AxiosInstance } from 'axios';

// Product Catalog Serviceの直接URL（将来的にAPI Gateway経由に変更予定）
const PRODUCT_CATALOG_BASE_URL = process.env.NEXT_PUBLIC_PRODUCT_CATALOG_URL || 'http://localhost:8083';

// API Gateway経由の場合は以下を使用
// const API_GATEWAY_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';

/**
 * APIベースURLを取得
 * 環境変数NEXT_PUBLIC_USE_API_GATEWAYがtrueの場合はAPI Gateway経由、
 * それ以外はProduct Catalog Serviceに直接アクセス
 */
const getBaseUrl = (): string => {
  const useApiGateway = process.env.NEXT_PUBLIC_USE_API_GATEWAY === 'true';
  
  if (useApiGateway) {
    // API Gateway経由の場合
    return process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
  } else {
    // Product Catalog Serviceに直接アクセス
    return PRODUCT_CATALOG_BASE_URL;
  }
};

// カテゴリ関連の型定義
export interface Category {
  id: string;
  name: string;
  description: string;
  path: string;
  level: number;
  sortOrder: number;
  imageUrl: string | null;
  productCount: number;
  active: boolean;
  parent?: {
    id: string;
    path: string;
  };
  children?: Category[];
  createdAt: string;
  updatedAt: string;
  root: boolean;
  subCategory: boolean;
  subCategoryCount: number;
}

// 商品関連の型定義
export interface Product {
  id: string;
  sku: string;
  name: string;
  description: string;
  shortDescription?: string;
  category: {
    id: string;
    path: string;
  };
  brand: {
    id: string;
    name?: string;
    country?: string;
  };
  basePrice: number;
  currentPrice: number;
  discountRate?: number;
  tags: string[];
  imageUrls?: string[];
  featured?: boolean;
  onSale: boolean;
  createdAt: string;
  updatedAt: string;
}

// 商品検索フィルター
export interface ProductSearchParams {
  search?: string;
  q?: string;
  categoryId?: string;
  categoryIds?: string[]; // 複数カテゴリIDをサポート
  includeSubcategories?: boolean; // サブカテゴリを含めるかどうか
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  sort?: 'price_asc' | 'price_desc' | 'name_asc' | 'name_desc';
  sortBy?: 'price' | 'name' | 'createdAt';
  sortOrder?: 'asc' | 'desc';
  featured?: boolean;
  onSale?: boolean;
  page?: number;
  size?: number;
}

// カテゴリ内商品情報
export interface CategoryWithProducts {
  id: string;
  name: string;
  description: string;
  path: string;
  level: number;
  sortOrder: number;
  imageUrl: string | null;
  active: boolean;
  productCount: number;
  products: Product[];
}

// サブカテゴリ内商品情報
export interface SubCategoryProducts {
  id: string;
  name: string;
  description: string;
  path: string;
  level: number;
  products: Product[];
}

// ヘルスチェック用の型定義
export interface HealthCheck {
  name: string;
  status: string;
  data?: Record<string, unknown>;
}

export interface HealthResponse {
  status: string;
  checks: HealthCheck[];
}

/**
 * Product Catalog Service専用HTTPクライアント
 */
class ProductCatalogHttpClient {
  private readonly axiosInstance: AxiosInstance;

  constructor(baseURL: string) {
    this.axiosInstance = axios.create({
      baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors(): void {
    // リクエストインターセプター
    this.axiosInstance.interceptors.request.use(
      (config) => {
        console.log(`[ProductCatalog] ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        console.error('[ProductCatalog] Request error:', error);
        return Promise.reject(new Error('Request failed'));
      }
    );

    // レスポンスインターセプター
    this.axiosInstance.interceptors.response.use(
      (response) => {
        console.log(`[ProductCatalog] ${response.status} ${response.config.url}`);
        return response;
      },
      (error) => {
        console.error('[ProductCatalog] Response error:', error);
        return Promise.reject(new Error('Response failed'));
      }
    );
  }

  public updateBaseURL(baseURL: string): void {
    this.axiosInstance.defaults.baseURL = baseURL;
  }

  public getAxiosInstance(): AxiosInstance {
    return this.axiosInstance;
  }
}

/**
 * Product Catalog Service API Client
 */
export class ProductCatalogApi {
  private readonly httpClient: ProductCatalogHttpClient;

  constructor() {
    this.httpClient = new ProductCatalogHttpClient(getBaseUrl());
  }

  /**
   * API Gateway経由に切り替える
   */
  public switchToApiGateway(): void {
    const gatewayUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
    this.httpClient.updateBaseURL(gatewayUrl);
  }

  /**
   * Product Catalog Service直接アクセスに切り替える
   */
  public switchToDirectAccess(): void {
    this.httpClient.updateBaseURL(PRODUCT_CATALOG_BASE_URL);
  }

  // =================
  // カテゴリ関連API
  // =================

  /**
   * 全カテゴリ一覧取得
   */
  async getAllCategories(): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>('/api/v1/categories');
    return response.data;
  }

  /**
   * ルートカテゴリ一覧取得
   */
  async getRootCategories(): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>('/api/v1/categories/root');
    return response.data;
  }

  /**
   * メインカテゴリ一覧取得
   */
  async getMainCategories(): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>('/api/v1/categories/main');
    return response.data;
  }

  /**
   * パスでカテゴリ取得
   */
  async getCategoryByPath(path: string): Promise<Category> {
    const response = await this.httpClient.getAxiosInstance().get<Category>(
      `/api/v1/categories/path?path=${encodeURIComponent(path)}`
    );
    return response.data;
  }

  /**
   * カテゴリ詳細取得
   */
  async getCategoryById(categoryId: string): Promise<Category> {
    const response = await this.httpClient.getAxiosInstance().get<Category>(`/api/v1/categories/${categoryId}`);
    return response.data;
  }

  /**
   * 子カテゴリ一覧取得
   */
  async getCategoryChildren(categoryId: string): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>(`/api/v1/categories/${categoryId}/children`);
    return response.data;
  }

  /**
   * レベル別カテゴリ取得
   */
  async getCategoriesByLevel(level: number): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>(`/api/v1/categories/level/${level}`);
    return response.data;
  }

  /**
   * サブカテゴリ一覧取得
   */
  async getCategorySubcategories(categoryId: string): Promise<Category[]> {
    const response = await this.httpClient.getAxiosInstance().get<Category[]>(`/api/v1/categories/${categoryId}/subcategories`);
    return response.data;
  }

  /**
   * カテゴリの商品一覧取得
   */
  async getCategoryProducts(categoryId: string): Promise<CategoryWithProducts> {
    const response = await this.httpClient.getAxiosInstance().get<CategoryWithProducts>(`/api/v1/categories/${categoryId}/products`);
    return response.data;
  }

  /**
   * サブカテゴリ毎の商品一覧取得
   */
  async getSubcategoryProducts(categoryId: string): Promise<SubCategoryProducts[]> {
    const response = await this.httpClient.getAxiosInstance().get<SubCategoryProducts[]>(`/api/v1/categories/${categoryId}/subcategories/products`);
    return response.data;
  }

  /**
   * カテゴリとサブカテゴリの全商品取得（新機能）
   */
  async getCategoryAllProducts(categoryId: string, limit?: number): Promise<CategoryWithProducts> {
    const queryParams = new URLSearchParams();
    if (limit !== undefined) queryParams.append('limit', limit.toString());
    
    const url = queryParams.toString() 
      ? `/api/v1/categories/${categoryId}/all-products?${queryParams.toString()}`
      : `/api/v1/categories/${categoryId}/all-products`;
      
    const response = await this.httpClient.getAxiosInstance().get<CategoryWithProducts>(url);
    return response.data;
  }

  // =================
  // 商品関連API
  // =================

  /**
   * 商品一覧・検索
   */
  async getProducts(params?: ProductSearchParams): Promise<Product[]> {
    const queryParams = new URLSearchParams();
    
    if (params?.search) queryParams.append('search', params.search);
    if (params?.q) queryParams.append('q', params.q);
    if (params?.categoryId) queryParams.append('categoryId', params.categoryId);
    
    // 複数カテゴリIDをサポート
    if (params?.categoryIds && params.categoryIds.length > 0) {
      queryParams.append('categoryIds', params.categoryIds.join(','));
    }
    
    // サブカテゴリを含めるオプション
    if (params?.includeSubcategories !== undefined) {
      queryParams.append('includeSubcategories', params.includeSubcategories.toString());
    }
    
    if (params?.brandId) queryParams.append('brandId', params.brandId);
    if (params?.minPrice !== undefined) queryParams.append('minPrice', params.minPrice.toString());
    if (params?.maxPrice !== undefined) queryParams.append('maxPrice', params.maxPrice.toString());
    if (params?.sort) queryParams.append('sort', params.sort);
    if (params?.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params?.sortOrder) queryParams.append('sortOrder', params.sortOrder);
    if (params?.featured !== undefined) queryParams.append('featured', params.featured.toString());
    if (params?.onSale !== undefined) queryParams.append('onSale', params.onSale.toString());
    if (params?.page !== undefined) queryParams.append('page', params.page.toString());
    if (params?.size !== undefined) queryParams.append('size', params.size.toString());

    const url = queryParams.toString() 
      ? `/api/v1/products?${queryParams.toString()}`
      : '/api/v1/products';

    const response = await this.httpClient.getAxiosInstance().get<Product[]>(url);
    return response.data;
  }

  /**
   * 注目商品一覧
   */
  async getFeaturedProducts(): Promise<Product[]> {
    const response = await this.httpClient.getAxiosInstance().get<Product[]>('/api/v1/products?featured=true');
    return response.data;
  }

  /**
   * 商品詳細取得
   */
  async getProductById(productId: string): Promise<Product> {
    const response = await this.httpClient.getAxiosInstance().get<Product>(`/api/v1/products/${productId}`);
    return response.data;
  }

  /**
   * SKUで商品取得
   */
  async getProductBySku(sku: string): Promise<Product> {
    const response = await this.httpClient.getAxiosInstance().get<Product>(`/api/v1/products/sku/${sku}`);
    return response.data;
  }

  /**
   * カテゴリ別商品一覧
   */
  async getProductsByCategory(categoryId: string): Promise<Product[]> {
    const response = await this.httpClient.getAxiosInstance().get<Product[]>(`/api/v1/products/category/${categoryId}`);
    return response.data;
  }

  /**
   * ブランド別商品一覧
   */
  async getProductsByBrand(brandId: string): Promise<Product[]> {
    const response = await this.httpClient.getAxiosInstance().get<Product[]>(`/api/v1/products/brand/${brandId}`);
    return response.data;
  }

  /**
   * サブカテゴリ毎の商品一覧取得
   */
  async getSubCategoriesWithProducts(categoryId: string, limit?: number): Promise<CategoryWithProducts[]> {
    const queryParams = new URLSearchParams();
    if (limit !== undefined) queryParams.append('limit', limit.toString());
    
    const queryString = queryParams.toString();
    const url = `/api/v1/categories/${categoryId}/subcategories/products` + (queryString ? `?${queryString}` : '');
    const response = await this.httpClient.getAxiosInstance().get<CategoryWithProducts[]>(url);
    return response.data;
  }

  /**
   * カテゴリとサブカテゴリの全商品取得
   */
  async getCategoryWithAllProducts(categoryId: string, limit?: number): Promise<CategoryWithProducts> {
    const queryParams = new URLSearchParams();
    if (limit !== undefined) queryParams.append('limit', limit.toString());
    
    const queryString = queryParams.toString();
    const url = `/api/v1/categories/${categoryId}/all-products` + (queryString ? `?${queryString}` : '');
    const response = await this.httpClient.getAxiosInstance().get<CategoryWithProducts>(url);
    return response.data;
  }

  // =================
  // システム関連API
  // =================

  /**
   * ヘルスチェック
   */
  async getHealth(): Promise<HealthResponse> {
    const response = await this.httpClient.getAxiosInstance().get<HealthResponse>('/q/health');
    return response.data;
  }

  /**
   * OpenAPI仕様書取得
   */
  async getOpenApiSpec(): Promise<string> {
    const response = await this.httpClient.getAxiosInstance().get<string>('/q/openapi');
    return response.data;
  }
}

// デフォルトインスタンス
export const productCatalogApi = new ProductCatalogApi();

// 便利関数
export const switchToApiGateway = () => {
  productCatalogApi.switchToApiGateway();
};

export const switchToDirectAccess = () => {
  productCatalogApi.switchToDirectAccess();
};
