import api from '../../../services/api';
import { paramsListadoOperativo } from '../../../core/types/filtrosListadoOperativo';
import type {
  FeedlotAjustePlantel,
  FeedlotAjustePlantelSolicitud,
  FeedlotAnalisisLotes,
  FeedlotCatalogo,
  FeedlotCatalogoSolicitud,
  FeedlotCierreLoteSolicitud,
  FeedlotCloseout,
  FeedlotConfiguracionCloseout,
  FeedlotConfiguracionCloseoutSolicitud,
  FeedlotConsumo,
  FeedlotConsumoActualizarSolicitud,
  FeedlotConsumoSolicitud,
  FeedlotConsumoTeorico,
  FeedlotCorral,
  FeedlotCorralSolicitud,
  FeedlotCurvaPeso,
  FeedlotDieta,
  FeedlotDietaFase,
  FeedlotDietaFaseSolicitud,
  FeedlotDietaSolicitud,
  FeedlotEstablecimiento,
  FeedlotEstablecimientoSolicitud,
  FeedlotEventoSanitario,
  FeedlotEventoSanitarioSolicitud,
  FeedlotLecturaComedero,
  FeedlotLecturaComederoSolicitud,
  FeedlotLote,
  FeedlotLoteSolicitud,
  FeedlotMuerte,
  FeedlotMuerteSolicitud,
  FeedlotPanelResumen,
  FeedlotPesada,
  FeedlotPesadaSolicitud,
  FeedlotProveedorOrigen,
  FeedlotProveedorOrigenSolicitud,
  FeedlotResumen,
  FeedlotVenta,
  FeedlotVentaSolicitud,
  FiltrosListadoLotesFeedlot,
} from '../types';

const BASE = '/feedlot';

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

function paramsLotesFeedlot(filtros?: FiltrosListadoLotesFeedlot): Record<string, string | boolean | number> {
  const params = paramsListadoOperativo(filtros) as Record<string, string | boolean | number>;
  if (filtros?.corralId != null) {
    params.corralId = filtros.corralId;
  }
  return params;
}

function descargarBlob(blob: Blob, nombreArchivo: string): void {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = nombreArchivo;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.URL.revokeObjectURL(url);
}

// —— Panel ——

export async function obtenerPanelResumen(): Promise<FeedlotPanelResumen> {
  const { data } = await api.get<FeedlotPanelResumen>(`${BASE}/panel/resumen`);
  return data;
}

// —— Establecimientos y corrales ——

export async function listarEstablecimientos(): Promise<FeedlotEstablecimiento[]> {
  const { data } = await api.get<FeedlotEstablecimiento[]>(`${BASE}/establecimientos`);
  return Array.isArray(data) ? data : [];
}

export async function crearEstablecimiento(
  cuerpo: FeedlotEstablecimientoSolicitud
): Promise<FeedlotEstablecimiento> {
  const { data } = await api.post<FeedlotEstablecimiento>(`${BASE}/establecimientos`, cuerpo);
  return data;
}

export async function actualizarEstablecimiento(
  id: number,
  cuerpo: FeedlotEstablecimientoSolicitud
): Promise<FeedlotEstablecimiento> {
  const { data } = await api.put<FeedlotEstablecimiento>(`${BASE}/establecimientos/${id}`, cuerpo);
  return data;
}

export async function listarCorrales(establecimientoId: number): Promise<FeedlotCorral[]> {
  const { data } = await api.get<FeedlotCorral[]>(
    `${BASE}/establecimientos/${establecimientoId}/corrales`
  );
  return Array.isArray(data) ? data : [];
}

export async function crearCorral(
  establecimientoId: number,
  cuerpo: FeedlotCorralSolicitud
): Promise<FeedlotCorral> {
  const { data } = await api.post<FeedlotCorral>(
    `${BASE}/establecimientos/${establecimientoId}/corrales`,
    cuerpo
  );
  return data;
}

