'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { MainLayout } from '@/components/layout/MainLayout';
import { ProductList } from '@/components/product/ProductList';
import { useCartStore } from '@/stores/cartStore';
import { Product } from '@/types/product';
import { productsAPI } from '@/services/api/products';

// サンプルのスキー用品データ（フォールバック用）
const sampleProducts: Product[] = [
  {
    id: '1',
    name: 'プロフェッショナル スキー板',
    description: '上級者向けの高性能スキー板。カービングに最適で、安定性と操作性を両立したモデルです。',
    shortDescription: '上級者向け高性能スキー板',
    price: 89000,
    originalPrice: 120000,
    discountPercentage: 26,
    sku: 'SKI-PRO-001',
    categoryId: 'cat-ski',
    category: {
      id: 'cat-ski',
      name: 'スキー板',
      slug: 'skis',
      description: '各種スキー板',
      children: [],
      isActive: true,
      sortOrder: 1,
      productCount: 15
    },
    images: [
      {
        id: 'img-1',
        url: '/placeholder-ski.jpg',
        alt: 'プロフェッショナル スキー板',
        sortOrder: 1,
        isPrimary: true
      }
    ],
    attributes: [],
    variants: [],
    inventory: {
      quantity: 25,
      reservedQuantity: 3,
      availableQuantity: 22,
      lowStockThreshold: 5,
      status: 'in_stock'
    },
    specifications: [],
    reviews: [],
    rating: 4.8,
    reviewCount: 24,
    tags: ['スキー', '上級者', '高性能'],
    isActive: true,
    isFeatured: true,
    isOnSale: true,
    createdAt: '2025-07-01T00:00:00Z',
    updatedAt: '2025-07-25T00:00:00Z'
  },
  {
    id: '2',
    name: 'スキーブーツ 快適フィット',
    description: '長時間の滑走でも疲れにくい快適なスキーブーツ。初心者から中級者におすすめです。',
    shortDescription: '快適なスキーブーツ',
    price: 45000,
    sku: 'BOOT-COM-001',
    categoryId: 'cat-boot',
    category: {
      id: 'cat-boot',
      name: 'スキーブーツ',
      slug: 'ski-boots',
      description: '各種スキーブーツ',
      children: [],
      isActive: true,
      sortOrder: 2,
      productCount: 12
    },
    images: [
      {
        id: 'img-2',
        url: '/placeholder-boots.jpg',
        alt: 'スキーブーツ 快適フィット',
        sortOrder: 1,
        isPrimary: true
      }
    ],
    attributes: [],
    variants: [],
    inventory: {
      quantity: 18,
      reservedQuantity: 2,
      availableQuantity: 16,
      lowStockThreshold: 5,
      status: 'in_stock'
    },
    specifications: [],
    reviews: [],
    rating: 4.5,
    reviewCount: 18,
    tags: ['スキーブーツ', '快適', '初心者'],
    isActive: true,
    isFeatured: false,
    isOnSale: false,
    createdAt: '2025-07-01T00:00:00Z',
    updatedAt: '2025-07-25T00:00:00Z'
  },
  {
    id: '3',
    name: 'スキーヘルメット セーフティプロ',
    description: '安全性を最優先に設計されたスキーヘルメット。軽量で通気性も抜群です。',
    shortDescription: '安全性重視のスキーヘルメット',
    price: 12000,
    sku: 'HELM-SAF-001',
    categoryId: 'cat-helmet',
    category: {
      id: 'cat-helmet',
      name: 'ヘルメット',
      slug: 'helmets',
      description: '各種ヘルメット',
      children: [],
      isActive: true,
      sortOrder: 3,
      productCount: 8
    },
    images: [
      {
        id: 'img-3',
        url: '/placeholder-helmet.jpg',
        alt: 'スキーヘルメット セーフティプロ',
        sortOrder: 1,
        isPrimary: true
      }
    ],
    attributes: [],
    variants: [],
    inventory: {
      quantity: 35,
      reservedQuantity: 5,
      availableQuantity: 30,
      lowStockThreshold: 10,
      status: 'in_stock'
    },
    specifications: [],
    reviews: [],
    rating: 4.7,
    reviewCount: 32,
    tags: ['ヘルメット', '安全', '軽量'],
    isActive: true,
    isFeatured: true,
    isOnSale: false,
    createdAt: '2025-07-01T00:00:00Z',
    updatedAt: '2025-07-25T00:00:00Z'
  },
  {
    id: '4',
    name: 'スキーウェア プレミアム',
    description: '防水・防風機能を備えたプレミアムスキーウェア。スタイリッシュなデザインで機能性も抜群。',
    shortDescription: 'プレミアムスキーウェア',
    price: 35000,
    originalPrice: 42000,
    discountPercentage: 17,
    sku: 'WEAR-PRE-001',
    categoryId: 'cat-wear',
    category: {
      id: 'cat-wear',
      name: 'スキーウェア',
      slug: 'ski-wear',
      description: '各種スキーウェア',
      children: [],
      isActive: true,
      sortOrder: 4,
      productCount: 20
    },
    images: [
      {
        id: 'img-4',
        url: '/placeholder-wear.jpg',
        alt: 'スキーウェア プレミアム',
        sortOrder: 1,
        isPrimary: true
      }
    ],
    attributes: [],
    variants: [],
    inventory: {
      quantity: 15,
      reservedQuantity: 3,
      availableQuantity: 12,
      lowStockThreshold: 5,
      status: 'in_stock'
    },
    specifications: [],
    reviews: [],
    rating: 4.6,
    reviewCount: 21,
    tags: ['スキーウェア', '防水', 'プレミアム'],
    isActive: true,
    isFeatured: true,
    isOnSale: true,
    createdAt: '2025-07-01T00:00:00Z',
    updatedAt: '2025-07-25T00:00:00Z'
  }
];

