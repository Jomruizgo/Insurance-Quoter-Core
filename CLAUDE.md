# CLAUDE.md — Insurance-Quoter-Core

Microservicio `plataforma-core-ohs` · Puerto **8081** · DB `insurance_core_db` (**:5433**) · Java 21 + Spring Boot **4.0.5**

---

## GitFlow — obligatorio

```
main ← solo desde release/* o hotfix/*
develop ← base de toda feature
feature/<ticket>-descripcion ← nueva funcionalidad
```

**Antes de cualquier cambio:**
```bash
git checkout develop
git checkout -b feature/<ticket>-descripcion
```

Nunca commitear directo a `main` ni a `develop`.

---

## Orden de trabajo

1. Spec aprobada en `.claude/specs/<feature>.spec.md` (`status: APPROVED`)
2. Rama `feature/` desde `develop`
3. TDD por cada clase de lógica: test → código → verde → refactor
4. Cerrar GitHub Issue de cada tarea al completarla: `gh issue close <N>`
5. PR a `develop` cuando todos los issues del feature estén cerrados

---

## Stack de tests

| Capa | Herramienta |
|------|-------------|
| Use case / Adapter / Mapper | `@ExtendWith(MockitoExtension.class)` — sin Spring |
| Controller | `@InjectMocks` + Mockito (`@WebMvcTest` no existe en SB4) |
| Integración / Concurrencia | `@SpringBootTest` + `@Testcontainers` + `PostgreSQLContainer` |

**Dependencias Testcontainers 2.x** (nombres cambiaron respecto a 1.x):
```kotlin
testImplementation("org.springframework.boot:spring-boot-testcontainers")
testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.4")
testImplementation("org.testcontainers:testcontainers-postgresql:2.0.4")
```

`@DynamicPropertySource` siempre incluye `spring.jpa.hibernate.ddl-auto=none`.

---

## Spring Boot 4 — diferencias clave vs 3.x

- **Flyway**: no se autoconfigura solo. Requiere `org.springframework.boot:spring-boot-starter-flyway`.
- **@WebMvcTest**: eliminado. Testear controllers con Mockito puro.
- **@MockBean**: reemplazado por `@MockitoBean`.

---

## Arquitectura hexagonal

```
domain/model/          ← records Java puros, sin Spring/JPA
domain/port/in/        ← interfaces de use cases
domain/port/out/       ← interfaces de repos/clientes externos
application/usecase/   ← implementaciones (sin @Service, registrar via @Bean en config/)
infrastructure/adapter/in/rest/       ← controllers (implements *Api)
infrastructure/adapter/in/rest/swaggerdocs/  ← @Tag @Operation (nunca en el controller)
infrastructure/adapter/out/persistence/      ← JPA adapters
infrastructure/config/ ← @Bean wiring de use cases + Clock
```
