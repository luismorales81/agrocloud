# 🌱 Flujo Completo de Estados de Lote: Desde Siembra hasta Cosecha

## 📊 Diagrama de Flujo Visual

```
┌─────────────────────────────────────────────────────────────────────┐
│                        CICLO DE VIDA DE UN LOTE                     │
└─────────────────────────────────────────────────────────────────────┘

┌──────────────┐
│  DISPONIBLE  │ ← Lote vacío, listo para trabajar
└──────┬───────┘
       │ Acción: Preparar suelo
       ↓
┌──────────────┐
│ PREPARADO    │ ← Suelo listo para sembrar
└──────┬───────┘
       │ Acción: 🌱 SEMBRAR (Modal simplificado)
       ↓
┌──────────────┐
│  SEMBRADO    │ ← Cultivo recién sembrado (0-30 días)
└──────┬───────┘
       │ Crece naturalmente + labores de mantenimiento
       ↓
┌──────────────────┐
│ EN_CRECIMIENTO   │ ← Desarrollo vegetativo (30-90 días)
└──────┬───────────┘
       │ Crece naturalmente + labores de mantenimiento
       ↓
┌──────────────────┐
│ EN_FLORACION     │ ← Fase de floración (90-120 días)
└──────┬───────────┘
       │ Formación de frutos/granos
       ↓
┌──────────────────────┐
│ EN_FRUTIFICACION     │ ← Maduración de frutos (120-150 días)
└──────┬───────────────┘
       │ Alcanza madurez
       ↓
┌──────────────────────┐
│ LISTO_PARA_COSECHA   │ ← Cultivo maduro, listo para cosechar
└──────┬───────────────┘
       │ Acción: 🌾 COSECHAR (Modal simplificado)
       ↓
┌──────────────┐
│  COSECHADO   │ ← Cosecha completada
└──────┬───────┘
       │ Período de descanso del suelo
       ↓
┌──────────────┐
│ EN_DESCANSO  │ ← Recuperación del suelo (30-90 días)
└──────┬───────┘
       │ Preparación para nuevo ciclo
       ↓
┌──────────────────┐
│ EN_PREPARACION   │ ← Preparando para nueva siembra
└──────┬───────────┘
       │ Ciclo completo, vuelve al inicio
       ↓
┌──────────────┐
│  DISPONIBLE  │ ← Listo para un nuevo ciclo
└──────────────┘


CAMINOS ALTERNATIVOS:
═══════════════════════

┌─────────────────┐
│ Cualquier estado│
│ con cultivo     │
└────────┬────────┘
         │ Problemas (plagas, sequía, etc.)
         ↓
┌──────────────┐
│   ENFERMO    │ ← Cultivo con problemas
└────────┬─────┘
         │
         ├─→ Recuperación → Vuelve a estado anterior
         │
         └─→ No recupera → ABANDONADO

┌──────────────┐
│  ABANDONADO  │ ← Cultivo perdido
└────────┬─────┘
         │ Rehabilitación del terreno
         ↓
┌──────────────────┐
│ EN_PREPARACION   │
└──────────────────┘
```

---

## 🎯 ESTADOS DETALLADOS

### 1️⃣ **DISPONIBLE**

**Descripción**: Lote vacío, sin cultivo, listo para comenzar un nuevo ciclo.

**Características:**
- Sin cultivo activo
- Suelo en condiciones básicas
- Puede requerir limpieza o preparación

**Botones disponibles:**
- 🌱 **SEMBRAR** → Va directo a modal de siembra
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Análisis de suelo
- ✅ Desmalezado
- ✅ Limpieza
- ✅ Aplicación de herbicidas
- ❌ Fertilización (no tiene sentido sin cultivo)
- ❌ Riego (no tiene cultivo)

**Transiciones válidas:**
- → `PREPARADO` (después de preparar el suelo)
- → `SEMBRADO` (siembra directa)

**Duración típica**: Variable (puede estar disponible indefinidamente)

---

### 2️⃣ **PREPARADO**

**Descripción**: Suelo preparado (arado, rastra, nivelado) listo para la siembra.

**Características:**
- Suelo trabajado y listo
- Condiciones óptimas para sembrar
- Puede tener pre-fertilización

**Botones disponibles:**
- 🌱 **SEMBRAR** → Va a modal de siembra
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Análisis de suelo
- ✅ Desmalezado de última hora
- ✅ Fertilización pre-siembra
- ❌ Riego (aún no hay cultivo)
- ❌ Control de plagas (no hay cultivo)

**Transiciones válidas:**
- → `SEMBRADO` (realizar siembra)
- → `DISPONIBLE` (si se decide no sembrar)

**Duración típica**: 1-7 días (no conviene dejarlo mucho tiempo preparado)

---

### 3️⃣ **SEMBRADO**

