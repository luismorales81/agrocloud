# SPEC — Economía de la empresa y resultados por módulo

**Versión:** 1.0  
**Fecha:** Octubre 2026  
**Metodología:** SDD  
**Estado:** Aprobada  
**Fecha aprobación:** Octubre 2026  
**Diseño:** `DISENO-TECNICO-ECONOMIA-EMPRESA.md`  
**Referencia:** plan «Economía compartida, resultados por módulo»  
**Relacionada con:** `docs/CORE-Y-MODULOS.md`, `SPEC-MODULO-FEEDLOT.md`, `SPEC-MODULO-PORCINOS-V2.md`, `SPEC-MODULO-LECHERIA.md`, `SPEC-CAMPANA-TRANSVERSAL.md`

No se genera código ni diseño técnico hasta la aprobación explícita de esta SPEC.

---

## 1. Objetivo

Unificar el dinero y el stock de una empresa que puede tener varios módulos productivos activos (cultivos, feedlot, porcinos, lechería, avícola).

- El **stock de insumos** y el **libro de ingresos y egresos** pertenecen a la empresa. No se duplican por módulo.
- Cada módulo conserva el **resultado de su ciclo** (margen del lote, closeout, venta de leche, venta de animales, cosecha) como cálculo y como hecho propio.
- Ese hecho, cuando mueve dinero, deja **una sola línea** en el libro compartido.
- Desde insumos se puede **dar de alta** un insumo y **reponer** uno ya cargado. La reposición por compra genera el egreso y suma stock por `InventoryService`, en la misma operación.

Posicionamiento: libro operativo de la explotación. No es contabilidad formal (no hay plan de cuentas, IVA, asientos ni libros fiscales).

---

## 2. Glosario

| Término | Definición |
|---------|------------|
| **Empresa** | Titular del stock y del libro. Varios módulos pueden estar habilitados a la vez. |
| **Libro** | Conjunto de ingresos y egresos de la empresa. Hoy las tablas son `cultivo_ingresos` y `cultivo_egresos`; el nombre histórico no implica que el dinero sea solo de cultivos. |
| **Hecho productivo** | Evento del módulo que explica una línea del libro: labor, cosecha, venta de feedlot, venta de porcinos, venta de leche, venta avícola, venta de grano. |
| **Resultado del módulo** | Margen calculado del ciclo (closeout, lote, cosecha). Es una vista. No es otro libro. |
| **Compra de insumo** | Egreso que repone stock de un insumo ya existente, o la primera carga de stock de un insumo recién dado de alta. |
| **Reposición por anulación** | Devolución de stock al deshacer un consumo. No es una compra y no genera egreso. Ya existe como `InventoryService.reponer`. |
| **Origen de inventario** | `InventoryOrigin`: `CULTIVOS`, `PORCINOS`, `FEEDLOT`, `LECHERIA`, `AVICOLA_CRIANZA`, `AVICOLA_CARNE`, `AVICOLA_HUEVOS`, `AVICOLA_PONEDORAS`. |
| **Módulo de la línea** | Módulo desde el cual se registró el ingreso o el egreso. Sirve para filtrar la pantalla. No parte el saldo de stock. |
| **Imputación** | `COMPARTIDA` o `MODULO`. Una compra de insumo es compartida: el stock lo puede consumir cualquier módulo. Un gasto directo del ciclo (mano de obra, alquiler, hotelería, servicio) se imputa al módulo. |
| **Costo unitario** | Precio de stock del insumo, usado para valuar consumos en el resultado del módulo. |

---

## 3. Decisión de alcance

### 3.1 Dentro de esta SPEC

1. Libro único por empresa, con módulo de la línea, imputación y referencia al hecho.
2. Alta de insumo (catálogo, stock inicial cero salvo que la misma operación sea una compra).
3. Reposición por compra de un insumo ya cargado: egreso + `InventoryService.reponer` en una transacción.
4. Pantalla de finanzas en cada módulo: mismas líneas del libro, filtradas.
5. Vista de empresa (el Balance existente) que suma todos los módulos.
6. Regla para no contar dos veces el mismo insumo (compra en el libro; consumo solo en el resultado del ciclo).

### 3.2 Fuera de alcance

- Libros, tablas o stocks separados por módulo (`finanzas_feedlot`, `finanzas_porcinos`, etc.).
- Contabilidad impositiva, cuentas corrientes de clientes y proveedores, cheques, medios de pago.
- Multimoneda. Se mantiene la moneda ya configurada en la empresa.
- Cambiar las fórmulas de closeout, GMD o conversión. Solo se aclara qué números de ese resultado son dinero del libro y cuáles son valuación de stock.
- Partir el saldo de un insumo según quién lo consume.
- Migrar de nombre las tablas `cultivo_ingresos` / `cultivo_egresos` en esta versión. El diseño técnico puede proponer columnas nuevas sobre esas tablas.

