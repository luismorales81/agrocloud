# 📋 Plan de Pruebas - Migración de Lógica al Backend

## 🎯 Objetivo
Verificar que la migración de lógica crítica del frontend al backend funciona correctamente y no introduce regresiones en el sistema.

---

## ✅ Cambios Implementados

### 1. **Servicio de Rendimiento** (Nuevo)
   - Archivo: `RendimientoService.java`
   - Controller: `RendimientoController.java`
   - Endpoints:
     - `POST /api/v1/rendimiento/calcular`
     - `POST /api/v1/rendimiento/diferencia`

### 2. **Servicio de Validación de Stock de Insumos** (Extendido)
   - Archivo: `InsumoService.java`
   - Controller: `InsumoController.java`
   - Endpoints:
     - `POST /api/insumos/{id}/validar-stock`
     - `POST /api/insumos/{id}/stock-disponible`

### 3. **Servicio de Cálculo de Costos de Labores** (Extendido)
   - Archivo: `LaborService.java`
   - Controller: `LaborController.java`
   - Endpoints:
     - `GET /api/labores/{id}/calcular-costo`
     - `GET /api/labores/{id}/desglose-costos`
     - `POST /api/labores/{id}/actualizar-costo`

---

## 📝 Plan de Pruebas

### **Fase 1: Pruebas de Backend (API)**

#### ✅ **Test 1.1: Cálculo de Rendimiento Real**

**Endpoint:** `POST /api/v1/rendimiento/calcular`

**Casos de Prueba:**

1. **Caso 1.1.1: Conversión kg a kg/ha**
   ```json
   POST /api/v1/rendimiento/calcular
   {
     "cantidad": 45000,
     "unidadCantidad": "kg",
     "superficie": 10,
     "unidadCultivo": "kg/ha"
   }
   ```
   **Resultado Esperado:**
   - `rendimiento: 4500`
   - `success: true`

2. **Caso 1.1.2: Conversión toneladas a qq/ha**
   ```json
   POST /api/v1/rendimiento/calcular
   {
     "cantidad": 50,
     "unidadCantidad": "tn",
     "superficie": 10,
     "unidadCultivo": "qq/ha"
   }
   ```
   **Resultado Esperado:**
   - `rendimiento: 108.70` (50tn = 50,000kg / 10ha = 5,000kg/ha = 108.70 qq/ha)
   - `success: true`

3. **Caso 1.1.3: Superficie cero o negativa**
   ```json
   POST /api/v1/rendimiento/calcular
   {
     "cantidad": 5000,
     "unidadCantidad": "kg",
     "superficie": 0,
     "unidadCultivo": "kg/ha"
   }
   ```
   **Resultado Esperado:**
   - `rendimiento: 0`
   - `success: true`

4. **Caso 1.1.4: Cantidad negativa**
   ```json
   POST /api/v1/rendimiento/calcular
   {
     "cantidad": -1000,
     "unidadCantidad": "kg",
     "superficie": 10,
     "unidadCultivo": "kg/ha"
   }
   ```
   **Resultado Esperado:**
   - `rendimiento: 0`
   - `success: true`

---

#### ✅ **Test 1.2: Diferencia Porcentual de Rendimiento**

**Endpoint:** `POST /api/v1/rendimiento/diferencia`

**Casos de Prueba:**

1. **Caso 1.2.1: Rendimiento supera expectativa (+10%)**
   ```json
   POST /api/v1/rendimiento/diferencia
   {
     "rendimientoEsperado": 4500,
     "rendimientoReal": 4950
   }
   ```
   **Resultado Esperado:**
   - `diferenciaPorcentual: 10.00`
   - `superaExpectativa: true`
   - `cumpleExpectativa: true`

2. **Caso 1.2.2: Rendimiento no alcanza expectativa (-15%)**
   ```json
   POST /api/v1/rendimiento/diferencia
   {
     "rendimientoEsperado": 4500,
     "rendimientoReal": 3825
   }
   ```
   **Resultado Esperado:**
   - `diferenciaPorcentual: -15.00`
   - `superaExpectativa: false`
   - `cumpleExpectativa: false`