export async function actualizarCorral(
  establecimientoId: number,
  corralId: number,
  cuerpo: FeedlotCorralSolicitud
): Promise<FeedlotCorral> {
  const { data } = await api.put<FeedlotCorral>(
    `${BASE}/establecimientos/${establecimientoId}/corrales/${corralId}`,
    cuerpo
  );
  return data;
}

// —— Catálogos ——

export async function listarCategorias(): Promise<FeedlotCatalogo[]> {
  const { data } = await api.get<FeedlotCatalogo[]>(`${BASE}/categorias`);
  return Array.isArray(data) ? data : [];
}

export async function crearCategoria(cuerpo: FeedlotCatalogoSolicitud): Promise<FeedlotCatalogo> {
  const { data } = await api.post<FeedlotCatalogo>(`${BASE}/categorias`, cuerpo);
  return data;
}

export async function actualizarCategoria(
  id: number,
  cuerpo: FeedlotCatalogoSolicitud
): Promise<FeedlotCatalogo> {
  const { data } = await api.put<FeedlotCatalogo>(`${BASE}/categorias/${id}`, cuerpo);
  return data;
}

export async function listarRazas(): Promise<FeedlotCatalogo[]> {
  const { data } = await api.get<FeedlotCatalogo[]>(`${BASE}/razas`);
  return Array.isArray(data) ? data : [];
}

export async function crearRaza(cuerpo: FeedlotCatalogoSolicitud): Promise<FeedlotCatalogo> {
  const { data } = await api.post<FeedlotCatalogo>(`${BASE}/razas`, cuerpo);
  return data;
}

export async function actualizarRaza(id: number, cuerpo: FeedlotCatalogoSolicitud): Promise<FeedlotCatalogo> {
  const { data } = await api.put<FeedlotCatalogo>(`${BASE}/razas/${id}`, cuerpo);
  return data;
}

export async function listarMotivosMuerte(): Promise<FeedlotCatalogo[]> {
  const { data } = await api.get<FeedlotCatalogo[]>(`${BASE}/motivos-muerte`);
  return Array.isArray(data) ? data : [];
}

export async function crearMotivoMuerte(cuerpo: FeedlotCatalogoSolicitud): Promise<FeedlotCatalogo> {
  const { data } = await api.post<FeedlotCatalogo>(`${BASE}/motivos-muerte`, cuerpo);
  return data;
}

export async function actualizarMotivoMuerte(
  id: number,
  cuerpo: FeedlotCatalogoSolicitud
): Promise<FeedlotCatalogo> {
  const { data } = await api.put<FeedlotCatalogo>(`${BASE}/motivos-muerte/${id}`, cuerpo);
  return data;
}

export async function listarProveedores(): Promise<FeedlotProveedorOrigen[]> {
  const { data } = await api.get<FeedlotProveedorOrigen[]>(`${BASE}/proveedores`);
  return Array.isArray(data) ? data : [];
}

export async function crearProveedor(
  cuerpo: FeedlotProveedorOrigenSolicitud
): Promise<FeedlotProveedorOrigen> {
  const { data } = await api.post<FeedlotProveedorOrigen>(`${BASE}/proveedores`, cuerpo);
  return data;
}

export async function actualizarProveedor(
  id: number,
  cuerpo: FeedlotProveedorOrigenSolicitud
): Promise<FeedlotProveedorOrigen> {
  const { data } = await api.put<FeedlotProveedorOrigen>(`${BASE}/proveedores/${id}`, cuerpo);
  return data;
}

// —— Lotes ——

export async function listarLotes(filtros?: FiltrosListadoLotesFeedlot): Promise<FeedlotLote[]> {
  const { data } = await api.get<FeedlotLote[]>(`${BASE}/lotes`, {
    params: paramsLotesFeedlot(filtros),
  });
  return Array.isArray(data) ? data : [];
}

export async function obtenerLote(id: number): Promise<FeedlotLote> {
  const { data } = await api.get<FeedlotLote>(`${BASE}/lotes/${id}`);
  return data;
}

