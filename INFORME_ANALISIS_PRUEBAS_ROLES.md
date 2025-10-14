# 📊 INFORME DE ANÁLISIS PARA PRUEBAS - ROLES Y DATOS

## 🎯 **OBJETIVO**
Este informe proporciona un análisis detallado de lo que cada usuario y rol debería ver en el sistema, basado en la estructura real de datos y la lógica implementada.

---

## 📋 **ESTRUCTURA DE USUARIOS Y JERARQUÍA**

### **🌳 Jerarquía de Usuarios:**
```
1. admin (SUPERADMIN) - parent_user_id: NULL
   └── Ve TODO el sistema

2. admin_empresa (ADMINISTRADOR) - parent_user_id: NULL
   └── Ve datos de su empresa "AgroCloud Demo"

3. admin.campo (ADMINISTRADOR) - parent_user_id: 2 (admin_empresa)
   └── Ve datos de su empresa "AgroCloud Demo" + sus dependientes

4. tecnico.juan (TECNICO) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

5. asesor.maria (ASESOR) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

6. productor.pedro (PRODUCTOR) - parent_user_id: 5 (admin.campo)
   └── Ve solo sus datos

7. operario.luis (OPERARIO) - parent_user_id: 6 (tecnico.juan)
   └── Ve solo sus datos

8. invitado.ana (INVITADO) - parent_user_id: 2 (admin_empresa)
   └── Ve solo sus datos
```

---

## 📊 **DATOS REALES POR USUARIO**

### **🔍 Datos Individuales (Lo que cada usuario tiene directamente):**

| Usuario | Campos | Lotes | Cultivos | Cosechas | Insumos | Maquinaria | Labores |
|---------|--------|-------|----------|----------|---------|------------|---------|
| **admin** | 0 | 0 | 0 | 4 | 0 | 0 | 4 |
| **admin_empresa** | 5 (4 activos) | 9 (8 activos) | 6 (6 activos) | 1 | 14 (14 activos) | 4 (4 activos) | 3 (1 activo) |
| **admin.campo** | 0 | 0 | 0 | 0 | 0 | 2 (2 activos) | 0 |
| **tecnico.juan** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **asesor.maria** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **productor.pedro** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **operario.luis** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| **invitado.ana** | 0 | 0 | 0 | 0 | 0 | 0 | 0 |

---

## 🎯 **LO QUE DEBERÍA VER CADA USUARIO SEGÚN SU ROL**

### **1. 👑 SUPERADMIN (admin)**
- **Ve**: TODO el sistema (todas las empresas, usuarios, datos)
- **Dashboard**: Estadísticas globales del sistema
- **Datos esperados**:
  - **Campos**: 5 (4 activos) - Solo campos activos de todo el sistema
  - **Lotes**: 9 (8 activos) - Solo lotes activos de todo el sistema
  - **Cultivos**: 6 (6 activos) - Solo cultivos activos de todo el sistema
  - **Cosechas**: 5 (4 de admin + 1 de admin_empresa)
  - **Insumos**: 14 (14 activos) - Solo insumos activos de todo el sistema
  - **Maquinaria**: 6 (4 de admin_empresa + 2 de admin.campo) - Solo activos
  - **Labores**: 7 (4 de admin + 3 de admin_empresa) - Solo activos

### **2. 🏢 ADMINISTRADOR (admin_empresa)**
- **Ve**: Datos de su empresa "AgroCloud Demo" (todos los usuarios de la empresa)
- **Dashboard**: Estadísticas de su empresa
- **Datos esperados**:
  - **Campos**: 5 (4 activos) - Solo campos activos de su empresa
  - **Lotes**: 9 (8 activos) - Solo lotes activos de su empresa
  - **Cultivos**: 6 (6 activos) - Solo cultivos activos de su empresa
  - **Cosechas**: 1 - Solo cosechas de su empresa
  - **Insumos**: 14 (14 activos) - Solo insumos activos de su empresa
  - **Maquinaria**: 6 (4 de admin_empresa + 2 de admin.campo) - Solo activos
  - **Labores**: 3 (1 activo) - Solo labores activas de su empresa

