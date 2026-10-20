# SPEC — Feedlot v2: carga de evento sobre lecturas del bastón

**Versión:** 1.1  
**Fecha:** 15 de septiembre de 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Fecha aprobación:** 15 de septiembre de 2026 (plan “CSV Tru-Test eventos”)  
**Módulo:** `FEEDLOT`  
**Padre:** `SPEC-FEEDLOT-V2-SESION-MANGA-BASTON.md`  
**Relacionada:** `SPEC-FEEDLOT-V2-RECUENTO-RESOLUCION.md`  
**Diseño técnico:** `DISENO-TECNICO-FEEDLOT-V2-CARGA-EVENTO-MANGA.md`

---

## 1. Objetivo

Que el operador actualice **un evento de tropa por carga** a partir del CSV de sesión de Tru-Test / Data Link. El archivo trae **todas las columnas de la sesión** (listas personalizables del bastón o cabezal). AgroGestion no adivina el evento: el operador lo elige. **Cómo se leen esas columnas** se configura de antemano en un **perfil de lectura** por tipo de evento.

No se cambia el modelo de negocio ya aprobado (padrón, tipos de sesión, lote híbrido, inventario CORE). Esta SPEC cubre la experiencia de carga, los perfiles y las reglas de coherencia a prueba de tontos.

---

## 2. Problema

Tru-Test no publica un CSV distinto por enrolar, pesar, vacunar, contar o dar de baja. Exporta una **sesión** con los encabezados que el establecimiento configuró (`EID`, `VID`, `Weight`, o nombres custom: `Peso vivo`, `Aftosa`, `Treatment2`). Data Link matchea por **nombre de columna**, no por posición.

El tipo de evento (vacuna vs baja) **no viene en el archivo**. Sistemas equivalentes (CattleMax, HerdMASTER, FarmIT) guardan **plantillas de mapeo** y validan antes del commit.

Hoy AgroGestion auto-mapea solo nombres de fábrica y el asistente pide el archivo antes de saber qué evento se va a aplicar.

---

## 3. Principio

**CSV = sesión Data Link (columnas custom). Evento = lo elige el operador (uno por carga). Lectura = perfil guardado.**

Una sesión = un tipo de evento + lecturas mapeadas con el perfil de ese tipo + cabecera coherente. No se confirma pesada y sanidad con el mismo archivo en la misma transacción.

---

## 4. Alcance

**Incluye**

- Perfiles de lectura por empresa y tipo de evento (CRUD). Alta desde un CSV de muestra.
- Asistente `/feedlot/manga`: evento → lecturas (con perfil) → lote → datos del evento → revisar y confirmar.
- Auto-mapeo por alias del perfil + fallback Data Link (`EID`, `VID`, `LID`, `Weight`, `Date`, `Time`, `Peso`, `Fecha`, `Hora`).
- Un archivo → un evento. Columnas de más: aviso e ignorar.
- Semáforo bloqueantes vs avisos; las mismas reglas en backend.
- EID duplicado: última lectura; aviso.
- Detección de export recortado (EID only) y de EID en notación científica (Excel).

**No incluye**

- Bluetooth, nube Datamars.
- Confirmar dos eventos con un solo CSV.
- BCS, preñez o draft como tipos de evento nuevos (se ignoran si no están mapeados).
- Mapear el nombre del tratamiento del cabezal al catálogo CORE salvo `insumo_id` opcional en el perfil.
- Hospital / movimiento de corral.
- Cambiar las RN-MG de la SPEC padre salvo las de coherencia de esta SPEC.

---

## 5. Perfil de lectura

Configuración previa (pantalla Feedlot, no en medio de la manga).

| Campo | Regla |
|-------|--------|
| Nombre | Obligatorio, único por empresa |
| Tipo de evento | `ENROLAMIENTO`, `PESADA`, `SANIDAD`, `RECUENTO`, `BAJA` |
| Predeterminado | A lo sumo uno por empresa y tipo |
| Alias por campo AG | Lista de encabezados (case-insensitive) |
| `insumo_id` | Opcional; solo SANIDAD |

