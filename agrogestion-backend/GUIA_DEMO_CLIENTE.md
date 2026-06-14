# 🎯 Guía de Demostración del Sistema AgroCloud

## 📋 Preparación Pre-Demo

### 1. Datos Requeridos

#### ✅ Datos Base (Ya cargados)
- ✅ Empresa: "AgroCloud" o similar
- ✅ Usuario administrador: `admin@agrocloud.com`
- ✅ Catálogos de porcinos completos

#### 📝 Scripts a Ejecutar (en orden)
```sql
-- 1. Crear tablas de catálogos (si no existen)
-- Ejecutar: CREAR_TABLAS_CATALOGOS_PORCINOS.sql

-- 2. Insertar catálogos
-- Ejecutar: INSERTAR_CATALOGOS_PORCINOS.sql

-- 3. Insertar datos de prueba de porcinos
-- Ejecutar: INSERTAR_DATOS_PORCINOS.sql
```

### 2. Verificación Pre-Demo

Antes de la demo, verificar que existan:
- ✅ Al menos 5-8 madres activas
- ✅ Al menos 2-3 padrillos activos
- ✅ Al menos 3-5 gestaciones activas
- ✅ Al menos 2-3 partos registrados
- ✅ Al menos 1-2 destetes
- ✅ Al menos 1-2 recrías activas
- ✅ Al menos 1-2 ventas registradas
- ✅ Al menos algunos consumos de alimento
- ✅ Al menos algunos eventos sanitarios

---

## 🎬 FLUJO DE DEMOSTRACIÓN (Camino Feliz)

### **FASE 1: INTRODUCCIÓN Y CONTEXTO** (2-3 minutos)

#### 1.1 Login y Dashboard Principal
```
📍 Ruta: /porcinos/dashboard
```

**Qué mostrar:**
- ✅ Calendario con eventos automáticos:
  - Partos próximos (desde gestaciones activas)
  - Ecografías programadas
  - Destetes programados
  - Controles de celo
- ✅ Explicar que el sistema genera alertas automáticamente
- ✅ Mostrar cómo se pueden crear recordatorios manuales

**Mensaje clave:**
> "El sistema le avisa automáticamente qué debe hacer cada día, basándose en los datos que registra"

---

### **FASE 2: GESTIÓN DE REPRODUCTORES** (5-7 minutos)

#### 2.1 Madres
```
📍 Ruta: /porcinos/madres
```

**Qué mostrar:**
- ✅ Lista de madres con estados (Gestación, Lactancia, Adulta)
- ✅ Detalle de una madre:
  - Historial de partos
  - Historial de servicios
  - Estado actual
  - Raza y ubicación
- ✅ Crear una nueva madre (opcional, si hay tiempo)

**Mensaje clave:**
> "Seguimiento individual de cada reproductora con todo su historial"

#### 2.2 Padrillos
```
📍 Ruta: /porcinos/padrillos
```

**Qué mostrar:**
- ✅ Lista de padrillos activos
- ✅ Detalle de un padrillo
- ✅ Explicar que se pueden registrar servicios con cada uno

---

### **FASE 3: CICLO REPRODUCTIVO** (8-10 minutos)

#### 3.1 Servicios
```
📍 Ruta: /porcinos/servicios
```

**Qué mostrar:**
- ✅ Lista de servicios realizados
- ✅ Crear un nuevo servicio:
  - Seleccionar madre
  - Tipo (IA o Monta Natural)
  - Seleccionar padrillo
  - Fecha de servicio
- ✅ Explicar que el sistema programa automáticamente:
  - Control de celo (21 días después)
  - Ecografía (si aplica)

**Mensaje clave:**
> "Al registrar un servicio, el sistema automáticamente programa los controles necesarios"

#### 3.2 Gestación
```
📍 Ruta: /porcinos/gestacion
```

**Qué mostrar:**
- ✅ Lista de gestaciones activas
- ✅ Detalle de una gestación:
  - Fecha de inicio
  - Fecha probable de parto
  - Chequeos realizados
