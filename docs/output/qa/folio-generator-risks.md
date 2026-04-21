# Matriz de Riesgos — Generación de Números de Folio Secuenciales

**Feature:** folio-generator | **Spec:** SPEC-001 | **Fecha:** 2026-04-21
**Analista QA:** Risk Identifier — ASDD

---

## Resumen

Total: 9 | Alto (A): 4 | Medio (S): 3 | Bajo (D): 2

---

## Detalle

| ID    | HU     | Descripción del Riesgo | Factores | Nivel | Testing |
|-------|--------|------------------------|----------|-------|---------|
| R-001 | HU-01  | Folio duplicado por falla en atomicidad de `nextval`: si la secuencia PostgreSQL falla silenciosamente o el adaptador reintenta la llamada, dos consumidores pueden recibir el mismo folio, corrompiendo el modelo de cotizaciones en Insurance-Quoter-Back | Operación irrecuperable, integración externa (DB), alta frecuencia | A | Obligatorio |
| R-002 | HU-01  | Indisponibilidad de la base de datos (`insurance_core_db:5433`): el servicio no tiene circuit breaker ni fallback; cualquier falla de red o caída del contenedor PostgreSQL expone un 503 sin gestión de degradación controlada | Integración externa, SLA implícito como dependencia bloqueante de cotizaciones | A | Obligatorio |
| R-003 | HU-01  | Overflow de padding a 5 dígitos: la secuencia supera 99.999 sin `MAXVALUE` ni `CYCLE`; el formato `FOL-2026-100000` excede los 14 caracteres esperados y puede romper validaciones en sistemas consumidores | Operación irrecuperable (datos en producción ya guardados con formato previo) | A | Obligatorio |
| R-004 | HU-01  | Rollover de año en el folio: el año se toma del reloj del sistema en `FolioSequenceJpaAdapter`, no del mismo instante que `generatedAt` en el use case; en el cambio de año (31-dic 23:59 → 01-ene 00:01 UTC) puede haber inconsistencia entre el año del `folioNumber` y el año real del `generatedAt` si el reloj se lee en dos puntos distintos del flujo | Lógica de negocio compleja, borde temporal irrecuperable | A | Obligatorio |
| R-005 | HU-01  | Ausencia de autenticación en `GET /v1/folios`: el endpoint no requiere auth por diseño ("servicio interno"). Sin controles de red (ej. service mesh, API Gateway), cualquier proceso con acceso a la red interna puede consumir la secuencia y agotar folios o causar gaps en la numeración | Seguridad, operación irrecuperable (secuencia consumida no se revierte) | S | Obligatorio |
| R-006 | HU-01  | Gaps en la secuencia por transacciones abortadas: si el use case lanza excepción después de `nextval` pero antes de retornar la respuesta HTTP, el valor de secuencia se pierde permanentemente (comportamiento esperado de PostgreSQL SEQUENCE, pero puede sorprender a auditores que esperan numeración continua) | Lógica de negocio compleja, muchas dependencias (JPA tx, Spring MVC) | S | Obligatorio |
| R-007 | HU-01  | La tabla auxiliar `folio_sequence_ctrl` es una dependencia de infraestructura frágil: su única fila puede ser eliminada por error (DELETE sin WHERE), lo que provoca que `FolioRepository.nextValue()` no encuentre la entidad JPA y falle; no hay lógica de re-seed ni constraint que lo prevenga | Alta frecuencia de uso, dependencia de datos de bootstrap en BD | S | Recomendado |
| R-008 | HU-01  | `generatedAt` generado en capa de aplicación y no sincronizado con el valor de secuencia: el timestamp se toma con `Instant.now(clock)` después de llamar `nextFolioNumber()`, lo que en condiciones de carga puede reflejar un instante posterior al de la obtención real del `nextval`, afectando la auditoría de orden temporal | Código nuevo, lógica de negocio de trazabilidad | D | Recomendado |
| R-009 | HU-01  | Acoplamiento del formato del folio al adapter de persistencia: el patrón `FOL-<año>-<NNNNN>` vive en `FolioSequenceJpaAdapter` (infraestructura), no en dominio; si el patrón cambia, los tests unitarios del adapter deben actualizarse pero la violación no es detectada por tests de dominio | Refactorización, deuda técnica de diseño | D | Recomendado |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: Folio duplicado por falla en atomicidad de `nextval`

