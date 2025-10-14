# 🌾 Flujo Completo de Estados del Lote - AgroCloud

## 📊 Diagrama de Estados y Transiciones

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    CICLO DE VIDA DEL LOTE AGRÍCOLA                      │
└─────────────────────────────────────────────────────────────────────────┘

                              ┌──────────────┐
                              │  DISPONIBLE  │ ← Estado inicial
                              └──────┬───────┘
                                     │ arado, rastra, fertilización
                                     ↓
                              ┌──────────────┐
                    ┌────────→│ EN_PREPARACION│
                    │         └──────┬───────┘
                    │                │ arado, rastra
                    │                ↓
                    │         ┌──────────────┐
                    │         │   PREPARADO  │
                    │         └──────┬───────┘
                    │                │ 🌱 SIEMBRA
                    │                ↓
                    │         ┌──────────────┐
                    │         │   SEMBRADO   │
                    │         └──────┬───────┘
                    │                │ Crecimiento natural
                    │                │ + Labores: riego, fertilización
                    │                ↓
                    │         ┌──────────────┐
                    │    ┌──→│EN_CRECIMIENTO│←──┐
                    │    │   └──────┬───────┘   │
                    │    │          │            │
                    │    │          ↓            │ Control de plagas
                    │    │   ┌──────────────┐   │ y malezas
                    │    │   │ EN_FLORACION │   │
                    │    │   └──────┬───────┘   │
                    │    │          │            │
                    │    │          ↓            │
                    │    │   ┌──────────────────┐│
                    │    │   │EN_FRUTIFICACION  ││
                    │    │   └──────┬───────────┘│
                    │    │          │             │
                    │    │          ↓             │
                    │    │   ┌────────────────┐  │
                    │    │   │LISTO_PARA_     │  │
                    │    │   │    COSECHA     │  │
                    │    │   └────────┬───────┘  │
                    │    │            │           │
                    │    │            ↓           │
                    │    │   ┌────────────────┐  │
                    │    │   │  EN_COSECHA    │  │
                    │    │   └────────┬───────┘  │
                    │    │            │           │
                    │    │            │ 🌾 COSECHA│
                    │    │            ↓           │
                    │    │   ┌────────────────┐  │
                    │    │   │   COSECHADO    │  │
                    │    │   └────────┬───────┘  │
                    │    │            │           │
                    │    │            ↓           │
                    │    │   ┌────────────────┐  │
                    │    └───│  EN_DESCANSO   │  │
                    │        └────────┬───────┘  │
                    │                 │           │
                    └─────────────────┘           │
                                                  │
                         ┌────────────────┐       │
                    ┌───→│    ENFERMO     │───────┘
                    │    └────────────────┘
                    │    
                    │    ┌────────────────┐
                    └───→│  ABANDONADO    │
                         └────────────────┘
