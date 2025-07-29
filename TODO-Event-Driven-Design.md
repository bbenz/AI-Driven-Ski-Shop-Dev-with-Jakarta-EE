# Product Catalog Service Event-Driven Design 修正要件

## 概要

`05-inventory-management-service.md` の実装により、Product Catalog Service側で以下のEvent-Driven Architecture対応が必要になります。在庫管理サービスが商品情報の Single Source of Truth として Product Catalog Service を位置づけているため、イベント発行機能の実装が必須です。

## 1. 必須イベント発行機能の実装

### 1.1 商品ライフサイクルイベント

Product Catalog Service で以下のイベントを Kafka に発行する必要があります：

#### 1.1.1 ProductCreatedEvent

```java
public record ProductCreatedEvent(
    UUID productId,
    String sku,
    String name,
    UUID categoryId,
    UUID brandId,
    String equipmentType,
    BigDecimal basePrice,
    LocalDateTime createdAt
) implements ProductEvent {}
```

**発行タイミング:**

- 商品エンティティがデータベースに保存された直後（@PostPersist）
- 商品が PUBLISHED 状態に変更された時

**対象処理**: `ProductService.createProduct()` メソッド

#### 1.1.2 ProductUpdatedEvent

```java
public record ProductUpdatedEvent(
    UUID productId,
    String sku,
    String name,
    UUID categoryId,
    UUID brandId,
    String equipmentType,
    BigDecimal basePrice,
    Map<String, String> changedFields,
    LocalDateTime updatedAt
) implements ProductEvent {}
```

**発行タイミング:**

- 商品情報が更新された直後（@PostUpdate）
- SKU、名前、カテゴリ、ブランド、基本価格のいずれかが変更された時

**対象処理**: `ProductService.updateProduct()` メソッド

#### 1.1.3 ProductDeletedEvent

```java
public record ProductDeletedEvent(
    UUID productId,
    String sku,
    String reason,
    LocalDateTime deletedAt
) implements ProductEvent {}
```

**発行タイミング:**

- 商品が論理削除された時
- 商品が ARCHIVED 状態に変更された時

**対象処理**: `ProductService.deleteProduct()`, `ProductService.archiveProduct()` メソッド

#### 1.1.4 ProductPriceChangedEvent

```java
public record ProductPriceChangedEvent(
    UUID productId,
    String sku,
    BigDecimal oldPrice,
    BigDecimal newPrice,
    String priceType, // "BASE_PRICE", "SALE_PRICE"
    LocalDateTime effectiveDate,
    LocalDateTime changedAt
) implements ProductEvent {}
```

**発行タイミング:**

- basePrice または salePrice が変更された時
- 価格履歴レコードが作成された時

**対象処理**: `ProductService.updatePrice()` メソッド

### 1.2 Kafka Topic 設計

#### 1.2.1 Topic 構成

```yaml
Topics:
  - name: "product-lifecycle-events"
    partitions: 6
    replication-factor: 3
    key: productId  # パーティション分散とイベント順序保証
    
  - name: "product-price-events"
    partitions: 3
    replication-factor: 3
    key: productId
```

#### 1.2.2 Event Schema Registry

```json
{
  "namespace": "com.ski.shop.catalog.events",
  "schemas": [
    {
      "name": "ProductCreatedEvent",
      "version": "1.0.0",
      "fields": ["productId", "sku", "name", "categoryId", "brandId", "equipmentType", "basePrice", "createdAt"]
    },
    {
      "name": "ProductUpdatedEvent", 
      "version": "1.0.0",
      "fields": ["productId", "sku", "name", "categoryId", "brandId", "equipmentType", "basePrice", "changedFields", "updatedAt"]
    },
    {
      "name": "ProductDeletedEvent",
      "version": "1.0.0", 
      "fields": ["productId", "sku", "reason", "deletedAt"]
    },
    {
      "name": "ProductPriceChangedEvent",
      "version": "1.0.0",
      "fields": ["productId", "sku", "oldPrice", "newPrice", "priceType", "effectiveDate", "changedAt"]
    }
  ]
}
```

## 2. 実装が必要なクラスとコンポーネント

### 2.1 Event Publisher の実装

#### 2.1.1 ProductEventPublisher

