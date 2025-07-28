// ショッピングカートサービスとの統合用型定義
// Shopping Cart Service APIからのレスポンス型
export interface CartResponse {
  cartId: string;
  customerId: string | null;
  sessionId: string | null;
  items: CartItemResponse[];
  totals: CartTotals;
  status: 'ACTIVE' | 'INACTIVE' | 'COMPLETED';
  itemCount: number;
  createdAt: string;
  updatedAt: string;
  expiresAt: string;
}

export interface CartItemResponse {
  itemId: string;
  productId: string;
  sku: string;
  productName: string;
  productImageUrl?: string;
  unitPrice: number;
  quantity: number;
  totalPrice: number;
  options?: Record<string, unknown>;
  addedAt: string;
  updatedAt: string;
}

export interface CartTotals {
  subtotalAmount: number;
  taxAmount: number;
  shippingAmount: number;
  discountAmount: number;
  totalAmount: number;
  currency: string;
}

// ショッピングカートサービスAPIリクエスト型
export interface AddToCartRequest {
  productId: string;
  sku: string;
  productName: string;
  productImageUrl?: string;
  unitPrice: number;
  quantity: number;
  options?: Record<string, unknown>;
}

export interface UpdateQuantityRequest {
  quantity: number;
}

export interface CartValidationResponse {
  valid: boolean;
  errors: string[];
}

// 既存のUIコンポーネント用型定義（下位互換性のため残す）
export interface Cart {
  id: string;
  userId?: string;
  sessionId?: string;
  items: CartItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  currency: string;
  couponCode?: string;
  shippingAddress?: ShippingAddress;
  billingAddress?: BillingAddress;
  createdAt: string;
  updatedAt: string;
}

export interface CartItem {
  id: string;
  productId: string;
  product: {
    id: string;
    name: string;
    price: number;
    images: Array<{ url: string; alt: string }>;
    sku: string;
    inventory: {
      availableQuantity: number;
      status: string;
    };
  };
  variantId?: string;
  variant?: {
    id: string;
    name: string;
    price: number;
    attributes: Array<{ name: string; value: string }>;
  };
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  note?: string;
  addedAt: string;
}

export interface ShippingAddress {
  id?: string;
  firstName: string;
  lastName: string;
  company?: string;
  street: string;
  apartment?: string;
  city: string;
  state: string;
  postalCode: string;
  country: string;
  phoneNumber?: string;
  isDefault?: boolean;
}

export interface BillingAddress extends ShippingAddress {
  isSameAsShipping: boolean;
}

export interface AddToCartRequest {
  productId: string;
  variantId?: string;
  quantity: number;
  note?: string;
}

export interface UpdateCartItemRequest {
  quantity: number;
  note?: string;
}

export interface ApplyCouponRequest {
  couponCode: string;
}

export interface ShippingMethod {
  id: string;
  name: string;
  description: string;
  price: number;
  estimatedDays: number;
  carrier: string;
  isAvailable: boolean;
}

export interface PaymentMethod {
  id: string;
  type: 'credit_card' | 'debit_card' | 'paypal' | 'apple_pay' | 'google_pay' | 'bank_transfer';
  name: string;
  description: string;
  isAvailable: boolean;
  fees?: number;
  icon?: string;
}

// カート統計
export interface CartSummary {
  itemCount: number;
  uniqueItemCount: number;
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  savings: number;
  currency: string;
}
