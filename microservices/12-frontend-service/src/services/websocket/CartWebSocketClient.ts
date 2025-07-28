/**
 * WebSocketを使用したリアルタイムカート同期サービス
 */

import { CartResponse } from '../../types/cart';

interface WebSocketMessage {
  type: 'connected' | 'cart_updated' | 'price_updated' | 'stock_updated' | 'error';
  data: Record<string, unknown>;
}

interface CartWebSocketConfig {
  url: string;
  cartId: string;
  onCartUpdate: (cart: CartResponse) => void;
  onError: (error: string) => void;
  onConnect: () => void;
  onDisconnect: () => void;
}

export class CartWebSocketClient {
  private ws: WebSocket | null = null;
  private readonly config: CartWebSocketConfig;
  private reconnectAttempts = 0;
  private readonly maxReconnectAttempts = 5;
  private readonly reconnectInterval = 1000;
  private heartbeatInterval: NodeJS.Timeout | null = null;

  constructor(config: CartWebSocketConfig) {
    this.config = config;
  }

  connect(): void {
    try {
      console.log('Connecting to WebSocket:', `${this.config.url}/${this.config.cartId}`);
      this.ws = new WebSocket(`${this.config.url}/${this.config.cartId}`);
      
      this.ws.onopen = this.handleOpen.bind(this);
      this.ws.onmessage = this.handleMessage.bind(this);
      this.ws.onclose = this.handleClose.bind(this);
      this.ws.onerror = this.handleError.bind(this);
    } catch (error) {
      console.error('Failed to create WebSocket connection:', error);
      this.config.onError('WebSocket接続の作成に失敗しました');
    }
  }

  private handleOpen(): void {
    console.log('WebSocket connected for cart:', this.config.cartId);
    this.reconnectAttempts = 0;
    this.config.onConnect();
    this.startHeartbeat();
  }

  private handleMessage(event: MessageEvent): void {
    try {
      const message: WebSocketMessage = JSON.parse(event.data);
      console.log('WebSocket message received:', message);

      switch (message.type) {
        case 'connected':
          console.log('WebSocket connection confirmed:', message.data);
          break;
          
        case 'cart_updated':
          console.log('Cart updated via WebSocket:', message.data.cart);
          if (message.data.cart) {
            this.config.onCartUpdate(message.data.cart as CartResponse);
          }
          break;
          
        case 'price_updated':
          console.log('Price updated:', message.data);
          // 価格更新時はカート全体を再取得
          this.requestCartRefresh();
          break;
          
        case 'stock_updated':
          console.log('Stock updated:', message.data);
          // 在庫更新時はカート全体を再取得
          this.requestCartRefresh();
          break;
          
        case 'error': {
          console.error('WebSocket error message:', message.data);
          const errorMessage = (message.data.message as string) || 'WebSocketエラーが発生しました';
          this.config.onError(errorMessage);
          break;
        }
          
        default:
          console.log('Unknown WebSocket message type:', message.type);
      }
    } catch (error) {
      console.error('Failed to parse WebSocket message:', error);
      this.config.onError('WebSocketメッセージの解析に失敗しました');
    }
  }

  private handleClose(event: CloseEvent): void {
    console.log('WebSocket closed:', event);
    this.stopHeartbeat();
    this.config.onDisconnect();
    
    // 自動再接続
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`WebSocket reconnection attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
      setTimeout(() => this.connect(), this.reconnectInterval * this.reconnectAttempts);
    } else {
      console.error('WebSocket max reconnection attempts exceeded');
      this.config.onError('WebSocket接続が失われ、再接続に失敗しました');
    }
  }

  private handleError(event: Event): void {
    console.error('WebSocket error:', event);
    this.config.onError('WebSocket接続エラーが発生しました');
  }

  private startHeartbeat(): void {
    this.heartbeatInterval = setInterval(() => {
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.send({ type: 'ping', data: { timestamp: Date.now() } });
      }
    }, 30000); // 30秒ごとにping
  }

  private stopHeartbeat(): void {
    if (this.heartbeatInterval) {
      clearInterval(this.heartbeatInterval);
      this.heartbeatInterval = null;
    }
  }

  private requestCartRefresh(): void {
    this.send({ type: 'refresh_cart', data: {} });
  }

  send(message: Record<string, unknown>): void {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      try {
        this.ws.send(JSON.stringify(message));
      } catch (error) {
        console.error('Failed to send WebSocket message:', error);
        this.config.onError('WebSocketメッセージの送信に失敗しました');
      }
    } else {
      console.warn('WebSocket is not connected, cannot send message:', message);
    }
  }

  disconnect(): void {
    this.stopHeartbeat();
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }

  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}
