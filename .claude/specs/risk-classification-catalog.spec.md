---
id: SPEC-006
status: DRAFT
feature: risk-classification-catalog
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-002", "SPEC-003", "SPEC-004"]
---

# Spec: Catálogo de Clasificaciones de Riesgo

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/catalogs/risk-classification` que retorna el catálogo completo de clasificaciones de riesgo para suscripción. Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación del catálogo en tiempo de ejecución.

`Insurance-Quoter-Back` utiliza este catálogo para validar la clasificación de riesgo seleccionada por el suscriptor durante el flujo de cotización.

### Requerimiento de Negocio

> Catálogo de clasificaciones de riesgo disponibles en el microservicio core.
> Endpoint `GET /v1/catalogs/risk-classification`. Los datos provienen de un archivo
> `src/main/resources/fixtures/risk-classifications.json` cargado en memoria al inicio.
> No hay escritura. El catálogo es estático y gestionado por configuración.

### Historias de Usuario

#### HU-01: Consultar catálogo de clasificaciones de riesgo

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/catalogs/risk-classification y recibir la lista completa
             de clasificaciones de riesgo disponibles
Para:        presentar opciones de clasificación al suscriptor en el flujo de cotización
             y validar que la clasificación seleccionada es válida

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
             Y el archivo fixtures/risk-classifications.json contiene al menos una clasificación
  Cuando:    se realiza GET /v1/catalogs/risk-classification
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "riskClassifications" con un array
             Y cada elemento del array contiene "code" y "description" con valores no vacíos
```

**Catálogo vacío**
```gherkin
CRITERIO-1.2: Catálogo sin entradas configuradas
  Dado que:  el archivo fixtures/risk-classifications.json existe pero el array está vacío
  Cuando:    se realiza GET /v1/catalogs/risk-classification
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "riskClassifications" con un array vacío []
```

**Error de configuración**
```gherkin
CRITERIO-1.3: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/risk-classifications.json no existe en el classpath
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
| RN-4 | El `code` de cada clasificación es un identificador de negocio opaco (ej. `STANDARD`), no una PK de base de datos. |
| RN-5 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |
| RN-6 | Los códigos de clasificación son: `STANDARD`, `PREFERRED`, `SUBSTANDARD`. Pueden extenderse en el futuro sin cambio de código (solo actualizar el JSON). |

---

## 2. DISEÑO

### Modelo de Dominio

#### `RiskClassification` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.riskclassification.domain.model.RiskClassification
public record RiskClassification(String code, String description) {}
```

| Campo         | Tipo   | Restricciones                                                        |
|---------------|--------|----------------------------------------------------------------------|
| `code`        | String | No nulo, no vacío — identificador de negocio (ej. `STANDARD`)       |
| `description` | String | No nulo, no vacío — descripción legible (ej. `Riesgo estándar`)     |

### Output Port

```java
// com.sofka.insurancequoter.core.riskclassification.domain.port.out.RiskClassificationRepository
public interface RiskClassificationRepository {
    List<RiskClassification> findAll();
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.riskclassification.domain.port.in.GetRiskClassificationsUseCase
public interface GetRiskClassificationsUseCase {
    List<RiskClassification> getAll();
}
```

### Fixture — `src/main/resources/fixtures/risk-classifications.json`

```json
[
  { "code": "STANDARD",    "description": "Riesgo estándar" },
  { "code": "PREFERRED",   "description": "Riesgo preferente" },
  { "code": "SUBSTANDARD", "description": "Riesgo subestándar" }
]
```

> El adapter deserializa este array en `List<RiskClassification>` al construirse (vía Jackson `ObjectMapper`).

### API Endpoint

#### `GET /v1/catalogs/risk-classification`

| Atributo  | Valor                                   |
|-----------|-----------------------------------------|
| Método    | `GET`                                   |
| Ruta      | `/v1/catalogs/risk-classification`      |
| Auth      | Ninguna (servicio interno)              |
| Produces  | `application/json`                      |

