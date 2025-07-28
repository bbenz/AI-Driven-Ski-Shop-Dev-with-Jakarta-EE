'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import {
  Bars3Icon,
  XMarkIcon,
  UserIcon,
  MagnifyingGlassIcon,
  ChatBubbleLeftRightIcon,
} from '@heroicons/react/24/outline';
import { Button } from '@/components/ui';
import { useAuth } from '@/hooks/useAuth';
import { MiniCart } from '@/components/cart/MiniCart';

export interface HeaderProps {
  className?: string;
}

export const Header: React.FC<HeaderProps> = ({ className = '' }) => {
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  
  const router = useRouter();
  const { user, logout } = useAuth();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      router.push(`/products?search=${encodeURIComponent(searchQuery.trim())}`);
      setSearchQuery('');
      setIsSearchOpen(false);
    }
  };

  const handleLogout = () => {
    logout();
    router.push('/');
  };

  const navigation = [
    { name: 'ホーム', href: '/' },
    { name: '商品', href: '/products' },
    { name: 'カテゴリ', href: '/categories' },
    { name: 'クーポン', href: '/coupons' },
    { name: 'お気に入り', href: '/favorites' },
    { name: 'AIサポート', href: '/chat' },
  ];

  return (
    <header className={`bg-white shadow-sm border-b border-gray-200 ${className}`}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16 gap-4">
          {/* Logo */}
          <div className="flex items-center flex-shrink-0">
            <Link href="/" className="flex items-center space-x-2">
              <div className="h-8 w-8 bg-blue-600 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">SK</span>
              </div>
              <span className="text-xl font-bold text-gray-900 hidden sm:block">
                Ski Resort Shop
              </span>
            </Link>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex space-x-4 lg:space-x-6">
            {navigation.map((item) => (
              <Link
                key={item.name}
                href={item.href}
                className="text-gray-700 hover:text-blue-600 px-2 lg:px-3 py-2 text-sm lg:text-base font-medium transition-colors whitespace-nowrap"
              >
                {item.name}
              </Link>
            ))}
          </nav>

          {/* Search Bar */}
          <div className="hidden lg:flex flex-1 max-w-sm mx-4 xl:max-w-md xl:mx-6">
            <form onSubmit={handleSearch} className="w-full">
              <div className="relative">
                <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input
                  id="search-desktop"
                  type="text"
                  placeholder="商品を検索..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  suppressHydrationWarning
                />
              </div>
            </form>
          </div>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            {/* Mobile Search Button */}
            <button
              type="button"
              className="lg:hidden p-2 text-gray-600 hover:text-gray-900"
              onClick={() => setIsSearchOpen(!isSearchOpen)}
              title="検索"
            >
              <MagnifyingGlassIcon className="h-6 w-6" />
            </button>

            {/* AI Chat */}
            <Link
              href="/chat"
              className="p-2 text-gray-600 hover:text-gray-900 transition-colors"
              title="AIサポートチャット"
            >
              <ChatBubbleLeftRightIcon className="h-6 w-6" />
            </Link>

            {/* Shopping Cart */}
            <MiniCart />

            {/* User Menu */}
            {user ? (
              <div className="flex items-center space-x-4">
                <div className="flex items-center space-x-2 text-gray-700">
                  <UserIcon className="h-6 w-6" />
                  <span className="hidden sm:block text-sm font-medium">
                    {user.firstName || user.email}
                  </span>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleLogout}
                  className="text-gray-600 hover:text-gray-900"
                >
                  ログアウト
                </Button>
              </div>
            ) : (
              <div className="flex space-x-2">
                <Link href="/login">
                  <Button variant="ghost" size="sm">
                    ログイン
                  </Button>
                </Link>
                <Link href="/register">
                  <Button size="sm">
                    新規登録
                  </Button>
                </Link>
              </div>
            )}

            {/* Mobile menu button */}
            <button
              type="button"
              className="md:hidden p-2 text-gray-600 hover:text-gray-900"
              onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
              title={isMobileMenuOpen ? 'メニューを閉じる' : 'メニューを開く'}
            >
              {isMobileMenuOpen ? (
                <XMarkIcon className="h-6 w-6" />
              ) : (
                <Bars3Icon className="h-6 w-6" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Search */}
        {isSearchOpen && (
          <div className="lg:hidden py-4 border-t border-gray-200">
            <form onSubmit={handleSearch}>
              <div className="relative">
                <MagnifyingGlassIcon className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
                <input
                  id="search-mobile"
                  type="text"
                  placeholder="商品を検索..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  autoFocus
                  suppressHydrationWarning
                />
              </div>
            </form>
          </div>
        )}

        {/* Mobile Navigation */}
        {isMobileMenuOpen && (
          <div className="md:hidden py-4 border-t border-gray-200">
            <nav className="space-y-2">
              {navigation.map((item) => (
                <Link
                  key={item.name}
                  href={item.href}
                  className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                  onClick={() => setIsMobileMenuOpen(false)}
                >
                  {item.name}
                </Link>
              ))}
              
              {user && (
                <div className="border-t border-gray-200 pt-4">
                  <Link
                    href="/profile"
                    className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    プロフィール
                  </Link>
                  <Link
                    href="/orders"
                    className="block px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                    onClick={() => setIsMobileMenuOpen(false)}
                  >
                    注文履歴
                  </Link>
                  <button
                    type="button"
                    className="block w-full text-left px-3 py-2 text-base font-medium text-gray-700 hover:text-blue-600 hover:bg-gray-50 rounded-md"
                    onClick={handleLogout}
                  >
                    ログアウト
                  </button>
                </div>
              )}
            </nav>
          </div>
        )}
      </div>
    </header>
  );
};
