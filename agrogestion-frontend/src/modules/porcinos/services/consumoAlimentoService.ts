/**
 * Servicio para gestión de Consumos de Alimento
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { ConsumoAlimento } from '../types';

export const consumoAlimentoService = {
  async crear(consumoData: Partial<ConsumoAlimento>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_ALIMENTACION.REGISTRAR_CONSUMO, consumoData);
    return response.data;
  },

  async listar(fechaDesde?: string, fechaHasta?: string) {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await api.get(`${API_ENDPOINTS.PORCINOS_ALIMENTACION.CONSUMOS}?${params.toString()}`);
    return response.data;
  },

  async obtenerConsumoTotalPorCategoria(categoria: string, fechaDesde?: string, fechaHasta?: string) {
    const params = new URLSearchParams();
    params.append('categoria', categoria);
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await api.get(`${API_ENDPOINTS.PORCINOS_ALIMENTACION.CONSUMO_TOTAL_CATEGORIA}?${params.toString()}`);
    return response.data;
  },
};







