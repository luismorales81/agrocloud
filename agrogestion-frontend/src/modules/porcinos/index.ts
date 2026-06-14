import { ModuleConfig } from '../../core/types/module.types';
import { porcinosMenu } from './menu';
import { porcinosRoutes } from './routes';

/**
 * Configuración del módulo de Porcinos
 */
export const porcinosModule: ModuleConfig = {
  id: 'porcinos',
  nombre: 'Porcinos',
  descripcion: 'Gestión de producción porcina',
  icono: 'PiggyBank',
  color: '#f59e0b',
  menu: porcinosMenu,
  routes: porcinosRoutes,
};

