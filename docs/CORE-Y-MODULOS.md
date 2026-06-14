# CORE y MÓDULOS – Documentación de delimitación

**Objetivo:** Identificar y documentar el CORE transversal del sistema y los módulos productivos (Cultivo, Porcinos) sin modificar comportamiento existente.

**Regla:** Ningún cambio funcional. Solo análisis y documentación.

---

## Paso 1 – CORE actual (estado real)

Elementos que se consideran **transversales** porque son usados por más de un módulo o no pertenecen conceptualmente a un solo módulo productivo.

### 1.1 Identidad / usuarios / roles / permisos

| Elemento | Ubicación | Usado por | Límites (qué NO debe hacer) |
|----------|-----------|-----------|-----------------------------|
| **User** | `model/entity/User.java` | Auth, todos los módulos (usuario en contexto) | No contener lógica de negocio de cultivo ni porcinos |
| **Role** | `model/entity/Role.java` | Admin, permisos globales | No depender de entidades de módulos |
| **Permission** | `model/entity/Permission.java` | ModulePermissionService, autorización | No referenciar entidades productivas |
| **UsuarioEmpresa** | `model/entity/UsuarioEmpresa.java` | Multiempresa, contexto empresa | Solo relación usuario–empresa–rol |
| **UsuarioEmpresaRol** | `model/entity/UsuarioEmpresaRol.java` | Roles por empresa | Solo datos de rol en empresa |
| **UserService** | `service/UserService.java` | Auth, CalendarioController, dashboards, módulos | No conocer Parto, Labor, Recria, etc. |
| **AuthService** | `service/AuthService.java` | Login, JWT | No depender de módulos productivos |
| **JwtService** | `service/JwtService.java` | Auth | Solo token |
| **RoleService** | `service/RoleService.java` | Admin, roles | Solo CRUD roles |
| **PermissionService** | `service/PermissionService.java` | Permisos | Solo permisos |
| **ModuleService** | `service/ModuleService.java` | Módulos del sistema | Solo catálogo de módulos |
| **ModulePermissionService** | `service/ModulePermissionService.java` | Permisos por módulo | Solo asociación módulo–permiso |
| **EmpresaUsuarioService** | `service/EmpresaUsuarioService.java` | Usuarios por empresa | Solo usuario–empresa |
| **AuthController** | `controller/AuthController.java` | API login | No exponer lógica de módulos |
| **AdminUsuarioController** | `controller/AdminUsuarioController.java` | Admin usuarios | No modificar datos productivos de módulos |
| **RolEmpresaController** | `controller/RolEmpresaController.java` | Roles por empresa | Solo roles |

### 1.2 Empresa y multiempresa

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **Empresa** | `model/entity/Empresa.java` | Todos los módulos (contexto empresa) | No contener reglas de negocio de cultivo/porcinos |
| **EmpresaService** | `service/EmpresaService.java` | Admin, contexto | Solo CRUD y datos de empresa |
| **EmpresaContextService** | `service/EmpresaContextService.java` | Porcinos, Cultivo (PlotService vía usuarios empresa), CalendarioAlimentacion, etc. | Solo resolver “empresa activa” del usuario; no conocer entidades de módulos |
| **EmpresaController** | `controller/EmpresaController.java` | API empresas | Solo empresas |
| **EmpresaUsuarioController** | `controller/EmpresaUsuarioController.java` | Usuarios de empresa | Solo relación usuario–empresa |

### 1.3 Calendario, recordatorios y eventos con fecha

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **Recordatorio** | `model/entity/Recordatorio.java` | Calendario general (cultivo), Calendario porcinos (eventos porcinos tienen partoId, servicioId, gestacionId) | Entidad compartida; los recordatorios “de porcinos” se filtran en API por módulo. El CORE no debe interpretar partoId/servicioId/gestacionId como regla de negocio porcino |
| **RecordatorioService** | `service/RecordatorioService.java` | CalendarioController, PartoService, GestacionService, ServicioService (crean recordatorios) | Solo CRUD recordatorios; no debe contener lógica de parto/gestación/servicio |
| **RecordatorioController** | `controller/RecordatorioController.java` | API recordatorios (crear, completar, listar) | Solo recordatorios |
| **CalendarioController** | `controller/CalendarioController.java` | Front: calendario cultivo (`/eventos`), calendario porcinos (`/porcinos`) | **Transversal:** agrega labores (Cultivo), cosechas (Cultivo), recordatorios (CORE), eventos porcinos (AlertasPorcinosService). No es “solo CORE”: depende de LaborService, PlotService, AlertasPorcinosService. Ver Paso 3 (riesgos) |

