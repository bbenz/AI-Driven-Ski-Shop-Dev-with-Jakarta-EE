-- 初期テーブル作成
-- V1.0.0__Create_initial_tables.sql

-- ブランドテーブル
CREATE TABLE brands (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1000),
    country VARCHAR(100),
    logo_url VARCHAR(500),
    website_url VARCHAR(500),
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_brands_name ON brands(name);
CREATE INDEX idx_brands_featured ON brands(is_featured, is_active);

-- カテゴリテーブル
CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    parent_id UUID REFERENCES categories(id),
    path VARCHAR(500) NOT NULL,
    level INTEGER NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_categories_parent ON categories(parent_id);
CREATE INDEX idx_categories_path ON categories(path);
CREATE INDEX idx_categories_level ON categories(level);
CREATE INDEX idx_categories_sort ON categories(sort_order);

-- PostgreSQL UUID拡張を有効化
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 商品カテゴリマスタ
CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    short_description VARCHAR(500),
    category_id UUID NOT NULL REFERENCES categories(id),
    brand_id UUID NOT NULL REFERENCES brands(id),
    material VARCHAR(50) NOT NULL,
    ski_type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(50) NOT NULL,
    length VARCHAR(20),
    width VARCHAR(20),
    weight VARCHAR(20),
    radius VARCHAR(20),
    flex VARCHAR(20),
    publish_status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_discontinued BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    discontinued_at TIMESTAMP,
    base_price DECIMAL(10,2) NOT NULL,
    sale_price DECIMAL(10,2),
    cost_price DECIMAL(10,2),
    sales_count BIGINT NOT NULL DEFAULT 0,
    view_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_products_publish_status ON products(publish_status, is_active);
CREATE INDEX idx_products_featured ON products(is_featured, is_active);
CREATE INDEX idx_products_ski_type ON products(ski_type);
CREATE INDEX idx_products_difficulty_level ON products(difficulty_level);
CREATE INDEX idx_products_price ON products(base_price);
CREATE INDEX idx_products_created_at ON products(created_at);
CREATE INDEX idx_products_sales_count ON products(sales_count);

-- 商品バリエーションテーブル
CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_type VARCHAR(50) NOT NULL,
    variant_value VARCHAR(100) NOT NULL,
    sku VARCHAR(100),
    additional_price DECIMAL(10,2) DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    stock_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_product_variants_product ON product_variants(product_id);
CREATE INDEX idx_product_variants_type ON product_variants(variant_type);
CREATE INDEX idx_product_variants_default ON product_variants(is_default);

-- 商品画像テーブル
CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(255),
    image_type VARCHAR(50),
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

CREATE INDEX idx_product_images_product ON product_images(product_id);
CREATE INDEX idx_product_images_sort ON product_images(sort_order);
CREATE INDEX idx_product_images_primary ON product_images(is_primary);

-- 商品タグテーブル
CREATE TABLE product_tags (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    tag VARCHAR(100) NOT NULL,
    PRIMARY KEY (product_id, tag)
);

CREATE INDEX idx_product_tags_tag ON product_tags(tag);

-- 商品追加仕様テーブル
CREATE TABLE product_additional_specs (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    spec_key VARCHAR(100) NOT NULL,
    spec_value VARCHAR(500),
    PRIMARY KEY (product_id, spec_key)
);

CREATE INDEX idx_product_additional_specs_key ON product_additional_specs(spec_key);
