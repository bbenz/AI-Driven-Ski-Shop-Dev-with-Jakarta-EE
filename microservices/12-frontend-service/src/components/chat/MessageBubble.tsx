'use client';

import React from 'react';
import { ChatMessage } from '@/types/ai-chat';
import { UserIcon, SparklesIcon } from '@heroicons/react/24/outline';

interface MessageBubbleProps {
  message: ChatMessage;
}

export default function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.isUser;
  const formattedTime = new Date(message.timestamp).toLocaleTimeString('ja-JP', {
    hour: '2-digit',
    minute: '2-digit',
  });

  const formatContent = (content: string) => {
    // Markdownライクなフォーマットを簡単なHTMLに変換
    const formatted = content
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/^### (.*$)/gm, '<h3 class="text-lg font-semibold mt-4 mb-2">$1</h3>')
      .replace(/^## (.*$)/gm, '<h2 class="text-xl font-semibold mt-4 mb-2">$1</h2>')
      .replace(/^# (.*$)/gm, '<h1 class="text-2xl font-bold mt-4 mb-2">$1</h1>')
      .replace(/^- (.*$)/gm, '<li class="ml-4">$1</li>')
      .replace(/\n\n/g, '</p><p class="mb-2">')
      .replace(/^(?!<[h|l])/gm, '<p class="mb-2">')
      .replace(/([^>])\n/g, '$1<br>');

    return `<div>${formatted}</div>`;
  };

  return (
    <div className={`flex gap-3 ${isUser ? 'justify-end' : 'justify-start'} mb-4`}>
      {!isUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
            <SparklesIcon className="w-5 h-5 text-white" />
          </div>
        </div>
      )}
      
      <div className={`max-w-xs lg:max-w-md xl:max-w-lg ${isUser ? 'order-1' : ''}`}>
        <div
          className={`px-4 py-2 rounded-lg ${
            isUser
              ? 'bg-blue-500 text-white'
              : 'bg-gray-100 text-gray-900 border'
          }`}
        >
          {isUser ? (
            <p className="text-sm">{message.content}</p>
          ) : (
            <div 
              className="text-sm prose prose-sm max-w-none"
              dangerouslySetInnerHTML={{ __html: formatContent(message.content) }}
            />
          )}
        </div>
        
        <div className={`text-xs text-gray-500 mt-1 ${isUser ? 'text-right' : 'text-left'}`}>
          {formattedTime}
          {!isUser && message.intent && (
            <span className="ml-2 px-2 py-1 bg-gray-200 rounded text-xs">
              {getIntentLabel(message.intent)}
            </span>
          )}
        </div>
      </div>

      {isUser && (
        <div className="flex-shrink-0">
          <div className="w-8 h-8 bg-gray-400 rounded-full flex items-center justify-center">
            <UserIcon className="w-5 h-5 text-white" />
          </div>
        </div>
      )}
    </div>
  );
}

function getIntentLabel(intent: ChatMessage['intent']): string {
  switch (intent) {
    case 'PRODUCT_RECOMMENDATION':
      return '商品推薦';
    case 'TECHNICAL_ADVICE':
      return '技術アドバイス';
    case 'GENERAL_INQUIRY':
      return '一般問合せ';
    case 'SUPPORT_REQUEST':
      return 'サポート要求';
    default:
      return '一般';
  }
}
