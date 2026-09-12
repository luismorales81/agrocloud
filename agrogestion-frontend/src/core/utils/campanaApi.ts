import type { Campana } from '../../contexts/CampanaContext';

/** Normaliza respuesta del API (camelCase o snake_case). */
export function normalizarCampana(dato: unknown): Campana | null {
  if (!dato || typeof dato !== 'object') {
    return null;
  }
  const o = dato as Record<string, unknown>;
  if (o.id == null) {
    return null;
  }
  return {
    id: Number(o.id),
    empresaId: Number(o.empresaId ?? o.empresa_id ?? 0),
    codigo: String(o.codigo ?? ''),
    nombre: String(o.nombre ?? ''),
    fechaInicio: String(o.fechaInicio ?? o.fecha_inicio ?? ''),
    fechaFin: String(o.fechaFin ?? o.fecha_fin ?? ''),
    estado: String(o.estado ?? 'BORRADOR') as Campana['estado'],
    esDefault: Boolean(o.esDefault ?? o.es_default ?? false),
  };
}

export function normalizarListaCampanas(dato: unknown): Campana[] {
  if (!Array.isArray(dato)) {
    return [];
  }
  return dato
    .map(normalizarCampana)
    .filter((c): c is Campana => c != null);
}
