import httpClient from '../http/client';

export interface PointsBalance {
  userId: string;
  totalPoints: number;
  availablePoints: number;
  pendingPoints: number;
  expiringPoints: Array<{
    points: number;
    expiryDate: string;
  }>;
  lastUpdated: string;
}

export interface PointTransaction {
  id: string;
  userId: string;
  type: 'earned' | 'redeemed' | 'expired' | 'refunded';
  points: number;
  description: string;
  orderId?: string;
  productId?: string;
  categoryId?: string;
  timestamp: string;
  expiryDate?: string;
  metadata?: {
    source: string;
    campaign?: string;
    multiplier?: number;
  };
}

export interface LoyaltyTier {
  id: string;
  name: string;
  minPoints: number;
  maxPoints?: number;
  benefits: Array<{
    type: 'discount' | 'points_multiplier' | 'free_shipping' | 'early_access' | 'birthday_bonus';
    value: number;
    description: string;
  }>;
  color: string;
  icon: string;
}

export interface PointsRedemption {
  id: string;
  name: string;
  description: string;
  pointsRequired: number;
  category: 'discount' | 'product' | 'experience' | 'gift_card';
  value: number;
  imageUrl?: string;
  terms?: string[];
  availability: {
    stock?: number;
    validUntil?: string;
    maxPerUser?: number;
  };
  restrictions?: {
    minPurchase?: number;
    categories?: string[];
    excludedProducts?: string[];
  };
}

export interface Campaign {
  id: string;
  name: string;
  description: string;
  type: 'points_multiplier' | 'bonus_points' | 'category_bonus' | 'purchase_threshold';
  startDate: string;
  endDate: string;
  isActive: boolean;
  rules: {
    multiplier?: number;
    bonusPoints?: number;
    minPurchase?: number;
    categories?: string[];
    products?: string[];
  };
  imageUrl?: string;
}

export interface UserLoyaltyProfile {
  userId: string;
  currentTier: LoyaltyTier;
  pointsBalance: PointsBalance;
  nextTier?: {
    tier: LoyaltyTier;
    pointsNeeded: number;
  };
  recentTransactions: PointTransaction[];
  availableRedemptions: PointsRedemption[];
  activeCampaigns: Campaign[];
  statistics: {
    totalEarned: number;
    totalRedeemed: number;
    joinDate: string;
    tierUpgradeDate?: string;
  };
}

export interface EarnPointsRequest {
  userId: string;
  orderId: string;
  orderAmount: number;
  items: Array<{
    productId: string;
    categoryId: string;
    quantity: number;
    unitPrice: number;
  }>;
  metadata?: {
    campaign?: string;
    source?: string;
  };
}

export interface RedeemPointsRequest {
  userId: string;
  redemptionId: string;
  pointsToRedeem: number;
  orderId?: string;
  metadata?: {
    appliedAt: string;
    discountAmount?: number;
  };
}

class PointsLoyaltyService {
  constructor() {
    // HttpClientはシングルトンとして使用
  }

  /**
   * ユーザーのポイント残高を取得
   */
  async getPointsBalance(userId: string): Promise<PointsBalance> {
    return httpClient.get(`/points-loyalty-service/api/users/${userId}/points`);
  }

  /**
   * ユーザーのロイヤルティプロフィールを取得
   */
  async getUserLoyaltyProfile(userId: string): Promise<UserLoyaltyProfile> {
    return httpClient.get(`/points-loyalty-service/api/users/${userId}/profile`);
  }

  /**
   * ポイント取引履歴を取得
   */
  async getPointsHistory(
    userId: string,
    options?: {
      page?: number;
      limit?: number;
      type?: PointTransaction['type'];
      dateFrom?: string;
      dateTo?: string;
    }
  ): Promise<{
    transactions: PointTransaction[];
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
    if (options?.type) params.append('type', options.type);
    if (options?.dateFrom) params.append('dateFrom', options.dateFrom);
    if (options?.dateTo) params.append('dateTo', options.dateTo);

    return httpClient.get(`/points-loyalty-service/api/users/${userId}/transactions?${params.toString()}`);
  }

  /**
   * ポイントを獲得
   */
  async earnPoints(request: EarnPointsRequest): Promise<{
    transaction: PointTransaction;
    newBalance: PointsBalance;
    tierUpdate?: {
      oldTier: LoyaltyTier;
      newTier: LoyaltyTier;
    };
  }> {
    return httpClient.post('/points-loyalty-service/api/points/earn', request);
  }

