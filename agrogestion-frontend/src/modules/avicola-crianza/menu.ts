import { MenuItem } from '../../core/types/module.types';

export const avicolaCrianzaMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/avicola-crianza/dashboard',
  },
  {
    id: 'panel',
    nombre: 'Resumen',
    icono: 'BarChart',
    ruta: '/avicola-crianza/panel',
  },
  {
    id: 'establecimientos',
    nombre: 'Establecimientos',
    icono: 'Warehouse',
    ruta: '/avicola-crianza/establecimientos',
  },
  {
    id: 'razas',
    nombre: 'Razas',
    icono: 'Bird',
    ruta: '/avicola-crianza/razas',
  },
  {
    id: 'lotes',
    nombre: 'Lotes',
    icono: 'List',
    ruta: '/avicola-crianza/lotes',
  },
  {
    id: 'insumos',
    nombre: 'Insumos',
    icono: 'Package',
    ruta: '/avicola-crianza/insumos',
    permisos: ['canViewInsumos'],
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/avicola-crianza/configuracion/periodos',
  },
];
