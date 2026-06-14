import exportService, { ReportData } from '../services/ExportService';
import {
  exportarInformeExcelConGraficos,
  exportarInformePdfConGraficos,
  type SeccionGraficoExportacion,
} from './exportacionConGraficos';
import type {
  AvicolaHuevoProduccionDiariaRespuesta,
  AvicolaHuevosReporteAnalisisRespuesta,
} from '../modules/avicola-huevos/services/avicolaHuevosApi';
import type {
  AvicolaConsumoRespuesta,
  AvicolaEventoSanitarioRespuesta,
  AvicolaMuerteRespuesta,
  AvicolaPesadaRespuesta,
  AvicolaVentaRespuesta,
} from '../modules/avicola-crianza/services/avicolaCrianzaApi';
import type { Consumo, EventoSanitario, Muerte, Pesada, Venta } from '../modules/avicola-carne/types';
import type {
  Consumo as ConsumoPon,
  DescarteAves,
  EventoSanitario as EventoPon,
  Muerte as MuertePon,
  Postura,
  VentaHuevos,
} from '../modules/avicola-ponedoras/types';

export type FormatoExportacionAvicola = 'excel' | 'pdf';

const sufijoFecha = () => new Date().toISOString().slice(0, 10);

async function exportarReporte(reporte: ReportData, formato: FormatoExportacionAvicola, nombreArchivo: string) {
  const opciones = { format: formato, filename: nombreArchivo.replace(/\.(csv|xlsx|pdf|html)$/i, '') } as const;
  if (formato === 'excel') {
    await exportService.exportToExcel(reporte, opciones);
  } else {
    await exportService.exportToPDF(reporte, opciones);
  }
}

/** Reporte de análisis de postura (huevos): series de postura y de gastos en una sola tabla. */
/** Solo tablas (sin captura de gráficos). Preferir {@link exportarAnalisisPosturaHuevosConGraficos} desde reportes con gráficos. */
export async function exportarAnalisisPosturaHuevos(
  datos: AvicolaHuevosReporteAnalisisRespuesta,
  desde: string,
  hasta: string,
  formato: FormatoExportacionAvicola
): Promise<void> {
  const reporte = construirReporteAnalisisPosturaHuevos(datos, desde, hasta);
  const ext = formato === 'excel' ? 'xlsx' : 'pdf';
  await exportarReporte(reporte, formato, `reporte_postura_huevos_${sufijoFecha()}.${ext}`);
}

function construirReporteAnalisisPosturaHuevos(
  datos: AvicolaHuevosReporteAnalisisRespuesta,
  desde: string,
  hasta: string
): ReportData {
  const filasPostura = (datos.seriePostura ?? []).map((p) => ({
    seccion: 'Postura',
    fecha: p.fecha,
    huevosDia: p.totalHuevos ?? '',
    edadPlanteDias: p.diasEdadLote ?? '',
    huevosPorAve: p.huevosPorAve ?? '',
    temperatura: p.temperatura ?? '',
    humedad: p.humedad ?? '',
    costoEstimadoDia: '',
  }));
  const filasGasto = (datos.serieGastos ?? []).map((g) => ({
    seccion: 'Gasto consumos (estimado)',
    fecha: g.fecha,
    huevosDia: '',
    edadPlanteDias: '',
    huevosPorAve: '',
    temperatura: '',
    humedad: '',
    costoEstimadoDia: g.costoEstimado ?? '',
  }));
  return {
    title: `Avícola huevos — análisis postura — ${datos.nombreLote ?? 'Lote'} (${desde} a ${hasta})`,
    data: [...filasPostura, ...filasGasto],
    columns: [
      { key: 'seccion', label: 'Sección', type: 'text' },
      { key: 'fecha', label: 'Fecha', type: 'text' },
      { key: 'huevosDia', label: 'Huevos día', type: 'number' },
      { key: 'edadPlanteDias', label: 'Edad plantel (días)', type: 'number' },
      { key: 'huevosPorAve', label: 'Huevos/ave', type: 'number' },
      { key: 'temperatura', label: 'Temperatura °C', type: 'number' },
      { key: 'humedad', label: 'Humedad %', type: 'number' },
      { key: 'costoEstimadoDia', label: 'Costo día (consumos)', type: 'number' },
    ],
    summary: {
      count: filasPostura.length + filasGasto.length,
      total:
        datos.totalCostoConsumosRango != null && !Number.isNaN(Number(datos.totalCostoConsumosRango))
          ? Number(datos.totalCostoConsumosRango)
          : undefined,
    },
  };
}

