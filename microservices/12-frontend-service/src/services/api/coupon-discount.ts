import httpClient from '../http/client';

export interface Coupon {
  couponId: string;
  code: string;
  name: string;
  description?: string;
  discountType: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING' | 'BUY_X_GET_Y';
  discountValue: number;
  minimumOrderAmount?: number;
  maximumDiscountAmount?: number;
  status: 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'USED';
  validFrom: string;
  validTo: string;
  usageLimit?: number;
  usageCount: number;
  perUserLimit?: number;
  applicableCategories?: string[];
  applicableProducts?: string[];
  excludedCategories?: string[];
  excludedProducts?: string[];
  isStackable: boolean;
  priority: number;
  metadata?: {
    campaignId?: string;
    source?: string;
    channel?: string;
  };
  createdAt: string;
  updatedAt: string;
}

export interface CouponUsage {
  usageId: string;
  couponId: string;
  userId: string;
  orderId?: string;
  discountAmount: number;
  usedAt: string;
  metadata?: {
    originalOrderAmount?: number;
    finalOrderAmount?: number;
  };
}

export interface ValidateCouponRequest {
  code: string;
  userId?: string;
  orderAmount?: number;
  items?: Array<{
    productId: string;
    categoryId: string;
    quantity: number;
    price: number;
  }>;
}

export interface ValidateCouponResponse {
  isValid: boolean;
  coupon?: Coupon;
  discountAmount?: number;
  finalAmount?: number;
  errorMessage?: string;
  errorCode?: 'COUPON_NOT_FOUND' | 'COUPON_EXPIRED' | 'COUPON_INACTIVE' | 'USAGE_LIMIT_EXCEEDED' | 'MINIMUM_ORDER_NOT_MET' | 'NOT_APPLICABLE';
}

export interface ApplyCouponRequest {
  code: string;
  userId: string;
  orderAmount: number;
  items: Array<{
    productId: string;
    categoryId: string;
    quantity: number;
    price: number;
  }>;
}

export interface ApplyCouponResponse {
  success: boolean;
  coupon?: Coupon;
  discountAmount?: number;
  finalAmount?: number;
  usageId?: string;
  errorMessage?: string;
  errorCode?: string;
}

export interface CouponSearchParams {
  status?: 'ACTIVE' | 'INACTIVE' | 'EXPIRED' | 'USED';
  discountType?: 'PERCENTAGE' | 'FIXED_AMOUNT' | 'FREE_SHIPPING' | 'BUY_X_GET_Y';
  validDate?: string;
  categoryId?: string;
  productId?: string;
  minimumOrderAmount?: number;
  page?: number;
  limit?: number;
  sortBy?: 'createdAt' | 'validFrom' | 'validTo' | 'priority';
  sortOrder?: 'asc' | 'desc';
}

export interface CouponListResponse {
  coupons: Coupon[];
  pagination: {
    page: number;
    limit: number;
    total: number;
    totalPages: number;
  };
}

class CouponDiscountService {
  constructor() {
    // HttpClientはシングルトンとして使用
  }

  /**
   * クーポンコードを検証
   */
  async validateCoupon(request: ValidateCouponRequest): Promise<ValidateCouponResponse> {
    return httpClient.post('/coupon-discount-service/api/coupons/validate', request);
  }

  /**
   * クーポンを適用
   */
  async applyCoupon(request: ApplyCouponRequest): Promise<ApplyCouponResponse> {
    return httpClient.post('/coupon-discount-service/api/coupons/apply', request);
  }

  /**
   * 利用可能なクーポン一覧を取得
   */
  async getAvailableCoupons(params?: CouponSearchParams): Promise<CouponListResponse> {
    const searchParams = new URLSearchParams();
    if (params?.status) searchParams.append('status', params.status);
    if (params?.discountType) searchParams.append('discountType', params.discountType);
    if (params?.validDate) searchParams.append('validDate', params.validDate);
    if (params?.categoryId) searchParams.append('categoryId', params.categoryId);
    if (params?.productId) searchParams.append('productId', params.productId);
    if (params?.minimumOrderAmount) searchParams.append('minimumOrderAmount', params.minimumOrderAmount.toString());
    if (params?.page) searchParams.append('page', params.page.toString());
    if (params?.limit) searchParams.append('limit', params.limit.toString());
    if (params?.sortBy) searchParams.append('sortBy', params.sortBy);
    if (params?.sortOrder) searchParams.append('sortOrder', params.sortOrder);

    const queryString = searchParams.toString();
    const url = `/coupon-discount-service/api/coupons${queryString ? `?${queryString}` : ''}`;
    
    return httpClient.get(url);
  }

