/** Formato numérico local (es-AR). */
export function formatearNumero(valor: number, fracciones = 2): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 0,
    maximumFractionDigits: fracciones,
  }).format(valor);
}

/** Fecha ISO o cadena a texto corto local. */
export function formatearFechaCorta(iso: string | null | undefined): string {
  if (iso == null || iso === '') {
    return '—';
  }
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) {
    return iso;
  }
  return d.toLocaleDateString('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function etiquetaEstadoGalpon(estado: string | null | undefined): string {
  if (estado == null || estado === '') {
    return '—';
  }
  const u = estado.toUpperCase();
  if (u === 'ACTIVO') return 'Activo';
  if (u === 'CERRADO') return 'Cerrado';
  return estado;
}
