/**
 * カートに追加ボタン
 * 商品一覧や商品詳細ページで使用する汎用的なカート追加ボタン
 */

'use client';

import React, { useState } from 'react';
import { PlusIcon, CheckIcon } from '@heroicons/react/24/outline';
import { useCartStore } from '../../stores/cartStore';

interface AddToCartButtonProps {
  productId: string;
  sku: string;
  quantity?: number;
  disabled?: boolean;
  className?: string;
  variant?: 'primary' | 'secondary' | 'outline';
  size?: 'sm' | 'md' | 'lg';
  showSuccessFeedback?: boolean;
  onAddSuccess?: () => void;
  onAddError?: (error: string) => void;
}

export const AddToCartButton: React.FC<AddToCartButtonProps> = ({
  productId,
  sku,
  quantity = 1,
  disabled = false,
  className = '',
  variant = 'primary',
  size = 'md',
  showSuccessFeedback = true,
  onAddSuccess,
  onAddError,
}) => {
  const { addItem, loading, error } = useCartStore();
  const [justAdded, setJustAdded] = useState(false);

  const handleAddToCart = async () => {
    try {
      console.log('Adding to cart:', { productId, sku, quantity });
      await addItem(productId, sku, quantity);
      
      // カート追加後の状態を確認
      const currentCart = useCartStore.getState().cart;
      const currentItemCount = useCartStore.getState().getItemCount();
      console.log('After adding to cart - cart:', currentCart);
      console.log('After adding to cart - itemCount:', currentItemCount);
      console.log('Successfully added to cart');
      
      if (showSuccessFeedback) {
        setJustAdded(true);
        setTimeout(() => setJustAdded(false), 2000);
      }
      
      onAddSuccess?.();
    } catch (err) {
      console.error('Failed to add to cart:', err);
      const errorMessage = err instanceof Error ? err.message : 'カートへの追加に失敗しました';
      onAddError?.(errorMessage);
    }
  };

  // バリアントスタイル
  const variantStyles = {
    primary: 'bg-blue-600 hover:bg-blue-700 text-white border-blue-600',
    secondary: 'bg-gray-600 hover:bg-gray-700 text-white border-gray-600',
    outline: 'bg-white hover:bg-gray-50 text-gray-900 border-gray-300 hover:border-gray-400',
  };

  // サイズスタイル
  const sizeStyles = {
    sm: 'px-3 py-1.5 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base',
  };

  // アイコンサイズ
  const iconSizes = {
    sm: 'w-4 h-4',
    md: 'w-5 h-5',
    lg: 'w-6 h-6',
  };

  const isDisabled = disabled || loading;
  const isSuccess = justAdded && !error;

  return (
    <button
      onClick={handleAddToCart}
      disabled={isDisabled}
      className={`
        inline-flex items-center justify-center
        font-medium rounded-lg border transition-colors
        disabled:opacity-50 disabled:cursor-not-allowed
        ${variantStyles[variant]}
        ${sizeStyles[size]}
        ${className}
        ${isSuccess ? 'bg-green-600 border-green-600 text-white' : ''}
      `}
      aria-label={`${sku}をカートに追加`}
    >
      {loading && (
        <>
          <div className={`animate-spin rounded-full border-2 border-current border-t-transparent mr-2 ${iconSizes[size]}`} />
          追加中...
        </>
      )}
      
      {!loading && isSuccess && (
        <>
          <CheckIcon className={`mr-2 ${iconSizes[size]}`} />
          追加済み
        </>
      )}
      
      {!loading && !isSuccess && (
        <>
          <PlusIcon className={`mr-2 ${iconSizes[size]}`} />
          カートに追加
        </>
      )}
    </button>
  );
};
