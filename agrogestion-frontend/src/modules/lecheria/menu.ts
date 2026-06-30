import { MenuItem } from '../../core/types/module.types';

export const lecheriaMenu: MenuItem[] = [
  {
    id: 'panel',
    nombre: 'Panel',
    icono: 'BarChart',
    ruta: '/lecheria/panel',
  },
  {
    id: 'animales',
    nombre: 'Animales',
    icono: 'List',
    ruta: '/lecheria/animales',
  },
  {
    id: 'ordene',
    nombre: 'Ordeñe',
    icono: 'Milk',
    ruta: '/lecheria/ordene',
  },
  {
    id: 'ventas',
    nombre: 'Ventas de leche',
    icono: 'DollarSign',
    ruta: '/lecheria/ventas',
  },
  {
    id: 'establecimientos',
    nombre: 'Establecimientos y rodeos',
    icono: 'Warehouse',
    ruta: '/lecheria/establecimientos',
  },
  {
    id: 'ubicacion-mapa',
    nombre: 'Ubicación en mapa',
    icono: 'MapPin',
    ruta: '/lecheria/establecimientos-mapa',
  },
  {
    id: 'catalogos',
    nombre: 'Catálogos',
    icono: 'BookOpen',
    ruta: '/lecheria/catalogos',
  },
  {
    id: 'reportes',
    nombre: 'Reportes',
    icono: 'BarChart',
    ruta: '/lecheria/reportes',
  },
  {
    id: 'import-csv',
    nombre: 'Importar control lechero',
    icono: 'Upload',
    ruta: '/lecheria/import',
  },
  {
    id: 'periodos-gestion',
    nombre: 'Períodos de gestión',
    icono: 'CalendarDays',
    ruta: '/lecheria/configuracion/periodos',
  },
];
