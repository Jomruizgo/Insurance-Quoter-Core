---
id: SPEC-008
status: IMPLEMENTED
feature: tariffs
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "2.0"
related-specs: ["SPEC-007"]
---

# Spec: Consulta y Actualización de Tarifas y Factores Técnicos

> **Estado:** `APPROVED`
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone dos endpoints sobre `/v1/tariffs`:

- `GET /v1/tariffs` — retorna los factores técnicos y tasas de tarificación vigentes.
- `PUT /v1/tariffs` — actualiza los factores técnicos (operación administrativa).

Los datos se persisten en la tabla `tariffs` de PostgreSQL (DB `insurance_core_db`, puerto 5433). La tabla tiene **una sola fila activa** identificada por `id = 1`. La migración Flyway siembra los valores iniciales.

Este endpoint es **crítico** para `Insurance-Quoter-Back`: cada vez que se ejecuta el cálculo de prima (`POST /v1/quotes/{folio}/calculate`), el cotizador consume estas tarifas para calcular la cobertura de incendio, robo, equipos electrónicos y las contribuciones CATTEV y CATFHM.

A diferencia de los catálogos de lista, `Tariffs` es un objeto único por diseño: `TariffRepository` expone `findCurrent(): Tariffs` y `save(Tariffs): Tariffs` en lugar de `findAll()`.

### Requerimiento de Negocio

> Endpoints de consulta y actualización de tarifas técnicas del microservicio core.
> `GET /v1/tariffs` retorna las tarifas vigentes desde PostgreSQL.
> `PUT /v1/tariffs` actualiza los factores técnicos (uso administrativo).
> La tabla `tariffs` tiene una fila única con `id = 1`.
> Los valores iniciales se cargan vía Flyway seed migration.
> El port `TariffRepository` expone `findCurrent()` y `save()`.
> Es consumido por `Insurance-Quoter-Back` en el flujo de cálculo de prima.

### Historias de Usuario

#### HU-01: Consultar tarifas técnicas vigentes

```
Como:        sistema cliente (Insurance-Quoter-Back)
Quiero:      llamar GET /v1/tariffs y recibir los factores técnicos vigentes
Para:        calcular la prima neta de cada garantía en el flujo de cotización

Prioridad:   Alta
Estimación:  S
Dependencias: Tabla tariffs creada y sembrada (Flyway)
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Retorno de tarifas vigentes
  Dado que:  el servicio Insurance-Quoter-Core está disponible
             Y la tabla tariffs contiene la fila con id = 1
  Cuando:    se realiza GET /v1/tariffs
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "tariffs" con un objeto
             Y el objeto contiene "fireRate", "cattevFactor", "catfhmFactor",
               "theftRate" y "electronicEquipmentRate" con valores numéricos positivos
```

**Fila no encontrada**
```gherkin
CRITERIO-1.2: Tabla vacía o fila id=1 ausente
  Dado que:  la tabla tariffs no contiene la fila con id = 1
  Cuando:    se realiza GET /v1/tariffs
  Entonces:  la respuesta tiene HTTP 404
             Y el cuerpo contiene "error": "Tariffs not found"
```

**Integridad de valores**
```gherkin
CRITERIO-1.3: Todos los factores son valores numéricos positivos
  Dado que:  la tabla tariffs contiene la fila con id = 1
  Cuando:    se realiza GET /v1/tariffs
  Entonces:  todos los valores numéricos de "tariffs" son mayores que cero
             Y ningún campo del objeto "tariffs" es nulo
```

#### HU-02: Actualizar tarifas técnicas

```
Como:        administrador del sistema
Quiero:      llamar PUT /v1/tariffs con nuevos valores
Para:        ajustar los factores técnicos que impactan el cálculo de prima

Prioridad:   Alta
Estimación:  S
Dependencias: HU-01 implementada
Capa:        Backend
```

#### Criterios de Aceptación — HU-02

**Happy Path**
```gherkin
CRITERIO-2.1: Actualización exitosa de tarifas
  Dado que:  el servicio está disponible
             Y la tabla tariffs contiene la fila con id = 1
  Cuando:    se realiza PUT /v1/tariffs con un body válido de UpdateTariffsRequest
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "tariffs" con los nuevos valores persistidos
             Y la siguiente llamada GET /v1/tariffs devuelve los valores actualizados
```

**Validación de campos**
```gherkin
CRITERIO-2.2: Rechazo de valores inválidos
  Dado que:  el servicio está disponible
  Cuando:    se realiza PUT /v1/tariffs con algún campo con valor <= 0
  Entonces:  la respuesta tiene HTTP 400
             Y el cuerpo describe qué campo es inválido
```

