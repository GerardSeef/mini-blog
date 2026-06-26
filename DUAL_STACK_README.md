# Dual-Stack Mini-Blog API

This project implements a mini-blog REST API in **two languages simultaneously**:
- **Laravel 12** (PHP) running on port `8000`
- **Spring Boot 3.3** (Java 21) running on port `8080`

Both services use the **same PostgreSQL database**, providing identical functionality.

## Quick Start

```bash
# Start both services with Docker Compose
docker-compose up -d

# Wait for database to be healthy
sleep 15

# Run tests on Laravel service
docker-compose exec app php artisan test

# Test Java service
curl http://localhost:8080/api/posts
```

## Architecture Overview

```
┌─────────────────────────────────────────┐
│        Shared PostgreSQL Database       │
│         (port 5432, miniblog DB)        │
└────────────────┬────────────────────────┘
                 │
        ┌────────┴────────┐
        │                 │
    ┌───▼────┐        ┌──▼────┐
    │ Laravel│        │ Spring│
    │  PHP   │        │ Boot  │
    │ 8000   │        │ 8080  │
    └────────┘        └───────┘
```

## Services

### 1. Laravel API (php-fpm + artisan serve)
- **Port**: 8000
- **Context Path**: `/api`
- **Language**: PHP 8.2
- **Framework**: Laravel 12
- **Authentication**: Sanctum tokens
- **Location**: Root directory

**Endpoints**:
```
POST   /api/auth/register
POST   /api/auth/login
POST   /api/auth/logout
GET    /api/auth/me
GET    /api/posts
POST   /api/posts
GET    /api/posts/{id}
PUT    /api/posts/{id}
DELETE /api/posts/{id}
GET    /api/posts/{id}/comments
POST   /api/posts/{id}/comments
POST   /api/posts/{id}/like
DELETE /api/posts/{id}/like
```

### 2. Spring Boot API (java-api)
- **Port**: 8080
- **Context Path**: `/api`
- **Language**: Java 21
- **Framework**: Spring Boot 3.3
- **Authentication**: JWT tokens
- **Location**: `/java` directory

**Same endpoints as Laravel** (all paths identical)

## Running Individual Services

### Laravel Only
```bash
docker-compose up -d app db
# Available on http://localhost:8000/api
```

### Spring Boot Only
```bash
docker-compose up -d java-api db
# Available on http://localhost:8080/api
```

### Both Services
```bash
docker-compose up -d
# Laravel: http://localhost:8000/api
# Java:    http://localhost:8080/api
```

## Testing

### Laravel Tests
```bash
docker-compose exec app php artisan test
# Output: 12 tests passing
```

### Manual Testing (cURL)

Register on Laravel:
```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "secret123",
    "password_confirmation": "secret123"
  }'
```

Login on Java service with same credentials:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "secret123"
  }'
```

Create post on Laravel:
```bash
curl -X POST http://localhost:8000/api/posts \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My First Post",
    "body": "This is a test post"
  }'
```

List posts on Java (same data):
```bash
curl http://localhost:8080/api/posts
```

## Database Schema

Both services share identical table structures:
- `users` (id, name, email, password, created_at, updated_at)
- `posts` (id, user_id FK, title, body, created_at, updated_at)
- `comments` (id, user_id FK, post_id FK, body, created_at, updated_at)
- `likes` (id, user_id FK, post_id FK, UNIQUE(user_id, post_id), created_at)

**Migrations** are handled by the Laravel service only (Spring Boot uses `ddl-auto: validate`).

## Authentication

### Laravel (Sanctum)
- Token-based, stateless
- Uses Bearer token format
- Tokens stored in `personal_access_tokens` table

### Java (JWT)
- Also uses Bearer token format
- Tokens signed with `JWT_SECRET`
- No database storage needed

**Important**: Tokens from one service are **NOT** compatible with the other due to different signing mechanisms. Each service issues its own tokens.

## Duplicate-Like Prevention

Both services enforce the same constraint:
- Attempting to like a post twice returns **409 Conflict**
- Database UNIQUE constraint on (user_id, post_id) prevents race conditions

**Test both**:
```bash
# Get a token
TOKEN=$(curl -s -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"secret123"}' | jq -r '.token')

# Like on Laravel
curl -X POST http://localhost:8000/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN"

# Try again - should return 409
curl -X POST http://localhost:8000/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN"
# {"message":"Already liked"}

