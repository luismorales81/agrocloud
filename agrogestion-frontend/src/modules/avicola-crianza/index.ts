import { ModuleConfig } from '../../core/types/module.types';
import { avicolaCrianzaMenu } from './menu';
import { avicolaCrianzaRoutes } from './routes';

export const avicolaCrianzaModule: ModuleConfig = {
  id: 'avicola-crianza',
  nombre: 'Avícola crianza',
  descripcion:
    'Crianza: establecimientos (con mapa), razas, insumos, lotes, pesadas, mortalidad, ventas, consumos y sanidad.',
  icono: 'Bird',
  color: '#0ea5e9',
  menu: avicolaCrianzaMenu,
  routes: avicolaCrianzaRoutes,
};
