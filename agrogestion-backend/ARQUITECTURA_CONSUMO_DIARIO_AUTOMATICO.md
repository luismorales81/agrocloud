# Arquitectura: Sistema de Consumo Diario Automático de Alimento

## Visión General

Sistema completo para gestión automática diaria de consumo de alimento en granjas porcinas, con trazabilidad total, manejo de stock negativo permitido solo para consumo automático, y calendario diario con estados de confirmación.

---

## 1. MODELO DE DATOS

### 1.1 DiaAlimentacion
**Tabla:** `porcinos_dias_alimentacion`

Registro diario del calendario de alimentación con estado de confirmación.

```sql
CREATE TABLE porcinos_dias_alimentacion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    empresa_id BIGINT NOT NULL,
    
    -- Estado del día
    estado ENUM('PENDIENTE', 'CONFIRMADO', 'CON_CORRECCIONES') NOT NULL DEFAULT 'PENDIENTE',
    
    -- Confirmación
    confirmado_por_id BIGINT NULL,
    fecha_confirmacion DATETIME NULL,
    observaciones_confirmacion TEXT,
    
    -- Resumen del día (calculado)
    total_lotes_atendidos INT DEFAULT 0,
    total_animales_atendidos INT DEFAULT 0,
    total_recetas_usadas INT DEFAULT 0,
    total_insumos_consumidos INT DEFAULT 0,
    
    -- Alertas del día
    tiene_alertas_stock_insuficiente BOOLEAN DEFAULT FALSE,
    cantidad_alertas INT DEFAULT 0,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_dia_empresa (empresa_id, fecha),
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (confirmado_por_id) REFERENCES usuarios(id),
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado),
    INDEX idx_empresa_fecha (empresa_id, fecha)
);
```

### 1.2 ConsumoDiarioAutomatico
**Tabla:** `porcinos_consumos_diarios_automaticos`

Consumo diario generado automáticamente para un lote/recría específico.

```sql
CREATE TABLE porcinos_consumos_diarios_automaticos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    dia_alimentacion_id BIGINT NOT NULL,
    recria_id BIGINT NULL,
    lote_id BIGINT NULL,
    madre_id BIGINT NULL, -- Para madres gestantes/lactantes
    
    -- Contexto del consumo
    etapa_alimentacion VARCHAR(50) NOT NULL, -- GESTACION, LACTANCIA, F1, F2, F3, F4, DESARROLLO, TERMINACION
    cantidad_animales INT NOT NULL,
    receta_id BIGINT NOT NULL, -- FK a insumos_compuestos
    
    -- Consumo calculado
    cantidad_receta_total DECIMAL(10,2) NOT NULL, -- Total de receta consumida (kg)
    cantidad_diaria_por_animal DECIMAL(10,2) NOT NULL, -- Cantidad por animal según receta
    
    -- Estado
    procesado BOOLEAN DEFAULT FALSE, -- Si ya se descontó stock
    fecha_procesamiento DATETIME NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE CASCADE,
    FOREIGN KEY (recria_id) REFERENCES porcinos_recria(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (madre_id) REFERENCES porcinos_madres(id) ON DELETE SET NULL,
    FOREIGN KEY (receta_id) REFERENCES insumos_compuestos(id),
    INDEX idx_dia (dia_alimentacion_id),
    INDEX idx_recria (recria_id),
    INDEX idx_lote (lote_id),
    INDEX idx_fecha_procesamiento (fecha_procesamiento)
);
```

### 1.3 ConsumoDiarioDetalle
**Tabla:** `porcinos_consumos_diarios_detalle`

Desglose detallado de insumos consumidos (componentes de la receta).