- ✅ Mostrar que aparece en el calendario como "Parto próximo"
- ✅ Agregar un chequeo de gestación (ecografía)

**Mensaje clave:**
> "Seguimiento completo del ciclo de gestación con alertas automáticas"

#### 3.3 Partos
```
📍 Ruta: /porcinos/partos
```

**Qué mostrar:**
- ✅ Lista de partos registrados
- ✅ Detalle de un parto:
  - Nacidos vivos/muertos/momias
  - Peso promedio
  - Tipo de parto
- ✅ Crear un nuevo parto:
  - Seleccionar madre
  - Registrar nacidos
  - Peso promedio
- ✅ Explicar que después del parto se puede registrar el destete

**Mensaje clave:**
> "Registro detallado de cada parto con todos los indicadores productivos"

#### 3.4 Destetes
```
📍 Ruta: /porcinos/partos/[id] (sección destetes)
```

**Qué mostrar:**
- ✅ Registrar un destete:
  - Fecha de destete
  - Cantidad destetados
  - Peso promedio
  - Días de lactancia
- ✅ Explicar que después del destete los lechones van a recría

---

### **FASE 4: CRECIMIENTO Y PRODUCCIÓN** (5-7 minutos)

#### 4.1 Recría
```
📍 Ruta: /porcinos/recria
```

**Qué mostrar:**
- ✅ Lista de recrías activas
- ✅ Detalle de una recría:
  - Cantidad de animales
  - Peso promedio
  - Etapa
  - Destino (Futura madre, Engorde)
- ✅ Registrar ingreso de recría:
  - Seleccionar lote
  - Cantidad de animales
  - Peso promedio
  - Sexo y destino

**Mensaje clave:**
> "Gestión por lotes para recría y engorde, más eficiente que el seguimiento individual"

---

### **FASE 5: ALIMENTACIÓN** (3-5 minutos)

#### 5.1 Consumos de Alimento
```
📍 Ruta: /porcinos/alimentacion/consumos
```

**Qué mostrar:**
- ✅ Lista de consumos registrados
- ✅ Registrar un consumo:
  - Categoría (Madres, Recría, Engorde, etc.)
  - Tipo de alimento
  - Cantidad en kg
  - Fecha
- ✅ Mostrar que se puede asociar a lote o madre específica

**Mensaje clave:**
> "Control de consumo de alimento por categoría para calcular costos y eficiencia"

---

### **FASE 6: SANIDAD** (3-5 minutos)

#### 6.1 Eventos Sanitarios
```
📍 Ruta: /porcinos/configuraciones (pestaña Eventos Sanitarios)
```

**Qué mostrar:**
- ✅ Lista de eventos sanitarios registrados
- ✅ Explicar que se pueden registrar:
  - Vacunaciones
  - Tratamientos
  - Desparasitaciones
- ✅ Mostrar control de retiros (fechas de retiro de medicamentos)

**Mensaje clave:**
> "Trazabilidad completa de todos los tratamientos sanitarios"

---

### **FASE 7: VENTAS Y ECONOMÍA** (3-5 minutos)

#### 7.1 Ventas
```
📍 Ruta: /porcinos/ventas
```

**Qué mostrar:**
- ✅ Lista de ventas realizadas
- ✅ Registrar una venta:
  - Tipo (Engorde o Reproductor)
  - Cantidad
  - Peso promedio
  - Precio por kg
  - Cliente
- ✅ Mostrar ingreso total calculado automáticamente

**Mensaje clave:**
> "Registro de ventas con cálculo automático de ingresos"

---

### **FASE 8: REPORTES Y ANÁLISIS** (5-7 minutos) ⭐ **MOMENTO CLAVE**

#### 8.1 Reportes
```
📍 Ruta: /porcinos/reportes
```

**Qué mostrar (en orden de importancia):**

