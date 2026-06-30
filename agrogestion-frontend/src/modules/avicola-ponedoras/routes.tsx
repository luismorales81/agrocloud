import React from 'react';
import { Navigate } from 'react-router-dom';
import { useModule } from '../../core/hooks/useModule';
import type { ModuleId, ModuleRoute } from '../../core/types/module.types';
import CalendarioPonedorasScreen from './pages/CalendarioPonedorasScreen';
import AvicolaPonedorasDashboard from './pages/AvicolaPonedorasDashboard';
import AvicolaPonedorasListado from './pages/AvicolaPonedorasListado';
import AvicolaPonedorasDetalleGalponScreen from './pages/AvicolaPonedorasDetalleGalponScreen';
import GestionPeriodosScreen from '../../components/GestionCampanasScreen';

const ID_MODULO: ModuleId = 'avicola-ponedoras';

/**
 * Solo renderiza si el módulo está en {@link useModule} (empresa con AVICOLA_PONEDORAS habilitado).
 * Distinto de avícola carne ({@code avicola-carne} / lotes).
 */
function envolverSiModuloPonedorasHabilitado(Componente: React.ComponentType): React.ComponentType {
  function RutaProtegidaModuloPonedoras() {
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

  return RutaProtegidaModuloPonedoras;
}

export const avicolaPonedorasRoutes: ModuleRoute[] = [
  {
    path: '/avicola-ponedoras/dashboard',
    name: 'Calendario',
    component: envolverSiModuloPonedorasHabilitado(CalendarioPonedorasScreen),
  },
  {
    path: '/avicola-ponedoras/panel',
    name: 'Resumen',
    component: envolverSiModuloPonedorasHabilitado(AvicolaPonedorasDashboard),
  },
  {
    path: '/avicola-ponedoras/galpones/:galponId',
    name: 'Detalle galpón',
    component: envolverSiModuloPonedorasHabilitado(AvicolaPonedorasDetalleGalponScreen),
  },
  {
    path: '/avicola-ponedoras/galpones',
    name: 'Galpones',
    component: envolverSiModuloPonedorasHabilitado(AvicolaPonedorasListado),
  },
  {
    path: '/avicola-ponedoras/configuracion/periodos',
    name: 'Períodos de gestión',
    component: envolverSiModuloPonedorasHabilitado(GestionPeriodosScreen),
  },
];
