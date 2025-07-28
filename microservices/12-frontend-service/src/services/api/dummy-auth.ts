import type { User, AuthResponse } from '@/types/auth';

// ダミーユーザのデータ
export const DUMMY_USER: User = {
  id: 'dummy-user-001',
  email: 'demo@skiresort.com',
  username: 'demo_user',
  firstName: '田中',
  lastName: '太郎',
  role: {
    id: 'customer',
    name: 'カスタマー',
    permissions: [
      {
        id: 'view-products',
        name: '商品閲覧',
        resource: 'products',
        action: 'read'
      },
      {
        id: 'purchase',
        name: '購入',
        resource: 'orders',
        action: 'create'
      }
    ]
  },
  isActive: true,
  phoneNumber: '090-1234-5678',
  dateOfBirth: '1985-05-15',
  address: {
    street: '1-2-3 スキー場通り',
    city: '白馬村',
    state: '長野県',
    postalCode: '399-9301',
    country: '日本'
  },
  preferences: {
    language: 'ja',
    timezone: 'Asia/Tokyo',
    notifications: {
      email: true,
      push: true,
      sms: false
    },
    theme: 'light'
  },
  createdAt: '2025-01-01T00:00:00Z',
  updatedAt: '2025-07-25T00:00:00Z'
};

// ダミーユーザの認証情報
export const DUMMY_CREDENTIALS = {
  email: 'demo@skiresort.com',
  password: 'demo123'
};

// 設定フラグ（本番環境では必ずfalseにする）
let isDummyUserEnabled = true;

/**
 * ダミーユーザの有効/無効を切り替え
 */
export const toggleDummyUser = (enabled: boolean): void => {
  isDummyUserEnabled = enabled;
  console.log(`ダミーユーザ機能: ${enabled ? '有効' : '無効'}`);
};

/**
 * ダミーユーザが有効かどうかを確認
 */
export const isDummyUserActive = (): boolean => {
  return isDummyUserEnabled;
};

/**
 * ダミーユーザでのログイン
 */
export const loginWithDummyUser = async (email: string, password: string): Promise<AuthResponse | null> => {
  if (!isDummyUserEnabled) {
    return null;
  }

  if (email === DUMMY_CREDENTIALS.email && password === DUMMY_CREDENTIALS.password) {
    // ダミートークンを生成
    const dummyToken = btoa(JSON.stringify({
      userId: DUMMY_USER.id,
      exp: Date.now() + (24 * 60 * 60 * 1000), // 24時間後に期限切れ
      type: 'dummy'
    }));

    return {
      user: DUMMY_USER,
      token: `dummy_${dummyToken}`,
      refreshToken: `dummy_refresh_${dummyToken}`,
      expiresIn: 24 * 60 * 60 // 24時間（秒）
    };
  }

  return null;
};

/**
 * ダミートークンかどうかを確認
 */
export const isDummyToken = (token: string): boolean => {
  return token.startsWith('dummy_');
};

/**
 * ダミートークンからユーザー情報を取得
 */
export const getUserFromDummyToken = (token: string): User | null => {
  if (!isDummyToken(token) || !isDummyUserEnabled) {
    return null;
  }

  try {
    const tokenData = token.replace('dummy_', '');
    const decoded = JSON.parse(atob(tokenData));
    
    // トークンの有効期限をチェック
    if (decoded.exp < Date.now()) {
      return null;
    }

    return DUMMY_USER;
  } catch {
    return null;
  }
};

/**
 * 環境に応じてダミーユーザを無効化
 */
export const initializeDummyUserSettings = (): void => {
  // 本番環境では自動的に無効化
  if (process.env.NODE_ENV === 'production') {
    isDummyUserEnabled = false;
    console.warn('本番環境のため、ダミーユーザ機能は無効化されています');
  }
  
  // 環境変数での制御も可能
  if (process.env.NEXT_PUBLIC_DISABLE_DUMMY_USER === 'true') {
    isDummyUserEnabled = false;
    console.log('環境変数によりダミーユーザ機能が無効化されています');
  }
};
