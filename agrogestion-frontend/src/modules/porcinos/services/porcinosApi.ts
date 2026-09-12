import api from '../../../services/api';
import { paramsListadoOperativo } from '../../../core/types/filtrosListadoOperativo';
import type {
  FiltrosListadoLotesPorcinos,
  PorcinosCatalogo,
  PorcinosCatalogoSolicitud,
  PorcinosConsumo,
  PorcinosConsumoSolicitud,
  PorcinosDestete,
  PorcinosDesteteSolicitud,
  PorcinosDieta,
  PorcinosDietaFaseSolicitud,
  PorcinosDietaSolicitud,
  PorcinosEstablecimiento,
  PorcinosEstablecimientoSolicitud,
  PorcinosEventoSanitario,
  PorcinosEventoSanitarioSolicitud,
  PorcinosGalpon,
  PorcinosGalponSolicitud,
  PorcinosGestacion,
  PorcinosLote,
  PorcinosLoteSolicitud,
  PorcinosMadre,
  PorcinosMadreSolicitud,
  PorcinosMuerte,
  PorcinosMuerteSolicitud,
  PorcinosPanelResumen,
  PorcinosPadrillo,
  PorcinosPadrilloSolicitud,
  PorcinosParto,
  PorcinosPartoSolicitud,
  PorcinosPesada,
  PorcinosPesadaSolicitud,
  PorcinosReproduccionResumen,
  PorcinosReporteResumen,
  PorcinosResumenEconomico,
  PorcinosServicio,
  PorcinosServicioSolicitud,
  PorcinosVenta,
  PorcinosVentaSolicitud,
} from '../typesApiV2';

const BASE = '/porcinos';

/** Extrae mensaje legible de errores Axios u otros. */
export function mensajeError(error: unknown): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const respuesta = (error as { response?: { data?: { message?: string; mensaje?: string } } }).response;
    if (respuesta?.data?.message) {
      return String(respuesta.data.message);
    }
    if (respuesta?.data?.mensaje) {
      return String(respuesta.data.mensaje);
    }
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Ocurrió un error inesperado';
}

function paramsLotesPorcinos(filtros?: FiltrosListadoLotesPorcinos): Record<string, string | boolean | number> {
  const params = paramsListadoOperativo(filtros) as Record<string, string | boolean | number>;
  if (filtros?.galponId != null) {
    params.galponId = filtros.galponId;
  }
  if (filtros?.etapa != null && filtros.etapa !== '') {
    params.etapa = filtros.etapa;
  }
  return params;
}

// —— Panel ——

export async function obtenerPanelResumen(): Promise<PorcinosPanelResumen> {
  const { data } = await api.get<PorcinosPanelResumen>(`${BASE}/panel/resumen`);
  return data;
}

// —— Establecimientos y galpones ——

export async function listarEstablecimientos(): Promise<PorcinosEstablecimiento[]> {
  const { data } = await api.get<PorcinosEstablecimiento[]>(`${BASE}/establecimientos`);
  return Array.isArray(data) ? data : [];
}

export async function crearEstablecimiento(
  cuerpo: PorcinosEstablecimientoSolicitud
): Promise<PorcinosEstablecimiento> {
  const { data } = await api.post<PorcinosEstablecimiento>(`${BASE}/establecimientos`, cuerpo);
  return data;
}

export async function actualizarEstablecimiento(
  id: number,
  cuerpo: PorcinosEstablecimientoSolicitud
): Promise<PorcinosEstablecimiento> {
  const { data } = await api.put<PorcinosEstablecimiento>(`${BASE}/establecimientos/${id}`, cuerpo);
  return data;
}

export async function listarGalpones(establecimientoId: number): Promise<PorcinosGalpon[]> {
  const { data } = await api.get<PorcinosGalpon[]>(
    `${BASE}/establecimientos/${establecimientoId}/galpones`
  );
  return Array.isArray(data) ? data : [];
}

