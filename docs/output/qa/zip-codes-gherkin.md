# Escenarios Gherkin — Códigos Postales (SPEC-005)

**Feature:** zip-codes  
**Microservicio:** Insurance-Quoter-Core · `GET /v1/zip-codes/{zipCode}` · `POST /v1/zip-codes/validate`  
**Fecha:** 2026-04-21

---

```gherkin
#language: es
Característica: Consulta y validación de códigos postales con zonas catastróficas

  Como sistema de cotización de seguros
  Quiero consultar y validar códigos postales
  Para obtener las zonas necesarias para el cálculo de prima y verificar la existencia del CP

  Antecedentes:
    Dado que el microservicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y la base de datos contiene el código postal "06600" en "Ciudad de México" con zona catastrófica "ZONE_A"

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-01: CONSULTA DE CÓDIGO POSTAL — HAPPY PATH
  # ─────────────────────────────────────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Consulta exitosa de un código postal existente con todos sus datos
    Dado que el código postal "06600" existe en la base de datos
    Cuando el sistema cliente solicita los datos del código postal "06600"
    Entonces la respuesta tiene estado HTTP 200
    Y la respuesta contiene el código postal "06600"
    Y contiene el estado "Ciudad de México"
    Y contiene el municipio "Cuauhtémoc"
    Y contiene la ciudad "Ciudad de México"
    Y contiene al menos una colonia en el listado de colonias
    Y contiene la zona catastrófica "ZONE_A"
    Y contiene la zona TEV "TEV-1"
    Y contiene la zona FHM "FHM-2"
    Y el campo "valid" es verdadero

  @smoke @critico @happy-path
  Escenario: El catálogo retorna las colonias asociadas al código postal
    Dado que el código postal "06600" tiene las colonias "Juárez" y "Tabacalera" registradas
    Cuando el sistema cliente solicita los datos del código postal "06600"
    Entonces la respuesta contiene la colonia "Juárez" en el listado de colonias
    Y contiene la colonia "Tabacalera" en el listado de colonias

  @smoke @critico @happy-path
  Escenario: Consulta de código postal de otra ciudad retorna su zona correcta
    Dado que el código postal "64000" existe en la base de datos con zona catastrófica "ZONE_C"
    Cuando el sistema cliente solicita los datos del código postal "64000"
    Entonces la respuesta tiene estado HTTP 200
    Y contiene el estado "Nuevo León"
    Y contiene la zona catastrófica "ZONE_C"
    Y contiene la zona TEV "TEV-3"

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-01: CONSULTA DE CÓDIGO POSTAL — ERROR PATH
  # ─────────────────────────────────────────────────────────────────────────────

  @error-path
  Escenario: Consulta de código postal inexistente retorna error 404
    Dado que el código postal "99999" no existe en la base de datos
    Cuando el sistema cliente solicita los datos del código postal "99999"
    Entonces la respuesta tiene estado HTTP 404
    Y la respuesta contiene el mensaje de error "Zip code not found"
    Y contiene el código de error "ZIP_CODE_NOT_FOUND"

  @error-path
  Escenario: Consulta con código postal de formato inválido retorna error 400
    Dado que "ABC" no es un código postal válido
    Cuando el sistema cliente solicita los datos del código postal "ABC"
    Entonces la respuesta tiene estado HTTP 400
    Y la respuesta contiene un mensaje de error descriptivo

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-01: CONSULTA DE CÓDIGO POSTAL — EDGE CASES
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Escenario: Código postal existente sin colonias retorna lista vacía
    Dado que el código postal "20000" existe en la base de datos pero no tiene colonias registradas
    Cuando el sistema cliente solicita los datos del código postal "20000"
    Entonces la respuesta tiene estado HTTP 200
    Y el listado de colonias está vacío
    Y todos los campos de zona contienen valores no vacíos

  @edge-case
  Escenario: La respuesta siempre incluye el campo "valid" en true para CP existente
    Dado que el código postal "44100" existe en la base de datos
    Cuando el sistema cliente solicita los datos del código postal "44100"
    Entonces la respuesta contiene el campo "valid" con valor verdadero

  @edge-case
  Esquema del escenario: Consulta de códigos postales de distintas zonas catastróficas
    Dado que el código postal "<cp>" existe con zona catastrófica "<zona>"
    Cuando el sistema cliente consulta ese código postal
    Entonces la respuesta tiene estado HTTP 200
    Y contiene la zona catastrófica "<zona>"

    Ejemplos:
      | cp    | zona   |
      | 06600 | ZONE_A |
      | 44100 | ZONE_B |
      | 64000 | ZONE_C |
      | 20000 | ZONE_D |

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-02: VALIDACIÓN DE CÓDIGO POSTAL — HAPPY PATH
  # ─────────────────────────────────────────────────────────────────────────────

  @smoke @critico @happy-path
  Escenario: Validación exitosa de código postal existente
    Dado que el código postal "06600" existe en la base de datos
    Cuando el sistema cliente valida el código postal "06600"
    Entonces la respuesta tiene estado HTTP 200
    Y la respuesta contiene "valid" con valor verdadero
    Y contiene el código postal "06600"

  @smoke @critico @happy-path
  Escenario: Validación de código postal inexistente retorna valid false con HTTP 200
    Dado que el código postal "99999" no existe en la base de datos
    Cuando el sistema cliente valida el código postal "99999"
    Entonces la respuesta tiene estado HTTP 200
    Y la respuesta contiene "valid" con valor falso
    Y contiene el código postal "99999"

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-02: VALIDACIÓN DE CÓDIGO POSTAL — ERROR PATH
  # ─────────────────────────────────────────────────────────────────────────────

  @error-path
  Escenario: Validación sin código postal en el body retorna error 400
    Dado que el sistema cliente envía una solicitud de validación sin el campo código postal
    Cuando se intenta validar el código postal
    Entonces la respuesta tiene estado HTTP 400

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-02: VALIDACIÓN DE CÓDIGO POSTAL — EDGE CASES
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Escenario: La validación siempre retorna HTTP 200 independientemente de si el CP existe
    Dado que el sistema cliente tiene un código postal para validar
    Cuando valida un código postal existente "06600"
    Entonces la respuesta tiene estado HTTP 200
    Cuando valida un código postal inexistente "99999"
    Entonces la respuesta tiene estado HTTP 200

  @edge-case
  Esquema del escenario: Validación de múltiples códigos postales
    Dado que el código postal "<cp>" "<existe>" en la base de datos
    Cuando el sistema cliente valida el código postal "<cp>"
    Entonces la respuesta contiene "valid" con valor "<resultado>"

    Ejemplos:
      | cp    | existe       | resultado |
      | 06600 | existe       | true      |
      | 44100 | existe       | true      |
      | 11111 | no existe    | false     |
      | 00000 | no existe    | false     |
```

