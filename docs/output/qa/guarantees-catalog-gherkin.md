# Escenarios Gherkin — Catálogo de Garantías (SPEC-007)

**Proyecto:** Insurance-Quoter-Core · `plataforma-core-ohs`
**Feature:** guarantees-catalog
**Fecha:** 2026-04-21
**Generado por:** gherkin-case-generator

---

## Archivo de feature

```gherkin
#language: es
Característica: Catálogo de garantías disponibles para cotización

  Como sistema cliente (cotizador de daños)
  Quiero consultar el catálogo de garantías disponibles
  Para presentar opciones de cobertura al asegurado
  Y determinar qué garantías participan en el cálculo de prima neta

  Antecedentes:
    Dado que el servicio de catálogos está disponible
    Y el catálogo de garantías contiene las entradas configuradas

  # ─────────────────────────────────────────────────────────────────────────────
  # HAPPY PATHS
  # ─────────────────────────────────────────────────────────────────────────────

  @happy-path @critico @smoke
  Escenario: Consultar el catálogo completo de garantías
    Dado que el catálogo tiene las garantías "Incendio edificios", "Robo" y "Vidrios" configuradas
    Cuando el cotizador solicita el catálogo de garantías
    Entonces recibe una respuesta exitosa
    Y la respuesta contiene 3 garantías
    Y cada garantía tiene un código, una descripción y un indicador de tarifabilidad

  @happy-path @critico
  Escenario: Verificar que las garantías tienen indicador de tarifabilidad correcto
    Dado que el catálogo contiene garantías configuradas como tarifables
    Cuando el cotizador solicita el catálogo de garantías
    Entonces todas las garantías con código "GUA-FIRE", "GUA-THEFT" y "GUA-GLASS" aparecen con tarifable en verdadero
    Y ninguna garantía del catálogo tiene tarifable en nulo

  @happy-path
  Escenario: Consultar garantías disponibles para el cálculo de prima
    Dado que el catálogo contiene garantías con indicador de tarifabilidad
    Cuando el módulo de cálculo de prima solicita solo las garantías tarifables
    Entonces recibe únicamente las garantías donde tarifable es verdadero
    Y el conjunto retornado incluye "Incendio edificios", "Robo" y "Vidrios"

  # ─────────────────────────────────────────────────────────────────────────────
  # ERROR PATHS
  # ─────────────────────────────────────────────────────────────────────────────

  @error-path @critico @smoke
  Escenario: El archivo de configuración de garantías no existe al iniciar el servicio
    Dado que el archivo de configuración de garantías no está disponible en el sistema
    Cuando el servicio de catálogos intenta arrancar
    Entonces el servicio rechaza el inicio con un mensaje de error claro
    Y NO se expone el endpoint de garantías con datos inconsistentes
    Y el equipo de operaciones puede identificar la causa del fallo en los logs

  @error-path
  Escenario: El archivo de configuración de garantías está vacío
    Dado que el archivo de configuración existe pero no contiene ninguna garantía
    Cuando el cotizador solicita el catálogo de garantías
    Entonces recibe una respuesta exitosa
    Y la respuesta contiene un catálogo vacío sin garantías

  @error-path
  Escenario: El archivo de configuración de garantías tiene formato inválido
    Dado que el archivo de configuración contiene datos con formato incorrecto
    Cuando el servicio de catálogos intenta arrancar
    Entonces el servicio rechaza el inicio con un mensaje de error descriptivo
    Y NO se expone el endpoint con datos parciales o incorrectos

  # ─────────────────────────────────────────────────────────────────────────────
  # EDGE CASES
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Escenario: El catálogo contiene garantías con y sin indicador de tarifabilidad activado
    Dado que el catálogo incluye garantías con tarifable en verdadero y garantías con tarifable en falso
    Cuando el módulo de cálculo solicita solo las garantías tarifables
    Entonces la respuesta incluye únicamente las garantías con tarifable en verdadero
    Y las garantías con tarifable en falso no aparecen en el resultado del cálculo

  @edge-case
  Escenario: El catálogo contiene exactamente una garantía tarifable
    Dado que el catálogo tiene configurada solo la garantía "Incendio edificios" como tarifable
    Cuando el módulo de cálculo solicita las garantías tarifables
    Entonces recibe una lista con exactamente un elemento
    Y ese elemento corresponde a "Incendio edificios"

  @edge-case @smoke
  Escenario: El catálogo es idempotente — múltiples consultas retornan el mismo resultado
    Dado que el catálogo de garantías está cargado en memoria
    Cuando el cotizador realiza tres consultas consecutivas al catálogo
    Entonces las tres respuestas contienen exactamente el mismo conjunto de garantías
    Y el número de garantías no varía entre consultas

  # ─────────────────────────────────────────────────────────────────────────────
  # ESQUEMAS (validación de estructura de respuesta)
  # ─────────────────────────────────────────────────────────────────────────────

  @edge-case
  Esquema del escenario: Verificar campos presentes en cada garantía del catálogo
    Dado que el catálogo contiene la garantía con código "<codigo>"
    Cuando el cotizador consulta el catálogo completo
    Entonces la garantía "<codigo>" tiene una descripción "<descripcion>" no vacía
    Y la garantía "<codigo>" tiene el indicador tarifable con valor "<tarifable>"

    Ejemplos:
      | codigo    | descripcion         | tarifable |
      | GUA-FIRE  | Incendio edificios  | true      |
      | GUA-THEFT | Robo                | true      |
      | GUA-GLASS | Vidrios             | true      |
```

