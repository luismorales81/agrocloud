# 📊 Tabla de Permisos por Rol - AgroGestion 2.0

## 🎯 Resumen Rápido

| **ROL** | **NIVEL** | **DESCRIPCIÓN** |
|---------|-----------|-----------------|
| 👑 **SUPERADMIN** | 5 | Administrador del sistema (gestión de empresas) |
| 👨‍💼 **ADMINISTRADOR** | 4 | Gerente de empresa (todo: operaciones + finanzas + usuarios) |
| 👨‍🌾 **JEFE_CAMPO** | 3 | Responsable de operaciones agrícolas (SIN finanzas) |
| 💰 **JEFE_FINANCIERO** | 2 | Responsable de finanzas (SIN crear operaciones) |
| 👷‍♂️ **OPERARIO** | 1 | Ejecutor de labores (solo registra trabajo) |
| 👁️ **CONSULTOR_EXTERNO** | 0 | Solo lectura completa (no modifica nada) |

---

## 📋 Permisos Detallados por Módulo

### **👥 GESTIÓN DE USUARIOS**

| ROL | CREAR | EDITAR | ELIMINAR | VER |
|-----|-------|--------|----------|-----|
| SUPERADMIN | ✅ | ✅ | ✅ | ✅ (global) |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ (empresa) |
| JEFE_CAMPO | ❌ | ❌ | ❌ | ❌ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | ❌ |
| OPERARIO | ❌ | ❌ | ❌ | ❌ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ |

---

### **🗺️ CAMPOS**

| ROL | CREAR | EDITAR | ELIMINAR | VER |
|-----|-------|--------|----------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ❌ | ❌ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | 👁️ |

---

### **📦 LOTES**

| ROL | CREAR | EDITAR | ELIMINAR | SEMBRAR | COSECHAR | VER |
|-----|-------|--------|----------|---------|----------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ❌ | ❌ | ❌ | ✅ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ | ❌ | 👁️ |

---

### **🌱 CULTIVOS**

| ROL | CREAR | EDITAR | ELIMINAR | VER |
|-----|-------|--------|----------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ❌ | ❌ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | 👁️ |

---

### **🧪 INSUMOS**

| ROL | CREAR | EDITAR | ELIMINAR | GESTIONAR STOCK | VER |
|-----|-------|--------|----------|-----------------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ❌ | ❌ | ❌ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ | 👁️ |

---

### **🚜 MAQUINARIA**

| ROL | CREAR | EDITAR | ELIMINAR | ASIGNAR | VER |
|-----|-------|--------|----------|---------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ❌ | ❌ | ❌ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ | 👁️ |

---

### **👷 LABORES**

| ROL | PLANIFICAR | EJECUTAR/REGISTRAR | EDITAR | ELIMINAR | VER |
|-----|------------|-------------------|--------|----------|-----|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_FINANCIERO | ❌ | ❌ | ❌ | ❌ | 👁️ |
| OPERARIO | ❌ | ✅ | ✅ | ❌ | 👁️ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ | 👁️ |

---

### **💰 FINANZAS**

| ROL | CREAR INGRESO | CREAR EGRESO | EDITAR | ELIMINAR | VER BALANCE | VER COSTOS |
|-----|---------------|--------------|--------|----------|-------------|------------|
| SUPERADMIN | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| JEFE_FINANCIERO | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| OPERARIO | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| CONSULTOR_EXTERNO | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

### **📊 REPORTES**

| ROL | OPERATIVOS | FINANCIEROS | EXPORTAR |
|-----|------------|-------------|----------|
| SUPERADMIN | ❌ | ❌ | ❌ |
| ADMINISTRADOR | ✅ | ✅ | ✅ |
| JEFE_CAMPO | ✅ | ❌ | ✅ |
| JEFE_FINANCIERO | 👁️ | ✅ | ✅ |
| OPERARIO | 👁️ | ❌ | ❌ |
| CONSULTOR_EXTERNO | ✅ | ❌ | ✅ |

---

## 🔑 LEYENDA COMPLETA

| Símbolo | Significado | Descripción |
|---------|-------------|-------------|
| ✅ | **Acceso Completo** | Puede crear, leer, editar y eliminar |
| 👁️ | **Solo Lectura** | Puede ver pero NO modificar |
| ❌ | **Sin Acceso** | No puede ver ni interactuar |

---

## 📞 AYUDA RÁPIDA

**¿No puedes hacer algo que necesitas?**
1. Verifica tu rol actual en el selector de empresas
2. Revisa esta tabla para ver si tienes permisos
3. Si necesitas más permisos, contacta al ADMINISTRADOR

**¿Un usuario tiene demasiados permisos?**
1. Revisa su rol
2. Si era PRODUCTOR/ASESOR/TÉCNICO → ahora es JEFE_CAMPO (permisos operativos completos)
3. Si necesitas restringir más, cámbialo a OPERARIO

---

**Documento generado:** 2025-10-08  
**Sistema:** AgroGestion 2.0  
**Roles totales:** 6 diferenciados



