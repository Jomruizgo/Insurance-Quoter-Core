# language: es
# Escenarios Gherkin — Generación de Números de Folio Secuenciales
#
# Feature:    folio-generator
# Spec:       .claude/specs/folio-generator.spec.md  (SPEC-001)
# Generado:   2026-04-21
# QA Lead:    ASDD / gherkin-case-generator
# Cobertura:  CRITERIO-1.1, CRITERIO-1.2, CRITERIO-1.3, CRITERIO-1.4, R-002, R-003

---

## Flujos críticos identificados

| ID | Flujo | Prioridad | Tag |
|----|-------|-----------|-----|
| FC-01 | Generación exitosa de folio con formato correcto | Alta | `@smoke @critico` |
| FC-02 | Unicidad de folios bajo llamadas concurrentes | Alta | `@smoke @critico @concurrencia` |
| FC-03 | Formato exacto con padding a 5 dígitos (seq=43) | Alta | `@smoke @critico` |
| FC-04 | El año en el folio refleja el año UTC del momento de generación (rollover) | Alta | `@smoke @critico` |
| FC-05 | La secuencia no se reinicia al cambiar de año | Alta | `@smoke @critico @edge-case` |
| FC-06 | Base de datos no disponible — respuesta de error controlada | Media | `@error-path` |
| FC-07 | Secuencia en el límite superior de 5 dígitos (seq=99999) | Media | `@edge-case` |
| FC-08 | Secuencia supera el límite de 5 dígitos (seq=100000) | Media | `@edge-case` |
| FC-09 | Dos llamadas consecutivas producen folios secuenciales | Alta | `@smoke @critico` |
| FC-10 | El timestamp de generación está en UTC | Alta | `@smoke @critico` |

---