**Descripción**: Cultivo recién sembrado, en fase de germinación y establecimiento inicial.

**Características:**
- Semilla plantada
- Emergencia de plántulas
- Establecimiento del cultivo
- Desarrollo de raíces

**Botones disponibles:**
- 🌾 **COSECHAR ▾** (dropdown) → Para casos especiales
  - 🌾 Cosechar Normal (si es necesario cosechar anticipadamente)
  - 🐄 Convertir a Forraje
  - 🚜 Limpiar Cultivo (si falló la siembra)
  - ⚠️ Abandonar Cultivo
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Riego (crítico en esta etapa)
- ✅ Control de malezas (temprano)
- ✅ Fertilización inicial
- ✅ Aplicación de herbicidas pre-emergentes
- ✅ Monitoreo de plagas
- ⚠️ Cosecha (solo en casos especiales: forraje, pérdida total, etc.)
- ❌ Siembra (ya está sembrado)

**Transiciones válidas:**
- → `EN_CRECIMIENTO` (desarrollo normal)
- → `ENFERMO` (problemas tempranos)
- → `ABANDONADO` (pérdida total de siembra)
- → `DISPONIBLE` (limpieza de cultivo fallido)

**Duración típica**: 15-30 días

---

### 4️⃣ **EN_CRECIMIENTO**

**Descripción**: Desarrollo vegetativo activo, crecimiento de hojas y tallos.

**Características:**
- Crecimiento vigoroso
- Desarrollo de biomasa
- Alta demanda de nutrientes y agua
- Etapa crítica para el rendimiento final

**Botones disponibles:**
- 🌾 **COSECHAR ▾** (dropdown) → Para casos especiales
  - 🐄 Convertir a Forraje (si se decide uso alternativo)
  - ⚠️ Abandonar Cultivo (por problemas severos)
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Riego (regular)
- ✅ Fertilización (nitrógeno principalmente)
- ✅ Control de malezas
- ✅ Control de plagas
- ✅ Aplicación de fungicidas
- ✅ Monitoreo constante
- ⚠️ Cosecha (solo forraje o casos de pérdida)
- ❌ Siembra

**Transiciones válidas:**
- → `EN_FLORACION` (desarrollo normal)
- → `ENFERMO` (problemas de salud)
- → `ABANDONADO` (pérdida por plagas/clima)

**Duración típica**: 30-60 días

---

### 5️⃣ **EN_FLORACION**

**Descripción**: Fase reproductiva, el cultivo está floreciendo.

**Características:**
- Aparición de flores
- Polinización activa
- Etapa sensible a estrés
- Determina el potencial de rendimiento

**Botones disponibles:**
- 🌾 **COSECHAR ▾** (dropdown) → Solo para emergencias
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Riego (crítico, sin mojar flores)
- ✅ Control de plagas (especialmente polinizadores de plagas)
- ✅ Aplicación cuidadosa de productos
- ✅ Monitoreo de enfermedades
- ⚠️ Fertilización (limitada, puede afectar floración)
- ⚠️ Control de malezas (cuidado con no dañar cultivo)
- ❌ Cosecha normal

**Transiciones válidas:**
- → `EN_FRUTIFICACION` (desarrollo normal)
- → `ENFERMO` (problemas durante floración)

**Duración típica**: 15-30 días

---

### 6️⃣ **EN_FRUTIFICACION**

**Descripción**: Formación y llenado de frutos/granos, maduración en proceso.

**Características:**
- Desarrollo de frutos/granos
- Llenado de grano
- Acumulación de reservas
- Maduración gradual

**Botones disponibles:**
- 🌾 **COSECHAR ▾** (dropdown) → Puede cosechar anticipadamente
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Riego (esencial para llenado de grano)
- ✅ Control de plagas (proteger frutos)
- ✅ Aplicación de fungicidas (prevenir enfermedades de fruto)
- ✅ Monitoreo de madurez
- ⚠️ Fertilización (limitada)
- ⚠️ Cosecha (anticipada si es necesario)
- ❌ Control de malezas (tarde, puede dañar cultivo)

**Transiciones válidas:**
- → `LISTO_PARA_COSECHA` (madurez alcanzada)
- → `ENFERMO` (problemas en etapa final)

**Duración típica**: 20-40 días

---

### 7️⃣ **LISTO_PARA_COSECHA**

**Descripción**: Cultivo en punto óptimo de madurez, listo para cosechar.

**Características:**
- Madurez fisiológica completa
- Humedad óptima del grano
- Condiciones ideales para cosecha
- Ventana de tiempo limitada

**Botones disponibles:**
- 🌾 **COSECHAR ▾** (dropdown) → **ACCIÓN PRINCIPAL**
  - 🌾 **Cosechar Normal** ← Opción recomendada
  - 🐄 Convertir a Forraje
  - ⚠️ Abandonar Cultivo

