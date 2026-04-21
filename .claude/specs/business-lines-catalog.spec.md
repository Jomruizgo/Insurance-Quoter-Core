---
id: SPEC-004
status: DRAFT
feature: business-lines-catalog
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-002", "SPEC-003"]
---

# Spec: Catálogo de Giros de Negocio

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/business-lines` que retorna el catálogo completo de giros de negocio (`businessLine`) disponibles, incluyendo su clave incendio (`fireKey`). Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación del catálogo en tiempo de ejecución.

El catálogo de giros de negocio es necesario para que `Insurance-Quoter-Back` pueda presentar opciones válidas al usuario durante el flujo de cotización y asociar la clave incendio correspondiente al cálculo de prima.

### Requerimiento de Negocio

> Catálogo de giros de negocio disponibles en el microservicio core.
> Endpoint `GET /v1/business-lines`. Los datos provienen de un archivo
> `src/main/resources/fixtures/business-lines.json` cargado en memoria al inicio.
> No hay escritura. El catálogo es estático y gestionado por configuración.
> El port `BusinessLineRepository` expone `findAll()` para consulta del catálogo completo.

### Historias de Usuario

#### HU-01: Consultar catálogo de giros de negocio

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/business-lines y recibir la lista completa de giros de negocio
Para:        presentar opciones de giro al usuario en el flujo de cotización y
             obtener la clave incendio asociada para el cálculo de prima

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
             Y el archivo fixtures/business-lines.json contiene al menos un giro de negocio
  Cuando:    se realiza GET /v1/business-lines
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "businessLines" con un array
             Y cada elemento del array contiene "code", "description" y "fireKey" con valores no vacíos
```

**Catálogo vacío**
```gherkin
CRITERIO-1.2: Catálogo sin entradas configuradas
  Dado que:  el archivo fixtures/business-lines.json existe pero el array está vacío
  Cuando:    se realiza GET /v1/business-lines
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "businessLines" con un array vacío []
```

**Error de configuración**
```gherkin
CRITERIO-1.3: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/business-lines.json no existe en el classpath
  Cuando:    la aplicación intenta arrancar
  Entonces:  la aplicación falla al iniciar con un mensaje de error claro
             Y NO se expone el endpoint con datos inconsistentes
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | El catálogo es de solo lectura — no existe endpoint de creación, modificación ni eliminación. |
| RN-2 | Los datos se cargan una sola vez al iniciar la aplicación (eager loading en el adapter). |
| RN-3 | Si el archivo JSON no existe o no es parseable, la aplicación debe fallar al arrancar (fail-fast). |
| RN-4 | El `code` de cada giro de negocio es un identificador de negocio opaco (ej. `BL-001`), no una PK de base de datos. |
| RN-5 | Cada giro de negocio tiene asociada una clave incendio (`fireKey`) que se utiliza en el cálculo de prima neta. La relación es interna al catálogo. |
| RN-6 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |

---

## 2. DISEÑO

### Modelo de Dominio

#### `BusinessLine` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.businessline.domain.model.BusinessLine
public record BusinessLine(String code, String description, String fireKey) {}
```

| Campo         | Tipo   | Restricciones                                                        |
|---------------|--------|----------------------------------------------------------------------|
| `code`        | String | No nulo, no vacío — identificador de negocio (ej. `BL-001`)         |
| `description` | String | No nulo, no vacío — descripción legible del giro (ej. `Bodega de mercancías`) |
| `fireKey`     | String | No nulo, no vacío — clave incendio asociada (ej. `FK-INC-01`)       |

### Output Port

```java
// com.sofka.insurancequoter.core.businessline.domain.port.out.BusinessLineRepository
public interface BusinessLineRepository {
    List<BusinessLine> findAll();
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.businessline.domain.port.in.GetBusinessLinesUseCase
public interface GetBusinessLinesUseCase {
    List<BusinessLine> getAll();
}
```

### Fixture — `src/main/resources/fixtures/business-lines.json`

```json
[
  { "code": "BL-001", "description": "Bodega de mercancías", "fireKey": "FK-INC-01" },
  { "code": "BL-002", "description": "Oficina administrativa", "fireKey": "FK-INC-02" },
  { "code": "BL-003", "description": "Local comercial", "fireKey": "FK-INC-03" },
  { "code": "BL-004", "description": "Restaurante", "fireKey": "FK-INC-04" },
  { "code": "BL-005", "description": "Industria ligera", "fireKey": "FK-INC-05" }
]
```

