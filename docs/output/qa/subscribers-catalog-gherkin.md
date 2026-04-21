# Escenarios Gherkin — Catálogo de Suscriptores

**Feature:** subscribers-catalog | **Spec:** SPEC-002 | **Fecha:** 2026-04-21
**Generado por:** gherkin-case-generator — ASDD

---

```gherkin
#language: es
Característica: Catálogo de suscriptores disponibles
  Como sistema cliente (cotizador de daños)
  Quiero consultar el catálogo de suscriptores disponibles
  Para presentar las opciones al usuario durante el flujo de cotización

  # ---------------------------------------------------------------------------
  # HAPPY PATHS
  # ---------------------------------------------------------------------------

  @smoke @critico @happy-path
  Escenario: Consultar catálogo con suscriptores configurados
    Dado que el servicio de catálogos está disponible
    Y el catálogo de suscriptores tiene al menos un suscriptor configurado
    Cuando el sistema solicita el listado de suscriptores disponibles
    Entonces la respuesta es exitosa
    Y la respuesta contiene una lista de suscriptores
    Y cada suscriptor tiene un identificador único y un nombre legible

  @smoke @critico @happy-path
  Escenario: Los suscriptores del catálogo contienen los datos esperados
    Dado que el catálogo de suscriptores está configurado con los suscriptores del negocio
    Cuando el sistema solicita el listado de suscriptores disponibles
    Entonces el catálogo contiene el suscriptor "Seguros Sofka" con identificador "SUB-001"
    Y el catálogo contiene el suscriptor "Aseguradora Norte" con identificador "SUB-002"

  @happy-path
  Escenario: Consultas consecutivas retornan el mismo catálogo
    Dado que el catálogo de suscriptores está disponible
    Cuando el sistema solicita el listado de suscriptores en dos ocasiones consecutivas
    Entonces ambas respuestas contienen exactamente los mismos suscriptores
    Y el orden de los suscriptores es el mismo en ambas respuestas

  # ---------------------------------------------------------------------------
  # EDGE CASES
  # ---------------------------------------------------------------------------

  @edge-case
  Escenario: Catálogo configurado sin suscriptores
    Dado que el archivo de configuración de suscriptores existe
    Pero el catálogo está configurado sin ningún suscriptor
    Cuando el sistema solicita el listado de suscriptores disponibles
    Entonces la respuesta es exitosa
    Y la lista de suscriptores está vacía

  @edge-case
  Escenario: Catálogo con un único suscriptor
    Dado que el catálogo de suscriptores está configurado con exactamente un suscriptor
    Cuando el sistema solicita el listado de suscriptores disponibles
    Entonces la respuesta contiene exactamente un suscriptor
    Y ese suscriptor tiene identificador y nombre válidos

  # ---------------------------------------------------------------------------
  # ERROR PATHS / CONFIGURACIÓN
  # ---------------------------------------------------------------------------

  @error-path @configuracion
  Escenario: Servicio no disponible por fallo de configuración
    Dado que el archivo de configuración del catálogo de suscriptores no existe
    Cuando el servicio intenta iniciar
    Entonces el servicio rechaza el arranque con un error claro
    Y NO expone el catálogo con datos incompletos o inconsistentes

  @error-path @configuracion
  Escenario: Archivo de configuración con formato inválido
    Dado que el archivo de configuración del catálogo contiene datos con formato incorrecto
    Cuando el servicio intenta iniciar
    Entonces el servicio rechaza el arranque indicando el problema de configuración
    Y NO expone un catálogo con suscriptores en estado inconsistente

  # ---------------------------------------------------------------------------
  # SEGURIDAD (por diseño: sin autenticación para servicio interno)
  # ---------------------------------------------------------------------------

  @seguridad
  Escenario: El catálogo es accesible sin credenciales como servicio interno
    Dado que el servicio de catálogos está disponible en la red interna
    Cuando un sistema cliente consulta el catálogo sin credenciales
    Entonces la respuesta es exitosa
    Y se retorna el catálogo completo de suscriptores

  @seguridad
  Escenario: El catálogo es de solo lectura
    Dado que el catálogo de suscriptores está disponible
    Cuando un sistema cliente intenta modificar o eliminar un suscriptor del catálogo
    Entonces la operación no está disponible
    Y el catálogo permanece sin cambios
```

---

## Datos de Prueba Sintéticos

| Escenario | Campo | Válido | Inválido / Borde |
|-----------|-------|--------|------------------|
| Catálogo con datos | `id` | `"SUB-001"`, `"SUB-002"` | `null`, `""` (producido por typo en fixture) |
| Catálogo con datos | `name` | `"Seguros Sofka"`, `"Aseguradora Norte"` | `null`, `""` (producido por typo en fixture) |
| Catálogo vacío | array | `[]` | N/A |
| Único suscriptor | array | `[{ "id": "SUB-TEST", "name": "Suscriptor Test" }]` | N/A |
| Fixture ausente | archivo | `fixtures/subscribers.json` presente | archivo eliminado / path incorrecto |
| Fixture inválido | contenido | `[{"id":"SUB-001","name":"Test"}]` | `"not-valid-json"`, `{}` (objeto en vez de array) |

---

## Trazabilidad Criterios de Aceptación → Escenarios

| Criterio Spec | Escenario Gherkin |
|---------------|-------------------|
| CRITERIO-1.1 Happy path catálogo con datos | Consultar catálogo con suscriptores configurados |
| CRITERIO-1.1 Campos id y name no vacíos | Los suscriptores del catálogo contienen los datos esperados |
| CRITERIO-1.2 Catálogo vacío → HTTP 200 con `[]` | Catálogo configurado sin suscriptores |
| CRITERIO-1.3 Fixture ausente → fail-fast al arrancar | Servicio no disponible por fallo de configuración |
| CRITERIO-1.3 Fixture inválido → fail-fast al arrancar | Archivo de configuración con formato inválido |
| RN-1 Solo lectura | El catálogo es de solo lectura |
| RN-2 Carga única al inicio | Consultas consecutivas retornan el mismo catálogo |
| RN-5 Sin auth para servicio interno | El catálogo es accesible sin credenciales |
