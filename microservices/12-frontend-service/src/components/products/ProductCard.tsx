/**
 * 商品カード（カート機能付き）
 * 商品一覧ページで使用する商品カードコンポーネント
 */

'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { StarIcon } from '@heroicons/react/24/solid';
import { StarIcon as StarOutlineIcon } from '@heroicons/react/24/outline';
import { AddToCartButton } from '../cart/AddToCartButton';
import { Product } from '../../services/api/product-catalog';

interface ProductCardProps {
  product: Product;
  className?: string;
}

export const ProductCard: React.FC<ProductCardProps> = ({
  product,
  className = '',
}) => {
  // 評価星を表示する関数
  const renderRating = (rating: number | null, reviewCount: number) => {
    if (!rating) return null;
    
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;

    for (let i = 0; i < 5; i++) {
      if (i < fullStars) {
        stars.push(
          <StarIcon key={i} className="w-4 h-4 text-yellow-400" />
        );
      } else if (i === fullStars && hasHalfStar) {
        stars.push(
          <div key={i} className="relative">
            <StarOutlineIcon className="w-4 h-4 text-gray-300" />
            <div className="absolute inset-0 overflow-hidden w-1/2">
              <StarIcon className="w-4 h-4 text-yellow-400" />
            </div>
          </div>
        );
      } else {
        stars.push(
          <StarOutlineIcon key={i} className="w-4 h-4 text-gray-300" />
        );
      }
    }

    return (
      <div className="flex items-center space-x-1">
        <div className="flex">{stars}</div>
        {reviewCount > 0 && (
          <span className="text-sm text-gray-500">({reviewCount})</span>
        )}
      </div>
    );
  };

  // 割引率計算
  const discountPercentage = product.discountRate || 
    (product.basePrice && product.currentPrice < product.basePrice 
      ? Math.round(((product.basePrice - product.currentPrice) / product.basePrice) * 100)
      : 0);

  return (
    <div className={`bg-white border border-gray-200 rounded-lg shadow-sm hover:shadow-md transition-shadow ${className}`}>
      {/* 商品画像 */}
      <div className="relative aspect-square overflow-hidden rounded-t-lg">
        <Link href={`/products/${product.id}`}>
          {product.imageUrls && product.imageUrls.length > 0 ? (
            <Image
              src={product.imageUrls[0]}
              alt={product.name}
              fill
              className="object-cover hover:scale-105 transition-transform duration-200"
            />
          ) : (
            <div className="w-full h-full bg-gray-200 flex items-center justify-center">
              <span className="text-gray-400">No Image</span>
            </div>
          )}
        </Link>
        
        {/* バッジ */}
        <div className="absolute top-2 left-2 flex flex-col space-y-1">
          {product.featured && (
            <span className="bg-yellow-500 text-white text-xs px-2 py-1 rounded">
              注目
            </span>
          )}
          {product.onSale && discountPercentage > 0 && (
            <span className="bg-red-500 text-white text-xs px-2 py-1 rounded">
              {discountPercentage}% OFF
            </span>
          )}
        </div>
      </div>

      {/* 商品情報 */}
      <div className="p-4">
        {/* ブランド */}
        {product.brand?.name && (
          <p className="text-sm text-gray-500 mb-1">{product.brand.name}</p>
        )}

        {/* 商品名 */}
        <Link href={`/products/${product.id}`}>
          <h3 className="text-lg font-semibold text-gray-900 mb-2 hover:text-blue-600 transition-colors line-clamp-2">
            {product.name}
          </h3>
        </Link>

        {/* 商品説明 */}
        {product.shortDescription && (
          <p className="text-sm text-gray-600 mb-3 line-clamp-2">
            {product.shortDescription}
          </p>
        )}

        {/* 評価 */}
        <div className="mb-3">
          {renderRating(null, 0)}
        </div>

        {/* 価格 */}
        <div className="mb-4">
          <div className="flex items-center space-x-2">
            <span className="text-xl font-bold text-gray-900">
              ¥{product.currentPrice.toLocaleString()}
            </span>
            {!!(product.basePrice && product.currentPrice < product.basePrice) && (
              <span className="text-sm text-gray-500 line-through">
                ¥{product.basePrice.toLocaleString()}
              </span>
            )}
          </div>
        </div>

        {/* タグ */}
        {product.tags && product.tags.length > 0 && (
          <div className="mb-4">
            <div className="flex flex-wrap gap-1">
              {product.tags.slice(0, 3).map((tag) => (
                <span
                  key={`tag-${tag}`}
                  className="text-xs bg-gray-100 text-gray-600 px-2 py-1 rounded"
                >
                  {tag}
                </span>
              ))}
              {product.tags.length > 3 && (
                <span className="text-xs text-gray-500">
                  +{product.tags.length - 3}
                </span>
              )}
            </div>
          </div>
        )}

        {/* カートに追加ボタン */}
        <AddToCartButton
          productId={product.id}
          sku={product.sku}
          className="w-full"
          onAddSuccess={() => {
            // 成功時の処理（必要に応じて）
            console.log(`商品 ${product.sku} をカートに追加しました`);
          }}
          onAddError={(error) => {
            // エラー時の処理
            console.error('カートへの追加に失敗:', error);
          }}
        />
      </div>
    </div>
  );
};
