---
name: Spring Boot 4 — RestClient.Builder no es un bean automático
description: En SB4 con spring-boot-starter-webmvc, RestClient.Builder no se registra como bean autoconfigured. Los tests de integración deben construir RestClient directamente.
type: feedback
---

`RestClient.Builder` no está disponible como `@Autowired` en Spring Boot 4 con `spring-boot-starter-webmvc`.

**Why:** La autoconfiguración de `RestClient.Builder` no existe en SB4 a diferencia de lo que ocurría en SB3.x con `RestTemplate` o `WebClient`.

**How to apply:** En tests de integración con `@SpringBootTest(webEnvironment = RANDOM_PORT)`, construir el client directamente en `@BeforeEach`:

```java
@BeforeEach
void setUp() {
    client = RestClient.builder()
            .baseUrl("http://localhost:" + port)
            .build();
}
```

No intentar inyectarlo con `@Autowired` ni con `RestClient.Builder`.
