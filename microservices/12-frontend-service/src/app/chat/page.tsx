import React from 'react';
import { MainLayout } from '@/components/layout/MainLayout';
import ChatInterface from '@/components/chat/ChatInterface';

export default function ChatPage() {
  return (
    <MainLayout>
      <div className="bg-gray-50 h-[calc(100vh-8rem)]">
        <div className="max-w-7xl mx-auto h-full">
          <ChatInterface />
        </div>
      </div>
    </MainLayout>
  );
}
