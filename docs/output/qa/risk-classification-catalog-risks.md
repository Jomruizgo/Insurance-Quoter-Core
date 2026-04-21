# Matriz de Riesgos — Catálogo de Clasificaciones de Riesgo (SPEC-006)

**Fecha:** 2026-04-21
**Feature:** `risk-classification-catalog`
**Responsable QA:** spec-generator / risk-identifier

---

## Resumen

Total: 5 | Alto (A): 2 | Medio (S): 1 | Bajo (D): 2

| Nivel | Cantidad | Acción |
|-------|----------|--------|
| **ALTO (A)** | 2 | Testing OBLIGATORIO — bloquea el release |
| **MEDIO (S)** | 1 | Testing RECOMENDADO — documentar si se omite |
| **BAJO (D)** | 2 | Testing OPCIONAL — priorizar en backlog |

---

## Detalle

| ID    | HU     | Descripción del Riesgo                                                             | Factores                                                     | Nivel | Testing      |
|-------|--------|------------------------------------------------------------------------------------|--------------------------------------------------------------|-------|--------------|
| R-001 | HU-01  | Fallo en arranque por fixture ausente o malformado bloquea Insurance-Quoter-Back   | Operación irrecuperable sin rollback / integración externa   | A     | Obligatorio  |
| R-002 | HU-01  | Desincronización de códigos entre fixture y validaciones de Insurance-Quoter-Back  | Integración externa / código nuevo sin historial             | A     | Obligatorio  |
| R-003 | HU-01  | Indisponibilidad del endpoint afecta todas las cotizaciones activas                | Alta frecuencia de uso / código nuevo sin historial          | S     | Recomendado  |
| R-004 | HU-01  | Lista retornada mutable expuesta accidentalmente a consumidores                    | Refactorización interna sin cambio de comportamiento         | D     | Opcional     |
| R-005 | HU-01  | Endpoint sin autenticación accesible desde redes no controladas                   | Feature interna / decisión de diseño documentada (RN-5)     | D     | Opcional     |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: Fallo en arranque por fixture ausente o malformado

**Escenario:** Si `fixtures/risk-classifications.json` no existe en el classpath o contiene JSON inválido, el `RiskClassificationJsonAdapter` lanza `IllegalStateException` en el constructor, impidiendo que Spring levante el contexto de la aplicación. Como consecuencia, el endpoint `GET /v1/catalogs/risk-classification` nunca llega a estar disponible, y `Insurance-Quoter-Back` no puede completar el flujo de cotización.

- **Mitigación implementada:**
  - `RiskClassificationJsonAdapter` usa patrón fail-fast: la excepción se lanza en el constructor (no lazy).
  - El mensaje de error incluye el descriptor del recurso para facilitar diagnóstico.
  - El archivo `fixtures/risk-classifications.json` está versionado en `src/main/resources/`.
- **Tests obligatorios:**
  - `RiskClassificationJsonAdapterTest#constructor_throwsWhenResourceIsUnreadable` ✅ (ya implementado)
  - Test de pipeline CI que verifique la presencia del fixture en el artefacto de despliegue.
- **Bloqueante para release:** ✅ Sí

---

### R-002: Desincronización de códigos entre fixture y validaciones de Insurance-Quoter-Back

**Escenario:** `Insurance-Quoter-Back` valida que la clasificación de riesgo seleccionada por el suscriptor sea un código válido devuelto por este catálogo. Si se elimina o renombra un código en el fixture sin actualizar la lógica de validación de Back, las cotizaciones con esa clasificación quedarán bloqueadas o serán rechazadas incorrectamente.

- **Mitigación implementada:**
  - Los 3 códigos canónicos (`STANDARD`, `PREFERRED`, `SUBSTANDARD`) están documentados en RN-6 y en la spec.
  - El test de integración `RiskClassificationCatalogIntegrationTest#getAll_returnsThreeClassificationsFromFixture` valida los 3 códigos exactos ✅.
- **Controles adicionales recomendados:**
  - Definir un contrato de API (`api-contracts.md`) que liste explícitamente los códigos aceptados por Back.
  - Todo cambio al fixture debe ir acompañado de un PR que también actualice las validaciones de Back (regla de código en Pull Request template).
  - Agregar un test de contrato (consumer-driven) si en el futuro se adopta Pact o similar.
- **Tests obligatorios:**
  - `RiskClassificationCatalogIntegrationTest#getAll_returnsThreeClassificationsFromFixture` ✅ (ya implementado)
  - `RiskClassificationCatalogIntegrationTest#getAll_containsAllExpectedCodes` ✅ (ya implementado)
- **Bloqueante para release:** ✅ Sí

---

## Plan de Mitigación — Riesgo MEDIO

### R-003: Indisponibilidad del endpoint afecta todas las cotizaciones activas

**Escenario:** El endpoint es consumido por `Insurance-Quoter-Back` en el flujo de cotización cada vez que se valida una clasificación de riesgo. Si el servicio Core cae o responde con latencia elevada, el flujo de cotización completo se degrada.

- **Mitigación implementada:**
  - Datos en memoria — sin dependencia de base de datos ni I/O en tiempo de request; latencia mínima.
  - El contexto Spring falla rápido al arrancar si el fixture no se puede cargar (no hay estado inconsistente en producción).
- **Controles adicionales recomendados:**
  - Configurar un health check en `Insurance-Quoter-Back` que verifique la disponibilidad del endpoint Core antes de aceptar cotizaciones.
  - Agregar métricas de disponibilidad (Actuator + Prometheus) en el endpoint `/v1/catalogs/risk-classification`.
  - Evaluar circuit breaker en `Insurance-Quoter-Back` para el cliente HTTP hacia Core.
- **Tests recomendados:**
  - Test de load básico para verificar que la latencia del endpoint se mantiene por debajo de 50 ms bajo carga concurrente.
- **Bloqueante para release:** ❌ No (mitigable operacionalmente)

---

## Riesgos BAJO — Notas

### R-004: Lista retornada mutable
Ya mitigado en implementación: `RiskClassificationJsonAdapter.findAll()` retorna `List.copyOf(classifications)`, garantizando inmutabilidad. Riesgo residual nulo. Sin acción adicional requerida.

### R-005: Endpoint sin autenticación
Decisión de diseño explícita (RN-5): el endpoint es un servicio interno de red, consumido solo por `Insurance-Quoter-Back` en la misma infraestructura. El riesgo de exposición está controlado por segmentación de red. Si en el futuro el servicio se expone externamente, se deberá revisar este punto.

---

## Cobertura de Tests Existente

| Test | Tipo | Riesgos Cubiertos |
|------|------|-------------------|
| `GetRiskClassificationsUseCaseImplTest` (3 tests) | Unitario | R-003 (delegación correcta) |
| `RiskClassificationJsonAdapterTest` (4 tests) | Unitario | R-001, R-004 |
| `RiskClassificationRestMapperTest` (2 tests) | Unitario | R-003 (mapeo correcto) |
| `RiskClassificationControllerTest` (2 tests) | Unitario | R-003 (HTTP 200) |
| `RiskClassificationCatalogIntegrationTest` (4 tests) | Integración | R-001, R-002, R-003 |

**Total: 15 tests** — cobertura estimada ≥ 80% en lógica de negocio.
