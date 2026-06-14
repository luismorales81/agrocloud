/**
 * Tipos TypeScript para el módulo de Porcinos
 */

// ============================================================================
// ESTADOS Y ENUMS
// ============================================================================

export type EstadoMadre = 
  | 'CACHORRA' 
  | 'ADULTA' 
  | 'GESTACION' 
  | 'LACTANCIA' 
  | 'RECRIA' 
  | 'DESCARTE';

export type TipoServicio = 'MONTA_NATURAL' | 'IA';

export type Origen = 'EXTERNA' | 'INTERNA';

export type OrigenSemen = 'INTERNO' | 'EXTERNO';

export type EstadoServicio = 
  | 'PENDIENTE_CONTROL' 
  | 'FALLIDO' 
  | 'PREÑEZ_CONFIRMADA';

export type EstadoGestacion = 'EN_CURSO' | 'ABORTO' | 'FINALIZADA' | 'ACTIVA';

export type CausaMuerte = 
  | 'APLASTAMIENTO' 
  | 'DIARREA' 
  | 'MALFORMACION' 
  | 'DEBILIDAD' 
  | 'OTRA';

export type DestinoRecria = 'VENTA' | 'FUTURA_MADRE' | 'ENGORDE';

export type EtapaAlimentacion = 
  | 'GESTACION' 
  | 'LACTANCIA' 
  | 'F1' 
  | 'F2' 
  | 'F3' 
  | 'F4' 
  | 'DESARROLLO' 
  | 'TERMINACION';

export type Sexo = 'MACHO' | 'HEMBRA';

// ============================================================================
// ENTIDADES PRINCIPALES
// ============================================================================

/**
 * Historial de estados de una madre
 */
export interface HistorialEstado {
  id?: number;
  estado: EstadoMadre;
  fechaInicio: string;
  fechaFin?: string;
  observaciones?: string;
}

/**
 * Madre / Unidad productiva
 */
