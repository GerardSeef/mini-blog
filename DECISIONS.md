# Technical Decisions - Mini-Blog API

## 1. Authentication: JWT (tymon/jwt-auth)

**Decision:** Use JWT (JSON Web Tokens) via tymon/jwt-auth package.

**Why:** 
- Stateless authentication, no sessions needed (ideal for APIs)
- Standard approach in modern REST APIs
- Easy to implement and test
- Self-contained tokens with expiration
- No database overhead for session management

**Trade-offs:**
- Tokens can't be revoked instantly (blacklist mechanism is optional)
- Slightly larger request headers due to token size

## 2. Database: PostgreSQL

**Decision:** Use PostgreSQL over MySQL.

**Why:**
- Better concurrency handling with native locking mechanisms (FOR UPDATE)
- Superior constraint enforcement, especially for unique keys
- ACID compliance guarantees prevent race conditions in likes table
- More robust for the unique constraint on (user_id, post_id)

## 3. Concurrency Protection for Likes

**Decision:** Rely on database UNIQUE constraint, handle duplicates at application level.

**Why:**
- DB-level uniqueness is bulletproof under concurrent writes
- Postgres locks handle simultaneous requests atomically
- Application catches constraint violations and returns 409 Conflict
- No need for pessimistic locking (FOR UPDATE) which would hurt performance

**Implementation:**
```php
try {
    $post->likes()->create(['user_id' => auth()->id()]);
} catch (QueryException $e) {
    return response()->json(['message' => 'Already liked'], 409);
}
```

## 4. N+1 Query Prevention

**Decision:** Use eager loading with `withCount()` for post listings.

**Why:**
- `Post::with('user')->withCount(['comments', 'likes'])->paginate()` generates exactly 3 queries
- No extra queries per post, regardless of page size
- Built-in Laravel feature, no custom logic needed

## 5. Authorization: PostPolicy

**Decision:** Use Laravel Policies for owner-only post updates/deletes.

**Why:**
- Declarative, centralized authorization logic
- Automatic 403 response on unauthorized access
- Testable and maintainable
- Follows Laravel conventions

## 6. API Response Format

**Decision:** Use Laravel Resources for consistent JSON transformation.

**Why:**
- Automatic relationship eager loading when used
- Consistent field formatting across endpoints
- Easy to modify response structure without touching controllers
- Supports pagination automatically

## 7. Bonus: Anthropic Claude API

**Decision:** Integrate with Anthropic Claude (claude-haiku-4-5-20251001).

**Why:**
- Haiku model is fast and cost-effective for summarization tasks
- Direct API calls via Guzzle (already in Laravel)
- Environment variable for API key (security best practice)
- Simple service wrapper for clean separation

**Not Implemented:**
- Error handling for API failures (optional enhancement)
- Caching of summaries
- Streaming responses

## 8. Bonus: Java + Spring Boot

**Decision:** Document as pending, not implemented.

**Why:**
- Time constraints (4-5 hour estimate)
- PHP/Laravel is primary focus
- Architecture supports adding it later with separate service
- Docker Compose already structured for multi-service setup

**How to add:**
- Create `/java` directory with Spring Boot microservice
- Add service to docker-compose.yml
- Implement same Like endpoint in Java
- Share PostgreSQL database

## What Was Left Out

1. **Email verification** - Marked as "optional" in prueba_técnica; can add if needed
2. **Refresh token mechanism** - JWT TTL sufficient for demo; can extend
3. **Rate limiting** - Not required; can add middleware if needed
4. **Request logging/audit trail** - Out of scope
5. **API documentation (Swagger)** - Route list in README sufficient
6. **Error message localization** - English sufficient
7. **Soft deletes** - Hard deletes are simpler and appropriate here
8. **Post search/filtering** - Not in requirements

## Summary

The implementation prioritizes:
- **Correctness** over features (concurrency safety first)
- **Simplicity** over abstraction (no over-engineering)
- **Laravel conventions** for maintainability
- **Database constraints** for data integrity
- **Production-ready Docker setup** for deployment

All required features are working and tested.