3. **Caso 1.2.3: Rendimiento esperado es cero**
   ```json
   POST /api/v1/rendimiento/diferencia
   {
     "rendimientoEsperado": 0,
     "rendimientoReal": 4500
   }
   ```
   **Resultado Esperado:**
   - `diferenciaPorcentual: 0`

---

#### ✅ **Test 1.3: Validación de Stock de Insumos**

**Endpoint:** `POST /api/insumos/{id}/validar-stock`

**Casos de Prueba:**

1. **Caso 1.3.1: Stock suficiente**
   ```json
   POST /api/insumos/1/validar-stock
   {
     "cantidad": 50
   }
   ```
   **Resultado Esperado:**
   - `valido: true`
   - `mensaje: "Stock disponible"`
   - `stockDisponible: 100` (o el valor actual)

2. **Caso 1.3.2: Stock insuficiente**
   ```json
   POST /api/insumos/1/validar-stock
   {
     "cantidad": 150
   }
   ```
   **Resultado Esperado:**
   - `valido: false`
   - `mensaje: "Stock insuficiente. Disponible: 100 kg, Requerido: 150 kg"`

3. **Caso 1.3.3: Insumo no existe**
   ```json
   POST /api/insumos/99999/validar-stock
   {
     "cantidad": 50
   }
   ```
   **Resultado Esperado:**
   - `valido: false`
   - `mensaje: "Insumo no encontrado"`

---

#### ✅ **Test 1.4: Cálculo de Stock Real Disponible**

**Endpoint:** `POST /api/insumos/{id}/stock-disponible`

**Casos de Prueba:**

1. **Caso 1.4.1: Sin cantidades ya asignadas**
   ```json
   POST /api/insumos/1/stock-disponible
   {}
   ```
   **Resultado Esperado:**
   - `success: true`
   - `stockDisponible: 100` (stock actual del insumo)

2. **Caso 1.4.2: Con cantidades ya asignadas**
   ```json
   POST /api/insumos/1/stock-disponible
   {
     "cantidadesYaAsignadas": 30
   }
   ```
   **Resultado Esperado:**
   - `success: true`
   - `stockDisponible: 70` (100 - 30)

---

#### ✅ **Test 1.5: Cálculo de Costo Total de Labor**

**Endpoint:** `GET /api/labores/{id}/calcular-costo`

**Casos de Prueba:**

1. **Caso 1.5.1: Labor con todos los componentes de costo**
   ```
   GET /api/labores/1/calcular-costo
   ```
   **Resultado Esperado:**
   - `success: true`
   - `costoTotal: 25000` (suma de insumos + maquinaria + mano de obra)
   - `mensaje: "Costo total calculado correctamente"`

2. **Caso 1.5.2: Labor sin costos (labor recién creada)**
   ```
   GET /api/labores/2/calcular-costo
   ```
   **Resultado Esperado:**
   - `success: true`
   - `costoTotal: 0`

3. **Caso 1.5.3: Labor no existe**
   ```
   GET /api/labores/99999/calcular-costo
   ```
   **Resultado Esperado:**
   - `success: false`
   - `mensaje: "Labor no encontrada con ID: 99999"`
   - Status: 400 Bad Request

---

#### ✅ **Test 1.6: Desglose Detallado de Costos**

**Endpoint:** `GET /api/labores/{id}/desglose-costos`

**Casos de Prueba:**

1. **Caso 1.6.1: Desglose completo**
   ```
   GET /api/labores/1/desglose-costos
   ```
   **Resultado Esperado:**
   ```json
   {
     "success": true,
     "costoInsumos": 10000,
     "costoMaquinaria": 8000,
     "costoManoObra": 7000,
     "costoTotal": 25000
   }
   ```

---

#### ✅ **Test 1.7: Actualizar Costo Total de Labor**

**Endpoint:** `POST /api/labores/{id}/actualizar-costo`

**Casos de Prueba:**

1. **Caso 1.7.1: Actualizar costo de labor existente**
   ```
   POST /api/labores/1/actualizar-costo
   ```
   **Resultado Esperado:**
   - `success: true`
   - `costoTotal: 25000`
   - `mensaje: "Costo actualizado correctamente"`
   - Verificar en BD que el campo `costo_total` de la labor se actualizó

