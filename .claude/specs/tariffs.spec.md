---
id: SPEC-008
status: DRAFT
feature: tariffs
created: 2026-04-21
updated: 2026-04-21
author: spec-generator
version: "1.0"
related-specs: ["SPEC-007"]
---

# Spec: Consulta de Tarifas y Factores Técnicos

> **Estado:** `DRAFT` → aprobar con `status: APPROVED` antes de iniciar implementación.
> **Ciclo de vida:** DRAFT → APPROVED → IN_PROGRESS → IMPLEMENTED → DEPRECATED

---

## 1. REQUERIMIENTOS

### Descripción

El microservicio `Insurance-Quoter-Core` expone un endpoint `GET /v1/tariffs` que retorna los factores técnicos y tasas de tarificación utilizados en el cálculo de prima neta. Los datos son estáticos: se cargan desde un archivo JSON al iniciar la aplicación y se sirven desde memoria. No existe escritura ni modificación de tarifas en tiempo de ejecución.

Este endpoint es **crítico** para `Insurance-Quoter-Back`: cada vez que se ejecuta el cálculo de prima (`POST /v1/quotes/{folio}/calculate`), el cotizador consume estas tarifas para calcular la cobertura de incendio, robo, equipos electrónicos y las contribuciones CATTEV y CATFHM.

A diferencia de los catálogos de entidades (garantías, giros, agentes), `Tariffs` es un objeto único — no una colección — por lo que `TariffRepository` expone `findCurrent(): Tariffs` en lugar de `findAll()`.

### Requerimiento de Negocio

> Endpoint de consulta de tarifas técnicas del microservicio core.
> `GET /v1/tariffs`. Los datos provienen de `src/main/resources/fixtures/tariffs.json`
> cargado en memoria al inicio. No hay escritura. Las tarifas son estáticas
> y gestionadas por configuración.
> El port `TariffRepository` expone `findCurrent()` que retorna el objeto `Tariffs`
> vigente. Es consumido por `Insurance-Quoter-Back` en el flujo de cálculo de prima.

### Historias de Usuario

#### HU-01: Consultar tarifas técnicas vigentes

```
Como:        sistema cliente (Insurance-Quoter-Back)
Quiero:      llamar GET /v1/tariffs y recibir los factores técnicos vigentes
Para:        calcular la prima neta de cada garantía en el flujo de cotización

Prioridad:   Alta
Estimación:  S
Dependencias: Ninguna (sin base de datos, datos en memoria)
Capa:        Backend
```

#### Criterios de Aceptación — HU-01

**Happy Path**
```gherkin
CRITERIO-1.1: Retorno de tarifas vigentes
  Dado que:  el servicio Insurance-Quoter-Core está disponible
             Y el archivo fixtures/tariffs.json contiene las tarifas configuradas
  Cuando:    se realiza GET /v1/tariffs
  Entonces:  la respuesta tiene HTTP 200
             Y el cuerpo contiene el campo "tariffs" con un objeto
             Y el objeto contiene "fireRate", "cattevFactor", "catfhmFactor",
               "theftRate" y "electronicEquipmentRate" con valores numéricos positivos
```

**Error de configuración**
```gherkin
CRITERIO-1.2: Archivo de fixtures no encontrado
  Dado que:  el archivo fixtures/tariffs.json no existe en el classpath
  Cuando:    la aplicación intenta arrancar
  Entonces:  la aplicación falla al iniciar con un mensaje de error claro
             Y NO se expone el endpoint con datos inconsistentes
```

**Integridad de valores**
```gherkin
CRITERIO-1.3: Todos los factores son valores numéricos positivos
  Dado que:  el archivo fixtures/tariffs.json contiene las tarifas configuradas
  Cuando:    se realiza GET /v1/tariffs
  Entonces:  todos los valores numéricos de "tariffs" son mayores que cero
             Y ningún campo del objeto "tariffs" es nulo
```

### Reglas de Negocio

