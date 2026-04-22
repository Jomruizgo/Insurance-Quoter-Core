# Matriz de Riesgos — Tariffs (SPEC-008)

> **Fecha:** 2026-04-21
> **Spec:** SPEC-008 v2.0 — APPROVED
> **Feature:** Consulta y Actualización de Tarifas y Factores Técnicos
> **Microservicio:** `plataforma-core-ohs` (Insurance-Quoter-Core) · Puerto 8081 · DB `insurance_core_db` :5433

---

## Resumen

Total: 9 | Alto (A): 4 | Medio (S): 4 | Bajo (D): 1

---

## Detalle

| ID    | HU / Endpoint | Descripción del Riesgo                                                                                     | Factores ASD                                                                              | Nivel | Testing recomendado                                                                |
|-------|---------------|------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------|-------|------------------------------------------------------------------------------------|
| R-001 | HU-01 / GET   | Los factores retornados impactan directamente el cálculo de prima neta en Insurance-Quoter-Back. Un valor incorrecto o desactualizado propaga error financiero a todas las cotizaciones activas. | Integración externa (consumidor back-end crítico), impacto financiero directo en cálculo de prima | **A** | Test de integración con Testcontainers verificando valores seed; contrato de API validado contra Insurance-Quoter-Back |
| R-002 | HU-02 / PUT   | La actualización de tarifas modifica los factores base del motor de cálculo. Una escritura incorrecta o parcial afecta todos los cálculos subsiguientes de prima sin advertencia al usuario final. | Operación que altera cálculos financieros, fila única mutable sin historial de versiones    | **A** | Test unitario UpdateTariffsUseCaseImplTest; test de integración GET→PUT→GET verificando persistencia; smoke test en pre-release |
| R-003 | HU-02 / PUT   | Ausencia de control de concurrencia (`@Version`). Dos actualizaciones simultáneas pueden sobrescribirse sin detección, produciendo valores inconsistentes en la tabla `tariffs`. | Fila única sin optimistic lock, alta frecuencia de lectura vs escritura concurrente        | **A** | Test de integración con múltiples hilos concurrentes; verificar que la entidad `TariffJpa` requiere `@Version` |
| R-004 | HU-01 / GET   | La fila semilla con `id = 1` es creada por Flyway. Si la migración V5 falla o es revertida, `GET /v1/tariffs` devuelve HTTP 404 en lugar de datos, bloqueando todo el flujo de cotización de Insurance-Quoter-Back. | Dependencia de migración Flyway, integración externa bloqueante, fila única sin fallback   | **A** | Test de integración validando presencia de seed; smoke test en arranque; verificar orden V4→V5 en pipeline |
| R-005 | HU-02 / PUT   | La validación `@Positive` actúa solo en la capa REST. Si en el futuro se llama `UpdateTariffsUseCaseImpl.update()` desde otro adaptador (mensajería, CLI), los valores inválidos alcanzan la base de datos y el CHECK de PostgreSQL lanza una excepción no controlada. | Lógica de negocio con validación solo en capa de infraestructura, ausencia de defensa en dominio | **S** | Test unitario del use case con valores límite (0.0, negativo) pasando directamente el dominio sin pasar por DTO |
| R-006 | HU-01 / GET   | El endpoint no requiere autenticación (RN-6). Si el microservicio queda expuesto accidentalmente fuera de la red interna, las tasas técnicas de tarificación son visibles sin restricción. | Ausencia de autenticación, dato sensible de negocio (estrategia de precios)               | **S** | Verificar que el endpoint solo es accesible desde la red interna / service mesh; revisar configuración de firewall y API gateway |
| R-007 | HU-02 / PUT   | El endpoint PUT tampoco requiere autenticación. Cualquier servicio interno con acceso a red puede sobrescribir los factores técnicos. El log de auditoría es inexistente en la spec. | Operación destructiva sin autenticación ni trazabilidad de cambios                        | **S** | Verificar restricción de red; añadir registro de auditoría (quién actualizó, cuándo, valores anteriores vs nuevos) |
| R-008 | HU-01+02      | `TariffJpaAdapter.save()` usa `jpaRepository.save()` con `id = 1` hardcodeado implícitamente. Si el id de la entidad existente no coincide con el del objeto construido en el mapper, JPA puede intentar un INSERT en lugar de UPDATE, violando la clave primaria. | Lógica de id fijo en mapper sin cobertura explícita de la asignación del id en TariffJpa  | **S** | Test unitario TariffJpaAdapterTest verificando que el `TariffJpa` construido para `save()` tiene `id = 1`; test de integración confirma UPDATE (no INSERT) |
| R-009 | TASK-1/2      | Las migraciones Flyway V4 y V5 se ejecutan en orden secuencial. Si se agrega una migración con número anterior en otra rama y se fusiona, se rompe el orden de Flyway y la aplicación no arranca. | Gestión de versiones de migraciones en entorno GitFlow multi-feature                       | **D** | Revisar numeración de migraciones en el PR; usar checklist de naming en DoR; CI valida `./gradlew flywayValidate` |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: Propagación de valores incorrectos de tarifa a Insurance-Quoter-Back

