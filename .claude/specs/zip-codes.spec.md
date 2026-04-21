---
id: SPEC-005
status: DRAFT
feature: zip-codes
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-001"]
---

# Spec: Consulta y Validación de Códigos Postales

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone dos endpoints para el manejo de códigos postales (`zipCode`):

- `GET /v1/zip-codes/{zipCode}` — retorna los datos completos de un código postal: estado, municipio, ciudad, colonias y zonas (catastrófica, TEV, FHM).
- `POST /v1/zip-codes/validate` — indica si un código postal existe y es válido.

Los datos se persisten en la tabla `zip_codes` de PostgreSQL (puerto 5433). Los datos iniciales se cargan mediante Flyway con la migración `V2__seed_zip_codes.sql`.

`Insurance-Quoter-Back` utiliza estos endpoints para validar el código postal ingresado por el usuario durante el flujo de cotización y obtener las zonas necesarias para el cálculo de prima.

### Requerimiento de Negocio

> Consulta de códigos postales con sus zonas catastróficas, TEV y FHM almacenados en base de datos.
> Dos endpoints: GET para obtener datos completos de un CP y POST para validar su existencia.
> Datos iniciales cargados con Flyway. La tabla `zip_codes` es de solo lectura en runtime —
> no se contemplan endpoints de escritura.

### Historias de Usuario

#### HU-01: Consultar datos de un código postal

```
Como:        sistema cliente (Insurance-Quoter-Back u otro consumidor)
Quiero:      llamar GET /v1/zip-codes/{zipCode} y recibir los datos completos del CP
Para:        mostrar al usuario las colonias disponibles y obtener las zonas
             necesarias para el cálculo de prima (catastrófica, TEV, FHM)

Prioridad:   Alta
Estimación:  M
Dependencias: Flyway V2 con datos seed en DB
Capa:        Backend
```

#### HU-02: Validar existencia de un código postal

```
Como:        sistema cliente (Insurance-Quoter-Back)
Quiero:      llamar POST /v1/zip-codes/validate con un código postal
Para:        confirmar que el CP es válido antes de continuar el flujo de cotización
             y evitar crear folios con CPs inexistentes

Prioridad:   Alta
Estimación:  S
Dependencias: HU-01 (mismo repositorio y tabla)
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Retorno de datos completos de un CP existente
  Dado que:  el código postal "06600" existe en la base de datos
  Cuando:    se realiza GET /v1/zip-codes/06600
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "zipCode": "06600"
             Y contiene "state", "municipality", "city" con valores no vacíos
             Y contiene "neighborhoods" como array con al menos una colonia
             Y contiene "catastrophicZone", "tevZone", "fhmZone" con valores no vacíos
             Y contiene "valid": true
```

**CP no encontrado**
```gherkin
CRITERIO-1.2: CP inexistente retorna 404
  Dado que:  el código postal "99999" NO existe en la base de datos
  Cuando:    se realiza GET /v1/zip-codes/99999
  Entonces:  la respuesta tiene HTTP 404
             Y el cuerpo contiene "error": "Zip code not found"
             Y contiene "code": "ZIP_CODE_NOT_FOUND"
```

**CP con formato inválido**
```gherkin
CRITERIO-1.3: CP con formato inválido retorna 400
  Dado que:  el valor "ABC" no es un código postal válido (no es numérico de 5 dígitos)
  Cuando:    se realiza GET /v1/zip-codes/ABC
  Entonces:  la respuesta tiene HTTP 400
             Y el cuerpo contiene un mensaje de error descriptivo
```

#### Criterios de Aceptación — HU-02

**CP válido**
```gherkin
CRITERIO-2.1: Validación exitosa de CP existente
  Dado que:  el código postal "06600" existe en la base de datos
  Cuando:    se realiza POST /v1/zip-codes/validate con body { "zipCode": "06600" }
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "valid": true
             Y contiene "zipCode": "06600"
```

**CP inválido**
```gherkin
CRITERIO-2.2: Validación de CP inexistente
  Dado que:  el código postal "99999" NO existe en la base de datos
  Cuando:    se realiza POST /v1/zip-codes/validate con body { "zipCode": "99999" }
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene "valid": false
             Y contiene "zipCode": "99999"
```

