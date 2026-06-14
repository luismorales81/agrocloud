import { lazy, ComponentType } from 'react';
import { ModuleRoute } from '../../core/types/module.types';

const CalendarioPorcinosDashboard = lazy(() => import('../../components/CalendarioPorcinosDashboard'));
const DashboardPorcinosScreen = lazy(() => import('./screens/DashboardPorcinosScreen'));
const ReproductoresScreen = lazy(() => import('./screens/Reproductores/ReproductoresScreen'));
const PadrillosListScreen = lazy(() => import('./screens/Padrillos/PadrillosListScreen'));
const PadrilloCreateScreen = lazy(() => import('./screens/Padrillos/PadrilloCreateScreen'));
const MadreCreateScreen = lazy(() => import('./screens/Madres/MadreCreateScreen'));
const MadreDetailScreen = lazy(() => import('./screens/Madres/MadreDetailScreen'));
const MadreHistoryScreen = lazy(() => import('./screens/Madres/MadreHistoryScreen'));
const ServiciosListScreen = lazy(() => import('./screens/Servicios/ServiciosListScreen'));
const ServicioCreateScreen = lazy(() => import('./screens/Servicios/ServicioCreateScreen'));
const ControlCeloScreen = lazy(() => import('./screens/Servicios/ControlCeloScreen'));
const GestacionListScreen = lazy(() => import('./screens/Gestacion/GestacionListScreen'));
const GestacionDetailScreen = lazy(() => import('./screens/Gestacion/GestacionDetailScreen'));
const PartosListScreen = lazy(() => import('./screens/Partos/PartosListScreen'));
const PartoCreateScreen = lazy(() => import('./screens/Partos/PartoCreateScreen'));
const PartoDetailScreen = lazy(() => import('./screens/Partos/PartoDetailScreen'));
const DestetesListScreen = lazy(() => import('./screens/Destetes/DestetesListScreen'));
const DesteteCreateScreen = lazy(() => import('./screens/Destetes/DesteteCreateScreen'));
const DesteteDetailScreen = lazy(() => import('./screens/Destetes/DesteteDetailScreen'));
const RecriaListScreen = lazy(() => import('./screens/Recria/RecriaListScreen'));
const RecriaIngresoScreen = lazy(() => import('./screens/Recria/RecriaIngresoScreen'));
const RecriaDetailScreen = lazy(() => import('./screens/Recria/RecriaDetailScreen'));
const FormulaListScreen = lazy(() => import('./screens/Alimentacion/FormulaListScreen'));
const FormulaCreateScreen = lazy(() => import('./screens/Alimentacion/FormulaCreateScreen'));
const ConsumosScreen = lazy(() => import('./screens/Alimentacion/ConsumosScreen'));
const HistorialConsumosScreen = lazy(() => import('./screens/Alimentacion/HistorialConsumosScreen'));
const InsumosCompuestosScreen = lazy(() => import('./screens/Alimentacion/InsumosCompuestosScreen'));
const CalendarioAlimentacionPorcinos = lazy(() => import('../../components/porcinos/CalendarioAlimentacionPorcinos'));
const VentasScreen = lazy(() => import('./screens/Ventas/VentasScreen'));
const FaenaListScreen = lazy(() => import('./screens/Faena/FaenaListScreen'));
const FaenaCreateScreen = lazy(() => import('./screens/Faena/FaenaCreateScreen'));
const ConfiguracionesScreen = lazy(() => import('./screens/Configuraciones/ConfiguracionesScreen'));
const AyudaPorcinosScreen = lazy(() => import('./screens/AyudaPorcinosScreen'));
const ReportesPorcinosScreen = lazy(() => import('./screens/Reportes/ReportesPorcinosScreen'));
const EventosSanitariosScreen = lazy(() => import('./screens/EventosSanitarios/EventosSanitariosScreen'));
const InventarioPorcinosScreen = lazy(() => import('./screens/Inventario/InventarioPorcinosScreen'));
const TransferenciasScreen = lazy(() => import('./screens/Transferencias/TransferenciasScreen'));
const PlanesRecriaListScreen = lazy(() => import('./screens/PlanesRecria/PlanesRecriaListScreen'));
const ExpedienteTrazabilidadPorcinos = lazy(() =>
  import('../../components/trazabilidad/pantallasExpedientePorModulo').then(m => ({
    default: m.ExpedienteTrazabilidadPorcinos,
  }))
);

function ruta(
  path: string,
  name: string,
  component: ComponentType<any>,
  permisos?: string[]
): ModuleRoute {
  return { path, name, component, permisos };
}

