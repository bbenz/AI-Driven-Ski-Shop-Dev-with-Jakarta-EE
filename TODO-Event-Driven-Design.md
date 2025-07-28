# Event-Driven Architecture 設計書

## 概要

本設計書では、現在のProduct CatalogサービスとInventory Managementサービス間のデータ重複問題を、Event-Driven Architectureで解決する方法を詳述します。

## 現在の問題点

### データ重複の問題

```sql
-- Product Catalog Service
products (id, sku, name, category, brand, price, specifications...)

-- Inventory Management Service (重複データ)
equipment (product_id, sku, name, category, brand, daily_rate, description...)
```

## Event-Driven Architecture 設計

### 1. アーキテクチャ概要図

```text
┌─────────────────────┐     Events     ┌─────────────────────┐
│ Product Catalog     │─────────────→│ Message Broker      │
│ Service             │               │ (Apache Kafka)      │
│ (Source of Truth)   │               │                     │
└─────────────────────┘               └─────────────────────┘
                                                │
                                                │ Subscribe
                                                ▼
                                    ┌─────────────────────┐
                                    │ Inventory Mgmt      │
                                    │ Service             │
                                    │ (Event Consumer)    │
                                    └─────────────────────┘
```

### 2. データ責任分離

| サービス | 責任 | 保持データ |
|---------|------|-----------|
| **Product Catalog** | 商品マスタ管理 | ・商品情報（名前、SKU、価格）<br>・カテゴリ・ブランド情報<br>・商品仕様・画像 |
| **Inventory Management** | 在庫管理専門 | ・在庫数・場所・状態<br>・レンタル料金（ビジネスロジック）<br>・予約・移動履歴 |

## 3. イベント設計

### 3.1 イベントタイプ定義

```java
// 基底イベントクラス
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProductCreatedEvent.class, name = "PRODUCT_CREATED"),
    @JsonSubTypes.Type(value = ProductUpdatedEvent.class, name = "PRODUCT_UPDATED"),
    @JsonSubTypes.Type(value = ProductDeletedEvent.class, name = "PRODUCT_DELETED"),
    @JsonSubTypes.Type(value = ProductActivatedEvent.class, name = "PRODUCT_ACTIVATED"),
    @JsonSubTypes.Type(value = ProductDeactivatedEvent.class, name = "PRODUCT_DEACTIVATED")
})
public abstract class ProductEvent {
    public String eventId = UUID.randomUUID().toString();
    public String eventType;
    public LocalDateTime timestamp = LocalDateTime.now();
    public String aggregateId; // Product ID
    public int version; // Event version for ordering
}
```

### 3.2 具体的なイベントクラス

```java
// 商品作成イベント
public class ProductCreatedEvent extends ProductEvent {
    public UUID productId;
    public String sku;
    public String name;
    public String categoryName;
    public String brandName;
    public String equipmentType; // SKI_BOARD, BOOT, HELMET, etc.
    public String sizeRange;
    public String difficultyLevel;
    public BigDecimal basePrice;
    public String description;
    public String imageUrl;
    public boolean isRentalAvailable;
    public boolean isActive;
    
    public ProductCreatedEvent(Product product) {
        this.eventType = "PRODUCT_CREATED";
        this.aggregateId = product.id.toString();
        this.productId = product.id;
        this.sku = product.sku;
        this.name = product.name;
        this.categoryName = product.category.name;
        this.brandName = product.brand.name;
        this.equipmentType = mapToEquipmentType(product.skiType);
        this.sizeRange = buildSizeRange(product);
        this.difficultyLevel = product.difficultyLevel.name();
        this.basePrice = product.basePrice;
        this.description = product.description;
        this.imageUrl = extractMainImageUrl(product);
        this.isRentalAvailable = determineRentalAvailability(product);
        this.isActive = product.isActive;
    }
    
    // Helper methods
    private String mapToEquipmentType(SkiType skiType) {
        // Product Catalog の SkiType を Inventory の EquipmentType にマッピング
        switch (skiType) {
            case SKI_BOARD: return "SKI_BOARD";
            case BINDING: return "BINDING";
            case POLE: return "POLE";
            case BOOT: return "BOOT";
            case HELMET: return "HELMET";
            case PROTECTOR: return "PROTECTOR";
            case WEAR: return "WEAR";
            case GOGGLE: return "GOGGLE";
            case GLOVE: return "GLOVE";
            case BAG: return "BAG";
            case WAX: return "WAX";
            case TUNING: return "TUNING";
            default: return "OTHER";
        }
    }
    
    private String buildSizeRange(Product product) {
        // 商品の仕様から適切なサイズ範囲を構築
        if (product.length != null) {
            return product.length;
        }
        // カテゴリに基づいてデフォルトサイズ範囲を設定
        return switch (product.category.path) {
            case "/ski-board" -> "150-190cm";
            case "/boot" -> "22.0-30.0cm";
            case "/helmet", "/goggle", "/glove", "/wear" -> "S-XL";
            case "/pole" -> "100-140cm";
            default -> "ONE_SIZE";
        };
    }
    
    private String extractMainImageUrl(Product product) {
        // 商品画像からメイン画像のURLを取得
        if (product.imageUrls != null && !product.imageUrls.isEmpty()) {
            return product.imageUrls.get(0);
        }
        return "/images/default-product.jpg";
    }
    
    private boolean determineRentalAvailability(Product product) {
        // ワックスやチューンナップ用品はレンタル対象外
        return !product.category.path.contains("/wax") && 
               !product.category.path.contains("/tuning");
    }
}

// 商品更新イベント
public class ProductUpdatedEvent extends ProductEvent {
    public UUID productId;
    public String sku;
    public String name;
    public String categoryName;
    public String brandName;
    public BigDecimal basePrice;
    public String description;
    public String imageUrl;
    public boolean isActive;
    public Map<String, Object> changedFields; // 変更されたフィールドのみ
    
    public ProductUpdatedEvent(Product oldProduct, Product newProduct) {
        this.eventType = "PRODUCT_UPDATED";
        this.aggregateId = newProduct.id.toString();
        this.productId = newProduct.id;
        this.changedFields = detectChanges(oldProduct, newProduct);
        // ... その他のフィールド設定
    }
}

// 商品削除イベント
public class ProductDeletedEvent extends ProductEvent {
    public UUID productId;
    public String sku;
    public LocalDateTime deletedAt;
    
    public ProductDeletedEvent(UUID productId, String sku) {
        this.eventType = "PRODUCT_DELETED";
        this.aggregateId = productId.toString();
        this.productId = productId;
        this.sku = sku;
        this.deletedAt = LocalDateTime.now();
    }
}
```

