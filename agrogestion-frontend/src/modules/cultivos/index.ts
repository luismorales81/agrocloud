import { ModuleConfig } from '../../core/types/module.types';
import { cultivosMenu } from './menu';
import { cultivosRoutes } from './routes';

/**
 * Configuración del módulo de Cultivos
 */
export const cultivosModule: ModuleConfig = {
  id: 'cultivos',
  nombre: 'Cultivos',
  descripcion: 'Gestión de campos, lotes, cultivos y labores',
  icono: 'Wheat',
  color: '#10b981',
  menu: cultivosMenu,
  routes: cultivosRoutes,
};

