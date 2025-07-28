'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { MainLayout } from '@/components/layout/MainLayout';
import { 
  Category, 
  Product,
  productCatalogApi 
} from '@/services/api/product-catalog';

/**
 * カテゴリ一覧ページ
 * Product Catalog Serviceから実際のメインカテゴリデータを取得して表示
 * サブカテゴリは表示せず、トップレベルのカテゴリのみを表示する
 */
// カテゴリ商品数計算のヘルパー関数
const calculateTotalProductCount = (mainCategory: Category, allCategories: Category[]): number => {
  // メインカテゴリ自体の商品数
  let totalCount = mainCategory.productCount || 0;
  
  // サブカテゴリの商品数を合計（pathベースでの親子関係判定）
  const subCategories = allCategories.filter(cat => 
    cat.level === 1 && cat.path.startsWith(mainCategory.path + '/')
  );
  
  subCategories.forEach(subCat => {
    totalCount += subCat.productCount || 0;
  });
  
  return totalCount;
};

// カテゴリ別の色を動的に決定するヘルパー関数
const getCategoryColor = (category: Category, categoryIndex?: number): { from: string; to: string; accent: string } => {
  // 心地よい配色パレット（色彩心理学に基づいた調和のとれた色）
  const colorPalettes = [
    { from: 'from-blue-400', to: 'to-blue-600', accent: 'text-blue-100' },        // 青 - 信頼性、安定性
    { from: 'from-purple-400', to: 'to-purple-600', accent: 'text-purple-100' },   // 紫 - 創造性、高級感
    { from: 'from-green-400', to: 'to-green-600', accent: 'text-green-100' },      // 緑 - 自然、安全性
    { from: 'from-orange-400', to: 'to-orange-600', accent: 'text-orange-100' },   // オレンジ - エネルギー、活力
    { from: 'from-indigo-400', to: 'to-indigo-600', accent: 'text-indigo-100' },   // インディゴ - 深さ、知性
    { from: 'from-teal-400', to: 'to-teal-600', accent: 'text-teal-100' },         // ティール - バランス、洗練
    { from: 'from-red-400', to: 'to-red-600', accent: 'text-red-100' },            // レッド - 情熱、注意
    { from: 'from-pink-400', to: 'to-pink-600', accent: 'text-pink-100' },         // ピンク - 優しさ、親しみやすさ
    { from: 'from-emerald-400', to: 'to-emerald-600', accent: 'text-emerald-100' }, // エメラルド - 成長、調和
    { from: 'from-cyan-400', to: 'to-cyan-600', accent: 'text-cyan-100' },         // シアン - 清涼感、革新
    { from: 'from-amber-400', to: 'to-amber-600', accent: 'text-amber-100' },      // アンバー - 温かさ、快適さ
    { from: 'from-violet-400', to: 'to-violet-600', accent: 'text-violet-100' },   // バイオレット - 神秘性、独創性
    { from: 'from-rose-400', to: 'to-rose-600', accent: 'text-rose-100' },         // ローズ - 魅力、ロマンス
    { from: 'from-sky-400', to: 'to-sky-600', accent: 'text-sky-100' },           // スカイ - 開放感、自由
    { from: 'from-lime-400', to: 'to-lime-600', accent: 'text-lime-100' },         // ライム - 新鮮さ、活気
    { from: 'from-slate-400', to: 'to-slate-600', accent: 'text-slate-100' },      // スレート - モダン、プロフェッショナル
  ];

  // カテゴリIDの文字列から数値ハッシュを生成（一貫性のある色割り当てのため）
  const generateHashFromId = (id: string): number => {
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
      const char = id.charCodeAt(i);
      hash = ((hash << 5) - hash) + char;
      hash = hash & hash; // 32bit整数に変換
    }
    return Math.abs(hash);
  };

  // 色のインデックスを決定（複数の方法でフォールバック）
  let colorIndex = 0;
  
  if (category.id) {
    // 1. カテゴリIDから一意のハッシュを生成
    colorIndex = generateHashFromId(category.id) % colorPalettes.length;
  } else if (categoryIndex !== undefined) {
    // 2. カテゴリの表示順インデックスを使用
    colorIndex = categoryIndex % colorPalettes.length;
  } else {
    // 3. カテゴリ名の文字数から決定（最後の手段）
    colorIndex = category.name.length % colorPalettes.length;
  }

  return colorPalettes[colorIndex];
};

