'use client';

import React, { useState } from 'react';
import { PaperAirplaneIcon } from '@heroicons/react/24/outline';
import { ChatMode } from '@/types/ai-chat';

interface ChatInputProps {
  readonly onSendMessage: (message: string, mode: ChatMode) => void;
  readonly isLoading?: boolean;
  readonly disabled?: boolean;
}

export default function ChatInput({ onSendMessage, isLoading, disabled }: ChatInputProps) {
  const [input, setInput] = useState('');
  const [selectedMode, setSelectedMode] = useState<ChatMode>('message');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (input.trim() && !isLoading && !disabled) {
      onSendMessage(input.trim(), selectedMode);
      setInput('');
    }
  };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="border-t bg-white p-4">
      {/* モード選択 */}
      <div className="mb-3">
        <div className="flex gap-2">
          <button
            type="button"
            onClick={() => setSelectedMode('message')}
            className={`px-3 py-1 text-sm rounded-full transition-colors ${
              selectedMode === 'message'
                ? 'bg-blue-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            一般チャット
          </button>
          <button
            type="button"
            onClick={() => setSelectedMode('recommend')}
            className={`px-3 py-1 text-sm rounded-full transition-colors ${
              selectedMode === 'recommend'
                ? 'bg-green-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            商品推薦
          </button>
          <button
            type="button"
            onClick={() => setSelectedMode('advice')}
            className={`px-3 py-1 text-sm rounded-full transition-colors ${
              selectedMode === 'advice'
                ? 'bg-purple-500 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            技術アドバイス
          </button>
        </div>
      </div>

      {/* 入力フォーム */}
      <form onSubmit={handleSubmit} className="flex gap-2">
        <div className="flex-1">
          <textarea
            id="chat-message-input"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={getPlaceholderText(selectedMode)}
            disabled={disabled || isLoading}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent disabled:bg-gray-100"
            rows={2}
            suppressHydrationWarning
          />
        </div>
        <button
          id="chat-send-button"
          type="submit"
          disabled={!input.trim() || isLoading || disabled}
          className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors flex items-center justify-center"
          suppressHydrationWarning
        >
          {isLoading ? (
            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
          ) : (
            <PaperAirplaneIcon className="w-5 h-5" />
          )}
        </button>
      </form>

      {/* モードの説明 */}
      <p className="text-xs text-gray-500 mt-2">
        {getModeDescription(selectedMode)}
      </p>
    </div>
  );
}

function getPlaceholderText(mode: ChatMode): string {
  switch (mode) {
    case 'recommend':
      return '商品についてお聞かせください（例：初心者向けのスキー板を推薦してください）';
    case 'advice':
      return '技術的な質問をどうぞ（例：パラレルターンのコツを教えてください）';
    case 'message':
      return 'メッセージを入力してください...';
    default:
      return 'メッセージを入力してください...';
  }
}

function getModeDescription(mode: ChatMode): string {
  switch (mode) {
    case 'recommend':
      return '商品推薦モード：あなたのニーズに合った商品をAIが推薦します';
    case 'advice':
      return '技術アドバイスモード：スキー技術に関する専門的なアドバイスを提供します';
    case 'message':
      return '一般チャットモード：スキーに関する様々な質問にお答えします';
    default:
      return '';
  }
}
