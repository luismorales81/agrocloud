import api from '../../../services/api';
import { API_ENDPOINTS } from '../../../services/apiEndpoints';

export interface PlanRecriaResumen {
  id: number;
  nombre: string;
  descripcion?: string;
  razaObjetivo?: string;
  proposito?: string;
  fechaCreacion?: string;
}

export interface PlanRecriaDetalleEdicion {
  id: number;
  propuesta: Record<string, unknown>;
}

export async function listarPlanesRecria(): Promise<PlanRecriaResumen[]> {
  const { data } = await api.get<PlanRecriaResumen[]>(API_ENDPOINTS.PORCINOS_PLANES_RECRIA.LISTAR);
  return Array.isArray(data) ? data : [];
}

export async function obtenerPlanRecriaParaEdicion(planId: number): Promise<PlanRecriaDetalleEdicion> {
  const { data } = await api.get<PlanRecriaDetalleEdicion>(API_ENDPOINTS.PORCINOS_PLANES_RECRIA.OBTENER(planId));
  return data;
}

export async function actualizarPlanRecria(planId: number, propuesta: Record<string, unknown>) {
  const { data } = await api.put<{ planRecriaId: number; mensaje: string }>(
    API_ENDPOINTS.PORCINOS_PLANES_RECRIA.ACTUALIZAR(planId),
    propuesta
  );
  return data;
}

export async function desactivarPlanRecria(planId: number) {
  const { data } = await api.delete<{ mensaje: string }>(API_ENDPOINTS.PORCINOS_PLANES_RECRIA.DESACTIVAR(planId));
  return data;
}
