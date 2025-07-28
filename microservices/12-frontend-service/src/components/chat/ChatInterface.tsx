'use client';

import React, { useEffect, useRef } from 'react';
import { useChatStore } from '@/stores/chatStore';
import { ChatMode } from '@/types/ai-chat';
import MessageBubble from './MessageBubble';
import ChatInput from './ChatInput';
import ConversationSidebar from './ConversationSidebar';
import { ExclamationTriangleIcon } from '@heroicons/react/24/outline';

export default function ChatInterface() {
  const {
    conversations,
    currentConversationId,
    isLoading,
    error,
    createConversation,
    setCurrentConversation,
    sendMessage,
    clearError,
    deleteConversation,
  } = useChatStore();

  const messagesEndRef = useRef<HTMLDivElement>(null);

  const currentConversation = conversations.find(conv => conv.id === currentConversationId);

  // 初回マウント時に会話を作成
  useEffect(() => {
    if (conversations.length === 0) {
      createConversation('新しい会話');
    }
  }, [conversations.length, createConversation]);

  // メッセージが追加されたら自動スクロール
  useEffect(() => {
    if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: 'smooth' });
    }
  }, [currentConversation?.messages]);

  const handleSendMessage = async (content: string, mode: ChatMode) => {
    if (!currentConversationId) return;
    await sendMessage(content, mode);
  };

  const handleCreateConversation = () => {
    createConversation();
  };

  const handleSelectConversation = (id: string) => {
    setCurrentConversation(id);
  };

  const handleDeleteConversation = (id: string) => {
    deleteConversation(id);
  };

  return (
    <div className="flex h-full bg-white">
      {/* サイドバー */}
      <ConversationSidebar
        conversations={conversations}
        currentConversationId={currentConversationId}
        onSelectConversation={handleSelectConversation}
        onCreateConversation={handleCreateConversation}
        onDeleteConversation={handleDeleteConversation}
      />

      {/* メインチャットエリア */}
      <div className="flex-1 flex flex-col">
        {/* ヘッダー */}
        <div className="border-b border-gray-200 p-4">
          <h1 className="text-xl font-semibold text-gray-900">
            {currentConversation?.title || 'AIサポートチャット'}
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            スキーに関するご質問やアドバイスをお気軽にどうぞ
          </p>
        </div>

        {/* エラー表示 */}
        {error && (
          <div className="mx-4 mt-4 p-3 bg-red-50 border border-red-200 rounded-lg flex items-center gap-2">
            <ExclamationTriangleIcon className="w-5 h-5 text-red-500" />
            <span className="text-sm text-red-700">{error}</span>
            <button
              onClick={clearError}
              className="ml-auto text-red-500 hover:text-red-700"
            >
              ✕
            </button>
          </div>
        )}

        {/* メッセージエリア */}
        <div className="flex-1 overflow-y-auto p-4">
          {!currentConversation || currentConversation.messages.length === 0 ? (
            <div className="flex items-center justify-center h-full">
              <div className="text-center">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">🎿</span>
                </div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">
                  AIサポートへようこそ！
                </h3>
                <p className="text-gray-500 max-w-md">
                  スキー用品の推薦、技術アドバイス、一般的な質問など、何でもお気軽にお聞きください。
                </p>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              {currentConversation.messages.map((message) => (
                <MessageBubble key={message.messageId} message={message} />
              ))}
              {isLoading && (
                <div className="flex justify-start">
                  <div className="bg-gray-100 rounded-lg px-4 py-2">
                    <div className="flex items-center space-x-2">
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:0.1s]"></div>
                      <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce [animation-delay:0.2s]"></div>
                    </div>
                  </div>
                </div>
              )}
              <div ref={messagesEndRef} />
            </div>
          )}
        </div>

        {/* 入力エリア */}
        <ChatInput
          onSendMessage={handleSendMessage}
          isLoading={isLoading}
          disabled={!currentConversationId}
        />
      </div>
    </div>
  );
}
