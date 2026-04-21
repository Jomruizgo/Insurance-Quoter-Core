# Escenarios Gherkin — Agents Catalog (SPEC-003)

> Feature: `agents-catalog` | Microservicio: `Insurance-Quoter-Core` (puerto 8081)
> Fecha: 2026-04-21 | Generado desde spec SPEC-003 v1.0

---

```gherkin
#language: es
Característica: Catálogo de agentes disponibles

  Como sistema cliente (Insurance-Quoter-Back)
  Quiero consultar el catálogo de agentes y validar su existencia
  Para mostrar opciones al usuario y asegurar integridad al crear folios

  # ─────────────────────────────────────────────
  # HU-01: Consultar catálogo de agentes
  # ─────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Consultar catálogo de agentes con datos configurados
    Dado que el servicio de catálogos está disponible
    Y el catálogo de agentes contiene los siguientes agentes:
      | código  | nombre       | suscriptor |
      | AGT-123 | Juan Pérez   | SUB-001    |
      | AGT-124 | María López  | SUB-001    |
      | AGT-201 | Carlos Ruiz  | SUB-002    |
    Cuando se solicita el listado completo de agentes disponibles
    Entonces la respuesta es exitosa
    Y el listado contiene 3 agentes
    Y cada agente tiene código, nombre y suscriptor con valores no vacíos

  @happy-path @edge-case
  Escenario: Consultar catálogo cuando no hay agentes configurados
    Dado que el servicio de catálogos está disponible
    Y el catálogo de agentes no contiene ningún agente
    Cuando se solicita el listado completo de agentes disponibles
    Entonces la respuesta es exitosa
    Y el listado de agentes está vacío

  @smoke @critico @happy-path
  Escenario: Verificar que el agente pertenece al suscriptor correcto
    Dado que el catálogo de agentes contiene al agente "AGT-123" del suscriptor "SUB-001"
    Cuando se solicita el listado completo de agentes disponibles
    Entonces el agente "AGT-123" aparece en el listado
    Y su suscriptor asociado es "SUB-001"

  @edge-case
  Escenario: Catálogo contiene agentes de múltiples suscriptores
    Dado que el catálogo de agentes contiene agentes de los suscriptores "SUB-001" y "SUB-002"
    Cuando se solicita el listado completo de agentes disponibles
    Entonces el listado incluye agentes de ambos suscriptores

  # ─────────────────────────────────────────────
  # HU-02: Validar existencia de agente por código
  # ─────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Validar un código de agente existente
    Dado que el catálogo de agentes contiene al agente con código "AGT-123"
    Cuando el sistema verifica si el código "AGT-123" corresponde a un agente válido
    Entonces el sistema confirma que el agente existe

  @error-path @critico
  Escenario: Rechazar un código de agente inexistente
    Dado que el catálogo de agentes no contiene ningún agente con código "AGT-999"
    Cuando el sistema verifica si el código "AGT-999" corresponde a un agente válido
    Entonces el sistema indica que el agente no existe

  @error-path
  Escenario: Rechazar validación cuando el catálogo está vacío
    Dado que el catálogo de agentes no contiene ningún agente
    Cuando el sistema verifica si el código "AGT-123" corresponde a un agente válido
    Entonces el sistema indica que el agente no existe

  @edge-case
  Escenario: Validación es sensible a mayúsculas y minúsculas
    Dado que el catálogo de agentes contiene al agente con código "AGT-123"
    Cuando el sistema verifica si el código "agt-123" corresponde a un agente válido
    Entonces el sistema indica que el agente no existe

  @edge-case
  Escenario: Validación es sensible a espacios en el código
    Dado que el catálogo de agentes contiene al agente con código "AGT-123"
    Cuando el sistema verifica si el código " AGT-123" corresponde a un agente válido
    Entonces el sistema indica que el agente no existe

  # ─────────────────────────────────────────────
  # RN-3: Fail-fast al arrancar
  # ─────────────────────────────────────────────

  @error-path @critico
  Escenario: El servicio no arranca si el archivo de catálogo no existe
    Dado que el archivo de configuración del catálogo de agentes no está disponible
    Cuando el servicio de catálogos intenta inicializarse
    Entonces el servicio falla al arrancar con un mensaje de error descriptivo
    Y no se expone ningún endpoint con datos inconsistentes

  @error-path
  Escenario: El servicio no arranca si el archivo de catálogo tiene formato inválido
    Dado que el archivo de configuración del catálogo de agentes contiene datos con formato incorrecto
    Cuando el servicio de catálogos intenta inicializarse
    Entonces el servicio falla al arrancar con un mensaje de error descriptivo
```

---

## Datos de Prueba

| Escenario | Campo | Valor válido | Valor inválido | Caso borde |
|-----------|-------|-------------|----------------|------------|
| Consultar catálogo | — | catálogo con 3 agentes | — | catálogo vacío `[]` |
| Validar agente existente | `code` | `"AGT-123"` | `"AGT-999"` | `"agt-123"` (minúsculas) |
| Validar agente existente | `code` | `"AGT-123"` | `"AGT-999"` | `" AGT-123"` (espacio inicial) |
| Arranque del servicio | fixture | `agents.json` válido | archivo ausente | JSON malformado |
| Relación con suscriptor | `subscriberId` | `"SUB-001"` | — | agentes de dos suscriptores distintos |

---

## Flujos priorizados para automatización (Serenity BDD)

| Prioridad | Escenario | Tag | Justificación |
|-----------|-----------|-----|---------------|
| 1 | Consultar catálogo con datos configurados | `@smoke @critico` | Happy path principal — base para otros flujos |
| 2 | Validar código de agente existente | `@smoke @critico` | Impacto directo en creación de folios |
| 3 | Rechazar código de agente inexistente | `@error-path @critico` | Previene folios con agentes inválidos |
| 4 | Servicio no arranca sin archivo de catálogo | `@error-path @critico` | Fail-fast — afecta disponibilidad total del servicio |
| 5 | Validación sensible a mayúsculas | `@edge-case` | Comportamiento no obvio según RN-7 |