1. **Reporte Reproductivo** ⭐⭐⭐
   - Seleccionar fechas (último mes)
   - Generar reporte
   - Mostrar:
     - Índices: Tasa de parición, Lechones/parto
     - Detalle de servicios, gestaciones, partos, destetes
   - **Exportar a Excel** 📊
   - Mostrar el archivo Excel generado

2. **Reporte Productivo** ⭐⭐⭐
   - Generar (no requiere fechas)
   - Mostrar KPIs:
     - Total madres
     - Tasa de parición
     - Lechones vivos/parto
     - Comparación con objetivos
   - **Exportar a Excel** 📊

3. **Reporte Económico** ⭐⭐
   - Seleccionar fechas
   - Generar reporte
   - Mostrar:
     - Ingresos totales
     - Costos
     - Balance neto
     - Margen de beneficio
   - **Exportar a Excel** 📊

4. **Reporte de Mortalidad** ⭐⭐
   - Seleccionar fechas
   - Generar reporte
   - Mostrar:
     - Muertes por etapa
     - Principales causas
     - Tendencias

**Mensaje clave:**
> "Todos los datos que registra se convierten automáticamente en reportes profesionales exportables a Excel para análisis y presentaciones"

---

### **FASE 9: CONFIGURACIONES** (3-5 minutos)

#### 9.1 Configuraciones
```
📍 Ruta: /porcinos/configuraciones
```

**Qué mostrar:**
- ✅ Catálogos configurables:
  - Razas
  - Tipos de alimento
  - Causas de mortalidad
  - Ubicaciones internas (jerarquía: Galpón → Sala → Corral)
- ✅ Parámetros productivos:
  - Días de gestación
  - Días de lactancia
  - Objetivos de producción
- ✅ Parámetros económicos:
  - Costos por etapa
  - Precios de venta

**Mensaje clave:**
> "El sistema es completamente configurable según las necesidades específicas de cada granja"

---

## 🎯 MENSAJES CLAVE A TRANSMITIR

### 1. **Automatización**
- El sistema genera alertas y recordatorios automáticamente
- No necesita recordar fechas manualmente
- El calendario le dice qué hacer cada día

### 2. **Trazabilidad Completa**
- Todo queda registrado: desde el servicio hasta la venta
- Historial completo de cada animal
- Trazabilidad sanitaria completa

### 3. **Análisis y Toma de Decisiones**
- Reportes profesionales exportables
- KPIs en tiempo real
- Comparación con objetivos
- Análisis de rentabilidad

### 4. **Eficiencia**
- Gestión híbrida: individual para reproductores, por lote para crecimiento
- Reducción de errores
- Ahorro de tiempo en registros

### 5. **Escalabilidad**
- Funciona para granjas pequeñas y grandes
- Configurable según necesidades
- Multi-empresa (si aplica)

---

## ⏱️ DURACIÓN TOTAL ESTIMADA

- **Demo rápida (30 minutos):**
  - Fase 1: 2 min
  - Fase 2: 3 min
  - Fase 3: 5 min
  - Fase 4: 2 min
  - Fase 5: 2 min
  - Fase 6: 2 min
  - Fase 7: 2 min
  - **Fase 8: 8 min** ⭐ (MÁS IMPORTANTE)
  - Fase 9: 2 min
  - Preguntas: 2 min

- **Demo completa (45-60 minutos):**
  - Todas las fases con más detalle
  - Crear datos en vivo
  - Mostrar más funcionalidades
  - Más tiempo para preguntas

---

## 🚨 PUNTOS DE ATENCIÓN

### Antes de la Demo:
1. ✅ Verificar que todos los datos estén cargados
2. ✅ Probar que el calendario muestre eventos
3. ✅ Verificar que los reportes se generen correctamente
4. ✅ Tener un Excel de ejemplo descargado para mostrar

### Durante la Demo:
1. ⚠️ Si algo no funciona, no detenerse mucho tiempo
2. ⚠️ Enfocarse en los beneficios, no en los detalles técnicos
3. ⚠️ Mostrar el flujo completo: desde servicio hasta venta
4. ⚠️ Destacar la exportación a Excel de los reportes

