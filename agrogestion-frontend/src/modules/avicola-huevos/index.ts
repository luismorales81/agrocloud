import { ModuleConfig } from '../../core/types/module.types';
import { avicolaHuevosMenu } from './menu';
import { avicolaHuevosRoutes } from './routes';

export const avicolaHuevosModule: ModuleConfig = {
  id: 'avicola-huevos',
  nombre: 'Avícola huevos',
  descripcion: 'Postura: calendario, lotes, producción diaria, consumos, insumos y sanidad.',
  icono: 'Egg',
  color: '#eab308',
  menu: avicolaHuevosMenu,
  routes: avicolaHuevosRoutes,
};
