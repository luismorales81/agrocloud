import api from '../../../services/api';
import { FiltrosListadoOperativo, paramsListadoOperativo } from '../../../core/types/filtrosListadoOperativo';

export interface AvicolaHuevoLoteRespuesta {
  id: number;
  empresaId: number;
  establecimientoId?: number;
  establecimientoNombre?: string;
  razaId?: number;
  razaNombre?: string;
  nombre: string;
  fechaInicio: string;
  cantidadAvesInicial?: number;
  cantidadAvesActual?: number;
  estado?: string;
  fechaCierre?: string | null;
  observaciones?: string | null;
  /** Centro del establecimiento para clima (backend); opcional. */
  climaLatitud?: number | null;
  climaLongitud?: number | null;
}

export interface AvicolaHuevoEstablecimientoRespuesta {
  id: number;
  empresaId: number;
  nombre: string;
  observaciones?: string | null;
  activo?: boolean;
  /** Texto de ubicación o dirección (opcional). */
  ubicacion?: string | null;
  /** JSON array de `{ lat, lng }` como en cultivos (opcional). */
  coordenadas?: string | null;
}

export interface AvicolaHuevoRazaRespuesta {
  id: number;
  empresaId: number;
  nombre: string;
  activo?: boolean;
}

export interface AvicolaHuevoLoteCuerpo {
  establecimientoId: number;
  razaId: number;
  nombre: string;
  fechaInicio: string;
  cantidadAvesInicial: number;
  cantidadAvesActual?: number;
  observaciones?: string | null;
}

export interface AvicolaHuevoLoteEdicionCuerpo {
  establecimientoId?: number;
  razaId?: number;
  nombre?: string;
  cantidadAvesActual?: number;
  observaciones?: string | null;
}

function mensajeError(e: unknown): string {
  if (typeof e === 'object' && e !== null && 'response' in e) {
    const r = (e as { response?: { data?: { message?: string } } }).response;
    if (r?.data?.message) return String(r.data.message);
  }
  if (e instanceof Error) return e.message;
  return 'Error desconocido';
}

export async function listarLotesHuevos(filtros?: FiltrosListadoOperativo): Promise<AvicolaHuevoLoteRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoLoteRespuesta[]>('/avicola-huevos/lotes', {
    params: paramsListadoOperativo(filtros),
  });
  return Array.isArray(data) ? data : [];
}

export async function crearLoteHuevos(cuerpo: AvicolaHuevoLoteCuerpo): Promise<AvicolaHuevoLoteRespuesta> {
  const { data } = await api.post<AvicolaHuevoLoteRespuesta>('/avicola-huevos/lotes', cuerpo);
  return data;
}

export async function actualizarLoteHuevos(
  loteId: number,
  cuerpo: AvicolaHuevoLoteEdicionCuerpo
): Promise<AvicolaHuevoLoteRespuesta> {
  const { data } = await api.put<AvicolaHuevoLoteRespuesta>(`/avicola-huevos/lotes/${loteId}`, cuerpo);
  return data;
}

export async function cerrarLoteHuevos(loteId: number): Promise<AvicolaHuevoLoteRespuesta> {
  const { data } = await api.post<AvicolaHuevoLoteRespuesta>(`/avicola-huevos/lotes/${loteId}/cierre`);
  return data;
}

export async function listarEstablecimientosHuevos(): Promise<AvicolaHuevoEstablecimientoRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoEstablecimientoRespuesta[]>('/avicola-huevos/establecimientos');
  return Array.isArray(data) ? data : [];
}

export async function crearEstablecimientoHuevos(cuerpo: {
  nombre: string;
  observaciones?: string | null;
  activo?: boolean;
  ubicacion?: string | null;
  coordenadas?: string | null;
}): Promise<AvicolaHuevoEstablecimientoRespuesta> {
  const { data } = await api.post<AvicolaHuevoEstablecimientoRespuesta>('/avicola-huevos/establecimientos', cuerpo);
  return data;
}

