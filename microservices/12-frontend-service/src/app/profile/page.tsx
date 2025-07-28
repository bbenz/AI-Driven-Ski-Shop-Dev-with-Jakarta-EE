'use client';

import React, { useState } from 'react';
import Link from 'next/link';

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState<'personal' | 'address' | 'preferences' | 'security'>('personal');
  const [isEditing, setIsEditing] = useState(false);
  const [profileData, setProfileData] = useState({
    // 個人情報
    firstName: '太郎',
    lastName: '山田',
    email: 'yamada.taro@example.com',
    phone: '090-1234-5678',
    birthDate: '1990-01-01',
    gender: 'male',
    
    // 住所情報
    postalCode: '100-0001',
    prefecture: '東京都',
    city: '千代田区',
    address: '丸の内1-1-1',
    building: 'サンプルビル101',
    
    // 設定
    language: 'ja',
    currency: 'JPY',
    notifications: {
      email: true,
      sms: false,
      push: true
    },
    
    // アカウント情報
    memberSince: '2023-01-15',
    lastLogin: '2024-01-20T10:30:00Z'
  });

  const handleInputChange = (field: string, value: string | boolean) => {
    if (field.includes('.')) {
      const [parent, child] = field.split('.');
      setProfileData(prev => ({
        ...prev,
        [parent]: {
          ...prev[parent as keyof typeof prev] as object,
          [child]: value
        }
      }));
    } else {
      setProfileData(prev => ({
        ...prev,
        [field]: value
      }));
    }
  };

  const handleSave = () => {
    // 保存処理
    console.log('プロフィール保存:', profileData);
    setIsEditing(false);
  };

  const tabs = [
    { id: 'personal', name: '個人情報', icon: '👤' },
    { id: 'address', name: '住所情報', icon: '📍' },
    { id: 'preferences', name: '設定', icon: '⚙️' },
    { id: 'security', name: 'セキュリティ', icon: '🔒' }
  ];

  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center py-6">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">プロフィール</h1>
              <p className="mt-1 text-sm text-gray-500">
                アカウント情報の確認・編集
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
        <div className="bg-white shadow rounded-lg">
          {/* タブナビゲーション */}
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8 px-6">
              {tabs.map((tab) => (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id as 'personal' | 'address' | 'preferences' | 'security')}
                  className={`py-4 px-1 border-b-2 font-medium text-sm whitespace-nowrap ${
                    activeTab === tab.id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <span className="mr-2">{tab.icon}</span>
                  {tab.name}
                </button>
              ))}
            </nav>
          </div>

          {/* タブコンテンツ */}
          <div className="p-6">
            {/* 編集ボタン */}
            <div className="flex justify-end mb-6">
              {isEditing ? (
                <div className="space-x-2">
                  <button
                    onClick={() => setIsEditing(false)}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50"
                  >
                    キャンセル
                  </button>
                  <button
                    onClick={handleSave}
                    className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                  >
                    保存
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setIsEditing(true)}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
                >
                  編集
                </button>
              )}
            </div>

            {/* 個人情報タブ */}
            {activeTab === 'personal' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      姓
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.firstName}
                        onChange={(e) => handleInputChange('firstName', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.firstName}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      名
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.lastName}
                        onChange={(e) => handleInputChange('lastName', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.lastName}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      メールアドレス
                    </label>
                    {isEditing ? (
                      <input
                        type="email"
                        value={profileData.email}
                        onChange={(e) => handleInputChange('email', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.email}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      電話番号
                    </label>
                    {isEditing ? (
                      <input
                        type="tel"
                        value={profileData.phone}
                        onChange={(e) => handleInputChange('phone', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.phone}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      生年月日
                    </label>
                    {isEditing ? (
                      <input
                        type="date"
                        value={profileData.birthDate}
                        onChange={(e) => handleInputChange('birthDate', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{new Date(profileData.birthDate).toLocaleDateString('ja-JP')}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      性別
                    </label>
                    {isEditing ? (
                      <select
                        value={profileData.gender}
                        onChange={(e) => handleInputChange('gender', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="male">男性</option>
                        <option value="female">女性</option>
                        <option value="other">その他</option>
                        <option value="prefer-not-to-say">回答しない</option>
                      </select>
                    ) : (
                      <p className="text-gray-900">
                        {profileData.gender === 'male' ? '男性' : 
                         profileData.gender === 'female' ? '女性' : 
                         profileData.gender === 'other' ? 'その他' : '回答しない'}
                      </p>
                    )}
                  </div>
                </div>

                <div className="pt-6 border-t border-gray-200">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">アカウント情報</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        登録日
                      </label>
                      <p className="text-gray-900">{new Date(profileData.memberSince).toLocaleDateString('ja-JP')}</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        最終ログイン
                      </label>
                      <p className="text-gray-900">{new Date(profileData.lastLogin).toLocaleString('ja-JP')}</p>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* 住所情報タブ */}
            {activeTab === 'address' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      郵便番号
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.postalCode}
                        onChange={(e) => handleInputChange('postalCode', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.postalCode}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      都道府県
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.prefecture}
                        onChange={(e) => handleInputChange('prefecture', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.prefecture}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      市区町村
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.city}
                        onChange={(e) => handleInputChange('city', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.city}</p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      番地
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.address}
                        onChange={(e) => handleInputChange('address', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.address}</p>
                    )}
                  </div>

                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      建物名・部屋番号
                    </label>
                    {isEditing ? (
                      <input
                        type="text"
                        value={profileData.building}
                        onChange={(e) => handleInputChange('building', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    ) : (
                      <p className="text-gray-900">{profileData.building}</p>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* 設定タブ */}
            {activeTab === 'preferences' && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      言語
                    </label>
                    {isEditing ? (
                      <select
                        value={profileData.language}
                        onChange={(e) => handleInputChange('language', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="ja">日本語</option>
                        <option value="en">English</option>
                        <option value="ko">한국어</option>
                        <option value="zh">中文</option>
                      </select>
                    ) : (
                      <p className="text-gray-900">
                        {profileData.language === 'ja' ? '日本語' : 
                         profileData.language === 'en' ? 'English' : 
                         profileData.language === 'ko' ? '한국어' : '中文'}
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      通貨
                    </label>
                    {isEditing ? (
                      <select
                        value={profileData.currency}
                        onChange={(e) => handleInputChange('currency', e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      >
                        <option value="JPY">日本円 (¥)</option>
                        <option value="USD">米ドル ($)</option>
                        <option value="EUR">ユーロ (€)</option>
                        <option value="KRW">韓国ウォン (₩)</option>
                      </select>
                    ) : (
                      <p className="text-gray-900">
                        {profileData.currency === 'JPY' ? '日本円 (¥)' : 
                         profileData.currency === 'USD' ? '米ドル ($)' : 
                         profileData.currency === 'EUR' ? 'ユーロ (€)' : '韓国ウォン (₩)'}
                      </p>
                    )}
                  </div>
                </div>

                <div className="pt-6 border-t border-gray-200">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">通知設定</h3>
                  <div className="space-y-4">
                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-900">メール通知</p>
                        <p className="text-sm text-gray-500">新商品やセール情報をメールで受け取る</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={profileData.notifications.email}
                          onChange={(e) => handleInputChange('notifications.email', e.target.checked)}
                          disabled={!isEditing}
                          className="sr-only peer"
                        />
                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                      </label>
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-900">SMS通知</p>
                        <p className="text-sm text-gray-500">注文状況をSMSで受け取る</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={profileData.notifications.sms}
                          onChange={(e) => handleInputChange('notifications.sms', e.target.checked)}
                          disabled={!isEditing}
                          className="sr-only peer"
                        />
                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                      </label>
                    </div>

                    <div className="flex items-center justify-between">
                      <div>
                        <p className="text-sm font-medium text-gray-900">プッシュ通知</p>
                        <p className="text-sm text-gray-500">アプリからの通知を受け取る</p>
                      </div>
                      <label className="relative inline-flex items-center cursor-pointer">
                        <input
                          type="checkbox"
                          checked={profileData.notifications.push}
                          onChange={(e) => handleInputChange('notifications.push', e.target.checked)}
                          disabled={!isEditing}
                          className="sr-only peer"
                        />
                        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                      </label>
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* セキュリティタブ */}
            {activeTab === 'security' && (
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-4">パスワード変更</h3>
                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        現在のパスワード
                      </label>
                      <input
                        type="password"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        新しいパスワード
                      </label>
                      <input
                        type="password"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        新しいパスワード（確認）
                      </label>
                      <input
                        type="password"
                        className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>
                    <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                      パスワードを変更
                    </button>
                  </div>
                </div>

                <div className="pt-6 border-t border-gray-200">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">二段階認証</h3>
                  <div className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium text-gray-900">SMS認証</p>
                      <p className="text-sm text-gray-500">ログイン時にSMS認証を要求する</p>
                    </div>
                    <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50">
                      設定
                    </button>
                  </div>
                </div>

                <div className="pt-6 border-t border-gray-200">
                  <h3 className="text-lg font-medium text-gray-900 mb-4">アカウント削除</h3>
                  <p className="text-sm text-gray-500 mb-4">
                    アカウントを削除すると、すべてのデータが永久に削除されます。この操作は取り消せません。
                  </p>
                  <button className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700">
                    アカウントを削除
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
