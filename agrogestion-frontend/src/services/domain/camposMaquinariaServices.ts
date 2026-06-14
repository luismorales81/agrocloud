/**
 * Servicios de campos (campos) y maquinaria.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

export const camposService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.CAMPOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.CAMPOS.OBTENER(id));
    return response.data;
  },

  async crear(campoData: any) {
    const response = await api.post(API_ENDPOINTS.CAMPOS.CREAR, campoData);
    return response.data;
  },

  async actualizar(id: number, campoData: any) {
    const response = await api.put(API_ENDPOINTS.CAMPOS.ACTUALIZAR(id), campoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.CAMPOS.ELIMINAR(id));
    return response.data;
  },

  async listarPublicos() {
    const response = await api.get('/public/campos');
    return response.data;
  },

  async listarV1() {
    const response = await api.get('/v1/campos');
    return response.data;
  },
};

export const maquinariaService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.MAQUINARIA.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.MAQUINARIA.OBTENER(id));
    return response.data;
  },

  async crear(maquinariaData: any) {
    const response = await api.post(API_ENDPOINTS.MAQUINARIA.CREAR, maquinariaData);
    return response.data;
  },

  async actualizar(id: number, maquinariaData: any) {
    const response = await api.put(API_ENDPOINTS.MAQUINARIA.ACTUALIZAR(id), maquinariaData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.MAQUINARIA.ELIMINAR(id));
    return response.data;
  },
};