export async function actualizarEstablecimientoHuevos(
  id: number,
  cuerpo: {
    nombre?: string;
    observaciones?: string | null;
    activo?: boolean;
    ubicacion?: string | null;
    /** Cadena vacía borra coordenadas guardadas. */
    coordenadas?: string | null;
  }
): Promise<AvicolaHuevoEstablecimientoRespuesta> {
  const { data } = await api.put<AvicolaHuevoEstablecimientoRespuesta>(
    `/avicola-huevos/establecimientos/${id}`,
    cuerpo
  );
  return data;
}

export async function listarRazasHuevos(): Promise<AvicolaHuevoRazaRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoRazaRespuesta[]>('/avicola-huevos/razas');
  return Array.isArray(data) ? data : [];
}

export async function crearRazaHuevos(cuerpo: { nombre: string; activo?: boolean }): Promise<AvicolaHuevoRazaRespuesta> {
  const { data } = await api.post<AvicolaHuevoRazaRespuesta>('/avicola-huevos/razas', cuerpo);
  return data;
}

export async function actualizarRazaHuevos(
  id: number,
  cuerpo: { nombre?: string; activo?: boolean }
): Promise<AvicolaHuevoRazaRespuesta> {
  const { data } = await api.put<AvicolaHuevoRazaRespuesta>(`/avicola-huevos/razas/${id}`, cuerpo);
  return data;
}

export async function obtenerLoteHuevos(loteId: number): Promise<AvicolaHuevoLoteRespuesta> {
  const { data } = await api.get<AvicolaHuevoLoteRespuesta>(`/avicola-huevos/lotes/${loteId}`);
  return data;
}

export interface AvicolaHuevosResumenRespuesta {
  loteId: number;
  cantidadAvesActual?: number;
  totalHuevosProducidos?: number;
  sumaConsumosInsumo?: number;
  diasEnPostura?: number;
  huevosPromedioPorAveYdia?: number;
}

export async function obtenerResumenHuevos(loteId: number): Promise<AvicolaHuevosResumenRespuesta> {
  const { data } = await api.get<AvicolaHuevosResumenRespuesta>(`/avicola-huevos/lotes/${loteId}/resumen`);
  return data;
}

export interface AvicolaHuevoProduccionDiariaRespuesta {
  id: number;
  loteId?: number;
  fecha: string;
  cantidadHuevos: number;
  huevosTam1?: number;
  huevosTam2?: number;
  huevosTam3?: number;
  huevosTam4?: number;
  huevosRotos?: number;
  totalHuevosDia?: number;
  temperaturaDia?: number | null;
  humedadDia?: number | null;
  observaciones?: string | null;
}

export interface AvicolaHuevoConsumoRespuesta {
  id: number;
  loteId?: number;
  insumoId: number;
  insumoNombre?: string | null;
  fecha: string;
  cantidad: number;
  tipo?: string;
  observaciones?: string | null;
}

export interface AvicolaHuevoEventoSanitarioRespuesta {
  id: number;
  fecha: string;
  tipo: string;
  descripcion?: string | null;
  insumoId?: number | null;
  insumoNombre?: string | null;
  dosis?: number | null;
  observaciones?: string | null;
}