export async function crearLote(cuerpo: FeedlotLoteSolicitud): Promise<FeedlotLote> {
  const { data } = await api.post<FeedlotLote>(`${BASE}/lotes`, cuerpo);
  return data;
}

export async function actualizarLote(id: number, cuerpo: FeedlotLoteSolicitud): Promise<FeedlotLote> {
  const { data } = await api.put<FeedlotLote>(`${BASE}/lotes/${id}`, cuerpo);
  return data;
}

export async function cerrarLote(
  id: number,
  cuerpo?: FeedlotCierreLoteSolicitud
): Promise<FeedlotLote> {
  const { data } = await api.post<FeedlotLote>(`${BASE}/lotes/${id}/cierre`, cuerpo ?? {});
  return data;
}

export async function obtenerResumenLote(id: number): Promise<FeedlotResumen> {
  const { data } = await api.get<FeedlotResumen>(`${BASE}/lotes/${id}/resumen`);
  return data;
}

export async function obtenerCloseout(id: number): Promise<FeedlotCloseout> {
  const { data } = await api.get<FeedlotCloseout>(`${BASE}/lotes/${id}/closeout`);
  return data;
}

export async function registrarAjustePlantel(
  loteId: number,
  cuerpo: FeedlotAjustePlantelSolicitud
): Promise<FeedlotAjustePlantel> {
  const { data } = await api.post<FeedlotAjustePlantel>(
    `${BASE}/lotes/${loteId}/ajustes-plantel`,
    cuerpo
  );
  return data;
}

export async function listarAjustesPlantel(loteId: number): Promise<FeedlotAjustePlantel[]> {
  const { data } = await api.get<FeedlotAjustePlantel[]>(`${BASE}/lotes/${loteId}/ajustes-plantel`);
  return Array.isArray(data) ? data : [];
}

// —— Pesadas ——

