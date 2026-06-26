# Mini-Blog Java API

Spring Boot 3.3 replica of the Laravel mini-blog API. Provides identical endpoints with PostgreSQL as the shared database.

## Architecture

- **Java**: 21 LTS
- **Spring Boot**: 3.3.4
- **Spring Security**: JWT-based authentication
- **Database**: Shared PostgreSQL with Laravel service
- **Port**: 8080

## Project Structure

```
java/
├── src/main/java/com/miniblog/
│   ├── LikesServiceApplication.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── PostController.java
│   │   ├── CommentController.java
│   │   └── LikeController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   └── LikeService.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   └── Like.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   └── LikeRepository.java
│   ├── security/
│   │   ├── JwtProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CustomUserDetailsService.java
│   └── config/
│       └── SecurityConfig.java
├── src/main/resources/
│   └── application.yml
├── pom.xml
└── Dockerfile
```

## API Endpoints

All endpoints mirror the Laravel API exactly:

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and receive token
- `POST /api/auth/logout` - Logout (protected)
- `GET /api/auth/me` - Get current user info (protected)

### Posts
- `GET /api/posts` - List all posts (paginated)
- `POST /api/posts` - Create post (protected)
- `GET /api/posts/{id}` - Get post by ID
- `PUT /api/posts/{id}` - Update post (owner only, protected)
- `DELETE /api/posts/{id}` - Delete post (owner only, protected)

### Comments
- `GET /api/posts/{id}/comments` - List comments for post
- `POST /api/posts/{id}/comments` - Add comment (protected)

### Likes
- `POST /api/posts/{id}/like` - Like post (returns 409 if already liked, protected)
- `DELETE /api/posts/{id}/like` - Unlike post (protected)

## Key Features

1. **Shared Database**: Uses the same PostgreSQL instance as Laravel service
2. **JWT Authentication**: Identical token format and validation as Laravel
3. **Duplicate Prevention**: UNIQUE constraint on (user_id, post_id) in likes table
4. **N+1 Prevention**: Eager loading with `@Query` in repositories
5. **Error Handling**: Same HTTP status codes as Laravel (409 for duplicate likes, 403 for unauthorized)

## Building & Running

### Docker (Recommended)
```bash
docker-compose up -d java-api
# Runs on http://localhost:8080
```

### Local Build
```bash
cd java
mvn clean package
java -jar target/java-api-1.0.0.jar
```

### Configuration

Set environment variables (or edit `application.yml`):
```
DB_HOST=localhost
DB_PORT=5432
DB_DATABASE=miniblog
DB_USERNAME=postgres
DB_PASSWORD=postgres
JWT_SECRET=your_secret_key
ANTHROPIC_API_KEY=optional_api_key
```

## Testing

```bash
# Once running, test endpoints
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "passwordConfirmation": "password123"
  }'

# Get token from response, then test protected endpoints
curl -X GET http://localhost:8080/api/posts \
  -H "Authorization: Bearer <your-token>"
```

## Notes

- Migrations are NOT run by this service; they're handled by the Laravel service
- Both services share the same PostgreSQL database
- JWT tokens are compatible between Laravel and Java endpoints
- Same validation rules as Laravel API
