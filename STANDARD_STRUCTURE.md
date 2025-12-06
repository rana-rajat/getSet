# Standard Project Structure

This project follows Spring Boot best practices with a standard directory layout.

## Directory Structure

```
src/
├── main/
│   ├── java/com/getset/
│   │   ├── GetSetApplication.java          # Main entry point
│   │   ├── api/                            # Controllers
│   │   ├── application/                    # Application services (facade layer)
│   │   ├── domain/                         # Domain models (entities)
│   │   ├── dto/                            # Data Transfer Objects
│   │   │   ├── request/                    # Request DTOs
│   │   │   ├── response/                   # Response DTOs
│   │   │   └── mapper/                     # DTO Mappers
│   │   ├── infrastructure/                 # Infrastructure layer
│   │   │   ├── repository/                 # Data repositories
│   │   │   ├── config/                     # Spring configurations
│   │   │   ├── security/                   # Security configurations
│   │   │   └── persistence/                # Database configurations
│   │   ├── exception/                      # Custom exceptions
│   │   ├── constant/                       # Application constants
│   │   ├── util/                           # Utility classes
│   │   ├── validator/                      # Custom validators (optional)
│   │   └── common/                         # Common utilities (error responses, etc.)
│   └── resources/
│       ├── application.yml                 # Main configuration
│       ├── application-dev.yml             # Development profile
│       ├── application-prod.yml            # Production profile
│       └── db/migration/                   # Database migrations (Flyway/Liquibase)
└── test/
    ├── java/com/getset/
    │   ├── api/                            # Controller tests
    │   ├── application/                    # Service tests
    │   ├── infrastructure/                 # Repository tests
    │   └── integration/                    # Integration tests
    └── resources/
        ├── application-test.yml            # Test configuration
        └── test-data.sql                   # Test data

```

## Layered Architecture

```
┌─────────────────────────────────────┐
│  Presentation Layer (API)           │
│  - Controllers                      │
│  - Request/Response DTOs            │
├─────────────────────────────────────┤
│  Application Layer (Services)       │
│  - Business logic                   │
│  - Service facades                  │
├─────────────────────────────────────┤
│  Domain Layer (Models)              │
│  - Domain entities                  │
│  - Business rules                   │
├─────────────────────────────────────┤
│  Infrastructure Layer               │
│  - Repositories                     │
│  - Database access                  │
│  - External services                │
└─────────────────────────────────────┘
```

## Package Naming Conventions

| Layer | Package | Examples |
|-------|---------|----------|
| API | `api`, `controller` | `PropertyController`, `AuthController` |
| Application | `application`, `service` | `PropertyService`, `AuthService` |
| Domain | `domain`, `model`, `entity` | `Property`, `User`, `PropertyDocument` |
| DTO | `dto.request`, `dto.response` | `PropertyCreateRequest`, `PropertyResponse` |
| Infrastructure | `infrastructure.repository` | `PropertyRepository` |
| Configuration | `config` | `SecurityConfig`, `CorsConfig` |
| Utilities | `util`, `utils` | `ValidationUtil`, `StringUtil` |
| Constants | `constant`, `constants` | `AppConstants` |
| Exception | `exception` | `GetSetException`, `NotFoundException` |

## File Naming Conventions

| Type | Naming Pattern | Example |
|------|----------------|---------|
| Controller | `*Controller` | `PropertyController` |
| Service | `*Service`, `*ServiceImpl` | `PropertyService`, `PropertyServiceImpl` |
| Repository | `*Repository` | `PropertyRepository` |
| Entity/Document | `*Document`, `*Entity` | `PropertyDocument`, `UserDocument` |
| DTO Request | `*Request` | `PropertyCreateRequest` |
| DTO Response | `*Response` | `PropertyResponse` |
| Mapper | `*Mapper` | `PropertyMapper` |
| Configuration | `*Config` | `SecurityConfig` |
| Exception | `*Exception` | `NotFoundException` |
| Utility | `*Util` | `ValidationUtil` |
| Validator | `*Validator` | `PropertyValidator` |
| Test | `*Test` | `PropertyControllerTest` |

## Best Practices

### 1. Single Responsibility
Each class should have one reason to change.

### 2. Separation of Concerns
- Controllers handle HTTP
- Services handle business logic
- Repositories handle data access

### 3. Dependency Injection
Use constructor injection with `@RequiredArgsConstructor`.

### 4. DTOs for APIs
Never expose entities directly in API responses.

### 5. Exception Handling
Use custom exceptions that extend `GetSetException`.

### 6. Logging
Use appropriate log levels (DEBUG, INFO, WARN, ERROR).

### 7. Validation
- Use annotations for simple validation
- Use custom validators for complex logic

### 8. Constants
Put magic numbers and strings in constants.

## Common Patterns

### Request Handler Pattern (Controller)
```java
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
    private final PropertyService propertyService;
    
    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyCreateRequest request) {
        return ResponseEntity.ok(propertyService.create(request));
    }
}
```

### Service Pattern
```java
@Service
@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository repository;
    private final PropertyMapper mapper;
    
    public PropertyResponse create(PropertyCreateRequest request) {
        // Business logic
        PropertyDocument document = mapper.toDocument(request);
        PropertyDocument saved = repository.save(document);
        return mapper.toResponse(saved);
    }
}
```

### Repository Pattern
```java
@Repository
public interface PropertyRepository extends MongoRepository<PropertyDocument, String> {
    List<PropertyDocument> findByAddress_City(String city);
}
```

## Configuration Files

- `application.yml` - Default profile (shared config)
- `application-dev.yml` - Development profile
- `application-prod.yml` - Production profile
- `application-test.yml` - Test profile

Use profiles to manage environment-specific configurations.

## Testing Structure

- **Unit Tests**: Test individual classes in isolation
- **Integration Tests**: Test multiple components together
- **Controller Tests**: Test REST endpoints
- **Repository Tests**: Test database operations
- **Service Tests**: Test business logic

## Recommended Tools

- **Build**: Maven (pom.xml)
- **Testing**: JUnit 5, Mockito, TestContainers
- **Logging**: SLF4J, Logback
- **Validation**: Jakarta Bean Validation
- **Documentation**: SpringDoc OpenAPI, Swagger

---

This structure promotes:
- ✅ Clean code
- ✅ Easy testing
- ✅ Clear separation of concerns
- ✅ Scalability
- ✅ Maintainability
- ✅ Team collaboration
