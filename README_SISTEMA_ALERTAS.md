# 🚨 Sistema de Alertas Inteligentes - AgroGestion

## 📋 Descripción General

El **Sistema de Alertas Inteligentes** es un módulo avanzado que proporciona notificaciones automáticas y proactivas sobre eventos críticos en la gestión agrícola. Integra datos meteorológicos, programación de tareas, gestión de inventario y mantenimiento para mantener informados a los usuarios sobre situaciones que requieren atención inmediata.

## ✨ Características Principales

### 🌤️ **Alertas Meteorológicas**
- **Integración con API Open-Meteo**: Datos meteorológicos en tiempo real
- **Detección de Heladas**: Alertas cuando la temperatura desciende a 0°C o menos
- **Lluvias Excesivas**: Notificaciones cuando la probabilidad de lluvia supera el 80%
- **Tormentas**: Alertas críticas para tormentas eléctricas previstas
- **Sequías**: Monitoreo de períodos sin precipitaciones

### 📅 **Alertas de Tareas Programadas**
- **Recordatorios Automáticos**: Notificaciones sobre tareas próximas a ejecutar
- **Priorización Inteligente**: 
  - **CRÍTICA**: Tareas programadas para HOY
  - **ALTA**: Tareas programadas para MAÑANA
  - **MEDIA**: Tareas en los próximos 3 días
  - **BAJA**: Tareas en los próximos 7 días
- **Vinculación con Lotes**: Asociación directa con lotes específicos

### 📦 **Alertas de Gestión de Stock**
- **Stock Bajo**: Notificaciones cuando los insumos están por debajo del mínimo
- **Vencimiento Próximo**: Alertas 30 días antes del vencimiento
- **Stock Crítico**: Cuando el stock está por debajo del 50% del mínimo
- **Categorización por Tipo**: Fertilizantes, pesticidas, semillas, etc.

### 🔧 **Alertas de Mantenimiento**
- **Mantenimiento Programado**: Recordatorios de mantenimiento preventivo
- **Maquinaria**: Alertas para tractores, cosechadoras, etc.
- **Equipos de Riego**: Mantenimiento de sistemas de riego
- **Herramientas**: Mantenimiento de equipos menores

## 🏗️ Arquitectura del Sistema

### 📊 **Entidades Principales**

#### `Alerta` (Tabla: `alertas`)
```sql
- id: Identificador único
- tipo_alerta: ENUM (CLIMA_HELADA, CLIMA_LLUVIA_EXCESIVA, CLIMA_SEQUIA, CLIMA_TORMENTA, TAREA_PROGRAMADA, MANTENIMIENTO_MAQUINARIA, STOCK_BAJO, INSUMO_VENCIMIENTO, RENDIMIENTO_BAJO, COSTO_ELEVADO)
- nivel_prioridad: ENUM (BAJA, MEDIA, ALTA, CRITICA)
- titulo: Título descriptivo de la alerta
- descripcion: Descripción detallada
- fecha_creacion: Timestamp de creación
- fecha_vencimiento: Timestamp de vencimiento (opcional)
- leida: Estado de lectura
- activa: Estado de activación
- datos_adicionales: JSON con datos específicos
- usuario_id: Usuario relacionado
- lote_id: Lote relacionado
- labor_id: Labor relacionada
- insumo_id: Insumo relacionado
```

### 🔄 **Flujo de Generación Automática**

1. **Programación Diaria**: Ejecución automática a las 6:00 AM
2. **Análisis Meteorológico**: Consulta a API Open-Meteo
3. **Evaluación de Tareas**: Revisión de labores programadas
4. **Control de Stock**: Verificación de inventarios
5. **Mantenimiento**: Revisión de tareas de mantenimiento
6. **Limpieza**: Desactivación de alertas vencidas

## 🚀 Instalación y Configuración

### 📋 **Prerrequisitos**
- Base de datos MySQL/MariaDB configurada
- Spring Boot 3.x
- React 18+
- API Key de Open-Meteo (opcional, usa coordenadas por defecto)

