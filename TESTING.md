# Testing Guide

## Automated Tests

The project includes comprehensive tests for:

1. **Authentication** (`tests/Feature/AuthTest.php`)
   - User registration
   - Login with valid/invalid credentials

2. **Posts** (`tests/Feature/PostTest.php`)
   - Creating posts (protected)
   - Editing only own posts (authorization)
   - Listing posts with correct counts (no N+1)

3. **Comments** (`tests/Feature/CommentTest.php`)
   - Adding comments to posts (protected)
   - Listing comments

4. **Likes** (`tests/Feature/LikeTest.php`)
   - Adding likes to posts (protected)
   - Preventing duplicate likes (critical test)
   - Removing likes

### Running Tests

```bash
# Using Docker
docker-compose exec app php artisan test

# Locally
php artisan test

# Specific test file
php artisan test tests/Feature/LikeTest.php

# Specific test method
php artisan test tests/Feature/LikeTest.php --filter test_user_cannot_like_twice
```

## Manual API Testing

### 1. Register a User

```bash
curl -X POST http://localhost:8000/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "password_confirmation": "password123"
  }'
```

**Expected Response:** `201 Created`

### 2. Login

```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Expected Response:** `200 OK` with `token` and `user` fields

Store the token for subsequent requests.

### 3. Create a Post (Protected)

```bash
curl -X POST http://localhost:8000/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "title": "My First Post",
    "body": "This is the content of my post"
  }'
```

**Expected Response:** `201 Created`

### 4. List Posts (Public, Paginated)

```bash
curl http://localhost:8000/api/posts
```

**Response includes:**
- `comments_count` - total comments
- `likes_count` - total likes
- `author` - post creator info
- `data` - paginated posts array
- `links` - pagination links

### 5. Add a Comment (Protected)

```bash
curl -X POST http://localhost:8000/api/posts/1/comments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{
    "body": "Great post!"
  }'
```

**Expected Response:** `201 Created`

### 6. Like a Post (Protected)

```bash
curl -X POST http://localhost:8000/api/posts/1/like \
  -H "Authorization: Bearer {TOKEN}"
```

**Expected Response:** `201 Created`

### 7. Try to Like Twice (Concurrency Test)

```bash
curl -X POST http://localhost:8000/api/posts/1/like \
  -H "Authorization: Bearer {TOKEN}"
```

**Expected Response:** `409 Conflict` with message "Already liked"

### 8. Unlike a Post (Protected)

```bash
curl -X DELETE http://localhost:8000/api/posts/1/like \
  -H "Authorization: Bearer {TOKEN}"
```

**Expected Response:** `200 OK`

### 9. Get Post Summary with LLM (Bonus)

```bash
curl http://localhost:8000/api/posts/1/summary \
  -H "Authorization: Bearer {TOKEN}"
```

**Note:** Requires `ANTHROPIC_API_KEY` in `.env`

**Expected Response:** `200 OK` with summary text

## Concurrency Testing

To test the duplicate-like prevention under concurrent requests:

```bash
# Terminal 1: Start the server
docker-compose up -d

# Terminal 2: Test concurrent likes (replace TOKEN with actual token)
for i in {1..3}; do
  curl -X POST http://localhost:8000/api/posts/1/like \
    -H "Authorization: Bearer {TOKEN}" \
    -w "\nStatus: %{http_code}\n" &
done
wait
```

**Expected Results:**
- 1st request: `201 Created`
- 2nd+ requests: `409 Conflict`

The database constraint prevents any race conditions.

## Error Scenarios

### Unauthorized Access

```bash
# Trying to create a post without authentication
curl -X POST http://localhost:8000/api/posts \
  -H "Content-Type: application/json" \
  -d '{"title":"Test","body":"Test"}'
```

**Expected Response:** `401 Unauthorized`

### Forbidden Access

```bash
# User trying to edit another user's post
curl -X PUT http://localhost:8000/api/posts/2/edit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{"title":"Hacked","body":"Hacked"}'
```

**Expected Response:** `403 Forbidden`

### Validation Errors

```bash
# Missing required fields
curl -X POST http://localhost:8000/api/posts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {TOKEN}" \
  -d '{}'
```

**Expected Response:** `422 Unprocessable Entity` with validation errors