**Request sin zipCode**
```gherkin
CRITERIO-2.3: Request sin campo zipCode retorna 400
  Dado que:  se envía un body vacío o sin el campo "zipCode"
  Cuando:    se realiza POST /v1/zip-codes/validate
  Entonces:  la respuesta tiene HTTP 400
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | La tabla `zip_codes` es de solo lectura en runtime — no existen endpoints de creación, modificación ni eliminación. |
| RN-2 | Las colonias (`neighborhoods`) se almacenan en una tabla separada `zip_code_neighborhoods` con FK a `zip_codes`. |
| RN-3 | Un CP puede tener cero o más colonias asociadas. |
| RN-4 | `GET /v1/zip-codes/{zipCode}` retorna 404 si el CP no existe; nunca retorna null en campos de zona. |
| RN-5 | `POST /v1/zip-codes/validate` siempre retorna HTTP 200; el campo `valid` indica la existencia. |
| RN-6 | Los valores de zona (`catastrophicZone`, `tevZone`, `fhmZone`) son strings opacos definidos por negocio (ej. `ZONE_A`, `TEV-1`, `FHM-2`). |
| RN-7 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |
| RN-8 | Los datos seed de `V2__seed_zip_codes.sql` deben incluir al menos 5 CPs representativos de distintos estados y zonas. |

---

## 2. DISEÑO

### Modelo de Dominio

#### `ZipCode` — domain model (POJO puro, sin anotaciones JPA ni Spring)

```java
// com.sofka.insurancequoter.core.zipcode.domain.model.ZipCode
public record ZipCode(
    String zipCode,
    String state,
    String municipality,
    String city,
    List<String> neighborhoods,
    String catastrophicZone,
    String tevZone,
    String fhmZone
) {}
```

| Campo              | Tipo           | Restricciones                                              |
|--------------------|----------------|------------------------------------------------------------|
| `zipCode`          | String         | No nulo, no vacío — 5 dígitos numéricos (ej. `06600`)     |
| `state`            | String         | No nulo, no vacío                                          |
| `municipality`     | String         | No nulo, no vacío                                          |
| `city`             | String         | No nulo, no vacío                                          |
| `neighborhoods`    | List\<String\> | No nulo — puede estar vacío                                |
| `catastrophicZone` | String         | No nulo, no vacío (ej. `ZONE_A`)                           |
| `tevZone`          | String         | No nulo, no vacío (ej. `TEV-1`)                            |
| `fhmZone`          | String         | No nulo, no vacío (ej. `FHM-2`)                            |

### Output Port

```java
// com.sofka.insurancequoter.core.zipcode.domain.port.out.ZipCodeRepository
public interface ZipCodeRepository {
    Optional<ZipCode> findByZipCode(String zipCode);
}
```

### Input Ports (Use Cases)

```java
// com.sofka.insurancequoter.core.zipcode.domain.port.in.GetZipCodeUseCase
public interface GetZipCodeUseCase {
    ZipCode getByZipCode(String zipCode);
}

// com.sofka.insurancequoter.core.zipcode.domain.port.in.ValidateZipCodeUseCase
public interface ValidateZipCodeUseCase {
    ZipCodeValidationResult validate(String zipCode);
}
```

> `ZipCodeValidationResult` es un record en el dominio: `record ZipCodeValidationResult(boolean valid, String zipCode) {}`
> `GetZipCodeUseCase` lanza `ZipCodeNotFoundException` (excepción de dominio) cuando el CP no existe.

### Modelo de Datos — Esquema PostgreSQL

#### Tabla `zip_codes`

```sql
CREATE TABLE zip_codes (
    zip_code         VARCHAR(10)  PRIMARY KEY,
    state            VARCHAR(100) NOT NULL,
    municipality     VARCHAR(100) NOT NULL,
    city             VARCHAR(100) NOT NULL,
    catastrophic_zone VARCHAR(20) NOT NULL,
    tev_zone         VARCHAR(20)  NOT NULL,
    fhm_zone         VARCHAR(20)  NOT NULL
);
```

#### Tabla `zip_code_neighborhoods`

```sql
CREATE TABLE zip_code_neighborhoods (
    id               BIGSERIAL    PRIMARY KEY,
    zip_code         VARCHAR(10)  NOT NULL REFERENCES zip_codes(zip_code),
    neighborhood     VARCHAR(150) NOT NULL
);