/**
 * Exporta el informe de análisis de postura incluyendo capturas de los gráficos visibles en pantalla.
 */
export async function exportarAnalisisPosturaHuevosConGraficos(
  datos: AvicolaHuevosReporteAnalisisRespuesta,
  desde: string,
  hasta: string,
  formato: FormatoExportacionAvicola,
  seccionesGraficos: SeccionGraficoExportacion[]
): Promise<void> {
  const reporte = construirReporteAnalisisPosturaHuevos(datos, desde, hasta);
  const base = `reporte_postura_huevos_${sufijoFecha()}`;
  if (formato === 'pdf') {
    await exportarInformePdfConGraficos(reporte.title, `${base}.pdf`, reporte, seccionesGraficos);
  } else {
    await exportarInformeExcelConGraficos(reporte.title, `${base}.xls`, reporte, seccionesGraficos);
  }
}

/** Producción diaria de un lote de postura (huevos). */
export async function exportarProduccionDiariaHuevos(
  produccion: AvicolaHuevoProduccionDiariaRespuesta[],
  nombreLote: string,
  formato: FormatoExportacionAvicola
): Promise<void> {
  const data = produccion.map((p) => ({
    fecha: p.fecha,
    tam1: p.huevosTam1 ?? 0,
    tam2: p.huevosTam2 ?? 0,
    tam3: p.huevosTam3 ?? 0,
    tam4: p.huevosTam4 ?? 0,
    rotos: p.huevosRotos ?? 0,
    total: p.totalHuevosDia ?? p.cantidadHuevos ?? 0,
    temperatura: p.temperaturaDia ?? '',
    humedad: p.humedadDia ?? '',
    observaciones: p.observaciones ?? '',
  }));
  const reporte: ReportData = {
    title: `Avícola huevos — producción diaria — ${nombreLote}`,
    data,
    columns: [
      { key: 'fecha', label: 'Fecha', type: 'text' },
      { key: 'tam1', label: 'Tam. 1', type: 'number' },
      { key: 'tam2', label: 'Tam. 2', type: 'number' },
      { key: 'tam3', label: 'Tam. 3', type: 'number' },
      { key: 'tam4', label: 'Tam. 4', type: 'number' },
      { key: 'rotos', label: 'Rotos', type: 'number' },
      { key: 'total', label: 'Total huevos', type: 'number' },
      { key: 'temperatura', label: 'Temp. °C', type: 'number' },
      { key: 'humedad', label: 'Hum. %', type: 'number' },
      { key: 'observaciones', label: 'Observaciones', type: 'text' },
    ],
    summary: { count: data.length },
  };
  const ext = formato === 'excel' ? 'xlsx' : 'pdf';
  await exportarReporte(reporte, formato, `produccion_huevos_${nombreLote.replace(/\s+/g, '_')}_${sufijoFecha()}.${ext}`);
}

export interface DatosExportacionLoteCrianza {
  nombreLote: string;
  pesadas: AvicolaPesadaRespuesta[];
  muertes: AvicolaMuerteRespuesta[];
  ventas: AvicolaVentaRespuesta[];
  consumos: AvicolaConsumoRespuesta[];
  eventos: AvicolaEventoSanitarioRespuesta[];
  nombreInsumo?: (insumoId: number) => string;
}

