/** Parámetros comunes para listados de lotes/galpones/recría con filtro temporal. */
export interface FiltrosListadoOperativo {
  estado?: string;
  activas?: boolean;
  delPeriodoActivo?: boolean;
}

export function paramsListadoOperativo(filtros?: FiltrosListadoOperativo): Record<string, string | boolean> {
  if (!filtros) return {};
  const params: Record<string, string | boolean> = {};
  if (filtros.estado != null && filtros.estado !== '') {
    params.estado = filtros.estado;
  }
  if (filtros.activas != null) {
    params.activas = filtros.activas;
  }
  if (filtros.delPeriodoActivo === true) {
    params.delPeriodoActivo = true;
  }
  return params;
}
