---
name: Spring Boot 4 — spring-boot-starter-aop no existe
description: En SB4, el artefacto spring-boot-starter-aop no existe en el BOM. Usar org.aspectj:aspectjweaver directamente.
type: feedback
---

En Spring Boot 4, `spring-boot-starter-aop` no existe como artefacto en el BOM y falla la resolución con "Could not find".

**Why:** El AOP ya viene transitivamente a través de `spring-boot-starter-webmvc` (vía `spring-context` → `spring-aop`). En SB4 el starter fue eliminado como artefacto independiente.

**How to apply:** Para declarar AOP explícitamente (necesario para `@Observed` con `ObservedAspect`), usar:
```kotlin
implementation("org.aspectj:aspectjweaver")
```
Sin versión — el BOM de SB4 la gestiona.
