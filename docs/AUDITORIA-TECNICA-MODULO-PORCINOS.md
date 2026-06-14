# Auditoría técnica del módulo Porcinos

**Fecha:** Enero 2025  
**Alcance:** Backend (`agrogestion-backend/`), Frontend (`agrogestion-frontend/`), modelo de dominio, servicios, controladores, integraciones y multitenant.  
**Objetivo:** Análisis del estado actual, detección de problemas de arquitectura, inconsistencias, deuda técnica y riesgos; solo análisis y recomendaciones, sin modificación de código.

---

## 1. Estado actual del módulo

### 1.1 Funcionalidades existentes

| Área | Funcionalidad | Backend | Frontend | Observación |
|------|---------------|---------|----------|-------------|
| **Reproductores** | Madres (CRUD, historial, muerte) | MadreController, MadreService | MadresListScreen, MadreCreateScreen, MadreDetailScreen, MadreHistoryScreen | Flujo completo |
| | Padrillos (CRUD) | PadrilloController, PadrilloService | PadrillosListScreen, PadrilloCreateScreen | Flujo completo |
| **Ciclo reproductivo** | Servicios (monta/IA, control celo) | ServicioController, ServicioService | ServiciosListScreen, ServicioCreateScreen, ControlCeloScreen | Flujo completo |
| | Gestación (alta, aborto, finalizar, chequeos) | GestacionController, GestacionService, ChequeoGestacionService | GestacionPartosScreen, GestacionDetailScreen, GestacionListScreen | Flujo completo |
| | Partos (registro, muertes lechón) | PartoController, PartoService | PartosListScreen, PartoCreateScreen, PartoDetailScreen | Flujo completo |
| | Destetes (registro, crea recría automática) | DesteteController, DesteteService | DestetesListScreen, DesteteCreateScreen, DesteteDetailScreen | Flujo completo; recría automática por CrearLotePorcinoPort |
| | Transferencias de lechones | TransferenciaLechonController, TransferenciaLechonService | TransferenciasScreen | Implementado |
| **Recría** | CRUD, ingreso externo, muertes, pesos, movimientos etapa | RecriaController, RecriaService, RegistroPesoService, MovimientoEtapaService, MuerteRecriaService | RecriaListScreen, RecriaIngresoScreen, RecriaDetailScreen | Origen DESTETE/EXTERNO; campo `origen` en entidad es @Transient pero existe migración V1_120 que agrega columna (ver §2) |
| **Alimentación** | Fórmulas por etapa, insumos compuestos, consumos, consumo diario automático | CalendarioAlimentacionController, ConsumoAlimentoController, ConsumoDiarioAutomaticoService, InsumoCompuestoController | FormulaListScreen, FormulaCreateScreen, InsumosCompuestosScreen, ConsumosScreen, HistorialConsumosScreen | Job @Scheduled 00:30 para consumo día anterior; integración con core.inventory |
| **Ventas y faena** | Ventas, faena (unificado en VentaPorcino) | VentaPorcinoController, FaenaController, VentaPorcinoService, FaenaService | VentasScreen, FaenaListScreen, FaenaCreateScreen | V1_109 unificó Faena en VentaPorcino |
| **Sanidad** | Eventos sanitarios (con retiro de insumos) | EventoSanitarioController, EventoSanitarioService | EventosSanitariosScreen | Descuento/reposición vía InventarioPorcinoService → InventoryService |
| **Inventario** | Vista inventario porcinos | (delega en core inventory / catalogos) | InventarioPorcinosScreen | InventarioPorcinoService usa core.inventory |
| **Configuración** | Parámetros, configuraciones, catálogos | ParametrosPorcinoController, ConfiguracionPorcinoController, CatalogosPorcinoController | ConfiguracionesScreen, ConfiguracionUnificadaScreen | Catálogos: razas, tipos servicio, causas mortalidad, motivos baja, esquemas sanitarios, tipos parto, tipos evento sanitario, ubicaciones internas |
| **Reportes** | Reproductivo, mortalidad, productivo, alimentación, económico, inventario, sanitario, ventas (+ Excel) | ReportesPorcinoController, ReportesPorcinoService | ReportesPorcinosScreen | Múltiples endpoints y export Excel |
| **Dashboard** | KPIs y alertas | PorcinosDashboardController, DashboardPorcinosService, AlertasPorcinosService | DashboardPorcinosScreen, CalendarioPorcinosDashboard | Calendario agrega eventos porcinos (parto, servicio, gestación) |