export async function exportarOperacionesLoteCrianza(
  datos: DatosExportacionLoteCrianza,
  formato: FormatoExportacionAvicola
): Promise<void> {
  const ins = datos.nombreInsumo ?? ((id: number) => String(id));
  const seccion = (nombre: string, filas: Record<string, unknown>[]) =>
    filas.map((f) => ({ seccion: nombre, ...f }));
  const filas = [
    ...seccion(
      'Pesadas',
      datos.pesadas.map((p) => ({
        fecha: p.fecha,
        detalle: `Peso prom. ${p.pesoPromedio} kg; cant. pesada ${p.cantidadPesada ?? '—'}`,
        obs: p.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Mortalidad',
      datos.muertes.map((m) => ({
        fecha: m.fecha,
        detalle: `Cantidad ${m.cantidad}; causa ${m.causa ?? '—'}`,
        obs: m.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Ventas / faena',
      datos.ventas.map((v) => ({
        fecha: v.fecha,
        detalle: `${v.tipo} — ${v.cantidad} cab. — total ${v.total ?? '—'}`,
        obs: v.comprador ?? '',
      }))
    ),
    ...seccion(
      'Consumos',
      datos.consumos.map((c) => ({
        fecha: c.fecha,
        detalle: `${ins(c.insumoId)} — ${c.cantidad}`,
        obs: c.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Sanidad',
      datos.eventos.map((e) => ({
        fecha: e.fecha,
        detalle: `${e.tipo} — insumo ${e.insumoId != null ? ins(e.insumoId) : '—'} dosis ${e.dosis ?? '—'}`,
        obs: (e.descripcion ?? '') + (e.observaciones ? ` | ${e.observaciones}` : ''),
      }))
    ),
  ];
  const reporte: ReportData = {
    title: `Avícola crianza — operaciones del lote — ${datos.nombreLote}`,
    data: filas,
    columns: [
      { key: 'seccion', label: 'Sección', type: 'text' },
      { key: 'fecha', label: 'Fecha', type: 'text' },
      { key: 'detalle', label: 'Detalle', type: 'text' },
      { key: 'obs', label: 'Observaciones / extra', type: 'text' },
    ],
    summary: { count: filas.length },
  };
  const ext = formato === 'excel' ? 'xlsx' : 'pdf';
  await exportarReporte(reporte, formato, `avicola_crianza_lote_${sufijoFecha()}.${ext}`);
}

export interface DatosExportacionLoteCarne {
  nombreLote: string;
  pesadas: Pesada[];
  muertes: Muerte[];
  ventas: Venta[];
  consumos: Consumo[];
  eventos: EventoSanitario[];
  nombreInsumo?: (insumoId: number) => string;
}

export async function exportarOperacionesLoteCarne(
  datos: DatosExportacionLoteCarne,
  formato: FormatoExportacionAvicola
): Promise<void> {
  const ins = datos.nombreInsumo ?? ((id: number) => String(id));
  const seccion = (nombre: string, filas: Record<string, unknown>[]) =>
    filas.map((f) => ({ seccion: nombre, ...f }));
  const filas = [
    ...seccion(
      'Pesadas',
      datos.pesadas.map((p) => ({
        fecha: p.fecha,
        detalle: `Peso prom. ${p.pesoPromedio} kg; cant. ${p.cantidadPesada ?? '—'}`,
        obs: p.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Mortalidad',
      datos.muertes.map((m) => ({
        fecha: m.fecha,
        detalle: `Cantidad ${m.cantidad}; causa ${m.causa ?? '—'}`,
        obs: m.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Ventas / faena',
      datos.ventas.map((v) => ({
        fecha: v.fecha,
        detalle: `${v.tipo} — ${v.cantidad} — total ${v.total ?? '—'}`,
        obs: v.comprador ?? '',
      }))
    ),
    ...seccion(
      'Consumos',
      datos.consumos.map((c) => ({
        fecha: c.fecha,
        detalle: `${ins(c.insumoId)} — ${c.cantidad}`,
        obs: c.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Sanidad',
      datos.eventos.map((e) => ({
        fecha: e.fecha,
        detalle: `${e.tipo} — insumo ${e.insumoId != null ? ins(e.insumoId) : '—'} — dosis ${e.dosis ?? '—'}`,
        obs: (e.descripcion ?? '') + (e.observaciones ? ` | ${e.observaciones}` : ''),
      }))
    ),
  ];
  const reporte: ReportData = {
    title: `Avícola carne — operaciones del lote — ${datos.nombreLote}`,
    data: filas,
    columns: [
      { key: 'seccion', label: 'Sección', type: 'text' },
      { key: 'fecha', label: 'Fecha', type: 'text' },
      { key: 'detalle', label: 'Detalle', type: 'text' },
      { key: 'obs', label: 'Observaciones / extra', type: 'text' },
    ],
    summary: { count: filas.length },
  };
  const ext = formato === 'excel' ? 'xlsx' : 'pdf';
  await exportarReporte(reporte, formato, `avicola_carne_lote_${sufijoFecha()}.${ext}`);
}

export interface DatosExportacionGalponPonedoras {
  nombreGalpon: string;
  posturas: Postura[];
  muertes: MuertePon[];
  consumos: ConsumoPon[];
  eventos: EventoPon[];
  ventas: VentaHuevos[];
  descartes: DescarteAves[];
  nombreInsumo?: (insumoId: number) => string;
}

export async function exportarOperacionesGalponPonedoras(
  datos: DatosExportacionGalponPonedoras,
  formato: FormatoExportacionAvicola
): Promise<void> {
  const ins = datos.nombreInsumo ?? ((id: number) => String(id));
  const seccion = (nombre: string, filas: Record<string, unknown>[]) =>
    filas.map((f) => ({ seccion: nombre, ...f }));
  const filas = [
    ...seccion(
      'Postura (huevos por categoría)',
      datos.posturas.map((p) => ({
        fecha: p.fecha,
        detalle: `Categoría ${p.categoriaHuevo} — ${p.cantidad} huevos`,
        obs: p.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Mortalidad',
      datos.muertes.map((m) => ({
        fecha: m.fecha,
        detalle: `Cantidad ${m.cantidad}; causa ${m.causa ?? '—'}`,
        obs: m.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Consumos',
      datos.consumos.map((c) => ({
        fecha: c.fecha,
        detalle: `${ins(c.insumoId)} — ${c.cantidad}`,
        obs: c.observaciones ?? '',
      }))
    ),
    ...seccion(
      'Sanidad',
      datos.eventos.map((e) => ({
        fecha: e.fecha,
        detalle: `${e.tipo ?? '—'} — insumo ${e.insumoId != null ? ins(e.insumoId) : '—'} — dosis ${e.dosis ?? '—'}`,
        obs: (e.descripcion ?? '') + (e.observaciones ? ` | ${e.observaciones}` : ''),
      }))
    ),
    ...seccion(
      'Ventas de huevos',
      datos.ventas.map((v) => ({
        fecha: v.fecha,
        detalle: `Cat. ${v.categoriaHuevo} — ${v.cantidad} huevos; total ${v.total ?? '—'}`,
        obs: (v.comprador ?? '') + (v.observaciones ? ` | ${v.observaciones}` : ''),
      }))
    ),
    ...seccion(
      'Descarte de aves',
      datos.descartes.map((d) => ({
        fecha: d.fecha,
        detalle: `${d.motivo} — ${d.cantidad} aves`,
        obs: d.observaciones ?? '',
      }))
    ),
  ];
  const reporte: ReportData = {
    title: `Avícola ponedoras — galpón — ${datos.nombreGalpon}`,
    data: filas,
    columns: [
      { key: 'seccion', label: 'Sección', type: 'text' },
      { key: 'fecha', label: 'Fecha', type: 'text' },
      { key: 'detalle', label: 'Detalle', type: 'text' },
      { key: 'obs', label: 'Observaciones / extra', type: 'text' },
    ],
    summary: { count: filas.length },
  };
  const ext = formato === 'excel' ? 'xlsx' : 'pdf';
  await exportarReporte(reporte, formato, `avicola_ponedoras_galpon_${sufijoFecha()}.${ext}`);
}
