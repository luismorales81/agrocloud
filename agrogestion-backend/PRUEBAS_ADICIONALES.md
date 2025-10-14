# 🧪 Pruebas Adicionales - AgroGestion

## 📋 Resumen

Este documento describe el conjunto completo de pruebas adicionales implementadas para el proyecto AgroGestion, incluyendo pruebas de integración de endpoints REST, pruebas de regresión en GitHub Actions y pruebas de performance.

---

## 🚀 1. Pruebas de Integración de Endpoints REST

### ✅ **Implementadas con MockMvc**

#### **FieldControllerIntegrationTest**
- **CRUD completo** para la entidad Field (Campos)
- **Validaciones de códigos HTTP**: 200, 201, 400, 404, 204
- **Validaciones de JSON**: Estructura y contenido de respuestas
- **Casos de error**: Datos inválidos, recursos no encontrados

#### **InsumoControllerIntegrationTest**
- **CRUD completo** para la entidad Insumo
- **Filtros especializados**: Por tipo, stock bajo
- **Validaciones de negocio**: Precios, stock, fechas de vencimiento
- **Endpoints específicos**: `/api/v1/insumos/stock-bajo`

#### **AuthControllerIntegrationTest**
- **Autenticación completa**: Login, cambio de contraseñas
- **Seguridad**: Validación de tokens, usuarios inactivos
- **Recuperación de contraseñas**: Request y reset
- **Endpoints de utilidad**: Test, generación de hash

#### **RoleControllerIntegrationTest**
- **Gestión de roles**: CRUD completo
- **Permisos**: Asignación y consulta de permisos
- **Estadísticas**: Métricas de roles y usuarios
- **Validaciones de negocio**: Nombres únicos, conflictos

### 📊 **Métricas de Cobertura**
- **Total de tests**: 40+ pruebas de integración
- **Endpoints cubiertos**: 15+ endpoints REST
- **Códigos HTTP validados**: 200, 201, 400, 404, 409, 204, 401
- **Casos de error**: 20+ escenarios de fallo

---

## 🔄 2. Pruebas de Regresión en GitHub Actions

### ✅ **Workflow: integration-tests.yml**

#### **Jobs Implementados:**
1. **integration-tests**: Pruebas de integración REST
2. **regression-tests**: Pruebas de regresión completas
3. **quality-gate**: Validación de calidad con SonarQube
4. **notify**: Notificaciones de resultados
5. **deploy**: Deployment automático (solo en main)

#### **Características:**
- **Base de datos MySQL**: Servicio configurado automáticamente
- **Cache de dependencias**: Optimización de tiempos de build
- **Reportes de cobertura**: Integración con Codecov
- **Bloqueo de merge**: PRs bloqueados si fallan tests
- **Notificaciones**: Comentarios automáticos en PRs

#### **Triggers:**
- **Push**: A branches main y develop
- **Pull Request**: A branches main y develop
- **Manual**: Workflow dispatch
- **Schedule**: Ejecución programada

### 📈 **Métricas de CI/CD**
- **Tiempo de ejecución**: ~15-20 minutos
- **Paralelización**: Jobs independientes
- **Retry logic**: Reintentos automáticos
- **Artifacts**: Reportes y logs preservados

---

## ⚡ 3. Pruebas de Performance

### ✅ **Herramientas Implementadas**

#### **JMeter Integration**
- **Plan de pruebas**: Configurado para endpoints clave
- **Escenarios de carga**: 20-50 usuarios concurrentes
- **Métricas**: Tiempo de respuesta, throughput, errores
- **Reportes HTML**: Generación automática de reportes

#### **Endpoints Probados**
- `GET /api/auth/test`: Endpoint de salud
- `GET /api/v1/campos`: Listado de campos
- `GET /api/v1/insumos`: Listado de insumos

#### **Configuración de Carga**
- **Usuarios concurrentes**: 20 (local), 50 (CI)
- **Ramp-up time**: 10s (local), 30s (CI)
- **Iteraciones**: 5 (local), 10 (CI)
- **Duración**: ~2-5 minutos

### 📊 **Métricas de Performance**
- **Tiempo de respuesta promedio**: < 200ms
- **Throughput**: > 100 requests/segundo
- **Tasa de error**: < 1%
- **Tiempo máximo**: < 1000ms

---

## 🛠️ 4. Instrucciones de Ejecución

### **Pruebas de Integración**

#### **Ejecutar todas las pruebas:**
```bash
./mvnw test -Dtest=IntegrationTestSuite
```

#### **Ejecutar pruebas específicas:**
```bash
# Solo pruebas de campos
./mvnw test -Dtest=FieldControllerIntegrationTest

# Solo pruebas de autenticación
./mvnw test -Dtest=AuthControllerIntegrationTest
```

### **Pruebas de Performance**

#### **Windows:**
```cmd
scripts\ejecutar-pruebas-performance.bat
```

#### **Linux/Mac:**
```bash
./scripts/ejecutar-pruebas-performance.sh
```

#### **Requisitos:**
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- JMeter 5.5+ (descargado automáticamente)

### **GitHub Actions**

#### **Ejecutar manualmente:**
1. Ir a la pestaña "Actions" en GitHub
2. Seleccionar "Pruebas de Integración y Regresión"
3. Hacer clic en "Run workflow"