export const porcinosRoutes: ModuleRoute[] = [
  ruta('/porcinos/dashboard', 'Calendario', CalendarioPorcinosDashboard),
  ruta('/porcinos/dashboard-resumen', 'Dashboard', DashboardPorcinosScreen),
  ruta('/porcinos/madres', 'Reproductores', ReproductoresScreen, ['canViewMadres', 'canViewPadrillos']),
  ruta('/porcinos/reproductores', 'Reproductores', ReproductoresScreen, ['canViewMadres', 'canViewPadrillos']),
  ruta('/porcinos/madres/nueva', 'Nueva Madre', MadreCreateScreen, ['canCreateMadres']),
  ruta('/porcinos/madres/:id', 'Detalle Madre', MadreDetailScreen, ['canViewMadres']),
  ruta('/porcinos/madres/:id/historial', 'Historial Madre', MadreHistoryScreen, ['canViewMadres']),
  ruta('/porcinos/padrillos', 'Padrillos', PadrillosListScreen, ['canViewPadrillos']),
  ruta('/porcinos/padrillos/nuevo', 'Nuevo Padrillo', PadrilloCreateScreen, ['canCreatePadrillos']),
  ruta('/porcinos/servicios', 'Servicios', ServiciosListScreen, ['canViewServicios']),
  ruta('/porcinos/servicios/nuevo', 'Nuevo Servicio', ServicioCreateScreen, ['canCreateServicios']),
  ruta('/porcinos/servicios/control-celo/:id', 'Control de Celo', ControlCeloScreen, ['canEditServicios']),
  ruta('/porcinos/gestacion', 'Gestación', GestacionListScreen, ['canViewGestacion']),
  ruta('/porcinos/gestacion/:id', 'Detalle Gestación', GestacionDetailScreen, ['canViewGestacion']),
  ruta('/porcinos/partos', 'Partos', PartosListScreen, ['canViewPartos']),
  ruta('/porcinos/partos/nuevo', 'Nuevo Parto', PartoCreateScreen, ['canCreatePartos']),
  ruta('/porcinos/partos/:id', 'Detalle Parto', PartoDetailScreen, ['canViewPartos']),
  ruta('/porcinos/destetes', 'Destetes', DestetesListScreen, ['canViewDestetes']),
  ruta('/porcinos/destetes/nuevo', 'Nuevo Destete', DesteteCreateScreen, ['canCreateDestetes']),
  ruta('/porcinos/destetes/:id', 'Detalle Destete', DesteteDetailScreen, ['canViewDestetes']),
  ruta('/porcinos/transferencias', 'Transferencias', TransferenciasScreen, ['canViewPartos']),
  ruta('/porcinos/recria', 'Recría', RecriaListScreen, ['canViewRecria']),
  ruta('/porcinos/recria/ingreso', 'Ingreso Recría', RecriaIngresoScreen, ['canCreateRecria']),
  ruta('/porcinos/recria/:id', 'Detalle Recría', RecriaDetailScreen, ['canViewRecria']),
  ruta('/porcinos/planes-recria', 'Planes de recría', PlanesRecriaListScreen, ['canViewRecria']),
  ruta('/porcinos/alimentacion/insumos-compuestos', 'Insumos Compuestos', InsumosCompuestosScreen, ['canViewAlimentacion']),
  ruta('/porcinos/alimentacion/formulas', 'Fórmulas', FormulaListScreen, ['canViewAlimentacion']),
  ruta('/porcinos/alimentacion/formulas/nueva', 'Nueva Fórmula', FormulaCreateScreen, ['canCreateFormulas']),
  ruta('/porcinos/alimentacion/consumos', 'Consumos', ConsumosScreen, ['canViewAlimentacion']),
  ruta('/porcinos/alimentacion/historial-raciones', 'Historial por fórmula', HistorialConsumosScreen, ['canViewAlimentacion']),
  ruta('/porcinos/alimentacion/calendario', 'Calendario Alimentación', CalendarioAlimentacionPorcinos, ['canViewAlimentacion']),
  ruta('/porcinos/ventas', 'Ventas y Faena', VentasScreen, ['canViewVentas', 'canViewFaena']),
  ruta('/porcinos/faena', 'Faena', FaenaListScreen, ['canViewFaena']),
  ruta('/porcinos/faena/nueva', 'Registrar Faena', FaenaCreateScreen, ['canCreateFaena']),
  ruta('/porcinos/eventos-sanitarios', 'Eventos Sanitarios', EventosSanitariosScreen, ['canViewEventosSanitarios']),
  ruta('/porcinos/inventario', 'Inventario', InventarioPorcinosScreen, ['canViewInventarioPorcinos']),
  ruta('/porcinos/configuracion', 'Configuración', ConfiguracionesScreen, ['canManageConfiguracionesPorcinos']),
  ruta('/porcinos/reportes', 'Reportes', ReportesPorcinosScreen, ['canViewReportesPorcinos']),
  ruta('/porcinos/trazabilidad-expediente', 'Expediente trazabilidad', ExpedienteTrazabilidadPorcinos, ['canViewRecria']),
  ruta('/porcinos/ayuda', 'Ayuda', AyudaPorcinosScreen, ['canViewPorcinos']),
];
