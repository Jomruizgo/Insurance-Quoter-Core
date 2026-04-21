# Insurance-Quoter-Core (plataforma-core-ohs)

Microservicio de referencia del cotizador de seguros de daños. Provee catálogos, tarifas, agentes, suscriptores, códigos postales y generación de folios.

## Stack
- Java 21 + Spring Boot 4 + Spring Data JPA
- PostgreSQL (puerto **5433** en desarrollo)
- Arquitectura hexagonal (ports & adapters)

## Puerto
- **8081** (development)

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
| GET | /v1/tariffs | Tarifas y factores técnicos |

## Ejecución local

```bash
cd Insurance-Quoter-Core
./gradlew bootRun
```

PostgreSQL se levanta automáticamente con Docker Compose en el puerto 5433.

## Nota
El folder `src/.../Insurance_Quoter/` es residuo de la copia inicial y puede eliminarse manualmente. El punto de entrada activo es `com.sofka.insurancequoter.core.InsuranceQuoterCoreApplication`.