---

## Datos de Prueba Sintéticos

| Escenario                          | Campo              | Válido                   | Inválido  | Borde               |
|------------------------------------|--------------------|--------------------------|-----------|---------------------|
| Consulta CP existente              | `zipCode` (path)   | `06600`, `44100`, `64000`| `99999`   | `00000`             |
| Consulta CP — formato              | `zipCode` (path)   | `06600`                  | `ABC`, `` | `0` (1 dígito)      |
| CP sin colonias                    | `neighborhoods`    | `[]`                     | —         | —                   |
| Zona catastrófica                  | `catastrophicZone` | `ZONE_A`..`ZONE_D`       | —         | —                   |
| Validación — body request          | `zipCode` (body)   | `{"zipCode":"06600"}`    | `{}`      | `{"zipCode":""}`    |
| Validación — CP existente          | `valid`            | `true`                   | —         | —                   |
| Validación — CP inexistente        | `valid`            | `false`                  | —         | —                   |

---

## Flujos Críticos Identificados

| Flujo | Tipo | Tags | Prioridad |
|-------|------|------|-----------|
| GET CP existente con todas las zonas | Happy path | `@smoke @critico` | Alta — zonas impactan prima |
| GET CP con colonias correctas | Happy path | `@smoke @critico` | Alta — N+1 risk |
| GET CP inexistente → 404 con código de error | Error path | `@error-path` | Alta — contrato con Back |
| POST validate CP existente → 200 valid:true | Happy path | `@smoke @critico` | Alta |
| POST validate CP inexistente → 200 valid:false | Happy path | `@smoke @critico` | Alta — RN-5 |
| GET CP sin colonias → array vacío | Edge case | `@edge-case` | Media |
| POST validate sin body → 400 | Error path | `@error-path` | Media |
| Distintas zonas catastróficas | Edge case | `@edge-case` | Media — cobertura de zonas |
