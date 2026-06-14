/**
 * Tipos alineados con el contrato GET /api/labores.
 * El backend devuelve siempre List<LaborDetalladoDTO> (con o sin filtros).
 */
export interface LaborDetalladoDTO {
  id: number;
  tipo?: string;
  nombre?: string;
  descripcion?: string;
  fechaInicio?: string;
  fechaFin?: string;
  estado?: string;
  loteId: number;
  loteNombre: string;
  /** Superficie del lote en ha si el backend la envía */
  loteSuperficieHa?: number;
  responsable?: string;
  horasTrabajo?: number;
  costoTotal?: number;
  observaciones?: string;
  fechaRealizacion?: string;
  cultivoId?: number;
  overdue?: boolean;
  costoBase?: number;
  costoMaquinaria?: number;
  costoManoObra?: number;
  costoInsumos?: number;
  activo?: boolean;
  /** Por compatibilidad: si el backend devolviera entidad Labor con objeto lote */
  lote?: { id?: number; nombre?: string };
  /** Lista de maquinaria asignada (camelCase desde backend) */
  maquinariaAsignada?: Array<{ maquinaria_id?: number; maquinaria_nombre?: string; costo_total?: number }>;
  maquinarias?: unknown[];
  manoObra?: unknown[];
  insumosUsados?: unknown[];
}