---

## 4. Estado actual (hechos del código)

| Pieza | Comportamiento hoy | Brecha |
|-------|--------------------|--------|
| `Insumo` + `InventoryService` | Un saldo por insumo y empresa. `consumir` y `reponer` exigen `InventoryOrigin`. | `reponer` devuelve stock (anulación). No representa una compra ni toca el libro. |
| Pantalla de insumos | `InsumosUnificados` lista y edita el catálogo. Feedlot, porcinos y avícola reutilizan esa pantalla. | No hay acción «Reponer». |
| `FinanzasManagement` | Solo está en el menú de cultivos (`/cultivos/finanzas`). El egreso de insumo suma `stockActual` en el cliente, llamando al alta o a la actualización del insumo. | No pasa por `InventoryService`. Puede crear el insumo como efecto del egreso. |
| `Ingreso` / `Egreso` | Entidades en el paquete core. El egreso puede apuntar a un `Plot` (lote de cultivo). Tipos de egreso: `INSUMO`, `INSUMOS`, `MAQUINARIA_COMPRA`, `MAQUINARIA_ALQUILER`, `SERVICIO`, `OTROS`. Tipos de ingreso: `VENTA_CULTIVO`, `VENTA_ANIMAL`, `SERVICIOS_AGRICOLAS`, `SUBSIDIOS`, `OTROS_INGRESOS`. Hay `campana_id`. No hay `empresa_id` ni módulo. | El balance agrega por usuario, no por empresa. Un lote de cultivo no sirve como referencia de una venta de feedlot. |
| Ventas de módulo | `feedlot_venta`, `porcinos` venta y venta avícola tienen columna `ingreso_id`. | Ningún servicio crea el `Ingreso` ni completa `ingreso_id` al registrar la venta. |
| Venta de grano | `InventarioGranoService` sí persiste un `Ingreso`. | Es el único hecho productivo que ya escribe el libro. |
| Closeout feedlot / lechería | Calcula margen del ciclo con costos e ingresos del módulo. | Ese margen no es el libro de caja. Mezclarlos duplicaría el alimento ya comprado. |

---

## 5. Modelo de negocio

```text
Compra de insumo  -->  egreso en el libro  +  entrada de stock (origen = módulo que carga)
Consumo del ciclo -->  baja de stock (origen = módulo que consume)  +  costo en el resultado del módulo
                       NO genera otro egreso
Venta del ciclo   -->  hecho del módulo  +  un ingreso en el libro (ingreso_id)
Gasto directo     -->  egreso imputado al módulo (mano de obra, alquiler, servicio, hotelería)
                       sin movimiento de stock
```

El resultado del módulo responde: «¿este lote o este ciclo dejó margen?».  
El libro responde: «¿cuánto dinero entró y salió en la empresa?».  
El Balance de empresa es la suma del libro. No es la suma de los closeouts.

---

## 6. Reglas de negocio

### 6.1 Stock

| Regla | Detalle |
|-------|---------|
| RN-S01 | Hay un solo saldo por insumo y empresa. El módulo no tiene depósito propio. |
| RN-S02 | Toda entrada o salida de stock pasa por `InventoryService`. Queda prohibido sumar o restar `stockActual` desde la pantalla de finanzas o desde un controlador de módulo. |
| RN-S03 | El alta de catálogo crea el insumo con stock cero. No es un egreso. |
| RN-S04 | La compra de un insumo ya existente exige insumo de la misma empresa, cantidad > 0, costo total > 0, fecha y módulo de la línea. |
| RN-S05 | La compra, en una sola transacción: persiste el egreso, llama a `reponer` con `referenciaId` = id del egreso y origen = módulo que carga, y guarda ese id en el egreso. Si `reponer` falla, no queda el egreso. |
| RN-S06 | Se puede dar de alta el insumo y registrar la primera compra en la misma acción de usuario. Siguen siendo dos pasos de negocio: catálogo y luego RN-S05. Si la compra falla, el insumo puede quedar con stock cero. |
| RN-S07 | Anular o cancelar una compra deshace el stock con una salida por `InventoryService` referenciada al mismo egreso. Si el saldo no alcanza (el insumo ya se consumió), la anulación se rechaza. No se permite stock negativo por esta vía. |
| RN-S08 | `reponer` usado al anular un consumo (labor, sanidad, sesión) no crea egreso ni ingreso. |
| RN-S09 | El costo unitario del insumo pasa a ser el promedio ponderado: `(saldo anterior × costo anterior + costo total de la compra) / saldo nuevo`. Si el saldo anterior es cero o el costo anterior es nulo, el costo unitario es `costo total / cantidad`. |
| RN-S10 | Una corrección manual del saldo, si se conserva en la pantalla de insumos, no es una compra: no crea egreso y queda registrada como ajuste, con origen. No se usa para «reponer» mercadería pagada. |

