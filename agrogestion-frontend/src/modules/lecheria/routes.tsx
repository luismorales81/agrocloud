import React from 'react';
import { Navigate } from 'react-router-dom';
import { useModule } from '../../core/hooks/useModule';
import type { ModuleId, ModuleRoute } from '../../core/types/module.types';
import GestionPeriodosScreen from '../../components/GestionCampanasScreen';
import PanelLecheriaScreen from './pages/PanelLecheriaScreen';
import AnimalesLecheriaScreen from './pages/AnimalesLecheriaScreen';
import DetalleAnimalLecheriaScreen from './pages/DetalleAnimalLecheriaScreen';
import RegistroOrdeneScreen from './pages/RegistroOrdeneScreen';
import EstablecimientosLecheriaScreen from './pages/EstablecimientosLecheriaScreen';
import UbicacionEstablecimientosLecheriaScreen from './pages/UbicacionEstablecimientosLecheriaScreen';
import CatalogosLecheriaScreen from './pages/CatalogosLecheriaScreen';
import VentasLecheScreen from './pages/VentasLecheScreen';
import ReportesLecheriaScreen from './pages/ReportesLecheriaScreen';
import ImportControlLecheroScreen from './pages/ImportControlLecheroScreen';

const ID_MODULO: ModuleId = 'lecheria';

function envolverSiModuloLecheriaHabilitado(Componente: React.ComponentType): React.ComponentType {
  function RutaProtegidaModuloLecheria() {
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

  return RutaProtegidaModuloLecheria;
}

export const lecheriaRoutes: ModuleRoute[] = [
  {
    path: '/lecheria/dashboard',
    name: 'Panel',
    component: envolverSiModuloLecheriaHabilitado(PanelLecheriaScreen),
  },
  {
    path: '/lecheria/panel',
    name: 'Panel',
    component: envolverSiModuloLecheriaHabilitado(PanelLecheriaScreen),
  },
  {
    path: '/lecheria/animales/:id',
    name: 'Detalle animal',
    component: envolverSiModuloLecheriaHabilitado(DetalleAnimalLecheriaScreen),
  },
  {
    path: '/lecheria/animales',
    name: 'Animales',
    component: envolverSiModuloLecheriaHabilitado(AnimalesLecheriaScreen),
  },
  {
    path: '/lecheria/ordene',
    name: 'Registro de ordeñe',
    component: envolverSiModuloLecheriaHabilitado(RegistroOrdeneScreen),
  },
  {
    path: '/lecheria/ventas',
    name: 'Ventas de leche',
    component: envolverSiModuloLecheriaHabilitado(VentasLecheScreen),
  },
  {
    path: '/lecheria/establecimientos-mapa',
    name: 'Ubicación en mapa',
    component: envolverSiModuloLecheriaHabilitado(UbicacionEstablecimientosLecheriaScreen),
  },
  {
    path: '/lecheria/establecimientos',
    name: 'Establecimientos',
    component: envolverSiModuloLecheriaHabilitado(EstablecimientosLecheriaScreen),
  },
  {
    path: '/lecheria/catalogos',
    name: 'Catálogos',
    component: envolverSiModuloLecheriaHabilitado(CatalogosLecheriaScreen),
  },
  {
    path: '/lecheria/reportes',
    name: 'Reportes',
    component: envolverSiModuloLecheriaHabilitado(ReportesLecheriaScreen),
  },
  {
    path: '/lecheria/import',
    name: 'Importar control lechero',
    component: envolverSiModuloLecheriaHabilitado(ImportControlLecheroScreen),
  },
  {
    path: '/lecheria/configuracion/periodos',
    name: 'Períodos de gestión',
    component: envolverSiModuloLecheriaHabilitado(GestionPeriodosScreen),
  },
];