```java
@ApplicationScoped
public class ProductEventPublisher {
    
    @Inject
    @Channel("product-lifecycle-events")
    Emitter<ProductEvent> lifecycleEventEmitter;
    
    @Inject
    @Channel("product-price-events")
    Emitter<ProductPriceChangedEvent> priceEventEmitter;
    
    @Asynchronous
    public CompletableFuture<Void> publishProductCreated(ProductCreatedEvent event) {
        return CompletableFuture.runAsync(() -> {
            lifecycleEventEmitter.send(Message.of(event)
                .withMetadata(Metadata.of(
                    "eventType", "ProductCreated",
                    "eventVersion", "1.0.0",
                    "correlationId", UUID.randomUUID().toString()
                )));
        });
    }
    
    @Asynchronous  
    public CompletableFuture<Void> publishProductUpdated(ProductUpdatedEvent event) {
        return CompletableFuture.runAsync(() -> {
            lifecycleEventEmitter.send(Message.of(event)
                .withMetadata(Metadata.of(
                    "eventType", "ProductUpdated",
                    "eventVersion", "1.0.0"
                )));
        });
    }
    
    @Asynchronous
    public CompletableFuture<Void> publishProductDeleted(ProductDeletedEvent event) {
        return CompletableFuture.runAsync(() -> {
            lifecycleEventEmitter.send(Message.of(event)
                .withMetadata(Metadata.of(
                    "eventType", "ProductDeleted",
                    "eventVersion", "1.0.0"
                )));
        });
    }
    
    @Asynchronous
    public CompletableFuture<Void> publishPriceChanged(ProductPriceChangedEvent event) {
        return CompletableFuture.runAsync(() -> {
            priceEventEmitter.send(Message.of(event)
                .withMetadata(Metadata.of(
                    "eventType", "ProductPriceChanged",
                    "eventVersion", "1.0.0"
                )));
        });
    }
}
```

#### 2.1.2 ProductEventListener (JPA Entity Listeners)

```java
@Component
public class ProductEventListener {
    
    @Inject
    private ProductEventPublisher eventPublisher;
    
    @PostPersist
    public void onProductCreated(Product product) {
        if (product.publishStatus == PublishStatus.PUBLISHED) {
            var event = new ProductCreatedEvent(
                product.id,
                product.sku,
                product.name,
                product.category.id,
                product.brand.id,
                mapToEquipmentType(product.category),
                product.basePrice,
                product.createdAt
            );
            eventPublisher.publishProductCreated(event);
        }
    }
    
    @PostUpdate
    public void onProductUpdated(Product product) {
        if (product.publishStatus == PublishStatus.PUBLISHED) {
            var changedFields = detectChangedFields(product);
            var event = new ProductUpdatedEvent(
                product.id,
                product.sku,
                product.name,
                product.category.id,
                product.brand.id,
                mapToEquipmentType(product.category),
                product.basePrice,
                changedFields,
                product.updatedAt
            );
            eventPublisher.publishProductUpdated(event);
        }
    }
    
    private String mapToEquipmentType(Category category) {
        // カテゴリから設備タイプへのマッピングロジック
        return switch (category.getName().toLowerCase()) {
            case "スキー板" -> "SKI";
            case "スノーボード" -> "SNOWBOARD";
            case "ブーツ" -> "BOOTS";
            case "ヘルメット" -> "HELMET";
            case "ウェア" -> "WEAR";
            default -> "OTHER";
        };
    }
    
    private Map<String, String> detectChangedFields(Product product) {
        // 変更フィールドの検出ロジック
        // この実装は Product Entity の修正と連携
        return product.getChangedFields();
    }
}
```

### 2.2 Product Entity の修正

#### 2.2.1 JPA Entity Listeners の追加

現在のProductエンティティに以下の修正が必要です：

