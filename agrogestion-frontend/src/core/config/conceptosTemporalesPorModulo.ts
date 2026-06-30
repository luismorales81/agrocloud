import { ModuleId } from '../types/module.types';

/** Concepto temporal transversal (entidad core_campanas) según el módulo activo. */
export interface ConceptoTemporalModulo {
  /** Etiqueta del selector global (barra superior). */
  etiquetaPeriodo: string;
  /** Unidad operativa principal del módulo (ciclo batch o productivo). */
  unidadOperativa: string;
  /** Texto breve para orientar al usuario. */
  ayuda: string;
  /** Ruta relativa al módulo para administrar períodos (solo administradores). */
  rutaGestionPeriodos: string;
}

const POR_DEFECTO: ConceptoTemporalModulo = {
  etiquetaPeriodo: 'Período de gestión',
  unidadOperativa: 'Operación del módulo',
  ayuda: 'Consolida reportes y movimientos económicos del período seleccionado.',
  rutaGestionPeriodos: 'configuracion/periodos',
};

export const conceptosTemporalesPorModulo: Record<ModuleId, ConceptoTemporalModulo> = {
  cultivos: {
    etiquetaPeriodo: 'Campaña agrícola',
    unidadOperativa: 'Ciclo de cultivo (siembra → cosecha)',
    ayuda: 'Cada lote puede tener un ciclo por campaña. Las labores y costos se imputan al ciclo activo.',
    rutaGestionPeriodos: 'configuracion/campanas',
  },
  porcinos: {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Lote de recría / engorde',
    ayuda: 'La reproducción (madres, servicios, partos) es continua. Recría, ventas y alimentación se agrupan por período.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
  'avicola-crianza': {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Lote parrillero (ciclo de engorde)',
    ayuda: 'Ciclos cortos por lote (35–50 días). El período agrupa consumos, ventas, faena y KPIs.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
  'avicola-huevos': {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Lote de postura',
    ayuda: 'La postura se sigue por lote y edad. El período consolida producción, consumos y ventas de huevos.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
  'avicola-ponedoras': {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Galpón / lote de aves',
    ayuda: 'Producción continua por galpón. El período ordena reportes económicos y de consumo.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
  feedlot: {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Lote de engorde',
    ayuda: 'Ciclos de 90–180 días por lote. El período agrupa consumos, faenas, KPIs y finanzas del módulo.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
  lecheria: {
    etiquetaPeriodo: 'Período de gestión',
    unidadOperativa: 'Rodeo / lactancia',
    ayuda: 'La producción lechera es continua. El período consolida ordeñes, ventas de leche, KPIs y reportes del tambo.',
    rutaGestionPeriodos: 'configuracion/periodos',
  },
};

export function obtenerConceptoTemporal(moduloId: ModuleId | null): ConceptoTemporalModulo {
  if (!moduloId) {
    return POR_DEFECTO;
  }
  return conceptosTemporalesPorModulo[moduloId] ?? POR_DEFECTO;
}
