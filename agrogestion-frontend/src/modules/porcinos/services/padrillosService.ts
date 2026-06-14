/**
 * Servicio para gestión de Padrillos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Padrillo } from '../types';

export const padrillosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PADRILLOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PADRILLOS.OBTENER(id));
    return response.data;
  },

  async crear(padrilloData: Partial<Padrillo>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PADRILLOS.CREAR, padrilloData);
    return response.data;
  },

  async actualizar(id: number, padrilloData: Partial<Padrillo>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_PADRILLOS.ACTUALIZAR(id), padrilloData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_PADRILLOS.ELIMINAR(id));
    return response.data;
  },
};