```java
@Entity
@Table(name = "products")
@EntityListeners(ProductEventListener.class)  // 追加
public class Product extends PanacheEntityBase {
    // 既存のフィールドはそのまま
    
    // 変更追跡のためのフィールド追加
    @Transient
    private Map<String, Object> originalValues = new HashMap<>();
    
    @PostLoad
    public void captureOriginalValues() {
        originalValues.put("sku", this.sku);
        originalValues.put("name", this.name);
        originalValues.put("categoryId", this.category != null ? this.category.id : null);
        originalValues.put("brandId", this.brand != null ? this.brand.id : null);
        originalValues.put("basePrice", this.basePrice);
    }
    
    public Map<String, String> getChangedFields() {
        Map<String, String> changes = new HashMap<>();
        
        if (!Objects.equals(originalValues.get("sku"), this.sku)) {
            changes.put("sku", String.valueOf(originalValues.get("sku")) + " -> " + this.sku);
        }
        if (!Objects.equals(originalValues.get("name"), this.name)) {
            changes.put("name", String.valueOf(originalValues.get("name")) + " -> " + this.name);
        }
        if (!Objects.equals(originalValues.get("basePrice"), this.basePrice)) {
            changes.put("basePrice", String.valueOf(originalValues.get("basePrice")) + " -> " + this.basePrice);
        }
        
        return changes;
    }
    
    // カテゴリから設備タイプへのマッピング用ヘルパーメソッド
    public String getEquipmentType() {
        if (this.category == null) return "OTHER";
        
        return switch (this.category.name.toLowerCase()) {
            case "スキー板", "alpine skis", "ski" -> "SKI";
            case "スノーボード", "snowboard" -> "SNOWBOARD";  
            case "ブーツ", "boots" -> "BOOTS";
            case "ヘルメット", "helmet" -> "HELMET";
            case "ウェア", "wear", "clothing" -> "WEAR";
            case "ゴーグル", "goggles" -> "GOGGLES";
            case "グローブ", "gloves" -> "GLOVES";
            case "プロテクター", "protector" -> "PROTECTOR";
            default -> "OTHER";
        };
    }
}
```

### 2.3 ProductService の修正

#### 2.3.1 明示的イベント発行の追加

現在のProductServiceに以下のメソッドを追加または修正する必要があります：

```java
@ApplicationScoped
@Transactional
public class ProductService {
    
    @Inject
    private ProductEventPublisher eventPublisher;
    
    // 既存のProductRepository等の注入はそのまま
    
    public Product updateProduct(UUID productId, ProductUpdateRequest request) {
        Product product = findProductById(productId);
        
        // 価格変更の検出
        BigDecimal oldPrice = product.basePrice;
        boolean priceChanged = false;
        
        // 商品情報の更新
        product.name = request.name();
        product.description = request.description();
        product.shortDescription = request.shortDescription();
        
        if (!oldPrice.equals(request.basePrice())) {
            product.basePrice = request.basePrice();
            priceChanged = true;
        }
        
        // カテゴリ・ブランドの更新
        if (request.categoryId() != null) {
            var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));
            product.category = category;
        }
        
        if (request.brandId() != null) {
            var brand = brandRepository.findById(request.brandId())
                .orElseThrow(() -> new BrandNotFoundException(request.brandId()));
            product.brand = brand;
        }
        
        product.updatedAt = LocalDateTime.now();
        
        Product savedProduct = productRepository.save(product);
        
        // 価格変更イベントの発行
        if (priceChanged) {
            var priceEvent = new ProductPriceChangedEvent(
                savedProduct.id,
                savedProduct.sku,
                oldPrice,
                savedProduct.basePrice,
                "BASE_PRICE",
                LocalDateTime.now(),
                LocalDateTime.now()
            );
            eventPublisher.publishPriceChanged(priceEvent);
        }
        
        return savedProduct;
    }
    
    public void deleteProduct(UUID productId, String reason) {
        Product product = findProductById(productId);
        
        // 論理削除の実行
        product.isActive = false;
        product.publishStatus = PublishStatus.ARCHIVED;
        product.discontinuedAt = LocalDateTime.now();
        product.updatedAt = LocalDateTime.now();
        
        productRepository.save(product);
        
        // 削除イベントの発行
        var deleteEvent = new ProductDeletedEvent(
            product.id,
            product.sku,
            reason,
            LocalDateTime.now()
        );
        eventPublisher.publishProductDeleted(deleteEvent);
    }
    
    public Product createProduct(ProductCreateRequest request) {
        // 既存の作成ロジック
        Product product = new Product();
        // ... 各フィールドの設定
        
        Product savedProduct = productRepository.save(product);
        
        // 作成イベントの明示的発行（@PostPersistと併用）
        if (savedProduct.publishStatus == PublishStatus.PUBLISHED) {
            var createEvent = new ProductCreatedEvent(
                savedProduct.id,
                savedProduct.sku,
                savedProduct.name,
                savedProduct.category.id,
                savedProduct.brand.id,
                savedProduct.getEquipmentType(),
                savedProduct.basePrice,
                savedProduct.createdAt
            );
            eventPublisher.publishProductCreated(createEvent);
        }
        
        return savedProduct;
    }
    
    // ヘルパーメソッド
    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
```