**Nota:** No existe una entidad “Evento” genérica; los eventos del calendario se arman en el controller a partir de Labor, Plot (cosecha esperada), Recordatorio y AlertasPorcinosService.

### 1.4 Inventario / stock compartido (insumos y granos)

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **Insumo** | `model/entity/Insumo.java` (tabla `cultivo_insumos`) | Cultivo (labores, movimientos, aplicaciones), Porcinos (recetas, consumo diario, derrames) | **Compartido:** ambos módulos leen/escriben stockActual. El CORE no debe implementar reglas de “cuándo” descontar; eso lo hacen los módulos |
| **InsumoService** | `service/InsumoService.java` | Cultivo (labores, insumos), Admin, Porcinos (catálogos/recetas usan insumos) | Solo CRUD y consulta de insumos; el descuento real lo hacen LaborService (MovimientoInventario) y ConsumoDiarioAutomaticoService (MovimientoStockPorcino) |
| **InventarioGrano** | `model/entity/InventarioGrano.java` | Cultivo (granos), Porcinos (consumo diario cuando la receta tiene componente “grano propio”) | **Compartido:** porcinos descuenta vía ConsumoDiarioAutomaticoService. El CORE no debe decidir consumos |
| **InventarioGranoService** | `service/InventarioGranoService.java` | Cultivo, Porcinos (ConsumoDiarioAutomaticoService usa repo) | Consultas y movimientos de grano; el “quién descuenta” son los servicios de módulo |
| **InventarioService** | `service/InventarioService.java` | Dashboard, reportes (stock bajo, etc.) | Solo lectura/agregados; no debe descontar stock |

**Importante:** Los **movimientos** de stock son por módulo: `MovimientoInventario` (cultivo), `MovimientoStockPorcino` (porcinos), `MovimientoInventarioGrano` (cultivo/origen; porcinos puede consumir). No hay un “MovimientoStock” único en el CORE.

### 1.5 Movimientos de stock e inventario (capa transversal)

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **MovimientoInventario** | `model/entity/MovimientoInventario.java` (tabla `cultivo_movimientos_inventario`) | Cultivo (LaborService, aplicaciones) | **Pertenece al módulo Cultivo.** No mover al CORE; solo documentar que el “stock compartido” (Insumo) se actualiza desde Cultivo vía esta entidad |
| **MovimientoStockPorcino** | `model/entity/MovimientoStockPorcino.java` (tabla `porcinos_movimientos_stock`) | Porcinos (ConsumoDiarioAutomaticoService, DerramePerdidaService) | **Pertenece al módulo Porcinos.** No mover al CORE |
| **MovimientoInventarioGrano** | `model/entity/MovimientoInventarioGrano.java` | Cultivo y Porcinos (consumo grano) | **Compartido:** usado por ambos. Documentar como punto de acoplamiento (Paso 3) |

### 1.6 Ingresos, egresos y reportes financieros

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **Ingreso** | `model/entity/Ingreso.java` (tabla `cultivo_ingresos`) | Dashboard, Balance, reportes admin | Aunque la tabla tiene prefijo cultivo_, se usa en reportes transversales (BalanceService, AdminDashboardService). No cambiar comportamiento |
| **Egreso** | `model/entity/Egreso.java` (tabla `cultivo_egresos`) | Idem | Idem |
| **EgresoService** | `service/EgresoService.java` | Cultivo, Dashboard, Balance | No añadir lógica de módulos |
| **IngresoController** / **EgresoController** | `controller/` | API ingresos/egresos | Solo CRUD y listados |
| **BalanceService** | `service/BalanceService.java` | Reporte balance, dashboard | Solo agregados financieros; no modificar reglas de negocio de módulos |
| **BalanceController** | `controller/BalanceController.java` | API balance | Solo exposición de balance |

