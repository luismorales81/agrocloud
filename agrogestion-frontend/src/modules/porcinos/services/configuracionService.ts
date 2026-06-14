/**
 * Servicio para gestión de Configuraciones del módulo Porcinos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { ConfiguracionPorcino } from '../types';

/**
 * Debe coincidir con {@code CalendarioAlimentacionService.CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL} en el backend.
 */
export const CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL = 'CONFIRMAR_CALENDARIO_SOLO_CON_REAL';

export const configuracionService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONFIGURACIONES.LISTAR);
    return response.data;
  },

  async obtener(clave: string) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONFIGURACIONES.OBTENER(clave));
    return response.data;
  },

  async obtenerPorCategoria(categoria: string) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONFIGURACIONES.POR_CATEGORIA(categoria));
    return response.data;
  },

  async guardar(configuracion: Partial<ConfiguracionPorcino>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CONFIGURACIONES.GUARDAR, configuracion);
    return response.data;
  },

  // Helpers para obtener valores comunes
  // NOTA: Solo mantener configuraciones que NO están en Parámetros Productivos
  // DIAS_CACHORRA es la única que se usa exclusivamente aquí (en MadreService)
  // Para días de gestación, lactancia, entre celos y control de celo, usar Parámetros Productivos
  async obtenerDiasCachorra(): Promise<number> {
    try {
      const config = await this.obtener('DIAS_CACHORRA');
      return parseInt(config?.valor || '160');
    } catch {
      return 160;
    }
  },

  /**
   * Si es verdadero, la empresa no puede confirmar un día del calendario de alimentación con consumos aún estimados.
   */
  async obtenerPoliticaConfirmacionCalendarioSoloConKgReal(): Promise<boolean> {
    try {
      const c = await this.obtener(CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL);
      const v = String(c?.valor ?? '').trim().toLowerCase();
      return v === 'true' || v === '1';
    } catch {
      return false;
    }
  },

  async guardarPoliticaConfirmacionCalendarioSoloConKgReal(activo: boolean): Promise<ConfiguracionPorcino> {
    return this.guardar({
      clave: CLAVE_CONFIRMACION_CALENDARIO_SOLO_CON_RACION_REAL,
      valor: activo ? 'true' : 'false',
      tipo: 'BOOLEAN',
      categoria: 'ALIMENTACION',
      descripcion:
        'Si está activo, no se puede confirmar un día del calendario de alimentación mientras quede algún consumo en modo estimado (sin kg real de ración).',
    });
  },
};







