/**
 * Servicio para gestión de Eventos Sanitarios
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { EventoSanitario, EventoSanitarioCreateDTO, FiltrosEventosSanitarios, TipoEventoSanitario } from '../types';

export const eventosSanitariosService = {
  async listar(filtros?: FiltrosEventosSanitarios) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.LISTAR, {
      params: filtros,
    });
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.OBTENER(id));
    return response.data;
  },

  async crear(eventoData: EventoSanitarioCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.CREAR, eventoData);
    return response.data;
  },

  async actualizar(id: number, eventoData: Partial<EventoSanitarioCreateDTO>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.ACTUALIZAR(id), eventoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerPorEntidad(tipoEntidad: string, entidadId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.POR_ENTIDAD(tipoEntidad, entidadId));
    return response.data;
  },

  async obtenerPorRango(fechaInicio: string, fechaFin: string) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.POR_RANGO, {
      params: { fechaInicio, fechaFin },
    });
    return response.data;
  },

  async marcarRetiroCumplido(id: number) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.MARCAR_RETIRO_CUMPLIDO(id));
    return response.data;
  },

  async obtenerRetirosVencidos() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.RETIROS_VENCIDOS);
    return response.data;
  },

  async obtenerRetirosProximos() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_EVENTOS_SANITARIOS.RETIROS_PROXIMOS);
    return response.data;
  },

  // Tipos de evento sanitario
  async obtenerTiposEventoSanitario() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_EVENTO_SANITARIO);
    return response.data;
  },

  async obtenerTiposEventoSanitarioPorCategoria(categoria: string) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_EVENTO_SANITARIO_POR_CATEGORIA(categoria));
    return response.data;
  },
};