### 6.2 Libro

| Regla | Detalle |
|-------|---------|
| RN-L01 | Una empresa tiene un solo libro. Los módulos leen y agregan líneas; no tienen tablas propias de ingresos y egresos. |
| RN-L02 | Cada línea nueva guarda empresa, módulo de la línea, imputación (`COMPARTIDA` o `MODULO`), tipo de referencia y id de referencia. La referencia es genérica (no solo lote de cultivo). |
| RN-L03 | `lote_id` sigue siendo opcional y solo válido para hechos de cultivos. Una venta de feedlot, porcinos, leche o avícola no exige lote de cultivo. |
| RN-L04 | La compra de insumo es imputación `COMPARTIDA`. El módulo de la línea indica quién la cargó, no el dueño del stock. |
| RN-L05 | Mano de obra, alquiler de maquinaria, servicio, hotelería y cualquier gasto que no reponga stock es imputación `MODULO`. |
| RN-L06 | El consumo de un insumo ya comprado no genera egreso. El resultado del módulo lo valúa con el costo unitario vigente (RN-S09) por la cantidad consumida. |
| RN-L07 | Una venta productiva genera como máximo un ingreso. Si el hecho ya tiene `ingreso_id`, no se crea otro. |
| RN-L08 | Anular la venta anula o marca cancelado el ingreso vinculado y limpia el vínculo, en la misma transacción. No se borra la historia: el ingreso queda `CANCELADO` y `activo = false`, igual que el criterio actual de estados. |
| RN-L09 | Los tipos de ingreso existentes se mantienen. Se agrega `VENTA_LECHE` para la venta de leche. `VENTA_ANIMAL` cubre feedlot, porcinos y avícola. `VENTA_CULTIVO` cubre cosecha y grano. |
| RN-L10 | Los tipos de egreso `INSUMO` e `INSUMOS` quedan unificados en el uso nuevo como compra de insumo (`INSUMO`). `INSUMOS` se acepta solo en datos viejos. |
| RN-L11 | Las líneas ya cargadas quedan en la empresa del usuario dueño. Su módulo de la línea es `CULTIVOS` si tienen lote o tipo de ingreso de cultivo; si no, `CULTIVOS` igualmente, porque la pantalla histórica es la de cultivos. La imputación de un egreso viejo de tipo insumo es `COMPARTIDA`; el resto, `MODULO`. No se reescribe el stock histórico. |
| RN-L12 | El período de gestión (`campana_id`) sigue siendo opcional en la línea y, cuando el hecho tiene período, la línea copia ese período. |
| RN-L13 | El alta manual sigue disponible para subsidios, servicios agrícolas, otros ingresos y gastos que no nacen de un hecho. Debe elegir módulo de la línea e imputación. No puede elegir tipo compra de insumo: esa entrada es solo RN-S05, para no volver a tocar el stock por fuera de `InventoryService`. |

### 6.3 Hechos que escriben el libro

| Hecho | Línea | Cuándo |
|-------|--------|--------|
| Compra / reposición de insumo | Egreso `INSUMO`, imputación `COMPARTIDA`, referencia = insumo y egreso como `referenciaId` del movimiento | Al confirmar la compra |
| Venta de feedlot, porcinos o avícola | Ingreso `VENTA_ANIMAL`, imputación `MODULO`, referencia = id de la venta | Al confirmar la venta, si el total es mayor que cero |
| Venta de leche | Ingreso `VENTA_LECHE`, imputación `MODULO`, referencia = id de la venta | Al confirmar la venta, si el importe es mayor que cero |
| Venta de grano ya implementada | Ingreso `VENTA_CULTIVO`, imputación `MODULO`, referencia = movimiento de grano | Se adapta para completar empresa, módulo `CULTIVOS` y la referencia. No se duplica el ingreso que ya crea `InventarioGranoService`. |
| Cosecha de cultivos | No genera ingreso por el solo hecho de cosechar | El ingreso nace cuando se vende el grano o cuando el usuario registra una venta manual `VENTA_CULTIVO` |
| Labor (insumos) | No genera egreso | El costo de insumos de la labor es valuación de stock en el resultado del lote |
| Labor (mano de obra o alquiler ya pactado) | Egreso imputación `MODULO`, referencia = labor, solo si el usuario confirma el gasto en esa labor | No es automático en esta versión: ver pregunta P2 |
| Closeout | No escribe el libro | Sigue siendo el resultado del ciclo. Puede mostrar el ingreso ya vinculado si la venta tiene `ingreso_id` |

