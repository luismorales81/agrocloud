/**
 * Tipos del módulo feedlot (alineados a la API /feedlot).
 */

export type FeedlotEstadoLote = 'ACTIVO' | 'CERRADO';

export type FeedlotEstadoCorral = 'DISPONIBLE' | 'OCUPADO' | 'INACTIVO';

export type FeedlotTipoTenencia = 'PROPIO' | 'CONSIGNACION';

export type FeedlotTipoVenta = 'FAENA' | 'VENTA_EN_PIE' | 'DESCARTE';

export type FeedlotTipoEventoSanitario = 'VACUNA' | 'TRATAMIENTO' | 'DIAGNOSTICO' | 'OTRO';

export type FeedlotTipoProveedor = 'PROPIETARIO' | 'CONSIGNATARIO' | 'CAMPO_PROPIO';

export interface FeedlotCatalogo {
  id: number;
  empresaId?: number;
  nombre: string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotCatalogoSolicitud {
  nombre: string;
  activo?: boolean;
}

export interface FeedlotProveedorOrigen extends FeedlotCatalogo {
  tipo?: FeedlotTipoProveedor | string;
}

export interface FeedlotProveedorOrigenSolicitud {
  nombre: string;
  tipo: FeedlotTipoProveedor | string;
  activo?: boolean;
}

export interface FeedlotEstablecimiento {
  id: number;
  empresaId?: number;
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  capacidadTotalCabezas?: number | null;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotEstablecimientoSolicitud {
  nombre: string;
  ubicacion?: string | null;
  coordenadas?: string | null;
  capacidadTotalCabezas?: number | null;
  activo?: boolean;
}

export interface FeedlotCorral {
  id: number;
  establecimientoId?: number;
  nombre: string;
  capacidadCabezas?: number | null;
  estado?: FeedlotEstadoCorral | string;
  activo?: boolean;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotCorralSolicitud {
  nombre: string;
  capacidadCabezas?: number | null;
  estado?: FeedlotEstadoCorral | string;
  activo?: boolean;
}

export interface FeedlotLote {
  id: number;
  empresaId?: number;
  corralId?: number;
  corralNombre?: string;
  corralEstado?: FeedlotEstadoCorral | string;
  establecimientoId?: number;
  establecimientoNombre?: string;
  campanaId?: number;
  nombre: string;
  categoriaId?: number;
  categoriaNombre?: string;
  razaId?: number;
  razaNombre?: string;
  proveedorId?: number;
  proveedorNombre?: string;
  tipoTenencia?: FeedlotTipoTenencia | string;
  fechaIngreso: string;
  fechaCierre?: string | null;
  cabezasInicial?: number;
  cabezasActuales?: number;
  pesoPromedioIngresoKg?: number | null;
  precioCompraKg?: number | null;
  costoHoteleriaDia?: number | null;
  estado?: FeedlotEstadoLote | string;
  observaciones?: string | null;
  dietaId?: number | null;
  dietaNombre?: string | null;
  createdAt?: string;
  updatedAt?: string | null;
  climaLatitud?: number | null;
  climaLongitud?: number | null;
}

export interface FeedlotLoteSolicitud {
  corralId: number;
  nombre: string;
  categoriaId: number;
  razaId: number;
  proveedorId?: number | null;
  tipoTenencia: FeedlotTipoTenencia | string;
  fechaIngreso: string;
  cabezasInicial: number;
  pesoPromedioIngresoKg?: number | null;
  precioCompraKg?: number | null;
  costoHoteleriaDia?: number | null;
  observaciones?: string | null;
  dietaId?: number | null;
}

export interface FeedlotPesada {
  id: number;
  loteId?: number;
  empresaId?: number;
  fecha: string;
  pesoPromedioKg: number;
  cabezasMuestreadas?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface FeedlotPesadaSolicitud {
  fecha: string;
  pesoPromedioKg: number;
  cabezasMuestreadas?: number | null;
  observaciones?: string | null;
}

export interface FeedlotConsumo {
  id: number;
  loteId?: number;
  empresaId?: number;
  campanaId?: number;
  insumoId: number;
  fecha: string;
  cantidadKg: number;
  materiaSecaPct?: number | null;
  observaciones?: string | null;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotConsumoSolicitud {
  insumoId: number;
  fecha: string;
  cantidadKg: number;
  materiaSecaPct?: number | null;
  observaciones?: string | null;
}

export interface FeedlotConsumoActualizarSolicitud {
  fecha?: string;
  cantidadKg?: number;
  materiaSecaPct?: number | null;
  observaciones?: string | null;
}

export interface FeedlotMuerte {
  id: number;
  loteId?: number;
  empresaId?: number;
  fecha: string;
  cabezas: number;
  motivoId?: number | null;
  motivoNombre?: string | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface FeedlotMuerteSolicitud {
  fecha: string;
  cabezas: number;
  motivoId?: number | null;
  observaciones?: string | null;
}

export interface FeedlotEventoSanitario {
  id: number;
  loteId?: number;
  empresaId?: number;
  fecha: string;
  tipo: FeedlotTipoEventoSanitario | string;
  descripcion?: string | null;
  insumoId?: number | null;
  diasRetiro?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface FeedlotEventoSanitarioSolicitud {
  fecha: string;
  tipo: FeedlotTipoEventoSanitario | string;
  descripcion?: string | null;
  insumoId?: number | null;
  cantidadInsumo?: number | null;
  diasRetiro?: number | null;
  observaciones?: string | null;
}

export interface FeedlotVenta {
  id: number;
  loteId?: number;
  empresaId?: number;
  campanaId?: number;
  fecha: string;
  tipo: FeedlotTipoVenta | string;
  cabezas: number;
  pesoPromedioKg?: number | null;
  precioKg?: number | null;
  total?: number | null;
  comprador?: string | null;
  ingresoId?: number | null;
  observaciones?: string | null;
  createdAt?: string;
}

export interface FeedlotVentaSolicitud {
  fecha: string;
  tipo: FeedlotTipoVenta | string;
  cabezas: number;
  pesoPromedioKg?: number | null;
  precioKg?: number | null;
  total?: number | null;
  comprador?: string | null;
  observaciones?: string | null;
}

export interface FeedlotResumen {
  loteId: number;
  cabezasActuales?: number;
  cabezasInicial?: number;
  diasEnFeedlot?: number;
  pesoActualKg?: number | null;
  gmd?: number | null;
  mortalidadPct?: number | null;
  totalAlimentoKg?: number | null;
  conversionAlimenticia?: number | null;
  consumoCabDia?: number | null;
}

export interface FeedlotCloseout {
  loteId: number;
  loteNombre?: string;
  fechaIngreso?: string;
  fechaReferencia?: string;
  diasEnFeedlot?: number;
  cabezasInicial?: number;
  cabezasActuales?: number;
  totalMuertes?: number;
  mortalidadPct?: number | null;
  pesoIngresoKg?: number | null;
  pesoActualKg?: number | null;
  gmd?: number | null;
  kgGanados?: number | null;
  totalAlimentoKg?: number | null;
  totalAlimentoMsKg?: number | null;
  conversionAlimenticia?: number | null;
  headDays?: number | null;
  costoCompra?: number | null;
  costoAlimento?: number | null;
  costoHoteleria?: number | null;
  costoAcumulado?: number | null;
  ingresosVentas?: number | null;
  margen?: number | null;
  breakevenKg?: number | null;
  metodoCloseout?: string;
}

export interface FeedlotPanelResumen {
  campanaId?: number | null;
  cabezasEnFeed?: number | null;
  lotesActivos?: number | null;
  gmdPromedio?: number | null;
  mortalidadPctPromedio?: number | null;
  consumoTotalKg?: number | null;
}

export interface FeedlotCierreLoteSolicitud {
  confirmarConCabezas?: boolean;
}

export interface FeedlotAjustePlantel {
  id: number;
  loteId?: number;
  empresaId?: number;
  usuarioId?: number;
  usuarioNombre?: string;
  fecha: string;
  cabezasAntes?: number;
  cabezasDespues?: number;
  motivo?: string;
  createdAt?: string;
}

export interface FeedlotAjustePlantelSolicitud {
  fecha: string;
  cabezasDespues: number;
  motivo: string;
}

export interface FiltrosListadoLotesFeedlot {
  estado?: FeedlotEstadoLote | string;
  delPeriodoActivo?: boolean;
  corralId?: number;
}

// —— v1.5 ——

export type FeedlotBunkScore = 'CERO' | 'MEDIO' | 'UNO' | 'DOS' | 'TRES' | 'CUATRO';

export type FeedlotMetodoCloseout = 'DEADS_IN' | 'DEADS_OUT';

export interface FeedlotDietaFase {
  id: number;
  dietaId?: number;
  nombreFase: string;
  diasDesdeIngreso: number;
  kgMsCabezaDia: number;
  insumoId?: number | null;
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotDietaFaseSolicitud {
  nombreFase: string;
  diasDesdeIngreso: number;
  kgMsCabezaDia: number;
  insumoId?: number | null;
}

export interface FeedlotDieta {
  id: number;
  empresaId?: number;
  nombre: string;
  activo?: boolean;
  fases?: FeedlotDietaFase[];
  createdAt?: string;
  updatedAt?: string | null;
}

export interface FeedlotDietaSolicitud {
  nombre: string;
  activo?: boolean;
}

export interface FeedlotLecturaComedero {
  id: number;
  loteId?: number;
  fecha: string;
  bunkScore: FeedlotBunkScore | string;
  kgEntregados?: number | null;
  observaciones?: string | null;
  temperaturaDia?: number | null;
  humedadDia?: number | null;
  createdAt?: string;
}

export interface FeedlotLecturaComederoSolicitud {
  fecha: string;
  bunkScore: FeedlotBunkScore | string;
  kgEntregados?: number | null;
  temperaturaDia?: number | null;
  humedadDia?: number | null;
  observaciones?: string | null;
}

export interface FeedlotConsumoTeoricoDia {
  fecha: string;
  faseNombre?: string | null;
  kgMsTeorico?: number | null;
  kgReal?: number | null;
  diferenciaKg?: number | null;
}

export interface FeedlotConsumoTeorico {
  loteId: number;
  dietaId?: number | null;
  dietaNombre?: string | null;
  fechaDesde?: string | null;
  fechaHasta?: string | null;
  dias?: FeedlotConsumoTeoricoDia[];
}

export interface FeedlotCurvaPesoPunto {
  fecha: string;
  pesoKg: number;
  proyectado?: boolean;
}

export interface FeedlotCurvaPeso {
  loteId: number;
  loteNombre?: string | null;
  pesoIngresoKg?: number | null;
  gmd?: number | null;
  serie?: FeedlotCurvaPesoPunto[];
  proyeccion?: FeedlotCurvaPesoPunto[];
  pesoObjetivoKg?: number | null;
  fechaProyeccionObjetivo?: string | null;
}

export interface FeedlotConfiguracionCloseout {
  metodoCloseout: FeedlotMetodoCloseout | string;
}

export interface FeedlotConfiguracionCloseoutSolicitud {
  metodoCloseout: FeedlotMetodoCloseout | string;
}

export interface FeedlotAnalisisLoteItem {
  loteId: number;
  loteNombre?: string | null;
  gmd?: number | null;
  conversionAlimenticia?: number | null;
  mortalidadPct?: number | null;
  margen?: number | null;
}

export interface FeedlotAnalisisLotes {
  lotes?: FeedlotAnalisisLoteItem[];
}

export type FeedlotEstadoAnimal = 'ACTIVO' | 'MUERTO' | 'VENDIDO' | 'FAENADO' | 'DESCARTADO';

export type FeedlotTipoSesionManga = 'ENROLAMIENTO' | 'PESADA' | 'SANIDAD' | 'RECUENTO' | 'BAJA';

export type FeedlotEstadoSesionManga = 'BORRADOR' | 'CONFIRMADA' | 'ANULADA';

export type FeedlotResultadoFilaManga = 'OK' | 'ERROR' | 'ADVERTENCIA';

export type FeedlotAplicador = 'PERSONAL_ESTABLECIMIENTO' | 'VETERINARIO';

export type FeedlotOrigenInsumo = 'STOCK_EMPRESA' | 'PROVEIDO_POR_APLICADOR' | 'SIN_INSUMO';

export type FeedlotSubtipoBaja = 'MUERTE' | 'FAENA' | 'VENTA_EN_PIE' | 'DESCARTE';

export interface FeedlotAnimal {
  id: number;
  loteId?: number;
  eid?: string | null;
  caravanaVisual?: string | null;
  sexo?: string | null;
  estado?: FeedlotEstadoAnimal | string;
  fechaAlta?: string;
  fechaBaja?: string | null;
  pesoIngresoKg?: number | null;
  retiroVigente?: boolean;
}

export interface FeedlotPesadaAnimal {
  id: number;
  fecha: string;
  pesoKg: number;
}

export interface FeedlotAnimalDetalle extends FeedlotAnimal {
  pesadas?: FeedlotPesadaAnimal[];
  eventosSanitarios?: FeedlotEventoSanitario[];
}

export interface FeedlotMapeoColumnas {
  indiceEid?: number | null;
  indiceCaravanaVisual?: number | null;
  indicePeso?: number | null;
  indiceFechaHora?: number | null;
  indiceHora?: number | null;
  delimitador?: string | null;
}

export interface FeedlotLecturaArchivoManga {
  eidCrudo?: string | null;
  eidNormalizado?: string | null;
  caravanaVisual?: string | null;
  pesoKg?: number | null;
  fechaHoraLectura?: string | null;
}

export interface FeedlotResumenArchivoManga {
  lecturas?: number;
  tienePeso?: boolean;
  pesosEncontrados?: number;
  eidRepetidos?: number;
  fechaMayoritaria?: string | null;
  horaDesde?: string | null;
  horaHasta?: string | null;
  entendioArchivo?: boolean;
  mapeo?: FeedlotMapeoColumnas;
  encabezados?: string[];
  filaMuestra?: string[];
  mensajeResumen?: string;
  loteSugeridoId?: number | null;
  loteSugeridoNombre?: string | null;
  mezclaLotes?: boolean;
  eidsOtroLote?: string[];
  lecturasDetalle?: FeedlotLecturaArchivoManga[];
  perfilArchivo?: string | null;
  columnasNoMapeadas?: string[];
  porcentajeEidInvalidos?: number;
  avisos?: string[];
  bloqueantes?: string[];
  perfilLecturaId?: number | null;
}

export interface FeedlotAliasColumnasPerfil {
  eid?: string[];
  vid?: string[];
  peso?: string[];
  fecha?: string[];
  hora?: string[];
  dosis?: string[];
  loteProducto?: string[];
  retiro?: string[];
  esi?: string[];
}

export interface FeedlotPerfilLecturaManga {
  id: number;
  nombre: string;
  tipo: FeedlotTipoSesionManga;
  predeterminado?: boolean;
  alias?: FeedlotAliasColumnasPerfil;
  insumoId?: number | null;
}

export interface FeedlotEncabezadosMuestra {
  encabezados?: string[];
  filaMuestra?: string[];
  delimitador?: string;
}

export interface FeedlotSesionMangaSolicitud {
  loteId: number;
  tipo: FeedlotTipoSesionManga;
  fecha: string;
  perfilLecturaId?: number | null;
  ajustarPlantel?: boolean;
  tipoEvento?: FeedlotTipoEventoSanitario;
  insumoId?: number | null;
  dosisPorAnimal?: number | null;
  unidadDosis?: string | null;
  cantidadTotal?: number | null;
  diasRetiro?: number | null;
  aplicador?: FeedlotAplicador;
  aplicadorNombre?: string | null;
  origenInsumo?: FeedlotOrigenInsumo;
  descripcion?: string | null;
  subtipoBaja?: FeedlotSubtipoBaja;
  motivoMuerteId?: number | null;
  precioKg?: number | null;
}

export type FeedlotAccionDesconocido = 'IGNORAR' | 'ENROLAR_EN_ESTE_LOTE' | 'ENROLAR_EN_LOTE_NUEVO';
export type FeedlotOrigenFilaRecuento =
  | 'PRESENTE'
  | 'FALTANTE'
  | 'OTRO_LOTE'
  | 'INACTIVO'
  | 'DESCONOCIDO';

export interface FeedlotAccionDesconocidoSolicitud {
  filaId: number;
  accion: FeedlotAccionDesconocido;
}

export interface FeedlotLoteNuevoRecuentoSolicitud {
  corralId: number;
  nombre: string;
  categoriaId?: number | null;
  razaId?: number | null;
}

export interface FeedlotConfirmarSesionSolicitud {
  ajustarPlantel?: boolean;
  cantidadTotal?: number | null;
  accionesDesconocidos?: FeedlotAccionDesconocidoSolicitud[];
  loteNuevo?: FeedlotLoteNuevoRecuentoSolicitud | null;
}

export interface FeedlotIdentificacionAnimal {
  id?: number | null;
  filaId?: number | null;
  eid?: string | null;
  caravanaVisual?: string | null;
  loteId?: number | null;
  loteNombre?: string | null;
  corralNombre?: string | null;
  estado?: string | null;
}

export interface FeedlotResumenRecuento {
  presentes?: number;
  cabezasSinEnrolar?: number;
  faltantes?: FeedlotIdentificacionAnimal[];
  intrusos?: FeedlotIdentificacionAnimal[];
  desconocidos?: FeedlotIdentificacionAnimal[];
  otroLote?: FeedlotIdentificacionAnimal[];
  inactivos?: FeedlotIdentificacionAnimal[];
}

export interface FeedlotSesionMangaFila {
  id?: number;
  numeroLinea?: number;
  eidCrudo?: string | null;
  eidNormalizado?: string | null;
  caravanaVisual?: string | null;
  pesoKg?: number | null;
  fechaHoraLectura?: string | null;
  animalId?: number | null;
  resultado?: FeedlotResultadoFilaManga | string;
  mensaje?: string | null;
  origenRecuento?: FeedlotOrigenFilaRecuento | string | null;
  accionDesconocido?: FeedlotAccionDesconocido | string | null;
  loteDestinoId?: number | null;
}

export interface FeedlotSesionManga {
  id: number;
  loteId?: number;
  loteNombre?: string | null;
  tipo: FeedlotTipoSesionManga | string;
  fecha: string;
  estado: FeedlotEstadoSesionManga | string;
  nombreArchivo?: string | null;
  filasOk?: number;
  filasError?: number;
  ajustarPlantel?: boolean;
  aplicador?: FeedlotAplicador | string | null;
  origenInsumo?: FeedlotOrigenInsumo | string | null;
  tipoEvento?: FeedlotTipoEventoSanitario | string | null;
  insumoId?: number | null;
  dosisPorAnimal?: number | null;
  cantidadTotal?: number | null;
  diasRetiro?: number | null;
  subtipoBaja?: FeedlotSubtipoBaja | string | null;
  filas?: FeedlotSesionMangaFila[];
  resumenRecuento?: FeedlotResumenRecuento | null;
  avisos?: string[];
  bloqueantes?: string[];
  puedeConfirmar?: boolean;
}
