/**
 * Servicio para gestión de Movimientos entre Etapas
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { MovimientoEtapa, EtapaRecria } from '../types';

export const movimientosService = {
  async crear(recriaId: number, movimientoData: Partial<MovimientoEtapa>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_MOVIMIENTOS.CREAR(recriaId), movimientoData);
    return response.data;
  },

  async obtenerPorRecria(recriaId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_MOVIMIENTOS.POR_RECRIA(recriaId));
    return response.data;
  },

  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_MOVIMIENTOS.LISTAR);
    return response.data;
  },
};







