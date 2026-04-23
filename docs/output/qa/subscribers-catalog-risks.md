# Matriz de Riesgos — Catálogo de Suscriptores

**Feature:** subscribers-catalog | **Spec:** SPEC-002 | **Fecha:** 2026-04-21
**Analista QA:** Risk Identifier — ASDD

---

## Resumen

Total: 6 | Alto (A): 0 | Medio (S): 3 | Bajo (D): 3

> Este feature es de solo lectura, sin base de datos, sin autenticación requerida por diseño y con datos estáticos. El perfil de riesgo es inherentemente bajo. Ningún riesgo bloquea el release.

---

## Detalle

| ID    | HU     | Descripción del Riesgo | Factores | Nivel | Testing |
|-------|--------|------------------------|----------|-------|---------|
| R-001 | HU-01  | `ObjectMapper` con configuración personalizada en el contexto Spring puede deserializar el fixture con resultados inesperados: la anotación `@ConditionalOnMissingBean` hace que nuestro `ObjectMapper` sea ignorado si ya existe uno con diferente configuración (ej. `snake_case`, módulos de fecha). Campos sin coincidencia se deserializan como `null` silenciosamente, retornando suscriptores con `id` o `name` nulos | Código nuevo sin historial, dependencia de infraestructura Spring (comportamiento de autoconfiguración) | S | Obligatorio |
| R-002 | HU-01  | Ausencia de autenticación en `GET /v1/subscribers`: el endpoint no requiere auth por diseño ("servicio interno"). Sin controles de red (service mesh, API Gateway), cualquier proceso con acceso a la red interna puede consultar el catálogo y obtener información de los suscriptores del negocio | Seguridad, alta frecuencia de uso esperada (consultado en cada cotización) | S | Obligatorio |
| R-003 | HU-01  | Sin validación de esquema del fixture JSON — Jackson deserializa con `DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES` desactivado por defecto, lo que significa que un typo en el fixture (ej. `"nane"` en lugar de `"name"`) produce un `Subscriber` con campo `null` sin ningún error. El servicio arranca correctamente y retorna datos corruptos silenciosamente | Código nuevo, lógica de negocio de solo datos pero frágil ante errores de configuración | S | Recomendado |
| R-004 | HU-01  | Fixture ausente o no empaquetado en el JAR de deployment: si `fixtures/subscribers.json` no está en el classpath del entorno de producción (ej. excluido del fat-jar por configuración de build), la app falla al arrancar con `IllegalStateException`. El fail-fast protege la consistencia, pero puede confundirse con un error de Spring si el mensaje no es suficientemente descriptivo | Dependencia de configuración de deployment, código nuevo | D | Recomendado |
| R-005 | HU-01  | Sin recarga en caliente del catálogo: agregar o modificar suscriptores requiere reiniciar la aplicación. En entornos con alta disponibilidad, esto implica un downtime controlado. No es un riesgo funcional pero sí operativo a medida que el negocio crece | Feature interna/administrativa, impacto operativo limitado | D | Opcional |
| R-006 | HU-01  | Formato del campo `id` no validado ni documentado en el contrato: el tipo `String` acepta cualquier valor. Si `Insurance-Quoter-Back` u otros consumidores asumen el patrón `SUB-XXX`, un cambio en el fixture puede romper el contrato silenciosamente sin fallo en compilación ni en tests | Refactorización de datos, deuda de contrato entre servicios | D | Opcional |

---

## Plan de Mitigación — Riesgos MEDIO

### R-001: `ObjectMapper` con configuración personalizada

- **Mitigación**: (1) Verificar en el test de integración `SubscriberCatalogIntegrationTest` que los suscriptores retornados no tienen campos `null`. Test ya cubre parcialmente esto (verifica `id` y `name`). (2) Agregar test específico que verifique que el `ObjectMapper` usado respeta los nombres de campo exactos del JSON (`id`, `name` en camelCase). (3) Evaluar declarar el `ObjectMapper` en `SubscriberConfig` sin `@ConditionalOnMissingBean` para tener control total sobre la configuración de deserialización.
- **Tests recomendados**: Test unitario `SubscriberJsonAdapterTest` ya cubre el caso de un JSON válido con un `ObjectMapper` puro. Agregar un test con `ObjectMapper` configurado con `PropertyNamingStrategies.SNAKE_CASE` para verificar que falla correctamente (no deserializa silenciosamente).
- **Bloqueante para release**: No

---

### R-002: Ausencia de autenticación en `GET /v1/subscribers`

- **Mitigación**: (1) Documentar explícitamente en la spec y en el README que el endpoint es de acceso interno y debe estar protegido por controles de red en el entorno de producción (ej. service mesh, VPC privada, API Gateway con restricción de IP). (2) Evaluar si en el futuro se requiere algún token de servicio interno (API key). Por ahora, la decisión de arquitectura es no requerir auth.
- **Tests recomendados**: Test que verifique que el endpoint responde sin credentials (confirmar que no hay configuración de seguridad Spring que lo bloquee accidentalmente). Incluir en el test de integración ya existente.
- **Bloqueante para release**: No

---

## Cobertura de Tests Existente vs. Riesgos Identificados

| Riesgo | Test existente | Estado |
|--------|---------------|--------|
| R-001 ObjectMapper personalizado | `SubscriberCatalogIntegrationTest` (verifica id/name no nulos) | Parcial — agregar test con ObjectMapper mal configurado |
| R-002 Sin auth | `SubscriberCatalogIntegrationTest` (contexto arranca sin error de seguridad) | Cubierto implícitamente |
| R-003 Typo en fixture | `SubscriberJsonAdapterTest` cubre JSON válido/inválido | Parcial — no cubre typo silencioso |
| R-004 Fixture ausente | `SubscriberJsonAdapterTest.constructor_throwsWhenResourceIsUnreadable` | Cubierto |
| R-005 Sin recarga en caliente | N/A — decisión de diseño aceptada | Bajo (D), no bloqueante |
| R-006 Formato id sin validar | N/A — deuda de contrato | Bajo (D), documentar en api-contracts.md |
