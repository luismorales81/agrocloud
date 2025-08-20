# Módulo de Gestión Económica - AgroGestion

## Descripción

El módulo de **Gestión Económica** permite el control completo de las operaciones financieras de la empresa agrícola, incluyendo ingresos por ventas, egresos por insumos y costos operativos, cálculo de rentabilidad por cultivo y lote, y generación de reportes económicos detallados.

## Características Principales

### 📊 Dashboard Económico
- **Métricas en tiempo real**: Ingresos totales, egresos totales, rentabilidad y margen bruto
- **Análisis por período**: Comparación mes actual vs año actual
- **Top egresos**: Categorías con mayor impacto en costos
- **Rentabilidad por cultivo**: Análisis de rentabilidad por tipo de cultivo
- **Evolución temporal**: Gráficos de tendencias mensuales y anuales

### 💰 Gestión de Operaciones
- **Registro de ingresos**: Venta de granos, subproductos, subsidios
- **Registro de egresos**: Insumos, combustible, mantenimiento, mano de obra
- **Asociación con lotes**: Vinculación opcional de operaciones con lotes específicos
- **Categorización**: Sistema de categorías para mejor organización
- **Comprobantes**: Soporte para adjuntar documentos de respaldo

### 📈 Análisis y Reportes
- **Costos por lote**: Cálculo automático de costos por hectárea
- **Rentabilidad por cultivo**: Análisis de rentabilidad por tipo de cultivo
- **Comparación proyecciones**: Análisis de desviaciones vs presupuesto
- **Exportación**: Reportes en PDF y Excel

## Arquitectura del Sistema

### Backend (Spring Boot)

#### Entidades
- **`OperacionEconomica`**: Entidad principal para operaciones económicas
  - Campos: id, tipo (INGRESO/EGRESO), monto, fecha, lote, descripción, categoría
  - Enums: TipoOperacion, CategoriaOperacion
  - Relaciones: ManyToOne con Lote y User

#### DTOs
- **`OperacionEconomicaDto`**: Transferencia de datos de operaciones
- **`DashboardEconomicoDto`**: Métricas y estadísticas del dashboard
  - TopEgresoDto, RentabilidadCultivoDto, MetricaTemporalDto, CostoLoteDto

#### Repositorios
- **`OperacionEconomicaRepository`**: Consultas personalizadas para métricas
  - Agregaciones por tipo, categoría, lote, período
  - Consultas para dashboard y reportes

#### Servicios
- **`OperacionEconomicaService`**: Lógica de negocio
  - CRUD de operaciones
  - Cálculos de métricas y rentabilidad
  - Generación de dashboard
  - Estadísticas y reportes

#### Controladores
- **`OperacionEconomicaController`**: API REST
  - Endpoints para CRUD de operaciones
  - Dashboard y métricas
  - Filtros y búsquedas
  - Cálculos de costos y rentabilidad

### Frontend (React + TypeScript)

#### Componentes
- **`EconomicManagement`**: Componente principal
  - Dashboard con métricas en tiempo real
  - Tabla de operaciones con filtros
  - Formulario de creación/edición
  - Gráficos y visualizaciones

#### Características de la UI
- **Material-UI**: Diseño moderno y responsive
- **Tabs**: Separación entre Dashboard y Operaciones
- **Formularios**: Validación y campos dinámicos
- **Tablas**: Paginación, ordenamiento, filtros
- **Gráficos**: Visualización de métricas y tendencias

## Instalación y Configuración

### 1. Base de Datos

Ejecutar el script SQL para crear la tabla de operaciones económicas:

```sql
-- Ejecutar: crear_tabla_operaciones_economicas.sql
USE agrogestion;

-- Crear tabla de operaciones económicas
CREATE TABLE IF NOT EXISTS operaciones_economicas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('INGRESO', 'EGRESO') NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    fecha DATETIME NOT NULL,
    lote_id BIGINT,
    descripcion VARCHAR(500) NOT NULL,
    categoria ENUM(
        'VENTA_GRANOS', 'VENTA_SUBPRODUCTOS', 'SUBSIDIOS', 'OTROS_INGRESOS',
        'INSUMOS', 'COMBUSTIBLE', 'MANTENIMIENTO', 'MANO_OBRA', 'MAQUINARIA',
        'FERTILIZANTES', 'PESTICIDAS', 'SEMILLAS', 'SEGUROS', 'IMPUESTOS',
        'ADMINISTRATIVOS', 'OTROS_EGRESOS'
    ),
    comprobante_url VARCHAR(500),
    observaciones VARCHAR(1000),
    usuario_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE RESTRICT
);
```

