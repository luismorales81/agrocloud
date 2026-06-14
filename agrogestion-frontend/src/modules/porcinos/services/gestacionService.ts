/**
 * Servicio para gestión de Gestaciones
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Gestacion } from '../types';

export const gestacionService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_GESTACION.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_GESTACION.OBTENER(id));
    return response.data;
  },

  async crear(gestacionData: Partial<Gestacion>) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_GESTACION.CREAR, gestacionData);
    return response.data;
  },

  async actualizar(id: number, gestacionData: Partial<Gestacion>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_GESTACION.ACTUALIZAR(id), gestacionData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_GESTACION.ELIMINAR(id));
    return response.data;
  },

  async obtenerActivas() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_GESTACION.ACTIVAS);
    return response.data;
  },

  /** Gestaciones con parto probable en los próximos `dias` días (cliente). */
  async obtenerProximosPartos(dias: number): Promise<Gestacion[]> {
    const activas: Gestacion[] = await gestacionService.obtenerActivas();
    const ahora = new Date();
    const limite = new Date();
    limite.setDate(limite.getDate() + dias);
    return activas.filter((g) => {
      const fp = new Date(g.fechaProbableParto);
      return !Number.isNaN(fp.getTime()) && fp >= ahora && fp <= limite;
    });
  },

  async obtenerPorMadre(madreId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_GESTACION.POR_MADRE(madreId));
    return response.data;
  },

  async registrarAborto(gestacionId: number, fechaAborto: string, causaAborto: string) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_GESTACION.REGISTRAR_ABORTO(gestacionId), {
      fechaAborto,
      causaAborto,
    });
    return response.data;
  },


  async finalizar(gestacionId: number) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_GESTACION.FINALIZAR(gestacionId));
    return response.data;
  },
};

