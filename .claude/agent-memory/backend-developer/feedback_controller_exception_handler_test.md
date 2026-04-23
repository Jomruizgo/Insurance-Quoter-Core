---
name: Testear @ExceptionHandler con Mockito puro — llamada directa al handler
description: Con Mockito puro (sin Spring dispatcher), el @ExceptionHandler no se invoca al lanzar la excepción desde el use case. Se debe llamar directamente al método handler en el test.
type: feedback
---

En los tests de controller con `@ExtendWith(MockitoExtension.class)` (sin Spring), si el controller tiene un `@ExceptionHandler`, este **no se invoca automáticamente** cuando el use case lanza la excepción.

**Why:** Mockito instancia el controller como POJO; el mecanismo del DispatcherServlet que intercepta excepciones y llama al handler no está presente.

**How to apply:** Testear el handler directamente:

```java
@Test
void handleZipCodeNotFound_returns404WithStandardErrorBody() {
    ZipCodeNotFoundException ex = new ZipCodeNotFoundException("99999");
    ResponseEntity<Map<String, String>> result = controller.handleZipCodeNotFound(ex);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody().get("code")).isEqualTo("ZIP_CODE_NOT_FOUND");
}
```

El comportamiento end-to-end (excepción → 404 HTTP real) se cubre en el test de integración con `@SpringBootTest`.
