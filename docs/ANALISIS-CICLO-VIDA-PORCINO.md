# Análisis: Ciclo de vida del cerdo y cómo lo maneja el sistema

**Objetivo:** Describir el ciclo de vida del cerdo en el módulo Porcinos (reproductoras, lechones, recría, venta/faena o baja) y cómo el sistema lo modela con entidades, estados y automatismos.

---

## 1. Visión general del ciclo

El sistema distingue **dos ramas principales** del ciclo:

| Rama | Entidades principales | Fin de ciclo |
|------|------------------------|--------------|
| **Reproductora (madre)** | Madre → Servicio → Gestación → Parto → (Lactancia) → Destete → vuelve a Servicio… | Muerte/baja (MadreMuerte) o descarte (EstadoMadre.DESCARTE). |
| **Lechón / Recría** | Parto (nacidos vivos) → lactancia con madre → Destete → **Recría** (por lote) → venta, faena o muerte. | VentaPorcino (ENGORDE, REPRODUCTOR, FAENA), MuerteRecria, o cierre de recría por movimiento a terminación. |

El **padrillo** (macho reproductor) no sigue un “ciclo” en el mismo sentido: se da de alta, se usa en servicios (monta natural o IA) y se puede dar de baja (fechaBaja, motivoBaja, activo=false). No hay estados ni transiciones automáticas.

---

## 2. Ciclo de la madre (cerda reproductora)

### 2.1 Estados de la madre (EstadoMadre)

- **CACHORRA:** Hembra desde nacimiento hasta una edad configurable (por defecto 160 días, clave `DIAS_CACHORRA`). Se calcula por fecha de nacimiento en `MadreService.calcularEstadoSegunEdad`.
- **ADULTA:** Después de CACHORRA; disponible para servicio. También se usa como estado “vuelta a disponibilidad” tras destete, aborto o servicio fallido.
- **GESTACION:** Desde que se confirma preñez (control de celo positivo o gestación creada) hasta el parto.
- **LACTANCIA:** Desde el parto hasta el destete (o hasta que se cierra el parto).
- **RECRIA:** Estado disponible en el enum; en el flujo actual no se asigna automáticamente a madres (las reproductoras no pasan por “recría” como etapa productiva).
- **DESCARTE:** Baja definitiva; se asigna al registrar **MadreMuerte** (madre.setActivo(false), fechaBaja, motivoBaja).

**Regla:** Si la madre está en GESTACION o LACTANCIA, el sistema **no** recalcula estado por edad; el estado lo marcan los eventos (servicio, parto, destete, aborto, muerte).

### 2.2 Historial de estados

Cada cambio de estado se registra en **HistorialEstadoMadre**: madre, estado, fechaInicio, fechaFin (se cierra el registro anterior al cambiar). Lo centraliza `MadreService.actualizarEstadoMadre`, usado por ServicioService, PartoService, DesteteService, GestacionService y MadreMuerteService.

### 2.3 Flujo paso a paso

1. **Alta de madre**  
   - Madre creada con estado inicial (típicamente CACHORRA o ADULTA según edad).  
   - Configuración: identificación, fecha nacimiento, fecha ingreso granja, raza, ubicación, etc.

2. **Servicio (monta o IA)**  
   - **Validaciones:** Madre activa; no en GESTACION ni LACTANCIA; no en DESCARTE. Opcional: tiempo mínimo entre servicios (ParametrosProductivosPorcino).  
   - Se crea **Servicio** (madre, tipo MONTA_NATURAL/IA, fechaServicio, machoId/machoNombre, estado PENDIENTE_CONTROL, fechaControlCelo = fechaServicio + 21 días).  
   - **Automatismos:**  
     - Madre → **ADULTA** (si no lo estaba).  
     - Se crea **Gestacion** (madre, servicio, fechaInicio = fechaServicio, fechaProbableParto = + 115 días por defecto, estado EN_CURSO).  
     - Recordatorio para control de celo (21 días).  
     - Recordatorio para parto esperado.

3. **Control de celo (21 días)**  
   - **Preñez confirmada:** Servicio → PREÑEZ_CONFIRMADA; madre → **GESTACION**; gestación sigue EN_CURSO.  
   - **Fallido:** Servicio → FALLIDO; gestación asociada → ABORTO (si existía); madre → **ADULTA**. La madre puede recibir un nuevo servicio (siguiente intento).

4. **Gestación manual (aborto / finalizar)**  
   - **Registrar aborto:** Gestacion → ABORTO (fechaAborto, causaAborto); madre → **ADULTA**.  
   - **Finalizar gestación:** Gestacion → FINALIZADA (sin parto registrado en el sistema); según implementación puede o no cambiar madre.  
   - Los **recordatorios** de parto se crean al dar la gestación (Servicio o GestacionService).