export async function crearGalpon(
  establecimientoId: number,
  cuerpo: PorcinosGalponSolicitud
): Promise<PorcinosGalpon> {
  const { data } = await api.post<PorcinosGalpon>(
    `${BASE}/establecimientos/${establecimientoId}/galpones`,
    cuerpo
  );
  return data;
}

export async function actualizarGalpon(
  establecimientoId: number,
  galponId: number,
  cuerpo: PorcinosGalponSolicitud
): Promise<PorcinosGalpon> {
  const { data } = await api.put<PorcinosGalpon>(
    `${BASE}/establecimientos/${establecimientoId}/galpones/${galponId}`,
    cuerpo
  );
  return data;
}

// —— Catálogos ——

export async function listarRazas(): Promise<PorcinosCatalogo[]> {
  const { data } = await api.get<PorcinosCatalogo[]>(`${BASE}/razas`);
  return Array.isArray(data) ? data : [];
}

export async function crearRaza(cuerpo: PorcinosCatalogoSolicitud): Promise<PorcinosCatalogo> {
  const { data } = await api.post<PorcinosCatalogo>(`${BASE}/razas`, cuerpo);
  return data;
}

export async function actualizarRaza(id: number, cuerpo: PorcinosCatalogoSolicitud): Promise<PorcinosCatalogo> {
  const { data } = await api.put<PorcinosCatalogo>(`${BASE}/razas/${id}`, cuerpo);
  return data;
}

export async function listarMotivosBaja(): Promise<PorcinosCatalogo[]> {
  const { data } = await api.get<PorcinosCatalogo[]>(`${BASE}/motivos-baja`);
  return Array.isArray(data) ? data : [];
}

export async function crearMotivoBaja(cuerpo: PorcinosCatalogoSolicitud): Promise<PorcinosCatalogo> {
  const { data } = await api.post<PorcinosCatalogo>(`${BASE}/motivos-baja`, cuerpo);
  return data;
}

export async function crearTipoServicio(cuerpo: PorcinosCatalogoSolicitud): Promise<PorcinosCatalogo> {
  const { data } = await api.post<PorcinosCatalogo>(`${BASE}/tipos-servicio`, cuerpo);
  return data;
}

export async function listarCausasMortalidad(): Promise<PorcinosCatalogo[]> {
  const { data } = await api.get<PorcinosCatalogo[]>(`${BASE}/causas-mortalidad`);
  return Array.isArray(data) ? data : [];
}

export async function crearCausaMortalidad(cuerpo: PorcinosCatalogoSolicitud): Promise<PorcinosCatalogo> {
  const { data } = await api.post<PorcinosCatalogo>(`${BASE}/causas-mortalidad`, cuerpo);
  return data;
}

export async function listarTiposServicio(): Promise<PorcinosCatalogo[]> {
  const { data } = await api.get<PorcinosCatalogo[]>(`${BASE}/tipos-servicio`);
  return Array.isArray(data) ? data : [];
}

// —— Reproducción ——

export async function listarMadres(): Promise<PorcinosMadre[]> {
  const { data } = await api.get<PorcinosMadre[]>(`${BASE}/madres`);
  return Array.isArray(data) ? data : [];
}

export async function obtenerMadre(id: number): Promise<PorcinosMadre> {
  const { data } = await api.get<PorcinosMadre>(`${BASE}/madres/${id}`);
  return data;
}

export async function crearMadre(cuerpo: PorcinosMadreSolicitud): Promise<PorcinosMadre> {
  const { data } = await api.post<PorcinosMadre>(`${BASE}/madres`, cuerpo);
  return data;
}

export async function actualizarMadre(id: number, cuerpo: PorcinosMadreSolicitud): Promise<PorcinosMadre> {
  const { data } = await api.put<PorcinosMadre>(`${BASE}/madres/${id}`, cuerpo);
  return data;
}

export async function listarPadrillos(): Promise<PorcinosPadrillo[]> {
  const { data } = await api.get<PorcinosPadrillo[]>(`${BASE}/padrillos`);
  return Array.isArray(data) ? data : [];
}

