---
id: SPEC-009
status: DRAFT
feature: observability-core
created: 2026-04-27
updated: 2026-04-27
author: spec-generator
version: "1.0"
related-specs: []
---

# SPEC-009 — Observabilidad completa (3 pilares) para Insurance-Quoter-Core

## 1. REQUERIMIENTOS

### Historia de usuario

**HU-01 — Logs estructurados con correlación de trazas**
> Como operador de plataforma, quiero que cada línea de log del Core incluya `traceId` y `spanId` en formato JSON estructurado, para poder correlacionar logs con trazas distribuidas en Jaeger.

**HU-02 — Métricas de negocio y sistema**
> Como SRE, quiero métricas de JVM, HTTP, pool de conexiones y operaciones de negocio expuestas en `/actuator/prometheus`, para alertar sobre degradación del servicio y auditar el volumen de operaciones.

**HU-03 — Trazas distribuidas continuas**
> Como desarrollador, quiero que el Core continúe la traza iniciada por Insurance-Quoter-Back (header `traceparent`) sin crear un nuevo root trace, y que cada use case genere su propio span mediante `@Observed`, para tener visibilidad end-to-end de cada request.

---

### Criterios de aceptación (Gherkin)

#### HU-01 — Logs estructurados

```gherkin
Escenario: Log de request HTTP incluye traceId y spanId
  Dado que Insurance-Quoter-Back llama a GET /v1/folios con header traceparent
  Cuando el Core procesa la solicitud
  Entonces cada línea de log de esa request contiene traceId no vacío
  Y cada línea de log contiene spanId no vacío
  Y el formato del log es JSON estructurado (Logstash/ECS)

Escenario: Log sin traceparent genera su propio traceId
  Dado que un cliente llama directamente al Core sin header traceparent
  Cuando el Core procesa la solicitud
  Entonces el log contiene un traceId generado localmente
  Y spanId no es vacío
```

#### HU-02 — Métricas de negocio

```gherkin
Escenario: Contador de folios generados
  Dado que /actuator/prometheus está habilitado
  Cuando se realiza un GET /v1/folios exitoso
  Entonces la métrica folios_generated_total se incrementa en 1

Escenario: Contador y timer de consultas de tarifas
  Dado que /actuator/prometheus está habilitado
  Cuando se realiza un GET /v1/tariffs con parámetro fireKey="RC001"
  Entonces tariff_lookups_total{fireKey="RC001"} se incrementa en 1
  Y tariff_lookup_duration_seconds{fireKey="RC001"} registra la duración

Escenario: Contador de búsquedas de código postal — encontrado
  Dado que /actuator/prometheus está habilitado
  Cuando se realiza un GET /v1/zip-codes/06600 y el código existe
  Entonces zipcode_lookups_total{found="true"} se incrementa en 1

Escenario: Contador de búsquedas de código postal — no encontrado
  Dado que /actuator/prometheus está habilitado
  Cuando se realiza un GET /v1/zip-codes/99999 y el código no existe
  Entonces zipcode_lookups_total{found="false"} se incrementa en 1

Escenario: Contadores de agentes y suscriptores
  Dado que /actuator/prometheus está habilitado
  Cuando se realiza un GET /v1/agents exitoso
  Entonces agent_queries_total se incrementa en 1
  Cuando se realiza un GET /v1/subscribers exitoso
  Entonces subscriber_queries_total se incrementa en 1

Escenario: Contador de errores de catálogo
  Dado que /actuator/prometheus está habilitado
  Cuando una operación de catálogo lanza una excepción de tipo "DATABASE_ERROR"
  Entonces catalog_errors_total{errorType="DATABASE_ERROR"} se incrementa en 1

Escenario: Métricas JVM y HTTP disponibles
  Dado que actuator está configurado
  Cuando se consulta /actuator/prometheus
  Entonces la respuesta contiene jvm_memory_used_bytes
  Y contiene http_server_requests_seconds_count
  Y contiene hikaricp_connections_active
```

#### HU-03 — Trazas distribuidas

