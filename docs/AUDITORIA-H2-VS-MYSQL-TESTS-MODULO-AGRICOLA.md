# Auditoría técnica: H2 vs MySQL en tests del módulo agrícola

**Objetivo:** Determinar si usar H2 en tests es seguro o si existen dependencias específicas de MySQL que requieran Testcontainers.  
**Alcance:** Repositorios JPA, entidades del módulo agrícola, configuración JPA, queries personalizadas, migraciones Flyway, transacciones y locking.  
**Restricción:** Solo análisis y reporte; no se ha modificado código.

---

## 1. Consultas nativas y SQL específico de MySQL

### 1.1 @Query con nativeQuery = true

| Ubicación | Uso | ¿Afecta al módulo agrícola? |
|-----------|-----|-----------------------------|
| `EmpresaRepository.java:54` | `findEmpresasConMasActividad()`: SELECT sobre `empresas`, `cultivo_campos`, `cultivo_lotes`, `cultivo_labores` con subconsultas COUNT y JOINs | **Sí (indirecto):** usa tablas del módulo agrícola. SQL estándar (sin funciones MySQL). |
| `UserRepository.java:75` | `findUsuariosConMasCampos()`: SELECT sobre `usuarios`, `cultivo_campos` con LEFT JOIN y GROUP BY | **Sí (indirecto):** usa `cultivo_campos`. SQL estándar. |

**Contenido de las consultas:** Solo SELECT, subconsultas, JOIN, GROUP BY, ORDER BY. **No se encontraron:** IFNULL, DATE_ADD, NOW(), GROUP_CONCAT, STR_TO_DATE, DATE_FORMAT, CONCAT_WS, FIND_IN_SET ni otras funciones propias de MySQL en estas consultas.

**Conclusión:** Las dos consultas nativas son SQL estándar. Los nombres de tablas (`empresas`, `usuarios`, `cultivo_campos`, `cultivo_lotes`, `cultivo_labores`) coinciden con los `@Table` de las entidades. En tests con H2, si el esquema se genera por Hibernate (ddl-auto=create-drop), los nombres serán los mismos (con `DATABASE_TO_LOWER=TRUE` en H2 quedan en minúsculas). **Riesgo bajo** para H2 en cuanto a sintaxis; el fallo actual de tests (“Table usuarios not found; this database is empty”) se debe a que el esquema no se crea, no a incompatibilidad de la consulta.

### 1.2 Otras búsquedas

- **LIMIT con sintaxis específica:** No se encontró uso de LIMIT en SQL nativo en repositorios.
- **ON DUPLICATE KEY UPDATE:** Presente en scripts SQL de datos e inicialización (`INSERTAR_*.sql`, `src/main/resources/sql/*.sql`, `create-multitenant-tables.sql`), **no** en repositorios JPA ni en migraciones Flyway bajo `db/migration`. Esos scripts no se ejecutan en el perfil de test.
- **FOR UPDATE / LOCK IN SHARE MODE:** No encontrados en el backend.
- **@Lock(LockModeType.PESSIMISTIC_*) / LockModeType:** No encontrados.

---

## 2. Repositorios y queries del módulo agrícola

### 2.1 Repositorios directamente del módulo agrícola (cultivos)

- **PlotRepository** (`cultivos/infrastructure/PlotRepository.java`): Solo JPQL (SELECT p FROM Plot p ...). COALESCE en una query; estándar en JPQL/HQL.
- **LaborRepository:** JPQL únicamente; sin SQL nativo.
- **HistorialCosechaRepository:** JPQL únicamente; AVG, COUNT, GROUP BY en JPQL (traducido por Hibernate al dialecto).
- **CultivoRepository:** JPQL únicamente.
- **EstadoLoteConfigRepository:** JPQL únicamente.

**Conclusión:** El módulo agrícola (Plot, Labor, HistorialCosecha, Cultivo, EstadoLoteConfig) **no usa SQL nativo** en sus repositorios. Todas las consultas son JPQL, traducidas por Hibernate al dialecto configurado (H2 en test). **Riesgo bajo** para H2.

### 2.2 Queries JPQL con posibles diferencias entre dialectos

- **EmpresaRepository:** `COUNT(CASE WHEN e.estado = 'ACTIVO' THEN 1 END)` — expresión CASE estándar en JPQL/SQL.
- **MovimientoInventarioRepository:** `COALESCE(SUM(CASE WHEN m.tipoMovimiento = 'ENTRADA' THEN m.cantidad ELSE -m.cantidad END), 0)` — estándar.

No se detectaron funciones que dependan del dialecto MySQL en las @Query revisadas.

---

## 3. Entidades del módulo agrícola y columnDefinition

### 3.1 Módulo agrícola (cultivos)