export interface Madre {
  id?: number;
  activo?: boolean;
  identificacion: string;
  fechaNacimiento: string;
  cantidadTetas: number;
  origen: Origen;
  estadoActual: EstadoMadre;
  fechaIngresoGranja: string;
  historialEstados?: HistorialEstado[];
  ultimoServicio?: Servicio;
  gestacionActiva?: Gestacion;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Servicio (monta natural o inseminación artificial)
 */
export interface Servicio {
  id?: number;
  madreId: number;
  tipo: TipoServicio;
  fechaServicio: string;
  machoId?: number;
  machoNombre?: string;
  origenSemen?: OrigenSemen;
  numeroIntento: number;
  estadoServicio: EstadoServicio;
  fechaControlCelo: string; // fechaServicio + 21 días
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Gestación
 */
export interface Gestacion {
  id?: number;
  madreId: number;
  madreIdentificacion?: string;
  fechaInicio: string;
  fechaProbableParto: string; // fechaInicio + 115 días (configurable)
  estado: EstadoGestacion;
  servicioId?: number;
  fechaAborto?: string;
  causaAborto?: string;
  fechaSalaMaternidad?: string;
  observaciones?: string;
  chequeos?: ChequeoGestacion[];
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Chequeo de gestación
 */
export interface ChequeoGestacion {
  id?: number;
  gestacionId: number;
  fecha: string;
  metodo: 'ECO' | 'PALPACION' | 'OBSERVACION';
  resultado: 'POSITIVO' | 'NEGATIVO';
  observaciones?: string;
  createdAt?: string;
}

/**
 * Muerte de lechones durante lactancia
 */
export interface MuerteLactancia {
  id?: number;
  fecha: string;
  causa: CausaMuerte;
  cantidad: number;
  observaciones?: string;
}

/**
 * Parto
 */
export interface Parto {
  id?: number;
  madreId: number;
  madreIdentificacion?: string;
  fechaInicio: string;
  fechaFin?: string;
  nacidosVivos: number;
  nacidosMuertos: number;
  momias: number;
  totalNacidos: number;
  pesoPromedioNacimiento?: number;
  muertesEnLactancia?: MuerteLactancia[];
  destete?: Destete;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Destete
 */
export interface Destete {
  id?: number;
  partoId: number;
  fechaDestete: string;
  cantidadDestetados: number;
  pesoPromedioDestete: number;
  diasLactancia?: number;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * DTO para crear un parto
 */
export interface PartoCreateDTO {
  madreId: number;
  gestacionId?: number;
  fechaInicio: string;
  fechaFin?: string;
  nacidosVivos: number;
  nacidosMuertos: number;
  momias: number;
  totalNacidos: number;
  pesoPromedioNacimiento?: number;
  observaciones?: string;
}

/**
 * Consumo de Alimento
 */
export interface ConsumoAlimento {
  id?: number;
  categoria: 'MADRES' | 'PADRILLOS' | 'RECRIA' | 'ENGORDE' | 'LECHONES';
  fecha: string;
  cantidadKg: number;
  tipoAlimento: 'BALANCEADO' | 'MAIZ' | 'GRANO_PROPIO';
  cultivoRelacionadoId?: number;
  cultivoRelacionadoNombre?: string;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Venta de Porcino (incluye ventas normales y faenas)
 */
export interface VentaPorcino {
  id?: number;
  tipo: 'ENGORDE' | 'REPRODUCTOR' | 'FAENA';
  fecha: string;
  cantidad: number;
  pesoPromedio: number;
  precioKg?: number; // Opcional para faenas
  ingresoTotal: number;
  loteId?: number;
  loteNombre?: string;
  recriaId?: number; // Obligatorio para FAENA
  cliente?: string;
  observaciones?: string;
  // Campos específicos de faena (solo cuando tipo = 'FAENA')
  pesoEnvio?: number;
  pesoFaena?: number;
  rendimiento?: number; // porcentaje
  fechaEnvio?: string;
  fechaFaena?: string;
  createdAt?: string;
  updatedAt?: string;
}

/** Alias: faenas usan el mismo contrato que ventas con tipo FAENA */
export type Faena = VentaPorcino;

/**
 * Catálogo legacy de tipos de alimento (UI en configuraciones; sin API dedicada).
 */
export interface TipoAlimentoPorcino {
  id?: number;
  nombre: string;
  categoria: string;
  descripcion?: string;
  porcentajeProteina?: number;
  precioKg?: number;
  unidadMedida?: string;
}

/**
 * Muerte en recría
 */
export interface MuerteRecria {
  id?: number;
  fecha: string;
  causa: CausaMuerte;
  cantidad: number;
  observaciones?: string;
}

/**
 * Recría
 */
export interface Recria {
  id?: number;
  loteId: number;
  loteNombre?: string;
  madreNombre?: string;
  // Origen de la recría: DESTETE (desde un parto/destete interno) o EXTERNO (compra/ingreso externo)
  origen?: 'DESTETE' | 'EXTERNO';
  fechaIngreso: string;
  pesoIndividual?: number;
  pesoPromedio: number;
  cantidadAnimales: number;
  sexo: Sexo;
  historialMuertes?: MuerteRecria[];
  fechaSalida?: string;
  destino?: DestinoRecria;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Insumo en fórmula de alimentación
 */
export interface InsumoFormula {
  id?: number;
  insumoId: number;
  insumoNombre?: string;
  cantidad: number;
  unidadMedida: string;
  porcentaje?: number;
}

/**
 * Fórmula de alimentación
 */
export interface Formula {
  id?: number;
  etapa: EtapaAlimentacion;
  nombre: string;
  insumos: InsumoFormula[];
  nucleos?: InsumoFormula[];
  vitaminas?: InsumoFormula[];
  antibioticos?: InsumoFormula[];
  cantidadRecomendada: number;
  unidadMedida: string;
  observaciones?: string;
  activa: boolean;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * Consumo de alimentación
 */
export interface ConsumoAlimentacion {
  id?: number;
  fecha: string;
  loteId?: number;
  loteNombre?: string;
  madreId?: number;
  madreIdentificacion?: string;
  formulaId: number;
  formulaNombre?: string;
  cantidad: number;
  unidadMedida: string;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ============================================================================
// DTOs PARA FORMULARIOS
// ============================================================================

export interface MadreCreateDTO {
  identificacion: string;
  fechaNacimiento: string;
  cantidadTetas: number;
  origen: Origen;
  fechaIngresoGranja: string;
  observaciones?: string;
}

export interface ServicioCreateDTO {
  madreId: number;
  tipo: TipoServicio;
  fechaServicio: string;
  machoId?: number;
  machoNombre?: string;
  origenSemen?: OrigenSemen;
  observaciones?: string;
}

export interface ControlCeloDTO {
  servicioId: number;
  preñada: boolean;
  observaciones?: string;
}

export interface PartoCreateDTO {
  madreId: number;
  fechaInicio: string;
  fechaFin?: string;
  nacidosVivos: number;
  nacidosMuertos: number;
  momias: number;
  muertesEnLactancia?: MuerteLactancia[];
  observaciones?: string;
}

export interface RecriaIngresoDTO {
  loteId: number;
  fechaIngreso: string;
  pesoIndividual?: number;
  pesoPromedio: number;
  cantidadAnimales: number;
  sexo: Sexo;
  observaciones?: string;
  /** Origen del lote de recría. En el alta manual siempre será EXTERNO; las recrías por destete se crean desde el módulo de destete. */
  origen?: 'DESTETE' | 'EXTERNO';
}

export interface FormulaCreateDTO {
  etapa: EtapaAlimentacion;
  nombre: string;
  insumos: InsumoFormula[];
  nucleos?: InsumoFormula[];
  vitaminas?: InsumoFormula[];
  antibioticos?: InsumoFormula[];
  cantidadRecomendada: number;
  unidadMedida: string;
  observaciones?: string;
}

export interface ConsumoCreateDTO {
  fecha: string;
  loteId?: number;
  madreId?: number;
  formulaId: number;
  cantidad: number;
  observaciones?: string;
}

// ============================================================================
// TIPOS PARA DASHBOARD Y KPIs
// ============================================================================

export interface DashboardKPIs {
  totalMadres: number;
  cerdasEnCelo: number;
  serviciosPendientesControl: number;
  gestacionesActivas: number;
  partosProximos: number; // próximos 7 días
  alertas: Alerta[];
}

export interface Alerta {
  id: string;
  tipo: 'GESTACION_PROXIMA' | 'CONTROL_CELO_PENDIENTE' | 'CERDA_EN_CELO' | 'MUERTE_RECENTE';
  titulo: string;
  descripcion: string;
  fecha: string;
  prioridad: 'ALTA' | 'MEDIA' | 'BAJA';
  relacionadoCon?: {
    tipo: 'MADRE' | 'SERVICIO' | 'GESTACION' | 'PARTO' | 'RECRIA';
    id: number;
  };
}

// ============================================================================
// TIPOS PARA FILTROS Y BÚSQUEDAS
// ============================================================================

export interface FiltrosMadres {
  estado?: EstadoMadre;
  origen?: Origen;
  fechaIngresoDesde?: string;
  fechaIngresoHasta?: string;
  identificacion?: string;
}

export interface FiltrosPartos {
  fechaDesde?: string;
  fechaHasta?: string;
  madreId?: number;
  loteId?: number;
}

export interface FiltrosRecria {
  loteId?: number;
  sexo?: Sexo;
  fechaIngresoDesde?: string;
  fechaIngresoHasta?: string;
  activas?: boolean;
}

// ============================================================================
// TIPOS PARA NUEVAS FUNCIONALIDADES
// ============================================================================

/**
 * Configuración del módulo Porcinos
 */
export interface ConfiguracionPorcino {
  id?: number;
  clave: string;
  valor: string;
  tipo: 'NUMERO' | 'TEXTO' | 'BOOLEAN' | 'FECHA';
  descripcion?: string;
  categoria: string;
  activo?: boolean;
}

// ============================================================================
// CATÁLOGOS CONFIGURABLES
// ============================================================================

/**
 * Raza de cerdo configurable
 */
export interface RazaPorcino {
  id?: number;
  nombre: string;
  tipo: 'MADRE' | 'PADRILLO' | 'HIBRIDO';
  descripcion?: string;
  activo?: boolean;
}

/**
 * Tipo de alimento configurable (ELIMINADO - REDUNDANTE)
 * 
 * NOTA: TipoAlimentoPorcino fue eliminado porque es redundante con:
 * - Recetas: InsumoCompuesto (tipo RACION) asociado a etapas mediante RecetaAlimentacionPorEtapa
 * - Balanceados comerciales: Insumo (tabla cultivo_insumos)
 * - Granos propios: Cultivo + InventarioGrano
 * TipoAlimentoPorcino solo existía como catálogo sin integración funcional
 * en el sistema de consumo actual (ConsumoDiarioAutomatico)
 */

/**
 * Tipo de servicio reproductivo configurable
 */
export interface TipoServicioPorcino {
  id?: number;
  nombre: string;
  tipo: 'MONTA_NATURAL_DIRECTA' | 'IA_POSCERVICAL' | 'IA_TRADICIONAL' | 'SERVICIO_REPETIDO';
  descripcion?: string;
  activo?: boolean;
}

/**
 * Causa de mortalidad configurable por etapa
 */
export interface CausaMortalidadPorcino {
  id?: number;
  nombre: string;
  etapa: 'LACTANCIA' | 'RECRIA' | 'ENGORDE' | 'GESTACION' | 'GENERAL';
  descripcion?: string;
  activo?: boolean;
}

/**
 * Motivo de baja configurable
 */
export interface MotivoBajaPorcino {
  id?: number;
  nombre: string;
  tipo: 'VENTA' | 'MUERTE' | 'REEMPLAZO' | 'PROBLEMAS_SANITARIOS' | 'PROBLEMAS_REPRODUCTIVOS';
  descripcion?: string;
  activo?: boolean;
}

/**
 * Esquema sanitario configurable
 */
export interface EsquemaSanitarioPorcino {
  id?: number;
  nombre: string;
  tipo: 'VACUNA' | 'DESPARASITACION' | 'ANTIBIOTICO' | 'OTRO';
  producto?: string;
  dosis?: string;
  frecuenciaDias?: number;
  fechaProgramada?: string;
  aplicableA?: string;
  descripcion?: string;
  activo?: boolean;
}

/**
 * Tipo de parto configurable
 */
export interface TipoParto {
  id?: number;
  nombre: string;
  descripcion?: string;
  requiereIntervencion: boolean;
  activo?: boolean;
}

/**
 * Ubicación interna con estructura jerárquica (Galpón → Sala → Corral)
 */
export interface UbicacionInterna {
  id?: number;
  nombre: string;
  codigo?: string;
  nivel: 'GALPON' | 'SALA' | 'CORRAL';
  ubicacionPadreId?: number;
  tipoUbicacion?: 'MATERNIDAD' | 'GESTACION' | 'RECRIA' | 'ENGORDE' | 'AISLAMIENTO' | 'GENERAL';
  capacidadMaxima?: number;
  descripcion?: string;
  activo?: boolean;
  ubicacionesHijas?: UbicacionInterna[];
}

// ============================================================================
// PARÁMETROS DEL SISTEMA
// ============================================================================

/**
 * Parámetros generales del establecimiento
 */
export interface ParametrosEstablecimientoPorcino {
  id?: number;
  nombreEstablecimiento: string;
  provincia?: string;
  localidad?: string;
  razonSocial?: string;
  unidadManejo: 'LOTES' | 'GRUPOS' | 'ANIMALES_INDIVIDUALES';
  maximoMadres?: number;
  maximoPadrillos?: number;
  maximaCapacidadRecriaEngorde?: number;
  categoriasHabilitadas?: string;
  ciclosProductivosPropios?: string;
  realizaFaena?: boolean; // Indica si el establecimiento realiza faenas
}

/**
 * Parámetros productivos configurables
 */
export interface ParametrosProductivosPorcino {
  id?: number;
  // Ciclo de producción
  diasPromedioGestacion?: number;
  diasLactancia?: number;
  diasRecriaAntesEngorde?: number;
  diasEngorde?: number;
  // Parámetros reproductivos adicionales
  diasToleranciaVencimientoGestacion?: number;
  diasControlCelo?: number;
  diasEntreCelos?: number;
  diasPasajeMaternidad?: number;
  // Servicios
  cantidadMaximaServiciosPadrilloDia?: number;
  tiempoEsperaEntreServiciosHoras?: number;
  // Alertas y recordatorios
  diasAntelacionAlertarPartos?: number;
  diasAntelacionAlertarEcografias?: number;
  diasAntelacionAlertarDestetes?: number;
  diasAntelacionAlertarPasajeMaternidad?: number;
  diasAntelacionAlertarRevisionesSanitarias?: number;
  // Umbrales
  umbralMortalidadLactanciaPorcentaje?: number;
  umbralMortalidadRecriaPorcentaje?: number;
  porcentajeMinimoPrenezAntesAdvertencia?: number;
  // Pesos estándar (kg)
  pesoPromedioNacimiento?: number;
  pesoDesteteObjetivo?: number;
  pesoVentaObjetivo?: number;
  // Índices productivos objetivo
  lechonesVivosPartoObjetivo?: number;
  lechonesDestetadosObjetivo?: number;
  partosMadreAnioObjetivo?: number;
}

/**
 * Datos económicos configurables
 */
export interface DatosEconomicosPorcino {
  id?: number;
  // Costos por categoría
  costoMadreGestacionDia?: number;
  costoMadreLactanciaDia?: number;
  costoLechon?: number;
  costoEngordeDia?: number;
  costoManoObraDia?: number;
  precioVentaCerdoTerminadoKg?: number;
  porcentajeMermaTransporte?: number;
  // Integración con cultivos
  kgMaizPorRacionEngorde?: number;
  porcentajeMezclaAlimentoPropioBalanceado?: number;
  indiceConversionObjetivo?: number;
  metodoImputacionCostoCultivo?: 'PROMEDIO_PONDERADO' | 'PRECIO_MERCADO' | 'PRECIO_MANUAL';
  precioManualCultivoKg?: number;
}

/**
 * Padrillo (macho reproductor)
 */
export interface Padrillo {
  id?: number;
  identificacion: string;
  fechaNacimiento: string;
  origen: 'EXTERNA' | 'INTERNA';
  fechaIngresoGranja: string;
  fechaBaja?: string;
  motivoBaja?: string;
  observaciones?: string;
  activo?: boolean;
}

/**
 * Lechón NN (sin origen claro)
 */
export interface LechonNN {
  id?: number;
  identificacion?: string;
  fechaRegistro: string;
  loteId: number;
  cantidad: number;
  pesoPromedio?: number;
  etapaIngreso: 'DESTETE' | 'RECRIA' | 'F1' | 'F2' | 'F3' | 'F4';
  observaciones?: string;
  activo?: boolean;
}

/**
 * Transferencia de Lechones
 */
export interface TransferenciaLechon {
  id?: number;
  partoOrigenId: number;
  madreOrigenId: number;
  partoDestinoId: number;
  madreDestinoId: number;
  fechaTransferencia: string;
  cantidad: number;
  motivo?: string;
  observaciones?: string;
}

/** Método de obtención del peso en una pesada */
export type MetodoPesaje = 'BALANZA' | 'MUESTREO' | 'ESTIMADO';

/**
 * Registro de Peso (histórico por lote/recría)
 */
export interface RegistroPeso {
  id?: number;
  recriaId?: number;
  fechaPesaje: string;
  pesoPromedio: number;
  cantidadAnimales: number;
  metodo?: MetodoPesaje;
  etapaAlMomento?: EtapaRecria;
  observaciones?: string;
}

/**
 * Movimiento entre Etapas
 */
export type EtapaRecria = 'F1' | 'F2' | 'F3' | 'F4' | 'DESARROLLO' | 'TERMINACION';

export interface MovimientoEtapa {
  id?: number;
  recriaOrigenId: number;
  recriaDestinoId?: number;
  etapaOrigen: EtapaRecria;
  etapaDestino: EtapaRecria;
  fechaMovimiento: string;
  cantidadAnimales: number;
  pesoPromedio?: number;
  loteDestinoId?: number;
  observaciones?: string;
}

/**
 * Faena
 */
// NOTA: La interfaz Faena fue unificada con VentaPorcino
// Para faenas, usar VentaPorcino con tipo: 'FAENA'

/**
 * Stock de Alimento
 */
export interface StockAlimento {
  id?: number;
  insumoId?: number;
  cultivoId?: number;
  tipo: 'INSUMO' | 'GRANO_PROPIO' | 'SUBPRODUCTO';
  nombre: string;
  cantidadDisponible: number;
  unidadMedida: string;
}

/**
 * Movimiento de Stock
 */
export interface MovimientoStock {
  id?: number;
  stockId: number;
  tipoMovimiento: 'ENTRADA' | 'SALIDA' | 'AJUSTE';
  cantidad: number;
  motivo?: string;
  relacionadoCon: 'CONSUMO' | 'COMPRA' | 'PRODUCCION' | 'VENTA' | 'AJUSTE';
  relacionadoId?: number;
  fechaMovimiento: string;
}

// Actualizar Recria para incluir etapa
export interface Recria {
  id?: number;
  loteId: number;
  loteNombre?: string;
  fechaIngreso: string;
  pesoIndividual?: number;
  pesoPromedio: number;
  cantidadAnimales: number;
  sexo: Sexo;
  etapa?: EtapaRecria; // Agregado
  historialMuertes?: MuerteRecria[];
  fechaSalida?: string;
  destino?: DestinoRecria;
  observaciones?: string;
  createdAt?: string;
  updatedAt?: string;
}

// ============================================================================
// INSUMOS COMPUESTOS (RECETAS/FORMULAS DE ALIMENTO)
// ============================================================================

/**
 * Tipo de insumo compuesto
 */
export type TipoInsumoCompuesto = 'RACION' | 'NUCLEO' | 'MEZCLA' | 'PREMEZCLA' | 'OTRO';

/**
 * Tipo de componente de un insumo compuesto
 */
export type TipoComponente = 'INSUMO' | 'GRANO_PROPIO' | 'INSUMO_COMPUESTO';

/**
 * Componente (ingrediente) de un insumo compuesto
 */
export interface ComponenteInsumoCompuesto {
  id?: number;
  insumoCompuestoId?: number;     // ID del insumo compuesto padre (se asigna automáticamente)
  tipoComponente: TipoComponente;
  
