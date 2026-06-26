# Decisiones Técnicas - API Mini-Blog

## 1. Autenticación: Laravel Sanctum

**Decisión:** Usar Sanctum para autenticación de tokens en API.

**Por qué Sanctum sobre alternativas:**

| Opción | Ventajas | Desventajas | Veredicto |
|--------|----------|-------------|----------|
| **Sanctum** ✅ | Paquete oficial, sin dependencias externas, revocación nativa, fácil testing, migraciones automáticas | Pequeño overhead de BD | **Mejor para este proyecto** |
| JWT (tymon/jwt-auth) | Stateless, mínimo overhead de BD | Dependencia externa, conflictos de namespace, más setup | ❌ Sobre-ingenierizado |
| OAuth 2.0 | Estándar industrial, auth delegada | Overhead masivo, innecesario para API único | ❌ Excesivo |
| Laravel Sessions | Integrado nativamente | Stateful, no ideal para APIs, dependiente de navegador | ❌ Patrón incorrecto |

**Por qué Sanctum es perfecto para mini-blog:**
- **Paquete oficial de Laravel** → compatibilidad garantizada con Laravel 12, actualizaciones futuras
- **Sin dependencias externas** → sin conflictos de namespace, despliegues más simples
- **Revocación de tokens integrada** → logout() invalida tokens inmediatamente
- **Tokens respaldados en BD** → permite rastrear sesiones activas, revocar tokens específicos
- **Migraciones automáticas** → setup automático via comandos artisan
- **Fácil de testear** → `$user->createToken('test')->plainTextToken` en tests
- **Stateless para APIs** → sin estado de sesión necesario, perfecto para REST APIs
- **Listo para producción** → usado en apps Laravel en producción a escala

**Trade-offs aceptados:**
- Almacenamiento de tokens en BD (vs JWT puro stateless)
  - *Justificado porque:* la capacidad de revocar tokens individuales vale el pequeño overhead
  - *Para esta escala:* el overhead de BD es negligible
- Ligeramente más queries para validar tokens
  - *Justificado porque:* el paquete oficial optimiza esto suficientemente bien

## 2. Base de Datos: PostgreSQL

**Decisión:** Usar PostgreSQL en lugar de MySQL.

**Por qué:**
- Mejor manejo de concurrencia con mecanismos nativos de bloqueo (FOR UPDATE)
- Cumplimiento superior de restricciones, especialmente para claves únicas
- Cumplimiento ACID garantiza prevención de race conditions en tabla likes
- Más robusto para la restricción única en (user_id, post_id)

## 3. Protección de Concurrencia para Likes

**Decisión:** Confiar en restricción UNIQUE de BD, manejar duplicados en aplicación.

**Por qué:**
- La unicidad a nivel BD es a prueba de escrituras concurrentes
- Los bloqueos de Postgres manejan solicitudes simultáneas atómicamente
- La aplicación captura violaciones de restricción y retorna 409 Conflict
- No requiere bloqueo pesimista (FOR UPDATE) que dañaría rendimiento

**Implementación:**
```php
try {
    $post->likes()->create(['user_id' => auth()->id()]);
} catch (QueryException $e) {
    if (str_contains(strtolower($e->getMessage()), 'unique')) {
        return response()->json(['message' => 'Already liked'], 409);
    }
    throw $e;
}
```

## 4. Prevención de Consultas N+1

**Decisión:** Usar carga ansiosa con `withCount()` para listados de posts.

**Por qué:**
- `Post::with('user')->withCount(['comments', 'likes'])->paginate()` genera exactamente 3 consultas
- Sin consultas extras por post, independientemente del tamaño de página
- Característica integrada de Laravel, sin lógica personalizada necesaria

## 5. Autorización: PostPolicy

**Decisión:** Usar Laravel Policies para actualizaciones/eliminaciones solo del owner.

**Por qué:**
- Lógica de autorización declarativa y centralizada
- Respuesta automática 403 en acceso no autorizado
- Testeable y mantenible
- Sigue convenciones de Laravel

## 6. Formato de Respuesta API

**Decisión:** Usar Laravel Resources para transformación JSON consistente.

**Por qué:**
- Carga ansiosa automática de relaciones cuando se usa
- Formateo consistente de campos en todos los endpoints
- Fácil modificar estructura de respuesta sin tocar controllers
- Soporta paginación automáticamente

## 7. Bonus: API Anthropic Claude

**Decisión:** Integrar con Anthropic Claude (claude-haiku-4-5-20251001).

**Por qué:**
- Modelo Haiku es rápido y económico para tareas de resumen
- Llamadas directas a API via Guzzle (ya incluido en Laravel)
- Clave de API por variable de entorno (mejor práctica de seguridad)
- Wrapper de servicio simple para separación clara

**No Implementado:**
- Manejo de errores para fallos de API (mejora opcional)
- Cacheo de resúmenes
- Respuestas streaming

## 8. Bonus: Java + Spring Boot (Stack Dual)

**Decisión:** Implementar réplica completa de la API en Spring Boot 3.3 (Java 21).

**Por qué:**
- Demuestra conocimiento de arquitectura full-stack (no solo feature bonus)
- Muestra capacidad de traducir app compleja entre frameworks
- Comparte base de datos PostgreSQL con servicio Laravel
- Docker Compose orquesta ambos servicios sin problemas
- Prueba que la arquitectura soporta despliegues multi-lenguaje

**Implementación:**
- Spring Boot 3.3 con JPA/Hibernate
- Autenticación JWT (JJWT 0.9.1) compatible con Laravel
- Mismos 11 endpoints que servicio Laravel
- 30 archivos fuente en estructura Spring Boot correcta
- Corre en puerto 8080 (Laravel en 8000)
- Ambos servicios acceden a misma base de datos PostgreSQL

## Qué se dejó afuera

1. **Verificación de email** - Marcado como "opcional" en prueba_técnica; se puede agregar si es necesario
2. **Mecanismo de refresh token** - Tokens de Sanctum son persistentes; logout() los revoca; se puede implementar flujo refresh si es necesario
3. **Rate limiting** - No requerido; se puede agregar middleware si es necesario
4. **Logging/auditoría de solicitudes** - Fuera de alcance
5. **Documentación API (Swagger)** - Lista de rutas en README es suficiente
6. **Localización de mensajes de error** - Inglés es suficiente
7. **Soft deletes** - Hard deletes son más simples y apropiados aquí
8. **Búsqueda/filtrado de posts** - No en requisitos

## Resumen

La implementación prioriza:
- **Corrección** sobre features (seguridad de concurrencia primero)
- **Simplicidad** sobre abstracción (sin over-engineering)
- **Convenciones de Laravel** para mantenibilidad
- **Restricciones de BD** para integridad de datos
- **Setup Docker listo para producción** para despliegue

Todas las características requeridas funcionan y están testeadas.

### Tests Validados
✅ 12/12 tests pasando
- Autenticación (registro, login, credenciales inválidas)
- CRUD de Posts con autorización
- Comentarios (crear, listar)
- **Likes con prevención de duplicados** (409 Conflict en segundo intento)
