# API Mini-Blog

Una API RESTful dual-stack completa para una plataforma mini-blog:
- **Laravel 12** (PHP 8.2) con autenticación Sanctum en puerto **8000**
- **Spring Boot 3.3** (Java 21) con autenticación JWT en puerto **8080**
- Base de datos **PostgreSQL 16** compartida

## Características

- Registro de usuarios y autenticación basada en tokens (Sanctum/JWT)
- Crear, leer, actualizar y eliminar posts (solo el propietario puede editar/eliminar)
- Agregar y gestionar comentarios en posts
- Funcionalidad de likes/dislikes con protección contra duplicados
- Listados de posts paginados con conteos (sin consultas N+1)
- Bonus: Resúmenes de posts impulsados por IA usando API Anthropic Claude
- Bonus: Implementación completa en Java/Spring Boot con endpoints idénticos

## Configuración e Instalación

### Requisitos previos

- Docker y Docker Compose
- (O PHP 8.2+, Composer, y PostgreSQL si se ejecuta localmente)

### Inicio rápido con Docker

```bash
# Clonar el repositorio
git clone https://github.com/GerardSeef/mini-blog.git
cd mini-blog

# Iniciar todos los servicios (Laravel, Java, PostgreSQL)
docker-compose up -d

# Las APIs estarán disponibles en:
# - Laravel:  http://localhost:8000/api
# - Java:     http://localhost:8080/api
# - BD:       localhost:5432
```

### Configuración manual (solo Laravel)

```bash
# Instalar dependencias
composer install

# Copiar archivo de entorno
cp .env.example .env

# Generar clave de aplicación
php artisan key:generate

# Configurar base de datos y ejecutar migraciones
php artisan migrate

# Iniciar el servidor
php artisan serve
```

## Configuración

### Variables de Entorno

Crear archivo `.env` basado en `.env.example` y configurar:

```
DB_CONNECTION=pgsql
DB_HOST=db
DB_PORT=5432
DB_DATABASE=miniblog
DB_USERNAME=postgres
DB_PASSWORD=postgres

APP_KEY=base64:...  # Generada por php artisan key:generate

ANTHROPIC_API_KEY=sk-ant-...  # Opcional, para feature de resúmenes con IA
```

**Nota:** 
- Laravel usa **Sanctum** para autenticación de tokens (sin config JWT requerida)
- Java usa **JWT** (JJWT 0.9.1) con JWT_SECRET del entorno
- Ambos se autentican contra la misma base de datos PostgreSQL compartida

## Endpoints de la API

### Autenticación

- `POST /api/auth/register` - Registrar nuevo usuario
- `POST /api/auth/login` - Login y recibir token
- `POST /api/auth/logout` - Logout (protegido)
- `GET /api/auth/me` - Obtener info del usuario actual (protegido)

### Posts

- `GET /api/posts` - Listar todos los posts (paginado, incluye conteos)
- `POST /api/posts` - Crear nuevo post (protegido)
- `GET /api/posts/{id}` - Obtener un post específico
- `PUT /api/posts/{id}` - Actualizar un post (solo propietario, protegido)
- `DELETE /api/posts/{id}` - Eliminar un post (solo propietario, protegido)

### Comentarios

- `GET /api/posts/{id}/comments` - Listar comentarios de un post
- `POST /api/posts/{id}/comments` - Agregar comentario (protegido)

### Likes

- `POST /api/posts/{id}/like` - Dar like a un post (protegido, retorna 409 si ya está likeado)
- `DELETE /api/posts/{id}/like` - Quitar like de un post (protegido)

### Bonus

- `GET /api/posts/{id}/summary` - Obtener resumen impulsado por IA del post y comentarios (protegido)

## Tests

```bash
# Correr tests
docker-compose exec app php artisan test

# O localmente
php artisan test
```

## Estructura del Proyecto

- `app/Models/` - Modelos Eloquent (User, Post, Comment, Like)
- `app/Http/Controllers/Api/` - Controladores API
- `app/Http/Requests/` - Validación de formularios
- `app/Http/Resources/` - Transformadores JSON
- `app/Policies/` - Políticas de autorización (PostPolicy)
- `app/Services/` - Lógica de negocio (AnthropicService)
- `database/migrations/` - Esquema de base de datos
- `routes/api.php` - Definiciones de rutas de API
- `tests/Feature/` - Tests funcionales

## Decisiones Técnicas

Ver `DECISIONS_ES.md` para explicaciones detalladas de todas las decisiones arquitectónicas.

## Estado de Implementación

✅ **Todas las características requeridas implementadas:**
- Autenticación de usuarios (Sanctum en Laravel, JWT en Java)
- CRUD completo para posts, comentarios, likes
- Paginación sin consultas N+1
- Prevención segura de duplicados en likes (nivel BD)
- Suite de pruebas integral (12 tests, 23 assertions)
- Orquestación Docker Compose

✅ **Ambas características bonus implementadas:**
- Resúmenes impulsados por IA (API Anthropic Claude)
- API completa Java/Spring Boot replica (dual-stack)

⚠️ **Mejoras opcionales no incluidas:**
- Verificación de email (marcado como opcional en requisitos)
- Flujo de refresh token (tokens Sanctum son persistentes)
- Rate limiting (no en requisitos)
- Documentación Swagger/OpenAPI (lista de rutas en README suficiente)

## Licencia

Este proyecto es parte de una evaluación técnica y se proporciona tal cual.

---

**Contacto:** cbenitez@tsgroup.com.ar
