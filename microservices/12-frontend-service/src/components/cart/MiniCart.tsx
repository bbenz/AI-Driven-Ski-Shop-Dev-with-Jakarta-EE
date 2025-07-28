/**
 * ショッピングカート - ミニカート（ヘッダー用）
 * カートアイコンをクリックするとドロップダウンでカート内容を表示
 */

'use client';

import React, { useEffect, useState } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { ShoppingCartIcon, TrashIcon, PlusIcon, MinusIcon } from '@heroicons/react/24/outline';
import { useCartStore } from '../../stores/cartStore';
import { CartItemResponse } from '../../types/cart';

interface MiniCartProps {
  className?: string;
}

// エラー表示コンポーネント
const ErrorAlert = ({ message, onClose }: { message: string; onClose: () => void }) => (
  <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded mb-4">
    <div className="flex justify-between items-center">
      <span className="text-sm">{message}</span>
      <button
        onClick={onClose}
        className="text-red-500 hover:text-red-700 ml-2"
        aria-label="エラーメッセージを閉じる"
      >
        ×
      </button>
    </div>
  </div>
);

// カートアイテムコンポーネント
const CartItem = ({ 
  item, 
  onUpdateQuantity, 
  onRemove, 
  loading 
}: { 
  item: CartItemResponse;
  onUpdateQuantity: (sku: string, quantity: number) => void;
  onRemove: (sku: string) => void;
  loading: boolean;
}) => (
  <div className="flex items-center py-3 border-b border-gray-200">
    {/* 商品画像 */}
    <div className="flex-shrink-0 w-12 h-12 bg-gray-100 rounded-lg overflow-hidden">
      {item.productImageUrl ? (
        <Image
          src={item.productImageUrl}
          alt={item.productName}
          width={48}
          height={48}
          className="w-full h-full object-cover"
        />
      ) : (
        <div className="w-full h-full bg-gray-200 flex items-center justify-center">
          <span className="text-gray-400 text-xs">No Image</span>
        </div>
      )}
    </div>

    {/* 商品情報 */}
    <div className="flex-1 ml-3">
      <h3 className="text-sm font-medium text-gray-900 line-clamp-2">
        {item.productName}
      </h3>
      <p className="text-sm text-gray-500">SKU: {item.sku}</p>
      <p className="text-sm font-medium text-gray-900">
        ¥{item.unitPrice.toLocaleString()} × {item.quantity}
      </p>
    </div>

    {/* 数量変更・削除ボタン */}
    <div className="flex flex-col items-end space-y-1">
      <div className="flex items-center space-x-1">
        <button
          onClick={() => onUpdateQuantity(item.sku, item.quantity - 1)}
          disabled={loading}
          className="p-1 hover:bg-gray-100 rounded disabled:opacity-50"
          aria-label="数量を減らす"
        >
          <MinusIcon className="w-3 h-3" />
        </button>
        <span className="text-sm font-medium min-w-[20px] text-center">
          {item.quantity}
        </span>
        <button
          onClick={() => onUpdateQuantity(item.sku, item.quantity + 1)}
          disabled={loading}
          className="p-1 hover:bg-gray-100 rounded disabled:opacity-50"
          aria-label="数量を増やす"
        >
          <PlusIcon className="w-3 h-3" />
        </button>
      </div>
      <button
        onClick={() => onRemove(item.sku)}
        disabled={loading}
        className="p-1 text-red-500 hover:text-red-700 disabled:opacity-50"
        aria-label="商品を削除"
      >
        <TrashIcon className="w-4 h-4" />
      </button>
    </div>
  </div>
);

