/** Modos de calendario compartido ({@link CalendarioDashboard}). */
export type ModoCalendario =
  | 'cultivos'
  | 'feedlot'
  | 'avicolaHuevos'
  | 'avicolaCrianza'
  | 'avicolaCarne'
  | 'avicolaPonedoras';

export interface OpcionTipoRecordatorio {
  valor: string;
  etiqueta: string;
}

const tiposAvicolaBase: OpcionTipoRecordatorio[] = [
  { valor: 'GENERAL', etiqueta: 'General' },
  { valor: 'ALIMENTACION', etiqueta: 'Alimentación' },
  { valor: 'SANIDAD', etiqueta: 'Sanidad / vacunas' },
  { valor: 'MANTENIMIENTO', etiqueta: 'Mantenimiento galpón' },
  { valor: 'REUNION', etiqueta: 'Reunión / visita' },
  { valor: 'OTRO', etiqueta: 'Otro' },
];

export const TIPOS_RECORDATORIO_POR_MODO: Record<ModoCalendario, OpcionTipoRecordatorio[]> = {
  cultivos: [
    { valor: 'GENERAL', etiqueta: 'General' },
    { valor: 'LABOR', etiqueta: 'Labor de campo' },
    { valor: 'COSECHA', etiqueta: 'Cosecha' },
    { valor: 'MANTENIMIENTO', etiqueta: 'Mantenimiento' },
    { valor: 'INSUMO', etiqueta: 'Insumo / agroquímico' },
    { valor: 'REUNION', etiqueta: 'Reunión' },
    { valor: 'OTRO', etiqueta: 'Otro' },
  ],
  feedlot: [
    { valor: 'GENERAL', etiqueta: 'General' },
    { valor: 'ALIMENTACION', etiqueta: 'Alimentación / dieta' },
    { valor: 'SANIDAD', etiqueta: 'Sanidad / tratamientos' },
    { valor: 'MANTENIMIENTO', etiqueta: 'Mantenimiento corral' },
    { valor: 'INSUMO', etiqueta: 'Insumo / balanceado' },
    { valor: 'REUNION', etiqueta: 'Reunión / visita' },
    { valor: 'OTRO', etiqueta: 'Otro' },
  ],
  avicolaHuevos: tiposAvicolaBase,
  avicolaCrianza: tiposAvicolaBase,
  avicolaCarne: [
    { valor: 'GENERAL', etiqueta: 'General' },
    { valor: 'ALIMENTACION', etiqueta: 'Alimentación' },
    { valor: 'SANIDAD', etiqueta: 'Sanidad' },
    { valor: 'MANTENIMIENTO', etiqueta: 'Mantenimiento galpón' },
    { valor: 'OTRO', etiqueta: 'Faena / venta / otro' },
    { valor: 'REUNION', etiqueta: 'Reunión / visita' },
  ],
  avicolaPonedoras: tiposAvicolaBase,
};

/** Compatibilidad con prop histórica `general`. */
export function normalizarModoCalendario(
  modo?: ModoCalendario | 'general'
): ModoCalendario {
  if (modo == null || modo === 'general') {
    return 'cultivos';
  }
  return modo;
}

export function tiposRecordatorioDelModo(
  modo?: ModoCalendario | 'general'
): OpcionTipoRecordatorio[] {
  return TIPOS_RECORDATORIO_POR_MODO[normalizarModoCalendario(modo)];
}

export function etiquetaTipoRecordatorio(
  tipo: string,
  modo?: ModoCalendario | 'general'
): string {
  const opcion = tiposRecordatorioDelModo(modo).find((t) => t.valor === tipo);
  if (opcion) {
    return opcion.etiqueta;
  }
  const cultivos = TIPOS_RECORDATORIO_POR_MODO.cultivos.find((t) => t.valor === tipo);
  return cultivos?.etiqueta ?? tipo;
}

export function tituloCalendario(modo?: ModoCalendario | 'general'): string {
  switch (normalizarModoCalendario(modo)) {
    case 'feedlot':
      return 'Calendario feedlot';
    case 'avicolaHuevos':
      return 'Calendario avícola huevos';
    case 'avicolaCrianza':
      return 'Calendario avícola crianza';
    case 'avicolaCarne':
      return 'Calendario avícola carne';
    case 'avicolaPonedoras':
      return 'Calendario avícola ponedoras';
    default:
      return 'Calendario de labores y tareas';
  }
}

/** El calendario feedlot solo lista tareas recurrentes con ámbito FEEDLOT. */
export function permiteRecordatoriosEnModo(modo?: ModoCalendario | 'general'): boolean {
  return normalizarModoCalendario(modo) !== 'feedlot';
}

export function esCalendarioCultivos(modo?: ModoCalendario | 'general'): boolean {
  return normalizarModoCalendario(modo) === 'cultivos';
}