---

## Datos de prueba

| Escenario | Campo | Valor válido | Valor inválido | Valor borde |
|-----------|-------|-------------|----------------|-------------|
| Catálogo completo | `code` | `GUA-FIRE` | `null` | `""` (vacío) |
| Catálogo completo | `description` | `"Incendio edificios"` | `null` | `""` (vacío) |
| Catálogo completo | `tarifable` | `true` | `null` | `false` |
| findAllTarifable | Mezcla tarifable | `[{tarifable:true}, {tarifable:false}]` | — | `[{tarifable:false}]` (ninguno tarifable) |
| Arranque | Fixture | JSON válido con array | JSON malformado `{broken` | Array vacío `[]` |
| Idempotencia | Número de respuestas | 3 respuestas iguales | — | 1 consulta |

---

## Mapa de cobertura — Criterios de Aceptación vs Escenarios

| Criterio spec | Escenario Gherkin | Tags |
|---------------|-------------------|------|
| CRITERIO-1.1: Retorno catálogo completo | Consultar el catálogo completo de garantías | `@happy-path @critico @smoke` |
| CRITERIO-1.1: Estructura de cada elemento | Verificar campos presentes en cada garantía | `@edge-case` |
| CRITERIO-1.2: Catálogo vacío | El archivo de configuración está vacío | `@error-path` |
| CRITERIO-1.3: Archivo no encontrado | El archivo de configuración no existe al iniciar | `@error-path @critico @smoke` |
| CRITERIO-1.4: findAllTarifable correcto | El catálogo contiene garantías con y sin tarifable | `@edge-case` |
| RN-2: Carga eager, idempotente | El catálogo es idempotente | `@edge-case @smoke` |
| RN-3: Fail-fast al arrancar | Archivo con formato inválido | `@error-path` |
| RN-5: Solo tarifables para prima | Consultar garantías para el cálculo de prima | `@happy-path` |

---

## Orden de ejecución sugerido para regresión

```
@smoke        → validación rápida en cada PR (5 escenarios)
@critico      → obligatorio antes de merge a develop (3 escenarios)
@happy-path   → suite completa happy paths (3 escenarios)
@error-path   → suite de errores y fallos (3 escenarios)
@edge-case    → casos borde y esquemas (4 escenarios)
```

**Comando de ejecución** (Serenity BDD / Screenplay):
```bash
# Solo smoke
mvn verify -Dcucumber.filter.tags="@smoke"

# Suite completa del feature
mvn verify -Dcucumber.filter.tags="@guarantees-catalog"
```