## 4. Product Catalog Service 実装

### 4.1 イベント発行の実装

```java
@ApplicationScoped
public class ProductEventPublisher {
    
    @Channel("product-events-out")
    Emitter<ProductEvent> eventEmitter;
    
    @Inject
    Logger logger;
    
    public void publishProductCreated(Product product) {
        ProductCreatedEvent event = new ProductCreatedEvent(product);
        eventEmitter.send(event)
            .whenComplete((success, failure) -> {
                if (failure != null) {
                    logger.error("Failed to publish product created event for product: " + product.id, failure);
                } else {
                    logger.info("Published product created event for product: " + product.id);
                }
            });
    }
    
    public void publishProductUpdated(Product oldProduct, Product newProduct) {
        ProductUpdatedEvent event = new ProductUpdatedEvent(oldProduct, newProduct);
        eventEmitter.send(event)
            .whenComplete((success, failure) -> {
                if (failure != null) {
                    logger.error("Failed to publish product updated event for product: " + newProduct.id, failure);
                } else {
                    logger.info("Published product updated event for product: " + newProduct.id);
                }
            });
    }
    
    public void publishProductDeleted(UUID productId, String sku) {
        ProductDeletedEvent event = new ProductDeletedEvent(productId, sku);
        eventEmitter.send(event);
    }
}
```

### 4.2 Product Service の修正

```java
@ApplicationScoped
@Transactional
public class ProductService {
    
    @Inject
    ProductEventPublisher eventPublisher;
    
    public Product createProduct(CreateProductRequest request) {
        // 商品作成ロジック
        Product product = new Product();
        // ... 商品データ設定
        
        product.persist();
        
        // イベント発行
        eventPublisher.publishProductCreated(product);
        
        return product;
    }
    
    public Product updateProduct(UUID productId, UpdateProductRequest request) {
        Product existingProduct = Product.findById(productId);
        if (existingProduct == null) {
            throw new ProductNotFoundException(productId);
        }
        
        // 更新前の状態を保存
        Product oldProduct = cloneProduct(existingProduct);
        
        // 商品更新
        existingProduct.name = request.name;
        existingProduct.description = request.description;
        // ... その他の更新
        
        existingProduct.persist();
        
        // 変更がある場合のみイベント発行
        if (hasChanges(oldProduct, existingProduct)) {
            eventPublisher.publishProductUpdated(oldProduct, existingProduct);
        }
        
        return existingProduct;
    }
    
    public void deleteProduct(UUID productId) {
        Product product = Product.findById(productId);
        if (product != null) {
            String sku = product.sku;
            product.delete();
            
            // イベント発行
            eventPublisher.publishProductDeleted(productId, sku);
        }
    }
}
```

## 5. Inventory Management Service 実装

### 5.1 修正後のテーブル設計

```sql
-- 設備マスタテーブル（商品詳細情報を削除）
CREATE TABLE equipment (
    id BIGSERIAL PRIMARY KEY,
    product_id UUID NOT NULL UNIQUE, -- Product Catalogサービスの products.id
    daily_rate DECIMAL(10,2) NOT NULL, -- ビジネス固有のレンタル料金
    is_rental_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- キャッシュテーブル（イベントから同期）
    cached_sku VARCHAR(100),
    cached_name VARCHAR(200),
    cached_category VARCHAR(100),
    cached_brand VARCHAR(100),
    cached_equipment_type VARCHAR(50),
    cached_size_range VARCHAR(50),
    cached_difficulty_level VARCHAR(20),
    cached_base_price DECIMAL(10,2),
    cached_description TEXT,
    cached_image_url VARCHAR(500),
    cache_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_equipment_product_id ON equipment(product_id);
CREATE INDEX idx_equipment_cached_sku ON equipment(cached_sku);
CREATE INDEX idx_equipment_cached_type ON equipment(cached_equipment_type);
```