CREATE INDEX idx_zip_code_neighborhoods_zip_code ON zip_code_neighborhoods(zip_code);
```

> Las migraciones se crean en `src/main/resources/db/migration/`:
> - `V2__create_zip_codes.sql` — DDL de las dos tablas
> - `V3__seed_zip_codes.sql` — datos iniciales representativos

### Entidades JPA

#### `ZipCodeJpa`

```java
// infrastructure/adapter/out/persistence/entity/ZipCodeJpa.java
@Entity
@Table(name = "zip_codes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ZipCodeJpa {

    @Id
    @Column(name = "zip_code", nullable = false, length = 10)
    private String zipCode;

    @Column(name = "state", nullable = false, length = 100)
    private String state;

    @Column(name = "municipality", nullable = false, length = 100)
    private String municipality;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "catastrophic_zone", nullable = false, length = 20)
    private String catastrophicZone;

    @Column(name = "tev_zone", nullable = false, length = 20)
    private String tevZone;

    @Column(name = "fhm_zone", nullable = false, length = 20)
    private String fhmZone;

    @OneToMany(mappedBy = "zipCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ZipCodeNeighborhoodJpa> neighborhoods;
}
```

#### `ZipCodeNeighborhoodJpa`

```java
// infrastructure/adapter/out/persistence/entity/ZipCodeNeighborhoodJpa.java
@Entity
@Table(name = "zip_code_neighborhoods")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ZipCodeNeighborhoodJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zip_code", nullable = false)
    private ZipCodeJpa zipCode;

    @Column(name = "neighborhood", nullable = false, length = 150)
    private String neighborhood;
}
```

### Spring Data JPA Repository

```java
// infrastructure/adapter/out/persistence/ZipCodeJpaRepository.java
public interface ZipCodeJpaRepository extends JpaRepository<ZipCodeJpa, String> {

    @Query("SELECT z FROM ZipCodeJpa z LEFT JOIN FETCH z.neighborhoods WHERE z.zipCode = :zipCode")
    Optional<ZipCodeJpa> findByZipCodeWithNeighborhoods(@Param("zipCode") String zipCode);
}
```

> Se usa `JOIN FETCH` para evitar N+1 al cargar colonias.

### Excepciones de dominio

```java
// domain/exception/ZipCodeNotFoundException.java
public class ZipCodeNotFoundException extends RuntimeException {
    public ZipCodeNotFoundException(String zipCode) {
        super("Zip code not found: " + zipCode);
    }
}
```

### API Endpoints

#### `GET /v1/zip-codes/{zipCode}`

| Atributo   | Valor                         |
|------------|-------------------------------|
| Método     | `GET`                         |
| Ruta       | `/v1/zip-codes/{zipCode}`     |
| Auth       | Ninguna (servicio interno)    |
| Produces   | `application/json`            |

**Response 200:**
```json
{
  "zipCode": "06600",
  "state": "Ciudad de México",
  "municipality": "Cuauhtémoc",
  "city": "Ciudad de México",
  "neighborhoods": ["Juárez", "Tabacalera"],
  "catastrophicZone": "ZONE_A",
  "tevZone": "TEV-1",
  "fhmZone": "FHM-2",
  "valid": true
}
```

**Response 404:**
```json
{ "error": "Zip code not found", "code": "ZIP_CODE_NOT_FOUND" }
```

**Response 400** (formato inválido):
```json
{ "error": "Invalid zip code format", "code": "INVALID_ZIP_CODE_FORMAT" }
```

#### `POST /v1/zip-codes/validate`

| Atributo   | Valor                            |
|------------|----------------------------------|
| Método     | `POST`                           |
| Ruta       | `/v1/zip-codes/validate`         |
| Auth       | Ninguna (servicio interno)       |
| Consumes   | `application/json`               |
| Produces   | `application/json`               |

**Request:**
```json
{ "zipCode": "06600" }
```

**Response 200 — CP válido:**
```json
{ "valid": true, "zipCode": "06600" }
```

**Response 200 — CP inválido:**
```json
{ "valid": false, "zipCode": "99999" }
```

**Response 400** (body sin `zipCode`):
```json
{ "error": "zipCode is required", "code": "MISSING_FIELD" }
```

### DTOs REST

```java
// dto de response para GET
public record ZipCodeResponse(
    String zipCode,
    String state,
    String municipality,
    String city,
    List<String> neighborhoods,
    String catastrophicZone,
    String tevZone,
    String fhmZone,
    boolean valid
) {}

