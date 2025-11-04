# 📘 Cómo Configurar Dosis al Dar de Alta un Insumo

## 🎯 Objetivo

Ahora puedes **configurar las dosis sugeridas directamente al dar de alta un insumo**, tanto desde el módulo de insumos como desde el módulo de finanzas (egreso de insumo).

## ✨ Funcionalidad Implementada

### **1. Crear Insumo con Dosis Sugeridas**

**Endpoint:** `POST /api/insumos/con-dosis`

**Request Body:**
```json
{
  "nombre": "Herbicida Glifosato 480",
  "descripcion": "Herbicida sistémico para control de malezas",
  "tipo": "HERBICIDA",
  "unidadMedida": "litros",
  "precioUnitario": 1500.00,
  "stockMinimo": 10.0,
  "stockActual": 100.0,
  "proveedor": "Syngenta",
  "fechaVencimiento": "2026-12-31",
  
  "dosisAplicaciones": [
    {
      "tipoAplicacion": "FOLIAR",
      "dosisPorHa": 2.5,
      "unidadMedida": "litros",
      "descripcion": "Aplicación foliar para control de malezas de hoja ancha"
    },
    {
      "tipoAplicacion": "TERRESTRE",
      "dosisPorHa": 3.0,
      "unidadMedida": "litros",
      "descripcion": "Aplicación terrestre con mochila"
    },
    {
      "tipoAplicacion": "AEREA",
      "dosisPorHa": 1.5,
      "unidadMedida": "litros",
      "descripcion": "Aplicación aérea con avión o dron"
    }
  ]
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Insumo creado exitosamente con 3 dosis configuradas",
  "data": {
    "id": 1,
    "nombre": "Herbicida Glifosato 480",
    "tipo": "HERBICIDA",
    "unidadMedida": "litros",
    "precioUnitario": 1500.00,
    "stockActual": 100.0,
    "tieneDosisConfiguradas": true,
    "mensajeSugerencia": "Insumo con 3 dosis configuradas.",
    "dosisAplicaciones": [
      {
        "id": 1,
        "tipoAplicacion": "FOLIAR",
        "dosisPorHa": 2.5,
        "unidadMedida": "litros",
        "descripcion": "Aplicación foliar para control de malezas de hoja ancha"
      },
      {
        "id": 2,
        "tipoAplicacion": "TERRESTRE",
        "dosisPorHa": 3.0,
        "unidadMedida": "litros",
        "descripcion": "Aplicación terrestre con mochila"
      },
      {
        "id": 3,
        "tipoAplicacion": "AEREA",
        "dosisPorHa": 1.5,
        "unidadMedida": "litros",
        "descripcion": "Aplicación aérea con avión o dron"
      }
    ]
  }
}
```

---

### **2. Actualizar Insumo con Dosis**

**Endpoint:** `PUT /api/insumos/{id}/con-dosis`

**Request Body:** (igual que crear, pero con el ID del insumo en la URL)

**Respuesta:**
```json
{
  "success": true,
  "message": "Insumo actualizado exitosamente con 3 dosis configuradas",
  "data": {
    "id": 1,
    "nombre": "Herbicida Glifosato 480",
    "tipo": "HERBICIDA",
    "tieneDosisConfiguradas": true,
    "mensajeSugerencia": "Insumo con 3 dosis configuradas.",
    "dosisAplicaciones": [...]
  }
}
```

---

### **3. Obtener Insumo con sus Dosis**

**Endpoint:** `GET /api/insumos/{id}/con-dosis`

**Respuesta:**
```json
{
  "id": 1,
  "nombre": "Herbicida Glifosato 480",
  "tipo": "HERBICIDA",
  "unidadMedida": "litros",
  "precioUnitario": 1500.00,
  "stockActual": 100.0,
  "tieneDosisConfiguradas": true,
  "mensajeSugerencia": "Insumo con 3 dosis configuradas.",
  "dosisAplicaciones": [
    {
      "id": 1,
      "tipoAplicacion": "FOLIAR",
      "dosisPorHa": 2.5,
      "unidadMedida": "litros",
      "descripcion": "Aplicación foliar para control de malezas"
    },
    {
      "id": 2,
      "tipoAplicacion": "TERRESTRE",
      "dosisPorHa": 3.0,
      "unidadMedida": "litros",
      "descripcion": "Aplicación terrestre con mochila"
    },
    {
      "id": 3,
      "tipoAplicacion": "AEREA",
      "dosisPorHa": 1.5,
      "unidadMedida": "litros",
      "descripcion": "Aplicación aérea con avión o dron"
    }
  ]
}
```

**Si NO tiene dosis configuradas:**
```json
{
  "id": 1,
  "nombre": "Herbicida Glifosato 480",
  "tipo": "HERBICIDA",
  "unidadMedida": "litros",
  "stockActual": 100.0,
  "tieneDosisConfiguradas": false,
  "mensajeSugerencia": "Este insumo no tiene dosis configuradas. Se recomienda configurar dosis sugeridas para facilitar las aplicaciones.",
  "dosisAplicaciones": []
}
```

---

### **4. Verificar si un Insumo Tiene Dosis**

**Endpoint:** `GET /api/insumos/{id}/tiene-dosis`

**Respuesta:**
```json
{
  "tieneDosis": true,
  "mensaje": "Este insumo tiene dosis configuradas"
}
```

O si no tiene:
```json
{
  "tieneDosis": false,
  "mensaje": "Este insumo NO tiene dosis configuradas. Se recomienda configurarlas."
}
```