export default function CategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([]);
  const [allCategories, setAllCategories] = useState<Category[]>([]);
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<Category | null>(null);
  const [subCategories, setSubCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // カテゴリデータを取得
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        setLoading(true);
        setError(null);
        
        const categoriesData = await productCatalogApi.getAllCategories();
        setAllCategories(categoriesData);
        
        // メインカテゴリのみをフィルタリング（parentが存在せずlevel=0のカテゴリ）
        const mainCategories = categoriesData.filter(category => {
          return !category.parent && category.level === 0;
        });
        
        // 各メインカテゴリの商品数を再計算（サブカテゴリ含む）
        const mainCategoriesWithTotalCount = mainCategories.map(mainCat => ({
          ...mainCat,
          totalProductCount: calculateTotalProductCount(mainCat, categoriesData)
        }));
        
        setCategories(mainCategoriesWithTotalCount);
        
        // 注目商品も取得（最初の8件のみ取得）
        const featuredData = await productCatalogApi.getProducts({ 
          featured: true
        });
        setFeaturedProducts(featuredData.slice(0, 8));
      } catch (err) {
        console.error('Failed to fetch categories:', err);
        setError('カテゴリデータの取得に失敗しました。');
      } finally {
        setLoading(false);
      }
    };

    fetchCategories();
  }, []);

  // カテゴリクリック処理
  const handleCategoryClick = (category: Category & { totalProductCount?: number }) => {
    const categorySubCategories = allCategories.filter(cat => 
      cat.level === 1 && cat.path.startsWith(category.path + '/')
    );
    
    if (categorySubCategories.length > 0) {
      // サブカテゴリが存在する場合はサブカテゴリ一覧を表示
      setSelectedCategory(category);
      setSubCategories(categorySubCategories);
    } else {
      // サブカテゴリがない場合は直接商品一覧へ
      window.location.href = `/products?categoryId=${category.id}`;
    }
  };

  // メインカテゴリ一覧に戻る
  const handleBackToMain = () => {
    setSelectedCategory(null);
    setSubCategories([]);
  };

  // カテゴリカード コンポーネント
  const CategoryCard: React.FC<{ 
    category: Category & { totalProductCount?: number };
    onClick?: () => void;
    showSubCategoryCount?: boolean;
    categoryIndex?: number; // 表示順インデックスを追加
  }> = ({ category, onClick, showSubCategoryCount = true, categoryIndex }) => {
    const subCategoryCount = allCategories.filter(cat => 
      cat.level === 1 && cat.path.startsWith(category.path + '/')
    ).length;

    // カテゴリ専用の色を動的に取得
    const categoryColors = getCategoryColor(category, categoryIndex);

    return (
      <div 
        onClick={onClick || (() => handleCategoryClick(category))}
        className="group cursor-pointer"
      >
        <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
          <div className={`h-48 bg-gradient-to-br ${categoryColors.from} ${categoryColors.to} flex items-center justify-center`}>
            <div className="text-center text-white">
              <h3 className="text-2xl font-bold mb-2">{category.name}</h3>
              <p className={categoryColors.accent}>
                {category.totalProductCount !== undefined 
                  ? category.totalProductCount 
                  : category.productCount || 0
                }件の商品
              </p>
              {showSubCategoryCount && subCategoryCount > 0 && (
                <p className={`${categoryColors.accent} opacity-80 text-sm mt-1`}>
                  {subCategoryCount}個のサブカテゴリ
                </p>
              )}
            </div>
          </div>
          
          <div className="p-4">
            {category.description && (
              <p className="text-gray-600 text-sm mb-3">
                {category.description}
              </p>
            )}
            
            <div className="flex items-center justify-between">
              <span className="text-sm text-gray-500">
                {subCategoryCount > 0 ? 'カテゴリを見る' : '商品を見る'}
              </span>
              <svg 
                className="w-4 h-4 text-gray-400 group-hover:text-blue-500 transition-colors" 
                fill="none" 
                stroke="currentColor" 
                viewBox="0 0 24 24"
              >
                <path 
                  strokeLinecap="round" 
                  strokeLinejoin="round" 
                  strokeWidth={2} 
                  d="M9 5l7 7-7 7" 
                />
              </svg>
            </div>
          </div>
        </div>
      </div>
    );
  };

  // 商品カード コンポーネント（簡易版）
  const ProductCard: React.FC<{ product: Product }> = ({ product }) => {
    const formatPrice = (price: number): string => {
      return new Intl.NumberFormat('ja-JP', {
        style: 'currency',
        currency: 'JPY',
      }).format(price);
    };

    return (
      <div 
        onClick={() => window.location.href = `/products/${product.id}`}
        className="group cursor-pointer"
      >
        <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow duration-300">
          <div className="aspect-w-1 aspect-h-1 w-full overflow-hidden bg-gray-200">
            {product.imageUrls && product.imageUrls.length > 0 ? (
              <img
                className="h-full w-full object-cover object-center group-hover:opacity-75"
                src={product.imageUrls[0]}
                alt={product.name}
              />
            ) : (
              <div className="h-48 bg-gray-200 flex items-center justify-center">
                <span className="text-gray-400">画像なし</span>
              </div>
            )}
          </div>
          
          <div className="p-4">
            <h3 className="text-sm font-medium text-gray-900 mb-1">
              {product.name}
            </h3>
            
            <div className="flex items-center justify-between">
              <span className="text-lg font-bold text-gray-900">
                {formatPrice(product.currentPrice)}
              </span>
              
              {product.onSale && (
                <span className="bg-red-100 text-red-800 text-xs font-medium px-2 py-1 rounded">
                  セール
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  return (
    <MainLayout>
      <div className="bg-gray-50 min-h-screen">
        <div className="container mx-auto px-4 py-8">
          {/* ヘッダー */}
          <div className="mb-8">
            {selectedCategory ? (
              <>
                <div className="flex items-center mb-4">
                  <button 
                    onClick={handleBackToMain}
                    className="flex items-center text-blue-600 hover:text-blue-700 mr-4"
                  >
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                    メインカテゴリに戻る
                  </button>
                </div>
                <h1 className="text-3xl font-bold text-gray-900 mb-4">
                  {selectedCategory.name} - サブカテゴリ
                </h1>
                <p className="text-gray-600">
                  {selectedCategory.name}のサブカテゴリから商品を探してください。
                </p>
              </>
            ) : (
              <>
                <h1 className="text-3xl font-bold text-gray-900 mb-4">
                  商品カテゴリ
                </h1>
                <p className="text-gray-600">
                  メインカテゴリを選択して商品を探してください。各カテゴリをクリックすると詳細な商品一覧をご覧いただけます。
                </p>
              </>
            )}
          </div>

          {/* エラー表示 */}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
              <div className="flex">
                <div className="text-red-700">
                  <p>{error}</p>
                </div>
              </div>
            </div>
          )}

          {/* カテゴリ一覧 */}
          <div className="mb-12">
            <h2 className="text-2xl font-bold text-gray-900 mb-6">
              {selectedCategory ? `${selectedCategory.name}のサブカテゴリ` : 'メインカテゴリ'}
            </h2>
            
            {loading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {[...Array(8)].map((_, index) => (
                  <div key={`loading-${index}`} className="bg-white rounded-lg shadow-md overflow-hidden animate-pulse">
                    <div className="h-48 bg-gray-200"></div>
                    <div className="p-4">
                      <div className="h-4 bg-gray-200 rounded mb-2"></div>
                      <div className="h-3 bg-gray-200 rounded"></div>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <>
                {(selectedCategory ? subCategories : categories).length > 0 ? (
                  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                    {(selectedCategory ? subCategories : categories).map((category, index) => (
                      <CategoryCard 
                        key={category.id} 
                        category={category}
                        categoryIndex={index} // インデックスを渡して一貫した色割り当てを実現
                        onClick={selectedCategory ? 
                          () => window.location.href = `/products?categoryId=${category.id}` : 
                          undefined
                        }
                        showSubCategoryCount={!selectedCategory}
                      />
                    ))}
                  </div>
                ) : (
                  <div className="text-center py-12">
                    <p className="text-gray-500 text-lg">
                      {selectedCategory ? 'サブカテゴリが見つかりませんでした。' : 'メインカテゴリが見つかりませんでした。'}
                    </p>
                  </div>
                )}
              </>
            )}
          </div>

          {/* 注目商品セクション（メインカテゴリ表示時のみ） */}
          {!selectedCategory && featuredProducts.length > 0 && (
            <div>
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-2xl font-bold text-gray-900">
                  注目商品
                </h2>
                <Link 
                  href="/products?featured=true"
                  className="text-blue-600 hover:text-blue-700 font-medium"
                >
                  すべて見る →
                </Link>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                {featuredProducts.slice(0, 4).map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </MainLayout>
  );
}
