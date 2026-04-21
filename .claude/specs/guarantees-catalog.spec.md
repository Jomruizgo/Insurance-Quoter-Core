---
id: SPEC-007
status: DRAFT
feature: guarantees-catalog
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-004"]
---

# Spec: Catálogo de Garantías

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/catalogs/guarantees` que retorna el catálogo completo de garantías disponibles para cotización, con indicador de tarifabilidad (`tarifable`). Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación del catálogo en tiempo de ejecución.

El output port `GuaranteeRepository` expone además `findAllTarifable()`, que filtra solo las garantías tarifables. Este método es consumido por `Insurance-Quoter-Back` durante el cálculo de prima neta.

### Requerimiento de Negocio

> Catálogo de garantías disponibles en el microservicio core.
> Endpoint `GET /v1/catalogs/guarantees`. Los datos provienen de un archivo
> `src/main/resources/fixtures/guarantees.json` cargado en memoria al inicio.
> No hay escritura. El catálogo es estático y gestionado por configuración.
> El port `GuaranteeRepository` expone `findAll()` para el catálogo completo
> y `findAllTarifable()` para las garantías que participan en el cálculo de prima.

### Historias de Usuario

#### HU-01: Consultar catálogo de garantías

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/catalogs/guarantees y recibir la lista completa de garantías
Para:        presentar las opciones de cobertura al usuario en el flujo de cotización
             e identificar qué garantías participan en el cálculo de prima

Prioridad:   Alta
Estimación:  S
Dependencias: Ninguna (sin base de datos, datos en memoria)
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Retorno del catálogo completo
  Dado que:  el servicio Insurance-Quoter-Core está disponible
             Y el archivo fixtures/guarantees.json contiene al menos una garantía
  Cuando:    se realiza GET /v1/catalogs/guarantees
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "guarantees" con un array
             Y cada elemento del array contiene "code", "description" y "tarifable" con valores no nulos
```

**Catálogo vacío**
```gherkin
CRITERIO-1.2: Catálogo sin entradas configuradas
  Dado que:  el archivo fixtures/guarantees.json existe pero el array está vacío
  Cuando:    se realiza GET /v1/catalogs/guarantees
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "guarantees" con un array vacío []
```

**Error de configuración**
```gherkin
CRITERIO-1.3: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/guarantees.json no existe en el classpath
  Cuando:    la aplicación intenta arrancar
  Entonces:  la aplicación falla al iniciar con un mensaje de error claro
             Y NO se expone el endpoint con datos inconsistentes
```

**Filtrado de garantías tarifables (uso interno)**
```gherkin
CRITERIO-1.4: findAllTarifable retorna solo garantías tarifables
  Dado que:  el catálogo contiene garantías con tarifable=true y tarifable=false
  Cuando:    se invoca GuaranteeRepository.findAllTarifable()
  Entonces:  se retorna únicamente la lista de garantías donde tarifable es true
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | El catálogo es de solo lectura — no existe endpoint de creación, modificación ni eliminación. |
| RN-2 | Los datos se cargan una sola vez al iniciar la aplicación (eager loading en el adapter). |
| RN-3 | Si el archivo JSON no existe o no es parseable, la aplicación debe fallar al arrancar (fail-fast). |
| RN-4 | El `code` de cada garantía es un identificador de negocio opaco (ej. `GUA-FIRE`), no una PK de base de datos. |
| RN-5 | El campo `tarifable` determina si la garantía participa en el cálculo de prima neta. Solo las garantías con `tarifable: true` son consideradas por `Insurance-Quoter-Back`. |
| RN-6 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |
| RN-7 | `findAllTarifable()` es un método del output port destinado al consumo interno (cálculo de prima), no se expone como endpoint REST propio. |

---

## 2. DISEÑO

### Modelo de Dominio

#### `Guarantee` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.guarantee.domain.model.Guarantee
public record Guarantee(String code, String description, boolean tarifable) {}
```

| Campo         | Tipo    | Restricciones                                                                |
|---------------|---------|------------------------------------------------------------------------------|
| `code`        | String  | No nulo, no vacío — identificador de negocio (ej. `GUA-FIRE`)               |
| `description` | String  | No nulo, no vacío — descripción legible (ej. `Incendio edificios`)          |
| `tarifable`   | boolean | `true` si la garantía participa en el cálculo de prima neta                 |

### Output Port

```java
// com.sofka.insurancequoter.core.guarantee.domain.port.out.GuaranteeRepository
public interface GuaranteeRepository {
    List<Guarantee> findAll();
    List<Guarantee> findAllTarifable();
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.guarantee.domain.port.in.GetGuaranteesUseCase
public interface GetGuaranteesUseCase {
    List<Guarantee> getAll();
}
```

### Fixture — `src/main/resources/fixtures/guarantees.json`

```json
[
  { "code": "GUA-FIRE",  "description": "Incendio edificios", "tarifable": true },
  { "code": "GUA-THEFT", "description": "Robo",               "tarifable": true },
  { "code": "GUA-GLASS", "description": "Vidrios",            "tarifable": true }
]
```

> El adapter deserializa este array en `List<Guarantee>` al construirse (vía Jackson `ObjectMapper`).
> `findAllTarifable()` filtra en memoria con `stream().filter(Guarantee::tarifable)`.

### API Endpoint

#### `GET /v1/catalogs/guarantees`