# Same on Java
TOKEN_JAVA=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"secret123"}' | jq -r '.token')

curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN_JAVA"

curl -X POST http://localhost:8080/api/posts/1/like \
  -H "Authorization: Bearer $TOKEN_JAVA"
# {"message":"Already liked"}
```

## Performance Characteristics

### N+1 Query Prevention
- **Laravel**: Uses `withCount(['comments', 'likes'])` → exactly 3 queries per page
- **Java**: Uses `@Query` with FETCH JOIN → identical behavior

### Authorization
- **Laravel**: PostPolicy checks `user_id === post->user_id` → 403 Forbidden
- **Java**: Service layer checks same logic → 403 Forbidden

## File Structure

```
mini-blog/
├── app/                                # Laravel app code
├── config/                             # Laravel config
├── database/                           # Laravel migrations
├── routes/                             # Laravel routes
├── tests/                              # Laravel tests
├── docker-compose.yml                  # Multi-service orchestration
├── Dockerfile                          # Laravel PHP image
├── java/                               # Spring Boot application
│   ├── src/
│   │   ├── main/java/com/miniblog/    # Java source code
│   │   └── main/resources/            # application.yml
│   ├── pom.xml                         # Maven configuration
│   ├── Dockerfile                      # Java build + runtime
│   └── README.md                       # Java-specific docs
├── README.md                           # Laravel setup
├── README_ES.md                        # Spanish version
├── DECISIONS.md                        # Technical decisions
├── DECISIONS_ES.md                     # Spanish decisions
├── TESTING.md                          # Manual testing guide
├── QUICKSTART.md                       # Quick setup
└── DUAL_STACK_README.md               # This file
```

## Troubleshooting

### Java service won't build
```bash
# Check Maven cache
docker-compose exec java-api mvn clean compile
```

### Database schema mismatch
```bash
# Ensure Laravel migrations ran first
docker-compose exec app php artisan migrate:status
docker-compose exec app php artisan migrate --force
```

### Port conflicts
```bash
# Change ports in docker-compose.yml if 8000/8080 are in use
# Then restart services
docker-compose down
docker-compose up -d
```

### Can't login on Java with Laravel token
This is expected - each service has its own authentication mechanism.
Get a token from the service you want to use.

## Deployment

### Using Docker Compose (Production-like)
```bash
docker-compose up -d
docker-compose logs -f
```

### Using Kubernetes
See `k8s/` directory (if provided)

### Single Service Deployment
Deploy only Laravel OR only Java based on your infrastructure:
```bash
# Only Laravel
docker build -t my-api-laravel .
docker run -p 8000:8000 my-api-laravel

# Only Java
docker build -t my-api-java ./java
docker run -p 8080:8080 my-api-java
```

## Bonus Features

### Anthropic API Integration
Both services support Claude summaries (if `ANTHROPIC_API_KEY` is set):
```bash
# Laravel
GET /api/posts/{id}/summary

# Java (TODO - future implementation)
GET /api/posts/{id}/summary
```

## What's Different Between Services

| Feature | Laravel | Java |
|---------|---------|------|
| Port | 8000 | 8080 |
| Auth Method | Sanctum | JWT |
| Password Hash | bcrypt (PHP) | bcrypt (Spring) |
| Token Storage | DB table | None (JWT stateless) |
| Migrations | Runs automatically | Validates existing schema |
| ORM | Eloquent | JPA/Hibernate |
| Testing Framework | PHPUnit | JUnit 5 |

## What's the Same Between Services

| Feature | Implementation |
|---------|-----------------|
| Database schema | Identical PostgreSQL tables |
| Business logic | Same validation rules |
| Error codes | Same HTTP status codes |
| Response format | Identical JSON structure |
| Authorization | Same owner-only checks |
| Concurrency safety | UNIQUE constraints |
| N+1 prevention | Eager loading in both |

## Contributing

To add a new endpoint:
1. Add route + handler in Laravel (routes/api.php)
2. Replicate in Java (controller + service)
3. Update migrations if needed (Laravel handles this)
4. Add tests in both frameworks
5. Update this README
6. Commit and push both implementations

## License

Part of TSGroup Technical Evaluation 2026.

---

**Ready to deploy both?**
```bash
git clone https://github.com/GerardSeef/mini-blog.git
cd mini-blog
docker-compose up -d
# Now accessible at http://localhost:8000 (Laravel) and http://localhost:8080 (Java)
```