```gherkin
Escenario: Core continúa traza de Back — no crea root
  Dado que Insurance-Quoter-Back envía request con header traceparent="00-<traceId>-<spanId>-01"
  Cuando el Core recibe la request
  Entonces el span creado por el Core tiene el mismo traceId de Back
  Y el span padre es el spanId enviado por Back
  Y Jaeger muestra ambos servicios bajo un mismo trace

Escenario: Use case genera span propio vía @Observed
  Dado que el Core recibe un request para GET /v1/folios
  Cuando GenerateFolioUseCaseImpl ejecuta
  Entonces Jaeger muestra un span con nombre "GenerateFolioUseCaseImpl#execute" (o similar)
  Y el span es hijo del span HTTP server

Escenario: Spans JDBC registrados automáticamente
  Dado que un use case ejecuta una query a PostgreSQL
  Cuando el Core procesa la solicitud
  Entonces Jaeger muestra un span db.postgresql con la query ejecutada
```

---

### Reglas de negocio

1. **Propagación de traza obligatoria**: El Core NO debe crear un root trace si el header `traceparent` (W3C Trace Context) está presente. Spring Boot 4 + `micrometer-tracing-bridge-otel` lo resuelve automáticamente; no se requiere código adicional.
2. **`@Observed` solo en use case implementations**: No anotar interfaces, controllers, ni adapters de persistencia.
3. **Tags de métricas bajos cardinalidad**: Los tags `fireKey` y `errorType` deben provenir de enumeraciones o valores controlados, nunca de input libre del usuario.
4. **Un solo Jaeger para ambos servicios**: El Core apunta a `localhost:4317` (OTLP gRPC) — el mismo Jaeger que usa Insurance-Quoter-Back. No se despliega un Jaeger separado.
5. **Prometheus scrape independiente**: El Prometheus del Core tiene `job_name: insurance-quoter-core` apuntando a `localhost:8081/actuator/prometheus`.
6. **Endpoints actuator mínimos expuestos**: `health`, `info`, `prometheus`, `metrics`. El endpoint `beans` y `env` NO se exponen en producción.

---

## 2. DISEÑO

### 2.1 Dependencias — `build.gradle.kts`

Agregar al bloque `dependencies`:

```kotlin
// Observabilidad
implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-registry-prometheus")
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")
runtimeOnly("io.micrometer:micrometer-observation")
```

> **Nota**: Con Spring Boot 4 BOM, no se especifican versiones explícitas para micrometer ni opentelemetry — el BOM los gestiona.

---

### 2.2 Configuración — `application.properties`

```properties
# ── Actuator ──────────────────────────────────────────────────────────────
management.endpoints.web.exposure.include=health,info,prometheus,metrics
management.endpoint.health.show-details=always
management.metrics.tags.application=${spring.application.name}
management.metrics.tags.environment=${spring.profiles.active:local}

# ── Tracing ───────────────────────────────────────────────────────────────
management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://localhost:4317
management.otlp.tracing.compression=gzip

# ── OTLP Metrics (opcional, si se quiere enviar métricas también a OTel collector)
# management.otlp.metrics.export.url=http://localhost:4318/v1/metrics

# ── Logging con traceId/spanId ─────────────────────────────────────────────
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

---

### 2.3 Logs estructurados — `logback-spring.xml`

Archivo: `src/main/resources/logback-spring.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

  <springProperty scope="context" name="appName" source="spring.application.name"/>

  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <customFields>{"service":"${appName}"}</customFields>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="CONSOLE"/>
  </root>

  <logger name="com.sofka.insurancequoter" level="DEBUG"/>
</configuration>
```

**Dependencia adicional para Logstash encoder:**
```kotlin
implementation("net.logstash.logback:logstash-logback-encoder:8.0")
```

> El encoder `LogstashEncoder` serializa cada log como JSON e inyecta automáticamente `traceId` y `spanId` desde el MDC cuando `micrometer-tracing` está en classpath.

---

### 2.4 Configuración de observabilidad — `ObservabilityConfig.java`

Ubicación: `infrastructure/config/ObservabilityConfig.java`

```java
@Configuration
@EnableObservability  // habilita @Observed AOP
public class ObservabilityConfig {