  // Solo uno de estos será usado según tipoComponente
  insumoId?: number;              // Si tipoComponente = 'INSUMO'
  cultivoId?: number;             // Si tipoComponente = 'GRANO_PROPIO'
  insumoCompuestoPadreId?: number; // Si tipoComponente = 'INSUMO_COMPUESTO'
  
  // Información del componente (para mostrar en UI)
  nombreComponente?: string;      // Nombre del componente (populado por el backend)
  insumoNombre?: string;
  cultivoNombre?: string;
  insumoCompuestoPadreNombre?: string;
  
  porcentaje?: number;            // Porcentaje en la mezcla (0-100)
  cantidadFija?: number;         // Cantidad fija (alternativa al porcentaje)
  unidadMedida?: string;
  ordenMezcla?: number;          // Orden en que se agregan los componentes
  observaciones?: string;
}

/**
 * Insumo compuesto (receta/formula de alimento)
 */
export interface InsumoCompuesto {
  id?: number;
  nombre: string;
  descripcion?: string;
  tipo: TipoInsumoCompuesto;
  unidadMedida?: string;
  rendimiento?: number;           // Rendimiento de producción (ej: 0.95 = 95%)
  costoUnitarioCalculado?: number;
  costoUnitarioManual?: number;
  stockActual?: number;
  stockMinimo?: number;
  activo?: boolean;
  componentes?: ComponenteInsumoCompuesto[];
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

/**
 * DTO de cálculo de componente para preparación de receta
 */
export interface ComponenteCalculoDTO {
  nombreComponente: string;
  tipoComponente: string;
  unidadMedida: string;
  cantidadPorKgReceta: number;      // Cantidad necesaria por kg de receta
  cantidadNecesaria: number;        // Cantidad necesaria para cantidad solicitada
  stockDisponible: number;
  maximoPreparable: number | null;  // Máximo preparable con este componente (en kg de receta)
  stockSuficiente: boolean;
  mensajeStock?: string;
}

/**
 * Respuesta de cálculo de preparación de receta
 */
export interface CalcularPreparacionRecetaResponse {
  recetaId: number;
  recetaNombre: string;
  unidadMedida: string;
  cantidadSolicitada: number;
  componentes: ComponenteCalculoDTO[];
  maximoPreparableConStock: number | null;  // Máximo preparable con stock disponible
  stockSuficienteGlobal: boolean;
  mensaje: string;
}

/**
 * Receta de alimentación por etapa
 * Asocia un insumo compuesto a una etapa de alimentación
 */
export interface RecetaAlimentacionPorEtapa {
  id?: number;
  insumoCompuestoId: number;
  insumoCompuestoNombre?: string;
  etapa: EtapaAlimentacion;
  cantidadDiariaPorAnimal: number;  // kg por animal por día
  cantidadDiariaMinima?: number;
  cantidadDiariaMaxima?: number;
  pesoMinimoAnimal?: number;         // kg
  pesoMaximoAnimal?: number;         // kg
  edadMinimaDias?: number;
  edadMaximaDias?: number;
  esPorDefecto?: boolean;
  observaciones?: string;
  activo?: boolean;
}

// ============================================================================
// EVENTOS SANITARIOS
// ============================================================================

/**
 * Categoría de evento sanitario
 */
export type CategoriaEventoSanitario = 
  | 'VACUNACION'
  | 'DESPARASITACION'
  | 'ANTIBIOTICO'
  | 'VITAMINA'
  | 'TRATAMIENTO'
  | 'CONTROL'
  | 'OTRO';

/**
 * Tipo de entidad a la que se aplica el evento sanitario
 */
export type TipoEntidadEvento = 'MADRE' | 'PADRILLO' | 'LOTE';

/**
 * Tipo de evento sanitario (catálogo)
 */
export interface TipoEventoSanitario {
  id?: number;
  nombre: string;
  descripcion?: string;
  categoria: CategoriaEventoSanitario;
  requiereFechaRetiro?: boolean;
  diasRetiroDefecto?: number;
  requiereLoteMedicamento?: boolean;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
}

/**
 * Evento sanitario registrado
 */
export interface EventoSanitario {
  id?: number;
  tipoEventoSanitarioId: number;
  tipoEventoSanitario?: TipoEventoSanitario;
  insumoId?: number;  // ID del insumo (vacuna/medicamento) usado
  insumo?: {  // Objeto insumo completo (opcional, viene del backend)
    id: number;
    nombre: string;
    unidadMedida?: string;
  };
  fecha: string;  // ISO date string
  tipoEntidad: TipoEntidadEvento;
  entidadId: number;
  dosis?: number;
  unidadDosis?: string;
  loteMedicamento?: string;
  profesionalResponsable?: string;
  fechaRetiro?: string;  // ISO date string
  retiroCumplido?: boolean;
  observaciones?: string;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  
  // Campos auxiliares para mostrar en la UI
  entidadNombre?: string;  // Nombre de la madre, padrillo o lote
  entidadCodigo?: string;  // Código de la entidad
}

/**
 * DTO para crear/actualizar evento sanitario
 */
export interface EventoSanitarioCreateDTO {
  tipoEventoSanitarioId: number;
  insumoId?: number;  // ID del insumo (vacuna/medicamento) a usar
  fecha: string;
  tipoEntidad: TipoEntidadEvento;
  entidadId: number;
  dosis?: number;
  unidadDosis?: string;
  loteMedicamento?: string;
  profesionalResponsable?: string;
  fechaRetiro?: string;
  observaciones?: string;
}

/**
 * Filtros para listar eventos sanitarios
 */
export interface FiltrosEventosSanitarios {
  tipoEntidad?: TipoEntidadEvento;
  entidadId?: number;
  fechaInicio?: string;
  fechaFin?: string;
  categoria?: CategoriaEventoSanitario;
  tipoEventoSanitarioId?: number;
  retirosVencidos?: boolean;
  retirosProximos?: boolean;
}