| ID   | Regla |
|------|-------|
| RN-1 | Las tarifas son de solo lectura — no existe endpoint de creación, modificación ni eliminación. |
| RN-2 | Los datos se cargan una sola vez al iniciar la aplicación (eager loading en el adapter). |
| RN-3 | Si el archivo JSON no existe o no es parseable, la aplicación debe fallar al arrancar (fail-fast). |
| RN-4 | `Tariffs` es un objeto único, no una colección. `TariffRepository.findCurrent()` retorna siempre el mismo objeto cargado al inicio. |
| RN-5 | Todos los factores son valores decimales positivos (double). Un valor de cero o negativo no es válido para el cálculo de prima. |
| RN-6 | El endpoint no requiere autenticación — es un servicio interno consumido por `Insurance-Quoter-Back`. |
| RN-7 | Los campos representan tasas (rate) o factores multiplicadores (factor). Su semántica la interpreta el motor de cálculo en `Insurance-Quoter-Back`. |

---

## 2. DISEÑO

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

| Campo                    | Tipo   | Restricciones                                                      |
|--------------------------|--------|--------------------------------------------------------------------|
| `fireRate`               | double | Positivo — tasa de incendio edificios                              |
| `cattevFactor`           | double | Positivo — factor de contribución CATTEV                          |
| `catfhmFactor`           | double | Positivo — factor de contribución CATFHM                          |
| `theftRate`              | double | Positivo — tasa de robo                                           |
| `electronicEquipmentRate`| double | Positivo — tasa de equipos electrónicos                           |

> Se usa `double` primitivo (no `Double` boxed) para garantizar que Jackson serialice siempre un número, nunca `null`.

### Output Port

```java
// com.sofka.insurancequoter.core.tariff.domain.port.out.TariffRepository
public interface TariffRepository {
    Tariffs findCurrent();
}
```

### Input Port (Use Case)

```java
// com.sofka.insurancequoter.core.tariff.domain.port.in.GetTariffsUseCase
public interface GetTariffsUseCase {
    Tariffs getCurrent();
}
```

### Fixture — `src/main/resources/fixtures/tariffs.json`

```json
{
  "fireRate": 0.0015,
  "cattevFactor": 0.0008,
  "catfhmFactor": 0.0005,
  "theftRate": 0.003,
  "electronicEquipmentRate": 0.002
}
```

> El adapter deserializa este objeto en `Tariffs` al construirse (vía Jackson `ObjectMapper`).
> A diferencia de los catálogos de lista, el fixture es un objeto JSON (no un array).

### API Endpoint

#### `GET /v1/tariffs`

| Atributo  | Valor                          |
|-----------|--------------------------------|
| Método    | `GET`                          |
| Ruta      | `/v1/tariffs`                  |
| Auth      | Ninguna (servicio interno)     |
| Produces  | `application/json`             |

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

> No hay respuestas 4xx ni 5xx en operación normal. Los errores de configuración se manifiestan al arrancar.

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

### Arquitectura Hexagonal — estructura de paquetes

```
com.sofka.insurancequoter.core.tariff/
├── domain/
│   ├── model/
│   │   └── Tariffs.java                                  ← record puro
│   └── port/
│       ├── in/
│       │   └── GetTariffsUseCase.java                    ← input port
│       └── out/
│           └── TariffRepository.java                     ← output port
├── application/
│   └── usecase/
│       └── GetTariffsUseCaseImpl.java                    ← sin @Service, wired via @Bean
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── rest/
    │   │       ├── swaggerdocs/
    │   │       │   └── TariffApi.java                    ← @Tag, @Operation
    │   │       ├── TariffController.java                 ← implements TariffApi
    │   │       ├── dto/
    │   │       │   ├── TariffsDto.java
    │   │       │   └── TariffsResponse.java
    │   │       └── mapper/
    │   │           └── TariffRestMapper.java
    │   └── out/
    │       └── json/
    │           └── TariffJsonAdapter.java                ← carga JSON, implementa TariffRepository
    └── config/
        └── TariffConfig.java                            ← @Bean wiring
```

