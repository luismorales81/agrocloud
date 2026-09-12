/**
 * Tipos del módulo Porcinos v2 (alineados a la API /porcinos).
 */

export type PorcinosLoteEstado = 'ACTIVO' | 'CERRADO';

export type PorcinosLoteEtapa = 'RECRIA' | 'ENGORDE';

export type PorcinosLoteOrigen = 'DESTETE' | 'EXTERNO';

export type PorcinosEstadoGalpon = 'DISPONIBLE' | 'OCUPADO' | 'INACTIVO';

export type PorcinosTipoVenta = 'ENGORDE' | 'REPRODUCTOR' | 'FAENA';

export type PorcinosTipoEventoSanitario = 'VACUNA' | 'TRATAMIENTO' | 'DIAGNOSTICO' | 'OTRO';

export type PorcinosMadreEstado =
  | 'CACHORRA'
  | 'ADULTA'
  | 'GESTACION'
  | 'LACTANCIA'
  | 'RECRIA'
  | 'DESCARTE';

export interface PorcinosCatalogo {
  id: number;
  empresaId?: number;
  nombre: string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosCatalogoSolicitud {
  nombre: string;
  activo?: boolean;
}

export interface PorcinosEstablecimiento {
  id: number;
  empresaId?: number;
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  diasGestacion?: number;
  diasLactancia?: number;
  diasEntreCelos?: number;
  faenaHabilitada?: boolean;
  capacidadCabezas?: number | null;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosEstablecimientoSolicitud {
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  diasGestacion?: number;
  diasLactancia?: number;
  diasEntreCelos?: number;
  faenaHabilitada?: boolean;
  capacidadCabezas?: number | null;
  activo?: boolean;
}

export interface PorcinosGalpon {
  id: number;
  establecimientoId?: number;
  nombre: string;
  capacidadCabezas?: number | null;
  estado?: PorcinosEstadoGalpon | string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosGalponSolicitud {
  nombre: string;
  capacidadCabezas?: number | null;
  estado?: PorcinosEstadoGalpon | string;
  activo?: boolean;
}

export interface PorcinosLote {
  id: number;
  empresaId?: number;
  galponId?: number;
  galponNombre?: string;
  establecimientoId?: number;
  establecimientoNombre?: string;
  campanaId?: number;
  nombre: string;
  origen?: PorcinosLoteOrigen | string;
  fechaIngreso: string;
  fechaCierre?: string | null;
  cabezasInicial?: number;
  cabezasActuales?: number;
  pesoPromedioIngresoKg?: number | null;
  etapa?: PorcinosLoteEtapa | string;
  estado?: PorcinosLoteEstado | string;
  observaciones?: string | null;
  dietaId?: number | null;
  dietaNombre?: string | null;
  createdAt?: string;
  updatedAt?: string | null;
  climaLatitud?: number | null;
  climaLongitud?: number | null;
}

export interface PorcinosLoteSolicitud {
  galponId: number;
  nombre: string;
  origen: PorcinosLoteOrigen | string;
  fechaIngreso: string;
  cabezasInicial: number;
  pesoPromedioIngresoKg?: number | null;
  etapa?: PorcinosLoteEtapa | string;
  observaciones?: string | null;
  dietaId?: number | null;
}

export interface PorcinosMadre {
  id: number;
  empresaId?: number;
  caravana: string;
  razaId?: number | null;
  razaNombre?: string | null;
  galponId?: number | null;
  galponNombre?: string | null;
  estado?: PorcinosMadreEstado | string;
  fechaIngreso?: string;
  fechaNacimiento?: string | null;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosMadreSolicitud {
  caravana: string;
  razaId?: number | null;
  galponId?: number | null;
  fechaIngreso: string;
  fechaNacimiento?: string | null;
  activo?: boolean;
}

export interface PorcinosPadrillo {
  id: number;
  empresaId?: number;
  nombre: string;
  razaId?: number | null;
  razaNombre?: string | null;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosPadrilloSolicitud {
  nombre: string;
  razaId?: number | null;
  activo?: boolean;
}

export interface PorcinosReproduccionResumen {
  madresActivas?: number;
  gestacionesEnCurso?: number;
  madresEnLactancia?: number;
  partosPeriodo?: number;
  destetesPeriodo?: number;
}

export interface PorcinosGestacion {
  id: number;
  madreId?: number;
  madreCaravana?: string;
  servicioId?: number;
  fechaInicio?: string;
  fechaProbableParto?: string;
  estado?: string;
  createdAt?: string;
}

export interface PorcinosServicio {
  id: number;
  madreId?: number;
  madreCaravana?: string;
  padrilloId?: number | null;
  padrilloNombre?: string | null;
  tipoServicioId?: number | null;
  tipoServicioNombre?: string | null;
  fecha: string;
  observaciones?: string | null;
  gestacionId?: number | null;
  fechaProbableParto?: string | null;
  createdAt?: string;
}

export interface PorcinosServicioSolicitud {
  padrilloId?: number | null;
  tipoServicioId?: number | null;
  fecha: string;
  observaciones?: string | null;
}

export interface PorcinosParto {
  id: number;
  gestacionId?: number;
  madreId?: number;
  madreCaravana?: string;
  fecha: string;
  nacidosVivos?: number;
  nacidosMuertos?: number;
  momificados?: number;
  temperaturaAmbiente?: number | null;
  humedadAmbiente?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosPartoSolicitud {
  fecha: string;
  nacidosVivos?: number;
  nacidosMuertos?: number;
  momificados?: number;
  temperaturaAmbiente?: number | null;
  humedadAmbiente?: number | null;
  observaciones?: string | null;
}

export interface PorcinosDestete {
  id: number;
  partoId?: number;
  fecha: string;
  cantidadDestetados: number;
  pesoPromedioKg: number;
  loteId?: number | null;
  loteNombre?: string | null;
  createdAt?: string;
}

export interface PorcinosDesteteSolicitud {
  fecha: string;
  cantidadDestetados: number;
  pesoPromedioKg: number;
  galponId: number;
  loteNombre?: string | null;
}

export interface PorcinosPesada {
  id: number;
  loteId?: number;
  fecha: string;
  pesoPromedioKg: number;
  cabezasMuestreadas?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosPesadaSolicitud {
  fecha: string;
  pesoPromedioKg: number;
  cabezasMuestreadas?: number | null;
  observaciones?: string | null;
}

export interface PorcinosConsumo {
  id: number;
  loteId?: number;
  insumoId: number;
  insumoNombre?: string | null;
  fecha: string;
  cantidadKg: number;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosConsumoSolicitud {
  insumoId: number;
  fecha: string;
  cantidadKg: number;
  observaciones?: string | null;
}

export interface PorcinosMuerte {
  id: number;
  loteId?: number;
  fecha: string;
  cabezas: number;
  causaMortalidadId?: number | null;
  causaMortalidadNombre?: string | null;
  causaNombre?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosMuerteSolicitud {
  fecha: string;
  cabezas: number;
  causaMortalidadId?: number | null;
  observaciones?: string | null;
}

export interface PorcinosEventoSanitario {
  id: number;
  loteId?: number;
  loteNombre?: string | null;
  fecha: string;
  tipo: PorcinosTipoEventoSanitario | string;
  descripcion?: string | null;
  insumoId?: number | null;
  diasRetiro?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosEventoSanitarioSolicitud {
  fecha: string;
  tipo: PorcinosTipoEventoSanitario | string;
  descripcion?: string | null;
  insumoId?: number | null;
  cantidadInsumo?: number | null;
  diasRetiro?: number | null;
  observaciones?: string | null;
}

export interface PorcinosVenta {
  id: number;
  loteId?: number | null;
  loteNombre?: string | null;
  fecha: string;
  tipo: PorcinosTipoVenta | string;
  cabezas: number;
  pesoPromedioKg?: number | null;
  precioKg?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface PorcinosVentaSolicitud {
  fecha: string;
  tipo: PorcinosTipoVenta | string;
  cabezas: number;
  pesoPromedioKg?: number | null;
  precioKg?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
}

export interface PorcinosDietaFase {
  id: number;
  dietaId?: number;
  nombreFase: string;
  diasDesdeIngreso: number;
  kgCabezaDia: number;
  insumoId?: number | null;
}

export interface PorcinosDieta {
  id: number;
  empresaId?: number;
  nombre: string;
  activo?: boolean;
  fases?: PorcinosDietaFase[];
  createdAt?: string;
  updatedAt?: string | null;
}

export interface PorcinosDietaSolicitud {
  nombre: string;
  activo?: boolean;
}

export interface PorcinosDietaFaseSolicitud {
  nombreFase: string;
  diasDesdeIngreso?: number;
  kgCabezaDia: number;
  insumoId?: number | null;
}

export interface PorcinosPanelResumen {
  campanaId?: number | null;
  cabezasEnLotes?: number | null;
  lotesActivos?: number | null;
  madresActivas?: number | null;
  gestacionesEnCurso?: number | null;
  madresEnLactancia?: number | null;
  mortalidadPctPromedio?: number | null;
  consumoTotalKg?: number | null;
}

export interface PorcinosReporteResumen {
  tipo?: string;
  titulo?: string;
  filas?: Record<string, string | number | null>[];
  metricas?: Record<string, number | string | null>;
}

export interface PorcinosResumenEconomico {
  loteId?: number;
  loteNombre?: string | null;
  costoAlimento?: number | null;
  costoSanidad?: number | null;
  costoAcumulado?: number | null;
  ingresosVentas?: number | null;
  margen?: number | null;
  totalAlimentoKg?: number | null;
}

export interface FiltrosListadoLotesPorcinos {
  estado?: PorcinosLoteEstado | string;
  etapa?: PorcinosLoteEtapa | string;
  delPeriodoActivo?: boolean;
  galponId?: number;
}
