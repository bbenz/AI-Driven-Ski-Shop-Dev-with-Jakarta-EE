# スキー用品販売ショップサイト マイクロサービス設計書

## 目次

1. [概要](#概要)
2. [システムアーキテクチャ](#システムアーキテクチャ)
3. [マイクロサービス仕様](#マイクロサービス仕様)
4. [データアーキテクチャ](#データアーキテクチャ)
5. [セキュリティ設計](#セキュリティ設計)
6. [APIデザイン](#apiデザイン)
7. [インフラ・デザイン](#インフラデザイン)
8. [非機能要件への対応](#非機能要件への対応)
9. [インフラストラクチャ](#インフラストラクチャ)
10. [運用・監視](#運用監視)
11. [開発・運用プロセス](#開発運用プロセス)
12. [リスク管理](#リスク管理)
13. [開発・デプロイメント](#開発デプロイメント)

## 概要

### システム概要

スキー用品を販売するオンラインショップサイトを**Jakarta EE 11**と**Java 21 LTS**を基盤としたマイクロサービスアーキテクチャで構築します。Virtual Threads、Record クラス、Pattern Matching などの最新Java機能を活用し、高い並行性と保守性を実現します。

### ビジネス要件

- **対象顧客**: スキー愛好家、初心者から上級者まで
- **商品カテゴリ**: スキー板、ブーツ、ウェア、アクセサリー、メンテナンス用品
- **予想同時利用者数**: 1,000〜10,000人（ピーク時）
- **売上目標**: 年間50億円
- **地域**: 日本全国（多言語対応準備）

### 技術要件

- **基盤技術**: Jakarta EE 11, Java 21 LTS
- **アーキテクチャ**: マイクロサービス
- **クラウド**: Microsoft Azure
- **コンテナ**: Docker + Kubernetes
- **データベース**: PostgreSQL, Redis, MongoDB
- **メッセージング**: Apache Kafka, Azure Service Bus

## システムアーキテクチャ

### 全体アーキテクチャ図

```mermaid
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web Frontend  │    │   Mobile Apps    │    │  Admin Portal   │
│     (React)     │    │ (React Native)   │    │    (Angular)    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
          │                       │                       │
          └───────────────────────┼───────────────────────┘
                                  │
                    ┌─────────────────────────┐
                    │     API Gateway         │
                    │   (Kong / Azure APIM)   │
                    └─────────────────────────┘
                                  │
          ┌───────────────────────┼───────────────────────┐
          │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Authentication  │    │ User Management │    │ Product Catalog │
│    Service      │    │    Service      │    │    Service      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
          │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Inventory Mgmt  │    │ Order/Sales     │    │ Payment/Cart    │
│    Service      │    │    Service      │    │    Service      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
          │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Coupon/Discount │    │ Points/Loyalty  │    │ AI Support      │
│    Service      │    │    Service      │    │    Service      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### マイクロサービス構成

| サービス名 | 責務 | 技術スタック |
|-----------|------|-------------|
| API Gateway | ルーティング、認証、レート制限 | Kong, Azure API Management |
| Authentication Service | OAuth 2.0/OIDC認証 | Jakarta Security, Keycloak |
| User Management Service | ユーザープロファイル管理 | Jakarta EE 11, PostgreSQL |
| Product Catalog Service | 商品情報管理 | Jakarta EE 11, PostgreSQL |
| Inventory Management Service | 在庫管理 | Jakarta EE 11, PostgreSQL |
| Order/Sales Service | 注文・販売処理 | Jakarta EE 11, PostgreSQL |
| Payment/Cart Service | 決済・カート処理 | Jakarta EE 11, PostgreSQL |
| Coupon/Discount Service | クーポン・割引管理 | Jakarta EE 11, Redis |
| Points/Loyalty Service | ポイント・ロイヤルティ | Jakarta EE 11, PostgreSQL |
| AI Support Service | AI チャットボット | Jakarta EE 11, Azure OpenAI |
| Frontend Service | Webサイト配信 | React, Next.js |

## マイクロサービス仕様

### 1. API Gateway Service

**責務**: 外部からの全リクエストの統一エントリーポイント

**主要機能**:

- リクエストルーティング
- 認証・認可の一元管理
- レート制限・スロットリング
- リクエスト/レスポンスの変換
- ロードバランシング
- 監視・ログ収集

**技術スタック**:

- Kong Gateway / Azure API Management
- OAuth 2.0 / OpenID Connect
- Rate Limiting (Redis)
- Health Check Integration

**API エンドポイント**:

```yaml
/api/v1/auth/*          → Authentication Service
/api/v1/users/*         → User Management Service
/api/v1/products/*      → Product Catalog Service
/api/v1/inventory/*     → Inventory Management Service
/api/v1/orders/*        → Order/Sales Service
/api/v1/payments/*      → Payment/Cart Service
/api/v1/coupons/*       → Coupon/Discount Service
/api/v1/points/*        → Points/Loyalty Service
/api/v1/support/*       → AI Support Service
```

### 2. Authentication Service

**責務**: OAuth 2.0/OpenID Connect による認証・認可

**主要機能**:

- OAuth 2.0 Authorization Server
- JWT トークン発行・検証
- リフレッシュトークン管理
- ソーシャルログイン連携（Google, Facebook, Line）
- MFA（多要素認証）
- セッション管理

**技術スタック**:

- Jakarta Security 3.1
- Keycloak (Identity Provider)
- Jakarta REST 4.0
- PostgreSQL (ユーザー認証情報)
- Redis (セッション管理)

**主要API**:

```java
// Jakarta EE Record ベース API
@Path("/auth")
@ApplicationScoped
public class AuthenticationResource {
    
    // OAuth 2.0 認証開始
    @POST
    @Path("/oauth/authorize")
    public Response authorize(AuthorizeRequest request);
    
    // アクセストークン取得
    @POST
    @Path("/oauth/token")
    public TokenResponse getToken(TokenRequest request);
    
    // トークン検証
    @POST
    @Path("/oauth/verify")
    public TokenValidationResponse validateToken(String token);
    
    // ログアウト
    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String token);
}

// Record クラスベースのデータ転送
public record AuthorizeRequest(
    String clientId,
    String redirectUri,
    String scope,
    String state
) {}

public record TokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    int expiresIn,
    String scope
) {}
```

### 3. User Management Service

**責務**: ユーザープロファイル・アカウント情報管理

**主要機能**:

- ユーザー登録・プロファイル管理
- 個人情報管理（GDPR対応）
- 配送先住所管理
- お気に入り商品管理
- 注文履歴参照
- アカウント設定

**技術スタック**:

- Jakarta EE 11 (CDI 4.1, Jakarta Persistence 3.2)
- Jakarta Data 1.0 (Repository Pattern)
- PostgreSQL
- Virtual Threads (非同期処理)

**ドメインモデル**:

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String phoneNumber;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Address> addresses;
    
    @OneToMany(mappedBy = "user")
    private List<UserPreference> preferences;
    
    // Virtual Threads 対応の非同期メソッド
    @Asynchronous
    public CompletableFuture<List<Order>> getOrderHistoryAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // 注文履歴取得処理
            return orderService.findByUserId(this.id);
        });
    }
}

// Record ベース Value Object
public record Address(
    String street,
    String city,
    String prefecture,
    String postalCode,
    String country,
    AddressType type
) {}

public enum AddressType {
    BILLING, SHIPPING, BOTH
}
```

**主要API**:

```java
@Path("/users")
@ApplicationScoped
public class UserResource {
    
    @Inject
    private UserRepository userRepository;
    
    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("userId") UUID userId) {
        return userRepository.findById(userId)
            .map(user -> Response.ok(UserDTO.from(user)).build())
            .orElse(Response.status(404).build());
    }
    
    @PUT
    @Path("/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("userId") UUID userId, 
                              UserUpdateRequest request) {
        // ビジネスロジック実装
    }
    
    @POST
    @Path("/{userId}/addresses")
    public Response addAddress(@PathParam("userId") UUID userId,
                              AddAddressRequest request) {
        // 住所追加処理
    }
}
```

### 4. Product Catalog Service

**責務**: 商品情報・カタログ管理

**主要機能**:

- 商品マスター管理
- カテゴリ管理（階層構造）
- 商品検索・フィルタリング
- 商品レビュー・評価
- 商品画像管理
- 価格管理・セール価格
- 商品推薦エンジン連携

**技術スタック**:

- Jakarta EE 11
- PostgreSQL (商品マスター)
- Elasticsearch (全文検索)
- Azure Blob Storage (画像保存)
- Redis (キャッシュ)

**ドメインモデル**:

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    private String name;
    private String description;
    private String brand;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Embedded
    private Price price;
    
    @ElementCollection
    @CollectionTable(name = "product_images")
    private List<ProductImage> images;
    
    @ElementCollection
    @Enumerated(EnumType.STRING)
    private Set<SkiType> skiTypes;
    
    // 商品仕様（Record クラス）
    @Embedded
    private SkiSpecification specification;
    
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Record ベース Value Object
public record Price(
    BigDecimal regularPrice,
    BigDecimal salePrice,
    String currency,
    LocalDateTime saleStartDate,
    LocalDateTime saleEndDate
) {
    public BigDecimal getCurrentPrice() {
        LocalDateTime now = LocalDateTime.now();
        if (salePrice != null && 
            saleStartDate != null && saleEndDate != null &&
            now.isAfter(saleStartDate) && now.isBefore(saleEndDate)) {
            return salePrice;
        }
        return regularPrice;
    }
}

public record SkiSpecification(
    Integer length,
    String material,
    String flexRating,
    String terrainType,
    String skillLevel
) {}

public enum SkiType {
    ALL_MOUNTAIN, CARVING, FREESTYLE, RACING, BACKCOUNTRY
}
```

**主要API**:

```java
@Path("/products")
@ApplicationScoped
public class ProductResource {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchProducts(
            @QueryParam("q") String query,
            @QueryParam("category") String category,
            @QueryParam("brand") String brand,
            @QueryParam("minPrice") BigDecimal minPrice,
            @QueryParam("maxPrice") BigDecimal maxPrice,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        
        var searchCriteria = new ProductSearchCriteria(
            query, category, brand, minPrice, maxPrice
        );
        
        var pageable = Pageable.of(page, size);
        var results = productSearchService.search(searchCriteria, pageable);
        
        return Response.ok(results).build();
    }
    
    @GET
    @Path("/{productId}")
    public Response getProduct(@PathParam("productId") UUID productId) {
        // 商品詳細取得
    }
    
    @GET
    @Path("/{productId}/recommendations")
    public Response getRecommendations(@PathParam("productId") UUID productId) {
        // AI推薦商品取得
    }
}
```

### 5. Inventory Management Service

**責務**: 在庫管理・在庫調整

**主要機能**:

- リアルタイム在庫管理
- 在庫予約・解放
- 入庫・出庫管理
- 在庫アラート・自動発注
- ロケーション管理（倉庫別）
- 在庫レポート

**技術スタック**:

- Jakarta EE 11
- PostgreSQL
- Redis (在庫キャッシュ)
- Apache Kafka (在庫イベント)

**ドメインモデル**:

```java
@Entity
@Table(name = "inventory")
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "product_id")
    private UUID productId;
    
    @Column(name = "warehouse_id")
    private UUID warehouseId;
    
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Integer totalQuantity;
    
    private Integer reorderPoint;
    private Integer maxStock;
    
    private LocalDateTime lastUpdated;
    
    // Virtual Threads での非同期在庫更新
    @Asynchronous
    public CompletableFuture<Void> updateInventoryAsync(
            InventoryUpdateEvent event) {
        return CompletableFuture.runAsync(() -> {
            // 在庫更新処理
            applyInventoryChange(event);
            publishInventoryUpdateEvent();
        });
    }
}

// Record ベースのイベント
public record InventoryUpdateEvent(
    UUID productId,
    UUID warehouseId,
    Integer quantityChange,
    InventoryOperation operation,
    String reason,
    LocalDateTime timestamp
) {}

public enum InventoryOperation {
    RESERVE, RELEASE, RESTOCK, ADJUSTMENT, SALE
}
```

**主要API**:

```java
@Path("/inventory")
@ApplicationScoped
public class InventoryResource {
    
    @GET
    @Path("/products/{productId}")
    public Response getInventory(@PathParam("productId") UUID productId) {
        var inventory = inventoryService.getAvailableInventory(productId);
        return Response.ok(inventory).build();
    }
    
    @POST
    @Path("/reserve")
    public Response reserveInventory(InventoryReservationRequest request) {
        try {
            var reservation = inventoryService.reserveInventory(
                request.productId(),
                request.quantity(),
                request.customerId()
            );
            return Response.ok(reservation).build();
        } catch (InsufficientInventoryException e) {
            return Response.status(409)
                .entity(new ErrorResponse("INSUFFICIENT_INVENTORY", e.getMessage()))
                .build();
        }
    }
    
    @POST
    @Path("/release")
    public Response releaseInventory(InventoryReleaseRequest request) {
        // 在庫解放処理
    }
}
```

### 6. Order/Sales Service

**責務**: 注文処理・販売管理

**主要機能**:

- 注文処理ワークフロー
- 注文状態管理
- 配送管理連携
- 注文キャンセル・返品処理
- 売上レポート
- 注文履歴管理

**技術スタック**:

- Jakarta EE 11
- PostgreSQL
- Apache Kafka (注文イベント)
- Saga Pattern (分散トランザクション)

**ドメインモデル**:

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
    
    @Embedded
    private OrderAmount amount;
    
    @Embedded
    private ShippingAddress shippingAddress;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Embedded
    private OrderTimestamps timestamps;
    
    // Saga Pattern による分散トランザクション
    @Transactional
    public OrderProcessResult processOrder() {
        // 1. 在庫予約
        // 2. 決済処理
        // 3. 配送手配
        // 4. 注文確定
        return new OrderProcessResult(this.id, OrderStatus.CONFIRMED);
    }
}

public record OrderAmount(
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal shipping,
    BigDecimal discount,
    BigDecimal total,
    String currency
) {}

public record OrderTimestamps(
    LocalDateTime orderedAt,
    LocalDateTime confirmedAt,
    LocalDateTime shippedAt,
    LocalDateTime deliveredAt
) {}

public enum OrderStatus {
    PENDING, CONFIRMED, PAID, SHIPPED, DELIVERED, CANCELLED, RETURNED
}
```

### 7. Payment/Cart Service

**責務**: ショッピングカート・決済処理

**主要機能**:

- ショッピングカート管理
- 決済処理（クレジットカード、電子マネー）
- 決済手数料計算
- 分割払い・後払い対応
- 決済状態管理
- 返金処理

**技術スタック**:

- Jakarta EE 11
- PostgreSQL
- Redis (カートセッション)
- Stripe/PayPal (決済ゲートウェイ)

**ドメインモデル**:

```java
@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "customer_id")
    private UUID customerId;
    
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> items;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    // Virtual Threads での非同期価格計算
    @Asynchronous
    public CompletableFuture<CartTotal> calculateTotalAsync() {
        return CompletableFuture.supplyAsync(() -> {
            return items.stream()
                .map(item -> item.calculateSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }).thenCompose(subtotal -> 
            taxService.calculateTaxAsync(subtotal)
                .thenApply(tax -> new CartTotal(subtotal, tax, subtotal.add(tax)))
        );
    }
}

public record CartTotal(
    BigDecimal subtotal,
    BigDecimal tax,
    BigDecimal total
) {}

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "order_id")
    private UUID orderId;
    
    @Embedded
    private PaymentAmount amount;
    
    @Enumerated(EnumType.STRING)
    private PaymentMethod method;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    
    private String transactionId;
    private String gatewayResponse;
    
    private LocalDateTime processedAt;
}

public enum PaymentMethod {
    CREDIT_CARD, DEBIT_CARD, PAYPAL, APPLE_PAY, GOOGLE_PAY, BANK_TRANSFER
}

public enum PaymentStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, REFUNDED, CANCELLED
}
```

### 8. Coupon/Discount Service

**責務**: クーポン・割引管理

**主要機能**:

- クーポン発行・管理
- 割引ルール設定
- プロモーション管理
- クーポン適用検証
- 使用履歴管理
- 期間限定セール

**技術スタック**:

- Jakarta EE 11
- Redis (高速アクセス)
- PostgreSQL (永続化)

**ドメインモデル**:

```java
@Entity
@Table(name = "coupons")
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true)
    private String code;
    
    private String name;
    private String description;
    
    @Embedded
    private DiscountRule discountRule;
    
    @Embedded
    private CouponValidityPeriod validityPeriod;
    
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean active;
    
    // ルールエンジンパターンで割引計算
    public DiscountResult calculateDiscount(OrderAmount orderAmount) {
        return discountRule.apply(orderAmount);
    }
}

public record DiscountRule(
    DiscountType type,
    BigDecimal value,
    BigDecimal minimumOrderAmount,
    Set<UUID> applicableProductIds,
    Set<String> applicableCategories
) {
    public DiscountResult apply(OrderAmount orderAmount) {
        // 割引計算ロジック
        return switch (type) {
            case PERCENTAGE -> applyPercentageDiscount(orderAmount);
            case FIXED_AMOUNT -> applyFixedAmountDiscount(orderAmount);
            case FREE_SHIPPING -> applyFreeShippingDiscount(orderAmount);
        };
    }
}

public enum DiscountType {
    PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
}
```

### 9. Points/Loyalty Service

**責務**: ポイント・ロイヤルティプログラム管理

**主要機能**:

- ポイント付与・消費
- ロイヤルティレベル管理
- ポイント有効期限管理
- ボーナスポイントキャンペーン
- ポイント履歴管理
- 会員ランク特典

**技術スタック**:

- Jakarta EE 11
- PostgreSQL
- Redis (ポイント残高キャッシュ)

### 10. AI Support Service

**責務**: AI対応カスタマーサポート

**主要機能**:

- チャットボット（Azure OpenAI GPT-4）
- 商品推薦エンジン
- FAQ自動応答
- 問い合わせ分類・ルーティング
- 感情分析
- 多言語対応

**技術スタック**:

- Jakarta EE 11
- Azure OpenAI Service
- MongoDB (会話履歴)
- Azure Cognitive Services

### 11. Frontend Service

**責務**: Webサイト・SPA配信

**主要機能**:

- レスポンシブWebデザイン
- PWA（Progressive Web App）
- SEO最適化
- パフォーマンス最適化
- A/Bテスト機能

**技術スタック**:

- React 18
- Next.js 14
- TypeScript
- Azure Static Web Apps

## データアーキテクチャ

### データベース設計

**PostgreSQL クラスター**:

- **Primary Database**: ユーザー、商品、注文、在庫
- **Read Replicas**: レポート、分析クエリ
- **Sharding**: 大容量データ（注文履歴、ログ）

**Redis クラスター**:

- **Session Store**: ユーザーセッション
- **Cache**: 商品情報、在庫情報
- **Queue**: 非同期処理タスク

**MongoDB**:

- **Document Store**: AI会話履歴、ログデータ
- **Time Series**: メトリクス、監視データ

### データ一貫性戦略

**Saga Pattern**: 分散トランザクション管理

```java
@ApplicationScoped
public class OrderProcessingSaga {
    
    @Inject
    private InventoryService inventoryService;
    
    @Inject
    private PaymentService paymentService;
    
    @Inject
    private ShippingService shippingService;
    
    // Virtual Threads による非同期 Saga 実行
    @Asynchronous
    public CompletableFuture<SagaResult> processOrderSaga(OrderCreatedEvent event) {
        return CompletableFuture
            .supplyAsync(() -> reserveInventory(event))
            .thenCompose(this::processPayment)
            .thenCompose(this::arrangeShipping)
            .thenCompose(this::confirmOrder)
            .exceptionally(this::handleSagaFailure);
    }
    
    private CompletableFuture<SagaStep> reserveInventory(OrderCreatedEvent event) {
        return inventoryService.reserveInventoryAsync(event.getOrderItems())
            .thenApply(result -> new SagaStep("INVENTORY_RESERVED", result))
            .exceptionally(ex -> new SagaStep("INVENTORY_FAILED", ex));
    }
}
```

## セキュリティ設計

### 認証・認可

**OAuth 2.0 / OpenID Connect フロー**:

1. クライアント認証（Client Credentials）
2. 認可コードフロー（Authorization Code Flow）
3. リフレッシュトークンローテーション
4. PKCE（Proof Key for Code Exchange）

**MicroProfile JWT構造**:

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "auth-service-key"
  },
  "payload": {
    "iss": "https://ski-equipment-shop.com",
    "sub": "user-uuid",
    "aud": "ski-equipment-shop",
    "exp": 1640995200,
    "iat": 1640991600,
    "groups": ["customer", "premium"],
    "permissions": ["read:profile", "write:cart", "read:orders"],
    "preferred_username": "user_12345678",
    "token_type": "access"
  }
}
```

**MicroProfile JWT標準**:

- Jakarta EE 11 Web Profile準拠
- `groups`クレーム：ユーザーロール（MicroProfile JWT標準）
- `permissions`クレーム：カスタム権限
- `preferred_username`クレーム：表示用ユーザー名
- RS256署名アルゴリズム使用

### セキュリティ対策

**OWASP Top 10 対策**:

- SQL インジェクション防御（Jakarta Persistence）
- XSS防御（CSP、入力サニタイズ）
- CSRF防御（SameSite Cookie、CSRFトークン）
- セキュリティヘッダー設定
- 入力値検証（Jakarta Validation）

**データ保護**:

- TLS 1.3（全通信暗号化）
- AES-256（機密データ暗号化）
- PII仮名化・マスキング
- GDPR 準拠（データ削除権等）

## APIデザイン

### RESTful API設計原則

**Richardson成熟度モデル レベル3準拠**:

- **Level 0**: HTTP使用
- **Level 1**: リソース指向URL設計
- **Level 2**: HTTP動詞の適切な使用
- **Level 3**: HATEOAS（Hypermedia as the Engine of Application State）

**API設計ガイドライン**:

```yaml
# OpenAPI 3.1 仕様例
openapi: 3.1.0
info:
  title: Ski Shop API
  version: 1.0.0
  description: スキー用品販売ショップ統合API

servers:
  - url: https://api.ski-shop.com/v1
    description: Production server
  - url: https://staging-api.ski-shop.com/v1
    description: Staging server

paths:
  /products:
    get:
      summary: 商品一覧取得
      parameters:
        - name: category
          in: query
          schema:
            type: string
          example: "skis"
        - name: page
          in: query
          schema:
            type: integer
            default: 0
        - name: size
          in: query
          schema:
            type: integer
            default: 20
            maximum: 100
      responses:
        '200':
          description: 商品一覧
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProductListResponse'
          links:
            nextPage:
              operationRef: '#/paths/~1products/get'
              parameters:
                page: '$response.body#/pagination/nextPage'

  /products/{productId}:
    get:
      summary: 商品詳細取得
      parameters:
        - name: productId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: 商品詳細
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ProductDetailResponse'
          links:
            addToCart:
              operationRef: '#/paths/~1cart~1items/post'
              parameters:
                productId: '$response.body#/id'

components:
  schemas:
    ProductListResponse:
      type: object
      properties:
        products:
          type: array
          items:
            $ref: '#/components/schemas/Product'
        pagination:
          $ref: '#/components/schemas/Pagination'
        _links:
          $ref: '#/components/schemas/Links'
```

### API バージョニング戦略

**セマンティックバージョニング採用**:

```java
// Jakarta REST によるバージョニング実装
@Path("/v1/products")
@ApplicationScoped
@ApiVersion("1.0")
public class ProductResourceV1 {
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "商品一覧取得 V1")
    public Response getProducts(
            @Parameter(description = "カテゴリフィルター") 
            @QueryParam("category") String category,
            @Parameter(description = "ページ番号") 
            @QueryParam("page") @DefaultValue("0") int page) {
        
        var products = productService.findProducts(category, page);
        var response = ProductListResponseV1.builder()
            .products(products)
            .addLink("self", buildSelfLink())
            .addLink("next", buildNextPageLink(page + 1))
            .build();
            
        return Response.ok(response).build();
    }
}

// API バージョン互換性管理
@ApplicationScoped
public class ApiVersionCompatibilityService {
    
    public <T> T convertResponse(Object sourceResponse, 
                                String targetVersion, 
                                Class<T> targetType) {
        return switch (targetVersion) {
            case "1.0" -> convertToV1(sourceResponse, targetType);
            case "2.0" -> convertToV2(sourceResponse, targetType);
            default -> throw new UnsupportedApiVersionException(targetVersion);
        };
    }
}
```

### エラーハンドリング標準

**RFC 7807 Problem Details準拠**:

```java
// Problem Details 実装
public record ApiProblem(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    Map<String, Object> extensions
) {
    
    public static ApiProblem builder() {
        return new ApiProblemBuilder();
    }
    
    // 標準的なエラーレスポンス
    public static ApiProblem validationError(List<ValidationError> errors) {
        return ApiProblem.builder()
            .type("https://api.ski-shop.com/problems/validation-error")
            .title("Validation Failed")
            .status(400)
            .detail("リクエストデータに検証エラーがあります")
            .extension("violations", errors)
            .build();
    }
    
    public static ApiProblem resourceNotFound(String resourceType, String id) {
        return ApiProblem.builder()
            .type("https://api.ski-shop.com/problems/resource-not-found")
            .title("Resource Not Found")
            .status(404)
            .detail(String.format("%s with id %s was not found", resourceType, id))
            .build();
    }
}

// 例外ハンドラー
@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {
    
    @Override
    public Response toResponse(Exception exception) {
        return switch (exception) {
            case ValidationException ve -> 
                Response.status(400)
                    .entity(ApiProblem.validationError(ve.getViolations()))
                    .type("application/problem+json")
                    .build();
                    
            case EntityNotFoundException enfe ->
                Response.status(404)
                    .entity(ApiProblem.resourceNotFound(enfe.getEntityType(), enfe.getId()))
                    .type("application/problem+json")
                    .build();
                    
            default ->
                Response.status(500)
                    .entity(ApiProblem.internalServerError())
                    .type("application/problem+json")
                    .build();
        };
    }
}
```

### レート制限・スロットリング

```java
// Jakarta CDI によるレート制限実装
@Interceptor
@RateLimit
@Priority(Interceptor.Priority.APPLICATION)
public class RateLimitInterceptor {
    
    @Inject
    private RateLimitService rateLimitService;
    
    @AroundInvoke
    public Object checkRateLimit(InvocationContext context) throws Exception {
        var method = context.getMethod();
        var rateLimitAnnotation = method.getAnnotation(RateLimit.class);
        
        var clientId = extractClientId(context);
        var key = generateRateLimitKey(clientId, method);
        
        if (!rateLimitService.isAllowed(key, 
                rateLimitAnnotation.requests(), 
                rateLimitAnnotation.period())) {
            throw new RateLimitExceededException();
        }
        
        return context.proceed();
    }
}

// 使用例
@Path("/products")
@ApplicationScoped
public class ProductResource {
    
    @GET
    @RateLimit(requests = 100, period = "1m")
    public Response getProducts() {
        // 実装
    }
    
    @POST
    @RateLimit(requests = 10, period = "1m")
    @Authenticated
    public Response createProduct() {
        // 実装
    }
}
```

## インフラ・デザイン

### ローカル開発環境（Docker Compose）

**開発環境構成**:

```yaml
# docker-compose.yml
version: '3.9'

services:
  # API Gateway
  kong:
    image: kong:3.7
    environment:
      KONG_DATABASE: postgres
      KONG_PG_HOST: kong-database
      KONG_PG_PASSWORD: kong-password
      KONG_PROXY_ACCESS_LOG: /dev/stdout
      KONG_ADMIN_ACCESS_LOG: /dev/stdout
      KONG_PROXY_ERROR_LOG: /dev/stderr
      KONG_ADMIN_ERROR_LOG: /dev/stderr
      KONG_ADMIN_LISTEN: 0.0.0.0:8001
    ports:
      - "8000:8000"
      - "8001:8001"
    depends_on:
      - kong-database

  kong-database:
    image: postgres:16
    environment:
      POSTGRES_DB: kong
      POSTGRES_USER: kong
      POSTGRES_PASSWORD: kong-password
    volumes:
      - kong_data:/var/lib/postgresql/data

  # マイクロサービス
  user-service:
    build:
      context: ./user-management-service
      dockerfile: Dockerfile.dev
    environment:
      - DATASOURCE_URL=jdbc:postgresql://postgres:5432/ski_shop_users
      - DATASOURCE_USERNAME=ski_shop_user
      - DATASOURCE_PASSWORD=password
      - REDIS_URL=redis://redis:6379
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
    ports:
      - "8080:8080"
    depends_on:
      - postgres
      - redis
      - kafka
    volumes:
      - ./user-management-service/src:/app/src
      - ./user-management-service/target:/app/target

  product-service:
    build:
      context: ./product-catalog-service
      dockerfile: Dockerfile.dev
    environment:
      - DATASOURCE_URL=jdbc:postgresql://postgres:5432/ski_shop_products
      - ELASTICSEARCH_URL=http://elasticsearch:9200
      - REDIS_URL=redis://redis:6379
    ports:
      - "8081:8080"
    depends_on:
      - postgres
      - elasticsearch
      - redis

  # データベース
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: ski_shop
      POSTGRES_USER: ski_shop_user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-databases.sql:/docker-entrypoint-initdb.d/init-databases.sql

  # Redis
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  # Elasticsearch
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data

  # Apache Kafka
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  # 監視
  prometheus:
    image: prom/prometheus:v2.47.0
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus

  grafana:
    image: grafana/grafana:10.2.0
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards

volumes:
  kong_data:
  postgres_data:
  redis_data:
  elasticsearch_data:
  prometheus_data:
  grafana_data:
```

**開発用 Dockerfile**:

```dockerfile
# Dockerfile.dev
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

# Java 21 Virtual Threads 最適化
ENV JAVA_OPTS="-XX:+UseZGC \
               -XX:+UnlockExperimentalVMOptions \
               --enable-preview \
               -XX:+UseStringDeduplication \
               -Xms512m -Xmx1g"

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# 開発時のホットリロード対応
COPY --from=build /app/target/classes ./classes

EXPOSE 8080

# Jakarta EE アプリケーション起動
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 本番環境（Azure Container Apps）

**Azure Container Apps Environment構成**:

```yaml
# azure-container-apps.bicep
param environmentName string = 'ski-shop-env'
param location string = resourceGroup().location

// Container Apps Environment
resource containerAppsEnvironment 'Microsoft.App/managedEnvironments@2023-05-01' = {
  name: environmentName
  location: location
  properties: {
    vnetConfiguration: {
      infrastructureSubnetId: subnet.id
    }
    workloadProfiles: [
      {
        name: 'Consumption'
        workloadProfileType: 'Consumption'
      }
      {
        name: 'D4'
        workloadProfileType: 'D4'
        minimumCount: 1
        maximumCount: 10
      }
    ]
  }
}

// User Management Service
resource userManagementApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: 'user-management-service'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnvironment.id
    workloadProfileName: 'D4'
    configuration: {
      activeRevisionsMode: 'Multiple'
      ingress: {
        external: false
        targetPort: 8080
        traffic: [
          {
            revisionName: 'user-management-service--latest'
            weight: 100
          }
        ]
      }
      secrets: [
        {
          name: 'database-connection-string'
          value: 'postgresql://...'
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'user-management'
          image: 'skiShopRegistry.azurecr.io/user-management:latest'
          env: [
            {
              name: 'DATASOURCE_URL'
              secretRef: 'database-connection-string'
            }
            {
              name: 'JAVA_OPTS'
              value: '-XX:+UseZGC -XX:+UnlockExperimentalVMOptions --enable-preview'
            }
          ]
          resources: {
            cpu: '1.0'
            memory: '2Gi'
          }
          probes: [
            {
              type: 'Liveness'
              httpGet: {
                path: '/health/live'
                port: 8080
              }
              initialDelaySeconds: 30
              periodSeconds: 10
            }
            {
              type: 'Readiness'
              httpGet: {
                path: '/health/ready'
                port: 8080
              }
              initialDelaySeconds: 5
              periodSeconds: 5
            }
          ]
        }
      ]
      scale: {
        minReplicas: 2
        maxReplicas: 20
        rules: [
          {
            name: 'http-scaling'
            http: {
              metadata: {
                concurrentRequests: '100'
              }
            }
          }
          {
            name: 'cpu-scaling'
            custom: {
              type: 'cpu'
              metadata: {
                type: 'Utilization'
                value: '70'
              }
            }
          }
        ]
      }
    }
  }
}
```

**Infrastructure as Code (Bicep)**:

```bicep
// main.bicep
targetScope = 'resourceGroup'

param location string = resourceGroup().location
param environment string = 'production'

// Azure Database for PostgreSQL
module database 'modules/database.bicep' = {
  name: 'database'
  params: {
    location: location
    environment: environment
    serverName: 'ski-shop-postgres-${environment}'
    administratorLogin: 'skiShopadmin'
    databases: [
      'ski_shop_users'
      'ski_shop_products'
      'ski_shop_orders'
      'ski_shop_inventory'
    ]
  }
}

// Azure Cache for Redis
module redis 'modules/redis.bicep' = {
  name: 'redis'
  params: {
    location: location
    environment: environment
    redisCacheName: 'ski-shop-redis-${environment}'
    sku: {
      name: 'Standard'
      family: 'C'
      capacity: 2
    }
  }
}

// Azure Service Bus
module serviceBus 'modules/servicebus.bicep' = {
  name: 'serviceBus'
  params: {
    location: location
    environment: environment
    namespaceName: 'ski-shop-servicebus-${environment}'
    topics: [
      'order-events'
      'inventory-events'
      'user-events'
    ]
  }
}

// Container Registry
module containerRegistry 'modules/acr.bicep' = {
  name: 'containerRegistry'
  params: {
    location: location
    registryName: 'skiShopRegistry${environment}'
  }
}
```

## 非機能要件への対応

### パフォーマンス要件

**レスポンス時間目標**:

| 機能 | 目標レスポンス時間 | 測定方法 |
|------|------------------|---------|
| 商品検索 | < 500ms (95%ile) | APM監視 |
| 商品詳細表示 | < 300ms (95%ile) | APM監視 |
| カート操作 | < 200ms (95%ile) | APM監視 |
| 注文処理 | < 2秒 (平均) | APM監視 |
| ユーザー認証 | < 1秒 (95%ile) | APM監視 |

**スループット要件**:

```java
// パフォーマンステスト実装例
@ApplicationScoped
public class PerformanceTestService {
    
    // Virtual Threads による高スループット実現
    private final Executor virtualThreadExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
    
    @Asynchronous
    public CompletableFuture<List<ProductSearchResult>> 
            performBulkSearch(List<SearchRequest> requests) {
        
        var futures = requests.stream()
            .map(request -> CompletableFuture.supplyAsync(
                () -> productSearchService.search(request),
                virtualThreadExecutor
            ))
            .toList();
            
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
}
```

### スケーラビリティ対応

**水平スケーリング戦略**:

```yaml
# Azure Container Apps Auto Scaling
resources:
  user-management-service:
    scaling:
      minReplicas: 2
      maxReplicas: 50
      rules:
        - name: http-requests
          type: http
          metadata:
            concurrentRequests: "100"
        - name: cpu-utilization
          type: cpu
          metadata:
            type: "Utilization"
            value: "70"
        - name: memory-utilization
          type: memory
          metadata:
            type: "Utilization"
            value: "80"

  product-catalog-service:
    scaling:
      minReplicas: 3
      maxReplicas: 100
      rules:
        - name: search-requests
          type: http
          metadata:
            concurrentRequests: "200"
```

### 可用性・信頼性

**高可用性アーキテクチャ**:

```java
// Circuit Breaker パターン実装
@ApplicationScoped
public class ProductService {
    
    @Inject
    @CircuitBreaker(
        requestVolumeThreshold = 20,
        failureRatio = 0.5,
        delay = 5000,
        successThreshold = 3
    )
    @Retry(maxRetries = 3, delay = 1000)
    @Timeout(value = 3000)
    public Product getProduct(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    // フォールバック実装
    @Fallback
    public Product getProductFallback(UUID productId) {
        // キャッシュから取得またはデフォルト値
        return productCacheService.getFromCache(productId)
            .orElse(Product.createUnavailable(productId));
    }
}
```

### セキュリティ要件対応

**多層防御戦略**:

```java
// Jakarta Security実装
@ApplicationScoped
@DeclareRoles({"user", "admin", "premium"})
public class SecurityConfiguration {
    
    @Produces
    @ApplicationScoped
    public DatabaseIdentityStore createIdentityStore() {
        return DatabaseIdentityStore.builder()
            .dataSource(dataSource)
            .callerQuery("SELECT password FROM users WHERE email = ?")
            .groupsQuery("SELECT role FROM user_roles WHERE email = ?")
            .hashAlgorithm(Pbkdf2PasswordHash.class)
            .priority(10)
            .build();
    }
    
    @Produces
    @ApplicationScoped
    public RememberMeIdentityStore createRememberMeStore() {
        return new JWTRememberMeIdentityStore();
    }
}

// MicroProfile JWT認可制御
@Path("/admin")
@RolesAllowed("admin")
public class AdminResource {
    
    @Inject
    private JsonWebToken jwt;  // MicroProfile JWTトークン
    
    @GET
    @Path("/users")
    @RolesAllowed({"admin", "support"})
    public Response getAllUsers() {
        // MicroProfile JWTからユーザー情報を取得
        String userId = jwt.getSubject();
        Set<String> groups = jwt.getGroups();
        
        // 管理者のみアクセス可能
        return Response.ok().build();
    }
    
    @GET
    @Path("/profile")
    public Response getUserProfile() {
        // @Claimアノテーションでクレーム値を直接注入
        String username = jwt.claim("preferred_username").orElse("unknown");
        
        return Response.ok()
            .entity(Map.of("user", username, "roles", jwt.getGroups()))
            .build();
    }
}
```

## インフラストラクチャ

### Azure クラウドアーキテクチャ

```yaml
# Kubernetes クラスター構成
apiVersion: v1
kind: Namespace
metadata:
  name: ski-shop

---
# User Management Service Deployment
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-management-service
  namespace: ski-shop
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-management-service
  template:
    metadata:
      labels:
        app: user-management-service
    spec:
      containers:
      - name: user-management
        image: skiShop/user-management:latest
        ports:
        - containerPort: 8080
        env:
        - name: DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: database-secret
              key: url
        - name: JAVA_OPTS
          value: "-XX:+UseZGC -XX:+UnlockExperimentalVMOptions --enable-preview"
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /health/live
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /health/ready
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
```

### 監視・可観測性

**メトリクス収集**:

- MicroProfile Metrics 5.1
- Prometheus + Grafana
- Custom Business Metrics

**分散トレーシング**:

- MicroProfile OpenTelemetry 2.0
- Jaeger
- Azure Application Insights

**ログ管理**:

- Structured Logging（JSON）
- ELK Stack（Elasticsearch, Logstash, Kibana）
- Azure Log Analytics

## 運用・監視

### SRE（Site Reliability Engineering）

**SLI/SLO設定**:

```yaml
# Service Level Indicators/Objectives
services:
  api-gateway:
    availability:
      sli: "success_rate"
      slo: "99.9%"
    latency:
      sli: "p95_response_time"
      slo: "< 500ms"
  
  user-management:
    availability:
      sli: "success_rate"  
      slo: "99.5%"
    latency:
      sli: "p99_response_time"
      slo: "< 1000ms"
```

**アラート設定**:

```yaml
# Prometheus Alert Rules
groups:
- name: ski-shop-alerts
  rules:
  - alert: HighErrorRate
    expr: rate(http_requests_total{status=~"5.."}[5m]) > 0.1
    for: 2m
    labels:
      severity: critical
    annotations:
      summary: "High error rate detected"
      
  - alert: HighLatency
    expr: histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m])) > 0.5
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "High latency detected"
```

### 災害復旧

**バックアップ戦略**:

- **PostgreSQL**: Point-in-time Recovery（PITR）
- **Redis**: AOF + RDB
- **MongoDB**: Replica Set + Sharding

**RTO/RPO目標**:

- **RTO**: 4時間以内
- **RPO**: 15分以内

## 開発・運用プロセス

### アジャイル開発プロセス

**スクラム採用**:

```yaml
# スプリント構成
sprint_duration: 2週間
team_structure:
  - product_owner: 1名
  - scrum_master: 1名
  - developers: 6-8名
  - qa_engineers: 2名
  - devops_engineers: 2名

ceremonies:
  daily_standup:
    duration: 15分
    time: "09:00"
    
  sprint_planning:
    duration: 4時間
    participants: "全チーム"
    
  sprint_review:
    duration: 2時間
    stakeholders: "PO + ステークホルダー"
    
  retrospective:
    duration: 1.5時間
    participants: "開発チーム"
```

**Definition of Done (DoD)**:

```markdown
## Definition of Done チェックリスト

### コード品質
- [ ] コードレビュー完了（2名以上承認）
- [ ] 単体テストカバレッジ 80%以上
- [ ] 統合テスト実行・パス
- [ ] SonarQube品質ゲートパス
- [ ] セキュリティスキャン実行・問題なし

### ドキュメント
- [ ] API仕様書更新
- [ ] README更新
- [ ] CHANGELOG更新
- [ ] 運用手順書更新（必要に応じて）

### テスト・品質保証
- [ ] 機能テスト完了
- [ ] 性能テスト実行（必要に応じて）
- [ ] アクセシビリティテスト実行
- [ ] ブラウザ互換性確認

### デプロイメント
- [ ] ステージング環境デプロイ・確認
- [ ] 本番リリース計画承認
- [ ] ロールバック手順確認
- [ ] 監視・アラート設定確認
```

### GitFlow ワークフロー

```mermaid
gitGraph
    commit id: "Initial"
    branch develop
    checkout develop
    commit id: "Feature A"
    branch feature/user-auth
    checkout feature/user-auth
    commit id: "Auth impl"
    commit id: "Auth tests"
    checkout develop
    merge feature/user-auth
    branch release/v1.1.0
    checkout release/v1.1.0
    commit id: "Release prep"
    checkout main
    merge release/v1.1.0
    tag v1.1.0
    checkout develop
    merge main
    branch hotfix/security-fix
    checkout hotfix/security-fix
    commit id: "Security patch"
    checkout main
    merge hotfix/security-fix
    tag v1.1.1
    checkout develop
    merge main
```

**ブランチ戦略**:

```bash
# ブランチ命名規則
main                    # 本番リリース
develop                 # 開発統合
feature/JIRA-123-*     # 機能開発
release/v1.2.0         # リリース準備
hotfix/critical-fix    # 緊急修正

# プルリクエストテンプレート
## 変更内容
- [ ] 新機能追加
- [ ] バグ修正
- [ ] パフォーマンス改善
- [ ] リファクタリング

## 影響範囲
- [ ] フロントエンド
- [ ] バックエンドAPI
- [ ] データベース
- [ ] インフラ

## テスト実行
- [ ] 単体テスト: npm test
- [ ] 統合テスト: npm run test:integration
- [ ] E2Eテスト: npm run test:e2e

## レビューポイント
- セキュリティ影響はないか？
- パフォーマンスへの影響は？
- 後方互換性は保たれているか？
```

### 継続的インテグレーション・デリバリー

**品質ゲート定義**:

```yaml
# .github/workflows/quality-gates.yml
name: Quality Gates

on:
  pull_request:
    branches: [main, develop]

jobs:
  code-quality:
    runs-on: ubuntu-latest
    steps:
      - name: Unit Tests
        run: mvn test
        env:
          MINIMUM_COVERAGE: 80
          
      - name: Integration Tests
        run: mvn verify -P integration-tests
        
      - name: SonarQube Analysis
        run: |
          mvn sonar:sonar \
            -Dsonar.projectKey=ski-shop \
            -Dsonar.qualitygate.wait=true
            
      - name: Security Scan
        run: mvn org.owasp:dependency-check-maven:check
        
      - name: Performance Tests
        run: |
          mvn gatling:test -P performance-tests
          if [ $? -ne 0 ]; then
            echo "Performance regression detected"
            exit 1
          fi

  deployment-readiness:
    needs: code-quality
    runs-on: ubuntu-latest
    steps:
      - name: Database Migration Test
        run: mvn flyway:migrate -P test-db
        
      - name: Container Build Test
        run: |
          docker build -t test-image .
          docker run --rm test-image java -version
          
      - name: Smoke Tests
        run: |
          docker-compose -f docker-compose.test.yml up -d
          npm run test:smoke
          docker-compose -f docker-compose.test.yml down
```

### 運用監視プロセス

**SRE（Site Reliability Engineering）実践**:

```java
// Error Budget 実装
@ApplicationScoped
public class ErrorBudgetService {
    
    // SLO: 99.9% availability = 0.1% error budget
    private static final double SLO_TARGET = 0.999;
    private static final double ERROR_BUDGET = 1.0 - SLO_TARGET;
    
    @Inject
    private MetricsService metricsService;
    
    public ErrorBudgetStatus calculateErrorBudget(String service, Duration period) {
        var totalRequests = metricsService.getTotalRequests(service, period);
        var errorRequests = metricsService.getErrorRequests(service, period);
        
        var actualErrorRate = (double) errorRequests / totalRequests;
        var budgetUsed = actualErrorRate / ERROR_BUDGET;
        
        return new ErrorBudgetStatus(
            service,
            budgetUsed,
            budgetUsed > 1.0 ? "EXHAUSTED" : "HEALTHY",
            calculateTimeToExhaustion(service, actualErrorRate)
        );
    }
}

// インシデント対応プロセス
@ApplicationScoped
public class IncidentResponseService {
    
    @EventObserver
    public void handleCriticalAlert(CriticalAlertEvent event) {
        // 1. 即座にオンコールエンジニアに通知
        notificationService.sendUrgentAlert(event);
        
        // 2. インシデント作成
        var incident = incidentService.createIncident(
            event.getService(),
            Severity.CRITICAL,
            event.getDescription()
        );
        
        // 3. エスカレーション開始
        escalationService.startEscalation(incident);
        
        // 4. ステータスページ更新
        statusPageService.updateStatus(
            event.getService(),
            Status.DEGRADED
        );
    }
}
```

## リスク管理

### 技術リスク

**リスク分析マトリックス**:

| リスク | 確率 | 影響度 | リスクレベル | 対策 |
|--------|------|--------|-------------|------|
| データベース障害 | 中 | 高 | 高 | 冗長構成、自動フェイルオーバー |
| セキュリティ侵害 | 低 | 極高 | 高 | 多層防御、定期監査 |
| 性能劣化 | 中 | 中 | 中 | 継続監視、自動スケーリング |
| 第三者API障害 | 高 | 中 | 中 | Circuit Breaker、フォールバック |
| コード品質問題 | 中 | 低 | 低 | 自動テスト、コードレビュー |

**リスク対策実装**:

```java
// Disaster Recovery 実装
@ApplicationScoped
public class DisasterRecoveryService {
    
    @Inject
    private BackupService backupService;
    
    @Inject
    private ReplicationService replicationService;
    
    // 自動バックアップスケジュール
    @Schedule(hour = "2", minute = "0", persistent = false)
    public void performDailyBackup() {
        try {
            var backupResult = backupService.createFullBackup();
            validateBackupIntegrity(backupResult);
            
            // 異なるリージョンへのレプリケーション
            replicationService.replicateToSecondaryRegion(backupResult);
            
        } catch (BackupException e) {
            alertService.sendCriticalAlert(
                "Daily backup failed: " + e.getMessage()
            );
        }
    }
    
    // 障害時自動復旧
    @EventObserver
    public void handleDatabaseFailure(DatabaseFailureEvent event) {
        if (event.isPrimary()) {
            // プライマリDB障害時は自動フェイルオーバー
            failoverService.promoteSecondaryToPrimary();
            
            // DNS更新で接続先変更
            dnsService.updateDatabaseEndpoint();
        }
    }
}
```

### ビジネスリスク

**ビジネス継続性計画（BCP）**:

```yaml
# BCP設定
business_continuity:
  rto_targets:
    critical_services:
      - user_authentication: "15分"
      - order_processing: "30分"
      - payment_processing: "15分"
    
    non_critical_services:
      - recommendation_engine: "4時間"
      - analytics_service: "24時間"
      - reporting_service: "24時間"

  communication_plan:
    internal:
      - incident_commander: "CTO"
      - technical_lead: "Lead Engineer"
      - business_stakeholder: "Product Manager"
    
    external:
      - customer_notification: "ステータスページ + メール"
      - partner_notification: "専用チャネル"
      - media_response: "広報担当"

  recovery_procedures:
    database_recovery:
      steps:
        1. "セカンダリDBをプライマリに昇格"
        2. "アプリケーション接続先変更"
        3. "データ整合性確認"
        4. "サービス再開"
      
    service_recovery:
      steps:
        1. "健全なインスタンスへトラフィック集約"
        2. "問題インスタンス特定・隔離"
        3. "新インスタンス起動"
        4. "段階的トラフィック復旧"
```

### セキュリティリスク

**脅威モデリング**:

```java
// セキュリティ監視実装
@ApplicationScoped
public class SecurityMonitoringService {
    
    @Inject
    private AuditLogService auditLogService;
    
    // 異常行動検知
    @EventObserver
    public void detectAnomalousActivity(UserActivityEvent event) {
        var user = event.getUser();
        var activity = event.getActivity();
        
        // 異常なログイン試行
        if (isAnomalousLogin(user, activity)) {
            securityAlertService.raiseAlert(
                SecurityAlertType.SUSPICIOUS_LOGIN,
                user.getId(),
                activity.getDetails()
            );
            
            // アカウント一時ロック
            userSecurityService.temporaryLock(user.getId());
        }
        
        // 異常な購入パターン
        if (isAnomalousPurchase(user, activity)) {
            fraudDetectionService.flagForReview(
                user.getId(),
                activity.getOrderId()
            );
        }
    }
    
    // セキュリティメトリクス収集
    @Schedule(minute = "*/5")
    public void collectSecurityMetrics() {
        var metrics = SecurityMetrics.builder()
            .failedLogins(getFailedLoginCount())
            .blockedIPs(getBlockedIPCount())
            .suspiciousActivities(getSuspiciousActivityCount())
            .build();
            
        metricsService.record(metrics);
        
        // 閾値超過時アラート
        if (metrics.failedLogins() > FAILED_LOGIN_THRESHOLD) {
            alertService.sendSecurityAlert(
                "Unusual number of failed logins detected"
            );
        }
    }
}
```

### 運用リスク

**変更管理プロセス**:

```yaml
# 変更管理ワークフロー
change_management:
  categories:
    emergency:
      approval_required: false
      notification: "事後報告"
      examples: ["セキュリティパッチ", "重大障害対応"]
    
    normal:
      approval_required: true
      approvers: ["Tech Lead", "Product Owner"]
      notice_period: "24時間前"
      
    major:
      approval_required: true
      approvers: ["CTO", "Product Manager", "Operations Manager"]
      notice_period: "1週間前"
      examples: ["アーキテクチャ変更", "データベース移行"]

  rollback_criteria:
    automatic:
      - "エラー率 > 5%"
      - "レスポンス時間 > 3秒"
      - "ヘルスチェック失敗"
    
    manual:
      - "ビジネス機能停止"
      - "データ整合性問題"
      - "セキュリティ脆弱性発見"
```

## 開発・デプロイメント

### CI/CD パイプライン

**GitHub Actions ワークフロー**:

```yaml
name: Ski Shop CI/CD

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    
    - name: Set up Java 21
      uses: actions/setup-java@v4
      with:
        java-version: '21'
        distribution: 'temurin'
        
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        
    - name: Run Tests
      run: |
        mvn clean test
        mvn verify -P integration-tests
        
    - name: SonarQube Analysis
      run: mvn sonar:sonar
      
    - name: Security Scan
      run: mvn org.owasp:dependency-check-maven:check

  build-and-deploy:
    needs: test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
    - name: Build Docker Image
      run: |
        docker build -t skiShop/user-management:${{ github.sha }} .
        
    - name: Push to Azure Container Registry
      run: |
        az acr login --name skipShopRegistry
        docker push skiShopRegistry.azurecr.io/user-management:${{ github.sha }}
        
    - name: Deploy to AKS
      run: |
        az aks get-credentials --resource-group ski-shop-rg --name ski-shop-aks
        kubectl set image deployment/user-management-service \
          user-management=skiShopRegistry.azurecr.io/user-management:${{ github.sha }}
```

### テスト戦略

**Arquillian 統合テスト**:

```java
@RunWith(Arquillian.class)
public class UserServiceIntegrationTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(UserService.class)
            .addClass(UserRepository.class)
            .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    
    @Inject
    private UserService userService;
    
    @Test
    @UsingDataSet("users.yml")
    @ShouldMatchDataSet("expected-users.yml")
    public void testCreateUser() {
        var userRequest = new CreateUserRequest(
            "test@example.com",
            "Test",
            "User"
        );
        
        var createdUser = userService.createUser(userRequest);
        
        assertThat(createdUser.email()).isEqualTo("test@example.com");
        assertThat(createdUser.id()).isNotNull();
    }
}
```

### パフォーマンス最適化

**Virtual Threads 活用**:

```java
@ApplicationScoped
public class InventoryService {
    
    // Virtual Threads での高並行処理
    @Asynchronous
    public CompletableFuture<List<InventoryStatus>> checkInventoryBatch(
            List<UUID> productIds) {
        
        return CompletableFuture.supplyAsync(() -> {
            // Virtual Threads で並列処理
            return productIds.parallelStream()
                .map(this::checkInventoryStatus)
                .collect(Collectors.toList());
        }, virtualThreadExecutor);
    }
    
    private final Executor virtualThreadExecutor = 
        Executors.newVirtualThreadPerTaskExecutor();
}
```

**JVM最適化設定**:

```bash
# Java 21 最適化設定
JAVA_OPTS="-XX:+UseZGC \
           -XX:+UnlockExperimentalVMOptions \
           --enable-preview \
           -XX:+UseStringDeduplication \
           -XX:MaxGCPauseMillis=100 \
           -Xms2g -Xmx4g"
```

## まとめ

本設計書では、Jakarta EE 11 と Java 21 LTS を基盤とした現代的なエンタープライズアーキテクチャでスキー用品販売ショップサイトを構築する包括的な設計を提示しました。

### 主要な技術的特徴

1. **Virtual Threads活用**: 高並行性と低リソース消費を実現
2. **Record クラス**: 不変データ構造による安全性向上
3. **Pattern Matching**: 可読性の高いビジネスロジック実装
4. **Jakarta EE 11**: 標準準拠でベンダーロックイン回避
5. **マイクロサービス**: 独立したデプロイ・スケーリング
6. **クラウドネイティブ**: Kubernetes環境での運用最適化

### ビジネス価値

- **拡張性**: ビジネス成長に合わせたスケーリング
- **保守性**: モダンなアーキテクチャパターンによる長期保守
- **可用性**: 99.9%の高可用性実現
- **セキュリティ**: エンタープライズレベルのセキュリティ対策
- **開発効率**: Jakarta EE標準による生産性向上

この設計により、競争力のあるスキー用品ECサイトの構築と継続的な価値提供が可能となります。

---

**文書バージョン**: 1.0  
**作成日**: 2025年7月24日  
**次回レビュー**: 2025年10月24日