    @Bean
    ObservedAspect observedAspect(ObservationRegistry registry) {
        return new ObservedAspect(registry);
    }
}
```

> `@EnableObservability` es la anotación de Spring Boot 4 que activa el soporte AOP para `@Observed`. Sin esto, la anotación no genera spans.

**Dependencia AOP requerida:**
```kotlin
implementation("org.springframework.boot:spring-boot-starter-aop")
```

---

### 2.5 `@Observed` en use cases

Agregar `@Observed` en cada use case implementation. La anotación va sobre la **clase** para interceptar todos sus métodos públicos.

| Use case | Bounded context | Span name sugerido |
|---|---|---|
| `GenerateFolioUseCaseImpl` | folio | `folio.generate` |
| `GetAgentsUseCaseImpl` | agent | `agent.get-all` |
| `GetBusinessLinesUseCaseImpl` | businessLine | `business-line.get-all` |
| `GetGuaranteesUseCaseImpl` | guarantee | `guarantee.get-all` |
| `GetRiskClassificationsUseCaseImpl` | riskClassification | `risk-classification.get-all` |
| `GetSubscribersUseCaseImpl` | subscriber | `subscriber.get-all` |
| `GetTariffsUseCaseImpl` | tariff | `tariff.get-all` |
| `UpdateTariffsUseCaseImpl` | tariff | `tariff.update` |
| `GetZipCodeUseCaseImpl` | zipcode | `zipcode.get` |
| `ValidateZipCodeUseCaseImpl` | zipcode | `zipcode.validate` |

Ejemplo de aplicación:

```java
@Observed(name = "folio.generate", contextualName = "generate-folio")
public class GenerateFolioUseCaseImpl implements GenerateFolioUseCase {
    // sin cambios en lógica
}
```

---

### 2.6 Métricas custom — diseño de clases

#### Estructura de paquetes

```
infrastructure/metrics/
├── FolioMetrics.java
├── TariffMetrics.java
├── ZipCodeMetrics.java
├── AgentMetrics.java
├── SubscriberMetrics.java
└── CatalogErrorMetrics.java
```

Cada clase es un `@Component` que recibe `MeterRegistry` por constructor y registra sus contadores/timers en `@PostConstruct`.

#### `FolioMetrics.java`

```java
@Component
public class FolioMetrics {
    private final Counter foliosGenerated;

    public FolioMetrics(MeterRegistry registry) {
        this.foliosGenerated = Counter.builder("folios_generated_total")
            .description("Folios generados exitosamente")
            .register(registry);
    }

    public void recordFolioGenerated() {
        foliosGenerated.increment();
    }
}
```

#### `TariffMetrics.java`

```java
@Component
public class TariffMetrics {
    private final MeterRegistry registry;

    public TariffMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordLookup(String fireKey) {
        Counter.builder("tariff_lookups_total")
            .tag("fireKey", fireKey)
            .description("Consultas de tarifas por clave de incendio")
            .register(registry)
            .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(registry);
    }

    public void stopTimer(Timer.Sample sample, String fireKey) {
        sample.stop(Timer.builder("tariff_lookup_duration_seconds")
            .tag("fireKey", fireKey)
            .description("Duración de consultas de tarifas")
            .register(registry));
    }
}
```

#### `ZipCodeMetrics.java`

```java
@Component
public class ZipCodeMetrics {
    private final MeterRegistry registry;

    public ZipCodeMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordLookup(boolean found) {
        Counter.builder("zipcode_lookups_total")
            .tag("found", String.valueOf(found))
            .description("Búsquedas de código postal")
            .register(registry)
            .increment();
    }
}
```

#### `AgentMetrics.java`

```java
@Component
public class AgentMetrics {
    private final Counter agentQueries;

    public AgentMetrics(MeterRegistry registry) {
        this.agentQueries = Counter.builder("agent_queries_total")
            .description("Consultas de agentes")
            .register(registry);
    }

    public void recordQuery() {
        agentQueries.increment();
    }
}
```

#### `SubscriberMetrics.java`

```java
@Component
public class SubscriberMetrics {
    private final Counter subscriberQueries;

    public SubscriberMetrics(MeterRegistry registry) {
        this.subscriberQueries = Counter.builder("subscriber_queries_total")
            .description("Consultas de suscriptores")
            .register(registry);
    }

    public void recordQuery() {
        subscriberQueries.increment();
    }
}
```

#### `CatalogErrorMetrics.java`

```java
@Component
public class CatalogErrorMetrics {
    private final MeterRegistry registry;

