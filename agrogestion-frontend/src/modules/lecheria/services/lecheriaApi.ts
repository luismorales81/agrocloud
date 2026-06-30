import api from '../../../services/api';
import type {
  LecheriaAnimal,
  LecheriaAnimalSolicitud,
  LecheriaBajaAnimal,
  LecheriaBajaAnimalSolicitud,
  LecheriaClimaProduccion,
  LecheriaCloseoutRodeo,
  LecheriaConsumo,
  LecheriaConsumoSolicitud,
  LecheriaCurvaLactancia,
  LecheriaEspecie,
  LecheriaEstablecimiento,
  LecheriaEstablecimientoSolicitud,
  LecheriaEventoReproductivo,
  LecheriaEventoReproductivoSolicitud,
  LecheriaEventoSanitario,
  LecheriaEventoSanitarioSolicitud,
  LecheriaImportControlLechero,
  LecheriaMotivoBaja,
  LecheriaMotivoBajaSolicitud,
  LecheriaMovimientoSenasa,
  LecheriaMovimientoSenasaSolicitud,
  LecheriaPanelResumen,
  LecheriaRankingProduccion,
  LecheriaRaza,
  LecheriaRazaSolicitud,
  LecheriaRegistroOrdene,
  LecheriaRegistroOrdeneSolicitud,
  LecheriaRodeo,
  LecheriaRodeoSolicitud,
  LecheriaScoreCorporal,
  LecheriaScoreCorporalSolicitud,
  LecheriaVentaLeche,
  LecheriaVentaLecheSolicitud,
} from '../types';

const BASE = '/lecheria';