### 1.7 Jobs automáticos y tareas programadas

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **ConsumoDiarioAutomaticoService** (método `@Scheduled`) | `service/ConsumoDiarioAutomaticoService.java` – `generarConsumoDiarioAutomatico()` con `@Scheduled(cron = "0 30 0 * * ?")` | Porcinos (generación diaria de consumo) | **Pertenece al módulo Porcinos.** Es el único job programado actual. No hay jobs “genéricos” en el CORE. Documentar: el CORE no tiene jobs propios; los jobs son del módulo que corresponda |

**Habilitación:** `AgroCloudApplication.java` con `@EnableScheduling`.

### 1.8 Configuración y logs

| Elemento | Ubicación | Usado por | Límites |
|----------|-----------|-----------|---------|
| **EulaService** | `service/EulaService.java` | Aceptación EULA (usuario) | Solo EULA; no datos productivos |
| **EulaController** | `controller/EulaController.java` | API EULA | Solo EULA |
| **LogAcceso** | `model/entity/LogAcceso.java` | Auditoría de acceso | Solo logs; no lógica de negocio |
| **LogAccesoService** | `service/LogAccesoService.java` | Registro de accesos | Solo escritura/consulta de logs |
| **CompanyModuleService** | `service/CompanyModuleService.java` | Módulos por empresa (Company, CompanyModule) | Solo relación empresa–módulo habilitado |
| **EmailService** | `service/EmailService.java` | Notificaciones, recuperación contraseña | Solo envío de correo |

---

## Paso 2 – MÓDULOS PRODUCTIVOS

### 2.1 Módulo CULTIVO

**Entidades propias (no mover al CORE):**

- Field, Plot, Cultivo, TipoCultivo  
- Labor, LaborInsumo, LaborManoObra, LaborMaquinaria  
- Cosecha, HistorialCosecha  
- EstadoLoteConfig, TransicionEstadoConfig, TareaPorEstadoConfig  
- Insumo (tabla cultivo_insumos; **compartida** con Porcinos – ver Paso 3)  
- MovimientoInventario, InventarioGrano, MovimientoInventarioGrano  
- DosisAgroquimico, DosisAplicacion, AplicacionAgroquimica, Agroquimico  
- Egreso, Ingreso (tablas cultivo_*; usadas también en reportes transversales)  
- Maquinaria, MantenimientoMaquinaria, AlquilerMaquinaria  

**Servicios propios:**

- FieldService, PlotService, LaborService, CultivoService, SiembraService, TransicionEstadoService, EstadoLoteService, ConfiguracionEstadosService  
- InsumoService (CRUD insumos; stock lo tocan LaborService y Porcinos)  
- InventarioGranoService, InventarioService  
- HistorialCosechaService, RendimientoService, ReporteService  
- DosisAgroquimicoService, DosisAplicacionService, AplicacionAgroquimicaService, AgroquimicoService  
- EgresoService, IngresoController (uso desde Balance/Dashboard es transversal)  
- MaquinariaService  

**Dependencias hacia el CORE:**

- User (usuario en labores, contexto)  
- Empresa (PlotService obtiene usuarios de empresa; LaborService usuario)  
- EmpresaContextService (ConfiguracionEstadosService usa empresa del usuario)  
- Recordatorio (recordatorios con laborId/loteId – tipo LABOR/COSECHA)  
- Auth/JWT para seguridad de APIs  

**Qué NO debe moverse al CORE aunque lo use:**

- Labor, Plot (lote), Cosecha, HistorialCosecha, MovimientoInventario  
- DiaAlimentacion no es de Cultivo; es de Porcinos  

---

### 2.2 Módulo PORCINOS

**Entidades propias (no mover al CORE):**

