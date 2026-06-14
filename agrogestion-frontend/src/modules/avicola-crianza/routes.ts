import React from 'react';
import { Navigate } from 'react-router-dom';
import { ModuleRoute } from '../../core/types/module.types';
import CalendarioDashboard from '../../components/CalendarioDashboard';
import DashboardCrianzaScreen from './screens/DashboardCrianzaScreen';
import EstablecimientosCrianzaScreen from './screens/EstablecimientosCrianzaScreen';
import RazasCrianzaScreen from './screens/RazasCrianzaScreen';
import UbicacionEstablecimientosCrianzaScreen from './screens/UbicacionEstablecimientosCrianzaScreen';
import InsumosCrianzaScreen from './screens/InsumosCrianzaScreen';
import LotesCrianzaScreen from './screens/LotesCrianzaScreen';
import DetalleLoteCrianzaScreen from './screens/DetalleLoteCrianzaScreen';
import { ExpedienteTrazabilidadAvicolaCrianza } from '../../components/trazabilidad/pantallasExpedientePorModulo';

const RedirigirCatalogoCrianzaAEstablecimientos = () =>
  React.createElement(Navigate, { to: '/avicola-crianza/establecimientos', replace: true });

export const avicolaCrianzaRoutes: ModuleRoute[] = [
  {
    path: '/avicola-crianza/dashboard',
    name: 'Calendario',
    component: CalendarioDashboard,
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
    path: '/avicola-crianza/trazabilidad-expediente',
    name: 'Expediente trazabilidad',
    component: ExpedienteTrazabilidadAvicolaCrianza,
  },
];
