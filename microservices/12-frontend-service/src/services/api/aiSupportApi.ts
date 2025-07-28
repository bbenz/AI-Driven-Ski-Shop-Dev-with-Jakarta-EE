import axios from 'axios';
import { ChatRequest, ChatResponse, ChatMode } from '@/types/ai-chat';

const AI_SUPPORT_BASE_URL = process.env.NEXT_PUBLIC_AI_SUPPORT_URL || 'http://localhost:8091';
const AI_SUPPORT_CHAT_URL = `${AI_SUPPORT_BASE_URL}/api/v1/chat`;

export class AiSupportApiService {
  private getEndpoint(mode: ChatMode): string {
    switch (mode) {
      case 'recommend':
        return `${AI_SUPPORT_CHAT_URL}/recommend`;
      case 'advice':
        return `${AI_SUPPORT_CHAT_URL}/advice`;
      case 'message':
        return `${AI_SUPPORT_CHAT_URL}/message`;
      default:
        return `${AI_SUPPORT_CHAT_URL}/message`;
    }
  }

  async sendMessage(request: ChatRequest, mode: ChatMode = 'message'): Promise<ChatResponse> {
    try {
      const endpoint = this.getEndpoint(mode);
      const response = await axios.post<ChatResponse>(endpoint, request, {
        headers: {
          'Content-Type': 'application/json',
        },
      });
      return response.data;
    } catch (error) {
      console.error('AI Support API Error:', error);
      throw new Error('AIサポートサービスとの通信でエラーが発生しました。');
    }
  }

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  async getConversationHistory(_conversationId: string): Promise<ChatResponse[]> {
    // AIサポートサービスに会話履歴取得APIがない場合は、
    // フロントエンド側で履歴を管理する
    return [];
  }
}

export const aiSupportApiService = new AiSupportApiService();
