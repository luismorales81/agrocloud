# Arquitectura: Monolito modular (Core / Cultivos / Porcinos)

**Stack:** Java 17, Spring Boot 3.x. Una sola aplicación (un único WAR/JAR), un único login y una única API REST.

**Objetivo:** Separación estricta por dominios con paquetes y dependencias claras, **sin** microservicios ni aplicaciones separadas.

---

## 1. Estructura de paquetes (equivalente a carpetas .NET)

```
src/main/java/com/agrocloud/
├── core/
│   ├── domain/          # Entidades, enums, value objects del núcleo
│   ├── application/     # Servicios de aplicación (casos de uso) Core
│   └── infrastructure/  # Repositorios y adaptadores Core
│
├── cultivos/
│   ├── domain/          # Entidades y lógica de dominio Cultivos
│   ├── application/     # Servicios de aplicación Cultivos
│   └── infrastructure/  # Repositorios Cultivos
│
├── porcinos/
│   ├── domain/          # Entidades y lógica de dominio Porcinos
│   ├── application/     # Servicios de aplicación Porcinos
│   └── infrastructure/  # Repositorios Porcinos
│
└── api/                 # Orquestación: controllers, config, seguridad, main
    ├── controller/      # Delegación a Core/Cultivos/Porcinos
    ├── config/
    └── AgroCloudApplication.java
```

**Criterios de validación:**
- Eliminar el paquete `porcinos` → `cultivos` y `core` deben seguir compilando.
- Eliminar el paquete `cultivos` → `porcinos` y `core` deben seguir compilando.
- `core` no depende de `cultivos` ni de `porcinos`.
- Los controllers en `api` solo orquestan y delegan a servicios de cada módulo.

---

## 2. Mapeo: qué va en cada módulo

### 2.1 Core (solo identidad, empresa, auth, módulos habilitados)

| Tipo       | Clases |
|-----------|--------|
| **domain** | User, Role, Permission, Empresa, UsuarioEmpresa, UsuarioEmpresaRol, Recordatorio, LogAcceso, EstadoUsuario, Company, CompanyModule, Module, ModuleRolePermission, RolePermission, UserRole, UserCompanyRole |
| **application** | AuthService, UserService, JwtService, RoleService, PermissionService, ModuleService, ModulePermissionService, EmpresaService, EmpresaContextService, EmpresaUsuarioService, RecordatorioService, EulaService, LogAccesoService, EmailService, CompanyModuleService |
| **infrastructure** | UserRepository, RoleRepository, PermissionRepository, EmpresaRepository, UsuarioEmpresaRepository, RecordatorioRepository, LogAccesoRepository, CompanyRepository, CompanyModuleRepository, ModuleRepository, ModuleRolePermissionRepository, RolePermissionRepository, UserRoleRepository, UserCompanyRoleRepository |

**Controllers (api):** AuthController, AdminUsuarioController, RolEmpresaController, EmpresaController, EmpresaUsuarioController, RecordatorioController, EulaController.

**Contratos (core.application):** Interfaces que otros módulos usan y Core (o otro módulo) implementa, p. ej. `EmpresaContextService` (Core implementa), y en Fase 3 `LoteParaPorcinosQuery` (Cultivos implementa, Porcinos consume vía interfaz en core).

---

### 2.2 Cultivos (solo lógica de cultivos; no referencia Porcinos)

| Tipo       | Clases |
|-----------|--------|
| **domain** | Field, Plot, Cultivo, TipoCultivo, Labor, LaborInsumo, LaborManoObra, LaborMaquinaria, Cosecha, HistorialCosecha, EstadoLoteConfig, TransicionEstadoConfig, TareaPorEstadoConfig, Insumo, MovimientoInventario, InventarioGrano, MovimientoInventarioGrano, DosisAgroquimico, DosisAplicacion, AplicacionAgroquimica, Agroquimico, Egreso, Ingreso, Maquinaria, MantenimientoMaquinaria, AlquilerMaquinaria |
| **application** | FieldService, PlotService, LaborService, CultivoService, SiembraService, TransicionEstadoService, EstadoLoteService, ConfiguracionEstadosService, InsumoService, InventarioGranoService, InventarioService, HistorialCosechaService, RendimientoService, ReporteService (solo cultivos), DosisAgroquimicoService, DosisAplicacionService, AplicacionAgroquimicaService, AgroquimicoService, EgresoService, MaquinariaService |
| **infrastructure** | Repositorios de todas las entidades listadas en domain |