### 5.2 イベント購読の実装

```java
@ApplicationScoped
public class ProductEventConsumer {
    
    @Inject
    EquipmentService equipmentService;
    
    @Inject
    Logger logger;
    
    @Incoming("product-events")
    @Blocking
    public void handleProductEvent(ProductEvent event) {
        try {
            switch (event.eventType) {
                case "PRODUCT_CREATED":
                    handleProductCreated((ProductCreatedEvent) event);
                    break;
                case "PRODUCT_UPDATED":
                    handleProductUpdated((ProductUpdatedEvent) event);
                    break;
                case "PRODUCT_DELETED":
                    handleProductDeleted((ProductDeletedEvent) event);
                    break;
                default:
                    logger.warn("Unknown product event type: " + event.eventType);
            }
        } catch (Exception e) {
            logger.error("Failed to process product event: " + event.eventId, e);
            throw e; // リトライメカニズムのため
        }
    }
    
    private void handleProductCreated(ProductCreatedEvent event) {
        if (isRentalEligible(event)) {
            equipmentService.createEquipmentFromProduct(event);
            logger.info("Created equipment for product: " + event.productId);
        }
    }
    
    private void handleProductUpdated(ProductUpdatedEvent event) {
        equipmentService.updateEquipmentFromProduct(event);
        logger.info("Updated equipment cache for product: " + event.productId);
    }
    
    private void handleProductDeleted(ProductDeletedEvent event) {
        equipmentService.deactivateEquipment(event.productId);
        logger.info("Deactivated equipment for deleted product: " + event.productId);
    }
    
    private boolean isRentalEligible(ProductCreatedEvent event) {
        // レンタル対象商品の判定ロジック
        return event.isRentalAvailable && 
               !event.equipmentType.equals("WAX") && 
               !event.equipmentType.equals("TUNING");
    }
}
```

### 5.3 Equipment Service の実装

```java
@ApplicationScoped
@Transactional
public class EquipmentService {
    
    public void createEquipmentFromProduct(ProductCreatedEvent event) {
        Equipment equipment = new Equipment();
        equipment.productId = event.productId;
        equipment.dailyRate = calculateDailyRate(event.basePrice, event.equipmentType);
        equipment.isRentalAvailable = event.isRentalAvailable;
        equipment.isActive = event.isActive;
        
        // キャッシュデータ設定
        updateCacheFromEvent(equipment, event);
        
        equipment.persist();
    }
    
    public void updateEquipmentFromProduct(ProductUpdatedEvent event) {
        Equipment equipment = Equipment.find("productId", event.productId).firstResult();
        if (equipment != null) {
            // 変更されたフィールドのみ更新
            if (event.changedFields.containsKey("name")) {
                equipment.cachedName = event.name;
            }
            if (event.changedFields.containsKey("basePrice")) {
                equipment.cachedBasePrice = event.basePrice;
            }
            // ... その他のフィールド更新
            
            equipment.cacheUpdatedAt = LocalDateTime.now();
            equipment.persist();
        }
    }
    
    public void deactivateEquipment(UUID productId) {
        Equipment equipment = Equipment.find("productId", productId).firstResult();
        if (equipment != null) {
            equipment.isActive = false;
            equipment.persist();
        }
    }
    
    private void updateCacheFromEvent(Equipment equipment, ProductCreatedEvent event) {
        equipment.cachedSku = event.sku;
        equipment.cachedName = event.name;
        equipment.cachedCategory = event.categoryName;
        equipment.cachedBrand = event.brandName;
        equipment.cachedEquipmentType = event.equipmentType;
        equipment.cachedSizeRange = event.sizeRange;
        equipment.cachedDifficultyLevel = event.difficultyLevel;
        equipment.cachedBasePrice = event.basePrice;
        equipment.cachedDescription = event.description;
        equipment.cachedImageUrl = event.imageUrl;
        equipment.cacheUpdatedAt = LocalDateTime.now();
    }
    
    private BigDecimal calculateDailyRate(BigDecimal basePrice, String equipmentType) {
        // ビジネスロジック：商品タイプに応じたレンタル料金計算
        if (basePrice == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal rate = basePrice.multiply(BigDecimal.valueOf(0.1)); // 基本10%
        
        switch (equipmentType) {
            case "SKI_BOARD":
                return rate.multiply(BigDecimal.valueOf(1.2)); // 20%増し
            case "BOOT":
                return rate.multiply(BigDecimal.valueOf(1.1)); // 10%増し
            case "HELMET":
                return rate.multiply(BigDecimal.valueOf(0.8)); // 20%減
            case "POLE":
                return rate.multiply(BigDecimal.valueOf(0.6)); // 40%減
            case "GOGGLE":
                return rate.multiply(BigDecimal.valueOf(0.5)); // 50%減
            case "GLOVE":
                return rate.multiply(BigDecimal.valueOf(0.4)); // 60%減
            default:
                return rate;
        }
    }
}
```

## 6. メッセージング設定

### 6.1 Product Catalog Service 設定