```

---

## 📋 Estados del Lote - Descripción Detallada

### 🟢 **1. DISPONIBLE** (Estado Inicial)
**Descripción**: Lote libre, listo para comenzar un nuevo ciclo de cultivo

**Características**:
- Sin cultivo activo
- Tierra sin preparar específicamente
- Puede estar en barbecho natural

**Labores Disponibles**:
- 🚜 **Arado**: Preparación profunda del suelo
- 🔧 **Rastra**: Nivelación y refinamiento
- 🌿 **Fertilización**: Aplicación de nutrientes base
- 👁️ **Monitoreo**: Inspección general

**Transiciones Posibles**:
- → `EN_PREPARACION`: Después de labores de preparación
- → `PREPARADO`: Después de preparación completa
- → `ENFERMO`: Si se detectan problemas
- → `ABANDONADO`: Si se decide no usar temporalmente

---

### 🟢 **2. EN_PREPARACION** (Preparación Activa)
**Descripción**: Lote en proceso de preparación para siembra

**Características**:
- Labores de preparación en curso
- No apto aún para siembra
- Proceso de acondicionamiento del suelo

**Labores Disponibles**:
- 🚜 **Arado**: Continuar preparación profunda
- 🔧 **Rastra**: Nivelación y refinamiento
- 🌿 **Fertilización**: Aplicación de nutrientes
- 👁️ **Monitoreo**: Inspección del progreso

**Transiciones Posibles**:
- → `PREPARADO`: Cuando la preparación está completa
- → `DISPONIBLE`: Si se cancela la preparación
- → `ENFERMO`: Si se detectan problemas
- → `ABANDONADO`: Si se abandona el proceso

---

### 🟢 **3. PREPARADO** (Listo para Siembra)
**Descripción**: Lote preparado y listo para sembrar

**Características**:
- Suelo acondicionado
- Fertilización base aplicada
- Condiciones óptimas para siembra

**Labores Disponibles**:
- 🌱 **Siembra**: Plantación del cultivo (ACCIÓN PRINCIPAL)
- 🌿 **Fertilización**: Ajustes finales de nutrientes
- 👁️ **Monitoreo**: Verificación pre-siembra

**Transiciones Posibles**:
- → `SEMBRADO`: Después de realizar la siembra ✅ PRINCIPAL
- → `DISPONIBLE`: Si se decide no sembrar
- → `EN_PREPARACION`: Si requiere más preparación
- → `ENFERMO`: Si se detectan problemas

**⚠️ NOTA IMPORTANTE**: Este es el ÚNICO estado desde el cual se puede sembrar correctamente.

---

### 🔵 **4. SEMBRADO** (Cultivo Recién Sembrado)
**Descripción**: Cultivo plantado en desarrollo inicial

**Características**:
- Semillas germinando o plántulas jóvenes
- Fase crítica de establecimiento
- Requiere cuidados especiales

**Labores Disponibles**:
- 💧 **Riego**: Aplicación de agua
- 🌿 **Fertilización**: Nutrientes para desarrollo
- 💨 **Pulverización**: Protección preventiva
- 👁️ **Monitoreo**: Seguimiento del desarrollo

**Transiciones Posibles**:
- → `EN_CRECIMIENTO`: Progreso natural del cultivo ✅ NORMAL
- → `COSECHADO`: Cosecha anticipada por problemas
- → `ENFERMO`: Si se detectan plagas o enfermedades
- → `ABANDONADO`: Si se pierde el cultivo

---

### 🔵 **5. EN_CRECIMIENTO** (Desarrollo Vegetativo)
**Descripción**: Cultivo en fase de crecimiento vegetativo activo

**Características**:
- Desarrollo de follaje y raíces
- Alta demanda de nutrientes
- Susceptible a malezas y plagas

**Labores Disponibles**:
- 💧 **Riego**: Aplicación de agua
- 🌿 **Fertilización**: Nutrientes para crecimiento
- 💨 **Pulverización**: Aplicación de fitosanitarios
- 🌾 **Desmalezado**: Control manual de malezas
- 🧪 **Aplicación de herbicida**: Control químico de malezas
- 🐛 **Aplicación de insecticida**: Control de plagas
- 👁️ **Monitoreo**: Seguimiento constante

**Transiciones Posibles**:
- → `EN_FLORACION`: Progreso natural del cultivo ✅ NORMAL
- → `COSECHADO`: Cosecha anticipada (forraje, etc.)
- → `ENFERMO`: Si se detectan problemas severos
- → `ABANDONADO`: Si se pierde el cultivo

---

### 🔵 **6. EN_FLORACION** (Floración Activa)
**Descripción**: Cultivo en fase de floración

**Características**:
- Producción de flores
- Polinización en proceso
- Sensible a factores ambientales

**Labores Disponibles**:
- 💧 **Riego**: Aplicación controlada de agua
- 💨 **Pulverización**: Aplicación cuidadosa de protección
- 🐛 **Aplicación de insecticida**: Control de plagas (cuidado con polinizadores)
- 👁️ **Monitoreo**: Seguimiento de la floración

**Transiciones Posibles**:
- → `EN_FRUTIFICACION`: Progreso natural del cultivo ✅ NORMAL
- → `COSECHADO`: Cosecha de flores (casos específicos)
- → `ENFERMO`: Si se detectan problemas
- → `ABANDONADO`: Si se pierde el cultivo

---

### 🔵 **7. EN_FRUTIFICACION** (Desarrollo de Frutos)
**Descripción**: Cultivo en fase de desarrollo de frutos/granos

**Características**:
- Formación y llenado de granos/frutos
- Alta demanda hídrica
- Etapa crítica para rendimiento final

**Labores Disponibles**:
- 💧 **Riego**: Aplicación de agua (crítico)
- 💨 **Pulverización**: Protección de frutos
- 🐛 **Aplicación de insecticida**: Control de plagas
- 👁️ **Monitoreo**: Seguimiento del desarrollo

**Transiciones Posibles**:
- → `LISTO_PARA_COSECHA`: Cuando el cultivo madura ✅ NORMAL
- → `COSECHADO`: Cosecha anticipada
- → `ENFERMO`: Si se detectan problemas
- → `ABANDONADO`: Si se pierde el cultivo

---

### 🟠 **8. LISTO_PARA_COSECHA** (Cultivo Maduro)
**Descripción**: Cultivo maduro, listo para cosechar

**Características**:
- Punto óptimo de madurez
- Máxima calidad y rendimiento
- Ventana temporal para cosecha

**Labores Disponibles**:
- 🌾 **Cosecha**: Recolección del cultivo (ACCIÓN PRINCIPAL)
- 👁️ **Monitoreo**: Verificación de condiciones

**Transiciones Posibles**:
- → `EN_COSECHA`: Al iniciar la cosecha
- → `COSECHADO`: Al completar la cosecha ✅ NORMAL
- → `ENFERMO`: Si se deteriora el cultivo
- → `ABANDONADO`: Si se pierde la ventana de cosecha

**⚠️ NOTA IMPORTANTE**: Estado ideal para cosechar. No retrasar mucho.

---

### 🟣 **9. EN_COSECHA** (Cosecha en Proceso)
**Descripción**: Proceso de cosecha en curso

**Características**:
- Recolección activa
- Personal y maquinaria trabajando
- Proceso parcialmente completado

**Labores Disponibles**:
- 🌾 **Cosecha**: Continuar recolección
- 👁️ **Monitoreo**: Supervisión del proceso

**Transiciones Posibles**:
- → `COSECHADO`: Al completar la cosecha ✅ NORMAL
- → `LISTO_PARA_COSECHA`: Si se interrumpe temporalmente

---

### 🟣 **10. COSECHADO** (Cosecha Completada)
**Descripción**: Cosecha finalizada exitosamente

**Características**:
- Cultivo recolectado
- Lote libre pero con residuos
- Requiere preparación para nuevo ciclo

**Labores Disponibles**:
- 🚜 **Arado**: Incorporación de rastrojos
- 🔧 **Rastra**: Preparación de suelo
- 👁️ **Monitoreo**: Evaluación post-cosecha

**Transiciones Posibles**:
- → `EN_DESCANSO`: Si requiere período de recuperación ✅ RECOMENDADO
- → `EN_PREPARACION`: Para nuevo ciclo inmediato
- → `DISPONIBLE`: Si no requiere descanso específico
- → `ABANDONADO`: Si se decide no usar temporalmente

**💡 RECOMENDACIÓN**: Evaluar si el suelo necesita descanso según el historial y estado.

---

### ⚪ **11. EN_DESCANSO** (Período de Recuperación)
**Descripción**: Lote en período de descanso programado

**Características**:
- Recuperación de nutrientes del suelo
- Sin actividad productiva
- Puede tener cultivo de cobertura

**Labores Disponibles**:
- 👁️ **Monitoreo**: Seguimiento del período de descanso

**Transiciones Posibles**:
- → `EN_PREPARACION`: Al finalizar el descanso ✅ NORMAL
- → `DISPONIBLE`: Si se acorta el período de descanso
- → `ABANDONADO`: Si se extiende indefinidamente

**⏱️ DURACIÓN TÍPICA**: 30-90 días según el cultivo anterior y plan de rotación

---

### 🔴 **12. ENFERMO** (Problemas de Salud del Cultivo)
**Descripción**: Cultivo con problemas de plagas o enfermedades

**Características**:
- Presencia de plagas, hongos o enfermedades
- Requiere intervención urgente
- Riesgo de pérdida total o parcial

**Labores Disponibles**:
- 💨 **Pulverización**: Aplicación intensiva de tratamientos
- 🧪 **Aplicación de herbicida**: Control de malezas invasivas
- 🐛 **Aplicación de insecticida**: Control de plagas
- 👁️ **Monitoreo**: Seguimiento constante
- 📝 **Otro**: Tratamientos especiales

**Transiciones Posibles**:
- → `EN_CRECIMIENTO`: Si se recupera el cultivo ✅ IDEAL
- → `EN_FLORACION`: Si se recupera en estado avanzado
- → `EN_FRUTIFICACION`: Si se recupera cerca de cosecha
- → `COSECHADO`: Cosecha anticipada para minimizar pérdidas
- → `ABANDONADO`: Si no es recuperable

**🚨 ACCIÓN REQUERIDA**: Evaluación urgente y plan de tratamiento

---

### ⚫ **13. ABANDONADO** (Lote Temporalmente Fuera de Uso)
**Descripción**: Lote abandonado temporalmente

**Características**:
- Sin uso productivo
- Puede tener malezas
- Requiere decisión sobre reactivación

**Labores Disponibles**:
- 👁️ **Monitoreo**: Inspección periódica
- 📝 **Otro**: Acciones de mantenimiento mínimo

**Transiciones Posibles**:
- → `EN_PREPARACION`: Si se decide reactivar ✅ REHABILITACIÓN
- → `DISPONIBLE`: Limpieza básica
- → `ENFERMO`: Si se detectan problemas que tratar

**⚠️ IMPORTANTE**: Evaluar las razones del abandono antes de reactivar

---

## 🎯 Resumen de Acciones Principales por Categoría

### 🌱 **SIEMBRA**
**Desde**: `PREPARADO` únicamente
**Resultado**: → `SEMBRADO`

### 🌾 **COSECHA**
**Desde**: `SEMBRADO`, `EN_CRECIMIENTO`, `EN_FLORACION`, `EN_FRUTIFICACION`, `LISTO_PARA_COSECHA`
**Resultado**: → `COSECHADO`

### 🔄 **CAMBIO DE ESTADO NATURAL** (Sin acción del usuario)
- `SEMBRADO` → `EN_CRECIMIENTO` (germinación completa)
- `EN_CRECIMIENTO` → `EN_FLORACION` (inicio floración)
- `EN_FLORACION` → `EN_FRUTIFICACION` (formación de frutos)
- `EN_FRUTIFICACION` → `LISTO_PARA_COSECHA` (madurez)

---

## 📊 Matriz de Labores por Estado

| Estado | Arado | Rastra | Siembra | Riego | Fertilización | Pulverización | Desmalezado | Herbicida | Insecticida | Cosecha | Monitoreo |
|--------|-------|--------|---------|-------|---------------|---------------|-------------|-----------|-------------|---------|-----------|
| **DISPONIBLE** | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **EN_PREPARACION** | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **PREPARADO** | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **SEMBRADO** | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ⚠️ | ✅ |
| **EN_CRECIMIENTO** | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ⚠️ | ✅ |
| **EN_FLORACION** | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | ⚠️ | ✅ |
| **EN_FRUTIFICACION** | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ❌ | ❌ | ✅ | ⚠️ | ✅ |
| **LISTO_PARA_COSECHA** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **EN_COSECHA** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ |
| **COSECHADO** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **EN_DESCANSO** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **ENFERMO** | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ | ❌ | ✅ | ✅ | ❌ | ✅ |
| **ABANDONADO** | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ✅ |

**Leyenda**: 
- ✅ Labor recomendada/permitida
- ⚠️ Cosecha anticipada (no ideal, situaciones especiales)
- ❌ Labor no permitida

---

## 🔄 Flujos Típicos de Trabajo

### **Ciclo Normal Exitoso**:
```
DISPONIBLE → EN_PREPARACION → PREPARADO → SIEMBRA → 
SEMBRADO → EN_CRECIMIENTO → EN_FLORACION → 
EN_FRUTIFICACION → LISTO_PARA_COSECHA → COSECHA → 
COSECHADO → EN_DESCANSO → EN_PREPARACION → ...
```

### **Ciclo con Cosecha Anticipada**:
```
... → SEMBRADO → EN_CRECIMIENTO → PROBLEMAS → 
COSECHA ANTICIPADA → COSECHADO → EN_DESCANSO → ...
```

### **Recuperación de Cultivo Enfermo**:
```
... → EN_CRECIMIENTO → ENFERMO → Tratamiento → 
EN_CRECIMIENTO → EN_FLORACION → ...
```

### **Ciclo Sin Descanso** (No recomendado):
```
... → COSECHADO → EN_PREPARACION → PREPARADO → 
SIEMBRA → ...
```

---

## ⚠️ Reglas y Restricciones Importantes

### **Regla 1: Solo se puede sembrar desde PREPARADO**
❌ **ERROR**: Intentar sembrar desde DISPONIBLE o COSECHADO
✅ **CORRECTO**: DISPONIBLE → EN_PREPARACION → PREPARADO → SIEMBRA

### **Regla 2: No se puede sembrar un lote con cultivo activo**
Estados con cultivo activo: SEMBRADO, EN_CRECIMIENTO, EN_FLORACION, EN_FRUTIFICACION

### **Regla 3: La cosecha anticipada requiere justificación**
Si se cosecha antes de LISTO_PARA_COSECHA, documentar la razón (plagas, clima, forraje, etc.)

### **Regla 4: El descanso se recomienda según historial**
- Después de cultivos demandantes (maíz, girasol): 60-90 días
- Después de cultivos menos demandantes (soja): 30-60 días
- Considerar rotación de cultivos

### **Regla 5: Estados especiales requieren atención**
- ENFERMO: Plan de tratamiento obligatorio
- ABANDONADO: Evaluación antes de reactivar

---

## 💡 Mejores Prácticas

### **1. Planificación de Rotación**
- Alternar cultivos de diferentes familias
- Respetar períodos de descanso
- Considerar cultivos de cobertura

### **2. Monitoreo Constante**
- Inspecciones regulares en todos los estados
- Detección temprana de problemas
- Registro de observaciones

### **3. Documentación**
- Registrar todas las labores realizadas
- Justificar cambios de estado no estándar
- Mantener historial de cosechas

### **4. Prevención**
- Labores preventivas en estados tempranos
- Tratamientos proactivos vs reactivos
- Mantenimiento del suelo

---

## 📝 Notas de Implementación

### **Cambios Recientes**
- ✅ Cosecha permitida desde SEMBRADO (para casos especiales)
- ✅ Validación de costos en $0 para labores sin recursos
- ✅ Filtrado inteligente de labores según estado del lote
- ✅ Modal de cosecha con información automática del cultivo

### **Próximas Mejoras Sugeridas**
- 🔄 Transiciones automáticas basadas en días desde siembra
- 📊 Alertas cuando un cultivo está listo para cosechar
- 🌡️ Integración con datos climáticos para recomendaciones
- 📈 Análisis de rendimiento por estado y transición

---

**Documento Generado**: 2025-10-01  
**Versión**: 1.0  
**Sistema**: AgroCloud - Gestión Agropecuaria


