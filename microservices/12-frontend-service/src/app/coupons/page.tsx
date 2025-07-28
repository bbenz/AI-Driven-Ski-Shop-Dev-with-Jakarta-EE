'use client';

import { useState } from 'react';
import { Tag, Search, Filter } from 'lucide-react';
import { MainLayout } from '@/components/layout/MainLayout';
import CouponList from '@/components/coupon/CouponList';
import CouponInput from '@/components/coupon/CouponInput';
import { type Coupon } from '@/services/api/coupon-discount';

export default function CouponPage() {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState<string>('all');
  const [appliedCoupon, setAppliedCoupon] = useState<Coupon | null>(null);
  const [discountAmount, setDiscountAmount] = useState(0);

  // デモ用の注文データ
  const mockOrderAmount = 50000;
  const mockItems = [
    {
      productId: '1',
      categoryId: 'cat-ski',
      quantity: 1,
      price: 35000
    },
    {
      productId: '2', 
      categoryId: 'cat-boots',
      quantity: 1,
      price: 15000
    }
  ];

  const handleCouponApplied = (coupon: Coupon, discount: number) => {
    setAppliedCoupon(coupon);
    setDiscountAmount(discount);
  };

  const handleCouponRemoved = () => {
    setAppliedCoupon(null);
    setDiscountAmount(0);
  };

  const handleCouponSelect = (coupon: Coupon) => {
    // 実際のアプリケーションでは、選択されたクーポンをカートに適用する処理を行う
    console.log('Selected coupon:', coupon);
  };

  return (
    <MainLayout>
      <div className="max-w-6xl mx-auto p-6">
      {/* ページヘッダー */}
      <div className="mb-8">
        <div className="flex items-center gap-3 mb-4">
          <Tag className="w-8 h-8 text-blue-600" />
          <h1 className="text-3xl font-bold text-gray-900">クーポン・割引</h1>
        </div>
        <p className="text-gray-600">
          お得なクーポンを使って、スキー用品をお得に購入しましょう。
        </p>
      </div>

      {/* クーポン入力セクション */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-8">
        <h2 className="text-xl font-semibold text-gray-900 mb-4">
          クーポンコードの入力
        </h2>
        <CouponInput
          orderAmount={mockOrderAmount}
          items={mockItems}
          appliedCoupon={appliedCoupon || undefined}
          discountAmount={discountAmount}
          onCouponApplied={handleCouponApplied}
          onCouponRemoved={handleCouponRemoved}
        />
        
        {/* 注文サマリー（デモ用） */}
        <div className="mt-6 pt-6 border-t border-gray-200">
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="font-semibold text-gray-900 mb-3">注文サマリー（デモ）</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>商品合計</span>
                <span>¥{mockOrderAmount.toLocaleString()}</span>
              </div>
              {appliedCoupon && (
                <div className="flex justify-between text-green-600">
                  <span>クーポン割引 ({appliedCoupon.code})</span>
                  <span>-¥{discountAmount.toLocaleString()}</span>
                </div>
              )}
              <div className="flex justify-between font-semibold text-lg pt-2 border-t border-gray-300">
                <span>合計</span>
                <span>¥{(mockOrderAmount - discountAmount).toLocaleString()}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 検索・フィルター */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-8">
        <div className="flex flex-col sm:flex-row gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              type="text"
              placeholder="クーポン名またはコードで検索..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <div className="relative">
            <Filter className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="pl-10 pr-8 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent appearance-none bg-white"
              title="クーポンタイプで絞り込み"
              aria-label="クーポンタイプで絞り込み"
            >
              <option value="all">すべてのクーポン</option>
              <option value="PERCENTAGE">パーセント割引</option>
              <option value="FIXED_AMOUNT">固定額割引</option>
              <option value="FREE_SHIPPING">送料無料</option>
              <option value="BUY_X_GET_Y">セット割引</option>
            </select>
          </div>
        </div>
      </div>

      {/* クーポン一覧 */}
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <CouponList
          orderAmount={mockOrderAmount}
          onCouponSelect={handleCouponSelect}
        />
      </div>

      {/* カテゴリ別クーポン */}
      <div className="mt-8 grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            スキー板用クーポン
          </h3>
          <CouponList
            categoryIds={['cat-ski']}
            orderAmount={mockOrderAmount}
            onCouponSelect={handleCouponSelect}
          />
        </div>
        
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">
            ウェア用クーポン
          </h3>
          <CouponList
            categoryIds={['cat-wear']}
            orderAmount={mockOrderAmount}
            onCouponSelect={handleCouponSelect}
          />
        </div>
      </div>
    </div>
    </MainLayout>
  );
}
