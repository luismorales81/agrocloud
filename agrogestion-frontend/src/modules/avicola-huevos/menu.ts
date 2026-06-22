import { MenuItem } from '../../core/types/module.types';

export const avicolaHuevosMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/avicola-huevos/dashboard',
  },
  {
    id: 'panel',
    nombre: 'Resumen',
    icono: 'BarChart',
    ruta: '/avicola-huevos/panel',
  },
  {
    id: 'establecimientos',
    nombre: 'Establecimientos',
    icono: 'Warehouse',
    ruta: '/avicola-huevos/establecimientos',
  },
  {
    id: 'razas',
    nombre: 'Razas / líneas',
    icono: 'Bird',
    ruta: '/avicola-huevos/razas',
  },
  {
    id: 'lotes',
    nombre: 'Lotes de postura',
    icono: 'List',
    ruta: '/avicola-huevos/lotes',
  },
  {
    id: 'insumos',
    nombre: 'Insumos',
    icono: 'Package',
    ruta: '/avicola-huevos/insumos',
    permisos: ['canViewInsumos'],
  },
  {
    id: 'reportes',
    nombre: 'Reportes',
    icono: 'TrendingUp',
    ruta: '/avicola-huevos/reportes',
    permisos: ['canViewReports'],
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/avicola-huevos/configuracion/periodos',
  },
];
