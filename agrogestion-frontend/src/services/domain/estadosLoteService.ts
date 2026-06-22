import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

export interface PasoCaminoEstado {
  id: number;
  nombre: string;
  color?: string;
  icono?: string;
  actual: boolean;
  completado: boolean;
  orden?: number;
}

export interface TareaProgresoEstado {
  tipoLabor: string;
  nombreTarea: string;
  esObligatoria: boolean;
  completada: boolean;
}

export interface ProgresoEstadoLote {
  loteId: number;
  loteNombre: string;
  usaConfiguracion: boolean;
  mensajeAvance: string;
  diasDesdeSiembra?: number;
  diasParaProximoEstado?: number;
  estadoActual?: {
    id?: number;
    nombre: string;
    color?: string;
    icono?: string;
    modoAvance?: string;
    diasMinimos?: number;
  };
  proximoEstado?: {
    id?: number;
    nombre: string;
    color?: string;
    icono?: string;
    diasMinimos?: number;
  };
  caminoEstados: PasoCaminoEstado[];
  tareas: TareaProgresoEstado[];
  transicionesDisponibles: Array<{
    destinoId: number;
    destinoNombre: string;
    destinoColor?: string;
    requiereMotivo: boolean;
  }>;
}

export const estadosLoteService = {
  async obtenerProgreso(loteId: number, empresaId?: number): Promise<ProgresoEstadoLote> {
    const params = empresaId ? { empresaId } : {};
    const response = await api.get(API_ENDPOINTS.ESTADOS_LOTES.PROGRESO(loteId), { params });
    return response.data;
  },

  async recalcularTodos(empresaId?: number) {
    const params = empresaId ? { empresaId } : {};
    const response = await api.post(API_ENDPOINTS.ESTADOS_LOTES.RECALCULAR_TODOS, null, { params });
    return response.data;
  },
};
