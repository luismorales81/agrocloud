# Funcionalidad de Balance de Costos y Beneficios

## Descripción General

Se ha implementado una nueva funcionalidad completa para el análisis de costos y beneficios en el sistema AgroGestion. Esta funcionalidad permite a los usuarios:

- Registrar ingresos/beneficios de diferentes tipos
- Calcular automáticamente el balance neto
- Generar reportes detallados por período
- Analizar la rentabilidad por lote
- Visualizar estadísticas de margen de beneficio

## Componentes Implementados

### Backend

#### 1. Entidad Ingreso (`Ingreso.java`)
- **Ubicación**: `agrogestion-backend/src/main/java/com/agrocloud/model/entity/Ingreso.java`
- **Funcionalidad**: Representa los ingresos/beneficios del sistema
- **Campos principales**:
  - `concepto`: Descripción del ingreso
  - `tipoIngreso`: Categoría (VENTA_CULTIVO, VENTA_ANIMAL, SERVICIOS_AGRICOLAS, SUBSIDIOS, OTROS_INGRESOS)
  - `fechaIngreso`: Fecha del ingreso
  - `monto`: Cantidad monetaria
  - `estado`: REGISTRADO, CONFIRMADO, CANCELADO
  - `lote`: Relación opcional con un lote específico

#### 2. DTOs de Balance
- **BalanceDTO**: Contiene el resumen del balance (ingresos, costos, balance neto, margen)
- **DetalleBalanceDTO**: Detalle de cada transacción individual

#### 3. Servicio de Balance (`BalanceService.java`)
- **Ubicación**: `agrogestion-backend/src/main/java/com/agrocloud/service/BalanceService.java`
- **Funcionalidades**:
  - Cálculo de balance general por usuario y período
  - Cálculo de balance por lote específico
  - Estadísticas mensuales y anuales
  - Agregación de costos de labores y mantenimiento

#### 4. Controladores
- **BalanceController**: Endpoints para reportes de balance
- **IngresoController**: CRUD completo para gestión de ingresos

#### 5. Repositorios
- **IngresoRepository**: Consultas específicas para ingresos
- **MantenimientoMaquinariaRepository**: Consultas para costos de mantenimiento

### Frontend

#### 1. Componente BalanceReport (`BalanceReport.tsx`)
- **Ubicación**: `agrogestion-frontend/src/components/BalanceReport.tsx`
- **Funcionalidades**:
  - Generación de reportes por período
  - Visualización de balance general y por lote
  - Gráficos de resumen (ingresos, costos, balance neto, margen)
  - Detalle de transacciones

#### 2. Componente IngresosManagement (`IngresosManagement.tsx`)
- **Ubicación**: `agrogestion-frontend/src/components/IngresosManagement.tsx`
- **Funcionalidades**:
  - CRUD completo de ingresos
  - Formulario de registro con validaciones
  - Lista de ingresos con filtros
  - Asociación con lotes

## Endpoints API

### Balance
- `GET /api/balance/general` - Balance general por período
- `GET /api/balance/lote/{loteId}` - Balance por lote específico
- `GET /api/balance/mes-actual` - Balance del mes actual
- `GET /api/balance/año-actual` - Balance del año actual
- `GET /api/balance/estadisticas/{año}` - Estadísticas mensuales

### Ingresos
- `GET /api/ingresos` - Listar ingresos del usuario
- `POST /api/ingresos` - Crear nuevo ingreso
- `PUT /api/ingresos/{id}` - Actualizar ingreso
- `DELETE /api/ingresos/{id}` - Eliminar ingreso
- `GET /api/ingresos/tipo/{tipo}` - Filtrar por tipo
- `GET /api/ingresos/lote/{loteId}` - Filtrar por lote

## Base de Datos