---

### **Fase 2: Pruebas de Integración (Frontend + Backend)**

#### ✅ **Test 2.1: Cosechas - Cálculo de Rendimiento**

**Funcionalidad:** Al registrar una cosecha, el rendimiento debe calcularse en el backend.

**Pasos:**
1. Ir a **Cosechas** → **Nueva Cosecha**
2. Seleccionar un lote con cultivo
3. Ingresar:
   - Cantidad: `50000`
   - Unidad: `kg`
   - Superficie del lote: `10 ha`
   - Unidad del cultivo: `tn/ha`
4. Guardar cosecha

**Resultado Esperado:**
- La cosecha se guarda correctamente
- El rendimiento calculado es: `5 tn/ha` (50000kg / 10ha = 5000kg/ha = 5tn/ha)
- El porcentaje de cumplimiento se calcula correctamente vs rendimiento esperado

**Cómo Verificar:**
- Ir a **Reportes** → **Cosechas**
- Buscar la cosecha creada
- Verificar que el `Rendimiento Real` muestre `5.00 tn/ha`

---

#### ✅ **Test 2.2: Labores - Validación de Stock de Insumos**

**Funcionalidad:** Al crear una labor, el sistema debe validar que hay stock suficiente de insumos.

**Pasos:**
1. Ir a **Insumos** y anotar el stock actual de "Fertilizante NPK" (ej: 100 kg)
2. Ir a **Labores** → **Nueva Labor**
3. Seleccionar tipo: **Fertilización**
4. Agregar insumo: "Fertilizante NPK" con cantidad `150 kg` (más del stock disponible)
5. Intentar guardar

**Resultado Esperado:**
- El sistema muestra error: `"Stock insuficiente. Disponible: 100 kg, Requerido: 150 kg"`
- La labor NO se crea

**Cómo Verificar:**
- Verificar que aparece el mensaje de error
- Ir a **Labores** y confirmar que NO se creó la labor

---

#### ✅ **Test 2.3: Labores - Stock Suficiente**

**Funcionalidad:** Al crear una labor con stock suficiente, el sistema debe permitirlo.

**Pasos:**
1. Ir a **Labores** → **Nueva Labor**
2. Seleccionar tipo: **Fertilización**
3. Agregar insumo: "Fertilizante NPK" con cantidad `50 kg` (menos del stock disponible)
4. Guardar labor

**Resultado Esperado:**
- La labor se crea exitosamente
- Se muestra mensaje: `"Labor creada exitosamente"`

**Cómo Verificar:**
- Ir a **Labores** y buscar la labor creada
- Verificar que la labor existe y tiene el insumo asignado

---

#### ✅ **Test 2.4: Labores - Cálculo Automático de Costo Total**

**Funcionalidad:** Al crear una labor con insumos, maquinaria y mano de obra, el costo total debe calcularse correctamente.

**Pasos:**
1. Ir a **Labores** → **Nueva Labor**
2. Agregar:
   - Insumo: "Fertilizante NPK" - 50 kg a $200/kg = **$10,000**
   - Maquinaria: "Tractor Fertilizador" - 5 horas a $1,500/h = **$7,500**
   - Mano de Obra: 2 personas x 8 horas a $500/hora = **$8,000**
3. Guardar labor

**Resultado Esperado:**
- La labor se crea
- Costo Total calculado: **$25,500**

**Cómo Verificar:**
- Ir a **Labores** y ver el detalle de la labor creada
- Verificar que el **Costo Total** sea `$25,500`
- Hacer clic en "Ver Desglose"
- Verificar:
  - Costo Insumos: `$10,000`
  - Costo Maquinaria: `$7,500`
  - Costo Mano de Obra: `$8,000`
  - **Total: `$25,500`**

---

### **Fase 3: Pruebas de Regresión**

#### ✅ **Test 3.1: Funcionalidades Existentes Siguen Funcionando**

**Objetivo:** Verificar que los cambios NO rompieron funcionalidades existentes.

**Pruebas:**

