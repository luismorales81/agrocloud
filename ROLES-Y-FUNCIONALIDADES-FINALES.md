# 📋 Roles y Funcionalidades del Sistema AgroGestion

## 🎯 Nueva Estructura de Roles (Versión 2.0)

**Última actualización:** 2025-10-08  
**Total de roles:** 6 roles diferenciados por área de responsabilidad

---

## 👨‍💼 **1. ADMINISTRADOR** (Nivel 4)

### **¿Quién es?**
Gerente general, dueño de la empresa, director de operaciones.

### **Responsabilidad:**
Gestión completa y estratégica de la empresa agrícola.

### **Puede hacer:**

#### **👥 Gestión de Usuarios:**
- ✅ Crear nuevos usuarios
- ✅ Editar usuarios existentes
- ✅ Activar/desactivar usuarios
- ✅ Asignar roles a usuarios
- ✅ Resetear contraseñas

#### **🏢 Gestión de Empresa:**
- ✅ Configurar datos de la empresa
- ✅ Ver dashboard general de la empresa
- ✅ Gestionar configuraciones

#### **🗺️ Campos:**
- ✅ Crear campos
- ✅ Editar campos
- ✅ Eliminar campos
- ✅ Ver todos los campos de la empresa

#### **📦 Lotes:**
- ✅ Crear lotes
- ✅ Editar lotes
- ✅ Eliminar lotes
- ✅ Sembrar lotes
- ✅ Cosechar lotes
- ✅ Cambiar estado de lotes

#### **🌱 Cultivos:**
- ✅ Crear cultivos
- ✅ Editar cultivos
- ✅ Eliminar cultivos

#### **🧪 Insumos:**
- ✅ Crear insumos
- ✅ Editar insumos
- ✅ Eliminar insumos
- ✅ Gestionar inventario

#### **🚜 Maquinaria:**
- ✅ Crear maquinaria
- ✅ Editar maquinaria
- ✅ Eliminar maquinaria
- ✅ Asignar a labores

#### **👷 Labores:**
- ✅ Crear labores
- ✅ Editar labores
- ✅ Eliminar labores
- ✅ Asignar recursos (insumos, maquinaria, mano de obra)

#### **💰 Finanzas:**
- ✅ Ver ingresos y egresos
- ✅ Crear ingresos
- ✅ Crear egresos
- ✅ Editar movimientos
- ✅ Eliminar movimientos
- ✅ Ver balance financiero

#### **📊 Reportes:**
- ✅ Ver reportes operativos
- ✅ Ver reportes financieros
- ✅ Exportar reportes

---

## 👨‍🌾 **2. JEFE DE CAMPO** (Nivel 3)

### **¿Quién es?**
Productor agropecuario, ingeniero agrónomo, técnico agrícola, responsable de campo.

**Nota:** Este rol fusiona PRODUCTOR + ASESOR + TÉCNICO (que tenían funciones muy similares).

### **Responsabilidad:**
Gestión completa de operaciones y producción agrícola.

### **Puede hacer:**

#### **🗺️ Campos:**
- ✅ Crear campos
- ✅ Editar campos
- ✅ Eliminar campos
- ✅ Ver información detallada

#### **📦 Lotes:**
- ✅ Crear lotes
- ✅ Editar lotes
- ✅ Eliminar lotes
- ✅ Sembrar lotes
- ✅ Cosechar lotes
- ✅ Cambiar estado de lotes
- ✅ Ver historial de cosechas

#### **🌱 Cultivos:**
- ✅ Crear cultivos
- ✅ Editar cultivos
- ✅ Eliminar cultivos

#### **🧪 Insumos:**
- ✅ Crear insumos
- ✅ Editar insumos
- ✅ Eliminar insumos
- ✅ Gestionar inventario
- ✅ Asignar a labores

#### **🚜 Maquinaria:**
- ✅ Crear maquinaria
- ✅ Editar maquinaria
- ✅ Eliminar maquinaria
- ✅ Asignar a labores