```properties
# application.properties

# Kafka設定
kafka.bootstrap.servers=localhost:9092

# Product events channel
mp.messaging.outgoing.product-events-out.connector=smallrye-kafka
mp.messaging.outgoing.product-events-out.topic=product-events
mp.messaging.outgoing.product-events-out.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.product-events-out.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

# Dead letter queue
mp.messaging.outgoing.product-events-out.enable-idempotence=true
mp.messaging.outgoing.product-events-out.retries=3
```

### 6.2 Inventory Management Service 設定

```properties
# application.properties

# Kafka設定
kafka.bootstrap.servers=localhost:9092

# Product events subscription
mp.messaging.incoming.product-events.connector=smallrye-kafka
mp.messaging.incoming.product-events.topic=product-events
mp.messaging.incoming.product-events.key.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.product-events.value.deserializer=io.quarkus.kafka.client.serialization.JsonbDeserializer
mp.messaging.incoming.product-events.group.id=inventory-management-group

# エラーハンドリング
mp.messaging.incoming.product-events.failure-strategy=retry
mp.messaging.incoming.product-events.retry.max-retries=3
mp.messaging.incoming.product-events.retry.delay=1000
mp.messaging.incoming.product-events.dead-letter-queue.topic=product-events-dlq
```

## 7. API改善

### 7.1 統合API（API Gateway または BFF）

```java
@Path("/api/products")
@ApplicationScoped
public class ProductIntegrationResource {
    
    @Inject
    @RestClient
    ProductCatalogClient productCatalogClient;
    
    @Inject
    @RestClient
    InventoryClient inventoryClient;
    
    @GET
    @Path("/{productId}/full")
    public Uni<ProductWithInventoryResponse> getProductWithInventory(@PathParam("productId") UUID productId) {
        return Uni.combine().all()
            .unis(
                productCatalogClient.getProduct(productId),
                inventoryClient.getInventoryByProduct(productId)
            )
            .combinedWith((product, inventory) -> 
                new ProductWithInventoryResponse(product, inventory)
            );
    }
    
    @GET
    @Path("/available")
    public Uni<List<AvailableProductResponse>> getAvailableProducts(
            @QueryParam("category") String category,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        
        return inventoryClient.getAvailableProducts(category, startDate, endDate)
            .chain(availableItems -> {
                Set<UUID> productIds = availableItems.stream()
                    .map(item -> item.productId)
                    .collect(Collectors.toSet());
                
                return productCatalogClient.getProductsByIds(productIds)
                    .map(products -> mergeProductsWithInventory(products, availableItems));
            });
    }
}
```

## 8. データ一貫性保証

### 8.1 イベント順序保証

```java
@ApplicationScoped
public class EventOrderingService {
    
    private final Map<UUID, Integer> productVersions = new ConcurrentHashMap<>();
    
    public boolean isEventInOrder(ProductEvent event) {
        UUID productId = UUID.fromString(event.aggregateId);
        Integer currentVersion = productVersions.get(productId);
        
        if (currentVersion == null) {
            // 初回イベント
            productVersions.put(productId, event.version);
            return true;
        }
        
        if (event.version <= currentVersion) {
            // 古いイベントまたは重複
            return false;
        }
        
        if (event.version == currentVersion + 1) {
            // 正しい順序
            productVersions.put(productId, event.version);
            return true;
        }
        
        // イベント順序不整合
        throw new EventOrderException("Event out of order for product: " + productId);
    }
}
```

### 8.2 データ同期ヘルスチェック

```java
@Path("/health/data-sync")
@ApplicationScoped
public class DataSyncHealthCheck {
    
    @Inject
    EquipmentService equipmentService;
    
    @Inject
    @RestClient
    ProductCatalogClient productCatalogClient;
    
    @GET
    public DataSyncStatus checkDataSync() {
        List<Equipment> equipments = equipmentService.findAll();
        List<UUID> productIds = equipments.stream()
            .map(e -> e.productId)
            .collect(Collectors.toList());
        
        // Product Catalog Service から商品情報を取得
        List<ProductDetailDto> products = productCatalogClient.getProductsBatch(
            productIds.stream().map(UUID::toString).collect(Collectors.toList())
        );
        
        Map<String, ProductDetailDto> productMap = products.stream()
            .collect(Collectors.toMap(p -> p.productId(), p -> p));
        
        List<SyncIssue> issues = new ArrayList<>();
        
        for (Equipment equipment : equipments) {
            String productIdStr = equipment.productId.toString();
            ProductDetailDto product = productMap.get(productIdStr);
            
            if (product == null) {
                issues.add(new SyncIssue(equipment.productId, "Product not found in catalog"));
            } else {
                // キャッシュデータと実際のデータの整合性チェック
                if (!equipment.cachedSku.equals(product.sku())) {
                    issues.add(new SyncIssue(equipment.productId, "SKU mismatch"));
                }
                if (!equipment.cachedName.equals(product.name())) {
                    issues.add(new SyncIssue(equipment.productId, "Name mismatch"));
                }
                // その他の整合性チェック
            }
        }
        
        return new DataSyncStatus(issues.isEmpty(), issues);
    }
}

// サポートクラス
public record SyncIssue(UUID productId, String issue) {}

public record DataSyncStatus(boolean isHealthy, List<SyncIssue> issues) {
    public List<SyncIssue> getIssues() {
        return issues;
    }
}
```