5. **Parto**  
   - **Validaciones:** Madre en **GESTACION**; existe gestación activa (o última finalizada); no hay parto abierto para esa madre.  
   - Se crea **Parto** (madre, fechaInicio, nacidosVivos, nacidosMuertos, momias, totalNacidos, etc.).  
   - **Automatismos:**  
     - Madre → **LACTANCIA**.  
     - Gestación → FINALIZADA.  
     - Recordatorio para destete (según parámetros, ej. días lactancia).  
   - **No** se crea recría en el parto; los lechones “viven” en el parto/lactancia hasta el destete.

6. **Muerte de lechón (lactancia)**  
   - **MuerteLechon:** parto, madre, fecha, etapa (ej. NACIMIENTO, LACTANCIA), causa, cantidad. No cambia el estado de la madre ni crea recría; es solo registro.

7. **Destete**  
   - **Validaciones:** Un solo destete por parto; cantidad destetada ≤ nacidos vivos; peso al destete validado (ParametrosProductivosPorcino: pesoDesteteObjetivo ±50 % o rangos por defecto).  
   - Se crea **Destete** (parto, fechaDestete, cantidadDestetados, pesoPromedioDestete, días lactancia).  
   - **Automatismos:**  
     - Si el parto no tenía fechaFin, se cierra con la fecha del destete.  
     - Madre → **ADULTA** (vuelve a estar disponible para servicio).  
     - **Creación automática de Recría:** lote nuevo o existente vía `CrearLotePorcinoPort` (nombre sugerido: "Madre - dd/MM/yyyy" del parto), Recria con origen **DESTETE**, etapa F1, cantidadAnimales = cantidadDestetados, pesoPromedio/pesoInicialKg = peso al destete, fechaIngreso = fechaDestete.

8. **Muerte o baja de madre**  
   - **MadreMuerte:** madre, fecha, causa, etapaMomento (GESTACION, etc.), motivoBaja opcional.  
   - **Automatismos:** Madre → **DESCARTE**, activo=false, fechaBaja; cierre de gestación activa, servicios pendientes y partos abiertos.

Con esto, el ciclo reproductivo queda: **ADULTA → Servicio → (control celo) → GESTACION → Parto → LACTANCIA → Destete → ADULTA** (y se repite), hasta **DESCARTE** por muerte/baja.

---

## 3. Ciclo del lechón y recría

### 3.1 Origen de la recría

- **DESTETE:** Recría creada automáticamente al registrar el destete (mismo lote sugerido por madre + fecha parto, cantidad y peso del destete).  
- **EXTERNO:** Recría creada manualmente (ingreso de animales comprados o ingresados por otro motivo). En alta manual se exige lote, cantidad, peso; opcional etapa (por defecto F1), sexo, destino.

### 3.2 Etapas de recría (EtapaRecria)

- **F1, F2, F3, F4, DESARROLLO, TERMINACION.**  
- Se usan para alimentación (receta por etapa) y para movimientos entre etapas/lotes.  
- La etapa se puede asignar al crear la recría o cambiar con **MovimientoEtapa** (mover animales a otro lote/etapa).

### 3.3 Eventos durante la recría

- **RegistroPeso:** pesadas por recría; actualizan pesoPromedio y fechaUltimaPesada (y opcionalmente pesoIndividual). No cambian etapa ni cantidad.  
- **MuerteRecria:** fecha, causa, cantidad, recría. **No** se descuenta de `Recria.cantidadAnimales`; los “animales disponibles” se calculan como cantidadAnimales − Σ(muertes) − Σ(ventas de esa recría).  
- **MovimientoEtapa:** mover N animales de una recría (origen) a etapa/lote destino. **Sí** actualiza cantidades: recría origen pierde N (y se cierra si queda 0); en lote destino se crea una nueva Recria o se suma a una existente del mismo lote y etapa.  
- **VentaPorcino (ENGORDE, REPRODUCTOR, FAENA):** **Sí** descuenta de `Recria.cantidadAnimales`. Si la recría queda en 0, se setea fechaSalida y destino VENTA. Para FAENA se exige recría asociada y parámetros de establecimiento (realizaFaena).

### 3.4 Cierre y destino de recría

- **FechaSalida** y **DestinoRecria** (VENTA, FUTURA_MADRE, ENGORDE) se setean cuando:  
  - Se venden/faenan todos los animales (VentaPorcinoService), o  
  - Se mueven todos a otra etapa/lote (MovimientoEtapaService, destino ENGORDE/terminación).  
- No hay un “estado” de recría (activo/inactivo); se usa `activo=true` y la existencia de fechaSalida para considerar la recría cerrada.

### 3.5 Trazabilidad de “animales disponibles”

- **VentaPorcinoService** y **MuerteRecriaService** calculan “animales disponibles” como:  
  `cantidadAnimales - Σ(muertes de esa recría) - Σ(ventas de esa recría)`.  
- Al registrar una **venta**, además se hace `recria.setCantidadAnimales(cantidadAnimales - cantidadVendida)`. Por tanto, la cantidad almacenada en Recria se reduce con cada venta; las muertes no la modifican. Cualquier lógica que combine cantidadAnimales con “ventas previas” debe evitar descontar dos veces las ventas (posible punto de confusión o bug si se mezclan ambos criterios).