**Labores permitidas:**
- ✅ **COSECHA** (acción principal)
- ✅ Monitoreo de humedad
- ✅ Análisis de calidad
- ⚠️ Riego (solo si se retrasa cosecha)
- ❌ Fertilización
- ❌ Control de plagas
- ❌ Control de malezas

**Transiciones válidas:**
- → `COSECHADO` (después de cosechar)
- → `ENFERMO` (deterioro por retraso)

**Duración típica**: 3-15 días (ventana corta, no conviene esperar)

---

### 8️⃣ **COSECHADO**

**Descripción**: Cosecha completada, lote sin cultivo activo.

**Características:**
- Cultivo removido
- Rastrojos en el suelo
- Suelo agotado
- Necesita descanso

**Botones disponibles:**
- 📝 **Editar**
- 📊 **Historial**
- 📈 **Ver Cosecha** (detalle de la cosecha realizada)

**Labores permitidas:**
- ✅ Análisis de suelo
- ✅ Desmalezado
- ✅ Limpieza de rastrojos
- ✅ Incorporación de materia orgánica
- ❌ Siembra (debe descansar primero)
- ❌ Riego
- ❌ Fertilización (aún no tiene sentido)

**Transiciones válidas:**
- → `EN_DESCANSO` (período de recuperación del suelo)

**Duración típica**: Inmediato (pasa rápido a EN_DESCANSO)

---

### 9️⃣ **EN_DESCANSO**

**Descripción**: Lote en período de recuperación del suelo post-cosecha.

**Características:**
- Suelo en recuperación
- Regeneración de nutrientes
- Descomposición de rastrojos
- Barbecho

**Botones disponibles:**
- 📝 **Editar**
- 📊 **Historial**
- 🔄 **Liberar para Siembra** (si cumplió período mínimo)

**Labores permitidas:**
- ✅ Análisis de suelo
- ✅ Desmalezado
- ✅ Mantenimiento
- ✅ Aplicación de enmiendas
- ✅ Incorporación de abonos verdes
- ❌ Siembra (hasta completar período mínimo)
- ❌ Riego
- ❌ Fertilización química

**Transiciones válidas:**
- → `EN_PREPARACION` (después del descanso mínimo)
- → `DISPONIBLE` (si se libera directamente)

**Duración típica**: 30-90 días (según cultivo anterior)

---

### 🔟 **EN_PREPARACION**

**Descripción**: Preparando el lote para un nuevo ciclo de siembra.

**Características:**
- Trabajos de preparación del suelo
- Arado, rastra, nivelación
- Incorporación de enmiendas
- Acondicionamiento

**Botones disponibles:**
- 🌱 **SEMBRAR** (si ya está listo)
- 📝 **Editar**
- 📊 **Historial**

**Labores permitidas:**
- ✅ Arado
- ✅ Rastra
- ✅ Nivelación
- ✅ Aplicación de enmiendas
- ✅ Fertilización de base
- ✅ Análisis de suelo
- ✅ Desmalezado
- ⚠️ Siembra (cuando esté listo)
- ❌ Riego (aún no hay cultivo)

**Transiciones válidas:**
- → `DISPONIBLE` (preparación básica)
- → `PREPARADO` (preparación completa)
- → `SEMBRADO` (siembra directa)

**Duración típica**: 3-10 días

---

## 🚨 ESTADOS ESPECIALES (Excepciones)

### ⚕️ **ENFERMO**

**Descripción**: Cultivo con problemas de salud (plagas, enfermedades, estrés severo).

**Características:**
- Presencia de plagas/enfermedades
- Deterioro visible
- Riesgo de pérdida parcial o total
- Requiere intervención urgente

**Botones disponibles:**
- 🚨 **Tratamiento de Emergencia**
- ⚠️ **Abandonar Cultivo**
- 📝 **Editar**

**Labores permitidas:**
- ✅ Control de plagas (intensivo)
- ✅ Aplicación de fungicidas/insecticidas
- ✅ Riego (según diagnóstico)
- ✅ Fertilización foliar (recuperación)
- ✅ Monitoreo constante

**Transiciones válidas:**
- → Estado anterior (si se recupera)
- → `ABANDONADO` (si no se puede salvar)

---

### ⚠️ **ABANDONADO**

**Descripción**: Cultivo perdido, abandonado por problemas irrecuperables.

**Características:**
- Pérdida total del cultivo
- Cultivo muerto o irrecuperable
- Requiere limpieza y rehabilitación

**Botones disponibles:**
- 🚜 **Limpiar y Rehabilitar**
- 📝 **Editar**

**Labores permitidas:**
- ✅ Limpieza
- ✅ Desmalezado
- ✅ Incorporación de rastrojos
- ✅ Análisis de suelo (diagnóstico)