#### **👷 Labores:**
- ✅ Crear labores (siembra, fertilización, riego, etc.)
- ✅ Editar labores
- ✅ Eliminar labores
- ✅ Asignar recursos
- ✅ Registrar ejecución

#### **📊 Reportes:**
- ✅ Ver reportes operativos
- ✅ Ver reportes de producción
- ✅ Exportar reportes operativos

### **NO puede hacer:**
- ❌ Ver finanzas (ingresos, egresos, balance)
- ❌ Crear o editar movimientos financieros
- ❌ Ver reportes financieros
- ❌ Gestionar usuarios

---

## 💰 **3. JEFE FINANCIERO** (Nivel 2)

### **¿Quién es?**
Contador, administrador financiero, responsable de finanzas.

**Nota:** Reemplaza al rol CONTADOR con funcionalidades más claras.

### **Responsabilidad:**
Gestión financiera y contable de todas las operaciones.

### **Puede hacer:**

#### **💰 Finanzas:**
- ✅ Ver todos los ingresos
- ✅ Ver todos los egresos
- ✅ Crear nuevos ingresos
- ✅ Crear nuevos egresos
- ✅ Editar movimientos financieros
- ✅ Eliminar movimientos financieros
- ✅ Ver balance general
- ✅ Categorizar movimientos

#### **📊 Reportes:**
- ✅ Ver reportes financieros
- ✅ Ver reportes de rentabilidad
- ✅ Exportar reportes financieros
- ✅ Análisis de costos por lote
- ✅ Análisis de rentabilidad por cultivo

#### **👁️ Consulta de Operaciones (Solo Lectura):**
- 👁️ Ver campos (para contexto)
- 👁️ Ver lotes (para análisis de costos)
- 👁️ Ver cultivos
- 👁️ Ver cosechas
- 👁️ Ver labores (para verificar costos)
- 👁️ Ver insumos y maquinaria (para valorización)

### **NO puede hacer:**
- ❌ Crear o editar campos
- ❌ Crear o editar lotes
- ❌ Crear o editar cultivos
- ❌ Crear o editar labores
- ❌ Gestionar inventario de insumos
- ❌ Asignar maquinaria
- ❌ Gestionar usuarios

---

## 👷‍♂️ **4. OPERARIO** (Nivel 1)

### **¿Quién es?**
Trabajador de campo, operador de maquinaria, personal operativo.

### **Responsabilidad:**
Ejecución y registro de labores asignadas.

### **Puede hacer:**

#### **👷 Labores:**
- ✅ Crear registros de labores ejecutadas
- ✅ Editar labores propias
- ✅ Registrar fecha de ejecución
- ✅ Registrar recursos utilizados
- ✅ Agregar observaciones

#### **🌽 Cosechas:**
- ✅ Registrar cosechas realizadas (desde Lotes)
- ✅ Registrar cantidad cosechada
- ✅ Agregar observaciones de campo

#### **👁️ Consulta (Solo Lectura):**
- 👁️ Ver campos asignados
- 👁️ Ver lotes donde trabaja
- 👁️ Ver cultivos
- 👁️ Ver insumos disponibles
- 👁️ Ver maquinaria asignada
- 👁️ Ver labores planificadas

#### **📊 Reportes:**
- 👁️ Ver reportes de trabajo
- 👁️ Ver sus labores ejecutadas

### **NO puede hacer:**
- ❌ Crear campos, lotes o cultivos
- ❌ Planificar labores (solo ejecutar)
- ❌ Gestionar inventario de insumos
- ❌ Asignar maquinaria
- ❌ Ver finanzas
- ❌ Ver reportes financieros
- ❌ Gestionar usuarios

---

## 👁️ **5. CONSULTOR EXTERNO** (Nivel 0)

### **¿Quién es?**
Asesor externo, auditor, consultor, invitado.

**Nota:** Reemplaza al rol LECTURA con un nombre más descriptivo.

### **Responsabilidad:**
Visualización y análisis de datos sin capacidad de modificación.

### **Puede hacer:**