1. **Crear Campo**
   - Ir a **Campos** → **Nuevo Campo**
   - Crear un campo
   - Verificar que se crea correctamente

2. **Crear Lote**
   - Ir a **Lotes** → **Nuevo Lote**
   - Crear un lote en un campo
   - Verificar que se valida la superficie disponible (debe funcionar como antes)

3. **Crear Cultivo**
   - Ir a **Cultivos** → **Nuevo Cultivo**
   - Crear un cultivo
   - Verificar que se crea correctamente

4. **Crear Insumo**
   - Ir a **Insumos** → **Nuevo Insumo**
   - Crear un insumo
   - Verificar que se crea correctamente

5. **Crear Maquinaria**
   - Ir a **Maquinaria** → **Nueva Maquinaria**
   - Crear una maquinaria
   - Verificar que se crea correctamente

6. **Reportes**
   - Ir a **Reportes**
   - Generar reporte de:
     - Rendimiento
     - Cosechas
     - Rentabilidad (si es Jefe Financiero)
   - Verificar que los reportes se generan correctamente

7. **Dashboard**
   - Ir a **Dashboard**
   - Verificar que las estadísticas se muestran correctamente
   - Verificar que los balances se calculan correctamente

---

### **Fase 4: Pruebas por Rol**

#### ✅ **Test 4.1: Rol OPERARIO**

**Funcionalidad:** El operario debe poder crear labores con los nuevos cálculos.

**Pasos:**
1. Iniciar sesión con usuario: `luis.operario@agrocloud.com.ar` / `Pass1234!`
2. Ir a **Labores** → **Nueva Labor**
3. Crear una labor con insumos
4. Verificar que el sistema valida stock
5. Verificar que el costo total se calcula automáticamente

**Resultado Esperado:**
- El operario puede crear labores
- Las validaciones funcionan
- Los cálculos se realizan correctamente

---

#### ✅ **Test 4.2: Rol JEFE_CAMPO**

**Funcionalidad:** El jefe de campo debe poder ver costos detallados.

**Pasos:**
1. Iniciar sesión con usuario: `juan.tecnico@agrocloud.com.ar` / `Pass1234!`
2. Ir a **Labores**
3. Ver el detalle de una labor
4. Hacer clic en "Ver Desglose de Costos"

**Resultado Esperado:**
- El jefe de campo ve los costos detallados
- Los cálculos son correctos

---

#### ✅ **Test 4.3: Rol JEFE_FINANCIERO**

**Funcionalidad:** El jefe financiero debe poder ver todos los costos y reportes financieros.

**Pasos:**
1. Iniciar sesión con usuario: `maria.financiero@agrocloud.com.ar` / `Pass1234!`
2. Ir a **Dashboard**
3. Verificar que ve las tarjetas financieras
4. Ir a **Reportes** → **Rentabilidad**
5. Generar reporte

**Resultado Esperado:**
- El jefe financiero ve todos los datos financieros
- Los reportes de rentabilidad se generan correctamente
- Los cálculos de costos están correctos

---

### **Fase 5: Pruebas de Estrés y Edge Cases**

#### ✅ **Test 5.1: Labor con Muchos Insumos**

**Objetivo:** Verificar que el sistema maneja correctamente labores con múltiples insumos.

**Pasos:**
1. Crear una labor con 10+ insumos diferentes
2. Verificar que el costo total se calcula correctamente
3. Verificar que el desglose muestra todos los insumos

**Resultado Esperado:**
- El sistema maneja correctamente múltiples insumos
- El costo total es correcto

---

#### ✅ **Test 5.2: Labor con Valores Decimales Complejos**

**Objetivo:** Verificar que el sistema maneja correctamente decimales.

**Pasos:**
1. Crear una labor con:
   - Insumo: 12.375 kg a $325.99/kg
   - Maquinaria: 3.5 horas a $1,275.50/h
2. Verificar cálculos

**Resultado Esperado:**
- Los cálculos son precisos (2 decimales)
- No hay errores de redondeo

---

#### ✅ **Test 5.3: Cosecha con Unidades Raras**

**Objetivo:** Verificar conversiones de unidades poco comunes.