**Enums/transiciones:** EstadoLote, TipoMaquinaria, TipoAplicacion, FormaAplicacion, UnidadDosis, UnidadMedida, TipoAgroquimico, TransicionEstadoLote → `cultivos.domain` (o subpaquete enums).

**Controllers (api):** FieldController, PlotController, LaborController, CultivoController, ConfiguracionEstadosController, InsumoController, InventarioGranoController, HistorialCosechaController, RendimientoController, DosisAgroquimicoController, AplicacionAgroquimicaController, AgroquimicoController, EgresoController, IngresoController, MaquinariaController, BalanceController, CalendarioController (orquesta; puede depender de Core + Cultivos + Porcinos vía interfaces), etc.

---

### 2.3 Porcinos (solo lógica porcina; no referencia Cultivos)

| Tipo       | Clases |
|-----------|--------|
| **domain** | Madre, Padrillo, Servicio, Gestacion, ChequeoGestacion, Parto, Destete, Reabsorcion, Recria, RegistroPeso, MovimientoEtapa, MuerteLechon, MuerteRecria, MadreMuerte, LechonNN, TransferenciaLechon, TransferenciaCorral, ConsumoAlimento, DiaAlimentacion, ConsumoDiarioAutomatico, ConsumoDiarioDetalle, RecetaAlimentacionPorEtapa, InsumoCompuesto, ComponenteInsumoCompuesto, MovimientoStockPorcino, DerramePerdida, StockAlimento, EventoSanitario, TipoEventoSanitario, Faena, VentaPorcino, HistorialEstadoMadre, RazaPorcino, TipoServicioPorcino, TipoParto, CausaMortalidadPorcino, MotivoBajaPorcino, EsquemaSanitarioPorcino, CausaMomificado, CausaNacidoMuerto, ProveedorGenetica, UbicacionInterna, ParametrosProductivosPorcino, ParametrosEstablecimientoPorcino, DatosEconomicosPorcino, ConfiguracionPorcino |
| **application** | MadreService, PadrilloService, ServicioService, GestacionService, ChequeoGestacionService, PartoService, DesteteService, RecriaService, RegistroPesoService, MovimientoEtapaService, MuerteLechonService, MuerteRecriaService, MadreMuerteService, TransferenciaLechonService, ConsumoAlimentoService, ConsumoDiarioAutomaticoService, CalendarioAlimentacionService, DerramePerdidaService, PreparacionRecetaService, InsumoCompuestoService, InventarioPorcinoService, EventoSanitarioService, FaenaService, VentaPorcinoService, CatalogosPorcinoService, ParametrosPorcinoService, ConfiguracionPorcinoService, AlertasPorcinosService, DashboardPorcinosService, ReportesPorcinoService, InicializacionPorcinoService |
| **infrastructure** | Repositorios de todas las entidades listadas en domain |

**Controllers (api):** Los controllers que exponen endpoints porcinos (CatalogosPorcinoController, etc.).

---

## 3. Desacoplamiento Crítico: Plot / Lote y Porcinos

Hoy **Porcinos** referencia la entidad **Plot** (Recria, VentaPorcino, MovimientoStockPorcino, CatalogosPorcinoController → PlotService). Para que Porcinos no dependa de Cultivos:

1. **Core** define una interfaz de lectura, por ejemplo en `core.application.port`:
   - `LoteParaPorcinosQuery`: `List<LoteMinimoDTO> listarPorEmpresa(Long empresaId);` (id, nombre, etc.).
2. **Cultivos** implementa esa interfaz (p. ej. `PlotService` o un adapter que use `PlotRepository` filtrando por `tipoUso = PORCINO`).
3. **Porcinos** solo usa `LoteParaPorcinosQuery` y guarda `loteId` (Long) en Recria, VentaPorcino, MovimientoStockPorcino; no referencia la entidad Plot ni PlotService.
4. En **api** (configuración Spring) se registra la implementación de `LoteParaPorcinosQuery` (desde Cultivos).

Así, si se elimina el paquete Cultivos, Porcinos sigue compilando; solo fallaría el wiring en tiempo de ejecución si no se proporciona un stub.

---

## 4. Recursos compartidos (Insumo, InventarioGrano)

- **Insumo** e **InventarioGrano** son usados por Cultivos y Porcinos. Opciones:
  - **A)** Dejarlos en **Core** (domain + repos + servicio de consulta/CRUD); Cultivos y Porcinos solo dependen de Core y usan IDs o DTOs. Los movimientos (MovimientoInventario, MovimientoStockPorcino) siguen en su módulo.
  - **B)** Dejarlos en **Cultivos** y que Porcinos acceda vía interfaz en Core (por ejemplo `InsumoQuery`) implementada por Cultivos.

