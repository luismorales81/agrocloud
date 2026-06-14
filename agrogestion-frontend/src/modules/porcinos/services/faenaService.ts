/**
 * Servicio para gestión de Faena
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Faena } from '../types';

export const faenaService = {
  async registrar(recriaId: number, faenaData: Partial<Faena>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_FAENA.REGISTRAR(recriaId), faenaData);
    return response.data;
  },

  async obtenerPorRecria(recriaId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_FAENA.POR_RECRIA(recriaId));
    return response.data;
  },

  async listar(fechaDesde?: string, fechaHasta?: string) {
    const params: any = {};
    if (fechaDesde) params.fechaDesde = fechaDesde;
    if (fechaHasta) params.fechaHasta = fechaHasta;
    const response = await api.get(API_ENDPOINTS.PORCINOS_FAENA.LISTAR, { params });
    return response.data;
  },

  async obtenerIngresosTotales(fechaDesde?: string, fechaHasta?: string) {
    const params: any = {};
    if (fechaDesde) params.fechaDesde = fechaDesde;
    if (fechaHasta) params.fechaHasta = fechaHasta;
    const response = await api.get(API_ENDPOINTS.PORCINOS_FAENA.INGRESOS_TOTALES, { params });
    return response.data;
  },
};







