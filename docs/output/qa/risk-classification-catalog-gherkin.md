# Escenarios Gherkin — Catálogo de Clasificaciones de Riesgo (SPEC-006)

**Fecha:** 2026-04-21
**Feature:** `risk-classification-catalog`
**Criterios de origen:** CRITERIO-1.1, CRITERIO-1.2, CRITERIO-1.3 · Reglas RN-1 a RN-6
**Riesgos cubiertos:** R-001, R-002, R-003

---

## Archivo de feature

```gherkin
#language: es
Característica: Catálogo de clasificaciones de riesgo

  Como sistema cliente (Insurance-Quoter-Back)
  Quiero consultar el catálogo de clasificaciones de riesgo disponibles
  Para validar la clasificación seleccionada por el suscriptor en el flujo de cotización

  Antecedentes:
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y el archivo de configuración contiene los códigos canónicos STANDARD, PREFERRED y SUBSTANDARD

  # ─────────────────────────────────────────────────
  # HAPPY PATH — CRITERIO-1.1
  # ─────────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Consultar el catálogo completo de clasificaciones de riesgo
    Dado que el catálogo contiene 3 clasificaciones configuradas
    Cuando el sistema cliente solicita GET /v1/catalogs/risk-classification
    Entonces la respuesta tiene código HTTP 200
    Y el cuerpo contiene el campo "riskClassifications" con una lista de 3 elementos
    Y cada elemento contiene los campos "code" y "description" con valores no vacíos
    Y el catálogo incluye la clasificación con código "STANDARD" y descripción "Riesgo estándar"
    Y el catálogo incluye la clasificación con código "PREFERRED" y descripción "Riesgo preferente"
    Y el catálogo incluye la clasificación con código "SUBSTANDARD" y descripción "Riesgo subestándar"

  @smoke @critico @happy-path
  Escenario: El catálogo siempre devuelve los tres códigos canónicos en el orden definido
    Dado que el catálogo está cargado en memoria al inicio de la aplicación
    Cuando el sistema cliente solicita el catálogo por primera vez
    Entonces los códigos aparecen en el orden: STANDARD, PREFERRED, SUBSTANDARD
    Y una segunda solicitud devuelve exactamente el mismo resultado
    Y una tercera solicitud devuelve exactamente el mismo resultado

  # ─────────────────────────────────────────────────
  # SOLO LECTURA — RN-1
  # ─────────────────────────────────────────────────

  @smoke @critico
  Escenario: El catálogo es de solo lectura — no existe endpoint de escritura
    Dado que el servicio Core está disponible
    Cuando el sistema cliente intenta POST /v1/catalogs/risk-classification con cualquier cuerpo
    Entonces la respuesta tiene código HTTP 404 o 405
    Y el catálogo no ha sido modificado

  # ─────────────────────────────────────────────────
  # EDGE CASE — CRITERIO-1.2 · catálogo vacío
  # ─────────────────────────────────────────────────

  @edge-case
  Escenario: El catálogo devuelve una lista vacía cuando el fixture no tiene entradas
    Dado que el archivo de configuración existe pero contiene un array vacío []
    Cuando el sistema cliente solicita GET /v1/catalogs/risk-classification
    Entonces la respuesta tiene código HTTP 200
    Y el cuerpo contiene "riskClassifications" con un array vacío []

  @edge-case
  Escenario: Solicitudes concurrentes al catálogo devuelven resultados idénticos
    Dado que el catálogo está cargado en memoria
    Cuando 10 sistemas cliente solicitan el catálogo de forma simultánea
    Entonces todas las respuestas tienen código HTTP 200
    Y todas las respuestas contienen exactamente los mismos 3 elementos
    Y ninguna respuesta tiene datos parciales ni inconsistentes

  # ─────────────────────────────────────────────────
  # ERROR PATH — CRITERIO-1.3 · fail-fast al arrancar
  # ─────────────────────────────────────────────────

  @error-path @critico
  Escenario: La aplicación no arranca si el archivo de fixture no existe
    Dado que el archivo fixtures/risk-classifications.json ha sido eliminado del classpath
    Cuando la aplicación intenta inicializarse
    Entonces la aplicación falla al arrancar con un mensaje que indica "Failed to load risk classifications"
    Y el endpoint GET /v1/catalogs/risk-classification no está disponible
    Y no se expone ningún dato inconsistente

  @error-path
  Escenario: La aplicación no arranca si el archivo de fixture contiene JSON inválido
    Dado que el archivo fixtures/risk-classifications.json contiene texto no parseável como JSON
    Cuando la aplicación intenta inicializarse
    Entonces la aplicación falla al arrancar con un error de parseo
    Y el endpoint GET /v1/catalogs/risk-classification no está disponible

  # ─────────────────────────────────────────────────
  # INTEGRIDAD DE DATOS — R-002
  # ─────────────────────────────────────────────────

  @critico
  Esquema del escenario: Verificar que el código "<codigo>" existe en el catálogo
    Dado que el catálogo está disponible con la configuración estándar
    Cuando el sistema cliente consulta el catálogo
    Entonces la lista contiene un elemento con código "<codigo>" y descripción "<descripcion>"

    Ejemplos:
      | codigo      | descripcion         |
      | STANDARD    | Riesgo estándar     |
      | PREFERRED   | Riesgo preferente   |
      | SUBSTANDARD | Riesgo subestándar  |

  @critico
  Escenario: El catálogo no contiene códigos no reconocidos
    Dado que el catálogo está disponible con la configuración estándar
    Cuando el sistema cliente consulta el catálogo
    Entonces la lista contiene exactamente 3 elementos
    Y ningún elemento tiene un código diferente a STANDARD, PREFERRED o SUBSTANDARD
    Y ningún elemento tiene "code" nulo o vacío
    Y ningún elemento tiene "description" nulo o vacío

  # ─────────────────────────────────────────────────
  # CONTENT-TYPE Y FORMATO DE RESPUESTA
  # ─────────────────────────────────────────────────

  @happy-path
  Escenario: La respuesta tiene el Content-Type correcto
    Cuando el sistema cliente solicita GET /v1/catalogs/risk-classification
    Entonces la respuesta tiene código HTTP 200
    Y el Content-Type de la respuesta es "application/json"
    Y la estructura JSON de la respuesta contiene la clave raíz "riskClassifications"
```

