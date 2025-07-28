'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';

// サンプル注文データ
const sampleOrders = [
  {
    id: 'ORD-2024-001',
    date: '2024-01-15',
    status: 'delivered',
    total: 156800,
    items: [
      {
        id: 1,
        name: 'プロスキー板セット',
        price: 89800,
        quantity: 1,
        image: '/images/skis-pro.jpg'
      },
      {
        id: 2,
        name: 'スキーブーツ 上級者向け',
        price: 45600,
        quantity: 1,
        image: '/images/boots-advanced.jpg'
      },
      {
        id: 3,
        name: 'ゴーグル UV400',
        price: 12800,
        quantity: 1,
        image: '/images/goggles-uv.jpg'
      }
    ],
    shipping: 1600,
    deliveryDate: '2024-01-18'
  },
  {
    id: 'ORD-2024-002',
    date: '2024-01-10',
    status: 'shipped',
    total: 38900,
    items: [
      {
        id: 4,
        name: 'スキーウェア 防水',
        price: 38900,
        quantity: 1,
        image: '/images/wear-waterproof.jpg'
      }
    ],
    shipping: 0,
    estimatedDelivery: '2024-01-22'
  },
  {
    id: 'ORD-2024-003',
    date: '2024-01-05',
    status: 'processing',
    total: 25600,
    items: [
      {
        id: 5,
        name: 'スキーグローブ',
        price: 8900,
        quantity: 1,
        image: '/images/gloves.jpg'
      },
      {
        id: 6,
        name: 'スキーソックス 3足セット',
        price: 4500,
        quantity: 2,
        image: '/images/socks-set.jpg'
      }
    ],
    shipping: 800
  }
];

