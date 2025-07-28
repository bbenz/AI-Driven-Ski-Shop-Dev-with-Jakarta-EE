// 注文関連の型定義
import { ShippingAddress, BillingAddress, ShippingMethod, PaymentMethod } from './cart';

export interface Order {
  id: string;
  orderNumber: string;
  userId: string;
  user: {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
  };
  status: OrderStatus;
  items: OrderItem[];
  subtotal: number;
  tax: number;
  shipping: number;
  discount: number;
  total: number;
  currency: string;
  couponCode?: string;
  shippingAddress: ShippingAddress;
  billingAddress: BillingAddress;
  shippingMethod: ShippingMethod;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  trackingNumber?: string;
  notes?: string;
  orderDate: string;
  shippedDate?: string;
  deliveredDate?: string;
  cancelledDate?: string;
  refundedDate?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItem {
  id: string;
  productId: string;
  product: {
    id: string;
    name: string;
    sku: string;
    images: Array<{ url: string; alt: string }>;
  };
  variantId?: string;
  variant?: {
    id: string;
    name: string;
    attributes: Array<{ name: string; value: string }>;
  };
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  status: OrderItemStatus;
}

export type OrderStatus = 
  | 'pending'
  | 'confirmed'
  | 'processing'
  | 'shipped'
  | 'delivered'
  | 'cancelled'
  | 'refunded'
  | 'partially_refunded';

export type OrderItemStatus = 
  | 'pending'
  | 'confirmed'
  | 'processing'
  | 'shipped'
  | 'delivered'
  | 'cancelled'
  | 'refunded';

export type PaymentStatus = 
  | 'pending'
  | 'processing'
  | 'paid'
  | 'failed'
  | 'cancelled'
  | 'refunded'
  | 'partially_refunded';

export interface CreateOrderRequest {
  cartId: string;
  shippingAddressId: string;
  billingAddressId: string;
  shippingMethodId: string;
  paymentMethodId: string;
  notes?: string;
}

export interface OrderSearchParams {
  status?: OrderStatus[];
  paymentStatus?: PaymentStatus[];
  dateFrom?: string;
  dateTo?: string;
  minAmount?: number;
  maxAmount?: number;
  page?: number;
  pageSize?: number;
  sortBy?: 'date_desc' | 'date_asc' | 'amount_desc' | 'amount_asc';
}

export interface OrderSearchResponse {
  orders: Order[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
}

// 注文追跡
export interface OrderTracking {
  orderId: string;
  trackingNumber: string;
  carrier: string;
  status: TrackingStatus;
  events: TrackingEvent[];
  estimatedDelivery?: string;
  actualDelivery?: string;
}

export type TrackingStatus = 
  | 'label_created'
  | 'picked_up'
  | 'in_transit'
  | 'out_for_delivery'
  | 'delivered'
  | 'delivery_attempted'
  | 'returned'
  | 'exception';

export interface TrackingEvent {
  id: string;
  status: TrackingStatus;
  description: string;
  location?: string;
  timestamp: string;
}

// 返品・交換
export interface Return {
  id: string;
  orderId: string;
  orderItemIds: string[];
  reason: ReturnReason;
  reasonDescription?: string;
  status: ReturnStatus;
  refundAmount: number;
  shippingLabel?: string;
  trackingNumber?: string;
  requestedAt: string;
  processedAt?: string;
  completedAt?: string;
}

export type ReturnReason = 
  | 'defective'
  | 'wrong_item'
  | 'not_as_described'
  | 'damaged_in_shipping'
  | 'changed_mind'
  | 'size_issue'
  | 'other';

export type ReturnStatus = 
  | 'requested'
  | 'approved'
  | 'rejected'
  | 'shipped'
  | 'received'
  | 'processed'
  | 'completed'
  | 'cancelled';

export interface CreateReturnRequest {
  orderId: string;
  orderItemIds: string[];
  reason: ReturnReason;
  reasonDescription?: string;
}