## 3. 設定ファイルの修正

### 3.1 application.properties の追加設定

現在のapplication.propertiesに以下の設定を追加：

```properties
# Kafka設定
kafka.bootstrap.servers=localhost:9092

# Product Lifecycle Events Channel
mp.messaging.outgoing.product-lifecycle-events.connector=smallrye-kafka
mp.messaging.outgoing.product-lifecycle-events.topic=product-lifecycle-events
mp.messaging.outgoing.product-lifecycle-events.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.product-lifecycle-events.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

# Product Price Events Channel  
mp.messaging.outgoing.product-price-events.connector=smallrye-kafka
mp.messaging.outgoing.product-price-events.topic=product-price-events
mp.messaging.outgoing.product-price-events.key.serializer=org.apache.kafka.common.serialization.StringSerializer
mp.messaging.outgoing.product-price-events.value.serializer=io.quarkus.kafka.client.serialization.JsonbSerializer

# Event publishing settings
product.events.enabled=true
product.events.async=true
product.events.retry.max-attempts=3
product.events.retry.delay=1s

# Producer settings
kafka.producer.acks=all
kafka.producer.retries=3
kafka.producer.retry.backoff.ms=1000
kafka.producer.delivery.timeout.ms=30000
```

### 3.2 pom.xml の依存関係追加

現在のpom.xmlに以下の依存関係を追加：

```xml
<dependencies>
    <!-- 既存の依存関係は保持 -->
    
    <!-- Kafka connector for Reactive Messaging -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-reactive-messaging-kafka</artifactId>
    </dependency>
    
    <!-- JSON-B for event serialization -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jsonb</artifactId>
    </dependency>
    
    <!-- MicroProfile Reactive Messaging -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-smallrye-reactive-messaging</artifactId>
    </dependency>
    
    <!-- Event sourcing support (optional) -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-kafka-client</artifactId>
    </dependency>
</dependencies>
```

## 4. Event Consumer確認エンドポイントの実装

### 4.1 Event Status API

Inventory Management Service側での適切なイベント受信を確認するためのAPI実装：

```java
@Path("/api/v1/products/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Product Events", description = "商品イベント管理API")
public class ProductEventResource {
    
    @Inject
    private EventAuditService eventAuditService;
    
    @GET
    @Path("/status")
    @Operation(summary = "イベント発行状況取得")
    public Response getEventStatus(
            @QueryParam("productId") UUID productId,
            @QueryParam("eventType") String eventType,
            @QueryParam("fromDate") String fromDate,
            @QueryParam("toDate") String toDate) {
        
        var status = eventAuditService.getEventStatus(
            productId, eventType, 
            parseDate(fromDate), parseDate(toDate)
        );
        
        return Response.ok(status).build();
    }
    
    @POST
    @Path("/replay/{productId}")
    @Operation(summary = "イベント再送信")
    public Response replayEvents(@PathParam("productId") UUID productId) {
        var result = eventAuditService.replayProductEvents(productId);
        return Response.ok(result).build();
    }
    
    @GET
    @Path("/health")
    @Operation(summary = "イベント発行システム健全性確認")
    public Response getEventSystemHealth() {
        var health = eventAuditService.checkEventSystemHealth();
        return Response.ok(health).build();
    }
    
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: " + dateString);
        }
    }
}
```

### 4.2 Event Audit Service

