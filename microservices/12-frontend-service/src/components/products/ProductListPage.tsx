/**
 * 商品一覧ページ
 * カテゴリ別商品表示とショッピングカート機能統合
 */

'use client';

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { ChevronRightIcon, FunnelIcon } from '@heroicons/react/24/outline';
import { ProductCard } from './ProductCard';
import { productCatalogApi, Product, Category } from '../../services/api/product-catalog';

interface ProductListPageProps {
  categoryId?: string;
  searchQuery?: string;
}

// パンくずリストコンポーネント
const Breadcrumb = ({ currentCategory }: { currentCategory: Category | null }) => (
  <nav className="flex mb-6" aria-label="Breadcrumb">
    <ol className="flex items-center space-x-2">
      <li>
        <Link href="/" className="text-gray-500 hover:text-gray-700">
          ホーム
        </Link>
      </li>
      <ChevronRightIcon className="w-4 h-4 text-gray-400" />
      <li>
        <Link href="/products" className="text-gray-500 hover:text-gray-700">
          商品一覧
        </Link>
      </li>
      {currentCategory && (
        <>
          <ChevronRightIcon className="w-4 h-4 text-gray-400" />
          <li className="text-gray-900 font-medium">
            {currentCategory.name}
          </li>
        </>
      )}
    </ol>
  </nav>
);

