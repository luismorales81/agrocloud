import React, { lazy, ComponentType } from 'react';
import { Navigate } from 'react-router-dom';
import { useModule } from '../../core/hooks/useModule';
import type { ModuleId, ModuleRoute } from '../../core/types/module.types';
import GestionPeriodosScreen from '../../components/GestionCampanasScreen';
import PanelPorcinosScreen from './pages/PanelPorcinosScreen';
import CalendarioPorcinosScreen from './pages/CalendarioPorcinosScreen';
import ReproduccionHubScreen from './pages/ReproduccionHubScreen';
import PorcinosLoteFormScreen from './pages/PorcinosLoteFormScreen';
import DetalleMadrePorcinosScreen from './pages/DetalleMadrePorcinosScreen';
import LotesPorcinosScreen from './pages/LotesPorcinosScreen';
import DetalleLotePorcinosScreen from './pages/DetalleLotePorcinosScreen';
import DietasPorcinosScreen from './pages/DietasPorcinosScreen';
import InsumosPorcinosScreen from './pages/InsumosPorcinosScreen';
import VentasPorcinosScreen from './pages/VentasPorcinosScreen';
import SanidadPorcinosScreen from './pages/SanidadPorcinosScreen';
import EstablecimientosPorcinosScreen from './pages/EstablecimientosPorcinosScreen';
import UbicacionEstablecimientosPorcinosScreen from './pages/UbicacionEstablecimientosPorcinosScreen';
import CatalogosPorcinosScreen from './pages/CatalogosPorcinosScreen';
import ReportesPorcinosScreen from './pages/ReportesPorcinosScreen';

// —— Pantallas legacy (deep links) ——
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
const VentasScreenLegacy = lazy(() => import('./screens/Ventas/VentasScreen'));
const FaenaListScreen = lazy(() => import('./screens/Faena/FaenaListScreen'));
const FaenaCreateScreen = lazy(() => import('./screens/Faena/FaenaCreateScreen'));
const ConfiguracionesScreen = lazy(() => import('./screens/Configuraciones/ConfiguracionesScreen'));
const AyudaPorcinosScreen = lazy(() => import('./screens/AyudaPorcinosScreen'));
const ReportesPorcinosScreenLegacy = lazy(() => import('./screens/Reportes/ReportesPorcinosScreen'));
const EventosSanitariosScreen = lazy(() => import('./screens/EventosSanitarios/EventosSanitariosScreen'));
const InventarioPorcinosScreen = lazy(() => import('./screens/Inventario/InventarioPorcinosScreen'));
const TransferenciasScreen = lazy(() => import('./screens/Transferencias/TransferenciasScreen'));
const PlanesRecriaListScreen = lazy(() => import('./screens/PlanesRecria/PlanesRecriaListScreen'));

const ID_MODULO: ModuleId = 'porcinos';

function envolverSiModuloPorcinosHabilitado(Componente: React.ComponentType): React.ComponentType {
  function RutaProtegidaModuloPorcinos() {
    const { availableModules, loading } = useModule();

    if (loading) {
      return (
        <div style={{ padding: 24, textAlign: 'center', color: '#6b7280' }}>
          Cargando módulo…
        </div>
      );
    }

    const habilitado = availableModules.some((m) => m.id === ID_MODULO);
    if (!habilitado) {
      return <Navigate to="/select-module" replace />;
    }

    return <Componente />;
  }

  return RutaProtegidaModuloPorcinos;
}

const RedirigirMadresAReproduccion = () =>
  React.createElement(Navigate, { to: '/porcinos/reproduccion', replace: true });

const RedirigirRecriaALotes = () =>
  React.createElement(Navigate, { to: '/porcinos/lotes', replace: true });

function rutaLegacy(
  path: string,
  name: string,
  component: ComponentType<any>,
  permisos?: string[]
): ModuleRoute {
  return {
    path,
    name,
    component: envolverSiModuloPorcinosHabilitado(component),
    permisos,
  };
}