// dto de request para POST /validate
public record ValidateZipCodeRequest(@NotBlank String zipCode) {}

// dto de response para POST /validate
public record ZipCodeValidationResponse(boolean valid, String zipCode) {}
```

### Manejo de errores

El controller captura `ZipCodeNotFoundException` y retorna HTTP 404 con el cuerpo estándar de error:
`{ "error": "Zip code not found", "code": "ZIP_CODE_NOT_FOUND" }`.

Alternativamente, puede manejarse en un `@RestControllerAdvice` global.

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.zipcode/
├── domain/
│   ├── model/
│   │   ├── ZipCode.java                                    ← record puro
│   │   └── ZipCodeValidationResult.java                   ← record puro
│   ├── exception/
│   │   └── ZipCodeNotFoundException.java                  ← excepción de dominio
│   └── port/
│       ├── in/
│       │   ├── GetZipCodeUseCase.java                     ← input port
│       │   └── ValidateZipCodeUseCase.java                ← input port
│       └── out/
│           └── ZipCodeRepository.java                     ← output port
├── application/
│   └── usecase/
│       ├── GetZipCodeUseCaseImpl.java                     ← sin @Service, wired via @Bean
│       └── ValidateZipCodeUseCaseImpl.java                ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── ZipCodeApi.java                    ← @Tag, @Operation
    │   │       ├── ZipCodeController.java                 ← implements ZipCodeApi
    │   │       ├── dto/
    │   │       │   ├── ZipCodeResponse.java
    │   │       │   ├── ValidateZipCodeRequest.java
    │   │       │   └── ZipCodeValidationResponse.java
    │   │       └── mapper/
    │   │           └── ZipCodeRestMapper.java
    │   └── out/
    │       └── persistence/
    │           ├── entity/
    │           │   ├── ZipCodeJpa.java
    │           │   └── ZipCodeNeighborhoodJpa.java
    │           ├── ZipCodeJpaRepository.java              ← Spring Data JPA
    │           └── ZipCodePersistenceAdapter.java         ← implementa ZipCodeRepository
    └── config/
        └── ZipCodeConfig.java                             ← @Bean wiring
```

### Migraciones Flyway

```
src/main/resources/db/migration/
├── V1__create_folio_sequence.sql   ← ya existe
├── V2__create_zip_codes.sql        ← DDL de zip_codes y zip_code_neighborhoods
└── V3__seed_zip_codes.sql          ← datos seed representativos (≥5 CPs, distintos estados y zonas)
```

**Datos seed mínimos para `V3__seed_zip_codes.sql`:**

| zip_code | state | municipality | city | catastrophic_zone | tev_zone | fhm_zone |
|----------|-------|-------------|------|-------------------|----------|----------|
| 06600 | Ciudad de México | Cuauhtémoc | Ciudad de México | ZONE_A | TEV-1 | FHM-2 |
| 44100 | Jalisco | Guadalajara | Guadalajara | ZONE_B | TEV-2 | FHM-1 |
| 64000 | Nuevo León | Monterrey | Monterrey | ZONE_C | TEV-3 | FHM-3 |
| 72000 | Puebla | Puebla | Puebla | ZONE_B | TEV-2 | FHM-2 |
| 20000 | Aguascalientes | Aguascalientes | Aguascalientes | ZONE_D | TEV-4 | FHM-1 |

Colonias de ejemplo para `06600`: `Juárez`, `Tabacalera`.

---

## 3. LISTA DE TAREAS

### Base de Datos

- [ ] **TASK-1** Crear migración `V2__create_zip_codes.sql` — DDL de las tablas `zip_codes` y `zip_code_neighborhoods` con índice en FK
- [ ] **TASK-2** Crear migración `V3__seed_zip_codes.sql` — datos seed de al menos 5 CPs representativos con colonias para `06600`