---

## 🔄 Flujo de Trabajo

### **Escenario 1: Alta de Insumo desde Módulo de Insumos**

1. Usuario va a "Insumos" → "Nuevo Insumo"
2. Completa los datos básicos del insumo
3. **NUEVO:** Sección "Dosis de Aplicación Sugeridas"
   - Selecciona tipo de aplicación (FOLIAR, TERRESTRE, AEREA, PRECISION)
   - Ingresa dosis por hectárea
   - Opcionalmente agrega descripción
   - Puede agregar múltiples dosis (una por cada tipo)
4. Guarda el insumo
5. El sistema crea el insumo Y las dosis configuradas

### **Escenario 2: Alta de Insumo desde Módulo de Finanzas (Egreso)**

1. Usuario va a "Finanzas" → "Nuevo Egreso"
2. Selecciona tipo: "Insumo"
3. **NUEVO:** Si el insumo no existe, puede crearlo con dosis
4. Completa datos del insumo
5. **NUEVO:** Configura dosis sugeridas (opcional)
6. Guarda el egreso y el insumo con dosis

### **Escenario 3: Editar Insumo Existente sin Dosis**

1. Usuario va a "Insumos" → Selecciona un insumo
2. **NUEVO:** El sistema muestra: "Este insumo no tiene dosis configuradas"
3. Usuario hace clic en "Configurar Dosis"
4. Agrega las dosis sugeridas
5. Guarda
6. Ahora el insumo tiene dosis configuradas

---

## 🎨 Tipos de Aplicación Disponibles

- **FOLIAR**: Aplicación foliar - pulverización sobre las hojas
- **TERRESTRE**: Aplicación terrestre - aplicación al suelo con mochila o tractor
- **AEREA**: Aplicación aérea - pulverización desde avión o dron
- **PRECISION**: Aplicación de precisión - aplicación dirigida con GPS

---

## 💡 Ventajas

1. **Configuración Centralizada**: Las dosis se configuran una sola vez al dar de alta el insumo
2. **Sugerencias Automáticas**: Al crear una aplicación, el sistema sugiere automáticamente la dosis configurada
3. **Flexibilidad**: Puedes configurar diferentes dosis para diferentes tipos de aplicación
4. **Trazabilidad**: Sabes exactamente qué dosis se recomendó para cada insumo
5. **Reducción de Errores**: No hay que calcular manualmente cada vez

---

## 📊 Ejemplo Completo

### **Crear un Fungicida con Dosis**

```json
POST /api/insumos/con-dosis
{
  "nombre": "Fungicida Cobre 50%",
  "descripcion": "Fungicida preventivo a base de cobre",
  "tipo": "FUNGICIDA",
  "unidadMedida": "kg",
  "precioUnitario": 2500.00,
  "stockMinimo": 5.0,
  "stockActual": 50.0,
  "proveedor": "Bayer",
  "fechaVencimiento": "2027-06-30",
  
  "dosisAplicaciones": [
    {
      "tipoAplicacion": "FOLIAR",
      "dosisPorHa": 2.0,
      "unidadMedida": "kg",
      "descripcion": "Aplicación preventiva en cultivos de soja"
    },
    {
      "tipoAplicacion": "TERRESTRE",
      "dosisPorHa": 2.5,
      "unidadMedida": "kg",
      "descripcion": "Aplicación con tractor en cultivos de maíz"
    }
  ]
}
```

### **Luego, al crear una aplicación:**

```json
POST /api/v1/aplicaciones-agroquimicas
{
  "laborId": 1,
  "insumoId": 1,  ← El fungicida recién creado
  "tipoAplicacion": "FOLIAR"
  ← NO necesitas especificar la cantidad!
}
```

**El sistema automáticamente:**
1. ✅ Detecta que el lote tiene 3 hectáreas
2. ✅ Obtiene la dosis sugerida (2.0 kg/ha)
3. ✅ Calcula: 3 ha × 2.0 kg/ha = 6 kg totales
4. ✅ Valida que tengas stock suficiente
5. ✅ Descuenta 6 kg del stock
6. ✅ Guarda la aplicación con trazabilidad completa

---

## 🔍 Endpoints Disponibles

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/insumos/con-dosis` | POST | Crear insumo con dosis |
| `/api/insumos/{id}/con-dosis` | PUT | Actualizar insumo con dosis |
| `/api/insumos/{id}/con-dosis` | GET | Obtener insumo con dosis |
| `/api/insumos/{id}/tiene-dosis` | GET | Verificar si tiene dosis |

---

## 📝 Notas Importantes

1. **Las dosis son opcionales**: Puedes crear un insumo sin dosis y configurarlas después
2. **Múltiples dosis**: Un insumo puede tener una dosis para cada tipo de aplicación
3. **Actualización**: Al actualizar un insumo con dosis, las dosis antiguas se marcan como inactivas y se crean las nuevas
4. **Sugerencias**: Si no hay dosis configurada, el sistema sugiere valores por defecto según el tipo de aplicación
5. **Validación**: El sistema valida que las dosis sean valores positivos

---

## 🚀 Próximos Pasos

1. **Frontend**: Implementar formularios para usar estos endpoints
2. **Validación**: Agregar validaciones en el frontend para dosis sugeridas
3. **Reportes**: Agregar reportes de insumos con/sin dosis configuradas
4. **Notificaciones**: Alertar cuando un insumo no tiene dosis configuradas

---

¿Quieres que te ayude a implementar el frontend para usar estos endpoints?