### 1.2 Flujos implementados

- **Ciclo reproductivo:** Madre → Servicio → Gestación → Parto → (MuerteLechon opcional) → Destete → creación automática de Recría (con lote vía CrearLotePorcinoPort) y actualización de estado madre a ADULTA.
- **Recría:** Ingreso externo (RecriaService.crearRecria) o por destete (DesteteService.crearRecriaDesdeDestete). Registro de pesos, muertes, movimientos de etapa, venta/faena.
- **Alimentación:** Recetas por etapa → consumo diario automático (job) que descuenta Insumo/InsumoCompuesto/Grano vía core.inventory y registra MovimientoStockPorcino (trazabilidad).
- **Multitenant:** Todas las entidades de negocio porcinos tienen `empresa_id`; los controladores obtienen empresa desde `user.getEmpresa()` o `EmpresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId())` y filtran/validan por empresa.

### 1.3 Partes incompletas o frágiles

- **Validación en API:** No se usa `@Valid` / Bean Validation en los controladores del módulo Porcinos; las validaciones están en servicios vía `IllegalArgumentException`. Los request bodies no tienen DTOs con anotaciones de validación.
- **DTOs:** No hay DTOs específicos del módulo Porcinos en backend para request/response; se exponen entidades JPA (Madre, Parto, Recria, etc.) con `@JsonIgnore`/`@JsonProperty` en entidades. Riesgo de sobre-exposición, acoplamiento y cambios de contrato al evolucionar el modelo.
- **Recria.origen:** La migración V1_120 agrega la columna `origen` a `porcinos_recria`, pero en la entidad `Recria` el campo `origen` está anotado como `@Transient`. El valor no se persiste; hay inconsistencia modelo/BD.
- **Rutas frontend:** Rutas duplicadas o redundantes (p. ej. `/porcinos/partos` y `/porcinos/gestacion-partos` apuntan al mismo componente; `/porcinos/faena` y `/porcinos/ventas` también). Menú y rutas podrían simplificarse.
- **Historial de consumos / “Fórmulas”:** La frontera entre “fórmulas” (recetas por etapa), “insumos compuestos” y “consumos” en la UI y en la API no está unificada en un único contrato claro (varios endpoints y pantallas).

---

## 2. Modelo de dominio

### 2.1 Entidades principales

- **Reproductores:** Madre, Padrillo, HistorialEstadoMadre.  
- **Ciclo:** Servicio, Gestacion, ChequeoGestacion, Parto, Destete, Reabsorcion, MuerteLechon, LechonNN, TransferenciaLechon, TransferenciaCorral.  
- **Recría:** Recria, RegistroPeso, MovimientoEtapa, MuerteRecria.  
- **Alimentación:** ConsumoAlimento, DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle, RecetaAlimentacionPorEtapa, StockAlimento; relación con InsumoCompuesto (model.entity).  
- **Stock/trazabilidad:** MovimientoStockPorcino (porcinos); referencia a Insumo, InsumoCompuesto (model.entity).  
- **Otros:** EventoSanitario, TipoEventoSanitario; Faena (unificado con VentaPorcino); VentaPorcino; MadreMuerte; DerramePerdida.  
- **Catálogos:** RazaPorcino, TipoServicioPorcino, TipoParto, CausaMortalidadPorcino, MotivoBajaPorcino, EsquemaSanitarioPorcino, CausaMomificado, CausaNacidoMuerto, ProveedorGenetica, UbicacionInterna.  
- **Configuración:** ParametrosProductivosPorcino, ParametrosEstablecimientoPorcino, DatosEconomicosPorcino, ConfiguracionPorcino.

### 2.2 Relaciones y diseño