**Transiciones válidas:**
- → `EN_PREPARACION` (después de rehabilitar)

---

## 📋 TABLA RESUMEN: Botones por Estado

| Estado | Botón SEMBRAR | Botón COSECHAR | Otros Botones |
|--------|---------------|----------------|---------------|
| **DISPONIBLE** | ✅ SÍ | ❌ NO | Editar, Historial |
| **PREPARADO** | ✅ SÍ | ❌ NO | Editar, Historial |
| **EN_PREPARACION** | ✅ SÍ | ❌ NO | Editar, Historial |
| **SEMBRADO** | ❌ NO | ⚠️ SÍ (Dropdown) | Editar, Historial |
| **EN_CRECIMIENTO** | ❌ NO | ⚠️ SÍ (Dropdown) | Editar, Historial |
| **EN_FLORACION** | ❌ NO | ⚠️ SÍ (Dropdown) | Editar, Historial |
| **EN_FRUTIFICACION** | ❌ NO | ⚠️ SÍ (Dropdown) | Editar, Historial |
| **LISTO_PARA_COSECHA** | ❌ NO | ✅ SÍ (Dropdown) | Editar, Historial |
| **COSECHADO** | ❌ NO | ❌ NO | Editar, Historial, Ver Cosecha |
| **EN_DESCANSO** | ❌ NO | ❌ NO | Editar, Historial, Liberar |
| **ENFERMO** | ❌ NO | ❌ NO | Tratamiento, Abandonar |
| **ABANDONADO** | ❌ NO | ❌ NO | Limpiar, Rehabilitar |

**Leyenda:**
- ✅ = Botón visible y acción recomendada
- ⚠️ = Botón visible pero para casos especiales
- ❌ = Botón NO visible

---

## 🎯 RECOMENDACIONES PARA IMPLEMENTACIÓN

### 1. **Backend - Actualizar validaciones:**

```java
// EstadoLote.java
public boolean puedeCosechar() {
    return this == SEMBRADO ||           // Cosecha anticipada
           this == EN_CRECIMIENTO ||     // Forraje, emergencia
           this == EN_FLORACION ||       // Casos especiales
           this == EN_FRUTIFICACION ||   // Cosecha semi-madura
           this == LISTO_PARA_COSECHA;   // IDEAL
}
```

### 2. **Frontend - Mantener lógica actual (ya corregida):**

```typescript
const puedeSembrar = (estado: string): boolean => {
  return estado === 'DISPONIBLE' || 
         estado === 'PREPARADO' || 
         estado === 'EN_PREPARACION';
};

const puedeCosechar = (estado: string): boolean => {
  return estado === 'SEMBRADO' ||           // ✅ YA CORREGIDO
         estado === 'LISTO_PARA_COSECHA' || 
         estado === 'EN_CRECIMIENTO' || 
         estado === 'EN_FLORACION' || 
         estado === 'EN_FRUTIFICACION';
};
```

### 3. **UX - Dropdown para cosecha con contexto:**

Cuando el estado es `SEMBRADO`, `EN_CRECIMIENTO`, etc. (no óptimos):
```
🌾 Cosechar ▾
  ├─ ⚠️ Cosechar Ahora (no es el momento óptimo)
  ├─ 🐄 Convertir a Forraje
  └─ ⚠️ Abandonar Cultivo
```

Cuando el estado es `LISTO_PARA_COSECHA` (óptimo):
```
🌾 Cosechar ▾
  ├─ ✅ Cosechar Ahora (momento óptimo) ← Destacado
  ├─ 🐄 Convertir a Forraje
  └─ 🚜 Limpiar Cultivo
```

---

## ✅ RESUMEN EJECUTIVO

### Ciclo Normal (Sin problemas):
```
DISPONIBLE → PREPARADO → SEMBRADO → EN_CRECIMIENTO → 
EN_FLORACION → EN_FRUTIFICACION → LISTO_PARA_COSECHA → 
COSECHADO → EN_DESCANSO → EN_PREPARACION → DISPONIBLE
```

### Cuándo aparece botón SEMBRAR (🌱):
- `DISPONIBLE`
- `PREPARADO`
- `EN_PREPARACION`

### Cuándo aparece botón COSECHAR (🌾):
- `SEMBRADO` (dropdown con advertencia)
- `EN_CRECIMIENTO` (dropdown con advertencia)
- `EN_FLORACION` (dropdown con advertencia)
- `EN_FRUTIFICACION` (dropdown)
- `LISTO_PARA_COSECHA` (dropdown, opción destacada)

### Duración total del ciclo típico:
- Soja: 120-150 días
- Maíz: 150-180 días
- Trigo: 180-210 días

---

**¿Este flujo tiene sentido para tu operación agrícola?** 🌾