### **3. 🏢 ADMINISTRADOR (admin.campo)**
- **Ve**: Datos de su empresa "AgroCloud Demo" (todos los usuarios de la empresa)
- **Dashboard**: Estadísticas de su empresa
- **Datos esperados**:
  - **Campos**: 5 (4 activos) - Solo campos activos de su empresa
  - **Lotes**: 9 (8 activos) - Solo lotes activos de su empresa
  - **Cultivos**: 6 (6 activos) - Solo cultivos activos de su empresa
  - **Cosechas**: 1 - Solo cosechas de su empresa
  - **Insumos**: 14 (14 activos) - Solo insumos activos de su empresa
  - **Maquinaria**: 6 (4 de admin_empresa + 2 de admin.campo) - Solo activos
  - **Labores**: 3 (1 activo) - Solo labores activas de su empresa

### **4. 🔧 TECNICO (tecnico.juan)**
- **Ve**: Solo sus datos + datos de usuarios dependientes
- **Dashboard**: Estadísticas personales
- **Datos esperados**:
  - **Campos**: 0 - Solo sus campos (no tiene)
  - **Lotes**: 0 - Solo sus lotes (no tiene)
  - **Cultivos**: 0 - Solo sus cultivos (no tiene)
  - **Cosechas**: 0 - Solo sus cosechas (no tiene)
  - **Insumos**: 0 - Solo sus insumos (no tiene)
  - **Maquinaria**: 0 - Solo su maquinaria (no tiene)
  - **Labores**: 0 - Solo sus labores (no tiene)

### **5. 👨‍🌾 ASESOR (asesor.maria)**
- **Ve**: Solo sus datos + datos de usuarios dependientes
- **Dashboard**: Estadísticas personales
- **Datos esperados**:
  - **Campos**: 0 - Solo sus campos (no tiene)
  - **Lotes**: 0 - Solo sus lotes (no tiene)
  - **Cultivos**: 0 - Solo sus cultivos (no tiene)
  - **Cosechas**: 0 - Solo sus cosechas (no tiene)
  - **Insumos**: 0 - Solo sus insumos (no tiene)
  - **Maquinaria**: 0 - Solo su maquinaria (no tiene)
  - **Labores**: 0 - Solo sus labores (no tiene)

### **6. 🌾 PRODUCTOR (productor.pedro)**
- **Ve**: Solo sus datos + datos de usuarios dependientes
- **Dashboard**: Estadísticas personales
- **Datos esperados**:
  - **Campos**: 0 - Solo sus campos (no tiene)
  - **Lotes**: 0 - Solo sus lotes (no tiene)
  - **Cultivos**: 0 - Solo sus cultivos (no tiene)
  - **Cosechas**: 0 - Solo sus cosechas (no tiene)
  - **Insumos**: 0 - Solo sus insumos (no tiene)
  - **Maquinaria**: 0 - Solo su maquinaria (no tiene)
  - **Labores**: 0 - Solo sus labores (no tiene)

### **7. 🚜 OPERARIO (operario.luis)**
- **Ve**: Solo sus datos + datos de usuarios dependientes
- **Dashboard**: Estadísticas personales
- **Datos esperados**:
  - **Campos**: 0 - Solo sus campos (no tiene)
  - **Lotes**: 0 - Solo sus lotes (no tiene)
  - **Cultivos**: 0 - Solo sus cultivos (no tiene)
  - **Cosechas**: 0 - Solo sus cosechas (no tiene)
  - **Insumos**: 0 - Solo sus insumos (no tiene)
  - **Maquinaria**: 0 - Solo su maquinaria (no tiene)
  - **Labores**: 0 - Solo sus labores (no tiene)

### **8. 👋 INVITADO (invitado.ana)**
- **Ve**: Solo sus datos + datos de usuarios dependientes
- **Dashboard**: Estadísticas personales
- **Datos esperados**:
  - **Campos**: 0 - Solo sus campos (no tiene)
  - **Lotes**: 0 - Solo sus lotes (no tiene)
  - **Cultivos**: 0 - Solo sus cultivos (no tiene)
  - **Cosechas**: 0 - Solo sus cosechas (no tiene)
  - **Insumos**: 0 - Solo sus insumos (no tiene)
  - **Maquinaria**: 0 - Solo su maquinaria (no tiene)
  - **Labores**: 0 - Solo sus labores (no tiene)

