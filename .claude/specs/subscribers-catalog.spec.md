---
id: SPEC-002
status: IMPLEMENTED
feature: subscribers-catalog
created: 2026-04-21
updated: 2026-04-21
approved: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: []
---

# Spec: Catálogo de Suscriptores Disponibles

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/subscribers` que retorna el catálogo completo de suscriptores disponibles. Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación del catálogo en tiempo de ejecución.

### Requerimiento de Negocio

> Catálogo de suscriptores disponibles en el microservicio core.
> Endpoint `GET /v1/subscribers`. Los datos provienen de un archivo
> `src/main/resources/fixtures/subscribers.json` cargado en memoria al inicio.
> No hay escritura. El catálogo es estático y gestionado por configuración.

### Historias de Usuario

#### HU-01: Consultar catálogo de suscriptores

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/subscribers y recibir la lista completa de suscriptores disponibles
Para:        presentar opciones de suscriptor al usuario en el flujo de cotización

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
             Y el archivo fixtures/subscribers.json contiene al menos un suscriptor
  Cuando:    se realiza GET /v1/subscribers
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "subscribers" con un array
             Y cada elemento del array contiene "id" y "name" con valores no vacíos
```

**Catálogo vacío**
```gherkin
CRITERIO-1.2: Catálogo sin entradas configuradas
  Dado que:  el archivo fixtures/subscribers.json existe pero el array está vacío
  Cuando:    se realiza GET /v1/subscribers
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "subscribers" con un array vacío []
```

**Error de configuración**
```gherkin
CRITERIO-1.3: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/subscribers.json no existe en el classpath
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
| RN-4 | El `id` de cada suscriptor es un identificador de negocio opaco (ej. `SUB-001`), no una PK de base de datos. |
| RN-5 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |

---

## 2. DISEÑO

### Modelo de Dominio

#### `Subscriber` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.subscriber.domain.model.Subscriber
public record Subscriber(String id, String name) {}
```

| Campo | Tipo   | Restricciones                        |
|-------|--------|--------------------------------------|
| `id`  | String | No nulo, no vacío — identificador de negocio (ej. `SUB-001`) |
| `name`| String | No nulo, no vacío — nombre legible del suscriptor |

### Output Port

```java
// com.sofka.insurancequoter.core.subscriber.domain.port.out.SubscriberRepository
public interface SubscriberRepository {
    List<Subscriber> findAll();
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.subscriber.domain.port.in.GetSubscribersUseCase
public interface GetSubscribersUseCase {
    List<Subscriber> getAll();
}
```

### Fixture — `src/main/resources/fixtures/subscribers.json`

```json
[
  { "id": "SUB-001", "name": "Seguros Sofka" },
  { "id": "SUB-002", "name": "Aseguradora Norte" }
]
```

> El adapter deserializa este array en `List<Subscriber>` al construirse (vía Jackson `ObjectMapper`).

### API Endpoint

#### `GET /v1/subscribers`

| Atributo  | Valor |
|-----------|-------|
| Método    | `GET` |
| Ruta      | `/v1/subscribers` |
| Auth      | Ninguna (servicio interno) |
| Produces  | `application/json` |

**Response 200 — catálogo con datos:**
```json
{
  "subscribers": [
    { "id": "SUB-001", "name": "Seguros Sofka" },
    { "id": "SUB-002", "name": "Aseguradora Norte" }
  ]
}
```

**Response 200 — catálogo vacío:**
```json
{
  "subscribers": []
}
```

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

### DTOs REST

#### `SubscriberDto`

```java
public record SubscriberDto(String id, String name) {}
```

#### `SubscribersResponse`

```java
public record SubscribersResponse(List<SubscriberDto> subscribers) {}
```

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.subscriber/
├── domain/
│   ├── model/
│   │   └── Subscriber.java                          ← record puro
│   └── port/
│       ├── in/
│       │   └── GetSubscribersUseCase.java            ← input port
│       └── out/
│           └── SubscriberRepository.java             ← output port
├── application/
│   └── usecase/
│       └── GetSubscribersUseCaseImpl.java            ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── SubscriberApi.java            ← @Tag, @Operation
    │   │       ├── SubscriberController.java          ← implements SubscriberApi
    │   │       ├── dto/
    │   │       │   ├── SubscriberDto.java
    │   │       │   └── SubscribersResponse.java
    │   │       └── mapper/
    │   │           └── SubscriberRestMapper.java
    │   └── out/
    │       └── json/
    │           └── SubscriberJsonAdapter.java         ← carga JSON, implementa SubscriberRepository
    └── config/
        └── SubscriberConfig.java                     ← @Bean wiring
```

> El adapter de persistencia es `out/json/` (no `out/persistence/`) porque no hay base de datos involucrada.

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `Subscriber` (record Java, sin anotaciones)
- [ ] **TASK-2** Crear output port `SubscriberRepository` con método `findAll(): List<Subscriber>`
- [ ] **TASK-3** Crear input port `GetSubscribersUseCase` con método `getAll(): List<Subscriber>`
- [ ] **TASK-4** Implementar `GetSubscribersUseCaseImpl` — delega a `SubscriberRepository`
- [ ] **TASK-5** Crear `SubscriberJsonAdapter` — carga `fixtures/subscribers.json` vía `ObjectMapper` al construirse; implementa `SubscriberRepository`
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/subscribers.json` con los datos estáticos de los suscriptores
- [ ] **TASK-7** Crear DTOs REST: `SubscriberDto`, `SubscribersResponse`
- [ ] **TASK-8** Crear `SubscriberRestMapper` — mapea `List<Subscriber>` → `SubscribersResponse`
- [ ] **TASK-9** Crear interfaz Swagger `SubscriberApi` (en `rest/swaggerdocs/`)
- [ ] **TASK-10** Crear `SubscriberController` — implementa `SubscriberApi`, llama `GetSubscribersUseCase`
- [ ] **TASK-11** Crear `SubscriberConfig` — registra `SubscriberJsonAdapter` y `GetSubscribersUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetSubscribersUseCaseImplTest` — verifica delegación al port y retorno de lista
- [ ] **TASK-13** Test unitario `SubscriberJsonAdapterTest` — verifica carga correcta del JSON y mapeo a `List<Subscriber>`; caso: JSON válido, JSON vacío
- [ ] **TASK-14** Test unitario `SubscriberRestMapperTest` — verifica mapeo `List<Subscriber>` → `SubscribersResponse`
- [ ] **TASK-15** Test unitario `SubscriberControllerTest` — verifica HTTP 200 y estructura del response
- [ ] **TASK-16** Test de integración `SubscriberCatalogIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/subscribers`, valida response 200 con datos del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `subscribers-catalog-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `subscribers-catalog-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
