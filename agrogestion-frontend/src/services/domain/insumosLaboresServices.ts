/**
 * Servicios de insumos, dosis de agroquímicos y labores.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';
import type { LaborDetalladoDTO } from '../../types/labor.types';

export const insumosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.INSUMOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.INSUMOS.OBTENER(id));
    return response.data;
  },

  async crear(insumoData: any) {
    const response = await api.post(API_ENDPOINTS.INSUMOS.CREAR, insumoData);
    return response.data;
  },

  async crearConDosis(insumoData: any) {
    const response = await api.post(API_ENDPOINTS.INSUMOS.CREAR_CON_DOSIS, insumoData);
    return response.data;
  },

  async actualizar(id: number, insumoData: any) {
    const response = await api.put(API_ENDPOINTS.INSUMOS.ACTUALIZAR(id), insumoData);
    return response.data;
  },

  async actualizarConDosis(id: number, insumoData: any) {
    const response = await api.put(API_ENDPOINTS.INSUMOS.ACTUALIZAR_CON_DOSIS(id), insumoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.INSUMOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerConDosis(id: number) {
    const response = await api.get(API_ENDPOINTS.INSUMOS.CON_DOSIS(id));
    return response.data;
  },

  async listarPublicos() {
    const response = await api.get('/public/insumos');
    return response.data;
  },

  async actualizarPublico(id: number, insumoData: any) {
    const response = await api.put(`/public/insumos/${id}`, insumoData);
    return response.data;
  },
};

export const dosisAgroquimicosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.DOSIS_AGROQUIMICOS.LISTAR);
    return response.data;
  },

  async obtenerPorInsumo(insumoId: number) {
    const response = await api.get(API_ENDPOINTS.DOSIS_AGROQUIMICOS.POR_INSUMO(insumoId));
    return response.data;
  },

  async crear(dosisData: any) {
    const response = await api.post(API_ENDPOINTS.DOSIS_AGROQUIMICOS.CREAR, dosisData);
    return response.data;
  },

  async actualizar(id: number, dosisData: any) {
    const response = await api.put(API_ENDPOINTS.DOSIS_AGROQUIMICOS.ACTUALIZAR(id), dosisData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.DOSIS_AGROQUIMICOS.ELIMINAR(id));
    return response.data;
  },
};

export const laboresService = {
  async listar(): Promise<LaborDetalladoDTO[]> {
    const response = await api.get<LaborDetalladoDTO[]>(API_ENDPOINTS.LABORES.LISTAR);
    return response.data;
  },

  async listarConFiltros(params: {
    fecha_desde?: string;
    fecha_hasta?: string;
    lote_id?: number;
    estado?: string;
    overdue?: boolean;
  }) {
    const searchParams = new URLSearchParams();
    if (params.fecha_desde) searchParams.set('fecha_desde', params.fecha_desde);
    if (params.fecha_hasta) searchParams.set('fecha_hasta', params.fecha_hasta);
    if (params.lote_id != null) searchParams.set('lote_id', String(params.lote_id));
    if (params.estado) searchParams.set('estado', params.estado);
    if (params.overdue === true) searchParams.set('overdue', 'true');
    const query = searchParams.toString();
    const url = query ? `${API_ENDPOINTS.LABORES.LISTAR_CON_FILTROS}?${query}` : API_ENDPOINTS.LABORES.LISTAR_CON_FILTROS;
    const response = await api.get<LaborDetalladoDTO[]>(url);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.LABORES.OBTENER(id));
    return response.data;
  },

  async crear(laborData: any) {
    const response = await api.post(API_ENDPOINTS.LABORES.CREAR, laborData);
    return response.data;
  },

  async actualizar(id: number, laborData: any) {
    const response = await api.put(API_ENDPOINTS.LABORES.ACTUALIZAR(id), laborData);
    return response.data;
  },

  async actualizarParcial(
    id: number,
    payload: { estado?: string; fecha_planificada?: string; fecha_realizacion?: string; observaciones?: string }
  ) {
    const body: Record<string, string> = {};
    if (payload.estado != null) body.estado = payload.estado;
    if (payload.fecha_planificada != null) body.fecha_planificada = payload.fecha_planificada;
    if (payload.fecha_realizacion != null) body.fecha_realizacion = payload.fecha_realizacion;
    if (payload.observaciones != null) body.observaciones = payload.observaciones;
    const response = await api.patch(API_ENDPOINTS.LABORES.ACTUALIZAR_PARCIAL(id), body);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.LABORES.ELIMINAR(id));
    return response.data;
  },

  async anular(id: number, anulacionData: any) {
    const response = await api.post(API_ENDPOINTS.LABORES.ANULAR(id), anulacionData);
    return response.data;
  },

  async actualizarCosto(id: number) {
    const response = await api.post(API_ENDPOINTS.LABORES.ACTUALIZAR_COSTO(id));
    return response.data;
  },

  async obtenerTareasDisponibles(estadoLote: string) {
    const response = await api.get(API_ENDPOINTS.LABORES.TAREAS_DISPONIBLES(estadoLote));
    return response.data;
  },

  async obtenerTareasDisponiblesPorLote(loteId: number) {
    const response = await api.get(API_ENDPOINTS.LABORES.TAREAS_DISPONIBLES_POR_LOTE(loteId));
    return response.data;
  },
};
