import React from 'react';
import { Navigate } from 'react-router-dom';
import { ModuleRoute } from '../../core/types/module.types';
import DashboardHuevosScreen from './screens/DashboardHuevosScreen';
import EstablecimientosHuevosScreen from './screens/EstablecimientosHuevosScreen';
import RazasHuevosScreen from './screens/RazasHuevosScreen';
import UbicacionEstablecimientosHuevosScreen from './screens/UbicacionEstablecimientosHuevosScreen';
import LotesHuevosScreen from './screens/LotesHuevosScreen';
import DetalleLoteHuevosScreen from './screens/DetalleLoteHuevosScreen';
import CalendarioHuevosScreen from './screens/CalendarioHuevosScreen';
import InsumosHuevosScreen from './screens/InsumosHuevosScreen';
import ReportesHuevosScreen from './screens/ReportesHuevosScreen';
import { ExpedienteTrazabilidadAvicolaHuevos } from '../../components/trazabilidad/pantallasExpedientePorModulo';

/** Compatibilidad con enlaces antiguos a `/avicola-huevos/calendario`. (Sin JSX: este archivo es .ts) */
const RedirigirCalendarioHuevosAlDashboard = () =>
  React.createElement(Navigate, { to: '/avicola-huevos/dashboard', replace: true });

/** Antigua ruta «catálogo» unificado: ahora hay entradas separadas en el menú. */
const RedirigirCatalogoHuevosAEstablecimientos = () =>
  React.createElement(Navigate, { to: '/avicola-huevos/establecimientos', replace: true });

export const avicolaHuevosRoutes: ModuleRoute[] = [
  {
    path: '/avicola-huevos/dashboard',
    name: 'Calendario',
    component: CalendarioHuevosScreen,
  },
  {
    path: '/avicola-huevos/panel',
    name: 'Resumen',
    component: DashboardHuevosScreen,
  },
  {
    path: '/avicola-huevos/calendario',
    name: 'Calendario',
    component: RedirigirCalendarioHuevosAlDashboard,
  },
  {
    path: '/avicola-huevos/catalogo',
    name: 'Catálogo (redirección)',
    component: RedirigirCatalogoHuevosAEstablecimientos,
  },
  {
    path: '/avicola-huevos/establecimientos',
    name: 'Establecimientos',
    component: EstablecimientosHuevosScreen,
  },
  {
    path: '/avicola-huevos/establecimientos-mapa',
    name: 'Ubicación en mapa',
    component: UbicacionEstablecimientosHuevosScreen,
  },
  {
    path: '/avicola-huevos/razas',
    name: 'Razas / líneas',
    component: RazasHuevosScreen,
  },
  {
    path: '/avicola-huevos/lotes/:loteId',
    name: 'Detalle lote',
    component: DetalleLoteHuevosScreen,
  },
  {
    path: '/avicola-huevos/lotes',
    name: 'Lotes',
    component: LotesHuevosScreen,
  },
  {
    path: '/avicola-huevos/insumos',
    name: 'Insumos',
    component: InsumosHuevosScreen,
    permisos: ['canViewInsumos'],
  },
  {
    path: '/avicola-huevos/reportes',
    name: 'Reportes',
    component: ReportesHuevosScreen,
    permisos: ['canViewReports'],
  },
  {
    path: '/avicola-huevos/trazabilidad-expediente',
    name: 'Expediente trazabilidad',
    component: ExpedienteTrazabilidadAvicolaHuevos,
  },
];
