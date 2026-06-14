# 📋 Análisis del Módulo de Eventos Sanitarios

## Estado General: ⚠️ **PARCIALMENTE IMPLEMENTADO**

---

## ✅ BACKEND - COMPLETO

### Entidades Java
- ✅ **`EventoSanitario`** - Entidad principal completa
  - Ubicación: `agrogestion-backend/src/main/java/com/agrocloud/model/entity/EventoSanitario.java`
  - Campos: tipoEventoSanitario, fecha, tipoEntidad, entidadId, dosis, unidadDosis, loteMedicamento, profesionalResponsable, fechaRetiro, retiroCumplido, observaciones
  - Relaciones: TipoEventoSanitario (EAGER), Empresa (LAZY), User (LAZY)
  - Enum: TipoEntidad (MADRE, PADRILLO, LOTE)

- ✅ **`TipoEventoSanitario`** - Catálogo de tipos de eventos
  - Ubicación: `agrogestion-backend/src/main/java/com/agrocloud/model/entity/TipoEventoSanitario.java`
  - Campos: nombre, categoria, requiereFechaRetiro, diasRetiro, descripcion
  - Enum: CategoriaEvento (VACUNA, DESPARASITACION, ANTIBIOTICO, TRATAMIENTO, OTRO)

### Controlador
- ✅ **`EventoSanitarioController`** - Completo con todos los endpoints
  - Ubicación: `agrogestion-backend/src/main/java/com/agrocloud/controller/EventoSanitarioController.java`
  - Endpoints:
    - `GET /api/v1/porcinos/eventos-sanitarios` - Listar todos
    - `GET /api/v1/porcinos/eventos-sanitarios/entidad/{tipoEntidad}/{entidadId}` - Por entidad
    - `GET /api/v1/porcinos/eventos-sanitarios/rango` - Por rango de fechas
    - `GET /api/v1/porcinos/eventos-sanitarios/{id}` - Por ID
    - `POST /api/v1/porcinos/eventos-sanitarios` - Crear/Actualizar
    - `DELETE /api/v1/porcinos/eventos-sanitarios/{id}` - Eliminar (soft delete)
    - `POST /api/v1/porcinos/eventos-sanitarios/{id}/marcar-retiro-cumplido` - Marcar retiro cumplido
    - `GET /api/v1/porcinos/eventos-sanitarios/retiros/vencidos` - Retiros vencidos
    - `GET /api/v1/porcinos/eventos-sanitarios/retiros/proximos` - Retiros próximos

### Servicio
- ✅ **`EventoSanitarioService`** - Lógica de negocio completa
  - Ubicación: `agrogestion-backend/src/main/java/com/agrocloud/service/EventoSanitarioService.java`
  - Métodos:
    - `obtenerEventosSanitarios(User user)`
    - `obtenerEventosPorEntidad(TipoEntidad, Long, User)`
    - `obtenerEventosPorRangoFechas(LocalDate, LocalDate, User)`
    - `guardarEventoSanitario(EventoSanitario, User)`
    - `obtenerEventoSanitarioPorId(Long, User)`
    - `eliminarEventoSanitario(Long, User)`
    - `marcarRetiroCumplido(Long, User)`
    - `obtenerRetirosVencidos(User)`
    - `obtenerRetirosProximosAVencer(User)`

### Repositorio
- ✅ **`EventoSanitarioRepository`** - Queries personalizadas
  - Ubicación: `agrogestion-backend/src/main/java/com/agrocloud/repository/EventoSanitarioRepository.java`
  - Métodos:
    - `findByEmpresaAndActivoTrue`
    - `findByEmpresaAndTipoEntidadAndEntidadIdAndActivoTrue`
    - `findByEmpresaAndFechaBetweenAndActivoTrue`
    - `findRetirosVencidos` (query personalizada)
    - `findRetirosProximosAVencer` (query personalizada)
    - `findByIdAndEmpresaAndActivoTrue`

### Catálogos
- ✅ **`CatalogosPorcinoService`** - Gestión de tipos de eventos sanitarios
  - Métodos para `TipoEventoSanitario`:
    - `obtenerTiposEventoSanitario(Long userId)`
    - `obtenerTiposEventoSanitarioPorCategoria(Long userId, CategoriaEvento)`
    - `guardarTipoEventoSanitario(TipoEventoSanitario, Long userId)`
    - `eliminarTipoEventoSanitario(Long id, Long userId)`

- ✅ **`CatalogosPorcinoController`** - Endpoints para tipos de eventos
  - Endpoints en `/api/v1/porcinos/catalogos/tipos-evento-sanitario`

---

## ❌ BASE DE DATOS - FALTA TABLA PRINCIPAL

### Tablas Existentes
- ✅ **`porcinos_tipos_evento_sanitario`** - Existe en la base de datos
  - Verificado con: `SHOW TABLES LIKE '%evento%'`

### Tablas Faltantes
- ❌ **`porcinos_eventos_sanitarios`** - **NO EXISTE**
  - La entidad Java está mapeada a esta tabla
  - No hay migración de Flyway que la cree
  - Necesita ser creada manualmente

