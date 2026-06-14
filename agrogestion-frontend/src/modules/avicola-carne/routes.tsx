import React from 'react';
import { Navigate } from 'react-router-dom';
import { useModule } from '../../core/hooks/useModule';
import type { ModuleId, ModuleRoute } from '../../core/types/module.types';
import CalendarioDashboard from '../../components/CalendarioDashboard';
import AvicolaCarneDashboard from './pages/AvicolaCarneDashboard';
import AvicolaCarneListadoScreen from './pages/AvicolaCarneListadoScreen';
import AvicolaCarneLoteNuevoPlaceholder from './pages/AvicolaCarneLoteNuevoPlaceholder';
import AvicolaCarneDetalleLoteScreen from './pages/AvicolaCarneDetalleLoteScreen';
import { ExpedienteTrazabilidadAvicolaCarne } from '../../components/trazabilidad/pantallasExpedientePorModulo';

const ID_MODULO: ModuleId = 'avicola-carne';

/**
 * Solo renderiza la pantalla si el módulo figura entre los habilitados para la empresa
 * (lista ya filtrada en {@link ModuleContext}).
 */
function envolverSiModuloCarneHabilitado(Componente: React.ComponentType): React.ComponentType {
  function RutaProtegidaModuloCarne() {
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

  return RutaProtegidaModuloCarne;
}

export const avicolaCarneRoutes: ModuleRoute[] = [
  {
    path: '/avicola-carne/dashboard',
    name: 'Calendario',
    component: envolverSiModuloCarneHabilitado(CalendarioDashboard),
  },
  {
    path: '/avicola-carne/panel',
    name: 'Resumen',
    component: envolverSiModuloCarneHabilitado(AvicolaCarneDashboard),
  },
  {
    path: '/avicola-carne/lotes/nuevo',
    name: 'Nuevo lote',
    component: envolverSiModuloCarneHabilitado(AvicolaCarneLoteNuevoPlaceholder),
  },
  {
    path: '/avicola-carne/lotes/:id',
    name: 'Detalle lote',
    component: envolverSiModuloCarneHabilitado(AvicolaCarneDetalleLoteScreen),
  },
  {
    path: '/avicola-carne/lotes',
    name: 'Lotes',
    component: envolverSiModuloCarneHabilitado(AvicolaCarneListadoScreen),
  },
  {
    path: '/avicola-carne/trazabilidad-expediente',
    name: 'Expediente trazabilidad',
    component: envolverSiModuloCarneHabilitado(ExpedienteTrazabilidadAvicolaCarne),
  },
];