```java
@ApplicationScoped
public class EventAuditService {
    
    @Inject
    private ProductRepository productRepository;
    
    @Inject
    private ProductEventPublisher eventPublisher;
    
    @Inject
    private EventLogRepository eventLogRepository;
    
    public EventStatusResponse getEventStatus(UUID productId, String eventType, 
                                            LocalDate fromDate, LocalDate toDate) {
        // イベント送信履歴の取得と分析
        var events = getPublishedEvents(productId, eventType, fromDate, toDate);
        var acknowledgments = getEventAcknowledgments(productId, eventType, fromDate, toDate);
        
        return new EventStatusResponse(
            events.size(),
            acknowledgments.size(),
            events.size() - acknowledgments.size(), // 未確認イベント数
            events,
            acknowledgments
        );
    }
    
    @Asynchronous
    public CompletableFuture<Void> replayProductEvents(UUID productId) {
        return CompletableFuture.runAsync(() -> {
            var product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
            
            // 全イベントの再送信
            var createdEvent = new ProductCreatedEvent(
                product.id, 
                product.sku, 
                product.name,
                product.category.id, 
                product.brand.id,
                product.getEquipmentType(),
                product.basePrice, 
                product.createdAt
            );
            eventPublisher.publishProductCreated(createdEvent);
        });
    }
    
    public EventSystemHealthResponse checkEventSystemHealth() {
        try {
            // Kafka接続確認
            boolean kafkaHealthy = testKafkaConnection();
            
            // 最近のイベント発行状況確認
            var recentEvents = getRecentEventStatistics();
            
            // エラー率の計算
            double errorRate = calculateRecentErrorRate();
            
            return new EventSystemHealthResponse(
                kafkaHealthy,
                recentEvents.totalEvents(),
                recentEvents.successfulEvents(),
                recentEvents.failedEvents(),
                errorRate,
                LocalDateTime.now()
            );
        } catch (Exception e) {
            return new EventSystemHealthResponse(
                false, 0, 0, 0, 1.0, LocalDateTime.now()
            );
        }
    }
    
    private List<EventLogEntry> getPublishedEvents(UUID productId, String eventType, 
                                                  LocalDate fromDate, LocalDate toDate) {
        // EventLogRepositoryからの検索実装
        return eventLogRepository.findByProductIdAndTypeAndDateRange(
            productId, eventType, fromDate, toDate);
    }
    
    private List<EventAcknowledgment> getEventAcknowledgments(UUID productId, String eventType,
                                                             LocalDate fromDate, LocalDate toDate) {
        // Inventory Serviceからの確認応答取得実装
        return List.of(); // 実装は省略
    }
    
    private boolean testKafkaConnection() {
        // Kafka接続テストの実装
        return true; // 実際の実装では適切なテストを行う
    }
    
    private EventStatistics getRecentEventStatistics() {
        var yesterday = LocalDateTime.now().minusDays(1);
        var events = eventLogRepository.findByTimestampAfter(yesterday);
        
        long total = events.size();
        long successful = events.stream().mapToLong(e -> e.isSuccessful() ? 1 : 0).sum();
        long failed = total - successful;
        
        return new EventStatistics(total, successful, failed);
    }
    
    private double calculateRecentErrorRate() {
        var stats = getRecentEventStatistics();
        if (stats.totalEvents() == 0) return 0.0;
        return (double) stats.failedEvents() / stats.totalEvents();
    }
}
```

## 5. テスト実装

### 5.1 イベント発行テスト