/** Extrae mensaje legible de errores Axios u otros. */
export function mensajeError(error: unknown): string {
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

// —— Panel ——

export async function obtenerPanelResumen(): Promise<LecheriaPanelResumen> {
  const { data } = await api.get<LecheriaPanelResumen>(`${BASE}/panel/resumen`);
  return data;
}

// —— Establecimientos y rodeos ——

export async function listarEstablecimientos(): Promise<LecheriaEstablecimiento[]> {
  const { data } = await api.get<LecheriaEstablecimiento[]>(`${BASE}/establecimientos`);
  return Array.isArray(data) ? data : [];
}

export async function crearEstablecimiento(
  cuerpo: LecheriaEstablecimientoSolicitud
): Promise<LecheriaEstablecimiento> {
  const { data } = await api.post<LecheriaEstablecimiento>(`${BASE}/establecimientos`, cuerpo);
  return data;
}

export async function actualizarEstablecimiento(
  id: number,
  cuerpo: LecheriaEstablecimientoSolicitud
): Promise<LecheriaEstablecimiento> {
  const { data } = await api.put<LecheriaEstablecimiento>(`${BASE}/establecimientos/${id}`, cuerpo);
  return data;
}

export async function listarRodeos(establecimientoId: number): Promise<LecheriaRodeo[]> {
  const { data } = await api.get<LecheriaRodeo[]>(
    `${BASE}/establecimientos/${establecimientoId}/rodeos`
  );
  return Array.isArray(data) ? data : [];
}

export async function crearRodeo(
  establecimientoId: number,
  cuerpo: LecheriaRodeoSolicitud
): Promise<LecheriaRodeo> {
  const { data } = await api.post<LecheriaRodeo>(
    `${BASE}/establecimientos/${establecimientoId}/rodeos`,
    cuerpo
  );
  return data;
}

// —— Catálogos ——

export async function listarRazas(especie?: LecheriaEspecie | string): Promise<LecheriaRaza[]> {
  const params: Record<string, string> = {};
  if (especie) {
    params.especie = String(especie);
  }
  const { data } = await api.get<LecheriaRaza[]>(`${BASE}/catalogos/razas`, { params });
  return Array.isArray(data) ? data : [];
}

export async function crearRaza(cuerpo: LecheriaRazaSolicitud): Promise<LecheriaRaza> {
  const { data } = await api.post<LecheriaRaza>(`${BASE}/catalogos/razas`, cuerpo);
  return data;
}

export async function actualizarRaza(id: number, cuerpo: LecheriaRazaSolicitud): Promise<LecheriaRaza> {
  const { data } = await api.put<LecheriaRaza>(`${BASE}/catalogos/razas/${id}`, cuerpo);
  return data;
}

export async function listarMotivosBaja(): Promise<LecheriaMotivoBaja[]> {
  const { data } = await api.get<LecheriaMotivoBaja[]>(`${BASE}/catalogos/motivos-baja`);
  return Array.isArray(data) ? data : [];
}

export async function crearMotivoBaja(cuerpo: LecheriaMotivoBajaSolicitud): Promise<LecheriaMotivoBaja> {
  const { data } = await api.post<LecheriaMotivoBaja>(`${BASE}/catalogos/motivos-baja`, cuerpo);
  return data;
}

export async function actualizarMotivoBaja(
  id: number,
  cuerpo: LecheriaMotivoBajaSolicitud
): Promise<LecheriaMotivoBaja> {
  const { data } = await api.put<LecheriaMotivoBaja>(`${BASE}/catalogos/motivos-baja/${id}`, cuerpo);
  return data;
}

// —— Animales ——

export async function listarAnimales(): Promise<LecheriaAnimal[]> {
  const { data } = await api.get<LecheriaAnimal[]>(`${BASE}/animales`);
  return Array.isArray(data) ? data : [];
}

export async function obtenerAnimal(id: number): Promise<LecheriaAnimal> {
  const { data } = await api.get<LecheriaAnimal>(`${BASE}/animales/${id}`);
  return data;
}

export async function crearAnimal(cuerpo: LecheriaAnimalSolicitud): Promise<LecheriaAnimal> {
  const { data } = await api.post<LecheriaAnimal>(`${BASE}/animales`, cuerpo);
  return data;
}

export async function actualizarAnimal(
  id: number,
  cuerpo: LecheriaAnimalSolicitud
): Promise<LecheriaAnimal> {
  const { data } = await api.put<LecheriaAnimal>(`${BASE}/animales/${id}`, cuerpo);
  return data;
}

export async function registrarBajaAnimal(
  id: number,
  cuerpo: LecheriaBajaAnimalSolicitud
): Promise<LecheriaBajaAnimal> {
  const { data } = await api.post<LecheriaBajaAnimal>(`${BASE}/animales/${id}/baja`, cuerpo);
  return data;
}

// —— Ordeñes ——

export async function listarOrdenes(animalId: number): Promise<LecheriaRegistroOrdene[]> {
  const { data } = await api.get<LecheriaRegistroOrdene[]>(`${BASE}/animales/${animalId}/ordenes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarOrdene(
  animalId: number,
  cuerpo: LecheriaRegistroOrdeneSolicitud
): Promise<LecheriaRegistroOrdene> {
  const { data } = await api.post<LecheriaRegistroOrdene>(
    `${BASE}/animales/${animalId}/ordenes`,
    cuerpo
  );
  return data;
}

// —— Reproducción ——

export async function listarEventosReproductivos(
  animalId: number
): Promise<LecheriaEventoReproductivo[]> {
  const { data } = await api.get<LecheriaEventoReproductivo[]>(
    `${BASE}/animales/${animalId}/eventos-reproductivos`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoReproductivo(
  animalId: number,
  cuerpo: LecheriaEventoReproductivoSolicitud
): Promise<LecheriaEventoReproductivo> {
  const { data } = await api.post<LecheriaEventoReproductivo>(
    `${BASE}/animales/${animalId}/eventos-reproductivos`,
    cuerpo
  );
  return data;
}

// —— Sanidad ——

export async function listarEventosSanitarios(animalId: number): Promise<LecheriaEventoSanitario[]> {
  const { data } = await api.get<LecheriaEventoSanitario[]>(
    `${BASE}/animales/${animalId}/eventos-sanitarios`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitario(
  animalId: number,
  cuerpo: LecheriaEventoSanitarioSolicitud
): Promise<LecheriaEventoSanitario> {
  const { data } = await api.post<LecheriaEventoSanitario>(
    `${BASE}/animales/${animalId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

// —— ECC (score corporal) ——

export async function listarScoresCorporales(animalId: number): Promise<LecheriaScoreCorporal[]> {
  const { data } = await api.get<LecheriaScoreCorporal[]>(
    `${BASE}/animales/${animalId}/scores-corporales`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarScoreCorporal(
  animalId: number,
  cuerpo: LecheriaScoreCorporalSolicitud
): Promise<LecheriaScoreCorporal> {
  const { data } = await api.post<LecheriaScoreCorporal>(
    `${BASE}/animales/${animalId}/scores-corporales`,
    cuerpo
  );
  return data;
}

// —— Consumos por rodeo ——

export async function listarConsumos(rodeoId: number): Promise<LecheriaConsumo[]> {
  const { data } = await api.get<LecheriaConsumo[]>(`${BASE}/rodeos/${rodeoId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumo(
  rodeoId: number,
  cuerpo: LecheriaConsumoSolicitud
): Promise<LecheriaConsumo> {
  const { data } = await api.post<LecheriaConsumo>(`${BASE}/rodeos/${rodeoId}/consumos`, cuerpo);
  return data;
}

// —— Ventas de leche ——

export async function listarVentasLeche(): Promise<LecheriaVentaLeche[]> {
  const { data } = await api.get<LecheriaVentaLeche[]>(`${BASE}/ventas-leche`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVentaLeche(
  cuerpo: LecheriaVentaLecheSolicitud
): Promise<LecheriaVentaLeche> {
  const { data } = await api.post<LecheriaVentaLeche>(`${BASE}/ventas-leche`, cuerpo);
  return data;
}

// —— Reportes ——

export async function obtenerCurvasLactancia(animalId?: number): Promise<LecheriaCurvaLactancia[]> {
  const params: Record<string, number> = {};
  if (animalId != null) {
    params.animalId = animalId;
  }
  const { data } = await api.get<LecheriaCurvaLactancia[]>(`${BASE}/reportes/curvas-lactancia`, {
    params,
  });
  return Array.isArray(data) ? data : [];
}

export async function obtenerRankingProduccion(
  desde?: string,
  hasta?: string
): Promise<LecheriaRankingProduccion> {
  const params: Record<string, string> = {};
  if (desde) params.desde = desde;
  if (hasta) params.hasta = hasta;
  const { data } = await api.get<LecheriaRankingProduccion>(
    `${BASE}/reportes/ranking-produccion`,
    { params }
  );
  return data;
}

export async function obtenerClimaProduccion(
  desde?: string,
  hasta?: string
): Promise<LecheriaClimaProduccion> {
  const params: Record<string, string> = {};
  if (desde) params.desde = desde;
  if (hasta) params.hasta = hasta;
  const { data } = await api.get<LecheriaClimaProduccion>(`${BASE}/reportes/clima-produccion`, {
    params,
  });
  return data;
}

// —— Importación ——

export async function importarControlLechero(archivo: File): Promise<LecheriaImportControlLechero> {
  const formulario = new FormData();
  formulario.append('archivo', archivo);
  const { data } = await api.post<LecheriaImportControlLechero>(
    `${BASE}/import/control-lechero`,
    formulario,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  );
  return data;
}

// —— Closeout rodeo ——

export async function obtenerCloseoutRodeo(rodeoId: number): Promise<LecheriaCloseoutRodeo> {
  const { data } = await api.get<LecheriaCloseoutRodeo>(`${BASE}/rodeos/${rodeoId}/closeout`);
  return data;
}

// —— SENASA ——

export async function registrarMovimientoSenasa(
  cuerpo: LecheriaMovimientoSenasaSolicitud
): Promise<LecheriaMovimientoSenasa> {
  const { data } = await api.post<LecheriaMovimientoSenasa>(`${BASE}/movimientos-senasa`, cuerpo);
  return data;
}

export async function listarMovimientosSenasaPendientes(): Promise<LecheriaMovimientoSenasa[]> {
  const { data } = await api.get<LecheriaMovimientoSenasa[]>(`${BASE}/movimientos-senasa/pendientes`);
  return Array.isArray(data) ? data : [];
}