```gherkin
#language: es
Característica: Generación de números de folio secuenciales

  Como sistema cliente (cotizador de seguros u otro microservicio)
  Quiero solicitar un número de folio al servicio core
  Para asociarlo a una nueva cotización garantizando unicidad global y orden temporal

  Antecedentes:
    Dado que el servicio de generación de folios está disponible
    Y la secuencia de folios existe en la base de datos del motor

  # ---------------------------------------------------------------------------
  # FC-01: Happy path — generación exitosa con estructura de respuesta completa
  # Cubre: CRITERIO-1.1
  # ---------------------------------------------------------------------------
  @smoke @critico
  Escenario: Generación exitosa de un número de folio
    Dado que el servicio de cotizaciones está disponible
    Cuando se solicita un nuevo número de folio
    Entonces el sistema retorna el folio sin error
    Y el número de folio tiene el formato "FOL-<año>-<secuencia>"
    Y la respuesta incluye la fecha y hora exacta de generación en UTC

  # ---------------------------------------------------------------------------
  # FC-02: Unicidad bajo concurrencia
  # Cubre: CRITERIO-1.2
  # ---------------------------------------------------------------------------
  @smoke @critico @concurrencia
  Escenario: Unicidad garantizada bajo solicitudes simultáneas
    Dado que hay 20 consumidores que solicitan un folio al mismo tiempo
    Cuando todas las solicitudes se procesan simultáneamente
    Entonces cada consumidor recibe un número de folio diferente
    Y no existen números de secuencia duplicados en el conjunto de folios generados
    Y todos los folios pertenecen al año en curso

  # ---------------------------------------------------------------------------
  # FC-03: Formato exacto con padding a 5 dígitos
  # Cubre: CRITERIO-1.3
  # ---------------------------------------------------------------------------
  @smoke @critico
  Escenario: Formato exacto del folio con relleno de ceros a 5 dígitos
    Dado que el siguiente valor de la secuencia global es 43
    Y el año UTC en curso es 2026
    Cuando se solicita un nuevo número de folio
    Entonces el número de folio es exactamente "FOL-2026-00043"
    Y la longitud total del número de folio es de 14 caracteres

  # ---------------------------------------------------------------------------
  # FC-04: Rollover de año — el folio refleja el año UTC correcto
  # Cubre: CRITERIO-1.4 (primera parte)
  # ---------------------------------------------------------------------------
  @smoke @critico
  Escenario: El folio del 31 de diciembre lleva el año saliente
    Dado que el momento de generación es el 31 de diciembre a las 23:59 UTC
    Cuando se solicita un número de folio
    Entonces el número de folio contiene el año saliente
    Y el número de folio incluye la fecha y hora de generación correspondiente

  @smoke @critico
  Escenario: El folio del 1 de enero lleva el año entrante
    Dado que el momento de generación es el 1 de enero a las 00:01 UTC
    Cuando se solicita un número de folio
    Entonces el número de folio contiene el año nuevo
    Y el número de folio incluye la fecha y hora de generación correspondiente

  # ---------------------------------------------------------------------------
  # FC-05: La secuencia no se reinicia al cambiar de año
  # Cubre: CRITERIO-1.4 (segunda parte — regla de negocio RN-02)
  # ---------------------------------------------------------------------------
  @smoke @critico @edge-case
  Escenario: La secuencia global continúa incrementándose tras el cambio de año
    Dado que se generó un folio el 31 de diciembre a las 23:59 UTC con secuencia 500
    Cuando se solicita un nuevo folio el 1 de enero a las 00:01 UTC
    Entonces el nuevo folio contiene el año nuevo
    Y la secuencia del nuevo folio es 501
    Y la secuencia no fue reiniciada a 1

  # ---------------------------------------------------------------------------
  # FC-06: Base de datos no disponible (R-002)
  # Cubre: riesgo R-002 — fallo de conectividad con la base de datos
  # ---------------------------------------------------------------------------
  @error-path
  Escenario: El servicio responde con error controlado cuando la base de datos no está disponible
    Dado que la base de datos del motor no está disponible
    Cuando se solicita un nuevo número de folio
    Entonces el sistema retorna un error de servicio no disponible
    Y el mensaje de error indica que el servicio no puede procesar la solicitud en este momento
    Y no se genera ningún número de folio

  # ---------------------------------------------------------------------------
  # FC-07: Secuencia en el límite superior de 5 dígitos (seq=99999)
  # Cubre: riesgo R-003 — secuencia alcanza límite de padding
  # ---------------------------------------------------------------------------
  @edge-case
  Escenario: El folio con secuencia máxima representable en 5 dígitos tiene el formato correcto
    Dado que el siguiente valor de la secuencia global es 99999
    Y el año UTC en curso es 2026
    Cuando se solicita un nuevo número de folio
    Entonces el número de folio es exactamente "FOL-2026-99999"
    Y la longitud total del número de folio es de 14 caracteres

  # ---------------------------------------------------------------------------
  # FC-08: Secuencia supera el límite de 5 dígitos (seq=100000)
  # Cubre: riesgo R-003 — desbordamiento de padding (overflow visible)
  # ---------------------------------------------------------------------------
  @edge-case
  Escenario: El folio con secuencia mayor a 99999 excede los 5 dígitos de padding
    Dado que el siguiente valor de la secuencia global es 100000
    Y el año UTC en curso es 2026
    Cuando se solicita un nuevo número de folio
    Entonces el número de folio generado es "FOL-2026-100000"
    Y la longitud total del número de folio es de 15 caracteres
    Y el sistema no produce un error — el folio se emite aunque rompa el patrón visual esperado

  # ---------------------------------------------------------------------------
  # FC-09: Dos llamadas consecutivas producen folios secuenciales
  # Cubre: CRITERIO-1.1 (incremento unitario entre llamadas)
  # ---------------------------------------------------------------------------
  @smoke @critico
  Escenario: Dos solicitudes consecutivas producen folios con secuencias consecutivas
    Dado que el servicio de cotizaciones está disponible
    Cuando se solicita un primer número de folio
    Y se solicita un segundo número de folio inmediatamente después
    Entonces la secuencia del segundo folio es exactamente una unidad mayor que la del primero
    Y ambos folios tienen el mismo año en el número de folio

  # ---------------------------------------------------------------------------
  # FC-10: El timestamp está en UTC
  # Cubre: regla de negocio RN-05 — generatedAt con Instant.now() en UTC
  # ---------------------------------------------------------------------------
  @smoke @critico
  Escenario: La fecha y hora de generación del folio está expresada en UTC
    Dado que el servicio de cotizaciones está disponible
    Cuando se solicita un nuevo número de folio
    Entonces la fecha y hora de generación está en formato ISO-8601
    Y la zona horaria del timestamp es UTC (denotada con "Z" al final)
    Y el timestamp corresponde al momento en que se generó el folio

  # ---------------------------------------------------------------------------
  # Escenario esquematizado: múltiples valores de secuencia con padding
  # Cubre: CRITERIO-1.3 — tabla de casos de padding
  # ---------------------------------------------------------------------------
  @smoke @critico
  Esquema del escenario: Validación del padding a 5 dígitos para distintos valores de secuencia
    Dado que el siguiente valor de la secuencia global es <secuencia>
    Y el año UTC en curso es <anio>
    Cuando se solicita un nuevo número de folio
    Entonces el número de folio es exactamente "<folio_esperado>"
    Y la longitud total del número de folio es de <longitud> caracteres

    Ejemplos:
      | secuencia | anio | folio_esperado   | longitud |
      | 1         | 2026 | FOL-2026-00001   | 14       |
      | 43        | 2026 | FOL-2026-00043   | 14       |
      | 999       | 2026 | FOL-2026-00999   | 14       |
      | 10000     | 2026 | FOL-2026-10000   | 14       |
      | 99999     | 2026 | FOL-2026-99999   | 14       |
      | 1         | 2027 | FOL-2027-00001   | 14       |
      | 500       | 2027 | FOL-2027-00500   | 14       |
```

