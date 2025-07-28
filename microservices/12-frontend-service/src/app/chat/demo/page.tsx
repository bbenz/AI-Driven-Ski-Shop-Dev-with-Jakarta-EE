import React from 'react';
import Link from 'next/link';
import ChatInterface from '@/components/chat/ChatInterface';
import { ArrowLeftIcon } from '@heroicons/react/24/outline';

export default function ChatDemo() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* ヘッダー */}
      <div className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center space-x-4">
              <Link
                href="/"
                className="flex items-center space-x-2 text-gray-600 hover:text-gray-900 transition-colors"
              >
                <ArrowLeftIcon className="w-5 h-5" />
                <span>ホームに戻る</span>
              </Link>
            </div>
            <h1 className="text-xl font-semibold text-gray-900">AIサポートチャット</h1>
            <div className="w-24"></div> {/* スペーサー */}
          </div>
        </div>
      </div>

      {/* チャットインターフェース */}
      <div className="h-[calc(100vh-4rem)]">
        <ChatInterface />
      </div>
    </div>
  );
}