### Después de la Demo:
1. 📝 Tomar nota de preguntas específicas
2. 📝 Ofrecer demo personalizada si necesita ver algo específico
3. 📝 Proporcionar acceso de prueba si es posible

---

## 📊 CHECKLIST PRE-DEMO

- [ ] Datos de prueba cargados (madres, padrillos, gestaciones, partos)
- [ ] Catálogos completos
- [ ] Calendario muestra eventos
- [ ] Reportes funcionan y se pueden exportar
- [ ] Navegación fluida entre pantallas
- [ ] Ejemplos de Excel descargados para mostrar
- [ ] Usuario de demo preparado (admin@agrocloud.com)
- [ ] Conexión a internet estable
- [ ] Navegador actualizado
- [ ] Pantalla en modo presentación (si aplica)

---

## 💡 TIPS PARA UNA DEMO EXITOSA

1. **Empiece con el calendario**: Es lo más visual e impactante
2. **Termine con reportes**: Es el valor agregado más claro
3. **Use datos realistas**: Nombres de animales, fechas recientes
4. **Muestre el flujo completo**: Servicio → Gestación → Parto → Destete → Recría → Venta
5. **Destaque la automatización**: "El sistema hace esto automáticamente"
6. **Muestre la exportación a Excel**: Es muy valorado por los clientes
7. **Sea específico**: "Con este reporte puede ver exactamente cuántos lechones destetó por madre este mes"

---

## 🎬 SCRIPT DE PRESENTACIÓN SUGERIDO

### Apertura (1 min)
> "Hoy le voy a mostrar cómo AgroCloud puede ayudarle a gestionar su producción porcina de manera más eficiente y profesional. Vamos a ver cómo el sistema le ayuda desde el registro de un servicio hasta la generación de reportes para análisis."

### Desarrollo (25-28 min)
> [Seguir el flujo de las fases]

### Cierre (1-2 min)
> "Como puede ver, AgroCloud le permite tener control total de su producción, con alertas automáticas, trazabilidad completa y reportes profesionales. ¿Hay alguna funcionalidad específica que le gustaría ver con más detalle?"

---

## 📈 MÉTRICAS A DESTACAR

- **Tiempo ahorrado**: "En lugar de llevar registros en papel, todo está digitalizado"
- **Precisión**: "Los cálculos son automáticos, sin errores humanos"
- **Trazabilidad**: "Sabe exactamente qué pasó con cada animal"
- **Análisis**: "Puede tomar decisiones basadas en datos reales"
- **Profesionalismo**: "Reportes listos para presentar a clientes o inversores"

---

## 🔄 FLUJO ALTERNATIVO (Si el cliente tiene necesidades específicas)

### Cliente enfocado en Reproducción:
- Más tiempo en: Servicios, Gestación, Partos, Reporte Reproductivo

### Cliente enfocado en Producción:
- Más tiempo en: Recría, Alimentación, Reporte Productivo, Reporte Económico

### Cliente enfocado en Sanidad:
- Más tiempo en: Eventos Sanitarios, Reporte Sanitario, Configuraciones sanitarias

### Cliente enfocado en Rentabilidad:
- Más tiempo en: Ventas, Reporte Económico, Parámetros económicos

---

## ✅ CONCLUSIÓN

El "camino feliz" para una demo exitosa es:

1. **Empezar con impacto**: Calendario con eventos automáticos
2. **Mostrar el flujo completo**: Desde servicio hasta venta
3. **Destacar la automatización**: El sistema hace el trabajo pesado
4. **Terminar con valor**: Reportes exportables a Excel
5. **Ser flexible**: Adaptarse a las necesidades específicas del cliente

**El objetivo es que el cliente vea que:**
- ✅ Es fácil de usar
- ✅ Ahorra tiempo
- ✅ Genera información valiosa
- ✅ Es profesional
- ✅ Le ayuda a tomar mejores decisiones