    public CatalogErrorMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordError(String errorType) {
        Counter.builder("catalog_errors_total")
            .tag("errorType", errorType)
            .description("Errores en operaciones de catálogo")
            .register(registry)
            .increment();
    }
}
```

---

### 2.7 Inyección de métricas en controladores/use cases

Las métricas custom se inyectan en los controllers (para `folios_generated_total`, `agent_queries_total`, `subscriber_queries_total`, `zipcode_lookups_total`) o en los use case implementations (para `tariff_lookups_total`, `tariff_lookup_duration_seconds`).

| Métrica | Punto de inyección | Justificación |
|---|---|---|
| `folios_generated_total` | `FolioController` (en respuesta 200) | Solo cuenta éxitos HTTP |
| `tariff_lookups_total` / `tariff_lookup_duration_seconds` | `GetTariffsUseCaseImpl` | Necesita acceso al fireKey de dominio |
| `zipcode_lookups_total` | `GetZipCodeUseCaseImpl` | Conoce si encontró o no el resultado |
| `agent_queries_total` | `AgentController` | Simple contador por request exitoso |
| `subscriber_queries_total` | `SubscriberController` | Simple contador por request exitoso |
| `catalog_errors_total` | Global exception handler (`@RestControllerAdvice`) | Captura todos los errores de catálogo |

---

### 2.8 `compose.yaml` — servicios de observabilidad

Agregar al `compose.yaml` existente (que ya tiene `postgres`):

```yaml
  jaeger:
    image: jaegertracing/all-in-one:1.57
    environment:
      - COLLECTOR_OTLP_ENABLED=true
    ports:
      - "16686:16686"   # Jaeger UI
      - "4317:4317"     # OTLP gRPC
      - "4318:4318"     # OTLP HTTP
    networks:
      - observability

  prometheus:
    image: prom/prometheus:v2.51.0
    volumes:
      - ./observability/prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports:
      - "9091:9090"     # Puerto 9091 para no colisionar con Back (9090)
    networks:
      - observability

  grafana:
    image: grafana/grafana:10.4.0
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    ports:
      - "3001:3000"     # Puerto 3001 para no colisionar con Back (3000)
    depends_on:
      - prometheus
    networks:
      - observability

networks:
  observability:
    driver: bridge
```

> **Nota de puertos**: Jaeger usa los mismos puertos (16686, 4317, 4318) que en Insurance-Quoter-Back. Si ambos `compose.yaml` se ejecutan simultáneamente en la misma máquina, los puertos de Jaeger colisionarán. La solución recomendada es ejecutar Jaeger solo desde uno de los dos proyectos (preferentemente Back) y que Core se conecte a ese mismo Jaeger. Documentar en README.

---

### 2.9 `observability/prometheus.yml`

Archivo: `observability/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: insurance-quoter-core
    metrics_path: /actuator/prometheus
    static_configs:
      - targets:
          - host.docker.internal:8081
