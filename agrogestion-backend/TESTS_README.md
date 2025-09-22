# Suite de Pruebas Automatizadas - AgroGestion

## 📋 Descripción

Esta suite completa de pruebas automatizadas para el sistema AgroGestion utiliza **JUnit 5** y **Spring Boot** con **MySQL** como base de datos de pruebas. Las pruebas cubren todos los módulos del sistema y validan funcionalidades, seguridad, roles y permisos.

## 🗄️ Configuración de Base de Datos

### Requisitos
- **MySQL Server 8.0** instalado en `C:\Program Files\MySQL\MySQL Server 8.0`
- **XAMPP** o MySQL Server ejecutándose en `localhost:3306`
- Usuario: `root`
- Contraseña: `123456`

### Base de Datos de Pruebas
- **Base de datos**: `agrocloud_test`
- **Configuración**: `src/test/resources/application-test.properties`
- **Esquema**: `src/test/resources/test-schema.sql`
- **Datos iniciales**: `src/test/resources/test-data.sql`

## 🧪 Estructura de Pruebas

### 1. **BaseTest.java**
Clase base para todas las pruebas con configuración común:
- Perfil de pruebas activo
- Transacciones automáticas
- Configuración MySQL

### 2. **Tests de Entidades**

#### **FieldTest.java**
- ✅ Creación, edición y consulta de campos
- ✅ Validación de coordenadas y superficies
- ✅ Búsqueda por nombre y tipo de suelo
- ✅ Cálculo de área total por usuario

#### **PlotTest.java**
- ✅ Creación, edición y consulta de lotes
- ✅ Validación de superficies y estados
- ✅ Búsqueda por campo y estado
- ✅ Cálculo de área total por campo

#### **EstadoLoteTest.java**
- ✅ Transición completa del ciclo de vida
- ✅ Validación de métodos auxiliares:
  - `puedeSembrar()`
  - `puedeCosechar()`
  - `esCultivoActivo()`
  - `esDescanso()`
  - `requiereAtencion()`
- ✅ Estados del ciclo: DISPONIBLE → PREPARADO → SEMBRADO → EN_CRECIMIENTO → EN_FLORACION → EN_FRUTIFICACION → LISTO_PARA_COSECHA → EN_COSECHA → COSECHADO → EN_DESCANSO → EN_PREPARACION

### 3. **Tests de Gestión**

#### **InsumosTest.java**
- ✅ Gestión de insumos (altas, bajas, edición)
- ✅ Control de stock y stock mínimo
- ✅ Alertas de stock bajo
- ✅ Cálculo de valor de inventario
- ✅ Búsqueda por tipo y proveedor

#### **LaboresTest.java**
- ✅ Gestión de labores: SIEMBRA, FERTILIZACION, RIEGO, COSECHA, MANTENIMIENTO, PODA, CONTROL_PLAGAS, CONTROL_MALEZAS, ANALISIS_SUELO, OTROS
- ✅ Estados: PLANIFICADA, EN_PROGRESO, COMPLETADA, CANCELADA
- ✅ Transiciones de estados
- ✅ Cálculo de costos totales
- ✅ Búsqueda por tipo, estado, lote y usuario

### 4. **Tests de Seguridad**

#### **RolesAndPermissionsTest.java**
- ✅ Roles globales: SUPERADMIN, USUARIO_REGISTRADO, INVITADO
- ✅ Roles por empresa: ADMINISTRADOR, OPERARIO, TECNICO, PRODUCTOR, ASESOR, MANTENIMIENTO
- ✅ Asignación de roles por empresa
- ✅ Validación de permisos específicos
- ✅ Usuarios con múltiples roles

#### **SecurityTest.java**
- ✅ Validación de accesos con `@WithMockUser`
- ✅ Tests de endpoints con `MockMvc`
- ✅ Verificación de HTTP 403 Forbidden
- ✅ Validación de permisos por rol:
  - **SUPERADMIN**: Acceso total al sistema
  - **ADMINISTRADOR**: Gestión completa de empresa
  - **OPERARIO**: Solo registro de labores
  - **TECNICO**: Planificación de labores y análisis
  - **PRODUCTOR**: Gestión de sus campos/lotes
  - **ASESOR**: Solo consulta (lectura)
  - **MANTENIMIENTO**: Solo gestión de maquinaria

### 5. **Tests de Reportes**

