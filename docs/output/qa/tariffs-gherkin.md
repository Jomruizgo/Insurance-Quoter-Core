# Escenarios Gherkin — Tarifas y Factores Técnicos (SPEC-008)

**Proyecto:** Insurance-Quoter-Core · `plataforma-core-ohs`
**Feature:** tariffs
**Spec:** SPEC-008 v2.0 — APPROVED
**Fecha:** 2026-04-21
**Generado por:** gherkin-case-generator

---

## Archivo de feature

```gherkin
#language: es
Característica: Consulta y actualización de tarifas técnicas

  Como sistema cliente (Insurance-Quoter-Back)
  Quiero consultar y actualizar los factores técnicos de tarificación
  Para calcular la prima neta de incendio, robo, equipos electrónicos,
  CATTEV y CATFHM en cada cotización

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-01 — CONSULTAR TARIFAS VIGENTES
  # ─────────────────────────────────────────────────────────────────────────────

  @happy-path @critico @smoke @criterio-1.1
  Escenario: CRITERIO-1.1 — Consultar tarifas vigentes exitosamente
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y la tabla "tariffs" contiene la fila con id igual a 1 con los valores seed:
      | fireRate | cattevFactor | catfhmFactor | theftRate | electronicEquipmentRate |
      | 0.0015   | 0.0008       | 0.0005       | 0.003     | 0.002                   |
    Cuando se realiza GET /v1/tariffs
    Entonces la respuesta tiene código HTTP 200
    Y el cuerpo contiene un objeto "tariffs"
    Y el objeto "tariffs" contiene los campos "fireRate", "cattevFactor", "catfhmFactor", "theftRate" y "electronicEquipmentRate"
    Y el campo "fireRate" tiene el valor 0.0015
    Y el campo "cattevFactor" tiene el valor 0.0008
    Y el campo "catfhmFactor" tiene el valor 0.0005
    Y el campo "theftRate" tiene el valor 0.003
    Y el campo "electronicEquipmentRate" tiene el valor 0.002

  @error-path @critico @smoke @criterio-1.2
  Escenario: CRITERIO-1.2 — Consultar tarifas cuando la fila id=1 no existe
    Dado que la tabla "tariffs" no contiene ninguna fila
    Cuando se realiza GET /v1/tariffs
    Entonces la respuesta tiene código HTTP 404
    Y el cuerpo contiene el campo "error" con el valor "Tariffs not found"

  @happy-path @critico @criterio-1.3
  Escenario: CRITERIO-1.3 — Verificar que todos los factores son positivos y no nulos
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y la tabla "tariffs" contiene la fila con id igual a 1
    Cuando se realiza GET /v1/tariffs
    Entonces la respuesta tiene código HTTP 200
    Y todos los valores numéricos del objeto "tariffs" son mayores que cero
    Y ningún campo del objeto "tariffs" tiene valor nulo

  # ─────────────────────────────────────────────────────────────────────────────
  # HU-02 — ACTUALIZAR TARIFAS TÉCNICAS
  # ─────────────────────────────────────────────────────────────────────────────

  @happy-path @critico @smoke @criterio-2.1
  Escenario: CRITERIO-2.1 — Actualizar tarifas exitosamente y verificar persistencia
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y la tabla "tariffs" contiene la fila con id igual a 1 con los valores seed
    Cuando se realiza PUT /v1/tariffs con el siguiente cuerpo:
      | fireRate | cattevFactor | catfhmFactor | theftRate | electronicEquipmentRate |
      | 0.0018   | 0.0009       | 0.0006       | 0.0035    | 0.0025                  |
    Entonces la respuesta tiene código HTTP 200
    Y el cuerpo contiene un objeto "tariffs" con los nuevos valores:
      | fireRate | cattevFactor | catfhmFactor | theftRate | electronicEquipmentRate |
      | 0.0018   | 0.0009       | 0.0006       | 0.0035    | 0.0025                  |
    Y una llamada subsiguiente a GET /v1/tariffs devuelve los mismos valores actualizados

  @error-path @critico @smoke @criterio-2.2
  Esquema del escenario: CRITERIO-2.2 — Rechazar actualización cuando un campo tiene valor cero o negativo
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Y la tabla "tariffs" contiene la fila con id igual a 1
    Cuando se realiza PUT /v1/tariffs con el campo "<campo>" con el valor <valor>
    Entonces la respuesta tiene código HTTP 400
    Y el cuerpo contiene un campo "error" que describe que "<campo>" es inválido

    Ejemplos:
      | campo                   | valor   | descripcion_caso        |
      | fireRate                | 0.0     | cero exacto             |
      | fireRate                | -0.001  | negativo                |
      | cattevFactor            | 0.0     | cero exacto             |
      | catfhmFactor            | -0.0005 | negativo                |
      | theftRate               | 0.0     | cero exacto             |
      | electronicEquipmentRate | -1.0    | negativo grande         |

  @error-path @critico @criterio-2.3
  Escenario: CRITERIO-2.3 — Rechazar actualización cuando la fila id=1 no existe
    Dado que la tabla "tariffs" no contiene ninguna fila
    Cuando se realiza PUT /v1/tariffs con un cuerpo válido:
      | fireRate | cattevFactor | catfhmFactor | theftRate | electronicEquipmentRate |
      | 0.0018   | 0.0009       | 0.0006       | 0.0035    | 0.0025                  |
    Entonces la respuesta tiene código HTTP 404
    Y el cuerpo contiene el campo "error" con el valor "Tariffs not found"

  # ─────────────────────────────────────────────────────────────────────────────
  # EDGE CASES — Reglas de negocio y comportamientos de borde
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case @smoke
  Escenario: La tabla "tariffs" tiene exactamente una fila — no se crean duplicados al actualizar
    Dado que la tabla "tariffs" contiene exactamente una fila con id igual a 1
    Cuando se realiza PUT /v1/tariffs con un cuerpo válido
    Entonces la respuesta tiene código HTTP 200
    Y la tabla "tariffs" sigue conteniendo exactamente una fila con id igual a 1
    Y no se ha insertado ninguna fila nueva

  @edge-case
  Escenario: Actualizar solo con el valor mínimo positivo aceptable (límite inferior)
    Dado que la tabla "tariffs" contiene la fila con id igual a 1
    Cuando se realiza PUT /v1/tariffs con todos los campos en el valor mínimo positivo representable como double:
      | fireRate     | cattevFactor | catfhmFactor | theftRate    | electronicEquipmentRate |
      | 4.9E-324     | 4.9E-324     | 4.9E-324     | 4.9E-324     | 4.9E-324                |
    Entonces la respuesta tiene código HTTP 200
    Y el cuerpo contiene un objeto "tariffs" con los valores actualizados

  @edge-case
  Escenario: Actualizar con valores muy grandes (double máximo) permanece coherente
    Dado que la tabla "tariffs" contiene la fila con id igual a 1
    Cuando se realiza PUT /v1/tariffs con todos los campos en valor 9999.9999
    Entonces la respuesta tiene código HTTP 200
    Y la respuesta GET /v1/tariffs subsiguiente devuelve los mismos valores 9999.9999 en cada campo

  @edge-case @critico
  Escenario: GET es idempotente — múltiples consultas consecutivas retornan el mismo resultado
    Dado que la tabla "tariffs" contiene la fila con id igual a 1 con los valores seed
    Cuando se realizan tres llamadas consecutivas a GET /v1/tariffs sin modificar los datos
    Entonces las tres respuestas tienen código HTTP 200
    Y las tres respuestas retornan exactamente los mismos cinco valores de tarifa

  @edge-case
  Escenario: PUT con cuerpo vacío o sin campos es rechazado
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Cuando se realiza PUT /v1/tariffs con un cuerpo JSON vacío "{}"
    Entonces la respuesta tiene código HTTP 400
    Y el cuerpo de error indica los campos requeridos faltantes

  @edge-case
  Escenario: PUT sin Content-Type application/json es rechazado
    Dado que el servicio Insurance-Quoter-Core está disponible en el puerto 8081
    Cuando se realiza PUT /v1/tariffs sin cabecera Content-Type
    Entonces la respuesta tiene código HTTP 415 o 400

  @edge-case @critico
  Escenario: El flujo completo GET → PUT → GET refleja la actualización sin reiniciar el servicio
    Dado que la tabla "tariffs" contiene la fila con id igual a 1 con los valores seed
    Cuando se realiza GET /v1/tariffs y se registran los valores originales
    Y se realiza PUT /v1/tariffs con todos los valores distintos a los originales:
      | fireRate | cattevFactor | catfhmFactor | theftRate | electronicEquipmentRate |
      | 0.002    | 0.001        | 0.0007       | 0.004     | 0.003                   |
    Y se realiza GET /v1/tariffs nuevamente
    Entonces la segunda respuesta GET tiene los valores del PUT
    Y los valores originales del primer GET ya no están presentes en la segunda consulta

  @edge-case
  Escenario: Concurrencia — dos PUT simultáneos no deben producir estado inconsistente
    Dado que la tabla "tariffs" contiene la fila con id igual a 1 con los valores seed
    Cuando dos clientes envían simultáneamente PUT /v1/tariffs con valores diferentes
    Entonces al menos una de las dos respuestas tiene código HTTP 200
    Y un GET /v1/tariffs posterior devuelve exactamente los cinco campos con valores coherentes y positivos
    Y no existen valores nulos ni cero en ningún campo de la fila id=1
```

