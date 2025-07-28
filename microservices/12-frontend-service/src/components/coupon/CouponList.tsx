'use client';

import { useState, useEffect, useCallback } from 'react';
import { Tag, Calendar, Percent, Gift, Copy, Check } from 'lucide-react';
import { couponDiscountService, type Coupon } from '@/services/api/coupon-discount';

interface CouponListProps {
  categoryIds?: string[];
  productIds?: string[];
  orderAmount?: number;
  onCouponSelect?: (coupon: Coupon) => void;
  className?: string;
}

export default function CouponList({
  categoryIds,
  productIds,
  orderAmount,
  onCouponSelect,
  className = ''
}: CouponListProps) {
  const [coupons, setCoupons] = useState<Coupon[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [copiedCode, setCopiedCode] = useState<string | null>(null);

  const loadCoupons = useCallback(async () => {
    setIsLoading(true);
    try {
      let response;
      
      if (categoryIds || productIds) {
        // 特定のカテゴリ・商品に適用可能なクーポンを取得
        response = await couponDiscountService.getApplicableCoupons(categoryIds, productIds);
        setCoupons(response);
      } else {
        // 一般的に利用可能なクーポンを取得
        const data = await couponDiscountService.getAvailableCoupons({
          status: 'ACTIVE',
          minimumOrderAmount: orderAmount,
          sortBy: 'priority',
          sortOrder: 'desc',
          limit: 20
        });
        setCoupons(data.coupons);
      }
    } catch (error) {
      console.error('Failed to load coupons:', error);
      setCoupons([]);
    } finally {
      setIsLoading(false);
    }
  }, [categoryIds, productIds, orderAmount]);

  useEffect(() => {
    loadCoupons();
  }, [loadCoupons]);

  const formatDiscountDisplay = (coupon: Coupon) => {
    switch (coupon.discountType) {
      case 'PERCENTAGE':
        return `${coupon.discountValue}%OFF`;
      case 'FIXED_AMOUNT':
        return `¥${coupon.discountValue.toLocaleString()}OFF`;
      case 'FREE_SHIPPING':
        return '送料無料';
      case 'BUY_X_GET_Y':
        return 'お得なセット割';
      default:
        return '割引';
    }
  };

  const getDiscountIcon = (discountType: Coupon['discountType']) => {
    switch (discountType) {
      case 'PERCENTAGE':
        return <Percent className="w-5 h-5" />;
      case 'FREE_SHIPPING':
        return <Gift className="w-5 h-5" />;
      default:
        return <Tag className="w-5 h-5" />;
    }
  };

  const getDiscountColor = (discountType: Coupon['discountType']) => {
    switch (discountType) {
      case 'PERCENTAGE':
        return 'bg-blue-100 text-blue-800';
      case 'FIXED_AMOUNT':
        return 'bg-green-100 text-green-800';
      case 'FREE_SHIPPING':
        return 'bg-purple-100 text-purple-800';
      case 'BUY_X_GET_Y':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ja-JP', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  const copyToClipboard = async (code: string) => {
    try {
      await navigator.clipboard.writeText(code);
      setCopiedCode(code);
      setTimeout(() => setCopiedCode(null), 2000);
    } catch (error) {
      console.error('Failed to copy to clipboard:', error);
    }
  };

  const isExpiringSoon = (validTo: string) => {
    const expiryDate = new Date(validTo);
    const now = new Date();
    const diffTime = expiryDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    return diffDays <= 7 && diffDays > 0;
  };

  if (isLoading) {
    return (
      <div className={`text-center py-8 ${className}`}>
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-2 text-gray-600">クーポンを読み込み中...</p>
      </div>
    );
  }

  if (coupons.length === 0) {
    return (
      <div className={`text-center py-8 ${className}`}>
        <Tag className="w-12 h-12 text-gray-400 mx-auto mb-2" />
        <p className="text-gray-600">利用可能なクーポンがありません</p>
      </div>
    );
  }

  return (
    <div className={`space-y-4 ${className}`}>
      <h3 className="text-lg font-semibold text-gray-900 flex items-center gap-2">
        <Tag className="w-5 h-5" />
        利用可能なクーポン
      </h3>
      
      <div className="grid gap-4">
        {coupons.map((coupon) => (
          <div
            key={coupon.couponId}
            className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <div className={`flex items-center gap-2 ${getDiscountColor(coupon.discountType)} px-3 py-1 rounded-full`}>
                    {getDiscountIcon(coupon.discountType)}
                    <span className="font-medium text-sm">
                      {formatDiscountDisplay(coupon)}
                    </span>
                  </div>
                  {isExpiringSoon(coupon.validTo) && (
                    <span className="bg-red-100 text-red-800 px-2 py-1 rounded text-xs font-medium">
                      期限間近
                    </span>
                  )}
                </div>
                
                <h4 className="font-semibold text-gray-900 mb-1">{coupon.name}</h4>
                {coupon.description && (
                  <p className="text-sm text-gray-600 mb-2">{coupon.description}</p>
                )}
                
                <div className="flex items-center gap-4 text-xs text-gray-500">
                  <div className="flex items-center gap-1">
                    <Calendar className="w-3 h-3" />
                    <span>有効期限: {formatDate(coupon.validTo)}</span>
                  </div>
                  {coupon.minimumOrderAmount && (
                    <span>最低注文金額: ¥{coupon.minimumOrderAmount.toLocaleString()}</span>
                  )}
                  {coupon.usageLimit && (
                    <span>残り{coupon.usageLimit - coupon.usageCount}回</span>
                  )}
                </div>
              </div>
              
              <div className="flex flex-col items-end gap-2">
                <div className="flex items-center gap-2 bg-gray-100 px-3 py-1 rounded border-2 border-dashed border-gray-300">
                  <span className="font-mono text-sm font-medium">{coupon.code}</span>
                  <button
                    onClick={() => copyToClipboard(coupon.code)}
                    className="text-gray-500 hover:text-gray-700 transition-colors"
                    title="クーポンコードをコピー"
                  >
                    {copiedCode === coupon.code ? (
                      <Check className="w-4 h-4 text-green-600" />
                    ) : (
                      <Copy className="w-4 h-4" />
                    )}
                  </button>
                </div>
                
                {onCouponSelect && (
                  <button
                    onClick={() => onCouponSelect(coupon)}
                    className="text-sm bg-blue-600 text-white px-3 py-1 rounded hover:bg-blue-700 transition-colors"
                  >
                    適用
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