> El adapter deserializa este array en `List<BusinessLine>` al construirse (vía Jackson `ObjectMapper`).

### API Endpoint

#### `GET /v1/business-lines`

| Atributo  | Valor                          |
|-----------|--------------------------------|
| Método    | `GET`                          |
| Ruta      | `/v1/business-lines`           |
| Auth      | Ninguna (servicio interno)     |
| Produces  | `application/json`             |

**Response 200 — catálogo con datos:**
```json
{
  "businessLines": [
    { "code": "BL-001", "description": "Bodega de mercancías", "fireKey": "FK-INC-01" },
    { "code": "BL-002", "description": "Oficina administrativa", "fireKey": "FK-INC-02" }
  ]
}
```

**Response 200 — catálogo vacío:**
```json
{
  "businessLines": []
}
```

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

### DTOs REST

#### `BusinessLineDto`

```java
public record BusinessLineDto(String code, String description, String fireKey) {}
```

#### `BusinessLinesResponse`

```java
public record BusinessLinesResponse(List<BusinessLineDto> businessLines) {}
```

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.businessline/
├── domain/
│   ├── model/
│   │   └── BusinessLine.java                              ← record puro
│   └── port/
│       ├── in/
│       │   └── GetBusinessLinesUseCase.java               ← input port
│       └── out/
│           └── BusinessLineRepository.java                ← output port
├── application/
│   └── usecase/
│       └── GetBusinessLinesUseCaseImpl.java               ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── BusinessLineApi.java               ← @Tag, @Operation
    │   │       ├── BusinessLineController.java            ← implements BusinessLineApi
    │   │       ├── dto/
    │   │       │   ├── BusinessLineDto.java
    │   │       │   └── BusinessLinesResponse.java
    │   │       └── mapper/
    │   │           └── BusinessLineRestMapper.java
    │   └── out/
    │       └── json/
    │           └── BusinessLineJsonAdapter.java           ← carga JSON, implementa BusinessLineRepository
    └── config/
        └── BusinessLineConfig.java                        ← @Bean wiring
```

> El adapter de persistencia es `out/json/` (no `out/persistence/`) porque no hay base de datos involucrada — el patrón es idéntico al de `agents-catalog` (SPEC-003).

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `BusinessLine` (record Java con `code`, `description`, `fireKey`, sin anotaciones)
- [ ] **TASK-2** Crear output port `BusinessLineRepository` con método `findAll(): List<BusinessLine>`
- [ ] **TASK-3** Crear input port `GetBusinessLinesUseCase` con método `getAll(): List<BusinessLine>`
- [ ] **TASK-4** Implementar `GetBusinessLinesUseCaseImpl` — delega a `BusinessLineRepository.findAll()`
- [ ] **TASK-5** Crear `BusinessLineJsonAdapter` — carga `fixtures/business-lines.json` vía `ObjectMapper` al construirse; implementa `BusinessLineRepository`
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/business-lines.json` con los datos estáticos de los giros de negocio
- [ ] **TASK-7** Crear DTOs REST: `BusinessLineDto`, `BusinessLinesResponse`
- [ ] **TASK-8** Crear `BusinessLineRestMapper` — mapea `List<BusinessLine>` → `BusinessLinesResponse`
- [ ] **TASK-9** Crear interfaz Swagger `BusinessLineApi` (en `rest/swaggerdocs/`)
- [ ] **TASK-10** Crear `BusinessLineController` — implementa `BusinessLineApi`, llama `GetBusinessLinesUseCase`
- [ ] **TASK-11** Crear `BusinessLineConfig` — registra `BusinessLineJsonAdapter` y `GetBusinessLinesUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetBusinessLinesUseCaseImplTest` — verifica delegación al port y retorno de lista
- [ ] **TASK-13** Test unitario `BusinessLineJsonAdapterTest` — verifica carga correcta del JSON y mapeo a `List<BusinessLine>`; verifica comportamiento con JSON vacío y error al no encontrar el archivo
- [ ] **TASK-14** Test unitario `BusinessLineRestMapperTest` — verifica mapeo `List<BusinessLine>` → `BusinessLinesResponse` (todos los campos)
- [ ] **TASK-15** Test unitario `BusinessLineControllerTest` — verifica HTTP 200 y estructura del response
- [ ] **TASK-16** Test de integración `BusinessLineCatalogIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/business-lines`, valida response 200 con datos del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `business-lines-catalog-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `business-lines-catalog-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