### 2. Backend

El módulo ya está integrado en el proyecto Spring Boot. Verificar que las dependencias estén presentes en `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 3. Frontend

Instalar dependencias adicionales si es necesario:

```bash
cd agrogestion-frontend
npm install @mui/x-date-pickers date-fns
```

## Uso del Sistema

### 1. Acceso al Módulo

1. Iniciar sesión en el sistema
2. Navegar a "Gestión Económica" en el menú lateral
3. El módulo se divide en dos pestañas: Dashboard y Operaciones

### 2. Dashboard Económico

#### Métricas Principales
- **Ingresos Totales**: Suma de todas las operaciones de ingreso
- **Egresos Totales**: Suma de todas las operaciones de egreso
- **Rentabilidad Total**: Diferencia entre ingresos y egresos
- **Margen Bruto**: Porcentaje de rentabilidad sobre ingresos

#### Análisis por Categoría
- **Top Egresos**: Categorías con mayor impacto en costos
- **Rentabilidad por Cultivo**: Análisis de rentabilidad por tipo de cultivo
- **Evolución Temporal**: Tendencias mensuales y anuales

### 3. Gestión de Operaciones

#### Crear Nueva Operación
1. Hacer clic en "Nueva Operación"
2. Seleccionar tipo: INGRESO o EGRESO
3. Ingresar monto y fecha
4. Seleccionar categoría (se filtra según el tipo)
5. Opcionalmente asociar a un lote
6. Agregar descripción y observaciones
7. Guardar operación

#### Categorías Disponibles

**Ingresos:**
- Venta de Granos
- Venta de Subproductos
- Subsidios
- Otros Ingresos

**Egresos:**
- Insumos
- Combustible
- Mantenimiento
- Mano de Obra
- Maquinaria
- Fertilizantes
- Pesticidas
- Semillas
- Seguros
- Impuestos
- Gastos Administrativos
- Otros Egresos

#### Editar/Eliminar Operaciones
- **Editar**: Hacer clic en el ícono de edición en la tabla
- **Eliminar**: Hacer clic en el ícono de eliminación (requiere confirmación)

### 4. Filtros y Búsquedas

- **Por tipo**: Filtrar solo ingresos o egresos
- **Por categoría**: Filtrar por categoría específica
- **Por lote**: Ver operaciones de un lote específico
- **Por fecha**: Rango de fechas personalizado
- **Búsqueda por descripción**: Búsqueda de texto libre

## API Endpoints

### Operaciones Económicas

#### CRUD Básico
- `POST /api/economic/operaciones` - Crear operación
- `GET /api/economic/operaciones` - Listar operaciones (paginado)
- `GET /api/economic/operaciones/{id}` - Obtener operación por ID
- `PUT /api/economic/operaciones/{id}` - Actualizar operación
- `DELETE /api/economic/operaciones/{id}` - Eliminar operación

#### Filtros y Búsquedas
- `GET /api/economic/operaciones/search?descripcion=texto` - Búsqueda por descripción
- `GET /api/economic/operaciones/tipo/{tipo}` - Por tipo (INGRESO/EGRESO)
- `GET /api/economic/operaciones/categoria/{categoria}` - Por categoría
- `GET /api/economic/operaciones/lote/{loteId}` - Por lote
- `GET /api/economic/operaciones/fecha?fechaInicio=X&fechaFin=Y` - Por rango de fechas

#### Dashboard y Métricas
- `GET /api/economic/dashboard` - Dashboard completo con todas las métricas
- `GET /api/economic/costos/lote/{loteId}` - Costos por lote
- `GET /api/economic/ingresos/lote/{loteId}` - Ingresos por lote
- `GET /api/economic/rentabilidad/lote/{loteId}` - Rentabilidad por lote

#### Estadísticas
- `GET /api/economic/stats/total` - Total de operaciones
- `GET /api/economic/stats/tipo/{tipo}` - Operaciones por tipo
- `GET /api/economic/stats/periodo` - Operaciones por período

#### Utilidades
- `GET /api/economic/tipos` - Tipos de operación disponibles
- `GET /api/economic/categorias` - Categorías disponibles
- `GET /api/economic/categorias/tipo/{tipo}` - Categorías por tipo

## Permisos y Seguridad

### Roles y Permisos

El módulo utiliza el sistema de permisos existente:

- **ECONOMIC_CREATE**: Crear operaciones económicas
- **ECONOMIC_READ**: Ver operaciones y dashboard
- **ECONOMIC_UPDATE**: Editar operaciones
- **ECONOMIC_DELETE**: Eliminar operaciones

### Asignación por Rol

- **ADMINISTRADOR**: Todos los permisos
- **INGENIERO_AGRONOMO**: Crear, leer y actualizar
- **OPERARIO**: Solo lectura
- **INVITADO**: Solo lectura

## Cálculos y Métricas

### Rentabilidad
```
Rentabilidad = Ingresos - Egresos
Margen Bruto = (Rentabilidad / Ingresos) × 100
```

### Costos por Hectárea
```
Costo por Hectárea = Costos Totales / Área del Lote
Rentabilidad por Hectárea = Rentabilidad / Área del Lote
```

### Análisis por Cultivo
- Agrupación de operaciones por cultivo asociado al lote
- Cálculo de ingresos, egresos y rentabilidad por cultivo
- Análisis de rentabilidad por hectárea

## Reportes y Exportación

### Tipos de Reportes Disponibles

1. **Reporte de Operaciones**
   - Listado detallado de todas las operaciones
   - Filtros por tipo, categoría, fecha, lote
   - Totales y subtotales

2. **Reporte de Rentabilidad**
   - Análisis de rentabilidad por cultivo
   - Comparación de costos vs ingresos
   - Métricas de eficiencia

3. **Reporte de Costos**
   - Desglose de costos por categoría
   - Análisis de costos por lote
   - Tendencias de costos

### Formatos de Exportación
- **PDF**: Reportes formateados para impresión
- **Excel**: Datos estructurados para análisis

## Datos de Ejemplo

El script SQL incluye datos de ejemplo para pruebas:

### Ingresos de Ejemplo
- Venta de soja: $50,000
- Venta de maíz: $35,000
- Venta de trigo: $42,000
- Venta de subproductos: $15,000
- Subsidio estatal: $8,000

### Egresos de Ejemplo
- Fertilizantes: $12,000
- Pesticidas: $8,500
- Semillas: $9,000
- Combustible: $9,600
- Mantenimiento: $11,300
- Mano de obra: $22,000
- Gastos administrativos: $5,300
- Seguros: $3,000
- Impuestos: $7,500

## Mantenimiento y Monitoreo

### Logs y Auditoría
- Todas las operaciones se registran con usuario y timestamp
- Logs de creación, modificación y eliminación
- Auditoría de cambios en operaciones

### Backup y Recuperación
- Backup automático de la tabla de operaciones
- Exportación periódica de datos críticos
- Procedimientos de recuperación documentados

### Monitoreo de Performance
- Índices optimizados para consultas frecuentes
- Monitoreo de tiempos de respuesta
- Alertas para operaciones anómalas

## Troubleshooting

### Problemas Comunes

1. **Error de permisos**
   - Verificar que el usuario tenga los permisos necesarios
   - Revisar configuración de roles

2. **Error en cálculos**
   - Verificar que los montos sean números válidos
   - Revisar formato de fechas

3. **Problemas de rendimiento**
   - Verificar índices de base de datos
   - Optimizar consultas complejas

### Logs de Error
- Revisar logs de Spring Boot para errores del backend
- Verificar consola del navegador para errores del frontend
- Monitorear logs de base de datos

## Futuras Mejoras

### Funcionalidades Planificadas
- **Proyecciones presupuestarias**: Planificación de ingresos y egresos
- **Análisis de tendencias**: Predicciones basadas en datos históricos
- **Integración bancaria**: Conexión con cuentas bancarias
- **Facturación**: Generación automática de facturas
- **Múltiples monedas**: Soporte para diferentes divisas
- **Análisis de ROI**: Retorno de inversión por cultivo

### Mejoras Técnicas
- **Caché de métricas**: Optimización de consultas del dashboard
- **Notificaciones**: Alertas para operaciones importantes
- **API GraphQL**: Consultas más eficientes
- **Microservicios**: Separación en servicios independientes

## Soporte y Contacto

Para soporte técnico o consultas sobre el módulo de Gestión Económica:

- **Documentación**: Este README y documentación de API
- **Issues**: Sistema de tickets del proyecto
- **Desarrollador**: Equipo de desarrollo AgroGestion

---

**Versión**: 1.0.0  
**Última actualización**: Diciembre 2024  
**Compatibilidad**: Spring Boot 3.x, React 18.x, MySQL 8.x