```java
@QuarkusTest
@TestMethodOrder(OrderAnnotation.class)
class ProductEventPublishingTest {
    
    @Inject
    ProductService productService;
    
    @InjectMock
    ProductEventPublisher eventPublisher;
    
    @Test
    @Order(1)
    void testProductCreatedEventPublishing() {
        // Given
        var request = new ProductCreateRequest(
            "SKI-TEST-001", "テストスキー", "テスト用スキー",
            categoryId, brandId, Material.COMPOSITE, SkiType.ALL_MOUNTAIN,
            DifficultyLevel.INTERMEDIATE, "165cm", "75mm", "2.8kg",
            "14m", Flex.MEDIUM, BigDecimal.valueOf(85000)
        );
        
        // When
        var product = productService.createProduct(request);
        
        // Then
        verify(eventPublisher, times(1))
            .publishProductCreated(any(ProductCreatedEvent.class));
        
        ArgumentCaptor<ProductCreatedEvent> eventCaptor = 
            ArgumentCaptor.forClass(ProductCreatedEvent.class);
        verify(eventPublisher).publishProductCreated(eventCaptor.capture());
        
        var publishedEvent = eventCaptor.getValue();
        assertEquals(product.id, publishedEvent.productId());
        assertEquals("SKI-TEST-001", publishedEvent.sku());
        assertEquals("テストスキー", publishedEvent.name());
        assertEquals("SKI", publishedEvent.equipmentType());
    }
    
    @Test
    @Order(2)
    void testProductUpdatedEventPublishing() {
        // Given
        var product = createTestProduct();
        var updateRequest = new ProductUpdateRequest(
            "SKI-TEST-001-UPDATED", "更新テストスキー", "更新されたテスト用スキー",
            categoryId, brandId, BigDecimal.valueOf(95000)
        );
        
        // When
        productService.updateProduct(product.id, updateRequest);
        
        // Then
        verify(eventPublisher, times(1))
            .publishProductUpdated(any(ProductUpdatedEvent.class));
        verify(eventPublisher, times(1))
            .publishPriceChanged(any(ProductPriceChangedEvent.class));
    }
    
    @Test
    @Order(3)
    void testProductDeletedEventPublishing() {
        // Given
        var product = createTestProduct();
        var reason = "商品販売終了";
        
        // When
        productService.deleteProduct(product.id, reason);
        
        // Then
        verify(eventPublisher, times(1))
            .publishProductDeleted(any(ProductDeletedEvent.class));
        
        ArgumentCaptor<ProductDeletedEvent> eventCaptor = 
            ArgumentCaptor.forClass(ProductDeletedEvent.class);
        verify(eventPublisher).publishProductDeleted(eventCaptor.capture());
        
        var publishedEvent = eventCaptor.getValue();
        assertEquals(product.id, publishedEvent.productId());
        assertEquals(reason, publishedEvent.reason());
    }
}
```

### 5.2 統合テスト

```java
@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
class ProductEventIntegrationTest {
    
    @InjectKafkaCompanion
    KafkaCompanion companion;
    
    @Inject
    ProductService productService;
    
    @Test
    void testEventPublishingToKafka() {
        // Given
        var request = new ProductCreateRequest(
            "SKI-INTEGRATION-001", "統合テストスキー", "統合テスト用",
            categoryId, brandId, Material.COMPOSITE, SkiType.ALL_MOUNTAIN,
            DifficultyLevel.INTERMEDIATE, "170cm", "80mm", "3.0kg",
            "15m", Flex.MEDIUM, BigDecimal.valueOf(120000)
        );
        
        // When
        productService.createProduct(request);
        
        // Then
        var records = companion.consume(String.class, ProductCreatedEvent.class)
            .fromTopics("product-lifecycle-events", 1)
            .awaitCompletion(Duration.ofSeconds(10));
            
        assertEquals(1, records.count());
        var event = records.getFirstRecord().value();
        assertEquals("SKI-INTEGRATION-001", event.sku());
        assertEquals("統合テストスキー", event.name());
        assertEquals("SKI", event.equipmentType());
    }
    
    @Test
    void testPriceChangeEventPublishingToKafka() {
        // Given
        var product = createTestProduct();
        var updateRequest = new ProductUpdateRequest(
            product.sku, product.name, product.description,
            product.category.id, product.brand.id, 
            BigDecimal.valueOf(150000) // 価格変更
        );
        
        // When
        productService.updateProduct(product.id, updateRequest);
        
        // Then
        var records = companion.consume(String.class, ProductPriceChangedEvent.class)
            .fromTopics("product-price-events", 1)
            .awaitCompletion(Duration.ofSeconds(10));
            
        assertEquals(1, records.count());
        var event = records.getFirstRecord().value();
        assertEquals(product.sku, event.sku());
        assertEquals("BASE_PRICE", event.priceType());
    }
}
```

## 6. 運用・監視の追加実装

### 6.1 Event Metrics