## 9. 運用・監視

### 9.1 イベント監視

```java
@ApplicationScoped
public class EventMetrics {
    
    @Counted(name = "product_events_published", description = "Number of product events published")
    public void countEventPublished(String eventType) {
        // Micrometer でメトリクス収集
    }
    
    @Timed(name = "product_event_processing_time", description = "Time to process product events")
    public void timeEventProcessing(String eventType, Duration duration) {
        // 処理時間計測
    }
}
```

### 9.2 データ同期監視

```java
@Scheduled(every = "5m")
@ApplicationScoped
public class DataSyncMonitor {
    
    @Inject
    DataSyncHealthCheck healthCheck;
    
    @Inject
    AlertService alertService;
    
    public void checkDataConsistency() {
        DataSyncStatus status = healthCheck.checkDataSync();
        
        if (!status.isHealthy()) {
            alertService.sendAlert("Data sync issues detected", status.getIssues());
        }
    }
}
```

## 10. 移行戦略

### 10.1 段階的移行

#### Phase 1: イベント発行開始**

- Product Catalogサービスにイベント発行機能追加
- 既存データはそのまま維持

#### Phase 2: イベント購読開始**

- Inventory Managementサービスにイベント購読機能追加
- キャッシュテーブルを並行して更新

#### Phase 3: データ検証

- 既存データとキャッシュデータの整合性確認
- 不整合データの修正

#### Phase 4: 切り替え

- Inventory Managementサービスのクエリをキャッシュテーブルに変更
- 重複カラムの削除

### 10.2 ロールバック計画

各フェーズでロールバック可能な設計を維持し、問題発生時は前段階に戻せるようにします。

## 11. メリット・効果

### 11.1 技術的メリット

- **データ一貫性**: Product Catalogが唯一の情報源
- **疎結合**: サービス間の直接依存を排除
- **拡張性**: 新しいサービスが簡単にイベント購読可能
- **リアルタイム同期**: イベント駆動による即座の同期

### 11.2 ビジネスメリット

- **メンテナンス性向上**: 商品情報の変更が一箇所で完結
- **開発効率向上**: サービス独立開発が可能
- **システム安定性**: 一つのサービス障害が他に波及しにくい

## 12. 既存サービスへの影響と対応

### 12.1 Frontend Service (Next.js) への影響

**現在の実装**: Product Catalog Service に直接アクセス

```typescript
// 現在の実装 (product-catalog.ts)
const PRODUCT_CATALOG_BASE_URL = process.env.NEXT_PUBLIC_PRODUCT_CATALOG_URL || 'http://localhost:8083';
```

**影響**: なし（Product Catalog Service の API は変更されないため）

**推奨改善**:

```typescript
// API Gateway 経由への切り替えを推奨
const getBaseUrl = (): string => {
  const useApiGateway = process.env.NEXT_PUBLIC_USE_API_GATEWAY === 'true';
  
  if (useApiGateway) {
    // 将来的に統合商品+在庫APIを使用
    return process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
  } else {
    return PRODUCT_CATALOG_BASE_URL;
  }
};

// 新しい統合API エンドポイント（推奨）
export const productIntegrationApi = {
  // 商品情報 + 在庫情報を一括取得
  getProductWithInventory: (productId: string) => 
    axios.get(`${getBaseUrl()}/api/products/${productId}/full`),
  
  // 利用可能な商品一覧（在庫フィルタ付き）
  getAvailableProducts: (params: AvailableProductParams) =>
    axios.get(`${getBaseUrl()}/api/products/available`, { params })
};
```

### 12.2 Shopping Cart Service への影響

**現在の実装**: Product Catalog Service と Inventory Management Service の両方に依存

**影響**: 軽微（APIインターフェースは変更されないため）

**推奨改善**:

#### 12.2.1 ProductCatalogClient の継続利用

```java
// 現在の実装は継続して利用可能
@RegisterRestClient(configKey = "product-catalog-service")
public interface ProductCatalogClient {
    @GET
    @Path("/{productId}")
    ProductDetailDto getProduct(@PathParam("productId") String productId);
    
    @POST
    @Path("/batch")
    List<ProductDetailDto> getProductsBatch(List<String> productIds);
}
```

#### 12.2.2 新しい統合APIクライアントの追加（推奨）

```java
// 新しい統合APIクライアント
@Path("/api/products")
@RegisterRestClient(configKey = "api-gateway-service")
public interface ProductIntegrationClient {
    
    @GET
    @Path("/{productId}/full")
    @Retry(maxRetries = 3, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(10000)
    ProductWithInventoryDto getProductWithInventory(@PathParam("productId") String productId);
    
    @GET
    @Path("/available")
    @Retry(maxRetries = 2, delay = 1000)
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 10000)
    @Timeout(15000)
    List<AvailableProductDto> getAvailableProducts(
        @QueryParam("category") String category,
        @QueryParam("startDate") String startDate,
        @QueryParam("endDate") String endDate
    );
    
    @POST
    @Path("/batch/with-inventory")
    @Retry(maxRetries = 2, delay = 1000)
    @Timeout(15000)
    List<ProductWithInventoryDto> getProductsWithInventoryBatch(List<String> productIds);
}

// 新しいDTO
public record ProductWithInventoryDto(
    String productId,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String category,
    String imageUrl,
    // 在庫情報
    Boolean inStock,
    Integer availableQuantity,
    String location,
    BigDecimal dailyRate
) {}
```

