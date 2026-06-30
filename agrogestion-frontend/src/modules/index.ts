import { ModuleConfig } from '../core/types/module.types';
import { cultivosModule } from './cultivos';
import { porcinosModule } from './porcinos';
import { avicolaCrianzaModule } from './avicola-crianza';
import { avicolaHuevosModule } from './avicola-huevos';
import { avicolaPonedorasModule } from './avicola-ponedoras';
import { feedlotModule } from './feedlot';
import { lecheriaModule } from './lecheria';

/**
 * Registro centralizado de todos los módulos disponibles
 * Para agregar un nuevo módulo, simplemente importarlo y agregarlo a este array
 */
export const availableModules: ModuleConfig[] = [
  cultivosModule,
  porcinosModule,
  avicolaCrianzaModule,
  avicolaHuevosModule,
  avicolaPonedorasModule,
  feedlotModule,
  lecheriaModule,
];

/**
 * Obtener la configuración de un módulo por su ID
 */
export const getModuleById = (moduleId: string): ModuleConfig | undefined => {
  return availableModules.find((module) => module.id === moduleId);
};

/**
 * Obtener todas las rutas de un módulo
 */
export const getModuleRoutes = (moduleId: string) => {
  const module = getModuleById(moduleId);
  return module?.routes || [];
};

/**
 * Obtener el menú de un módulo
 */
export const getModuleMenu = (moduleId: string) => {
  const module = getModuleById(moduleId);
  return module?.menu || [];
};

