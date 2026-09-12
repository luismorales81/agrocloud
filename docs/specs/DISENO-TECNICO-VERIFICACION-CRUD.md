# Diseño técnico — Verificación CRUD automatizada

**SPEC:** `SPEC-VERIFICACION-CRUD-MODULOS.md`  
**Estado:** Aprobado

---

## 1. Arquitectura de tests

```
src/test/java/com/agrocloud/
├── test/
│   └── BaseIntegracionCrudTest.java      # Setup usuario/empresa/módulo/campaña
├── cultivos/
│   └── *CrudIntegracionTest.java         # Por dominio cultivos
├── porcinos/
├── feedlot/
├── lecheria/
├── avicola/
└── admin/
    └── *CrudIntegracionTest.java
```

**Stack:** Spring Boot Test, MockMvc, `@ActiveProfiles("test")`, `@AutoConfigureMockMvc(addFilters = false)`.

---

## 2. Clase base `BaseIntegracionCrudTest`

Responsabilidades:

- Crear `User`, `Empresa`, `UsuarioEmpresa` (rol ADMINISTRADOR).
- Opcional: habilitar módulo (`CompanyModule`) y campaña activa.
- Autenticar con `UserDetails` (email como username).
- Helpers HTTP: `getJson`, `postJson`, `putJson`, `deleteJson` con headers `X-Company-Id`, `X-Campaign-Id`.
- `generarSufijo()` para datos únicos por ejecución.

---

## 3. Configuración `application-test.properties`

Ubicación: `src/test/resources/application-test.properties.example` (plantilla versionada).

CI copia la plantilla antes de `mvn test`. Variables:

| Propiedad | Origen |
|-----------|--------|
| `spring.datasource.url` | `DATABASE_URL` o default localhost/agrocloud_test |
| `spring.datasource.username` | `DATABASE_USERNAME` / `DB_USER` |
| `spring.datasource.password` | `DATABASE_PASSWORD` / `DB_PASS` |
| `jwt.secret` | valor fijo de test (32+ chars) |
| `spring.flyway.enabled` | `true` |
| `spring.jpa.hibernate.ddl-auto` | `none` |

---

## 4. Patrón de test CRUD

```java
@Test
void cicloCrud_campo_ok() throws Exception {
    // CREATE
    MvcResult creado = postJson("/api/campos", cuerpoCampo)
        .andExpect(status().isCreated())
        .andReturn();
    Long id = extraerId(creado);

    // READ
    getJson("/api/campos/" + id).andExpect(status().isOk())
        .andExpect(jsonPath("$.nombre").value("Campo Test"));

    // UPDATE
    putJson("/api/campos/" + id, cuerpoActualizado)
        .andExpect(status().isOk());

    // DELETE
    deleteJson("/api/campos/" + id).andExpect(status().isNoContent());
    getJson("/api/campos/" + id).andExpect(status().isNotFound());
}
```

---

## 5. CI

Workflow raíz `.github/workflows/maven-tests.yml`:

- Copiar `application-test.properties.example` → `application-test.properties`.
- Inyectar `DATABASE_URL`, `JWT_SECRET`.
- Eliminar paso `jacoco:report` (plugin no configurado).

---

## 6. Orden de implementación

1. Infra + base class  
2. Cultivos (8 entidades)  
3. Porcinos, Feedlot, Lechería  
4. Avícola × 3 + Admin  
5. Reporte `docs/REPORTE-VERIFICACION-CRUD.md`
