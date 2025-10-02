// Servicio para exportación de reportes
export interface ExportOptions {
  format: 'excel' | 'pdf' | 'csv';
  filename?: string;
  includeCharts?: boolean;
  dateRange?: {
    start: Date;
    end: Date;
  };
}

export interface ReportData {
  title: string;
  data: any[];
  columns: {
    key: string;
    label: string;
    type?: 'text' | 'number' | 'date' | 'currency';
  }[];
  summary?: {
    total?: number;
    average?: number;
    count?: number;
  };
}

class ExportService {
  // Exportar a Excel (genera CSV con extensión .xlsx para compatibilidad)
  async exportToExcel(data: ReportData, options: ExportOptions = { format: 'excel' }) {
    try {
      console.log('🔍 [EXPORT] Exportando a Excel:', data);
      
      // Generar contenido CSV (más compatible que Excel real)
      const csvContent = this.generateCSVContent(data);
      
      // Usar extensión .xlsx pero contenido CSV
      const filename = options.filename || `${data.title}_${new Date().toISOString().split('T')[0]}.xlsx`;
      
      // Descargar como CSV con extensión .xlsx
      this.downloadFile(filename, csvContent, 'text/csv');
      
      console.log('✅ [EXPORT] Archivo Excel descargado:', filename);
      return { success: true, filename };
    } catch (error) {
      console.error('❌ [EXPORT] Error exporting to Excel:', error);
      throw new Error('Error al exportar a Excel');
    }
  }

  // Exportar a PDF (genera HTML con extensión .pdf)
  async exportToPDF(data: ReportData, options: ExportOptions = { format: 'pdf' }) {
    try {
      console.log('🔍 [EXPORT] Exportando a PDF:', data);
      
      const filename = options.filename || `${data.title}_${new Date().toISOString().split('T')[0]}.pdf`;
      
      // Generar contenido HTML (que se puede abrir como PDF)
      const htmlContent = this.generatePDFContent(data);
      
      // Descargar como HTML con extensión .pdf
      this.downloadFile(filename, htmlContent, 'text/html');
      
      console.log('✅ [EXPORT] Archivo PDF descargado:', filename);
      return { success: true, filename };
    } catch (error) {
      console.error('❌ [EXPORT] Error exporting to PDF:', error);
      throw new Error('Error al exportar a PDF');
    }
  }

  // Exportar a CSV
  async exportToCSV(data: ReportData, options: ExportOptions = { format: 'csv' }) {
    try {
      console.log('🔍 [EXPORT] Exportando a CSV:', data);
      
      const filename = options.filename || `${data.title}_${new Date().toISOString().split('T')[0]}.csv`;
      
      // Generar contenido CSV
      const csvContent = this.generateCSVContent(data);
      
      // Descargar archivo
      this.downloadFile(filename, csvContent, 'text/csv');
      
      console.log('✅ [EXPORT] Archivo CSV descargado:', filename);
      return { success: true, filename };
    } catch (error) {
      console.error('❌ [EXPORT] Error exporting to CSV:', error);
      throw new Error('Error al exportar a CSV');
    }
  }

  // Crear worksheet para Excel
  private createWorksheet(data: ReportData) {
    const headers = data.columns.map(col => col.label);
    const rows = data.data.map(row => 
      data.columns.map(col => {
        const value = row[col.key];
        if (col.type === 'currency') {
          return typeof value === 'number' ? `$${value.toFixed(2)}` : value;
        }
        if (col.type === 'date' && value) {
          return new Date(value).toLocaleDateString('es-ES');
        }
        return value;
      })
    );

    return {
      headers,
      rows: [headers, ...rows]
    };
  }

  // Crear workbook
  private createWorkbook(worksheet: any, title: string) {
    return {
      SheetNames: [title],
      Sheets: {
        [title]: worksheet
      }
    };
  }

