# Matriz de Riesgos — Agents Catalog (SPEC-003)

> Feature: `agents-catalog` | Microservicio: `Insurance-Quoter-Core` (puerto 8081)
> Fecha: 2026-04-21 | Metodología: Regla ASD del CoE

---

## Resumen

| Total | Alto (A) | Medio (S) | Bajo (D) |
|-------|----------|-----------|----------|
| 6     | 2        | 3         | 1        |

---

## Detalle

| ID    | HU     | Descripción del Riesgo                                                              | Factores                                             | Nivel | Testing      |
|-------|--------|-------------------------------------------------------------------------------------|------------------------------------------------------|-------|--------------|
| R-001 | HU-02  | `existsByCode` devuelve resultado incorrecto, permitiendo folios con agentes falsos | Integración cross-service, impacto en flujo crítico  | A     | Obligatorio  |
| R-002 | HU-01  | Fallo al arrancar por JSON malformado/ausente deja el servicio inoperable           | Integraciones con sistemas externos (fixture), fail-fast | A  | Obligatorio  |
| R-003 | HU-01  | Catálogo desactualizado servido desde memoria sin mecanismo de recarga              | Código nuevo sin historial, alta frecuencia de uso   | S     | Recomendado  |
| R-004 | HU-02  | Comparación case-sensitive en `existsByCode` rechaza códigos con diferencia de mayúsculas | Lógica de negocio, comportamiento no obvio      | S     | Recomendado  |
| R-005 | HU-01  | Endpoint sin autenticación expuesto si el servicio se despliega en red pública      | Seguridad de acceso, componente con dependencias     | S     | Recomendado  |
| R-006 | HU-01  | Estructura del JSON de fixture no coincide con el record `Agent`                    | Refactorización sin cambio de comportamiento externo | D     | Opcional     |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: `existsByCode` devuelve resultado incorrecto

**Contexto:** `Insurance-Quoter-Back` invoca `existsByCode` al crear un folio (`POST /v1/folios`). Si retorna `true` para un código inexistente o `false` para uno válido, el resultado es: folios creados con agentes inválidos (integridad de datos) o rechazo de agentes legítimos (experiencia de usuario).

- **Mitigación:** tests unitarios exhaustivos sobre `AgentJsonAdapter.existsByCode` cubriendo código existente, inexistente, lista vacía y comparación exacta (case-sensitive). Test de integración `AgentCatalogIntegrationTest` verifica el comportamiento end-to-end con el fixture real.
- **Tests obligatorios:**
  - `AgentJsonAdapterTest#existsByCode_returnsTrueWhenCodeExists` ✅
  - `AgentJsonAdapterTest#existsByCode_returnsFalseWhenCodeDoesNotExist` ✅
  - `AgentJsonAdapterTest#existsByCode_returnsFalseWhenCatalogIsEmpty` ✅
  - `AgentCatalogIntegrationTest#existsByCode_returnsTrueForExistingAgent` ✅
  - `AgentCatalogIntegrationTest#existsByCode_returnsFalseForUnknownAgent` ✅
- **Bloqueante para release:** ✅ Sí

---

### R-002: Fallo al arrancar por JSON malformado o ausente

**Contexto:** `AgentJsonAdapter` carga el fixture en el constructor. Si el archivo no existe o contiene JSON inválido, la excepción propagada deja el contexto de Spring sin levantar, haciendo que el servicio completo quede inoperativo (incluyendo todos los demás endpoints).

- **Mitigación:** comportamiento fail-fast con `IllegalStateException` y mensaje descriptivo. El test `AgentJsonAdapterTest#constructor_throwsWhenResourceIsUnreadable` valida este camino. En despliegue, incluir el archivo en el artefacto y verificar en pipeline CI.
- **Tests obligatorios:**
  - `AgentJsonAdapterTest#constructor_throwsWhenResourceIsUnreadable` ✅
- **Bloqueante para release:** ✅ Sí

---

## Mitigación — Riesgos MEDIO

### R-003: Catálogo desactualizado en memoria

- **Mitigación:** documentar explícitamente que un cambio en el fixture requiere redeploy. Añadir comentario en `agents.json` o en `AgentConfig`. No se requiere test automatizado, pero sí un proceso operativo documentado.

### R-004: Comparación case-sensitive en `existsByCode`

- **Mitigación:** ya cubierto por RN-7 en la spec. Documentar en el contrato de integración que `Insurance-Quoter-Back` debe enviar el código exactamente como lo devuelve `GET /v1/agents`. Test de contrato recomendado en `Auto_Api_Screenplay`.

### R-005: Endpoint sin autenticación

- **Mitigación:** confirmar que el servicio opera únicamente en red interna (VPC/namespace de Kubernetes). Si en algún momento se expone externamente, añadir autenticación básica o API key antes del release.
