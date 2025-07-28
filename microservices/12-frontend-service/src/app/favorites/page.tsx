'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { MainLayout } from '@/components/layout/MainLayout';

// サンプルお気に入り商品データ
const sampleFavorites = [
  {
    id: 1,
    name: 'プロスキー板セット',
    price: 89800,
    image: '/images/skis-pro.jpg',
    category: 'スキー板',
    brand: 'Alpine Pro',
    addedDate: '2024-01-15'
  },
  {
    id: 2,
    name: 'スキーブーツ 上級者向け',
    price: 45600,
    image: '/images/boots-advanced.jpg',
    category: 'ブーツ',
    brand: 'TechSki',
    addedDate: '2024-01-10'
  },
  {
    id: 3,
    name: 'ゴーグル UV400',
    price: 12800,
    image: '/images/goggles-uv.jpg',
    category: 'アクセサリー',
    brand: 'VisionSki',
    addedDate: '2024-01-08'
  },
  {
    id: 4,
    name: 'スキーウェア 防水',
    price: 38900,
    image: '/images/wear-waterproof.jpg',
    category: 'ウェア',
    brand: 'AlpineWear',
    addedDate: '2024-01-05'
  }
];

export default function FavoritesPage() {
  const [favorites, setFavorites] = useState(sampleFavorites);
  const [sortBy, setSortBy] = useState<'name' | 'price' | 'date'>('date');

  const handleRemoveFromFavorites = (productId: number) => {
    setFavorites(prevFavorites => 
      prevFavorites.filter(item => item.id !== productId)
    );
  };

  const sortedFavorites = [...favorites].sort((a, b) => {
    switch (sortBy) {
      case 'name':
        return a.name.localeCompare(b.name);
      case 'price':
        return a.price - b.price;
      case 'date':
        return new Date(b.addedDate).getTime() - new Date(a.addedDate).getTime();
      default:
        return 0;
    }
  });

  return (
    <MainLayout>
      <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">お気に入り</h1>
              <p className="mt-1 text-sm text-gray-500">
                {favorites.length}件の商品
              </p>
            </div>
            <Link
              href="/"
              className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
            >
              ホームに戻る
            </Link>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {favorites.length === 0 ? (
          /* 空の状態 */
          <div className="text-center py-16">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">お気に入りはありません</h3>
            <p className="mt-1 text-sm text-gray-500">
              気になる商品をお気に入りに追加しましょう
            </p>
            <div className="mt-6">
              <Link
                href="/products"
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                商品を見る
              </Link>
            </div>
          </div>
        ) : (
          <>
            {/* ソート・フィルター */}
            <div className="bg-white px-4 py-3 border-b border-gray-200 sm:px-6">
              <div className="flex items-center justify-between">
                <h3 className="text-lg leading-6 font-medium text-gray-900">
                  お気に入り商品一覧
                </h3>
                <div className="flex items-center space-x-2">
                  <label htmlFor="sort" className="text-sm font-medium text-gray-700">
                    並び替え:
                  </label>
                  <select
                    id="sort"
                    value={sortBy}
                    onChange={(e) => setSortBy(e.target.value as 'name' | 'price' | 'date')}
                    className="border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  >
                    <option value="date">追加日時</option>
                    <option value="name">商品名</option>
                    <option value="price">価格</option>
                  </select>
                </div>
              </div>
            </div>

            {/* 商品グリッド */}
            <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {sortedFavorites.map((product) => (
                <div key={product.id} className="group relative bg-white rounded-lg shadow-sm hover:shadow-md transition-shadow duration-200">
                  <div className="aspect-w-1 aspect-h-1 w-full overflow-hidden rounded-t-lg bg-gray-200">
                    <Image
                      src={product.image}
                      alt={product.name}
                      width={300}
                      height={300}
                      className="h-48 w-full object-cover object-center group-hover:opacity-75"
                    />
                    <button
                      onClick={() => handleRemoveFromFavorites(product.id)}
                      className="absolute top-2 right-2 p-2 rounded-full bg-white shadow-md hover:bg-red-50 transition-colors"
                      aria-label="お気に入りから削除"
                    >
                      <svg className="h-5 w-5 text-red-500" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                    </button>
                  </div>
                  <div className="p-4">
                    <h3 className="text-sm font-medium text-gray-900 line-clamp-2">
                      <Link href={`/products/${product.id}`}>
                        {product.name}
                      </Link>
                    </h3>
                    <p className="mt-1 text-sm text-gray-500">{product.brand}</p>
                    <p className="mt-1 text-sm text-gray-500">{product.category}</p>
                    <p className="mt-2 text-lg font-medium text-gray-900">
                      ¥{product.price.toLocaleString()}
                    </p>
                    <p className="mt-1 text-xs text-gray-400">
                      追加日: {new Date(product.addedDate).toLocaleDateString('ja-JP')}
                    </p>
                    <div className="mt-4 flex space-x-2">
                      <button className="flex-1 bg-blue-600 text-white text-sm font-medium py-2 px-4 rounded-md hover:bg-blue-700 transition-colors">
                        カートに追加
                      </button>
                      <Link
                        href={`/products/${product.id}`}
                        className="flex-1 bg-gray-200 text-gray-900 text-sm font-medium py-2 px-4 rounded-md hover:bg-gray-300 transition-colors text-center"
                      >
                        詳細を見る
                      </Link>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* お気に入り操作 */}
            <div className="mt-8 bg-white rounded-lg shadow p-6">
              <h3 className="text-lg font-medium text-gray-900 mb-4">一括操作</h3>
              <div className="flex space-x-4">
                <button
                  onClick={() => setFavorites([])}
                  className="px-4 py-2 border border-red-300 text-red-700 rounded-md hover:bg-red-50 transition-colors"
                >
                  すべて削除
                </button>
                <button
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                  onClick={() => {
                    // すべてカートに追加の処理
                    console.log('すべてカートに追加');
                  }}
                >
                  すべてカートに追加
                </button>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
    </MainLayout>
  );
}