#### 12.2.3 CartValidationService の改善

```java
@ApplicationScoped
public class EnhancedCartValidationService {
    
    @Inject
    @RestClient
    ProductIntegrationClient productIntegrationClient;
    
    @Fallback(fallbackMethod = "validateCartItemsFallback")
    public List<CartValidationIssue> validateCartItems(List<CartItem> cartItems) {
        List<String> productIds = cartItems.stream()
            .map(CartItem::getProductId)
            .toList();
        
        // 商品情報と在庫情報を一括取得
        List<ProductWithInventoryDto> productsWithInventory = 
            productIntegrationClient.getProductsWithInventoryBatch(productIds);
        
        return cartItems.stream()
            .map(item -> validateSingleItem(item, productsWithInventory))
            .filter(Objects::nonNull)
            .toList();
    }
    
    private CartValidationIssue validateSingleItem(
            CartItem item, 
            List<ProductWithInventoryDto> productsWithInventory) {
        
        ProductWithInventoryDto product = productsWithInventory.stream()
            .filter(p -> p.productId().equals(item.getProductId()))
            .findFirst()
            .orElse(null);
        
        if (product == null) {
            return new CartValidationIssue(
                item.getProductId(), 
                "PRODUCT_NOT_FOUND", 
                "商品が見つかりません"
            );
        }
        
        if (!product.inStock() || product.availableQuantity() < item.getQuantity()) {
            return new CartValidationIssue(
                item.getProductId(), 
                "INSUFFICIENT_STOCK", 
                "在庫が不足しています"
            );
        }
        
        return null; // 問題なし
    }
}
```

### 12.3 API Gateway Service への影響

**現在の実装**: Product Catalog Service にルーティング

**必要な変更**:

#### 12.3.1 新しい統合エンドポイントの追加

```java
@ApplicationScoped
public class EnhancedRoutingService {
    
    public String getTargetServiceUrl(String path) {
        // 既存のルーティング
        if (path.startsWith("/api/v1/products") || path.startsWith("/api/v1/categories")) {
            return productServiceUrl;
        } 
        // 新しい統合APIのルーティング
        else if (path.startsWith("/api/products")) {
            return "internal"; // API Gateway内で処理
        }
        // ... その他のルーティング
    }
}
```

#### 12.3.2 統合APIの実装

```java
@Path("/api/products")
@ApplicationScoped
public class ProductIntegrationResource {
    
    @Inject
    @RestClient
    ProductCatalogClient productCatalogClient;
    
    @Inject
    @RestClient
    InventoryClient inventoryClient;
    
    @GET
    @Path("/{productId}/full")
    public Uni<ProductWithInventoryResponse> getProductWithInventory(
            @PathParam("productId") String productId) {
        
        return Uni.combine().all()
            .unis(
                Uni.createFrom().item(() -> productCatalogClient.getProduct(productId)),
                Uni.createFrom().item(() -> inventoryClient.getInventoryByProduct(productId))
            )
            .combinedWith((product, inventory) -> 
                new ProductWithInventoryResponse(product, inventory)
            )
            .onFailure().recoverWithItem(failure -> {
                logger.error("Failed to get product with inventory: " + productId, failure);
                return createFallbackResponse(productId);
            });
    }
    
    @GET
    @Path("/available")
    public Uni<List<AvailableProductResponse>> getAvailableProducts(
            @QueryParam("category") String category,
            @QueryParam("startDate") String startDate,
            @QueryParam("endDate") String endDate) {
        
        // まず在庫サービスから利用可能商品を取得
        return Uni.createFrom().item(() -> 
                inventoryClient.getAvailableProducts(category, startDate, endDate))
            .chain(availableItems -> {
                Set<String> productIds = availableItems.stream()
                    .map(item -> item.productId)
                    .collect(Collectors.toSet());
                
                // 商品詳細情報を取得
                return Uni.createFrom().item(() -> 
                        productCatalogClient.getProductsByIds(new ArrayList<>(productIds)))
                    .map(products -> mergeProductsWithInventory(products, availableItems));
            });
    }
}
```

### 12.4 設定変更

#### 12.4.1 Shopping Cart Service 設定更新

```properties
# application.properties

# 既存の設定（継続利用）
product-catalog-service/mp-rest/url=http://localhost:8083
inventory-management-service/mp-rest/url=http://localhost:8085

# 新しい統合API設定（推奨）
api-gateway-service/mp-rest/url=http://localhost:8080
```

#### 12.4.2 Frontend Service 環境変数

