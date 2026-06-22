import type { ModuleConfig } from '../../core/types/module.types';
import { feedlotMenu } from './menu';
import { feedlotRoutes } from './routes';

export const feedlotModule: ModuleConfig = {
  id: 'feedlot',
  nombre: 'Engorde a corral (Feedlot)',
  descripcion:
    'Engorde bovino en confinamiento: lotes por corral, pesadas, consumos, sanidad, mortalidad y ventas/faenas.',
  icono: 'Beef',
  color: '#78350f',
  menu: feedlotMenu,
  routes: feedlotRoutes,
};

export { feedlotMenu } from './menu';
export { feedlotRoutes } from './routes';