### 6.4 Pantallas

| Regla | Detalle |
|-------|---------|
| RN-P01 | Cada módulo productivo muestra «Finanzas» con el mismo libro filtrado: líneas cuyo módulo de la línea es ese módulo, más las compras `COMPARTIDA` (para ver de qué stock se dispone el dinero ya gastado). |
| RN-P02 | El resultado del módulo (margen, closeout) no se mezcla en la misma grilla del libro. Se muestra aparte, como hoy el closeout, con una leyenda de que el alimento consumido está valuado y la compra ya está en el libro. |
| RN-P03 | El Balance de empresa lista y suma todas las líneas de la empresa, sin filtrar por módulo. Los filtros opcionales son módulo, imputación y período. |
| RN-P04 | En insumos, la acción «Reponer» abre la compra (RN-S04 / RN-S05) del insumo elegido. «Nuevo insumo» abre el alta de catálogo (RN-S03) y permite indicar una primera compra. |
| RN-P05 | La pantalla de finanzas de cultivos deja de ser el único lugar donde se compra stock. El egreso de insumo que hoy suma stock en el navegador se reemplaza por RN-S05. |
| RN-P06 | Feedlot, porcinos, lechería, avícola y cultivos muestran el ítem Finanzas. No se crean pantallas de finanzas distintas por módulo: es la misma vista con el filtro del módulo activo. |

### 6.5 Permisos

| Regla | Detalle |
|-------|---------|
| RN-A01 | Ver el libro del módulo usa el permiso ya existente `canViewFinances`. |
| RN-A02 | Cargar, anular compras e ingresos manuales usa el mismo permiso con el que hoy se crea un egreso o un ingreso. El diseño técnico no inventa un permiso nuevo salvo que el de escritura no exista separado del de lectura. |
| RN-A03 | Reponer exige, además, permiso de insumos (`canViewInsumos` no alcanza para escribir: se usa el permiso con el que hoy se actualiza un insumo). |
| RN-A04 | El módulo de la línea tiene que estar habilitado en la empresa. No se imputa un egreso a un módulo apagado. |

---

## 7. Criterios de aceptación

1. Con cultivos y feedlot habilitados, una compra de maíz hecha desde feedlot aumenta un solo saldo. Cultivos puede consumir ese maíz en una labor sin registrar otra compra.
2. Esa compra aparece una vez en el libro de la empresa y una vez en la grilla de finanzas de cualquier módulo (porque es `COMPARTIDA`). No aparece como egreso de nuevo cuando la labor o la dieta lo consumen.
3. El closeout del lote de feedlot sigue valuando el alimento consumido. Ese importe no se suma otra vez al Balance como egreso.
4. Registrar una venta de feedlot, porcinos, avícola o leche con total mayor que cero crea un ingreso y guarda `ingreso_id`. Repetir el guardado de la misma venta no crea un segundo ingreso.
5. Anular esa venta deja el ingreso cancelado y el vínculo vacío o apuntando al ingreso cancelado, sin un ingreso activo duplicado.
6. Anular una compra con saldo suficiente devuelve el stock al valor anterior y cancela el egreso. Anularla sin saldo suficiente no modifica stock ni egreso.
7. Después de dos compras del mismo insumo, el costo unitario es el promedio ponderado definido en RN-S09.
8. El Balance de la empresa incluye líneas de cultivos y de los otros módulos. Filtrar por módulo muestra solo las de ese módulo más, en la pantalla del módulo, las compartidas (RN-P01).
9. No existe tabla nueva de finanzas por módulo.
10. Los egresos e ingresos ya guardados siguen listándose en finanzas de cultivos después de la migración de RN-L11.

---

## 8. Reglas que esta SPEC cierra con un defecto propuesto

Quedan abiertas solo si el aprobador las cambia. Si se aprueba la SPEC sin cambios, valen estos defectos.

