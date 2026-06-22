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
    id: 'galpones',
    nombre: 'Galpones',
    icono: 'List',
    ruta: '/avicola-ponedoras/galpones',
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/avicola-ponedoras/configuracion/periodos',
  },
];