### Tabla `ingresos`
```sql
CREATE TABLE ingresos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    concepto VARCHAR(200) NOT NULL,
    descripcion VARCHAR(500),
    tipo_ingreso ENUM('VENTA_CULTIVO', 'VENTA_ANIMAL', 'SERVICIOS_AGRICOLAS', 'SUBSIDIOS', 'OTROS_INGRESOS') NOT NULL,
    fecha_ingreso DATE NOT NULL,
    monto DECIMAL(15,2) NOT NULL,
    unidad_medida VARCHAR(100),
    cantidad DECIMAL(10,2),
    cliente_comprador VARCHAR(200),
    estado ENUM('REGISTRADO', 'CONFIRMADO', 'CANCELADO') DEFAULT 'REGISTRADO',
    observaciones VARCHAR(1000),
    lote_id BIGINT,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Cálculo del Balance

### Fórmulas Utilizadas

1. **Balance Neto** = Total Ingresos - Total Costos
2. **Margen de Beneficio** = (Balance Neto / Total Ingresos) × 100

### Fuentes de Datos

#### Ingresos
- Registros de la tabla `ingresos` con estado 'CONFIRMADO'
- Filtrados por usuario y período de fechas

#### Costos
- **Labores**: Costos de la tabla `labores` asociadas al usuario
- **Mantenimiento**: Costos de la tabla `mantenimiento_maquinaria` asociadas al usuario
- Filtrados por período de fechas

## Características Destacadas

### 1. Análisis por Lote
- Permite analizar la rentabilidad de lotes específicos
- Asocia ingresos y costos a lotes particulares
- Facilita la toma de decisiones por área de cultivo

### 2. Reportes Flexibles
- Balance general de toda la operación
- Balance específico por lote
- Reportes predefinidos (mes actual, año actual)
- Estadísticas mensuales para análisis de tendencias

### 3. Gestión de Estados
- Estados de ingresos: REGISTRADO, CONFIRMADO, CANCELADO
- Solo ingresos confirmados se incluyen en los cálculos
- Permite control de flujo de trabajo

### 4. Tipos de Ingreso
- **VENTA_CULTIVO**: Ventas de productos agrícolas
- **VENTA_ANIMAL**: Ventas de ganado
- **SERVICIOS_AGRICOLAS**: Servicios prestados a terceros
- **SUBSIDIOS**: Subsidios gubernamentales
- **OTROS_INGRESOS**: Otros tipos de ingresos

## Instalación y Configuración

### 1. Ejecutar Script de Base de Datos
```sql
-- Ejecutar el archivo: migration-create-ingresos-table.sql
```

### 2. Reiniciar el Backend
El backend detectará automáticamente las nuevas entidades y creará las tablas si no existen.

### 3. Integrar en el Frontend
Los componentes están listos para ser integrados en el menú principal del sistema.

## Uso del Sistema

### 1. Registrar Ingresos
1. Ir a "Gestión de Ingresos"
2. Hacer clic en "Nuevo Ingreso"
3. Completar el formulario con los datos del ingreso
4. Guardar el registro

### 2. Generar Reportes de Balance
1. Ir a "Balance de Costos y Beneficios"
2. Seleccionar el tipo de reporte
3. Configurar fechas si es necesario
4. Hacer clic en "Generar Reporte"

### 3. Analizar Resultados
- Revisar el resumen de ingresos, costos y balance neto
- Analizar el margen de beneficio
- Revisar el detalle de transacciones
- Exportar datos si es necesario

## Beneficios de la Implementación

1. **Visibilidad Financiera**: Control total sobre ingresos y costos
2. **Análisis de Rentabilidad**: Identificación de lotes más rentables
3. **Toma de Decisiones**: Datos para planificación estratégica
4. **Cumplimiento**: Registro detallado para auditorías
5. **Optimización**: Identificación de oportunidades de mejora

## Próximas Mejoras Sugeridas

1. **Gráficos Interactivos**: Implementar gráficos de tendencias
2. **Exportación**: Funcionalidad para exportar reportes a PDF/Excel
3. **Alertas**: Notificaciones de márgenes bajos
4. **Presupuestos**: Comparación con presupuestos planificados
5. **Análisis de Estacionalidad**: Patrones de ingresos por temporada
