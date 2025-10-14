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
      const response = await api.get('/api/v1/lotes');
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
        url = `/api/v1/balance/general?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
      } else if (tipoReporte === 'lote') {
        url = `/api/v1/balance/lote/${loteId}?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;
      } else if (tipoReporte === 'mes-actual') {
        url = '/api/v1/balance/mes-actual';
      } else if (tipoReporte === 'año-actual') {
        url = '/api/v1/balance/año-actual';
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

          {/* Detalles del Balance */}
          <Card>
            <CardHeader>
              <CardTitle>Detalles del Balance</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {balance.detalles.map((detalle, index) => (
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
            </CardContent>
          </Card>
        </>
      )}
    </div>
  );
};

export default BalanceReport;
