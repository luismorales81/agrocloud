import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';
import type { ReportData } from '../services/ExportService';

export interface SeccionGraficoExportacion {
  titulo: string;
  /** Contenedor del gráfico (ResponsiveContainer o Box padre). */
  elemento: HTMLElement;
}

function esperar(ms: number) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

/** Espera a que Recharts termine de pintar SVG antes de capturar. */
export async function esperarRenderizadoGraficos(ms = 450) {
  await esperar(ms);
  await new Promise<void>((resolve) => {
    requestAnimationFrame(() => requestAnimationFrame(() => resolve()));
  });
}

async function capturarElemento(elemento: HTMLElement): Promise<string> {
  const canvas = await html2canvas(elemento, {
    scale: 2,
    useCORS: true,
    logging: false,
    backgroundColor: '#ffffff',
    onclone: (doc) => {
      doc.querySelectorAll('svg').forEach((svg) => {
        const el = svg as SVGElement;
        if (!el.getAttribute('width')) {
          const box = svg.getBoundingClientRect();
          if (box.width > 0) el.setAttribute('width', String(Math.ceil(box.width)));
          if (box.height > 0) el.setAttribute('height', String(Math.ceil(box.height)));
        }
      });
    },
  });
  return canvas.toDataURL('image/png');
}

function descargarBlob(blob: Blob, nombreArchivo: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = nombreArchivo;
  link.style.visibility = 'hidden';
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function escaparHtml(texto: string) {
  return texto
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

function generarTablaHtml(reporte: ReportData): string {
  const cabeceras = reporte.columns.map((c) => `<th>${escaparHtml(c.label)}</th>`).join('');
  const filas = reporte.data
    .map((fila) => {
      const celdas = reporte.columns
        .map((col) => {
          const v = fila[col.key];
          const texto = v == null ? '' : String(v);
          return `<td>${escaparHtml(texto)}</td>`;
        })
        .join('');
      return `<tr>${celdas}</tr>`;
    })
    .join('');
  return `<table border="1" cellpadding="4" cellspacing="0"><thead><tr>${cabeceras}</tr></thead><tbody>${filas}</tbody></table>`;
}

/**
 * PDF con gráficos capturados + tablas (jsPDF). Si no hay gráficos, solo tablas.
 */
export async function exportarInformePdfConGraficos(
  titulo: string,
  nombreArchivo: string,
  reporte: ReportData,
  seccionesGraficos: SeccionGraficoExportacion[]
): Promise<void> {
  const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
  const anchoPagina = doc.internal.pageSize.getWidth();
  const margen = 14;
  const anchoUtil = anchoPagina - margen * 2;
  let y = margen;

  doc.setFontSize(14);
  doc.text(titulo, margen, y);
  y += 8;
  doc.setFontSize(9);
  doc.setTextColor(100);
  doc.text(`Generado: ${new Date().toLocaleString('es-AR')}`, margen, y);
  y += 10;
  doc.setTextColor(0);

  for (const seccion of seccionesGraficos) {
    const img = await capturarElemento(seccion.elemento);
    const props = doc.getImageProperties(img);
    const altoImg = (props.height * anchoUtil) / props.width;

    if (y + altoImg + 12 > doc.internal.pageSize.getHeight() - margen) {
      doc.addPage();
      y = margen;
    }

    doc.setFontSize(11);
    doc.text(seccion.titulo, margen, y);
    y += 5;
    doc.addImage(img, 'PNG', margen, y, anchoUtil, altoImg);
    y += altoImg + 8;
  }

  if (reporte.data.length > 0) {
    if (y > doc.internal.pageSize.getHeight() - 40) {
      doc.addPage();
      y = margen;
    }
    doc.setFontSize(11);
    doc.text('Datos tabulares', margen, y);
    y += 4;

    autoTable(doc, {
      startY: y,
      head: [reporte.columns.map((c) => c.label)],
      body: reporte.data.map((fila) => reporte.columns.map((col) => (fila[col.key] == null ? '' : String(fila[col.key])))),
      margin: { left: margen, right: margen },
      styles: { fontSize: 8 },
      headStyles: { fillColor: [30, 64, 175] },
    });
  }

  doc.save(nombreArchivo.endsWith('.pdf') ? nombreArchivo : `${nombreArchivo}.pdf`);
}

/**
 * Excel (.xls HTML): imagen de todos los gráficos apilados + tablas (abre en Excel con gráficos visibles).
 */
export async function exportarInformeExcelConGraficos(
  titulo: string,
  nombreArchivo: string,
  reporte: ReportData,
  seccionesGraficos: SeccionGraficoExportacion[]
): Promise<void> {
  const bloquesGraficos: string[] = [];
  for (const seccion of seccionesGraficos) {
    const img = await capturarElemento(seccion.elemento);
    bloquesGraficos.push(
      `<h3 style="font-family:Arial;margin:16px 0 8px;">${escaparHtml(seccion.titulo)}</h3>` +
        `<img src="${img}" style="max-width:100%;height:auto;display:block;margin-bottom:12px;" alt="${escaparHtml(seccion.titulo)}" />`
    );
  }

  const resumen =
    reporte.summary?.count != null
      ? `<p><strong>Registros en tabla:</strong> ${reporte.summary.count}</p>`
      : '';

  const html = `<!DOCTYPE html>
<html xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel">
<head>
<meta charset="UTF-8"/>
<!--[if gte mso 9]><xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>
<x:Name>Reporte</x:Name></x:ExcelWorksheet></x:ExcelWorksheets></x:ExcelWorkbook></xml><![endif]-->
</head>
<body style="font-family:Arial,sans-serif;padding:12px;">
<h1>${escaparHtml(titulo)}</h1>
<p style="color:#555;">Generado: ${escaparHtml(new Date().toLocaleString('es-AR'))}</p>
${resumen}
${bloquesGraficos.join('\n')}
<h2 style="margin-top:24px;">Datos tabulares</h2>
${generarTablaHtml(reporte)}
</body>
</html>`;

  const blob = new Blob(['\ufeff', html], { type: 'application/vnd.ms-excel;charset=utf-8' });
  descargarBlob(blob, nombreArchivo.endsWith('.xls') ? nombreArchivo : `${nombreArchivo}.xls`);
}
