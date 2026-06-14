/**
 * Servicios de ingresos, egresos y balance.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

export const ingresosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.INGRESOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.INGRESOS.OBTENER(id));
    return response.data;
  },

  async crear(ingresoData: any) {
    const response = await api.post(API_ENDPOINTS.INGRESOS.CREAR, ingresoData);
    return response.data;
  },

  async actualizar(id: number, ingresoData: any) {
    const response = await api.put(API_ENDPOINTS.INGRESOS.ACTUALIZAR(id), ingresoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.INGRESOS.ELIMINAR(id));
    return response.data;
  },

  async listarV1() {
    const response = await api.get(API_ENDPOINTS.INGRESOS.V1.LISTAR);
    return response.data;
  },

  async crearV1(ingresoData: any) {
    const response = await api.post(API_ENDPOINTS.INGRESOS.V1.CREAR, ingresoData);
    return response.data;
  },

  async actualizarV1(id: number, ingresoData: any) {
    const response = await api.put(API_ENDPOINTS.INGRESOS.V1.ACTUALIZAR(id), ingresoData);
    return response.data;
  },

  async eliminarV1(id: number) {
    const response = await api.delete(API_ENDPOINTS.INGRESOS.V1.ELIMINAR(id));
    return response.data;
  },
};

export const egresosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.EGRESOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.EGRESOS.OBTENER(id));
    return response.data;
  },

  async crear(egresoData: any) {
    const response = await api.post(API_ENDPOINTS.EGRESOS.CREAR, egresoData);
    return response.data;
  },

  async actualizar(id: number, egresoData: any) {
    const response = await api.put(API_ENDPOINTS.EGRESOS.ACTUALIZAR(id), egresoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.EGRESOS.ELIMINAR(id));
    return response.data;
  },

  async listarV1() {
    const response = await api.get(API_ENDPOINTS.EGRESOS.V1.LISTAR);
    return response.data;
  },

  async crearIntegrado(egresoData: any) {
    const response = await api.post(API_ENDPOINTS.EGRESOS.V1.INTEGRADO, egresoData);
    return response.data;
  },

  async eliminarV1(id: number) {
    const response = await api.delete(API_ENDPOINTS.EGRESOS.V1.ELIMINAR(id));
    return response.data;
  },
};

export const balanceService = {
  async obtenerGeneral(fechaInicio: string, fechaFin: string) {
    const response = await api.get(API_ENDPOINTS.BALANCE.GENERAL(fechaInicio, fechaFin));
    return response.data;
  },

  async obtenerPorLote(loteId: number, fechaInicio: string, fechaFin: string) {
    const response = await api.get(API_ENDPOINTS.BALANCE.LOTE(loteId, fechaInicio, fechaFin));
    return response.data;
  },

  async obtenerMesActual() {
    const response = await api.get(API_ENDPOINTS.BALANCE.MES_ACTUAL);
    return response.data;
  },

  async obtenerAñoActual() {
    const response = await api.get(API_ENDPOINTS.BALANCE.AÑO_ACTUAL);
    return response.data;
  },
};
