# SPEC: Importación de Configuración de Estados y Tareas desde Excel

**Versión:** 1.0  
**Fecha:** 2025-03-15  
**Estado:** Aprobada

---

## 1. Objetivo

Permitir que los usuarios carguen la configuración completa de estados, transiciones y tareas de un tipo de cultivo mediante un archivo Excel, evitando la carga manual uno a uno. Incluir un archivo de ejemplo descargable para que el usuario comprenda la estructura.

---

## 2. Alcance

### 2.1 Incluido
- **Importación**: Subir archivo Excel (.xlsx) con 3 pestañas: Estados, Transiciones, Tareas
- **Exportación plantilla**: Descargar archivo Excel de ejemplo con estructura y datos de referencia
- **Validación**: Validar datos antes de persistir; reportar errores por fila
- **Modo**: Crear nuevo tipo de cultivo O actualizar existente (según parámetro)

### 2.2 Excluido
- Importación de tipos de cultivo desde Excel (solo se asocia a uno existente o se crea uno nuevo con nombre del archivo)
- Importación parcial (solo estados o solo tareas): el archivo debe tener al menos la pestaña Estados

---

## 3. Estructura del Archivo Excel

### 3.1 Pestaña "Estados"
| Columna | Obligatorio | Descripción | Ejemplo |
|---------|-------------|-------------|---------|
| nombre | Sí | Nombre del estado | Disponible |
| descripcion | No | Descripción | Lote listo para trabajar |
| color | No | Color hex (#RRGGBB) | #10b981 |
| icono | No | Emoji o carácter | 🟢 |
| orden | Sí | Orden en el flujo (1, 2, 3...) | 1 |
| es_estado_inicial | No | true/false | true |
| es_estado_final | No | true/false | false |

### 3.2 Pestaña "Transiciones"
| Columna | Obligatorio | Descripción | Ejemplo |
|---------|-------------|-------------|---------|
| estado_origen | Sí | Nombre exacto del estado origen | Disponible |
| estado_destino | Sí | Nombre exacto del estado destino | Preparado |
| requiere_motivo | No | true/false | false |

### 3.3 Pestaña "Tareas"
| Columna | Obligatorio | Descripción | Ejemplo |
|---------|-------------|-------------|---------|
| estado | Sí | Nombre exacto del estado | Sembrado |
| tipo_labor | Sí | Código: SIEMBRA, FERTILIZACION, RIEGO, COSECHA, MANTENIMIENTO, PODA, CONTROL_PLAGAS, CONTROL_MALEZAS, ANALISIS_SUELO, OTROS | RIEGO |
| nombre_tarea | Sí | Nombre amigable | Riego |
| descripcion | No | Descripción | Aplicación de agua |
| es_obligatoria | No | true/false | false |
| orden | No | Orden (default 0) | 1 |

---

## 4. Reglas de Negocio

1. **Orden de procesamiento**: Estados → Transiciones → Tareas (las transiciones y tareas referencian estados por nombre)
2. **Nombres únicos**: Los nombres de estados deben ser únicos dentro del tipo de cultivo
3. **Referencias**: Transiciones y tareas deben referenciar estados que existan en la pestaña Estados
4. **Tipo de labor**: Debe ser uno de los valores del enum Labor.TipoLabor
5. **Un estado inicial y un estado final**: Debe haber exactamente un estado con es_estado_inicial=true y uno con es_estado_final=true
6. **Empresa**: Si se proporciona empresaId, se crea personalización; si no, se crea plantilla (solo superadmin)

---

## 5. Validaciones y Errores

- **Archivo vacío o sin pestaña Estados**: Error 400
- **Fila con datos inválidos**: Incluir en reporte de errores con número de fila
- **Estado duplicado**: Error en fila
- **Transición con estado inexistente**: Error en fila
- **Tarea con estado inexistente**: Error en fila
- **Tipo de labor inválido**: Error en fila

---

## 6. Riesgos Identificados

- **Archivos grandes**: Limitar a 500 filas por pestaña
- **Encoding**: Excel maneja UTF-8; validar caracteres especiales en nombres
- **Transaccionalidad**: Si falla en medio, hacer rollback completo

---

## 7. Endpoints API

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/v1/configuracion-estados/importar | Subir Excel (multipart/form-data) |
| GET | /api/v1/configuracion-estados/plantilla-excel | Descargar archivo de ejemplo |

---

## 8. Respuesta de Importación

```json
{
  "exito": true,
  "tipoCultivoId": 5,
  "tipoCultivoNombre": "Soja",
  "estadosCreados": 8,
  "transicionesCreadas": 8,
  "tareasCreadas": 15,
  "errores": []
}
```

Si hay errores:
```json
{
  "exito": false,
  "errores": [
    { "pestana": "Transiciones", "fila": 3, "mensaje": "Estado origen 'Preparado' no encontrado" },
    { "pestana": "Tareas", "fila": 5, "mensaje": "Tipo de labor 'FUMIGACION' no válido" }
  ]
}
```