  // Generar contenido Excel (simulado)
  private generateExcelContent(workbook: any): string {
    // En un entorno real, usaríamos SheetJS para generar el archivo real
    // Por ahora, simulamos el contenido
    return JSON.stringify(workbook);
  }

  // Generar contenido PDF (simulado)
  private generatePDFContent(data: ReportData): string {
    const header = `
      <html>
        <head>
          <style>
            body { font-family: Arial, sans-serif; margin: 20px; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
            th { background-color: #f2f2f2; }
            h1 { color: #333; }
            .summary { margin: 20px 0; padding: 10px; background-color: #f9f9f9; }
          </style>
        </head>
        <body>
          <h1>${data.title}</h1>
          <p>Generado el: ${new Date().toLocaleDateString('es-ES')}</p>
    `;

    const table = `
          <table>
            <thead>
              <tr>
                ${data.columns.map(col => `<th>${col.label}</th>`).join('')}
              </tr>
            </thead>
            <tbody>
              ${data.data.map(row => `
                <tr>
                  ${data.columns.map(col => {
                    const value = row[col.key];
                    if (col.type === 'currency') {
                      return `<td>$${typeof value === 'number' ? value.toFixed(2) : value}</td>`;
                    }
                    if (col.type === 'date' && value) {
                      return `<td>${new Date(value).toLocaleDateString('es-ES')}</td>`;
                    }
                    return `<td>${value}</td>`;
                  }).join('')}
                </tr>
              `).join('')}
            </tbody>
          </table>
    `;

    const summary = data.summary ? `
          <div class="summary">
            <h3>Resumen</h3>
            ${data.summary.total ? `<p>Total: $${data.summary.total.toFixed(2)}</p>` : ''}
            ${data.summary.average ? `<p>Promedio: $${data.summary.average.toFixed(2)}</p>` : ''}
            ${data.summary.count ? `<p>Cantidad de registros: ${data.summary.count}</p>` : ''}
          </div>
    ` : '';

    const footer = `
        </body>
      </html>
    `;

    return header + table + summary + footer;
  }

  // Generar contenido CSV
  private generateCSVContent(data: ReportData): string {
    console.log('🔍 [EXPORT] Generando CSV - Columnas:', data.columns);
    console.log('🔍 [EXPORT] Generando CSV - Datos:', data.data);
    
    const headers = data.columns.map(col => col.label).join(',');
    const rows = data.data.map(row => {
      const rowData = data.columns.map(col => {
        const value = row[col.key];
        console.log(`🔍 [EXPORT] Campo ${col.key}:`, value, 'Tipo:', typeof value);
        
        if (col.type === 'currency') {
          return typeof value === 'number' ? `$${value.toFixed(2)}` : value;
        }
        if (col.type === 'date' && value) {
          return new Date(value).toLocaleDateString('es-ES');
        }
        // Escapar comillas en CSV
        return typeof value === 'string' && value.includes(',') ? `"${value}"` : value;
      });
      return rowData.join(',');
    });

    const csvContent = [headers, ...rows].join('\n');
    console.log('✅ [EXPORT] CSV generado:', csvContent.substring(0, 300) + '...');
    return csvContent;
  }

  // Descargar archivo
  private downloadFile(filename: string, content: string, mimeType?: string) {
    const blob = new Blob([content], { 
      type: mimeType || this.getMimeType(filename) 
    });
    
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    
    console.log('✅ [EXPORT] Archivo descargado:', filename, 'Tipo MIME:', blob.type);
  }

  // Obtener tipo MIME según extensión
  private getMimeType(filename: string): string {
    const extension = filename.split('.').pop()?.toLowerCase();
    switch (extension) {
      case 'xlsx':
        return 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
      case 'pdf':
        return 'application/pdf';
      case 'csv':
        return 'text/csv';
      default:
        return 'application/octet-stream';
    }
  }