export default function OrdersPage() {
  const [orders] = useState(sampleOrders);
  const [filterStatus, setFilterStatus] = useState<string>('all');
  const [sortBy, setSortBy] = useState<'date' | 'total'>('date');

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'delivered':
        return 'bg-green-100 text-green-800';
      case 'shipped':
        return 'bg-blue-100 text-blue-800';
      case 'processing':
        return 'bg-yellow-100 text-yellow-800';
      case 'cancelled':
        return 'bg-red-100 text-red-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'delivered':
        return '配送完了';
      case 'shipped':
        return '配送中';
      case 'processing':
        return '処理中';
      case 'cancelled':
        return 'キャンセル';
      default:
        return '不明';
    }
  };

  const filteredOrders = orders.filter(order => 
    filterStatus === 'all' || order.status === filterStatus
  );

  const sortedOrders = [...filteredOrders].sort((a, b) => {
    if (sortBy === 'date') {
      return new Date(b.date).getTime() - new Date(a.date).getTime();
    } else {
      return b.total - a.total;
    }
  });

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">注文履歴</h1>
              <p className="mt-1 text-sm text-gray-500">
                {orders.length}件の注文
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
        {orders.length === 0 ? (
          /* 空の状態 */
          <div className="text-center py-16">
            <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
            </svg>
            <h3 className="mt-2 text-sm font-medium text-gray-900">注文履歴はありません</h3>
            <p className="mt-1 text-sm text-gray-500">
              商品を購入すると、ここに注文履歴が表示されます
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
            {/* フィルター・ソート */}
            <div className="bg-white px-4 py-3 border-b border-gray-200 sm:px-6 mb-6">
              <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
                <h3 className="text-lg leading-6 font-medium text-gray-900 mb-4 sm:mb-0">
                  注文一覧
                </h3>
                <div className="flex flex-col sm:flex-row sm:items-center space-y-2 sm:space-y-0 sm:space-x-4">
                  <div className="flex items-center space-x-2">
                    <label htmlFor="status-filter" className="text-sm font-medium text-gray-700">
                      ステータス:
                    </label>
                    <select
                      id="status-filter"
                      value={filterStatus}
                      onChange={(e) => setFilterStatus(e.target.value)}
                      className="border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option value="all">すべて</option>
                      <option value="processing">処理中</option>
                      <option value="shipped">配送中</option>
                      <option value="delivered">配送完了</option>
                      <option value="cancelled">キャンセル</option>
                    </select>
                  </div>
                  <div className="flex items-center space-x-2">
                    <label htmlFor="sort" className="text-sm font-medium text-gray-700">
                      並び替え:
                    </label>
                    <select
                      id="sort"
                      value={sortBy}
                      onChange={(e) => setSortBy(e.target.value as 'date' | 'total')}
                      className="border border-gray-300 rounded-md px-3 py-1 text-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    >
                      <option value="date">注文日時</option>
                      <option value="total">金額</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            {/* 注文リスト */}
            <div className="space-y-6">
              {sortedOrders.map((order) => (
                <div key={order.id} className="bg-white shadow rounded-lg overflow-hidden">
                  {/* 注文ヘッダー */}
                  <div className="px-6 py-4 border-b border-gray-200">
                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
                      <div className="flex flex-col sm:flex-row sm:items-center sm:space-x-6">
                        <div>
                          <p className="text-sm font-medium text-gray-900">注文番号</p>
                          <p className="text-sm text-gray-500">{order.id}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">注文日</p>
                          <p className="text-sm text-gray-500">{new Date(order.date).toLocaleDateString('ja-JP')}</p>
                        </div>
                        <div>
                          <p className="text-sm font-medium text-gray-900">合計金額</p>
                          <p className="text-sm text-gray-500">¥{order.total.toLocaleString()}</p>
                        </div>
                      </div>
                      <div className="mt-4 sm:mt-0 flex items-center space-x-4">
                        <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(order.status)}`}>
                          {getStatusText(order.status)}
                        </span>
                        <Link
                          href={`/orders/${order.id}`}
                          className="text-blue-600 hover:text-blue-500 text-sm font-medium"
                        >
                          詳細を見る
                        </Link>
                      </div>
                    </div>

                    {/* 配送情報 */}
                    {order.status === 'delivered' && order.deliveryDate && (
                      <div className="mt-2">
                        <p className="text-sm text-green-600">
                          {new Date(order.deliveryDate).toLocaleDateString('ja-JP')} に配送完了
                        </p>
                      </div>
                    )}
                    {order.status === 'shipped' && order.estimatedDelivery && (
                      <div className="mt-2">
                        <p className="text-sm text-blue-600">
                          {new Date(order.estimatedDelivery).toLocaleDateString('ja-JP')} 到着予定
                        </p>
                      </div>
                    )}
                  </div>

                  {/* 注文商品 */}
                  <div className="px-6 py-4">
                    <div className="space-y-4">
                      {order.items.map((item) => (
                        <div key={item.id} className="flex items-center space-x-4">
                          <div className="flex-shrink-0">
                            <Image
                              src={item.image}
                              alt={item.name}
                              width={80}
                              height={80}
                              className="h-20 w-20 object-cover rounded-md"
                            />
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-gray-900 truncate">
                              {item.name}
                            </p>
                            <p className="text-sm text-gray-500">
                              数量: {item.quantity}
                            </p>
                          </div>
                          <div className="text-sm font-medium text-gray-900">
                            ¥{(item.price * item.quantity).toLocaleString()}
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* 注文サマリー */}
                    <div className="mt-6 border-t border-gray-200 pt-4">
                      <div className="flex justify-end space-y-1 text-sm">
                        <div className="space-y-1">
                          <div className="flex justify-between">
                            <span className="text-gray-500">小計:</span>
                            <span className="text-gray-900">¥{(order.total - order.shipping).toLocaleString()}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">送料:</span>
                            <span className="text-gray-900">
                              {order.shipping === 0 ? '無料' : `¥${order.shipping.toLocaleString()}`}
                            </span>
                          </div>
                          <div className="flex justify-between font-medium text-gray-900 border-t border-gray-200 pt-1">
                            <span>合計:</span>
                            <span>¥{order.total.toLocaleString()}</span>
                          </div>
                        </div>
                      </div>
                    </div>

                    {/* アクション */}
                    <div className="mt-6 flex flex-col sm:flex-row sm:justify-end space-y-2 sm:space-y-0 sm:space-x-2">
                      {order.status === 'delivered' && (
                        <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-sm">
                          再注文
                        </button>
                      )}
                      <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-sm">
                        領収書をダウンロード
                      </button>
                      {order.status === 'processing' && (
                        <button className="px-4 py-2 border border-red-300 text-red-700 rounded-md hover:bg-red-50 text-sm">
                          キャンセル
                        </button>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