**Pasos:**
1. Crear una cosecha con:
   - Cantidad: 250 qq (quintales)
   - Unidad Cultivo: kg/ha
   - Superficie: 2.5 ha
2. Verificar rendimiento

**Resultado Esperado:**
- Rendimiento calculado: `4,600 kg/ha` (250qq = 11,500kg / 2.5ha)

---

## 📊 Checklist de Pruebas

### **Backend (API)**
- [ ] Test 1.1: Cálculo de Rendimiento Real (4 casos)
- [ ] Test 1.2: Diferencia Porcentual (3 casos)
- [ ] Test 1.3: Validación de Stock (3 casos)
- [ ] Test 1.4: Stock Real Disponible (2 casos)
- [ ] Test 1.5: Cálculo de Costo Total (3 casos)
- [ ] Test 1.6: Desglose de Costos (1 caso)
- [ ] Test 1.7: Actualizar Costo Total (1 caso)

### **Integración (Frontend + Backend)**
- [ ] Test 2.1: Cosechas - Rendimiento
- [ ] Test 2.2: Labores - Validación Stock Insuficiente
- [ ] Test 2.3: Labores - Stock Suficiente
- [ ] Test 2.4: Labores - Cálculo Automático de Costo

### **Regresión**
- [ ] Test 3.1: Funcionalidades existentes (7 pruebas)

### **Por Rol**
- [ ] Test 4.1: OPERARIO
- [ ] Test 4.2: JEFE_CAMPO
- [ ] Test 4.3: JEFE_FINANCIERO

### **Edge Cases**
- [ ] Test 5.1: Labor con muchos insumos
- [ ] Test 5.2: Valores decimales complejos
- [ ] Test 5.3: Unidades raras

---

## 🔍 Cómo Ejecutar las Pruebas

### **1. Preparar el Entorno**
```bash
# Backend
cd agrogestion-backend
.\mvnw.cmd spring-boot:run

# Frontend (en otra terminal)
cd agrogestion-frontend
npm start
```

### **2. Pruebas de API (Backend)**

**Usando Postman o cURL:**

```bash
# Test 1.1.1: Rendimiento kg a kg/ha
curl -X POST http://localhost:8080/api/v1/rendimiento/calcular \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "cantidad": 45000,
    "unidadCantidad": "kg",
    "superficie": 10,
    "unidadCultivo": "kg/ha"
  }'

# Test 1.3.1: Validar stock
curl -X POST http://localhost:8080/api/insumos/1/validar-stock \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"cantidad": 50}'

# Test 1.5.1: Calcular costo total
curl -X GET http://localhost:8080/api/labores/1/calcular-costo \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### **3. Pruebas de Integración (Frontend)**

1. Abrir navegador en `http://localhost:3000`
2. Iniciar sesión con cada rol
3. Seguir los pasos de cada test

---

## 📈 Criterios de Éxito

✅ **Todos los tests de backend (Fase 1) pasan correctamente**  
✅ **Todas las pruebas de integración (Fase 2) funcionan**  
✅ **No hay regresiones (Fase 3)**  
✅ **Todos los roles funcionan correctamente (Fase 4)**  
✅ **Edge cases manejados correctamente (Fase 5)**  

---

## 🚨 Problemas Conocidos

### **Ninguno por ahora** ✅

---

## 📝 Notas

1. **Tokens de autenticación:** Para las pruebas de API, necesitas obtener un token JWT válido:
   ```bash
   curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "admin@agrocloud.com.ar",
       "password": "Admin123!"
     }'
   ```

2. **Limpiar datos de prueba:** Después de las pruebas, puede ser necesario limpiar datos de prueba de la BD.

3. **Performance:** Monitorear tiempos de respuesta. Los endpoints nuevos deben responder en < 500ms.

---

## ✅ Conclusión

Este plan cubre todas las funcionalidades nuevas y verifica que no se rompió nada existente. 

**Tiempo estimado de ejecución completo:** 2-3 horas

**Fecha del plan:** 10 de Octubre, 2025  
**Responsable:** Equipo de Desarrollo  
**Estado:** ✅ Listo para ejecutar