- **Lote/Plot:** Recria y VentaPorcino usan `loteId` (Long); no referencian la entidad Plot. Correcto respecto a la documentación (porcinos no depende de cultivos.domain). CatalogosPorcinoController usa LoteParaPorcinosQuery (puerto en core).
- **Insumo compartido:** MovimientoStockPorcino, ConsumoDiarioDetalle, DerramePerdida, StockAlimento, EventoSanitario referencian `com.agrocloud.model.entity.Insumo` o `InsumoCompuesto`. El dominio porcinos depende de `model.entity`, no de un módulo “inventario” acotado por interfaz.
- **Entidades con campos transitorios para UI:** Madre (gestacionActivaTransient, ultimoServicioTransient), Recria (loteNombre, madreNombre, origen, pesoActualKg). Mezcla de estado persistido y estado derivado/calculado en la misma entidad.
- **Parto:** Usa `totalNacidos` como campo obligatorio; relación con Destete (1 parto → 1 destete en la lógica actual). Coherencia con el flujo “un destete por parto”.

### 2.3 Posibles errores de diseño

- **Recria.origen:** Definido como `@Transient` pero existe columna en BD (V1_120). Si se desea persistir origen (DESTETE/EXTERNO), la entidad debe mapear la columna; si no, la migración sobra o la columna se usa desde otro lugar (p. ej. nativo).
- **Dependencia directa a model.entity:** Porcinos.domain tiene entidades puras (sin import de Cultivos), pero porcinos.application e incluso porcinos.domain (MovimientoStockPorcino, ConsumoDiarioDetalle, DerramePerdida, EventoSanitario, StockAlimento, RecetaAlimentacionPorEtapa, ConsumoDiarioAutomatico) referencian `com.agrocloud.model.entity.Insumo`, `InsumoCompuesto`, etc. No hay capa de puertos/adapters para “inventario”; el acoplamiento es fuerte.
- **Cultivos en servicios porcinos:** ConsumoDiarioAutomaticoService y ReportesPorcinoService/DashboardPorcinosService importan `com.agrocloud.cultivos.domain.Cultivo` y CultivoRepository para grano propio / reportes. Viola la regla “Porcinos no depende de Cultivos”; debería existir un puerto de solo lectura (p. ej. “ConsultaGranosPorEmpresa”) implementado por Cultivos.
- **Wildcards en servicios:** Varios servicios usan `import com.agrocloud.model.entity.*`, lo que oculta dependencias concretas y dificulta refactors y tests.

### 2.4 Acoplamientos innecesarios

- **Recordatorio (core):** PartoService, GestacionService, ServicioService crean recordatorios (partoId, servicioId, gestacionId). Aceptable como uso del core; el riesgo está en CalendarioController (ver CORE-Y-MODULOS.md).
- **InsumoCompuesto / ComponenteInsumoCompuesto:** Viven en model.entity; Porcinos los usa en dominio (RecetaAlimentacionPorEtapa, ConsumoDiarioDetalle, MovimientoStockPorcino, etc.). Si “insumos compuestos” son compartidos entre Cultivo y Porcinos, convendría que el contrato esté en core (interfaz o entidad compartida) y no que Porcinos dependa del paquete model.entity que puede contener entidades de Cultivo.

---

## 3. Reglas de negocio

### 3.1 Explícitas (en código)

- Parto: madre debe estar en estado GESTACION; validación de empresa.
- Destete: un solo destete por parto; cantidad destetada ≤ nacidos vivos; validación de peso al destete (ParametrosProductivosPorcino: pesoDesteteObjetivo ±50% o rangos 3–12 kg); cierre de parto si no tenía fechaFin; actualización estado madre a ADULTA; creación automática de recría con origen DESTETE.
- Recría: validación de lote existente (LoteParaPorcinosQuery); validación de pesos y densidad (ParametrosProductivosPorcino); origen por defecto EXTERNO en alta manual.
- Consumo diario automático: solo día anterior; no recalcular días ya confirmados; permite stock negativo solo para consumo automático; trazabilidad en MovimientoStockPorcino; uso de core.inventory (InventoryService) para descuentos.
- Evento sanitario: descuento/reposición de insumo vía InventarioPorcinoService → InventoryService.
- Empresa: todas las operaciones filtradas/validadas por empresa del usuario (EmpresaContextService o user.getEmpresa()).

### 3.2 Implícitas (no documentadas en código)

