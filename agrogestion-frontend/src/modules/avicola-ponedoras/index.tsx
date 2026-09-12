import type { ModuleConfig } from '../../core/types/module.types';
import { avicolaPonedorasMenu } from './menu';
import { avicolaPonedorasRoutes } from './routes';

export const avicolaPonedorasModule: ModuleConfig = {
  id: 'avicola-ponedoras',
  nombre: 'Avícola ponedoras',
  descripcion: 'Ponedoras y recría: galpones, postura, mortalidad, consumos, sanidad, ventas de huevos y descarte.',
  icono: 'Egg',
  color: '#7c3aed',
  menu: avicolaPonedorasMenu,
  routes: avicolaPonedorasRoutes,
  /** Consolidado en avicola-huevos; rutas legacy sin selector duplicado. */
  visibleEnSelector: false,
};

export { avicolaPonedorasMenu } from './menu';
export { avicolaPonedorasRoutes } from './routes';