Campos AG mapeables: EID, VID/caravana, fecha, hora, peso, dosis, lote de producto (batch), retiro (WHP), ESI, ignorar.

**RN de guardado**

- Identidad: al menos un alias de EID o de VID.
- PESADA: al menos un alias de peso.
- El resto de tipos no exige peso.

**Alta desde muestra:** el usuario sube un CSV, ve los encabezados reales y asigna a cada uno un campo AG o “ignorar”, más el tipo de evento del perfil.

Sin perfil: se usan los nombres Data Link de fábrica (comportamiento actual).

---

## 6. Flujo de usuario (carga)

```mermaid
flowchart LR
  evento[Paso1_evento]
  lecturas[Paso2_lecturas]
  lote[Paso3_lote]
  datos[Paso4_datos]
  revisar[Paso5_revisar]
  evento --> lecturas --> lote --> datos --> revisar
```

### Paso 1 — Qué evento

Tarjetas: Enrolar · Pesar · Aplicar producto · Contar · Dar de baja.  
Se carga el perfil predeterminado de ese tipo (o el usuario elige otro del mismo tipo). Enlace a “Configurar lecturas del bastón”.

### Paso 2 — Lecturas

Soltar CSV. El sistema aplica el perfil. Muestra cantidad de lecturas, fecha, rango de hora, pesos, EID repetidos, columnas ignoradas. Si faltan columnas obligatorias: bloqueante + mapeo visual o ir a editar el perfil.

Pesar sin peso en archivo: checkbox “cargar el kilo a mano”; si no, no avanza.

### Paso 3 — Lote

Enrolamiento: elige lote activo.  
Resto: sugerir lote por mayoría de EID activos. Mezcla: elección explícita. Mostrar plantel vs enrolados.

### Paso 4 — Datos del evento

Solo campos del tipo. Ver §7. Dosis/retiro del CSV pueden prellenar si el perfil los mapeó.

### Paso 5 — Revisar y confirmar

Preview de filas. Recuento: bloques de la SPEC de resolución. Frase única. Confirmar = transacción única (SPEC padre).

---

## 7. Cabecera por tipo

| Tipo | Datos que pide el asistente |
|------|-----------------------------|
| Enrolamiento | Sexo/categoría por defecto opcionales. Ajuste de plantel **solo** si el preview lo exige y el rol lo permite. |
| Pesada | Estadística min/max/promedio. Filas sin peso o fuera de rango en rojo. |
| Sanidad | ¿Hay producto? Buscador de insumo (prellenar si el perfil tiene `insumo_id`). Dosis por animal (prellenar si hay columna). Total = dosis × filas OK (editable). Días de retiro (editable; prellenar WHP). Quién aplicó. Origen del producto (defaults RN-MG-S07). Diagnóstico → sin insumo. |
| Recuento | Sin cabecera extra. |
| Baja | Primero qué pasó (murió / faena / venta en pie / descarte). **Sin** default silencioso a muerte. |

---

## 8. Validaciones (RN-CE)

Complementan RN-MG-E/P/S/R/B.