### 🔧 **Configuración del Backend**

#### 1. **Dependencias en `pom.xml`**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

#### 2. **Configuración en `application.properties`**
```properties
# Configuración de la API del clima
app.weather.api.url=https://api.open-meteo.com/v1/forecast
app.weather.latitude=-34.6037
app.weather.longitude=-58.3816

# Configuración de programación
spring.task.scheduling.pool.size=5
```

#### 3. **Ejecutar Scripts SQL**
```bash
# 1. Crear tabla de alertas
mysql -u root -p agrogestion < crear_tabla_alertas.sql

# 2. Agregar datos de prueba
mysql -u root -p agrogestion < datos_prueba_alertas.sql
```

### 🎨 **Configuración del Frontend**

#### 1. **Instalar Dependencias**
```bash
cd agrogestion-frontend
npm install @mui/material @emotion/react @emotion/styled
npm install @mui/icons-material
npm install date-fns
```

#### 2. **Importar Componente**
```typescript
import AlertasManagement from './components/AlertasManagement';
```

## 📱 Interfaz de Usuario

### 🎯 **Dashboard de Alertas**
- **Métricas en Tiempo Real**: Total de alertas, no leídas, críticas
- **Gráficos de Distribución**: Por tipo y prioridad
- **Alertas Críticas Recientes**: Lista de las más importantes
- **Filtros Avanzados**: Por tipo, prioridad, estado de lectura

### 🔍 **Funcionalidades de Filtrado**
- **Búsqueda por Texto**: En título y descripción
- **Filtro por Tipo**: Clima, tareas, stock, mantenimiento
- **Filtro por Prioridad**: Crítica, alta, media, baja
- **Filtro por Estado**: Leídas, no leídas, todas

### ✅ **Gestión de Estado**
- **Marcar como Leída**: Individual o masiva
- **Detalles Completos**: Modal con información detallada
- **Acciones Rápidas**: Botones de acción directa
- **Generación Manual**: Para administradores

## 🔌 API Endpoints

### 📊 **Endpoints Principales**

#### `GET /api/alertas`
Obtiene todas las alertas activas
```json
{
  "id": 1,
  "tipoAlerta": "CLIMA_HELADA",
  "nivelPrioridad": "ALTA",
  "titulo": "Riesgo de helada",
  "descripcion": "Temperatura prevista: -2.5°C",
  "fechaCreacion": "2024-12-15T06:00:00",
  "leida": false,
  "activa": true
}
```

#### `GET /api/alertas/dashboard`
Obtiene estadísticas del dashboard
```json
{
  "totalAlertas": 15,
  "alertasNoLeidas": 8,
  "alertasCriticas": 3,
  "alertasAltas": 5,
  "alertasMedias": 4,
  "alertasBajas": 3,
  "alertasPorTipo": {
    "CLIMA_HELADA": 2,
    "TAREA_PROGRAMADA": 5,
    "STOCK_BAJO": 3
  },
  "alertasPorPrioridad": {
    "CRITICA": 3,
    "ALTA": 5,
    "MEDIA": 4,
    "BAJA": 3
  }
}
```

#### `PUT /api/alertas/{id}/marcar-leida`
Marca una alerta como leída

#### `PUT /api/alertas/marcar-todas-leidas`
Marca todas las alertas como leídas

#### `POST /api/alertas/generar-manual`
Genera alertas manualmente (solo administradores)

### 🔍 **Endpoints de Filtrado**

#### `GET /api/alertas/tipo/{tipoAlerta}`
Filtra por tipo de alerta

#### `GET /api/alertas/prioridad/{nivelPrioridad}`
Filtra por nivel de prioridad

#### `GET /api/alertas/buscar?termino=texto`
Búsqueda por término

#### `GET /api/alertas/clima`
Solo alertas meteorológicas

#### `GET /api/alertas/tareas`
Solo alertas de tareas programadas

