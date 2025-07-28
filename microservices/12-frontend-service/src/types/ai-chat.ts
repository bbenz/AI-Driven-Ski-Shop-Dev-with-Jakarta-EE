export interface ChatMessage {
  messageId: string;
  conversationId: string;
  content: string;
  intent: 'PRODUCT_RECOMMENDATION' | 'TECHNICAL_ADVICE' | 'GENERAL_INQUIRY' | 'SUPPORT_REQUEST';
  confidence: number;
  timestamp: string;
  context?: Record<string, unknown>;
  isUser?: boolean;
}

export interface ChatRequest {
  userId: string;
  content: string;
  conversationId: string;
}

export interface ChatResponse {
  messageId: string;
  conversationId: string;
  content: string;
  intent: string;
  confidence: number;
  timestamp: string;
  context?: Record<string, unknown>;
}

export interface ChatConversation {
  id: string;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
  title?: string;
}

export type ChatMode = 'recommend' | 'advice' | 'message';

export interface ChatStore {
  conversations: ChatConversation[];
  currentConversationId: string | null;
  isLoading: boolean;
  error: string | null;
  createConversation: (title?: string) => string;
  addMessage: (message: ChatMessage) => void;
  setCurrentConversation: (id: string) => void;
  sendMessage: (content: string, mode: ChatMode) => Promise<void>;
  clearError: () => void;
  deleteConversation: (id: string) => void;
}
