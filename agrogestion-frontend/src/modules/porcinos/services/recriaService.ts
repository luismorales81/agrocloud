/**
 * Servicio para gestión de Recría
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Recria, RecriaIngresoDTO, MuerteRecria, FiltrosRecria } from '../types';

export const recriaService = {
  async listar(filtros?: FiltrosRecria) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_RECRIA.LISTAR, {
      params: filtros,
    });
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_RECRIA.OBTENER(id));
    return response.data;
  },

  async crear(recriaData: RecriaIngresoDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_RECRIA.CREAR, recriaData);
    return response.data;
  },

  async actualizar(id: number, recriaData: Partial<Recria>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_RECRIA.ACTUALIZAR(id), recriaData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_RECRIA.ELIMINAR(id));
    return response.data;
  },

  async obtenerPorLote(loteId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_RECRIA.POR_LOTE(loteId));
    return response.data;
  },

  async registrarMuerte(id: number, muerteData: MuerteRecria) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_RECRIA.REGISTRAR_MUERTE(id), muerteData);
    return response.data;
  },

  async registrarEgreso(id: number, fechaSalida: string, destino: string, observaciones?: string) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_RECRIA.REGISTRAR_EGRESO(id), {
      fechaSalida,
      destino,
      observaciones,
    });
    return response.data;
  },
};

