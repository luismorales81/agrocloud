import React, { useState, useEffect } from 'react';
import Button from './ui/Button';
import Input from './ui/Input';
import { Card, CardContent, CardHeader, CardTitle } from './ui/Card';
import Badge from './ui/Badge';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from './ui/Select';
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
  estado: 'REGISTRADO' | 'CONFIRMADO' | 'PAGADO' | 'CANCELADO';
  observaciones?: string;
  lote?: {
    id: number;
    nombre: string;
  };
}

interface Egreso {
  id?: number;
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
  descripcion?: string;
  tipo?: string;
  unidad: string;
  precioUnitario?: number;
  stockMinimo?: number;
  stockActual: number;
  proveedor?: string;
  fechaVencimiento?: string;
  activo?: boolean;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  userId?: number;
  userName?: string;
}

const FinanzasManagement: React.FC = () => {
  const [ingresos, setIngresos] = useState<Ingreso[]>([]);
  const [egresos, setEgresos] = useState<Egreso[]>([]);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'ingreso' | 'egreso'>('ingreso');
  const [showInsumoModal, setShowInsumoModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'ingresos' | 'egresos'>('ingresos');
  
  // Formulario de ingreso
  const [ingresoForm, setIngresoForm] = useState<Ingreso>({
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

  // Formulario de egreso
  const [egresoForm, setEgresoForm] = useState<Egreso>({
    concepto: '',
    descripcion: '',
    tipoEgreso: '',
    fechaEgreso: new Date().toISOString().split('T')[0],
    monto: 0,
    unidadMedida: '',
    cantidad: 0,
    proveedor: '',
    estado: 'REGISTRADO',
    observaciones: ''
  });

  // Formulario de nuevo insumo
  const [nuevoInsumoForm, setNuevoInsumoForm] = useState({
    nombre: '',
    descripcion: '',
    tipo: 'FERTILIZANTE',
    unidad: '',
    precioUnitario: 0,
    stockMinimo: 0,
    stockActual: 0,
    proveedor: '',
    fechaVencimiento: ''
  });

  const tiposIngreso = [
    { value: 'VENTA_CULTIVO', label: 'Venta de Cultivo' },
    { value: 'VENTA_ANIMAL', label: 'Venta de Animal' },
    { value: 'SERVICIOS_AGRICOLAS', label: 'Servicios Agrícolas' },
    { value: 'SUBSIDIOS', label: 'Subsidios' },
    { value: 'OTROS_INGRESOS', label: 'Otros Ingresos' }
  ];

  const tiposEgreso = [
    { value: 'INSUMOS', label: 'Insumos' },
    { value: 'COMBUSTIBLE', label: 'Combustible' },
    { value: 'MANO_OBRA', label: 'Mano de Obra' },
    { value: 'MAQUINARIA', label: 'Maquinaria' },
    { value: 'SERVICIOS', label: 'Servicios' },
    { value: 'IMPUESTOS', label: 'Impuestos' },
    { value: 'OTROS_EGRESOS', label: 'Otros Egresos' }
  ];

  const tiposInsumo = [
    { value: 'FERTILIZANTE', label: 'Fertilizante' },
    { value: 'HERBICIDA', label: 'Herbicida' },
    { value: 'FUNGICIDA', label: 'Fungicida' },
    { value: 'INSECTICIDA', label: 'Insecticida' },
    { value: 'SEMILLA', label: 'Semilla' },
    { value: 'COMBUSTIBLE', label: 'Combustible' },
    { value: 'LUBRICANTE', label: 'Lubricante' },
    { value: 'REPUESTO', label: 'Repuesto' },
    { value: 'HERRAMIENTA', label: 'Herramienta' },
    { value: 'OTROS', label: 'Otros' }
  ];

  const estados = [
    { value: 'REGISTRADO', label: 'Registrado' },
    { value: 'CONFIRMADO', label: 'Confirmado' },
    { value: 'PAGADO', label: 'Pagado' },
    { value: 'CANCELADO', label: 'Cancelado' }
  ];

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      await Promise.all([
        cargarIngresos(),
        cargarEgresos(),
        cargarLotes(),
        cargarInsumos() // Ahora cargamos los insumos existentes
      ]);
    } catch (error) {
      console.error('Error cargando datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const cargarIngresos = async () => {
    try {
      // Por ahora, usar datos locales
      setIngresos([]);
    } catch (error) {
      console.error('Error cargando ingresos:', error);
    }
  };

  const cargarEgresos = async () => {
    try {
      // Por ahora, usar datos locales
      setEgresos([]);
    } catch (error) {
      console.error('Error cargando egresos:', error);
    }
  };

  const cargarLotes = async () => {
    try {
      const response = await api.get('/public/campos');
      setLotes(response.data || []);
      console.log('Lotes cargados:', response.data);
    } catch (error) {
      console.error('Error cargando lotes:', error);
      setLotes([]);
    }
  };

  const cargarInsumos = async () => {
    try {
      const response = await api.get('/public/insumos');
      setInsumos(response.data || []);
      console.log('Insumos cargados:', response.data);
    } catch (error) {
      console.error('Error cargando insumos:', error);
      setInsumos([]);
    }
  };

  const abrirModal = (tipo: 'ingreso' | 'egreso') => {
    setModalType(tipo);
    setShowModal(true);
    // Limpiar formularios
    if (tipo === 'ingreso') {
      setIngresoForm({
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
    } else {
      setEgresoForm({
        concepto: '',
        descripcion: '',
        tipoEgreso: '',
        fechaEgreso: new Date().toISOString().split('T')[0],
        monto: 0,
        unidadMedida: '',
        cantidad: 0,
        proveedor: '',
        estado: 'REGISTRADO',
        observaciones: ''
      });
    }
  };

  const cerrarModal = () => {
    setShowModal(false);
  };

  const handleSubmitIngreso = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Agregar ingreso localmente
      const nuevoIngreso = {
        ...ingresoForm,
        id: Date.now() // ID temporal
      };
      setIngresos(prev => [...prev, nuevoIngreso]);
      cerrarModal();
    } catch (error) {
      console.error('Error guardando ingreso:', error);
      alert('Error al guardar el ingreso');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitEgreso = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Validar que si es un egreso de insumos, tenga la información completa
      if (egresoForm.tipoEgreso === 'INSUMOS') {
        if (!egresoForm.insumo?.nombre) {
          alert('⚠️ Para egresos de insumos, debes especificar el nombre del insumo');
          setLoading(false);
          return;
        }
        if (!egresoForm.cantidad || egresoForm.cantidad <= 0) {
          alert('⚠️ Para egresos de insumos, debes especificar una cantidad válida');
          setLoading(false);
          return;
        }
        if (!egresoForm.unidadMedida) {
          alert('⚠️ Para egresos de insumos, debes especificar la unidad de medida');
          setLoading(false);
          return;
        }
      }

      // Agregar egreso localmente
      const nuevoEgreso = {
        ...egresoForm,
        id: Date.now() // ID temporal
      };
      setEgresos(prev => [...prev, nuevoEgreso]);
      
      // Si es un egreso de insumos, actualizar el inventario
      if (egresoForm.tipoEgreso === 'INSUMOS') {
        if (egresoForm.insumo?.id) {
          // Actualizar insumo existente
          await actualizarInventarioInsumo(egresoForm.insumo.id, egresoForm.cantidad || 0);
        } else if (egresoForm.insumo?.nombre) {
          // Crear nuevo insumo con la cantidad comprada
          const nuevoInsumo = {
            nombre: egresoForm.insumo.nombre,
            descripcion: egresoForm.descripcion || `Comprado el ${egresoForm.fechaEgreso}`,
            tipo: 'OTROS',
            unidad: egresoForm.unidadMedida || 'unidades',
            precioUnitario: egresoForm.monto / (egresoForm.cantidad || 1),
            stockMinimo: 0,
            stockActual: egresoForm.cantidad || 0,
            proveedor: egresoForm.proveedor || '',
            fechaVencimiento: null,
            activo: true
          };

          try {
            const response = await api.post('/public/insumos', nuevoInsumo);
            const insumoCreado = response.data;
            setInsumos(prev => [...prev, insumoCreado]);
            alert(`✅ Nuevo insumo "${insumoCreado.nombre}" creado y agregado al inventario`);
          } catch (error) {
            console.error('Error creando insumo:', error);
            alert('⚠️ El egreso se guardó pero hubo un error creando el insumo');
          }
        }
      }
      
      cerrarModal();
    } catch (error) {
      console.error('Error guardando egreso:', error);
      alert('Error al guardar el egreso');
    } finally {
      setLoading(false);
    }
  };

  // Función para crear un nuevo insumo
  const crearNuevoInsumo = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Crear el nuevo insumo en el backend
      const response = await api.post('/public/insumos', {
        ...nuevoInsumoForm,
        activo: true
      });

      const insumoCreado = response.data;
      
      // Agregar a la lista local
      setInsumos(prev => [...prev, insumoCreado]);
      
      // Cerrar modal y limpiar formulario
      setShowInsumoModal(false);
      setNuevoInsumoForm({
        nombre: '',
        descripcion: '',
        tipo: 'FERTILIZANTE',
        unidad: '',
        precioUnitario: 0,
        stockMinimo: 0,
        stockActual: 0,
        proveedor: '',
        fechaVencimiento: ''
      });

      // Mostrar mensaje de éxito
      alert(`✅ Insumo "${insumoCreado.nombre}" creado exitosamente`);
      
    } catch (error) {
      console.error('Error creando insumo:', error);
      alert('❌ Error al crear el insumo. Intenta nuevamente.');
    } finally {
      setLoading(false);
    }
  };

  // Función para actualizar el inventario de insumos
  const actualizarInventarioInsumo = async (insumoId: number, cantidad: number) => {
    try {
      // Actualizar el stock en el backend
      const insumoExistente = insumos.find(insumo => insumo.id === insumoId);
      if (!insumoExistente) {
        alert('❌ Error: No se encontró el insumo seleccionado');
        return;
      }

      const nuevoStock = insumoExistente.stockActual + cantidad;
      
      // Actualizar en el backend
      await api.put(`/public/insumos/${insumoId}`, {
        ...insumoExistente,
        stockActual: nuevoStock
      });

      // Actualizar en el estado local
      setInsumos(prev => prev.map(insumo => 
        insumo.id === insumoId
          ? { ...insumo, stockActual: nuevoStock }
          : insumo
      ));
      
      // Mostrar mensaje de confirmación
      alert(`✅ Inventario actualizado: ${insumoExistente.nombre} - Stock actual: ${nuevoStock} ${insumoExistente.unidad}`);
    } catch (error) {
      console.error('Error actualizando inventario:', error);
      alert('❌ Error al actualizar el inventario. Intenta nuevamente.');
    }
  };

  const handleDeleteIngreso = async (id: number) => {
    if (confirm('¿Estás seguro de que quieres eliminar este ingreso?')) {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        const response = await fetch(`http://localhost:8080/api/ingresos/${id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok || response.status === 204) {
          // Eliminar del estado local solo si la API confirma la eliminación
          setIngresos(prev => prev.filter(ingreso => ingreso.id !== id));
          alert('Ingreso eliminado exitosamente');
        } else {
          console.error('Error al eliminar el ingreso:', response.status, response.statusText);
          alert('Error al eliminar el ingreso. Por favor, inténtalo de nuevo.');
        }
      } catch (error) {
        console.error('Error de conexión al eliminar el ingreso:', error);
        alert('Error de conexión al eliminar el ingreso. Por favor, verifica tu conexión e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    }
  };

  const handleDeleteEgreso = async (id: number) => {
    if (confirm('¿Estás seguro de que quieres eliminar este egreso?')) {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        const response = await fetch(`http://localhost:8080/api/egresos/${id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok || response.status === 204) {
          // Eliminar del estado local solo si la API confirma la eliminación
          setEgresos(prev => prev.filter(egreso => egreso.id !== id));
          alert('Egreso eliminado exitosamente');
        } else {
          console.error('Error al eliminar el egreso:', response.status, response.statusText);
          alert('Error al eliminar el egreso. Por favor, inténtalo de nuevo.');
        }
      } catch (error) {
        console.error('Error de conexión al eliminar el egreso:', error);
        alert('Error de conexión al eliminar el egreso. Por favor, verifica tu conexión e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    }
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
      case 'CONFIRMADO':
      case 'PAGADO': return 'green';
      case 'CANCELADO': return 'red';
      default: return 'yellow';
    }
  };

  const obtenerColorTipo = (tipo: string) => {
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

  return (
    <div className="p-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-900">💰 Gestión Financiera</h1>
        <div className="flex gap-2">
          <Button onClick={() => abrirModal('ingreso')} className="bg-green-600 hover:bg-green-700">
            ➕ Nuevo Ingreso
          </Button>
          <Button onClick={() => abrirModal('egreso')} className="bg-red-600 hover:bg-red-700">
            ➖ Nuevo Egreso
          </Button>
        </div>
             </div>

       {/* Resumen Financiero */}
       <div className="mb-6 grid grid-cols-1 md:grid-cols-3 gap-4">
         <Card>
           <CardContent className="p-4">
             <div className="text-center">
               <p className="text-sm text-gray-600">Total Ingresos</p>
               <p className="text-2xl font-bold text-green-600">
                 {formatearMoneda(ingresos.reduce((sum, ingreso) => sum + ingreso.monto, 0))}
               </p>
               <p className="text-xs text-gray-500">{ingresos.length} registros</p>
             </div>
           </CardContent>
         </Card>
         <Card>
           <CardContent className="p-4">
             <div className="text-center">
               <p className="text-sm text-gray-600">Total Egresos</p>
               <p className="text-2xl font-bold text-red-600">
                 {formatearMoneda(egresos.reduce((sum, egreso) => sum + egreso.monto, 0))}
               </p>
               <p className="text-xs text-gray-500">{egresos.length} registros</p>
             </div>
           </CardContent>
         </Card>
         <Card>
           <CardContent className="p-4">
             <div className="text-center">
               <p className="text-sm text-gray-600">Balance Neto</p>
               <p className={`text-2xl font-bold ${
                 (ingresos.reduce((sum, ingreso) => sum + ingreso.monto, 0) - 
                  egresos.reduce((sum, egreso) => sum + egreso.monto, 0)) >= 0 
                   ? 'text-green-600' : 'text-red-600'
               }`}>
                 {formatearMoneda(
                   ingresos.reduce((sum, ingreso) => sum + ingreso.monto, 0) - 
                   egresos.reduce((sum, egreso) => sum + egreso.monto, 0)
                 )}
               </p>
               <p className="text-xs text-gray-500">Ingresos - Egresos</p>
             </div>
           </CardContent>
         </Card>
       </div>

       {/* Pestañas */}
      <div className="mb-6">
        <div className="border-b border-gray-200">
          <nav className="-mb-px flex space-x-8">
            <button
              onClick={() => setActiveTab('ingresos')}
              className={`py-2 px-1 border-b-2 font-medium text-sm ${
                activeTab === 'ingresos'
                  ? 'border-green-500 text-green-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              📈 Ingresos ({ingresos.length})
            </button>
                         <button
               onClick={() => setActiveTab('egresos')}
               className={`py-2 px-1 border-b-2 font-medium text-sm ${
                 activeTab === 'egresos'
                   ? 'border-red-500 text-red-600'
                   : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
               }`}
             >
               📉 Egresos ({egresos.length})
             </button>

          </nav>
        </div>
      </div>

      {/* Contenido de las pestañas */}
      {activeTab === 'ingresos' && (
        <Card>
          <CardHeader>
            <CardTitle>📈 Lista de Ingresos</CardTitle>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-8">
                <p>⏳ Cargando ingresos...</p>
              </div>
            ) : ingresos.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">No hay ingresos registrados</p>
              </div>
            ) : (
              <div className="space-y-4">
                {ingresos.map((ingreso) => (
                  <div key={ingreso.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="font-semibold">{ingreso.concepto}</h3>
                        <Badge color={obtenerColorEstado(ingreso.estado)}>
                          {ingreso.estado}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">
                        📅 {formatearFecha(ingreso.fechaIngreso)} • {ingreso.tipoIngreso.replace('_', ' ')}
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
                      <Button
                        onClick={() => handleDeleteIngreso(ingreso.id!)}
                        className="mt-2 text-red-600 hover:text-red-900"
                      >
                        🗑️ Eliminar
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {activeTab === 'egresos' && (
        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <CardTitle>📉 Lista de Egresos</CardTitle>
              <Button 
                onClick={() => setShowInsumoModal(true)} 
                className="bg-blue-600 hover:bg-blue-700 text-white"
              >
                📦 Gestionar Insumos
              </Button>
            </div>
          </CardHeader>
          <CardContent>
            {loading ? (
              <div className="text-center py-8">
                <p>⏳ Cargando egresos...</p>
              </div>
            ) : egresos.length === 0 ? (
              <div className="text-center py-8">
                <p className="text-gray-500">No hay egresos registrados</p>
              </div>
            ) : (
              <div className="space-y-4">
                {egresos.map((egreso) => (
                  <div key={egreso.id} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-2">
                        <h3 className="font-semibold">{egreso.concepto}</h3>
                        <Badge color={obtenerColorTipo(egreso.tipoEgreso)}>
                          {egreso.tipoEgreso.replace('_', ' ')}
                        </Badge>
                        <Badge color={obtenerColorEstado(egreso.estado)}>
                          {egreso.estado}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 mb-2">
                        📅 {formatearFecha(egreso.fechaEgreso)}
                        {egreso.lote && ` • Lote: ${egreso.lote.nombre}`}
                        {egreso.insumo && ` • Insumo: ${egreso.insumo.nombre}`}
                      </p>
                      {egreso.descripcion && (
                        <p className="text-sm text-gray-500 mb-2">{egreso.descripcion}</p>
                      )}
                      {egreso.proveedor && (
                        <p className="text-sm text-gray-500">Proveedor: {egreso.proveedor}</p>
                      )}
                    </div>
                    <div className="text-right">
                      <p className="text-xl font-bold text-red-600">
                        {formatearMoneda(egreso.monto)}
                      </p>
                      <Button
                        onClick={() => handleDeleteEgreso(egreso.id!)}
                        className="mt-2 text-red-600 hover:text-red-900"
                      >
                        🗑️ Eliminar
                      </Button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}



      {/* Modal para Ingreso */}
      {showModal && modalType === 'ingreso' && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">📈 Nuevo Ingreso</h2>
              <Button onClick={cerrarModal} className="text-gray-500 hover:text-gray-700">
                ❌
              </Button>
            </div>
            
            <form onSubmit={handleSubmitIngreso} className="space-y-4">
              {/* Campos básicos */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Concepto *
                  </label>
                                     <Input
                     type="text"
                     value={ingresoForm.concepto}
                     onChange={(value) => setIngresoForm({...ingresoForm, concepto: value})}
                     placeholder="Ej: Venta de trigo"
                     required
                   />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Ingreso *
                  </label>
                  <Select
                    value={ingresoForm.tipoIngreso}
                    onValueChange={(value: any) => setIngresoForm({...ingresoForm, tipoIngreso: value})}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {tiposIngreso.map(tipo => (
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
                     value={ingresoForm.fechaIngreso}
                     onChange={(value) => setIngresoForm({...ingresoForm, fechaIngreso: value})}
                     required
                   />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Monto *
                  </label>
                                     <Input
                     type="number"
                     value={ingresoForm.monto.toString()}
                     onChange={(value) => setIngresoForm({...ingresoForm, monto: parseFloat(value) || 0})}
                     placeholder="0.00"
                     required
                   />
                </div>
              </div>

              {/* Campos opcionales expandibles */}
              <details className="group">
                <summary className="cursor-pointer text-sm font-medium text-gray-700 hover:text-gray-900">
                  📋 Información Adicional (opcional)
                </summary>
                <div className="mt-4 space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                         <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">
                         Lote (opcional)
                       </label>
                                               <Input
                          type="text"
                          value={ingresoForm.lote?.nombre || ''}
                          onChange={(value) => setIngresoForm({...ingresoForm, lote: value ? {id: 0, nombre: value} : undefined})}
                          placeholder="Nombre del lote"
                        />
                     </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Estado
                      </label>
                      <Select
                        value={ingresoForm.estado}
                        onValueChange={(value: any) => setIngresoForm({...ingresoForm, estado: value})}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {estados.map(estado => (
                            <SelectItem key={estado.value} value={estado.value}>
                              {estado.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Unidad de Medida
                      </label>
                                             <Input
                         type="text"
                         value={ingresoForm.unidadMedida || ''}
                         onChange={(value) => setIngresoForm({...ingresoForm, unidadMedida: value})}
                         placeholder="kg, toneladas, etc."
                       />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cantidad
                      </label>
                                             <Input
                         type="number"
                         value={(ingresoForm.cantidad || 0).toString()}
                         onChange={(value) => setIngresoForm({...ingresoForm, cantidad: parseFloat(value) || 0})}
                         placeholder="0.00"
                       />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Cliente/Comprador
                    </label>
                                         <Input
                       type="text"
                       value={ingresoForm.clienteComprador || ''}
                       onChange={(value) => setIngresoForm({...ingresoForm, clienteComprador: value})}
                       placeholder="Nombre del cliente o comprador"
                     />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Descripción
                    </label>
                    <textarea
                      value={ingresoForm.descripcion}
                      onChange={(e) => setIngresoForm({...ingresoForm, descripcion: e.target.value})}
                      placeholder="Descripción adicional del ingreso..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={3}
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Observaciones
                    </label>
                    <textarea
                      value={ingresoForm.observaciones}
                      onChange={(e) => setIngresoForm({...ingresoForm, observaciones: e.target.value})}
                      placeholder="Observaciones adicionales..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={2}
                    />
                  </div>
                </div>
              </details>

              <div className="flex justify-end space-x-3">
                <Button type="button" onClick={cerrarModal}>
                  ❌ Cancelar
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading ? '⏳ Guardando...' : '💾 Guardar Ingreso'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal para Egreso */}
      {showModal && modalType === 'egreso' && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">📉 Nuevo Egreso</h2>
              <Button onClick={cerrarModal} className="text-gray-500 hover:text-gray-700">
                ❌
              </Button>
            </div>
            
            <form onSubmit={handleSubmitEgreso} className="space-y-4">
              {/* Campos básicos */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Concepto *
                  </label>
                                     <Input
                     type="text"
                     value={egresoForm.concepto}
                     onChange={(value) => setEgresoForm({...egresoForm, concepto: value})}
                     placeholder="Ej: Compra de fertilizante"
                     required
                   />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Egreso *
                  </label>
                  <Select
                    value={egresoForm.tipoEgreso}
                    onValueChange={(value) => setEgresoForm({...egresoForm, tipoEgreso: value})}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Seleccionar tipo" />
                    </SelectTrigger>
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
                     value={egresoForm.fechaEgreso}
                     onChange={(value) => setEgresoForm({...egresoForm, fechaEgreso: value})}
                     required
                   />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Monto *
                  </label>
                                     <Input
                     type="number"
                     value={egresoForm.monto.toString()}
                     onChange={(value) => setEgresoForm({...egresoForm, monto: parseFloat(value) || 0})}
                     placeholder="0.00"
                     required
                   />
                </div>
              </div>

              {/* Campos opcionales expandibles */}
              <details className="group">
                <summary className="cursor-pointer text-sm font-medium text-gray-700 hover:text-gray-900">
                  📋 Información Adicional (opcional)
                </summary>
                <div className="mt-4 space-y-4">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                         <div>
                       <label className="block text-sm font-medium text-gray-700 mb-1">
                         Lote (opcional)
                       </label>
                                               <Input
                          type="text"
                          value={egresoForm.lote?.nombre || ''}
                          onChange={(value) => setEgresoForm({...egresoForm, lote: value ? {id: 0, nombre: value} : undefined})}
                          placeholder="Nombre del lote"
                        />
                     </div>

                                           <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Insumo {egresoForm.tipoEgreso === 'INSUMOS' ? '*' : '(opcional)'}
                        </label>
                        
                        {/* Opción para elegir insumo existente */}
                        <div className="mb-2">
                          <Select
                            value={egresoForm.insumo?.id?.toString() || ''}
                            onValueChange={(value) => {
                              if (value === 'nuevo') {
                                // Limpiar insumo seleccionado para crear uno nuevo
                                setEgresoForm({
                                  ...egresoForm, 
                                  insumo: { id: 0, nombre: '' },
                                  // Limpiar campos relacionados con insumos
                                  unidadMedida: '',
                                  cantidad: 0,
                                  proveedor: ''
                                });
                              } else {
                                const insumoSeleccionado = insumos.find(insumo => insumo.id.toString() === value);
                                if (insumoSeleccionado) {
                                  // Cargar automáticamente todos los campos del insumo seleccionado
                                  setEgresoForm({
                                    ...egresoForm, 
                                    insumo: {
                                      id: insumoSeleccionado.id, 
                                      nombre: insumoSeleccionado.nombre
                                    },
                                    // Cargar campos del insumo seleccionado
                                    unidadMedida: insumoSeleccionado.unidad || '',
                                    cantidad: 0, // La cantidad se mantiene en 0 para que el usuario la especifique
                                    proveedor: insumoSeleccionado.proveedor || '',
                                    descripcion: insumoSeleccionado.descripcion || egresoForm.descripcion,
                                    // Si no hay monto, calcular basado en precio unitario
                                    monto: egresoForm.monto || 0
                                  });
                                  
                                  console.log('✅ Insumo seleccionado:', insumoSeleccionado.nombre);
                                  console.log('📊 Campos cargados automáticamente:', {
                                    unidad: insumoSeleccionado.unidad,
                                    proveedor: insumoSeleccionado.proveedor,
                                    descripcion: insumoSeleccionado.descripcion
                                  });
                                }
                              }
                            }}
                          >
                            <SelectTrigger>
                              <SelectValue placeholder="Seleccionar insumo existente" />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="nuevo">
                                ➕ Crear nuevo insumo
                              </SelectItem>
                              {insumos.map(insumo => (
                                <SelectItem key={insumo.id} value={insumo.id.toString()}>
                                  {insumo.nombre} - Stock: {insumo.stockActual} {insumo.unidad}
                                </SelectItem>
                              ))}
                            </SelectContent>
                          </Select>
                        </div>

                        {/* Campo para nombre de nuevo insumo */}
                        {(!egresoForm.insumo?.id && egresoForm.tipoEgreso === 'INSUMOS') && (
                          <div className="mb-2">
                            <Input
                              type="text"
                              value={egresoForm.insumo?.nombre || ''}
                              onChange={(value) => setEgresoForm({
                                ...egresoForm, 
                                insumo: { id: 0, nombre: value }
                              })}
                              placeholder="Nombre del nuevo insumo"
                              required
                            />
                            <p className="text-xs text-gray-500 mt-1">
                              ✨ Se creará automáticamente un nuevo insumo en el inventario
                            </p>
                          </div>
                        )}

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
                         value={egresoForm.unidadMedida || ''}
                         onChange={(value) => setEgresoForm({...egresoForm, unidadMedida: value})}
                         placeholder="kg, litros, etc."
                       />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cantidad
                      </label>
                                             <Input
                         type="number"
                         value={(egresoForm.cantidad || 0).toString()}
                         onChange={(value) => setEgresoForm({...egresoForm, cantidad: parseFloat(value) || 0})}
                         placeholder="0.00"
                       />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Proveedor
                      </label>
                                             <Input
                         type="text"
                         value={egresoForm.proveedor || ''}
                         onChange={(value) => setEgresoForm({...egresoForm, proveedor: value})}
                       />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Estado
                      </label>
                      <Select
                        value={egresoForm.estado}
                        onValueChange={(value) => setEgresoForm({...egresoForm, estado: value})}
                      >
                        <SelectTrigger>
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {estados.map(estado => (
                            <SelectItem key={estado.value} value={estado.value}>
                              {estado.label}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Descripción
                    </label>
                    <textarea
                      value={egresoForm.descripcion}
                      onChange={(e) => setEgresoForm({...egresoForm, descripcion: e.target.value})}
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
                      value={egresoForm.observaciones}
                      onChange={(e) => setEgresoForm({...egresoForm, observaciones: e.target.value})}
                      placeholder="Observaciones adicionales..."
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={2}
                    />
                  </div>
                </div>
              </details>

              <div className="flex justify-end space-x-3">
                <Button type="button" onClick={cerrarModal}>
                  ❌ Cancelar
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading ? '⏳ Guardando...' : '💾 Guardar Egreso'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal para Nuevo Insumo */}
      {showInsumoModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">📦 Nuevo Insumo</h2>
              <Button onClick={() => setShowInsumoModal(false)} className="text-gray-500 hover:text-gray-700">
                ❌
              </Button>
            </div>
            
            <form onSubmit={crearNuevoInsumo} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Nombre del Insumo *
                  </label>
                  <Input
                    type="text"
                    value={nuevoInsumoForm.nombre}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, nombre: value})}
                    placeholder="Ej: Fertilizante NPK"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Insumo *
                  </label>
                  <Select
                    value={nuevoInsumoForm.tipo}
                    onValueChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, tipo: value})}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      {tiposInsumo.map(tipo => (
                        <SelectItem key={tipo.value} value={tipo.value}>
                          {tipo.label}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Unidad de Medida *
                  </label>
                  <Input
                    type="text"
                    value={nuevoInsumoForm.unidad}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, unidad: value})}
                    placeholder="kg, litros, unidades"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Precio Unitario *
                  </label>
                  <Input
                    type="number"
                    value={nuevoInsumoForm.precioUnitario.toString()}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, precioUnitario: parseFloat(value) || 0})}
                    placeholder="0.00"
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Stock Mínimo
                  </label>
                  <Input
                    type="number"
                    value={nuevoInsumoForm.stockMinimo.toString()}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, stockMinimo: parseFloat(value) || 0})}
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Stock Inicial
                  </label>
                  <Input
                    type="number"
                    value={nuevoInsumoForm.stockActual.toString()}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, stockActual: parseFloat(value) || 0})}
                    placeholder="0.00"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Proveedor
                  </label>
                  <Input
                    type="text"
                    value={nuevoInsumoForm.proveedor}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, proveedor: value})}
                    placeholder="Nombre del proveedor"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fecha de Vencimiento
                  </label>
                  <Input
                    type="date"
                    value={nuevoInsumoForm.fechaVencimiento}
                    onChange={(value) => setNuevoInsumoForm({...nuevoInsumoForm, fechaVencimiento: value})}
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descripción
                </label>
                <textarea
                  value={nuevoInsumoForm.descripcion}
                  onChange={(e) => setNuevoInsumoForm({...nuevoInsumoForm, descripcion: e.target.value})}
                  placeholder="Descripción del insumo..."
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  rows={3}
                />
              </div>

              <div className="flex justify-end space-x-3">
                <Button type="button" onClick={() => setShowInsumoModal(false)}>
                  ❌ Cancelar
                </Button>
                <Button type="submit" disabled={loading}>
                  {loading ? '⏳ Creando...' : '💾 Crear Insumo'}
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default FinanzasManagement;
