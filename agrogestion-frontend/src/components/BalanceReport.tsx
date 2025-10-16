import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Button from './ui/Button';
import Input from './ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/Select';
import Badge from './ui/Badge';
// Iconos simplificados sin lucide-react
const Calendar = () => <span>📅</span>;
const TrendingUp = () => <span>📈</span>;
const TrendingDown = () => <span>📉</span>;
const DollarSign = () => <span>💰</span>;
const BarChart3 = () => <span>📊</span>;
import api from '../services/api';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useCurrencyUpdate } from '../hooks/useCurrencyUpdate';

interface BalanceData {
  fechaInicio: string;
  fechaFin: string;
  totalIngresos: number;
  totalCostos: number;
  balanceNeto: number;
  margenBeneficio: number;
  detalles: DetalleBalance[];
}

interface DetalleBalance {
  tipo: 'INGRESO' | 'COSTO';
  concepto: string;
  fecha: string;
  monto: number;
  categoria: string;
  lote?: string;
  descripcion?: string;
}

const BalanceReport: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const [balance, setBalance] = useState<BalanceData | null>(null);
  const [loading, setLoading] = useState(false);
  
  // Forzar actualización cuando cambie la moneda
  useCurrencyUpdate();
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [tipoReporte, setTipoReporte] = useState('general');
  const [loteId, setLoteId] = useState('');
  const [lotes, setLotes] = useState<any[]>([]);
  const [errorFechas, setErrorFechas] = useState<string>('');

  // Estados para paginación, filtrado y búsqueda
  const [busqueda, setBusqueda] = useState('');
  const [filtroTipo, setFiltroTipo] = useState('TODOS');
  const [filtroCategoria, setFiltroCategoria] = useState('TODAS');
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);

  // Función para filtrar y paginar los detalles
  const obtenerDetallesFiltrados = () => {
    if (!balance) return { detalles: [], totalPaginas: 0, totalElementos: 0 };

    let detallesFiltrados = balance.detalles;

    // Aplicar filtro de búsqueda
    if (busqueda) {
      detallesFiltrados = detallesFiltrados.filter(detalle =>
        detalle.concepto.toLowerCase().includes(busqueda.toLowerCase()) ||
        detalle.categoria.toLowerCase().includes(busqueda.toLowerCase()) ||
        (detalle.lote && detalle.lote.toLowerCase().includes(busqueda.toLowerCase()))
      );
    }

    // Aplicar filtro de tipo
    if (filtroTipo !== 'TODOS') {
      detallesFiltrados = detallesFiltrados.filter(detalle => detalle.tipo === filtroTipo);
    }

    // Aplicar filtro de categoría
    if (filtroCategoria !== 'TODAS') {
      detallesFiltrados = detallesFiltrados.filter(detalle => detalle.categoria === filtroCategoria);
    }

    const totalElementos = detallesFiltrados.length;
    const totalPaginas = Math.ceil(totalElementos / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const detallesPaginados = detallesFiltrados.slice(inicio, fin);

    return { detalles: detallesPaginados, totalPaginas, totalElementos };
  };

  // Obtener categorías únicas para el filtro
  const obtenerCategoriasUnicas = () => {
    if (!balance) return [];
    const categorias = [...new Set(balance.detalles.map(d => d.categoria))];
    return categorias.sort();
  };

  // Resetear paginación cuando cambien los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [busqueda, filtroTipo, filtroCategoria]);

  useEffect(() => {
    // Establecer fechas por defecto (mes actual)
    const hoy = new Date();
    const primerDiaMes = new Date(hoy.getFullYear(), hoy.getMonth(), 1);
    const ultimoDiaMes = new Date(hoy.getFullYear(), hoy.getMonth() + 1, 0);
    
    setFechaInicio(primerDiaMes.toISOString().split('T')[0]);
    setFechaFin(ultimoDiaMes.toISOString().split('T')[0]);
    
    cargarLotes();
  }, []);

  const cargarLotes = async () => {
    try {
      const response = await api.get('/v1/lotes');
      setLotes(response.data);
    } catch (error) {
      console.error('Error cargando lotes:', error);
    }
  };

  const generarReporte = async () => {
    // Validar fechas antes de generar el reporte
    if ((tipoReporte === 'general' || tipoReporte === 'lote') && fechaInicio && fechaFin) {
      const inicio = new Date(fechaInicio);
      const fin = new Date(fechaFin);
      
      if (fin < inicio) {
        setErrorFechas('La fecha de fin no puede ser anterior a la fecha de inicio');
        alert('Error: La fecha de fin no puede ser anterior a la fecha de inicio');
        return;
      }
    }
    
    setErrorFechas('');
    setLoading(true);
    try {
      let url = '';
      if (tipoReporte === 'general') {
        url = `/v1/balance/general?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
      } else if (tipoReporte === 'lote') {
        url = `/v1/balance/lote/${loteId}?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
      } else if (tipoReporte === 'mes-actual') {
        url = '/v1/balance/mes-actual';
      } else if (tipoReporte === 'año-actual') {
        url = '/v1/balance/año-actual';
      }

      const response = await api.get(url);
      setBalance(response.data);
    } catch (error) {
      console.error('Error generando reporte:', error);
      alert('Error al generar el reporte de balance');
    } finally {
      setLoading(false);
    }
  };

  // Función para exportar el balance a Excel
  const exportarExcel = () => {
    if (!balance) return;
    
    const fechaActual = new Date().toLocaleDateString('es-ES');
    const nombreReporte = `Balance_${tipoReporte}_${fechaActual.replace(/\//g, '-')}`;
    
    // Crear contenido HTML para Excel
    const contenido = `
      <html>
        <head>
          <meta charset="utf-8">
          <title>${nombreReporte}</title>
        </head>
        <body>
          <h1>Reporte de Balance - AgroCloud</h1>
          <p><strong>Generado el:</strong> ${fechaActual}</p>
          <p><strong>Tipo de Reporte:</strong> ${tipoReporte}</p>
          ${fechaInicio && fechaFin ? `<p><strong>Período:</strong> ${fechaInicio} - ${fechaFin}</p>` : ''}
          
          <h2>Resumen del Balance</h2>
          <table border="1" style="border-collapse: collapse; width: 100%;">
            <tr>
              <th style="background-color: #f0f0f0; padding: 8px;">Concepto</th>
              <th style="background-color: #f0f0f0; padding: 8px;">Monto</th>
            </tr>
            <tr>
              <td style="padding: 8px;">Total Ingresos</td>
              <td style="padding: 8px; color: green;">${formatCurrency(balance.totalIngresos)}</td>
            </tr>
            <tr>
              <td style="padding: 8px;">Total Costos</td>
              <td style="padding: 8px; color: red;">${formatCurrency(balance.totalCostos)}</td>
            </tr>
            <tr>
              <td style="padding: 8px; font-weight: bold;">Balance Neto</td>
              <td style="padding: 8px; font-weight: bold; color: ${balance.balanceNeto >= 0 ? 'green' : 'red'};">${formatCurrency(balance.balanceNeto)}</td>
            </tr>
            <tr>
              <td style="padding: 8px;">Margen de Beneficio</td>
              <td style="padding: 8px;">${balance.margenBeneficio.toFixed(2)}%</td>
            </tr>
          </table>
          
          <h2>Detalles</h2>
          <table border="1" style="border-collapse: collapse; width: 100%;">
            <tr>
              <th style="background-color: #f0f0f0; padding: 8px;">Tipo</th>
              <th style="background-color: #f0f0f0; padding: 8px;">Concepto</th>
              <th style="background-color: #f0f0f0; padding: 8px;">Fecha</th>
              <th style="background-color: #f0f0f0; padding: 8px;">Monto</th>
              <th style="background-color: #f0f0f0; padding: 8px;">Categoría</th>
              ${tipoReporte === 'lote' ? '<th style="background-color: #f0f0f0; padding: 8px;">Lote</th>' : ''}
            </tr>
            ${balance.detalles.map(detalle => `
              <tr>
                <td style="padding: 8px;">${detalle.tipo}</td>
                <td style="padding: 8px;">${detalle.concepto}</td>
                <td style="padding: 8px;">${detalle.fecha}</td>
                <td style="padding: 8px; color: ${detalle.tipo === 'INGRESO' ? 'green' : 'red'};">${formatCurrency(detalle.monto)}</td>
                <td style="padding: 8px;">${detalle.categoria}</td>
                ${tipoReporte === 'lote' ? `<td style="padding: 8px;">${detalle.lote || '-'}</td>` : ''}
              </tr>
            `).join('')}
          </table>
        </body>
      </html>
    `;
    
    const blob = new Blob([contenido], { type: 'application/vnd.ms-excel' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${nombreReporte}.xls`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  // Función para exportar el balance a PDF
  const exportarPDF = () => {
    if (!balance) return;
    
    const fechaActual = new Date().toLocaleDateString('es-ES');
    const nombreReporte = `Balance_${tipoReporte}_${fechaActual.replace(/\//g, '-')}`;
    
    // Crear contenido para PDF
    const contenido = `
      <div style="font-family: Arial, sans-serif; padding: 20px;">
        <div style="text-align: center; margin-bottom: 30px;">
          <h1 style="color: #2c3e50; margin-bottom: 10px;">Reporte de Balance - AgroCloud</h1>
          <p style="color: #7f8c8d;">Generado el ${fechaActual}</p>
          <p style="color: #7f8c8d;">Tipo de Reporte: ${tipoReporte}</p>
          ${fechaInicio && fechaFin ? `<p style="color: #7f8c8d;">Período: ${fechaInicio} - ${fechaFin}</p>` : ''}
        </div>
        
        <div style="margin-bottom: 30px;">
          <h2 style="color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 5px;">Resumen del Balance</h2>
          <table style="width: 100%; border-collapse: collapse; margin-top: 15px;">
            <tr style="background-color: #ecf0f1;">
              <th style="border: 1px solid #bdc3c7; padding: 12px; text-align: left;">Concepto</th>
              <th style="border: 1px solid #bdc3c7; padding: 12px; text-align: right;">Monto</th>
            </tr>
            <tr>
              <td style="border: 1px solid #bdc3c7; padding: 12px;">Total Ingresos</td>
              <td style="border: 1px solid #bdc3c7; padding: 12px; text-align: right; color: #27ae60; font-weight: bold;">${formatCurrency(balance.totalIngresos)}</td>
            </tr>
            <tr>
              <td style="border: 1px solid #bdc3c7; padding: 12px;">Total Costos</td>
              <td style="border: 1px solid #bdc3c7; padding: 12px; text-align: right; color: #e74c3c; font-weight: bold;">${formatCurrency(balance.totalCostos)}</td>
            </tr>
            <tr style="background-color: #f8f9fa;">
              <td style="border: 1px solid #bdc3c7; padding: 12px; font-weight: bold;">Balance Neto</td>
              <td style="border: 1px solid #bdc3c7; padding: 12px; text-align: right; font-weight: bold; color: ${balance.balanceNeto >= 0 ? '#27ae60' : '#e74c3c'};">${formatCurrency(balance.balanceNeto)}</td>
            </tr>
            <tr>
              <td style="border: 1px solid #bdc3c7; padding: 12px;">Margen de Beneficio</td>
              <td style="border: 1px solid #bdc3c7; padding: 12px; text-align: right;">${balance.margenBeneficio.toFixed(2)}%</td>
            </tr>
          </table>
        </div>
        
        <div>
          <h2 style="color: #34495e; border-bottom: 2px solid #3498db; padding-bottom: 5px;">Detalles</h2>
          <table style="width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 12px;">
            <tr style="background-color: #ecf0f1;">
              <th style="border: 1px solid #bdc3c7; padding: 8px; text-align: left;">Tipo</th>
              <th style="border: 1px solid #bdc3c7; padding: 8px; text-align: left;">Concepto</th>
              <th style="border: 1px solid #bdc3c7; padding: 8px; text-align: left;">Fecha</th>
              <th style="border: 1px solid #bdc3c7; padding: 8px; text-align: right;">Monto</th>
              <th style="border: 1px solid #bdc3c7; padding: 8px; text-align: left;">Categoría</th>
              ${tipoReporte === 'lote' ? '<th style="border: 1px solid #bdc3c7; padding: 8px; text-align: left;">Lote</th>' : ''}
            </tr>
            ${balance.detalles.map(detalle => `
              <tr>
                <td style="border: 1px solid #bdc3c7; padding: 8px;">${detalle.tipo}</td>
                <td style="border: 1px solid #bdc3c7; padding: 8px;">${detalle.concepto}</td>
                <td style="border: 1px solid #bdc3c7; padding: 8px;">${detalle.fecha}</td>
                <td style="border: 1px solid #bdc3c7; padding: 8px; text-align: right; color: ${detalle.tipo === 'INGRESO' ? '#27ae60' : '#e74c3c'};">${formatCurrency(detalle.monto)}</td>
                <td style="border: 1px solid #bdc3c7; padding: 8px;">${detalle.categoria}</td>
                ${tipoReporte === 'lote' ? `<td style="border: 1px solid #bdc3c7; padding: 8px;">${detalle.lote || '-'}</td>` : ''}
              </tr>
            `).join('')}
          </table>
        </div>
      </div>
    `;
    
    const ventana = window.open('', '_blank');
    if (ventana) {
      ventana.document.write(contenido);
      ventana.document.close();
      ventana.print();
    }
  };

  // Función para exportar el balance a CSV
  const exportarCSV = () => {
    if (!balance) return;
    
    const fechaActual = new Date().toLocaleDateString('es-ES');
    const nombreReporte = `Balance_${tipoReporte}_${fechaActual.replace(/\//g, '-')}`;
    
    // Crear contenido CSV
    let csvContent = 'Tipo,Concepto,Fecha,Monto,Categoría';
    if (tipoReporte === 'lote') {
      csvContent += ',Lote';
    }
    csvContent += '\n';
    
    // Agregar resumen
    csvContent += `RESUMEN,Total Ingresos,${fechaActual},${balance.totalIngresos},Ingresos\n`;
    csvContent += `RESUMEN,Total Costos,${fechaActual},${balance.totalCostos},Costos\n`;
    csvContent += `RESUMEN,Balance Neto,${fechaActual},${balance.balanceNeto},Balance\n`;
    csvContent += `RESUMEN,Margen Beneficio,${fechaActual},${balance.margenBeneficio}%,Margen\n`;
    csvContent += '\n';
    
    // Agregar detalles
    balance.detalles.forEach(detalle => {
      csvContent += `${detalle.tipo},${detalle.concepto},${detalle.fecha},${detalle.monto},${detalle.categoria}`;
      if (tipoReporte === 'lote') {
        csvContent += `,${detalle.lote || ''}`;
      }
      csvContent += '\n';
    });
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${nombreReporte}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  };

  const formatearFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-AR');
  };

  const obtenerColorBalance = (balanceNeto: number) => {
    return balanceNeto >= 0 ? 'text-green-600' : 'text-red-600';
  };

  const obtenerIconoBalance = (balanceNeto: number) => {
    return balanceNeto >= 0 ? <TrendingUp className="w-5 h-5" /> : <TrendingDown className="w-5 h-5" />;
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <BarChart3 className="w-6 h-6" />
            Balance de Costos y Beneficios
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <div>
              <label className="block text-sm font-medium mb-2">Tipo de Reporte</label>
              <Select value={tipoReporte} onValueChange={setTipoReporte}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="general">Balance General</SelectItem>
                  <SelectItem value="lote">Balance por Lote</SelectItem>
                  <SelectItem value="mes-actual">Mes Actual</SelectItem>
                  <SelectItem value="año-actual">Año Actual</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {tipoReporte === 'lote' && (
              <div>
                <label className="block text-sm font-medium mb-2">Lote</label>
                <Select value={loteId} onValueChange={setLoteId}>
                  <SelectTrigger>
                    <SelectValue placeholder="Seleccionar lote" />
                  </SelectTrigger>
                  <SelectContent>
                    {lotes.map((lote) => (
                      <SelectItem key={lote.id} value={lote.id.toString()}>
                        {lote.nombre}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            )}

            {(tipoReporte === 'general' || tipoReporte === 'lote') && (
              <>
                <div>
                  <label className="block text-sm font-medium mb-2">Fecha Inicio</label>
                  <Input
                    type="date"
                    value={fechaInicio}
                    onChange={(value) => {
                      setFechaInicio(value);
                      setErrorFechas('');
                    }}
                    className={errorFechas ? 'border-red-500' : ''}
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-2">Fecha Fin</label>
                  <Input
                    type="date"
                    value={fechaFin}
                    onChange={(value) => {
                      setFechaFin(value);
                      setErrorFechas('');
                    }}
                    min={fechaInicio}
                    className={errorFechas ? 'border-red-500' : ''}
                  />
                  {errorFechas && (
                    <p className="text-sm text-red-600 mt-1">⚠️ {errorFechas}</p>
                  )}
                </div>
              </>
            )}
          </div>

          <Button 
            onClick={generarReporte} 
            disabled={loading || (tipoReporte === 'lote' && !loteId)}
            className="w-full"
          >
            {loading ? 'Generando...' : 'Generar Reporte'}
          </Button>
        </CardContent>
      </Card>

      {/* Botones de exportación */}
      {balance && (
        <Card className="mb-6">
          <CardContent className="p-4">
            <div style={{ 
              display: 'flex', 
              gap: '8px', 
              alignItems: 'center', 
              flexWrap: 'wrap'
            }}>
              <span style={{ fontWeight: 'bold', color: '#495057', marginRight: '8px' }}>📤 Exportar Balance:</span>
              <button
                onClick={exportarExcel}
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
                onClick={exportarPDF}
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
                onClick={exportarCSV}
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
          </CardContent>
        </Card>
      )}

      {balance && (
        <>
          {/* Resumen del Balance */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Total Ingresos</p>
                    <p className="text-2xl font-bold text-green-600">
                      {formatCurrency(balance.totalIngresos)}
                    </p>
                  </div>
                  <DollarSign className="w-8 h-8 text-green-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Total Costos</p>
                    <p className="text-2xl font-bold text-red-600">
                      {formatCurrency(balance.totalCostos)}
                    </p>
                  </div>
                  <DollarSign className="w-8 h-8 text-red-600" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Balance Neto</p>
                    <p className={`text-2xl font-bold ${obtenerColorBalance(balance.balanceNeto)}`}>
                      {formatCurrency(balance.balanceNeto)}
                    </p>
                  </div>
                  {obtenerIconoBalance(balance.balanceNeto)}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardContent className="p-4">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm text-gray-600">Margen Beneficio</p>
                    <p className={`text-2xl font-bold ${obtenerColorBalance(balance.margenBeneficio)}`}>
                      {balance.margenBeneficio.toFixed(2)}%
                    </p>
                  </div>
                  <TrendingUp className="w-8 h-8 text-blue-600" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Período del Reporte */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Calendar className="w-5 h-5" />
                Período del Reporte
              </CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-lg">
                Desde: <strong>{formatearFecha(balance.fechaInicio)}</strong> hasta: <strong>{formatearFecha(balance.fechaFin)}</strong>
              </p>
            </CardContent>
          </Card>

          {/* Gráficos de Distribución por Tipo */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
            {/* Gráfico de Ingresos por Tipo */}
            <Card>
              <CardHeader>
                <CardTitle>📊 Ingresos por Tipo</CardTitle>
              </CardHeader>
              <CardContent>
                {(() => {
                  const ingresos = balance.detalles.filter(d => d.tipo === 'INGRESO');
                  const ingresosPorCategoria = ingresos.reduce((acc, detalle) => {
                    const categoria = detalle.categoria || 'OTROS';
                    acc[categoria] = (acc[categoria] || 0) + detalle.monto;
                    return acc;
                  }, {} as Record<string, number>);
                  
                  const totalIngresos = Object.values(ingresosPorCategoria).reduce((sum, val) => sum + val, 0);
                  
                  return (
                    <div className="space-y-3">
                      {Object.entries(ingresosPorCategoria).map(([categoria, monto]) => {
                        const porcentaje = totalIngresos > 0 ? (monto / totalIngresos) * 100 : 0;
                        return (
                          <div key={categoria} className="space-y-1">
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-700">{categoria.replace('_', ' ')}</span>
                              <span className="font-medium">{formatCurrency(monto)}</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className="bg-green-600 h-2 rounded-full transition-all" 
                                style={{width: `${porcentaje}%`}}
                              />
                            </div>
                            <div className="text-xs text-gray-500 text-right">
                              {porcentaje.toFixed(1)}%
                            </div>
                          </div>
                        );
                      })}
                      {Object.keys(ingresosPorCategoria).length === 0 && (
                        <p className="text-gray-500 text-sm text-center py-4">No hay ingresos en este período</p>
                      )}
                    </div>
                  );
                })()}
              </CardContent>
            </Card>

            {/* Gráfico de Costos por Tipo */}
            <Card>
              <CardHeader>
                <CardTitle>📊 Costos por Tipo</CardTitle>
              </CardHeader>
              <CardContent>
                {(() => {
                  const costos = balance.detalles.filter(d => d.tipo === 'COSTO' || d.tipo === 'EGRESO');
                  const costosPorCategoria = costos.reduce((acc, detalle) => {
                    const categoria = detalle.categoria || 'OTROS';
                    acc[categoria] = (acc[categoria] || 0) + detalle.monto;
                    return acc;
                  }, {} as Record<string, number>);
                  
                  const totalCostos = Object.values(costosPorCategoria).reduce((sum, val) => sum + val, 0);
                  
                  return (
                    <div className="space-y-3">
                      {Object.entries(costosPorCategoria).map(([categoria, monto]) => {
                        const porcentaje = totalCostos > 0 ? (monto / totalCostos) * 100 : 0;
                        return (
                          <div key={categoria} className="space-y-1">
                            <div className="flex justify-between text-sm">
                              <span className="text-gray-700">{categoria.replace('LABOR - ', '').replace('_', ' ')}</span>
                              <span className="font-medium">{formatCurrency(monto)}</span>
                            </div>
                            <div className="w-full bg-gray-200 rounded-full h-2">
                              <div 
                                className="bg-red-600 h-2 rounded-full transition-all" 
                                style={{width: `${porcentaje}%`}}
                              />
                            </div>
                            <div className="text-xs text-gray-500 text-right">
                              {porcentaje.toFixed(1)}%
                            </div>
                          </div>
                        );
                      })}
                      {Object.keys(costosPorCategoria).length === 0 && (
                        <p className="text-gray-500 text-sm text-center py-4">No hay costos en este período</p>
                      )}
                    </div>
                  );
                })()}
              </CardContent>
            </Card>
          </div>

          {/* Filtros y Búsqueda */}
          <Card className="mb-6">
            <CardHeader>
              <CardTitle>🔍 Filtros y Búsqueda</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                {/* Búsqueda */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Buscar
                  </label>
                  <Input
                    type="text"
                    placeholder="Buscar por concepto, categoría o lote..."
                    value={busqueda}
                    onChange={(e) => setBusqueda(e.target.value)}
                    className="w-full h-10"
                  />
                </div>

                {/* Filtro por Tipo */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo
                  </label>
                  <Select value={filtroTipo} onValueChange={setFiltroTipo}>
                    <SelectTrigger className="h-10">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="TODOS">Todos</SelectItem>
                      <SelectItem value="INGRESO">Ingresos</SelectItem>
                      <SelectItem value="COSTO">Costos</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                {/* Filtro por Categoría */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Categoría
                  </label>
                  <Select value={filtroCategoria} onValueChange={setFiltroCategoria}>
                    <SelectTrigger className="h-10">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="TODAS">Todas</SelectItem>
                      {obtenerCategoriasUnicas().map(categoria => (
                        <SelectItem key={categoria} value={categoria}>
                          {categoria.replace('_', ' ')}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                {/* Botón Limpiar Filtros */}
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    &nbsp;
                  </label>
                  <Button
                    variant="outline"
                    onClick={() => {
                      setBusqueda('');
                      setFiltroTipo('TODOS');
                      setFiltroCategoria('TODAS');
                    }}
                    className="w-full h-10 text-sm"
                    size="sm"
                  >
                    🗑️ Limpiar
                  </Button>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Detalles del Balance */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center justify-between">
                <span>Detalles del Balance</span>
                <span className="text-sm text-gray-500">
                  {(() => {
                    const { totalElementos } = obtenerDetallesFiltrados();
                    return `${totalElementos} elemento${totalElementos !== 1 ? 's' : ''}`;
                  })()}
                </span>
              </CardTitle>
            </CardHeader>
            <CardContent>
              {(() => {
                const { detalles, totalElementos } = obtenerDetallesFiltrados();
                
                if (totalElementos === 0) {
                  return (
                    <div className="text-center py-8">
                      <p className="text-gray-500">No se encontraron elementos con los filtros aplicados</p>
                    </div>
                  );
                }

                return (
                  <>
                    <div className="space-y-4">
                      {detalles.map((detalle, index) => (
                        <div key={index} className="flex items-center justify-between p-4 border rounded-lg">
                          <div className="flex items-center gap-4">
                            <Badge variant={detalle.tipo === 'INGRESO' ? 'default' : 'destructive'}>
                              {detalle.tipo}
                            </Badge>
                            <div>
                              <p className="font-medium">{detalle.concepto}</p>
                              <p className="text-sm text-gray-600">
                                {formatearFecha(detalle.fecha)} • {detalle.categoria}
                                {detalle.lote && ` • Lote: ${detalle.lote}`}
                              </p>
                              {detalle.descripcion && (
                                <p className="text-sm text-gray-500">{detalle.descripcion}</p>
                              )}
                            </div>
                          </div>
                          <div className="text-right">
                            <p className={`font-bold ${detalle.tipo === 'INGRESO' ? 'text-green-600' : 'text-red-600'}`}>
                              {detalle.tipo === 'INGRESO' ? '+' : '-'} {formatCurrency(detalle.monto)}
                            </p>
                          </div>
                        </div>
                      ))}
                    </div>

                    {/* Paginación */}
                    {(() => {
                      const { totalPaginas } = obtenerDetallesFiltrados();
                      if (totalPaginas <= 1) return null;

                      return (
                        <div className="flex items-center justify-between mt-6 pt-4 border-t">
                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => setPaginaActual(1)}
                              disabled={paginaActual === 1}
                            >
                              ⏮️ Primera
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => setPaginaActual(paginaActual - 1)}
                              disabled={paginaActual === 1}
                            >
                              ⬅️ Anterior
                            </Button>
                          </div>

                          <div className="flex items-center gap-2">
                            <span className="text-sm text-gray-600">
                              Página {paginaActual} de {totalPaginas}
                            </span>
                          </div>

                          <div className="flex items-center gap-2">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => setPaginaActual(paginaActual + 1)}
                              disabled={paginaActual === totalPaginas}
                            >
                              Siguiente ➡️
                            </Button>
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => setPaginaActual(totalPaginas)}
                              disabled={paginaActual === totalPaginas}
                            >
                              Última ⏭️
                            </Button>
                          </div>
                        </div>
                      );
                    })()}
                  </>
                );
              })()}
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
};

export default BalanceReport;
