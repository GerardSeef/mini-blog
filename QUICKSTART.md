# Quick Start

## With Docker (Recommended)

```bash
# 1. Clone/navigate to the project
cd mini-blog

# 2. Start all services (app + PostgreSQL)
docker-compose up -d

# 3. Wait for migrations to run automatically (check logs)
docker-compose logs -f app

# 4. API is ready at http://localhost:8000

# 5. Run tests
docker-compose exec app php artisan test

# 6. Stop services
docker-compose down
```

## Local Setup (macOS/Linux)

```bash
# 1. Install PHP dependencies
composer install

# 2. Copy environment
cp .env.example .env

# 3. Generate app key
php artisan key:generate

# 4. Setup database (requires PostgreSQL running)
# Edit .env with your DB credentials, then:
php artisan migrate

# 5. Start the server
php artisan serve

# 6. API is at http://localhost:8000

# 7. Run tests
php artisan test
```

## First Request

Register a user:
```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "password_confirmation": "password123"
  }'
```

Then login to get a token (see TESTING.md for more examples).

## Bonus LLM Feature

To enable AI-powered post summaries:

1. Get an API key from https://console.anthropic.com/
2. Add to `.env`: `ANTHROPIC_API_KEY=sk-ant-xxx`
3. Make requests to `/api/posts/{id}/summary` (authenticated)

## Documentation Files

- **README.md** - Full API documentation and features
- **TESTING.md** - Manual testing examples and curl commands
- **DECISIONS.md** - Technical decisions and trade-offs

## Key Features Verified

✅ User authentication with JWT tokens  
✅ CRUD operations on posts (owner-only edit/delete)  
✅ Comments on posts  
✅ Likes with duplicate prevention (409 on retry)  
✅ Paginated post list with counts (no N+1 queries)  
✅ Tests for all major flows  
✅ Docker setup for easy deployment  
✅ Bonus: Anthropic API integration for summaries  

## Endpoints Summary

Public:
- `POST /api/auth/register` - Register
- `POST /api/auth/login` - Login
- `GET /api/posts` - List posts
- `GET /api/posts/{id}` - Get post
- `GET /api/posts/{id}/comments` - List comments

Protected (require token):
- `POST /api/posts` - Create post
- `PUT /api/posts/{id}` - Update own post
- `DELETE /api/posts/{id}` - Delete own post
- `POST /api/posts/{id}/comments` - Add comment
- `POST /api/posts/{id}/like` - Like post
- `DELETE /api/posts/{id}/like` - Unlike post
- `GET /api/posts/{id}/summary` - AI summary (bonus)

See TESTING.md for full curl examples.