- Un parto tiene como máximo un destete (no está explícito en modelo; se valida en DesteteService).
- Orden de estados de madre (CACHORRA → ADULTA → GESTACION → LACTANCIA → RECRIA/DESCARTE) no está formalizado como máquina de estados; las transiciones se hacen en servicios (p. ej. PartoService, DesteteService, MadreService) sin un único lugar que defina transiciones permitidas.
- Criterios de “stock suficiente” y “permite negativo” dependen de InventoryService y de cada flujo (consumo automático vs evento sanitario); no hay documento único de reglas de stock para Porcinos.
- Duración de gestación (p. ej. 115 días) y fechas probables de parto/destete pueden estar en parámetros; la lógica está repartida entre GestacionService, RecordatorioService y front.

### 3.3 Por formalizar

- Documentar máquina de estados de Madre (estados y transiciones permitidas) y centralizar transiciones en un único servicio o dominio.
- Definir explícitamente “un parto → un destete” en modelo o en regla de dominio documentada.
- Reglas de negocio de venta/faena (qué recrías pueden venderse/faenarse; bloqueos por etapa o estado).
- Convención de uso de Insumo/InventarioGrano entre Cultivo y Porcinos (quién descuenta, cuándo, stock negativo) en un único documento o contrato.
- Criterios de “ciclo productivo válido” (usado en ReportesPorcinoController.validar-ciclo-productivo): documentar qué se considera válido para reportes.

---

## 4. Problemas arquitectónicos

### 4.1 Violación de separación de capas

- **Controllers sin DTOs:** Los controladores reciben y devuelven entidades JPA. La capa de presentación está acoplada al modelo de persistencia; cualquier cambio en entidades afecta el contrato API.
- **Lógica en controladores:** Patrón repetido: obtener User, validar user.getEmpresa(), llamar servicio. La obtención de empresa podría centralizarse en un filter/interceptor o en un método base; no es lógica de negocio pero está repetida en muchos controladores.
- **Entidades con lógica de presentación:** Uso de @JsonProperty, @Transient y campos “para la API” (loteNombre, madreNombre, gestacionActiva) en entidades de dominio mezcla persistencia, dominio y contrato de salida.

### 4.2 Lógica en lugares incorrectos

- **Validaciones solo en servicio:** Las validaciones de negocio están bien en servicios, pero la validación de formato (campos obligatorios, rangos, tipos) podría estar en DTOs con Bean Validation y en el controller con @Valid, dejando en el servicio solo reglas de negocio.
- **Cálculos derivados en entidad:** Recria.getPesoActualKg() está en la entidad como transitorio; podría ser responsabilidad de un servicio de lectura o de un DTO de respuesta.

### 4.3 Acoplamiento fuerte con otros módulos

- **model.entity (Insumo, InsumoCompuesto, InventarioGrano, etc.):** Múltiples servicios y entidades porcinos dependen directamente. No hay interfaz “Inventario” que Porcinos use; el módulo queda atado al modelo compartido.
- **cultivos.domain.Cultivo y CultivoRepository:** ConsumoDiarioAutomaticoService y reportes/dashboard porcinos usan Cultivo para “grano propio”. Debería existir un puerto en core (por ejemplo “ConsultaInventarioGrano”) implementado por el módulo que posea Cultivo/InventarioGrano, y Porcinos solo consumiría ese puerto.
- **Recordatorio (core):** Uso correcto; el riesgo está en que el calendario transversal (CalendarioController) conozca Parto/Servicio/Gestacion por ID, ya documentado en CORE-Y-MODULOS.md.

### 4.4 Inconsistencias de paquetes

- Dominio porcinos: `com.agrocloud.porcinos.domain` (correcto).
- Infraestructura: `com.agrocloud.porcinos.infrastructure` (repositorios).
- Aplicación: `com.agrocloud.porcinos.application`.
- Controllers: `com.agrocloud.controller` (fuera del paquete porcinos). Coherente con “API orquesta”, pero los controladores porcinos están mezclados con el resto en el mismo paquete; no hay subpaquete `controller.porcinos` o similar.
- Entidades compartidas: Insumo, InsumoCompuesto, Plot (lote) están en model.entity o cultivos.domain; no hay un “core.domain” único para compartidos, lo que refuerza el acoplamiento.

---

## 5. Riesgos a futuro

### 5.1 Escalabilidad

