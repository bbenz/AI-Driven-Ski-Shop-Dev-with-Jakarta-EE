import { create } from 'zustand';
import { ChatStore, ChatMessage, ChatConversation, ChatMode } from '@/types/ai-chat';
import { aiSupportApiService } from '@/services/api/aiSupportApi';

export const useChatStore = create<ChatStore>((set, get) => ({
  conversations: [],
  currentConversationId: null,
  isLoading: false,
  error: null,

  createConversation: (title?: string) => {
    const id = `conversation-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
    const newConversation: ChatConversation = {
      id,
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      title: title || `会話 ${get().conversations.length + 1}`,
    };

    set((state) => ({
      conversations: [newConversation, ...state.conversations],
      currentConversationId: id,
    }));

    return id;
  },

  addMessage: (message: ChatMessage) => {
    set((state) => ({
      conversations: state.conversations.map((conv) =>
        conv.id === message.conversationId
          ? {
              ...conv,
              messages: [...conv.messages, message],
              updatedAt: new Date().toISOString(),
            }
          : conv
      ),
    }));
  },

  setCurrentConversation: (id: string) => {
    set({ currentConversationId: id });
  },

  sendMessage: async (content: string, mode: ChatMode = 'message') => {
    const { currentConversationId, addMessage } = get();
    
    if (!currentConversationId) {
      set({ error: '会話が選択されていません。' });
      return;
    }

    set({ isLoading: true, error: null });

    try {
      // ユーザーメッセージを追加
      const userMessage: ChatMessage = {
        messageId: `user-${Date.now()}`,
        conversationId: currentConversationId,
        content,
        intent: 'GENERAL_INQUIRY',
        confidence: 1.0,
        timestamp: new Date().toISOString(),
        isUser: true,
      };
      addMessage(userMessage);

      // AIサポートサービスにメッセージを送信
      const response = await aiSupportApiService.sendMessage(
        {
          userId: 'user', // 実際のアプリケーションでは認証されたユーザーIDを使用
          content,
          conversationId: currentConversationId,
        },
        mode
      );

      // AIレスポンスを追加
      const aiMessage: ChatMessage = {
        messageId: response.messageId,
        conversationId: response.conversationId,
        content: response.content,
        intent: response.intent as ChatMessage['intent'],
        confidence: response.confidence,
        timestamp: response.timestamp,
        context: response.context,
        isUser: false,
      };
      addMessage(aiMessage);
    } catch (error) {
      set({ error: error instanceof Error ? error.message : 'エラーが発生しました。' });
    } finally {
      set({ isLoading: false });
    }
  },

  clearError: () => {
    set({ error: null });
  },

  deleteConversation: (id: string) => {
    set((state) => ({
      conversations: state.conversations.filter((conv) => conv.id !== id),
      currentConversationId: state.currentConversationId === id ? null : state.currentConversationId,
    }));
  },
}));
