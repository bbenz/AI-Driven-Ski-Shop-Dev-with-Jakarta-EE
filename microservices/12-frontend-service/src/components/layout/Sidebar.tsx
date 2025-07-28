import React from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  XMarkIcon,
  HomeIcon,
  ShoppingBagIcon,
  TagIcon,
  HeartIcon,
  UserIcon,
  ShoppingCartIcon,
  ClipboardDocumentListIcon,
} from '@heroicons/react/24/outline';
import { cn } from '@/utils/helpers';
import { useAuthStore } from '@/stores/authStore';

export interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
  className?: string;
}

export const Sidebar: React.FC<SidebarProps> = ({
  isOpen,
  onClose,
  className = '',
}) => {
  const pathname = usePathname();
  const { user, logout } = useAuthStore();

  const navigation = [
    { name: 'ホーム', href: '/', icon: HomeIcon },
    { name: '商品一覧', href: '/products', icon: ShoppingBagIcon },
    { name: 'カテゴリ', href: '/categories', icon: TagIcon },
    { name: 'お気に入り', href: '/favorites', icon: HeartIcon },
    { name: 'カート', href: '/cart', icon: ShoppingCartIcon },
  ];

  const userNavigation = user
    ? [
        { name: 'プロフィール', href: '/profile', icon: UserIcon },
        { name: '注文履歴', href: '/orders', icon: ClipboardDocumentListIcon },
      ]
    : [];

  const handleLinkClick = () => {
    onClose();
  };

  const handleLogout = () => {
    logout();
    onClose();
  };

  return (
    <>
      {/* Overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black bg-opacity-50 lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <div
        className={cn(
          'fixed top-0 left-0 z-50 h-full w-64 bg-white shadow-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:shadow-none',
          isOpen ? 'translate-x-0' : '-translate-x-full',
          className
        )}
      >
        {/* Header */}
        <div className="flex items-center justify-between p-4 border-b border-gray-200 lg:hidden">
          <div className="flex items-center space-x-2">
            <div className="h-8 w-8 bg-blue-600 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold text-sm">SK</span>
            </div>
            <span className="text-lg font-bold text-gray-900">Menu</span>
          </div>
          <button
            type="button"
            className="p-2 text-gray-400 hover:text-gray-600"
            onClick={onClose}
            aria-label="メニューを閉じる"
          >
            <XMarkIcon className="h-6 w-6" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-4 py-6 space-y-2">
          {/* Main Navigation */}
          <div className="space-y-1">
            {navigation.map((item) => {
              const isActive = pathname === item.href;
              return (
                <Link
                  key={item.name}
                  href={item.href}
                  className={cn(
                    'flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-blue-100 text-blue-700'
                      : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                  )}
                  onClick={handleLinkClick}
                >
                  <item.icon className="h-5 w-5" />
                  <span>{item.name}</span>
                </Link>
              );
            })}
          </div>

          {/* User Navigation */}
          {user && (
            <div className="pt-6 border-t border-gray-200">
              <h3 className="px-3 text-xs font-medium text-gray-500 uppercase tracking-wider mb-2">
                マイアカウント
              </h3>
              <div className="space-y-1">
                {userNavigation.map((item) => {
                  const isActive = pathname === item.href;
                  return (
                    <Link
                      key={item.name}
                      href={item.href}
                      className={cn(
                        'flex items-center space-x-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                        isActive
                          ? 'bg-blue-100 text-blue-700'
                          : 'text-gray-700 hover:bg-gray-100 hover:text-gray-900'
                      )}
                      onClick={handleLinkClick}
                    >
                      <item.icon className="h-5 w-5" />
                      <span>{item.name}</span>
                    </Link>
                  );
                })}
              </div>
            </div>
          )}
        </nav>

        {/* User Section */}
        <div className="border-t border-gray-200 p-4">
          {user ? (
            <div className="space-y-3">
              <div className="flex items-center space-x-3">
                <div className="h-8 w-8 bg-gray-300 rounded-full flex items-center justify-center">
                  <UserIcon className="h-5 w-5 text-gray-600" />
                </div>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {user.firstName || user.email}
                  </p>
                  <p className="text-xs text-gray-500 truncate">
                    {user.email}
                  </p>
                </div>
              </div>
              <button
                type="button"
                className="w-full px-3 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                onClick={handleLogout}
              >
                ログアウト
              </button>
            </div>
          ) : (
            <div className="space-y-2">
              <Link
                href="/login"
                className="block w-full px-3 py-2 text-sm font-medium text-center text-white bg-blue-600 rounded-lg hover:bg-blue-700 transition-colors"
                onClick={handleLinkClick}
              >
                ログイン
              </Link>
              <Link
                href="/register"
                className="block w-full px-3 py-2 text-sm font-medium text-center text-blue-600 bg-blue-100 rounded-lg hover:bg-blue-200 transition-colors"
                onClick={handleLinkClick}
              >
                新規登録
              </Link>
            </div>
          )}
        </div>
      </div>
    </>
  );
};