**Fila no encontrada**
```gherkin
CRITERIO-2.3: Tabla vacía o fila id=1 ausente
  Dado que:  la tabla tariffs no contiene la fila con id = 1
  Cuando:    se realiza PUT /v1/tariffs
  Entonces:  la respuesta tiene HTTP 404
             Y el cuerpo contiene "error": "Tariffs not found"
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | La tabla `tariffs` tiene exactamente una fila activa con `id = 1`. No se crean filas adicionales. |
| RN-2 | `GET /v1/tariffs` retorna HTTP 200 con las tarifas vigentes, o HTTP 404 si la fila no existe. |
| RN-3 | `PUT /v1/tariffs` actualiza la fila existente (update, no insert). Retorna HTTP 404 si la fila no existe. |
| RN-4 | Todos los factores son `double` positivos (> 0). Un valor de cero o negativo es inválido y devuelve HTTP 400. |
| RN-5 | Los valores iniciales se cargan vía Flyway seed migration al arrancar la aplicación. |
| RN-6 | El endpoint no requiere autenticación — es un servicio interno. |
| RN-7 | Los campos representan tasas (rate) o factores multiplicadores (factor). Su semántica la interpreta el motor de cálculo en `Insurance-Quoter-Back`. |

---

## 2. DISEÑO

### Modelo de Datos (PostgreSQL)

#### Tabla `tariffs`

```sql
CREATE TABLE tariffs (
    id                       BIGINT PRIMARY KEY DEFAULT 1,
    fire_rate                DOUBLE PRECISION NOT NULL CHECK (fire_rate > 0),
    cattev_factor            DOUBLE PRECISION NOT NULL CHECK (cattev_factor > 0),
    catfhm_factor            DOUBLE PRECISION NOT NULL CHECK (catfhm_factor > 0),
    theft_rate               DOUBLE PRECISION NOT NULL CHECK (theft_rate > 0),
    electronic_equipment_rate DOUBLE PRECISION NOT NULL CHECK (electronic_equipment_rate > 0)
);
```

**Seed inicial (Flyway):**

```sql
INSERT INTO tariffs (id, fire_rate, cattev_factor, catfhm_factor, theft_rate, electronic_equipment_rate)
VALUES (1, 0.0015, 0.0008, 0.0005, 0.003, 0.002);
```

### Modelo de Dominio

#### `Tariffs` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.tariff.domain.model.Tariffs
public record Tariffs(
    double fireRate,
    double cattevFactor,
    double catfhmFactor,
    double theftRate,
    double electronicEquipmentRate
) {}
```

| Campo                     | Tipo   | Restricciones            |
|---------------------------|--------|--------------------------|
| `fireRate`                | double | Positivo — tasa incendio |
| `cattevFactor`            | double | Positivo — factor CATTEV |
| `catfhmFactor`            | double | Positivo — factor CATFHM |
| `theftRate`               | double | Positivo — tasa robo     |
| `electronicEquipmentRate` | double | Positivo — tasa equipos  |

> `double` primitivo garantiza que Jackson serialice siempre un número, nunca `null`.

### Entidad JPA

```java
// infrastructure/adapter/out/persistence/entity/TariffJpa.java
@Entity
@Table(name = "tariffs")
public class TariffJpa {
    @Id
    private Long id;
    private double fireRate;
    private double cattevFactor;
    private double catfhmFactor;
    private double theftRate;
    private double electronicEquipmentRate;
}
```

### Output Port

```java
// domain/port/out/TariffRepository.java
public interface TariffRepository {
    Optional<Tariffs> findCurrent();
    Tariffs save(Tariffs tariffs);
}
```

### Input Port (Use Cases)

```java
// domain/port/in/GetTariffsUseCase.java
public interface GetTariffsUseCase {
    Tariffs getCurrent();
}

// domain/port/in/UpdateTariffsUseCase.java
public interface UpdateTariffsUseCase {
    Tariffs update(Tariffs tariffs);
}
```

### API Endpoints

#### `GET /v1/tariffs`

| Atributo | Valor                      |
|----------|----------------------------|
| Método   | `GET`                      |
| Ruta     | `/v1/tariffs`              |
| Auth     | Ninguna (servicio interno) |
| Produces | `application/json`         |

**Response 200:**
```json
{
  "tariffs": {
    "fireRate": 0.0015,
    "cattevFactor": 0.0008,
    "catfhmFactor": 0.0005,
    "theftRate": 0.003,
    "electronicEquipmentRate": 0.002
  }
}
```