### Estructura Requerida (según la entidad Java)
```sql
CREATE TABLE porcinos_eventos_sanitarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_evento_sanitario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    tipo_entidad ENUM('MADRE', 'PADRILLO', 'LOTE') NOT NULL,
    entidad_id BIGINT NOT NULL,
    dosis DECIMAL(10,2),
    unidad_dosis VARCHAR(50) DEFAULT 'ml',
    lote_medicamento VARCHAR(100),
    profesional_responsable VARCHAR(200),
    fecha_retiro DATE,
    retiro_cumplido BOOLEAN NOT NULL DEFAULT FALSE,
    observaciones TEXT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    empresa_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tipo_evento_sanitario_id) REFERENCES porcinos_tipos_evento_sanitario(id),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    
    INDEX idx_eventos_sanitarios_empresa (empresa_id),
    INDEX idx_eventos_sanitarios_tipo_entidad (tipo_entidad, entidad_id),
    INDEX idx_eventos_sanitarios_fecha (fecha),
    INDEX idx_eventos_sanitarios_activo (activo),
    INDEX idx_eventos_sanitarios_retiro (fecha_retiro, retiro_cumplido)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## ❌ FRONTEND - NO IMPLEMENTADO

### Pantallas
- ❌ **No existe pantalla de Eventos Sanitarios**
  - No hay componente en `agrogestion-frontend/src/modules/porcinos/screens/`
  - No está en el menú (`menu.ts`)
  - No hay ruta configurada

### Servicios
- ❌ **No existe servicio de eventos sanitarios**
  - No hay archivo en `agrogestion-frontend/src/modules/porcinos/services/`
  - No hay métodos para consumir la API

### Endpoints API
- ❌ **No están definidos en `apiEndpoints.ts`**
  - No hay constantes para los endpoints de eventos sanitarios

### Tipos TypeScript
- ❌ **No hay tipos definidos en `types.ts`**
  - No hay interface para `EventoSanitario`
  - No hay interface para `TipoEventoSanitario`

### Integración
- ⚠️ **Solo mencionado en reportes**
  - `ReportesPorcinosScreen.tsx` menciona "Eventos sanitarios y tratamientos"
  - El backend de reportes ya incluye eventos sanitarios

---

## 📊 RESUMEN

| Componente | Estado | Observaciones |
|------------|--------|---------------|
| **Backend - Entidades** | ✅ Completo | `EventoSanitario` y `TipoEventoSanitario` |
| **Backend - Controller** | ✅ Completo | Todos los endpoints implementados |
| **Backend - Service** | ✅ Completo | Toda la lógica de negocio |
| **Backend - Repository** | ✅ Completo | Queries personalizadas |
| **Base de Datos - Tabla Principal** | ❌ **FALTA** | `porcinos_eventos_sanitarios` no existe |
| **Base de Datos - Tabla Catálogo** | ✅ Existe | `porcinos_tipos_evento_sanitario` existe |
| **Frontend - Pantalla** | ❌ **FALTA** | No hay componente |
| **Frontend - Servicio** | ❌ **FALTA** | No hay servicio TypeScript |
| **Frontend - Endpoints** | ❌ **FALTA** | No están en `apiEndpoints.ts` |
| **Frontend - Tipos** | ❌ **FALTA** | No hay interfaces TypeScript |
| **Frontend - Menú** | ❌ **FALTA** | No está en el menú |
| **Frontend - Rutas** | ❌ **FALTA** | No hay ruta configurada |

---

## 🔧 ACCIONES REQUERIDAS

### 1. Crear Tabla en Base de Datos
- [ ] Crear script SQL para `porcinos_eventos_sanitarios`
- [ ] Ejecutar script en la base de datos

### 2. Implementar Frontend
- [ ] Crear tipos TypeScript en `types.ts`
- [ ] Agregar endpoints en `apiEndpoints.ts`
- [ ] Crear servicio `eventosSanitariosService.ts`
- [ ] Crear pantalla `EventosSanitariosScreen.tsx`
- [ ] Agregar al menú (`menu.ts`)
- [ ] Configurar ruta (`routes.ts`)

### 3. Funcionalidades del Frontend
- [ ] Listar eventos sanitarios (con filtros)
- [ ] Crear nuevo evento sanitario
- [ ] Editar evento sanitario
- [ ] Eliminar evento sanitario
- [ ] Filtrar por entidad (Madre, Padrillo, Lote)
- [ ] Filtrar por rango de fechas
- [ ] Ver retiros vencidos
- [ ] Ver retiros próximos
- [ ] Marcar retiro cumplido
- [ ] Integrar con detalles de Madre/Padrillo/Recría

---

## 📝 NOTAS ADICIONALES

1. **Integración con otras pantallas:**
   - Los eventos sanitarios deberían poder verse desde:
     - Detalle de Madre (historial sanitario)
     - Detalle de Padrillo (historial sanitario)
     - Detalle de Recría (historial sanitario del lote)

2. **Control de Retiros:**
   - El sistema tiene lógica para controlar retiros de medicamentos
   - Debe mostrarse alertas para retiros vencidos y próximos
   - Puede integrarse con el calendario del dashboard

3. **Catálogos:**
   - Los tipos de eventos sanitarios se gestionan desde Configuraciones
   - Ya existe la funcionalidad en el backend

4. **Reportes:**
   - El reporte sanitario ya incluye eventos sanitarios
   - Solo falta la interfaz para registrar los eventos

---

## 🎯 PRIORIDAD

**ALTA** - El módulo está casi completo en el backend, solo falta:
1. Crear la tabla (5 minutos)
2. Implementar el frontend (2-3 horas)

El backend está listo y funcional, solo necesita la interfaz de usuario.














