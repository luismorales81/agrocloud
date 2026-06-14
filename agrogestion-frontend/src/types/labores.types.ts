/**
 * Tipos usados por LaboresManagement y componentes de labores.
 * Extraídos para reducir tamaño del componente y mejorar rendimiento del IDE.
 */

export interface Insumo {
  id: number;
  nombre: string;
  tipo: string;
  stock_actual: number;
  unidad_medida: string;
  precio_unitario: number;
}

export interface Maquinaria {
  id: number;
  nombre: string;
  tipo: string;
  estado: string;
  kilometros_uso: number;
  costo_por_hora: number;
}

export interface MaquinariaAsignada {
  maquinaria_id: number;
  maquinaria_nombre: string;
  costo_total: number;
  proveedor?: string;
}

export interface InsumoUsado {
  insumo_id: number;
  insumo_nombre: string;
  cantidad_usada: number;
  cantidad_planificada?: number;
  unidad_medida: string;
  costo_unitario: number;
  costo_total: number;
  /** Compatibilidad con backend en camelCase y variantes */
  idInsumo?: number;
  id?: number;
  insumoNombre?: string;
  nombre?: string;
  cantidadUsada?: number;
  cantidad?: number;
  costoUnitario?: number;
  precio_unitario?: number;
  unidadMedida?: string;
  unidad?: string;
  costoTotal?: number;
  observaciones?: string;
}

export interface LaborMaquinaria {
  id_labor_maquinaria: number;
  id_labor: number;
  descripcion: string;
  tipo?: string;
  proveedor?: string;
  costo: number;
  observaciones?: string;
}

export interface LaborManoObra {
  id_labor_mano_obra: number;
  id_labor: number;
  descripcion: string;
  cantidad_personas: number;
  /** Compatibilidad con backend en camelCase */
  cantidadPersonas?: number;
  idLaborManoObra?: number;
  idLabor?: number;
  proveedor?: string;
  costo_total: number;
  costoTotal?: number;
  horas_trabajo?: number;
  horasTrabajo?: number;
  observaciones?: string;
}

export type EstadoLabor = 'planificada' | 'en_progreso' | 'completada' | 'interrumpida' | 'cancelada' | 'anulada';

export interface Labor {
  id?: number;
  tipo: string;
  fecha: string;
  fecha_fin?: string;
  observaciones: string;
  lote_id: number;
  lote_nombre: string;
  cultivo_id?: number;
  estado: EstadoLabor;
  overdue?: boolean;
  fecha_realizacion?: string;
  insumos_usados: InsumoUsado[];
  maquinaria_asignada: MaquinariaAsignada[];
  responsable: string;
  horas_trabajo?: number;
  costo_total?: number;
  costo_maquinaria?: number;
  costo_mano_obra?: number;
  costo_insumos?: number;
  maquinarias?: LaborMaquinaria[];
  mano_obra?: LaborManoObra[];
}

export interface Lote {
  id: number;
  nombre: string;
  superficie: number;
  cultivo: string;
  estado?: string;
  fechaSiembra?: string;
  fechaCosechaEsperada?: string;
}