```properties
# .env.local

# 現在の設定（継続利用可能）
NEXT_PUBLIC_PRODUCT_CATALOG_URL=http://localhost:8083

# API Gateway経由の設定（推奨）
NEXT_PUBLIC_USE_API_GATEWAY=true
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

## 13. 移行手順の詳細

### 13.1 段階的移行計画

#### Phase 1: Event-Driven Infrastructure 構築

1. **Product Catalog Service にイベント発行機能追加**
   - ProductEventPublisher クラス実装
   - Kafka設定追加
   - 既存APIは変更なし

2. **Inventory Management Service にイベント購読機能追加**
   - ProductEventConsumer クラス実装
   - equipmentテーブルにキャッシュカラム追加
   - 既存APIは変更なし

3. **検証**: イベントが正常に発行・購読されることを確認

#### Phase 2: API Gateway 統合機能追加

1. **API Gateway に統合APIを実装**
   - ProductIntegrationResource 追加
   - 商品+在庫情報の統合エンドポイント作成

2. **Shopping Cart Service に新しいクライアント追加**
   - ProductIntegrationClient 実装
   - 既存のクライアントと並行運用

3. **検証**: 統合APIが正常に動作することを確認

#### Phase 3: Frontend Service の段階的切り替え

1. **環境変数による切り替え機能実装**
   - API Gateway経由と直接アクセスの選択可能化

2. **新しい統合APIの利用開始**
   - 商品詳細ページから順次切り替え
   - 商品一覧ページの在庫フィルタ機能追加

3. **検証**: フロントエンド機能に問題がないことを確認

#### Phase 4: 完全移行とクリーンアップ

1. **全サービスの新API利用への切り替え**
2. **古いAPIの段階的廃止**
3. **不要なコード・設定の削除**

### 13.2 ロールバック戦略

各フェーズで問題が発生した場合：

- **Phase 1-2**: イベント機能を無効化し、既存フローに戻す
- **Phase 3**: 環境変数で直接アクセスに戻す
- **Phase 4**: 古いAPIを再有効化

## 14. 監視・運用

### 14.1 新しいメトリクス

```java
@ApplicationScoped
public class IntegrationMetrics {
    
    @Counted(name = "product_integration_api_calls", description = "統合API呼び出し回数")
    public void countIntegrationApiCall(String endpoint) {}
    
    @Timed(name = "product_integration_response_time", description = "統合API応答時間")
    public void timeIntegrationApiCall(String endpoint, Duration duration) {}
    
    @Counted(name = "cache_hit_rate", description = "キャッシュヒット率")
    public void countCacheHit(boolean hit) {}
}
```

### 14.2 ヘルスチェック拡張

```java
@ApplicationScoped
public class EnhancedHealthCheck {
    
    @Readiness
    public HealthCheckResponse checkDataSync() {
        // Product CatalogとInventory Managementの同期状態確認
        return HealthCheckResponse.builder()
            .name("product-inventory-sync")
            .status(checkSyncStatus())
            .withData("last_sync", getLastSyncTime())
            .withData("pending_events", getPendingEventCount())
            .build();
    }
}
```

## 15. 今後の拡張計画

- **Notification Service**: 商品変更通知の自動送信
- **Analytics Service**: 商品イベントの分析
- **Cache Service**: 商品情報の分散キャッシュ
- **Search Service**: 商品検索の専門サービス
- **Real-time Updates**: WebSocketによるリアルタイム在庫更新

## 16. まとめ

### 16.1 変更が必要なサービス

| サービス | 変更レベル | 説明 |
|---------|-----------|------|
| **Product Catalog** | 中 | イベント発行機能追加 |
| **Inventory Management** | 中 | イベント購読、キャッシュテーブル追加 |
| **API Gateway** | 中 | 統合APIエンドポイント追加 |
| **Shopping Cart** | 小 | 新しいクライアント追加（既存も併用） |
| **Frontend** | 小 | 環境変数による切り替え機能追加 |

### 16.2 互換性保証

- **既存APIは全て維持**: 破壊的変更なし
- **段階的移行**: 各段階でロールバック可能
- **設定ベース切り替え**: 環境変数で新旧機能を選択可能

## 17. 設計書の検証と追加考慮事項

### 17.1 設計内容の検証結果

#### ✅ **確認済み項目**

- **現在のコード構造**: 全サービスの実装を確認済み
- **APIインターフェース**: 既存APIとの互換性確保
- **データモデル**: Product/Equipment の関係性を正確に反映
- **依存関係**: サービス間の実際の依存関係を網羅
- **移行戦略**: 段階的な移行による安全性確保

#### ✅ **追加されたカバレッジ**

- **Frontend Service**: TypeScript APIクライアントの対応
- **Shopping Cart Service**: RESTクライアントとDTO の対応
- **API Gateway Service**: ルーティングと統合API の対応統合API の対応
- **エラーハンドリング**: フォールバック戦略の詳細化
- **監視・運用**: ヘルスチェックとメトリクス の具体化

### 17.2 潜在的リスク と対策

#### 🚨 **イベント順序性の問題**

**リスク**: 同一商品への並行更新でイベント順序が不整合になる可能性

**対策**:

```java
// Product Catalog Service でのイベントバージョニング
@Entity
public class Product {
    // ... existing fields
    
    @Version
    @Column(name = "event_version")
    private int eventVersion;
    
    public void incrementEventVersion() {
        this.eventVersion++;
    }
}