---

## Datos de prueba sintéticos

### Conjunto A — Valores seed (estado inicial Flyway)

| Campo | Valor | Tipo | Origen |
|-------|-------|------|--------|
| `fireRate` | `0.0015` | double | Migración V5 |
| `cattevFactor` | `0.0008` | double | Migración V5 |
| `catfhmFactor` | `0.0005` | double | Migración V5 |
| `theftRate` | `0.003` | double | Migración V5 |
| `electronicEquipmentRate` | `0.002` | double | Migración V5 |

### Conjunto B — Valores de actualización válidos (PUT exitoso)

| Campo | Valor | Descripción |
|-------|-------|-------------|
| `fireRate` | `0.0018` | Incremento moderado sobre seed |
| `cattevFactor` | `0.0009` | Incremento moderado sobre seed |
| `catfhmFactor` | `0.0006` | Incremento moderado sobre seed |
| `theftRate` | `0.0035` | Incremento moderado sobre seed |
| `electronicEquipmentRate` | `0.0025` | Incremento moderado sobre seed |

### Conjunto C — Valores inválidos por campo (PUT 400)

| Campo | Valor inválido | Motivo de rechazo |
|-------|---------------|-------------------|
| `fireRate` | `0.0` | Cero — viola @Positive |
| `fireRate` | `-0.001` | Negativo — viola @Positive |
| `cattevFactor` | `0.0` | Cero — viola @Positive |
| `catfhmFactor` | `-0.0005` | Negativo — viola @Positive |
| `theftRate` | `0.0` | Cero — viola @Positive |
| `electronicEquipmentRate` | `-1.0` | Negativo — viola @Positive |

