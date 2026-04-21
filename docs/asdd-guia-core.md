# Guía ASDD — Insurance-Quoter-Core (plataforma-core-ohs)

> Esta guía es exclusiva para el microservicio **`Insurance-Quoter-Core/`** (plataforma-core-ohs, puerto 8081, DB `insurance_core_db` :5433).  
> Para el quoter principal ver `docs/asdd-guia-backend.md`.

El Core provee catálogos, tarifas y generación de folios que consume el Back. **Debe implementarse primero.**

**Prerequisito:** tener Claude Code corriendo en la raíz del proyecto (`Sofka-IQ/`).

---

## Flujo ASDD por feature

```
[FASE 1 — Secuencial]
  /generate-spec <feature>
  → usuario revisa y aprueba (status: APPROVED)

[FASE 2 — Implementación paralela]
  backend-developer  ∥  database-agent (si hay modelos nuevos)

[FASE 3 — Tests paralelos]
  test-engineer-backend

[FASE 4 — QA]
  qa-agent
  └── /gherkin-case-generator  → test cases funcionales (Gherkin)
  └── /risk-identifier         → matriz de riesgos ASD
  └── /performance-analyzer    → si hay SLAs definidos
```

> **Atajo:** `/asdd-orchestrate <feature>` corre las 4 fases automáticamente.  
> **Nunca saltar Fase 1** — sin spec `APPROVED` no hay implementación.

---

## Fase 1 — Generar y aprobar la spec

### Comando
```
/generate-spec <nombre-feature>
```

### Aprobación
Cuando `/generate-spec` crea `.claude/specs/<feature>.spec.md`, revisar y cambiar:

```yaml
status: APPROVED
updated: 2026-04-XX
```

---

## Fase 2 — Implementación

### Features a implementar

| # | Feature | Endpoints | Estrategia de datos |
|---|---------|-----------|---------------------|
| 1 | `folio-generator` | GET /v1/folios | Secuencia en DB |
| 2 | `subscribers-catalog` | GET /v1/subscribers | Fixtures JSON |
| 3 | `agents-catalog` | GET /v1/agents | Fixtures JSON |
| 4 | `business-lines-catalog` | GET /v1/business-lines | Fixtures JSON |
| 5 | `zip-codes` | GET /v1/zip-codes/{zipCode}, POST /v1/zip-codes/validate | DB con migración |
| 6 | `risk-classification-catalog` | GET /v1/catalogs/risk-classification | Fixtures JSON |
| 7 | `guarantees-catalog` | GET /v1/catalogs/guarantees | Fixtures JSON |
| 8 | `tariffs` | GET /v1/tariffs | Fixtures JSON |

> **Estrategia fixtures JSON:** datos estáticos en `src/main/resources/fixtures/<feature>.json`, cargados por un adapter que implementa el output port. Permite reemplazar por DB o API externa sin tocar use cases.

### Comando de implementación
```
/implement-backend <nombre-feature>
```

### Swagger / OpenAPI (obligatorio en todo endpoint nuevo)

Las anotaciones Swagger **nunca van en el controller**. Se declaran en una interfaz dentro de `rest/swaggerdocs/`. El controller implementa esa interfaz y queda limpio.

```
infrastructure/adapter/in/rest/
├── swaggerdocs/
│   └── FolioGeneratorApi.java   ← @Tag, @Operation, @ApiResponse aquí
└── FolioGeneratorController.java ← implements FolioGeneratorApi, sin anotaciones Swagger
```

```java
// swaggerdocs/SubscribersApi.java
@Tag(name = "Subscribers", description = "Catálogo de suscriptores")
@RequestMapping("/v1/subscribers")
public interface SubscribersApi {

    @Operation(summary = "Listar todos los suscriptores")
    @ApiResponse(responseCode = "200", description = "Lista de suscriptores")
    @GetMapping
    ResponseEntity<SubscribersResponse> getSubscribers();
}

// SubscribersController.java
@RestController
@RequiredArgsConstructor
public class SubscribersController implements SubscribersApi {
    private final GetSubscribersUseCase getSubscribersUseCase;

    @Override
    public ResponseEntity<SubscribersResponse> getSubscribers() {
        return ResponseEntity.ok(getSubscribersUseCase.getAll());
    }
}
```

Swagger UI: `http://localhost:8081/swagger-ui/index.html`

> Ver patrón completo y anti-patrones en `.claude/rules/backend.md` — sección "OpenAPI / Swagger".

---

## Prompts por feature

### Feature 1: Generador de folios

```
/generate-spec folio-generator

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081, DB :5433).
Feature: Generación de números de folio secuenciales.

Endpoint: GET /v1/folios

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "folioNumber": "FOL-2026-00043",
  "generatedAt": "2026-04-20T14:30:00Z"
}

Reglas de negocio:
- El número de folio sigue el patrón FOL-<año>-<secuencia 5 dígitos con padding cero>.
- La secuencia es global (no por suscriptor): se incrementa con cada llamada.
- La secuencia debe ser consistente bajo concurrencia — usar secuencia de base de datos (PostgreSQL SEQUENCE).
- generatedAt debe ser el timestamp UTC del momento de generación.

Entidades JPA: no se almacena el folio aquí, solo la secuencia PostgreSQL.
Output port: FolioSequencePort con método nextFolioNumber(): String
```

