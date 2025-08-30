import React, { useState, useEffect } from 'react';
import Button from './ui/Button';
import Input from './ui/Input';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Badge from './ui/Badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/Select';
import api from '../services/api';

interface Egreso {
  id: number;
  concepto: string;
  descripcion?: string;
  tipoEgreso: string;
  fechaEgreso: string;
  monto: number;
  unidadMedida?: string;
  cantidad?: number;
  proveedor?: string;
  estado: string;
  observaciones?: string;
  lote?: {
    id: number;
    nombre: string;
  };
  insumo?: {
    id: number;
    nombre: string;
  };
}

interface Lote {
  id: number;
  nombre: string;
}

interface Insumo {
  id: number;
  nombre: string;
  stockActual: number;
  unidad: string;
}

const EgresosManagement: React.FC = () => {
  const [egresos, setEgresos] = useState<Egreso[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  
  // Formulario
  const [formData, setFormData] = useState({
    concepto: '',
    descripcion: '',
    tipoEgreso: '',
    fechaEgreso: new Date().toISOString().split('T')[0],
    monto: '',
    unidadMedida: '',
    cantidad: '',
    proveedor: '',
    observaciones: '',
    loteId: '',
    insumoId: '',
    actualizarInsumo: false
  });

  const tiposEgreso = [
    { value: 'INSUMOS', label: 'Insumos' },
    { value: 'COMBUSTIBLE', label: 'Combustible' },
    { value: 'MANO_OBRA', label: 'Mano de Obra' },
    { value: 'MAQUINARIA', label: 'Maquinaria' },
    { value: 'SERVICIOS', label: 'Servicios' },
    { value: 'IMPUESTOS', label: 'Impuestos' },
    { value: 'OTROS_EGRESOS', label: 'Otros Egresos' }
  ];

  const estadosEgreso = [
    { value: 'REGISTRADO', label: 'Registrado' },
    { value: 'PAGADO', label: 'Pagado' },
    { value: 'CANCELADO', label: 'Cancelado' }
  ];

  useEffect(() => {
    cargarEgresos();
    cargarLotes();
    cargarInsumos();
  }, []);

  const cargarEgresos = async () => {
    try {
      setLoading(true);
      const response = await api.get('/api/v1/egresos');
      setEgresos(response.data);
    } catch (error) {
      console.error('Error cargando egresos:', error);
    } finally {
      setLoading(false);
    }
  };

  const cargarLotes = async () => {
    try {
      const response = await api.get('/api/public/campos');
      setLotes(response.data);
    } catch (error) {
      console.error('Error cargando lotes:', error);
    }
  };

  const cargarInsumos = async () => {
    try {
      const response = await api.get('/api/public/insumos');
      setInsumos(response.data);
    } catch (error) {
      console.error('Error cargando insumos:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      const egresoData = {
        concepto: formData.concepto,
        descripcion: formData.descripcion,
        tipoEgreso: formData.tipoEgreso,
        fechaEgreso: formData.fechaEgreso,
        monto: parseFloat(formData.monto),
        unidadMedida: formData.unidadMedida,
        cantidad: formData.cantidad ? parseFloat(formData.cantidad) : null,
        proveedor: formData.proveedor,
        observaciones: formData.observaciones,
        lote: formData.loteId ? { id: parseInt(formData.loteId) } : null,
        insumo: formData.insumoId ? { id: parseInt(formData.insumoId) } : null
      };

      await api.post('/api/v1/egresos', egresoData);
      
      // Limpiar formulario
      setFormData({
        concepto: '',
        descripcion: '',
        tipoEgreso: '',
        fechaEgreso: new Date().toISOString().split('T')[0],
        monto: '',
        unidadMedida: '',
        cantidad: '',
        proveedor: '',
        observaciones: '',
        loteId: '',
        insumoId: '',
        actualizarInsumo: false
      });
      
      setShowForm(false);
      cargarEgresos();
      cargarInsumos(); // Recargar insumos para ver cambios en stock
      
    } catch (error) {
      console.error('Error creando egreso:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm('¿Estás seguro de que quieres eliminar este egreso?')) {
      return;
    }
    
    try {
      await api.delete(`/api/v1/egresos/${id}`);
      cargarEgresos();
    } catch (error) {
      console.error('Error eliminando egreso:', error);
    }
  };

  const getEstadoColor = (estado: string) => {
    switch (estado) {
      case 'PAGADO': return 'green';
      case 'REGISTRADO': return 'yellow';
      case 'CANCELADO': return 'red';
      default: return 'gray';
    }
  };

  const getTipoColor = (tipo: string) => {
    switch (tipo) {
      case 'INSUMOS': return 'blue';
      case 'COMBUSTIBLE': return 'orange';
      case 'MANO_OBRA': return 'purple';
      case 'MAQUINARIA': return 'red';
      case 'SERVICIOS': return 'green';
      case 'IMPUESTOS': return 'yellow';
      default: return 'gray';
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('es-AR');
  };

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">💰 Gestión de Egresos</h1>
        <Button onClick={() => setShowForm(!showForm)}>
          {showForm ? '❌ Cancelar' : '➕ Nuevo Egreso'}
        </Button>
      </div>

      {/* Formulario de registro */}
      {showForm && (
        <Card className="mb-6">
          <div className="p-6">
            <h2 className="text-xl font-semibold mb-4">📝 Registrar Nuevo Egreso</h2>
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Concepto *
                  </label>
                  <Input
                    type="text"
                    value={formData.concepto}
                    onChange={(e) => setFormData({...formData, concepto: e.target.value})}
                    placeholder="Ej: Compra de fertilizante"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Egreso *
                  </label>
                  <Select
                    value={formData.tipoEgreso}
                    onValueChange={(value) => setFormData({...formData, tipoEgreso: value})}
                    placeholder="Seleccionar tipo"
                  >
                    <SelectContent>
                      {tiposEgreso.map(tipo => (
                        <SelectItem key={tipo.value} value={tipo.value}>
                          {tipo.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fecha *
                  </label>
                  <Input
                    type="date"
                    value={formData.fechaEgreso}
                    onChange={(e) => setFormData({...formData, fechaEgreso: e.target.value})}
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Monto *
                  </label>
                  <Input
                    type="number"
                    step="0.01"
                    value={formData.monto}
                    onChange={(e) => setFormData({...formData, monto: e.target.value})}
                    placeholder="0.00"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Lote (opcional)
                  </label>
                  <Select
                    value={formData.loteId}
                    onValueChange={(value) => setFormData({...formData, loteId: value})}
                    placeholder="Seleccionar lote"
                  >
                    <SelectContent>
                      <SelectItem value="">Sin lote específico</SelectItem>
                      {lotes.map(lote => (
                        <SelectItem key={lote.id} value={lote.id.toString()}>
                          {lote.nombre}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                                 <div>
                   <label className="block text-sm font-medium text-gray-700 mb-1">
                     Insumo (opcional)
                   </label>
                   <Select
                     value={formData.insumoId}
                     onValueChange={(value) => setFormData({...formData, insumoId: value})}
                     placeholder="Seleccionar insumo"
                   >
                     <SelectContent>
                       <SelectItem value="">Sin insumo específico</SelectItem>
                       {insumos.map(insumo => (
                         <SelectItem key={insumo.id} value={insumo.id.toString()}>
                           {insumo.nombre} ({insumo.stockActual} {insumo.unidad})
                         </SelectItem>
                       ))}
                     </SelectContent>
                   </Select>
                   <p className="text-xs text-gray-500 mt-1">
                     💡 Al registrar un egreso de insumos, el inventario se actualiza automáticamente
                   </p>
                 </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Unidad de Medida
                  </label>
                  <Input
                    type="text"
                    value={formData.unidadMedida}
                    onChange={(e) => setFormData({...formData, unidadMedida: e.target.value})}
                    placeholder="Ej: kg, litros, etc."
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Cantidad
                  </label>
                  <Input
                    type="number"
                    step="0.01"
                    value={formData.cantidad}
                    onChange={(e) => setFormData({...formData, cantidad: e.target.value})}
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Proveedor
                  </label>
                  <Input
                    type="text"
                    value={formData.proveedor}
                    onChange={(e) => setFormData({...formData, proveedor: e.target.value})}
                    placeholder="Nombre del proveedor"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descripción
                </label>
                <textarea
                  value={formData.descripcion}
                  onChange={(e) => setFormData({...formData, descripcion: e.target.value})}
                  placeholder="Descripción detallada del egreso..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Observaciones
                </label>
                <textarea
                  value={formData.observaciones}
                  onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                  placeholder="Observaciones adicionales..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={2}
                />
              </div>

              <div className="flex justify-end space-x-3">
                <Button type="button" onClick={() => setShowForm(false)}>
                  ❌ Cancelar
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading ? '⏳ Guardando...' : '💾 Guardar Egreso'}
                </Button>
              </div>
            </form>
          </div>
        </Card>
      )}

      {/* Lista de egresos */}
      <Card>
        <div className="p-6">
          <h2 className="text-xl font-semibold mb-4">📋 Lista de Egresos</h2>
          
          {loading ? (
            <div className="text-center py-8">
              <p>⏳ Cargando egresos...</p>
            </div>
          ) : egresos.length === 0 ? (
            <div className="text-center py-8">
              <p className="text-gray-500">No hay egresos registrados</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Concepto
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Tipo
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Fecha
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Monto
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Estado
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Lote
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Acciones
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {egresos.map((egreso) => (
                    <tr key={egreso.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div>
                          <div className="text-sm font-medium text-gray-900">
                            {egreso.concepto}
                          </div>
                          {egreso.descripcion && (
                            <div className="text-sm text-gray-500">
                              {egreso.descripcion}
                            </div>
                          )}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge color={getTipoColor(egreso.tipoEgreso)}>
                          {egreso.tipoEgreso.replace('_', ' ')}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {formatDate(egreso.fechaEgreso)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <span className="font-medium text-red-600">
                          {formatCurrency(egreso.monto)}
                        </span>
                        {egreso.cantidad && egreso.unidadMedida && (
                          <div className="text-xs text-gray-500">
                            {egreso.cantidad} {egreso.unidadMedida}
                          </div>
                        )}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <Badge color={getEstadoColor(egreso.estado)}>
                          {egreso.estado}
                        </Badge>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {egreso.lote ? egreso.lote.nombre : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <Button
                          onClick={() => handleDelete(egreso.id)}
                          className="text-red-600 hover:text-red-900"
                        >
                          🗑️ Eliminar
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </Card>
    </div>
  );
};

export default EgresosManagement;
