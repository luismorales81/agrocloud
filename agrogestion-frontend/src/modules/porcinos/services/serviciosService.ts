/**
 * Servicio para gestión de Servicios (monta natural e IA)
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Servicio, ServicioCreateDTO, ControlCeloDTO } from '../types';

export const serviciosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_SERVICIOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_SERVICIOS.OBTENER(id));
    return response.data;
  },

  async crear(servicioData: ServicioCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_SERVICIOS.CREAR, servicioData);
    return response.data;
  },

  async actualizar(id: number, servicioData: Partial<Servicio>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_SERVICIOS.ACTUALIZAR(id), servicioData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_SERVICIOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerPorMadre(madreId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_SERVICIOS.POR_MADRE(madreId));
    return response.data;
  },

  async obtenerPendientesControl() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_SERVICIOS.PENDIENTES_CONTROL);
    return response.data;
  },

  async controlCelo(id: number, controlData: ControlCeloDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_SERVICIOS.CONTROL_CELO(id), controlData);
    return response.data;
  },
};

