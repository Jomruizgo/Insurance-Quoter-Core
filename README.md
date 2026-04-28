# Insurance-Quoter-Core (plataforma-core-ohs)

Microservicio de referencia del cotizador de seguros de daños. Provee catálogos, tarifas, agentes, suscriptores, códigos postales y generación de folios.

## Stack
- Java 21 + Spring Boot 4 + Spring Data JPA
- PostgreSQL (puerto **5433** en desarrollo)
- Arquitectura hexagonal (ports & adapters)
- Observabilidad: Actuator + Micrometer + OpenTelemetry + Logback JSON

## Puerto
- **8081** (desarrollo)

## Endpoints que expone

Ver contratos completos en `docs/api-contracts.md` — sección 8.

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /v1/subscribers | Catálogo de suscriptores |
| GET | /v1/agents | Catálogo de agentes |
| GET | /v1/business-lines | Catálogo de giros |
| GET | /v1/zip-codes/{zipCode} | Consulta código postal |
| POST | /v1/zip-codes/validate | Valida código postal |
| GET | /v1/folios | Genera siguiente folio secuencial |
| GET | /v1/catalogs/risk-classification | Clasificación de riesgo |
| GET | /v1/catalogs/guarantees | Catálogo de garantías |
| GET | /v1/tariffs | Consulta tarifas y factores técnicos |
| PUT | /v1/tariffs | Actualiza factores técnicos |

## Ejecución local

### 1. Levantar infraestructura

```bash
cd Insurance-Quoter-Core
docker compose up -d
```

Servicios disponibles tras el comando:

| Servicio | URL | Credenciales |
|----------|-----|-------------|
| PostgreSQL | `localhost:5433` | via variables de entorno |
| Jaeger UI | http://localhost:16686 | — |
| Prometheus | http://localhost:9091 | — |
| Grafana | http://localhost:3001 | admin / admin |

> **Nota de Jaeger**: se levanta un Jaeger por proyecto. Si Insurance-Quoter-Back ya tiene uno corriendo en los puertos 4317/16686, detén el de Core antes de arrancar ambos compose simultáneamente.

### 2. Arrancar la aplicación

Requiere variables de entorno `DB_USERNAME` y `DB_PASSWORD` (archivo `.env` en la raíz del proyecto):

```bash
./gradlew bootRun
```

La app arranca en `http://localhost:8081`.  
Swagger UI: http://localhost:8081/swagger-ui/index.html

### 3. Verificar observabilidad

```bash
# Health
curl http://localhost:8081/actuator/health

# Métricas Prometheus
curl http://localhost:8081/actuator/prometheus
```

Prometheus empieza a scrapear `/actuator/prometheus` cada 15 s. Las métricas aparecen en Grafana tras el primer scrape.

## Observabilidad (3 pilares)

### Logs estructurados
Cada línea de log es JSON con `traceId` y `spanId` en el MDC:
```json
{"@timestamp":"...","level":"INFO","service":"Insurance-Quoter-Core","traceId":"abc123","spanId":"def456","message":"..."}
```

### Métricas custom (Prometheus)

| Métrica | Tipo | Descripción |
|---------|------|-------------|
| `folios_generated_total` | Counter | Folios generados exitosamente |
| `tariff_lookups_total` | Counter | Consultas al catálogo de tarifas |
| `tariff_lookup_duration_seconds` | Timer | Duración de consultas de tarifas |
| `zipcode_lookups_total{found}` | Counter | Búsquedas de CP (tag: `found=true\|false`) |
| `agent_queries_total` | Counter | Consultas de agentes |
| `subscriber_queries_total` | Counter | Consultas de suscriptores |
| `catalog_errors_total{errorType}` | Counter | Errores no manejados por contexto |

Más métricas automáticas: JVM (`jvm_memory_*`), HTTP (`http_server_requests_seconds`), pool de conexiones (`hikaricp_connections_*`).

### Trazas distribuidas
- Propaga el header W3C `traceparent` recibido desde Insurance-Quoter-Back — no crea un root trace nuevo
- `@Observed` en los 10 use cases genera un span por invocación
- Exporta a Jaeger vía OTLP gRPC (`localhost:4317`)
- Spans JDBC registrados automáticamente

## Nota
El folder `src/.../Insurance_Quoter/` es residuo de la copia inicial y puede eliminarse manualmente. El punto de entrada activo es `com.sofka.insurancequoter.core.InsuranceQuoterCoreApplication`.
