import { MenuItem } from '../../core/types/module.types';

/**
 * Menú del módulo de Cultivos
 */
export const cultivosMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Dashboard',
    icono: '📊',
    ruta: '/cultivos/dashboard',
  },
  {
    id: 'fields',
    nombre: 'Campos',
    icono: '🌾',
    ruta: '/cultivos/campos',
    permisos: ['canViewFields'],
  },
  {
    id: 'plots',
    nombre: 'Lotes',
    icono: '🗺️',
    ruta: '/cultivos/lotes',
    permisos: ['canViewLotes'],
  },
  {
    id: 'crops',
    nombre: 'Cultivos',
    icono: '🌱',
    ruta: '/cultivos/cultivos',
    permisos: ['canViewCultivos'],
  },
  {
    id: 'insumos',
    nombre: 'Insumos',
    icono: '🧪',
    ruta: '/cultivos/insumos',
    permisos: ['canViewInsumos'],
  },
  {
    id: 'machinery',
    nombre: 'Maquinaria',
    icono: '🚜',
    ruta: '/cultivos/maquinaria',
    permisos: ['canViewMaquinaria'],
  },
  {
    id: 'labors',
    nombre: 'Labores',
    icono: '⚒️',
    ruta: '/cultivos/labores',
    permisos: ['canViewLabores'],
  },
  {
    id: 'reports',
    nombre: 'Reportes',
    icono: '📈',
    ruta: '/cultivos/reportes',
    permisos: ['canViewReports'],
  },
  {
    id: 'finances',
    nombre: 'Finanzas',
    icono: '💰',
    ruta: '/cultivos/finanzas',
    permisos: ['canViewFinances'],
  },
  {
    id: 'inventory',
    nombre: 'Inventario Granos',
    icono: '📦',
    ruta: '/cultivos/inventario',
    permisos: ['canViewInventario'],
  },
  {
    id: 'users',
    nombre: 'Usuarios',
    icono: '👥',
    ruta: '/cultivos/usuarios',
    permisos: ['canManageUsers'],
  },
  {
    id: 'ayuda',
    nombre: 'Ayuda',
    icono: '📚',
    ruta: '/cultivos/ayuda',
    permisos: ['canManageUsers'],
  },
];

