import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Tailwind CSSクラスを安全にマージするユーティリティ関数
 */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * 文字列を安全にトリミングする
 */
export function safeTrim(str: string | null | undefined): string {
  return str?.trim() ?? '';
}

/**
 * 空の値をチェックする
 */
export function isEmpty(value: unknown): boolean {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim().length === 0;
  if (Array.isArray(value)) return value.length === 0;
  if (typeof value === 'object') return Object.keys(value as object).length === 0;
  return false;
}

/**
 * オブジェクトから未定義のプロパティを除去する
 */
export function removeUndefined<T extends Record<string, unknown>>(obj: T): Partial<T> {
  const result: Partial<T> = {};
  for (const [key, value] of Object.entries(obj)) {
    if (value !== undefined) {
      (result as Record<string, unknown>)[key] = value;
    }
  }
  return result;
}

/**
 * ディープクローンを作成する
 */
export function deepClone<T>(obj: T): T {
  if (obj === null || typeof obj !== 'object') return obj;
  if (obj instanceof Date) return new Date(obj.getTime()) as T;
  if (obj instanceof Array) return obj.map(deepClone) as T;
  if (typeof obj === 'object') {
    const clonedObj = {} as T;
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        clonedObj[key] = deepClone(obj[key]);
      }
    }
    return clonedObj;
  }
  return obj;
}

/**
 * デバウンス関数
 */
export function debounce<T extends (...args: Parameters<T>) => ReturnType<T>>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout;
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

/**
 * スロットル関数
 */
export function throttle<T extends (...args: Parameters<T>) => ReturnType<T>>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let inThrottle = false;
  return (...args: Parameters<T>) => {
    if (!inThrottle) {
      func(...args);
      inThrottle = true;
      setTimeout(() => (inThrottle = false), delay);
    }
  };
}

/**
 * ランダムIDを生成する
 */
export function generateId(prefix = 'id'): string {
  return `${prefix}_${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * URLからクエリパラメータを取得する
 */
export function getQueryParams(url?: string): Record<string, string> {
  const searchParams = new URLSearchParams(url ? new URL(url).search : window.location.search);
  const params: Record<string, string> = {};
  for (const [key, value] of searchParams.entries()) {
    params[key] = value;
  }
  return params;
}

/**
 * オブジェクトをクエリパラメータ文字列に変換する
 */
export function objectToQueryParams(obj: Record<string, unknown>): string {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(obj)) {
    if (value !== null && value !== undefined) {
      if (Array.isArray(value)) {
        value.forEach(item => params.append(key, String(item)));
      } else {
        params.append(key, String(value));
      }
    }
  }
  return params.toString();
}

/**
 * ファイルサイズを人間が読める形式に変換する
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * エラーオブジェクトから安全にメッセージを取得する
 */
export function getErrorMessage(error: unknown): string {
  if (error instanceof Error) return error.message;
  if (typeof error === 'string') return error;
  if (error && typeof error === 'object' && 'message' in error) {
    return String(error.message);
  }
  return 'An unknown error occurred';
}

/**
 * 環境変数を安全に取得する
 */
export function getEnvVar(key: string, defaultValue?: string): string {
  const value = process.env[key];
  if (value === undefined && defaultValue === undefined) {
    throw new Error(`Environment variable ${key} is not defined`);
  }
  return value ?? defaultValue ?? '';
}

/**
 * ブラウザの種類を判定する
 */
export function getBrowserInfo() {
  if (typeof window === 'undefined') return { name: 'unknown', version: 'unknown' };
  
  const userAgent = navigator.userAgent;
  let browserName = 'unknown';
  let browserVersion = 'unknown';

  if (userAgent.includes('Chrome')) {
    browserName = 'Chrome';
    browserVersion = userAgent.match(/Chrome\/(\d+)/)?.[1] ?? 'unknown';
  } else if (userAgent.includes('Firefox')) {
    browserName = 'Firefox';
    browserVersion = userAgent.match(/Firefox\/(\d+)/)?.[1] ?? 'unknown';
  } else if (userAgent.includes('Safari')) {
    browserName = 'Safari';
    browserVersion = userAgent.match(/Version\/(\d+)/)?.[1] ?? 'unknown';
  } else if (userAgent.includes('Edge')) {
    browserName = 'Edge';
    browserVersion = userAgent.match(/Edge\/(\d+)/)?.[1] ?? 'unknown';
  }

  return { name: browserName, version: browserVersion };
}