  /**
   * クーポンコードで詳細を取得
   */
  async getCouponByCode(code: string): Promise<Coupon> {
    return httpClient.get(`/coupon-discount-service/api/coupons/code/${encodeURIComponent(code)}`);
  }

  /**
   * ユーザーのクーポン使用履歴を取得
   */
  async getUserCouponUsage(
    userId: string,
    options?: {
      page?: number;
      limit?: number;
      dateFrom?: string;
      dateTo?: string;
    }
  ): Promise<{
    usages: CouponUsage[];
    pagination: {
      page: number;
      limit: number;
      total: number;
      totalPages: number;
    };
  }> {
    const params = new URLSearchParams();
    if (options?.page) params.append('page', options.page.toString());
    if (options?.limit) params.append('limit', options.limit.toString());
    if (options?.dateFrom) params.append('dateFrom', options.dateFrom);
    if (options?.dateTo) params.append('dateTo', options.dateTo);

    return httpClient.get(`/coupon-discount-service/api/users/${userId}/coupon-usage?${params.toString()}`);
  }

  /**
   * 特定カテゴリ・商品に適用可能なクーポンを取得
   */
  async getApplicableCoupons(
    categoryIds?: string[],
    productIds?: string[]
  ): Promise<Coupon[]> {
    const params = new URLSearchParams();
    if (categoryIds) {
      categoryIds.forEach(id => params.append('categoryId', id));
    }
    if (productIds) {
      productIds.forEach(id => params.append('productId', id));
    }

    return httpClient.get(`/coupon-discount-service/api/coupons/applicable?${params.toString()}`);
  }

  /**
   * クーポンの使用を記録（注文完了時）
   */
  async recordCouponUsage(
    couponId: string,
    userId: string,
    orderId: string,
    discountAmount: number,
    metadata?: {
      originalOrderAmount?: number;
      finalOrderAmount?: number;
    }
  ): Promise<CouponUsage> {
    return httpClient.post('/coupon-discount-service/api/coupons/usage', {
      couponId,
      userId,
      orderId,
      discountAmount,
      metadata
    });
  }

  /**
   * 管理者用：クーポン作成
   */
  async createCoupon(coupon: Omit<Coupon, 'couponId' | 'usageCount' | 'createdAt' | 'updatedAt'>): Promise<Coupon> {
    return httpClient.post('/coupon-discount-service/api/admin/coupons', coupon);
  }

  /**
   * 管理者用：クーポン更新
   */
  async updateCoupon(couponId: string, updates: Partial<Coupon>): Promise<Coupon> {
    return httpClient.put(`/coupon-discount-service/api/admin/coupons/${couponId}`, updates);
  }

  /**
   * 管理者用：クーポン削除
   */
  async deleteCoupon(couponId: string): Promise<void> {
    return httpClient.delete(`/coupon-discount-service/api/admin/coupons/${couponId}`);
  }

  /**
   * 管理者用：クーポン統計
   */
  async getCouponStatistics(
    dateFrom?: string,
    dateTo?: string
  ): Promise<{
    totalCoupons: number;
    activeCoupons: number;
    totalUsages: number;
    totalDiscountAmount: number;
    topCoupons: Array<{
      coupon: Coupon;
      usageCount: number;
      totalDiscountAmount: number;
    }>;
  }> {
    const params = new URLSearchParams();
    if (dateFrom) params.append('dateFrom', dateFrom);
    if (dateTo) params.append('dateTo', dateTo);

    return httpClient.get(`/coupon-discount-service/api/admin/statistics?${params.toString()}`);
  }
}

export const couponDiscountService = new CouponDiscountService();
export default couponDiscountService;
