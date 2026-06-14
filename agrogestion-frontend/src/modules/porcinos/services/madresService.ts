/**
 * Servicio para gestión de Madres / Unidades productivas
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Madre, MadreCreateDTO, FiltrosMadres } from '../types';

export const madresService = {
  async listar(filtros?: FiltrosMadres) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_MADRES.LISTAR, {
      params: filtros,
    });
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_MADRES.OBTENER(id));
    return response.data;
  },

  async crear(madreData: MadreCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_MADRES.CREAR, madreData);
    return response.data;
  },

  async actualizar(id: number, madreData: Partial<Madre>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_MADRES.ACTUALIZAR(id), madreData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_MADRES.ELIMINAR(id));
    return response.data;
  },

  async obtenerHistorial(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_MADRES.HISTORIAL(id));
    return response.data;
  },

  async cambiarEstado(id: number, nuevoEstado: string, observaciones?: string) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_MADRES.CAMBIAR_ESTADO(id), {
      estado: nuevoEstado,
      observaciones,
    });
    return response.data;
  },
};

