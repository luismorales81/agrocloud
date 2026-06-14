/**
 * Servicio para gestión de Chequeos de Gestación
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { ChequeoGestacion } from '../types';

export const chequeoGestacionService = {
  async crear(gestacionId: number, chequeoData: Partial<ChequeoGestacion>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_GESTACION.CHEQUEOS(gestacionId), chequeoData);
    return response.data;
  },

  async obtenerPorGestacion(gestacionId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_GESTACION.CHEQUEOS(gestacionId));
    return response.data;
  },
};