```sql
CREATE TABLE porcinos_consumos_diarios_detalle (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumo_diario_id BIGINT NOT NULL,
    
    -- Insumo consumido (puede ser Insumo, Cultivo, o InsumoCompuesto)
    insumo_id BIGINT NULL, -- FK a cultivo_insumos
    cultivo_id BIGINT NULL, -- FK a cultivos (grano propio)
    insumo_compuesto_id BIGINT NULL, -- FK a insumos_compuestos (sub-receta)
    
    tipo_componente ENUM('INSUMO', 'CULTIVO', 'INSUMO_COMPUESTO') NOT NULL,
    
    -- Cantidad consumida
    cantidad_requerida DECIMAL(10,2) NOT NULL, -- Cantidad total requerida (kg)
    cantidad_disponible DECIMAL(10,2) NOT NULL, -- Stock disponible al momento del consumo
    cantidad_descontada DECIMAL(10,2) NOT NULL, -- Cantidad realmente descontada
    stock_resultante DECIMAL(10,2) NOT NULL, -- Stock después del descuento (puede ser negativo)
    deficit DECIMAL(10,2) DEFAULT 0.00, -- Déficit si stock insuficiente
    
    -- Alertas
    tiene_deficit BOOLEAN DEFAULT FALSE,
    porcentaje_cobertura DECIMAL(5,2) DEFAULT 100.00, -- % del requerimiento cubierto
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (consumo_diario_id) REFERENCES porcinos_consumos_diarios_automaticos(id) ON DELETE CASCADE,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id),
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id),
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id),
    INDEX idx_consumo (consumo_diario_id),
    INDEX idx_deficit (tiene_deficit)
);
```

### 1.4 MovimientoStockPorcino
**Tabla:** `porcinos_movimientos_stock`

Trazabilidad completa de todos los movimientos de stock (consumo automático, derrames, ajustes).

```sql
CREATE TABLE porcinos_movimientos_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    
    -- Origen del movimiento
    tipo_movimiento ENUM(
        'CONSUMO_AUTOMATICO', 
        'DERRAME', 
        'PERDIDA', 
        'AJUSTE_INVENTARIO',
        'INGRESO',
        'TRANSFERENCIA'
    ) NOT NULL,
    
    -- Referencia al origen
    consumo_diario_id BIGINT NULL, -- Si es consumo automático
    derrame_id BIGINT NULL, -- Si es derrame
    dia_alimentacion_id BIGINT NULL, -- Para consumo automático
    fecha_movimiento DATE NOT NULL,
    
    -- Insumo afectado
    insumo_id BIGINT NULL,
    cultivo_id BIGINT NULL,
    insumo_compuesto_id BIGINT NULL,
    tipo_insumo ENUM('INSUMO', 'CULTIVO', 'INSUMO_COMPUESTO') NOT NULL,
    
    -- Detalles del movimiento
    cantidad DECIMAL(10,2) NOT NULL,
    stock_anterior DECIMAL(10,2) NOT NULL,
    stock_posterior DECIMAL(10,2) NOT NULL, -- Puede ser negativo solo si es CONSUMO_AUTOMATICO
    permite_negativo BOOLEAN DEFAULT FALSE, -- Solo true para CONSUMO_AUTOMATICO
    
    -- Información adicional
    motivo VARCHAR(200),
    observaciones TEXT,
    lote_id BIGINT NULL,
    recria_id BIGINT NULL,
    
    -- Usuario responsable
    usuario_id BIGINT NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (consumo_diario_id) REFERENCES porcinos_consumos_diarios_automaticos(id) ON DELETE SET NULL,
    FOREIGN KEY (derrame_id) REFERENCES porcinos_derrames_perdidas(id) ON DELETE SET NULL,
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (recria_id) REFERENCES porcinos_recria(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    INDEX idx_fecha (fecha_movimiento),
    INDEX idx_tipo (tipo_movimiento),
    INDEX idx_insumo (tipo_insumo, insumo_id, cultivo_id, insumo_compuesto_id),
    INDEX idx_empresa_fecha (empresa_id, fecha_movimiento)
);
```

### 1.5 DerramePerdida
**Tabla:** `porcinos_derrames_perdidas`

Registro de derrames/pérdidas/accidentes independientes del consumo diario.

