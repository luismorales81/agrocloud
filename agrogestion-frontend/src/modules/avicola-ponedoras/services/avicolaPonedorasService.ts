import api from '../../../services/api';
import type {
  AvicolaPonedorasGalpon,
  Consumo,
  ConsumoCreacionCuerpo,
  DescarteAves,
  DescarteAvesCreacionCuerpo,
  EventoSanitario,
  EventoSanitarioCreacionCuerpo,
  GalponCreacionCuerpo,
  GalponEdicionCuerpo,
  Muerte,
  MuerteCreacionCuerpo,
  Postura,
  PosturaCreacionCuerpo,
  Resumen,
  VentaHuevos,
  VentaHuevosCreacionCuerpo,
} from '../types';

const BASE = '/avicola-ponedoras';

/** Mensaje legible desde error Axios. */
export function mensajeErrorPonedoras(error: unknown): string {
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

export async function listarGalpones(estado?: string): Promise<AvicolaPonedorasGalpon[]> {
  const { data } = await api.get<AvicolaPonedorasGalpon[]>(`${BASE}/galpones`, {
    params: estado != null && estado !== '' ? { estado } : undefined,
  });
  return Array.isArray(data) ? data : [];
}

export async function crearGalpon(cuerpo: GalponCreacionCuerpo): Promise<AvicolaPonedorasGalpon> {
  const { data } = await api.post<AvicolaPonedorasGalpon>(`${BASE}/galpones`, cuerpo);
  return data;
}

export async function obtenerGalpon(galponId: number): Promise<AvicolaPonedorasGalpon> {
  const { data } = await api.get<AvicolaPonedorasGalpon>(`${BASE}/galpones/${galponId}`);
  return data;
}

export async function actualizarGalpon(
  galponId: number,
  cuerpo: GalponEdicionCuerpo
): Promise<AvicolaPonedorasGalpon> {
  const { data } = await api.put<AvicolaPonedorasGalpon>(`${BASE}/galpones/${galponId}`, cuerpo);
  return data;
}

export async function listarPosturas(galponId: number): Promise<Postura[]> {
  const { data } = await api.get<Postura[]>(`${BASE}/galpones/${galponId}/posturas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarPostura(
  galponId: number,
  cuerpo: PosturaCreacionCuerpo
): Promise<Postura> {
  const { data } = await api.post<Postura>(`${BASE}/galpones/${galponId}/posturas`, cuerpo);
  return data;
}

export async function listarMuertes(galponId: number): Promise<Muerte[]> {
  const { data } = await api.get<Muerte[]>(`${BASE}/galpones/${galponId}/muertes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarMuerte(galponId: number, cuerpo: MuerteCreacionCuerpo): Promise<Muerte> {
  const { data } = await api.post<Muerte>(`${BASE}/galpones/${galponId}/muertes`, cuerpo);
  return data;
}

export async function listarConsumos(galponId: number): Promise<Consumo[]> {
  const { data } = await api.get<Consumo[]>(`${BASE}/galpones/${galponId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumo(
  galponId: number,
  cuerpo: ConsumoCreacionCuerpo
): Promise<Consumo> {
  const { data } = await api.post<Consumo>(`${BASE}/galpones/${galponId}/consumos`, cuerpo);
  return data;
}

export async function listarEventosSanitarios(galponId: number): Promise<EventoSanitario[]> {
  const { data } = await api.get<EventoSanitario[]>(`${BASE}/galpones/${galponId}/eventos-sanitarios`);
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitario(
  galponId: number,
  cuerpo: EventoSanitarioCreacionCuerpo
): Promise<EventoSanitario> {
  const { data } = await api.post<EventoSanitario>(
    `${BASE}/galpones/${galponId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

export async function listarVentasHuevos(galponId: number): Promise<VentaHuevos[]> {
  const { data } = await api.get<VentaHuevos[]>(`${BASE}/galpones/${galponId}/ventas-huevos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVentaHuevos(
  galponId: number,
  cuerpo: VentaHuevosCreacionCuerpo
): Promise<VentaHuevos> {
  const { data } = await api.post<VentaHuevos>(`${BASE}/galpones/${galponId}/ventas-huevos`, cuerpo);
  return data;
}

export async function listarDescarteAves(galponId: number): Promise<DescarteAves[]> {
  const { data } = await api.get<DescarteAves[]>(`${BASE}/galpones/${galponId}/descarte-aves`);
  return Array.isArray(data) ? data : [];
}

export async function registrarDescarteAves(
  galponId: number,
  cuerpo: DescarteAvesCreacionCuerpo
): Promise<DescarteAves> {
  const { data } = await api.post<DescarteAves>(`${BASE}/galpones/${galponId}/descarte-aves`, cuerpo);
  return data;
}

export async function obtenerResumenGalpon(galponId: number): Promise<Resumen> {
  const { data } = await api.get<Resumen>(`${BASE}/galpones/${galponId}/resumen`);
  return data;
}
