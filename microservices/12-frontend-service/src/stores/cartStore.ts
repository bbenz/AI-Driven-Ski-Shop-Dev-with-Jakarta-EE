import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { shoppingCartApi } from '../services/api/shopping-cart';
import { productCatalogApi } from '../services/api/product-catalog';
import { CartResponse, CartItemResponse, AddToCartRequest } from '../types/cart';
import { CartWebSocketClient } from '../services/websocket/CartWebSocketClient';

// セッションIDを生成する関数
const generateSessionId = (): string => {
  return `session-${Date.now()}-${Math.random().toString(36).substring(2, 11)}`;
};

interface CartState {
  // カート状態
  cart: CartResponse | null;
  sessionId: string;
  customerId: string | null;
  loading: boolean;
  error: string | null;
  
  // WebSocket関連
  wsClient: CartWebSocketClient | null;
  wsConnected: boolean;
  
  // アクション
  initializeCart: () => Promise<void>;
  addItem: (productId: string, sku: string, quantity: number) => Promise<void>;
  removeItem: (sku: string) => Promise<void>;
  updateQuantity: (sku: string, quantity: number) => Promise<void>;
  clearCart: () => Promise<void>;
  validateCart: () => Promise<boolean>;
  mergeGuestCart: (customerId: string) => Promise<void>;
  
  // WebSocket管理
  connectWebSocket: () => void;
  disconnectWebSocket: () => void;
  updateCartFromWebSocket: (cart: CartResponse) => void;
  
  // ゲッター
  getItemCount: () => number;
  getSubtotal: () => number;
  getTax: () => number;
  getTotal: () => number;
  getCartItem: (sku: string) => CartItemResponse | undefined;
  
  // ユーザー管理
  setCustomerId: (customerId: string | null) => void;
  
  // エラー管理
  clearError: () => void;
}

