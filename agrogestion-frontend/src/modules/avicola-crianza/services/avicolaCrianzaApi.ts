import api from '../../../services/api';
import { FiltrosListadoOperativo, paramsListadoOperativo } from '../../../core/types/filtrosListadoOperativo';

export type EspecieCrianza =
  | 'POLLO_PARRILLERO'
  | 'GALLINA_PONEDORA'
  | 'PAVO'
  | 'OTRO';

export interface AvicolaLoteRespuesta {
  id: number;
  empresaId: number;
  establecimientoId?: number;
  establecimientoNombre?: string;
  razaId?: number;
  razaNombre?: string;
  nombre: string;
  especie?: string;
  origen?: string;
  fechaIngreso: string;
  cantidadInicial?: number;
  cantidadAnimales?: number;
  pesoPromedioIngreso?: number;
  estado?: string;
  fechaSalida?: string | null;
  observaciones?: string | null;
  climaLatitud?: number | null;
  climaLongitud?: number | null;
}

export interface AvicolaEstablecimientoRespuesta {
  id: number;
  empresaId: number;
  nombre: string;
  observaciones?: string | null;
  activo?: boolean;
  ubicacion?: string | null;
  coordenadas?: string | null;
}

export interface AvicolaRazaRespuesta {
  id: number;
  empresaId: number;
  nombre: string;
  activo?: boolean;
}

export interface AvicolaLoteSolicitudCuerpo {
  establecimientoId: number;
  razaId: number;
  nombre: string;
  especie: EspecieCrianza;
  origen?: string;
  fechaIngreso: string;
  cantidadInicial: number;
  cantidadAnimales?: number;
  pesoPromedioIngreso?: number;
  observaciones?: string | null;
}