---

## 🔍 **CASOS DE PRUEBA ESPECÍFICOS**

### **✅ Caso 1: admin_empresa (ADMINISTRADOR)**
- **Login**: admin.empresa@agrocloud.com
- **Dashboard esperado**:
  - Campos: 4 (solo activos)
  - Lotes: 8 (solo activos)
  - Cultivos: 6 (solo activos)
  - Cosechas: 1
  - Insumos: 14 (solo activos)
  - Maquinaria: 6 (solo activos)
  - Labores: 1 (solo activos)

### **✅ Caso 2: admin.campo (ADMINISTRADOR)**
- **Login**: admin.campo@agrocloud.com
- **Dashboard esperado**: Mismos datos que admin_empresa (empresa completa)

### **✅ Caso 3: tecnico.juan (TECNICO)**
- **Login**: tecnico.juan@agrocloud.com
- **Dashboard esperado**: Todos los valores en 0 (no tiene datos)

### **✅ Caso 4: admin (SUPERADMIN)**
- **Login**: admin@agrocloud.com
- **Dashboard esperado**: Datos globales del sistema

---

## 🚨 **PUNTOS CRÍTICOS PARA VERIFICAR**

### **1. Filtrado por Estado Activo**
- ✅ **Campos**: Solo mostrar activos (activo = 1)
- ✅ **Lotes**: Solo mostrar activos (activo = 1)
- ✅ **Cultivos**: Solo mostrar activos (activo = 1)
- ✅ **Insumos**: Solo mostrar activos (activo = 1)
- ✅ **Maquinaria**: Solo mostrar activos (activo = 1)
- ✅ **Labores**: Solo mostrar activos (activo = 1)
- ⚠️ **Cosechas**: No tienen campo activo (mostrar todas)

### **2. Jerarquía de Empresas**
- ✅ **SUPERADMIN**: Ve todo el sistema
- ✅ **ADMINISTRADOR**: Ve solo su empresa
- ✅ **Otros roles**: Ven solo sus datos

### **3. Coincidencia Dashboard vs Lista**
- ✅ **Dashboard**: Debe mostrar la misma cantidad que la lista de elementos
- ✅ **Campos**: 4 activos en dashboard = 4 campos en lista
- ✅ **Lotes**: 8 activos en dashboard = 8 lotes en lista
- ✅ **Cultivos**: 6 activos en dashboard = 6 cultivos en lista

---

## 📝 **CHECKLIST DE PRUEBAS**

### **Para admin_empresa:**
- [ ] Dashboard muestra 4 campos
- [ ] Lista de campos muestra 4 campos
- [ ] Dashboard muestra 8 lotes
- [ ] Lista de lotes muestra 8 lotes
- [ ] Dashboard muestra 6 cultivos
- [ ] Lista de cultivos muestra 6 cultivos
- [ ] Dashboard muestra 1 cosecha
- [ ] Lista de cosechas muestra 1 cosecha
- [ ] Dashboard muestra 14 insumos
- [ ] Lista de insumos muestra 14 insumos
- [ ] Dashboard muestra 6 maquinaria
- [ ] Lista de maquinaria muestra 6 maquinaria
- [ ] Dashboard muestra 1 labor
- [ ] Lista de labores muestra 1 labor

### **Para admin.campo:**
- [ ] Mismos datos que admin_empresa (empresa completa)

### **Para tecnico.juan:**
- [ ] Todos los valores en 0 (no tiene datos)

### **Para admin (SUPERADMIN):**
- [ ] Datos globales del sistema

---

## 🎯 **RESULTADO ESPERADO**

**✅ ÉXITO**: Dashboard y listas muestran exactamente la misma cantidad de elementos activos.

**❌ FALLO**: Dashboard y listas muestran cantidades diferentes.

---

*Este informe se basa en el análisis real de la base de datos y la lógica implementada en el sistema.*
