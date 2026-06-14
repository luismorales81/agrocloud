import type { ModuleConfig } from '../../core/types/module.types';
import { avicolaCarneMenu } from './menu';
import { avicolaCarneRoutes } from './routes';

export const avicolaCarneModule: ModuleConfig = {
  id: 'avicola-carne',
  nombre: 'Avícola carne',
  descripcion:
    'Engorde / carne: panel y lotes (API propia). Para catálogo de establecimientos, mapa e insumos de crianza usá el módulo «Avícola crianza».',
  icono: 'Drumstick',
  color: '#dc2626',
  menu: avicolaCarneMenu,
  routes: avicolaCarneRoutes,
};

export { avicolaCarneMenu } from './menu';
export { avicolaCarneRoutes } from './routes';