```sql
CREATE TABLE porcinos_derrames_perdidas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    empresa_id BIGINT NOT NULL,
    
    -- Tipo de pérdida
    tipo ENUM('DERRAME', 'PERDIDA', 'ACCIDENTE', 'OTRO') NOT NULL,
    fecha DATE NOT NULL,
    dia_alimentacion_id BIGINT NULL, -- Día asociado (solo referencia, no modifica consumo)
    
    -- Insumo afectado
    insumo_id BIGINT NULL,
    cultivo_id BIGINT NULL,
    insumo_compuesto_id BIGINT NULL,
    tipo_insumo ENUM('INSUMO', 'CULTIVO', 'INSUMO_COMPUESTO') NOT NULL,
    
    -- Detalles
    cantidad DECIMAL(10,2) NOT NULL,
    motivo VARCHAR(200) NOT NULL, -- Obligatorio
    observaciones TEXT NOT NULL, -- Obligatorio
    ubicacion VARCHAR(200),
    lote_id BIGINT NULL,
    
    -- Usuario responsable
    usuario_id BIGINT NOT NULL,
    
    -- Auditoría
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (empresa_id) REFERENCES empresas(id),
    FOREIGN KEY (dia_alimentacion_id) REFERENCES porcinos_dias_alimentacion(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_id) REFERENCES cultivo_insumos(id) ON DELETE SET NULL,
    FOREIGN KEY (cultivo_id) REFERENCES cultivos(id) ON DELETE SET NULL,
    FOREIGN KEY (insumo_compuesto_id) REFERENCES insumos_compuestos(id) ON DELETE SET NULL,
    FOREIGN KEY (lote_id) REFERENCES lotes(id) ON DELETE SET NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_fecha (fecha),
    INDEX idx_tipo (tipo),
    INDEX idx_empresa_fecha (empresa_id, fecha)
);
```

### 1.6 Modificación a Insumo

Agregar campo para controlar si permite stock negativo:

```sql
ALTER TABLE cultivo_insumos 
ADD COLUMN permite_stock_negativo BOOLEAN DEFAULT FALSE 
COMMENT 'Permite stock negativo solo para consumo automático';
```

---

## 2. LÓGICA DEL PROCESO DIARIO

### 2.1 Job Programado Diario

**Servicio:** `ConsumoDiarioAutomaticoService`

```java
@Service
@EnableScheduling
public class ConsumoDiarioAutomaticoService {
    
    /**
     * Job automático que se ejecuta diariamente a las 00:30 AM
     * Genera el consumo del día anterior si no existe
     */
    @Scheduled(cron = "0 30 0 * * ?") // Todos los días a las 00:30
    public void generarConsumoDiarioAutomatico() {
        LocalDate fechaAyer = LocalDate.now().minusDays(1);
        generarConsumoDiarioParaFecha(fechaAyer);
    }
    
    /**
     * Genera consumo diario automático para una fecha específica
     * Se puede llamar manualmente para reprocesar días anteriores
     */
    @Transactional
    public DiaAlimentacion generarConsumoDiarioParaFecha(LocalDate fecha) {
        // 1. Verificar si ya existe día para esta fecha
        // 2. Obtener todas las empresas activas
        // 3. Para cada empresa:
        //    a. Crear o obtener DiaAlimentacion en estado PENDIENTE
        //    b. Calcular consumos para todos los lotes activos
        //    c. Descontar stock (permitiendo negativo)
        //    d. Registrar movimientos
        //    e. Generar alertas
        // 4. Retornar DiaAlimentacion creado
    }
}
```

### 2.2 Algoritmo de Cálculo de Consumo

```java
private List<ConsumoDiarioAutomatico> calcularConsumosParaFecha(
        LocalDate fecha, Empresa empresa) {
    
    List<ConsumoDiarioAutomatico> consumos = new ArrayList<>();
    
    // 1. Obtener todas las recrías activas al día
    List<Recria> recrias = obtenerRecriasActivasEnFecha(fecha, empresa);
    
    // 2. Obtener todas las madres gestantes/lactantes al día
    List<Madre> madres = obtenerMadresActivasEnFecha(fecha, empresa);
    
    // 3. Para cada recría:
    for (Recria recria : recrias) {
        // a. Determinar etapa de alimentación según edad/peso
        RecetaAlimentacionPorEtapa.EtapaAlimentacion etapa = 
            determinarEtapaAlimentacion(recria, fecha);
        
        // b. Obtener receta asignada para esa etapa
        RecetaAlimentacionPorEtapa recetaEtapa = 
            obtenerRecetaParaEtapa(etapa, empresa);
        
        if (recetaEtapa != null) {
            // c. Calcular cantidad total = cantidad por animal × cantidad animales
            BigDecimal cantidadTotal = recetaEtapa.getCantidadDiariaPorAnimal()
                .multiply(BigDecimal.valueOf(recria.getCantidadAnimales()));
            
            // d. Crear ConsumoDiarioAutomatico
            ConsumoDiarioAutomatico consumo = crearConsumo(
                recria, etapa, recetaEtapa, cantidadTotal, fecha);
            consumos.add(consumo);
        }
    }
    
    // 4. Similar para madres gestantes/lactantes
    
    return consumos;
}
```