export async function listarPesadas(loteId: number): Promise<FeedlotPesada[]> {
  const { data } = await api.get<FeedlotPesada[]>(`${BASE}/lotes/${loteId}/pesadas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarPesada(
  loteId: number,
  cuerpo: FeedlotPesadaSolicitud
): Promise<FeedlotPesada> {
  const { data } = await api.post<FeedlotPesada>(`${BASE}/lotes/${loteId}/pesadas`, cuerpo);
  return data;
}

export async function actualizarPesada(
  loteId: number,
  pesadaId: number,
  cuerpo: FeedlotPesadaSolicitud
): Promise<FeedlotPesada> {
  const { data } = await api.put<FeedlotPesada>(
    `${BASE}/lotes/${loteId}/pesadas/${pesadaId}`,
    cuerpo
  );
  return data;
}

export async function eliminarPesada(loteId: number, pesadaId: number): Promise<void> {
  await api.delete(`${BASE}/lotes/${loteId}/pesadas/${pesadaId}`);
}

// —— Consumos ——

export async function listarConsumos(loteId: number): Promise<FeedlotConsumo[]> {
  const { data } = await api.get<FeedlotConsumo[]>(`${BASE}/lotes/${loteId}/consumos`);
  return Array.isArray(data) ? data : [];
}

export async function registrarConsumo(
  loteId: number,
  cuerpo: FeedlotConsumoSolicitud
): Promise<FeedlotConsumo> {
  const { data } = await api.post<FeedlotConsumo>(`${BASE}/lotes/${loteId}/consumos`, cuerpo);
  return data;
}

export async function actualizarConsumo(
  loteId: number,
  consumoId: number,
  cuerpo: FeedlotConsumoActualizarSolicitud
): Promise<FeedlotConsumo> {
  const { data } = await api.put<FeedlotConsumo>(
    `${BASE}/lotes/${loteId}/consumos/${consumoId}`,
    cuerpo
  );
  return data;
}

export async function eliminarConsumo(loteId: number, consumoId: number): Promise<void> {
  await api.delete(`${BASE}/lotes/${loteId}/consumos/${consumoId}`);
}

// —— Muertes ——

export async function listarMuertes(loteId: number): Promise<FeedlotMuerte[]> {
  const { data } = await api.get<FeedlotMuerte[]>(`${BASE}/lotes/${loteId}/muertes`);
  return Array.isArray(data) ? data : [];
}

export async function registrarMuerte(
  loteId: number,
  cuerpo: FeedlotMuerteSolicitud
): Promise<FeedlotMuerte> {
  const { data } = await api.post<FeedlotMuerte>(`${BASE}/lotes/${loteId}/muertes`, cuerpo);
  return data;
}

// —— Eventos sanitarios ——

export async function listarEventosSanitarios(loteId: number): Promise<FeedlotEventoSanitario[]> {
  const { data } = await api.get<FeedlotEventoSanitario[]>(
    `${BASE}/lotes/${loteId}/eventos-sanitarios`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarEventoSanitario(
  loteId: number,
  cuerpo: FeedlotEventoSanitarioSolicitud
): Promise<FeedlotEventoSanitario> {
  const { data } = await api.post<FeedlotEventoSanitario>(
    `${BASE}/lotes/${loteId}/eventos-sanitarios`,
    cuerpo
  );
  return data;
}

export async function actualizarEventoSanitario(
  loteId: number,
  eventoId: number,
  cuerpo: FeedlotEventoSanitarioSolicitud
): Promise<FeedlotEventoSanitario> {
  const { data } = await api.put<FeedlotEventoSanitario>(
    `${BASE}/lotes/${loteId}/eventos-sanitarios/${eventoId}`,
    cuerpo
  );
  return data;
}

export async function eliminarEventoSanitario(loteId: number, eventoId: number): Promise<void> {
  await api.delete(`${BASE}/lotes/${loteId}/eventos-sanitarios/${eventoId}`);
}

// —— Ventas ——

export async function listarVentas(loteId: number): Promise<FeedlotVenta[]> {
  const { data } = await api.get<FeedlotVenta[]>(`${BASE}/lotes/${loteId}/ventas`);
  return Array.isArray(data) ? data : [];
}

export async function registrarVenta(
  loteId: number,
  cuerpo: FeedlotVentaSolicitud
): Promise<FeedlotVenta> {
  const { data } = await api.post<FeedlotVenta>(`${BASE}/lotes/${loteId}/ventas`, cuerpo);
  return data;
}

// —— Dietas (v1.5) ——

export async function listarDietas(): Promise<FeedlotDieta[]> {
  const { data } = await api.get<FeedlotDieta[]>(`${BASE}/dietas`);
  return Array.isArray(data) ? data : [];
}

export async function obtenerDieta(id: number): Promise<FeedlotDieta> {
  const { data } = await api.get<FeedlotDieta>(`${BASE}/dietas/${id}`);
  return data;
}

export async function crearDieta(cuerpo: FeedlotDietaSolicitud): Promise<FeedlotDieta> {
  const { data } = await api.post<FeedlotDieta>(`${BASE}/dietas`, cuerpo);
  return data;
}

export async function actualizarDieta(id: number, cuerpo: FeedlotDietaSolicitud): Promise<FeedlotDieta> {
  const { data } = await api.put<FeedlotDieta>(`${BASE}/dietas/${id}`, cuerpo);
  return data;
}

export async function crearFaseDieta(
  dietaId: number,
  cuerpo: FeedlotDietaFaseSolicitud
): Promise<FeedlotDietaFase> {
  const { data } = await api.post<FeedlotDietaFase>(
    `${BASE}/dietas/${dietaId}/fases`,
    cuerpo
  );
  return data;
}

export async function actualizarFaseDieta(
  dietaId: number,
  faseId: number,
  cuerpo: FeedlotDietaFaseSolicitud
): Promise<FeedlotDietaFase> {
  const { data } = await api.put<FeedlotDietaFase>(
    `${BASE}/dietas/${dietaId}/fases/${faseId}`,
    cuerpo
  );
  return data;
}

// —— Lecturas comedero (v1.5) ——

export async function listarLecturasComedero(loteId: number): Promise<FeedlotLecturaComedero[]> {
  const { data } = await api.get<FeedlotLecturaComedero[]>(
    `${BASE}/lotes/${loteId}/lecturas-comedero`
  );
  return Array.isArray(data) ? data : [];
}

export async function registrarLecturaComedero(
  loteId: number,
  cuerpo: FeedlotLecturaComederoSolicitud
): Promise<FeedlotLecturaComedero> {
  const { data } = await api.post<FeedlotLecturaComedero>(
    `${BASE}/lotes/${loteId}/lecturas-comedero`,
    cuerpo
  );
  return data;
}

export async function actualizarLecturaComedero(
  loteId: number,
  lecturaId: number,
  cuerpo: FeedlotLecturaComederoSolicitud
): Promise<FeedlotLecturaComedero> {
  const { data } = await api.put<FeedlotLecturaComedero>(
    `${BASE}/lotes/${loteId}/lecturas-comedero/${lecturaId}`,
    cuerpo
  );
  return data;
}

export async function eliminarLecturaComedero(loteId: number, lecturaId: number): Promise<void> {
  await api.delete(`${BASE}/lotes/${loteId}/lecturas-comedero/${lecturaId}`);
}

// —— Consumo teórico y reportes (v1.5) ——

export async function obtenerConsumoTeorico(
  loteId: number,
  fecha?: string
): Promise<FeedlotConsumoTeorico> {
  const params: Record<string, string> = {};
  if (fecha) {
    params.fechaDesde = fecha;
    params.fechaHasta = fecha;
  }
  const { data } = await api.get<FeedlotConsumoTeorico>(
    `${BASE}/lotes/${loteId}/consumo-teorico`,
    { params }
  );
  return data;
}

export async function obtenerCurvaPeso(
  loteId: number,
  pesoObjetivo?: number
): Promise<FeedlotCurvaPeso> {
  const params: Record<string, number> = {};
  if (pesoObjetivo != null) {
    params.pesoObjetivoKg = pesoObjetivo;
  }
  const { data } = await api.get<FeedlotCurvaPeso>(
    `${BASE}/reportes/lote/${loteId}/curva-peso`,
    { params }
  );
  return data;
}

export async function obtenerAnalisisLotes(): Promise<FeedlotAnalisisLotes> {
  const { data } = await api.get<FeedlotAnalisisLotes>(`${BASE}/reportes/analisis-lotes`);
  return data;
}

export async function exportarReportesExcel(): Promise<void> {
  const response = await api.get(`${BASE}/reportes/exportar`, { responseType: 'blob' });
  const fecha = new Date().toISOString().slice(0, 10).replace(/-/g, '');
  descargarBlob(
    new Blob([response.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    }),
    `Feedlot_Comparativa_${fecha}.xlsx`
  );
}

export async function descargarCloseoutPdf(loteId: number): Promise<void> {
  const response = await api.get(`${BASE}/lotes/${loteId}/closeout/pdf`, { responseType: 'blob' });
  descargarBlob(new Blob([response.data], { type: 'application/pdf' }), `Closeout_Lote_${loteId}.pdf`);
}

// —— Configuración closeout (v1.5) ——

export async function obtenerConfigCloseout(): Promise<FeedlotConfiguracionCloseout> {
  const { data } = await api.get<FeedlotConfiguracionCloseout>(`${BASE}/configuracion/closeout`);
  return data;
}

export async function actualizarConfigCloseout(
  cuerpo: FeedlotConfiguracionCloseoutSolicitud
): Promise<FeedlotConfiguracionCloseout> {
  const { data } = await api.put<FeedlotConfiguracionCloseout>(
    `${BASE}/configuracion/closeout`,
    cuerpo
  );
  return data;
}
