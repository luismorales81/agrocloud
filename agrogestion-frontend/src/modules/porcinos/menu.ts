import { MenuItem } from '../../core/types/module.types';

/**
 * Menú del módulo de Porcinos
 * Nota: Los iconos ahora son nombres de iconos de Lucide que se renderizan en el componente ModularSidebar
 */
export const porcinosMenu: MenuItem[] = [
  {
    id: 'dashboard',
    nombre: 'Calendario',
    icono: 'CalendarDays',
    ruta: '/porcinos/dashboard',
  },
  {
    id: 'reproductores',
    nombre: 'Reproductores',
    icono: 'PiggyBank',
    ruta: '/porcinos/madres',
    permisos: ['canViewMadres', 'canViewPadrillos'],
  },
  {
    id: 'servicios',
    nombre: 'Servicios',
    icono: 'Heart',
    ruta: '/porcinos/servicios',
    permisos: ['canViewServicios'],
  },
  {
    id: 'gestacion-partos',
    nombre: 'Gestación y Partos',
    icono: 'Baby',
    ruta: '/porcinos/gestacion',
    permisos: ['canViewGestacion', 'canViewPartos'],
  },
  {
    id: 'destetes',
    nombre: 'Destetes',
    icono: 'Baby',
    ruta: '/porcinos/destetes',
    permisos: ['canViewDestetes'],
  },
  {
    id: 'transferencias',
    nombre: 'Transferencias',
    icono: 'RefreshCw',
    ruta: '/porcinos/transferencias',
    permisos: ['canViewPartos'],
  },
  {
    id: 'recria',
    nombre: 'Recría',
    icono: 'Circle',
    ruta: '/porcinos/recria',
    permisos: ['canViewRecria'],
  },
  {
    id: 'planes-recria',
    nombre: 'Planes de recría',
    icono: 'Layers',
    ruta: '/porcinos/planes-recria',
    permisos: ['canViewRecria'],
  },
    {
      id: 'alimentacion',
      nombre: 'Alimentación',
      icono: 'HelpCircle',
      ruta: '/porcinos/alimentacion/insumos-compuestos',
      permisos: ['canViewAlimentacion'],
    },
    {
      id: 'alimentacion-historial-formulas',
      nombre: 'Historial por fórmula',
      icono: 'BarChart',
      ruta: '/porcinos/alimentacion/historial-raciones',
      permisos: ['canViewAlimentacion'],
    },
    {
      id: 'alimentacion-calendario',
      nombre: 'Calendario diario',
      icono: 'Calendar',
      ruta: '/porcinos/alimentacion/calendario',
      permisos: ['canViewAlimentacion'],
    },
    {
      id: 'ventas',
      nombre: 'Ventas y Faena',
      icono: 'DollarSign',
      ruta: '/porcinos/ventas',
      permisos: ['canViewVentas', 'canViewFaena'],
    },
    {
      id: 'eventos-sanitarios',
      nombre: 'Eventos Sanitarios',
      icono: 'Syringe',
      ruta: '/porcinos/eventos-sanitarios',
      permisos: ['canViewEventosSanitarios'],
    },
    {
      id: 'inventario',
      nombre: 'Inventario',
      icono: 'Package',
      ruta: '/porcinos/inventario',
      permisos: ['canViewInventarioPorcinos'],
    },
    {
      id: 'configuracion',
      nombre: 'Configuración',
      icono: 'Settings',
      ruta: '/porcinos/configuracion',
      permisos: ['canManageConfiguracionesPorcinos'],
    },
    {
      id: 'reportes',
      nombre: 'Reportes',
      icono: 'BarChart',
      ruta: '/porcinos/reportes',
      permisos: ['canViewReportesPorcinos'],
    },
    {
      id: 'trazabilidad-expediente',
      nombre: 'Expediente trazabilidad',
      icono: 'FileText',
      ruta: '/porcinos/trazabilidad-expediente',
      permisos: ['canViewRecria'],
    },
    {
      id: 'ayuda',
      nombre: 'Ayuda',
      icono: 'Book',
      ruta: '/porcinos/ayuda',
      permisos: ['canViewPorcinos'],
    },
];
