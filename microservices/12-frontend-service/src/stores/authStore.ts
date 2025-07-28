import React from 'react';
import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import { authAPI } from '@/services/api/auth';
import { STORAGE_KEYS } from '@/utils/constants';
import type { User, LoginRequest, RegisterRequest } from '@/types/auth';

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (credentials: LoginRequest) => Promise<void>;
  register: (userData: RegisterRequest) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
  updateUser: (updates: Partial<User>) => void;
  clearError: () => void;
  initialize: () => Promise<void>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,

      // Actions
      login: async (credentials: LoginRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authAPI.login(credentials);
          
          // トークンをローカルストレージに保存
          if (typeof window !== 'undefined') {
            localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, response.token);
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
          }

          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'ログインに失敗しました';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      register: async (userData: RegisterRequest) => {
        set({ isLoading: true, error: null });
        
        try {
          const response = await authAPI.register(userData);
          
          // トークンをローカルストレージに保存
          if (typeof window !== 'undefined') {
            localStorage.setItem(STORAGE_KEYS.AUTH_TOKEN, response.token);
            localStorage.setItem(STORAGE_KEYS.REFRESH_TOKEN, response.refreshToken);
          }

          set({
            user: response.user,
            token: response.token,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : '登録に失敗しました';
          set({
            user: null,
            token: null,
            isAuthenticated: false,
            isLoading: false,
            error: errorMessage,
          });
          throw error;
        }
      },

      logout: () => {
        // APIにログアウトリクエストを送信（バックグラウンドで実行）
        authAPI.logout().catch(() => {
          // エラーが発生してもクライアント側のログアウトは続行
        });

        // ローカルストレージをクリア
        if (typeof window !== 'undefined') {
          localStorage.removeItem(STORAGE_KEYS.AUTH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.REFRESH_TOKEN);
          localStorage.removeItem(STORAGE_KEYS.USER_PREFERENCES);
        }

        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        });
      },

      refreshUser: async () => {
        if (!get().isAuthenticated) return;

        set({ isLoading: true });
        
        try {
          const user = await authAPI.getCurrentUser();
          set({
            user,
            isLoading: false,
            error: null,
          });
        } catch (error) {
          const errorMessage = error instanceof Error ? error.message : 'ユーザー情報の取得に失敗しました';
          set({
            isLoading: false,
            error: errorMessage,
          });
          
          // 認証エラーの場合はログアウト
          if (error instanceof Error && error.message.includes('401')) {
            get().logout();
          }
        }
      },

      updateUser: (updates: Partial<User>) => {
        const currentUser = get().user;
        if (currentUser) {
          set({
            user: { ...currentUser, ...updates },
          });
        }
      },

      clearError: () => {
        set({ error: null });
      },

      initialize: async () => {
        if (typeof window === 'undefined') return;

        const token = localStorage.getItem(STORAGE_KEYS.AUTH_TOKEN);
        
        if (token) {
          set({ token, isAuthenticated: true });
          
          try {
            await get().refreshUser();
          } catch {
            // ユーザー情報の取得に失敗した場合はログアウト
            get().logout();
          }
        }
      },
    }),
    {
      name: 'auth-store',
      storage: createJSONStorage(() => 
        typeof window !== 'undefined' ? localStorage : {
          getItem: () => null,
          setItem: () => {},
          removeItem: () => {},
        }
      ),
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
);

// 初期化フック（アプリ起動時に呼び出す）
export const useAuthInitialization = () => {
  const initialize = useAuthStore((state) => state.initialize);
  
  React.useEffect(() => {
    initialize();
  }, [initialize]);
};

// 認証済みユーザーのみアクセス可能なページで使用するフック
export const useRequireAuth = (redirectTo = '/login') => {
  const { isAuthenticated, isLoading } = useAuthStore();
  
  React.useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      window.location.href = redirectTo;
    }
  }, [isAuthenticated, isLoading, redirectTo]);
  
  return { isAuthenticated, isLoading };
};