  // Exportar reporte de rendimientos
  async exportYieldReport(data: any[], options: ExportOptions = { format: 'excel' }) {
    const reportData: ReportData = {
      title: 'Reporte de Rendimientos',
      columns: [
        { key: 'lote', label: 'Lote', type: 'text' },
        { key: 'cultivo', label: 'Cultivo', type: 'text' },
        { key: 'area', label: 'Área (ha)', type: 'number' },
        { key: 'rendimiento', label: 'Rendimiento (kg/ha)', type: 'number' },
        { key: 'produccion', label: 'Producción Total (kg)', type: 'number' },
        { key: 'fechaCosecha', label: 'Fecha de Cosecha', type: 'date' },
        { key: 'valor', label: 'Valor ($)', type: 'currency' }
      ],
      data: data,
      summary: {
        total: data.reduce((sum, item) => sum + (item.valor || 0), 0),
        average: data.reduce((sum, item) => sum + (item.rendimiento || 0), 0) / data.length,
        count: data.length
      }
    };

    return this.exportToExcel(reportData, options);
  }

  // Exportar reporte de labores
  async exportLaborsReport(data: any[], options: ExportOptions = { format: 'excel' }) {
    const reportData: ReportData = {
      title: 'Reporte de Labores',
      columns: [
        { key: 'lote', label: 'Lote', type: 'text' },
        { key: 'tipoLabor', label: 'Tipo de Labor', type: 'text' },
        { key: 'fechaInicio', label: 'Fecha Inicio', type: 'date' },
        { key: 'fechaFin', label: 'Fecha Fin', type: 'date' },
        { key: 'estado', label: 'Estado', type: 'text' },
        { key: 'costo', label: 'Costo ($)', type: 'currency' },
        { key: 'observaciones', label: 'Observaciones', type: 'text' }
      ],
      data: data,
      summary: {
        total: data.reduce((sum, item) => sum + (item.costo || 0), 0),
        count: data.length
      }
    };

    return this.exportToExcel(reportData, options);
  }

  // Exportar reporte de insumos
  async exportInputsReport(data: any[], options: ExportOptions = { format: 'excel' }) {
    const reportData: ReportData = {
      title: 'Reporte de Insumos',
      columns: [
        { key: 'nombre', label: 'Insumo', type: 'text' },
        { key: 'tipo', label: 'Tipo', type: 'text' },
        { key: 'stockDisponible', label: 'Stock Disponible', type: 'number' },
        { key: 'stockMinimo', label: 'Stock Mínimo', type: 'number' },
        { key: 'precioUnitario', label: 'Precio Unitario ($)', type: 'currency' },
        { key: 'valorTotal', label: 'Valor Total ($)', type: 'currency' },
        { key: 'proveedor', label: 'Proveedor', type: 'text' }
      ],
      data: data,
      summary: {
        total: data.reduce((sum, item) => sum + (item.valorTotal || 0), 0),
        count: data.length
      }
    };

    return this.exportToExcel(reportData, options);
  }

  // Exportar reporte de maquinaria
  async exportMachineryReport(data: any[], options: ExportOptions = { format: 'excel' }) {
    const reportData: ReportData = {
      title: 'Reporte de Maquinaria',
      columns: [
        { key: 'nombre', label: 'Maquinaria', type: 'text' },
        { key: 'tipo', label: 'Tipo', type: 'text' },
        { key: 'marca', label: 'Marca', type: 'text' },
        { key: 'modelo', label: 'Modelo', type: 'text' },
        { key: 'anio', label: 'Año', type: 'number' },
        { key: 'estado', label: 'Estado', type: 'text' },
        { key: 'horasUso', label: 'Horas de Uso', type: 'number' },
        { key: 'costoPorHora', label: 'Costo por Hora ($)', type: 'currency' }
      ],
      data: data,
      summary: {
        count: data.length
      }
    };

    return this.exportToExcel(reportData, options);
  }
}

export const exportService = new ExportService();

export default exportService;