| Entidad | Archivo | columnDefinition | Riesgo H2 |
|---------|---------|------------------|-----------|
| Field | `cultivos/domain/Field.java` | `TEXT` (polígono), **`JSON`** (coordenadas) | **JSON:** H2 2.x soporta tipo JSON; con `MODE=MySQL` el DDL puede variar. Riesgo **medio** si Hibernate/H2Dialect no mapean bien "JSON". |
| Cultivo | `cultivos/domain/Cultivo.java` | `TEXT` (descripcion) | TEXT portable. Sin riesgo. |
| Plot | `cultivos/domain/Plot.java` | No usa columnDefinition para tipos especiales | Solo @Enumerated(STRING). Sin riesgo. |
| Labor | `model/entity/Labor.java` | No | @Enumerated(STRING). Sin riesgo. |
| HistorialCosecha | `model/entity/HistorialCosecha.java` | `TEXT` (observaciones) | Portable. Sin riesgo. |
| Maquinaria, LaborManoObra, LaborMaquinaria, etc. | Varios | `TEXT` (observaciones/descripcion) | Portable. Sin riesgo. |

### 3.2 Otras entidades (referencia)

- **columnDefinition = "TEXT":** Múltiples entidades (porcinos, core, model). TEXT es compatible con H2.
- **columnDefinition = "JSON":** Solo **Field.coordenadas** en el módulo agrícola. En MySQL es tipo nativo JSON; en H2 (H2Dialect) Hibernate puede generar VARCHAR/CLOB o, en versiones recientes, tipo JSON según dialecto. **Riesgo:** posible diferencia de tipo o de validación entre H2 y MySQL; impacto limitado si el contenido se guarda como string serializado.

### 3.3 Tipos no encontrados

- No se usa `columnDefinition = "TINYINT(1)"` ni `"datetime"` en las entidades revisadas.
- **@Enumerated:** Solo `EnumType.STRING` en todo el proyecto (Plot, Labor, User, etc.). Comportamiento independiente del dialecto; **sin riesgo** para H2.

---

## 4. Migraciones Flyway

### 4.1 Uso de Flyway en tests

En `application-test.properties`:

- `spring.flyway.enabled=false`
- `spring.jpa.hibernate.ddl-auto=create-drop`

En tests **no se ejecutan** las migraciones Flyway; el esquema lo genera Hibernate a partir de las entidades. Por tanto, la sintaxis MySQL de las migraciones **no afecta** a la ejecución de tests con H2.

### 4.2 Contenido de las migraciones (relevante para producción y para valorar portabilidad)

- **ENGINE=InnoDB, CHARSET, COLLATE:** Presentes en varias migraciones (V1_25, V1_28, V1_29, V1_102, V1_105, V1_112, etc.). Son específicos de MySQL; H2 no los soporta. **No impactan tests** porque Flyway está deshabilitado en test.
- **AUTO_INCREMENT:** Usado en muchas migraciones. H2 soporta IDENTITY/AUTO_INCREMENT con sintaxis compatible en modo MySQL.
- **MODIFY COLUMN ... COMMENT:** Por ejemplo en V1_4 (historial_cosechas). Sintaxis MySQL; H2 no soporta COMMENT en columnas. No se ejecuta en test.
- **Vistas con DATEDIFF, CURDATE(), ROUND:** Por ejemplo en V1_4 (vista_historial_cosechas_completo, vista_lotes_requieren_descanso). Son vistas definidas en Flyway; no se crean en test.
- **RENAME TABLE:** V1_99 renombra tablas a prefijos cultivo_/porcinos_. Solo relevante para MySQL en despliegue.

**Conclusión:** Las migraciones Flyway son **incompatibles con H2** tal cual (InnoDB, COMMENT, vistas con funciones MySQL). Para tests esto **no es un problema** porque Flyway está deshabilitado y el esquema se crea con Hibernate + H2.

---

## 5. Configuración JPA

### 5.1 Producción / desarrollo

- **application.properties / application-agrocloud.properties / application-prod.properties:**  
  `hibernate.dialect=MySQLDialect` o `MySQL8Dialect`, `ddl-auto=none` o `validate`.
- **application-railway-h2.properties:** `H2Dialect`, `ddl-auto=create-drop` (entorno H2 en Railway).
- No se encontró configuración explícita de **PhysicalNamingStrategy** ni **ImplicitNamingStrategy**; se usan los valores por defecto de Hibernate/Spring Boot.

### 5.2 Tests

- **application-test.properties:**  
  `spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_LOWER=TRUE`  
  `spring.jpa.hibernate.ddl-auto=create-drop`  
  `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect`  
  `spring.flyway.enabled=false`

H2 se usa en **modo compatibilidad MySQL** (`MODE=MySQL`) y con nombres en minúsculas (`DATABASE_TO_LOWER=TRUE`), lo que acerca el comportamiento al de MySQL y mantiene la coincidencia con los nombres de tablas usados en las consultas nativas.

### 5.3 Transacciones y aislamiento

- No se encontró configuración explícita de isolation level en los archivos de propiedades revisados.
- No se encontró uso de `@Transactional(propagation = ...)` con REQUIRES_NEW, MANDATORY, NESTED ni propagaciones especiales en el código revisado.

---

## 6. Resumen de hallazgos

