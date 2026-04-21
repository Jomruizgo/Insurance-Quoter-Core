# Matriz de Riesgos — Catálogo de Giros de Negocio (SPEC-004)

**Feature:** business-lines-catalog  
**Microservicio:** Insurance-Quoter-Core (puerto 8081)  
**Fecha:** 2026-04-21

---

## Resumen

Total: 6 | Alto (A): 2 | Medio (S): 3 | Bajo (D): 1

---

## Detalle

| ID    | HU     | Descripción del Riesgo                                                             | Factores                                              | Nivel | Testing      |
|-------|--------|------------------------------------------------------------------------------------|-------------------------------------------------------|-------|--------------|
| R-001 | HU-01  | `fireKey` incorrecta o ausente en el fixture afecta el cálculo de prima neta       | Dato crítico para cálculo financiero; sin validación en DB | A  | Obligatorio  |
| R-002 | HU-01  | Fallo al arrancar si `business-lines.json` no existe o es malformado (fail-fast)   | Código nuevo sin historial; integridad del contexto Spring | A  | Obligatorio  |
| R-003 | HU-01  | Catálogo con datos desactualizados o inconsistentes entre ambientes                 | Dato gestionado por configuración (sin base de datos)  | S     | Recomendado  |
| R-004 | HU-01  | Respuesta con campos nulos si el JSON tiene entradas incompletas                   | Código nuevo; deserialización sin validación estricta  | S     | Recomendado  |
| R-005 | HU-01  | Degradación del endpoint bajo alta concurrencia (lista cargada en memoria compartida) | Alta frecuencia de uso; consumido por cada cotización | S     | Recomendado  |
| R-006 | HU-01  | Cambio de estructura del fixture rompe silenciosamente el mapeo a `BusinessLineDto` | Refactorización futura sin tests de contrato           | D     | Opcional     |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: `fireKey` incorrecta afecta el cálculo de prima neta

- **Contexto:** La `fireKey` de cada `BusinessLine` es el dato que `Insurance-Quoter-Back` usa para calcular la prima neta. Si este valor es incorrecto en el fixture, el cálculo se realizará con datos erróneos sin error visible en runtime.
- **Mitigación:**
  - Validar en `BusinessLineJsonAdapterTest` que cada entrada del fixture tiene `fireKey` no nulo y no vacío.
  - Considerar añadir validación defensiva en `BusinessLineJsonAdapter` que lance `IllegalStateException` si algún campo es blank al cargar.
  - Revisar el fixture `business-lines.json` como parte del proceso de cambio de catálogo (PR review obligatorio).
- **Tests obligatorios:**
  - Test unitario: `BusinessLineJsonAdapterTest` — verificar `fireKey` no vacía en cada elemento deserializado.
  - Test de integración: `BusinessLineCatalogIntegrationTest` — verificar que el response contiene `fireKey` con valor en cada elemento.
- **Bloqueante para release:** ✅ Sí

---

### R-002: Fallo silencioso o inconsistente si el archivo JSON es inválido

- **Contexto:** Si `business-lines.json` no existe en el classpath o tiene JSON malformado, el comportamiento esperado es fail-fast al arrancar (RN-3). Sin embargo, si el adapter no lanza una excepción clara, Spring podría iniciar con el bean en estado inconsistente.
- **Mitigación:**
  - `BusinessLineJsonAdapter` debe capturar `IOException` / `JsonProcessingException` y relanzar como `IllegalStateException` o `BeanCreationException` con mensaje descriptivo.
  - Test unitario `BusinessLineJsonAdapterTest` debe incluir el escenario de archivo no encontrado y verificar que se lanza la excepción correcta.
  - En entornos CI/CD, ejecutar el test de integración `BusinessLineCatalogIntegrationTest` que valida el contexto completo.
- **Tests obligatorios:**
  - Test unitario: `BusinessLineJsonAdapterTest` — escenario `classpath:fixtures/invalid.json` → excepción lanzada.
  - Test de integración: `BusinessLineCatalogIntegrationTest` — contexto arranca correctamente con el fixture real.
- **Bloqueante para release:** ✅ Sí

---

## Riesgos MEDIO — Acciones Recomendadas

### R-003: Datos desactualizados entre ambientes
- Establecer proceso de revisión del fixture al hacer deploy a producción.
- Documentar en el README del módulo cómo actualizar el catálogo.

### R-004: Campos nulos por entradas incompletas en JSON
- Añadir aserción en `BusinessLineJsonAdapterTest` que verifique que `code`, `description` y `fireKey` son no nulos tras la deserialización.
- Considerar usar `@JsonProperty(required = true)` en un DTO de deserialización intermedio.

### R-005: Degradación bajo alta concurrencia
- La lista se carga una vez en el constructor y es inmutable (o al menos no se modifica), por lo que el riesgo es bajo en la implementación actual.
- Si el catálogo crece significativamente (>500 entradas), evaluar si la búsqueda lineal impacta la latencia.
- Documentar la estrategia de carga en el CLAUDE.md del módulo.