### Conjunto D — Valores límite (edge cases)

| Escenario | Campo | Valor | Descripción |
|-----------|-------|-------|-------------|
| Límite inferior positivo | todos | `4.9E-324` | `Double.MIN_VALUE` — mínimo double positivo en JVM |
| Valor alto coherente | todos | `9999.9999` | Double grande pero representable con precisión |
| Flujo GET→PUT→GET | todos | `0.002 / 0.001 / 0.0007 / 0.004 / 0.003` | Conjunto diferenciable del seed para verificar persistencia |

### Conjunto E — Estructuras de request inválidas

| Escenario | Body enviado | Código esperado |
|-----------|-------------|-----------------|
| Body vacío | `{}` | 400 |
| Sin Content-Type | Body válido pero cabecera omitida | 415 o 400 |
| Body con campo nulo | `{"fireRate": null, ...}` | 400 |
| Fila inexistente (GET) | N/A — tabla truncada | 404 |
| Fila inexistente (PUT) | Body válido — tabla truncada | 404 |

---

## Mapa de cobertura — Criterios de Aceptación vs Escenarios

| Criterio spec | Escenario Gherkin | Tags |
|---------------|-------------------|------|
| CRITERIO-1.1 Retorno de tarifas vigentes | Consultar tarifas vigentes exitosamente | `@happy-path @critico @smoke @criterio-1.1` |
| CRITERIO-1.2 Tabla vacía / fila id=1 ausente (GET) | Consultar tarifas cuando la fila id=1 no existe | `@error-path @critico @smoke @criterio-1.2` |
| CRITERIO-1.3 Todos los factores positivos y no nulos | Verificar que todos los factores son positivos y no nulos | `@happy-path @critico @criterio-1.3` |
| CRITERIO-2.1 Actualización exitosa de tarifas | Actualizar tarifas exitosamente y verificar persistencia | `@happy-path @critico @smoke @criterio-2.1` |
| CRITERIO-2.2 Rechazo de valores inválidos | Rechazar actualización cuando un campo tiene valor cero o negativo (esquema x6) | `@error-path @critico @smoke @criterio-2.2` |
| CRITERIO-2.3 Tabla vacía / fila id=1 ausente (PUT) | Rechazar actualización cuando la fila id=1 no existe | `@error-path @critico @criterio-2.3` |
| RN-1 Exactamente una fila activa | La tabla "tariffs" tiene exactamente una fila — no se crean duplicados | `@edge-case @smoke` |
| RN-3 PUT actualiza, no inserta | La tabla "tariffs" tiene exactamente una fila — no se crean duplicados | `@edge-case @smoke` |
| RN-4 Factores double positivos (límite inferior) | Actualizar solo con el valor mínimo positivo aceptable | `@edge-case` |
| RN-4 Factores double positivos (valores altos) | Actualizar con valores muy grandes permanece coherente | `@edge-case` |
| RN-2 Idempotencia GET | GET es idempotente — múltiples consultas consecutivas | `@edge-case @critico` |
| Flujo completo integración | El flujo completo GET → PUT → GET refleja la actualización | `@edge-case @critico` |
| R-003 Concurrencia (ver risks) | Concurrencia — dos PUT simultáneos no producen estado inconsistente | `@edge-case` |

