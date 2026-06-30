import React from 'react';
import { Navigate } from 'react-router-dom';
import { useModule } from '../../core/hooks/useModule';
import type { ModuleId, ModuleRoute } from '../../core/types/module.types';
import GestionPeriodosScreen from '../../components/GestionCampanasScreen';
import FeedlotDashboardScreen from './pages/FeedlotDashboardScreen';
import LotesFeedlotScreen from './pages/LotesFeedlotScreen';
import FeedlotLoteFormScreen from './pages/FeedlotLoteFormScreen';
import DetalleLoteFeedlotScreen from './pages/DetalleLoteFeedlotScreen';
import EstablecimientosFeedlotScreen from './pages/EstablecimientosFeedlotScreen';
import UbicacionEstablecimientosFeedlotScreen from './pages/UbicacionEstablecimientosFeedlotScreen';
import CatalogosFeedlotScreen from './pages/CatalogosFeedlotScreen';
import InsumosFeedlotScreen from './pages/InsumosFeedlotScreen';
import ReportesFeedlotScreen from './pages/ReportesFeedlotScreen';
import DietasFeedlotScreen from './pages/DietasFeedlotScreen';
import CalendarioFeedlotScreen from './pages/CalendarioFeedlotScreen';

const ID_MODULO: ModuleId = 'feedlot';

function envolverSiModuloFeedlotHabilitado(Componente: React.ComponentType): React.ComponentType {
  function RutaProtegidaModuloFeedlot() {
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

  return RutaProtegidaModuloFeedlot;
}

export const feedlotRoutes: ModuleRoute[] = [
  {
    path: '/feedlot/dashboard',
    name: 'Panel',
    component: envolverSiModuloFeedlotHabilitado(FeedlotDashboardScreen),
  },
  {
    path: '/feedlot/panel',
    name: 'Panel',
    component: envolverSiModuloFeedlotHabilitado(FeedlotDashboardScreen),
  },
  {
    path: '/feedlot/lotes/nuevo',
    name: 'Nuevo lote',
    component: envolverSiModuloFeedlotHabilitado(FeedlotLoteFormScreen),
  },
  {
    path: '/feedlot/lotes/:id/editar',
    name: 'Editar lote',
    component: envolverSiModuloFeedlotHabilitado(FeedlotLoteFormScreen),
  },
  {
    path: '/feedlot/lotes/:id',
    name: 'Detalle lote',
    component: envolverSiModuloFeedlotHabilitado(DetalleLoteFeedlotScreen),
  },
  {
    path: '/feedlot/lotes',
    name: 'Lotes',
    component: envolverSiModuloFeedlotHabilitado(LotesFeedlotScreen),
  },
  {
    path: '/feedlot/establecimientos-mapa',
    name: 'Ubicación en mapa',
    component: envolverSiModuloFeedlotHabilitado(UbicacionEstablecimientosFeedlotScreen),
  },
  {
    path: '/feedlot/establecimientos',
    name: 'Establecimientos',
    component: envolverSiModuloFeedlotHabilitado(EstablecimientosFeedlotScreen),
  },
  {
    path: '/feedlot/catalogos',
    name: 'Catálogos',
    component: envolverSiModuloFeedlotHabilitado(CatalogosFeedlotScreen),
  },
  {
    path: '/feedlot/insumos',
    name: 'Insumos',
    component: envolverSiModuloFeedlotHabilitado(InsumosFeedlotScreen),
  },
  {
    path: '/feedlot/reportes',
    name: 'Reportes',
    component: envolverSiModuloFeedlotHabilitado(ReportesFeedlotScreen),
  },
  {
    path: '/feedlot/dietas',
    name: 'Dietas',
    component: envolverSiModuloFeedlotHabilitado(DietasFeedlotScreen),
  },
  {
    path: '/feedlot/calendario',
    name: 'Calendario',
    component: envolverSiModuloFeedlotHabilitado(CalendarioFeedlotScreen),
  },
  {
    path: '/feedlot/configuracion/periodos',
    name: 'Períodos de gestión',
    component: envolverSiModuloFeedlotHabilitado(GestionPeriodosScreen),
  },
];
