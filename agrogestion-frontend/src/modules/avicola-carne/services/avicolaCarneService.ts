import api from '../../../services/api';
import { FiltrosListadoOperativo, paramsListadoOperativo } from '../../../core/types/filtrosListadoOperativo';
import type {
  AvicolaCarneLote,
  AvicolaCarneLoteCreacionCuerpo,
  AvicolaCarneLoteEdicionCuerpo,
  Consumo,
  ConsumoCreacionCuerpo,
  EventoSanitario,
  EventoSanitarioCreacionCuerpo,
  Muerte,
  MuerteCreacionCuerpo,
  Pesada,
  PesadaCreacionCuerpo,
  Resumen,
  Venta,
  VentaCreacionCuerpo,
} from '../types';

const BASE = '/avicola-carne';

/** Extrae mensaje legible de errores Axios u otros. */
export function mensajeErrorCarne(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const respuesta = (error as { response?: { data?: { message?: string } } }).response;
    if (respuesta?.data?.message) {
      return String(respuesta.data.message);
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Ocurrió un error inesperado';
}

export async function listarLotesCarne(filtros?: FiltrosListadoOperativo): Promise<AvicolaCarneLote[]> {
  const { data } = await api.get<AvicolaCarneLote[]>(`${BASE}/lotes`, {
    params: paramsListadoOperativo(filtros),
  });
  return Array.isArray(data) ? data : [];
}

export async function crearLoteCarne(cuerpo: AvicolaCarneLoteCreacionCuerpo): Promise<AvicolaCarneLote> {
  const { data } = await api.post<AvicolaCarneLote>(`${BASE}/lotes`, cuerpo);
  return data;
}

export async function obtenerLoteCarne(loteId: number): Promise<AvicolaCarneLote> {
  const { data } = await api.get<AvicolaCarneLote>(`${BASE}/lotes/${loteId}`);
  return data;
}

export async function actualizarLoteCarne(
  loteId: number,
  cuerpo: AvicolaCarneLoteEdicionCuerpo
): Promise<AvicolaCarneLote> {
  const { data } = await api.put<AvicolaCarneLote>(`${BASE}/lotes/${loteId}`, cuerpo);
  return data;
}

export async function listarPesadasCarne(loteId: number): Promise<Pesada[]> {
  const { data } = await api.get<Pesada[]>(`${BASE}/lotes/${loteId}/pesadas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarPesadaCarne(
  loteId: number,
  cuerpo: PesadaCreacionCuerpo
): Promise<Pesada> {
  const { data } = await api.post<Pesada>(`${BASE}/lotes/${loteId}/pesadas`, cuerpo);
  return data;
}

export async function listarMuertesCarne(loteId: number): Promise<Muerte[]> {
  const { data } = await api.get<Muerte[]>(`${BASE}/lotes/${loteId}/muertes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarMuerteCarne(loteId: number, cuerpo: MuerteCreacionCuerpo): Promise<Muerte> {
  const { data } = await api.post<Muerte>(`${BASE}/lotes/${loteId}/muertes`, cuerpo);
  return data;
}

export async function listarVentasCarne(loteId: number): Promise<Venta[]> {
  const { data } = await api.get<Venta[]>(`${BASE}/lotes/${loteId}/ventas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVentaCarne(loteId: number, cuerpo: VentaCreacionCuerpo): Promise<Venta> {
  const { data } = await api.post<Venta>(`${BASE}/lotes/${loteId}/ventas`, cuerpo);
  return data;
}

export async function listarConsumosCarne(loteId: number): Promise<Consumo[]> {
  const { data } = await api.get<Consumo[]>(`${BASE}/lotes/${loteId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumoCarne(
  loteId: number,
  cuerpo: ConsumoCreacionCuerpo
): Promise<Consumo> {
  const { data } = await api.post<Consumo>(`${BASE}/lotes/${loteId}/consumos`, cuerpo);
  return data;
}

export async function listarEventosSanitariosCarne(loteId: number): Promise<EventoSanitario[]> {
  const { data } = await api.get<EventoSanitario[]>(`${BASE}/lotes/${loteId}/eventos-sanitarios`);
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitarioCarne(
  loteId: number,
  cuerpo: EventoSanitarioCreacionCuerpo
): Promise<EventoSanitario> {
  const { data } = await api.post<EventoSanitario>(`${BASE}/lotes/${loteId}/eventos-sanitarios`, cuerpo);
  return data;
}

export async function obtenerResumenCarne(loteId: number): Promise<Resumen> {
  const { data } = await api.get<Resumen>(`${BASE}/lotes/${loteId}/resumen`);
  return data;
}
