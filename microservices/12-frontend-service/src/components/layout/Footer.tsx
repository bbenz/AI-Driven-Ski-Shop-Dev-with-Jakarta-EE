"use client";

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import {
  MapPinIcon,
  PhoneIcon,
  EnvelopeIcon,
} from '@heroicons/react/24/outline';

interface Category {
  id: string;
  name: string;
  path: string;
  level: number;
  slug: string;
}

interface ApiCategory {
  id: string;
  name: string;
  path: string;
  level: number;
  description: string;
  sortOrder: number;
  imageUrl: string | null;
  productCount: number;
  active: boolean;
}

export interface FooterProps {
  className?: string;
}

export const Footer: React.FC<FooterProps> = ({ className = '' }) => {
  const currentYear = new Date().getFullYear();
  const [mainCategories, setMainCategories] = useState<Category[]>([]);

  // メインカテゴリを取得
  useEffect(() => {
    const fetchMainCategories = async () => {
      try {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8083'}/api/v1/categories`
        );
        
        if (response.ok) {
          const categories: ApiCategory[] = await response.json();
          // レベル0（メインカテゴリ）のみをフィルタリング
          const mainCats = categories
            .filter((cat: ApiCategory) => cat.level === 0)
            .map((cat: ApiCategory) => ({
              ...cat,
              slug: cat.path.replace(/^\//, '').replace(/\//g, '-')
            }))
            .slice(0, 6); // 最大6個まで表示
          
          setMainCategories(mainCats);
        }
      } catch (error) {
        console.error('カテゴリの取得に失敗しました:', error);
        // エラー時はデフォルトカテゴリを表示
        setMainCategories([]);
      }
    };

    fetchMainCategories();
  }, []);

  const navigation = {
    shop: [
      { name: '全商品', href: '/products' },
      ...mainCategories.map(category => ({
        name: category.name,
        href: `/products?categoryId=${category.id}&includeSubcategories=true`
      }))
    ],
    support: [
      { name: 'お問い合わせ', href: '/contact' },
      { name: 'よくある質問', href: '/faq' },
      { name: '配送について', href: '/shipping' },
      { name: '返品・交換', href: '/returns' },
      { name: 'サイズガイド', href: '/size-guide' },
    ],
    company: [
      { name: '会社概要', href: '/about' },
      { name: 'プライバシーポリシー', href: '/privacy' },
      { name: '利用規約', href: '/terms' },
      { name: '特定商取引法', href: '/legal' },
    ],
    social: [
      { name: 'Facebook', href: '#', icon: '📘' },
      { name: 'Twitter', href: '#', icon: '🐦' },
      { name: 'Instagram', href: '#', icon: '📷' },
      { name: 'YouTube', href: '#', icon: '📺' },
    ],
  };

  return (
    <footer className={`bg-gray-900 text-white ${className}`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {/* Company Info */}
          <div className="space-y-4">
            <div className="flex items-center space-x-2">
              <div className="h-8 w-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">SK</span>
              </div>
              <span className="text-xl font-bold">Ski Resort Shop</span>
            </div>
            <p className="text-gray-400 text-sm">
              最高品質のスキー・スノーボード用品を提供する専門店。
              安全で楽しいウィンタースポーツをサポートします。
            </p>
            <div className="space-y-2 text-sm text-gray-400">
              <div className="flex items-center space-x-2">
                <MapPinIcon className="h-4 w-4" />
                <span>〒100-0001 東京都千代田区千代田1-1-1</span>
              </div>
              <div className="flex items-center space-x-2">
                <PhoneIcon className="h-4 w-4" />
                <span>03-1234-5678</span>
              </div>
              <div className="flex items-center space-x-2">
                <EnvelopeIcon className="h-4 w-4" />
                <span>info@skiresortshop.com</span>
              </div>
            </div>
          </div>

          {/* Shop Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">商品カテゴリ</h3>
            <ul className="space-y-2">
              {navigation.shop.map((item) => (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className="text-gray-400 hover:text-white text-sm transition-colors"
                  >
                    {item.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Support Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">サポート</h3>
            <ul className="space-y-2">
              {navigation.support.map((item) => (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className="text-gray-400 hover:text-white text-sm transition-colors"
                  >
                    {item.name}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Company Links */}
          <div>
            <h3 className="text-lg font-semibold mb-4">会社情報</h3>
            <ul className="space-y-2 mb-6">
              {navigation.company.map((item) => (
                <li key={item.name}>
                  <Link
                    href={item.href}
                    className="text-gray-400 hover:text-white text-sm transition-colors"
                  >
                    {item.name}
                  </Link>
                </li>
              ))}
            </ul>

            {/* Social Links */}
            <div>
              <h4 className="font-medium mb-3">SNSフォロー</h4>
              <div className="flex space-x-3">
                {navigation.social.map((item) => (
                  <a
                    key={item.name}
                    href={item.href}
                    className="text-gray-400 hover:text-white transition-colors"
                    aria-label={item.name}
                  >
                    <span className="text-xl">{item.icon}</span>
                  </a>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Newsletter Subscription */}
        <div className="border-t border-gray-800 mt-8 pt-8">
          <div className="max-w-md">
            <h3 className="text-lg font-semibold mb-2">ニュースレター購読</h3>
            <p className="text-gray-400 text-sm mb-4">
              新商品情報やセール情報をいち早くお届けします。
            </p>
            <form className="flex space-x-2">
              <input
                id="newsletter-email"
                type="email"
                placeholder="メールアドレス"
                className="flex-1 px-3 py-2 bg-gray-800 border border-gray-700 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500"
                suppressHydrationWarning
              />
              <button
                id="newsletter-submit"
                type="submit"
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-md text-white font-medium transition-colors"
                suppressHydrationWarning
              >
                購読
              </button>
            </form>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="border-t border-gray-800 mt-8 pt-8 flex flex-col sm:flex-row justify-between items-center">
          <p className="text-gray-400 text-sm">
            © {currentYear} Ski Resort Shop. All rights reserved.
          </p>
          <div className="flex space-x-6 mt-4 sm:mt-0">
            <Link
              href="/privacy"
              className="text-gray-400 hover:text-white text-sm transition-colors"
            >
              プライバシーポリシー
            </Link>
            <Link
              href="/terms"
              className="text-gray-400 hover:text-white text-sm transition-colors"
            >
              利用規約
            </Link>
            <Link
              href="/sitemap"
              className="text-gray-400 hover:text-white text-sm transition-colors"
            >
              サイトマップ
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
};
