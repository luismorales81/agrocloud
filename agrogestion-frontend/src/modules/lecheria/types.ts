/**
 * Tipos del módulo lechería (alineados a la API /lecheria).
 */

export type LecheriaEspecie = 'BOVINO' | 'BUFALO' | 'CAPRINO' | 'OVINO' | 'CAMELIDO';

export type LecheriaEstadoAnimal = 'LACTANDO' | 'SECA' | 'PRENADA' | 'VAQUILLONA';

export type LecheriaSexoAnimal = 'MACHO' | 'HEMBRA';

export type LecheriaTurnoOrdene = 'AM' | 'PM' | 'TOTAL';

export type LecheriaTipoEventoReproductivo =
  | 'SERVICIO'
  | 'TACTO'
  | 'PARTO'
  | 'SECADO'
  | 'ABORTO';

export type LecheriaTipoEventoSanitario =
  | 'VACUNA'
  | 'DESPARASITACION'
  | 'TRATAMIENTO'
  | 'MASTITIS'
  | 'CONTROL'
  | 'OTRO';

export interface LecheriaEstablecimiento {
  id: number;
  empresaId?: number;
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  capacidadAnimales?: number | null;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface LecheriaEstablecimientoSolicitud {
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  capacidadAnimales?: number | null;
  activo?: boolean;
}

export interface LecheriaRodeo {
  id: number;
  establecimientoId?: number;
  empresaId?: number;
  nombre: string;
  especie?: LecheriaEspecie | string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface LecheriaRodeoSolicitud {
  nombre: string;
  especie: LecheriaEspecie | string;
  activo?: boolean;
}

export interface LecheriaRaza {
  id: number;
  empresaId?: number;
  nombre: string;
  especie?: LecheriaEspecie | string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface LecheriaRazaSolicitud {
  nombre: string;
  especie: LecheriaEspecie | string;
  activo?: boolean;
}

export interface LecheriaMotivoBaja {
  id: number;
  empresaId?: number;
  nombre: string;
  activo?: boolean;
  createdAt?: string;
}

export interface LecheriaMotivoBajaSolicitud {
  nombre: string;
  activo?: boolean;
}

export interface LecheriaLactancia {
  id: number;
  animalId?: number;
  numeroLactancia?: number;
  fechaParto?: string;
  fechaSecado?: string | null;
  activa?: boolean;
  dim?: number | null;
  observaciones?: string | null;
}

export interface LecheriaAnimal {
  id: number;
  empresaId?: number;
  identificacion: string;
  especie?: LecheriaEspecie | string;
  sexo?: LecheriaSexoAnimal | string;
  estado?: LecheriaEstadoAnimal | string;
  razaId?: number;
  razaNombre?: string | null;
  rodeoId?: number;
  rodeoNombre?: string | null;
  campanaId?: number;
  fechaNacimiento?: string | null;
  fechaIngreso?: string | null;
  fechaBaja?: string | null;
  activo?: boolean;
  observaciones?: string | null;
  lactanciaActiva?: LecheriaLactancia | null;
  climaLatitud?: number | null;
  climaLongitud?: number | null;
  dim?: number | null;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface LecheriaAnimalSolicitud {
  identificacion: string;
  especie: LecheriaEspecie | string;
  sexo: LecheriaSexoAnimal | string;
  estado: LecheriaEstadoAnimal | string;
  razaId: number;
  rodeoId: number;
  fechaNacimiento?: string | null;
  fechaIngreso?: string | null;
  observaciones?: string | null;
}

export interface LecheriaRegistroOrdene {
  id: number;
  animalId?: number;
  lactanciaId?: number;
  fecha: string;
  turno?: LecheriaTurnoOrdene | string;
  litros: number;
  grasaPct?: number | null;
  proteinaPct?: number | null;
  rcs?: number | null;
  temperaturaAmbiente?: number | null;
  humedadAmbiente?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaRegistroOrdeneSolicitud {
  fecha: string;
  turno: LecheriaTurnoOrdene | string;
  litros: number;
  grasaPct?: number | null;
  proteinaPct?: number | null;
  rcs?: number | null;
  temperaturaAmbiente?: number | null;
  humedadAmbiente?: number | null;
  observaciones?: string | null;
}

export interface LecheriaEventoReproductivo {
  id: number;
  animalId?: number;
  tipo?: LecheriaTipoEventoReproductivo | string;
  fecha: string;
  resultado?: string | null;
  toroPajuela?: string | null;
  fechaPrevistaParto?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaEventoReproductivoSolicitud {
  tipo: LecheriaTipoEventoReproductivo | string;
  fecha: string;
  resultado?: string | null;
  toroPajuela?: string | null;
  fechaPrevistaParto?: string | null;
  observaciones?: string | null;
}

export interface LecheriaEventoSanitario {
  id: number;
  animalId?: number;
  tipo?: LecheriaTipoEventoSanitario | string;
  fecha: string;
  descripcion?: string | null;
  insumoId?: number | null;
  cantidad?: number | null;
  diasRetiro?: number | null;
  createdAt?: string;
}

export interface LecheriaEventoSanitarioSolicitud {
  tipo: LecheriaTipoEventoSanitario | string;
  fecha: string;
  descripcion?: string | null;
  insumoId?: number | null;
  cantidad?: number | null;
  diasRetiro?: number | null;
}

export interface LecheriaScoreCorporal {
  id: number;
  animalId?: number;
  fecha: string;
  valor: number;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaScoreCorporalSolicitud {
  fecha: string;
  valor: number;
  observaciones?: string | null;
}

export interface LecheriaBajaAnimal {
  id: number;
  animalId?: number;
  fecha: string;
  motivoId?: number;
  motivoNombre?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaBajaAnimalSolicitud {
  fecha: string;
  motivoId: number;
  observaciones?: string | null;
}

export interface LecheriaConsumo {
  id: number;
  rodeoId?: number;
  campanaId?: number;
  insumoId: number;
  fecha: string;
  cantidadKg: number;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaConsumoSolicitud {
  insumoId: number;
  fecha: string;
  cantidadKg: number;
  observaciones?: string | null;
}

export interface LecheriaVentaLeche {
  id: number;
  empresaId?: number;
  campanaId?: number;
  fecha: string;
  litros: number;
  precioLitro?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface LecheriaVentaLecheSolicitud {
  fecha: string;
  litros: number;
  precioLitro?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
}

export interface LecheriaAccionDia {
  tipo?: string;
  animalId?: number;
  identificacion?: string;
  mensaje?: string;
}

export interface LecheriaPanelResumen {
  campanaId?: number | null;
  litrosTotalesPeriodo?: number | null;
  litrosPorAnimalLactante?: number | null;
  promedioRcs?: number | null;
  animalesLactando?: number | null;
  animalesSecas?: number | null;
  animalesPrenadas?: number | null;
  acciones?: LecheriaAccionDia[];
}

export interface LecheriaPuntoDimLitros {
  dim?: number;
  litros?: number;
  fecha?: string;
}

export interface LecheriaCurvaLactancia {
  animalId?: number;
  identificacion?: string;
  puntos?: LecheriaPuntoDimLitros[];
}

export interface LecheriaRankingItem {
  animalId?: number;
  identificacion?: string;
  litrosTotales?: number;
}

export interface LecheriaRankingProduccion {
  items?: LecheriaRankingItem[];
}

export interface LecheriaPuntoClimaProduccion {
  fecha?: string;
  temperatura?: number | null;
  humedad?: number | null;
  litros?: number | null;
}

export interface LecheriaClimaProduccion {
  puntos?: LecheriaPuntoClimaProduccion[];
}

export interface LecheriaImportControlLechero {
  id: number;
  nombreArchivo?: string;
  filasProcesadas?: number;
  filasError?: number;
  detalleErrores?: string | null;
  createdAt?: string;
}

export interface LecheriaCloseoutRodeo {
  id?: number;
  rodeoId?: number;
  rodeoNombre?: string;
  campanaId?: number;
  fechaCierre?: string;
  litrosTotales?: number | null;
  costoAlimentacion?: number | null;
  ingresosLeche?: number | null;
  margen?: number | null;
  observaciones?: string | null;
}

export interface LecheriaMovimientoSenasa {
  id: number;
  animalId?: number;
  tipoMovimiento?: string;
  fecha?: string;
  datosJson?: string | null;
  exportado?: boolean;
  createdAt?: string;
}

export interface LecheriaMovimientoSenasaSolicitud {
  animalId: number;
  tipoMovimiento: string;
  fecha: string;
  datosJson?: string | null;
}
