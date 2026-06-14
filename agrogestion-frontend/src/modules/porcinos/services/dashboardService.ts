/**
 * Servicio para Dashboard de Porcinos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { DashboardKPIs, Alerta } from '../types';

export const dashboardService = {
  async obtenerKPIs() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_DASHBOARD.KPIS);
    return response.data as DashboardKPIs;
  },

  async obtenerAlertas() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_DASHBOARD.ALERTAS);
    return response.data as Alerta[];
  },
};