- Madre, Padrillo, Servicio, Gestacion, ChequeoGestacion, Parto, Destete, Reabsorcion  
- Recria, RegistroPeso, MovimientoEtapa  
- MuerteLechon, MuerteRecria, MadreMuerte, LechonNN, TransferenciaLechon, TransferenciaCorral  
- ConsumoAlimento, DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle  
- RecetaAlimentacionPorEtapa, InsumoCompuesto, ComponenteInsumoCompuesto  
- MovimientoStockPorcino, DerramePerdida, StockAlimento  
- EventoSanitario, TipoEventoSanitario  
- Faena, VentaPorcino  
- HistorialEstadoMadre  
- Catálogos: RazaPorcino, TipoServicioPorcino, TipoParto, CausaMortalidadPorcino, MotivoBajaPorcino, EsquemaSanitarioPorcino, CausaMomificado, CausaNacidoMuerto, ProveedorGenetica, UbicacionInterna  
- Parámetros: ParametrosProductivosPorcino, ParametrosEstablecimientoPorcino, DatosEconomicosPorcino, ConfiguracionPorcino  

**Servicios propios:**

- MadreService, PadrilloService, ServicioService, GestacionService, ChequeoGestacionService, PartoService, DesteteService, RecriaService, RegistroPesoService, MovimientoEtapaService  
- MuerteLechonService, MuerteRecriaService, MadreMuerteService, TransferenciaLechonService  
- ConsumoAlimentoService, ConsumoDiarioAutomaticoService, CalendarioAlimentacionService, DerramePerdidaService, PreparacionRecetaService, InsumoCompuestoService  
- InventarioPorcinoService  
- EventoSanitarioService, FaenaService, VentaPorcinoService  
- CatalogosPorcinoService, ParametrosPorcinoService, ConfiguracionPorcinoService  
- AlertasPorcinosService, DashboardPorcinosService, ReportesPorcinoService, InicializacionPorcinoService  

**Dependencias hacia el CORE:**

- User, Empresa, EmpresaContextService (todas las operaciones por empresa)  
- Recordatorio (PartoService, GestacionService, ServicioService crean recordatorios de destete/parto/control celo)  
- Insumo, InventarioGrano (consumo diario y derrames descontan de ahí)  
- Plot (Recria, VentaPorcino usan lote; PlotService.getLotesPorcinosByUser)  
- Auth/JWT  

**Qué NO debe moverse al CORE aunque lo use:**

- DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle, MovimientoStockPorcino  
- Parto, Destete, Recria, Gestacion, Servicio, Madre, etc.  
- Recordatorio **sí** es entidad CORE, pero los que tienen partoId/servicioId/gestacionId son “de porcinos” a efectos de UI (calendario porcinos vs calendario cultivo)  

---

## Paso 3 – Puntos de riesgo (solo documentar, no corregir)

### 3.1 CORE depende de un módulo

| Punto | Descripción |
|-------|-------------|
| **CalendarioController** | El endpoint “genérico” `/api/calendario/eventos` usa LaborService (Cultivo) y PlotService (Cultivo) y RecordatorioService (CORE) y filtra recordatorios porcinos por partoId/servicioId/gestacionId. El endpoint `/api/calendario/porcinos` usa AlertasPorcinosService (Porcinos). Es decir: el “calendario” transversal depende de ambos módulos. **Riesgo:** si se extrae un “core de calendario”, no debe invocar servicios de módulos; hoy sí lo hace. |
| **DashboardService** | Usa InsumoRepository, LaborRepository, PlotRepository, etc. Dashboard “general” con datos de labores/lotes/insumos es transversal pero depende de entidades de Cultivo. **Riesgo:** dashboard transversal acoplado a Cultivo. |
| **AdminDashboardService** | Usa InsumoRepository, labores, lotes, ingresos, egresos. Idem. |

### 3.2 Entidades compartidas con semántica ambigua