export async function crearPadrillo(cuerpo: PorcinosPadrilloSolicitud): Promise<PorcinosPadrillo> {
  const { data } = await api.post<PorcinosPadrillo>(`${BASE}/padrillos`, cuerpo);
  return data;
}

export async function obtenerReproduccionResumen(): Promise<PorcinosReproduccionResumen> {
  const { data } = await api.get<PorcinosReproduccionResumen>(`${BASE}/reproduccion/resumen`);
  return data;
}

export async function registrarServicioMadre(
  madreId: number,
  cuerpo: PorcinosServicioSolicitud
): Promise<PorcinosServicio> {
  const { data } = await api.post<PorcinosServicio>(`${BASE}/madres/${madreId}/servicios`, cuerpo);
  return data;
}

export async function obtenerGestacionActivaMadre(madreId: number): Promise<PorcinosGestacion> {
  const { data } = await api.get<PorcinosGestacion>(`${BASE}/madres/${madreId}/gestacion-activa`);
  return data;
}

export async function obtenerPartoPendienteDestete(madreId: number): Promise<PorcinosParto> {
  const { data } = await api.get<PorcinosParto>(`${BASE}/madres/${madreId}/parto-pendiente-destete`);
  return data;
}

export async function registrarParto(
  gestacionId: number,
  cuerpo: PorcinosPartoSolicitud
): Promise<PorcinosParto> {
  const { data } = await api.post<PorcinosParto>(`${BASE}/gestaciones/${gestacionId}/partos`, cuerpo);
  return data;
}

export async function registrarDestete(
  partoId: number,
  cuerpo: PorcinosDesteteSolicitud
): Promise<PorcinosDestete> {
  const { data } = await api.post<PorcinosDestete>(`${BASE}/partos/${partoId}/destetes`, cuerpo);
  return data;
}

// —— Lotes ——

export async function listarLotes(filtros?: FiltrosListadoLotesPorcinos): Promise<PorcinosLote[]> {
  const { data } = await api.get<PorcinosLote[]>(`${BASE}/lotes`, {
    params: paramsLotesPorcinos(filtros),
  });
  return Array.isArray(data) ? data : [];
}

export async function obtenerLote(id: number): Promise<PorcinosLote> {
  const { data } = await api.get<PorcinosLote>(`${BASE}/lotes/${id}`);
  return data;
}

export async function crearLote(cuerpo: PorcinosLoteSolicitud): Promise<PorcinosLote> {
  const { data } = await api.post<PorcinosLote>(`${BASE}/lotes`, cuerpo);
  return data;
}

export async function actualizarLote(id: number, cuerpo: PorcinosLoteSolicitud): Promise<PorcinosLote> {
  const { data } = await api.put<PorcinosLote>(`${BASE}/lotes/${id}`, cuerpo);
  return data;
}

export async function cerrarLote(id: number): Promise<PorcinosLote> {
  const { data } = await api.post<PorcinosLote>(`${BASE}/lotes/${id}/cierre`, {});
  return data;
}

// —— Operaciones de lote ——