export const useCartStore = create<CartState>()(
  persist(
    (set, get) => ({
      // 初期状態
      cart: null,
      sessionId: generateSessionId(),
      customerId: null,
      loading: false,
      error: null,
      
      // WebSocket関連の初期状態
      wsClient: null,
      wsConnected: false,

      // カート初期化
      initializeCart: async () => {
        const { sessionId, customerId } = get();
        console.log('Initializing cart with sessionId:', sessionId, 'customerId:', customerId);
        set({ loading: true, error: null });

        try {
          let cart: CartResponse;
          
          if (customerId) {
            // ログインユーザーの場合は顧客ベースでカート取得
            console.log('Getting cart by customer:', customerId);
            cart = await shoppingCartApi.getOrCreateCartByCustomer(customerId);
          } else {
            // ゲストユーザーの場合はセッションベースでカート取得
            console.log('Getting cart by session:', sessionId);
            cart = await shoppingCartApi.getOrCreateCartBySession(sessionId);
          }
          
          console.log('Cart initialized:', cart);
          set({ cart, loading: false });
          
          // WebSocket接続を開始
          setTimeout(() => {
            get().connectWebSocket();
          }, 100); // 少し遅延させてstateの更新を確実にする
        } catch (error) {
          console.error('Failed to initialize cart:', error);
          set({ 
            error: error instanceof Error ? error.message : 'カートの初期化に失敗しました',
            loading: false 
          });
        }
      },

      // カートに商品追加
      addItem: async (productId: string, sku: string, quantity: number) => {
        console.log('CartStore.addItem called:', { productId, sku, quantity });
        const { cart } = get();
        if (!cart) {
          console.log('No cart found, initializing...');
          await get().initializeCart();
          return get().addItem(productId, sku, quantity);
        }

        console.log('Adding item to cart:', cart.cartId);
        set({ loading: true, error: null });

        try {
          // 商品詳細を取得
          console.log('Fetching product details for:', productId);
          const product = await productCatalogApi.getProductById(productId);
          console.log('Product details:', product);
          
          const addRequest: AddToCartRequest = {
            productId,
            sku,
            productName: product.name,
            productImageUrl: product.imageUrls?.[0] || undefined,
            unitPrice: product.currentPrice,
            quantity,
            options: {} // 必要に応じてオプションを追加
          };

          console.log('Adding item to cart via API:', addRequest);
          const updatedCart = await shoppingCartApi.addItemToCart(cart.cartId, addRequest);
          console.log('Cart updated:', updatedCart);
          set({ cart: updatedCart, loading: false });
          
          // 状態変更を確実にするため、getItemCountをログ出力
          const newItemCount = get().getItemCount();
          console.log('New item count after cart update:', newItemCount);
          
          // WebSocket接続を確認（まだ接続されていない場合は接続）
          const { wsConnected } = get();
          if (!wsConnected) {
            console.log('WebSocket not connected, attempting to connect...');
            get().connectWebSocket();
          }
        } catch (error) {
          console.error('Failed to add item to cart:', error);
          set({ 
            error: error instanceof Error ? error.message : '商品の追加に失敗しました',
            loading: false 
          });
        }
      },

      // カートから商品削除
      removeItem: async (sku: string) => {
        const { cart } = get();
        if (!cart) return;

        set({ loading: true, error: null });

        try {
          const updatedCart = await shoppingCartApi.removeItemFromCart(cart.cartId, sku);
          set({ cart: updatedCart, loading: false });
        } catch (error) {
          console.error('Failed to remove item from cart:', error);
          set({ 
            error: error instanceof Error ? error.message : '商品の削除に失敗しました',
            loading: false 
          });
        }
      },

      // 数量更新
      updateQuantity: async (sku: string, quantity: number) => {
        const { cart } = get();
        if (!cart) return;

        set({ loading: true, error: null });

        try {
          if (quantity <= 0) {
            // 数量が0以下の場合は商品を削除
            await get().removeItem(sku);
          } else {
            const updatedCart = await shoppingCartApi.updateItemQuantity(cart.cartId, sku, { quantity });
            set({ cart: updatedCart, loading: false });
          }
        } catch (error) {
          console.error('Failed to update item quantity:', error);
          set({ 
            error: error instanceof Error ? error.message : '数量の更新に失敗しました',
            loading: false 
          });
        }
      },

      // カートクリア
      clearCart: async () => {
        const { cart, customerId } = get();
        if (!cart) return;

        set({ loading: true, error: null });

        try {
          console.log('Clearing cart:', cart.cartId);
          await shoppingCartApi.clearCart(cart.cartId);
          console.log('Cart cleared successfully, creating new cart with fresh session...');
          
          // WebSocket接続を切断
          get().disconnectWebSocket();
          
          // カートクリア後、完全に新しいカートを作成
          let newCart: CartResponse;
          try {
            if (customerId) {
              // カスタマーの場合は既存のカスタマーIDを使用
              newCart = await shoppingCartApi.getOrCreateCartByCustomer(customerId);
            } else {
              // ゲストユーザーの場合は新しいセッションIDを生成
              const newSessionId = generateSessionId();
              console.log('Generated new session ID:', newSessionId);
              newCart = await shoppingCartApi.getOrCreateCartBySession(newSessionId);
              // 新しいセッションIDを保存
              set({ sessionId: newSessionId });
            }
            console.log('New clean cart created:', newCart.cartId, 'items:', newCart.items.length);
          } catch (newCartError) {
            console.warn('Failed to create new cart, setting empty cart state:', newCartError);
            // 新しいカート作成に失敗した場合は、カート状態をnullにして再初期化を促す
            const newSessionId = generateSessionId();
            set({ cart: null, sessionId: newSessionId, loading: false });
            return;
          }
          
          set({ cart: newCart, loading: false });
          
          // 新しいカートでWebSocket接続を再開
          setTimeout(() => {
            get().connectWebSocket();
          }, 100);
        } catch (error) {
          console.error('Failed to clear cart:', error);
          set({ 
            error: error instanceof Error ? error.message : 'カートのクリアに失敗しました',
            loading: false 
          });
        }
      },

      // カート検証
      validateCart: async (): Promise<boolean> => {
        const { cart } = get();
        if (!cart) return false;

        try {
          const validation = await shoppingCartApi.validateCart(cart.cartId);
          if (!validation.valid) {
            set({ error: validation.errors.join(', ') });
          }
          return validation.valid;
        } catch (error) {
          console.error('Failed to validate cart:', error);
          set({ 
            error: error instanceof Error ? error.message : 'カートの検証に失敗しました'
          });
          return false;
        }
      },

      // ゲストカートマージ
      mergeGuestCart: async (customerId: string) => {
        const { cart } = get();
        if (!cart || cart.customerId) return; // 既に顧客カートの場合はスキップ

        set({ loading: true, error: null });

        try {
          const mergedCart = await shoppingCartApi.mergeGuestCart(cart.cartId, customerId);
          set({ 
            cart: mergedCart, 
            customerId,
            loading: false 
          });
        } catch (error) {
          console.error('Failed to merge guest cart:', error);
          set({ 
            error: error instanceof Error ? error.message : 'カートのマージに失敗しました',
            loading: false 
          });
        }
      },

      // ゲッター関数
      getItemCount: () => {
        const { cart } = get();
        if (!cart?.items) {
          console.log('getItemCount: No cart or items');
          return 0;
        }
        const count = cart.items.reduce((total, item) => total + item.quantity, 0);
        console.log('getItemCount: cart.items.length =', cart.items.length, 'total quantity =', count);
        return count;
      },

      getSubtotal: () => {
        const { cart } = get();
        return cart?.totals.subtotalAmount || 0;
      },

      getTax: () => {
        const { cart } = get();
        return cart?.totals.taxAmount || 0;
      },

      getTotal: () => {
        const { cart } = get();
        return cart?.totals.totalAmount || 0;
      },

      getCartItem: (sku: string) => {
        const { cart } = get();
        return cart?.items.find(item => item.sku === sku);
      },

      // ユーザー管理
      setCustomerId: (customerId: string | null) => {
        set({ customerId });
        // 顧客IDが設定された場合はカートを再初期化
        if (customerId) {
          get().initializeCart();
        }
      },

      // WebSocket管理
      connectWebSocket: () => {
        const { cart, wsClient } = get();
        if (!cart || wsClient) return; // カートがないか、既に接続済みの場合は何もしない

        console.log('Connecting WebSocket for cart:', cart.cartId);
        const client = new CartWebSocketClient({
          url: 'ws://localhost:8088/api/v1/carts/ws',
          cartId: cart.cartId,
          onCartUpdate: (updatedCart: CartResponse) => {
            get().updateCartFromWebSocket(updatedCart);
          },
          onError: (error: string) => {
            console.error('WebSocket error:', error);
            set({ error, wsConnected: false });
          },
          onConnect: () => {
            console.log('WebSocket connected');
            set({ wsConnected: true });
          },
          onDisconnect: () => {
            console.log('WebSocket disconnected');
            set({ wsConnected: false });
          },
        });

        client.connect();
        set({ wsClient: client });
      },

      disconnectWebSocket: () => {
        const { wsClient } = get();
        if (wsClient) {
          wsClient.disconnect();
          set({ wsClient: null, wsConnected: false });
        }
      },

      updateCartFromWebSocket: (cart: CartResponse) => {
        console.log('Updating cart from WebSocket:', cart);
        set({ cart });
      },

      // エラー管理
      clearError: () => {
        set({ error: null });
      },
    }),
    {
      name: 'cart-storage',
      version: 2,
      // 永続化するフィールドを指定
      partialize: (state) => ({
        cart: state.cart,
        sessionId: state.sessionId,
        customerId: state.customerId,
      }),
    }
  )
);