---

## Tabla de datos de prueba sintéticos

### DT-01 — Valores de secuencia para validación de formato y padding

| ID Dato | Valor de secuencia | Año UTC | Folio esperado | Longitud | Caso que cubre |
|---------|-------------------|---------|----------------|----------|----------------|
| DT-01-A | 1 | 2026 | FOL-2026-00001 | 14 | Primer folio del sistema (inicio de secuencia) |
| DT-01-B | 43 | 2026 | FOL-2026-00043 | 14 | CRITERIO-1.3 — valor exacto de la spec |
| DT-01-C | 999 | 2026 | FOL-2026-00999 | 14 | Tres dígitos significativos — padding de 2 ceros |
| DT-01-D | 10000 | 2026 | FOL-2026-10000 | 14 | Cinco dígitos — sin padding |
| DT-01-E | 99999 | 2026 | FOL-2026-99999 | 14 | Límite superior representable en 5 dígitos (R-003) |
| DT-01-F | 100000 | 2026 | FOL-2026-100000 | 15 | Desbordamiento de padding — 6 dígitos (R-003) |
| DT-01-G | 500 | 2027 | FOL-2027-00500 | 14 | Rollover de año — secuencia continúa desde 500 |

### DT-02 — Timestamps de rollover de año para CRITERIO-1.4

| ID Dato | Momento de generación (UTC) | Año esperado en folio | Descripción |
|---------|-----------------------------|-----------------------|-------------|
| DT-02-A | 2026-12-31T23:59:59Z | 2026 | Último segundo del año saliente |
| DT-02-B | 2027-01-01T00:00:01Z | 2027 | Primer segundo del año entrante |
| DT-02-C | 2027-01-01T00:00:00Z | 2027 | Exactamente en el cambio de año UTC |

### DT-03 — Pares de folios consecutivos para validación de secuencialidad (FC-09)

| ID Dato | Secuencia folio N | Secuencia folio N+1 | Año | Diferencia esperada |
|---------|------------------|----------------------|-----|---------------------|
| DT-03-A | 1 | 2 | 2026 | 1 |
| DT-03-B | 42 | 43 | 2026 | 1 |
| DT-03-C | 99998 | 99999 | 2026 | 1 |
| DT-03-D | 99999 | 100000 | 2026 | 1 (desbordamiento de padding) |

### DT-04 — Conjunto de folios concurrentes para validación de unicidad (FC-02)

| ID Dato | Número de hilos concurrentes | Folios duplicados esperados | Año | Referencia de test |
|---------|-----------------------------|-----------------------------|-----|-------------------|
| DT-04-A | 20 | 0 | 2026 | FolioSequenceConcurrencyTest (20 threads) |
| DT-04-B | 50 | 0 | 2026 | Escenario de carga mayor — ejecución manual |
| DT-04-C | 100 | 0 | 2026 | Escenario de estrés — ejecución bajo demanda |

### DT-05 — Escenarios de error para FC-06 (R-002)

| ID Dato | Condición de falla | Respuesta esperada | Código HTTP | Folio generado |
|---------|--------------------|--------------------|-------------|----------------|
| DT-05-A | Base de datos detenida (contenedor PostgreSQL parado) | Error de servicio no disponible | 503 | Ninguno |
| DT-05-B | Conexión a DB con timeout | Error de servicio no disponible | 503 | Ninguno |
| DT-05-C | Secuencia `folio_sequence` eliminada de la DB | Error de servicio no disponible | 503 | Ninguno |

---

## Trazabilidad Escenario → Criterio de Aceptación

| Escenario | CRITERIO-1.1 | CRITERIO-1.2 | CRITERIO-1.3 | CRITERIO-1.4 | R-002 | R-003 | RN |
|-----------|:------------:|:------------:|:------------:|:------------:|:-----:|:-----:|:--:|
| FC-01 | X | | | | | | RN-01, RN-05 |
| FC-02 | | X | | | | | RN-03 |
| FC-03 | | | X | | | | RN-01 |
| FC-04 | | | | X | | | RN-02 |
| FC-05 | | | | X | | | RN-02 |
| FC-06 | | | | | X | | — |
| FC-07 | | | | | | X | RN-01 |
| FC-08 | | | | | | X | RN-01 |
| FC-09 | X | | | | | | RN-02 |
| FC-10 | X | | | | | | RN-05 |
| Esquema | | | X | | | X | RN-01 |

**Reglas de negocio referenciadas:**
- RN-01: Patrón `FOL-<YYYY>-<NNNNN>` con padding 5 dígitos
- RN-02: Secuencia global sin reinicio por año ni criterio adicional
- RN-03: Unicidad garantizada por `nextval('folio_sequence')` en PostgreSQL
- RN-05: `generatedAt` generado con `Instant.now()` en UTC
