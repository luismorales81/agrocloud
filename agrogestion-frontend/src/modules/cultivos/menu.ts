import { MenuItem } from '../../core/types/module.types';

/**
 * Menú del módulo de Cultivos
 * Nota: Los iconos ahora son nombres de iconos de Lucide que se renderizan en el componente ModularSidebar
 */
export const cultivosMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/cultivos/dashboard',
  },
  {
    id: 'fields',
    nombre: 'Campos',
    icono: 'Wheat',
    ruta: '/cultivos/campos',
    permisos: ['canViewFields'],
  },
  {
    id: 'plots',
    nombre: 'Lotes',
    icono: 'MapPin',
    ruta: '/cultivos/lotes',
    permisos: ['canViewLotes'],
  },
  {
    id: 'crops',
    nombre: 'Cultivos',
    icono: 'Sprout',
    ruta: '/cultivos/cultivos',
    permisos: ['canViewCultivos'],
  },
  {
    id: 'insumos',
    nombre: 'Insumos',
    icono: 'Package',
    ruta: '/cultivos/insumos',
    permisos: ['canViewInsumos'],
  },
  {
    id: 'machinery',
    nombre: 'Maquinaria',
    icono: 'Tractor',
    ruta: '/cultivos/maquinaria',
    permisos: ['canViewMaquinaria'],
  },
  {
    id: 'labors',
    nombre: 'Labores',
    icono: 'Wrench',
    ruta: '/cultivos/labores',
    permisos: ['canViewLabores'],
  },
  {
    id: 'reports',
    nombre: 'Reportes',
    icono: 'TrendingUp',
    ruta: '/cultivos/reportes',
    permisos: ['canViewReports'],
  },
  {
    id: 'finances',
    nombre: 'Finanzas',
    icono: 'DollarSign',
    ruta: '/cultivos/finanzas',
    permisos: ['canViewFinances'],
  },
  {
    id: 'inventory',
    nombre: 'Inventario Granos',
    icono: 'Package',
    ruta: '/cultivos/inventario',
    permisos: ['canViewInventario'],
  },
  {
    id: 'configuracion',
    nombre: 'Configuración',
    icono: 'Settings',
    ruta: '/cultivos/configuracion',
    permisos: ['canManageUsers'],
  },
  {
    id: 'ayuda',
    nombre: 'Ayuda',
    icono: 'Book',
    ruta: '/cultivos/ayuda',
    permisos: ['canManageUsers'],
  },
];