### 2.3 Descuento de Stock (Permitiendo Negativo)

```java
@Transactional
private void descontarStockYRegistrarMovimiento(
        ConsumoDiarioAutomatico consumo,
        ComponenteInsumoCompuesto componente,
        BigDecimal cantidadRequerida) {
    
    BigDecimal stockAnterior;
    BigDecimal stockPosterior;
    BigDecimal cantidadDescontada;
    BigDecimal deficit = BigDecimal.ZERO;
    
    if (componente.getTipoComponente() == TipoComponente.INSUMO) {
        Insumo insumo = componente.getInsumo();
        stockAnterior = insumo.getStockActual();
        
        // DESCONTAR PERMITIENDO NEGATIVO
        stockPosterior = stockAnterior.subtract(cantidadRequerida);
        insumo.setStockActual(stockPosterior);
        insumoRepository.save(insumo);
        
        cantidadDescontada = cantidadRequerida;
        
        // Calcular déficit si quedó negativo
        if (stockPosterior.compareTo(BigDecimal.ZERO) < 0) {
            deficit = stockPosterior.abs();
        }
        
        // Registrar movimiento
        registrarMovimientoStock(
            TipoMovimiento.CONSUMO_AUTOMATICO,
            insumo, null, null,
            TipoInsumo.INSUMO,
            cantidadRequerida,
            stockAnterior,
            stockPosterior,
            true, // permite_negativo = true
            consumo.getDiaAlimentacion(),
            consumo,
            null
        );
    }
    // Similar para CULTIVO e INSUMO_COMPUESTO
    
    // Guardar detalle del consumo
    guardarConsumoDiarioDetalle(
        consumo, componente, cantidadRequerida,
        stockAnterior, cantidadDescontada, stockPosterior, deficit);
}
```

---

## 3. ENDPOINTS Y SERVICIOS

### 3.1 Calendario Mensual

```java
@GetMapping("/porcinos/calendario/mensual")
public ResponseEntity<Map<String, Object>> obtenerCalendarioMensual(
        @RequestParam int año,
        @RequestParam int mes,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // Retorna: Map<dia, DiaAlimentacionDTO>
    // DTO incluye: fecha, estado, totalConsumos, cantidadAlertas, etc.
}
```

### 3.2 Detalle de Día

```java
@GetMapping("/porcinos/calendario/dia/{fecha}")
public ResponseEntity<DiaAlimentacionDetalleDTO> obtenerDetalleDia(
        @PathVariable String fecha, // formato: YYYY-MM-DD
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // Retorna: DTO completo con:
    // - Info del día (estado, confirmación, etc.)
    // - Lista de consumos por lote/recría
    // - Detalle de insumos consumidos
    // - Alertas de stock insuficiente
    // - Déficits por insumo
}
```

### 3.3 Confirmar Día

```java
@PostMapping("/porcinos/calendario/dia/{fecha}/confirmar")
public ResponseEntity<DiaAlimentacionDTO> confirmarDia(
        @PathVariable String fecha,
        @RequestBody ConfirmacionDiaDTO confirmacion,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // 1. Validar que el día existe y está en estado PENDIENTE
    // 2. Validar que NO está confirmado (idempotencia)
    // 3. Actualizar estado a CONFIRMADO o CON_CORRECCIONES
    // 4. Guardar usuario, fecha/hora de confirmación, observaciones
    // 5. NO modificar stock (ya se descontó en consumo automático)
    // 6. NO recalcular consumos (día ya cerrado)
    // 7. Retornar día actualizado
}
```

### 3.4 Registrar Derrame/Pérdida

```java
@PostMapping("/porcinos/derrames-perdidas")
public ResponseEntity<DerramePerdidaDTO> registrarDerrame(
        @RequestBody DerramePerdidaDTO derrameData,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // 1. Validar campos obligatorios (motivo, observaciones)
    // 2. Validar cantidad > 0
    // 3. Descontar stock INMEDIATAMENTE (no permite negativo)
    // 4. Registrar movimiento de stock (tipo DERRAME)
    // 5. Asociar a día (solo referencia, no modifica consumo del día)
    // 6. Guardar derrame
    // 7. Retornar derrame guardado
}
```