```java
@ApplicationScoped
public class ProductEventMetrics {
    
    @Inject
    MeterRegistry meterRegistry;
    
    private final Counter eventPublishedCounter;
    private final Counter eventFailedCounter;
    private final Timer eventPublishingTimer;
    private final Gauge pendingEventsGauge;
    
    public ProductEventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        this.eventPublishedCounter = Counter.builder("product.events.published")
            .description("Published product events count")
            .tag("service", "product-catalog")
            .register(meterRegistry);
            
        this.eventFailedCounter = Counter.builder("product.events.failed")
            .description("Failed product events count")
            .tag("service", "product-catalog")
            .register(meterRegistry);
            
        this.eventPublishingTimer = Timer.builder("product.events.publish.duration")
            .description("Product event publishing duration")
            .tag("service", "product-catalog")
            .register(meterRegistry);
            
        this.pendingEventsGauge = Gauge.builder("product.events.pending")
            .description("Number of pending events")
            .tag("service", "product-catalog")
            .register(meterRegistry, this, ProductEventMetrics::getPendingEventCount);
    }
    
    public void recordEventPublished(String eventType) {
        eventPublishedCounter.increment(Tags.of("eventType", eventType));
    }
    
    public void recordEventFailed(String eventType, String reason) {
        eventFailedCounter.increment(Tags.of("eventType", eventType, "reason", reason));
    }
    
    public Timer.Sample startPublishingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPublishingDuration(Timer.Sample sample, String eventType) {
        sample.stop(eventPublishingTimer.withTags("eventType", eventType));
    }
    
    private double getPendingEventCount() {
        // 実装: 未送信イベント数の取得
        return 0.0; // 実際の実装では適切な値を返す
    }
}
```

### 6.2 Health Check

```java
@ApplicationScoped
public class ProductEventHealthCheck implements HealthCheck {
    
    @Inject
    ProductEventPublisher eventPublisher;
    
    @Inject
    EventAuditService eventAuditService;
    
    @Override
    public HealthCheckResponse call() {
        try {
            // Kafka接続テスト
            boolean kafkaHealthy = testKafkaConnection();
            
            // 最近のエラー率チェック
            double errorRate = eventAuditService.calculateRecentErrorRate();
            boolean errorRateAcceptable = errorRate < 0.05; // 5%未満
            
            if (kafkaHealthy && errorRateAcceptable) {
                return HealthCheckResponse.up("product-events")
                    .withData("kafka", "UP")
                    .withData("error_rate", errorRate)
                    .withData("last_check", LocalDateTime.now().toString())
                    .build();
            } else {
                return HealthCheckResponse.down("product-events")
                    .withData("kafka", kafkaHealthy ? "UP" : "DOWN")
                    .withData("error_rate", errorRate)
                    .withData("error_rate_acceptable", errorRateAcceptable)
                    .withData("last_check", LocalDateTime.now().toString())
                    .build();
            }
        } catch (Exception e) {
            return HealthCheckResponse.down("product-events")
                .withData("error", e.getMessage())
                .withData("last_check", LocalDateTime.now().toString())
                .build();
        }
    }
    
    private boolean testKafkaConnection() {
        // Kafka接続テストの実装
        try {
            // 簡単な接続テスト（実際の実装では適切なテストを行う）
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

## 7. 実装優先順位

### Phase 1 (必須・最優先)

1. **ProductEventPublisher の実装**
   - Kafkaへのイベント発行機能
   - 非同期処理対応
   - エラーハンドリング

2. **Event Record クラスの実装**
   - ProductCreatedEvent
   - ProductUpdatedEvent
   - ProductDeletedEvent
   - ProductPriceChangedEvent

3. **Product Entity の修正**
   - @EntityListeners の追加
   - 変更追跡フィールドの追加
   - getEquipmentType() メソッドの追加

4. **Kafka依存関係と設定の追加**
   - pom.xml の更新
   - application.properties の設定

### Phase 2 (重要)

1. **ProductService の修正**
   - 明示的イベント発行の実装
   - 価格変更検出ロジック
   - エラーハンドリング強化

2. **Event Status API の実装**
   - ProductEventResource
   - EventAuditService
   - イベント再送信機能

3. **基本的なテストの実装**
   - 単体テスト
   - イベント発行テスト

### Phase 3 (運用改善)

1. **監視・メトリクス機能**
   - ProductEventMetrics
   - ProductEventHealthCheck
   - ダッシュボード連携

2. **統合テストの充実**
   - Kafka統合テスト
   - End-to-End テスト

3. **運用機能の充実**
   - Event Replay 機能
   - バッチ処理対応
   - アラート機能

## 8. 注意事項・制約事項

### 8.1 データ整合性

- **非同期処理**: イベント発行は非同期で行い、商品データの保存処理をブロックしない
- **冪等性**: イベントの重複送信を許容し、Inventory Service側で冪等性を保証する
- **順序保証**: 同一商品のイベントは productId をキーとしてパーティション分散で順序保証
- **リトライ機構**: イベント発行失敗時の自動リトライ機構を実装

### 8.2 性能影響

- **バッチ処理**: 大量の商品更新時にKafkaメッセージングが負荷にならないよう配慮
- **レスポンス時間**: イベント発行によるレスポンス時間への影響を最小限に抑制
- **リソース使用量**: 非同期処理によるメモリ・CPU使用量の監視
- **スループット**: 高負荷時のイベント発行スループットの確保

### 8.3 後方互換性

- **REST API**: 既存のREST APIには影響を与えない
- **データベーススキーマ**: 既存のデータベーススキーマとの互換性維持
- **イベントスキーマ**: イベントスキーマの変更時はバージョニングを適用
- **クライアント影響**: 既存のクライアントアプリケーションへの影響なし

### 8.4 運用考慮事項

- **監視**: イベント発行状況の継続的な監視
- **アラート**: エラー率やレイテンシの閾値監視
- **ログ**: 適切なログレベルでのイベント発行記録
- **ドキュメント**: イベントスキーマと運用手順のドキュメント化

## 9. DTOクラスとレスポンス定義

### 9.1 Event関連のDTOクラス

```java
// Event Status Response
public record EventStatusResponse(
    int totalEvents,
    int acknowledgedEvents,
    int pendingEvents,
    List<EventLogEntry> events,
    List<EventAcknowledgment> acknowledgments
) {}

