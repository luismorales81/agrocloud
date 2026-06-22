import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../../../../contexts/CurrencyContext';
import { exportService } from '../../../../services/ExportService';
import type { ExportOptions } from '../../../../services/ExportService';
import api from '../../../../services/api';
import { Icon, SemanticIcon } from '../../../../components/icons';
import useRangoPeriodoActivo from '../../../../hooks/useRangoPeriodoActivo';
import useConceptoTemporalModulo from '../../../../core/hooks/useConceptoTemporalModulo';

interface Reporte {
  id: string;
  nombre: string;
  descripcion: string;
  icono: string;
  requiereFechas: boolean;
}

const REPORTES: Reporte[] = [
  {
    id: 'reproductivo',
    nombre: 'Reporte Reproductivo',
    descripcion: 'Servicios, gestaciones, partos, destetes e índices reproductivos',
    icono: 'Baby',
    requiereFechas: true,
  },
  {
    id: 'mortalidad',
    nombre: 'Reporte de Mortalidad',
    descripcion: 'Análisis de muertes por etapa y causa',
    icono: 'Skull',
    requiereFechas: true,
  },
  {
    id: 'productivo',
    nombre: 'Reporte Productivo',
    descripcion: 'KPIs e índices productivos vs objetivos',
    icono: 'TrendingUp',
    requiereFechas: false,
  },
  {
    id: 'alimentacion',
    nombre: 'Reporte de Alimentación',
    descripcion: 'Consumo de alimento por categoría y tipo',
    icono: 'Wheat',
    requiereFechas: true,
  },
  {
    id: 'economico',
    nombre: 'Reporte Económico',
    descripcion: 'Ingresos, costos y rentabilidad',
    icono: 'DollarSign',
    requiereFechas: true,
  },
  {
    id: 'inventario',
    nombre: 'Reporte de Inventario',
    descripcion: 'Stock de animales por etapa y ubicación',
    icono: 'Clipboard',
    requiereFechas: false,
  },
  {
    id: 'sanitario',
    nombre: 'Reporte Sanitario',
    descripcion: 'Eventos sanitarios y tratamientos',
    icono: 'Syringe',
    requiereFechas: true,
  },
  {
    id: 'ventas',
    nombre: 'Reporte de Ventas',
    descripcion: 'Ventas realizadas y análisis de ingresos',
    icono: 'ShoppingCart',
    requiereFechas: true,
  },
];