### Backend — Dominio

- [ ] **TASK-3** Crear domain model `ZipCode` (record Java, sin anotaciones)
- [ ] **TASK-4** Crear domain model `ZipCodeValidationResult` (record Java)
- [ ] **TASK-5** Crear excepción de dominio `ZipCodeNotFoundException`
- [ ] **TASK-6** Crear output port `ZipCodeRepository` con método `findByZipCode(String): Optional<ZipCode>`
- [ ] **TASK-7** Crear input port `GetZipCodeUseCase` con método `getByZipCode(String): ZipCode`
- [ ] **TASK-8** Crear input port `ValidateZipCodeUseCase` con método `validate(String): ZipCodeValidationResult`

### Backend — Aplicación

- [ ] **TASK-9** Implementar `GetZipCodeUseCaseImpl` — llama a `ZipCodeRepository.findByZipCode`, lanza `ZipCodeNotFoundException` si no existe
- [ ] **TASK-10** Implementar `ValidateZipCodeUseCaseImpl` — llama a `ZipCodeRepository.findByZipCode`, retorna `ZipCodeValidationResult(valid, zipCode)`

### Backend — Persistencia

- [ ] **TASK-11** Crear entidad JPA `ZipCodeJpa` (tabla `zip_codes`) con relación `@OneToMany` a `ZipCodeNeighborhoodJpa`
- [ ] **TASK-12** Crear entidad JPA `ZipCodeNeighborhoodJpa` (tabla `zip_code_neighborhoods`) con `@ManyToOne` a `ZipCodeJpa`
- [ ] **TASK-13** Crear `ZipCodeJpaRepository` con método `findByZipCodeWithNeighborhoods` usando `JOIN FETCH`
- [ ] **TASK-14** Implementar `ZipCodePersistenceAdapter` — implementa `ZipCodeRepository`, mapea `ZipCodeJpa` → `ZipCode`

### Backend — REST

- [ ] **TASK-15** Crear DTOs REST: `ZipCodeResponse`, `ValidateZipCodeRequest`, `ZipCodeValidationResponse`
- [ ] **TASK-16** Crear `ZipCodeRestMapper` — mapea `ZipCode` → `ZipCodeResponse` y `ZipCodeValidationResult` → `ZipCodeValidationResponse`
- [ ] **TASK-17** Crear interfaz Swagger `ZipCodeApi` (en `rest/swaggerdocs/`)
- [ ] **TASK-18** Crear `ZipCodeController` — implementa `ZipCodeApi`, captura `ZipCodeNotFoundException` → 404

### Backend — Configuración

- [ ] **TASK-19** Crear `ZipCodeConfig` — registra `GetZipCodeUseCaseImpl` y `ValidateZipCodeUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-20** Test unitario `GetZipCodeUseCaseImplTest` — CP existente retorna `ZipCode`, CP inexistente lanza `ZipCodeNotFoundException`
- [ ] **TASK-21** Test unitario `ValidateZipCodeUseCaseImplTest` — CP existente retorna `valid: true`, CP inexistente retorna `valid: false`
- [ ] **TASK-22** Test unitario `ZipCodePersistenceAdapterTest` — verifica mapeo `ZipCodeJpa` → `ZipCode` (con y sin colonias)
- [ ] **TASK-23** Test unitario `ZipCodeRestMapperTest` — verifica mapeo `ZipCode` → `ZipCodeResponse` y `ZipCodeValidationResult` → `ZipCodeValidationResponse`
- [ ] **TASK-24** Test unitario `ZipCodeControllerTest` — GET 200, GET 404, POST validate 200 (valid/invalid)
- [ ] **TASK-25** Test de integración `ZipCodeIntegrationTest` (`@SpringBootTest` + `@Testcontainers`) — GET CP existente, GET CP inexistente 404, POST validate true, POST validate false

### QA

- [ ] **TASK-26** Ejecutar `/risk-identifier` para generar matriz de riesgos `zip-codes-risks.md`
- [ ] **TASK-27** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `zip-codes-gherkin.md`
- [ ] **TASK-28** Validar endpoints manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