export async function listarPesadas(loteId: number): Promise<PorcinosPesada[]> {
  const { data } = await api.get<PorcinosPesada[]>(`${BASE}/lotes/${loteId}/pesadas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarPesada(
  loteId: number,
  cuerpo: PorcinosPesadaSolicitud
): Promise<PorcinosPesada> {
  const { data } = await api.post<PorcinosPesada>(`${BASE}/lotes/${loteId}/pesadas`, cuerpo);
  return data;
}

export async function listarConsumos(loteId: number): Promise<PorcinosConsumo[]> {
  const { data } = await api.get<PorcinosConsumo[]>(`${BASE}/lotes/${loteId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumo(
  loteId: number,
  cuerpo: PorcinosConsumoSolicitud
): Promise<PorcinosConsumo> {
  const { data } = await api.post<PorcinosConsumo>(`${BASE}/lotes/${loteId}/consumos`, cuerpo);
  return data;
}

export async function listarMuertes(loteId: number): Promise<PorcinosMuerte[]> {
  const { data } = await api.get<PorcinosMuerte[]>(`${BASE}/lotes/${loteId}/muertes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarMuerte(
  loteId: number,
  cuerpo: PorcinosMuerteSolicitud
): Promise<PorcinosMuerte> {
  const { data } = await api.post<PorcinosMuerte>(`${BASE}/lotes/${loteId}/muertes`, cuerpo);
  return data;
}

export async function listarEventosSanitariosEmpresa(): Promise<PorcinosEventoSanitario[]> {
  const { data } = await api.get<PorcinosEventoSanitario[]>(`${BASE}/eventos-sanitarios`);
  return Array.isArray(data) ? data : [];
}

export async function listarEventosSanitarios(loteId: number): Promise<PorcinosEventoSanitario[]> {
  const { data } = await api.get<PorcinosEventoSanitario[]>(
    `${BASE}/lotes/${loteId}/eventos-sanitarios`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitario(
  loteId: number,
  cuerpo: PorcinosEventoSanitarioSolicitud
): Promise<PorcinosEventoSanitario> {
  const { data } = await api.post<PorcinosEventoSanitario>(
    `${BASE}/lotes/${loteId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

export async function listarVentasLote(loteId: number): Promise<PorcinosVenta[]> {
  const { data } = await api.get<PorcinosVenta[]>(`${BASE}/lotes/${loteId}/ventas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVentaLote(
  loteId: number,
  cuerpo: PorcinosVentaSolicitud
): Promise<PorcinosVenta> {
  const { data } = await api.post<PorcinosVenta>(`${BASE}/lotes/${loteId}/ventas`, cuerpo);
  return data;
}

// —— Ventas globales ——

export async function listarVentas(): Promise<PorcinosVenta[]> {
  const { data } = await api.get<PorcinosVenta[]>(`${BASE}/ventas`);
  return Array.isArray(data) ? data : [];
}

// —— Dietas ——

export async function listarDietas(): Promise<PorcinosDieta[]> {
  const { data } = await api.get<PorcinosDieta[]>(`${BASE}/dietas`);
  return Array.isArray(data) ? data : [];
}

export async function crearDieta(cuerpo: PorcinosDietaSolicitud): Promise<PorcinosDieta> {
  const { data } = await api.post<PorcinosDieta>(`${BASE}/dietas`, cuerpo);
  return data;
}

export async function actualizarDieta(id: number, cuerpo: PorcinosDietaSolicitud): Promise<PorcinosDieta> {
  const { data } = await api.put<PorcinosDieta>(`${BASE}/dietas/${id}`, cuerpo);
  return data;
}

export async function obtenerResumenEconomicoLote(loteId: number): Promise<PorcinosResumenEconomico> {
  const { data } = await api.get<PorcinosResumenEconomico>(`${BASE}/lotes/${loteId}/resumen-economico`);
  return data;
}

export async function crearFaseDieta(
  dietaId: number,
  cuerpo: PorcinosDietaFaseSolicitud
): Promise<PorcinosDieta> {
  await api.post(`${BASE}/dietas/${dietaId}/fases`, cuerpo);
  const { data } = await api.get<PorcinosDieta>(`${BASE}/dietas/${dietaId}`);
  return data;
}

export async function actualizarFaseDieta(
  dietaId: number,
  faseId: number,
  cuerpo: PorcinosDietaFaseSolicitud
): Promise<void> {
  await api.put(`${BASE}/dietas/${dietaId}/fases/${faseId}`, cuerpo);
}

// —— Reportes ——

export async function obtenerReporte(tipo: string): Promise<PorcinosReporteResumen> {
  const { data } = await api.get<PorcinosReporteResumen>(`${BASE}/reportes/${tipo}`);
  return data;
}
