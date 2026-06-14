/**
 * Servicio compartido para gestión de Insumos Compuestos (Recetas/Formulas)
 * Disponible para módulos de Cultivos y Porcinos
 */
import api from './api';
import type {
  InsumoCompuesto,
  ComponenteInsumoCompuesto,
  RecetaAlimentacionPorEtapa,
  EtapaAlimentacion,
} from '../modules/porcinos/types';

const BASE_URL = '/v1/insumos-compuestos';

export const insumosCompuestosService = {
  /**
   * Obtener todos los insumos compuestos
   */
  async listar(): Promise<InsumoCompuesto[]> {
    const response = await api.get(BASE_URL);
    return response.data;
  },

  /**
   * Obtener un insumo compuesto por ID
   */
  async obtenerPorId(id: number): Promise<InsumoCompuesto> {
    const response = await api.get(`${BASE_URL}/${id}`);
    return response.data;
  },

  /**
   * Crear o actualizar un insumo compuesto
   */
  async guardar(insumoCompuesto: Partial<InsumoCompuesto>): Promise<InsumoCompuesto> {
    // El backend usa POST para ambos casos (crear y actualizar)
    const response = await api.post(BASE_URL, insumoCompuesto);
    return response.data;
  },

  /**
   * Eliminar (desactivar) un insumo compuesto
   */
  async eliminar(id: number): Promise<void> {
    await api.delete(`${BASE_URL}/${id}`);
  },

  /**
   * Recalcular costo unitario de un insumo compuesto
   */
  async recalcularCosto(id: number): Promise<InsumoCompuesto> {
    const response = await api.post(`${BASE_URL}/${id}/recalcular-costo`);
    return response.data;
  },

  /**
   * Asociar receta a una etapa de alimentación (solo para Porcinos)
   */
  async asociarRecetaAEtapa(
    insumoCompuestoId: number,
    etapa: EtapaAlimentacion,
    cantidadDiariaPorAnimal: number
  ): Promise<RecetaAlimentacionPorEtapa> {
    const response = await api.post(`${BASE_URL}/${insumoCompuestoId}/asociar-etapa`, {
      etapa,
      cantidadDiariaPorAnimal,
    });
    return response.data;
  },

  /**
   * Obtener recetas por etapa (solo para Porcinos)
   */
  async obtenerRecetasPorEtapa(etapa: EtapaAlimentacion): Promise<RecetaAlimentacionPorEtapa[]> {
    const response = await api.get(`${BASE_URL}/etapas/${etapa}`);
    return response.data;
  },

  /**
   * Obtener receta por defecto para una etapa (solo para Porcinos)
   */
  async obtenerRecetaPorDefecto(etapa: EtapaAlimentacion): Promise<RecetaAlimentacionPorEtapa | null> {
    try {
      const response = await api.get(`${BASE_URL}/etapas/${etapa}/por-defecto`);
      return response.data;
    } catch (error: any) {
      if (error.response?.status === 404) {
        return null;
      }
      throw error;
    }
  },

  /**
   * Calcular preparación de receta sin descontar del inventario
   * Muestra cuánto se necesita de cada componente por kg de receta,
   * el stock disponible, y el máximo preparable
   */
  async calcularPreparacion(
    insumoCompuestoId: number,
    cantidad?: number
  ): Promise<any> {
    const params = cantidad ? `?cantidad=${cantidad}` : '';
    const response = await api.get(`${BASE_URL}/${insumoCompuestoId}/calcular-preparacion${params}`);
    return response.data;
  },

  /**
   * Preparar una receta (descontar ingredientes del inventario)
   */
  async prepararReceta(
    insumoCompuestoId: number,
    cantidadPreparada: number,
    fechaPreparacion?: string
  ): Promise<any> {
    const response = await api.post(`${BASE_URL}/${insumoCompuestoId}/preparar`, {
      cantidadPreparada,
      fechaPreparacion,
    });
    return response.data;
  },
};





