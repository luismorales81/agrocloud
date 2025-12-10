import { ModuleRoute } from '../../core/types/module.types';
import FieldsManagement from '../../components/FieldsManagement';
import LotesManagement from '../../components/LotesManagement';
import CultivosManagement from '../../components/CultivosManagement';
import InsumosUnificados from '../../components/InsumosUnificados';
import MaquinariaManagement from '../../components/MaquinariaManagement';
import LaboresManagement from '../../components/LaboresManagement';
import ReportsManagement from '../../components/ReportsManagement';
import FinanzasManagement from '../../components/FinanzasManagement';
import InventarioGranosManagement from '../../components/InventarioGranosManagement';
import CalendarioDashboard from '../../components/CalendarioDashboard';
import AdminUsuarios from '../../components/AdminUsuarios';
import AyudaSistema from '../../components/AyudaSistema';

/**
 * Rutas del módulo de Cultivos
 */
export const cultivosRoutes: ModuleRoute[] = [
  {
    path: '/cultivos/dashboard',
    name: 'Dashboard',
    component: CalendarioDashboard,
  },
  {
    path: '/cultivos/campos',
    name: 'Campos',
    component: FieldsManagement,
    permisos: ['canViewFields'],
  },
  {
    path: '/cultivos/lotes',
    name: 'Lotes',
    component: LotesManagement,
    permisos: ['canViewLotes'],
  },
  {
    path: '/cultivos/cultivos',
    name: 'Cultivos',
    component: CultivosManagement,
    permisos: ['canViewCultivos'],
  },
  {
    path: '/cultivos/insumos',
    name: 'Insumos',
    component: InsumosUnificados,
    permisos: ['canViewInsumos'],
  },
  {
    path: '/cultivos/maquinaria',
    name: 'Maquinaria',
    component: MaquinariaManagement,
    permisos: ['canViewMaquinaria'],
  },
  {
    path: '/cultivos/labores',
    name: 'Labores',
    component: LaboresManagement,
    permisos: ['canViewLabores'],
  },
  {
    path: '/cultivos/reportes',
    name: 'Reportes',
    component: ReportsManagement,
    permisos: ['canViewReports'],
  },
  {
    path: '/cultivos/finanzas',
    name: 'Finanzas',
    component: FinanzasManagement,
    permisos: ['canViewFinances'],
  },
  {
    path: '/cultivos/inventario',
    name: 'Inventario Granos',
    component: InventarioGranosManagement,
    permisos: ['canViewInventario'],
  },
  {
    path: '/cultivos/usuarios',
    name: 'Usuarios',
    component: AdminUsuarios,
    permisos: ['canManageUsers'],
  },
  {
    path: '/cultivos/ayuda',
    name: 'Ayuda',
    component: AyudaSistema,
    permisos: ['canManageUsers'],
  },
];