---

## Datos de Prueba

| Escenario | Campo | Valor válido | Valor inválido | Caso borde |
|-----------|-------|-------------|----------------|------------|
| Catálogo completo | `code` | `STANDARD` | `UNKNOWN_CODE` | Cadena vacía `""` |
| Catálogo completo | `description` | `Riesgo estándar` | `null` | Cadena de 1 carácter |
| Catálogo vacío | Array `riskClassifications` | `[]` | JSON malformado | Array con 1000 elementos |
| Fail-fast | Archivo fixture | Presente y válido | Ausente del classpath | Presente pero vacío `[]` |
| Concurrencia | Número de clientes | 1 | — | 10 solicitudes simultáneas |

---

## Cobertura de Criterios

| Criterio de Aceptación | Escenario(s) Gherkin | Estado |
|------------------------|----------------------|--------|
| CRITERIO-1.1 — Retorno del catálogo completo | Escenario: "Consultar el catálogo completo" | ✅ Cubierto |
| CRITERIO-1.1 — Campos `code` y `description` no vacíos | Esquema del escenario: "Verificar que el código existe" | ✅ Cubierto |
| CRITERIO-1.1 — Solo lectura (RN-1) | Escenario: "El catálogo es de solo lectura" | ✅ Cubierto |
| CRITERIO-1.2 — Catálogo vacío devuelve 200 con array vacío | Escenario: "El catálogo devuelve una lista vacía" | ✅ Cubierto |
| CRITERIO-1.3 — Fail-fast si fixture no existe | Escenario: "La aplicación no arranca si el fixture no existe" | ✅ Cubierto |
| CRITERIO-1.3 — Fail-fast si fixture es JSON inválido | Escenario: "La aplicación no arranca si el fixture contiene JSON inválido" | ✅ Cubierto |
| RN-2 — Carga eager al iniciar | Escenario: "El catálogo siempre devuelve los tres códigos canónicos" | ✅ Cubierto |
| RN-6 — Códigos canónicos exactos | Esquema del escenario + "El catálogo no contiene códigos no reconocidos" | ✅ Cubierto |

---

## Prioridad de Automatización

| Prioridad | Escenarios | Framework sugerido |
|-----------|-----------|-------------------|
| **Inmediata** (smoke) | Catálogo completo · Solo lectura · Fail-fast sin fixture | Serenity BDD + REST Assured (`Auto_Api_Screenplay/`) |
| **Sprint siguiente** | Concurrencia · Catálogo vacío | Serenity BDD + REST Assured |
| **Backlog** | JSON inválido · Content-Type | Serenity BDD + REST Assured |
