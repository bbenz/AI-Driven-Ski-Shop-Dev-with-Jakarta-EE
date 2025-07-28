// API関連の共通型定義
export interface ApiResponse<T = unknown> {
  data: T;
  message?: string;
  success: boolean;
  timestamp: string;
  requestId?: string;
}

export interface ApiError {
  code: string;
  message: string;
  details?: Record<string, unknown>;
  timestamp: string;
  requestId?: string;
  field?: string;
}

export interface PaginationParams {
  page?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
}

export interface PaginatedResponse<T> {
  items: T[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
  hasNextPage: boolean;
  hasPreviousPage: boolean;
}

export interface SearchParams extends PaginationParams {
  query?: string;
  filters?: Record<string, string | number | boolean | string[]>;
}

// HTTP Methods
export type HttpMethod = 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE';

// API Endpoints
export interface ApiEndpoints {
  auth: {
    login: string;
    register: string;
    logout: string;
    refresh: string;
    forgotPassword: string;
    resetPassword: string;
    changePassword: string;
    profile: string;
  };
  products: {
    list: string;
    detail: (id: string) => string;
    search: string;
    categories: string;
    brands: string;
    reviews: (productId: string) => string;
  };
  cart: {
    get: string;
    add: string;
    update: (itemId: string) => string;
    remove: (itemId: string) => string;
    clear: string;
    applyCoupon: string;
    removeCoupon: string;
  };
  orders: {
    list: string;
    detail: (id: string) => string;
    create: string;
    cancel: (id: string) => string;
    track: (id: string) => string;
    returns: string;
  };
  checkout: {
    shippingMethods: string;
    paymentMethods: string;
    calculateTax: string;
    validateAddress: string;
  };
  user: {
    addresses: string;
    wishlist: string;
    preferences: string;
    notifications: string;
  };
  ai: {
    chat: string;
    recommendations: string;
    analyze: string;
  };
}

// Request/Response interceptor types
export interface RequestInterceptor {
  onRequest?: (config: Record<string, unknown>) => Record<string, unknown>;
  onRequestError?: (error: Error) => Promise<Error>;
}

export interface ResponseInterceptor {
  onResponse?: (response: Record<string, unknown>) => Record<string, unknown>;
  onResponseError?: (error: Error) => Promise<Error>;
}

// Cache configuration
export interface CacheConfig {
  ttl?: number; // Time to live in milliseconds
  key?: string;
  enabled?: boolean;
  strategy?: 'cache-first' | 'network-first' | 'cache-only' | 'network-only';
}

// API client configuration
export interface ApiClientConfig {
  baseURL: string;
  timeout?: number;
  headers?: Record<string, string>;
  retries?: number;
  retryDelay?: number;
  cache?: CacheConfig;
  interceptors?: {
    request?: RequestInterceptor[];
    response?: ResponseInterceptor[];
  };
}
