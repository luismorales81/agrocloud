/**
 * Servicio para gestión de Transferencias de Lechones
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { TransferenciaLechon } from '../types';

export const transferenciasService = {
  async crear(transferenciaData: Partial<TransferenciaLechon>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_TRANSFERENCIAS.CREAR, transferenciaData);
    return response.data;
  },

  async obtenerPorParto(partoId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_TRANSFERENCIAS.POR_PARTO(partoId));
    return response.data;
  },

  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_TRANSFERENCIAS.LISTAR);
    return response.data;
  },
};







