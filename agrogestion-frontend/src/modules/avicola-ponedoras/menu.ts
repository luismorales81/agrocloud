import { MenuItem } from '../../core/types/module.types';

/** Menú lateral del módulo (solo se muestra si la empresa tiene AVICOLA_PONEDORAS). */
export const avicolaPonedorasMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/avicola-ponedoras/dashboard',
  },
  {
    id: 'panel',
    nombre: 'Resumen',
    icono: 'BarChart',
    ruta: '/avicola-ponedoras/panel',
  },
  {
    id: 'razas',
    nombre: 'Razas',
    icono: 'Bird',
    ruta: '/avicola-ponedoras/razas',
  },
  {
    id: 'galpones',
    nombre: 'Galpones',
    icono: 'List',
    ruta: '/avicola-ponedoras/galpones',
  },
  {
    id: 'finanzas',
    nombre: 'Finanzas',
    icono: 'DollarSign',
    ruta: '/avicola-ponedoras/finanzas',
    permisos: ['canViewFinances'],
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/avicola-ponedoras/configuracion/periodos',
  },
];
