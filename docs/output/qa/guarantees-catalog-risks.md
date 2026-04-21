# Matriz de Riesgos — Catálogo de Garantías (SPEC-007)

**Proyecto:** Insurance-Quoter-Core · `plataforma-core-ohs`
**Feature:** guarantees-catalog
**Fecha:** 2026-04-21
**Analista:** risk-identifier

---

## Resumen

Total: 6 | Alto (A): 2 | Medio (S): 3 | Bajo (D): 1

---

## Detalle

| ID    | HU     | Descripción del Riesgo                                                              | Factores                                          | Nivel | Testing      |
|-------|--------|-------------------------------------------------------------------------------------|---------------------------------------------------|-------|--------------|
| R-001 | HU-01  | `findAllTarifable()` retorna un conjunto incorrecto → error en cálculo de prima neta | Integración con Insurance-Quoter-Back; impacto financiero indirecto en la cotización | A     | Obligatorio  |
| R-002 | HU-01  | Archivo `fixtures/guarantees.json` ausente o mal formado provoca arranque silencioso sin error claro | Código nuevo sin historial; fail-fast no garantizado si la excepción es capturada | A     | Obligatorio  |
| R-003 | HU-01  | Contrato de respuesta REST inconsistente (`tarifable` serializado incorrectamente)  | Código nuevo; dependencia de otro microservicio   | S     | Recomendado  |
| R-004 | HU-01  | Degradación de rendimiento si el catálogo crece en número de garantías              | Alta frecuencia de uso (consultado en cada cotización) | S     | Recomendado  |
| R-005 | HU-01  | Desincronización entre fixture en memoria y cambios en el archivo JSON sin reinicio | Lógica de carga eager; sin mecanismo de recarga   | S     | Recomendado  |
| R-006 | HU-01  | Campo `description` vacío o nulo en el fixture pasa sin validación                  | Feature interno/administrativo; sin escritura de usuario | D     | Opcional     |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: `findAllTarifable()` retorna conjunto incorrecto

- **Descripción:** `GuaranteeJsonAdapter.findAllTarifable()` filtra en memoria. Si el predicado es incorrecto (ej. `!tarifable` en lugar de `tarifable`) o si el fixture contiene garantías con `tarifable: false` no previstas, `Insurance-Quoter-Back` calculará la prima sobre un conjunto errado.
- **Impacto:** Cálculo de prima neta incorrecto → cotizaciones con valores equivocados → impacto financiero en el negocio.
- **Mitigación:**
  - Test unitario `GuaranteeJsonAdapterTest` con fixture mixto (tarifable=true y tarifable=false) que verifica que `findAllTarifable()` solo retorna `true`.
  - Fixture de producción actual tiene todas las garantías con `tarifable: true`; documentar explícitamente si se añaden con `false`.
  - Contract test entre `Insurance-Quoter-Back` y `Insurance-Quoter-Core` para validar el conjunto de códigos tarifables.
- **Tests obligatorios:**
  - `GuaranteeJsonAdapterTest#findAllTarifable_returnsOnlyTarifableGuarantees`
  - `GuaranteeJsonAdapterTest#findAllTarifable_excludesNonTarifable` (fixture con al menos una no-tarifable)
  - Test de contrato (consumer-driven) entre ambos microservicios — recomendado como mejora futura
- **Bloqueante para release:** ✅ Sí

---

### R-002: Fail-fast no garantizado al arrancar

- **Descripción:** Si `GuaranteeJsonAdapter` captura la excepción de carga (IOException, JsonParseException) en un bloque try-catch vacío o la convierte en lista vacía, la aplicación arranca sin error y expone el endpoint con datos incorrectos o vacíos sin que el operador lo detecte.
- **Impacto:** El sistema corre con catálogo vacío; ninguna garantía disponible para cotización; errores silenciosos en producción.
- **Mitigación:**
  - El adapter debe relanzar como `IllegalStateException` (o `BeanCreationException`) para que Spring aborte el inicio.
  - Test unitario `GuaranteeJsonAdapterTest#constructor_throwsWhenFileNotFound` verifica que se lanza excepción al no encontrar el recurso.
  - Test unitario `GuaranteeJsonAdapterTest#constructor_throwsWhenJsonInvalid` verifica que JSON malformado también falla.
  - Revisión de código: confirmar que no existe `catch (Exception e) { return Collections.emptyList(); }` en el adapter.
- **Tests obligatorios:**
  - `GuaranteeJsonAdapterTest#constructor_throwsWhenFileNotFound`
  - `GuaranteeJsonAdapterTest#constructor_throwsWhenJsonMalformed`
- **Bloqueante para release:** ✅ Sí

---

## Riesgos MEDIO — Acciones Recomendadas

### R-003: Contrato REST inconsistente

- **Acción:** Test de integración `GuaranteeCatalogIntegrationTest` debe verificar el JSON exacto del response (`code`, `description`, `tarifable` como booleano, no como string). Jackson serializa `boolean` como `true`/`false` de forma nativa, pero un record con `Boolean` (boxed) puede serializar como `null`.
- **Verificación:** Confirmar que `GuaranteeDto` usa `boolean` primitivo, no `Boolean`.

### R-004: Rendimiento con catálogo grande

- **Acción:** El catálogo actual tiene 3 garantías — riesgo bajo en la práctica. Si crece a >100 entradas, evaluar caching explícito con `@Cacheable`. Documentar el límite esperado del catálogo en la spec.
- **Monitoreo:** Incluir métrica de tiempo de respuesta de `GET /v1/catalogs/guarantees` en el dashboard de observabilidad.

### R-005: Desincronización fixture/memoria

- **Acción:** Documentar en el README del módulo que cualquier cambio en `fixtures/guarantees.json` requiere reinicio de la aplicación. No es un defecto, es una decisión de diseño consciente (RN-2).
- **Mitigación a futuro:** Si se requiere recarga en caliente, evaluar Spring Cloud Config o un endpoint de admin `/actuator/refresh`.

---

## Cobertura de Criterios de Aceptación

| Criterio | Riesgo relacionado | Cubierto por |
|----------|--------------------|--------------|
| CRITERIO-1.1: Retorno del catálogo completo | R-003 | `GuaranteeCatalogIntegrationTest` |
| CRITERIO-1.2: Catálogo vacío | R-002 (parcial) | `GuaranteeJsonAdapterTest` |
| CRITERIO-1.3: Archivo no encontrado | R-002 | `GuaranteeJsonAdapterTest#constructor_throwsWhenFileNotFound` |
| CRITERIO-1.4: `findAllTarifable()` correcto | R-001 | `GuaranteeJsonAdapterTest#findAllTarifable_*` |