export interface AvicolaLoteEdicionCuerpo {
  establecimientoId?: number;
  razaId?: number;
  nombre?: string;
  especie?: EspecieCrianza;
  origen?: string;
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

export async function listarLotesCrianza(filtros?: FiltrosListadoOperativo): Promise<AvicolaLoteRespuesta[]> {
  const { data } = await api.get<AvicolaLoteRespuesta[]>('/avicola-crianza/lotes', {
    params: paramsListadoOperativo(filtros),
  });
  return Array.isArray(data) ? data : [];
}

export async function crearLoteCrianza(cuerpo: AvicolaLoteSolicitudCuerpo): Promise<AvicolaLoteRespuesta> {
  const { data } = await api.post<AvicolaLoteRespuesta>('/avicola-crianza/lotes', cuerpo);
  return data;
}

export async function actualizarLoteCrianza(
  loteId: number,
  cuerpo: AvicolaLoteEdicionCuerpo
): Promise<AvicolaLoteRespuesta> {
  const { data } = await api.put<AvicolaLoteRespuesta>(`/avicola-crianza/lotes/${loteId}`, cuerpo);
  return data;
}

export async function listarEstablecimientosCrianza(): Promise<AvicolaEstablecimientoRespuesta[]> {
  const { data } = await api.get<AvicolaEstablecimientoRespuesta[]>('/avicola-crianza/establecimientos');
  return Array.isArray(data) ? data : [];
}

export async function crearEstablecimientoCrianza(cuerpo: {
  nombre: string;
  observaciones?: string | null;
  activo?: boolean;
  ubicacion?: string | null;
  coordenadas?: string | null;
}): Promise<AvicolaEstablecimientoRespuesta> {
  const { data } = await api.post<AvicolaEstablecimientoRespuesta>('/avicola-crianza/establecimientos', cuerpo);
  return data;
}

export async function actualizarEstablecimientoCrianza(
  id: number,
  cuerpo: {
    nombre?: string;
    observaciones?: string | null;
    activo?: boolean;
    ubicacion?: string | null;
    /** Cadena vacía borra coordenadas guardadas. */
    coordenadas?: string | null;
  }
): Promise<AvicolaEstablecimientoRespuesta> {
  const { data } = await api.put<AvicolaEstablecimientoRespuesta>(
    `/avicola-crianza/establecimientos/${id}`,
    cuerpo
  );
  return data;
}

export async function listarRazasCrianza(): Promise<AvicolaRazaRespuesta[]> {
  const { data } = await api.get<AvicolaRazaRespuesta[]>('/avicola-crianza/razas');
  return Array.isArray(data) ? data : [];
}

export async function crearRazaCrianza(cuerpo: { nombre: string; activo?: boolean }): Promise<AvicolaRazaRespuesta> {
  const { data } = await api.post<AvicolaRazaRespuesta>('/avicola-crianza/razas', cuerpo);
  return data;
}

export async function actualizarRazaCrianza(
  id: number,
  cuerpo: { nombre?: string; activo?: boolean }
): Promise<AvicolaRazaRespuesta> {
  const { data } = await api.put<AvicolaRazaRespuesta>(`/avicola-crianza/razas/${id}`, cuerpo);
  return data;
}

export type TipoVentaCrianza = 'FAENA' | 'VENTA_EN_PIE' | 'DESCARTE';

export interface AvicolaCrianzaResumenRespuesta {
  loteId: number;
  cantidadAnimalesRegistrada?: number;
  sumaMuertes?: number;
  cantidadDisponible?: number;
  mortalidadPorcentaje?: number;
  sumaConsumos?: number;
  pesoPromedioReferencia?: number;
  conversionAlimenticia?: number;
  diasEnProduccion?: number;
}

export interface AvicolaPesadaRespuesta {
  id: number;
  loteId?: number;
  fecha: string;
  pesoPromedio: number;
  cantidadPesada?: number;
  observaciones?: string | null;
}

export interface AvicolaMuerteRespuesta {
  id: number;
  fecha: string;
  cantidad: number;
  causa?: string | null;
  observaciones?: string | null;
}

export interface AvicolaVentaRespuesta {
  id: number;
  fecha: string;
  tipo: string;
  cantidad: number;
  pesoPromedio?: number;
  precioUnitario?: number;
  total?: number;
  comprador?: string | null;
}

export interface AvicolaConsumoRespuesta {
  id: number;
  insumoId: number;
  fecha: string;
  cantidad: number;
  tipo?: string;
  observaciones?: string | null;
}

export interface AvicolaEventoSanitarioRespuesta {
  id: number;
  fecha: string;
  tipo: string;
  descripcion?: string | null;
  insumoId?: number | null;
  dosis?: number | null;
  observaciones?: string | null;
}

export async function obtenerLoteCrianza(loteId: number): Promise<AvicolaLoteRespuesta> {
  const { data } = await api.get<AvicolaLoteRespuesta>(`/avicola-crianza/lotes/${loteId}`);
  return data;
}

export async function obtenerResumenCrianza(loteId: number): Promise<AvicolaCrianzaResumenRespuesta> {
  const { data } = await api.get<AvicolaCrianzaResumenRespuesta>(`/avicola-crianza/lotes/${loteId}/resumen`);
  return data;
}

export async function listarPesadasCrianza(loteId: number): Promise<AvicolaPesadaRespuesta[]> {
  const { data } = await api.get<AvicolaPesadaRespuesta[]>(`/avicola-crianza/lotes/${loteId}/pesadas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarPesadaCrianza(
  loteId: number,
  cuerpo: {
    fecha: string;
    pesoPromedio: number;
    cantidadPesada?: number;
    observaciones?: string | null;
  }
): Promise<AvicolaPesadaRespuesta> {
  const { data } = await api.post<AvicolaPesadaRespuesta>(`/avicola-crianza/lotes/${loteId}/pesadas`, cuerpo);
  return data;
}

export async function listarMuertesCrianza(loteId: number): Promise<AvicolaMuerteRespuesta[]> {
  const { data } = await api.get<AvicolaMuerteRespuesta[]>(`/avicola-crianza/lotes/${loteId}/muertes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarMuerteCrianza(
  loteId: number,
  cuerpo: { fecha: string; cantidad: number; causa?: string | null; observaciones?: string | null }
): Promise<AvicolaMuerteRespuesta> {
  const { data } = await api.post<AvicolaMuerteRespuesta>(`/avicola-crianza/lotes/${loteId}/muertes`, cuerpo);
  return data;
}

export async function listarVentasCrianza(loteId: number): Promise<AvicolaVentaRespuesta[]> {
  const { data } = await api.get<AvicolaVentaRespuesta[]>(`/avicola-crianza/lotes/${loteId}/ventas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVentaCrianza(
  loteId: number,
  cuerpo: {
    fecha: string;
    tipo: TipoVentaCrianza;
    cantidad: number;
    pesoPromedio?: number;
    precioUnitario?: number;
    total?: number;
    comprador?: string | null;
    observaciones?: string | null;
  }
): Promise<AvicolaVentaRespuesta> {
  const { data } = await api.post<AvicolaVentaRespuesta>(`/avicola-crianza/lotes/${loteId}/ventas`, cuerpo);
  return data;
}

export async function listarConsumosCrianza(loteId: number): Promise<AvicolaConsumoRespuesta[]> {
  const { data } = await api.get<AvicolaConsumoRespuesta[]>(`/avicola-crianza/lotes/${loteId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumoCrianza(
  loteId: number,
  cuerpo: {
    insumoId: number;
    fecha: string;
    cantidad: number;
    tipo?: string;
    observaciones?: string | null;
  }
): Promise<AvicolaConsumoRespuesta> {
  const { data } = await api.post<AvicolaConsumoRespuesta>(`/avicola-crianza/lotes/${loteId}/consumos`, cuerpo);
  return data;
}

export async function listarEventosSanitariosCrianza(loteId: number): Promise<AvicolaEventoSanitarioRespuesta[]> {
  const { data } = await api.get<AvicolaEventoSanitarioRespuesta[]>(
    `/avicola-crianza/lotes/${loteId}/eventos-sanitarios`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitarioCrianza(
  loteId: number,
  cuerpo: {
    fecha: string;
    tipo: string;
    descripcion?: string | null;
    insumoId?: number | null;
    dosis?: number | null;
    observaciones?: string | null;
  }
): Promise<AvicolaEventoSanitarioRespuesta> {
  const { data } = await api.post<AvicolaEventoSanitarioRespuesta>(
    `/avicola-crianza/lotes/${loteId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

export { mensajeError };
