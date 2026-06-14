# Diagnóstico: Hibernate no crea tablas antes del primer save (EstadoDerivadoIntegracionTest)

**Error:** `Table "usuarios" not found (this database is empty)`  
**Objetivo:** Diagnosticar por qué con `ddl-auto=create-drop` y Flyway deshabilitado el esquema no existe al ejecutar `userRepository.save(usuario)`.

---

## 1. Clase principal @SpringBootApplication

**Archivo:** `agrogestion-backend/src/main/java/com/agrocloud/AgroCloudApplication.java`

```java
package com.agrocloud;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class AgroCloudApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgroCloudApplication.class, args);
    }
}
```

**Paquete raíz:** `com.agrocloud`

---

## 2. Paquetes de las entidades involucradas

| Entidad           | Archivo                    | Paquete                |
|-------------------|----------------------------|-------------------------|
| User              | core/domain/User.java      | `com.agrocloud.core.domain` |
| Plot              | cultivos/domain/Plot.java  | `com.agrocloud.cultivos.domain` |
| Labor             | model/entity/Labor.java    | `com.agrocloud.model.entity` |
| HistorialCosecha  | model/entity/HistorialCosecha.java | `com.agrocloud.model.entity` |
| Empresa           | core/domain/Empresa.java   | `com.agrocloud.core.domain` |
| Field             | cultivos/domain/Field.java | `com.agrocloud.cultivos.domain` |

---

## 3. ¿El paquete raíz cubre todas las entidades?

**Sí.** El paquete raíz `com.agrocloud` incluye:

- `com.agrocloud.core.domain` (User, Empresa)
- `com.agrocloud.cultivos.domain` (Plot, Field, Cultivo)
- `com.agrocloud.model.entity` (Labor, HistorialCosecha)

Por defecto, `@SpringBootApplication` escanea el paquete de la clase anotada y todos los subpaquetes, así que todas estas entidades están dentro del escaneo.

Además, en **código productivo** existe `DatabaseConfig` que declara explícitamente:

- **@EntityScan(basePackages = {**  
  `"com.agrocloud.model.entity"`,  
  `"com.agrocloud.core.domain"`,  
  `"com.agrocloud.cultivos.domain"`,  
  `"com.agrocloud.porcinos.domain"`  
  **})**
- **@EnableJpaRepositories(basePackages = {**  
  `"com.agrocloud.repository"`,  
  `"com.agrocloud.core.infrastructure"`,  
  `"com.agrocloud.cultivos.infrastructure"`,  
  `"com.agrocloud.porcinos.infrastructure"`  
  **})**

Esos paquetes incluyen User, Plot, Labor, HistorialCosecha y los repositorios usados en el test. En principio el escaneo es correcto.

---

## 4. Anotaciones del test EstadoDerivadoIntegracionTest

