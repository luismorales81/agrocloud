import React from 'react';
import { Navigate } from 'react-router-dom';
import { ModuleRoute } from '../../core/types/module.types';
import CalendarioCrianzaScreen from './screens/CalendarioCrianzaScreen';
import DashboardCrianzaScreen from './screens/DashboardCrianzaScreen';
import EstablecimientosCrianzaScreen from './screens/EstablecimientosCrianzaScreen';
import RazasCrianzaScreen from './screens/RazasCrianzaScreen';
import UbicacionEstablecimientosCrianzaScreen from './screens/UbicacionEstablecimientosCrianzaScreen';
import InsumosCrianzaScreen from './screens/InsumosCrianzaScreen';
import LotesCrianzaScreen from './screens/LotesCrianzaScreen';
import DetalleLoteCrianzaScreen from './screens/DetalleLoteCrianzaScreen';
import ReportesCrianzaScreen from './screens/ReportesCrianzaScreen';
import GestionPeriodosScreen from '../../components/GestionCampanasScreen';

const RedirigirCatalogoCrianzaAEstablecimientos = () =>
  React.createElement(Navigate, { to: '/avicola-crianza/establecimientos', replace: true });

export const avicolaCrianzaRoutes: ModuleRoute[] = [
  {
    path: '/avicola-crianza/dashboard',
    name: 'Calendario',
    component: CalendarioCrianzaScreen,
  },
  {
    path: '/avicola-crianza/panel',
    name: 'Resumen',
    component: DashboardCrianzaScreen,
  },
  {
    path: '/avicola-crianza/catalogo',
    name: 'Catálogo (redirección)',
    component: RedirigirCatalogoCrianzaAEstablecimientos,
  },
  {
    path: '/avicola-crianza/establecimientos',
    name: 'Establecimientos',
    component: EstablecimientosCrianzaScreen,
  },
  {
    path: '/avicola-crianza/establecimientos-mapa',
    name: 'Ubicación en mapa',
    component: UbicacionEstablecimientosCrianzaScreen,
  },
  {
    path: '/avicola-crianza/razas',
    name: 'Razas',
    component: RazasCrianzaScreen,
  },
  {
    path: '/avicola-crianza/lotes/:loteId',
    name: 'Detalle lote',
    component: DetalleLoteCrianzaScreen,
  },
  {
    path: '/avicola-crianza/lotes',
    name: 'Lotes',
    component: LotesCrianzaScreen,
  },
  {
    path: '/avicola-crianza/insumos',
    name: 'Insumos',
    component: InsumosCrianzaScreen,
    permisos: ['canViewInsumos'],
  },
  {
    path: '/avicola-crianza/reportes',
    name: 'Reportes',
    component: ReportesCrianzaScreen,
  },
  {
    path: '/avicola-crianza/configuracion/periodos',
    name: 'Períodos de gestión',
    component: GestionPeriodosScreen,
  },
];