| Entidad | Riesgo |
|---------|--------|
| **Plot** | Tabla `cultivo_lotes`; usada por Cultivo (labores, cosecha) y por Porcinos (Recria, VentaPorcino). Tiene `tipoUso` (CULTIVO/PORCINO). Dos módulos interpretan el mismo ID: Cultivo como “lote de campo”, Porcinos como “corral”. Cambios en Plot (por ejemplo nuevos campos) pueden afectar a ambos. |
| **Insumo** | Tabla `cultivo_insumos`; Cultivo la usa para labores y MovimientoInventario; Porcinos para recetas y consumo diario (descuento directo + MovimientoStockPorcino). Stock único compartido; convenciones de uso (quién descuenta, cuándo) no están en un solo lugar. |
| **Recordatorio** | Tiene laborId, loteId (cultivo) y partoId, servicioId, gestacionId (porcinos). Una sola tabla para dos semánticas; el filtrado por “módulo” se hace en el controller (calendario). |
| **Ingreso / Egreso** | Tablas `cultivo_ingresos`, `cultivo_egresos`; usadas en reportes financieros transversales. Origen conceptualmente ligado a Cultivo; uso transversal en Balance/Admin. |

### 3.3 Cambios en un módulo que impactan en otro

| Escenario | Riesgo |
|-----------|--------|
| Cambio en **Insumo** (ej. nuevo campo, validación) | Afecta a LaborService (Cultivo) y a ConsumoDiarioAutomaticoService (Porcinos). |
| Cambio en **Plot** (ej. tipoUso, estados) | Afecta a LaborService, SiembraService, RecriaService, VentaPorcinoService, PlotService.getLotesPorcinosByUser. |
| Cambio en **Recordatorio** (ej. nuevos campos por módulo) | Afecta a CalendarioController (filtros) y a PartoService, GestacionService, ServicioService (creación). |
| Cambio en **InventarioGrano** o **MovimientoInventarioGrano** | Afecta a Cultivo y a ConsumoDiarioAutomaticoService (Porcinos). |

---

## Paso 4 – Documentación de límites

### 4.1 Definición del CORE actual (estado real)

- **CORE** = identidad (User, Role, Permission, UsuarioEmpresa, UsuarioEmpresaRol), empresa (Empresa, EmpresaContextService, EmpresaService), recordatorios (Recordatorio, RecordatorioService), EULA, logs (LogAcceso), email, y la **exposición** de datos compartidos (Insumo, InventarioGrano como entidades/servicios de consulta y CRUD; Balance/Ingreso/Egreso para reportes financieros).  
- **No** forman parte del CORE: jobs (el único @Scheduled es de Porcinos), “calendario” como agregador (CalendarioController depende de módulos), ni las entidades de movimientos de stock (cada módulo tiene las suyas).

### 4.2 Carpetas / paquetes que se consideran CORE

- **Entidades:** User, Role, Permission, Empresa, UsuarioEmpresa, UsuarioEmpresaRol, Recordatorio, LogAcceso, (EstadoUsuario si se usa).  
- **Servicios:** AuthService, UserService, JwtService, RoleService, PermissionService, ModuleService, ModulePermissionService, EmpresaService, EmpresaContextService, EmpresaUsuarioService, RecordatorioService, EulaService, LogAccesoService, EmailService, CompanyModuleService. BalanceService e InventarioService (solo lectura/agregados) se consideran transversales; Ingreso/Egreso y sus servicios están en paquete general y son usados por Cultivo y por reportes.  
- **Controllers:** AuthController, AdminUsuarioController, RolEmpresaController, EmpresaController, EmpresaUsuarioController, RecordatorioController, EulaController.  
- **Repositorios:** UserRepository, RoleRepository, PermissionRepository, UsuarioEmpresaRepository, EmpresaRepository, RecordatorioRepository, etc., asociados a las entidades anteriores.

**Nota:** En el proyecto actual no hay subpaquetes `core` vs `cultivo` vs `porcinos`; todo está bajo `com.agrocloud`. La delimitación es **conceptual** por entidad/servicio, no por carpeta física.

### 4.3 Carpetas / paquetes que son MÓDULOS

- **Cultivo (conceptual):** Field, Plot, Labor, Cosecha, HistorialCosecha, Cultivo, TipoCultivo, Insumo, MovimientoInventario, InventarioGrano, MovimientoInventarioGrano, estados de lote, agroquímicos, Egreso, Ingreso, Maquinaria, y sus servicios/controllers.  
- **Porcinos (conceptual):** Todas las entidades en tablas `porcinos_*`, DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle, MovimientoStockPorcino, DerramePerdida, InsumoCompuesto, ComponenteInsumoCompuesto, RecetaAlimentacionPorEtapa, y sus servicios/controllers.

