import React, { useState } from 'react';
import { FunnelIcon, XMarkIcon } from '@heroicons/react/24/outline';
import { Button, Badge } from '@/components/ui';
import { Category, Brand } from '@/types/product';
import { cn } from '@/utils/helpers';

export interface ProductFilters {
  categoryId?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  inStock?: boolean;
  onSale?: boolean;
  featured?: boolean;
  rating?: number;
  search?: string;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface ProductFilterProps {
  filters: ProductFilters;
  categories: Category[];
  brands: Brand[];
  priceRange?: { min: number; max: number };
  onFiltersChange: (filters: ProductFilters) => void;
  onClearFilters: () => void;
  className?: string;
  showMobileFilter?: boolean;
  onCloseMobileFilter?: () => void;
}

export const ProductFilter: React.FC<ProductFilterProps> = ({
  filters,
  categories,
  brands,
  priceRange = { min: 0, max: 100000 },
  onFiltersChange,
  onClearFilters,
  className,
  showMobileFilter = false,
  onCloseMobileFilter,
}) => {
  const [localFilters, setLocalFilters] = useState<ProductFilters>(filters);

  const sortOptions = [
    { value: 'default', label: 'おすすめ順' },
    { value: 'createdAt_desc', label: '新着順' },
    { value: 'price_asc', label: '価格の安い順' },
    { value: 'price_desc', label: '価格の高い順' },
    { value: 'rating_desc', label: '評価の高い順' },
    { value: 'name_asc', label: '名前順（A-Z）' },
  ];

  const categoryOptions = [
    { value: '', label: 'すべてのカテゴリ' },
    ...categories.map(cat => ({ value: cat.id, label: cat.name })),
  ];

  const brandOptions = [
    { value: '', label: 'すべてのブランド' },
    ...brands.map(brand => ({ value: brand.id, label: brand.name })),
  ];

  const handleFilterChange = (field: keyof ProductFilters, value: unknown) => {
    const newFilters = { ...localFilters, [field]: value };
    setLocalFilters(newFilters);
    onFiltersChange(newFilters);
  };

  const handleClearFilters = () => {
    setLocalFilters({});
    onClearFilters();
  };

  const getActiveFilterCount = () => {
    return Object.values(filters).filter(value => 
      value !== undefined && value !== '' && value !== false
    ).length;
  };

  const activeFilterCount = getActiveFilterCount();

  const filterContent = (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <FunnelIcon className="h-5 w-5 text-gray-600" />
          <h3 className="text-lg font-medium text-gray-900">フィルター</h3>
          {activeFilterCount > 0 && (
            <Badge variant="primary" size="sm">
              {activeFilterCount}
            </Badge>
          )}
        </div>
        
        {showMobileFilter && onCloseMobileFilter && (
          <button
            type="button"
            className="p-2 text-gray-400 hover:text-gray-600"
            onClick={onCloseMobileFilter}
            aria-label="フィルターを閉じる"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        )}
      </div>

      <div className="space-y-6">
        {/* Sort */}
        <div>
          <label htmlFor="sortBy" className="block text-sm font-medium text-gray-700 mb-2">
            並び順
          </label>
          <select
            id="sortBy"
            value={localFilters.sortBy || 'default'}
            onChange={(e) => handleFilterChange('sortBy', e.target.value)}
            className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Category */}
        <div>
          <label htmlFor="categoryId" className="block text-sm font-medium text-gray-700 mb-2">
            カテゴリ
          </label>
          <select
            id="categoryId"
            value={localFilters.categoryId || ''}
            onChange={(e) => handleFilterChange('categoryId', e.target.value || undefined)}
            className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          >
            {categoryOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Brand */}
        <div>
          <label htmlFor="brandId" className="block text-sm font-medium text-gray-700 mb-2">
            ブランド
          </label>
          <select
            id="brandId"
            value={localFilters.brandId || ''}
            onChange={(e) => handleFilterChange('brandId', e.target.value || undefined)}
            className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
          >
            {brandOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Price Range */}
        <div>
          <label htmlFor="min-price-filter" className="block text-sm font-medium text-gray-700 mb-2">
            価格帯
          </label>
          <div className="grid grid-cols-2 gap-2">
            <div>
              <input
                id="min-price-filter"
                type="number"
                placeholder="最低価格"
                min={priceRange.min}
                max={priceRange.max}
                value={localFilters.minPrice || ''}
                onChange={(e) => handleFilterChange('minPrice', e.target.value ? Number(e.target.value) : undefined)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                suppressHydrationWarning
              />
            </div>
            <div>
              <input
                id="max-price-filter"
                type="number"
                placeholder="最高価格"
                min={priceRange.min}
                max={priceRange.max}
                value={localFilters.maxPrice || ''}
                onChange={(e) => handleFilterChange('maxPrice', e.target.value ? Number(e.target.value) : undefined)}
                className="block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm placeholder-gray-400 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                suppressHydrationWarning
              />
            </div>
          </div>
        </div>

        {/* Options */}
        <div className="space-y-3">
          <div className="flex items-center">
            <input
              id="inStock"
              type="checkbox"
              checked={localFilters.inStock || false}
              onChange={(e) => handleFilterChange('inStock', e.target.checked || undefined)}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              suppressHydrationWarning
            />
            <label htmlFor="inStock" className="ml-3 text-sm font-medium text-gray-700">
              在庫あり
            </label>
          </div>
          
          <div className="flex items-center">
            <input
              id="onSale"
              type="checkbox"
              checked={localFilters.onSale || false}
              onChange={(e) => handleFilterChange('onSale', e.target.checked || undefined)}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              suppressHydrationWarning
            />
            <label htmlFor="onSale" className="ml-3 text-sm font-medium text-gray-700">
              セール中
            </label>
          </div>
          
          <div className="flex items-center">
            <input
              id="featured"
              type="checkbox"
              checked={localFilters.featured || false}
              onChange={(e) => handleFilterChange('featured', e.target.checked || undefined)}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
              suppressHydrationWarning
            />
            <label htmlFor="featured" className="ml-3 text-sm font-medium text-gray-700">
              おすすめ商品
            </label>
          </div>
        </div>

        {/* Actions */}
        <div className="pt-4 border-t border-gray-200">
          <Button
            type="button"
            variant="ghost"
            onClick={handleClearFilters}
            className="w-full mb-2"
            disabled={activeFilterCount === 0}
          >
            フィルターをクリア
          </Button>
        </div>
      </div>
    </div>
  );

  if (showMobileFilter) {
    return (
      <div className="fixed inset-0 z-50 lg:hidden">
        <button 
          className="fixed inset-0 bg-black bg-opacity-25" 
          onClick={onCloseMobileFilter}
          onKeyDown={(e) => {
            if (e.key === 'Escape' && onCloseMobileFilter) {
              onCloseMobileFilter();
            }
          }}
          aria-label="フィルターを閉じる"
          type="button"
        />
        <div className="fixed right-0 top-0 h-full w-full max-w-xs bg-white p-6 overflow-y-auto">
          {filterContent}
        </div>
      </div>
    );
  }

  return (
    <div className={cn('bg-white p-6 rounded-lg border border-gray-200', className)}>
      {filterContent}
    </div>
  );
};