- **Mitigación**: (1) Verificar mediante inspección de código que `FolioRepository.nextValue()` no está anotado con `@Transactional` con propagación `REQUIRES_NEW` que pueda provocar reintentos implícitos. (2) Agregar un test de integración con Testcontainers que inyecte un fallo de red simulado post-`nextval` y confirme que no se generan duplicados. (3) Prohibir explícitamente reintentos automáticos (Spring Retry, Resilience4j) sobre este endpoint.
- **Tests obligatorios**: Test de concurrencia (`FolioSequenceConcurrencyTest` — ya existe con 20 threads, ampliar a 100), test de integración con inyección de fallo (`@DataJpaTest` + Testcontainers + rollback forzado), revisión de código del adapter para confirmar ausencia de lógica de reintento.
- **Bloqueante para release**: Si

---

### R-002: Indisponibilidad de `insurance_core_db:5433`

- **Mitigación**: (1) Validar que Spring Boot retorna HTTP 503 con body de error estructurado (`{ "error": "...", "code": "DB_UNAVAILABLE" }`) cuando el datasource no está disponible, no un stack trace expuesto. (2) Documentar el SLA de disponibilidad de la DB como dependencia de `Insurance-Quoter-Back`. (3) Evaluar la introducción de un health check (`/actuator/health`) que incluya el estado del datasource y sea consultado por los consumidores antes de llamar al endpoint.
- **Tests obligatorios**: Test de integración con Testcontainers — detener el contenedor PostgreSQL antes de la llamada y verificar respuesta HTTP 503. Test de contrato para validar que el body del error 503 es parseable por `Insurance-Quoter-Back`.
- **Bloqueante para release**: Si

---

### R-003: Overflow de padding a 5 dígitos (secuencia > 99.999)

- **Mitigación**: (1) Agregar un test unitario en `FolioSequenceJpaAdapterTest` con `seq = 100000L` y verificar que el resultado produce `FOL-2026-100000` (6 dígitos) para documentar el comportamiento y alertar al equipo. (2) Crear una alerta de monitoreo operativo cuando `nextval` supere 90.000 (10% de margen). (3) Definir en la spec si el padding debe extenderse a 6 dígitos o si los consumidores validan longitud fija de 14 chars — la regla de negocio 1 dice "máximo representable: 99.999 por año sin overflow de dígitos" pero la secuencia no se reinicia por año, lo que hace el límite alcanzable en operación normal.
- **Tests obligatorios**: Test unitario con valor de secuencia en borde superior (99999, 100000). Test de contrato en `Insurance-Quoter-Back` que verifique qué ocurre si el `folioNumber` recibido tiene más de 14 chars.
- **Bloqueante para release**: Si (requiere decisión de negocio antes de ir a producción)

---

### R-004: Inconsistencia de año entre `folioNumber` y `generatedAt` en rollover de año

- **Mitigación**: (1) Refactorizar el flujo para que tanto el año del folio como el `generatedAt` se deriven de un único `Instant` capturado en el use case antes de llamar a `nextFolioNumber()`, pasando el año como parámetro al adapter o consolidando la lectura del reloj en un solo punto. (2) Agregar test de borde con `Clock.fixed()` a `2026-12-31T23:59:59.999Z` y `2027-01-01T00:00:00.001Z` para verificar consistencia entre año del folio y año del timestamp.
- **Tests obligatorios**: Test unitario de borde para cambio de año en `GenerateFolioUseCaseImplTest` con dos instancias de `Clock.fixed()` (31-dic y 01-ene). Test de integración que simule el escenario con timestamps controlados.
- **Bloqueante para release**: Si

---

## Cobertura de Tests Existente vs. Riesgos Identificados

| Riesgo | Test existente | Estado |
|--------|---------------|--------|
| R-001 Duplicado concurrente | `FolioSequenceConcurrencyTest` (20 threads) | Parcial — ampliar a ≥ 100 threads |
| R-002 DB indisponible | Ninguno | Sin cobertura |
| R-003 Overflow padding | Ninguno | Sin cobertura |
| R-004 Rollover de año | Ninguno | Sin cobertura |
| R-005 Ausencia de auth | Ninguno (decisión de arquitectura) | Pendiente decisión |
| R-006 Gaps en secuencia | Ninguno | Sin cobertura |
| R-007 Fila única en folio_sequence_ctrl | Ninguno | Sin cobertura |
| R-008 Timestamp desacoplado | `GenerateFolioUseCaseImplTest` happy path | Parcial — no verifica orden relativo |
| R-009 Formato en infraestructura | `FolioSequenceJpaAdapterTest` | Cubierto — documentar como deuda técnica |