| ID | Check | Efecto |
|----|-------|--------|
| RN-CE-01 | EID no normalizable y sin caravana visual | Fila error |
| RN-CE-02 | EID duplicado en el archivo | Aviso; última lectura gana |
| RN-CE-03 | Animal no enrolado (pesada, sanidad, baja) | Fila error; no crear |
| RN-CE-04 | Animal de otro lote (pesada, sanidad, baja) | Fila error. Recuento: informe |
| RN-CE-05 | Animal no `ACTIVO` (pesada, sanidad, baja) | Fila error |
| RN-CE-06 | Pesada: peso nulo, ≤ 0 o fuera de 50–900 kg | Fila error |
| RN-CE-07 | Pesada: peso menor que la última del animal | Aviso de fila; no bloquea |
| RN-CE-08 | Sanidad vacuna/tratamiento + stock sin insumo, dosis o cantidad | No confirmar |
| RN-CE-09 | Stock insuficiente | No confirmar (RN-MG-S05) |
| RN-CE-10 | Faena con retiro vigente | Fila error; el resto puede confirmarse |
| RN-CE-11 | Enrolar más que plantel sin ajuste | No confirmar |
| RN-CE-12 | Fecha CSV vs fecha operativa > 1 día | Aviso de sesión |
| RN-CE-13 | 0 filas OK (salvo recuento) | No confirmar |
| RN-CE-14 | Baja sin subtipo elegido | No confirmar |
| RN-CE-15 | Baja de EID no enrolado | Fila error |
| RN-CE-16 | Perfil de un tipo usado en otro evento | No analizar / no confirmar |
| RN-CE-17 | Faltan columnas obligatorias del perfil/evento | No avanzar / no confirmar |
| RN-CE-18 | Más del 50% de EID no normalizables (p. ej. Excel) | No avanzar |
| RN-CE-19 | Encabezados no mapeados | Aviso; ignorar |
| RN-CE-20 | Guardar perfil PESADA sin peso, o perfil sin EID ni VID | No guardar |
| RN-CE-21 | Confirmar un CSV distinto al analizado | No confirmar (nombre + tamaño en la sesión) |

---

## 9. API (incremento)

| Método | Ruta | Uso |
|--------|------|-----|
| GET | `/api/feedlot/perfiles-lectura-manga` | Listar (`?tipo=` opcional) |
| POST | `/api/feedlot/perfiles-lectura-manga` | Crear |
| PUT | `/api/feedlot/perfiles-lectura-manga/{id}` | Actualizar |
| DELETE | `/api/feedlot/perfiles-lectura-manga/{id}` | Borrar |
| POST | `/api/feedlot/perfiles-lectura-manga/desde-muestra` | CSV → encabezados (no persiste) |
| POST | `/api/feedlot/sesiones-manga/analizar-archivo` | CSV + `perfilLecturaId` opcional + `tipo` opcional |

`mapeo` puede ir vacío. El resto de endpoints de sesión se mantiene.

---

## 10. Criterios de aceptación

- [ ] Un CSV `EID,Date,Time` (sin peso) permite enrolar, sanidad, recuento y baja; **no** pesada salvo grilla de kilos.
- [ ] Un CSV con `Weight` o con alias custom (`Peso vivo`) mapeado en perfil de pesada registra kilos sin índices de columna.
- [ ] Recuento sobre un CSV que también tiene peso: se ignora el peso (aviso) y se cuenta.
- [ ] Perfil PESADA no se guarda sin columna de peso.
- [ ] Perfil de SANIDAD no se puede elegir en una carga de PESADA.
- [ ] El asistente no muestra “columna 0 / 1 / 2” en el camino feliz.
- [ ] Sanidad no pide ID numérico de insumo; usa buscador.
- [ ] Baja no asume muerte hasta que el usuario elige qué pasó.
- [ ] Confirmar está deshabilitado si falta cabecera o hay 0 filas OK (excepto recuento).
- [ ] Duplicados: queda la última lectura y hay aviso.
- [ ] Peso 20 kg o 2000 kg es error de fila.
- [ ] Backend rechaza confirmar sanidad con stock empresa sin insumo aunque la UI se saltee.
- [ ] Recuento y resolución de desconocidos/faltantes siguen vigentes.
- [ ] Más del 50% de EID tipo `9.82E+14` bloquea el avance.

---

## 11. Aprobación

v1.0 aprobada el 14 de septiembre de 2026.  
v1.1 aprobada el 15 de septiembre de 2026 (perfiles de lectura, un evento por archivo, CSV completo Tru-Test).
