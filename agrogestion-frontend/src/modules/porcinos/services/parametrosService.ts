/**
 * Servicio para gestión de Parámetros del módulo Porcinos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type {
  ParametrosEstablecimientoPorcino,
  ParametrosProductivosPorcino,
  DatosEconomicosPorcino,
} from '../types';

export const parametrosService = {
  // ============================================================================
  // PARÁMETROS DEL ESTABLECIMIENTO
  // ============================================================================
  
  async obtenerParametrosEstablecimiento(): Promise<ParametrosEstablecimientoPorcino | null> {
    try {
      const response = await api.get(API_ENDPOINTS.PORCINOS_PARAMETROS.ESTABLECIMIENTO);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async guardarParametrosEstablecimiento(
    parametros: Partial<ParametrosEstablecimientoPorcino>
  ): Promise<ParametrosEstablecimientoPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PARAMETROS.ESTABLECIMIENTO, parametros);
    return response.data;
  },

  // ============================================================================
  // PARÁMETROS PRODUCTIVOS
  // ============================================================================
  
  async obtenerParametrosProductivos(): Promise<ParametrosProductivosPorcino | null> {
    try {
      const response = await api.get(API_ENDPOINTS.PORCINOS_PARAMETROS.PRODUCTIVOS);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async guardarParametrosProductivos(
    parametros: Partial<ParametrosProductivosPorcino>
  ): Promise<ParametrosProductivosPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PARAMETROS.PRODUCTIVOS, parametros);
    return response.data;
  },

  // ============================================================================
  // DATOS ECONÓMICOS
  // ============================================================================
  
  async obtenerDatosEconomicos(): Promise<DatosEconomicosPorcino | null> {
    try {
      const response = await api.get(API_ENDPOINTS.PORCINOS_PARAMETROS.ECONOMICOS);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  async guardarDatosEconomicos(
    datos: Partial<DatosEconomicosPorcino>
  ): Promise<DatosEconomicosPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_PARAMETROS.ECONOMICOS, datos);
    return response.data;
  },
};

