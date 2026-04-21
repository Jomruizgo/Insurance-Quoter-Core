---
id: SPEC-003
status: DRAFT
feature: agents-catalog
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-002"]
---

# Spec: Catálogo de Agentes Disponibles

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/agents` que retorna el catálogo completo de agentes disponibles. Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación del catálogo en tiempo de ejecución.

Además del endpoint de consulta, el output port expone `existsByCode(String code): boolean` para que `Insurance-Quoter-Back` pueda validar si un agente es válido al momento de crear un folio (referencia: contrato `POST /v1/folios`, error `INVALID_REFERENCE`).

### Requerimiento de Negocio

> Catálogo de agentes disponibles en el microservicio core.
> Endpoint `GET /v1/agents`. Los datos provienen de un archivo
> `src/main/resources/fixtures/agents.json` cargado en memoria al inicio.
> No hay escritura. El catálogo es estático y gestionado por configuración.
> El port `AgentRepository` también expone `existsByCode` para validación
> cross-service desde `Insurance-Quoter-Back`.

### Historias de Usuario

#### HU-01: Consultar catálogo de agentes

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/agents y recibir la lista completa de agentes disponibles
Para:        presentar opciones de agente al usuario en el flujo de cotización

Prioridad:   Alta
Estimación:  S
Dependencias: Ninguna (sin base de datos, datos en memoria)
Capa:        Backend
```

#### HU-02: Validar existencia de agente por código

```
Como:        Insurance-Quoter-Back
Quiero:      invocar AgentRepository.existsByCode(code) para verificar si un agente es válido
Para:        rechazar la creación de folios con agentes inexistentes (error INVALID_REFERENCE)

Prioridad:   Alta
Estimación:  XS
Dependencias: HU-01 (requiere que el catálogo esté cargado en memoria)
Capa:        Backend (uso interno vía port, no expone endpoint adicional)
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Retorno del catálogo completo
  Dado que:  el servicio Insurance-Quoter-Core está disponible
             Y el archivo fixtures/agents.json contiene al menos un agente
  Cuando:    se realiza GET /v1/agents
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "agents" con un array
             Y cada elemento del array contiene "code", "name" y "subscriberId" con valores no vacíos
```

**Catálogo vacío**
```gherkin
CRITERIO-1.2: Catálogo sin entradas configuradas
  Dado que:  el archivo fixtures/agents.json existe pero el array está vacío
  Cuando:    se realiza GET /v1/agents
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "agents" con un array vacío []
```

**Error de configuración**
```gherkin
CRITERIO-1.3: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/agents.json no existe en el classpath
  Cuando:    la aplicación intenta arrancar
  Entonces:  la aplicación falla al iniciar con un mensaje de error claro
             Y NO se expone el endpoint con datos inconsistentes
```

#### Criterios de Aceptación — HU-02

**Agente existente**
```gherkin
CRITERIO-2.1: Código de agente válido
  Dado que:  el catálogo en memoria contiene el agente con code "AGT-123"
  Cuando:    se invoca AgentRepository.existsByCode("AGT-123")
  Entonces:  el método retorna true
```

**Agente inexistente**
```gherkin
CRITERIO-2.2: Código de agente inválido
  Dado que:  el catálogo en memoria NO contiene ningún agente con code "AGT-999"
  Cuando:    se invoca AgentRepository.existsByCode("AGT-999")
  Entonces:  el método retorna false
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | El catálogo es de solo lectura — no existe endpoint de creación, modificación ni eliminación. |
| RN-2 | Los datos se cargan una sola vez al iniciar la aplicación (eager loading en el adapter). |
| RN-3 | Si el archivo JSON no existe o no es parseable, la aplicación debe fallar al arrancar (fail-fast). |
| RN-4 | El `code` de cada agente es un identificador de negocio opaco (ej. `AGT-123`), no una PK de base de datos. |
| RN-5 | Cada agente pertenece a un suscriptor identificado por `subscriberId` (ej. `SUB-001`). La relación es referencial — no se valida que el `subscriberId` exista en el catálogo de suscriptores al cargar el JSON. |
| RN-6 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |
| RN-7 | `existsByCode` compara por igualdad exacta (case-sensitive). |

---

## 2. DISEÑO

### Modelo de Dominio

#### `Agent` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.agent.domain.model.Agent
public record Agent(String code, String name, String subscriberId) {}
```

| Campo          | Tipo   | Restricciones                                               |
|----------------|--------|-------------------------------------------------------------|
| `code`         | String | No nulo, no vacío — identificador de negocio (ej. `AGT-123`) |
| `name`         | String | No nulo, no vacío — nombre legible del agente               |
| `subscriberId` | String | No nulo, no vacío — referencia al suscriptor al que pertenece (ej. `SUB-001`) |

### Output Port