export const porcinosRoutes: ModuleRoute[] = [
  // —— v2: rutas principales ——
  {
    path: '/porcinos/panel',
    name: 'Panel',
    component: envolverSiModuloPorcinosHabilitado(PanelPorcinosScreen),
  },
  {
    path: '/porcinos/dashboard',
    name: 'Panel',
    component: envolverSiModuloPorcinosHabilitado(PanelPorcinosScreen),
  },
  {
    path: '/porcinos/calendario',
    name: 'Calendario',
    component: envolverSiModuloPorcinosHabilitado(CalendarioPorcinosScreen),
  },
  {
    path: '/porcinos/reproduccion',
    name: 'Reproducción',
    component: envolverSiModuloPorcinosHabilitado(ReproduccionHubScreen),
  },
  {
    path: '/porcinos/madres/:id',
    name: 'Detalle madre',
    component: envolverSiModuloPorcinosHabilitado(DetalleMadrePorcinosScreen),
  },
  {
    path: '/porcinos/lotes/nuevo',
    name: 'Nuevo lote',
    component: envolverSiModuloPorcinosHabilitado(PorcinosLoteFormScreen),
  },
  {
    path: '/porcinos/lotes/:id/editar',
    name: 'Editar lote',
    component: envolverSiModuloPorcinosHabilitado(PorcinosLoteFormScreen),
  },
  {
    path: '/porcinos/lotes/:id',
    name: 'Detalle lote',
    component: envolverSiModuloPorcinosHabilitado(DetalleLotePorcinosScreen),
  },
  {
    path: '/porcinos/lotes',
    name: 'Lotes de engorde',
    component: envolverSiModuloPorcinosHabilitado(LotesPorcinosScreen),
  },
  {
    path: '/porcinos/dietas',
    name: 'Dietas',
    component: envolverSiModuloPorcinosHabilitado(DietasPorcinosScreen),
  },
  {
    path: '/porcinos/insumos',
    name: 'Insumos',
    component: envolverSiModuloPorcinosHabilitado(InsumosPorcinosScreen),
  },
  {
    path: '/porcinos/ventas',
    name: 'Ventas',
    component: envolverSiModuloPorcinosHabilitado(VentasPorcinosScreen),
  },
  {
    path: '/porcinos/sanidad',
    name: 'Sanidad',
    component: envolverSiModuloPorcinosHabilitado(SanidadPorcinosScreen),
  },
  {
    path: '/porcinos/establecimientos-mapa',
    name: 'Ubicación en mapa',
    component: envolverSiModuloPorcinosHabilitado(UbicacionEstablecimientosPorcinosScreen),
  },
  {
    path: '/porcinos/establecimientos',
    name: 'Establecimientos',
    component: envolverSiModuloPorcinosHabilitado(EstablecimientosPorcinosScreen),
  },
  {
    path: '/porcinos/catalogos',
    name: 'Catálogos',
    component: envolverSiModuloPorcinosHabilitado(CatalogosPorcinosScreen),
  },
  {
    path: '/porcinos/reportes',
    name: 'Reportes',
    component: envolverSiModuloPorcinosHabilitado(ReportesPorcinosScreen),
  },
  {
    path: '/porcinos/configuracion/periodos',
    name: 'Períodos de gestión',
    component: envolverSiModuloPorcinosHabilitado(GestionPeriodosScreen),
  },

  // —— Redirects legacy ——
  {
    path: '/porcinos/madres',
    name: 'Reproductores (redirección)',
    component: envolverSiModuloPorcinosHabilitado(RedirigirMadresAReproduccion),
  },
  {
    path: '/porcinos/recria',
    name: 'Recría (redirección)',
    component: envolverSiModuloPorcinosHabilitado(RedirigirRecriaALotes),
  },

  // —— Legacy: deep links y pantallas antiguas ——
  rutaLegacy('/porcinos/dashboard-resumen', 'Dashboard legacy', DashboardPorcinosScreen),
  rutaLegacy('/porcinos/reproductores', 'Reproductores', ReproductoresScreen, ['canViewMadres', 'canViewPadrillos']),
  rutaLegacy('/porcinos/madres/nueva', 'Nueva Madre', MadreCreateScreen, ['canCreateMadres']),
  rutaLegacy('/porcinos/madres/:id/historial', 'Historial Madre', MadreHistoryScreen, ['canViewMadres']),
  rutaLegacy('/porcinos/madres/:id', 'Detalle Madre', MadreDetailScreen, ['canViewMadres']),
  rutaLegacy('/porcinos/padrillos', 'Padrillos', PadrillosListScreen, ['canViewPadrillos']),
  rutaLegacy('/porcinos/padrillos/nuevo', 'Nuevo Padrillo', PadrilloCreateScreen, ['canCreatePadrillos']),
  rutaLegacy('/porcinos/servicios', 'Servicios', ServiciosListScreen, ['canViewServicios']),
  rutaLegacy('/porcinos/servicios/nuevo', 'Nuevo Servicio', ServicioCreateScreen, ['canCreateServicios']),
  rutaLegacy('/porcinos/servicios/control-celo/:id', 'Control de Celo', ControlCeloScreen, ['canEditServicios']),
  rutaLegacy('/porcinos/gestacion', 'Gestación', GestacionListScreen, ['canViewGestacion']),
  rutaLegacy('/porcinos/gestacion/:id', 'Detalle Gestación', GestacionDetailScreen, ['canViewGestacion']),
  rutaLegacy('/porcinos/partos', 'Partos', PartosListScreen, ['canViewPartos']),
  rutaLegacy('/porcinos/partos/nuevo', 'Nuevo Parto', PartoCreateScreen, ['canCreatePartos']),
  rutaLegacy('/porcinos/partos/:id', 'Detalle Parto', PartoDetailScreen, ['canViewPartos']),
  rutaLegacy('/porcinos/destetes', 'Destetes', DestetesListScreen, ['canViewDestetes']),
  rutaLegacy('/porcinos/destetes/nuevo', 'Nuevo Destete', DesteteCreateScreen, ['canCreateDestetes']),
  rutaLegacy('/porcinos/destetes/:id', 'Detalle Destete', DesteteDetailScreen, ['canViewDestetes']),
  rutaLegacy('/porcinos/transferencias', 'Transferencias', TransferenciasScreen, ['canViewPartos']),
  rutaLegacy('/porcinos/recria/ingreso', 'Ingreso Recría', RecriaIngresoScreen, ['canCreateRecria']),
  rutaLegacy('/porcinos/recria/:id', 'Detalle Recría', RecriaDetailScreen, ['canViewRecria']),
  rutaLegacy('/porcinos/planes-recria', 'Planes de recría', PlanesRecriaListScreen, ['canViewRecria']),
  rutaLegacy('/porcinos/alimentacion/insumos-compuestos', 'Insumos Compuestos', InsumosCompuestosScreen, ['canViewAlimentacion']),
  rutaLegacy('/porcinos/alimentacion/formulas', 'Fórmulas', FormulaListScreen, ['canViewAlimentacion']),
  rutaLegacy('/porcinos/alimentacion/formulas/nueva', 'Nueva Fórmula', FormulaCreateScreen, ['canCreateFormulas']),
  rutaLegacy('/porcinos/alimentacion/consumos', 'Consumos', ConsumosScreen, ['canViewAlimentacion']),
  rutaLegacy('/porcinos/alimentacion/historial-raciones', 'Historial por fórmula', HistorialConsumosScreen, ['canViewAlimentacion']),
  rutaLegacy('/porcinos/alimentacion/calendario', 'Calendario Alimentación', CalendarioAlimentacionPorcinos, ['canViewAlimentacion']),
  rutaLegacy('/porcinos/ventas-legacy', 'Ventas legacy', VentasScreenLegacy, ['canViewVentas', 'canViewFaena']),
  rutaLegacy('/porcinos/faena', 'Faena', FaenaListScreen, ['canViewFaena']),
  rutaLegacy('/porcinos/faena/nueva', 'Registrar Faena', FaenaCreateScreen, ['canCreateFaena']),
  rutaLegacy('/porcinos/eventos-sanitarios', 'Eventos Sanitarios', EventosSanitariosScreen, ['canViewEventosSanitarios']),
  rutaLegacy('/porcinos/inventario', 'Inventario', InventarioPorcinosScreen, ['canViewInventarioPorcinos']),
  rutaLegacy('/porcinos/configuracion', 'Configuración', ConfiguracionesScreen, ['canManageConfiguracionesPorcinos']),
  rutaLegacy('/porcinos/reportes-legacy', 'Reportes legacy', ReportesPorcinosScreenLegacy, ['canViewReportesPorcinos']),
  rutaLegacy('/porcinos/ayuda', 'Ayuda', AyudaPorcinosScreen, ['canViewPorcinos']),
];
