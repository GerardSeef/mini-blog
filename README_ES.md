# API Mini-Blog

API RESTful para una plataforma mini-blog construida con Laravel 12, autenticación basada en tokens con Sanctum, y PostgreSQL.

## Características

- Registro de usuarios y autenticación basada en tokens Sanctum
- Crear, leer, actualizar y eliminar posts (solo el propietario puede editar/eliminar)
- Agregar y gestionar comentarios en posts
- Funcionalidad de likes/dislikes con protección contra duplicados
- Listados de posts paginados con conteos (sin consultas N+1)
- Bonus: Resúmenes de posts impulsados por IA usando API Anthropic Claude

## Configuración e Instalación

### Requisitos previos

- Docker y Docker Compose
- (O PHP 8.2+, Composer, y PostgreSQL si se ejecuta localmente)

### Inicio rápido con Docker

```bash
# Clonar el repositorio
git clone <repo-url>
cd mini-blog

# Iniciar los servicios
docker-compose up -d

# La API estará disponible en http://localhost:8000
```

### Configuración manual

```bash
# Instalar dependencias
composer install

# Copiar archivo de entorno
cp .env.example .env

# Generar clave de aplicación
php artisan key:generate

# Configurar base de datos (requiere PostgreSQL ejecutándose)
# Editar .env con credenciales de BD, luego:
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

ANTHROPIC_API_KEY=sk-ant-xxx-opcional
```

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

## Limitaciones Conocidas

- El endpoint de Java + Spring Boot está documentado pero no implementado
- La integración de Anthropic API requiere una clave API válida (característica opcional)

## Licencia

Este proyecto es parte de una evaluación técnica y se proporciona tal cual.

---

**Contacto:** cbenitez@tsgroup.com.ar
