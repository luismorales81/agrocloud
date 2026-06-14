/**
 * Servicio para gestión de Destetes
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Destete } from '../types';

export const desteteService = {
  async crear(partoId: number, desteteData: Partial<Destete>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_DESTETES.CREAR, {
      ...desteteData,
      partoId
    });
    return response.data;
  },

  async obtenerPorParto(partoId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_DESTETES.POR_PARTO(partoId));
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_DESTETES.OBTENER(id));
    return response.data;
  },

  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_DESTETES.LISTAR);
    return response.data;
  },
};