**Response 404:**
```json
{ "error": "Tariffs not found" }
```

#### `PUT /v1/tariffs`

| Atributo  | Valor                      |
|-----------|----------------------------|
| Método    | `PUT`                      |
| Ruta      | `/v1/tariffs`              |
| Auth      | Ninguna (servicio interno) |
| Consumes  | `application/json`         |
| Produces  | `application/json`         |

**Request body:**
```json
{
  "fireRate": 0.0018,
  "cattevFactor": 0.0009,
  "catfhmFactor": 0.0006,
  "theftRate": 0.0035,
  "electronicEquipmentRate": 0.0025
}
```

**Response 200:**
```json
{
  "tariffs": {
    "fireRate": 0.0018,
    "cattevFactor": 0.0009,
    "catfhmFactor": 0.0006,
    "theftRate": 0.0035,
    "electronicEquipmentRate": 0.0025
  }
}
```

**Response 400:**
```json
{ "error": "fireRate must be greater than 0" }
```

**Response 404:**
```json
{ "error": "Tariffs not found" }
```

### DTOs REST

#### `TariffsDto`

```java
public record TariffsDto(
    double fireRate,
    double cattevFactor,
    double catfhmFactor,
    double theftRate,
    double electronicEquipmentRate
) {}
```

#### `TariffsResponse`

```java
public record TariffsResponse(TariffsDto tariffs) {}
```

#### `UpdateTariffsRequest`

```java
public record UpdateTariffsRequest(
    @Positive double fireRate,
    @Positive double cattevFactor,
    @Positive double catfhmFactor,
    @Positive double theftRate,
    @Positive double electronicEquipmentRate
) {}
```

> Se usa `@Positive` de Jakarta Validation — rechaza cero y negativos con HTTP 400 automáticamente.

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.tariff/
├── domain/
│   ├── model/
│   │   └── Tariffs.java                                     ← record puro
│   └── port/
│       ├── in/
│       │   ├── GetTariffsUseCase.java                       ← input port GET
│       │   └── UpdateTariffsUseCase.java                    ← input port PUT
│       └── out/
│           └── TariffRepository.java                        ← output port (findCurrent + save)
├── application/
│   └── usecase/
│       ├── GetTariffsUseCaseImpl.java                       ← delega a TariffRepository.findCurrent()
│       └── UpdateTariffsUseCaseImpl.java                    ← valida + llama TariffRepository.save()
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── TariffApi.java                       ← @Tag, @Operation
    │   │       ├── TariffController.java                    ← implements TariffApi
    │   │       ├── dto/
    │   │       │   ├── TariffsDto.java
    │   │       │   ├── TariffsResponse.java
    │   │       │   └── UpdateTariffsRequest.java
    │   │       └── mapper/
    │   │           └── TariffRestMapper.java
    │   └── out/
    │       └── persistence/
    │           ├── entity/
    │           │   └── TariffJpa.java                       ← entidad JPA
    │           ├── repository/
    │           │   └── TariffJpaRepository.java             ← extends JpaRepository<TariffJpa, Long>
    │           └── TariffJpaAdapter.java                    ← implementa TariffRepository
    └── config/
        └── TariffConfig.java                               ← @Bean wiring
