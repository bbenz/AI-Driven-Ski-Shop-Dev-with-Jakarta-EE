'use client';

import { useState, useCallback } from 'react';
import { Tag, X, Percent, Gift, Loader2 } from 'lucide-react';
import { couponDiscountService, type Coupon, type ValidateCouponResponse } from '@/services/api/coupon-discount';
import { useAuth } from '@/hooks/useAuth';

interface CouponInputProps {
  orderAmount: number;
  items: Array<{
    productId: string;
    categoryId: string;
    quantity: number;
    price: number;
  }>;
  appliedCoupon?: Coupon;
  discountAmount?: number;
  onCouponApplied: (coupon: Coupon, discountAmount: number) => void;
  onCouponRemoved: () => void;
  className?: string;
}

export default function CouponInput({
  orderAmount,
  items,
  appliedCoupon,
  discountAmount = 0,
  onCouponApplied,
  onCouponRemoved,
  className = ''
}: CouponInputProps) {
  const { user } = useAuth();
  const [couponCode, setCouponCode] = useState('');
  const [isValidating, setIsValidating] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [availableCoupons, setAvailableCoupons] = useState<Coupon[]>([]);
  const [showSuggestions, setShowSuggestions] = useState(false);

  const validateAndApplyCoupon = useCallback(async (code: string) => {
    if (!code.trim()) {
      setError('クーポンコードを入力してください');
      return;
    }

    setIsValidating(true);
    setError(null);

    try {
      const response: ValidateCouponResponse = await couponDiscountService.validateCoupon({
        code: code.trim(),
        userId: user?.id,
        orderAmount,
        items
      });

      if (response.isValid && response.coupon && response.discountAmount !== undefined) {
        onCouponApplied(response.coupon, response.discountAmount);
        setCouponCode('');
        setShowSuggestions(false);
      } else {
        setError(response.errorMessage || 'クーポンが適用できません');
      }
    } catch (error) {
      console.error('Failed to validate coupon:', error);
      setError('クーポンの検証に失敗しました。再度お試しください。');
    } finally {
      setIsValidating(false);
    }
  }, [orderAmount, items, user?.id, onCouponApplied]);

  const loadAvailableCoupons = useCallback(async () => {
    try {
      const response = await couponDiscountService.getAvailableCoupons({
        status: 'ACTIVE',
        minimumOrderAmount: orderAmount,
        limit: 5
      });
      setAvailableCoupons(response.coupons);
    } catch (error) {
      console.error('Failed to load available coupons:', error);
    }
  }, [orderAmount]);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    validateAndApplyCoupon(couponCode);
  };

  const handleRemoveCoupon = () => {
    onCouponRemoved();
    setError(null);
  };

  const handleInputFocus = () => {
    if (!showSuggestions && availableCoupons.length === 0) {
      loadAvailableCoupons();
    }
    setShowSuggestions(true);
  };

  const handleInputBlur = () => {
    // 少し遅らせてsuggestionsを隠す（クリックイベントを優先）
    setTimeout(() => setShowSuggestions(false), 150);
  };

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
        return <Percent className="w-4 h-4" />;
      case 'FREE_SHIPPING':
        return <Gift className="w-4 h-4" />;
      default:
        return <Tag className="w-4 h-4" />;
    }
  };

  return (
    <div className={`space-y-4 ${className}`}>
      {/* 適用済みクーポン表示 */}
      {appliedCoupon && (
        <div className="bg-green-50 border border-green-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex items-center gap-2 text-green-700">
                {getDiscountIcon(appliedCoupon.discountType)}
                <span className="font-medium">{appliedCoupon.name}</span>
              </div>
              <span className="bg-green-100 text-green-800 px-2 py-1 rounded text-sm font-medium">
                {formatDiscountDisplay(appliedCoupon)}
              </span>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-green-700 font-semibold">
                -¥{discountAmount.toLocaleString()}
              </span>
              <button
                onClick={handleRemoveCoupon}
                className="text-green-600 hover:text-green-800 p-1 rounded-full hover:bg-green-100 transition-colors"
                title="クーポンを削除"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>
          {appliedCoupon.description && (
            <p className="text-sm text-green-600 mt-2">{appliedCoupon.description}</p>
          )}
        </div>
      )}

      {/* クーポン入力フォーム（適用済みでない場合のみ表示） */}
      {!appliedCoupon && (
        <div className="relative">
          <form onSubmit={handleSubmit} className="flex gap-2">
            <div className="flex-1 relative">
              <input
                type="text"
                value={couponCode}
                onChange={(e) => setCouponCode(e.target.value.toUpperCase())}
                onFocus={handleInputFocus}
                onBlur={handleInputBlur}
                placeholder="クーポンコードを入力"
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                disabled={isValidating}
              />
              
              {/* 利用可能なクーポンの提案 */}
              {showSuggestions && availableCoupons.length > 0 && (
                <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg z-10">
                  <div className="p-2 text-xs text-gray-500 border-b">
                    利用可能なクーポン
                  </div>
                  {availableCoupons.map((coupon) => (
                    <button
                      key={coupon.couponId}
                      type="button"
                      onClick={() => {
                        setCouponCode(coupon.code);
                        setShowSuggestions(false);
                        validateAndApplyCoupon(coupon.code);
                      }}
                      className="w-full p-3 text-left hover:bg-gray-50 border-b last:border-b-0"
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <div className="flex items-center gap-2">
                            {getDiscountIcon(coupon.discountType)}
                            <span className="font-medium text-sm">{coupon.name}</span>
                          </div>
                          <div className="text-xs text-gray-500 mt-1">
                            コード: {coupon.code}
                          </div>
                        </div>
                        <span className="bg-blue-100 text-blue-800 px-2 py-1 rounded text-xs font-medium">
                          {formatDiscountDisplay(coupon)}
                        </span>
                      </div>
                    </button>
                  ))}
                </div>
              )}
            </div>
            <button
              type="submit"
              disabled={isValidating || !couponCode.trim()}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
            >
              {isValidating ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  確認中
                </>
              ) : (
                '適用'
              )}
            </button>
          </form>

          {/* エラーメッセージ */}
          {error && (
            <div className="mt-2 text-sm text-red-600 bg-red-50 border border-red-200 rounded p-2">
              {error}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