// Event Log Entry
public record EventLogEntry(
    UUID eventId,
    UUID productId,
    String eventType,
    String status,
    LocalDateTime timestamp,
    String errorMessage
) {}

// Event Acknowledgment
public record EventAcknowledgment(
    UUID eventId,
    String consumerService,
    LocalDateTime acknowledgedAt,
    String status
) {}

// Event System Health Response
public record EventSystemHealthResponse(
    boolean kafkaHealthy,
    long totalEvents,
    long successfulEvents,
    long failedEvents,
    double errorRate,
    LocalDateTime lastChecked
) {}

// Event Statistics
public record EventStatistics(
    long totalEvents,
    long successfulEvents,
    long failedEvents
) {}

// Product Update Request (既存の拡張)
public record ProductUpdateRequest(
    String name,
    String description,
    String shortDescription,
    UUID categoryId,
    UUID brandId,
    BigDecimal basePrice,
    BigDecimal salePrice,
    Map<String, String> additionalSpecs
) {}
```

### 9.2 例外クラス

```java
// Category Not Found Exception
public class CategoryNotFoundException extends ProductCatalogException {
    public CategoryNotFoundException(UUID categoryId) {
        super("CATEGORY_NOT_FOUND", 
              "指定されたカテゴリが見つかりません: " + categoryId, 404);
        withDetail("categoryId", categoryId);
    }
}

// Brand Not Found Exception  
public class BrandNotFoundException extends ProductCatalogException {
    public BrandNotFoundException(UUID brandId) {
        super("BRAND_NOT_FOUND",
              "指定されたブランドが見つかりません: " + brandId, 404);
        withDetail("brandId", brandId);
    }
}

// Event Publishing Exception
public class EventPublishingException extends ProductCatalogException {
    public EventPublishingException(String message, Throwable cause) {
        super("EVENT_PUBLISHING_ERROR",
              "イベント発行に失敗しました: " + message, 500);
        initCause(cause);
    }
}
```

この実装により、Product Catalog ServiceとInventory Management Service間のEvent-Driven連携が実現され、データの整合性を保ちながら疎結合なアーキテクチャを構築できます。在庫管理サービスは商品情報のキャッシュを適切に維持し、リアルタイムでの在庫管理業務をサポートできるようになります。
