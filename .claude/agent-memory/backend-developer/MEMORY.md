# Agent Memory — backend-developer

- [folio-generator feature — implementation state](project_folio_feature.md) — SPEC-001 implementado, compila. Estructura de paquetes, decisiones de diseño y archivos clave.
- [Insurance-Quoter-Core — setup y configuración base](project_core_setup.md) — Paquete base, puerto, DB, dependencias reales del proyecto core.
- [Spring Boot 4 — RestClient.Builder no es bean automático](feedback_spring_boot4_restclient.md) — En tests de integración construir RestClient directamente en @BeforeEach, no inyectar con @Autowired.
- [Testear @ExceptionHandler con Mockito puro](feedback_controller_exception_handler_test.md) — Llamar directamente al método handler; el dispatcher de Spring no está activo en tests Mockito puros.