### Feature 2: Catálogo de suscriptores

```
/generate-spec subscribers-catalog

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Catálogo de suscriptores disponibles.

Endpoint: GET /v1/subscribers

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "subscribers": [
    { "id": "SUB-001", "name": "Seguros Sofka" },
    { "id": "SUB-002", "name": "Aseguradora Norte" }
  ]
}

Estrategia: datos estáticos en src/main/resources/fixtures/subscribers.json.
El adapter lee el JSON al inicio y lo sirve desde memoria (no hay escritura).

Output port: SubscriberRepository con método findAll(): List<Subscriber>
Domain model: Subscriber (id, name) — POJO sin anotaciones JPA.
```

### Feature 3: Catálogo de agentes

```
/generate-spec agents-catalog

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Catálogo de agentes disponibles.

Endpoint: GET /v1/agents

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "agents": [
    { "code": "AGT-123", "name": "Juan Pérez", "subscriberId": "SUB-001" }
  ]
}

Estrategia: datos estáticos en src/main/resources/fixtures/agents.json.
El adapter lee el JSON al inicio y lo sirve desde memoria.

Output port: AgentRepository con métodos findAll(): List<Agent> y existsByCode(String code): boolean
Domain model: Agent (code, name, subscriberId) — POJO sin anotaciones JPA.

Nota: existsByCode es necesario porque Insurance-Quoter-Back lo usará para validar agentes al crear folios.
```

### Feature 4: Catálogo de giros de negocio

```
/generate-spec business-lines-catalog

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Catálogo de giros de negocio (businessLine) con su clave incendio.

Endpoint: GET /v1/business-lines

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "businessLines": [
    { "code": "BL-001", "description": "Bodega de mercancías", "fireKey": "FK-INC-01" },
    { "code": "BL-002", "description": "Oficina administrativa", "fireKey": "FK-INC-02" }
  ]
}

Estrategia: datos estáticos en src/main/resources/fixtures/business-lines.json.

Output port: BusinessLineRepository con método findAll(): List<BusinessLine>
Domain model: BusinessLine (code, description, fireKey) — POJO sin anotaciones JPA.
```

### Feature 5: Códigos postales

```
/generate-spec zip-codes

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081, DB :5433).
Feature: Consulta y validación de códigos postales con sus zonas catastróficas.

Endpoints:
- GET /v1/zip-codes/{zipCode}
- POST /v1/zip-codes/validate

Contratos (docs/api-contracts.md, sección 8):

GET response 200:
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
GET response 404: { "error": "Zip code not found", "code": "ZIP_CODE_NOT_FOUND" }

POST /v1/zip-codes/validate
Request: { "zipCode": "06600" }
Response 200: { "valid": true, "zipCode": "06600" }

Estrategia: persistir en tabla zip_codes en DB. Cargar datos iniciales con Flyway desde
src/main/resources/db/migration/V2__seed_zip_codes.sql (datos de ejemplo representativos).

Entidades JPA: ZipCodeJpa (tabla: zip_codes, PK: zip_code varchar).
Columnas: zip_code, state, municipality, city, catastrophic_zone, tev_zone, fhm_zone.
Neighborhoods como array PostgreSQL o tabla separada zip_code_neighborhoods.

Domain model: ZipCode (zipCode, state, municipality, city, neighborhoods, catastrophicZone, tevZone, fhmZone) — POJO.
Output port: ZipCodeRepository con findByZipCode(String): Optional<ZipCode>
```

### Feature 6: Catálogo de clasificación de riesgo

```
/generate-spec risk-classification-catalog

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Catálogo de clasificaciones de riesgo para suscripción.

Endpoint: GET /v1/catalogs/risk-classification

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "riskClassifications": [
    { "code": "STANDARD", "description": "Riesgo estándar" },
    { "code": "PREFERRED", "description": "Riesgo preferente" },
    { "code": "SUBSTANDARD", "description": "Riesgo subestándar" }
  ]
}

Estrategia: datos estáticos en src/main/resources/fixtures/risk-classifications.json.

Output port: RiskClassificationRepository con findAll(): List<RiskClassification>
Domain model: RiskClassification (code, description) — POJO sin anotaciones JPA.
```

### Feature 7: Catálogo de garantías

