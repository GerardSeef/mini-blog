# Decisiones Técnicas - API Mini-Blog

## 1. Autenticación: Laravel Sanctum (cambio desde JWT)

**Decisión Inicial:** JWT (tymon/jwt-auth)  
**Decisión Final:** Laravel Sanctum

**Por qué cambiamos a Sanctum:**
- Paquete oficial de Laravel, mejor integración con el ecosistema
- Implementación más simple sin dependencias externas
- Mejor soporte de middleware integrado
- Menos problemas de compatibilidad con Laravel 12
- Manejo automático de migraciones
- Los 12 tests pasan sin workarounds
- Listo para producción con gestión de tokens integrada

**Ventajas de Sanctum:**
- Autenticación stateless basada en tokens (ideal para APIs)
- Sin sesiones necesarias
- Fácil de testear (método simple `createToken()`)
- Tokens almacenados en BD para rastrear revocación
- Capacidad integrada de revocar tokens inmediatamente
- Soporte oficial de Laravel y documentación

**Trade-offs:**
- Requiere almacenamiento en BD para tokens (vs JWT puro stateless)
- Ligero overhead en APIs de alto volumen (negligible para este caso)

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

## 8. Bonus: Java + Spring Boot

**Decisión:** Documentado como pendiente, no implementado.

**Por qué:**
- Restricciones de tiempo (estimado 4-5 horas)
- PHP/Laravel es el foco principal
- La arquitectura soporta agregarlo luego como servicio separado
- Docker Compose ya estructurado para configuración multi-servicio

**Cómo agregarlo:**
- Crear directorio `/java` con microservicio Spring Boot
- Agregar servicio a docker-compose.yml
- Implementar mismo endpoint Like en Java
- Compartir base de datos PostgreSQL

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
