import React, { useState, useEffect } from 'react';
import Button from './ui/Button';
import Input from './ui/Input';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Badge from './ui/Badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/Select';
import { egresosService, camposService, insumosService } from '../services/apiServices';

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
  estado: 'REGISTRADO' | 'CONFIRMADO' | 'PAGADO' | 'CANCELADO';
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
    actualizarInsumo: false,
    // Campos para maquinaria
    marca: '',
    modelo: '',
    // Campos para alquiler
    maquinariaId: '',
    fechaInicio: new Date().toISOString().split('T')[0],
    fechaFin: new Date().toISOString().split('T')[0],
    costoDia: ''
  });

  const tiposEgreso = [
    { value: 'INSUMO', label: 'Insumos' },
    { value: 'MAQUINARIA_COMPRA', label: 'Compra de Maquinaria' },
    { value: 'MAQUINARIA_ALQUILER', label: 'Alquiler de Maquinaria' },
    { value: 'SERVICIO', label: 'Servicios' },
    { value: 'OTROS', label: 'Otros Egresos' }
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
      const data = await egresosService.listarV1();
      setEgresos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando egresos:', error);
    } finally {
      setLoading(false);
    }
  };

  const cargarLotes = async () => {
    try {
      const data = await camposService.listar();
      setLotes(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando lotes:', error);
    }
  };

  const cargarInsumos = async () => {
    try {
      const data = await insumosService.listar();
      setInsumos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando insumos:', error);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Preparar datos según el tipo de egreso
      let egresoData: any = {
        tipoEgreso: formData.tipoEgreso,
        fechaEgreso: formData.fechaEgreso,
        observaciones: formData.observaciones,
        loteId: formData.loteId ? parseInt(formData.loteId) : null
      };

      switch (formData.tipoEgreso) {
        case 'INSUMO':
          egresoData.insumoId = parseInt(formData.insumoId);
          egresoData.cantidad = parseFloat(formData.cantidad);
          break;
        case 'MAQUINARIA_COMPRA':
          egresoData.concepto = formData.concepto;
          egresoData.marca = formData.marca;
          egresoData.modelo = formData.modelo;
          egresoData.monto = parseFloat(formData.monto);
          break;
        case 'MAQUINARIA_ALQUILER':
          egresoData.maquinariaId = parseInt(formData.maquinariaId);
          egresoData.fechaInicio = formData.fechaInicio;
          egresoData.fechaFin = formData.fechaFin;
          egresoData.costoDia = parseFloat(formData.costoDia);
          egresoData.monto = parseFloat(formData.monto);
          break;
        default:
          egresoData.concepto = formData.concepto;
          egresoData.monto = parseFloat(formData.monto);
          break;
      }

      // Usar el endpoint integrado
      await egresosService.crearIntegrado(egresoData);
      
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
        actualizarInsumo: false,
        marca: '',
        modelo: '',
        maquinariaId: '',
        fechaInicio: new Date().toISOString().split('T')[0],
        fechaFin: new Date().toISOString().split('T')[0],
        costoDia: ''
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
      await egresosService.eliminarV1(id);
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
      case 'INSUMO': return 'blue';
      case 'MAQUINARIA_COMPRA': return 'red';
      case 'MAQUINARIA_ALQUILER': return 'orange';
      case 'SERVICIO': return 'green';
      case 'OTROS': return 'gray';
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
                {/* Paso 1: Selección del tipo de egreso */}
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
                    onChange={(valor) => setFormData({...formData, fechaEgreso: valor})}
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

                {/* Paso 2: Campos dinámicos según el tipo de egreso */}
                
                {/* INSUMO → seleccionar insumo, cantidad, costo se calcula automáticamente */}
                {formData.tipoEgreso === 'INSUMO' && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Insumo *
                      </label>
                      <Select
                        value={formData.insumoId}
                        onValueChange={(value) => setFormData({...formData, insumoId: value})}
                        placeholder="Seleccionar insumo"
                      >
                        <SelectContent>
                          {insumos.map(insumo => (
                            <SelectItem key={insumo.id} value={insumo.id.toString()}>
                              {insumo.nombre} - Stock: {insumo.stockActual} {insumo.unidad}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cantidad *
                      </label>
                      <Input
                        type="number"
                        step="0.01"
                        value={formData.cantidad}
                        onChange={(valor) => setFormData({...formData, cantidad: valor})}
                        placeholder="0.00"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <p className="text-xs text-blue-600 bg-blue-50 p-2 rounded">
                        💡 El costo total se calculará automáticamente basado en el precio histórico del insumo
                      </p>
                    </div>
                  </>
                )}

                {/* MAQUINARIA_COMPRA → registrar maquinaria y costo */}
                {formData.tipoEgreso === 'MAQUINARIA_COMPRA' && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Nombre de la Maquinaria *
                      </label>
                      <Input
                        type="text"
                        value={formData.concepto}
                        onChange={(valor) => setFormData({...formData, concepto: valor})}
                        placeholder="Ej: Tractor John Deere"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Marca *
                      </label>
                      <Input
                        type="text"
                        value={formData.marca}
                        onChange={(valor) => setFormData({...formData, marca: valor})}
                        placeholder="Marca de la maquinaria"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Modelo *
                      </label>
                      <Input
                        type="text"
                        value={formData.modelo}
                        onChange={(valor) => setFormData({...formData, modelo: valor})}
                        placeholder="Modelo de la maquinaria"
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Costo de Compra *
                      </label>
                      <Input
                        type="number"
                        step="0.01"
                        value={formData.monto}
                        onChange={(valor) => setFormData({...formData, monto: valor})}
                        placeholder="0.00"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <p className="text-xs text-green-600 bg-green-50 p-2 rounded">
                        💡 Se creará automáticamente un registro de maquinaria en el inventario
                      </p>
                    </div>
                  </>
                )}

                {/* MAQUINARIA_ALQUILER → seleccionar maquinaria, fecha inicio, fin, costo */}
                {formData.tipoEgreso === 'MAQUINARIA_ALQUILER' && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Maquinaria a Alquilar *
                      </label>
                      <Select
                        value={formData.maquinariaId}
                        onValueChange={(value) => setFormData({...formData, maquinariaId: value})}
                        placeholder="Seleccionar maquinaria"
                      >
                        <SelectContent>
                          {/* Aquí deberías cargar la lista de maquinaria disponible */}
                          <SelectItem value="1">Tractor John Deere</SelectItem>
                          <SelectItem value="2">Cosechadora New Holland</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Fecha Inicio *
                      </label>
                      <Input
                        type="date"
                        value={formData.fechaInicio}
                        onChange={(valor) => setFormData({...formData, fechaInicio: valor})}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Fecha Fin *
                      </label>
                      <Input
                        type="date"
                        value={formData.fechaFin}
                        onChange={(valor) => setFormData({...formData, fechaFin: valor})}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Costo por Día *
                      </label>
                      <Input
                        type="number"
                        step="0.01"
                        value={formData.costoDia}
                        onChange={(valor) => setFormData({...formData, costoDia: valor})}
                        placeholder="0.00"
                        required
                      />
                    </div>
                    <div className="col-span-2">
                      <p className="text-xs text-orange-600 bg-orange-50 p-2 rounded">
                        💡 Se registrará automáticamente el alquiler y se calculará el costo total
                      </p>
                    </div>
                  </>
                )}

                {/* SERVICIO/OTROS → descripción libre, costo */}
                {(formData.tipoEgreso === 'SERVICIO' || formData.tipoEgreso === 'OTROS') && (
                  <>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Concepto *
                      </label>
                      <Input
                        type="text"
                        value={formData.concepto}
                        onChange={(valor) => setFormData({...formData, concepto: valor})}
                        placeholder="Descripción del servicio o gasto"
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
                        onChange={(valor) => setFormData({...formData, monto: valor})}
                        placeholder="0.00"
                        required
                      />
                    </div>
                  </>
                )}
              </div>

              {/* Campo de observaciones común para todos los tipos */}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Observaciones
                </label>
                <textarea
                  value={formData.observaciones}
                  onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                  placeholder="Observaciones adicionales, detalles del proveedor, notas importantes..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
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
