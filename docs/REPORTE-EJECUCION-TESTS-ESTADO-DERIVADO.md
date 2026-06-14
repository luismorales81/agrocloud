# Reporte de ejecución — Tests del módulo agrícola (estado derivado)

**Comando ejecutado:**  
`mvn clean test "-Dtest=EstadoLoteCalculatorTest,EstadoDerivadoIntegracionTest"`  
(primero sin `-U`; luego con `mvn -U clean test ...` y permisos completos para evitar bloqueo en `.m2`)

**Proyecto:** `agrogestion-backend`

---

## 1. Verificación de Maven

```
Apache Maven 3.9.6 (bc0240f3c744dd6b6ec2920b3cd08dcc295161ae)
Maven home: C:\Users\moral\Apache\maven
Java version: 17.0.12, vendor: Oracle Corporation
OS: Windows 11, arch: amd64
```

**Resultado:** Maven está disponible y correctamente configurado.

---

## 2. Primera ejecución (sin `-U`, en sandbox)

**Comando:** `mvn clean test "-Dtest=EstadoLoteCalculatorTest,EstadoDerivadoIntegracionTest"`

**Resultado:** Los tests **no pudieron ejecutarse** por error de entorno.

- **Error:** `java.nio.file.AccessDeniedException: C:\Users\moral\.m2\repository\org\apache\maven\surefire\surefire-junit-platform\3.5.4`
- **Causa:** Maven no pudo crear o escribir en el directorio del repositorio local (`.m2`) al descargar/actualizar el artefacto `surefire-junit-platform`.
- **Stacktrace relevante:**
  ```
  java.nio.file.AccessDeniedException: C:\Users\moral\.m2\repository\org\apache\maven\surefire\surefire-junit-platform\3.5.4
      at sun.nio.fs.WindowsException.translateToIOException(WindowsException.java:89)
      at sun.nio.fs.WindowsFileSystemProvider.createDirectory(WindowsFileSystemProvider.java:521)
      at java.nio.file.Files.createDirectory(Files.java:700)
      ...
      at org.apache.maven.plugin.surefire.AbstractSurefireMojo.execute(AbstractSurefireMojo.java:969)
  ```
- **Conclusión:** Restricciones del entorno (sandbox) impidieron escribir en `~/.m2`. No se llegó a ejecutar ningún test.

---

## 3. Segunda ejecución (con `-U` y permisos completos)

**Comando:** `mvn -U clean test "-Dtest=EstadoLoteCalculatorTest,EstadoDerivadoIntegracionTest"` (con permisos que permiten escritura en `.m2`).

**Resultado:** Los tests **sí se ejecutaron**. Resumen:

| Métrica | Valor |
|--------|--------|
| **Tests totales** | 24 |
| **Tests fallidos (Failures)** | 0 |
| **Tests con error (Errors)** | 12 |
| **Tests omitidos (Skipped)** | 0 |
| **Tiempo total** | ~4 min 29 s (04:29 min) |
| **BUILD** | **FAILURE** |

---

## 4. Desglose por clase

### EstadoLoteCalculatorTest (unitarios)

- **Todos pasaron.**
- Tests ejecutados por grupo (según log): 1) Lote sin labores → 1 test; 2) Labor SIEMBRA → 2; 3–5) Simular días → 3; 6) Cosecha vigente → 1; 8) Abandono → 2; Labor COSECHA activa → 1; Labor inactiva → 2.
- **Resultado:** 12 tests, 0 fallos, 0 errores. Tiempo ~0,5 s.

### EstadoDerivadoIntegracionTest (integración)

- **Todos fallaron con error** (no assertion failure).
- **Causa común:** En `@BeforeEach setUp()` (línea 78 del test), al hacer `userRepository.save(usuario)` la base H2 en memoria no tiene la tabla `usuarios`:  
  **Table "usuarios" not found (this database is empty)**.
- Los 12 tests de integración reportan el mismo error: fallo en `setUp` al insertar el `User`. Es decir, el esquema de H2 no se creó antes del primer uso (o no se aplicó correctamente con `spring.jpa.hibernate.ddl-auto=create-drop` en el perfil `test`).

---

## 5. Detalle de los 12 errores (integración)

- **Clase:** `EstadoDerivadoIntegracionTest`
- **Método donde falla:** `setUp` (línea 78)
- **Excepción:** `org.springframework.dao.InvalidDataAccessResourceUsageException`
- **Mensaje:**  
  `could not prepare statement [Table "usuarios" not found (this database is empty); SQL statement: insert into usuarios (...) values (?,?,?,...) [42104-232]]`
- **Causa raíz (H2):**  
  `org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "usuarios" not found (this database is empty)`
- **Línea exacta (en el test):** 78 — corresponde al `userRepository.save(usuario)` dentro de `@BeforeEach void setUp()`.

**Stacktrace relevante (fragmento):**

```
org.springframework.dao.InvalidDataAccessResourceUsageException: could not prepare statement [Table "usuarios" not found (this database is empty); SQL statement:
insert into usuarios (activo,creado_por_id,email,email_verified,estado,...) values (?,?,?,?,?,...) [42104-232]]
	at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:281)
	...
Caused by: org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "usuarios" not found (this database is empty); SQL statement:
insert into usuarios (...) values (?,?,?,...) [42104-232]
	at org.h2.message.DbException.getJdbcSQLException(DbException.java:514)
	at org.h2.command.Parser.getTableOrViewNotFoundDbException(Parser.java:7932)
```

---

## 6. Warnings relevantes durante la ejecución

- **Compilación:** Uso de métodos deprecados en código productivo:
  - `EstadoLoteController.java:71` — `confirmarCambioEstado(...)` deprecado.
  - `LaborController.java:297` — `confirmarLaborCosecha(...)` deprecado.
- **Hibernate:** `HHH90000025: H2Dialect does not need to be specified explicitly` (propiedad de dialecto redundante en perfil test).
- **Spring Security:** Aviso sobre `AuthenticationManager` y `UserDetailsService` (no afecta al fallo de los tests).

---

## 7. Resumen final

| Pregunta | Respuesta |
|----------|-----------|
| **¿Todos los tests pasaron?** | **No.** 12 de 24 pasaron; 12 fallaron. |
| **¿Alguno falló?** | **Sí.** Los 12 tests de **EstadoDerivadoIntegracionTest** fallan por error en `setUp`. |
| **¿No pudieron ejecutarse por entorno?** | En la **primera** ejecución (sandbox), sí: no se ejecutaron por `AccessDeniedException` en `.m2`. En la **segunda** (con permisos), los tests sí se ejecutaron y el resultado es el indicado arriba. |

**Conclusión:**

- **EstadoLoteCalculatorTest:** OK; el modelo de estado derivado a nivel de cálculo puro se valida correctamente.
- **EstadoDerivadoIntegracionTest:** No pueden pasar con la configuración actual porque la base H2 en memoria no tiene tablas creadas cuando se ejecuta `setUp` (tabla `usuarios` no encontrada). La causa es de **configuración/entorno de test** (esquema H2 con `ddl-auto=create-drop` no generando tablas antes del primer `save`), no de la lógica de negocio de los tests.

**No se ha modificado código productivo, dependencias ni `pom.xml`; solo ejecución y diagnóstico.**
