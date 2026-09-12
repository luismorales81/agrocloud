import type { ModuleConfig } from '../../core/types/module.types';
import { porcinosMenu } from './menu';
import { porcinosRoutes } from './routes';

/**
 * Configuración del módulo de Porcinos v2
 */
export const porcinosModule: ModuleConfig = {
  id: 'porcinos',
  nombre: 'Porcinos',
  descripcion: 'Gestión integral de producción porcina: reproducción, lotes, dietas, sanidad y ventas.',
  icono: 'PiggyBank',
  color: '#f59e0b',
  menu: porcinosMenu,
  routes: porcinosRoutes,
};

export { porcinosMenu } from './menu';
export { porcinosRoutes } from './routes';
export * from './typesApiV2';
export * from './services/porcinosApi';
