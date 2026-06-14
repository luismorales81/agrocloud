import { MenuItem } from '../../core/types/module.types';

export const avicolaCarneMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/avicola-carne/dashboard',
  },
  {
    id: 'panel',
    nombre: 'Resumen',
    icono: 'BarChart',
    ruta: '/avicola-carne/panel',
  },
  {
    id: 'lotes',
    nombre: 'Lotes',
    icono: 'List',
    ruta: '/avicola-carne/lotes',
  },
  {
    id: 'trazabilidad-expediente',
    nombre: 'Expediente trazabilidad',
    icono: 'FileText',
    ruta: '/avicola-carne/trazabilidad-expediente',
  },
];
