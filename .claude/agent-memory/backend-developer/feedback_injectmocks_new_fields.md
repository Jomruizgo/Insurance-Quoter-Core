---
name: @InjectMocks rompe cuando se agregan campos al constructor sin declarar @Mock
description: Al añadir un nuevo campo final a un controller/use case que usa @InjectMocks, Mockito lo deja null y causa NPE. Siempre agregar @Mock para cada campo nuevo.
type: feedback
---

Cuando se agrega un campo `final` nuevo a una clase que ya tiene tests con `@InjectMocks`, Mockito no inyecta nada para ese campo (queda `null`). Si el método bajo prueba llama a ese campo, lanza `NullPointerException`.

**Why:** Mockito solo inyecta mocks de los campos que están declarados con `@Mock` en la clase de test. Si se agrega un campo al constructor de producción sin el `@Mock` correspondiente en el test, Mockito lo omite silenciosamente.

**How to apply:** Cada vez que se agrega un campo `final` a una clase testeada con `@InjectMocks`, verificar la clase de test y añadir `@Mock` para ese campo. Ocurrió en SPEC-009 al inyectar `FolioMetrics`, `AgentMetrics` y `SubscriberMetrics` en sus respectivos controllers.
