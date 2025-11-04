# 🧪 Guía de Prueba del Módulo de Aplicaciones de Agroquímicos

## 📋 Pasos para Probar Localmente

### 1. Ejecutar el Script SQL

Ejecuta el script batch para crear las nuevas tablas:

```bash
ejecutar-tablas-agroquimicos.bat
```

Esto creará las tablas:
- `dosis_aplicacion` - Para configurar dosis sugeridas
- `aplicaciones_agroquimicas` - Para registrar aplicaciones

### 2. Reiniciar el Backend

Reinicia el backend para que reconozca las nuevas entidades:

```bash
cd agrogestion-backend
.\mvnw.cmd spring-boot:run
```

### 3. Probar los Endpoints

#### 3.1. Configurar una Dosis de Aplicación

**Endpoint:** `POST /api/v1/aplicaciones-agroquimicas/dosis`

```json
{
  "insumoId": 1,
  "tipoAplicacion": "FOLIAR",
  "dosisPorHa": 2.5,
  "unidadMedida": "litros",
  "descripcion": "Aplicación foliar para control de malezas"
}
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Dosis de aplicación configurada exitosamente",
  "data": {
    "id": 1,
    "insumoId": 1,
    "insumoNombre": "Herbicida Glifosato",
    "tipoAplicacion": "FOLIAR",
    "dosisPorHa": 2.5,
    "unidadMedida": "litros",
    "descripcion": "Aplicación foliar para control de malezas",
    "activo": true
  }
}
```

#### 3.2. Obtener Dosis Sugerida

**Endpoint:** `GET /api/v1/aplicaciones-agroquimicas/dosis/sugerir?insumoId=1&tipoAplicacion=FOLIAR`

**Respuesta esperada:**
```json
{
  "id": 1,
  "insumoId": 1,
  "insumoNombre": "Herbicida Glifosato",
  "tipoAplicacion": "FOLIAR",
  "dosisPorHa": 2.5,
  "unidadMedida": "litros",
  "descripcion": "Aplicación foliar para control de malezas",
  "activo": true
}
```

#### 3.3. Crear una Aplicación de Agroquímico

**Endpoint:** `POST /api/v1/aplicaciones-agroquimicas`

```json
{
  "laborId": 1,
  "insumoId": 1,
  "tipoAplicacion": "FOLIAR",
  "cantidadTotalAplicar": 5.0,
  "dosisAplicadaPorHa": 2.5,
  "superficieAplicadaHa": 2.0,
  "unidadMedida": "litros",
  "observaciones": "Aplicación realizada en horario matutino",
  "fechaAplicacion": "2025-10-18T08:00:00"
}
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Aplicación de agroquímico registrada exitosamente",
  "data": {
    "id": 1,
    "laborId": 1,
    "laborNombre": "PULVERIZACION",
    "insumoId": 1,
    "insumoNombre": "Herbicida Glifosato",
    "tipoAplicacion": "FOLIAR",
    "cantidadTotalAplicar": 5.0,
    "dosisAplicadaPorHa": 2.5,
    "superficieAplicadaHa": 2.0,
    "unidadMedida": "litros",
    "observaciones": "Aplicación realizada en horario matutino",
    "fechaAplicacion": "2025-10-18T08:00:00",
    "fechaRegistro": "2025-10-18T23:00:00",
    "activo": true
  }
}
```

**Validaciones que se realizan:**
- ✅ Verifica que la labor exista
- ✅ Verifica que el insumo exista
- ✅ Verifica que la labor tenga un lote asociado
- ✅ Calcula automáticamente la cantidad si no se proporciona
- ✅ Valida que el stock sea suficiente
- ✅ Descuenta automáticamente del stock

#### 3.4. Obtener Aplicaciones por Labor

**Endpoint:** `GET /api/v1/aplicaciones-agroquimicas/labor/1`

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "laborId": 1,
    "laborNombre": "PULVERIZACION",
    "insumoId": 1,
    "insumoNombre": "Herbicida Glifosato",
    "tipoAplicacion": "FOLIAR",
    "cantidadTotalAplicar": 5.0,
    "dosisAplicadaPorHa": 2.5,
    "superficieAplicadaHa": 2.0,
    "unidadMedida": "litros",
    "observaciones": "Aplicación realizada en horario matutino",
    "fechaAplicacion": "2025-10-18T08:00:00",
    "fechaRegistro": "2025-10-18T23:00:00",
    "activo": true
  }
]
```

#### 3.5. Obtener Estadísticas por Insumo

**Endpoint:** `GET /api/v1/aplicaciones-agroquimicas/insumo/1/estadisticas`

**Respuesta esperada:**
```json
{
  "insumoId": 1,
  "insumoNombre": "Herbicida Glifosato",
  "vecesUtilizado": 1,
  "totalAplicado": 5.0,
  "stockActual": 95.0,
  "aplicaciones": [
    {
      "id": 1,
      "laborId": 1,
      "laborNombre": "PULVERIZACION",
      "insumoId": 1,
      "insumoNombre": "Herbicida Glifosato",
      "tipoAplicacion": "FOLIAR",
      "cantidadTotalAplicar": 5.0,
      "dosisAplicadaPorHa": 2.5,
      "superficieAplicadaHa": 2.0,
      "unidadMedida": "litros",
      "observaciones": "Aplicación realizada en horario matutino",
      "fechaAplicacion": "2025-10-18T08:00:00",
      "fechaRegistro": "2025-10-18T23:00:00",
      "activo": true
    }
  ]
}
```

#### 3.6. Eliminar una Aplicación

**Endpoint:** `DELETE /api/v1/aplicaciones-agroquimicas/1`

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Aplicación eliminada exitosamente"
}
```

