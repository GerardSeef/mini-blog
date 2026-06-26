# Mini-Blog API

A RESTful API for a mini-blog platform built with Laravel 12, JWT authentication, and PostgreSQL.

## Features

- User registration and JWT-based authentication
- Create, read, update, and delete posts (owner-only for edit/delete)
- Add and manage comments on posts
- Like/unlike functionality with concurrency protection
- Paginated post listings with counts (no N+1 queries)
- Bonus: AI-powered post summaries using Anthropic Claude API

## Setup & Installation

### Prerequisites

- Docker and Docker Compose
- (Or PHP 8.2+, Composer, and PostgreSQL if running locally)

### Quick Start with Docker

```bash
# Clone the repository
git clone <repo-url>
cd mini-blog

# Start the services
docker-compose up -d

# The API will be available at http://localhost:8000
```

### Manual Setup

```bash
# Install dependencies
composer install

# Copy environment file
cp .env.example .env

# Generate app key
php artisan key:generate

# Generate JWT secret (add to .env as JWT_SECRET)
# Generate a random string and add it to .env

# Run migrations
php artisan migrate

# Start the server
php artisan serve
```

## Configuration

### Environment Variables

Create a `.env` file based on `.env.example` and set:

```
DB_CONNECTION=pgsql
DB_HOST=db
DB_PORT=5432
DB_DATABASE=miniblog
DB_USERNAME=postgres
DB_PASSWORD=postgres

JWT_SECRET=your_jwt_secret_key
JWT_ALGORITHM=HS256
JWT_TTL=60

ANTHROPIC_API_KEY=your_anthropic_api_key_optional
```

## API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and receive JWT token
- `POST /api/auth/logout` - Logout (protected)
- `GET /api/auth/me` - Get current user info (protected)

### Posts

- `GET /api/posts` - List all posts (paginated, includes counts)
- `POST /api/posts` - Create a new post (protected)
- `GET /api/posts/{id}` - Get a specific post
- `PUT /api/posts/{id}` - Update a post (owner-only, protected)
- `DELETE /api/posts/{id}` - Delete a post (owner-only, protected)

### Comments

- `GET /api/posts/{id}/comments` - List comments for a post
- `POST /api/posts/{id}/comments` - Add a comment (protected)

### Likes

- `POST /api/posts/{id}/like` - Like a post (protected, returns 409 if already liked)
- `DELETE /api/posts/{id}/like` - Unlike a post (protected)

### Bonus

- `GET /api/posts/{id}/summary` - Get an AI-powered summary of the post and comments (protected)

## Testing

```bash
# Run tests
docker-compose exec app php artisan test

# Or locally
php artisan test
```

## Project Structure

- `app/Models/` - Eloquent models (User, Post, Comment, Like)
- `app/Http/Controllers/Api/` - API controllers
- `app/Http/Requests/` - Form request validation
- `app/Http/Resources/` - JSON resource transformations
- `app/Policies/` - Authorization policies (PostPolicy)
- `app/Services/` - Business logic (AnthropicService)
- `database/migrations/` - Database schema
- `routes/api.php` - API route definitions
- `tests/Feature/` - Feature tests

## Technical Decisions

See `DECISIONS.md` for detailed explanations of key architectural choices.

## Known Limitations

- Java + Spring Boot bonus endpoint is documented but not yet implemented
- Anthropic API integration requires a valid API key (optional feature)

## License

This project is part of a technical assessment and is provided as-is.