| # | Hallazgo | Riesgo H2 | Notas |
|---|----------|-----------|--------|
| 1 | Dos consultas nativas (EmpresaRepository, UserRepository) que usan tablas del módulo agrícola | Bajo | SQL estándar; nombres de tablas alineados con @Table. |
| 2 | Field.coordenadas con columnDefinition = "JSON" | Medio | Posible diferencia de tipo/DDL entre MySQL y H2; contenido puede ser string. |
| 3 | Resto de columnDefinition en módulo agrícola: TEXT | Nulo | Portable. |
| 4 | @Enumerated(EnumType.STRING) en todo el proyecto | Nulo | Portable. |
| 5 | Repositorios del módulo agrícola (Plot, Labor, HistorialCosecha, Cultivo, EstadoLoteConfig): solo JPQL | Nulo | Sin SQL nativo. |
| 6 | Flyway deshabilitado en test; migraciones con MySQL (InnoDB, COMMENT, vistas) | Nulo para tests | No se ejecutan en test. |
| 7 | ON DUPLICATE KEY en scripts SQL de datos/inicialización | Nulo para tests | No usados en tests. |
| 8 | Sin FOR UPDATE, LOCK IN SHARE MODE ni @Lock PESSIMISTIC | Nulo | No hay locking MySQL-específico en repos. |
| 9 | H2 en test con MODE=MySQL y DATABASE_TO_LOWER=TRUE | Reduce riesgo | Mejor alineación con nombres y comportamiento. |

---

## 7. ¿Es suficiente H2 para los tests actuales?

**Sí, desde el punto de vista de compatibilidad SQL/dialecto.** No hay dependencias de MySQL en el código del módulo agrícola que impidan usar H2 en tests, siempre que:

1. El esquema se cree correctamente en test (Hibernate con ddl-auto=create-drop). El fallo actual (“Table usuarios not found; this database is empty”) indica un problema de **orden de inicialización o de contexto** (por ejemplo, EntityManagerFactory no creando el esquema antes del primer uso), no de incompatibilidad de consultas o DDL con H2.
2. La columna `coordenadas` (JSON) en Field se acepte por H2 (p. ej. como VARCHAR/CLOB o JSON según versión); si en algún test se persiste/lee ese campo, conviene comprobar que no haya errores de tipo.

Recomendación operativa: **corregir la configuración o el orden de arranque del contexto de test** para que Hibernate cree el esquema antes de ejecutar el `@BeforeEach` que hace `userRepository.save(usuario)`. No es necesario cambiar a Testcontainers solo por compatibilidad de consultas o DDL del módulo agrícola.

---

## 8. ¿Se recomienda migrar a Testcontainers?

**No como requisito** para que los tests del módulo agrícola pasen. La auditoría no encontró:

- SQL nativo con funciones MySQL en repositorios del módulo agrícola.
- Uso de FOR UPDATE, LOCK IN SHARE MODE ni locking pesimista.
- Dependencia de vistas Flyway o de scripts con ON DUPLICATE KEY en la ruta de ejecución de tests.
- Comportamiento transaccional o de aislamiento que exija MySQL.

**Testcontainers podría estar justificado** en otros casos, no por el módulo agrícola en sí:

- Si se quisiera ejecutar **Flyway en test** sobre la misma base que en producción (esquema idéntico, incluidas vistas y sintaxis MySQL).
- Si se añadieran en el futuro consultas nativas con funciones MySQL (IFNULL, DATE_ADD, GROUP_CONCAT, etc.) o características como JSON nativo MySQL.
- Si se requiriera validar el mismo motor que producción (MySQL) por política de calidad o compliance.

**Justificación técnica resumida:** Con la configuración actual (Flyway deshabilitado en test, esquema vía Hibernate, JPQL en el módulo agrícola, consultas nativas estándar y H2 en MODE=MySQL), **H2 es suficiente** para los tests del módulo agrícola. La migración a Testcontainers es opcional y dependería de requisitos de fidelidad al entorno productivo o de evolución futura del código (más SQL nativo MySQL, uso de Flyway en test, etc.).

---

## 9. Lista de archivos revisados (resumen)

- **Repositorios:** EmpresaRepository, UserRepository, PlotRepository (cultivos), LaborRepository, HistorialCosechaRepository, CultivoRepository, EstadoLoteConfigRepository, MovimientoInventarioRepository, y otros con @Query.
- **Entidades (módulo agrícola y relacionadas):** Plot, Labor, Field, Cultivo, HistorialCosecha, y entidades con columnDefinition.
- **Configuración:** application.properties, application-test.properties, application-*-mysql.properties, application-railway-h2.properties.
- **Migraciones:** Flyway en `db/migration` (búsqueda de ENGINE, AUTO_INCREMENT, COMMENT, vistas, RENAME) y scripts SQL en `src/main/resources/sql` y raíz del backend.
- **Locking/transacciones:** Búsqueda de @Lock, LockModeType, FOR UPDATE, LOCK IN SHARE MODE, ON DUPLICATE KEY, propagación transaccional especial.

---

*Auditoría realizada sin modificación de código. Solo análisis y reporte.*