### Riesgos cubiertos desde tariffs-risks.md

| Riesgo | Escenario que lo cubre |
|--------|----------------------|
| R-001 Propagación de valores incorrectos a Back | CRITERIO-1.1 + flujo GET→PUT→GET |
| R-002 Escritura incorrecta sin historial | CRITERIO-2.1 + flujo GET→PUT→GET |
| R-003 Ausencia de control de concurrencia | Concurrencia — dos PUT simultáneos |
| R-004 Fallo Flyway bloquea cotización | CRITERIO-1.2 (fila ausente) + CRITERIO-2.3 |
| R-008 JPA INSERT en lugar de UPDATE | RN-1 — verifica fila única tras PUT |

---

## Orden de ejecución sugerido para regresión

```
@smoke        → validación rápida en cada PR (5 escenarios + 6 ejemplos del esquema)
@critico      → obligatorio antes de merge a develop (9 escenarios)
@happy-path   → suite completa de happy paths (3 escenarios)
@error-path   → suite de errores y validaciones (3 escenarios + 6 ejemplos)
@edge-case    → casos de borde, concurrencia y límites (7 escenarios)
```

**Comandos de ejecución** (Serenity BDD / Screenplay):

```bash
# Solo smoke — validación rápida por PR
mvn verify -Dcucumber.filter.tags="@smoke"

# Solo criticos — antes de merge a develop
mvn verify -Dcucumber.filter.tags="@critico"

# Suite completa del feature
mvn verify -Dcucumber.filter.tags="@tariffs"

# Por criterio individual
mvn verify -Dcucumber.filter.tags="@criterio-2.2"
```

---

## Notas de implementación para automatización

- **Prerequisito de BD:** Cada escenario que requiere "fila inexistente" debe ejecutar `DELETE FROM tariffs WHERE id = 1` o truncar la tabla como step de `@Antes`. Usar `@Sql` de Spring Test o un step definition con `JdbcTemplate` en el proyecto `Auto_Api_Screenplay`.
- **Prerequisito seed:** Los escenarios del Conjunto A deben restaurar los valores seed en el `@Antes` para garantizar aislamiento entre escenarios. No asumir que el orden de ejecución preserva el estado.
- **Validación de body 400:** El campo `"error"` en el body del 400 contiene el nombre del campo inválido (ej. `"fireRate must be greater than 0"`). El step definition debe extraer el nombre del campo del mensaje para comparar con la tabla de ejemplos.
- **Escenario de concurrencia:** Implementar con `ExecutorService` de 2 hilos en el step definition; no es viable con Cucumber puro sin código Java de soporte. Marcarlo con `@Ignore` hasta que el equipo confirme soporte de optimistic locking (ver Gap G-003 en tariffs-risks.md).
- **Valor `Double.MIN_VALUE`:** En el step definition mapear la cadena `"4.9E-324"` al literal `Double.MIN_VALUE` de Java para evitar pérdida de precisión en la serialización JSON.