```

### Notas de Implementación

- `TariffJpaAdapter.findCurrent()` llama `jpaRepository.findById(1L)` y retorna `Optional<Tariffs>` mapeado desde `TariffJpa`.
- `TariffJpaAdapter.save()` llama `jpaRepository.save(tariffJpa)` — Spring Data hace update si `id = 1` ya existe (merge semántico de JPA).
- `GetTariffsUseCaseImpl` lanza `TariffNotFoundException` (que el controller convierte en HTTP 404) cuando `findCurrent()` retorna `Optional.empty()`.
- `UpdateTariffsUseCaseImpl` llama `findCurrent()` primero; si no existe lanza `TariffNotFoundException`. Si existe, aplica los nuevos valores y llama `save()`.
- La validación `@Positive` en `UpdateTariffsRequest` es suficiente para RN-4 — no hace falta lógica adicional en el use case.
- La ruta `/v1/tariffs` no usa el prefijo `/catalogs/` — las tarifas son factores técnicos del motor de cálculo.

---

## 3. LISTA DE TAREAS

### Base de Datos

- [ ] **TASK-1** Crear migración Flyway `V4__create_tariffs.sql` — tabla `tariffs` con columnas y constraints `CHECK (> 0)`
- [ ] **TASK-2** Crear migración Flyway `V5__seed_tariffs.sql` — INSERT de la fila inicial con `id = 1` y valores del contrato

### Backend — Dominio

- [ ] **TASK-3** Crear domain model `Tariffs` (record Java con los 5 campos `double`, sin anotaciones)
- [ ] **TASK-4** Crear output port `TariffRepository` con métodos `findCurrent(): Optional<Tariffs>` y `save(Tariffs): Tariffs`
- [ ] **TASK-5** Crear input port `GetTariffsUseCase` con método `getCurrent(): Tariffs`
- [ ] **TASK-6** Crear input port `UpdateTariffsUseCase` con método `update(Tariffs): Tariffs`

### Backend — Aplicación

- [ ] **TASK-7** Implementar `GetTariffsUseCaseImpl` — llama `TariffRepository.findCurrent()`, lanza `TariffNotFoundException` si vacío
- [ ] **TASK-8** Implementar `UpdateTariffsUseCaseImpl` — verifica existencia con `findCurrent()`, actualiza con `save()`, lanza `TariffNotFoundException` si no existe

### Backend — Infraestructura / Persistencia

- [ ] **TASK-9** Crear entidad JPA `TariffJpa` (tabla `tariffs`, `@Id Long id`)
- [ ] **TASK-10** Crear `TariffJpaRepository` — `extends JpaRepository<TariffJpa, Long>`
- [ ] **TASK-11** Crear `TariffJpaAdapter` — implementa `TariffRepository`, mapea `TariffJpa` ↔ `Tariffs`, llama `findById(1L)` y `save()`

### Backend — REST

- [ ] **TASK-12** Crear DTOs: `TariffsDto`, `TariffsResponse`, `UpdateTariffsRequest` (con `@Positive` en los 5 campos)
- [ ] **TASK-13** Crear `TariffRestMapper` — mapea `Tariffs` → `TariffsResponse` y `UpdateTariffsRequest` → `Tariffs`
- [ ] **TASK-14** Crear interfaz Swagger `TariffApi` (en `rest/swaggerdocs/`) con `@Tag`, `@Operation`, `@ApiResponse` para GET y PUT
- [ ] **TASK-15** Crear `TariffController` — implementa `TariffApi`, mapea `GET /v1/tariffs` y `PUT /v1/tariffs`, maneja `TariffNotFoundException` con HTTP 404

### Backend — Configuración

- [ ] **TASK-16** Crear `TariffConfig` — registra `TariffJpaAdapter`, `GetTariffsUseCaseImpl` y `UpdateTariffsUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-17** Test unitario `GetTariffsUseCaseImplTest` — verifica delegación al port, retorno de `Tariffs` y lanzamiento de `TariffNotFoundException` cuando `Optional.empty()`
- [ ] **TASK-18** Test unitario `UpdateTariffsUseCaseImplTest` — verifica: actualización exitosa, `TariffNotFoundException` cuando no existe fila
- [ ] **TASK-19** Test unitario `TariffJpaAdapterTest` — verifica: `findCurrent()` retorna `Optional<Tariffs>` mapeado desde `TariffJpa`, `save()` llama `jpaRepository.save()`, `Optional.empty()` cuando `findById` no encuentra nada
- [ ] **TASK-20** Test unitario `TariffRestMapperTest` — verifica mapeo `Tariffs` → `TariffsResponse` y `UpdateTariffsRequest` → `Tariffs` con los 5 campos
- [ ] **TASK-21** Test unitario `TariffControllerTest` — verifica: GET 200 con tariffs, GET 404, PUT 200 con valores actualizados, PUT 400 con campo inválido, PUT 404
- [ ] **TASK-22** Test de integración `TariffIntegrationTest` (`@SpringBootTest` + `@Testcontainers` + `PostgreSQLContainer`) — levanta contexto real, llama `GET /v1/tariffs` (200 con seed), `PUT /v1/tariffs` (200 con nuevos valores), `GET /v1/tariffs` (200 con valores actualizados)

### QA

- [ ] **TASK-23** Ejecutar `/risk-identifier` para generar matriz de riesgos `tariffs-risks.md`
- [ ] **TASK-24** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `tariffs-gherkin.md`
- [ ] **TASK-25** Validar endpoints manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`) — GET y PUT
- [ ] **TASK-26** Verificar que `Insurance-Quoter-Back` consume correctamente `GET /v1/tariffs` en el flujo de cálculo de prima (revisar contrato en `docs/api-contracts.md`)
