import httpClient from '@/services/http/client';
import type {
  User,
  LoginRequest,
  RegisterRequest,
  AuthResponse,
  RefreshTokenRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest,
  ChangePasswordRequest,
  UpdateProfileRequest,
} from '@/types/auth';
import { 
  loginWithDummyUser, 
  isDummyUserActive, 
  getUserFromDummyToken, 
  isDummyToken,
  initializeDummyUserSettings
} from './dummy-auth';

/**
 * 認証関連API
 */
export const authAPI = {
  /**
   * ログイン
   */
  async login(credentials: LoginRequest): Promise<AuthResponse> {
    // ダミーユーザ機能が有効な場合、まずダミーユーザでのログインを試行
    if (isDummyUserActive()) {
      const dummyResponse = await loginWithDummyUser(credentials.email, credentials.password);
      if (dummyResponse) {
        console.log('ダミーユーザでログインしました');
        return dummyResponse;
      }
    }

    // 通常のログイン処理
    return await httpClient.post<AuthResponse>('/auth/login', credentials);
  },

  /**
   * ログアウト
   */
  async logout(): Promise<void> {
    return await httpClient.post<void>('/auth/logout');
  },

  /**
   * ユーザー登録
   */
  async register(userData: RegisterRequest): Promise<AuthResponse> {
    return await httpClient.post<AuthResponse>('/auth/register', userData);
  },

  /**
   * トークンリフレッシュ
   */
  async refreshToken(request: RefreshTokenRequest): Promise<AuthResponse> {
    return await httpClient.post<AuthResponse>('/auth/refresh', request);
  },

  /**
   * 現在のユーザー情報を取得
   */
  async getCurrentUser(): Promise<User> {
    // ダミートークンの場合はダミーユーザを返す
    const token = localStorage.getItem('auth_token');
    if (token && isDummyToken(token)) {
      const dummyUser = getUserFromDummyToken(token);
      if (dummyUser) {
        return dummyUser;
      }
    }

    return await httpClient.get<User>('/auth/me');
  },

  /**
   * プロフィール更新
   */
  async updateProfile(updates: UpdateProfileRequest): Promise<User> {
    return await httpClient.patch<User>('/auth/profile', updates);
  },

  /**
   * パスワード変更
   */
  async changePassword(request: ChangePasswordRequest): Promise<void> {
    return await httpClient.post<void>('/auth/change-password', request);
  },

  /**
   * パスワード忘れ
   */
  async forgotPassword(request: ForgotPasswordRequest): Promise<void> {
    return await httpClient.post<void>('/auth/forgot-password', request);
  },

  /**
   * パスワードリセット
   */
  async resetPassword(request: ResetPasswordRequest): Promise<void> {
    return await httpClient.post<void>('/auth/reset-password', request);
  },

  /**
   * メールアドレス確認
   */
  async verifyEmail(token: string): Promise<void> {
    return await httpClient.post<void>('/auth/verify-email', { token });
  },

  /**
   * メールアドレス確認メール再送信
   */
  async resendVerificationEmail(): Promise<void> {
    return await httpClient.post<void>('/auth/resend-verification');
  },

  /**
   * アカウント削除
   */
  async deleteAccount(password: string): Promise<void> {
    return await httpClient.delete<void>('/auth/account', {
      data: { password },
    });
  },

  /**
   * ユーザー設定更新
   */
  async updatePreferences(preferences: User['preferences']): Promise<User> {
    return await httpClient.patch<User>('/auth/preferences', preferences);
  },

  /**
   * 2FA有効化
   */
  async enable2FA(): Promise<{ qrCode: string; backupCodes: string[] }> {
    return await httpClient.post<{ qrCode: string; backupCodes: string[] }>('/auth/2fa/enable');
  },

  /**
   * 2FA無効化
   */
  async disable2FA(code: string): Promise<void> {
    return await httpClient.post<void>('/auth/2fa/disable', { code });
  },

  /**
   * 2FA確認
   */
  async verify2FA(code: string): Promise<{ isValid: boolean }> {
    return await httpClient.post<{ isValid: boolean }>('/auth/2fa/verify', { code });
  },

  /**
   * ログインセッション一覧取得
   */
  async getSessions(): Promise<Array<{
    id: string;
    deviceName: string;
    ipAddress: string;
    userAgent: string;
    lastActivity: string;
    isCurrent: boolean;
  }>> {
    return await httpClient.get<Array<{
      id: string;
      deviceName: string;
      ipAddress: string;
      userAgent: string;
      lastActivity: string;
      isCurrent: boolean;
    }>>('/auth/sessions');
  },

  /**
   * セッション削除
   */
  async revokeSession(sessionId: string): Promise<void> {
    return await httpClient.delete<void>(`/auth/sessions/${sessionId}`);
  },

  /**
   * 全セッション削除（現在のセッション以外）
   */
  async revokeAllSessions(): Promise<void> {
    return await httpClient.delete<void>('/auth/sessions');
  },
};

// ダミーユーザ設定の初期化（アプリ起動時に呼び出す）
initializeDummyUserSettings();