| Atributo  | Valor                                |
|-----------|--------------------------------------|
| Método    | `GET`                                |
| Ruta      | `/v1/catalogs/guarantees`            |
| Auth      | Ninguna (servicio interno)           |
| Produces  | `application/json`                   |

**Response 200 — catálogo con datos:**
```json
{
  "guarantees": [
    { "code": "GUA-FIRE",  "description": "Incendio edificios", "tarifable": true },
    { "code": "GUA-THEFT", "description": "Robo",               "tarifable": true },
    { "code": "GUA-GLASS", "description": "Vidrios",            "tarifable": true }
  ]
}
```

**Response 200 — catálogo vacío:**
```json
{
  "guarantees": []
}
```

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

### DTOs REST

#### `GuaranteeDto`

```java
public record GuaranteeDto(String code, String description, boolean tarifable) {}
```

#### `GuaranteesResponse`

```java
public record GuaranteesResponse(List<GuaranteeDto> guarantees) {}
```

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.guarantee/
├── domain/
│   ├── model/
│   │   └── Guarantee.java                                ← record puro
│   └── port/
│       ├── in/
│       │   └── GetGuaranteesUseCase.java                 ← input port
│       └── out/
│           └── GuaranteeRepository.java                  ← output port
├── application/
│   └── usecase/
│       └── GetGuaranteesUseCaseImpl.java                 ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── GuaranteeApi.java                 ← @Tag, @Operation
    │   │       ├── GuaranteeController.java              ← implements GuaranteeApi
    │   │       ├── dto/
    │   │       │   ├── GuaranteeDto.java
    │   │       │   └── GuaranteesResponse.java
    │   │       └── mapper/
    │   │           └── GuaranteeRestMapper.java
    │   └── out/
    │       └── json/
    │           └── GuaranteeJsonAdapter.java             ← carga JSON, implementa GuaranteeRepository
    └── config/
        └── GuaranteeConfig.java                         ← @Bean wiring
```

> El adapter de persistencia es `out/json/` (no `out/persistence/`) porque no hay base de datos involucrada.
> El patrón es idéntico al de `business-lines-catalog` (SPEC-004) y `agents-catalog` (SPEC-003).

### Notas de Implementación

- `GuaranteeJsonAdapter` carga el fixture al construirse. Si el archivo no existe, lanza `IllegalStateException` para activar el fail-fast de Spring.
- `findAllTarifable()` filtra en memoria: `guarantees.stream().filter(Guarantee::tarifable).toList()`. No requiere lógica adicional en el use case.
- El ruta del endpoint (`/v1/catalogs/guarantees`) usa el prefijo `/catalogs/` para agrupar endpoints de catálogos estáticos. Verificar si el `@RequestMapping` base del controller debe incluir este prefijo o si se gestiona con un prefijo global en `application.properties`.

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `Guarantee` (record Java con `code`, `description`, `tarifable`, sin anotaciones)
- [ ] **TASK-2** Crear output port `GuaranteeRepository` con métodos `findAll(): List<Guarantee>` y `findAllTarifable(): List<Guarantee>`
- [ ] **TASK-3** Crear input port `GetGuaranteesUseCase` con método `getAll(): List<Guarantee>`
- [ ] **TASK-4** Implementar `GetGuaranteesUseCaseImpl` — delega a `GuaranteeRepository.findAll()`
- [ ] **TASK-5** Crear `GuaranteeJsonAdapter` — carga `fixtures/guarantees.json` vía `ObjectMapper` al construirse; implementa `GuaranteeRepository`; `findAllTarifable()` filtra en memoria
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/guarantees.json` con los datos estáticos (GUA-FIRE, GUA-THEFT, GUA-GLASS)
- [ ] **TASK-7** Crear DTOs REST: `GuaranteeDto`, `GuaranteesResponse`
- [ ] **TASK-8** Crear `GuaranteeRestMapper` — mapea `List<Guarantee>` → `GuaranteesResponse`
- [ ] **TASK-9** Crear interfaz Swagger `GuaranteeApi` (en `rest/swaggerdocs/`) con `@Tag`, `@Operation`, `@ApiResponse`
- [ ] **TASK-10** Crear `GuaranteeController` — implementa `GuaranteeApi`, mapea `GET /v1/catalogs/guarantees`, llama `GetGuaranteesUseCase`
- [ ] **TASK-11** Crear `GuaranteeConfig` — registra `GuaranteeJsonAdapter` y `GetGuaranteesUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetGuaranteesUseCaseImplTest` — verifica delegación al port y retorno de lista completa
- [ ] **TASK-13** Test unitario `GuaranteeJsonAdapterTest` — verifica:
  - carga correcta del JSON y mapeo a `List<Guarantee>`
  - `findAllTarifable()` retorna solo garantías con `tarifable=true`
  - comportamiento con JSON con array vacío
  - excepción al no encontrar el archivo de fixtures
- [ ] **TASK-14** Test unitario `GuaranteeRestMapperTest` — verifica mapeo `List<Guarantee>` → `GuaranteesResponse` (todos los campos incluyendo `tarifable`)
- [ ] **TASK-15** Test unitario `GuaranteeControllerTest` — verifica HTTP 200 y estructura del response (`guarantees` array con campos esperados)
- [ ] **TASK-16** Test de integración `GuaranteeCatalogIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/catalogs/guarantees`, valida response 200 con datos del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `guarantees-catalog-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `guarantees-catalog-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
- [ ] **TASK-20** Verificar que `findAllTarifable()` es accesible desde `Insurance-Quoter-Back` (revisar contrato en `docs/api-contracts.md`)
