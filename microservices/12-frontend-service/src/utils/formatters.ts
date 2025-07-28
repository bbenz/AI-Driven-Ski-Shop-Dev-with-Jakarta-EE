/**
 * 価格をフォーマットする
 */
export function formatPrice(
  amount: number,
  currency = 'JPY',
  locale = 'ja-JP'
): string {
  return new Intl.NumberFormat(locale, {
    style: 'currency',
    currency,
  }).format(amount);
}

/**
 * 数値をパーセンテージでフォーマットする
 */
export function formatPercentage(
  value: number,
  decimals = 0,
  locale = 'ja-JP'
): string {
  return new Intl.NumberFormat(locale, {
    style: 'percent',
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(value / 100);
}

/**
 * 大きな数値を簡潔にフォーマットする (1K, 1M, 1B など)
 */
export function formatCompactNumber(
  value: number,
  locale = 'ja-JP'
): string {
  return new Intl.NumberFormat(locale, {
    notation: 'compact',
    compactDisplay: 'short',
  }).format(value);
}

/**
 * 日付をフォーマットする
 */
export function formatDate(
  date: string | Date,
  options: Intl.DateTimeFormatOptions = {},
  locale = 'ja-JP'
): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  };

  return new Intl.DateTimeFormat(locale, { ...defaultOptions, ...options }).format(dateObj);
}

/**
 * 相対的な時間をフォーマットする (例: "2時間前")
 */
export function formatRelativeTime(
  date: string | Date,
  locale = 'ja-JP'
): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - dateObj.getTime()) / 1000);

  const rtf = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });

  if (diffInSeconds < 60) {
    return rtf.format(-diffInSeconds, 'second');
  }

  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) {
    return rtf.format(-diffInMinutes, 'minute');
  }

  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) {
    return rtf.format(-diffInHours, 'hour');
  }

  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) {
    return rtf.format(-diffInDays, 'day');
  }

  const diffInMonths = Math.floor(diffInDays / 30);
  if (diffInMonths < 12) {
    return rtf.format(-diffInMonths, 'month');
  }

  const diffInYears = Math.floor(diffInMonths / 12);
  return rtf.format(-diffInYears, 'year');
}

/**
 * 時間のみをフォーマットする
 */
export function formatTime(
  date: string | Date,
  options: Intl.DateTimeFormatOptions = {},
  locale = 'ja-JP'
): string {
  const dateObj = typeof date === 'string' ? new Date(date) : date;
  
  const defaultOptions: Intl.DateTimeFormatOptions = {
    hour: '2-digit',
    minute: '2-digit',
  };

  return new Intl.DateTimeFormat(locale, { ...defaultOptions, ...options }).format(dateObj);
}

/**
 * 電話番号をフォーマットする (日本の電話番号形式)
 */
export function formatPhoneNumber(phoneNumber: string): string {
  // 数字のみを抽出
  const cleaned = phoneNumber.replace(/\D/g, '');
  
  // 日本の携帯電話番号の場合 (090, 080, 070で始まる11桁)
  if (cleaned.match(/^(090|080|070)\d{8}$/)) {
    return cleaned.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
  }
  
  // 日本の固定電話番号の場合 (0から始まる10桁)
  if (cleaned.match(/^0\d{9}$/)) {
    return cleaned.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
  }
  
  // その他の場合はそのまま返す
  return phoneNumber;
}

/**
 * 郵便番号をフォーマットする (日本の郵便番号形式)
 */
export function formatPostalCode(postalCode: string): string {
  const cleaned = postalCode.replace(/\D/g, '');
  
  if (cleaned.length === 7) {
    return cleaned.replace(/(\d{3})(\d{4})/, '$1-$2');
  }
  
  return postalCode;
}

/**
 * クレジットカード番号をフォーマットする (マスクしつつハイフンを追加)
 */
export function formatCreditCardNumber(cardNumber: string, mask = true): string {
  const cleaned = cardNumber.replace(/\D/g, '');
  
  if (mask && cleaned.length >= 4) {
    const lastFour = cleaned.slice(-4);
    const masked = '*'.repeat(Math.max(0, cleaned.length - 4));
    const combined = masked + lastFour;
    return combined.replace(/(.{4})/g, '$1 ').trim();
  }
  
  return cleaned.replace(/(.{4})/g, '$1 ').trim();
}

/**
 * テキストを指定した長さで切り詰める
 */
export function truncateText(text: string, maxLength: number, ellipsis = '...'): string {
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength - ellipsis.length) + ellipsis;
}

/**
 * URLを安全なリンクテキストとしてフォーマットする
 */
export function formatUrl(url: string, maxLength = 50): string {
  try {
    const urlObj = new URL(url);
    const displayUrl = urlObj.hostname + urlObj.pathname;
    return truncateText(displayUrl, maxLength);
  } catch {
    return truncateText(url, maxLength);
  }
}

/**
 * ファイル名から拡張子を取得する
 */
export function getFileExtension(filename: string): string {
  return filename.slice((filename.lastIndexOf('.') - 1 >>> 0) + 2);
}

/**
 * MIMEタイプからファイルタイプのカテゴリを取得する
 */
export function getFileTypeCategory(mimeType: string): string {
  if (mimeType.startsWith('image/')) return 'image';
  if (mimeType.startsWith('video/')) return 'video';
  if (mimeType.startsWith('audio/')) return 'audio';
  if (mimeType.includes('pdf')) return 'pdf';
  if (mimeType.includes('word') || mimeType.includes('document')) return 'document';
  if (mimeType.includes('sheet') || mimeType.includes('excel')) return 'spreadsheet';
  if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return 'presentation';
  if (mimeType.includes('zip') || mimeType.includes('compressed')) return 'archive';
  return 'other';
}

/**
 * カラーコードを16進数からRGBに変換する
 */
export function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
  return result ? {
    r: parseInt(result[1], 16),
    g: parseInt(result[2], 16),
    b: parseInt(result[3], 16)
  } : null;
}

/**
 * RGBからHSLに変換する
 */
export function rgbToHsl(r: number, g: number, b: number): { h: number; s: number; l: number } {
  r /= 255;
  g /= 255;
  b /= 255;

  const max = Math.max(r, g, b);
  const min = Math.min(r, g, b);
  let h = 0;
  let s = 0;
  const l = (max + min) / 2;

  if (max === min) {
    h = s = 0; // achromatic
  } else {
    const d = max - min;
    s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

    switch (max) {
      case r: h = (g - b) / d + (g < b ? 6 : 0); break;
      case g: h = (b - r) / d + 2; break;
      case b: h = (r - g) / d + 4; break;
    }

    h /= 6;
  }

  return { h: Math.round(h * 360), s: Math.round(s * 100), l: Math.round(l * 100) };
}
