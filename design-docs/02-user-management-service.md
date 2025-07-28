# User Management Service 詳細設計書

## 目次

1. [概要](#概要)
2. [技術仕様](#技術仕様)
3. [アーキテクチャ設計](#アーキテクチャ設計)
4. [API設計](#api設計)
5. [データベース設計](#データベース設計)
6. [エラー処理](#エラー処理)
7. [セキュリティ設計](#security-design)
8. [テスト設計](#テスト設計)
9. [ローカル開発環境](#ローカル開発環境)
10. [本番デプロイメント](#production-deployment)
11. [監視・運用](#監視運用)
12. [障害対応](#障害対応)

## 概要

### サービス概要

User Management Serviceは、スキー用品販売ショップサイトのユーザー管理機能を担当するマイクロサービスです。ユーザーの登録、認証、プロファイル管理、設定管理など、ユーザーに関する全ての機能を提供します。

### 主要責務

- **ユーザー登録・認証**: 新規ユーザー登録、ログイン・ログアウト処理
- **プロファイル管理**: ユーザー基本情報、住所、連絡先の管理
- **設定管理**: アカウント設定、プライバシー設定、通知設定
- **権限管理**: ユーザーロール、権限の管理
- **セッション管理**: ユーザーセッションの管理
- **監査機能**: ユーザー操作の監査ログ管理

### ビジネス価値

- **顧客体験向上**: スムーズな登録・ログイン体験
- **パーソナライゼーション**: ユーザー属性に基づくカスタマイズ
- **セキュリティ**: 堅牢なユーザー管理とアクセス制御
- **GDPR対応**: 個人データ保護規制への準拠

## 技術仕様

### 使用技術スタック

| 技術領域 | 技術/ライブラリ | バージョン | 用途 |
|---------|----------------|-----------|------|
| **Runtime** | OpenJDK | 21 LTS | Java実行環境 |
| **Framework** | Jakarta EE | 11 | エンタープライズフレームワーク |
| **Application Server** | WildFly | 31.0.1 | Jakarta EEアプリケーションサーバー |
| **Persistence** | Jakarta Persistence (JPA) | 3.2 | ORM |
| **Data Access** | Jakarta Data | 1.0 | Repository抽象化 |
| **REST API** | Jakarta REST (JAX-RS) | 4.0 | RESTful Web Services |
| **CDI** | Jakarta CDI | 4.1 | 依存性注入・管理 |
| **Validation** | Jakarta Validation | 3.1 | Bean Validation |
| **Security** | Jakarta Security | 3.0 | セキュリティ |
| **JSON Processing** | Jakarta JSON-P | 2.1 | JSON処理 |
| **Database** | PostgreSQL | 16 | 主データベース |
| **Cache** | Redis | 7.2 | セッション・キャッシュ |
| **Message Queue** | Apache Kafka | 3.7 | 非同期イベント処理 |
| **Password Hashing** | BCrypt | 0.10.2 | パスワードハッシュ化 |
| **Monitoring** | MicroProfile Metrics | 5.1 | メトリクス収集 |
| **Tracing** | MicroProfile OpenTelemetry | 2.0 | 分散トレーシング |
| **Health Check** | MicroProfile Health | 4.0 | ヘルスチェック |
| **Configuration** | MicroProfile Config | 3.1 | 設定管理 |

### 除外技術

- **Lombok**: Jakarta EE 11のRecord クラスとモダンJava機能を活用するため使用しません

### Java 21 LTS 活用機能

- **Virtual Threads**: 高並行処理による性能向上
- **Record Classes**: 不変データ構造の簡潔な実装
- **Pattern Matching**: 型安全で可読性の高いコード
- **Text Blocks**: 複雑なSQL・JSON定義の可読性向上
- **Sealed Classes**: 型安全性の向上

## アーキテクチャ設計

### システムアーキテクチャ図

```mermaid
graph TB
    subgraph "External Clients"
        WEB[Web Frontend]
        MOBILE[Mobile App]
        ADMIN[Admin Portal]
    end
    
    subgraph "API Gateway Layer"
        GATEWAY[API Gateway]
    end
    
    subgraph "User Management Service"
        REST[REST Controller]
        AUTH[Authentication Service]
        PROFILE[Profile Service]
        PREF[Preference Service]
        AUDIT[Audit Service]
    end
    
    subgraph "Data Layer"
        POSTGRES[(PostgreSQL)]
        REDIS[(Redis Cache)]
        KAFKA[Kafka Topics]
    end
    
    subgraph "External Services"
        EMAIL[Email Service]
        SMS[SMS Service]
        KYC[KYC Service]
    end
    
    WEB --> GATEWAY
    MOBILE --> GATEWAY
    ADMIN --> GATEWAY
    
    GATEWAY --> REST
    
    REST --> AUTH
    REST --> PROFILE
    REST --> PREF
    REST --> AUDIT
    
    AUTH --> POSTGRES
    PROFILE --> POSTGRES
    PREF --> REDIS
    AUDIT --> KAFKA
    
    AUTH --> EMAIL
    AUTH --> SMS
    PROFILE --> KYC
```

### ドメインモデル設計

```java
// ユーザーエンティティ（Jakarta EE 11 Record活用）
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    
    @Embedded
    private PersonalInfo personalInfo;
    
    @Embedded
    private AccountStatus accountStatus;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserPreference> preferences = new ArrayList<>();
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Virtual Threads対応の非同期メソッド
    @Asynchronous
    public CompletableFuture<List<Order>> getUserOrdersAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // 注文サービス連携（非同期）
            return orderServiceClient.getOrdersByUserId(this.id);
        });
    }
}

// Record ベース Value Objects
public record PersonalInfo(
    String firstName,
    String lastName,
    String firstNameKana,
    String lastNameKana,
    LocalDate birthDate,
    Gender gender,
    String phoneNumber
) {
    public String getFullName() {
        return String.format("%s %s", firstName, lastName);
    }
    
    public String getFullNameKana() {
        return String.format("%s %s", firstNameKana, lastNameKana);
    }
}

public record AccountStatus(
    UserStatus status,
    boolean emailVerified,
    boolean phoneVerified,
    LocalDateTime lastLoginAt,
    int loginFailureCount,
    LocalDateTime lockoutUntil
) {
    public boolean isLocked() {
        return lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now());
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !isLocked();
    }
}

// Sealed Classes for Type Safety
public sealed interface UserEvent
    permits UserRegisteredEvent, UserProfileUpdatedEvent, UserDeactivatedEvent {
}

public record UserRegisteredEvent(
    UUID userId,
    String email,
    LocalDateTime registeredAt
) implements UserEvent {}

public record UserProfileUpdatedEvent(
    UUID userId,
    String fieldName,
    String oldValue,
    String newValue,
    LocalDateTime updatedAt
) implements UserEvent {}

// Enums
public enum UserStatus {
    PENDING_VERIFICATION,
    ACTIVE, 
    SUSPENDED,
    DEACTIVATED,
    DELETED
}

public enum Gender {
    MALE, FEMALE, OTHER, NOT_SPECIFIED
}
```

### レイヤードアーキテクチャ

```java
// REST Controller Layer
@Path("/users")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    
    @Inject
    private UserService userService;
    
    @Inject
    private UserMapper userMapper;
    
    @POST
    @Path("/register")
    @Valid
    public Response registerUser(@Valid UserRegistrationRequest request) {
        try {
            var user = userService.registerUser(request);
            var response = userMapper.toResponse(user);
            
            return Response.status(Response.Status.CREATED)
                .entity(response)
                .location(URI.create("/users/" + user.getId()))
                .build();
                
        } catch (EmailAlreadyExistsException e) {
                .build();
        }
    }
}
```

## セキュリティ設計 {#security-design}

### パスワード管理

```java
// パスワードエンコーディングサービス
@ApplicationScoped
public class PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder;
    
    public PasswordEncoder() {
        this.encoder = new BCryptPasswordEncoder(12); // より強力な暗号化強度
    }
    
    public String encode(String rawPassword) {
        validatePasswordStrength(rawPassword);
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    private void validatePasswordStrength(String password) {
        var violations = new ArrayList<String>();
        
        if (password.length() < 8) {
            violations.add("パスワードは8文字以上である必要があります");
        }
        
        if (!password.matches(".*[a-z].*")) {
            violations.add("小文字を含む必要があります");
        }
        
        if (!password.matches(".*[A-Z].*")) {
            violations.add("大文字を含む必要があります");
        }
        
        if (!password.matches(".*\\d.*")) {
            violations.add("数字を含む必要があります");
        }
        
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?].*")) {
            violations.add("特殊文字を含む必要があります");
        }
        
        if (!violations.isEmpty()) {
            throw new PasswordValidationException(violations);
        }
    }
}

// セッション管理サービス
@ApplicationScoped
public class SessionManagementService {
    
    @Inject
    private RedisTemplate redisTemplate;
    
    @Inject
    @ConfigProperty(name = "session.timeout.minutes", defaultValue = "30")
    private Integer sessionTimeoutMinutes;
    
    @Inject
    @ConfigProperty(name = "session.max.concurrent", defaultValue = "5")
    private Integer maxConcurrentSessions;
    
    public UserSession createSession(User user, String ipAddress, String userAgent) {
        // 既存セッション数チェック
        limitConcurrentSessions(user.getId());
        
        var sessionToken = generateSecureToken();
        var deviceInfo = parseDeviceInfo(userAgent);
        var expiresAt = LocalDateTime.now().plusMinutes(sessionTimeoutMinutes);
        
        var session = new UserSession(
            UUID.randomUUID(),
            user.getId(),
            sessionToken,
            deviceInfo,
            ipAddress,
            expiresAt,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        // Redisにセッション保存
        var sessionKey = "session:" + sessionToken;
        redisTemplate.opsForValue().set(
            sessionKey, 
            session, 
            Duration.ofMinutes(sessionTimeoutMinutes)
        );
        
        // ユーザー別セッションリスト更新
        var userSessionsKey = "user_sessions:" + user.getId();
        redisTemplate.opsForSet().add(userSessionsKey, sessionToken);
        redisTemplate.expire(userSessionsKey, Duration.ofHours(24));
        
        return session;
    }
    
    public Optional<UserSession> validateSession(String sessionToken) {
        var sessionKey = "session:" + sessionToken;
        var session = redisTemplate.opsForValue().get(sessionKey);
        
        if (session != null && !session.isExpired()) {
            // 最終アクセス時刻更新
            updateLastAccessed(sessionToken);
            return Optional.of(session);
        }
        
        return Optional.empty();
    }
    
    private void limitConcurrentSessions(UUID userId) {
        var userSessionsKey = "user_sessions:" + userId;
        var sessionTokens = redisTemplate.opsForSet().members(userSessionsKey);
        
        if (sessionTokens.size() >= maxConcurrentSessions) {
            // 最も古いセッションを削除
            var oldestSession = findOldestSession(sessionTokens);
            if (oldestSession != null) {
                invalidateSession(oldestSession);
            }
        }
    }
}
```

### JWT実装

```java
// JWT サービス
@ApplicationScoped
public class JwtService {
    
    @Inject
    @ConfigProperty(name = "jwt.secret")
    private String jwtSecret;
    
    @Inject
    @ConfigProperty(name = "jwt.issuer")
    private String jwtIssuer;
    
    @Inject
    @ConfigProperty(name = "jwt.expiration.hours", defaultValue = "2")
    private Integer jwtExpirationHours;
    
    private Key signingKey;
    
    @PostConstruct
    void initializeKey() {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    
    public String generateToken(User user) {
        var now = Instant.now();
        var expiration = now.plus(jwtExpirationHours, ChronoUnit.HOURS);
        
        return Jwts.builder()
            .setIssuer(jwtIssuer)
            .setSubject(user.getId().toString())
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .claim("email", user.getEmail())
            .claim("roles", extractUserRoles(user))
            .claim("permissions", extractUserPermissions(user))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }
    
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(jwtIssuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException e) {
            throw new InvalidCredentialsException();
        }
    }
    
    private Set<String> extractUserRoles(User user) {
        // ユーザーロール抽出ロジック
        return Set.of("USER"); // 基本実装
    }
    
    private Set<String> extractUserPermissions(User user) {
        // ユーザー権限抽出ロジック
        return Set.of("READ_PROFILE", "UPDATE_PROFILE");
    }
}
```

### セキュリティフィルター

```java
// 認証フィルター
@Provider
@PreMatching
public class AuthenticationFilter implements ContainerRequestFilter {
    
    @Inject
    private JwtService jwtService;
    
    @Inject
    private SessionManagementService sessionService;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        var path = requestContext.getUriInfo().getPath();
        
        // パブリックエンドポイントはスキップ
        if (isPublicEndpoint(path)) {
            return;
        }
        
        var authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            abortWithUnauthorized(requestContext);
            return;
        }
        
        var token = authHeader.substring("Bearer ".length());
        
        try {
            var claims = jwtService.validateToken(token);
            var userPrincipal = createUserPrincipal(claims);
            
            // セキュリティコンテキスト設定
            requestContext.setSecurityContext(new UserSecurityContext(userPrincipal));
            
        } catch (Exception e) {
            abortWithUnauthorized(requestContext);
        }
    }
    
    private boolean isPublicEndpoint(String path) {
        return path.matches("/(health|metrics|register|login|forgot-password).*");
    }
    
    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        requestContext.abortWith(
            Response.status(Response.Status.UNAUTHORIZED)
                .entity(new ErrorResponse(
                    "https://api.ski-shop.com/problems/unauthorized",
                    "Unauthorized",
                    401,
                    "認証が必要です",
                    requestContext.getUriInfo().getPath(),
                    LocalDateTime.now(),
                    Collections.emptyMap()
                ))
                .build()
        );
    }
}

// セキュリティコンテキスト
public class UserSecurityContext implements SecurityContext {
    private final UserPrincipal userPrincipal;
    
    public UserSecurityContext(UserPrincipal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }
    
    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }
    
    @Override
    public boolean isUserInRole(String role) {
        return userPrincipal.hasRole(role);
    }
    
    @Override
    public boolean isSecure() {
        return true;
    }
    
    @Override
    public String getAuthenticationScheme() {
        return "BEARER";
    }
}
```

## テスト設計

### 単体テスト

```java
// UserService テスト
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @InjectMocks
    private UserService userService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private UserEventPublisher eventPublisher;
    
    @Test
    @DisplayName("有効なリクエストでユーザー登録が成功する")
    void shouldRegisterUser_WhenValidRequest() {
        // Given
        var request = new UserRegistrationRequest(
            "test@example.com",
            "SecurePass123!",
            "太郎",
            "田中",
            "タロウ",
            "タナカ",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "090-1234-5678"
        );
        
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            var user = (User) invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        
        // When
        var result = userService.registerUser(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(request.email());
        assertThat(result.getPersonalInfo().firstName()).isEqualTo(request.firstName());
        
        verify(userRepository).save(any(User.class));
        verify(eventPublisher).publish(any(UserRegisteredEvent.class));
    }
    
    @Test
    @DisplayName("既存メールアドレスでの登録時に例外が発生する")
    void shouldThrowException_WhenEmailAlreadyExists() {
        // Given
        var request = new UserRegistrationRequest(
            "existing@example.com",
            "SecurePass123!",
            "太郎",
            "田中",
            "タロウ",
            "タナカ",
            LocalDate.of(1990, 1, 1),
            Gender.MALE,
            "090-1234-5678"
        );
        
        var existingUser = new User();
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(existingUser));
        
        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(EmailAlreadyExistsException.class)
            .hasMessageContaining("既に使用されています");
        
        verify(userRepository, never()).save(any(User.class));
    }
}

// 統合テスト
@QuarkusIntegrationTest
@TestProfile(IntegrationTestProfile.class)
@TestMethodOrder(OrderAnnotation.class)
class UserManagementIntegrationTest {
    
    @Test
    @Order(1)
    @DisplayName("ユーザー登録APIが正常に動作する")
    void shouldRegisterUserSuccessfully() {
        var request = """
            {
                "email": "integration-test@example.com",
                "password": "SecurePass123!",
                "firstName": "統合",
                "lastName": "テスト",
                "firstNameKana": "トウゴウ",
                "lastNameKana": "テスト",
                "birthDate": "1990-01-01",
                "gender": "MALE",
                "phoneNumber": "090-1234-5678"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/users/register")
        .then()
            .statusCode(201)
            .body("email", equalTo("integration-test@example.com"))
            .body("firstName", equalTo("統合"))
            .body("status", equalTo("PENDING_VERIFICATION"));
    }
    
    @Test
    @Order(2)
    @DisplayName("登録されたユーザーでログインできる")
    void shouldLoginWithRegisteredUser() {
        var loginRequest = """
            {
                "email": "integration-test@example.com",
                "password": "SecurePass123!"
            }
            """;
        
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            .statusCode(200)
            .body("accessToken", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .body("expiresIn", greaterThan(0));
    }
}
```

### パフォーマンステスト

```java
@Component
public class UserManagementLoadTest {
    
    @Test
    public void registrationLoadTest() {
        var scenario = Scenario.builder()
            .name("User Registration Load Test")
            .users(500)
            .rampUp(Duration.ofMinutes(2))
            .duration(Duration.ofMinutes(5))
            .protocol(http.baseUrl("http://localhost:8081"))
            .exec(
                http("register_user")
                    .post("/users/register")
                    .header("Content-Type", "application/json")
                    .body(StringBody(session -> generateRandomUserRegistration()))
                    .check(status().is(201))
            )
            .build();
            
        var simulation = Simulation.builder()
            .scenarios(scenario)
            .checks(
                Check.responseTime().percentile(95).lessThan(1000),
                Check.successRate().greaterThan(99.0)
            )
            .build();
            
        simulation.run();
    }
    
    private String generateRandomUserRegistration() {
        var random = new Random();
        var email = "load-test-" + random.nextInt(100000) + "@example.com";
        
        return String.format("""
            {
                "email": "%s",
                "password": "LoadTest123!",
                "firstName": "負荷",
                "lastName": "テスト",
                "firstNameKana": "フカ",
                "lastNameKana": "テスト",
                "birthDate": "1990-01-01",
                "gender": "MALE",
                "phoneNumber": "090-1234-5678"
            }
            """, email);
    }
}
```

## ローカル開発環境

### Docker Compose設定

```yaml
# docker-compose.yml
version: '3.9'

services:
  user-management:
    build:
      context: .
      dockerfile: Dockerfile.dev
    ports:
      - "8081:8081"
      - "9991:9990"  # WildFly Admin Console
    environment:
      - DATABASE_URL=jdbc:postgresql://postgres:5432/user_db
      - DATABASE_USER=user_service
      - DATABASE_PASSWORD=user_pass
      - REDIS_URL=redis://redis:6379
      - KAFKA_BOOTSTRAP_SERVERS=kafka:9092
      - JWT_SECRET=dev-jwt-secret-key-change-in-production
      - JWT_ISSUER=ski-shop-dev
      - LOG_LEVEL=DEBUG
      - SESSION_TIMEOUT_MINUTES=30
    volumes:
      - ./src:/app/src
      - ./config:/app/config
      - user_logs:/app/logs
    depends_on:
      - postgres
      - redis
      - kafka
    networks:
      - ski-shop-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  postgres:
    image: postgres:16-alpine
    environment:
      - POSTGRES_DB=user_db
      - POSTGRES_USER=user_service
      - POSTGRES_PASSWORD=user_pass
    ports:
      - "5433:5432"
    volumes:
      - postgres_user_data:/var/lib/postgresql/data
      - ./scripts/init-user-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    networks:
      - ski-shop-network

  redis:
    image: redis:7-alpine
    command: redis-server --appendonly yes
    ports:
      - "6380:6379"
    volumes:
      - redis_user_data:/data
    networks:
      - ski-shop-network

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9093:9092"
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true
    depends_on:
      - zookeeper
    networks:
      - ski-shop-network

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    networks:
      - ski-shop-network

volumes:
  postgres_user_data:
  redis_user_data:
  user_logs:

networks:
  ski-shop-network:
    driver: bridge
```

### 開発用設定

```bash
#!/bin/bash
# run-user-service.sh

echo "Starting User Management Service locally..."

# 1. 環境変数設定
export JAVA_OPTS="-server \
                  -XX:+UseZGC \
                  -XX:+UnlockExperimentalVMOptions \
                  --enable-preview \
                  -Xms256m \
                  -Xmx1g \
                  -Djboss.bind.address=0.0.0.0 \
                  -Djboss.bind.address.management=0.0.0.0"

# 2. デバッグモード（オプション）
if [ "$1" = "debug" ]; then
    export JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"
    echo "Debug mode enabled on port 5006"
fi

# 3. データベース準備
echo "Preparing database..."
docker-compose up -d postgres redis kafka
sleep 20

# 4. データベース初期化
echo "Initializing database schema..."
mvn flyway:migrate -Dflyway.configFiles=src/main/resources/db/flyway.conf

# 5. テストデータ投入
echo "Loading test data..."
mvn exec:java -Dexec.mainClass="com.skishop.user.dev.TestDataLoader"

# 6. アプリケーション起動
echo "Starting User Management Service..."
docker-compose up --build user-management

echo "User Management Service is ready at http://localhost:8081"
```

## 本番デプロイメント {#production-deployment}

### Azure Container Apps設定

```bicep
// user-management.bicep
param environment string = 'production'
param location string = resourceGroup().location

// Container Apps Environment
resource containerAppsEnv 'Microsoft.App/managedEnvironments@2023-05-01' existing = {
  name: 'ski-shop-${environment}'
}

// User Management Container App
resource userManagementApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: 'user-management'
  location: location
  properties: {
    managedEnvironmentId: containerAppsEnv.id
    configuration: {
      activeRevisionsMode: 'Multiple'
      ingress: {
        external: false  // Internal service
        targetPort: 8081
        allowInsecure: false
        traffic: [
          {
            revisionName: 'user-management--latest'
            weight: 100
          }
        ]
      }
      secrets: [
        {
          name: 'database-connection-string'
          value: 'postgresql://${postgresServer.name}.postgres.database.azure.com:5432/user_db'
        }
        {
          name: 'database-password'
          keyVaultUrl: keyVault.properties.vaultUri
          identity: managedIdentity.id
        }
        {
          name: 'redis-connection-string'
          value: '${redisCache.properties.hostName}:6380,password=${redisCache.listKeys().primaryKey},ssl=true'
        }
        {
          name: 'jwt-secret'
          keyVaultUrl: keyVault.properties.vaultUri
          identity: managedIdentity.id
        }
      ]
      registries: [
        {
          server: '${containerRegistry.name}.azurecr.io'
          identity: managedIdentity.id
        }
      ]
    }
    template: {
      containers: [
        {
          name: 'user-management'
          image: '${containerRegistry.name}.azurecr.io/user-management:latest'
          env: [
            {
              name: 'DATABASE_URL'
              secretRef: 'database-connection-string'
            }
            {
              name: 'DATABASE_PASSWORD'
              secretRef: 'database-password'
            }
            {
              name: 'REDIS_URL'
              secretRef: 'redis-connection-string'
            }
            {
              name: 'JWT_SECRET'
              secretRef: 'jwt-secret'
            }
            {
              name: 'ENVIRONMENT'
              value: environment
            }
            {
              name: 'LOG_LEVEL'
              value: 'INFO'
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
                port: 8081
              }
              initialDelaySeconds: 60
              periodSeconds: 30
              timeoutSeconds: 10
              failureThreshold: 3
            }
            {
              type: 'Readiness'
              httpGet: {
                path: '/health/ready'
                port: 8081
              }
              initialDelaySeconds: 30
              periodSeconds: 10
              timeoutSeconds: 5
              failureThreshold: 3
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
          {
            name: 'memory-scaling'
            custom: {
              type: 'memory'
              metadata: {
                type: 'Utilization'
                value: '80'
              }
            }
          }
        ]
      }
    }
  }
}
```

### CI/CD Pipeline

```yaml
# .github/workflows/deploy-user-management.yml
name: Deploy User Management Service

on:
  push:
    branches: [main]
    paths: ['user-management/**']

env:
  AZURE_RESOURCE_GROUP: ski-shop-rg
  CONTAINER_REGISTRY: skishopregistry
  APP_NAME: user-management

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: test_user_db
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_pass
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
      
      redis:
        image: redis:7
        options: >-
          --health-cmd "redis-cli ping"
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 6379:6379

    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          
      - name: Run Unit Tests
        run: |
          cd user-management
          mvn clean test
          
      - name: Run Integration Tests
        env:
          DATABASE_URL: jdbc:postgresql://localhost:5432/test_user_db
          DATABASE_USER: test_user
          DATABASE_PASSWORD: test_pass
          REDIS_URL: redis://localhost:6379
        run: |
          cd user-management
          mvn verify -P integration-tests
          
      - name: Generate Test Reports
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: User Management Tests
          path: 'user-management/target/surefire-reports/*.xml'
          reporter: java-junit

  security-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run OWASP Dependency Check
        run: |
          cd user-management
          mvn org.owasp:dependency-check-maven:check
          
      - name: Upload Security Scan Results
        uses: actions/upload-artifact@v3
        with:
          name: security-scan-results
          path: user-management/target/dependency-check-report.html

  build-and-deploy:
    needs: [test, security-scan]
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
        
      - name: Login to Azure Container Registry
        uses: azure/docker-login@v1
        with:
          login-server: ${{ env.CONTAINER_REGISTRY }}.azurecr.io
          username: ${{ secrets.ACR_USERNAME }}
          password: ${{ secrets.ACR_PASSWORD }}
          
      - name: Build and Push Docker Image
        run: |
          cd user-management
          docker build -t ${{ env.CONTAINER_REGISTRY }}.azurecr.io/${{ env.APP_NAME }}:${{ github.sha }} .
          docker build -t ${{ env.CONTAINER_REGISTRY }}.azurecr.io/${{ env.APP_NAME }}:latest .
          docker push ${{ env.CONTAINER_REGISTRY }}.azurecr.io/${{ env.APP_NAME }}:${{ github.sha }}
          docker push ${{ env.CONTAINER_REGISTRY }}.azurecr.io/${{ env.APP_NAME }}:latest
          
      - name: Login to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}
          
      - name: Deploy to Container Apps
        run: |
          az containerapp update \
            --name ${{ env.APP_NAME }} \
            --resource-group ${{ env.AZURE_RESOURCE_GROUP }} \
            --image ${{ env.CONTAINER_REGISTRY }}.azurecr.io/${{ env.APP_NAME }}:${{ github.sha }}
            
      - name: Verify Deployment
        run: |
          # ヘルスチェック待機
          sleep 60
          
          # Internal URL取得（API Gateway経由でアクセス）
          GATEWAY_URL=$(az containerapp show \
            --name api-gateway \
            --resource-group ${{ env.AZURE_RESOURCE_GROUP }} \
            --query properties.configuration.ingress.fqdn -o tsv)
          
          # ヘルスチェック実行
          curl -f "https://$GATEWAY_URL/api/v1/users/health" || exit 1
```

## 監視・運用

### メトリクス実装

```java
@ApplicationScoped
public class UserManagementMetricsService {
    
    @Inject
    @Metric(name = "user_registrations_total",
            description = "Total number of user registrations")
    private Counter registrationCounter;
    
    @Inject
    @Metric(name = "user_logins_total",
            description = "Total number of user logins")
    private Counter loginCounter;
    
    @Inject
    @Metric(name = "user_login_failures_total",
            description = "Total number of failed login attempts")
    private Counter loginFailureCounter;
    
    @Inject
    @Metric(name = "active_user_sessions",
            description = "Number of active user sessions")
    private Gauge<Integer> activeSessionsGauge;
    
    @Inject
    @Metric(name = "user_operation_duration_seconds",
            description = "Duration of user operations")
    private Timer operationTimer;
    
    public void recordRegistration(String source) {
        registrationCounter.inc(Tags.of("source", source));
    }
    
    public void recordLogin(String method, boolean success) {
        if (success) {
            loginCounter.inc(Tags.of("method", method));
        } else {
            loginFailureCounter.inc(Tags.of("method", method));
        }
    }
    
    public Timer.Sample startOperationTimer(String operation) {
        return Timer.start(
            Tags.of("operation", operation)
        );
    }
    
    public void updateActiveSessionsCount(int count) {
        // Gauge更新（セッション数を定期的に更新）
        activeSessionsGauge.set(count);
    }
}

// ヘルスチェック実装
@ApplicationScoped
@Health
public class UserManagementHealthCheck implements HealthCheck {
    
    @Inject
    private DataSource dataSource;
    
    @Inject
    private RedisTemplate redisTemplate;
    
    @Override
    public HealthCheckResponse call() {
        var builder = HealthCheckResponse.named("user-management");
        
        try {
            // データベース接続確認
            if (!isDatabaseHealthy()) {
                return builder.down()
                    .withData("database", "unhealthy")
                    .build();
            }
            
            // Redis接続確認
            if (!isRedisHealthy()) {
                return builder.down()
                    .withData("redis", "unhealthy")
                    .build();
            }
            
            return builder.up()
                .withData("database", "healthy")
                .withData("redis", "healthy")
                .withData("active_users", getActiveUserCount())
                .withData("uptime", getUptime())
                .build();
                
        } catch (Exception e) {
            return builder.down()
                .withData("error", e.getMessage())
                .build();
        }
    }
    
    private boolean isDatabaseHealthy() {
        try (var connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isRedisHealthy() {
        try {
            redisTemplate.execute(connection -> {
                connection.ping();
                return true;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### ログ設定

```xml
<!-- logback-spring.xml -->
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <springProfile name="local">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <level/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <logger name="com.skishop.user" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
        <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>
        
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>
    
    <springProfile name="production">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/app/logs/user-management.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/app/logs/user-management.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <level/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
        
        <appender name="AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/app/logs/user-audit.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/app/logs/user-audit.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>90</maxHistory>
                <totalSizeCap>10GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <level/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                </providers>
            </encoder>
        </appender>
        
        <logger name="com.skishop.user.audit" level="INFO" additivity="false">
            <appender-ref ref="AUDIT"/>
        </logger>
        
        <root level="WARN">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

## 障害対応

### 障害シナリオと対応手順

#### 1. データベース接続障害

```bash
# 症状確認
curl -f http://user-management:8081/health
# Response: {"status": "DOWN", "checks": [{"name": "database", "status": "DOWN"}]}

# 対応手順
echo "1. データベース接続状況確認"
kubectl logs deployment/user-management -n ski-shop | grep -i database

echo "2. PostgreSQL接続確認"
kubectl exec -it deployment/user-management -n ski-shop -- \
  psql -h postgres -U user_service -d user_db -c "SELECT 1"

echo "3. 接続プール状況確認"
curl http://user-management:8081/metrics | grep -E "(hikari|connection)"

echo "4. 緊急時読み取り専用モード有効化"
kubectl set env deployment/user-management -n ski-shop READ_ONLY_MODE=true
```

#### 2. Redis セッション障害

```bash
# 症状: ログイン済みユーザーが認証エラーになる
echo "Redis接続確認"
kubectl exec -it deployment/user-management -n ski-shop -- redis-cli -h redis ping

# セッション情報確認
kubectl exec -it deployment/user-management -n ski-shop -- \
  redis-cli -h redis keys "session:*" | wc -l

# フォールバック機能有効化（データベースセッション）
kubectl set env deployment/user-management -n ski-shop SESSION_FALLBACK_MODE=database
```

#### 3. 大量登録リクエスト攻撃

```bash
# レート制限確認
curl http://user-management:8081/metrics | grep rate_limit

# 緊急時レート制限強化
kubectl set env deployment/user-management -n ski-shop \
  REGISTRATION_RATE_LIMIT=10 \
  REGISTRATION_RATE_WINDOW=PT1H

# 疑わしいIPアドレスのブロック
kubectl create configmap blocked-ips \
  --from-literal=ips="192.168.1.100,10.0.0.50" \
  -n ski-shop

kubectl set env deployment/user-management -n ski-shop \
  BLOCKED_IPS_CONFIGMAP=blocked-ips
```

#### 4. メモリリーク対応

```bash
# JVMメトリクス確認
curl http://user-management:8081/metrics | grep jvm_memory

# ヒープダンプ取得
kubectl exec -it deployment/user-management-xxx -n ski-shop -- \
  jcmd 1 GC.run_finalization
kubectl exec -it deployment/user-management-xxx -n ski-shop -- \
  jcmd 1 VM.gc

# 緊急時Pod再起動
kubectl rollout restart deployment/user-management -n ski-shop
```

### 自動復旧機能

```java
@ApplicationScoped
public class UserManagementFailureDetectionService {
    
    @Inject
    private NotificationService notificationService;
    
    @Inject
    private UserManagementMetricsService metricsService;
    
    @Schedule(every = "30s")
    public void checkSystemHealth() {
        var healthStatus = performDetailedHealthCheck();
        
        if (healthStatus.isCritical()) {
            var incident = createIncident(healthStatus);
            notificationService.sendCriticalAlert(incident);
            
            // 自動復旧試行
            if (healthStatus.isAutoRecoverable()) {
                performAutoRecovery(healthStatus);
            }
        }
    }
    
    private void performAutoRecovery(HealthStatus status) {
        switch (status.getFailureType()) {
            case DATABASE_CONNECTION -> restartConnectionPool();
            case REDIS_CONNECTION -> enableSessionFallback();
            case HIGH_MEMORY_USAGE -> triggerGarbageCollection();
            case HIGH_ERROR_RATE -> enableCircuitBreaker();
            case SUSPICIOUS_ACTIVITY -> activateRateLimiting();
        }
    }
    
    @Asynchronous
    private void restartConnectionPool() {
        try {
            // 接続プールの再初期化
            dataSourceManager.restart();
            logger.info("Database connection pool restarted successfully");
        } catch (Exception e) {
            logger.error("Failed to restart connection pool", e);
            notificationService.sendAlert("Connection pool restart failed: " + e.getMessage());
        }
    }
}
```

このUser Management Serviceの詳細設計書により、スキー用品ショップサイトのユーザー管理機能の包括的な実装が可能です。続いて、次のマイクロサービス（Product Catalog Service）の詳細設計書を作成いたします。

```java

// CQRS Commands
public sealed interface UserCommand permits RegisterUserCommand, UpdateUserProfileCommand, 
        DeactivateUserCommand, ChangePasswordCommand, UpdateUserPreferencesCommand {}

public record RegisterUserCommand(
    String email,
    String password,
    String firstName,
    String lastName,
    String firstNameKana,
    String lastNameKana,
    LocalDate birthDate,
    Gender gender,
    String phoneNumber,
    Address address
) implements UserCommand {}

public record UpdateUserProfileCommand(
    UUID userId,
    String firstName,
    String lastName,
    String firstNameKana,
    String lastNameKana,
    LocalDate birthDate,
    Gender gender,
    String phoneNumber,
    List<Address> addresses
) implements UserCommand {}

public record DeactivateUserCommand(
    UUID userId,
    String reason,
    UUID deactivatedBy
) implements UserCommand {}

public record ChangePasswordCommand(
    UUID userId,
    String currentPassword,
    String newPassword
) implements UserCommand {}

public record UpdateUserPreferencesCommand(
    UUID userId,
    String language,
    String timezone,
    boolean emailNotifications,
    boolean smsNotifications,
    Map<String, Object> customPreferences
) implements UserCommand {}

// CQRS Queries
public sealed interface UserQuery permits GetUserByIdQuery, GetUserByEmailQuery, 
        GetUsersByRoleQuery, GetUserStatisticsQuery, SearchUsersQuery {}

public record GetUserByIdQuery(
    UUID userId
) implements UserQuery {}

public record GetUserByEmailQuery(
    String email
) implements UserQuery {}

public record GetUsersByRoleQuery(
    String role,
    int page,
    int size
) implements UserQuery {}

public record GetUserStatisticsQuery(
    LocalDate fromDate,
    LocalDate toDate,
    String groupBy
) implements UserQuery {}

public record SearchUsersQuery(
    String searchTerm,
    Set<UserStatus> statuses,
    Set<String> roles,
    int page,
    int size,
    String sortBy,
    String sortDirection
) implements UserQuery {}

// CQRS Projections
public record UserProjection(
    UUID id,
    String email,
    String fullName,
    String fullNameKana,
    UserStatus status,
    List<String> roles,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt
) {}

public record UserDetailsProjection(
    UUID id,
    String email,
    PersonalInfo personalInfo,
    List<Address> addresses,
    UserPreferences preferences,
    AccountStatus accountStatus,
    List<String> roles,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt
) {}

public record UserStatisticsProjection(
    long totalUsers,
    long activeUsers,
    long pendingUsers,
    long deactivatedUsers,
    Map<String, Long> roleDistribution,
    Map<LocalDate, Long> registrationTrend
) {}

// Service Layer with CQRS
@ApplicationScoped
@Transactional
public class UserService {
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private PasswordEncoder passwordEncoder;
    
    @Inject
    private UserEventPublisher eventPublisher;
    
    @Inject
    private UserQueryService queryService;
    
    // Command Handlers
    @CommandHandler
    public UUID handle(RegisterUserCommand command) {
        validateRegistrationRequest(command);
        
        var user = User.builder()
            .email(command.email())
            .passwordHash(passwordEncoder.encode(command.password()))
            .personalInfo(new PersonalInfo(
                command.firstName(),
                command.lastName(),
                command.firstNameKana(),
                command.lastNameKana(),
                command.birthDate(),
                command.gender(),
                command.phoneNumber()
            ))
            .accountStatus(new AccountStatus(
                UserStatus.PENDING_VERIFICATION,
                false,
                false,
                null,
                0,
                null
            ))
            .build();
            
        if (command.address() != null) {
            user.addAddress(command.address());
        }
            
        var savedUser = userRepository.save(user);
        
        // イベント発行
        eventPublisher.publish(new UserRegisteredEvent(
            savedUser.getId(),
            savedUser.getEmail(),
            LocalDateTime.now()
        ));
        
        return savedUser.getId();
    }
    
    @CommandHandler
    public void handle(UpdateUserProfileCommand command) {
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + command.userId()));
        
        user.updatePersonalInfo(new PersonalInfo(
            command.firstName(),
            command.lastName(),
            command.firstNameKana(),
            command.lastNameKana(),
            command.birthDate(),
            command.gender(),
            command.phoneNumber()
        ));
        
        // 住所更新
        if (command.addresses() != null) {
            user.updateAddresses(command.addresses());
        }
        
        userRepository.save(user);
        
        eventPublisher.publish(new UserProfileUpdatedEvent(
            user.getId(),
            user.getEmail(),
            Map.of(
                "firstName", command.firstName(),
                "lastName", command.lastName()
            ),
            LocalDateTime.now()
        ));
    }
    
    @CommandHandler
    public void handle(DeactivateUserCommand command) {
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + command.userId()));
        
        user.deactivate(command.reason());
        userRepository.save(user);
        
        eventPublisher.publish(new UserDeactivatedEvent(
            user.getId(),
            user.getEmail(),
            command.reason(),
            command.deactivatedBy(),
            LocalDateTime.now()
        ));
    }
    
    @CommandHandler
    public void handle(ChangePasswordCommand command) {
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + command.userId()));
        
        if (!passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        
        user.changePassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
        
        eventPublisher.publish(new UserPasswordChangedEvent(
            user.getId(),
            LocalDateTime.now()
        ));
    }
    
    @CommandHandler
    public void handle(UpdateUserPreferencesCommand command) {
        var user = userRepository.findById(command.userId())
            .orElseThrow(() -> new UserNotFoundException("User not found: " + command.userId()));
        
        var preferences = new UserPreferences(
            command.language(),
            command.timezone(),
            command.emailNotifications(),
            command.smsNotifications(),
            command.customPreferences()
        );
        
        user.updatePreferences(preferences);
        userRepository.save(user);
        
        eventPublisher.publish(new UserPreferencesUpdatedEvent(
            user.getId(),
            preferences,
            LocalDateTime.now()
        ));
    }
    
    private void validateRegistrationRequest(RegisterUserCommand command) {
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + command.email());
        }
        
        if (!isValidPassword(command.password())) {
            throw new InvalidPasswordException("Password does not meet requirements");
        }
    }
    
    private boolean isValidPassword(String password) {
        return password.length() >= 8 && 
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*[0-9].*") &&
               password.matches(".*[!@#$%^&*()].*");
    }
}

// Query Service
@ApplicationScoped
public class UserQueryService {
    
    @Inject
    private UserRepository userRepository;
    
    @QueryHandler
    public Optional<UserDetailsProjection> handle(GetUserByIdQuery query) {
        return userRepository.findById(query.userId())
            .map(this::toDetailsProjection);
    }
    
    @QueryHandler
    public Optional<UserProjection> handle(GetUserByEmailQuery query) {
        return userRepository.findByEmail(query.email())
            .map(this::toProjection);
    }
    
    @QueryHandler
    public List<UserProjection> handle(GetUsersByRoleQuery query) {
        return userRepository.findByRole(query.role(), query.page(), query.size())
            .stream()
            .map(this::toProjection)
            .toList();
    }
    
    @QueryHandler
    public UserStatisticsProjection handle(GetUserStatisticsQuery query) {
        var totalUsers = userRepository.countByCreatedAtBetween(
            query.fromDate().atStartOfDay(),
            query.toDate().atTime(LocalTime.MAX)
        );
        
        var activeUsers = userRepository.countByStatusAndCreatedAtBetween(
            UserStatus.ACTIVE,
            query.fromDate().atStartOfDay(),
            query.toDate().atTime(LocalTime.MAX)
        );
        
        var pendingUsers = userRepository.countByStatusAndCreatedAtBetween(
            UserStatus.PENDING_VERIFICATION,
            query.fromDate().atStartOfDay(),
            query.toDate().atTime(LocalTime.MAX)
        );
        
        var deactivatedUsers = userRepository.countByStatusAndCreatedAtBetween(
            UserStatus.DEACTIVATED,
            query.fromDate().atStartOfDay(),
            query.toDate().atTime(LocalTime.MAX)
        );
        
        return new UserStatisticsProjection(
            totalUsers,
            activeUsers,
            pendingUsers,
            deactivatedUsers,
            Map.of(), // Role distribution would be calculated
            Map.of()  // Registration trend would be calculated
        );
    }
    
    @QueryHandler
    public List<UserProjection> handle(SearchUsersQuery query) {
        return userRepository.searchUsers(
            query.searchTerm(),
            query.statuses(),
            query.roles(),
            query.page(),
            query.size(),
            query.sortBy(),
            query.sortDirection()
        ).stream()
        .map(this::toProjection)
        .toList();
    }
    
    private UserProjection toProjection(User user) {
        return new UserProjection(
            user.getId(),
            user.getEmail(),
            user.getPersonalInfo().getFullName(),
            user.getPersonalInfo().getFullNameKana(),
            user.getAccountStatus().getStatus(),
            user.getRoles().stream().map(Role::getName).toList(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
    
    private UserDetailsProjection toDetailsProjection(User user) {
        return new UserDetailsProjection(
            user.getId(),
            user.getEmail(),
            user.getPersonalInfo(),
            user.getAddresses(),
            user.getPreferences(),
            user.getAccountStatus(),
            user.getRoles().stream().map(Role::getName).toList(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoginAt()
        );
    }
}

// User Management Saga Patterns
@ApplicationScoped
@Transactional
public class UserRegistrationSaga {
    
    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationSaga.class);
    
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private SagaStateRepository sagaStateRepository;
    
    @Inject
    private UserEventPublisher eventPublisher;
    
    @Inject
    private EmailService emailService;
    
    @Inject
    private LoyaltyService loyaltyService;
    
    public CompletableFuture<SagaResult> processUserRegistration(
            RegisterUserCommand command) {
        
        return CompletableFuture.supplyAsync(() -> {
            var sagaId = UUID.randomUUID();
            var sagaState = new SagaState(sagaId, command.email(), SagaType.USER_REGISTRATION);
            sagaStateRepository.save(sagaState);
            
            try {
                logger.info("ユーザー登録Saga開始: sagaId={}, email={}", sagaId, command.email());
                
                // Step 1: ユーザー作成
                var userResult = createUserAccount(command);
                if (!userResult.success()) {
                    return handleSagaFailure(sagaState, "ユーザー作成失敗", userResult.message());
                }
                
                sagaState.markStepCompleted("user_created", userResult.userId().toString());
                sagaStateRepository.save(sagaState);
                
                // Step 2: 確認メール送信
                var emailResult = sendVerificationEmail(userResult.userId(), command.email());
                if (!emailResult.success()) {
                    // 補償アクション: ユーザー削除
                    compensateUserCreation(userResult.userId());
                    return handleSagaFailure(sagaState, "確認メール送信失敗", emailResult.message());
                }
                
                sagaState.markStepCompleted("verification_email_sent", emailResult.verificationToken());
                sagaStateRepository.save(sagaState);
                
                // Step 3: ロイヤルティアカウント作成
                var loyaltyResult = createLoyaltyAccount(userResult.userId());
                if (!loyaltyResult.success()) {
                    // 補償アクション: メール無効化、ユーザー削除
                    compensateEmailVerification(emailResult.verificationToken());
                    compensateUserCreation(userResult.userId());
                    return handleSagaFailure(sagaState, "ロイヤルティアカウント作成失敗", loyaltyResult.message());
                }
                
                sagaState.markStepCompleted("loyalty_account_created", loyaltyResult.loyaltyAccountId().toString());
                sagaState.complete();
                sagaStateRepository.save(sagaState);
                
                // 完了イベント発行
                eventPublisher.publish(new UserRegistrationCompletedEvent(
                    userResult.userId(),
                    command.email(),
                    loyaltyResult.loyaltyAccountId(),
                    LocalDateTime.now()
                ));
                
                logger.info("ユーザー登録Saga完了: sagaId={}, userId={}", sagaId, userResult.userId());
                return new SagaResult(true, "ユーザー登録完了");
                
            } catch (Exception e) {
                logger.error("ユーザー登録Saga実行エラー: sagaId=" + sagaId, e);
                return handleSagaFailure(sagaState, "予期しないエラー", e.getMessage());
            }
        }, VirtualThread.ofVirtual().factory());
    }
    
    public CompletableFuture<SagaResult> processUserDeactivation(
            UUID userId, String reason, UUID deactivatedBy) {
        
        return CompletableFuture.supplyAsync(() -> {
            var sagaId = UUID.randomUUID();
            var sagaState = new SagaState(sagaId, userId.toString(), SagaType.USER_DEACTIVATION);
            sagaStateRepository.save(sagaState);
            
            try {
                logger.info("ユーザー無効化Saga開始: sagaId={}, userId={}", sagaId, userId);
                
                // Step 1: アクティブセッション無効化
                var sessionResult = invalidateUserSessions(userId);
                if (!sessionResult.success()) {
                    return handleSagaFailure(sagaState, "セッション無効化失敗", sessionResult.message());
                }
                
                sagaState.markStepCompleted("sessions_invalidated", sessionResult.sessionCount().toString());
                sagaStateRepository.save(sagaState);
                
                // Step 2: ユーザーアカウント無効化
                var userResult = deactivateUserAccount(userId, reason);
                if (!userResult.success()) {
                    // 補償アクション: セッション復元（通常は不要）
                    return handleSagaFailure(sagaState, "ユーザー無効化失敗", userResult.message());
                }
                
                sagaState.markStepCompleted("user_deactivated", userId.toString());
                sagaStateRepository.save(sagaState);
                
                // Step 3: 関連サービスへの通知
                var notificationResult = notifyDeactivationToServices(userId);
                if (!notificationResult.success()) {
                    // 補償アクション: ユーザー再有効化
                    compensateUserDeactivation(userId);
                    return handleSagaFailure(sagaState, "サービス通知失敗", notificationResult.message());
                }
                
                sagaState.markStepCompleted("services_notified", notificationResult.notifiedServices().toString());
                sagaState.complete();
                sagaStateRepository.save(sagaState);
                
                // 完了イベント発行
                eventPublisher.publish(new UserDeactivationCompletedEvent(
                    userId,
                    reason,
                    deactivatedBy,
                    LocalDateTime.now()
                ));
                
                logger.info("ユーザー無効化Saga完了: sagaId={}, userId={}", sagaId, userId);
                return new SagaResult(true, "ユーザー無効化完了");
                
            } catch (Exception e) {
                logger.error("ユーザー無効化Saga実行エラー: sagaId=" + sagaId, e);
                return handleSagaFailure(sagaState, "予期しないエラー", e.getMessage());
            }
        }, VirtualThread.ofVirtual().factory());
    }
    
    // プライベートヘルパーメソッド
    private UserCreationResult createUserAccount(RegisterUserCommand command) {
        try {
            var user = User.builder()
                .email(command.email())
                .passwordHash(passwordEncoder.encode(command.password()))
                .personalInfo(new PersonalInfo(
                    command.firstName(),
                    command.lastName(),
                    command.firstNameKana(),
                    command.lastNameKana(),
                    command.birthDate(),
                    command.gender(),
                    command.phoneNumber()
                ))
                .accountStatus(new AccountStatus(
                    UserStatus.PENDING_VERIFICATION,
                    false,
                    false,
                    null,
                    0,
                    null
                ))
                .build();
                
            if (command.address() != null) {
                user.addAddress(command.address());
            }
                
            var savedUser = userRepository.save(user);
            return new UserCreationResult(true, savedUser.getId(), "ユーザー作成完了");
            
        } catch (Exception e) {
            return new UserCreationResult(false, null, "ユーザー作成エラー: " + e.getMessage());
        }
    }
    
    private EmailResult sendVerificationEmail(UUID userId, String email) {
        try {
            var verificationToken = UUID.randomUUID().toString();
            emailService.sendVerificationEmail(email, verificationToken);
            return new EmailResult(true, verificationToken, "確認メール送信完了");
            
        } catch (Exception e) {
            return new EmailResult(false, null, "メール送信エラー: " + e.getMessage());
        }
    }
    
    private LoyaltyResult createLoyaltyAccount(UUID userId) {
        try {
            var loyaltyAccountId = loyaltyService.createAccount(userId);
            return new LoyaltyResult(true, loyaltyAccountId, "ロイヤルティアカウント作成完了");
            
        } catch (Exception e) {
            return new LoyaltyResult(false, null, "ロイヤルティアカウント作成エラー: " + e.getMessage());
        }
    }
    
    private SessionResult invalidateUserSessions(UUID userId) {
        try {
            var sessionCount = sessionService.invalidateAllUserSessions(userId);
            return new SessionResult(true, sessionCount, "セッション無効化完了");
            
        } catch (Exception e) {
            return new SessionResult(false, 0, "セッション無効化エラー: " + e.getMessage());
        }
    }
    
    private UserDeactivationResult deactivateUserAccount(UUID userId, String reason) {
        try {
            var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            user.deactivate(reason);
            userRepository.save(user);
            
            return new UserDeactivationResult(true, "ユーザー無効化完了");
            
        } catch (Exception e) {
            return new UserDeactivationResult(false, "ユーザー無効化エラー: " + e.getMessage());
        }
    }
    
    private NotificationResult notifyDeactivationToServices(UUID userId) {
        try {
            var notifiedServices = List.of("order", "cart", "loyalty", "payment");
            
            eventPublisher.publish(new UserDeactivatedEvent(
                userId,
                null, // email will be filled by event handler
                "Account deactivated",
                null, // deactivatedBy will be filled
                LocalDateTime.now()
            ));
            
            return new NotificationResult(true, notifiedServices, "サービス通知完了");
            
        } catch (Exception e) {
            return new NotificationResult(false, List.of(), "サービス通知エラー: " + e.getMessage());
        }
    }
    
    // 補償アクション
    private void compensateUserCreation(UUID userId) {
        try {
            userRepository.deleteById(userId);
            logger.info("ユーザー削除補償完了: userId={}", userId);
        } catch (Exception e) {
            logger.error("ユーザー削除補償エラー: userId=" + userId, e);
        }
    }
    
    private void compensateEmailVerification(String verificationToken) {
        try {
            emailService.invalidateVerificationToken(verificationToken);
            logger.info("確認メール無効化補償完了: token={}", verificationToken);
        } catch (Exception e) {
            logger.error("確認メール無効化補償エラー: token=" + verificationToken, e);
        }
    }
    
    private void compensateUserDeactivation(UUID userId) {
        try {
            var user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
            
            user.reactivate();
            userRepository.save(user);
            
            logger.info("ユーザー再有効化補償完了: userId={}", userId);
        } catch (Exception e) {
            logger.error("ユーザー再有効化補償エラー: userId=" + userId, e);
        }
    }
    
    private SagaResult handleSagaFailure(SagaState sagaState, String reason, String message) {
        sagaState.fail(reason + ": " + message);
        sagaStateRepository.save(sagaState);
        
        return new SagaResult(false, reason + ": " + message);
    }
}

// Saga Result Records
public record UserCreationResult(
    boolean success,
    UUID userId,
    String message
) {}

public record EmailResult(
    boolean success,
    String verificationToken,
    String message
) {}

public record LoyaltyResult(
    boolean success,
    UUID loyaltyAccountId,
    String message
) {}

public record SessionResult(
    boolean success,
    int sessionCount,
    String message
) {}

public record UserDeactivationResult(
    boolean success,
    String message
) {}

public record NotificationResult(
    boolean success,
    List<String> notifiedServices,
    String message
) {}

// Additional Events
public record UserRegistrationCompletedEvent(
    UUID userId,
    String email,
    UUID loyaltyAccountId,
    LocalDateTime timestamp
) implements UserEvent {}

public record UserDeactivationCompletedEvent(
    UUID userId,
    String reason,
    UUID deactivatedBy,
    LocalDateTime timestamp
) implements UserEvent {}

public record UserPasswordChangedEvent(
    UUID userId,
    LocalDateTime timestamp
) implements UserEvent {}

public record UserPreferencesUpdatedEvent(
    UUID userId,
    UserPreferences preferences,
    LocalDateTime timestamp
) implements UserEvent {}

// CQRS Annotations
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CommandHandler {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface QueryHandler {}

// External Events (from other services)
public record OrderCreatedEvent(
    UUID orderId,
    UUID customerId,
    BigDecimal totalAmount,
    LocalDateTime timestamp
) {}

public record PaymentCompletedEvent(
    UUID paymentId,
    UUID orderId,
    UUID customerId,
    BigDecimal amount,
    LocalDateTime timestamp
) {}

public record LoyaltyPointsEarnedEvent(
    UUID userId,
    Integer points,
    String source,
    LocalDateTime timestamp
) {}

// Repository Layer (Jakarta Data) - Extended
@Repository
public interface UserRepository extends BasicRepository<User, UUID> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.accountStatus.status = :status 
        ORDER BY u.createdAt DESC
        """)
    List<User> findByStatus(UserStatus status);
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.personalInfo.firstName LIKE :firstName% 
        AND u.personalInfo.lastName LIKE :lastName%
        """)
    List<User> findByName(String firstName, String lastName);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Page<User> findByRole(String roleName, int page, int size);
    
    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.createdAt BETWEEN :startDate AND :endDate
        """)
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.accountStatus.status = :status 
        AND u.createdAt BETWEEN :startDate AND :endDate
        """)
    long countByStatusAndCreatedAtBetween(UserStatus status, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("""
        SELECT u FROM User u 
        WHERE (:searchTerm IS NULL 
            OR LOWER(u.personalInfo.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) 
            OR LOWER(u.personalInfo.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND (:statuses IS NULL OR u.accountStatus.status IN :statuses)
        """)
    List<User> searchUsers(String searchTerm, Set<UserStatus> statuses, Set<String> roles, 
                          int page, int size, String sortBy, String sortDirection);
}
```

## API設計

### OpenAPI 3.1 仕様

```yaml
# user-management-api.yml
openapi: 3.1.0
info:
  title: User Management Service API
  version: 1.0.0
  description: スキー用品ショップ ユーザー管理サービス
  contact:
    name: Development Team
    email: dev@ski-shop.com

servers:
  - url: https://api.ski-shop.com/v1
    description: Production server
  - url: https://staging.api.ski-shop.com/v1
    description: Staging server
  - url: http://localhost:8081
    description: Local development

paths:
  /users/register:
    post:
      summary: 新規ユーザー登録
      operationId: registerUser
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserRegistrationRequest'
            examples:
              valid_user:
                summary: 有効なユーザー登録
                value:
                  email: "user@example.com"
                  password: "SecurePass123!"
                  firstName: "太郎"
                  lastName: "田中"
                  firstNameKana: "タロウ"
                  lastNameKana: "タナカ"
                  birthDate: "1990-01-01"
                  gender: "MALE"
                  phoneNumber: "090-1234-5678"
      responses:
        '201':
          description: ユーザー登録成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        '400':
          description: バリデーションエラー
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ValidationErrorResponse'
        '409':
          description: メールアドレス既に存在
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'

  /users/login:
    post:
      summary: ユーザーログイン
      operationId: loginUser
      tags: [Authentication]
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/LoginRequest'
      responses:
        '200':
          description: ログイン成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/LoginResponse'
        '401':
          description: 認証失敗
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/ErrorResponse'
        '423':
          description: アカウントロック
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountLockedResponse'

  /users/{userId}:
    get:
      summary: ユーザー情報取得
      operationId: getUser
      tags: [Profile]
      security:
        - BearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
          example: "123e4567-e89b-12d3-a456-426614174000"
      responses:
        '200':
          description: ユーザー情報
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserDetailResponse'
        '403':
          description: アクセス権限なし
        '404':
          description: ユーザーが見つからない

    put:
      summary: ユーザー情報更新
      operationId: updateUser
      tags: [Profile]
      security:
        - BearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/UserUpdateRequest'
      responses:
        '200':
          description: 更新成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/UserResponse'
        '400':
          description: バリデーションエラー
        '403':
          description: アクセス権限なし

  /users/{userId}/addresses:
    get:
      summary: ユーザー住所一覧取得
      operationId: getUserAddresses
      tags: [Address]
      security:
        - BearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      responses:
        '200':
          description: 住所一覧
          content:
            application/json:
              schema:
                type: array
                items:
                  $ref: '#/components/schemas/AddressResponse'

    post:
      summary: 新規住所追加
      operationId: addUserAddress
      tags: [Address]
      security:
        - BearerAuth: []
      parameters:
        - name: userId
          in: path
          required: true
          schema:
            type: string
            format: uuid
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AddressRequest'
      responses:
        '201':
          description: 住所追加成功
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AddressResponse'

components:
  schemas:
    UserRegistrationRequest:
      type: object
      required:
        - email
        - password
        - firstName
        - lastName
        - firstNameKana
        - lastNameKana
        - birthDate
        - gender
      properties:
        email:
          type: string
          format: email
          maxLength: 255
          example: "user@example.com"
        password:
          type: string
          minLength: 8
          maxLength: 128
          pattern: '^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]'
          example: "SecurePass123!"
        firstName:
          type: string
          maxLength: 50
          example: "太郎"
        lastName:
          type: string
          maxLength: 50
          example: "田中"
        firstNameKana:
          type: string
          maxLength: 50
          pattern: '^[ァ-ヶー]+$'
          example: "タロウ"
        lastNameKana:
          type: string
          maxLength: 50
          pattern: '^[ァ-ヶー]+$'
          example: "タナカ"
        birthDate:
          type: string
          format: date
          example: "1990-01-01"
        gender:
          type: string
          enum: [MALE, FEMALE, OTHER, NOT_SPECIFIED]
          example: "MALE"
        phoneNumber:
          type: string
          pattern: '^0\d{2,3}-\d{4}-\d{4}$'
          example: "090-1234-5678"

    LoginRequest:
      type: object
      required:
        - email
        - password
      properties:
        email:
          type: string
          format: email
          example: "user@example.com"
        password:
          type: string
          example: "SecurePass123!"
        rememberMe:
          type: boolean
          default: false

    UserResponse:
      type: object
      properties:
        id:
          type: string
          format: uuid
          example: "123e4567-e89b-12d3-a456-426614174000"
        email:
          type: string
          format: email
          example: "user@example.com"
        firstName:
          type: string
          example: "太郎"
        lastName:
          type: string
          example: "田中"
        status:
          type: string
          enum: [PENDING_VERIFICATION, ACTIVE, SUSPENDED, DEACTIVATED]
          example: "ACTIVE"
        emailVerified:
          type: boolean
          example: true
        createdAt:
          type: string
          format: date-time
          example: "2023-01-01T10:00:00Z"

    ErrorResponse:
      type: object
      properties:
        type:
          type: string
          example: "https://api.ski-shop.com/problems/validation-error"
        title:
          type: string
          example: "Validation Failed"
        status:
          type: integer
          example: 400
        detail:
          type: string
          example: "リクエストデータに検証エラーがあります"
        instance:
          type: string
          example: "/users/register"
        timestamp:
          type: string
          format: date-time
          example: "2023-01-01T10:00:00Z"

  securitySchemes:
    BearerAuth:
      type: http
      scheme: bearer
      bearerFormat: JWT
```

## データベース設計

### ERD（Entity Relationship Diagram）

```mermaid
erDiagram
    USERS {
        UUID id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR first_name_kana
        VARCHAR last_name_kana
        DATE birth_date
        VARCHAR gender
        VARCHAR phone_number
        VARCHAR status
        BOOLEAN email_verified
        BOOLEAN phone_verified
        TIMESTAMP last_login_at
        INTEGER login_failure_count
        TIMESTAMP lockout_until
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    
    ADDRESSES {
        UUID id PK
        UUID user_id FK
        VARCHAR type
        VARCHAR postal_code
        VARCHAR prefecture
        VARCHAR city
        VARCHAR address_line1
        VARCHAR address_line2
        VARCHAR building
        BOOLEAN is_default
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    
    USER_PREFERENCES {
        UUID id PK
        UUID user_id FK
        VARCHAR preference_key
        TEXT preference_value
        VARCHAR category
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    
    USER_SESSIONS {
        UUID id PK
        UUID user_id FK
        VARCHAR session_token
        VARCHAR device_info
        VARCHAR ip_address
        VARCHAR user_agent
        TIMESTAMP expires_at
        TIMESTAMP created_at
        TIMESTAMP last_accessed_at
    }
    
    USER_AUDIT_LOGS {
        UUID id PK
        UUID user_id FK
        VARCHAR action
        VARCHAR resource
        JSONB old_values
        JSONB new_values
        VARCHAR ip_address
        VARCHAR user_agent
        TIMESTAMP timestamp
    }
    
    PASSWORD_RESET_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token_hash
        TIMESTAMP expires_at
        BOOLEAN used
        TIMESTAMP created_at
    }
    
    EMAIL_VERIFICATION_TOKENS {
        UUID id PK
        UUID user_id FK
        VARCHAR token_hash
        VARCHAR email
        TIMESTAMP expires_at
        BOOLEAN verified
        TIMESTAMP created_at
    }
    
    USERS ||--o{ ADDRESSES : "has"
    USERS ||--o{ USER_PREFERENCES : "has"
    USERS ||--o{ USER_SESSIONS : "has"
    USERS ||--o{ USER_AUDIT_LOGS : "generates"
    USERS ||--o{ PASSWORD_RESET_TOKENS : "requests"
    USERS ||--o{ EMAIL_VERIFICATION_TOKENS : "requires"
```

### エンティティ詳細設計

```java
// 住所エンティティ
@Entity
@Table(name = "addresses")
public class Address {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private AddressType type;
    
    @Column(name = "postal_code", length = 8)
    @Pattern(regexp = "^\\d{3}-\\d{4}$")
    private String postalCode;
    
    @Column(name = "prefecture", length = 20, nullable = false)
    private String prefecture;
    
    @Column(name = "city", length = 50, nullable = false)
    private String city;
    
    @Column(name = "address_line1", length = 100, nullable = false)
    private String addressLine1;
    
    @Column(name = "address_line2", length = 100)
    private String addressLine2;
    
    @Column(name = "building", length = 100)
    private String building;
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Virtual Threads対応
    @Asynchronous
    public CompletableFuture<GeocodeResult> geocodeAsync() {
        return CompletableFuture.supplyAsync(() -> {
            // 住所から座標を取得する非同期処理
            return geocodingService.geocode(getFullAddress());
        });
    }
}

// ユーザー設定エンティティ
@Entity
@Table(name = "user_preferences")
public class UserPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "preference_key", length = 100, nullable = false)
    private String preferenceKey;
    
    @Column(name = "preference_value", columnDefinition = "TEXT")
    private String preferenceValue;
    
    @Column(name = "category", length = 50)
    private String category;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

// Record ベース DTO設計
public record AddressDTO(
    UUID id,
    AddressType type,
    String postalCode,
    String prefecture,
    String city,
    String addressLine1,
    String addressLine2,
    String building,
    Boolean isDefault,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static AddressDTO from(Address entity) {
        return new AddressDTO(
            entity.getId(),
            entity.getType(),
            entity.getPostalCode(),
            entity.getPrefecture(),
            entity.getCity(),
            entity.getAddressLine1(),
            entity.getAddressLine2(),
            entity.getBuilding(),
            entity.getIsDefault(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
    
    public String getFullAddress() {
        var builder = new StringBuilder()
            .append(prefecture)
            .append(city)
            .append(addressLine1);
            
        if (addressLine2 != null && !addressLine2.isBlank()) {
            builder.append(" ").append(addressLine2);
        }
        
        if (building != null && !building.isBlank()) {
            builder.append(" ").append(building);
        }
        
        return builder.toString();
    }
}

// セッション管理Record
public record UserSession(
    UUID id,
    UUID userId,
    String sessionToken,
    DeviceInfo deviceInfo,
    String ipAddress,
    LocalDateTime expiresAt,
    LocalDateTime createdAt,
    LocalDateTime lastAccessedAt
) {
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isValid() {
        return !isExpired() && sessionToken != null;
    }
}

public record DeviceInfo(
    String userAgent,
    String browser,
    String os,
    String deviceType,
    boolean isMobile
) {}

// Enums定義
public enum AddressType {
    HOME("自宅"),
    WORK("勤務先"),
    DELIVERY("配送先"),
    OTHER("その他");
    
    private final String description;
    
    AddressType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

### Repository設計（Jakarta Data活用）

```java
// ユーザーRepository
@Repository
public interface UserRepository extends BasicRepository<User, UUID> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(String email);
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.accountStatus.status = :status 
        AND u.createdAt >= :fromDate 
        ORDER BY u.createdAt DESC
        """)
    List<User> findByStatusAndCreatedAfter(UserStatus status, LocalDateTime fromDate);
    
    @Query("""
        SELECT u FROM User u 
        WHERE u.personalInfo.firstName LIKE :firstName% 
        AND u.personalInfo.lastName LIKE :lastName%
        AND u.accountStatus.status = 'ACTIVE'
        """)
    List<User> searchByName(String firstName, String lastName);
    
    @Query("""
        SELECT COUNT(u) FROM User u 
        WHERE u.accountStatus.status = :status
        """)
    long countByStatus(UserStatus status);
    
    @Query("""
        UPDATE User u 
        SET u.accountStatus.lastLoginAt = :loginTime 
        WHERE u.id = :userId
        """)
    @Modifying
    void updateLastLoginTime(UUID userId, LocalDateTime loginTime);
}

// 住所Repository
@Repository
public interface AddressRepository extends BasicRepository<Address, UUID> {
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId ORDER BY a.isDefault DESC, a.createdAt ASC")
    List<Address> findByUserId(UUID userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultByUserId(UUID userId);
    
    @Query("""
        SELECT a FROM Address a 
        WHERE a.user.id = :userId 
        AND a.type = :type
        """)
    List<Address> findByUserIdAndType(UUID userId, AddressType type);
    
    @Query("""
        UPDATE Address a 
        SET a.isDefault = false 
        WHERE a.user.id = :userId 
        AND a.id != :excludeId
        """)
    @Modifying
    void clearDefaultFlags(UUID userId, UUID excludeId);
}

// 設定Repository
@Repository  
public interface UserPreferenceRepository extends BasicRepository<UserPreference, UUID> {
    
    @Query("SELECT p FROM UserPreference p WHERE p.user.id = :userId")
    List<UserPreference> findByUserId(UUID userId);
    
    @Query("""
        SELECT p FROM UserPreference p 
        WHERE p.user.id = :userId 
        AND p.preferenceKey = :key
        """)
    Optional<UserPreference> findByUserIdAndKey(UUID userId, String key);
    
    @Query("""
        SELECT p FROM UserPreference p 
        WHERE p.user.id = :userId 
        AND p.category = :category
        """)
    List<UserPreference> findByUserIdAndCategory(UUID userId, String category);
}
```

## エラー処理

### エラー階層設計

```java
// ベース例外クラス
public abstract class UserManagementException extends RuntimeException {
    protected final String errorCode;
    protected final int httpStatus;
    protected final Map<String, Object> details;
    
    protected UserManagementException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.details = new HashMap<>();
    }
    
    public UserManagementException withDetail(String key, Object value) {
        this.details.put(key, value);
        return this;
    }
    
    // Getters
    public String getErrorCode() { return errorCode; }
    public int getHttpStatus() { return httpStatus; }
    public Map<String, Object> getDetails() { return details; }
}

// 具体的な例外クラス
public class EmailAlreadyExistsException extends UserManagementException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", 
              "指定されたメールアドレスは既に使用されています", 409);
        withDetail("email", email);
    }
}

public class UserNotFoundException extends UserManagementException {
    public UserNotFoundException(UUID userId) {
        super("USER_NOT_FOUND", 
              "指定されたユーザーが見つかりません", 404);
        withDetail("userId", userId);
    }
}

public class InvalidCredentialsException extends UserManagementException {
    public InvalidCredentialsException() {
        super("INVALID_CREDENTIALS", 
              "メールアドレスまたはパスワードが正しくありません", 401);
    }
}

public class AccountLockedException extends UserManagementException {
    public AccountLockedException(LocalDateTime lockoutUntil) {
        super("ACCOUNT_LOCKED", 
              "アカウントがロックされています", 423);
        withDetail("lockoutUntil", lockoutUntil);
    }
}

public class PasswordValidationException extends UserManagementException {
    public PasswordValidationException(List<String> violations) {
        super("PASSWORD_VALIDATION_FAILED", 
              "パスワードが要件を満たしていません", 400);
        withDetail("violations", violations);
    }
}

// バリデーション例外
public class ValidationException extends UserManagementException {
    private final Set<ConstraintViolation<?>> violations;
    
    public ValidationException(Set<ConstraintViolation<?>> violations) {
        super("VALIDATION_FAILED", 
              "入力データの検証に失敗しました", 400);
        this.violations = violations;
        
        var violationDetails = violations.stream()
            .collect(Collectors.toMap(
                v -> v.getPropertyPath().toString(),
                ConstraintViolation::getMessage
            ));
        withDetail("violations", violationDetails);
    }
}
```

### 統一エラーハンドリング

```java
// グローバル例外ハンドラー
@Provider
public class UserManagementExceptionMapper implements ExceptionMapper<Exception> {
    
    private static final Logger logger = LoggerFactory.getLogger(UserManagementExceptionMapper.class);
    
    @Override
    public Response toResponse(Exception exception) {
        String requestId = MDC.get("requestId");
        
        return switch (exception) {
            case UserManagementException ume -> {
                logger.warn("User management error: {} - {}", ume.getErrorCode(), ume.getMessage());
                yield Response.status(ume.getHttpStatus())
                    .entity(createErrorResponse(ume, requestId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
            
            case ConstraintViolationException cve -> {
                logger.warn("Validation error: {}", cve.getMessage());
                yield Response.status(400)
                    .entity(createValidationErrorResponse(cve, requestId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
            
            case SecurityException se -> {
                logger.warn("Security error: {}", se.getMessage());
                yield Response.status(403)
                    .entity(createSecurityErrorResponse(requestId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
            
            default -> {
                logger.error("Unexpected error: {}", exception.getMessage(), exception);
                yield Response.status(500)
                    .entity(createInternalErrorResponse(requestId))
                    .type(MediaType.APPLICATION_JSON)
                    .build();
            }
        };
    }
    
    private ErrorResponse createErrorResponse(UserManagementException exception, String requestId) {
        return new ErrorResponse(
            "https://api.ski-shop.com/problems/" + exception.getErrorCode().toLowerCase().replace("_", "-"),
            exception.getErrorCode(),
            exception.getHttpStatus(),
            exception.getMessage(),
            requestId,
            LocalDateTime.now(),
            exception.getDetails()
        );
    }
}

// エラーレスポンス Record
public record ErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    LocalDateTime timestamp,
    Map<String, Object> extensions
) {}

public record ValidationErrorResponse(
    String type,
    String title,
    int status,
    String detail,
    String instance,
    LocalDateTime timestamp,
    Map<String, String> violations
) {}
```

### カスタムバリデーション

```java
// カスタムバリデーションアノテーション
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordStrengthValidator.class)
public @interface PasswordStrength {
    String message() default "パスワードが強度要件を満たしていません";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// パスワード強度バリデーター
public class PasswordStrengthValidator implements ConstraintValidator<PasswordStrength, String> {
    
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>?]");
    
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        var violations = new ArrayList<String>();
        
        if (!LOWERCASE.matcher(password).find()) {
            violations.add("小文字を含む必要があります");
        }
        
        if (!UPPERCASE.matcher(password).find()) {
            violations.add("大文字を含む必要があります");
        }
        
        if (!DIGIT.matcher(password).find()) {
            violations.add("数字を含む必要があります");
        }
        
        if (!SPECIAL.matcher(password).find()) {
            violations.add("特殊文字を含む必要があります");
        }
        
        if (!violations.isEmpty()) {
            context.disableDefaultConstraintViolation();
            violations.forEach(violation -> 
                context.buildConstraintViolationWithTemplate(violation)
                    .addConstraintViolation()
            );
            return false;
        }
        
        return true;
    }
}
```
