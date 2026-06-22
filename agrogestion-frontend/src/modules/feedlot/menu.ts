import { MenuItem } from '../../core/types/module.types';

export const feedlotMenu: MenuItem[] = [
  {
    id: 'panel',
    nombre: 'Panel',
    icono: 'BarChart',
    ruta: '/feedlot/panel',
  },
  {
    id: 'lotes',
    nombre: 'Lotes de engorde',
    icono: 'List',
    ruta: '/feedlot/lotes',
  },
  {
    id: 'reportes',
    nombre: 'Reportes',
    icono: 'BarChart',
    ruta: '/feedlot/reportes',
  },
  {
    id: 'dietas',
    nombre: 'Dietas',
    icono: 'Utensils',
    ruta: '/feedlot/dietas',
  },
  {
    id: 'calendario',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/feedlot/calendario',
  },
  {
    id: 'establecimientos',
    nombre: 'Establecimientos y corrales',
    icono: 'Warehouse',
    ruta: '/feedlot/establecimientos',
  },
  {
    id: 'catalogos',
    nombre: 'Catálogos',
    icono: 'BookOpen',
    ruta: '/feedlot/catalogos',
  },
  {
    id: 'insumos',
    nombre: 'Insumos',
    icono: 'Package',
    ruta: '/feedlot/insumos',
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/feedlot/configuracion/periodos',
  },
  {
    id: 'expediente',
    nombre: 'Expediente',
    icono: 'FileText',
    ruta: '/feedlot/expediente',
  },
  {
    id: 'config-closeout',
    nombre: 'Closeout',
    icono: 'Settings',
    ruta: '/feedlot/configuracion/closeout',
  },
];
