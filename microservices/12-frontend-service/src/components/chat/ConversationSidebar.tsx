'use client';

import React from 'react';
import { ChatConversation } from '@/types/ai-chat';
import { 
  PlusIcon, 
  TrashIcon, 
  ChatBubbleLeftIcon 
} from '@heroicons/react/24/outline';

interface ConversationSidebarProps {
  conversations: ChatConversation[];
  currentConversationId: string | null;
  onSelectConversation: (id: string) => void;
  onCreateConversation: () => void;
  onDeleteConversation: (id: string) => void;
}

export default function ConversationSidebar({
  conversations,
  currentConversationId,
  onSelectConversation,
  onCreateConversation,
  onDeleteConversation,
}: Readonly<ConversationSidebarProps>) {
  const formatRelativeTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));
    
    if (diffInHours < 1) {
      return 'たった今';
    } else if (diffInHours < 24) {
      return `${diffInHours}時間前`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays}日前`;
    }
  };



  return (
    <div className="w-80 bg-gray-50 border-r border-gray-200 flex flex-col h-full">
      {/* ヘッダー */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-semibold text-gray-900">AI チャット</h2>
          <button
            onClick={onCreateConversation}
            className="p-2 text-gray-500 hover:text-gray-700 hover:bg-gray-200 rounded-lg transition-colors"
            title="新しい会話を開始"
          >
            <PlusIcon className="w-5 h-5" />
          </button>
        </div>
      </div>

      {/* 会話リスト */}
      <div className="flex-1 overflow-y-auto">
        {conversations.length === 0 ? (
          <div className="p-4 text-center text-gray-500">
            <ChatBubbleLeftIcon className="w-12 h-12 mx-auto mb-2 text-gray-300" />
            <p className="text-sm">まだ会話がありません</p>
            <p className="text-xs">新しい会話を開始してください</p>
          </div>
        ) : (
          <ul className="space-y-1 p-2">
            {conversations.map((conversation) => (
              <li
                key={conversation.id}
                className="relative group"
              >
                                <button
                  className={`w-full p-3 rounded-lg transition-colors text-left ${
                    currentConversationId === conversation.id
                      ? 'bg-blue-100 border border-blue-200'
                      : 'hover:bg-gray-100'
                  }`}
                  onClick={() => onSelectConversation(conversation.id)}
                  aria-label={`会話を選択: ${conversation.title}`}
                  suppressHydrationWarning
                >
                  <div className="flex items-center justify-between">
                    <div className="flex-1 min-w-0">
                      <h3 className="text-sm font-medium text-gray-900 truncate">
                        {conversation.title}
                      </h3>
                      <p className="text-xs text-gray-500 mt-1">
                        {formatRelativeTime(conversation.updatedAt)}
                      </p>
                    </div>
                  </div>
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    onDeleteConversation(conversation.id);
                  }}
                  className="absolute top-3 right-3 opacity-0 group-hover:opacity-100 p-1 text-gray-400 hover:text-red-500 transition-all"
                  title="会話を削除"
                  suppressHydrationWarning
                >
                  <TrashIcon className="w-4 h-4" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* フッター */}
      <div className="p-4 border-t border-gray-200">
        <p className="text-xs text-gray-500 text-center">
          AIサポートサービス
        </p>
      </div>
    </div>
  );
}
