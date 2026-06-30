# SPEC — Módulo Lechería multi-especie

**Versión:** 1.0  
**Fecha:** Junio 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Fecha aprobación:** Junio 2026  
**Referencia modelo:** `SPEC-MODULO-FEEDLOT.md`, `SPEC-UBICACION-CLIMA-MODULOS-AVICOLA-FEEDLOT.md`  
**Relacionada con:** `SPEC-CAMPANA-TRANSVERSAL.md`, `SPEC-TRAZABILIDAD-COMERCIAL-CERTIFICABLE.md`

---

## 1. Objetivo

Gestionar explotaciones **lecheras multi-especie** (bovino, caprino, ovino, búfalo; extensible a camélido) con registro **individual por animal**, ciclo **reproductivo y lactancia**, **ordeñe** diario, **sanidad**, **alimentación** (inventario CORE), **ventas de leche**, **ubicación geográfica** (Google Maps) y **clima** operativo.

Posicionamiento: plataforma agropecuaria integrada — no competir con hardware de sala de ordeñe en v1.

---

## 2. Glosario agronómico

| Término | Definición |
|---------|------------|
| **Tambo / explotación lechera** | Establecimiento del módulo (`lecheria_establecimiento`). |
| **Rodeo** | Unidad de manejo (grupo) de animales dentro del establecimiento. |
| **Animal productivo** | Hembra o macho reproductor con identificación única por empresa. |
| **Lactancia** | Período desde parto válido hasta secado o baja. |
| **DIM** | Días en leche = fecha registro − fecha parto (si lactancia activa). |
| **Ordeñe** | Registro de litros (y calidad opcional) por animal, fecha y turno. |
| **RCS / SCC** | Recuento de células somáticas (calidad ubre). |
| **Secado** | Fin de lactancia antes del próximo parto. |
| **ECC / BCS** | Estado corporal (score 1–5). |
| **Período de gestión** | `core_campanas`. |

---

## 3. Módulo y seguridad

| Concepto | Valor |
|----------|--------|
| Código `modules.code` | `LECHERIA` |
| `@RequiresModule` | `"LECHERIA"` |
| `InventoryOrigin` | `LECHERIA` |
| API | `/api/lecheria` |
| Paquete Java | `com.agrocloud.lecheria` |
| Frontend | `agrogestion-frontend/src/modules/lecheria` |
| Prefijo tablas | `lecheria_*` |
| Nombre UI | **Lechería** |

---

## 4. Especies v1 y parámetros

| Especie | Días gestación | Ordeñes/día típico | Umbral RCS alerta (cel/ml) |
|---------|---------------|-------------------|---------------------------|
| BOVINO | 280 | 2 (AM/PM) | 200000 |
| BUFALO | 310 | 2 | 200000 |
| CAPRINO | 150 | 2 | 500000 |
| OVINO | 147 | 2 | 500000 |
| CAMELIDO | 345 | 1–2 | 300000 |

Parámetros en `lecheria_parametro_especie` (seed en migración).

---

## 5. Entidades v1

Ver `DISENO-TECNICO-MODULO-LECHERIA.md` para DDL completo.

**Catálogos:** establecimiento, rodeo, raza (por especie), motivo baja, parámetro especie.  
**Núcleo:** animal, lactancia, registro ordeñe, evento reproductivo, evento sanitario, score corporal, consumo, venta leche, baja animal.

---

## 6. Reglas de negocio

### 6.1 Animal

| Regla | Detalle |
|-------|---------|
| RN-A01 | `identificacion` única por `empresa_id`. |
| RN-A02 | Hembras productivas: estados `LACTANDO`, `SECA`, `PRENADA`, `VAQUILLONA`. |
| RN-A03 | Al registrar **parto**, se crea lactancia activa y estado → `LACTANDO`. |
| RN-A04 | Al **secado**, lactancia activa → cerrada; estado → `SECA` o `PRENADA` si gestante. |
| RN-A05 | **Baja** marca `activo=false`, `fecha_baja`, no permite ordeñe posterior. |

### 6.2 Lactancia y ordeñe

| Regla | Detalle |
|-------|---------|
| RN-L01 | Una sola lactancia `ACTIVA` por animal. |
| RN-L02 | Ordeñe solo en animal `LACTANDO` con lactancia activa. |
| RN-L03 | Turnos: `AM`, `PM`, `TOTAL`. Máximo un registro por animal+fecha+turno. |
| RN-L04 | Litros > 0 obligatorios. Grasa, proteína, RCS opcionales. |
| RN-L05 | DIM calculado en lectura, no persistido redundante. |

### 6.3 Reproducción

| Regla | Detalle |
|-------|---------|
| RN-R01 | Tipos: `SERVICIO`, `TACTO`, `PARTO`, `SECADO`, `ABORTO`. |
| RN-R02 | `PARTO` crea lactancia y cierra gestación implícita. |
| RN-R03 | `TACTO` positivo → estado `PRENADA`. |
| RN-R04 | `SECADO` cierra lactancia activa. |

### 6.4 Sanidad e inventario

| Regla | Detalle |
|-------|---------|
| RN-S01 | Evento sanitario opcionalmente descuenta `insumo_id` vía `InventoryService` origen `LECHERIA`. |
| RN-S02 | `dias_retiro` opcional; alerta en panel si retiro vigente. |

### 6.5 Ubicación y clima

| Regla | Detalle |
|-------|---------|
| RN-U01 | Coordenadas JSON `[{lat,lng}]` en establecimiento. |
| RN-U02 | DTO expone `climaLatitud`/`climaLongitud` por centroide del establecimiento del rodeo del animal. |
| RN-U03 | Ordeñe puede guardar `temperatura_ambiente`, `humedad_ambiente` (manual o desde clima). |

---

## 7. KPIs panel v1

- Litros totales período / litros por animal lactante / día
- Promedio RCS (último control)
- Animales lactando / secas / preñadas
- **Acciones del día:** a servir (DIM > umbral y no preñada), a secar (DIM > objetivo especie), a parir (fecha prevista ± ventana), RCS alto, retiro activo

---

## 8. Exclusiones v1

- Integración hardware sala de ordeñe / RFID
- Sincronización DHI automática (v1.5: import CSV)
- Pastoreo virtual / sensores de actividad
- Offline completo (v1.5)

---

## 9. Criterios de aceptación v1

- [ ] Módulo `LECHERIA` habilitable por empresa
- [ ] Animales de ≥2 especies con razas distintas
- [ ] Ciclo parto → ordeñe → secado → nuevo parto
- [ ] Panel KPIs y acciones del día
- [ ] Mapa en establecimiento; clima en detalle animal
- [ ] Ordeñe con autocompletar clima
- [ ] Sanidad descuenta inventario CORE

---

## 10. v1.5 (alcance extendido)

- Import CSV control lechero
- Closeout económico por rodeo
- Reporte gráfico temperatura vs producción
- Export movimientos formato SENASA (borrador)
