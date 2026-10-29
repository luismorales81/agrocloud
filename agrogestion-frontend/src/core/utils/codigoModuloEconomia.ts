import { ModuleId } from '../types/module.types';

const CODIGOS: Record<ModuleId, string> = {
  cultivos: 'CULTIVOS',
  porcinos: 'PORCINOS',
  'avicola-crianza': 'AVICOLA_CRIANZA',
  'avicola-huevos': 'AVICOLA_HUEVOS',
  'avicola-ponedoras': 'AVICOLA_PONEDORAS',
  feedlot: 'FEEDLOT',
  lecheria: 'LECHERIA',
};

export function codigoModuloEconomia(modulo: ModuleId | null | undefined): string {
  if (!modulo) {
    return 'CULTIVOS';
  }
  return CODIGOS[modulo] ?? 'CULTIVOS';
}