  /**
   * ポイントを使用
   */
  async redeemPoints(request: RedeemPointsRequest): Promise<{
    transaction: PointTransaction;
    newBalance: PointsBalance;
    redemption: PointsRedemption;
  }> {
    return httpClient.post('/points-loyalty-service/api/points/redeem', request);
  }

  /**
   * 利用可能なロイヤルティティアを取得
   */
  async getLoyaltyTiers(): Promise<LoyaltyTier[]> {
    return httpClient.get('/points-loyalty-service/api/tiers');
  }

  /**
   * ポイント交換商品一覧を取得
   */
  async getRedemptions(options?: {
    category?: PointsRedemption['category'];
    minPoints?: number;
    maxPoints?: number;
    available?: boolean;
  }): Promise<PointsRedemption[]> {
    const params = new URLSearchParams();
    if (options?.category) params.append('category', options.category);
    if (options?.minPoints) params.append('minPoints', options.minPoints.toString());
    if (options?.maxPoints) params.append('maxPoints', options.maxPoints.toString());
    if (options?.available !== undefined) params.append('available', options.available.toString());

    return httpClient.get(`/points-loyalty-service/api/redemptions?${params.toString()}`);
  }

  /**
   * 特定のポイント交換商品詳細を取得
   */
  async getRedemptionDetails(redemptionId: string): Promise<PointsRedemption> {
    return httpClient.get(`/points-loyalty-service/api/redemptions/${redemptionId}`);
  }

  /**
   * アクティブなキャンペーン一覧を取得
   */
  async getActiveCampaigns(): Promise<Campaign[]> {
    return httpClient.get('/points-loyalty-service/api/campaigns/active');
  }

  /**
   * 特定のキャンペーン詳細を取得
   */
  async getCampaignDetails(campaignId: string): Promise<Campaign> {
    return httpClient.get(`/points-loyalty-service/api/campaigns/${campaignId}`);
  }

  /**
   * ユーザーのポイント予測を取得
   */
  async getPointsForecast(userId: string, months: number = 12): Promise<{
    projectedEarnings: Array<{
      month: string;
      estimatedPoints: number;
      basedOn: string;
    }>;
    expiringPoints: Array<{
      month: string;
      points: number;
    }>;
    tierProjections: Array<{
      tier: LoyaltyTier;
      achievableDate?: string;
      pointsNeeded: number;
    }>;
  }> {
    return httpClient.get(`/points-loyalty-service/api/users/${userId}/forecast?months=${months}`);
  }

  /**
   * ポイント獲得のシミュレーション
   */
  async simulatePointsEarning(request: {
    userId: string;
    orderAmount: number;
    items: Array<{
      productId: string;
      categoryId: string;
      quantity: number;
      unitPrice: number;
    }>;
    campaignId?: string;
  }): Promise<{
    basePoints: number;
    bonusPoints: number;
    multiplier: number;
    totalPoints: number;
    tierAfterPurchase: LoyaltyTier;
    nextTierProgress?: {
      pointsNeeded: number;
      percentage: number;
    };
  }> {
    return httpClient.post('/points-loyalty-service/api/points/simulate', request);
  }

  /**
   * ポイント交換の確認
   */
  async validateRedemption(userId: string, redemptionId: string, pointsToRedeem: number): Promise<{
    isValid: boolean;
    errors?: string[];
    warnings?: string[];
    finalDiscount?: number;
    termsAndConditions: string[];
  }> {
    return httpClient.post('/points-loyalty-service/api/redemptions/validate', {
      userId,
      redemptionId,
      pointsToRedeem,
    });
  }

  /**
   * ロイヤルティ統計を取得
   */
  async getLoyaltyStats(userId: string, period: 'month' | 'quarter' | 'year' = 'year'): Promise<{
    period: string;
    pointsEarned: number;
    pointsRedeemed: number;
    ordersCount: number;
    averageOrderValue: number;
    tierUpgrades: number;
    favoriteCategories: Array<{
      category: string;
      pointsEarned: number;
      ordersCount: number;
    }>;
    monthlyBreakdown: Array<{
      month: string;
      earned: number;
      redeemed: number;
      orders: number;
    }>;
  }> {
    return httpClient.get(`/points-loyalty-service/api/users/${userId}/stats?period=${period}`);
  }
}

export const pointsLoyaltyService = new PointsLoyaltyService();
export default pointsLoyaltyService;
