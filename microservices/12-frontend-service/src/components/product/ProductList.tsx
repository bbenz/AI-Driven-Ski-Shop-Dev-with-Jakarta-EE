import React from 'react';
import { ProductCard } from './ProductCard';
import { SectionLoading } from '@/components/ui';
import { Product } from '@/types/product';
import { cn } from '@/utils/helpers';

export interface ProductListProps {
  products: Product[];
  onAddToCart?: (productId: string) => void;
  onToggleFavorite?: (productId: string) => void;
  favoriteProductIds?: string[];
  isLoading?: boolean;
  loadingText?: string;
  emptyText?: string;
  className?: string;
  cardSize?: 'sm' | 'md' | 'lg';
  columns?: 2 | 3 | 4 | 5 | 6;
}

export const ProductList: React.FC<ProductListProps> = ({
  products,
  onAddToCart,
  onToggleFavorite,
  favoriteProductIds = [],
  isLoading = false,
  loadingText = '商品を読み込み中...',
  emptyText = '商品が見つかりませんでした。',
  className,
  cardSize = 'md',
  columns = 4,
}) => {
  const gridClasses = {
    2: 'grid-cols-1 sm:grid-cols-2',
    3: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
    4: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
    5: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5',
    6: 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-6',
  };

  if (isLoading) {
    return (
      <SectionLoading
        text={loadingText}
        variant="pulse"
        className={className}
      />
    );
  }

  if (products.length === 0) {
    return (
      <div className={cn('text-center py-12', className)}>
        <div className="max-w-md mx-auto">
          <div className="mb-4">
            <svg
              className="mx-auto h-12 w-12 text-gray-400"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2M4 13h2m0 0V9a2 2 0 012-2h2a2 2 0 012 2v4.01"
              />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            商品が見つかりません
          </h3>
          <p className="text-gray-500">{emptyText}</p>
        </div>
      </div>
    );
  }

  return (
    <div
      className={cn(
        'grid gap-6',
        gridClasses[columns],
        className
      )}
    >
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          onAddToCart={onAddToCart}
          onToggleFavorite={onToggleFavorite}
          isFavorite={favoriteProductIds.includes(product.id)}
          size={cardSize}
        />
      ))}
    </div>
  );
};
