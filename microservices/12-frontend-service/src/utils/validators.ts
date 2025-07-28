import { VALIDATION } from './constants';

/**
 * メールアドレスの妥当性を検証する
 */
export function validateEmail(email: string): boolean {
  return VALIDATION.EMAIL_PATTERN.test(email);
}

/**
 * パスワードの妥当性を検証する
 */
export function validatePassword(password: string): {
  isValid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (password.length < VALIDATION.PASSWORD_MIN_LENGTH) {
    errors.push(`パスワードは${VALIDATION.PASSWORD_MIN_LENGTH}文字以上である必要があります`);
  }

  if (!/[a-z]/.test(password)) {
    errors.push('パスワードには小文字を含める必要があります');
  }

  if (!/[A-Z]/.test(password)) {
    errors.push('パスワードには大文字を含める必要があります');
  }

  if (!/[0-9]/.test(password)) {
    errors.push('パスワードには数字を含める必要があります');
  }

  if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
    errors.push('パスワードには特殊文字を含める必要があります');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * ユーザー名の妥当性を検証する
 */
export function validateUsername(username: string): {
  isValid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (username.length < VALIDATION.USERNAME_MIN_LENGTH) {
    errors.push(`ユーザー名は${VALIDATION.USERNAME_MIN_LENGTH}文字以上である必要があります`);
  }

  if (username.length > VALIDATION.USERNAME_MAX_LENGTH) {
    errors.push(`ユーザー名は${VALIDATION.USERNAME_MAX_LENGTH}文字以下である必要があります`);
  }

  if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
    errors.push('ユーザー名には英数字、ハイフン、アンダースコアのみ使用できます');
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * 電話番号の妥当性を検証する
 */
export function validatePhoneNumber(phoneNumber: string): boolean {
  return VALIDATION.PHONE_PATTERN.test(phoneNumber);
}

/**
 * 郵便番号の妥当性を検証する（日本の郵便番号）
 */
export function validatePostalCode(postalCode: string): boolean {
  return VALIDATION.POSTAL_CODE_PATTERN.test(postalCode);
}

/**
 * クレジットカード番号の妥当性を検証する（Luhnアルゴリズム）
 */
export function validateCreditCard(cardNumber: string): {
  isValid: boolean;
  cardType: string;
} {
  // 数字のみを抽出
  const cleaned = cardNumber.replace(/\D/g, '');

  // Luhnアルゴリズムによる検証
  let sum = 0;
  let isEven = false;

  for (let i = cleaned.length - 1; i >= 0; i--) {
    let digit = parseInt(cleaned.charAt(i), 10);

    if (isEven) {
      digit *= 2;
      if (digit > 9) {
        digit -= 9;
      }
    }

    sum += digit;
    isEven = !isEven;
  }

  const isValid = sum % 10 === 0 && cleaned.length >= 13;

  // カードタイプの判定
  let cardType = 'unknown';
  if (/^4/.test(cleaned)) {
    cardType = 'visa';
  } else if (/^5[1-5]/.test(cleaned)) {
    cardType = 'mastercard';
  } else if (/^3[47]/.test(cleaned)) {
    cardType = 'amex';
  } else if (/^6(?:011|5)/.test(cleaned)) {
    cardType = 'discover';
  } else if (/^35/.test(cleaned)) {
    cardType = 'jcb';
  }

  return { isValid, cardType };
}

/**
 * CVVの妥当性を検証する
 */
export function validateCVV(cvv: string, cardType: string): boolean {
  const cleaned = cvv.replace(/\D/g, '');
  
  if (cardType === 'amex') {
    return cleaned.length === 4;
  }
  
  return cleaned.length === 3;
}

/**
 * 有効期限の妥当性を検証する
 */
export function validateExpiryDate(expiryMonth: string, expiryYear: string): {
  isValid: boolean;
  error?: string;
} {
  const month = parseInt(expiryMonth, 10);
  const year = parseInt(expiryYear, 10);
  
  if (month < 1 || month > 12) {
    return { isValid: false, error: '月は1-12の間で入力してください' };
  }

  const now = new Date();
  const currentYear = now.getFullYear() % 100; // 下2桁
  const currentMonth = now.getMonth() + 1;

  if (year < currentYear || (year === currentYear && month < currentMonth)) {
    return { isValid: false, error: '有効期限が過去の日付です' };
  }

  return { isValid: true };
}

/**
 * URLの妥当性を検証する
 */
export function validateUrl(url: string): boolean {
  try {
    new URL(url);
    return true;
  } catch {
    return false;
  }
}

/**
 * 必須フィールドの検証
 */
export function validateRequired(value: unknown, fieldName: string): {
  isValid: boolean;
  error?: string;
} {
  if (value === null || value === undefined || value === '') {
    return { isValid: false, error: `${fieldName}は必須です` };
  }

  if (typeof value === 'string' && value.trim() === '') {
    return { isValid: false, error: `${fieldName}は必須です` };
  }

  if (Array.isArray(value) && value.length === 0) {
    return { isValid: false, error: `${fieldName}は必須です` };
  }

  return { isValid: true };
}

/**
 * 数値の範囲検証
 */
export function validateRange(
  value: number,
  min: number,
  max: number,
  fieldName: string
): {
  isValid: boolean;
  error?: string;
} {
  if (value < min || value > max) {
    return {
      isValid: false,
      error: `${fieldName}は${min}から${max}の間で入力してください`
    };
  }

  return { isValid: true };
}

/**
 * 文字列の長さ検証
 */
export function validateLength(
  value: string,
  min: number,
  max: number,
  fieldName: string
): {
  isValid: boolean;
  error?: string;
} {
  if (value.length < min) {
    return {
      isValid: false,
      error: `${fieldName}は${min}文字以上で入力してください`
    };
  }

  if (value.length > max) {
    return {
      isValid: false,
      error: `${fieldName}は${max}文字以下で入力してください`
    };
  }

  return { isValid: true };
}

/**
 * ファイルの妥当性を検証する
 */
export function validateFile(
  file: File,
  allowedTypes: string[],
  maxSize: number
): {
  isValid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (!allowedTypes.includes(file.type)) {
    errors.push('許可されていないファイル形式です');
  }

  if (file.size > maxSize) {
    const maxSizeMB = Math.round(maxSize / (1024 * 1024));
    errors.push(`ファイルサイズは${maxSizeMB}MB以下にしてください`);
  }

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * 複数ファイルの妥当性を検証する
 */
export function validateFiles(
  files: File[],
  allowedTypes: string[],
  maxSize: number,
  maxFiles: number
): {
  isValid: boolean;
  errors: string[];
} {
  const errors: string[] = [];

  if (files.length > maxFiles) {
    errors.push(`ファイルは${maxFiles}個まで選択できます`);
  }

  files.forEach((file, index) => {
    const fileValidation = validateFile(file, allowedTypes, maxSize);
    if (!fileValidation.isValid) {
      errors.push(`ファイル${index + 1}: ${fileValidation.errors.join(', ')}`);
    }
  });

  return {
    isValid: errors.length === 0,
    errors
  };
}

/**
 * 日付の妥当性を検証する
 */
export function validateDate(dateString: string): {
  isValid: boolean;
  error?: string;
} {
  const date = new Date(dateString);
  
  if (isNaN(date.getTime())) {
    return { isValid: false, error: '有効な日付を入力してください' };
  }

  return { isValid: true };
}

/**
 * 年齢の妥当性を検証する
 */
export function validateAge(birthDate: string, minAge: number, maxAge: number): {
  isValid: boolean;
  error?: string;
} {
  const birth = new Date(birthDate);
  const today = new Date();
  
  if (isNaN(birth.getTime())) {
    return { isValid: false, error: '有効な生年月日を入力してください' };
  }

  const age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  
  const actualAge = monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate()) 
    ? age - 1 
    : age;

  if (actualAge < minAge || actualAge > maxAge) {
    return {
      isValid: false,
      error: `年齢は${minAge}歳から${maxAge}歳の間である必要があります`
    };
  }

  return { isValid: true };
}