### 4.4 Reglas de oro (para futuros desarrollos)

1. **El CORE no conoce módulos:** Los servicios/entidades del CORE no deben importar ni depender de Parto, Labor, Recria, DiaAlimentacion, etc. Excepción aceptada hoy: CalendarioController y dashboards que agregan datos de varios orígenes; documentado como riesgo.  
2. **Los módulos no modifican stock “directamente” en nombre del CORE:** Hoy los módulos sí modifican Insumo.stockActual e InventarioGrano (Cultivo vía MovimientoInventario; Porcinos vía ConsumoDiarioAutomaticoService y MovimientoStockPorcino). La regla deseable a futuro: que el descuento pase por una capa o contrato claro (ej. “servicio de stock” que recibe “movimiento” y actualiza Insumo/InventarioGrano), sin que cada módulo toque directamente el stock. **No aplicar ahora;** solo documentar.  
3. **Todo dato productivo pertenece a una empresa:** Recria, Labor, Parto, etc. están asociados a Empresa (o al usuario que pertenece a una empresa). EmpresaContextService es el punto único para “empresa activa” del usuario.

---

## Paso 5 – Preparación para futuro (solo propuesta en documentación)

### 5.1 Cómo agregar un nuevo módulo productivo (ej. pollos o vacunos)

- **No tocar:** Lógica existente de Cultivo y Porcinos, reglas de stock actuales, CalendarioController (salvo añadir origen de eventos si se define un contrato), jobs de ConsumoDiarioAutomaticoService.  
- **Reutilizar del CORE:** User, Empresa, EmpresaContextService, Auth/JWT, Recordatorio (crear recordatorios del nuevo módulo con tipos o campos propios si se extiende), permisos/módulos (dar de alta el nuevo módulo en Module / CompanyModule).  
- **Reutilizar compartido (con cuidado):** Insumo / InventarioGrano si el nuevo módulo consume los mismos insumos o granos; Plot solo si se decide que “lote” también representa corral/gallinero/etc. (revisar semántica y tipoUso).  
- **Nuevo en el módulo:** Entidades propias (ej. Gallinero, LotePollos, CicloPonedora, etc.), tablas con prefijo propio (ej. `pollos_*`), servicios que usen EmpresaContextService y, si aplica, InsumoRepository o InventarioGranoRepository para consumos.  
- **Reportes y dashboards:** Si se agrega un dashboard “general” que incluya el nuevo módulo, hacerlo sin que el CORE dependa del módulo (ej. que el front llame a un endpoint del módulo y arme la vista; o un endpoint agregador que reciba “módulos a incluir”).  
- **Jobs:** Cualquier job del nuevo módulo (ej. generación diaria de consumo) debe vivir en el paquete/servicio de ese módulo y no en un “core de jobs”.

### 5.2 Qué NO habría que tocar

- Entidades y servicios de Cultivo y Porcinos ya listados en el Paso 2.  
- Comportamiento de stock (cuándo se descuenta, quién descuenta, stock negativo en porcinos).  
- Filtrado de recordatorios por módulo en CalendarioController (eventos vs porcinos).  
- Estructura de tablas existentes (cultivo_*, porcinos_*, recordatorios, empresas, usuarios).  
- Único job programado actual (generación consumo diario porcinos).

---

## Resumen

- **CORE (conceptual):** identidad, empresa, recordatorios, EULA, logs, email, Balance/reportes financieros (uso de Ingreso/Egreso), CRUD y consulta de Insumo/InventarioGrano sin reglas de descuento por módulo.  
- **Módulos:** Cultivo (Field, Plot, Labor, Cosecha, Insumo como origen de datos, movimientos cultivo, etc.) y Porcinos (todo lo que está en porcinos_* y consumo/día alimentación).  
- **Riesgos documentados:** CalendarioController y dashboards dependen de módulos; Plot e Insumo compartidos con semántica dual; Recordatorio con campos por módulo; impacto cruzado en cambios de Insumo/Plot/InventarioGrano.  
- **Sin cambios funcionales:** esta documentación solo delimita y documenta; no se ha modificado ningún comportamiento ni archivo de código.
