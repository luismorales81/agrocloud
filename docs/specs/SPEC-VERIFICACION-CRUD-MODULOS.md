# SPEC — Verificación CRUD automatizada de módulos

**Versión:** 1.0  
**Fecha:** Julio 2026  
**Metodología:** SDD  
**Estado:** Aprobada (implementación en curso)

---

## 1. Objetivo

Garantizar mediante tests de integración automatizados (JUnit 5 + MockMvc) que las operaciones de **alta, lectura, edición y baja** de datos funcionen correctamente en todos los módulos de AgroGestion, ejecutables en entorno local con MySQL (`agrocloud_test`).

---

## 2. Alcance

| Incluido | Excluido (fase posterior) |
|----------|---------------------------|
| API REST backend (85 controllers) | Tests e2e frontend (Playwright) |
| Ciclo CRUD donde el diseño lo permita | Tests de rendimiento (JMeter) |
| Casos negativos mínimos (400, 404) | Seguridad JWT con filtros activos (fase 2) |
| Multi-tenant vía `X-Company-Id` | Endpoints legacy no usados por UI activa |

---

## 3. Matriz entidad × operación

### 3.1 Cultivos

| Entidad | Ruta API | C | R | U | D | Notas |
|---------|----------|---|---|---|---|-------|
| Campos | `/api/campos` | ✓ | ✓ | ✓ | ✓ | |
| Lotes | `/api/v1/lotes` | ✓ | ✓ | ✓ | ✓ | Baja lógica |
| Cultivos | `/api/v1/cultivos` | ✓ | ✓ | ✓ | ✓ | Baja lógica |
| Insumos | `/api/insumos` | ✓ | ✓ | ✓ | ✓ | |
| Labores | `/api/labores` | ✓ | ✓ | ✓ | ✓ | |
| Ingresos | `/api/v1/ingresos` | ✓ | ✓ | ✓ | ✓ | |
| Egresos | `/api/v1/egresos` | ✓ | ✓ | ✓ | ✓ | |
| Maquinaria | `/api/maquinaria` | ✓ | ✓ | ✓ | ✓ | Baja lógica; UI persiste DELETE |
| Config. estados | `/api/v1/configuracion-estados` | ✓ | ✓ | ✓ | ✓ | Sub-recursos |
| Campañas | `/api/v1/campanas` | ✓ | ✓ | — | — | Activar/cerrar, sin DELETE |

### 3.2 Porcinos

| Entidad | Ruta API | C | R | U | D | Notas |
|---------|----------|---|---|---|---|-------|
| Madres | `/api/v1/porcinos/madres` | ✓ | ✓ | ✓ | ✓ | |
| Padrillos | `/api/v1/porcinos/padrillos` | ✓ | ✓ | ✓ | ✓ | |
| Recría | `/api/v1/porcinos/recria` | ✓ | ✓ | ✓ | ✓ | |
| Catálogos | `/api/v1/porcinos/catalogos` | ✓ | ✓ | — | ✓ | |
| Eventos sanitarios | `/api/v1/porcinos/eventos-sanitarios` | ✓ | ✓ | — | ✓ | Sin PUT |
| Servicios/partos/destetes | varios | ✓ | ✓ | — | — | Solo registro |
| Lotes engorde | `/api/porcinos` | ✓ | ✓ | ✓ | — | Cierre, no DELETE |

### 3.3 Feedlot

| Entidad | Ruta API | C | R | U | D |
|---------|----------|---|---|---|---|
| Lotes | `/api/feedlot/lotes` | ✓ | ✓ | ✓ | — |
| Establecimientos | `/api/feedlot/establecimientos` | ✓ | ✓ | ✓ | — |
| Corrales | `/api/feedlot/corrales` | ✓ | ✓ | ✓ | — |
| Catálogos/dietas | `/api/feedlot/catalogos`, `/dietas` | ✓ | ✓ | ✓ | ✓ |

### 3.4 Lechería

| Entidad | Ruta API | C | R | U | D |
|---------|----------|---|---|---|---|
| Animales | `/api/lecheria/animales` | ✓ | ✓ | ✓ | — |
| Establecimientos | `/api/lecheria/establecimientos` | ✓ | ✓ | ✓ | — |
| Rodeos | `/api/lecheria/rodeos` | ✓ | ✓ | — | — |
| Ordeñe/ventas | `/api/lecheria/ordenes`, `/ventas-leche` | ✓ | ✓ | — | — |

### 3.5 Avícola (crianza, huevos, ponedoras)

| Entidad | Ruta base | C | R | U | D |
|---------|-----------|---|---|---|---|
| Lotes | `/api/avicola-*` | ✓ | ✓ | ✓ | — |
| Establecimientos | `/api/avicola-*` | ✓ | ✓ | ✓ | — |
| Razas | `/api/avicola-*` | ✓ | ✓ | ✓ | — |

### 3.6 Administración / transversal

| Entidad | Ruta API | C | R | U | D |
|---------|----------|---|---|---|---|
| Recordatorios | `/api/recordatorios` | ✓ | ✓ | ✓ | ✓ |
| Roles | `/api/roles` | ✓ | ✓ | ✓ | ✓ |
| Tareas recurrentes | `/api/calendario/tareas-recurrentes` | ✓ | ✓ | ✓ | ✓ |

---

## 4. Criterios de aceptación por test

1. **CREATE:** HTTP 200/201; body con `id` asignado; persistencia verificable con GET.
2. **READ:** HTTP 200; datos coinciden con los creados.
3. **UPDATE:** HTTP 200; campo modificado reflejado en GET.
4. **DELETE:** HTTP 204/200; GET posterior devuelve 404 o recurso inactivo.
5. **Multi-tenant:** recurso de otra empresa no accesible (403/404).
6. **Validación:** body inválido → 400.
7. **Módulo deshabilitado:** donde aplique `@RequiresModule` → 402 con `MODULE_NOT_ENABLED`.
8. **Campaña cerrada:** escrituras rechazadas → 400.

---

## 5. Entidades sin DELETE por diseño

Documentadas para no fallar tests por expectativa incorrecta:

- Lotes porcinos/feedlot/avícola → `cerrarLote` o cambio de estado.
- Campañas → `activar` / `cerrar`.
- Servicios, partos, destetes, ordeñe → registro inmutable.
- Maquinaria → DELETE lógico en API y UI.

---

## 6. Riesgos

| Riesgo | Mitigación |
|--------|------------|
| BD test sucia | BD dedicada `agrocloud_test`; sufijos únicos por test |
| Flyway lento | `spring.flyway.enabled=true` una vez; datos aislados por empresa |
| Filtros seguridad desactivados | Documentado; subset futuro con JWT |
| Rutas legacy duplicadas | Tests solo contra rutas del frontend activo |

---

## 7. Aprobación

| Rol | Estado | Fecha |
|-----|--------|-------|
| Producto / solicitante | Aprobada (implementar plan adjunto) | Jul 2026 |