#### **👁️ Consulta Completa (Solo Lectura):**
- 👁️ Ver todos los campos
- 👁️ Ver todos los lotes
- 👁️ Ver todos los cultivos
- 👁️ Ver cosechas registradas
- 👁️ Ver insumos
- 👁️ Ver maquinaria
- 👁️ Ver labores ejecutadas

#### **📊 Reportes:**
- ✅ Ver reportes operativos
- ✅ Exportar reportes operativos
- ✅ Ver análisis de rendimientos

### **NO puede hacer:**
- ❌ Crear, editar o eliminar NADA
- ❌ Ver finanzas (ingresos, egresos, balance)
- ❌ Ver reportes financieros
- ❌ Gestionar usuarios
- ❌ Exportar reportes financieros

---

## 👑 **6. SUPERADMIN** (Nivel 5)

### **¿Quién es?**
Administrador global del sistema (desarrollador, soporte técnico).

### **Responsabilidad:**
Administración global del sistema multi-empresa.

### **Puede hacer:**

#### **🏢 Gestión Global:**
- ✅ Ver todas las empresas del sistema
- ✅ Crear nuevas empresas
- ✅ Editar empresas
- ✅ Activar/desactivar empresas

#### **👥 Usuarios Globales:**
- ✅ Ver todos los usuarios del sistema
- ✅ Gestionar usuarios de cualquier empresa

#### **📊 Dashboard Global:**
- ✅ Ver estadísticas del sistema completo
- ✅ Monitorear todas las empresas

### **NO puede hacer:**
- ❌ Ver operaciones de campos (es administrador de sistema, no de operaciones)
- ❌ Crear lotes, labores, cosechas
- ❌ Ver finanzas de empresas específicas
- ❌ Gestionar inventarios

**Nota:** SUPERADMIN gestiona el sistema, no las operaciones agrícolas.

---

## 📊 **MATRIZ COMPARATIVA DE PERMISOS**

| **Funcionalidad** | **ADMIN** | **JEFE CAMPO** | **JEFE FINANCIERO** | **OPERARIO** | **CONSULTOR** | **SUPERADMIN** |
|-------------------|-----------|----------------|---------------------|--------------|---------------|----------------|
| **Gestión Usuarios** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ (global) |
| **Gestión Empresas** | ✅ | ❌ | ❌ | ❌ | ❌ | ✅ |
| **Crear Campos** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Crear Lotes** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Crear Cultivos** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Gestionar Insumos** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Gestionar Maquinaria** | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Crear Labores** | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Registrar Cosechas** | ✅ | ✅ | ❌ | ✅ | ❌ | ❌ |
| **Ver Finanzas** | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Gestionar Finanzas** | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |
| **Ver Reportes Operativos** | ✅ | ✅ | 👁️ | 👁️ | ✅ | ❌ |
| **Ver Reportes Financieros** | ✅ | ❌ | ✅ | ❌ | ❌ | ❌ |

**Leyenda:**
- ✅ = Puede crear, editar, eliminar
- 👁️ = Solo puede ver (lectura)
- ❌ = Sin acceso

---

## 🔍 **DIFERENCIAS CLAVE ENTRE ROLES**

### **ADMINISTRADOR vs JEFE DE CAMPO:**
| Aspecto | ADMINISTRADOR | JEFE DE CAMPO |
|---------|---------------|---------------|
| Usuarios | ✅ Gestiona | ❌ No puede |
| Campos y Lotes | ✅ Gestiona | ✅ Gestiona |
| Finanzas | ✅ Acceso total | ❌ Sin acceso |
| Reportes Financieros | ✅ Ve | ❌ No ve |

**Conclusión:** JEFE DE CAMPO maneja operaciones, ADMINISTRADOR maneja operaciones + finanzas + usuarios.

---

