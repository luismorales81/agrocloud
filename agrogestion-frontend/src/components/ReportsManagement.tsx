import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { exportService } from '../services/ExportService';
import type { ExportOptions } from '../services/ExportService';
import { reportesService } from '../services/apiServices';

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
  const { formatCurrency } = useCurrencyContext();
  const empresaContext = useEmpresa();
  const tienePermisoFinanciero = empresaContext?.tienePermisoFinanciero() || false;
  
  const [activeReport, setActiveReport] = useState<string>('rindes');
  const [dateRange, setDateRange] = useState({ inicio: '', fin: '' });
  const [loading, setLoading] = useState(false);
  const [reportData, setReportData] = useState<any>(null);
  const [errorFechas, setErrorFechas] = useState<string>('');
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  // Función para obtener datos paginados
  const obtenerDatosPaginados = (datos: any[]) => {
    const totalPaginas = Math.ceil(datos.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const datosPaginados = datos.slice(inicio, fin);
    
    return { datosPaginados, totalPaginas };
  };

  // Resetear paginación cuando cambie el reporte
  useEffect(() => {
    setPaginaActual(1);
  }, [activeReport]);

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
    console.log('🔍 [REPORTS] Iniciando generateReport para tipo:', tipo);
    
    // Validar fechas
    if (dateRange.inicio && dateRange.fin) {
      const inicio = new Date(dateRange.inicio);
      const fin = new Date(dateRange.fin);
      
      if (fin < inicio) {
        setErrorFechas('La fecha de fin no puede ser anterior a la fecha de inicio');
        alert('Error: La fecha de fin no puede ser anterior a la fecha de inicio');
        return;
      }
    }
    
    setErrorFechas('');
    setLoading(true);
    
    try {
      let data;
      const fechaInicio = dateRange.inicio || null;
      const fechaFin = dateRange.fin || null;
      
      console.log('🔍 [REPORTS] Parámetros de fecha:', { fechaInicio, fechaFin });
      
      // Construir parámetros solo con valores válidos
      const params: any = {};
      if (fechaInicio) params.fechaInicio = fechaInicio;
      if (fechaFin) params.fechaFin = fechaFin;
      
      console.log('🔍 [REPORTS] Parámetros finales:', params);
      
      switch (tipo) {
        case 'rindes':
          console.log('🔍 [REPORTS] Llamando API de rendimiento...');
          const rendimientoData = await reportesService.obtenerRendimiento(params);
          console.log('✅ [REPORTS] Respuesta de rendimiento:', rendimientoData);
          console.log('🔍 [REPORTS] Tipo de datos:', typeof rendimientoData, 'Es array:', Array.isArray(rendimientoData));
          data = {
            titulo: 'Reporte de Rindes por Lote',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: rendimientoData,
            estadisticas: calcularEstadisticasRendimiento(rendimientoData)
          };
          break;
        case 'produccion':
          console.log('🔍 [REPORTS] Llamando API de estadísticas de producción...');
          const estadisticasData = await reportesService.obtenerEstadisticasProduccion(params);
          console.log('✅ [REPORTS] Respuesta de estadísticas:', estadisticasData);
          data = estadisticasData; // El backend ya devuelve la estructura correcta
          break;
        case 'cosechas':
          console.log('🔍 [REPORTS] Llamando API de cosechas...');
          const cosechasData = await reportesService.obtenerCosechas(params);
          console.log('✅ [REPORTS] Respuesta de cosechas:', cosechasData);
          console.log('🔍 [REPORTS] Tipo de datos cosechas:', typeof cosechasData, 'Es array:', Array.isArray(cosechasData));
          data = {
            titulo: 'Reporte de Cosechas',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: cosechasData,
            estadisticas: calcularEstadisticasCosechas(cosechasData)
          };
          break;
        case 'rentabilidad':
          console.log('🔍 [REPORTS] Llamando API de rentabilidad...');
          const rentabilidadData = await reportesService.obtenerRentabilidad(params);
          console.log('✅ [REPORTS] Respuesta de rentabilidad:', rentabilidadData);
          console.log('🔍 [REPORTS] Tipo de datos rentabilidad:', typeof rentabilidadData, 'Es array:', Array.isArray(rentabilidadData));
          data = {
            titulo: 'Reporte de Rentabilidad por Cultivo',
            fechaGeneracion: new Date().toLocaleDateString('es-ES'),
            datos: rentabilidadData,
            estadisticas: calcularEstadisticasRentabilidad(rentabilidadData)
          };
          break;
        default:
          data = null;
      }
      
      console.log('🔍 [REPORTS] Datos finales establecidos:', data);
      setReportData(data);
      setActiveReport(tipo);
      console.log('✅ [REPORTS] Reporte generado exitosamente para tipo:', tipo);
    } catch (error) {
      console.error('❌ [REPORTS] Error generando reporte:', error);
      alert('Error al generar el reporte. Verifica la conexión con el servidor.');
    } finally {
      setLoading(false);
    }
  };

  // Funciones de cálculo de estadísticas
  const calcularEstadisticasRendimiento = (datos: any[]) => {
    if (!datos || datos.length === 0) return {};
    
    const rendimientos = datos.map(item => parseFloat(item.rendimientoReal) || 0);
    const cumplimientos = datos.map(item => parseFloat(item.porcentajeCumplimiento) || 0);
    
    return {
      promedioRinde: rendimientos.reduce((sum, r) => sum + r, 0) / rendimientos.length,
      mejorRinde: Math.max(...rendimientos),
      peorRinde: Math.min(...rendimientos),
      promedioCumplimiento: cumplimientos.reduce((sum, c) => sum + c, 0) / cumplimientos.length,
      totalLotes: datos.length
    };
  };

  const calcularEstadisticasCosechas = (datos: any[]) => {
    if (!datos || datos.length === 0) return {};
    
    const rendimientos = datos.map(item => parseFloat(item.rendimientoReal) || 0);
    
    return {
      totalCosechas: datos.length,
      rendimientoPromedio: rendimientos.reduce((sum, r) => sum + r, 0) / rendimientos.length
    };
  };


  const calcularEstadisticasRentabilidad = (datos: any[]) => {
    if (!datos || datos.length === 0) return {};
    
    const rentabilidades = datos.map(item => parseFloat(item.porcentajeRentabilidad) || 0);
    const ingresos = datos.map(item => parseFloat(item.ingresoTotal) || 0);
    const costos = datos.map(item => parseFloat(item.costoTotal) || 0);
    
    return {
      totalAnalisis: datos.length,
      rentabilidadPromedio: rentabilidades.reduce((sum, r) => sum + r, 0) / rentabilidades.length,
      ingresoTotal: ingresos.reduce((sum, i) => sum + i, 0),
      costoTotal: costos.reduce((sum, c) => sum + c, 0),
      cultivosRentables: datos.filter(item => parseFloat(item.porcentajeRentabilidad) > 0).length
    };
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
          { key: 'nombreLote', label: 'Lote', type: 'text' as const },
          { key: 'nombreCultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'superficieHectareas', label: 'Superficie (ha)', type: 'number' as const },
          { key: 'rendimientoReal', label: 'Rinde Real (kg/ha)', type: 'number' as const },
          { key: 'rendimientoEsperado', label: 'Rinde Esperado (kg/ha)', type: 'number' as const },
          { key: 'porcentajeCumplimiento', label: 'Cumplimiento (%)', type: 'number' as const },
          { key: 'fechaCosecha', label: 'Fecha de Cosecha', type: 'date' as const }
        ];
      case 'cosechas':
        return [
          { key: 'nombreLote', label: 'Lote', type: 'text' as const },
          { key: 'nombreCultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'fechaCosecha', label: 'Fecha de Cosecha', type: 'date' as const },
          { key: 'cantidadCosechada', label: 'Cantidad Cosechada', type: 'number' as const },
          { key: 'unidadCosecha', label: 'Unidad', type: 'text' as const },
          { key: 'rendimientoReal', label: 'Rendimiento Real', type: 'number' as const }
        ];
      case 'rentabilidad':
        return [
          { key: 'lote', label: 'Lote', type: 'text' as const },
          { key: 'cultivo', label: 'Cultivo', type: 'text' as const },
          { key: 'ingresoTotal', label: 'Ingreso Total ($)', type: 'currency' as const },
          { key: 'costoTotal', label: 'Costo Total ($)', type: 'currency' as const },
          { key: 'margen', label: 'Margen ($)', type: 'currency' as const },
          { key: 'porcentajeRentabilidad', label: 'Rentabilidad (%)', type: 'number' as const }
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


  const renderRindeReport = () => {
    console.log('🔍 [RENDER] renderRindeReport - reportData:', reportData);
    console.log('🔍 [RENDER] Tipo de reportData:', typeof reportData, 'Es array:', Array.isArray(reportData));
    
    // Verificar que reportData existe y tiene la estructura correcta
    if (!reportData || !reportData.datos || !Array.isArray(reportData.datos)) {
      console.log('❌ [RENDER] No hay datos de rendimiento - reportData:', reportData);
      return (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No hay datos de rendimiento disponibles
        </div>
      );
    }
    
    const datos = reportData.datos;
    console.log('✅ [RENDER] Datos de rendimiento encontrados:', datos.length, 'registros');

    // Calcular estadísticas básicas
    const promedioRinde = datos.length > 0 
      ? datos.reduce((sum: number, item: any) => sum + (item.rendimientoReal || 0), 0) / datos.length
      : 0;
    const mejorRinde = datos.length > 0 
      ? Math.max(...datos.map((item: any) => item.rendimientoReal || 0))
      : 0;
    const promedioCumplimiento = datos.length > 0 
      ? datos.reduce((sum: number, item: any) => sum + (item.porcentajeCumplimiento || 0), 0) / datos.length
      : 0;

    return (
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
                {Math.round(promedioRinde)} kg/ha
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Rinde Promedio</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {Math.round(mejorRinde)} kg/ha
              </div>
              <div style={{ fontSize: '14px', color: '#92400e' }}>Mejor Rinde</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#92400e' }}>
                {Math.round(promedioCumplimiento)}%
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
                {(() => {
                  const { datosPaginados, totalPaginas } = obtenerDatosPaginados(datos);
                  
                  return datosPaginados.map((item: any, index: number) => (
                    <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                      <td style={{ padding: '12px' }}><strong>{item.nombreLote || 'N/A'}</strong></td>
                      <td style={{ padding: '12px' }}>{item.nombreCultivo || 'N/A'}</td>
                      <td style={{ padding: '12px' }}>{item.superficieHectareas || 0}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                          {item.rendimientoReal || 0}
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>{item.rendimientoEsperado || 0}</td>
                      <td style={{ padding: '12px' }}>
                        <span style={{
                          padding: '4px 8px',
                          borderRadius: '12px',
                          fontSize: '12px',
                          fontWeight: 'bold',
                          background: (item.porcentajeCumplimiento || 0) >= 100 ? '#dcfce7' : 
                                      (item.porcentajeCumplimiento || 0) >= 80 ? '#fef3c7' : '#fecaca',
                          color: (item.porcentajeCumplimiento || 0) >= 100 ? '#166534' : 
                                 (item.porcentajeCumplimiento || 0) >= 80 ? '#92400e' : '#991b1b'
                        }}>
                          {item.porcentajeCumplimiento || 0}%
                        </span>
                      </td>
                      <td style={{ padding: '12px' }}>
                        {item.fechaCosecha ? new Date(item.fechaCosecha).toLocaleDateString('es-ES') : 'N/A'}
                      </td>
                    </tr>
                  ));
                })()}
              </tbody>
            </table>
          </div>
          
          {/* Paginación */}
          {(() => {
            const { totalPaginas } = obtenerDatosPaginados(datos);
            
            if (totalPaginas > 1) {
              return (
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center', 
                  padding: '20px',
                  borderTop: '1px solid #e5e7eb',
                  background: '#f8f9fa'
                }}>
                  <div style={{ fontSize: '14px', color: '#6b7280' }}>
                    Mostrando {((paginaActual - 1) * elementosPorPagina) + 1} - {Math.min(paginaActual * elementosPorPagina, datos.length)} de {datos.length} registros
                  </div>
                  
                  <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
                    <button
                      onClick={() => setPaginaActual(1)}
                      disabled={paginaActual === 1}
                      style={{
                        padding: '8px 12px',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        background: paginaActual === 1 ? '#f3f4f6' : 'white',
                        color: paginaActual === 1 ? '#9ca3af' : '#374151',
                        cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                        fontSize: '14px'
                      }}
                    >
                      ⏮️ Primera
                    </button>
                    
                    <button
                      onClick={() => setPaginaActual(paginaActual - 1)}
                      disabled={paginaActual === 1}
                      style={{
                        padding: '8px 12px',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        background: paginaActual === 1 ? '#f3f4f6' : 'white',
                        color: paginaActual === 1 ? '#9ca3af' : '#374151',
                        cursor: paginaActual === 1 ? 'not-allowed' : 'pointer',
                        fontSize: '14px'
                      }}
                    >
                      ⬅️ Anterior
                    </button>
                    
                    <span style={{ 
                      padding: '8px 12px', 
                      fontSize: '14px',
                      color: '#374151',
                      fontWeight: 'bold'
                    }}>
                      Página {paginaActual} de {totalPaginas}
                    </span>
                    
                    <button
                      onClick={() => setPaginaActual(paginaActual + 1)}
                      disabled={paginaActual === totalPaginas}
                      style={{
                        padding: '8px 12px',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                        color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                        cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                        fontSize: '14px'
                      }}
                    >
                      Siguiente ➡️
                    </button>
                    
                    <button
                      onClick={() => setPaginaActual(totalPaginas)}
                      disabled={paginaActual === totalPaginas}
                      style={{
                        padding: '8px 12px',
                        border: '1px solid #d1d5db',
                        borderRadius: '6px',
                        background: paginaActual === totalPaginas ? '#f3f4f6' : 'white',
                        color: paginaActual === totalPaginas ? '#9ca3af' : '#374151',
                        cursor: paginaActual === totalPaginas ? 'not-allowed' : 'pointer',
                        fontSize: '14px'
                      }}
                    >
                      Última ⏭️
                    </button>
                  </div>
                </div>
              );
            }
            return null;
          })()}
        </div>
      </div>
    );
  };

  const renderProduccionReport = () => {
    // Verificar que reportData existe y tiene la estructura correcta
    if (!reportData || !reportData.porCultivo) {
      return (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No hay datos de producción disponibles
        </div>
      );
    }

    // Convertir porCultivo a array para poder hacer map
    const cultivosArray = Object.entries(reportData.porCultivo).map(([cultivo, stats]: [string, any]) => ({
      cultivo,
      cantidadCosechas: stats.cantidadCosechas || 0,
      superficieTotal: stats.superficieTotal || 0,
      rendimientoPromedio: stats.rendimientoPromedio || 0
    }));

    return (
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
                {reportData.superficieTotal || 0} ha
              </div>
              <div style={{ fontSize: '14px', color: '#065f46' }}>Superficie Total</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#065f46' }}>
                {reportData.totalCosechas || 0}
              </div>
              <div style={{ fontSize: '14px', color: '#065f46' }}>Total Cosechas</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#065f46' }}>
                {cultivosArray.length}
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad Cosechas</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rinde Promedio (kg/ha)</th>
                </tr>
              </thead>
              <tbody>
                {cultivosArray.map((item, index: number) => (
                  <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}><strong>{item.cultivo}</strong></td>
                    <td style={{ padding: '12px' }}>{item.superficieTotal}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                        {item.cantidadCosechas}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>{item.rendimientoPromedio}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

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

  const renderCosechasReport = () => {
    // Verificar que reportData existe y tiene la estructura correcta
    if (!reportData || !reportData.datos || !Array.isArray(reportData.datos)) {
      return (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No hay datos de cosechas disponibles
        </div>
      );
    }

    const datos = reportData.datos;
    
    // Función para determinar el estado de la cosecha
    const getEstadoCosecha = (item: any) => {
      // Si tiene fecha de cosecha, está completada
      if (item.fechaCosecha) {
        return 'Completada';
      }
      // Si tiene cantidad cosechada, está en proceso
      if (item.cantidadCosechada && item.cantidadCosechada > 0) {
        return 'En Proceso';
      }
      // Si no tiene nada, está pendiente
      return 'Pendiente';
    };
    console.log('✅ [RENDER] Datos de cosechas encontrados:', datos.length, 'registros');

    // Calcular estadísticas básicas
    const totalCosechas = datos.length;
    const rendimientoPromedio = datos.length > 0 
      ? datos.reduce((sum: number, item: any) => sum + (item.rendimientoReal || 0), 0) / datos.length
      : 0;

    return (
      <div>
        <div style={{ 
          background: '#f3e8ff', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #8b5cf6'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#6b21a8' }}>🌾 Análisis de Cosechas</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#6b21a8' }}>
                {totalCosechas}
              </div>
              <div style={{ fontSize: '14px', color: '#6b21a8' }}>Total Cosechas</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#6b21a8' }}>
                {Math.round(rendimientoPromedio * 10) / 10}
              </div>
              <div style={{ fontSize: '14px', color: '#6b21a8' }}>Rendimiento Promedio</div>
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
            🌾 Detalle de Cosechas
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Fecha</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cantidad</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rendimiento</th>
                </tr>
              </thead>
              <tbody>
                {datos.map((item: any, index: number) => (
                  <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}><strong>{item.nombreLote || 'N/A'}</strong></td>
                    <td style={{ padding: '12px' }}>{item.nombreCultivo || 'N/A'}</td>
                    <td style={{ padding: '12px' }}>
                      {item.fechaCosecha ? new Date(item.fechaCosecha).toLocaleDateString('es-ES') : 'N/A'}
                    </td>
                    <td style={{ padding: '12px' }}>{item.cantidadCosechada || 0} {item.unidadCosecha || ''}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: getEstadoCosecha(item) === 'Completada' ? '#dcfce7' : 
                                   getEstadoCosecha(item) === 'En Proceso' ? '#fef3c7' : '#fecaca',
                        color: getEstadoCosecha(item) === 'Completada' ? '#166534' : 
                               getEstadoCosecha(item) === 'En Proceso' ? '#92400e' : '#991b1b'
                      }}>
                        {getEstadoCosecha(item)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>{item.rendimientoReal || 0} {item.unidadRendimiento || ''}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    );
  };

  const renderHumedadReport = () => (
    <div>
      <div style={{ 
        background: '#ecfeff', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #06b6d4'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#155e75' }}>💧 Análisis de Humedad por Ubicación</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#155e75' }}>
              {reportData.estadisticas.ubicacionesAnalizadas}
            </div>
            <div style={{ fontSize: '14px', color: '#155e75' }}>Ubicaciones</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#155e75' }}>
              {Math.round(reportData.estadisticas.humedadPromedioGeneral * 10) / 10}%
            </div>
            <div style={{ fontSize: '14px', color: '#155e75' }}>Humedad Promedio</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#155e75' }}>
              {reportData.estadisticas.ubicacionesOptimas}
            </div>
            <div style={{ fontSize: '14px', color: '#155e75' }}>Ubicaciones Óptimas</div>
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
          💧 Análisis por Ubicación
        </div>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ 
            width: '100%', 
            borderCollapse: 'collapse',
            fontSize: '14px'
          }}>
            <thead>
              <tr style={{ background: '#f8f9fa' }}>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ubicación</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Humedad Promedio</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rango</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Cosechas</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Estado</th>
                <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Recomendación</th>
              </tr>
            </thead>
            <tbody>
              {reportData.datos.map((item: any, index: number) => (
                <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                  <td style={{ padding: '12px' }}><strong>{item.ubicacion}</strong></td>
                  <td style={{ padding: '12px' }}>{item.humedadPromedio}%</td>
                  <td style={{ padding: '12px' }}>{item.humedadMinima}% - {item.humedadMaxima}%</td>
                  <td style={{ padding: '12px' }}>{item.cantidadCosechas}</td>
                  <td style={{ padding: '12px' }}>
                    <span style={{
                      padding: '4px 8px',
                      borderRadius: '12px',
                      fontSize: '12px',
                      fontWeight: 'bold',
                      background: item.estadoHumedad === 'Óptimo' ? '#dcfce7' : 
                                  item.estadoHumedad === 'Aceptable' ? '#fef3c7' : '#fecaca',
                      color: item.estadoHumedad === 'Óptimo' ? '#166534' : 
                             item.estadoHumedad === 'Aceptable' ? '#92400e' : '#991b1b'
                    }}>
                      {item.estadoHumedad}
                    </span>
                  </td>
                  <td style={{ padding: '12px', fontSize: '12px' }}>{item.recomendaciones}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  const renderRentabilidadReport = () => {
    console.log('🔍 [RENDER] renderRentabilidadReport - reportData:', reportData);
    console.log('🔍 [RENDER] Tipo de reportData:', typeof reportData, 'Es array:', Array.isArray(reportData));
    
    // Verificar que reportData existe y tiene la estructura correcta
    if (!reportData || !reportData.datos || !Array.isArray(reportData.datos)) {
      console.log('❌ [RENDER] No hay datos de rentabilidad - reportData:', reportData);
      return (
        <div style={{ textAlign: 'center', padding: '40px', color: '#666' }}>
          No hay datos de rentabilidad disponibles
        </div>
      );
    }
    
    const datos = reportData.datos;
    console.log('✅ [RENDER] Datos de rentabilidad encontrados:', datos.length, 'registros');

    // Calcular estadísticas básicas
    const totalAnalisis = datos.length;
    const rentabilidadPromedio = datos.length > 0 
      ? datos.reduce((sum: number, item: any) => sum + (item.porcentajeRentabilidad || 0), 0) / datos.length
      : 0;
    const cultivosRentables = datos.filter((item: any) => (item.porcentajeRentabilidad || 0) > 0).length;

    return (
      <div>
        <div style={{ 
          background: '#fef2f2', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #ef4444'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#991b1b' }}>💰 Análisis de Rentabilidad</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {totalAnalisis}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Análisis Realizados</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {isNaN(rentabilidadPromedio) ? '0' : Math.round(rentabilidadPromedio * 10) / 10}%
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Rentabilidad Promedio</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#991b1b' }}>
                {cultivosRentables}
              </div>
              <div style={{ fontSize: '14px', color: '#991b1b' }}>Cultivos Rentables</div>
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
            💰 Detalle de Rentabilidad
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
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Ingreso Total</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Costo Total</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Margen</th>
                  <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Rentabilidad</th>
                </tr>
              </thead>
              <tbody>
                {datos.map((item: any, index: number) => (
                  <tr key={index} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}><strong>{item.lote || 'N/A'}</strong></td>
                    <td style={{ padding: '12px' }}>{item.cultivo || 'N/A'}</td>
                    <td style={{ padding: '12px' }}>{formatCurrency(item.ingresoTotal || 0)}</td>
                    <td style={{ padding: '12px' }}>{formatCurrency(item.costoTotal || 0)}</td>
                    <td style={{ padding: '12px' }}>{formatCurrency(item.margen || 0)}</td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: (item.porcentajeRentabilidad || 0) > 0 ? '#dcfce7' : 
                                    (item.porcentajeRentabilidad || 0) === 0 ? '#fef3c7' : '#fecaca',
                        color: (item.porcentajeRentabilidad || 0) > 0 ? '#166534' : 
                               (item.porcentajeRentabilidad || 0) === 0 ? '#92400e' : '#991b1b'
                      }}>
                        {item.porcentajeRentabilidad || 0}%
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
  };

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
              Genera y analiza reportes detallados de tu producción agropecuaria
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
            onClick={() => setActiveReport('cosechas')}
            style={{
              background: activeReport === 'cosechas' ? '#8b5cf6' : '#e5e7eb',
              color: activeReport === 'cosechas' ? 'white' : '#374151',
              border: 'none',
              padding: '12px 20px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            🌾 Cosechas
          </button>
          {/* Reporte de Rentabilidad - Solo para usuarios con permiso financiero */}
          {tienePermisoFinanciero && (
            <button
              onClick={() => setActiveReport('rentabilidad')}
              style={{
                background: activeReport === 'rentabilidad' ? '#ef4444' : '#e5e7eb',
                color: activeReport === 'rentabilidad' ? 'white' : '#374151',
                border: 'none',
                padding: '12px 20px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px',
                fontWeight: 'bold'
              }}
            >
              💰 Rentabilidad
            </button>
          )}
        </div>

        {/* Filtros de fecha */}
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center', flexWrap: 'wrap' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha Inicio:</label>
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
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha Fin:</label>
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
              ⚠️ {errorFechas}
            </div>
          )}
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
            gap: '8px', 
            alignItems: 'center', 
            flexWrap: 'wrap',
            marginTop: '15px',
            padding: '12px',
            background: '#f8f9fa',
            borderRadius: '8px',
            border: '1px solid #e9ecef'
          }}>
            <span style={{ fontWeight: 'bold', color: '#495057', marginRight: '8px' }}>📤 Exportar:</span>
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
              📊 Excel
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
              📄 PDF
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
          </div>

          {/* Contenido específico del reporte */}
          {activeReport === 'rindes' && renderRindeReport()}
          {activeReport === 'produccion' && renderProduccionReport()}
          {activeReport === 'cosechas' && renderCosechasReport()}
          {activeReport === 'rentabilidad' && tienePermisoFinanciero && renderRentabilidadReport()}
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