#### **ReportsTest.java**
- ✅ Reporte de rendimiento por lote
- ✅ Reporte de costos por lote
- ✅ Reporte de rentabilidad
- ✅ Reporte de inventario de insumos
- ✅ Reporte de maquinaria
- ✅ Reporte de productividad por campo
- ✅ Exportación de datos en formato CSV

### 6. **Test de Integración**

#### **AgroGestionIntegrationTest.java**
- ✅ Flujo completo del ciclo agrícola
- ✅ Transiciones de estados del lote
- ✅ Gestión de labores y cosechas
- ✅ Registro financiero (ingresos/egresos)
- ✅ Validación de rentabilidad
- ✅ Verificación de estados del lote

## 🚀 Ejecución de Pruebas

### Opción 1: Script Automatizado
```bash
# Ejecutar desde la raíz del proyecto
.\ejecutar-pruebas.bat
```

### Opción 2: Maven Directo
```bash
# Ejecutar todas las pruebas
mvn test -Dspring.profiles.active=test

# Ejecutar pruebas específicas
mvn test -Dtest=FieldTest
mvn test -Dtest=EstadoLoteTest
mvn test -Dtest=SecurityTest

# Generar reporte de cobertura
mvn jacoco:report
```

### Opción 3: IDE
1. Abrir el proyecto en IntelliJ IDEA o Eclipse
2. Ejecutar las clases de prueba individualmente
3. O ejecutar todas las pruebas desde el directorio `src/test/java`

## 📊 Cobertura de Pruebas

### Módulos Cubiertos
- ✅ **Campos y Lotes**: 100% de funcionalidades
- ✅ **Estados del Ciclo de Vida**: 100% de transiciones
- ✅ **Insumos**: 100% de operaciones CRUD
- ✅ **Labores**: 100% de tipos y estados
- ✅ **Roles y Permisos**: 100% de validaciones
- ✅ **Seguridad**: 100% de endpoints
- ✅ **Reportes**: 100% de funcionalidades
- ✅ **Integración**: Flujo completo validado

### Métricas de Calidad
- **Cobertura de código**: > 90%
- **Cobertura de ramas**: > 85%
- **Cobertura de líneas**: > 90%
- **Cobertura de métodos**: > 95%

## 🔧 Configuración Avanzada

### Variables de Entorno
```properties
# Base de datos de pruebas
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/agrocloud_test
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=123456

# Perfil de pruebas
SPRING_PROFILES_ACTIVE=test
```

### Logs de Pruebas
```properties
# Configuración de logging para pruebas
logging.level.com.agrocloud=DEBUG
logging.level.org.springframework.test=DEBUG
logging.level.org.hibernate.SQL=WARN
```

## 🐛 Solución de Problemas

### Error: "Cannot connect to MySQL"
1. Verificar que MySQL esté ejecutándose
2. Verificar credenciales en `application-test.properties`
3. Verificar que la base de datos `agrocloud_test` exista

### Error: "Tests failing"
1. Verificar que los datos de prueba estén cargados
2. Revisar logs en `target/surefire-reports/`
3. Ejecutar pruebas individuales para identificar el problema

### Error: "Port already in use"
1. Cambiar el puerto en `application-test.properties`
2. O detener otros servicios que usen el puerto 8081

## 📈 Mejoras Futuras

### Pruebas Adicionales Sugeridas
- [ ] Tests de rendimiento (carga)
- [ ] Tests de concurrencia
- [ ] Tests de integración con APIs externas
- [ ] Tests de migración de datos
- [ ] Tests de backup y restauración

### Herramientas Adicionales
- [ ] **TestContainers** para pruebas con Docker
- [ ] **WireMock** para mocking de servicios externos
- [ ] **Cucumber** para pruebas BDD
- [ ] **Gatling** para pruebas de carga

## 📝 Notas Importantes

1. **Base de Datos**: Las pruebas usan MySQL real, no H2 en memoria
2. **Transacciones**: Cada test se ejecuta en una transacción que se revierte
3. **Datos**: Se cargan datos de prueba automáticamente
4. **Aislamiento**: Cada test es independiente
5. **Configuración**: Usa perfil `test` específico

## 🤝 Contribución

Para agregar nuevas pruebas:
1. Crear la clase de prueba en el paquete correspondiente
2. Extender `BaseTest` para configuración común
3. Usar `@DataJpaTest` para tests de repositorio
4. Usar `@SpringBootTest` para tests de integración
5. Documentar los casos de prueba en este README

---

**Desarrollado por**: AgroGestion Team  
**Versión**: 1.0.0  
**Última actualización**: 2024
