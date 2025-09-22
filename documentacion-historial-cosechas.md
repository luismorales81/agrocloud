# 📊 Documentación: Sistema de Historial de Cosechas

## 🏗️ Estructura de la Base de Datos

### Tabla Principal: `historial_cosechas`

```sql
historial_cosechas
├── id (BIGINT, PK, AUTO_INCREMENT)
├── lote_id (BIGINT, FK → lotes.id)
├── cultivo_id (BIGINT, FK → cultivos.id)
├── usuario_id (BIGINT, FK → users.id)
├── fecha_siembra (DATE)
├── fecha_cosecha (DATE)
├── superficie_hectareas (DECIMAL(10,2))
├── cantidad_cosechada (DECIMAL(10,2))
├── unidad_cosecha (VARCHAR(10))
├── rendimiento_real (DECIMAL(10,2))
├── rendimiento_esperado (DECIMAL(10,2))
├── humedad_cosecha (DECIMAL(5,2))
├── variedad_semilla (VARCHAR(100))
├── observaciones (TEXT)
├── estado_suelo (VARCHAR(50)) -- BUENO, DESCANSANDO, AGOTADO
├── requiere_descanso (BOOLEAN)
├── dias_descanso_recomendados (INT)
├── fecha_creacion (TIMESTAMP)
└── fecha_actualizacion (TIMESTAMP)
```

## 🔗 Relaciones

### Relaciones Externas
- **lotes** ← `lote_id` → **historial_cosechas**
- **cultivos** ← `cultivo_id` → **historial_cosechas**  
- **users** ← `usuario_id` → **historial_cosechas**

### Restricciones de Integridad
- `ON DELETE CASCADE` para todas las FK
- Validaciones CHECK para valores positivos
- Validación de fechas (siembra ≤ cosecha)
- Validación de humedad (0-100%)

## 📈 Índices Optimizados

### Índices Simples
- `idx_historial_cosechas_lote_id`
- `idx_historial_cosechas_cultivo_id`
- `idx_historial_cosechas_usuario_id`
- `idx_historial_cosechas_fecha_cosecha`
- `idx_historial_cosechas_estado_suelo`
- `idx_historial_cosechas_requiere_descanso`

### Índices Compuestos
- `idx_historial_cosechas_lote_fecha` (lote_id, fecha_cosecha DESC)
- `idx_historial_cosechas_usuario_fecha` (usuario_id, fecha_cosecha DESC)

## 👁️ Vistas Creadas

### 1. `vista_historial_cosechas_completo`
Vista completa con JOINs a todas las tablas relacionadas:
- Información del lote y cultivo
- Cálculo automático de días de ciclo
- Porcentaje de cumplimiento
- Datos del usuario

### 2. `vista_estadisticas_rendimiento_cultivo`
Estadísticas agregadas por cultivo:
- Total de cosechas
- Rendimiento promedio, mínimo, máximo
- Cumplimiento promedio
- Superficie y cantidad total cosechada

### 3. `vista_lotes_requieren_descanso`
Lotes que están en período de descanso:
- Estado actual del lote
- Días transcurridos desde la cosecha
- Estado: EN_DESCANSO o LISTO_PARA_SIEMBRA

## 🔄 Flujo de Datos

### 1. Registro de Cosecha
```
Usuario registra cosecha → CosechaService.crearCosecha()
    ↓
Se guarda en tabla 'cosechas'
    ↓
Se crea automáticamente en 'historial_cosechas'
    ↓
Se calculan recomendaciones de descanso
```

### 2. Liberación de Lote
```
Usuario confirma liberación → HistorialCosechaService.liberarLoteParaNuevaSiembra()
    ↓
Validación: período mínimo de 7 días
    ↓
Actualización: lote.estado = 'DISPONIBLE'
    ↓
Limpieza: lote.cultivo_actual = NULL
```

## 🧮 Cálculos Automáticos

### Período de Descanso
- **Soja/Maíz/Trigo**: 30 días base + 15 si rendimiento < 70%
- **Otros cultivos**: 15 días base + 15 si rendimiento < 70%
- **Estado del suelo**: BUENO → DESCANSANDO → AGOTADO

### Rendimiento Corregido
```sql
R_corregido = R_real * (100 - humedad_real) / (100 - humedad_estándar)
```

### Porcentaje de Cumplimiento
```sql
%_cumplimiento = (rendimiento_real / rendimiento_esperado) * 100
```

## 🚀 Endpoints API

### HistorialCosechaController
- `GET /api/historial-cosechas` - Historial del usuario
- `GET /api/historial-cosechas/lote/{id}` - Historial por lote
- `GET /api/historial-cosechas/lote/{id}/ultima` - Última cosecha del lote
- `PUT /api/historial-cosechas/lote/{id}/liberar` - Liberar lote
- `GET /api/historial-cosechas/lote/{id}/puede-liberar` - Verificar si puede liberar
- `GET /api/historial-cosechas/estadisticas/rendimiento` - Estadísticas por cultivo
- `GET /api/historial-cosechas/recientes` - Cosechas recientes (30 días)

## 📋 Scripts de Migración

### Archivos Creados
1. `V1_4__Create_HistorialCosechas_Table.sql` - Migración principal
2. `verificar-tabla-historial-cosechas.sql` - Script de verificación
3. `aplicar-migracion-historial-cosechas.bat` - Script de aplicación

### Comandos de Aplicación
```bash
# Aplicar migración
mvn flyway:migrate

# Verificar estructura
mysql -u root -p agrogestion < verificar-tabla-historial-cosechas.sql
```

## ✅ Validaciones Implementadas

### Backend
- Período mínimo de 7 días entre cosechas
- Validación de permisos por usuario
- Cálculo automático de recomendaciones
- Manejo de errores y excepciones

### Frontend
- Confirmación antes de liberar lote
- Modal de historial con información completa
- Indicadores visuales de estado del suelo
- Recarga automática de datos

## 🎯 Beneficios del Sistema

1. **Trazabilidad Completa**: Historial de todos los ciclos de cultivo
2. **Gestión Inteligente**: Recomendaciones automáticas de descanso
3. **Análisis de Rendimiento**: Comparaciones históricas por cultivo
4. **Prevención de Errores**: Validaciones de períodos mínimos
5. **Planificación Mejorada**: Datos para futuras siembras
6. **Optimización de Recursos**: Mejor uso del suelo y rotación de cultivos

## 🔧 Mantenimiento

### Consultas Útiles
```sql
-- Ver lotes que requieren descanso
SELECT * FROM vista_lotes_requieren_descanso;

-- Estadísticas por cultivo
SELECT * FROM vista_estadisticas_rendimiento_cultivo;

-- Historial completo de un lote
SELECT * FROM vista_historial_cosechas_completo WHERE lote_id = 1;
```

### Limpieza de Datos
```sql
-- Eliminar historial antiguo (más de 5 años)
DELETE FROM historial_cosechas 
WHERE fecha_cosecha < DATE_SUB(CURDATE(), INTERVAL 5 YEAR);
```

---

**Sistema implementado y listo para producción** ✅