### 3.5 Alertas Activas

```java
@GetMapping("/porcinos/alertas/stock-insuficiente")
public ResponseEntity<List<AlertaStockDTO>> obtenerAlertasStock(
        @RequestParam(required = false) LocalDate fechaDesde,
        @RequestParam(required = false) LocalDate fechaHasta,
        @AuthenticationPrincipal UserDetails userDetails) {
    
    // Retorna: Lista de alertas de stock insuficiente
    // Cada alerta incluye: insumo, déficit, fecha, día asociado, etc.
}
```

---

## 4. BUENAS PRÁCTICAS IMPLEMENTADAS

### 4.1 No Recalcular Días Cerrados
- Validar estado del día antes de procesar
- Si estado = CONFIRMADO o CON_CORRECCIONES, no permitir recálculo
- Log de intentos de modificar días cerrados

### 4.2 No Bloquear por Stock Insuficiente
- Consumo automático SIEMPRE se registra
- Stock puede quedar negativo solo en consumo automático
- Alertas visibles pero no bloquean operación

### 4.3 Separación Clara de Conceptos
- **Consumo automático**: Generado por job, permite negativo, desglose de recetas
- **Derrames/pérdidas**: Registro manual, NO permite negativo, impacto inmediato
- **Ajustes**: Correcciones manuales, registro completo de razón

### 4.4 Trazabilidad Completa
- Todo movimiento de stock queda registrado
- Auditoría completa: usuario, fecha, tipo, motivo
- Historial inmutable (no ediciones destructivas)

### 4.5 Código Modular
- Servicios separados por responsabilidad
- Repositorios específicos por entidad
- DTOs para transferencia de datos
- Validaciones centralizadas

---

## 5. DIAGRAMA DE FLUJO

```
[Día Anterior 00:30] → Job Automático
    ↓
Verificar si existe DiaAlimentacion para fecha
    ↓ (NO existe)
Crear DiaAlimentacion (estado: PENDIENTE)
    ↓
Obtener todas las recrías/madres activas
    ↓
Para cada recría/madre:
    ├─ Determinar etapa alimentación
    ├─ Obtener receta asignada
    ├─ Calcular consumo total
    ├─ Crear ConsumoDiarioAutomatico
    ├─ Para cada componente de la receta:
    │   ├─ Calcular cantidad requerida
    │   ├─ Obtener stock actual
    │   ├─ DESCONTAR (permitiendo negativo)
    │   ├─ Calcular déficit si hay
    │   ├─ Crear ConsumoDiarioDetalle
    │   └─ Registrar MovimientoStock
    └─ Generar alertas si hay déficit
    ↓
Actualizar resumen del día
    ↓
Día queda en estado PENDIENTE (ROJO en calendario)
    ↓
[Usuario confirma día] → Actualizar estado a CONFIRMADO (VERDE)
    ↓ (NO modifica stock, NO recalcula)
```

---

## 6. CONSIDERACIONES TÉCNICAS

### 6.1 Transacciones
- Consumo diario completo debe ser transaccional
- Si falla algún descuento, rollback completo del día
- Movimientos de stock siempre atómicos

### 6.2 Rendimiento
- Job programado ejecuta fuera de horario de trabajo
- Índices en fechas y estados
- Caché de recetas por etapa (evitar consultas repetidas)

### 6.3 Seguridad
- Validar permisos en cada endpoint
- Solo usuarios autorizados pueden confirmar días
- Auditoría completa de confirmaciones

### 6.4 Escalabilidad
- Procesar por lotes si hay muchas recrías
- Considerar procesamiento asíncrono para empresas grandes
- Monitoreo de tiempos de ejecución del job

---

## 7. PRÓXIMOS PASOS

1. ✅ Crear entidades JPA
2. ✅ Crear repositorios
3. ✅ Implementar servicios
4. ✅ Crear migración Flyway
5. ✅ Crear controladores y DTOs
6. ✅ Implementar job programado
7. ✅ Crear frontend (calendario, confirmación, derrames)
8. ✅ Pruebas unitarias e integración
9. ✅ Documentación API
