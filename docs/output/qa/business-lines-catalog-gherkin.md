# Escenarios Gherkin — Catálogo de Giros de Negocio (SPEC-004)

**Feature:** business-lines-catalog  
**Microservicio:** Insurance-Quoter-Core · `GET /v1/business-lines`  
**Fecha:** 2026-04-21

---

```gherkin
#language: es
Característica: Catálogo de giros de negocio con clave incendio

  Como sistema cliente (cotizador de daños)
  Quiero consultar el catálogo de giros de negocio
  Para presentar opciones al usuario y obtener la clave incendio para el cálculo de prima

  Antecedentes:
    Dado que el microservicio Insurance-Quoter-Core está disponible en el puerto 8081

  # ─────────────────────────────────────────────────────────────────────────────
  # HAPPY PATH
  # ─────────────────────────────────────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Consulta exitosa del catálogo de giros de negocio
    Dado que el catálogo contiene giros de negocio configurados
    Cuando el sistema cliente solicita la lista de giros de negocio
    Entonces la respuesta tiene estado HTTP 200
    Y la respuesta contiene un array "businessLines" con al menos un elemento
    Y cada elemento del array tiene los campos "code", "description" y "fireKey" con valores no vacíos

  @smoke @critico @happy-path
  Escenario: El catálogo retorna la clave incendio correcta para cada giro
    Dado que el catálogo contiene el giro "Bodega de mercancías" con clave incendio "FK-INC-01"
    Cuando el sistema cliente consulta el catálogo de giros de negocio
    Entonces la respuesta incluye el giro con código "BL-001"
    Y ese giro tiene la clave incendio "FK-INC-01"
    Y tiene la descripción "Bodega de mercancías"

  @smoke @critico @happy-path
  Escenario: El catálogo retorna todos los giros de negocio disponibles
    Dado que el catálogo tiene 5 giros de negocio configurados
    Cuando el sistema cliente consulta el catálogo de giros de negocio
    Entonces la respuesta contiene exactamente 5 giros de negocio
    Y los códigos retornados incluyen "BL-001", "BL-002", "BL-003", "BL-004" y "BL-005"

  # ─────────────────────────────────────────────────────────────────────────────
  # EDGE CASES
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Escenario: El catálogo responde con lista vacía cuando no hay giros configurados
    Dado que el catálogo de giros de negocio está vacío
    Cuando el sistema cliente consulta el catálogo de giros de negocio
    Entonces la respuesta tiene estado HTTP 200
    Y la respuesta contiene un array "businessLines" vacío

  @edge-case
  Escenario: La estructura del response siempre incluye el campo "businessLines" aunque esté vacío
    Dado que el catálogo de giros de negocio está vacío
    Cuando el sistema cliente solicita la lista de giros de negocio
    Entonces el campo "businessLines" está presente en el response
    Y su valor es un array (no nulo)

  @edge-case
  Escenario: El catálogo es inmutable durante la ejecución
    Dado que el catálogo de giros de negocio fue cargado al iniciar el servicio
    Cuando el sistema cliente consulta el catálogo múltiples veces seguidas
    Entonces todas las respuestas retornan la misma lista de giros de negocio
    Y ninguna consulta modifica el catálogo

  # ─────────────────────────────────────────────────────────────────────────────
  # ERROR PATH — Configuración
  # ─────────────────────────────────────────────────────────────────────────────

  @error-path
  Escenario: El servicio falla al arrancar si el archivo de catálogo no existe
    Dado que el archivo de configuración del catálogo no está disponible en el classpath
    Cuando el servicio Insurance-Quoter-Core intenta iniciar
    Entonces el servicio NO arranca
    Y se registra un mensaje de error claro indicando que el catálogo no pudo cargarse
    Y NO se expone el endpoint con datos inconsistentes

  @error-path
  Escenario: El servicio falla al arrancar si el archivo de catálogo tiene formato inválido
    Dado que el archivo de configuración del catálogo contiene JSON malformado
    Cuando el servicio Insurance-Quoter-Core intenta iniciar
    Entonces el servicio NO arranca
    Y se registra un error de parseo con el detalle del problema

  # ─────────────────────────────────────────────────────────────────────────────
  # VALIDACIÓN DE DATOS
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Esquema del escenario: El catálogo valida que los campos obligatorios no estén vacíos
    Dado que el catálogo contiene un giro con <campo> vacío
    Cuando el servicio Insurance-Quoter-Core intenta iniciar
    Entonces el comportamiento es "<resultado>"

    Ejemplos:
      | campo         | resultado                                         |
      | code          | el servicio arranca pero el giro es inválido      |
      | description   | el servicio arranca pero el giro es inválido      |
      | fireKey       | el servicio arranca pero la prima no puede calcularse |
```

---

## Datos de Prueba Sintéticos

| Escenario                              | Campo         | Válido           | Inválido | Borde          |
|----------------------------------------|---------------|------------------|----------|----------------|
| Consulta exitosa                       | `code`        | `BL-001`         | `""`     | `BL-999`       |
| Consulta exitosa                       | `description` | `Bodega de mercancías` | `""` | (string largo) |
| Consulta exitosa                       | `fireKey`     | `FK-INC-01`      | `""`     | `FK-INC-999`   |
| Catálogo vacío                         | `businessLines` | `[]`           | —        | —              |
| Archivo no encontrado                  | classpath     | `fixtures/business-lines.json` | `fixtures/missing.json` | — |
| Archivo malformado                     | contenido JSON | `[{...}]`      | `{broken` | `[]`           |

---

## Flujos Críticos Identificados

| Flujo | Tipo | Tags | Prioridad |
|-------|------|------|-----------|
| Retorno del catálogo completo con todos los campos | Happy path | `@smoke @critico` | Alta |
| Verificación de `fireKey` por giro | Happy path | `@smoke @critico` | Alta — impacta cálculo de prima |
| Catálogo vacío retorna 200 con array vacío | Edge case | `@edge-case` | Media |
| Fail-fast si archivo no existe | Error path | `@error-path` | Alta — bloquea arranque |
| Fail-fast si JSON malformado | Error path | `@error-path` | Alta — bloquea arranque |
| Inmutabilidad del catálogo en ejecución | Edge case | `@edge-case` | Baja |