#### `GET /api/alertas/stock`
Solo alertas de stock

#### `GET /api/alertas/mantenimiento`
Solo alertas de mantenimiento

## 🔧 Configuración Avanzada

### 🌍 **Personalización de Coordenadas**
```properties
# Configurar coordenadas de tu ubicación
app.weather.latitude=-34.6037
app.weather.longitude=-58.3816
```

### ⏰ **Programación Personalizada**
```java
// Cambiar frecuencia de ejecución
@Scheduled(cron = "0 0 6 * * ?") // Diario a las 6:00 AM
@Scheduled(cron = "0 */2 * * * ?") // Cada 2 horas
@Scheduled(cron = "0 0 */6 * * ?") // Cada 6 horas
```

### 🎯 **Umbrales Personalizables**
```java
// Umbrales de temperatura para heladas
if (temp <= 0) // Configurable

// Umbrales de probabilidad de lluvia
if (precipProb > 80) // Configurable

// Días antes del vencimiento
LocalDate fechaLimite = hoy.plusDays(30); // Configurable
```

## 📊 Monitoreo y Logs

### 📝 **Logs del Sistema**
```java
logger.info("Alerta de clima creada: {}", titulo);
logger.info("Generación automática de alertas completada");
logger.error("Error al obtener datos del clima", e);
```

### 📈 **Métricas de Rendimiento**
- Tiempo de respuesta de la API
- Número de alertas generadas por día
- Tasa de alertas leídas vs no leídas
- Distribución por tipo y prioridad

## 🛠️ Mantenimiento

### 🔄 **Tareas Programadas**
- **Limpieza Automática**: Eliminación de alertas vencidas
- **Optimización de Base de Datos**: Índices automáticos
- **Backup de Configuración**: Respaldo de parámetros

### 🐛 **Solución de Problemas**

#### **Alertas no se generan**
1. Verificar conectividad con API del clima
2. Revisar logs de Spring Boot
3. Verificar configuración de coordenadas
4. Comprobar permisos de base de datos

#### **Alertas duplicadas**
1. Verificar lógica de deduplicación
2. Revisar intervalos de ejecución
3. Comprobar filtros de tiempo

#### **Rendimiento lento**
1. Optimizar consultas de base de datos
2. Revisar índices
3. Implementar paginación
4. Usar caché para datos meteorológicos

## 🔮 Futuras Mejoras

### 🚀 **Funcionalidades Planificadas**
- **Notificaciones Push**: Integración con servicios móviles
- **Alertas por Email**: Notificaciones por correo electrónico
- **Alertas por SMS**: Mensajes de texto para emergencias
- **Machine Learning**: Predicción de alertas basada en patrones
- **Integración IoT**: Sensores de campo en tiempo real
- **Mapas Interactivos**: Visualización geográfica de alertas
- **Alertas Personalizadas**: Configuración por usuario
- **Historial de Alertas**: Análisis histórico y tendencias

### 🔗 **Integraciones Futuras**
- **WhatsApp Business API**: Notificaciones por WhatsApp
- **Telegram Bot**: Bot de Telegram para alertas
- **Slack Integration**: Notificaciones en canales de trabajo
- **Microsoft Teams**: Integración con Teams
- **Google Calendar**: Sincronización con calendarios
- **Weather APIs**: Múltiples proveedores meteorológicos

## 📞 Soporte

### 🆘 **Contacto**
- **Email**: soporte@agrogestion.com
- **Documentación**: [docs.agrogestion.com](https://docs.agrogestion.com)
- **GitHub**: [github.com/agrogestion/alertas](https://github.com/agrogestion/alertas)

### 📚 **Recursos Adicionales**
- **Guía de Usuario**: [Manual de Usuario](https://docs.agrogestion.com/manual)
- **API Documentation**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- **Videos Tutoriales**: [YouTube Channel](https://youtube.com/agrogestion)

---

**© 2024 AgroGestion - Sistema de Gestión Agrícola Inteligente**
