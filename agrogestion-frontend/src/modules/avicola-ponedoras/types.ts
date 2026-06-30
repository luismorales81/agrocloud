/**
 * Tipos del módulo avícola ponedoras (API /avicola-ponedoras/galpones).
 * Entidad central: galpón (no lote). Fechas ISO string; decimales como number.
 */

/** Coincide con enum backend AvicolaPonedorasHuevoCategoria. */
export type HuevoCategoria = 'A' | 'B' | 'ROTO' | 'SUCIO';

export type GalponEstado = 'ACTIVO' | 'CERRADO';

export type ConsumoTipo = 'MANUAL' | 'AUTOMATICO';

export type DescarteMotivo = 'LIQUIDACION' | 'BAJA_PRODUCTIVA' | 'OTRO';

/** Galpón de ponedoras / recría (respuesta API). */
export interface AvicolaPonedorasGalpon {
  id: number;
  empresaId: number;
  establecimientoId?: number | null;
  establecimientoNombre?: string | null;
  nombre: string;
  raza?: string | null;
  fechaIngreso: string;
  cantidadInicial?: number | null;
  cantidadAves?: number | null;
  estado?: GalponEstado | string | null;
  fechaCierre?: string | null;
  observaciones?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  climaLatitud?: number | null;
  climaLongitud?: number | null;
}

export interface AmbienteDiario {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  temperaturaDia?: number | null;
  humedadDia?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AmbienteDiarioCuerpo {
  fecha: string;
  temperaturaDia?: number | null;
  humedadDia?: number | null;
}

/** Alta de galpón (POST). */
export interface GalponCreacionCuerpo {
  establecimientoId: number;
  nombre: string;
  raza?: string | null;
  fechaIngreso: string;
  cantidadInicial: number;
  cantidadAves?: number | null;
  estado?: GalponEstado | string | null;
  fechaCierre?: string | null;
  observaciones?: string | null;
}

/** Actualización de galpón (PUT; campos opcionales). */
export interface GalponEdicionCuerpo {
  establecimientoId?: number | null;
  nombre?: string | null;
  raza?: string | null;
  fechaIngreso?: string | null;
  cantidadInicial?: number | null;
  cantidadAves?: number | null;
  estado?: GalponEstado | string | null;
  fechaCierre?: string | null;
  observaciones?: string | null;
}

export interface Postura {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  categoriaHuevo: HuevoCategoria | string;
  cantidad: number;
  observaciones?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface PosturaCreacionCuerpo {
  fecha: string;
  categoriaHuevo: HuevoCategoria | string;
  cantidad: number;
  observaciones?: string | null;
}

export interface Muerte {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  cantidad: number;
  causa?: string | null;
  observaciones?: string | null;
  createdAt?: string | null;
}

export interface MuerteCreacionCuerpo {
  fecha: string;
  cantidad: number;
  causa?: string | null;
  observaciones?: string | null;
}

export interface Consumo {
  id: number;
  galponId: number;
  empresaId: number;
  insumoId: number;
  fecha: string;
  cantidad: number;
  tipo?: ConsumoTipo | string | null;
  observaciones?: string | null;
  createdAt?: string | null;
}

export interface ConsumoCreacionCuerpo {
  insumoId: number;
  fecha: string;
  cantidad: number;
  tipo?: ConsumoTipo | string | null;
  observaciones?: string | null;
}

export interface EventoSanitario {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  tipo?: string | null;
  descripcion?: string | null;
  insumoId?: number | null;
  dosis?: number | null;
  observaciones?: string | null;
  createdAt?: string | null;
}

export interface EventoSanitarioCreacionCuerpo {
  fecha: string;
  tipo?: string | null;
  descripcion?: string | null;
  insumoId?: number | null;
  dosis?: number | null;
  observaciones?: string | null;
}

export interface VentaHuevos {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  categoriaHuevo: HuevoCategoria | string;
  cantidad: number;
  precioUnitario?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
  createdAt?: string | null;
}

export interface VentaHuevosCreacionCuerpo {
  fecha: string;
  categoriaHuevo: HuevoCategoria | string;
  cantidad: number;
  precioUnitario?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
}

export interface DescarteAves {
  id: number;
  galponId: number;
  empresaId: number;
  fecha: string;
  cantidad: number;
  motivo: DescarteMotivo | string;
  observaciones?: string | null;
  createdAt?: string | null;
}

export interface DescarteAvesCreacionCuerpo {
  fecha: string;
  cantidad: number;
  motivo: DescarteMotivo | string;
  observaciones?: string | null;
}

export interface Resumen {
  galponId: number;
  cantidadDisponible?: number | null;
  mortalidadPct?: number | null;
  totalHuevos?: number | null;
  diasEnProduccion: number;
  porcentajePostura?: number | null;
  huevosPorAvePorDia?: number | null;
  distribucionCategorias?: Record<string, number>;
}