```

> `host.docker.internal` permite que Prometheus dentro de Docker acceda a la aplicación Java corriendo en el host.

---

### 2.10 Endpoints actuator resultantes

| Endpoint | Descripción |
|---|---|
| `GET /actuator/health` | Estado del servicio y DB |
| `GET /actuator/metrics` | Lista de métricas disponibles |
| `GET /actuator/prometheus` | Scrape endpoint para Prometheus |
| `GET /actuator/info` | Info de la aplicación |

---

## 3. LISTA DE TAREAS

### Backend

#### Fase 1 — Infraestructura base
- [ ] **T-001** Agregar dependencias de observabilidad en `build.gradle.kts`: `actuator`, `micrometer-registry-prometheus`, `micrometer-tracing-bridge-otel`, `opentelemetry-exporter-otlp`, `logstash-logback-encoder:8.0`, `spring-boot-starter-aop`
- [ ] **T-002** Crear `src/main/resources/logback-spring.xml` con `LogstashEncoder` (JSON estructurado + traceId/spanId en MDC)
- [ ] **T-003** Configurar `application.properties`: actuator endpoints, tracing sampling=1.0, OTLP endpoint, logging pattern
- [ ] **T-004** Crear `ObservabilityConfig.java` en `infrastructure/config/` con `@EnableObservability` y bean `ObservedAspect`

#### Fase 2 — `@Observed` en use cases
- [ ] **T-005** Agregar `@Observed(name="folio.generate")` en `GenerateFolioUseCaseImpl`
- [ ] **T-006** Agregar `@Observed(name="agent.get-all")` en `GetAgentsUseCaseImpl`
- [ ] **T-007** Agregar `@Observed(name="business-line.get-all")` en `GetBusinessLinesUseCaseImpl`
- [ ] **T-008** Agregar `@Observed(name="guarantee.get-all")` en `GetGuaranteesUseCaseImpl`
- [ ] **T-009** Agregar `@Observed(name="risk-classification.get-all")` en `GetRiskClassificationsUseCaseImpl`
- [ ] **T-010** Agregar `@Observed(name="subscriber.get-all")` en `GetSubscribersUseCaseImpl`
- [ ] **T-011** Agregar `@Observed(name="tariff.get-all")` en `GetTariffsUseCaseImpl`
- [ ] **T-012** Agregar `@Observed(name="tariff.update")` en `UpdateTariffsUseCaseImpl`
- [ ] **T-013** Agregar `@Observed(name="zipcode.get")` en `GetZipCodeUseCaseImpl`
- [ ] **T-014** Agregar `@Observed(name="zipcode.validate")` en `ValidateZipCodeUseCaseImpl`

#### Fase 3 — Métricas custom
- [ ] **T-015** Crear `FolioMetrics.java` en `infrastructure/metrics/` con counter `folios_generated_total`; inyectar en `FolioController`
- [ ] **T-016** Crear `TariffMetrics.java` con counter `tariff_lookups_total{fireKey}` y timer `tariff_lookup_duration_seconds{fireKey}`; inyectar en `GetTariffsUseCaseImpl`
- [ ] **T-017** Crear `ZipCodeMetrics.java` con counter `zipcode_lookups_total{found}`; inyectar en `GetZipCodeUseCaseImpl`
- [ ] **T-018** Crear `AgentMetrics.java` con counter `agent_queries_total`; inyectar en `AgentController`
- [ ] **T-019** Crear `SubscriberMetrics.java` con counter `subscriber_queries_total`; inyectar en `SubscriberController`
- [ ] **T-020** Crear `CatalogErrorMetrics.java` con counter `catalog_errors_total{errorType}`; inyectar en `@RestControllerAdvice` global

#### Fase 4 — Docker Compose e infraestructura
- [ ] **T-021** Agregar servicios `jaeger`, `prometheus`, `grafana` al `compose.yaml` existente con puertos diferenciados (Prometheus: 9091, Grafana: 3001)
- [ ] **T-022** Crear `observability/prometheus.yml` con `job_name: insurance-quoter-core` apuntando a `host.docker.internal:8081/actuator/prometheus`

#### Fase 5 — Tests unitarios (TDD)
- [ ] **T-023** Test unitario para `FolioMetrics`: verificar que `recordFolioGenerated()` incrementa el counter
- [ ] **T-024** Test unitario para `TariffMetrics`: verificar counter por fireKey y que el timer registra duración
- [ ] **T-025** Test unitario para `ZipCodeMetrics`: verificar counter con tags `found=true` y `found=false`
- [ ] **T-026** Test unitario para `AgentMetrics`: verificar counter
- [ ] **T-027** Test unitario para `SubscriberMetrics`: verificar counter
- [ ] **T-028** Test unitario para `CatalogErrorMetrics`: verificar counter por errorType
- [ ] **T-029** Test de integración (`@SpringBootTest`): verificar que `/actuator/prometheus` devuelve 200 y contiene `jvm_memory_used_bytes`, `http_server_requests_seconds`, `hikaricp_connections_active`
- [ ] **T-030** Test de integración: verificar que `ObservabilityConfig` registra `ObservedAspect` en el contexto de Spring

### Frontend
> No aplica — este feature es exclusivamente backend.

### QA / Validación manual
- [ ] Arrancar la app y verificar que `/actuator/health` devuelve `{"status":"UP"}`
- [ ] Verificar que `/actuator/prometheus` expone métricas JVM, HTTP y custom
- [ ] Ejecutar GET /v1/folios y confirmar `folios_generated_total` incrementa en Prometheus
- [ ] Ejecutar GET /v1/tariffs con `fireKey` y confirmar counter + timer en Prometheus
- [ ] Ejecutar GET /v1/zip-codes/{code} con código existente y no existente; confirmar tags `found=true/false`
- [ ] Confirmar en Jaeger UI (http://localhost:16686) que el servicio `insurance-quoter-core` aparece
- [ ] Enviar request con header `traceparent` de Back y verificar en Jaeger que Core continúa la misma traza (mismo traceId)
- [ ] Verificar en logs JSON que `traceId` y `spanId` están presentes y no vacíos