---

## 4. Padrillo (macho reproductor)

- **Alta:** Padrillo con identificacion, fechaNacimiento, origen (EXTERNA/INTERNA), fechaIngresoGranja, raza, proveedorGenetica, ubicacionInterna.  
- **Uso:** Se referencia en **Servicio** (machoId, machoNombre) para monta natural o IA. No hay ciclo de estados; solo se valida que esté activo y de la empresa.  
- **Baja:** fechaBaja, motivoBaja (catálogo), activo=false. No hay entidad “PadrilloMuerte”; la baja es administrativa.

---

## 5. Transferencias de lechones

- **TransferenciaLechon:** lechones pasan de un parto/madre origen a otro parto/madre destino (cantidad, fecha, motivo).  
- Solo registro; no crea ni modifica recrías ni descuenta “nacidos vivos” del parto origen de forma automática. Útil para reflejar nodrizas o corrección de camada.

---

## 6. Resumen: dónde se modela cada fase

| Fase del ciclo | Entidad / concepto | Quién lo crea / actualiza |
|----------------|--------------------|----------------------------|
| Madre disponible | Madre.estadoActual = ADULTA (o CACHORRA por edad) | MadreService (edad), ServicioService (tras servicio), DesteteService (tras destete), GestacionService (tras aborto). |
| Servicio / preñez | Servicio, Gestacion | ServicioService (alta servicio + gestación automática); control celo confirma preñez o fallido. |
| Gestación | Gestacion (EN_CURSO, ABORTO, FINALIZADA) | ServicioService, GestacionService (aborto/finalizar), PartoService (finalizar al parir). |
| Parto | Parto | PartoService; cierra gestación y pasa madre a LACTANCIA. |
| Lactancia | Madre LACTANCIA; Parto sin destete | PartoService; DesteteService cierra lactancia (madre → ADULTA). |
| Muertes lechón | MuerteLechon | MuerteLechonService; no cambia parto ni madre. |
| Destete | Destete | DesteteService; crea Recria (origen DESTETE) y cierra parto. |
| Recría (lote/corral) | Recria (origen DESTETE o EXTERNO, etapa F1–TERMINACION) | DesteteService (automático), RecriaService (alta manual), MovimientoEtapaService (crear/actualizar recría destino). |
| Pesas en recría | RegistroPeso | RegistroPesoService; actualiza peso y fecha última pesada. |
| Muertes en recría | MuerteRecria | MuerteRecriaService; no modifica cantidadAnimales. |
| Movimiento entre etapas/lotes | MovimientoEtapa | MovimientoEtapaService; descuenta en origen, crea o suma en destino. |
| Venta / Faena | VentaPorcino (ENGORDE, REPRODUCTOR, FAENA) | VentaPorcinoService; descuenta cantidadAnimales, cierra recría si queda 0. |
| Baja madre | MadreMuerte, Madre.DESCARTE, activo=false | MadreMuerteService; cierra gestación, servicios y partos abiertos. |
| Baja padrillo | Padrillo.fechaBaja, activo=false | PadrilloService (baja administrativa). |

---

## 7. Puntos débiles y observaciones

- **Recría.origen:** El campo `origen` (DESTETE/EXTERNO) en la entidad está como `@Transient`; existe migración que agrega columna en BD. Si se quiere persistir y filtrar por origen, conviene mapear la columna.  
- **Animales disponibles:** Las muertes no restan de `Recria.cantidadAnimales`; las ventas sí. El cálculo “disponibles = cantidad − muertes − ventas” debe usarse con cuidado para no duplicar el descuento de ventas si en algún flujo también se actualiza cantidadAnimales.  
- **Un parto, un destete:** La regla “un solo destete por parto” está solo en validación de DesteteService; no hay restricción única en BD (parto_id en destetes podría permitir varios si se saltara la capa de servicio).  
- **Transferencia de lechones:** No actualiza nacidos vivos ni cantidades en partos; es solo trazabilidad. Si se quisiera consistencia numérica (ej. “nacidos vivos − transferidos out = en lactancia”), habría que añadir lógica.  
- **Máquina de estados de la madre:** Las transiciones (CACHORRA↔ADULTA por edad; ADULTA→GESTACION→LACTANCIA→ADULTA por eventos) están implementadas en varios servicios pero no documentadas en un único lugar (enum de transiciones permitidas o diagrama). Sería útil centralizar y documentar las transiciones válidas.  
- **Padrillo:** No hay “estado” ni historial; solo activo y fecha/motivo de baja. Si en el futuro se requiere trazabilidad de uso o estado reproductivo, habría que extender el modelo.

Con esto queda descrito el ciclo de vida del cerdo tal como lo maneja el sistema hoy y los principales puntos a tener en cuenta para mantener o extender la lógica.
