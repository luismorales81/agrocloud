/**
 * Servicio para gestión de Catálogos Configurables del módulo Porcinos
 */
import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';
import type {
  RazaPorcino,
  TipoServicioPorcino,
  CausaMortalidadPorcino,
  MotivoBajaPorcino,
  EsquemaSanitarioPorcino,
  TipoParto,
  UbicacionInterna,
  TipoAlimentoPorcino,
} from '../types';

export const catalogosService = {
  // ============================================================================
  // RAZAS
  // ============================================================================
  
  async listarRazas(): Promise<RazaPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.RAZAS);
    return response.data;
  },

  async listarRazasPorTipo(tipo: string): Promise<RazaPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.RAZAS_POR_TIPO(tipo));
    return response.data;
  },

  async guardarRaza(raza: Partial<RazaPorcino>): Promise<RazaPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.RAZAS, raza);
    return response.data;
  },

  async eliminarRaza(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.RAZAS}/${id}`);
  },

  // ============================================================================
  // TIPOS DE ALIMENTO (stub: sin endpoints; la pantalla de configuración queda vacía)
  // ============================================================================

  async listarTiposAlimento(): Promise<TipoAlimentoPorcino[]> {
    return [];
  },

  async guardarTipoAlimento(_tipo: Partial<TipoAlimentoPorcino>): Promise<void> {
    return;
  },

  async eliminarTipoAlimento(_id: number): Promise<void> {
    return;
  },

  // ============================================================================
  // TIPOS DE SERVICIO
  // ============================================================================
  
  async listarTiposServicio(): Promise<TipoServicioPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_SERVICIO);
    return response.data;
  },

  async guardarTipoServicio(tipoServicio: Partial<TipoServicioPorcino>): Promise<TipoServicioPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_SERVICIO, tipoServicio);
    return response.data;
  },

  async eliminarTipoServicio(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_SERVICIO}/${id}`);
  },

  // ============================================================================
  // CAUSAS DE MORTALIDAD
  // ============================================================================
  
  async listarCausasMortalidad(): Promise<CausaMortalidadPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.CAUSAS_MORTALIDAD);
    return response.data;
  },

  async guardarCausaMortalidad(causa: Partial<CausaMortalidadPorcino>): Promise<CausaMortalidadPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.CAUSAS_MORTALIDAD, causa);
    return response.data;
  },

  async eliminarCausaMortalidad(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.CAUSAS_MORTALIDAD}/${id}`);
  },

  // ============================================================================
  // MOTIVOS DE BAJA
  // ============================================================================
  
  async listarMotivosBaja(): Promise<MotivoBajaPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.MOTIVOS_BAJA);
    return response.data;
  },

  async guardarMotivoBaja(motivo: Partial<MotivoBajaPorcino>): Promise<MotivoBajaPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.MOTIVOS_BAJA, motivo);
    return response.data;
  },

  async eliminarMotivoBaja(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.MOTIVOS_BAJA}/${id}`);
  },

  // ============================================================================
  // ESQUEMAS SANITARIOS
  // ============================================================================
  
  async listarEsquemasSanitarios(): Promise<EsquemaSanitarioPorcino[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.ESQUEMAS_SANITARIOS);
    return response.data;
  },

  async guardarEsquemaSanitario(esquema: Partial<EsquemaSanitarioPorcino>): Promise<EsquemaSanitarioPorcino> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.ESQUEMAS_SANITARIOS, esquema);
    return response.data;
  },

  async eliminarEsquemaSanitario(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.ESQUEMAS_SANITARIOS}/${id}`);
  },

  // ============================================================================
  // TIPOS DE PARTO
  // ============================================================================
  
  async listarTiposParto(): Promise<TipoParto[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_PARTO);
    return response.data;
  },

  async guardarTipoParto(tipoParto: Partial<TipoParto>): Promise<TipoParto> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_PARTO, tipoParto);
    return response.data;
  },

  async eliminarTipoParto(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.TIPOS_PARTO}/${id}`);
  },

  // ============================================================================
  // UBICACIONES INTERNAS
  // ============================================================================
  
  async listarUbicacionesInternas(): Promise<UbicacionInterna[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.UBICACIONES_INTERNAS);
    return response.data;
  },

  async listarUbicacionesPorNivel(nivel: string): Promise<UbicacionInterna[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.UBICACIONES_POR_NIVEL(nivel));
    return response.data;
  },

  async listarUbicacionesHijas(padreId: number): Promise<UbicacionInterna[]> {
    const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.UBICACIONES_HIJAS(padreId));
    return response.data;
  },

  async guardarUbicacionInterna(ubicacion: Partial<UbicacionInterna>): Promise<UbicacionInterna> {
    const response = await api.post(API_ENDPOINTS.PORCINOS_CATALOGOS.UBICACIONES_INTERNAS, ubicacion);
    return response.data;
  },

  async eliminarUbicacionInterna(id: number): Promise<void> {
    await api.delete(`${API_ENDPOINTS.PORCINOS_CATALOGOS.UBICACIONES_INTERNAS}/${id}`);
  },
};

