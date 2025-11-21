# Matriz de Versiones por Módulo

Este documento mantiene el registro de las versiones de cada módulo en los diferentes entornos.

**Última actualización**: 2025-01-16

---

## Versiones por Entorno

| Módulo | Testing | Production | Última Versión | Estado |
|--------|---------|------------|----------------|--------|
| **auth** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **admin-global** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **empresas** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **campos** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **cultivos** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **cosechas** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **insumos** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **agroquimicos** | 1.1.0 | 1.0.0 | 1.1.0 | 🚧 En desarrollo |
| **labores** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **maquinaria** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **finanzas** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **rendimientos** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **dashboard** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **weather** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |
| **roles** | 1.0.0 | 1.0.0 | 1.0.0 | ✅ Estable |

---

## Versiones del Sistema

| Componente | Versión Actual | Última Actualización |
|------------|----------------|----------------------|
| **Backend** | 1.1.0 | 2025-01-16 |
| **Frontend** | 1.0.0 | 2025-01-16 |

---

## Leyenda de Estados

- ✅ **Estable**: Módulo estable y funcionando correctamente
- 🚧 **En desarrollo**: Módulo en desarrollo activo
- ⚠️ **En pruebas**: Módulo en fase de testing
- 🔴 **Con problemas**: Módulo con problemas conocidos
- 📦 **Pendiente**: Módulo planificado pero no iniciado

---

## Historial de Cambios

### 2025-01-16
- **Backend**: Actualizado de 1.0.0 → 1.1.0
- **Frontend**: Actualizado de 0.0.0 → 1.0.0
- **modulo-agroquimicos**: Versión 1.1.0 en testing

### 2024-12-30
- Release inicial v1.0.0
- Todos los módulos base en versión 1.0.0

---

## Notas

- Las versiones en **Testing** representan la última versión disponible para pruebas
- Las versiones en **Production** representan la versión desplegada en producción
- **Última Versión** es la versión más reciente desarrollada (puede estar solo en desarrollo)

---

## Consultar Versiones

### Endpoint API
```bash
GET /api/version
```

### Respuesta Ejemplo
```json
{
  "versions": {
    "backend": "1.1.0",
    "frontend": "1.0.0"
  },
  "modules": {
    "auth": "1.0.0",
    "agroquimicos": "1.1.0",
    ...
  },
  "timestamp": "2025-01-16T10:30:00",
  "environment": "production"
}
```

