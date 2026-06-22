import { lazy, ComponentType } from 'react';
import { ModuleRoute } from '../../core/types/module.types';

const CalendarioDashboard = lazy(() => import('../../components/CalendarioDashboard'));
const FieldsManagement = lazy(() => import('../../components/FieldsManagement'));
const LotesManagement = lazy(() => import('../../components/LotesManagement'));
const CultivosManagement = lazy(() => import('../../components/CultivosManagement'));
const InsumosUnificados = lazy(() => import('../../components/InsumosUnificados'));
const MaquinariaManagement = lazy(() => import('../../components/MaquinariaManagement'));
const LaboresManagement = lazy(() => import('../../components/LaboresManagement'));
const ReportsManagement = lazy(() => import('../../components/ReportsManagement'));
const FinanzasManagement = lazy(() => import('../../components/FinanzasManagement'));
const InventarioGranosManagement = lazy(() => import('../../components/InventarioGranosManagement'));
const AdminUsuarios = lazy(() => import('../../components/AdminUsuarios'));
const AyudaSistema = lazy(() => import('../../components/AyudaSistema'));
const ConfiguracionUnificadaScreen = lazy(() => import('../../components/ConfiguracionUnificadaScreen'));
const RedireccionConfiguracionPeriodos = lazy(() => import('../../components/RedireccionConfiguracionPeriodos'));

function ruta(
  path: string,
  name: string,
  component: ComponentType<any>,
  permisos?: string[]
): ModuleRoute {
  return { path, name, component, permisos };
}

/**
 * Rutas del módulo de Cultivos (carga diferida por pantalla).
 */
export const cultivosRoutes: ModuleRoute[] = [
  ruta('/cultivos/dashboard', 'Calendario', CalendarioDashboard),
  ruta('/cultivos/campos', 'Campos', FieldsManagement, ['canViewFields']),
  ruta('/cultivos/lotes', 'Lotes', LotesManagement, ['canViewLotes']),
  ruta('/cultivos/cultivos', 'Cultivos', CultivosManagement, ['canViewCultivos']),
  ruta('/cultivos/insumos', 'Insumos', InsumosUnificados, ['canViewInsumos']),
  ruta('/cultivos/maquinaria', 'Maquinaria', MaquinariaManagement, ['canViewMaquinaria']),
  ruta('/cultivos/labores', 'Labores', LaboresManagement, ['canViewLabores']),
  ruta('/cultivos/reportes', 'Reportes', ReportsManagement, ['canViewReports']),
  ruta('/cultivos/finanzas', 'Finanzas', FinanzasManagement, ['canViewFinances']),
  ruta('/cultivos/inventario', 'Inventario Granos', InventarioGranosManagement, ['canViewInventario']),
  ruta('/cultivos/configuracion', 'Configuración', ConfiguracionUnificadaScreen, ['canManageUsers']),
  ruta('/cultivos/configuracion/campanas', 'Períodos de gestión', RedireccionConfiguracionPeriodos, ['canManageUsers']),
  ruta('/cultivos/ayuda', 'Ayuda', AyudaSistema, ['canManageUsers']),
];