**Estado actual (definitivo):**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
class EstadoDerivadoIntegracionTest {
```

**No** se deben añadir en el test `@EntityScan` ni `@EnableJpaRepositories`: **DatabaseConfig** (código productivo) ya declara ambos con paquetes concretos. Si se duplican en el test con `basePackages = "com.agrocloud"`, Spring intenta registrar los mismos repositorios dos veces y se produce **BeanDefinitionOverrideException** (el contexto no llega a cargar y los tests ni siquiera ejecutan el `@BeforeEach`).

---

## 6. Verificación del DataSource en test

Se añadió una clase de diagnóstico **solo en test** (no modifica código productivo):

**Archivo:** `src/test/java/com/agrocloud/config/DiagnosticoDataSourceTestConfig.java`

- Inyecta `DataSource` y `Environment`.
- En `@PostConstruct` escribe en log:
  - `spring.datasource.url`
  - `spring.jpa.properties.hibernate.dialect`
  - `spring.jpa.hibernate.ddl-auto`
  - Si el `DataSource` es Hikari: URL efectiva (`getJdbcUrl()`).

Al ejecutar los tests deberías ver líneas como:

```
[DIAGNOSTICO TEST] spring.datasource.url = jdbc:h2:mem:testdb;...
[DIAGNOSTICO TEST] hibernate.dialect = org.hibernate.dialect.H2Dialect
[DIAGNOSTICO TEST] spring.jpa.hibernate.ddl-auto = create-drop
[DIAGNOSTICO TEST] DataSource efectivo (Hikari) URL = jdbc:h2:mem:testdb;...
```

Si `ddl-auto` no aparece como `create-drop` o la URL no es la de H2, el problema está en la configuración activa en test.

---

## 7. Logging habilitado en application-test.properties

Se dejó en el perfil de test:

- **spring.jpa.show-sql=true**
- **logging.level.org.hibernate.SQL=DEBUG**
- **logging.level.org.hibernate.tool.schema.internal=DEBUG**

Con esto deberías ver:

- Las sentencias SQL que ejecuta Hibernate (incluidas DDL si se generan).
- Los mensajes del módulo de generación de esquema (por ejemplo, creación de tablas).

Si Hibernate está aplicando `create-drop`, antes del primer `save` deberían aparecer múltiples `create table ...` en los logs.

---

## 8. Cómo re-ejecutar y qué revisar

En la raíz del backend:

```bash
mvn clean test -Dtest=EstadoLoteCalculatorTest,EstadoDerivadoIntegracionTest
```

Revisar en la salida:

1. **¿Aparecen las líneas [DIAGNOSTICO TEST]?**  
   - URL de H2, dialecto H2, `ddl-auto=create-drop`.  
   - Si no, el perfil `test` o la configuración no se están aplicando como se espera.

2. **¿Aparecen sentencias `create table` (DDL)?**  
   - Buscar en el log por `create table` o por la salida de `org.hibernate.tool.schema.internal`.  
   - Si **no** hay DDL: Hibernate no está generando el esquema (revisar que no haya otra propiedad que fuerce `ddl-auto=none` o que el EMF se cree con otra configuración).  
   - Si **sí** hay DDL pero el error sigue: podría haber dos bases distintas (por ejemplo, dos URLs H2 en memoria) o un problema de orden de inicialización.

3. **¿El error sigue siendo "Table usuarios not found"?**  
   - Si con `@EntityScan` y `@EnableJpaRepositories` en el test y los logs anteriores el esquema se crea y los tests pasan, la causa era el escaneo/orden en el contexto de test.  
   - Si el esquema se crea pero el error persiste, habría que revisar nombres de tablas (por ejemplo, mayúsculas/minúsculas con H2) o que el `DataSource` usado por el repositorio sea el mismo que el del EMF.

---

## 9. Hallazgo crítico: BeanDefinitionOverrideException

Al añadir en el test `@EntityScan(basePackages = "com.agrocloud")` y `@EnableJpaRepositories(basePackages = "com.agrocloud")`:

- El **ApplicationContext deja de cargar**.
- Error: **`BeanDefinitionOverrideException`**: no se puede registrar el bean `desteteRepository` (y el resto de repositorios) porque ya están definidos en `@EnableJpaRepositories` de **DatabaseConfig**.
- Consecuencia: los tests no llegan a ejecutar el `@BeforeEach`; fallan en la carga del contexto.

**Conclusión:** No duplicar escaneo en el test. El escaneo ya está bien definido en **DatabaseConfig** (incluye `core.domain`, `model.entity`, `cultivos.domain`, etc.). La causa de *"Table usuarios not found"* **no** es falta de escaneo de entidades/repositorios.

---

## 10. Diagnóstico y causa raíz (resumen)

- **Error original:** `Table "usuarios" not found (this database is empty)` en el primer `userRepository.save(usuario)` del `@BeforeEach`.
- **Contexto:** Perfil `test`, H2 en memoria, `ddl-auto=create-drop`, Flyway deshabilitado. El paquete raíz y **DatabaseConfig** cubren todas las entidades y repositorios necesarios.
- **Causa raíz (probable):** Con el contexto cargando correctamente (sin duplicar `@EnableJpaRepositories`), el esquema debería generarse al crear el `EntityManagerFactory`. Si la base sigue vacía en el primer `save`, las posibilidades son:
  1. **Configuración efectiva en test:** Que el valor usado por Hibernate no sea `create-drop` (p. ej. otro perfil o `application.properties` con `ddl-auto=none`). **Verificación:** ver en logs `[DIAGNOSTICO TEST] spring.jpa.hibernate.ddl-auto`.
  2. **DDL no ejecutado:** Que Hibernate no llegue a ejecutar el DDL al construir el EMF. **Verificación:** buscar en logs `create table` o salida de `org.hibernate.tool.schema.internal`.
  3. **Dos bases H2 distintas:** Poco probable con `jdbc:h2:mem:testdb` y un solo DataSource; los logs de diagnóstico muestran la URL efectiva.

**Ajuste mínimo aplicado (solo test):**

- **No** poner `@EntityScan` ni `@EnableJpaRepositories` en el test (evitar BeanDefinitionOverrideException).
- Mantener **DiagnosticoDataSourceTestConfig** para imprimir URL, dialecto y `ddl-auto` al arrancar.
- En **application-test.properties**: `spring.jpa.show-sql=true` y logging DEBUG para `org.hibernate.SQL` y `org.hibernate.tool.schema.internal`.

En **application-test.properties** se añadió también `spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop` para que Hibernate reciba explícitamente la orden de generar el esquema (por si en test solo `ddl-auto` no se propagaba correctamente).

**Próximo paso:** Ejecutar de nuevo los tests. Si el contexto carga y el error "usuarios not found" reaparece, revisar en la salida: (1) valor de `[DIAGNOSTICO TEST] spring.jpa.hibernate.ddl-auto` y (2) si aparecen sentencias `create table`. Si `ddl-auto` es `create-drop` y no hay DDL en logs, el siguiente paso sería revisar inicialización del EMF o uso de `SchemaManagementTool`.

---

## 11. Archivos tocados (solo test/config de test)

| Archivo | Cambio |
|---------|--------|
| `EstadoDerivadoIntegracionTest.java` | Sin `@EntityScan` ni `@EnableJpaRepositories` (evitar conflicto con DatabaseConfig). Comentario en javadoc explicando por qué no usarlos. |
| `application-test.properties` | `spring.jpa.show-sql=true`; `spring.jpa.properties.hibernate.hbm2ddl.auto=create-drop`; `logging.level.org.hibernate.SQL=DEBUG`; `logging.level.org.hibernate.tool.schema.internal=DEBUG`. |
| `DiagnosticoDataSourceTestConfig.java` | Nuevo (solo en test): escribe en log URL, dialecto y `ddl-auto` al arrancar el contexto. |

No se ha modificado código productivo.

---

## 12. Resumen ejecutivo

| Punto | Conclusión |
|------|------------|
| **Clase principal** | `com.agrocloud.AgroCloudApplication` (paquete `com.agrocloud`). |
| **Paquetes de entidades** | User, Empresa, Plot, Field en `core.domain` / `cultivos.domain`; Labor, HistorialCosecha en `model.entity`. Todos bajo `com.agrocloud`. |
| **¿Raíz cubre entidades?** | Sí. DatabaseConfig además declara @EntityScan y @EnableJpaRepositories con los paquetes necesarios. |
| **¿Añadir @EntityScan/@EnableJpaRepositories en el test?** | **No.** Provoca BeanDefinitionOverrideException porque los repositorios quedan registrados dos veces. |
| **Causa de "Table usuarios not found"** | Aún por confirmar con logs: o bien la configuración efectiva no es `create-drop`, o bien el DDL no se ejecuta al crear el EMF. |
| **Ajuste mínimo** | Test sin anotaciones duplicadas; `application-test` con `ddl-auto` y `hbm2ddl.auto=create-drop`, show-sql y logging de schema; DiagnosticoDataSourceTestConfig para imprimir URL, dialecto y ddl-auto. |
| **Comando para validar** | `mvn test -Dtest=EstadoDerivadoIntegracionTest` y revisar en consola `[DIAGNOSTICO TEST]` y aparición de `create table`. |

---

## 13. Diagnóstico final: bean EMF explícito con JpaProperties

**Hipótesis comprobada:** Aunque DatabaseConfig **no** definía un bean `LocalContainerEntityManagerFactoryBean` (solo @EntityScan y @EnableJpaRepositories), la auto-configuración de Spring Boot en el contexto de test no estaba propagando correctamente `hbm2ddl.auto`/`ddl-auto` a Hibernate, por lo que el esquema no se generaba.

**Solución aplicada (código productivo, mínimo):** En **DatabaseConfig** se añadió un bean explícito que construye el EMF con `EntityManagerFactoryBuilder`, `DataSource` y **`JpaProperties`**, pasando `jpaProperties.getProperties()` al builder para que las propiedades JPA (incl. `hibernate.hbm2ddl.auto` / `ddl-auto`) lleguen a Hibernate:

```java
@Bean
public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        DataSource dataSource,
        JpaProperties jpaProperties) {
    return builder
            .dataSource(dataSource)
            .packages("com.agrocloud")
            .properties(jpaProperties.getProperties())
            .build();
}
```

**Resultado:**

- En logs aparecen **create table** (incl. `usuarios`, `empresas`, `cultivo_lotes`, etc.) y **drop table** (create-drop).
- Los tests de **EstadoDerivadoIntegracionTest** pasan (tras corregir en test el setUp asignando `empresa.setCuit("30-12345678-9")` porque la columna `cuit` es NOT NULL).

**Cambios realizados:**

| Archivo | Cambio |
|---------|--------|
| `DatabaseConfig.java` | Añadido bean `entityManagerFactory(EntityManagerFactoryBuilder, DataSource, JpaProperties)` que propaga `jpaProperties.getProperties()` al builder. |
| `EstadoDerivadoIntegracionTest.java` | En `setUp`, asignar `empresa.setCuit("30-12345678-9")` para cumplir NOT NULL en la tabla `empresas`. |
