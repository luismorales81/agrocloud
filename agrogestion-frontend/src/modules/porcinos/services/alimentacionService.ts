/**
 * Servicio para gestión de Alimentación (Fórmulas y Consumos)
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type { Formula, FormulaCreateDTO, ConsumoAlimentacion, ConsumoCreateDTO } from '../types';

export const alimentacionService = {
  // Fórmulas
  async listarFormulas() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_FORMULAS.LISTAR);
    return response.data;
  },

  async obtenerFormula(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_FORMULAS.OBTENER(id));
    return response.data;
  },

  async crearFormula(formulaData: FormulaCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_FORMULAS.CREAR, formulaData);
    return response.data;
  },

  async actualizarFormula(id: number, formulaData: Partial<Formula>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_FORMULAS.ACTUALIZAR(id), formulaData);
    return response.data;
  },

  async eliminarFormula(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_FORMULAS.ELIMINAR(id));
    return response.data;
  },

  async obtenerFormulasPorEtapa(etapa: string) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_FORMULAS.POR_ETAPA(etapa));
    return response.data;
  },

  async activarFormula(id: number) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_FORMULAS.ACTIVAR(id));
    return response.data;
  },

  async desactivarFormula(id: number) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_FORMULAS.DESACTIVAR(id));
    return response.data;
  },

  // Consumos
  async listarConsumos() {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONSUMOS.LISTAR);
    return response.data;
  },

  async obtenerConsumo(id: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONSUMOS.OBTENER(id));
    return response.data;
  },

  async crearConsumo(consumoData: ConsumoCreateDTO) {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CONSUMOS.CREAR, consumoData);
    return response.data;
  },

  async actualizarConsumo(id: number, consumoData: Partial<ConsumoAlimentacion>) {
    const response = await api.put(API_ENDPOINTS.PORCINOS_CONSUMOS.ACTUALIZAR(id), consumoData);
    return response.data;
  },

  async eliminarConsumo(id: number) {
    const response = await api.delete(API_ENDPOINTS.PORCINOS_CONSUMOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerConsumosPorLote(loteId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONSUMOS.POR_LOTE(loteId));
    return response.data;
  },

  async obtenerConsumosPorMadre(madreId: number) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONSUMOS.POR_MADRE(madreId));
    return response.data;
  },

  async obtenerHistorialConsumos(filtros?: { fechaDesde?: string; fechaHasta?: string; loteId?: number; madreId?: number }) {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CONSUMOS.HISTORIAL, {
      params: filtros,
    });
    return response.data;
  },
};