> Mismo patrón `out/json/` que `guarantee`, `businessline` y `agent`. La diferencia clave: el fixture es un objeto JSON, no un array, por lo que `ObjectMapper.readValue(resource.getInputStream(), Tariffs.class)`.

### Notas de Implementación

- `TariffJsonAdapter` carga el fixture al construirse. Si el archivo no existe, lanza `IllegalStateException` para activar el fail-fast de Spring.
- `findCurrent()` retorna siempre la misma instancia inmutable cargada al inicio — no requiere copia defensiva ya que `Tariffs` es un record (inmutable por definición).
- La ruta `/v1/tariffs` no usa el prefijo `/catalogs/` — las tarifas son factores técnicos del motor de cálculo, no un catálogo de opciones de usuario.

---

## 3. LISTA DE TAREAS

### Backend

- [ ] **TASK-1** Crear domain model `Tariffs` (record Java con los 5 campos `double`, sin anotaciones)
- [ ] **TASK-2** Crear output port `TariffRepository` con método `findCurrent(): Tariffs`
- [ ] **TASK-3** Crear input port `GetTariffsUseCase` con método `getCurrent(): Tariffs`
- [ ] **TASK-4** Implementar `GetTariffsUseCaseImpl` — delega a `TariffRepository.findCurrent()`
- [ ] **TASK-5** Crear `TariffJsonAdapter` — carga `fixtures/tariffs.json` vía `ObjectMapper` al construirse (objeto, no array); implementa `TariffRepository`
- [ ] **TASK-6** Crear archivo `src/main/resources/fixtures/tariffs.json` con los valores del contrato
- [ ] **TASK-7** Crear DTOs REST: `TariffsDto`, `TariffsResponse`
- [ ] **TASK-8** Crear `TariffRestMapper` — mapea `Tariffs` → `TariffsResponse`
- [ ] **TASK-9** Crear interfaz Swagger `TariffApi` (en `rest/swaggerdocs/`) con `@Tag`, `@Operation`, `@ApiResponse`
- [ ] **TASK-10** Crear `TariffController` — implementa `TariffApi`, mapea `GET /v1/tariffs`, llama `GetTariffsUseCase`
- [ ] **TASK-11** Crear `TariffConfig` — registra `TariffJsonAdapter` y `GetTariffsUseCaseImpl` como `@Bean`

### Tests (TDD — escribir antes de implementar)

- [ ] **TASK-12** Test unitario `GetTariffsUseCaseImplTest` — verifica delegación al port y retorno del objeto `Tariffs`
- [ ] **TASK-13** Test unitario `TariffJsonAdapterTest` — verifica:
  - Deserialización correcta de los 5 campos desde JSON objeto (no array)
  - `findCurrent()` retorna siempre la misma instancia
  - `IllegalStateException` al no encontrar el archivo de fixtures
  - `IllegalStateException` con JSON malformado
- [ ] **TASK-14** Test unitario `TariffRestMapperTest` — verifica mapeo `Tariffs` → `TariffsResponse` (los 5 campos con sus valores exactos)
- [ ] **TASK-15** Test unitario `TariffControllerTest` — verifica HTTP 200 y estructura del response (`tariffs` objeto con los 5 campos)
- [ ] **TASK-16** Test de integración `TariffIntegrationTest` (`@SpringBootTest`) — levanta contexto completo, llama `GET /v1/tariffs`, valida response 200 con los valores del fixture

### QA

- [ ] **TASK-17** Ejecutar `/risk-identifier` para generar matriz de riesgos `tariffs-risks.md`
- [ ] **TASK-18** Ejecutar `/gherkin-case-generator` para generar escenarios Gherkin `tariffs-gherkin.md`
- [ ] **TASK-19** Validar endpoint manualmente con curl o Swagger UI (`http://localhost:8081/swagger-ui/index.html`)
- [ ] **TASK-20** Verificar que `Insurance-Quoter-Back` consume correctamente `GET /v1/tariffs` en el flujo de cálculo de prima (revisar contrato en `docs/api-contracts.md`)