```
/generate-spec guarantees-catalog

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Catálogo de garantías disponibles para cotización, con indicador de tarifabilidad.

Endpoint: GET /v1/catalogs/guarantees

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "guarantees": [
    { "code": "GUA-FIRE", "description": "Incendio edificios", "tarifable": true },
    { "code": "GUA-THEFT", "description": "Robo", "tarifable": true },
    { "code": "GUA-GLASS", "description": "Vidrios", "tarifable": true }
  ]
}

Estrategia: datos estáticos en src/main/resources/fixtures/guarantees.json.

Output port: GuaranteeRepository con findAll(): List<Guarantee> y findAllTarifable(): List<Guarantee>
Domain model: Guarantee (code, description, tarifable) — POJO sin anotaciones JPA.

Nota: findAllTarifable es relevante para el cálculo de prima en Insurance-Quoter-Back.
```

### Feature 8: Tarifas y factores técnicos

```
/generate-spec tariffs

Contexto: Insurance-Quoter-Core — Java 21 + Spring Boot 4 + Hexagonal + PostgreSQL (puerto 8081).
Feature: Consulta de tarifas y factores técnicos para el cálculo de prima.

Endpoint: GET /v1/tariffs

Contrato (docs/api-contracts.md, sección 8):
Response 200:
{
  "tariffs": {
    "fireRate": 0.0015,
    "cattevFactor": 0.0008,
    "catfhmFactor": 0.0005,
    "theftRate": 0.003,
    "electronicEquipmentRate": 0.002
  }
}

Estrategia: datos estáticos en src/main/resources/fixtures/tariffs.json.
El adapter deserializa el JSON a un objeto Tariffs y lo sirve desde memoria.

Output port: TariffRepository con findCurrent(): Tariffs
Domain model: Tariffs (fireRate, cattevFactor, catfhmFactor, theftRate, electronicEquipmentRate) — POJO.

Nota: este endpoint es crítico para Insurance-Quoter-Back en la fase de cálculo de prima.
```

---

## Fase 3 — Tests de integración

Ejecutado automáticamente en el flujo ASDD después de la Fase 2. Para invocación manual:

```
@test-engineer-backend ejecuta auditoría de tests para <feature>
```

Genera:
- Tests `@WebMvcTest` para cada controller (happy path + 404 + errores de validación)
- Tests `@SpringBootTest` para flujos de integración completos
- Para zip-codes: tests `@DataJpaTest` con base de datos embebida H2
- Reporte de cobertura — quality gate: **≥ 80% en lógica de negocio**

---

## Fase 4 — QA y generación de test plan

```
@qa-agent ejecuta QA para .claude/specs/<feature>.spec.md
```

### Qué se genera

| Artefacto | Skill | Ubicación |
|-----------|-------|-----------|
| Test cases funcionales (Gherkin) | `/gherkin-case-generator` | `docs/output/qa/<feature>-gherkin.md` |
| Matriz de riesgos ASD | `/risk-identifier` | `docs/output/qa/<feature>-risks.md` |
| Plan de performance | `/performance-analyzer` | `docs/output/qa/<feature>-performance.md` (solo si hay SLAs) |
| Propuesta de automatización | `/automation-flow-proposer` | `docs/output/qa/automation-proposal.md` (bajo pedido) |

---

## Orden recomendado de implementación del Core

```
1. folio-generator          ← lo necesita Insurance-Quoter-Back para crear folios
2. subscribers-catalog      ← validación de suscriptores en creación de folio
3. agents-catalog           ← validación de agentes en creación de folio
4. business-lines-catalog   ← necesario para location-management en el Back
5. zip-codes                ← necesario para validación de ubicaciones en el Back
6. risk-classification-catalog  ← necesario para quote-general-info en el Back
7. guarantees-catalog       ← necesario para coverage-options y cálculo en el Back
8. tariffs                  ← necesario para premium-calculation en el Back
```

---

## Comandos de referencia rápida

| Qué hacer | Comando |
|-----------|---------|
| Generar spec | `/generate-spec <feature>` |
| Implementar feature | `/implement-backend <feature>` |
| Auditar tests | `@test-engineer-backend` |
| Generar test plan (Gherkin) | `/gherkin-case-generator <feature>` |
| Identificar riesgos | `/risk-identifier <feature>` |
| QA completo (Fase 4) | `@qa-agent` |
| Orquestar flujo completo | `/asdd-orchestrate <feature>` |
| Ver estado del flujo | `/asdd-orchestrate status` |
| Crear issues en GitHub | `/tasks-to-issues <feature>` |

---

## Notas importantes

- **Core antes que Back** — todos los features del Back consumen endpoints del Core.
- **TDD es obligatorio** — el test del use case se escribe antes que el código de producción.
- **Los contratos son la fuente de verdad** — si hay divergencia entre spec y `docs/api-contracts.md`, prevalece el contrato.
- **Fixtures sobre hardcode** — nunca incrustar datos de catálogo directamente en el código; siempre en `src/main/resources/fixtures/`.
- **Swagger en swaggerdocs/** — las anotaciones `@Tag`, `@Operation`, `@ApiResponse` van en la interfaz `<Context>Api.java`, nunca en el controller.
- **Sin `@Autowired`** — usar constructor injection con `@RequiredArgsConstructor` o constructor explícito.
- **GitFlow** — cada feature en su propia rama `feature/<ticket>-<nombre>` desde `develop`.
