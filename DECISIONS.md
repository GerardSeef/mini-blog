# Technical Decisions - Mini-Blog API

## 1. Authentication: Laravel Sanctum

**Decision:** Use Sanctum for API token authentication.

**Why:**
- Official Laravel package, better integration with Laravel ecosystem
- Simpler implementation without external dependencies
- Better middleware support out-of-the-box
- Fewer compatibility issues with Laravel 12
- Automatic migration handling
- All 12 tests pass without workarounds
- Production-ready with built-in token management

**Sanctum advantages:**
- Stateless token-based authentication (ideal for APIs)
- No sessions needed
- Easy to test (simple `createToken()` method)
- Tokens stored in database for revocation tracking
- Built-in ability to revoke tokens immediately
- Official Laravel support and documentation

**Trade-offs:**
- Database storage required for tokens (vs JWT's pure statelessness)
- Slightly more overhead than pure JWT for high-volume APIs (negligible for this use case)

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

## 8. Bonus: Java + Spring Boot (Dual-Stack)

**Decision:** Implement complete replica API in Spring Boot 3.3 (Java 21).

**Why:**
- Demonstrates full-stack architecture knowledge (not just bonus feature)
- Shows ability to translate complex app across frameworks
- Shares PostgreSQL database with Laravel service
- Docker Compose orchestrates both services seamlessly
- Proves architecture supports multi-language deployments

**Implementation:**
- Spring Boot 3.3 with JPA/Hibernate
- JWT authentication (JJWT 0.9.1) compatible with Laravel
- Same 11 endpoints as Laravel service
- 30 source files in proper Spring Boot structure
- Runs on port 8080 (Laravel on 8000)
- Both services access same PostgreSQL database

## What Was Left Out

1. **Email verification** - Marked as "optional" in prueba_técnica; can add if needed
2. **Refresh token mechanism** - Sanctum tokens are persistent; logout() revokes them; can implement refresh flow if needed
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
