import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Button from './ui/Button';
import Input from './ui/Input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/Select';
import Badge from './ui/Badge';
// Iconos simplificados sin lucide-react
const Plus = () => <span>➕</span>;
const Edit = () => <span>✏️</span>;
const Trash2 = () => <span>🗑️</span>;
const DollarSign = () => <span>💰</span>;
const Calendar = () => <span>📅</span>;
import api from '../services/api';

interface Ingreso {
  id?: number;
  concepto: string;
  descripcion?: string;
  tipoIngreso: 'VENTA_CULTIVO' | 'VENTA_ANIMAL' | 'SERVICIOS_AGRICOLAS' | 'SUBSIDIOS' | 'OTROS_INGRESOS';
  fechaIngreso: string;
  monto: number;
  unidadMedida?: string;
  cantidad?: number;
  clienteComprador?: string;
  estado: 'REGISTRADO' | 'CONFIRMADO' | 'CANCELADO';
  observaciones?: string;
  lote?: {
    id: number;
    nombre: string;
  };
}

const IngresosManagement: React.FC = () => {
  const [ingresos, setIngresos] = useState<Ingreso[]>([]);
  const [lotes, setLotes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [editingIngreso, setEditingIngreso] = useState<Ingreso | null>(null);
  const [formData, setFormData] = useState<Ingreso>({
    concepto: '',
    descripcion: '',
    tipoIngreso: 'VENTA_CULTIVO',
    fechaIngreso: new Date().toISOString().split('T')[0],
    monto: 0,
    unidadMedida: '',
    cantidad: 0,
    clienteComprador: '',
    estado: 'REGISTRADO',
    observaciones: ''
  });

  useEffect(() => {
    cargarIngresos();
    cargarLotes();
  }, []);

  const cargarIngresos = async () => {
    setLoading(true);
    try {
      const response = await api.get('/api/v1/ingresos');
      setIngresos(response.data);
    } catch (error) {
      console.error('Error cargando ingresos:', error);
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      if (editingIngreso?.id) {
        await api.put(`/api/v1/ingresos/${editingIngreso.id}`, formData);
      } else {
        await api.post('/api/v1/ingresos', formData);
      }
      cargarIngresos();
      resetForm();
    } catch (error) {
      console.error('Error guardando ingreso:', error);
      alert('Error al guardar el ingreso');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (ingreso: Ingreso) => {
    setEditingIngreso(ingreso);
    setFormData({
      concepto: ingreso.concepto,
      descripcion: ingreso.descripcion || '',
      tipoIngreso: ingreso.tipoIngreso,
      fechaIngreso: ingreso.fechaIngreso,
      monto: ingreso.monto,
      unidadMedida: ingreso.unidadMedida || '',
      cantidad: ingreso.cantidad || 0,
      clienteComprador: ingreso.clienteComprador || '',
      estado: ingreso.estado,
      observaciones: ingreso.observaciones || '',
      lote: ingreso.lote
    });
    setShowForm(true);
  };

  const handleDelete = async (id: number) => {
    if (confirm('¿Estás seguro de que quieres eliminar este ingreso?')) {
      try {
        await api.delete(`/api/v1/ingresos/${id}`);
        cargarIngresos();
      } catch (error) {
        console.error('Error eliminando ingreso:', error);
        alert('Error al eliminar el ingreso');
      }
    }
  };

  const resetForm = () => {
    setFormData({
      concepto: '',
      descripcion: '',
      tipoIngreso: 'VENTA_CULTIVO',
      fechaIngreso: new Date().toISOString().split('T')[0],
      monto: 0,
      unidadMedida: '',
      cantidad: 0,
      clienteComprador: '',
      estado: 'REGISTRADO',
      observaciones: ''
    });
    setEditingIngreso(null);
    setShowForm(false);
  };

  const formatearMoneda = (monto: number) => {
    return new Intl.NumberFormat('es-AR', {
      style: 'currency',
      currency: 'ARS'
    }).format(monto);
  };

  const formatearFecha = (fecha: string) => {
    return new Date(fecha).toLocaleDateString('es-AR');
  };

  const obtenerColorEstado = (estado: string) => {
    switch (estado) {
      case 'CONFIRMADO': return 'bg-green-100 text-green-800';
      case 'CANCELADO': return 'bg-red-100 text-red-800';
      default: return 'bg-yellow-100 text-yellow-800';
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <DollarSign className="w-6 h-6" />
            Gestión de Ingresos
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Button onClick={() => setShowForm(true)} className="mb-4">
            <Plus className="w-4 h-4 mr-2" />
            Nuevo Ingreso
          </Button>

          {showForm && (
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>
                  {editingIngreso ? 'Editar Ingreso' : 'Nuevo Ingreso'}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">Concepto *</label>
                      <Input
                        value={formData.concepto}
                        onChange={(e) => setFormData({...formData, concepto: e.target.value})}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Tipo de Ingreso *</label>
                      <Select value={formData.tipoIngreso} onValueChange={(value: any) => setFormData({...formData, tipoIngreso: value})}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="VENTA_CULTIVO">Venta de Cultivo</SelectItem>
                          <SelectItem value="VENTA_ANIMAL">Venta de Animal</SelectItem>
                          <SelectItem value="SERVICIOS_AGRICOLAS">Servicios Agrícolas</SelectItem>
                          <SelectItem value="SUBSIDIOS">Subsidios</SelectItem>
                          <SelectItem value="OTROS_INGRESOS">Otros Ingresos</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Fecha *</label>
                      <Input
                        type="date"
                        value={formData.fechaIngreso}
                        onChange={(e) => setFormData({...formData, fechaIngreso: e.target.value})}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Monto *</label>
                      <Input
                        type="number"
                        step="0.01"
                        value={formData.monto}
                        onChange={(e) => setFormData({...formData, monto: parseFloat(e.target.value) || 0})}
                        required
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Lote</label>
                      <Select value={formData.lote?.id?.toString() || ''} onValueChange={(value) => {
                        const lote = lotes.find(l => l.id.toString() === value);
                        setFormData({...formData, lote: lote ? {id: lote.id, nombre: lote.nombre} : undefined});
                      }}>
                        <SelectTrigger>
                          <SelectValue placeholder="Seleccionar lote (opcional)" />
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
                    <div>
                      <label className="block text-sm font-medium mb-2">Estado</label>
                      <Select value={formData.estado} onValueChange={(value: any) => setFormData({...formData, estado: value})}>
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          <SelectItem value="REGISTRADO">Registrado</SelectItem>
                          <SelectItem value="CONFIRMADO">Confirmado</SelectItem>
                          <SelectItem value="CANCELADO">Cancelado</SelectItem>
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium mb-2">Unidad de Medida</label>
                      <Input
                        value={formData.unidadMedida}
                        onChange={(e) => setFormData({...formData, unidadMedida: e.target.value})}
                        placeholder="kg, toneladas, etc."
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium mb-2">Cantidad</label>
                      <Input
                        type="number"
                        step="0.01"
                        value={formData.cantidad}
                        onChange={(e) => setFormData({...formData, cantidad: parseFloat(e.target.value) || 0})}
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">Cliente/Comprador</label>
                    <Input
                      value={formData.clienteComprador}
                      onChange={(e) => setFormData({...formData, clienteComprador: e.target.value})}
                      placeholder="Nombre del cliente o comprador"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">Descripción</label>
                    <Input
                      value={formData.descripcion}
                      onChange={(e) => setFormData({...formData, descripcion: e.target.value})}
                      placeholder="Descripción adicional del ingreso"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium mb-2">Observaciones</label>
                    <Input
                      value={formData.observaciones}
                      onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                      placeholder="Observaciones adicionales"
                    />
                  </div>

                  <div className="flex gap-2">
                    <Button type="submit" disabled={loading}>
                      {loading ? 'Guardando...' : (editingIngreso ? 'Actualizar' : 'Guardar')}
                    </Button>
                    <Button type="button" variant="outline" onClick={resetForm}>
                      Cancelar
                    </Button>
                  </div>
                </form>
              </CardContent>
            </Card>
          )}

          {loading ? (
            <div className="text-center py-8">Cargando ingresos...</div>
          ) : (
            <div className="space-y-4">
              {ingresos.map((ingreso) => (
                <Card key={ingreso.id}>
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold">{ingreso.concepto}</h3>
                          <Badge className={obtenerColorEstado(ingreso.estado)}>
                            {ingreso.estado}
                          </Badge>
                        </div>
                        <p className="text-sm text-gray-600 mb-2">
                          <Calendar className="w-4 h-4 inline mr-1" />
                          {formatearFecha(ingreso.fechaIngreso)} • {ingreso.tipoIngreso.replace('_', ' ')}
                          {ingreso.lote && ` • Lote: ${ingreso.lote.nombre}`}
                        </p>
                        {ingreso.descripcion && (
                          <p className="text-sm text-gray-500 mb-2">{ingreso.descripcion}</p>
                        )}
                        {ingreso.clienteComprador && (
                          <p className="text-sm text-gray-500">Cliente: {ingreso.clienteComprador}</p>
                        )}
                      </div>
                      <div className="text-right">
                        <p className="text-xl font-bold text-green-600">
                          {formatearMoneda(ingreso.monto)}
                        </p>
                        <div className="flex gap-2 mt-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleEdit(ingreso)}
                          >
                            <Edit className="w-4 h-4" />
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleDelete(ingreso.id!)}
                          >
                            <Trash2 className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default IngresosManagement;
