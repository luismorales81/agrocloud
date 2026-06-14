/**
 * Servicio para gestión de Ventas de Porcinos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { VentaPorcino } from '../types';

export const ventaPorcinoService = {
  async crear(ventaData: Partial<VentaPorcino>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_VENTAS.CREAR, ventaData);
    return response.data;
  },

  async listar(fechaDesde?: string, fechaHasta?: string) {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await api.get(`${API_ENDPOINTS.PORCINOS_VENTAS.LISTAR}?${params.toString()}`);
    return response.data;
  },

  async obtenerIngresosTotales(fechaDesde?: string, fechaHasta?: string) {
    const params = new URLSearchParams();
    if (fechaDesde) params.append('fechaDesde', fechaDesde);
    if (fechaHasta) params.append('fechaHasta', fechaHasta);
    const response = await api.get(`${API_ENDPOINTS.PORCINOS_VENTAS.INGRESOS_TOTALES}?${params.toString()}`);
    return response.data;
  },
};