```java
// com.sofka.insurancequoter.core.agent.domain.port.out.AgentRepository
public interface AgentRepository {
    List<Agent> findAll();
    boolean existsByCode(String code);
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.agent.domain.port.in.GetAgentsUseCase
public interface GetAgentsUseCase {
    List<Agent> getAll();
}
```

### Fixture — `src/main/resources/fixtures/agents.json`

```json
[
  { "code": "AGT-123", "name": "Juan Pérez", "subscriberId": "SUB-001" },
  { "code": "AGT-124", "name": "María López", "subscriberId": "SUB-001" },
  { "code": "AGT-201", "name": "Carlos Ruiz", "subscriberId": "SUB-002" }
]
```

> El adapter deserializa este array en `List<Agent>` al construirse (vía Jackson `ObjectMapper`).
> `existsByCode` se implementa buscando en la lista en memoria con stream.

### API Endpoint

#### `GET /v1/agents`

| Atributo  | Valor               |
|-----------|---------------------|
| Método    | `GET`               |
| Ruta      | `/v1/agents`        |
| Auth      | Ninguna (servicio interno) |
| Produces  | `application/json`  |

**Response 200 — catálogo con datos:**
```json
{
  "agents": [
    { "code": "AGT-123", "name": "Juan Pérez", "subscriberId": "SUB-001" },
    { "code": "AGT-124", "name": "María López", "subscriberId": "SUB-001" },
    { "code": "AGT-201", "name": "Carlos Ruiz", "subscriberId": "SUB-002" }
  ]
}
```

**Response 200 — catálogo vacío:**
```json
{
  "agents": []
}
```

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

### DTOs REST

#### `AgentDto`

```java
public record AgentDto(String code, String name, String subscriberId) {}
```

#### `AgentsResponse`

```java
public record AgentsResponse(List<AgentDto> agents) {}
```

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.agent/
├── domain/
│   ├── model/
│   │   └── Agent.java                              ← record puro
│   └── port/
│       ├── in/
│       │   └── GetAgentsUseCase.java               ← input port
│       └── out/
│           └── AgentRepository.java                ← output port
├── application/
│   └── usecase/
│       └── GetAgentsUseCaseImpl.java               ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── AgentApi.java               ← @Tag, @Operation
    │   │       ├── AgentController.java            ← implements AgentApi
    │   │       ├── dto/
    │   │       │   ├── AgentDto.java
    │   │       │   └── AgentsResponse.java
    │   │       └── mapper/
    │   │           └── AgentRestMapper.java
    │   └── out/
    │       └── json/
    │           └── AgentJsonAdapter.java           ← carga JSON, implementa AgentRepository
    └── config/
        └── AgentConfig.java                        ← @Bean wiring
```

> El adapter de persistencia es `out/json/` (no `out/persistence/`) porque no hay base de datos involucrada.

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `Agent` (record Java con `code`, `name`, `subscriberId`, sin anotaciones)
- [ ] **TASK-2** Crear output port `AgentRepository` con métodos `findAll(): List<Agent>` y `existsByCode(String code): boolean`
- [ ] **TASK-3** Crear input port `GetAgentsUseCase` con método `getAll(): List<Agent>`
- [ ] **TASK-4** Implementar `GetAgentsUseCaseImpl` — delega a `AgentRepository.findAll()`
- [ ] **TASK-5** Crear `AgentJsonAdapter` — carga `fixtures/agents.json` vía `ObjectMapper` al construirse; implementa `AgentRepository` (incluyendo `existsByCode` con búsqueda en stream)
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/agents.json` con los datos estáticos de los agentes
- [ ] **TASK-7** Crear DTOs REST: `AgentDto`, `AgentsResponse`
- [ ] **TASK-8** Crear `AgentRestMapper` — mapea `List<Agent>` → `AgentsResponse`
- [ ] **TASK-9** Crear interfaz Swagger `AgentApi` (en `rest/swaggerdocs/`)
- [ ] **TASK-10** Crear `AgentController` — implementa `AgentApi`, llama `GetAgentsUseCase`
- [ ] **TASK-11** Crear `AgentConfig` — registra `AgentJsonAdapter` y `GetAgentsUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetAgentsUseCaseImplTest` — verifica delegación al port y retorno de lista
- [ ] **TASK-13** Test unitario `AgentJsonAdapterTest` — verifica carga correcta del JSON y mapeo a `List<Agent>`; verifica `existsByCode` con código existente, código inexistente y JSON vacío
- [ ] **TASK-14** Test unitario `AgentRestMapperTest` — verifica mapeo `List<Agent>` → `AgentsResponse` (incluye campo `subscriberId`)
- [ ] **TASK-15** Test unitario `AgentControllerTest` — verifica HTTP 200 y estructura del response
- [ ] **TASK-16** Test de integración `AgentCatalogIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/agents`, valida response 200 con datos del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `agents-catalog-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `agents-catalog-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