- **Consumo diario automático:** El job recorre todas las empresas activas y para cada una procesa lotes/recrías/madres y genera consumos y movimientos. Con muchas empresas y muchos datos, el tiempo de ejecución y el bloqueo de tablas pueden crecer; convendría procesar por empresa en lotes o con cola asíncrona.
- **Reportes y Excel:** ReportesPorcinoController expone varios reportes y exportaciones Excel; si los conjuntos de datos crecen, pueden requerir paginación, streaming o jobs en background.
- **Listados sin paginación:** Muchos listados (madres, partos, recrías, servicios, etc.) no muestran paginación en la API; con gran volumen de datos la respuesta y la UI pueden degradarse.

### 5.2 Mantenibilidad

- **Imports con wildcard (model.entity.*):** Dificultan ver de un vistazo qué entidades externas usa cada servicio y complican refactors y extracción de módulos.
- **Falta de DTOs:** Cualquier cambio en entidades (nuevo campo, renombre, relación) impacta el contrato REST; no hay capa de estabilidad.
- **Reglas de negocio dispersas:** Transiciones de estado de madre, validaciones de peso, reglas de destete/parto están en varios servicios; no hay un “dominio” explícito (aggregates, domain services) documentado, lo que dificulta onboarding y cambios seguros.
- **Tipos frontend (types.ts):** El archivo es grande (~990+ líneas); interfaces y enums duplican en parte el modelo del backend. Cualquier cambio en API puede requerir actualizar tipos a mano; no hay generación de tipos desde OpenAPI.

### 5.3 Crecimiento del módulo

- **Más flujos (sanidad, genética, etc.):** Si se agregan más entidades y flujos, la dependencia actual a model.entity y a cultivos.domain hará que cualquier cambio en Insumo/Cultivo/Plot afecte Porcinos. Un bounded context claro con puertos para “inventario” y “lotes” reduciría el impacto.
- **Múltiples orígenes de recría:** Hoy hay DESTETE y EXTERNO; si se agregan más orígenes o flujos (compra, transferencia entre establecimientos), persistir y usar bien el campo `origen` (y alinear entidad con BD) será importante.
- **Permisos granulares:** El front usa permisos (canViewMadres, canCreatePartos, etc.); si se añaden más roles o permisos por subflujo, conviene tener un mapa claro permiso–recurso en backend y front.

---

## 6. Recomendaciones estructurales

### 6.1 Refactors sugeridos (prioridad alta)

- **Alinear Recria.origen con la BD:** Si la columna `origen` debe persistirse, mapearla en la entidad (quitar @Transient, añadir @Column/@Enumerated). Si no, documentar por qué es solo transitorio y si la migración debe revertirse o la columna usarse por otro medio.
- **Introducir DTOs de request/response para los flujos principales:** Al menos para: alta/actualización de Madre, registro de Parto, registro de Destete, alta de Recría, registro de Servicio/Gestación, venta/faena. Validación con Bean Validation en DTOs y @Valid en controller.
- **Eliminar wildcards:** Sustituir `import com.agrocloud.model.entity.*` por imports explícitos en todos los servicios porcinos para dejar clara la dependencia y facilitar futuros movimientos.

### 6.2 Refactors sugeridos (prioridad media)

- **Centralizar obtención de empresa en API:** Un argument resolver o interceptor que inyecte Empresa (o empresaId) desde el usuario en los controladores que lo necesiten, reduciendo repetición y errores.
- **Desacoplar Porcinos de Cultivos para “grano propio”:** Definir en core un puerto (ej. `ConsultaInventarioGranoPorEmpresa` o `InventarioGranoQuery`) que devuelva solo lo necesario para consumos/reportes; implementarlo en el módulo que tenga InventarioGrano/Cultivo; ConsumoDiarioAutomaticoService y ReportesPorcinoService usarían solo el puerto.
- **Documentar máquina de estados de Madre:** En código (enum de transiciones permitidas o dominio explícito) o en documento de dominio, y concentrar transiciones en un único punto (p. ej. MadreService o un MadreEstadoService).

### 6.3 Organización por bounded context

