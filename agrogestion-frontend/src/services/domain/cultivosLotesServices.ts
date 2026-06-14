/**
 * Servicios de cultivos y lotes.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

export const cultivosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.OBTENER(id));
    return response.data;
  },

  async crear(cultivoData: any) {
    const response = await api.post(API_ENDPOINTS.CULTIVOS.CREAR, cultivoData);
    return response.data;
  },

  async actualizar(id: number, cultivoData: any) {
    const response = await api.put(API_ENDPOINTS.CULTIVOS.ACTUALIZAR(id), cultivoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.CULTIVOS.ELIMINAR(id));
    return response.data;
  },

  async eliminarFisico(id: number) {
    const response = await api.delete(API_ENDPOINTS.CULTIVOS.ELIMINAR_FISICO(id));
    return response.data;
  },

  async restaurar(id: number) {
    const response = await api.put(API_ENDPOINTS.CULTIVOS.RESTAURAR(id), {});
    return response.data;
  },

  async buscar(nombre: string) {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.BUSCAR, { params: { nombre } });
    return response.data;
  },

  async obtenerEliminados() {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.ELIMINADOS);
    return response.data;
  },
};

export const lotesService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.LOTES.LISTAR);
    return response.data;
  },

  async listarCultivo() {
    const response = await api.get(API_ENDPOINTS.LOTES.LISTAR_CULTIVO);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.LOTES.OBTENER(id));
    return response.data;
  },

  async crear(loteData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.CREAR, loteData);
    return response.data;
  },

  async actualizar(id: number, loteData: any) {
    const response = await api.put(API_ENDPOINTS.LOTES.ACTUALIZAR(id), loteData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.LOTES.ELIMINAR(id));
    return response.data;
  },

  async sembrar(id: number, siembraData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.SEMBRAR(id), siembraData);
    return response.data;
  },

  async resetear(id: number, estadoInicialId: number, motivo: string) {
    const response = await api.post(API_ENDPOINTS.LOTES.RESETEAR(id), { estadoInicialId, motivo });
    return response.data;
  },

  async cosechar(id: number, cosechaData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.COSECHAR(id), cosechaData);
    return response.data;
  },

  async obtenerInfoCosecha(id: number) {
    const response = await api.get(API_ENDPOINTS.LOTES.INFO_COSECHA(id));
    return response.data;
  },

  async abandonar(id: number, motivo: string) {
    const response = await api.post(API_ENDPOINTS.LOTES.ABANDONAR(id), { motivo });
    return response.data;
  },

  async convertirForraje(id: number, cosechaData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.CONVERTIR_FORRAJE(id), cosechaData);
    return response.data;
  },
};