// イベント発行時にバージョンを設定
public void publishProductUpdated(Product oldProduct, Product newProduct) {
    newProduct.incrementEventVersion();
    ProductUpdatedEvent event = new ProductUpdatedEvent(oldProduct, newProduct);
    event.version = newProduct.getEventVersion();
    eventEmitter.send(event);
}
```

#### 🚨 **ネットワーク分断時のデータ不整合**

**リスク**: Kafka やサービス間通信の障害でデータ同期が停止

**対策**:

```java
// 定期的な差分同期機能
@Scheduled(every = "1h")
@ApplicationScoped
public class DataSyncRecoveryService {
    
    public void performDifferentialSync() {
        // 最後の同期時刻以降の変更を取得
        LocalDateTime lastSync = getLastSyncTimestamp();
        List<Product> changedProducts = 
            Product.find("updatedAt > ?1", lastSync).list();
        
        // 変更があった商品のイベントを再発行
        for (Product product : changedProducts) {
            republishProductEvent(product);
        }
    }
}
```

#### 🚨 **大量データ同期の性能問題**

**リスク**: 初期同期時や復旧時の大量データ処理でシステム負荷が高くなる

**対策**:

```java
// バッチ処理での段階的同期
@ApplicationScoped
public class BulkSyncService {
    
    private static final int BATCH_SIZE = 100;
    
    public void performBulkSync() {
        List<Product> allProducts = Product.listAll();
        
        // バッチ単位で処理
        for (int i = 0; i < allProducts.size(); i += BATCH_SIZE) {
            List<Product> batch = allProducts.subList(
                i, Math.min(i + BATCH_SIZE, allProducts.size())
            );
            
            processBatch(batch);
            
            // 負荷軽減のための待機
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

### 17.3 運用時の重要な考慮事項

#### 📊 **監視すべきメトリクス**

```java
// 包括的な監視メトリクス
@ApplicationScoped
public class ComprehensiveMetrics {
    
    // イベント処理メトリクス
    @Counted(name = "product_events_processed_total")
    @Timed(name = "product_event_processing_duration")
    public void processEvent(String eventType) {}
    
    // データ同期メトリクス
    @Gauge(name = "data_sync_lag_seconds", description = "Data synchronization lag in seconds")
    public long getDataSyncLag() {
        return calculateSyncLag();
    }
    
    // API統合メトリクス
    @Histogram(name = "api_integration_response_size")
    public void recordResponseSize(int size) {}
    
    // エラー率メトリクス
    @Counted(name = "integration_errors_total")
    public void countIntegrationError(String service, String errorType) {}
}
```

#### 🔄 **災害復旧手順**

1. **Kafka 障害時**: Product Catalog の変更を一時的にキューに保存
2. **Product Catalog 障害時**: Inventory のキャッシュデータで継続運用
3. **Inventory 障害時**: Product Catalog のみで基本機能を提供
4. **API Gateway 障害時**: 各サービスへの直接アクセスに切り替え

### 17.4 今後の機能拡張ロードマップ

#### 📅 **短期 (3-6ヶ月)**

- **リアルタイム在庫更新**: WebSocket による在庫状況のリアルタイム通知
- **商品推薦エンジン**: 商品閲覧・購入履歴を活用した推薦機能
- **在庫予測**: 機械学習による需要予測と自動発注機能

#### 📅 **中期 (6-12ヶ月)**

- **マルチテナント対応**: 複数スキーリゾートでの共有利用
- **グローバル対応**: 多言語・多通貨対応
- **モバイルアプリ**: 専用モバイルアプリの開発

#### 📅 **長期 (12ヶ月以上)**

- **IoT統合**: RFID/QRコード による機器の自動追跡
- **AI活用**: 画像認識による機器状態の自動判定
- **ブロックチェーン**: 機器の履歴管理と真正性証明

### 17.5 コスト・ROI 分析

#### 💰 **導入コスト**

- **開発工数**: 約 3-4 人月
- **インフラコスト**: Kafka クラスタ運用費 (月額 $200-500)
- **監視ツール**: Prometheus/Grafana 等 (月額 $100-300)

#### 📈 **期待される効果**

- **開発効率**: 30% 向上 (サービス独立開発)
- **運用コスト**: 20% 削減 (データ整合性問題の減少)
- **システム可用性**: 99.9% 以上 (障害の局所化)
- **新機能開発**: 50% 高速化 (疎結合アーキテクチャ)

### 17.6 チーム・組織への影響

#### 👥 **必要なスキルセット**

- **Event-Driven Architecture**: Kafka, イベントソーシング
- **マイクロサービス運用**: 分散トレーシング、ログ集約
- **DevOps**: コンテナ化、CI/CD パイプライン
- **監視・アラート**: メトリクス分析、障害対応

#### 📚 **研修・教育計画**

1. **Event-Driven Architecture 基礎研修** (1週間)
2. **Kafka 運用研修** (3日間)
3. **マイクロサービス監視研修** (2日間)
4. **障害対応演習** (継続的)

この包括的な設計により、技術的な優位性だけでなく、ビジネス価値と運用の現実性を兼ね備えたEvent-Driven Architectureを実現できます。
