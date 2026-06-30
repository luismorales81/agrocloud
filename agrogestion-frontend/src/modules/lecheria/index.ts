import type { ModuleConfig } from '../../core/types/module.types';
import { lecheriaMenu } from './menu';
import { lecheriaRoutes } from './routes';

export const lecheriaModule: ModuleConfig = {
  id: 'lecheria',
  nombre: 'Lechería',
  descripcion:
    'Gestión lechera: animales, ordeñes, reproducción, sanidad, ventas de leche y reportes de producción.',
  icono: 'Milk',
  color: '#0284c7',
  menu: lecheriaMenu,
  routes: lecheriaRoutes,
};

export { lecheriaMenu } from './menu';
export { lecheriaRoutes } from './routes';