- **Porcinos como bounded context:** Ya está delimitado por paquete y por flujos (reproductores, recría, alimentación, ventas, sanidad). Mantener que no referencie entidades de Cultivos; usar solo puertos de core (LoteParaPorcinosQuery, CrearLotePorcinoPort, y en el futuro Inventario/InventarioGrano como puertos).
- **Inventario compartido:** Definir un “contexto compartido” o “core de inventario” con interfaces (por ejemplo: ReservarStock, ConsultarStock, RegistrarMovimiento) y que tanto Cultivo como Porcinos consuman esas interfaces; las implementaciones pueden vivir en un módulo “inventario” o en core.infrastructure.
- **Lotes/Corrales:** Ya se usa LoteParaPorcinosQuery y CrearLotePorcinoPort; las entidades Porcinos usan solo loteId. Mantener esta línea; no introducir referencia a Plot en dominio porcinos.

### 6.4 Mejoras en modelo

- **Persistir origen en Recria** (si es regla de negocio): Mapear columna `origen` y usarla en consultas y reportes (filtrar por DESTETE vs EXTERNO).
- **Evitar entidades con estado transitorio de presentación:** Mover “nombre de lote”, “nombre madre”, “gestación activa”, “último servicio” a DTOs de respuesta o a servicios de lectura que armen un DTO, en lugar de cargar la entidad con @Transient para la API.
- **Considerar agregados explícitos:** Por ejemplo “CicloReproductivo” (Madre + Servicio + Gestación + Parto + Destete) o “Recría” como agregado con RegistroPeso, MuerteRecria, MovimientoEtapa, para acotar invariantes y transacciones.

### 6.5 Mejoras en contratos (DTO / API)

- **Versionado de API:** Los controladores porcinos usan `/api/v1/porcinos/...`; el front usa `/v1/porcinos/...` (el prefijo `/api` lo agrega el interceptor). Mantener un único criterio (v1) y documentar la política de versionado (cuándo v2, compatibilidad).
- **Contratos estables con DTOs:** Definir DTOs de respuesta para listados y detalle (MadreDTO, PartoDTO, RecriaDTO, etc.) con solo los campos necesarios para la UI; no exponer entidades JPA directamente.
- **Validación y códigos de error:** Usar @Valid en request bodies y devolver 400 con mensajes claros (campo + mensaje); opcionalmente códigos de error estructurados para que el front muestre mensajes por código.
- **OpenAPI/Esquema:** Generar especificación OpenAPI del módulo (o global) para documentar la API y, si es posible, generar tipos TypeScript en front a partir de ella, evitando desfases entre backend y types.ts.

### 6.6 Frontend

- **Simplificar rutas y menú:** Unificar rutas que apuntan al mismo componente (partos/gestacion-partos, ventas/faena) y evitar duplicados en el menú para reducir confusión y mantenimiento.
- **Servicios por dominio:** Los servicios en `modules/porcinos/services/` (gestacionService, desteteService, recriaService, etc.) están bien separados; mantener esta organización y evitar que un servicio llame a muchos endpoints de otros subdominios sin necesidad.
- **Manejo de errores y empresa:** Asegurar que todas las llamadas que requieran “empresa activa” manejen el caso de usuario sin empresa (mensaje claro, redirección o deshabilitar módulo).

---

## Resumen ejecutivo

- **Fortalezas:** Ciclo reproductivo (madre → servicio → gestación → parto → destete → recría) está implementado y con automatismos claros; multitenant por empresa está aplicado; uso de puertos (LoteParaPorcinosQuery, CrearLotePorcinoPort) para no depender de Plot; consumo diario automático integrado con core.inventory; documentación CORE-Y-MODULOS y ARQUITECTURA-MONOLITO-MODULAR ya identifican riesgos de compartidos.
- **Debilidades:** Dependencia directa a model.entity y a cultivos.domain en varios servicios; ausencia de DTOs y de Bean Validation en API; inconsistencia Recria.origen (Transient vs columna); entidades con lógica de presentación (transientes); listados sin paginación.
- **Riesgos:** Crecimiento del módulo y de datos puede chocar con acoplamiento a Insumo/Cultivo y con jobs/reportes síncronos; mantenibilidad limitada por falta de capa de contrato estable (DTOs) y por reglas de negocio dispersas.
- **Recomendaciones clave:** Alinear Recria.origen con BD; introducir DTOs y @Valid; desacoplar inventario y “grano propio” vía puertos; documentar máquina de estados de Madre; eliminar wildcards y referencias directas a Cultivos en Porcinos; simplificar rutas y menú en front; considerar OpenAPI y generación de tipos en front.
