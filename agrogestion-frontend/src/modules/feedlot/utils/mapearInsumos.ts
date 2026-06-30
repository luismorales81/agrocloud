export interface InsumoOpcionFeedlot {
  id: number;
  nombre: string;
  tipo?: string;
  unidadMedida?: string;
  stockActual?: number;
}

const TIPOS_AGROQUIMICO = new Set(['HERBICIDA', 'FUNGICIDA', 'INSECTICIDA', 'FERTILIZANTE']);

export function esAgroquimico(tipo?: string): boolean {
  return tipo != null && TIPOS_AGROQUIMICO.has(tipo.toUpperCase());
}

/** Mapea la respuesta de GET /api/insumos al formato usado en pantallas feedlot. */
export function mapearInsumosDesdeApi(raw: unknown): InsumoOpcionFeedlot[] {
  const arr = Array.isArray(raw) ? raw : [];
  return arr
    .map((x: Record<string, unknown>) => ({
      id: Number(x.id),
      nombre: String(x.nombre ?? x.descripcion ?? `Insumo ${x.id}`),
      tipo: x.tipo != null ? String(x.tipo) : undefined,
      unidadMedida:
        x.unidadMedida != null
          ? String(x.unidadMedida)
          : x.unidad_medida != null
            ? String(x.unidad_medida)
            : 'kg',
      stockActual:
        x.stockActual != null
          ? Number(x.stockActual)
          : x.stock_actual != null
            ? Number(x.stock_actual)
            : undefined,
    }))
    .filter((i) => !Number.isNaN(i.id) && i.id > 0);
}

/** Solo insumos aptos para alimentación (excluye agroquímicos y fertilizantes). */
export function filtrarInsumosAlimento(insumos: InsumoOpcionFeedlot[]): InsumoOpcionFeedlot[] {
  return insumos
    .filter((i) => !esAgroquimico(i.tipo))
    .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
}

/** Etiqueta para combo/autocomplete (nombre + stock si está disponible). */
export function etiquetaInsumoOpcion(i: InsumoOpcionFeedlot): string {
  return i.stockActual != null
    ? `${i.nombre} · Stock: ${i.stockActual} ${i.unidadMedida ?? 'kg'}`
    : i.nombre;
}

/** Lista ordenada para autocomplete, incluyendo el insumo ya seleccionado aunque no esté en el filtro. */
export function opcionesInsumoConSeleccion(
  insumosFiltrados: InsumoOpcionFeedlot[],
  insumosCompletos: InsumoOpcionFeedlot[],
  idSeleccionado: number | ''
): InsumoOpcionFeedlot[] {
  const lista = [...insumosFiltrados];
  if (idSeleccionado !== '' && !lista.some((i) => i.id === idSeleccionado)) {
    const actual = insumosCompletos.find((i) => i.id === idSeleccionado);
    if (actual) lista.unshift(actual);
  }
  return lista;
}