| Id | Tema | Defecto de esta SPEC |
|----|------|----------------------|
| P1 | Costo unitario | Promedio ponderado (RN-S09), no el último precio. |
| P2 | Gasto de labor | No se genera solo. El usuario registra el egreso de mano de obra o alquiler y lo vincula a la labor. El costo de insumos de la labor no es egreso (RN-L06). |
| P3 | Cosecha | Cosechar no crea ingreso. Lo crea la venta de grano o un ingreso manual `VENTA_CULTIVO`. |
| P4 | Compras compartidas en la grilla del módulo | Se ven en todos los módulos (RN-P01) y no entran al margen del ciclo. |
| P5 | Alcance del Balance | Pasa de «por usuario» a «por empresa». Las líneas viejas se atribuyen a la empresa del usuario dueño (RN-L11). |
| P6 | Anulación de compra | Se rechaza si el stock no alcanza (RN-S07). No hay stock negativo por anulación de compra. |
| P7 | Venta con total cero o nulo | No crea ingreso. |

---

## 9. Riesgos de arquitectura

| Riesgo | Por qué importa | Mitigación en el diseño técnico (aún no se escribe) |
|--------|-----------------|------------------------------------------------------|
| Doble conteo | El Balance suma egresos de compra y el closeout vuelve a mostrar el alimento. Un tablero que sume ambos infla el costo. | El Balance solo lee el libro. El closeout se etiqueta como resultado del ciclo, no como caja. |
| Stock tocado por fuera de `InventoryService` | `FinanzasManagement` hoy suma `stockActual` directo. Cualquier camino viejo que siga vivo desalinea saldo y libro. | Un solo caso de uso de compra. Quitar la suma de stock del cliente. |
| `reponer` significa dos cosas | Hoy es «devolver un consumo». La compra también entra stock. | La compra llama a `reponer` con referencia al egreso. La anulación de consumo sigue sin egreso (RN-S08). El diseño técnico puede separar el nombre interno si hace falta, sin dos saldos. |
| Libro atado a `Plot` | `Egreso` e `Ingreso` tienen relación con lote de cultivo. Forzar esa relación en feedlot o leche rompe el alta. | Referencia genérica. `lote_id` queda opcional y solo cultivos. |
| Balance por usuario | Varios usuarios de la misma empresa verían cajas distintas. Contradice el libro único. | RN-L11 y P5. Hay que revisar consultas de `BalanceService`, `DashboardService` y `AdminDashboardService` en el diseño, sin cambiar su contrato hasta esa aprobación. |
| `ingreso_id` huérfano | La columna existe y nadie la escribe. Completarla a partir de esta SPEC cambia el significado de una venta ya guardada: las ventas viejas siguen sin ingreso. | No se inventan ingresos retroactivos. Solo las ventas nuevas (y las que el usuario reconfirme) escriben el libro. |
| Promedio ponderado con stock negativo previo | Porcinos puede consumir en negativo. El promedio con saldo negativo no es interpretable. | RN-S09: si el saldo anterior es menor o igual a cero, el costo unitario de la compra reemplaza al anterior. |
| Empresa nula en insumos viejos | `reponer` rechaza el insumo sin empresa. | La compra falla con error claro. No se reasigna empresa en silencio. |
| Permisos distintos por módulo | Un usuario con finanzas en cultivos y sin finanzas en feedlot. | RN-A01 se evalúa igual en todos los módulos. Si en algún módulo el permiso no está asignado, la pantalla no se muestra; el libro de la empresa no se parte. |
| Nombres de tablas `cultivo_*` | Invitan a tratar el libro como del módulo cultivos. | No se renombran tablas en esta versión. La SPEC manda el comportamiento; el diseño técnico decide si agrega columnas. |

---

## 10. Preguntas de clarificación

Si la respuesta es el defecto de la sección 8, alcanza con aprobar la SPEC.

1. ¿El costo del insumo es el promedio ponderado (P1) o el último precio de compra?
2. ¿La mano de obra y el alquiler de una labor generan el egreso solos o solo cuando el usuario lo confirma (P2)?
3. ¿Registrar la cosecha debe crear el ingreso, o el ingreso nace recién al vender (P3)?
4. ¿Las compras de stock se listan dentro de las finanzas de cada módulo (P4) o solo en el Balance de la empresa?
5. ¿El Balance deja de ser por usuario y pasa a ser por empresa (P5)?

---

## 11. Qué no autoriza esta SPEC

- No autoriza código, migraciones ni cambios de pantalla.
- No autoriza el diseño técnico. Ese documento se propone después de la aprobación explícita y se aprueba aparte.
- No reemplaza los SPEC de cada módulo. Esos siguen dueños del hecho (venta, labor, cosecha, closeout). Esta SPEC es dueña del libro, de la compra de insumo y de la regla anti doble conteo.
