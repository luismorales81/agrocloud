import api from '../../services/api';
import { API_ENDPOINTS } from '../../services/apiEndpoints';
import { listarLotesCrianza } from '../../modules/avicola-crianza/services/avicolaCrianzaApi';
import { listarGalpones } from '../../modules/avicola-ponedoras/services/avicolaPonedorasService';
import { listarLotesHuevos } from '../../modules/avicola-huevos/services/avicolaHuevosApi';
import type { TipoEntidadExpediente } from '../../services/trazabilidadExpedienteService';

export interface OpcionEntidad {
  id: number;
  etiqueta: string;
}

export interface ConfiguracionExpedienteModulo {
  tituloPagina: string;
  descripcion: string;
  tiposEntidad: { valor: TipoEntidadExpediente; etiqueta: string }[];
  cargarEntidades: (tipo: TipoEntidadExpediente) => Promise<OpcionEntidad[]>;
}

export const configuracionExpedienteCultivos: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Cultivos',
  descripcion: 'Genera un PDF con el ciclo de vida del lote o ciclo de cosecha (labores, insumos, sanidad en parcela).',
  tiposEntidad: [
    { valor: 'LOTE', etiqueta: 'Lote / parcela' },
    { valor: 'COSECHA', etiqueta: 'Ciclo de cosecha (historial)' },
  ],
  cargarEntidades: async (tipo) => {
    if (tipo === 'LOTE') {
      const { data } = await api.get<{ id: number; nombre?: string }[]>(API_ENDPOINTS.LOTES.LISTAR_CULTIVO);
      return (data ?? []).map((l) => ({ id: l.id, etiqueta: l.nombre ?? `Lote ${l.id}` }));
    }
    const { data } = await api.get<
      { id: number; cultivoNombre?: string; loteNombre?: string; fechaSiembra?: string; fechaCosecha?: string }[]
    >(API_ENDPOINTS.COSECHAS.LISTAR);
    return (data ?? []).map((c) => ({
      id: c.id,
      etiqueta: `${c.cultivoNombre ?? 'Cultivo'} — ${c.loteNombre ?? 'Lote'}${c.fechaCosecha ? ` (${c.fechaCosecha})` : ''} (#${c.id})`,
    }));
  },
};

export const configuracionExpedientePorcinos: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Porcinos',
  descripcion: 'Documento PDF con origen, pesadas, alimentación, sanidad y venta de la recría o faena.',
  tiposEntidad: [
    { valor: 'RECRIA', etiqueta: 'Recría' },
    { valor: 'VENTA_PORCINO', etiqueta: 'Venta / faena' },
  ],
  cargarEntidades: async (tipo) => {
    if (tipo === 'RECRIA') {
      const { data } = await api.get<{ id: number; loteId?: number; cantidadAnimales?: number }[]>(
        API_ENDPOINTS.PORCINOS_RECRIA.LISTAR
      );
      return (data ?? []).map((r) => ({
        id: r.id,
        etiqueta: `Recría #${r.id} (${r.cantidadAnimales ?? '?'} animales, lote ${r.loteId ?? '-'})`,
      }));
    }
    const { data } = await api.get<{ id: number; fecha?: string; tipo?: string; cantidad?: number }[]>(
      API_ENDPOINTS.PORCINOS_VENTAS.LISTAR
    );
    return (data ?? []).map((v) => ({
      id: v.id,
      etiqueta: `${v.tipo ?? 'Venta'} ${v.fecha ?? ''} — ${v.cantidad ?? 0} cab. (#${v.id})`,
    }));
  },
};

export const configuracionExpedienteAvicolaHuevos: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Huevos',
  descripcion: 'Ciclo del lote de postura: producción, consumos y sanidad.',
  tiposEntidad: [{ valor: 'AVICOLA_HUEVOS', etiqueta: 'Lote de postura' }],
  cargarEntidades: async () => {
    const lotes = await listarLotesHuevos();
    return lotes.map((l) => ({ id: l.id, etiqueta: l.nombre ?? `Lote ${l.id}` }));
  },
};

export const configuracionExpedienteAvicolaCrianza: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Avícola crianza',
  descripcion: 'Lote parrillero: pesadas, consumos, mortalidad, ventas y sanidad.',
  tiposEntidad: [
    { valor: 'AVICOLA_CRIANZA', etiqueta: 'Lote parrillero' },
    { valor: 'AVICOLA_CARNE', etiqueta: 'Lote parrillero (histórico)' },
  ],
  cargarEntidades: async () => {
    const lotes = await listarLotesCrianza();
    return lotes.map((l) => ({ id: l.id, etiqueta: l.nombre ?? `Lote ${l.id}` }));
  },
};

export const configuracionExpedienteAvicolaPonedoras: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Ponedoras',
  descripcion: 'Galpón: postura, consumos, mortalidad, ventas de huevos y sanidad.',
  tiposEntidad: [{ valor: 'AVICOLA_PONEDORAS', etiqueta: 'Galpón' }],
  cargarEntidades: async () => {
    const galpones = await listarGalpones();
    return galpones.map((g) => ({ id: g.id, etiqueta: g.nombre ?? `Galpón ${g.id}` }));
  },
};

export const configuracionExpedienteFeedlot: ConfiguracionExpedienteModulo = {
  tituloPagina: 'Expediente de trazabilidad — Feedlot',
  descripcion: 'Ciclo del lote de engorde: pesadas, consumos, mortalidad, sanidad, ventas y closeout.',
  tiposEntidad: [{ valor: 'FEEDLOT_LOTE', etiqueta: 'Lote de engorde' }],
  cargarEntidades: async () => {
    const { listarLotes } = await import('../../modules/feedlot/services/feedlotApi');
    const lotes = await listarLotes({ delPeriodoActivo: false });
    return lotes.map((l) => ({ id: l.id, etiqueta: l.nombre ?? `Lote ${l.id}` }));
  },
};
