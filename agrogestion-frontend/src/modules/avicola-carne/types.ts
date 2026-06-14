/**
 * Tipos del módulo avícola carne (alineados a la API /avicola-carne).
 * Fechas en ISO string; importes y pesos como number (BigDecimal en backend).
 */

export type AvicolaCarneEstadoLote = 'ACTIVO' | 'CERRADO';

export type AvicolaCarneEspecie =
  | 'POLLO_PARRILLERO'
  | 'PAVO'
  | 'PATO'
  | 'PERDIZ'
  | 'GALLINA_PONEDORA'
  | 'OTRO';

export type AvicolaCarneTipoVenta = 'FAENA' | 'VENTA_EN_PIE' | 'DESCARTE';

export interface AvicolaCarneLote {
  id: number;
  empresaId: number;
  establecimientoId?: number;
  establecimientoNombre?: string;
  razaId?: number;
  razaNombre?: string;
  nombre: string;
  especie?: AvicolaCarneEspecie | string;
  origen?: string;
  fechaIngreso: string;
  cantidadInicial?: number;
  cantidadAnimales?: number;
  pesoPromedioIngreso?: number | null;
  estado?: AvicolaCarneEstadoLote | string;
  fechaSalida?: string | null;
  observaciones?: string | null;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface AvicolaCarneLoteCreacionCuerpo {
  establecimientoId: number;
  razaId: number;
  nombre: string;
  especie: AvicolaCarneEspecie | string;
  origen?: string;
  fechaIngreso: string;
  cantidadInicial: number;
  cantidadAnimales?: number;
  pesoPromedioIngreso?: number | null;
  observaciones?: string | null;
}

export interface AvicolaCarneLoteEdicionCuerpo {
  establecimientoId?: number;
  razaId?: number;
  nombre?: string;
  especie?: AvicolaCarneEspecie | string;
  origen?: string;
  observaciones?: string | null;
}

/** Pesada de un lote */
export interface Pesada {
  id: number;
  loteId?: number;
  empresaId: number;
  fecha: string;
  pesoPromedio: number;
  cantidadPesada?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PesadaCreacionCuerpo {
  fecha: string;
  pesoPromedio: number;
  cantidadPesada?: number | null;
  observaciones?: string | null;
}

/** Registro de mortalidad */
export interface Muerte {
  id: number;
  loteId?: number;
  empresaId: number;
  fecha: string;
  cantidad: number;
  causa?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface MuerteCreacionCuerpo {
  fecha: string;
  cantidad: number;
  causa?: string | null;
  observaciones?: string | null;
}

/** Venta o faena */
export interface Venta {
  id: number;
  loteId?: number;
  empresaId: number;
  fecha: string;
  tipo: AvicolaCarneTipoVenta | string;
  cantidad: number;
  pesoPromedio?: number | null;
  precioUnitario?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
  ingresoId?: number | null;
  createdAt?: string;
}

export interface VentaCreacionCuerpo {
  fecha: string;
  tipo: AvicolaCarneTipoVenta | string;
  cantidad: number;
  pesoPromedio?: number | null;
  precioUnitario?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
}

/** Consumo de insumo por lote */
export interface Consumo {
  id: number;
  loteId?: number;
  empresaId: number;
  insumoId: number;
  fecha: string;
  cantidad: number;
  tipo?: string;
  observaciones?: string | null;
  createdAt?: string;
}

export interface ConsumoCreacionCuerpo {
  insumoId: number;
  fecha: string;
  cantidad: number;
  tipo?: string;
  observaciones?: string | null;
}

/** Evento sanitario del lote */
export interface EventoSanitario {
  id: number;
  loteId?: number;
  empresaId: number;
  fecha: string;
  tipo: string;
  descripcion?: string | null;
  insumoId?: number | null;
  dosis?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface EventoSanitarioCreacionCuerpo {
  fecha: string;
  tipo: string;
  descripcion?: string | null;
  insumoId?: number | null;
  dosis?: number | null;
  observaciones?: string | null;
}

/** Resumen KPI (GET /lotes/{id}/resumen) */
export interface Resumen {
  loteId: number;
  cantidadDisponible: number;
  mortalidadPct: number;
  /** Puede ser null si no aplica (p. ej. sin peso o sin aves disponibles). */
  conversionAlimenticia: number | null;
  diasEnProduccion: number;
}