### **JEFE DE CAMPO vs JEFE FINANCIERO:**
| Aspecto | JEFE DE CAMPO | JEFE FINANCIERO |
|---------|---------------|-----------------|
| Crear Campos/Lotes | ✅ Puede | ❌ No puede |
| Gestionar Insumos | ✅ Puede | 👁️ Solo ve |
| Crear Labores | ✅ Puede | 👁️ Solo ve |
| Gestionar Finanzas | ❌ No puede | ✅ Puede |
| Ver Costos | 👁️ Solo ve | ✅ Gestiona |

**Conclusión:** Roles **completamente diferentes** - uno maneja campo, otro maneja dinero.

---

### **JEFE DE CAMPO vs OPERARIO:**
| Aspecto | JEFE DE CAMPO | OPERARIO |
|---------|---------------|----------|
| Crear Campos | ✅ Puede | ❌ No puede |
| Planificar Labores | ✅ Puede | ❌ No puede |
| Ejecutar Labores | ✅ Puede | ✅ Puede |
| Gestionar Insumos | ✅ Puede | 👁️ Solo ve |
| Ver Finanzas | ❌ No puede | ❌ No puede |

**Conclusión:** JEFE DE CAMPO planifica y ejecuta, OPERARIO solo ejecuta.

---

## 🎯 **CASOS DE USO REALES**

### **Caso 1: Empresa Familiar Pequeña**

**Estructura:**
```
👨‍💼 Pedro (Dueño) → ADMINISTRADOR
   - Gestiona todo: usuarios, campos, finanzas

👨‍🌾 Juan (Hijo/Encargado) → JEFE DE CAMPO
   - Maneja las operaciones diarias
   - NO ve las finanzas

👷‍♂️ Luis, Carlos (Empleados) → OPERARIO
   - Registran el trabajo que hacen
```

---

### **Caso 2: Empresa Agropecuaria Mediana**

**Estructura:**
```
👨‍💼 María (Gerente) → ADMINISTRADOR
   - Supervisión general

👨‍🌾 Carlos (Ingeniero Agrónomo) → JEFE DE CAMPO
   - 500 hectáreas bajo su responsabilidad
   - Planifica siembras, rotaciones, labores
   - NO ve cuánto cuesta cada cosa

💰 Ana (Contadora) → JEFE FINANCIERO
   - Gestiona todo lo económico
   - Ve costos de operaciones
   - NO puede crear lotes ni labores

👷‍♂️ 5 Operarios → OPERARIO
   - Registran labores ejecutadas
   - Registran cosechas
```

---

### **Caso 3: Empresa Grande con Auditoría**

**Estructura:**
```
👨‍💼 Administrador Principal → ADMINISTRADOR

👨‍🌾 3 Jefes de Campo → JEFE DE CAMPO
   - Cada uno maneja diferentes secciones
   - 1000+ hectáreas total

💰 Departamento Contable → JEFE FINANCIERO
   - 2 personas gestionando finanzas

👷‍♂️ 15 Operarios → OPERARIO

👁️ Auditor Externo → CONSULTOR EXTERNO
   - Ve todo para hacer auditoría
   - NO puede modificar nada
```

---

## 📝 **MENÚ DISPONIBLE POR ROL**

### **👨‍💼 ADMINISTRADOR ve:**
```
📊 Dashboard
🌱 Producción
   🌾 Campos
   🔲 Lotes (con siembra y cosecha)
   🌱 Cultivos
   ⚒️ Labores
📦 Recursos & Stock
   🧪 Insumos
   🚜 Maquinaria
   🌾 Inventario Granos
💰 Finanzas
   💵 Ingresos
   💸 Egresos
   💰 Balance
📊 Reportes
👥 Administración
   👤 Usuarios
   🏢 Empresas (solo SUPERADMIN)
```

---

### **👨‍🌾 JEFE DE CAMPO ve:**
```
📊 Dashboard
🌱 Producción
   🌾 Campos
   🔲 Lotes (con siembra y cosecha)
   🌱 Cultivos
   ⚒️ Labores
📦 Recursos & Stock
   🧪 Insumos
   🚜 Maquinaria
   🌾 Inventario Granos
📊 Reportes (solo operativos)
```

