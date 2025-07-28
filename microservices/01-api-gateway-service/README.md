# API Gateway Service

This is the central API Gateway for the Ski Resort Management System. It provides routing, authentication, rate limiting, and other cross-cutting concerns for all microservices.

## Features

- **Request Routing**: Routes incoming requests to appropriate downstream microservices
- **Authentication**: JWT token validation and user context extraction
- **Rate Limiting**: Per-client request rate limiting with configurable limits
- **Health Monitoring**: Health checks for all downstream services
- **CORS Support**: Cross-Origin Resource Sharing configuration
- **Service Discovery**: Dynamic routing based on service availability

## Technology Stack

- Jakarta EE 11
- Java 21 LTS with Virtual Threads
- WildFly 31.0.1 Application Server
- MicroProfile 6.1 (Config, Health, Metrics, OpenAPI)
- JWT (JSON Web Tokens) for authentication
- Maven for build management

## Configuration

Configuration is managed through MicroProfile Config in `META-INF/microprofile-config.properties`:

```properties
# Server Configuration
server.host=0.0.0.0
server.port=8080

# JWT Configuration
jwt.secret=your-256-bit-secret-key
jwt.expiration.hours=24

# Rate Limiting
gateway.ratelimit.default.requests=100
gateway.ratelimit.auth.requests=200
gateway.ratelimit.window.seconds=60

# Service URLs
services.user.url=http://localhost:8081
services.product.url=http://localhost:8082
# ... other services
```

## API Routes

The gateway routes requests based on path prefixes:

- `/users/*` → User Management Service (port 8081)
- `/api/v1/products/*` → Product Catalog Service (port 8083)
- `/api/v1/categories/*` → Product Catalog Service (port 8083)
- `/products/*` → Product Catalog Service (port 8083) (deprecated path)
- `/categories/*` → Product Catalog Service (port 8083) (deprecated path)
- `/auth/*` → Authentication Service (port 8084)
- `/inventory/*` → Inventory Management Service (port 8085)
- `/orders/*` → Order Management Service (port 8086)
- `/payments/*` → Payment Service (port 8087)
- `/cart/*` → Shopping Cart Service (port 8088)
- `/coupons/*`, `/discounts/*` → Coupon/Discount Service (port 8089)
- `/points/*`, `/loyalty/*` → Points/Loyalty Service (port 8090)
- `/ai/*`, `/support/*` → AI Support Service (port 8091)

## Rate Limiting

Rate limiting is implemented with the following features:

- Per-client tracking (by user ID or IP address)
- Configurable limits per endpoint category
- Sliding window algorithm
- Rate limit headers in responses

## Authentication

JWT-based authentication with:

- Token validation on protected endpoints
- User context extraction and forwarding
- Public endpoints (health checks, auth endpoints)
- Authorization header forwarding to downstream services

## Product Catalog Service Endpoints

The API Gateway routes the following Product Catalog Service endpoints:

### Category Management (10 endpoints)

- `GET /api/v1/categories` - 全カテゴリ一覧取得
- `GET /api/v1/categories/root` - ルートカテゴリ一覧取得
- `GET /api/v1/categories/main` - メインカテゴリ一覧取得
- `GET /api/v1/categories/path` - パスでカテゴリ取得
- `GET /api/v1/categories/{categoryId}` - カテゴリ詳細取得
- `GET /api/v1/categories/{categoryId}/children` - 子カテゴリ一覧取得
- `GET /api/v1/categories/level/{level}` - レベル別カテゴリ取得
- `GET /api/v1/categories/{categoryId}/subcategories` - サブカテゴリ一覧取得
- `GET /api/v1/categories/{categoryId}/products` - カテゴリの商品一覧取得
- `GET /api/v1/categories/{categoryId}/subcategories/products` - サブカテゴリ毎の商品一覧取得

### Product Management (9 endpoints)

- `GET /api/v1/products` - 商品一覧・検索
- `GET /api/v1/products/featured` - 注目商品一覧
- `GET /api/v1/products/{productId}` - 商品詳細取得
- `GET /api/v1/products/sku/{sku}` - SKUで商品取得
- `GET /api/v1/products/category/{categoryId}` - カテゴリ別商品一覧
- `GET /api/v1/products/brand/{brandId}` - ブランド別商品一覧
- `POST /api/v1/products` - 商品登録
- `PUT /api/v1/products/{productId}` - 商品更新
- `DELETE /api/v1/products/{productId}` - 商品削除

### System Endpoints

- `GET /q/health` - ヘルスチェック
- `GET /q/openapi` - OpenAPI仕様書取得

## Health Checks

Multiple health check endpoints:

- `/health` - MicroProfile Health checks
- `/health/services` - Detailed status of all downstream services
- Liveness and Readiness probes

## Building and Running

```bash
# Build the project
mvn clean compile

# Run tests
mvn test

# Package WAR file
mvn package

# Deploy to WildFly
mvn wildfly:deploy
```

## Development

The service includes comprehensive unit tests and follows Jakarta EE best practices:

- Constructor injection instead of field injection
- Proper exception handling and logging
- Configurable timeouts and retry logic
- Clean separation of concerns

## Monitoring

The gateway provides metrics and monitoring through:

- MicroProfile Metrics
- Structured logging
- Service health tracking
- Request/response timing

## Security Considerations

- JWT secret key must be changed in production
- HTTPS should be enforced in production
- Rate limiting helps prevent abuse
- Input validation on all proxied requests
- Proper CORS configuration for web clients