const ReportesPorcinosScreen: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const { inicio: inicioPeriodo, fin: finPeriodo } = useRangoPeriodoActivo();
  const { concepto } = useConceptoTemporalModulo();
  const [activeReport, setActiveReport] = useState<string>('reproductivo');
  const [dateRange, setDateRange] = useState({ inicio: '', fin: '' });
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState<any>(null);
  const [errorFechas, setErrorFechas] = useState<string>('');

  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  // Fechas por defecto: período de gestión activo (barra superior)
  useEffect(() => {
    setDateRange({ inicio: inicioPeriodo, fin: finPeriodo });
  }, [inicioPeriodo, finPeriodo]);

  // Resetear paginación cuando cambie el reporte
  useEffect(() => {
    setPaginaActual(1);
  }, [activeReport]);

  // Función para obtener datos paginados
  const obtenerDatosPaginados = (datos: any[]) => {
    const totalPaginas = Math.ceil(datos.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const datosPaginados = datos.slice(inicio, fin);
    
    return { datosPaginados, totalPaginas };
  };

  const generateReport = async (tipo: string) => {
    console.log('🔍 [REPORTS] Iniciando generateReport para tipo:', tipo);
    
    const reporte = REPORTES.find(r => r.id === tipo);
    if (!reporte) return;

    // Validar fechas si el reporte las requiere obligatoriamente
    if (reporte.requiereFechas) {
      if (!dateRange.inicio || !dateRange.fin) {
        setErrorFechas('Por favor, seleccione las fechas de inicio y fin');
        alert('Error: Por favor, seleccione las fechas de inicio y fin');
        return;
      }

      const inicio = new Date(dateRange.inicio);
      const fin = new Date(dateRange.fin);
      
      if (fin < inicio) {
        setErrorFechas('La fecha de fin no puede ser anterior a la fecha de inicio');
        alert('Error: La fecha de fin no puede ser anterior a la fecha de inicio');
        return;
      }
    } else {
      // Validar que si se ingresaron fechas, sean válidas
      if (dateRange.inicio && dateRange.fin) {
        const inicio = new Date(dateRange.inicio);
        const fin = new Date(dateRange.fin);
        
        if (fin < inicio) {
          setErrorFechas('La fecha de fin no puede ser anterior a la fecha de inicio');
          alert('Error: La fecha de fin no puede ser anterior a la fecha de inicio');
          return;
        }
      }
    }
    
    setErrorFechas('');
    setLoading(true);
    
    try {
      let url = `/v1/porcinos/reportes/${tipo}`;
      const params: any = {};
      
      // Enviar fechas si están presentes (obligatorias o opcionales)
      if (dateRange.inicio && dateRange.fin) {
        params.fechaInicio = dateRange.inicio;
        params.fechaFin = dateRange.fin;
      } else if (reporte.requiereFechas) {
        // Si el reporte requiere fechas pero no están, ya se validó antes
        params.fechaInicio = dateRange.inicio;
        params.fechaFin = dateRange.fin;
      }
      
      const queryString = new URLSearchParams(params).toString();
      if (queryString) {
        url += '?' + queryString;
      }

      console.log('[REPORTS] Llamando API:', url);
      const response = await api.get(url);
      console.log('[REPORTS] Respuesta recibida:', response.data);

      const data = {
        titulo: reporte.nombre,
        fechaGeneracion: new Date().toLocaleDateString('es-ES'),
        datos: response.data,
        tipo: tipo,
      };
      
      console.log('[REPORTS] Datos finales establecidos:', data);
      setReportData(data);
      setActiveReport(tipo);
      console.log('[REPORTS] Reporte generado exitosamente para tipo:', tipo);
    } catch (error: any) {
      console.error('[REPORTS] Error generando reporte:', error);
      alert('Error al generar el reporte. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  const exportReport = async (format: 'excel' | 'pdf' | 'csv') => {
    if (!reportData) return;
    
    try {
      const reporte = REPORTES.find(r => r.id === activeReport);
      if (!reporte) return;

      // Para Excel, usar el endpoint del backend
      if (format === 'excel') {
        let url = `/v1/porcinos/reportes/${activeReport}/excel`;
        const params: any = {};
        
        // Enviar fechas si están presentes (obligatorias o opcionales)
        if (dateRange.inicio && dateRange.fin) {
          params.fechaInicio = dateRange.inicio;
          params.fechaFin = dateRange.fin;
        } else if (reporte.requiereFechas) {
          // Si el reporte requiere fechas pero no están, ya se validó antes
          params.fechaInicio = dateRange.inicio;
          params.fechaFin = dateRange.fin;
        }
        
        const queryString = new URLSearchParams(params).toString();
        if (queryString) {
          url += '?' + queryString;
        }

        const response = await api.get(url, {
          responseType: 'blob',
        });

        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const urlBlob = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = urlBlob;
        link.download = `${reporte.nombre.replace(/\s+/g, '_')}_${new Date().toISOString().split('T')[0]}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(urlBlob);

        alert(`Reporte "${reporte.nombre}" exportado exitosamente`);
        return;
      }

      // Para PDF y CSV, usar el servicio de exportación del frontend
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
        case 'pdf':
          result = await exportService.exportToPDF({
            title: reportData.titulo,
            data: convertirDatosParaExportacion(reportData.datos, activeReport),
            columns: getColumnsForReport(activeReport),
            summary: getSummaryForReport(reportData, activeReport)
          }, options);
          break;
        case 'csv':
          result = await exportService.exportToCSV({
            title: reportData.titulo,
            data: convertirDatosParaExportacion(reportData.datos, activeReport),
            columns: getColumnsForReport(activeReport),
            summary: getSummaryForReport(reportData, activeReport)
          }, options);
          break;
      }
      
      if (result) {
        alert(`Reporte exportado exitosamente: ${result.filename}`);
      }
    } catch (error) {
      console.error('Error exporting report:', error);
      alert('Error al exportar el reporte');
    }
  };

  const convertirDatosParaExportacion = (datos: any, tipo: string): any[] => {
    if (!datos) return [];
    
    switch (tipo) {
      case 'reproductivo':
        return [
          ...(datos.servicios || []),
          ...(datos.gestaciones || []),
          ...(datos.partos || []),
          ...(datos.destetes || []),
        ];
      case 'mortalidad':
        return [
          ...(datos.muertesMadres || []),
          ...(datos.muertesLechones || []),
          ...(datos.muertesRecria || []),
        ];
      case 'productivo':
        return datos.kpis ? [datos.kpis] : [];
      case 'alimentacion':
        return datos.consumos || [];
      case 'economico':
        return datos.ventas || [];
      case 'inventario':
        return [
          ...(datos.madres || []),
          ...(datos.padrillos || []),
          ...(datos.recrias || []),
        ];
      case 'sanitario':
        return datos.eventos || [];
      case 'ventas':
        return datos.ventas || [];
      default:
        return [];
    }
  };

  const getColumnsForReport = (reportType: string) => {
    switch (reportType) {
      case 'reproductivo':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Madre', label: 'Madre', type: 'text' as const },
          { key: 'Tipo', label: 'Tipo', type: 'text' as const },
          { key: 'Estado', label: 'Estado', type: 'text' as const },
        ];
      case 'mortalidad':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Causa', label: 'Causa', type: 'text' as const },
          { key: 'Cantidad', label: 'Cantidad', type: 'number' as const },
        ];
      case 'productivo':
        return [
          { key: 'Total Madres', label: 'Total Madres', type: 'number' as const },
          { key: 'Tasa de Parición (%)', label: 'Tasa de Parición (%)', type: 'number' as const },
        ];
      case 'alimentacion':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Categoría', label: 'Categoría', type: 'text' as const },
          { key: 'Cantidad (kg)', label: 'Cantidad (kg)', type: 'number' as const },
        ];
      case 'economico':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Ingreso Total', label: 'Ingreso Total', type: 'currency' as const },
        ];
      case 'inventario':
        return [
          { key: 'Identificación', label: 'Identificación', type: 'text' as const },
          { key: 'Estado', label: 'Estado', type: 'text' as const },
        ];
      case 'sanitario':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Tipo Evento', label: 'Tipo Evento', type: 'text' as const },
        ];
      case 'ventas':
        return [
          { key: 'Fecha', label: 'Fecha', type: 'date' as const },
          { key: 'Ingreso Total', label: 'Ingreso Total', type: 'currency' as const },
        ];
      default:
        return [];
    }
  };

  const getSummaryForReport = (data: any, tipo: string) => {
    if (!data || !data.datos) return undefined;
    
    switch (tipo) {
      case 'reproductivo':
        return {
          total: data.datos.indices?.totalServicios || 0,
          count: data.datos.indices?.totalPartos || 0
        };
      case 'mortalidad':
        return {
          total: data.datos.estadisticas?.totalMuertesLechones || 0,
          count: data.datos.estadisticas?.totalMuertesRecria || 0
        };
      default:
        return undefined;
    }
  };

  const renderReportContent = () => {
    if (!reportData || !reportData.datos) {
      return (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No hay datos disponibles para este reporte
        </div>
      );
    }

    const datos = reportData.datos;
    
    switch (activeReport) {
      case 'reproductivo':
        return renderReproductivoReport(datos);
      case 'mortalidad':
        return renderMortalidadReport(datos);
      case 'productivo':
        return renderProductivoReport(datos);
      case 'alimentacion':
        return renderAlimentacionReport(datos);
      case 'economico':
        return renderEconomicoReport(datos);
      case 'inventario':
        return renderInventarioReport(datos);
      case 'sanitario':
        return renderSanitarioReport(datos);
      case 'ventas':
        return renderVentasReport(datos);
      default:
        return null;
    }
  };

  const renderReproductivoReport = (datos: any) => {
    const servicios = datos.servicios || [];
    const partos = datos.partos || [];
    const destetes = datos.destetes || [];
    const indices = datos.indices || {};

    return (
      <div>
        {/* Estadísticas */}
        <div style={{ 
          background: '#fef3c7', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #f59e0b'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#92400e', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={24} /> Índices Reproductivos</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {indices['Tasa de Parición (%)'] || 0}%
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Tasa de Parición</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {indices['Lechones Vivos por Parto'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Lechones Vivos/Parto</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {indices['Total Partos'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Total Partos</div>
            </div>
          </div>
        </div>

        {/* Tabla de Partos */}
        {partos.length > 0 && (
          <div style={{ 
            background: 'white', 
            borderRadius: '10px', 
            overflow: 'hidden',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            marginBottom: '20px'
          }}>
            <div style={{ 
              background: '#f8f9fa', 
              padding: '15px', 
              borderBottom: '1px solid #dee2e6',
              fontWeight: 'bold'
            }}>
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Partos
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Madre</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Nacidos Vivos</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Nacidos Muertos</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Peso Promedio</th>
                  </tr>
                </thead>
                <tbody>
                  {partos.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}><strong>{item.Madre || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>{item['Nacidos Vivos'] || 0}</td>
                      <td style={{ padding: '12px' }}>{item['Nacidos Muertos'] || 0}</td>
                      <td style={{ padding: '12px' }}>{item['Peso Promedio Nacimiento'] || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderMortalidadReport = (datos: any) => {
    const estadisticas = datos.estadisticas || {};
    const muertesLechones = datos.muertesLechones || [];

    return (
      <div>
        <div style={{ 
          background: '#fef2f2', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ef4444'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#991b1b', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Skull" size={24} /> Estadísticas de Mortalidad</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {estadisticas['Total Muertes Lechones'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Muertes Lechones</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {estadisticas['Total Muertes Recría'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Muertes Recría</div>
            </div>
          </div>
        </div>

        {muertesLechones.length > 0 && (
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
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Muertes de Lechones
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Madre</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Causa</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad</th>
                  </tr>
                </thead>
                <tbody>
                  {muertesLechones.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}><strong>{item.Madre || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>{item.Causa || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Cantidad || 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderProductivoReport = (datos: any) => {
    const kpis = datos.kpis || {};
    const tieneDatos = Object.keys(kpis).length > 0;

    if (!tieneDatos) {
      return (
        <div style={{ 
          textAlign: 'center', 
          padding: '40px', 
          background: 'white', 
          borderRadius: '10px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
        }}>
          <Icon name="TrendingUp" size={48} style={{ marginBottom: '1rem', opacity: 0.5 }} />
          <h3 style={{ margin: '0 0 10px 0', color: '#111827' }}>No hay datos disponibles</h3>
          <p style={{ color: '#6b7280', margin: '0' }}>
            No se encontraron indicadores productivos para mostrar. 
            Asegúrate de tener madres, padrillos y datos productivos registrados.
          </p>
        </div>
      );
    }

    return (
      <div>
        <div style={{ 
          background: '#ecfdf5', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #10b981'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#065f46', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="TrendingUp" size={24} /> Indicadores Productivos
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            {Object.entries(kpis).map(([key, value]: [string, any]) => {
              // Formatear valores numéricos
              let valorMostrar: string;
              if (value === null || value === undefined) {
                valorMostrar = 'N/A';
              } else if (typeof value === 'number') {
                // Si es un porcentaje, mostrar con % y 2 decimales
                if (key.includes('%') || key.includes('Porcentaje') || key.includes('Tasa')) {
                  valorMostrar = value.toFixed(2) + '%';
                } else if (Number.isInteger(value)) {
                  valorMostrar = value.toString();
                } else {
                  valorMostrar = value.toFixed(2);
                }
              } else if (typeof value === 'object' && value !== null && 'doubleValue' in value) {
                // Objeto numérico tipo BigDecimal serializado desde el backend
                const numValue = typeof value === 'object' ? parseFloat(value.toString()) : value;
                valorMostrar = numValue.toFixed(2);
              } else {
                valorMostrar = String(value);
              }

              return (
                <div 
                  key={key} 
                  style={{ 
                    textAlign: 'center',
                    padding: '15px',
                    background: 'white',
                    borderRadius: '8px',
                    border: '1px solid #d1fae5'
                  }}
                >
                  <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#065f46', marginBottom: '5px' }}>
                    {valorMostrar}
                  </div>
                  <div style={{ fontSize: '13px', color: '#047857', lineHeight: '1.4' }}>
                    {key}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  };

  const renderAlimentacionReport = (datos: any) => {
    const consumos = datos.consumos || [];
    const resumen = datos.resumen || {};

    return (
      <div>
        <div style={{ 
          background: '#f0fdf4', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #22c55e'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#166534', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Wheat" size={24} /> Resumen de Consumo</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#166534' }}>
                {resumen['Consumo Total (kg)'] || 0} kg
              </div>
              <div style={{ fontSize: '14px', color: '#166534' }}>Consumo Total</div>
            </div>
          </div>
        </div>

        {consumos.length > 0 && (
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
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Consumos
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Categoría</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad (kg)</th>
                  </tr>
                </thead>
                <tbody>
                  {consumos.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item.Categoría || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item['Tipo Alimento'] || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item['Cantidad (kg)'] || 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderEconomicoReport = (datos: any) => {
    const ventas = datos.ventas || [];
    const resumen = datos.resumen || {};

    return (
      <div>
        <div style={{ 
          background: '#fef2f2', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ef4444'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#991b1b', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="DollarSign" size={24} /> Resumen Económico</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {formatCurrency(resumen['Ingresos Totales'] || 0)}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Ingresos Totales</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {formatCurrency(resumen['Balance Neto'] || 0)}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Balance Neto</div>
            </div>
          </div>
        </div>

        {ventas.length > 0 && (
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
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Ventas
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ingreso Total</th>
                  </tr>
                </thead>
                <tbody>
                  {ventas.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item.Tipo || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Cantidad || 0}</td>
                      <td style={{ padding: '12px' }}>{formatCurrency(item['Ingreso Total'] || 0)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderInventarioReport = (datos: any) => {
    const resumen = datos.resumen || {};
    const madres = datos.madres || [];
    const padrillos = datos.padrillos || [];
    const recrias = datos.recrias || [];
    const tieneDatos = madres.length > 0 || padrillos.length > 0 || recrias.length > 0;

    if (!tieneDatos) {
      return (
        <div style={{ 
          textAlign: 'center', 
          padding: '40px', 
          background: 'white', 
          borderRadius: '10px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
        }}>
          <Icon name="Clipboard" size={48} style={{ marginBottom: '1rem', opacity: 0.5 }} />
          <h3 style={{ margin: '0 0 10px 0', color: '#111827' }}>No hay datos disponibles</h3>
          <p style={{ color: '#6b7280', margin: '0' }}>
            No se encontraron animales en el inventario. 
            Asegúrate de tener madres, padrillos o recrías registrados.
          </p>
        </div>
      );
    }

    const totalMadres = resumen['Total Madres'] || 0;
    const totalPadrillos = resumen['Total Padrillos'] || 0;
    const totalAnimalesRecria = resumen['Total Animales en Recría'] || 0;
    const totalRecriasActivas = resumen['Total Recrías Activas'] || 0;

    return (
      <div>
        <div style={{ 
          background: '#f0f9ff', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #3b82f6'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#1e40af', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Icon name="Clipboard" size={24} /> Resumen de Inventario
          </h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center', padding: '15px', background: 'white', borderRadius: '8px', border: '1px solid #dbeafe' }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#1e40af', marginBottom: '5px' }}>
                {totalMadres}
              </div>
              <div style={{ fontSize: '14px', color: '#1e3a8a' }}>Total Madres</div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: 'white', borderRadius: '8px', border: '1px solid #dbeafe' }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#1e40af', marginBottom: '5px' }}>
                {totalPadrillos}
              </div>
              <div style={{ fontSize: '14px', color: '#1e3a8a' }}>Total Padrillos</div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: 'white', borderRadius: '8px', border: '1px solid #dbeafe' }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#1e40af', marginBottom: '5px' }}>
                {totalAnimalesRecria}
              </div>
              <div style={{ fontSize: '14px', color: '#1e3a8a' }}>Animales en Recría</div>
            </div>
            <div style={{ textAlign: 'center', padding: '15px', background: 'white', borderRadius: '8px', border: '1px solid #dbeafe' }}>
              <div style={{ fontSize: '28px', fontWeight: 'bold', color: '#1e40af', marginBottom: '5px' }}>
                {totalRecriasActivas}
              </div>
              <div style={{ fontSize: '14px', color: '#1e3a8a' }}>Recrías Activas</div>
            </div>
          </div>
        </div>

        {madres.length > 0 && (
          <div style={{ 
            background: 'white', 
            borderRadius: '10px', 
            overflow: 'hidden',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            marginBottom: '20px'
          }}>
            <div style={{ 
              background: '#f8f9fa', 
              padding: '15px', 
              borderBottom: '1px solid #dee2e6',
              fontWeight: 'bold'
            }}>
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Madres
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Identificación</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Raza</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Número Partos</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ubicación</th>
                  </tr>
                </thead>
                <tbody>
                  {madres.slice(0, 20).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}><strong>{item.Identificación || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>{item.Estado || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Raza || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item['Número Partos'] || 0}</td>
                      <td style={{ padding: '12px' }}>{item.Ubicación || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {padrillos.length > 0 && (
          <div style={{ 
            background: 'white', 
            borderRadius: '10px', 
            overflow: 'hidden',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            marginBottom: '20px'
          }}>
            <div style={{ 
              background: '#f8f9fa', 
              padding: '15px', 
              borderBottom: '1px solid #dee2e6',
              fontWeight: 'bold'
            }}>
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Padrillos
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Identificación</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Raza</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ubicación</th>
                  </tr>
                </thead>
                <tbody>
                  {padrillos.slice(0, 20).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}><strong>{item.Identificación || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>{item.Raza || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Ubicación || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {recrias.length > 0 && (
          <div style={{ 
            background: 'white', 
            borderRadius: '10px', 
            overflow: 'hidden',
            boxShadow: '0 2px 10px rgba(0,0,0,0.1)',
            marginBottom: '20px'
          }}>
            <div style={{ 
              background: '#f8f9fa', 
              padding: '15px', 
              borderBottom: '1px solid #dee2e6',
              fontWeight: 'bold'
            }}>
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Recrías Activas
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Lote</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha Ingreso</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad Animales</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Peso Promedio (kg)</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Sexo</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Etapa</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Destino</th>
                  </tr>
                </thead>
                <tbody>
                  {recrias.slice(0, 20).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}><strong>{item.Lote || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>
                        {item['Fecha Ingreso'] ? new Date(item['Fecha Ingreso']).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item['Cantidad Animales'] || 0}</td>
                      <td style={{ padding: '12px' }}>
                        {typeof item['Peso Promedio (kg)'] === 'number' ? item['Peso Promedio (kg)'].toFixed(2) : item['Peso Promedio (kg)'] || 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item.Sexo || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Etapa || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Destino || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderSanitarioReport = (datos: any) => {
    const eventos = datos.eventos || [];
    const resumen = datos.resumen || {};

    return (
      <div>
        <div style={{ 
          background: '#fef3c7', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #f59e0b'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#92400e', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Syringe" size={24} /> Resumen Sanitario</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {resumen['Total Eventos'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Total Eventos</div>
            </div>
          </div>
        </div>

        {eventos.length > 0 && (
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
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Eventos Sanitarios
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo Evento</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Categoría</th>
                  </tr>
                </thead>
                <tbody>
                  {eventos.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item['Tipo Evento'] || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Categoría || 'N/A'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const renderVentasReport = (datos: any) => {
    const ventas = datos.ventas || [];
    const resumen = datos.resumen || {};

    return (
      <div>
        <div style={{ 
          background: '#f0fdf4', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #22c55e'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#166534', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="ShoppingCart" size={24} /> Resumen de Ventas</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#166534' }}>
                {formatCurrency(resumen['Ingresos Totales'] || 0)}
              </div>
              <div style={{ fontSize: '14px', color: '#166534' }}>Ingresos Totales</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#166534' }}>
                {resumen['Total Ventas'] || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#166534' }}>Total Ventas</div>
            </div>
          </div>
        </div>

        {ventas.length > 0 && (
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
              <Icon name="Clipboard" size={20} style={{ marginRight: '0.5rem' }} /> Detalle de Ventas
            </div>
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px' }}>
                <thead>
                  <tr style={{ background: '#f8f9fa' }}>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad</th>
                    <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ingreso Total</th>
                  </tr>
                </thead>
                <tbody>
                  {ventas.slice(0, 10).map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}>
                        {item.Fecha ? new Date(item.Fecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                      <td style={{ padding: '12px' }}>{item.Tipo || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.Cantidad || 0}</td>
                      <td style={{ padding: '12px' }}>{formatCurrency(item['Ingreso Total'] || 0)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    );
  };

  const reporteActual = REPORTES.find(r => r.id === activeReport);

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      {/* Header */}
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
            <Icon name="PiggyBank" size={32} />
          </div>
          <div>
            <h1 style={{ margin: '0 0 5px 0', fontSize: '24px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="BarChart" size={28} /> Sistema de Reportes - Porcinos</h1>
            <p style={{ margin: '0', opacity: '0.9' }}>
              Genera y analiza reportes detallados de tu producción porcina
            </p>
            <p style={{ margin: '0.5rem 0 0', opacity: '0.85', fontSize: '13px' }}>
              {concepto.etiquetaPeriodo}: fechas por defecto del selector superior.
              Reproducción por rango de fechas; recría y ventas por {concepto.unidadOperativa.toLowerCase()}.
            </p>
          </div>
        </div>
      </div>

      {/* Selector de reportes */}
      <div style={{ marginBottom: '20px' }}>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '15px' }}>
          {REPORTES.map((reporte) => (
            <button
              key={reporte.id}
              onClick={() => setActiveReport(reporte.id)}
              style={{
                background: activeReport === reporte.id ? '#3b82f6' : '#e5e7eb',
                color: activeReport === reporte.id ? 'white' : '#374151',
                border: 'none',
                padding: '12px 20px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Icon name={reporte.icono as any} size={18} />
              <span>{reporte.nombre}</span>
            </button>
          ))}
        </div>

        {/* Filtros de fecha y botón de generar */}
        <div style={{ display: 'flex', gap: '15px', alignItems: 'flex-end', flexWrap: 'wrap' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Fecha Inicio{reporteActual?.requiereFechas ? ' *' : ' (opcional)'}:
            </label>
            <input
              type="date"
              value={dateRange.inicio}
              onChange={(e) => {
                setDateRange(prev => ({ ...prev, inicio: e.target.value }));
                setErrorFechas('');
              }}
              style={{
                padding: '8px 12px',
                border: errorFechas ? '2px solid #ef4444' : '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>
              Fecha Fin{reporteActual?.requiereFechas ? ' *' : ' (opcional)'}:
            </label>
            <input
              type="date"
              value={dateRange.fin}
              onChange={(e) => {
                setDateRange(prev => ({ ...prev, fin: e.target.value }));
                setErrorFechas('');
              }}
              min={dateRange.inicio}
              style={{
                padding: '8px 12px',
                border: errorFechas ? '2px solid #ef4444' : '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px'
              }}
            />
          </div>
          {errorFechas && (
            <div style={{ 
              width: '100%',
              padding: '10px',
              background: '#fef2f2',
              border: '1px solid #ef4444',
              borderRadius: '5px',
              color: '#991b1b',
              fontSize: '14px'
            }}>
              <SemanticIcon semanticName="warning" size={20} style={{ marginRight: '0.5rem' }} /> {errorFechas}
            </div>
          )}
          <button
            onClick={() => generateReport(activeReport)}
            disabled={loading}
            style={{
              background: '#10b981',
              color: 'white',
              border: 'none',
              padding: '10px 20px',
              borderRadius: '5px',
              cursor: loading ? 'not-allowed' : 'pointer',
              fontSize: '14px',
              fontWeight: 'bold',
              opacity: loading ? 0.6 : 1,
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              height: 'fit-content'
            }}
          >
            {loading ? (
              <>
                <Icon name="RefreshCw" size={18} /> Generando...
              </>
            ) : (
              <>
                <Icon name="BarChart" size={18} /> Generar Reporte
              </>
            )}
          </button>
        </div>

        {/* Botones de exportación */}
        {reportData && (
          <div style={{ 
            display: 'flex', 
            gap: '8px', 
            alignItems: 'center', 
            flexWrap: 'wrap',
            marginTop: '15px',
            padding: '12px',
            background: '#f8f9fa',
            borderRadius: '8px',
            border: '1px solid #e9ecef'
          }}>
            <span style={{ fontWeight: 'bold', color: '#495057', marginRight: '8px', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Icon name="Upload" size={18} /> Exportar:</span>
            <button
              onClick={() => exportReport('excel')}
              style={{
                background: '#28a745',
                color: 'white',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Icon name="FileSpreadsheet" size={16} style={{ marginRight: '0.25rem' }} /> Excel
            </button>
            <button
              onClick={() => exportReport('pdf')}
              style={{
                background: '#dc3545',
                color: 'white',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Icon name="FileText" size={16} style={{ marginRight: '0.25rem' }} /> PDF
            </button>
            <button
              onClick={() => exportReport('csv')}
              style={{
                background: '#6c757d',
                color: 'white',
                border: 'none',
                padding: '6px 12px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px',
                fontWeight: 'bold',
                display: 'flex',
                alignItems: 'center',
                gap: '4px'
              }}
            >
              <Icon name="FileSpreadsheet" size={16} style={{ marginRight: '0.25rem' }} /> CSV
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
          <Icon name="RefreshCw" size={24} />
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
                {reporteActual && <Icon name={reporteActual.icono as any} size={32} />}
              </div>
              <div>
                <h2 style={{ margin: '0 0 5px 0', color: '#111827' }}>{reportData.titulo}</h2>
                <p style={{ margin: '0', color: '#6b7280', fontSize: '14px' }}>
                  Generado el {reportData.fechaGeneracion}
                </p>
              </div>
            </div>
          </div>

          {/* Contenido específico del reporte */}
          {renderReportContent()}
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
          <Icon name="BarChart" size={48} />
          <h3 style={{ margin: '0 0 10px 0', color: '#111827' }}>Selecciona un tipo de reporte</h3>
          <p style={{ color: '#6b7280', margin: '0' }}>
            Elige entre los reportes disponibles y genera un reporte detallado
          </p>
        </div>
      )}
    </div>
  );
};

export default ReportesPorcinosScreen;