Para no reescribir todo, se recomienda **A**: Insumo e InventarioGrano (y sus repos y un servicio de lectura/CRUD) en **Core**; reglas de descuento siguen en cada módulo (Cultivos: MovimientoInventario; Porcinos: MovimientoStockPorcino, etc.).

---

## 5. Plan de ejecución incremental

| Fase | Acción | Estado |
|------|--------|--------|
| **1** | Crear paquetes `core.domain`, `core.application`, `core.infrastructure`. Mover entidades, servicios y repositorios Core. Actualizar imports en todo el proyecto. Mover controllers Core a `api.controller`. | **En curso:** Role, Permission, RolePermission en core.domain; RoleRepository, PermissionRepository, RolePermissionRepository en core.infrastructure; RoleService, ModulePermissionService en core.application. Resto pendiente. |
| **2** | Crear paquetes `cultivos.domain`, `cultivos.application`, `cultivos.infrastructure`. Mover entidades, servicios y repositorios de Cultivos. Actualizar imports. Controllers Cultivos a `api.controller`. | Pendiente. |
| **3** | Introducir `LoteParaPorcinosQuery` en Core; Porcinos usar solo la interfaz y `loteId` (Long). Implementar la interfaz (adapter con PlotService). Crear paquetes Porcinos y mover entidades/servicios/repos; eliminar referencias a Plot en entidades Porcinos (solo loteId). | **Parcial:** puerto `LoteParaPorcinosQuery` + `LoteMinimoDTO` en Core; adapter en service; `CatalogosPorcinoController` ya usa el puerto (no referencia PlotService). Entidades Recria/VentaPorcino/MovimientoStockPorcino siguen con `Plot lote` (cambio a `loteId` en fase posterior). |
| **4** | Revisar CalendarioController y dashboards transversales: que dependan de interfaces (en Core) implementadas por cada módulo. | Pendiente. |
| **5** | Opcional: tests ArchUnit para prohibir `cultivos` → `porcinos` y `porcinos` → `cultivos`. | Pendiente. |

### Checklist Fase 1 (movimiento físico a Core)

Cuando se ejecute el movimiento de clases a `com.agrocloud.core.*`:

- **core.domain:** ✅ Role, Permission, RolePermission, Module. Pendiente: User, Empresa, UsuarioEmpresa, UsuarioEmpresaRol, Recordatorio, LogAcceso, EstadoUsuario, Company, CompanyModule, ModuleRolePermission, UserRole, UserCompanyRole.
- **core.application:** ✅ RoleService, ModulePermissionService, ModuleService, CompanyModuleService, AuthService, PermissionService, JwtService, UserService, EmpresaService, EmpresaContextService. Puertos LoteParaPorcinosQuery, LoteMinimoDTO en core.application.port. Pendiente: EmpresaUsuarioService, RecordatorioService, EulaService, LogAccesoService, EmailService.
- **core.infrastructure:** ✅ RoleRepository, PermissionRepository, RolePermissionRepository, ModuleRepository. Pendiente: UserRepository, EmpresaRepository, UsuarioEmpresaRepository, RecordatorioRepository, LogAccesoRepository, CompanyRepository, CompanyModuleRepository, ModuleRolePermissionRepository, UserRoleRepository, UserCompanyRoleRepository.
- Actualizar todos los imports en el resto del proyecto que referencien las clases movidas.
- Enums usados solo por Core (p. ej. RolEmpresa) pueden moverse a core.domain.enums o permanecer en model.enums hasta unificar.

---

## 6. Reglas de oro

1. **Core** no importa nada de `cultivos` ni `porcinos`.
2. **Cultivos** no importa nada de `porcinos`; solo de `core` (y opcionalmente de `api` para anotaciones/config si aplica).
3. **Porcinos** no importa nada de `cultivos`; solo de `core` (y `api` si aplica).
4. **api** puede importar Core, Cultivos y Porcinos para orquestar (controllers, seguridad, configuración).
5. Lógica de negocio en servicios (application), no en controllers.
6. No compartir entidades entre Cultivos y Porcinos; comunicación por interfaces en Core y DTOs/IDs.

---

*Documento base para la refactorización incremental. El proyecto actual es Java/Spring Boot; la estructura equivale a la de un monolito modular en .NET (Core, Cultivos, Porcinos, WebApi).*