// フィルターパネルコンポーネント
const FilterPanel = ({ 
  filters, 
  onFiltersChange 
}: { 
  filters: {
    minPrice: string;
    maxPrice: string;
    brandId: string;
    sortBy: string;
    onSale: boolean;
    featured: boolean;
  };
  onFiltersChange: (filters: {
    minPrice: string;
    maxPrice: string;
    brandId: string;
    sortBy: string;
    onSale: boolean;
    featured: boolean;
  }) => void;
}) => (
  <div className="bg-white border border-gray-200 rounded-lg p-6 mb-6">
    <div className="flex items-center mb-4">
      <FunnelIcon className="w-5 h-5 mr-2 text-gray-500" />
      <h2 className="text-lg font-semibold">フィルター</h2>
    </div>
    
    <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
      {/* 価格範囲 */}
      <div>
        <span className="block text-sm font-medium text-gray-700 mb-2">
          価格範囲
        </span>
        <div className="flex space-x-2">
          <input
            type="number"
            placeholder="最低価格"
            value={filters.minPrice}
            onChange={(e) => onFiltersChange({ ...filters, minPrice: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
            aria-label="最低価格"
          />
          <input
            type="number"
            placeholder="最高価格"
            value={filters.maxPrice}
            onChange={(e) => onFiltersChange({ ...filters, maxPrice: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
            aria-label="最高価格"
          />
        </div>
      </div>

      {/* 並び順 */}
      <div>
        <label htmlFor="sortBy" className="block text-sm font-medium text-gray-700 mb-2">
          並び順
        </label>
        <select
          id="sortBy"
          value={filters.sortBy}
          onChange={(e) => onFiltersChange({ ...filters, sortBy: e.target.value })}
          className="w-full px-3 py-2 border border-gray-300 rounded-md text-sm"
        >
          <option value="name_asc">名前順</option>
          <option value="price_asc">価格の安い順</option>
          <option value="price_desc">価格の高い順</option>
        </select>
      </div>

      {/* セール商品 */}
      <div>
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={filters.onSale}
            onChange={(e) => onFiltersChange({ ...filters, onSale: e.target.checked })}
            className="mr-2 rounded"
          />
          <span className="text-sm font-medium text-gray-700">セール商品のみ</span>
        </label>
      </div>

      {/* 注目商品 */}
      <div>
        <label className="flex items-center">
          <input
            type="checkbox"
            checked={filters.featured}
            onChange={(e) => onFiltersChange({ ...filters, featured: e.target.checked })}
            className="mr-2 rounded"
          />
          <span className="text-sm font-medium text-gray-700">注目商品のみ</span>
        </label>
      </div>

      {/* フィルタークリア */}
      <div className="flex items-end">
        <button
          onClick={() => onFiltersChange({
            minPrice: '',
            maxPrice: '',
            brandId: '',
            sortBy: 'name_asc',
            onSale: false,
            featured: false,
          })}
          className="w-full px-4 py-2 border border-gray-300 rounded-md text-sm hover:bg-gray-50"
        >
          クリア
        </button>
      </div>
    </div>
  </div>
);

export const ProductListPage: React.FC<ProductListPageProps> = ({
  categoryId,
  searchQuery,
}) => {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [currentCategory, setCurrentCategory] = useState<Category | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // フィルター状態
  const [filters, setFilters] = useState({
    minPrice: '',
    maxPrice: '',
    brandId: '',
    sortBy: 'name_asc',
    onSale: false,
    featured: false,
  });

  // 商品とカテゴリデータ取得
  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);

        // カテゴリ一覧を取得
        const categoriesData = await productCatalogApi.getRootCategories();
        setCategories(categoriesData);

        // 現在のカテゴリ情報を取得
        if (categoryId) {
          const categoryData = await productCatalogApi.getCategoryById(categoryId);
          setCurrentCategory(categoryData);
        }

        // 商品データを取得
        const params = {
          ...(categoryId && { categoryId }),
          ...(searchQuery && { search: searchQuery }),
          ...(filters.minPrice && { minPrice: parseFloat(filters.minPrice) }),
          ...(filters.maxPrice && { maxPrice: parseFloat(filters.maxPrice) }),
          ...(filters.brandId && { brandId: filters.brandId }),
          ...(filters.onSale && { onSale: true }),
          ...(filters.featured && { featured: true }),
          ...(filters.sortBy && { sort: filters.sortBy as 'price_asc' | 'price_desc' | 'name_asc' | 'name_desc' }),
          size: 50, // 1ページあたりの商品数
        };

        const productsData = await productCatalogApi.getProducts(params);
        setProducts(productsData);
      } catch (err) {
        console.error('Failed to fetch products:', err);
        setError('商品の取得に失敗しました');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [categoryId, searchQuery, filters]);

  // ページタイトルを計算
  const getPageTitle = () => {
    if (currentCategory) return currentCategory.name;
    if (searchQuery) return `"${searchQuery}"の検索結果`;
    return '商品一覧';
  };

  // エラー表示
  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-7xl mx-auto px-4">
          <div className="bg-red-50 border border-red-200 text-red-700 px-6 py-4 rounded-lg">
            <h2 className="text-lg font-medium">エラーが発生しました</h2>
            <p className="mt-1">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-7xl mx-auto px-4">
        {/* パンくずリスト */}
        <Breadcrumb currentCategory={currentCategory} />

        {/* ページヘッダー */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            {getPageTitle()}
          </h1>
          {currentCategory?.description && (
            <p className="text-gray-600">{currentCategory.description}</p>
          )}
        </div>

        {/* カテゴリナビゲーション */}
        {!categoryId && categories.length > 0 && (
          <div className="mb-8">
            <h2 className="text-xl font-semibold mb-4">カテゴリから探す</h2>
            <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
              {categories.map((category) => (
                <Link
                  key={category.id}
                  href={`/products?category=${category.id}`}
                  className="bg-white border border-gray-200 rounded-lg p-4 text-center hover:shadow-md transition-shadow"
                >
                  <h3 className="font-medium text-gray-900">{category.name}</h3>
                  <p className="text-sm text-gray-500">{category.productCount}商品</p>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* フィルターパネル */}
        <FilterPanel filters={filters} onFiltersChange={setFilters} />

        {/* 商品一覧 */}
        {loading ? (
          <div className="flex justify-center items-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900"></div>
          </div>
        ) : (
          <>
            <div className="mb-6">
              <p className="text-gray-600">
                {products.length}件の商品が見つかりました
              </p>
            </div>
            
            {products.length === 0 ? (
              <div className="text-center py-16">
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  商品が見つかりませんでした
                </h3>
                <p className="text-gray-600">
                  検索条件を変更するか、フィルターをクリアしてください。
                </p>
              </div>
            ) : (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {products.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
};