- **Mitigación:** Definir un contrato de API formal (en `docs/api-contracts.md`) entre Core y Back incluyendo el esquema JSON esperado. Implementar consumer-driven contract test (Pact o equivalente) o al menos una prueba de integración que verifique que los valores del seed corresponden exactamente a los usados en el cálculo de prima. Configurar alertas de disponibilidad del endpoint en el entorno de desarrollo compartido.
- **Tests obligatorios:**
  - `TariffIntegrationTest.getTariffs_returnsSeededValues()` — verificar los 5 factores con valores exactos del seed
  - Test de contrato (Insurance-Quoter-Back) simulando respuesta de Core con valores alterados para confirmar propagación correcta
  - Smoke test `@Tag("smoke")` ejecutado en cada PR
- **Bloqueante para release:** Si

### R-002: Escritura incorrecta o parcial de tarifas sin historial

- **Mitigación:** Ampliar `TariffIntegrationTest` con el flujo GET→PUT→GET que ya existe pero agregar verificación de todos los campos (no solo `fireRate`). Considerar persistir un log de auditoría básico (tabla `tariff_audit_log` o equivalente) con los valores anteriores, nuevos, timestamp y origen de la llamada. En ausencia de auditoría, al menos registrar a nivel de log de aplicación con nivel INFO antes y después del `save()`.
- **Tests obligatorios:**
  - `TariffIntegrationTest.updateTariffs_persistsNewValues()` — ampliar a los 5 campos
  - `UpdateTariffsUseCaseImplTest` — happy path y `TariffNotFoundException`
  - `TariffControllerTest.updateTariffs_whenValid_returns200WithUpdatedBody()`
- **Bloqueante para release:** Si

### R-003: Ausencia de control de concurrencia en fila única

- **Mitigación:** Agregar `@Version private Long version` en `TariffJpa` y la columna `version BIGINT NOT NULL DEFAULT 0` en la migración SQL. Esto activa optimistic locking de JPA/Hibernate: si dos transacciones intentan actualizar simultáneamente, la segunda recibe `OptimisticLockException`. El controller debe manejar esta excepción con HTTP 409 Conflict. Esta es una observación de diseño no resuelta en la spec actual — requiere actualización de SPEC-008 antes de cerrar el feature.
- **Tests obligatorios:**
  - Test de concurrencia con `@SpringBootTest` + `ExecutorService` lanzando dos PUT simultáneos — verificar que exactamente uno tiene éxito y el otro recibe 409 o el último gana de forma determinista
  - `TariffJpaAdapterTest` — verificar que el campo `version` se propaga correctamente en la entidad
- **Bloqueante para release:** Si (gap de diseño identificado — la spec no define el comportamiento ante escrituras concurrentes)

### R-004: Fallo de migración Flyway bloquea todo el flujo de cotización

- **Mitigación:** Añadir en el pipeline de CI una fase `./gradlew flywayValidate` antes del despliegue. El test de integración `TariffIntegrationTest` ya levanta Testcontainers con Flyway — verificar que el contenedor arranca correctamente y el seed está presente como paso previo al primer assert. Documentar la dependencia explícita entre V4 y V5 en el README del módulo. Configurar un health check del microservicio que verifique `GET /v1/tariffs` responde 200 al arrancar (no 404).
- **Tests obligatorios:**
  - `TariffIntegrationTest.getTariffs_returnsSeededValues()` como primer test de la suite (valida Flyway completo)
  - Test de migración aislado con `@SpringBootTest(properties = "spring.flyway.clean-on-validation-error=false")` que verifique rechazo si V5 no existe
- **Bloqueante para release:** Si

---

## Gaps identificados respecto a la spec

| Gap | Descripción | Severidad | Acción recomendada |
|-----|-------------|-----------|-------------------|
| G-001 | La spec RN-6 establece "sin autenticación — servicio interno" pero no define controles de red (IP whitelist, service mesh, mTLS). | Alta | Añadir RN a la spec indicando el mecanismo de control de acceso en red |
| G-002 | No existe historial de cambios de tarifas. Un PUT incorrecto es irreversible sin backup. | Alta | Evaluar tabla `tariff_audit_log` o soft-versioning; incluir en la spec como RN |
| G-003 | La spec no define comportamiento ante escrituras concurrentes (fila única sin `@Version`). | Alta | Actualizar SPEC-008 con RN de optimistic locking y comportamiento HTTP 409 |
| G-004 | El test `TariffIntegrationTest.updateTariffs_withZeroValue_returns400()` no valida el cuerpo de la respuesta de error — solo el código HTTP. El contrato especifica `{ "error": "fireRate must be greater than 0" }`. | Media | Ampliar el test para validar el mensaje de error en el body |
| G-005 | El test de integración tiene comentario `// 404 when tariff row deleted would be edge case — skip in integration`. Este escenario está cubierto solo a nivel unitario; en integración real es un path alcanzable si se borra la fila de BD directamente. | Baja | Considerar test con `@Sql` que trunca la tabla antes de ejecutar GET/PUT y verifica HTTP 404 |