**Response 200 — catálogo con datos:**
```json
{
  "riskClassifications": [
    { "code": "STANDARD",    "description": "Riesgo estándar" },
    { "code": "PREFERRED",   "description": "Riesgo preferente" },
    { "code": "SUBSTANDARD", "description": "Riesgo subestándar" }
  ]
}
```

**Response 200 — catálogo vacío:**
```json
{
  "riskClassifications": []
}
```

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

### DTOs REST

```java
public record RiskClassificationDto(String code, String description) {}

public record RiskClassificationsResponse(List<RiskClassificationDto> riskClassifications) {}
```

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.riskclassification/
├── domain/
│   ├── model/
│   │   └── RiskClassification.java                              ← record puro
│   └── port/
│       ├── in/
│       │   └── GetRiskClassificationsUseCase.java               ← input port
│       └── out/
│           └── RiskClassificationRepository.java                ← output port
├── application/
│   └── usecase/
│       └── GetRiskClassificationsUseCaseImpl.java               ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── RiskClassificationApi.java               ← @Tag, @Operation
    │   │       ├── RiskClassificationController.java            ← implements RiskClassificationApi
    │   │       ├── dto/
    │   │       │   ├── RiskClassificationDto.java
    │   │       │   └── RiskClassificationsResponse.java
    │   │       └── mapper/
    │   │           └── RiskClassificationRestMapper.java
    │   └── out/
    │       └── json/
    │           └── RiskClassificationJsonAdapter.java           ← carga JSON, implementa RiskClassificationRepository
    └── config/
        └── RiskClassificationConfig.java                        ← @Bean wiring
```

> Patrón idéntico a `agents-catalog` (SPEC-003) y `business-lines-catalog` (SPEC-004): datos estáticos en `out/json/`, sin base de datos.

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `RiskClassification` (record Java con `code`, `description`, sin anotaciones)
- [ ] **TASK-2** Crear output port `RiskClassificationRepository` con método `findAll(): List<RiskClassification>`
- [ ] **TASK-3** Crear input port `GetRiskClassificationsUseCase` con método `getAll(): List<RiskClassification>`
- [ ] **TASK-4** Implementar `GetRiskClassificationsUseCaseImpl` — delega a `RiskClassificationRepository.findAll()`
- [ ] **TASK-5** Crear `RiskClassificationJsonAdapter` — carga `fixtures/risk-classifications.json` vía `ObjectMapper` al construirse; implementa `RiskClassificationRepository`; fail-fast si el archivo no existe
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/risk-classifications.json` con los 3 valores del contrato
- [ ] **TASK-7** Crear DTOs REST: `RiskClassificationDto`, `RiskClassificationsResponse`
- [ ] **TASK-8** Crear `RiskClassificationRestMapper` — mapea `List<RiskClassification>` → `RiskClassificationsResponse`
- [ ] **TASK-9** Crear interfaz Swagger `RiskClassificationApi` (en `rest/swaggerdocs/`)
- [ ] **TASK-10** Crear `RiskClassificationController` — implementa `RiskClassificationApi`, llama `GetRiskClassificationsUseCase`
- [ ] **TASK-11** Crear `RiskClassificationConfig` — registra `RiskClassificationJsonAdapter` y `GetRiskClassificationsUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetRiskClassificationsUseCaseImplTest` — verifica delegación al port y retorno de lista
- [ ] **TASK-13** Test unitario `RiskClassificationJsonAdapterTest` — verifica carga del JSON, mapeo correcto, lista vacía y fail-fast con archivo inexistente
- [ ] **TASK-14** Test unitario `RiskClassificationRestMapperTest` — verifica mapeo `List<RiskClassification>` → `RiskClassificationsResponse`
- [ ] **TASK-15** Test unitario `RiskClassificationControllerTest` — verifica HTTP 200 y estructura del response
- [ ] **TASK-16** Test de integración `RiskClassificationCatalogIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/catalogs/risk-classification`, valida response 200 con los 3 códigos del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `risk-classification-catalog-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `risk-classification-catalog-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