#### **Configurar secrets:**
- `SONAR_HOST_URL`: URL de SonarQube
- `SONAR_TOKEN`: Token de SonarQube
- `GITHUB_TOKEN`: Token de GitHub (automático)

---

## 📈 5. Reportes y Métricas

### **Reportes Generados**

#### **Pruebas de Integración:**
- **Surefire reports**: `target/surefire-reports/`
- **Cobertura**: `target/site/jacoco/`
- **Logs**: Console output con detalles

#### **Pruebas de Performance:**
- **Reporte HTML**: `performance-reports/performance-report/index.html`
- **Resumen**: `performance-reports/performance-summary.md`
- **Datos brutos**: `performance-reports/performance-results.jtl`

#### **GitHub Actions:**
- **Artifacts**: Reportes y logs descargables
- **Comentarios**: Resultados en PRs
- **Badges**: Estado de build en README

### **Métricas Clave**

#### **Cobertura de Código:**
- **Líneas cubiertas**: > 80%
- **Branches cubiertos**: > 75%
- **Métodos cubiertos**: > 85%

#### **Performance:**
- **Tiempo de respuesta**: < 200ms promedio
- **Throughput**: > 100 req/s
- **Disponibilidad**: > 99.9%

---

## 🔧 6. Configuración y Personalización

### **Personalizar Pruebas de Performance**

#### **Modificar carga:**
Editar `performance-test.jmx`:
```xml
<stringProp name="ThreadGroup.num_threads">50</stringProp>
<stringProp name="ThreadGroup.ramp_time">30</stringProp>
<stringProp name="LoopController.loops">10</stringProp>
```

#### **Agregar endpoints:**
Agregar nuevos `HTTPSamplerProxy` en el plan JMeter.

### **Personalizar GitHub Actions**

#### **Modificar triggers:**
```yaml
on:
  push:
    branches: [ main, develop, feature/* ]
  pull_request:
    branches: [ main ]
```

#### **Agregar jobs:**
```yaml
new-job:
  name: Nuevo Job
  runs-on: ubuntu-latest
  steps:
    - name: Paso personalizado
      run: echo "Hola mundo"
```

---

## 🚨 7. Troubleshooting

### **Problemas Comunes**

#### **Tests fallan en CI pero pasan localmente:**
- Verificar configuración de base de datos
- Revisar variables de entorno
- Comprobar timeouts y recursos

#### **Performance tests fallan:**
- Verificar que la aplicación esté iniciada
- Comprobar conectividad a puerto 8080
- Revisar logs de la aplicación

#### **GitHub Actions fallan:**
- Verificar secrets configurados
- Revisar permisos del repositorio
- Comprobar límites de recursos

### **Logs y Debugging**

#### **Ver logs de aplicación:**
```bash
tail -f app.log
```

#### **Ver logs de JMeter:**
```bash
tail -f jmeter.log
```

#### **Debug en GitHub Actions:**
- Revisar logs del job
- Descargar artifacts
- Verificar configuración de servicios

---

## 📚 8. Referencias y Enlaces

### **Documentación:**
- [Spring Boot Testing](https://spring.io/guides/gs/testing-web/)
- [MockMvc Reference](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html#spring-mvc-test-framework)
- [JMeter User Manual](https://jmeter.apache.org/usermanual/)

### **Herramientas:**
- [GitHub Actions](https://docs.github.com/en/actions)
- [SonarQube](https://docs.sonarqube.org/)
- [Codecov](https://docs.codecov.com/)

### **Recursos del Proyecto:**
- [README Principal](../README.md)
- [Configuración de Tests](OPTIMIZACIONES_TESTS.md)
- [Reporte de Cambios](REPORTE_CAMBIOS_COMPATIBILIDAD.md)

---

## 🎯 9. Próximos Pasos

### **Mejoras Planificadas:**
1. **Tests de carga avanzados**: Simulación de usuarios reales
2. **Monitoreo en tiempo real**: Métricas de producción
3. **Tests de seguridad**: Penetration testing
4. **Tests de compatibilidad**: Múltiples versiones de Java/MySQL

### **Métricas a Implementar:**
1. **SLA monitoring**: Tiempo de respuesta garantizado
2. **Error rate tracking**: Tasa de errores en producción
3. **User experience metrics**: Tiempo de carga de páginas
4. **Database performance**: Queries lentas y optimización

---

## ✅ 10. Checklist de Implementación

### **Pruebas de Integración:**
- [x] FieldControllerIntegrationTest
- [x] InsumoControllerIntegrationTest
- [x] AuthControllerIntegrationTest
- [x] RoleControllerIntegrationTest
- [x] IntegrationTestSuite

### **GitHub Actions:**
- [x] Workflow de integración
- [x] Workflow de performance
- [x] Configuración de MySQL
- [x] Reportes de cobertura
- [x] Notificaciones

### **Performance Testing:**
- [x] Scripts de ejecución local
- [x] Plan de pruebas JMeter
- [x] Reportes HTML
- [x] Métricas de rendimiento

### **Documentación:**
- [x] Instrucciones de ejecución
- [x] Troubleshooting guide
- [x] Configuración y personalización
- [x] Referencias y enlaces

---

**🎉 ¡Implementación Completa de Pruebas Adicionales!**

Todas las pruebas están listas para uso en desarrollo, CI/CD y producción. El sistema ahora cuenta con una cobertura completa de testing que garantiza la calidad y rendimiento del código.
