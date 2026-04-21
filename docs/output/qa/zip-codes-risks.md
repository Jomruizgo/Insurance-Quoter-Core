# Matriz de Riesgos — Códigos Postales (SPEC-005)

**Feature:** zip-codes  
**Microservicio:** Insurance-Quoter-Core (puerto 8081)  
**Fecha:** 2026-04-21

---

## Resumen

Total: 8 | Alto (A): 3 | Medio (S): 4 | Bajo (D): 1

---

## Detalle

| ID    | HU     | Descripción del Riesgo                                                                        | Factores                                                        | Nivel | Testing      |
|-------|--------|-----------------------------------------------------------------------------------------------|-----------------------------------------------------------------|-------|--------------|
| R-001 | HU-01  | `catastrophicZone`, `tevZone` o `fhmZone` incorrectos en seed → cálculo de prima erróneo     | Dato crítico para cálculo financiero; gestionado por migración  | A     | Obligatorio  |
| R-002 | HU-01  | Problema N+1 al cargar colonias sin `JOIN FETCH` → degradación severa bajo carga             | Alta frecuencia de uso; consulta en cada cotización             | A     | Obligatorio  |
| R-003 | HU-01/02 | Migración Flyway falla en deploy → servicio no arranca; bloquea todo el flujo de cotización | Integridad del contexto Spring; dependencia de DB externa       | A     | Obligatorio  |
| R-004 | HU-01  | GET retorna 200 con campos nulos si el mapeo JPA → domain falla silenciosamente              | Código nuevo sin historial; múltiples capas de mapeo            | S     | Recomendado  |
| R-005 | HU-02  | POST /validate retorna HTTP distinto de 200 para CP inexistente (viola RN-5)                 | Lógica de negocio específica; ruptura de contrato con Back      | S     | Recomendado  |
| R-006 | HU-01  | CP con colonias en distintos estados simultáneos rompe consistencia de `neighborhoods`       | Lógica de negocio compleja; relación `@OneToMany` con LAZY load | S     | Recomendado  |
| R-007 | HU-01/02 | Datos seed insuficientes o sin representación de todas las zonas catastróficas             | Código nuevo; cobertura de datos de prueba                      | S     | Recomendado  |
| R-008 | HU-01  | Formato del `zipCode` no validado en el path variable (ej. cadena larga o con caracteres especiales) | Feature interno; impacto limitado a llamadas malformadas   | D     | Opcional     |

---

## Plan de Mitigación — Riesgos ALTO

### R-001: Datos de zona incorrectos en seed afectan el cálculo de prima

- **Contexto:** Los valores `catastrophicZone`, `tevZone` y `fhmZone` que entrega este endpoint son consumidos directamente por `Insurance-Quoter-Back` para calcular la prima. Un seed erróneo produce cálculos incorrectos sin error visible en runtime.
- **Mitigación:**
  - Revisar el archivo `V3__seed_zip_codes.sql` en cada PR que lo modifique.
  - Añadir aserción en `ZipCodeIntegrationTest` que verifique los valores de zona del CP `06600` contra los valores esperados del seed.
  - Considerar validación defensiva en `ZipCodePersistenceAdapter` que rechace registros con zonas vacías.
- **Tests obligatorios:**
  - Test de integración: verificar que `GET /v1/zip-codes/06600` retorna `catastrophicZone=ZONE_A`, `tevZone=TEV-1`, `fhmZone=FHM-2`.
  - Test unitario de mapper: verificar que ningún campo de zona se mapea a null.
- **Bloqueante para release:** ✅ Sí

---

### R-002: Problema N+1 al cargar colonias

- **Contexto:** Sin `JOIN FETCH`, Hibernate ejecuta una query por cada CP para cargar sus colonias. En un endpoint consultado en cada cotización, esto puede producir cientos de queries por segundo bajo carga media.
- **Mitigación:**
  - Verificar que `ZipCodeJpaRepository.findByZipCodeWithNeighborhoods` usa `LEFT JOIN FETCH z.neighborhoods`.
  - Activar `spring.jpa.show-sql=true` en test de integración y verificar que se ejecuta exactamente 1 query al llamar el endpoint.
  - Añadir `@BatchSize` como fallback si en el futuro se usa `findAll()`.
- **Tests obligatorios:**
  - Test de integración: llamar `GET /v1/zip-codes/06600` y verificar que `neighborhoods` contiene los valores esperados (prueba indirecta de que el fetch funcionó).
- **Bloqueante para release:** ✅ Sí

---

### R-003: Fallo de migración Flyway bloquea el arranque

- **Contexto:** Si `V2__create_zip_codes.sql` o `V3__seed_zip_codes.sql` tienen errores SQL, la aplicación no arranca. Esto afecta no solo a `zip-codes` sino a todo el microservicio.
- **Mitigación:**
  - El test de integración `ZipCodeIntegrationTest` con Testcontainers valida implícitamente que las migraciones corren sin error (el contexto Spring levanta).
  - Ejecutar `./gradlew test` antes de cualquier merge — si las migraciones fallan, el test de integración falla primero.
  - Revisar el orden de versiones Flyway: V1 ya existe, V2 y V3 deben ser consecutivas.
- **Tests obligatorios:**
  - Test de integración: contexto Spring arranca correctamente con Testcontainers (cualquier fallo de Flyway revienta este test).
- **Bloqueante para release:** ✅ Sí

---

## Riesgos MEDIO — Acciones Recomendadas

### R-004: Campos nulos por fallo silencioso en mapeo JPA → domain
- Añadir assertion en `ZipCodePersistenceAdapterTest` que verifique que ningún campo del `ZipCode` resultante es null.
- El test de integración que verifica todos los campos del response actúa como red de seguridad.

### R-005: POST /validate viola RN-5 si retorna non-200 para CP inexistente
- Cubrir explícitamente en `ZipCodeControllerTest` y en `ZipCodeIntegrationTest` el caso de CP inexistente en POST /validate → debe retornar HTTP 200 con `valid: false`.

### R-006: Consistencia de `neighborhoods` con carga LAZY
- Verificar en `ZipCodeIntegrationTest` que el array `neighborhoods` no es null y contiene los valores del seed para `06600`.
- La relación `@OneToMany` con `LAZY` + `JOIN FETCH` en la query es la estrategia correcta — confirmar en tests que no se produce `LazyInitializationException`.

### R-007: Cobertura de datos seed por zona
- El seed actual cubre `ZONE_A`, `ZONE_B`, `ZONE_C`, `ZONE_D` — suficiente para pruebas representativas.
- Documentar en el README del módulo qué zonas cubre el seed y cómo agregar nuevos CPs.
