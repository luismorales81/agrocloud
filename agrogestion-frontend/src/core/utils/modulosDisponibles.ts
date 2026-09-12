import { ModuleId, ModuleInfo } from '../types/module.types';
import { modulosParaSelector as catalogoModulosFrontend } from '../../modules';
import { availableModules as todosLosModulos } from '../../modules';

/** Módulos del catálogo frontend cuando el API no responde o la empresa aún no cargó. */
export function modulosDesdeCatalogoFrontend(): ModuleInfo[] {
  return catalogoModulosFrontend.map((m) => ({
    id: m.id,
    nombre: m.nombre,
    descripcion: m.descripcion,
    icono: m.icono,
    color: m.color,
    habilitado: true,
  }));
}

export function normalizarIdModulo(idBruto: string): ModuleId | null {
  let id = idBruto.trim();
  if (!id) return null;

  const mapa: Record<string, ModuleId> = {
    crops: 'cultivos',
    CULTIVOS: 'cultivos',
    pigs: 'porcinos',
    PORCINOS: 'porcinos',
    avicola_crianza: 'avicola-crianza',
    AVICOLA_CRIANZA: 'avicola-crianza',
    avicola_carne: 'avicola-crianza',
    AVICOLA_CARNE: 'avicola-crianza',
    avicola_huevos: 'avicola-huevos',
    AVICOLA_HUEVOS: 'avicola-huevos',
    avicola_ponedoras: 'avicola-ponedoras',
    AVICOLA_PONEDORAS: 'avicola-ponedoras',
    FEEDLOT: 'feedlot',
    feedlot: 'feedlot',
    LECHERIA: 'lecheria',
    lecheria: 'lecheria',
  };

  if (mapa[id]) {
    return mapa[id];
  }
  if (todosLosModulos.some((m) => m.id === id)) {
    return id as ModuleId;
  }
  return null;
}

export function fusionarModulosApiConCatalogo(
  datosApi: Record<string, unknown>[]
): ModuleInfo[] {
  const habilitadosApi = new Map<ModuleId, ModuleInfo>();

  for (const fila of datosApi) {
    const idNormalizado = normalizarIdModulo(String(fila.id ?? ''));
    if (!idNormalizado) continue;
    if (!catalogoModulosFrontend.some((m) => m.id === idNormalizado)) continue;

    const cfg = catalogoModulosFrontend.find((m) => m.id === idNormalizado);
    const existente = habilitadosApi.get(idNormalizado);
    const habilitado = fila.habilitado !== false;

    if (!existente || (habilitado && !existente.habilitado)) {
      habilitadosApi.set(idNormalizado, {
        id: idNormalizado,
        nombre: cfg?.nombre ?? String(fila.nombre ?? idNormalizado),
        descripcion: cfg?.descripcion ?? String(fila.descripcion ?? ''),
        icono: cfg?.icono ?? String(fila.icono ?? 'Package'),
        color: cfg?.color ?? String(fila.color ?? '#3b82f6'),
        habilitado,
      });
    }
  }

  const resultado: ModuleInfo[] = [];
  for (const moduloCatalogo of catalogoModulosFrontend) {
    const desdeApi = habilitadosApi.get(moduloCatalogo.id);
    if (desdeApi?.habilitado) {
      resultado.push(desdeApi);
    } else if (desdeApi) {
      // API lo listó explícitamente deshabilitado: no mostrar en selector
      continue;
    } else {
      // Sin fila en API: mostrar si el catálogo lo incluye (resiliencia)
      resultado.push({
        id: moduloCatalogo.id,
        nombre: moduloCatalogo.nombre,
        descripcion: moduloCatalogo.descripcion,
        icono: moduloCatalogo.icono,
        color: moduloCatalogo.color,
        habilitado: true,
      });
    }
  }

  return resultado;
}