**NO VE:** Finanzas, Administración de usuarios

---

### **💰 JEFE FINANCIERO ve:**
```
📊 Dashboard
🌱 Producción (solo lectura)
   🌾 Campos (solo ver)
   🔲 Lotes (solo ver)
   🌱 Cultivos (solo ver)
   ⚒️ Labores (solo ver)
📦 Recursos & Stock (solo lectura)
   🧪 Insumos (solo ver)
   🚜 Maquinaria (solo ver)
💰 Finanzas
   💵 Ingresos
   💸 Egresos
   💰 Balance
📊 Reportes Financieros
```

**NO VE:** Administración de usuarios  
**NO PUEDE:** Crear campos, lotes, labores

---

### **👷‍♂️ OPERARIO ve:**
```
📊 Dashboard
🌱 Producción (solo lectura excepto labores)
   🌾 Campos (solo ver)
   🔲 Lotes (solo ver)
   🌱 Cultivos (solo ver)
   ⚒️ Labores (puede crear/editar)
📦 Recursos & Stock (solo lectura)
   🧪 Insumos (solo ver disponibles)
   🚜 Maquinaria (solo ver asignadas)
📊 Reportes (básicos)
```

**NO VE:** Finanzas, Administración  
**NO PUEDE:** Crear campos, lotes, cultivos, gestionar inventarios

---

### **👁️ CONSULTOR EXTERNO ve:**
```
📊 Dashboard (solo lectura)
🌱 Producción (todo en solo lectura)
   🌾 Campos
   🔲 Lotes
   🌱 Cultivos
   ⚒️ Labores
📦 Recursos & Stock (solo lectura)
   🧪 Insumos
   🚜 Maquinaria
📊 Reportes Operativos
```

**NO VE:** Finanzas, Administración  
**NO PUEDE:** Modificar absolutamente nada

---

## 🔄 **MAPEO DE ROLES ANTIGUOS**

Si ya tenías usuarios con roles antiguos, se mapearon así:

| Rol Antiguo | Rol Nuevo | Cambio en Permisos |
|-------------|-----------|-------------------|
| **PRODUCTOR** | JEFE_CAMPO | Perdió acceso a finanzas |
| **ASESOR** | JEFE_CAMPO | Ganó permisos de crear (antes solo leía) |
| **TÉCNICO** | JEFE_CAMPO | Ganó permisos de crear campos/lotes |
| **CONTADOR** | JEFE_FINANCIERO | Ganó acceso a ver operaciones (antes solo finanzas) |
| **LECTURA** | CONSULTOR_EXTERNO | Ganó capacidad de exportar reportes |

---

## 🎓 **RECOMENDACIONES DE USO**

### **¿Cuándo usar JEFE DE CAMPO?**
- Empleados de confianza que manejan las operaciones
- Ingenieros agrónomos internos
- Responsables de producción
- **NO necesitan ver finanzas**

### **¿Cuándo usar JEFE FINANCIERO?**
- Contadores
- Administradores financieros
- Personal de finanzas
- **NO necesitan crear campos ni labores**

### **¿Cuándo usar OPERARIO?**
- Trabajadores de campo
- Personal operativo
- Maquinistas
- **Solo registran lo que hacen**

### **¿Cuándo usar CONSULTOR EXTERNO?**
- Asesores externos
- Auditores
- Invitados
- Socios que solo quieren ver
- **No modifican nada**

---

## ✅ **VENTAJAS DE LA NUEVA ESTRUCTURA**

1. ✅ **Separación clara:** Operaciones ≠ Finanzas
2. ✅ **Sin redundancia:** No hay roles con permisos idénticos
3. ✅ **Seguridad:** Cada rol solo ve lo que necesita
4. ✅ **Simplicidad:** Fácil de entender y asignar
5. ✅ **Escalabilidad:** Fácil agregar nuevos permisos

---

**Fecha de implementación:** 2025-10-08  
**Versión del sistema:** 2.0.0  
**Estado:** ✅ Implementado y probado