export default function Home() {
  const router = useRouter();
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { addItem, loading: cartLoading, error: cartError } = useCartStore();

  // featured商品を取得
  useEffect(() => {
    const fetchFeaturedProducts = async () => {
      try {
        setIsLoading(true);
        setError(null);
        
        // APIからfeatured products取得
        const products = await productsAPI.getFeaturedProducts(6);
        console.log('Featured products from API:', products);
        console.log('First product structure:', products[0]);
        setFeaturedProducts(products);
        
      } catch (err) {
        console.error('Featured products fetch error:', err);
        setError('注目商品の取得に失敗しました');
        // エラー時はサンプルデータを使用
        setFeaturedProducts(sampleProducts.filter(p => p.isFeatured));
      } finally {
        setIsLoading(false);
      }
    };

    fetchFeaturedProducts();
  }, []);

  const handleAddToCart = async (productId: string) => {
    console.log('🛒 [HomePage] カートに追加呼び出し:', productId);
    
    // 商品詳細を取得
    const product = featuredProducts.find(p => p.id === productId);
    if (!product) {
      console.error('❌ [HomePage] 商品が見つかりません:', productId);
      console.log('🔍 [HomePage] Available products:', featuredProducts.map(p => ({ id: p.id, name: p.name })));
      alert('商品の情報が取得できませんでした');
      return;
    }

    console.log('✅ [HomePage] 商品情報:', { 
      id: product.id, 
      name: product.name, 
      sku: product.sku,
      inStock: product.inventory.status === 'in_stock'
    });

    try {
      console.log('🚀 [HomePage] Adding to cart:', product.id, product.sku, 1);
      await addItem(product.id, product.sku, 1);
      
      // 成功通知
      console.log('✅ [HomePage] カート追加成功');
      alert(`${product.name} をカートに追加しました`);
    } catch (error) {
      console.error('❌ [HomePage] カートへの追加に失敗しました:', error);
      const errorMessage = cartError || 'カートへの追加に失敗しました';
      alert(`エラー: ${errorMessage}`);
    }
  };

  const handleToggleFavorite = (productId: string) => {
    console.log('お気に入り切り替え:', productId);
    // お気に入り切り替えのロジックを実装
  };

  const handleNavigateToProducts = () => {
    router.push('/products');
  };

  const handleNavigateToAISupport = () => {
    router.push('/chat');
  };

  return (
    <MainLayout>
      <div className="bg-gradient-to-b from-blue-50 to-white">
        {/* ヒーローセクション */}
        <section className="relative h-96 bg-gradient-to-r from-blue-600 to-blue-800 text-white">
          <div className="absolute inset-0 bg-gradient-to-br from-blue-50/80 via-blue-100/70 to-blue-200/60" style={{ backgroundColor: '#E3F2FD', backgroundImage: 'linear-gradient(to bottom right, #E3F2FD 0%, #BBDEFB 50%, #90CAF9 100%)', opacity: '0.85' }}></div>
          <div className="relative container mx-auto px-4 h-full flex items-center justify-center text-center">
            <div>
              <h1 className="text-5xl font-bold mb-4 text-blue-900">Azure SkiShop へようこそ</h1>
              <p className="text-xl mb-8 text-blue-800">最高品質のスキー・スノーボード用品で、あなたの冬のアドベンチャーを始めよう</p>
              <div className="flex gap-4 justify-center">
                <button 
                  onClick={handleNavigateToProducts}
                  className="bg-blue-600 text-white px-8 py-3 rounded-lg font-semibold hover:bg-blue-700 transition-colors shadow-lg"
                >
                  商品を見る
                </button>
                <button 
                  onClick={handleNavigateToAISupport}
                  className="bg-white/20 text-blue-900 border border-blue-300 px-8 py-3 rounded-lg font-semibold hover:bg-white/30 transition-colors shadow-lg backdrop-blur-sm"
                >
                  AI相談を始める
                </button>
              </div>
            </div>
          </div>
        </section>

        {/* 商品セクション */}
        <section className="py-16">
          <div className="container mx-auto px-4">
            <div className="text-center mb-12">
              <h2 className="text-3xl font-bold text-gray-800 mb-4">
                おすすめスキー用品
              </h2>
              <p className="text-gray-600 max-w-2xl mx-auto">
                プロフェッショナルから初心者まで、あらゆるレベルのスキーヤーに最適な用品を取り揃えています。
              </p>
            </div>
            
            {isLoading && (
              <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
              </div>
            )}
            
            {error && (
              <div className="text-center text-red-600 mb-8">
                <p>{error}</p>
                <p className="text-sm text-gray-500 mt-2">サンプルデータを表示しています</p>
              </div>
            )}
            
            <ProductList
              products={featuredProducts}
              onAddToCart={handleAddToCart}
              onToggleFavorite={handleToggleFavorite}
              favoriteProductIds={[]}
              columns={4}
              className="max-w-7xl mx-auto"
            />
          </div>
        </section>

        {/* 特徴セクション */}
        <section className="py-16 bg-gray-50">
          <div className="container mx-auto px-4">
            <h2 className="text-3xl font-bold text-center text-gray-800 mb-12">
              なぜ私たちを選ぶのか
            </h2>
            <div className="grid md:grid-cols-3 gap-8">
              <div className="text-center">
                <div className="bg-blue-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold mb-2">高品質な用品</h3>
                <p className="text-gray-600">厳選されたブランドの高品質なスキー用品のみを取り扱っています。</p>
              </div>
              <div className="text-center">
                <div className="bg-green-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold mb-2">お得な価格</h3>
                <p className="text-gray-600">競争力のある価格設定で、最高のコストパフォーマンスを提供します。</p>
              </div>
              <div className="text-center">
                <div className="bg-purple-100 w-16 h-16 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18.364 5.636l-3.536 3.536m0 5.656l3.536 3.536M9.172 9.172L5.636 5.636m3.536 9.192L5.636 18.364M12 12h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold mb-2">専門サポート</h3>
                <p className="text-gray-600">経験豊富なスタッフが最適な用品選びをサポートします。</p>
              </div>
            </div>
          </div>
        </section>
      </div>
    </MainLayout>
  );
}