export const MiniCart: React.FC<MiniCartProps> = ({ className = '' }) => {
  // Zustandセレクターパターンで必要な状態のみを監視
  const cart = useCartStore(state => state.cart);
  const loading = useCartStore(state => state.loading);
  const error = useCartStore(state => state.error);
  const wsConnected = useCartStore(state => state.wsConnected);
  const initializeCart = useCartStore(state => state.initializeCart);
  const removeItem = useCartStore(state => state.removeItem);
  const updateQuantity = useCartStore(state => state.updateQuantity);
  const clearError = useCartStore(state => state.clearError);
  
  // アイテム数を直接計算（セレクターパターン）
  const itemCount = useCartStore(state => {
    if (!state.cart?.items) return 0;
    return state.cart.items.reduce((total, item) => total + item.quantity, 0);
  });

  const [isOpen, setIsOpen] = useState(false);

  // デバッグ情報をログ出力
  useEffect(() => {
    console.log('MiniCart render - cart:', cart);
    console.log('MiniCart render - itemCount:', itemCount);
    console.log('MiniCart render - wsConnected:', wsConnected);
    console.log('MiniCart render - cart?.itemCount:', cart?.itemCount);
    console.log('MiniCart render - cart?.items.length:', cart?.items?.length);
  });

  // コンポーネントマウント時にカートを初期化
  useEffect(() => {
    // カートが存在しない場合は初期化
    initializeCart();
  }, [initializeCart]);

  // キーボードイベントハンドラー
  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === 'Escape') {
      setIsOpen(false);
    }
  };

  return (
    <div className={`relative ${className}`}>
      {/* カートアイコン */}
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="relative flex items-center justify-center w-8 h-8 text-gray-600 hover:text-gray-900 transition-colors"
        aria-label="ショッピングカートを開く"
      >
        <ShoppingCartIcon className="w-6 h-6" />
        {itemCount > 0 && (
          <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
            {itemCount}
          </span>
        )}
        {/* WebSocket接続状態インジケーター */}
        <span 
          className={`absolute -bottom-1 -right-1 w-2 h-2 rounded-full ${
            wsConnected ? 'bg-green-500' : 'bg-gray-400'
          }`}
          title={wsConnected ? 'リアルタイム同期中' : 'オフライン'}
        />
      </button>

      {/* ドロップダウンメニュー */}
      {isOpen && (
        <>
          {/* オーバーレイ */}
          <button
            className="fixed inset-0 z-40 bg-transparent cursor-default"
            onClick={() => setIsOpen(false)}
            onKeyDown={handleKeyDown}
            aria-label="カートを閉じる"
            type="button"
          />
          
          {/* カートドロップダウン */}
          <div className="absolute right-0 top-full mt-2 w-96 bg-white border border-gray-200 rounded-lg shadow-lg z-50">
            <div className="p-4">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">
                ショッピングカート
              </h2>

              {/* エラー表示 */}
              {error && <ErrorAlert message={error} onClose={clearError} />}

              {/* ローディング表示 */}
              {loading && (
                <div className="flex justify-center items-center py-8">
                  <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
                </div>
              )}

              {/* カート内容 */}
              {!loading && cart && (
                <>
                  {cart.items.length === 0 ? (
                    <div className="text-center py-8 text-gray-500">
                      <ShoppingCartIcon className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                      <p>カートは空です</p>
                    </div>
                  ) : (
                    <>
                      {/* カートアイテム一覧 */}
                      <div className="max-h-64 overflow-y-auto">
                        {cart.items.map((item) => (
                          <CartItem 
                            key={item.itemId} 
                            item={item}
                            onUpdateQuantity={updateQuantity}
                            onRemove={removeItem}
                            loading={loading}
                          />
                        ))}
                      </div>

                      {/* 合計金額 */}
                      <div className="mt-4 pt-4 border-t border-gray-200">
                        <div className="flex justify-between items-center mb-2">
                          <span className="text-sm text-gray-600">小計:</span>
                          <span className="text-sm font-medium">
                            ¥{cart.totals.subtotalAmount.toLocaleString()}
                          </span>
                        </div>
                        <div className="flex justify-between items-center mb-2">
                          <span className="text-sm text-gray-600">税込:</span>
                          <span className="text-sm font-medium">
                            ¥{cart.totals.taxAmount.toLocaleString()}
                          </span>
                        </div>
                        <div className="flex justify-between items-center mb-4">
                          <span className="font-semibold">合計:</span>
                          <span className="font-semibold text-lg">
                            ¥{cart.totals.totalAmount.toLocaleString()}
                          </span>
                        </div>

                        {/* アクションボタン */}
                        <div className="space-y-2">
                          <Link 
                            href="/cart" 
                            className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors block text-center"
                          >
                            カートを見る
                          </Link>
                          <button className="w-full bg-green-600 text-white py-2 px-4 rounded-lg hover:bg-green-700 transition-colors">
                            チェックアウト
                          </button>
                        </div>
                      </div>
                    </>
                  )}
                </>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};