**Validaciones que se realizan:**
- ✅ Restaura el stock del insumo
- ✅ Elimina lógicamente la aplicación (activo = false)

## 🧪 Casos de Prueba

### Caso 1: Crear aplicación con stock suficiente
1. Crear un insumo con stock = 100
2. Crear una aplicación con cantidad = 5
3. ✅ Verificar que se guarde correctamente
4. ✅ Verificar que el stock se actualice a 95

### Caso 2: Crear aplicación con stock insuficiente
1. Crear un insumo con stock = 5
2. Intentar crear una aplicación con cantidad = 10
3. ❌ Debe devolver error: "Stock insuficiente"

### Caso 3: Cálculo automático de cantidad
1. Crear una labor con lote de superficie = 2 ha
2. Crear una aplicación sin especificar cantidadTotalAplicar
3. ✅ Debe calcular automáticamente: superficie * dosisPorHa

### Caso 4: Eliminar aplicación y restaurar stock
1. Crear una aplicación con cantidad = 5
2. Verificar que el stock se redujo
3. Eliminar la aplicación
4. ✅ Verificar que el stock se restaure

### Caso 5: Sugerir dosis por defecto
1. Solicitar dosis sugerida para un insumo sin configuración
2. ✅ Debe devolver valores por defecto según el tipo de aplicación

## 📊 Endpoints Disponibles

### Aplicaciones
- `GET /api/v1/aplicaciones-agroquimicas` - Listar todas
- `GET /api/v1/aplicaciones-agroquimicas/labor/{laborId}` - Por labor
- `GET /api/v1/aplicaciones-agroquimicas/insumo/{insumoId}` - Por insumo
- `GET /api/v1/aplicaciones-agroquimicas/{id}` - Por ID
- `POST /api/v1/aplicaciones-agroquimicas` - Crear
- `DELETE /api/v1/aplicaciones-agroquimicas/{id}` - Eliminar
- `GET /api/v1/aplicaciones-agroquimicas/insumo/{insumoId}/estadisticas` - Estadísticas

### Dosis
- `GET /api/v1/aplicaciones-agroquimicas/dosis/insumo/{insumoId}` - Listar dosis
- `GET /api/v1/aplicaciones-agroquimicas/dosis/sugerir` - Sugerir dosis
- `POST /api/v1/aplicaciones-agroquimicas/dosis` - Crear dosis
- `PUT /api/v1/aplicaciones-agroquimicas/dosis/{id}` - Actualizar dosis
- `DELETE /api/v1/aplicaciones-agroquimicas/dosis/{id}` - Eliminar dosis

## 🔍 Verificación en Base de Datos

Puedes verificar las tablas creadas ejecutando:

```sql
-- Ver todas las dosis configuradas
SELECT * FROM dosis_aplicacion;

-- Ver todas las aplicaciones
SELECT * FROM aplicaciones_agroquimicas;

-- Ver aplicaciones con detalles
SELECT 
    aa.id,
    aa.cantidad_total_aplicar,
    aa.tipo_aplicacion,
    i.nombre as insumo,
    l.tipo_labor as labor,
    p.nombre as lote
FROM aplicaciones_agroquimicas aa
JOIN insumos i ON aa.insumo_id = i.id
JOIN labores l ON aa.labor_id = l.id
JOIN lotes p ON l.lote_id = p.id
WHERE aa.activo = true;
```

## ✅ Checklist de Prueba

- [ ] Script SQL ejecutado correctamente
- [ ] Backend reiniciado sin errores
- [ ] Endpoint de crear dosis funciona
- [ ] Endpoint de sugerir dosis funciona
- [ ] Endpoint de crear aplicación funciona
- [ ] Validación de stock funciona
- [ ] Descuento de stock funciona
- [ ] Cálculo automático de cantidad funciona
- [ ] Endpoint de listar aplicaciones funciona
- [ ] Endpoint de estadísticas funciona
- [ ] Endpoint de eliminar aplicación funciona
- [ ] Restauración de stock al eliminar funciona

## 🐛 Troubleshooting

### Error: "Table doesn't exist"
- Verifica que el script SQL se ejecutó correctamente
- Ejecuta nuevamente: `ejecutar-tablas-agroquimicos.bat`

### Error: "Stock insuficiente"
- Verifica que el insumo tenga suficiente stock
- Actualiza el stock: `UPDATE insumos SET stock_actual = 100 WHERE id = 1;`

### Error: "Labor no encontrada"
- Verifica que exista una labor con el ID especificado
- Consulta: `SELECT * FROM labores;`

### Error: "Insumo no encontrado"
- Verifica que exista un insumo con el ID especificado
- Consulta: `SELECT * FROM insumos;`











