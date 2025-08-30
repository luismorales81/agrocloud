import React, { useState } from 'react';
import { useCurrency } from '../hooks/useCurrency';
import { exportService } from '../services/ExportService';
import type { ExportOptions } from '../services/ExportService';

// interface ReportData {
//   id: number;
//   tipo: string;
//   titulo: string;
//   descripcion: string;
//   fechaGeneracion: string;
//   datos: any;
// }

interface RindeData {
  lote: string;
  cultivo: string;
  superficie: number;
  rindeReal: number;
  rindeEsperado: number;
  cumplimiento: number;
  fechaCosecha: string;
}

interface ProduccionData {
  cultivo: string;
  superficieTotal: number;
  produccionTotal: number;
  rindePromedio: number;
  cantidadLotes: number;
}

const ReportsManagement: React.FC = () => {
  const { formatCurrency } = useCurrency();
  const [activeReport, setActiveReport] = useState<string>('rindes');
  const [dateRange, setDateRange] = useState({ inicio: '', fin: '' });
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState<any>(null);

  // Datos mock para los reportes
  const mockRindeData: RindeData[] = [
    { lote: 'Lote A1', cultivo: 'Soja', superficie: 25.5, rindeReal: 3500, rindeEsperado: 3500, cumplimiento: 100, fechaCosecha: '2024-07-15' },
    { lote: 'Lote A2', cultivo: 'Maíz', superficie: 30.25, rindeReal: 12500, rindeEsperado: 12500, cumplimiento: 100, fechaCosecha: '2024-07-20' },
    { lote: 'Lote B1', cultivo: 'Trigo', superficie: 40.0, rindeReal: 4500, rindeEsperado: 4500, cumplimiento: 100, fechaCosecha: '2024-11-15' },
    { lote: 'Lote B2', cultivo: 'Soja', superficie: 35.75, rindeReal: 3745, rindeEsperado: 3800, cumplimiento: 98.55, fechaCosecha: '2024-07-25' }
  ];

  const mockProduccionData: ProduccionData[] = [
    { cultivo: 'Soja', superficieTotal: 61.25, produccionTotal: 214125, rindePromedio: 3622.5, cantidadLotes: 2 },
    { cultivo: 'Maíz', superficieTotal: 30.25, produccionTotal: 378125, rindePromedio: 12500, cantidadLotes: 1 },
    { cultivo: 'Trigo', superficieTotal: 40.0, produccionTotal: 180000, rindePromedio: 4500, cantidadLotes: 1 }
  ];

  const generateReport = async (tipo: string) => {
    setLoading(true);
    
    // Simulación de generación de reporte
    setTimeout(() => {
      let data;
      switch (tipo) {
        case 'rindes':
          data = {
            titulo: 'Reporte de Rindes por Lote',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: mockRindeData,
            estadisticas: {
              promedioRinde: mockRindeData.reduce((sum, item) => sum + item.rindeReal, 0) / mockRindeData.length,
              mejorRinde: Math.max(...mockRindeData.map(item => item.rindeReal)),
              peorRinde: Math.min(...mockRindeData.map(item => item.rindeReal)),
              promedioCumplimiento: mockRindeData.reduce((sum, item) => sum + item.cumplimiento, 0) / mockRindeData.length
            }
          };
          break;
        case 'produccion':
          data = {
            titulo: 'Reporte de Producción por Cultivo',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: mockProduccionData,
            estadisticas: {
              superficieTotal: mockProduccionData.reduce((sum, item) => sum + item.superficieTotal, 0),
              produccionTotal: mockProduccionData.reduce((sum, item) => sum + item.produccionTotal, 0),
              cultivosActivos: mockProduccionData.length
            }
          };
          break;
        case 'costos':
          data = {
            titulo: 'Reporte de Costos y Rentabilidad',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: [
              { cultivo: 'Soja', costoHectarea: 850, precioVenta: 450, rentabilidad: 89.4 },
              { cultivo: 'Maíz', costoHectarea: 1200, precioVenta: 180, rentabilidad: 50.0 },
              { cultivo: 'Trigo', costoHectarea: 950, precioVenta: 380, rentabilidad: 300.0 }
            ]
          };
          break;
        default:
          data = null;
      }
      
      setReportData(data);
      setLoading(false);
    }, 1000);
  };

  const exportReport = async (format: 'excel' | 'pdf' | 'csv') => {
    if (!reportData) return;
    
    try {
      const options: ExportOptions = {
        format,
        filename: `${reportData.titulo}_${new Date().toISOString().split('T')[0]}`,
        dateRange: dateRange.inicio && dateRange.fin ? {
          start: new Date(dateRange.inicio),
          end: new Date(dateRange.fin)
        } : undefined
      };

      let result;
      switch (format) {
        case 'excel':
          result = await exportService.exportToExcel({
            title: reportData.titulo,
            data: reportData.datos,
            columns: getColumnsForReport(activeReport),
            summary: getSummaryForReport(reportData)
          }, options);
          break;
        case 'pdf':
          result = await exportService.exportToPDF({
            title: reportData.titulo,
            data: reportData.datos,
            columns: getColumnsForReport(activeReport),
            summary: getSummaryForReport(reportData)
          }, options);
          break;
        case 'csv':
          result = await exportService.exportToCSV({
            title: reportData.titulo,
            data: reportData.datos,
            columns: getColumnsForReport(activeReport),
            summary: getSummaryForReport(reportData)
          }, options);
          break;
      }
      
      alert(`Reporte exportado exitosamente: ${result?.filename}`);
    } catch (error) {
      console.error('Error exporting report:', error);
      alert('Error al exportar el reporte');
    }
  };

  const getColumnsForReport = (reportType: string) => {
    switch (reportType) {
      case 'rindes':
        return [
          { key: 'lote', label: 'Lote', type: 'text' as const },
          { key: 'cultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'superficie', label: 'Superficie (ha)', type: 'number' as const },
          { key: 'rindeReal', label: 'Rinde Real (kg/ha)', type: 'number' as const },
          { key: 'rindeEsperado', label: 'Rinde Esperado (kg/ha)', type: 'number' as const },
          { key: 'cumplimiento', label: 'Cumplimiento (%)', type: 'number' as const },
          { key: 'fechaCosecha', label: 'Fecha de Cosecha', type: 'date' as const }
        ];
      case 'produccion':
        return [
          { key: 'cultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'superficieTotal', label: 'Superficie Total (ha)', type: 'number' as const },
          { key: 'produccionTotal', label: 'Producción Total (kg)', type: 'number' as const },
          { key: 'rindePromedio', label: 'Rinde Promedio (kg/ha)', type: 'number' as const },
          { key: 'cantidadLotes', label: 'Cantidad de Lotes', type: 'number' as const }
        ];
      case 'costos':
        return [
          { key: 'cultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'costoHectarea', label: 'Costo por Hectárea ($)', type: 'currency' as const },
          { key: 'precioVenta', label: 'Precio de Venta ($/kg)', type: 'currency' as const },
          { key: 'rentabilidad', label: 'Rentabilidad (%)', type: 'number' as const }
        ];
      default:
        return [];
    }
  };

  const getSummaryForReport = (data: any) => {
    if (!data || !data.datos) return undefined;
    
    switch (activeReport) {
      case 'rindes':
        return {
          total: data.datos.reduce((sum: number, item: any) => sum + (item.rindeReal * item.superficie), 0),
          average: data.estadisticas?.promedioRinde,
          count: data.datos.length
        };
      case 'produccion':
        return {
          total: data.estadisticas?.produccionTotal,
          count: data.datos.length
        };
      case 'costos':
        return {
          total: data.datos.reduce((sum: number, item: any) => sum + item.costoHectarea, 0),
          count: data.datos.length
        };
      default:
        return undefined;
    }
  };

  const exportToExcel = (data: any, filename: string) => {
    // Simulación de exportación a Excel
    const csvContent = [
      Object.keys(data.datos[0]).join(','),
      ...data.datos.map((row: any) => Object.values(row).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', `${filename}_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const renderRindeReport = () => (
    <div>
      <div style={{ 
        background: '#fef3c7', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #f59e0b'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#92400e' }}>📊 Estadísticas de Rindes</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(reportData.estadisticas.promedioRinde)} kg/ha
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Rinde Promedio</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(reportData.estadisticas.mejorRinde)} kg/ha
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Mejor Rinde</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
              {Math.round(reportData.estadisticas.promedioCumplimiento)}%
            </div>
            <div style={{ fontSize: '14px', color: '#92400e' }}>Cumplimiento Promedio</div>
          </div>
        </div>
      </div>

      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6',
          fontWeight: 'bold'
        }}>
          📋 Detalle de Rindes por Lote
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ 
            width: '100%', 
            borderCollapse: 'collapse',
            fontSize: '14px'
          }}>
            <thead>
              <tr style={{ background: '#f8f9fa' }}>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Superficie (ha)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Real (kg/ha)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Esperado (kg/ha)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cumplimiento (%)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha Cosecha</th>
              </tr>
            </thead>
            <tbody>
              {reportData.datos.map((item: RindeData, index: number) => (
                <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                  <td style={{ padding: '12px' }}><strong>{item.lote}</strong></td>
                  <td style={{ padding: '12px' }}>{item.cultivo}</td>
                  <td style={{ padding: '12px' }}>{item.superficie}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                      {item.rindeReal}
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>{item.rindeEsperado}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      background: item.cumplimiento >= 100 ? '#dcfce7' : '#fef3c7',
                      color: item.cumplimiento >= 100 ? '#166534' : '#92400e'
                    }}>
                      {item.cumplimiento}%
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>
                    {new Date(item.fechaCosecha).toLocaleDateString('es-ES')}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  const renderProduccionReport = () => (
    <div>
      <div style={{ 
        background: '#ecfdf5', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #10b981'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#065f46' }}>🌾 Resumen de Producción</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#065f46' }}>
              {reportData.estadisticas.superficieTotal} ha
            </div>
            <div style={{ fontSize: '14px', color: '#065f46' }}>Superficie Total</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#065f46' }}>
              {Math.round(reportData.estadisticas.produccionTotal / 1000)} tn
            </div>
            <div style={{ fontSize: '14px', color: '#065f46' }}>Producción Total</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#065f46' }}>
              {reportData.estadisticas.cultivosActivos}
            </div>
            <div style={{ fontSize: '14px', color: '#065f46' }}>Cultivos Activos</div>
          </div>
        </div>
      </div>

      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6',
          fontWeight: 'bold'
        }}>
          📊 Producción por Cultivo
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ 
            width: '100%', 
            borderCollapse: 'collapse',
            fontSize: '14px'
          }}>
            <thead>
              <tr style={{ background: '#f8f9fa' }}>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Superficie (ha)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Producción (kg)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Promedio (kg/ha)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad de Lotes</th>
              </tr>
            </thead>
            <tbody>
              {reportData.datos.map((item: ProduccionData, index: number) => (
                <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                  <td style={{ padding: '12px' }}><strong>{item.cultivo}</strong></td>
                  <td style={{ padding: '12px' }}>{item.superficieTotal}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                      {Math.round(item.produccionTotal / 1000)} tn
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>{item.rindePromedio}</td>
                  <td style={{ padding: '12px' }}>{item.cantidadLotes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  const renderCostosReport = () => (
    <div>
      <div style={{ 
        background: '#fef2f2', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #ef4444'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#991b1b' }}>💰 Análisis de Rentabilidad</h3>
        <p style={{ color: '#991b1b', margin: '0' }}>
          Análisis de costos por hectárea y rentabilidad por cultivo
        </p>
      </div>

      <div style={{ 
        background: 'white', 
        borderRadius: '10px', 
        overflow: 'hidden',
        boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
      }}>
        <div style={{ 
          background: '#f8f9fa', 
          padding: '15px', 
          borderBottom: '1px solid #dee2e6',
          fontWeight: 'bold'
        }}>
          💰 Costos y Rentabilidad por Cultivo
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ 
            width: '100%', 
            borderCollapse: 'collapse',
            fontSize: '14px'
          }}>
            <thead>
              <tr style={{ background: '#f8f9fa' }}>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cultivo</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Costo/ha ($)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Precio Venta ($/tn)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rentabilidad (%)</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
              </tr>
            </thead>
            <tbody>
              {reportData.datos.map((item: any, index: number) => (
                <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                  <td style={{ padding: '12px' }}><strong>{item.cultivo}</strong></td>
                  <td style={{ padding: '12px' }}>{formatCurrency(item.costoHectarea)}</td>
                  <td style={{ padding: '12px' }}>{formatCurrency(item.precioVenta)}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      background: item.rentabilidad > 50 ? '#dcfce7' : '#fef3c7',
                      color: item.rentabilidad > 50 ? '#166534' : '#92400e'
                    }}>
                      {item.rentabilidad}%
                    </span>
                  </td>
                  <td style={{ padding: '12px' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      background: item.rentabilidad > 50 ? '#dcfce7' : '#fef3c7',
                      color: item.rentabilidad > 50 ? '#166534' : '#92400e'
                    }}>
                      {item.rentabilidad > 50 ? 'Rentable' : 'Marginal'}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <div style={{ 
          display: 'flex', 
          alignItems: 'center', 
          gap: '15px',
          marginBottom: '10px'
        }}>
          <div style={{ 
            width: '48px', 
            height: '48px',
            background: 'white',
            borderRadius: '50%',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            padding: '6px'
          }}>
            <img 
              src="/icons/agrocloud-logo.svg" 
              alt="AgroCloud Logo" 
              style={{ width: '100%', height: '100%' }}
            />
          </div>
          <div>
            <h1 style={{ margin: '0 0 5px 0', fontSize: '24px' }}>📈 Sistema de Reportes</h1>
            <p style={{ margin: '0', opacity: '0.9' }}>
              Genera y analiza reportes detallados de tu producción agrícola
            </p>
          </div>
        </div>
      </div>

      {/* Selector de reportes */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '15px' }}>
          <button
            onClick={() => setActiveReport('rindes')}
            style={{
              background: activeReport === 'rindes' ? '#f59e0b' : '#e5e7eb',
              color: activeReport === 'rindes' ? 'white' : '#374151',
              border: 'none',
              padding: '12px 20px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            📊 Rindes
          </button>
          <button
            onClick={() => setActiveReport('produccion')}
            style={{
              background: activeReport === 'produccion' ? '#10b981' : '#e5e7eb',
              color: activeReport === 'produccion' ? 'white' : '#374151',
              border: 'none',
              padding: '12px 20px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            🌾 Producción
          </button>
          <button
            onClick={() => setActiveReport('costos')}
            style={{
              background: activeReport === 'costos' ? '#ef4444' : '#e5e7eb',
              color: activeReport === 'costos' ? 'white' : '#374151',
              border: 'none',
              padding: '12px 20px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            💰 Costos
          </button>
        </div>

        {/* Filtros de fecha */}
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha Inicio:</label>
            <input
              type="date"
              value={dateRange.inicio}
              onChange={(e) => setDateRange(prev => ({ ...prev, inicio: e.target.value }))}
              style={{
                padding: '8px 12px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha Fin:</label>
            <input
              type="date"
              value={dateRange.fin}
              onChange={(e) => setDateRange(prev => ({ ...prev, fin: e.target.value }))}
              style={{
                padding: '8px 12px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>
          <button
            onClick={() => generateReport(activeReport)}
            disabled={loading}
            style={{
              background: '#3b82f6',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '5px',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '14px',
              fontWeight: 'bold',
              marginTop: '20px',
              opacity: loading ? 0.6 : 1
            }}
          >
            {loading ? '🔄 Generando...' : '📊 Generar Reporte'}
          </button>
        </div>

        {/* Botones de exportación */}
        {reportData && (
          <div style={{ 
            display: 'flex', 
            gap: '10px', 
            alignItems: 'center', 
            flexWrap: 'wrap',
            marginTop: '15px',
            padding: '15px',
            background: '#f8f9fa',
            borderRadius: '8px',
            border: '1px solid #e9ecef'
          }}>
            <span style={{ fontWeight: 'bold', color: '#495057' }}>📤 Exportar:</span>
            <button
              onClick={() => exportReport('excel')}
              style={{
                background: '#28a745',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: 'bold'
              }}
            >
              📊 Excel
            </button>
            <button
              onClick={() => exportReport('pdf')}
              style={{
                background: '#dc3545',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: 'bold'
              }}
            >
              📄 PDF
            </button>
            <button
              onClick={() => exportReport('csv')}
              style={{
                background: '#6c757d',
                color: 'white',
                border: 'none',
                padding: '8px 16px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '13px',
                fontWeight: 'bold'
              }}
            >
              📋 CSV
            </button>
          </div>
        )}
      </div>

      {/* Contenido del reporte */}
      {loading && (
        <div style={{ 
          textAlign: 'center', 
          padding: '40px', 
          background: 'white', 
          borderRadius: '10px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
        }}>
          <div style={{ fontSize: '24px', marginBottom: '10px' }}>🔄</div>
          <p style={{ color: '#666', margin: '0' }}>Generando reporte...</p>
        </div>
      )}

      {!loading && reportData && (
        <div>
          {/* Header del reporte */}
          <div style={{ 
            background: 'white', 
            padding: '20px', 
            borderRadius: '10px', 
            marginBottom: '20px',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: '15px'
          }}>
            <div style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '12px'
            }}>
              <div style={{ 
                width: '40px', 
                height: '40px',
                background: 'white',
                borderRadius: '50%',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                flexShrink: 0,
                padding: '4px'
              }}>
                <img 
                  src="/icons/agrocloud-logo.svg" 
                  alt="AgroCloud Logo" 
                  style={{ width: '100%', height: '100%' }}
                />
              </div>
              <div>
                <h2 style={{ margin: '0 0 5px 0', color: '#111827' }}>{reportData.titulo}</h2>
                <p style={{ margin: '0', color: '#6b7280', fontSize: '14px' }}>
                  Generado el {reportData.fechaGeneracion}
                </p>
              </div>
            </div>
            <button
              onClick={() => exportToExcel(reportData, activeReport)}
              style={{
                background: '#10b981',
                color: 'white',
                border: 'none',
                padding: '10px 20px',
                borderRadius: '5px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              📥 Exportar Excel
            </button>
          </div>

          {/* Contenido específico del reporte */}
          {activeReport === 'rindes' && renderRindeReport()}
          {activeReport === 'produccion' && renderProduccionReport()}
          {activeReport === 'costos' && renderCostosReport()}
        </div>
      )}

      {!loading && !reportData && (
        <div style={{ 
          textAlign: 'center', 
          padding: '40px', 
          background: 'white', 
          borderRadius: '10px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
        }}>
          <div style={{ fontSize: '48px', marginBottom: '20px' }}>📊</div>
          <h3 style={{ margin: '0 0 10px 0', color: '#111827' }}>Selecciona un tipo de reporte</h3>
          <p style={{ color: '#6b7280', margin: '0' }}>
            Elige entre Rindes, Producción o Costos para generar un reporte detallado
          </p>
        </div>
      )}
    </div>
  );
};

export default ReportsManagement;