export async function listarProduccionHuevos(loteId: number): Promise<AvicolaHuevoProduccionDiariaRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoProduccionDiariaRespuesta[]>(
    `/avicola-huevos/lotes/${loteId}/produccion-diaria`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarProduccionHuevos(
  loteId: number,
  cuerpo: {
    fecha: string;
    cantidadHuevos?: number;
    huevosTam1?: number;
    huevosTam2?: number;
    huevosTam3?: number;
    huevosTam4?: number;
    huevosRotos?: number;
    temperaturaDia?: number | null;
    humedadDia?: number | null;
    observaciones?: string | null;
  }
): Promise<AvicolaHuevoProduccionDiariaRespuesta> {
  const { data } = await api.post<AvicolaHuevoProduccionDiariaRespuesta>(
    `/avicola-huevos/lotes/${loteId}/produccion-diaria`,
    cuerpo
  );
  return data;
}

export async function listarConsumosHuevos(loteId: number): Promise<AvicolaHuevoConsumoRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoConsumoRespuesta[]>(`/avicola-huevos/lotes/${loteId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumoHuevos(
  loteId: number,
  cuerpo: {
    insumoId: number;
    fecha: string;
    cantidad: number;
    tipo?: string;
    observaciones?: string | null;
  }
): Promise<AvicolaHuevoConsumoRespuesta> {
  const { data } = await api.post<AvicolaHuevoConsumoRespuesta>(`/avicola-huevos/lotes/${loteId}/consumos`, cuerpo);
  return data;
}

export async function listarEventosSanitariosHuevos(loteId: number): Promise<AvicolaHuevoEventoSanitarioRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoEventoSanitarioRespuesta[]>(
    `/avicola-huevos/lotes/${loteId}/eventos-sanitarios`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitarioHuevos(
  loteId: number,
  cuerpo: {
    fecha: string;
    tipo: string;
    descripcion?: string | null;
    insumoId?: number | null;
    dosis?: number | null;
    observaciones?: string | null;
  }
): Promise<AvicolaHuevoEventoSanitarioRespuesta> {
  const { data } = await api.post<AvicolaHuevoEventoSanitarioRespuesta>(
    `/avicola-huevos/lotes/${loteId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

export async function actualizarConsumoHuevos(
  loteId: number,
  consumoId: number,
  cuerpo: { fecha: string; cantidad: number; observaciones?: string | null }
): Promise<AvicolaHuevoConsumoRespuesta> {
  const { data } = await api.put<AvicolaHuevoConsumoRespuesta>(
    `/avicola-huevos/lotes/${loteId}/consumos/${consumoId}`,
    cuerpo
  );
  return data;
}

export async function registrarAjustePlantelHuevos(
  loteId: number,
  cuerpo: { cantidadAvesNueva: number; motivo: string }
): Promise<AvicolaHuevoLoteRespuesta> {
  const { data } = await api.post<AvicolaHuevoLoteRespuesta>(`/avicola-huevos/lotes/${loteId}/ajuste-plantel`, cuerpo);
  return data;
}

export interface AvicolaHuevoAjustePlantelRespuesta {
  id: number;
  loteId?: number;
  fechaHora: string;
  cantidadAvesAnterior: number;
  cantidadAvesNueva: number;
  motivo: string;
  usuarioId?: number;
}

export async function listarAjustesPlantelHuevos(loteId: number): Promise<AvicolaHuevoAjustePlantelRespuesta[]> {
  const { data } = await api.get<AvicolaHuevoAjustePlantelRespuesta[]>(`/avicola-huevos/lotes/${loteId}/ajustes-plantel`);
  return Array.isArray(data) ? data : [];
}

export interface AvicolaHuevosReporteResumenRespuesta {
  desde?: string;
  hasta?: string;
  totalHuevosRegistrados?: number;
  porLote?: {
    loteId: number;
    nombreLote?: string;
    totalHuevos?: number;
    consumoInsumoEnRango?: number;
    temperaturaPromedio?: number | null;
    humedadPromedio?: number | null;
  }[];
}

export async function obtenerReporteResumenHuevos(
  desde: string,
  hasta: string
): Promise<AvicolaHuevosReporteResumenRespuesta> {
  const { data } = await api.get<AvicolaHuevosReporteResumenRespuesta>(
    `/avicola-huevos/reportes/resumen?desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}`
  );
  return data;
}

export interface AvicolaHuevosReporteAnalisisRespuesta {
  loteId?: number;
  nombreLote?: string;
  fechaInicioLote?: string;
  cantidadAvesActual?: number;
  desde?: string;
  hasta?: string;
  totalCostoConsumosRango?: number | string;
  seriePostura?: {
    fecha?: string;
    totalHuevos?: number;
    temperatura?: number | null;
    humedad?: number | null;
    diasEdadLote?: number;
    huevosPorAve?: number | string | null;
  }[];
  serieGastos?: {
    fecha?: string;
    costoEstimado?: number | string;
  }[];
}

export async function obtenerReporteAnalisisPosturaHuevos(
  loteId: number,
  desde: string,
  hasta: string
): Promise<AvicolaHuevosReporteAnalisisRespuesta> {
  const { data } = await api.get<AvicolaHuevosReporteAnalisisRespuesta>(
    `/avicola-huevos/reportes/analisis-postura?loteId=${loteId}&desde=${encodeURIComponent(desde)}&hasta=${encodeURIComponent(hasta)}`
  );
  return data;
}

export { mensajeError };
