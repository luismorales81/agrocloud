/**
 * Servicio para gestión de Registros de Peso
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { RegistroPeso } from '../types';

export const pesosService = {
  async registrar(recriaId: number, registroPesoData: Partial<RegistroPeso>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PESOS.REGISTRAR(recriaId), registroPesoData);
    return response.data;
  },

  async obtenerPorRecria(recriaId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PESOS.POR_RECRIA(recriaId));
    return response.data;
  },

  async calcularGDP(recriaId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_PESOS.GDP(recriaId));
    return response.data;
  },
};







