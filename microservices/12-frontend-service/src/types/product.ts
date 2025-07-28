// 商品関連の型定義
export interface Product {
  id: string;
  name: string;
  description: string;
  shortDescription?: string;
  price: number;
  originalPrice?: number;
  discountPercentage?: number;
  sku: string;
  categoryId: string;
  category: Category;
  brandId?: string;
  brand?: Brand;
  images: ProductImage[];
  attributes: ProductAttribute[];
  variants: ProductVariant[];
  inventory: ProductInventory;
  specifications: ProductSpecification[];
  reviews: ProductReview[];
  rating: number;
  reviewCount: number;
  tags: string[];
  isActive: boolean;
  isFeatured: boolean;
  isOnSale: boolean;
  weight?: number;
  dimensions?: ProductDimensions;
  shippingInfo?: ShippingInfo;
  warranty?: WarrantyInfo;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: string;
  name: string;
  slug: string;
  description?: string;
  parentId?: string;
  parent?: Category;
  children: Category[];
  image?: string;
  isActive: boolean;
  sortOrder: number;
  productCount: number;
}

export interface Brand {
  id: string;
  name: string;
  slug: string;
  description?: string;
  logo?: string;
  website?: string;
  isActive: boolean;
}

export interface ProductImage {
  id: string;
  url: string;
  alt: string;
  sortOrder: number;
  isPrimary: boolean;
}

export interface ProductAttribute {
  id: string;
  name: string;
  value: string;
  type: 'text' | 'number' | 'boolean' | 'date' | 'color' | 'size';
  unit?: string;
  isFilter: boolean;
  sortOrder: number;
}

export interface ProductVariant {
  id: string;
  name: string;
  sku: string;
  price: number;
  attributes: ProductAttribute[];
  inventory: number;
  isActive: boolean;
}

export interface ProductInventory {
  quantity: number;
  reservedQuantity: number;
  availableQuantity: number;
  lowStockThreshold: number;
  status: 'in_stock' | 'low_stock' | 'out_of_stock' | 'discontinued';
}

export interface ProductSpecification {
  id: string;
  name: string;
  value: string;
  group: string;
  sortOrder: number;
}

export interface ProductReview {
  id: string;
  userId: string;
  user: {
    id: string;
    username: string;
    firstName: string;
  };
  rating: number;
  title: string;
  comment: string;
  isVerifiedPurchase: boolean;
  helpfulVotes: number;
  images?: string[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductDimensions {
  length: number;
  width: number;
  height: number;
  unit: 'cm' | 'inch';
}

export interface ShippingInfo {
  freeShipping: boolean;
  weight: number;
  dimensions: ProductDimensions;
  shippingMethods: string[];
  estimatedDelivery: string;
}

export interface WarrantyInfo {
  duration: number;
  unit: 'months' | 'years';
  description: string;
  coverage: string[];
}

// 商品検索・フィルタリング関連
export interface ProductSearchParams {
  query?: string;
  categoryId?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  rating?: number;
  inStock?: boolean;
  onSale?: boolean;
  attributes?: Record<string, string[]>;
  sortBy?: 'relevance' | 'price_asc' | 'price_desc' | 'rating' | 'newest' | 'oldest';
  page?: number;
  pageSize?: number;
}

export interface ProductSearchResponse {
  products: Product[];
  totalCount: number;
  totalPages: number;
  currentPage: number;
  filters: ProductFilter[];
  aggregations: ProductAggregations;
}

export interface ProductFilter {
  type: 'category' | 'brand' | 'price' | 'rating' | 'attribute';
  name: string;
  values: ProductFilterValue[];
}

export interface ProductFilterValue {
  value: string;
  label: string;
  count: number;
  selected: boolean;
}

export interface ProductAggregations {
  minPrice: number;
  maxPrice: number;
  avgRating: number;
  totalProducts: number;
  brands: Array<{ id: string; name: string; count: number }>;
  categories: Array<{ id: string; name: string; count: number }>;
}
