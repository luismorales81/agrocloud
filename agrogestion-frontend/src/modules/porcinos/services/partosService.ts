/**
 * Servicio para gestión de Partos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Parto, PartoCreateDTO, FiltrosPartos } from '../types';

export const partosService = {
  async listar(filtros?: FiltrosPartos) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PARTOS.LISTAR, {
      params: filtros,
    });
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PARTOS.OBTENER(id));
    return response.data;
  },

  async crear(partoData: PartoCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PARTOS.CREAR, partoData);
    return response.data;
  },

  async actualizar(id: number, partoData: Partial<Parto>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_PARTOS.ACTUALIZAR(id), partoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_PARTOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerPorMadre(madreId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PARTOS.POR_MADRE(madreId));
    return response.data;
  },

  async obtenerProximos(dias: number = 7) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PARTOS.PROXIMOS, {
      params: { dias },
    });
    return response.data;
  },
};

