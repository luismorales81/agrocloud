import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { insumosService } from '../services/apiServices';
import PermissionGate from './PermissionGate';
import InsumoWizard from './InsumoWizard';

interface Insumo {
  id?: number;
  nombre: string;
  tipo: string;
  descripcion: string;
  unidad_medida: string;
  precio_unitario: number;
  stock_actual: number;
  stock_minimo: number;
  proveedor: string;
  fecha_vencimiento: string;
  estado: 'activo' | 'inactivo';
  categoria: string;
}

interface DosisAplicacion {
  tipoAplicacion: string;
  dosisPorHa: number;
  unidadMedida: string;
  descripcion: string;
}

const InsumosManagement: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingInsumo, setEditingInsumo] = useState<Insumo | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('todos');
  const [loading, setLoading] = useState(false);
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD' | 'EUR'>('ARS');
  
  // Estados para el wizard
  const [showWizard, setShowWizard] = useState(false);
  const [wizardMode, setWizardMode] = useState<'create' | 'edit'>('create');
  
  // Estados para paginación
  const [paginaActual, setPaginaActual] = useState(1);
  const [elementosPorPagina] = useState(10);
  const [formData, setFormData] = useState<Insumo>({
    nombre: '',
    tipo: '',
    descripcion: '',
    unidad_medida: '',
    precio_unitario: 0,
    stock_actual: 0,
    stock_minimo: 0,
    proveedor: '',
    fecha_vencimiento: '',
    estado: 'activo',
    categoria: ''
  });
  
  // Estados para dosis de aplicación
  const [dosisAplicaciones, setDosisAplicaciones] = useState<DosisAplicacion[]>([]);
  const [showDosisSection, setShowDosisSection] = useState(false);


  // Categorías de insumos
  const categorias = [
    'Semillas',
    'Fertilizantes',
    'Herbicidas',
    'Fungicidas',
    'Insecticidas',
    'Combustibles',
    'Repuestos',
    'Herramientas',
    'Otros'
  ];

  // Función para mapear categorías del frontend a tipos del backend
  const mapearCategoriaATipo = (categoria: string): string => {
    const mapeo: { [key: string]: string } = {
      'Semillas': 'SEMILLA',
      'Fertilizantes': 'FERTILIZANTE',
      'Herbicidas': 'HERBICIDA',
      'Fungicidas': 'FUNGICIDA',
      'Insecticidas': 'INSECTICIDA',
      'Combustibles': 'COMBUSTIBLE',
      'Repuestos': 'REPUESTO',
      'Herramientas': 'HERRAMIENTA',
      'Otros': 'OTROS'
    };
    return mapeo[categoria] || 'OTROS';
  };

  // Función para mapear tipos del backend a categorías del frontend
  const mapearTipoACategoria = (tipo: string): string => {
    const mapeo: { [key: string]: string } = {
      'SEMILLA': 'Semillas',
      'FERTILIZANTE': 'Fertilizantes',
      'HERBICIDA': 'Herbicidas',
      'FUNGICIDA': 'Fungicidas',
      'INSECTICIDA': 'Insecticidas',
      'COMBUSTIBLE': 'Combustibles',
      'LUBRICANTE': 'Combustibles',
      'REPUESTO': 'Repuestos',
      'HERRAMIENTA': 'Herramientas',
      'OTROS': 'Otros'
    };
    return mapeo[tipo] || 'Otros';
  };

  // Unidades de medida
  // Verificar si el usuario puede modificar insumos
  const puedeModificarInsumos = () => {
    if (!rolUsuario) return false;
    // Solo OPERARIO, INVITADO y CONSULTOR_EXTERNO NO pueden modificar (solo lectura)
    return rolUsuario !== 'OPERARIO' && 
           rolUsuario !== 'INVITADO' && 
           rolUsuario !== 'CONSULTOR_EXTERNO' && 
           rolUsuario !== 'LECTURA';
  };

  const unidadesMedida = [
    'Kg',
    'Litro',
    'Bolsa',
    'unidad',
    'm²',
    'm³',
    'tonelada',
    'metro',
    'caja'
  ];

  // Tipos de aplicación para dosis
  const tiposAplicacion = [
    { value: 'FOLIAR', label: 'Foliar' },
    { value: 'TERRESTRE', label: 'Terrestre' },
    { value: 'AEREA', label: 'Aérea' },
    { value: 'PRECISION', label: 'Precisión' }
  ];

  // Cargar configuración de moneda
  useEffect(() => {
    // La configuración se maneja ahora a través del contexto
  }, []);

  // Cargar datos desde la API
    const loadInsumos = async () => {
      try {
        setLoading(true);
        
        try {
          const data = await insumosService.listar();
          
          // Mapear datos del backend al frontend con valores por defecto
          const insumosMapeados = (Array.isArray(data) ? data : []).map((insumo: any) => ({
            id: insumo.id,
            nombre: insumo.nombre || 'Sin nombre',
            tipo: insumo.tipo || 'otro',
            descripcion: insumo.descripcion || 'Sin descripción',
            unidad_medida: insumo.unidadMedida || insumo.unidad_medida || 'unidad',
            precio_unitario: insumo.precioUnitario || insumo.precio_unitario || 0,
            stock_actual: insumo.stockActual || insumo.stock_actual || 0,
            stock_minimo: insumo.stockMinimo || insumo.stock_minimo || 0,
            proveedor: insumo.proveedor || 'No especificado',
            fecha_vencimiento: insumo.fechaVencimiento || insumo.fecha_vencimiento || '',
            estado: insumo.estado || 'activo',
            categoria: mapearTipoACategoria(insumo.tipo || insumo.categoria || 'OTROS')
          }));
          
          setInsumos(insumosMapeados);
        } catch (apiError) {
          console.warn('Error del backend, usando datos simulados:', apiError);
          
          // Datos simulados para desarrollo
          const insumosSimulados = [
            {
              id: 1,
              nombre: 'Glifosato 48%',
              tipo: 'HERBICIDA',
              descripcion: 'Herbicida sistémico de amplio espectro',
              unidad_medida: 'L',
              precio_unitario: 1500,
              stock_actual: 100,
              stock_minimo: 10,
              proveedor: 'AgroQuímica S.A.',
              fecha_vencimiento: '2025-12-31',
              estado: 'activo',
              categoria: 'Herbicidas'
            },
            {
              id: 2,
              nombre: 'Atrazina 50%',
              tipo: 'HERBICIDA',
              descripcion: 'Herbicida selectivo para maíz',
              unidad_medida: 'L',
              precio_unitario: 2000,
              stock_actual: 50,
              stock_minimo: 5,
              proveedor: 'Química Agrícola',
              fecha_vencimiento: '2025-11-30',
              estado: 'activo',
              categoria: 'Herbicidas'
            },
            {
              id: 3,
              nombre: 'Urea 46%',
              tipo: 'FERTILIZANTE',
              descripcion: 'Fertilizante nitrogenado',
              unidad_medida: 'kg',
              precio_unitario: 800,
              stock_actual: 200,
              stock_minimo: 20,
              proveedor: 'Fertilizantes del Norte',
              fecha_vencimiento: '2026-06-30',
              estado: 'activo',
              categoria: 'Fertilizantes'
            }
          ];
          
          setInsumos(insumosSimulados);
        }
        
      } catch (error) {
        console.error('Error cargando insumos:', error);
        setInsumos([]);
      } finally {
        setLoading(false);
      }
    };

  useEffect(() => {
    loadInsumos();
  }, []);

  // Guardar insumo
  const saveInsumo = async () => {
    try {
      setLoading(true);
      
      if (!formData.nombre || !formData.categoria || !formData.unidad_medida) {
        alert('Por favor complete todos los campos obligatorios');
        return;
      }

      // Preparar datos para envío al backend (camelCase)
      const datosParaEnvio: any = {
        nombre: formData.nombre,
        descripcion: formData.descripcion,
        unidadMedida: formData.unidad_medida,
        precioUnitario: formData.precio_unitario || 0,
        stockActual: formData.stock_actual || 0,
        stockMinimo: formData.stock_minimo || 0,
        proveedor: formData.proveedor,
        fechaVencimiento: formData.fecha_vencimiento,
        estado: formData.estado,
        tipo: mapearCategoriaATipo(formData.categoria)
      };

      // Si hay dosis configuradas, agregarlas al request
      if (dosisAplicaciones.length > 0) {
        datosParaEnvio.dosisAplicaciones = dosisAplicaciones;
      }

      if (editingInsumo) {
        // Editar insumo existente
        const updatedInsumo = dosisAplicaciones.length > 0
          ? await insumosService.actualizarConDosis(editingInsumo.id!, datosParaEnvio)
          : await insumosService.actualizar(editingInsumo.id!, datosParaEnvio);
        
        const updatedInsumos = insumos.map(insumo => 
          insumo.id === editingInsumo.id ? updatedInsumo : insumo
        );
        setInsumos(updatedInsumos);
        
        const mensaje = dosisAplicaciones.length > 0
          ? `Insumo actualizado exitosamente con ${dosisAplicaciones.length} dosis configuradas`
          : 'Insumo actualizado exitosamente';
        alert(mensaje);
      } else {
        // Crear nuevo insumo
        const nuevoInsumo = dosisAplicaciones.length > 0
          ? await insumosService.crearConDosis(datosParaEnvio)
          : await insumosService.crear(datosParaEnvio);
        
        setInsumos(prev => [...prev, nuevoInsumo]);
        
        const mensaje = dosisAplicaciones.length > 0
          ? `Insumo creado exitosamente con ${dosisAplicaciones.length} dosis configuradas`
          : 'Insumo creado exitosamente';
        alert(mensaje);
      }

      resetForm();
    } catch (error) {
      console.error('Error guardando insumo:', error);
      alert('Error al guardar el insumo');
    } finally {
      setLoading(false);
    }
  };

  // Mostrar formulario con scroll
  const showFormWithScroll = () => {
    setShowForm(true);
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
  };

  // Editar insumo
  const editInsumo = (insumo: Insumo) => {
    setEditingInsumo(insumo);
    setWizardMode('edit');
    setShowWizard(true);
  };

  const handleCreate = () => {
    setEditingInsumo(null);
    setWizardMode('create');
    setShowWizard(true);
  };

  const handleWizardClose = () => {
    setShowWizard(false);
    setEditingInsumo(null);
  };

  const handleWizardSave = () => {
    loadInsumos();
    setShowWizard(false);
    setEditingInsumo(null);
  };

  // Eliminar insumo
  const deleteInsumo = async (id: number) => {
    if (window.confirm('¿Está seguro de que desea eliminar este insumo?')) {
      try {
        setLoading(true);
        
        const token = localStorage.getItem('token');
        if (!token) {
          alert('Error de autenticación. Por favor, inicia sesión nuevamente.');
          return;
        }

        await insumosService.eliminar(id);
        
        // Eliminar del estado local solo si la API confirma la eliminación
        setInsumos(prev => prev.filter(insumo => insumo.id !== id));
        alert('Insumo eliminado exitosamente');
      } catch (error) {
        console.error('Error de conexión al eliminar el insumo:', error);
        alert('Error de conexión al eliminar el insumo. Por favor, verifica tu conexión e intenta nuevamente.');
      } finally {
        setLoading(false);
      }
    }
  };

  // Resetear formulario
  const resetForm = () => {
    setFormData({
      nombre: '',
      tipo: '',
      descripcion: '',
      unidad_medida: '',
      precio_unitario: 0,
      stock_actual: 0,
      stock_minimo: 0,
      proveedor: '',
      fecha_vencimiento: '',
      estado: 'activo',
      categoria: ''
    });
    setEditingInsumo(null);
    setShowForm(false);
    setDosisAplicaciones([]);
    setShowDosisSection(false);
  };

  // Agregar nueva dosis
  const addDosis = () => {
    setDosisAplicaciones([
      ...dosisAplicaciones,
      {
        tipoAplicacion: 'FOLIAR',
        dosisPorHa: 0,
        unidadMedida: formData.unidad_medida || 'Litro',
        descripcion: ''
      }
    ]);
  };

  // Eliminar dosis
  const removeDosis = (index: number) => {
    setDosisAplicaciones(dosisAplicaciones.filter((_, i) => i !== index));
  };

  // Actualizar dosis
  const updateDosis = (index: number, field: keyof DosisAplicacion, value: any) => {
    const updated = [...dosisAplicaciones];
    updated[index] = { ...updated[index], [field]: value };
    setDosisAplicaciones(updated);
  };

  // Filtrar insumos
  const filteredInsumos = insumos.filter(insumo => {
    const matchesSearch = insumo.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         insumo.descripcion.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         insumo.proveedor.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesType = filterType === 'todos' || insumo.categoria === filterType;
    
    return matchesSearch && matchesType;
  });

  // Función para obtener insumos paginados
  const obtenerInsumosPaginados = () => {
    const totalPaginas = Math.ceil(filteredInsumos.length / elementosPorPagina);
    const inicio = (paginaActual - 1) * elementosPorPagina;
    const fin = inicio + elementosPorPagina;
    const insumosPaginados = filteredInsumos.slice(inicio, fin);
    
    return { insumosPaginados, totalPaginas };
  };

  // Resetear paginación cuando cambien los filtros
  useEffect(() => {
    setPaginaActual(1);
  }, [searchTerm, filterType]);

  // Obtener estadísticas
  const estadisticas = {
    totalInsumos: insumos.length,
    stockBajo: insumos.filter(i => i.stock_actual <= i.stock_minimo).length,
    proximosVencer: insumos.filter(i => {
      const fechaVenc = new Date(i.fecha_vencimiento);
      const hoy = new Date();
      const diffTime = fechaVenc.getTime() - hoy.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays <= 30 && diffDays > 0;
    }).length,
    valorTotal: insumos.reduce((sum, i) => sum + (i.precio_unitario * i.stock_actual), 0)
  };

  // Obtener color de estado del stock
  const getStockStatusColor = (insumo: Insumo) => {
    if (insumo.stock_actual <= insumo.stock_minimo) return '#ef4444';
    if (insumo.stock_actual <= insumo.stock_minimo * 1.5) return '#f59e0b';
    return '#10b981';
  };

  // Obtener etiqueta de estado del stock
  const getStockStatusLabel = (insumo: Insumo) => {
    if (insumo.stock_actual <= insumo.stock_minimo) return 'Crítico';
    if (insumo.stock_actual <= insumo.stock_minimo * 1.5) return 'Bajo';
    return 'Normal';
  };

  return (
    <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
      <div style={{ 
        background: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)', 
        color: 'white', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px' 
      }}>
        <h1 style={{ margin: '0 0 10px 0', fontSize: '24px' }}>🧪 Gestión de Insumos</h1>
        <p style={{ margin: '0', opacity: '0.9' }}>
          Administra el inventario de insumos agrícolas
        </p>
      </div>

      {/* Mensaje informativo sobre el estado del sistema */}
      <div style={{
        background: '#e3f2fd',
        border: '1px solid #2196f3',
        borderRadius: '8px',
        padding: '16px',
        marginBottom: '20px',
        display: 'flex',
        alignItems: 'center'
      }}>
        <span style={{ fontSize: '20px', marginRight: '12px' }}>ℹ️</span>
        <div>
          <h3 style={{ margin: '0 0 4px 0', color: '#1976d2', fontSize: '16px', fontWeight: '600' }}>
            Sistema de Insumos
          </h3>
          <p style={{ margin: '0', color: '#1565c0', fontSize: '14px' }}>
            El sistema está funcionando con datos simulados. Algunas funcionalidades pueden requerir que el backend esté completamente configurado.
          </p>
        </div>
      </div>

      {/* Estadísticas */}
      <div style={{ 
        background: '#f3f4f6', 
        padding: '20px', 
        borderRadius: '10px', 
        marginBottom: '20px',
        border: '1px solid #e5e7eb'
      }}>
        <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>📊 Resumen de Inventario</h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '15px' }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#8b5cf6' }}>
              {estadisticas.totalInsumos}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Total Insumos</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#ef4444' }}>
              {estadisticas.stockBajo}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Stock Crítico</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#f59e0b' }}>
              {estadisticas.proximosVencer}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Próximos a Vencer</div>
          </div>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: '24px', fontWeight: 'bold', color: '#10b981' }}>
              {formatCurrency(estadisticas.valorTotal)}
            </div>
            <div style={{ fontSize: '14px', color: '#6b7280' }}>Valor Total</div>
          </div>
        </div>
      </div>

      {/* Botones de acción */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
        <PermissionGate permission="canCreateInsumos">
          <button
            onClick={handleCreate}
            style={{
              background: '#8b5cf6',
              color: 'white',
              border: 'none',
              padding: '12px 24px',
              borderRadius: '8px',
              cursor: 'pointer',
              fontSize: '14px',
              fontWeight: 'bold'
            }}
          >
            ➕ Nuevo Insumo
          </button>
        </PermissionGate>
      </div>

      {/* Filtros */}
      <div style={{ marginBottom: '20px', display: 'flex', gap: '15px', flexWrap: 'wrap', alignItems: 'center' }}>
        <div style={{ flex: '1', minWidth: '250px' }}>
          <input
            type="text"
            placeholder="🔍 Buscar insumos..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            style={{
              width: '100%',
              padding: '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: '14px'
            }}
          />
        </div>
        <div>
          <select
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
            style={{
              padding: '10px',
              border: '1px solid #ddd',
              borderRadius: '5px',
              fontSize: '14px'
            }}
          >
            <option value="todos">Todas las categorías</option>
            {categorias.map(categoria => (
              <option key={categoria} value={categoria}>
                {categoria}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Formulario */}
      {showForm && puedeModificarInsumos() && (
        <div style={{ 
          background: '#f9fafb', 
          padding: '20px', 
          borderRadius: '10px', 
          marginBottom: '20px',
          border: '1px solid #e5e7eb'
        }}>
          <h3 style={{ margin: '0 0 15px 0', color: '#374151' }}>
            {editingInsumo ? '✏️ Editar Insumo' : '📝 Nuevo Insumo'}
          </h3>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '15px' }}>
            {/* Nombre */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Nombre *:</label>
              <input
                type="text"
                value={formData.nombre}
                onChange={(e) => setFormData(prev => ({ ...prev, nombre: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Ej: Semilla Soja DM 53i54"
              />
            </div>

            {/* Categoría */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Categoría *:</label>
              <select
                value={formData.categoria}
                onChange={(e) => setFormData(prev => ({ ...prev, categoria: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="">Seleccionar categoría</option>
                {categorias.map(cat => (
                  <option key={cat} value={cat}>{cat}</option>
                ))}
              </select>
            </div>

            {/* Unidad de medida */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Unidad de Medida *:</label>
              <select
                value={formData.unidad_medida}
                onChange={(e) => setFormData(prev => ({ ...prev, unidad_medida: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="">Seleccionar unidad</option>
                {unidadesMedida.map(unidad => (
                  <option key={unidad} value={unidad}>{unidad}</option>
                ))}
              </select>
            </div>

            {/* Precio unitario */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Precio Unitario ($):</label>
              <input
                type="number"
                step="0.01"
                value={formData.precio_unitario}
                onChange={(e) => setFormData(prev => ({ ...prev, precio_unitario: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Stock actual */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Stock Actual:</label>
              <input
                type="number"
                value={formData.stock_actual}
                onChange={(e) => setFormData(prev => ({ ...prev, stock_actual: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Stock mínimo */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Stock Mínimo:</label>
              <input
                type="number"
                value={formData.stock_minimo}
                onChange={(e) => setFormData(prev => ({ ...prev, stock_minimo: Number(e.target.value) }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Proveedor */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Proveedor:</label>
              <input
                type="text"
                value={formData.proveedor}
                onChange={(e) => setFormData(prev => ({ ...prev, proveedor: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
                placeholder="Nombre del proveedor"
              />
            </div>

            {/* Fecha de vencimiento */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Fecha de Vencimiento:</label>
              <input
                type="date"
                value={formData.fecha_vencimiento}
                onChange={(e) => setFormData(prev => ({ ...prev, fecha_vencimiento: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              />
            </div>

            {/* Estado */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Estado:</label>
              <select
                value={formData.estado}
                onChange={(e) => setFormData(prev => ({ ...prev, estado: e.target.value as 'activo' | 'inactivo' }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="activo">Activo</option>
                <option value="inactivo">Inactivo</option>
              </select>
            </div>
          </div>

          {/* Descripción */}
          <div style={{ marginTop: '15px' }}>
            <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Descripción:</label>
            <textarea
              value={formData.descripcion}
              onChange={(e) => setFormData(prev => ({ ...prev, descripcion: e.target.value }))}
              style={{
                width: '100%',
                padding: '10px',
                border: '1px solid #ddd',
                borderRadius: '5px',
                fontSize: '14px',
                minHeight: '80px',
                resize: 'vertical'
              }}
              placeholder="Descripción detallada del insumo..."
            />
          </div>

          {/* Sección de Dosis de Aplicación */}
          <div style={{ marginTop: '20px', padding: '15px', background: '#f9fafb', borderRadius: '8px', border: '1px solid #e5e7eb' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
              <h4 style={{ margin: 0, color: '#374151' }}>
                🧪 Dosis de Aplicación Sugeridas (Opcional)
              </h4>
              <button
                type="button"
                onClick={() => setShowDosisSection(!showDosisSection)}
                style={{
                  background: showDosisSection ? '#ef4444' : '#8b5cf6',
                  color: 'white',
                  border: 'none',
                  padding: '8px 16px',
                  borderRadius: '5px',
                  cursor: 'pointer',
                  fontSize: '13px',
                  fontWeight: 'bold'
                }}
              >
                {showDosisSection ? '❌ Ocultar' : '➕ Configurar Dosis'}
              </button>
            </div>

            {showDosisSection && (
              <div>
                <p style={{ margin: '0 0 15px 0', color: '#6b7280', fontSize: '14px' }}>
                  Configure las dosis sugeridas para este insumo según el tipo de aplicación. 
                  Esto facilitará el cálculo automático de cantidades al crear aplicaciones.
                </p>

                {dosisAplicaciones.length === 0 && (
                  <div style={{ textAlign: 'center', padding: '20px', color: '#6b7280' }}>
                    <p>No hay dosis configuradas</p>
                    <button
                      type="button"
                      onClick={addDosis}
                      style={{
                        background: '#8b5cf6',
                        color: 'white',
                        border: 'none',
                        padding: '10px 20px',
                        borderRadius: '5px',
                        cursor: 'pointer',
                        fontSize: '14px',
                        fontWeight: 'bold'
                      }}
                    >
                      ➕ Agregar Primera Dosis
                    </button>
                  </div>
                )}

                {dosisAplicaciones.map((dosis, index) => (
                  <div key={index} style={{ 
                    marginBottom: '15px', 
                    padding: '15px', 
                    background: 'white', 
                    borderRadius: '5px',
                    border: '1px solid #d1d5db'
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                      <h5 style={{ margin: 0, color: '#374151' }}>Dosis {index + 1}</h5>
                      <button
                        type="button"
                        onClick={() => removeDosis(index)}
                        style={{
                          background: '#ef4444',
                          color: 'white',
                          border: 'none',
                          padding: '5px 10px',
                          borderRadius: '3px',
                          cursor: 'pointer',
                          fontSize: '12px'
                        }}
                      >
                        ❌ Eliminar
                      </button>
                    </div>

                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '10px' }}>
                      {/* Tipo de aplicación */}
                      <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px', fontWeight: 'bold' }}>
                          Tipo de Aplicación *
                        </label>
                        <select
                          value={dosis.tipoAplicacion}
                          onChange={(e) => updateDosis(index, 'tipoAplicacion', e.target.value)}
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #ddd',
                            borderRadius: '5px',
                            fontSize: '13px'
                          }}
                        >
                          {tiposAplicacion.map(tipo => (
                            <option key={tipo.value} value={tipo.value}>{tipo.label}</option>
                          ))}
                        </select>
                      </div>

                      {/* Dosis por hectárea */}
                      <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px', fontWeight: 'bold' }}>
                          Dosis por Hectárea *
                        </label>
                        <input
                          type="number"
                          step="0.01"
                          value={dosis.dosisPorHa}
                          onChange={(e) => updateDosis(index, 'dosisPorHa', Number(e.target.value))}
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #ddd',
                            borderRadius: '5px',
                            fontSize: '13px'
                          }}
                          placeholder="Ej: 2.5"
                        />
                      </div>

                      {/* Unidad de medida */}
                      <div>
                        <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px', fontWeight: 'bold' }}>
                          Unidad
                        </label>
                        <input
                          type="text"
                          value={dosis.unidadMedida}
                          onChange={(e) => updateDosis(index, 'unidadMedida', e.target.value)}
                          style={{
                            width: '100%',
                            padding: '8px',
                            border: '1px solid #ddd',
                            borderRadius: '5px',
                            fontSize: '13px'
                          }}
                          placeholder="Ej: litros, kg"
                        />
                      </div>
                    </div>

                    {/* Descripción */}
                    <div style={{ marginTop: '10px' }}>
                      <label style={{ display: 'block', marginBottom: '5px', fontSize: '13px', fontWeight: 'bold' }}>
                        Descripción (Opcional)
                      </label>
                      <input
                        type="text"
                        value={dosis.descripcion}
                        onChange={(e) => updateDosis(index, 'descripcion', e.target.value)}
                        style={{
                          width: '100%',
                          padding: '8px',
                          border: '1px solid #ddd',
                          borderRadius: '5px',
                          fontSize: '13px'
                        }}
                        placeholder="Ej: Aplicación foliar para control de malezas"
                      />
                    </div>
                  </div>
                ))}

                {dosisAplicaciones.length > 0 && (
                  <button
                    type="button"
                    onClick={addDosis}
                    style={{
                      background: '#8b5cf6',
                      color: 'white',
                      border: 'none',
                      padding: '10px 20px',
                      borderRadius: '5px',
                      cursor: 'pointer',
                      fontSize: '14px',
                      fontWeight: 'bold',
                      width: '100%'
                    }}
                  >
                    ➕ Agregar Otra Dosis
                  </button>
                )}
              </div>
            )}
          </div>

          {/* Botones */}
          <div style={{ display: 'flex', gap: '10px', marginTop: '15px' }}>
            <button
              onClick={saveInsumo}
              disabled={loading}
              style={{
                background: '#8b5cf6',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontSize: '14px',
                fontWeight: 'bold',
                opacity: loading ? 0.6 : 1
              }}
            >
              {loading ? '💾 Guardando...' : (editingInsumo ? '💾 Actualizar' : '💾 Guardar')}
            </button>
            <button
              onClick={resetForm}
              style={{
                background: '#6b7280',
                color: 'white',
                border: 'none',
                padding: '12px 24px',
                borderRadius: '8px',
                cursor: 'pointer',
                fontSize: '14px'
              }}
            >
              ❌ Cancelar
            </button>
          </div>
        </div>
      )}

      {/* Tabla de insumos */}
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
          🧪 Insumos Registrados ({filteredInsumos.length})
        </div>
        
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            🔄 Cargando insumos...
          </div>
        ) : (() => {
          const { insumosPaginados, totalPaginas } = obtenerInsumosPaginados();
          
          if (filteredInsumos.length === 0) {
            return (
              <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
                {searchTerm || filterType !== 'todos' ? 'No se encontraron insumos que coincidan con los filtros' : 'No hay insumos registrados'}
              </div>
            );
          }

          return (
            <>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ 
                  width: '100%', 
                  borderCollapse: 'collapse',
                  fontSize: '14px'
                }}>
                  <thead>
                    <tr style={{ background: '#f8f9fa' }}>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Insumo</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Tipo</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Stock</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Precio</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Proveedor</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Vencimiento</th>
                      <th style={{ padding: '12px', textAlign: 'left', borderBottom: '1px solid #dee2e6' }}>Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {insumosPaginados.map(insumo => (
                  <tr key={insumo.id} style={{ borderBottom: '1px solid #f1f3f4' }}>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <strong>{insumo.nombre}</strong>
                        <div style={{ fontSize: '12px', color: '#6b7280', marginTop: '2px' }}>
                          {insumo.descripcion.substring(0, 50)}...
                        </div>
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{
                        padding: '4px 8px',
                        borderRadius: '12px',
                        fontSize: '12px',
                        fontWeight: 'bold',
                        background: '#e0e7ff',
                        color: '#3730a3'
                      }}>
                        {insumo.tipo.charAt(0).toUpperCase() + insumo.tipo.slice(1)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div>
                        <span style={{ fontWeight: 'bold' }}>
                          {insumo.stock_actual} {insumo.unidad_medida}
                        </span>
                        <div style={{ fontSize: '12px', color: '#6b7280' }}>
                          Mín: {insumo.stock_minimo}
                        </div>
                        <span style={{
                          padding: '2px 6px',
                          borderRadius: '8px',
                          fontSize: '10px',
                          fontWeight: 'bold',
                          background: getStockStatusColor(insumo) + '20',
                          color: getStockStatusColor(insumo)
                        }}>
                          {getStockStatusLabel(insumo)}
                        </span>
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span style={{ fontWeight: 'bold', color: '#10b981' }}>
                        {formatCurrency(insumo.precio_unitario || 0)}
                      </span>
                    </td>
                    <td style={{ padding: '12px' }}>
                      {insumo.proveedor}
                    </td>
                    <td style={{ padding: '12px' }}>
                      {insumo.fecha_vencimiento ? (
                        <span style={{
                          color: new Date(insumo.fecha_vencimiento) < new Date() ? '#ef4444' : '#6b7280'
                        }}>
                          {new Date(insumo.fecha_vencimiento).toLocaleDateString('es-ES')}
                        </span>
                      ) : (
                        <span style={{ color: '#6b7280' }}>No especificada</span>
                      )}
                    </td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        {puedeModificarInsumos() ? (
                          <>
                            <button
                              onClick={() => editInsumo(insumo)}
                              style={{
                                background: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                padding: '6px 12px',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '12px'
                              }}
                            >
                              ✏️ Editar
                            </button>
                            <button
                              onClick={() => deleteInsumo(insumo.id!)}
                              style={{
                                background: '#ef4444',
                                color: 'white',
                                border: 'none',
                                padding: '6px 12px',
                                borderRadius: '4px',
                                cursor: 'pointer',
                                fontSize: '12px'
                              }}
                            >
                              🗑️ Eliminar
                            </button>
                          </>
                        ) : (
                          <span style={{ 
                            color: '#6b7280',
                            fontSize: '12px',
                            fontStyle: 'italic'
                          }}>
                            Solo lectura
                          </span>
                        )}
                      </div>
                    </td>
                  </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Paginación */}
              {totalPaginas > 1 && (
                <div style={{ 
                  display: 'flex', 
                  justifyContent: 'space-between', 
                  alignItems: 'center', 
                  padding: '20px',
                  borderTop: '1px solid #e5e7eb',
                  background: '#f8f9fa'
                }}>
                  <div style={{ fontSize: '14px', color: '#6b7280' }}>
                    Mostrando {((paginaActual - 1) * elementosPorPagina) + 1} - {Math.min(paginaActual * elementosPorPagina, filteredInsumos.length)} de {filteredInsumos.length} insumos
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
              )}
            </>
          );
        })()}
      </div>

      {/* Wizard de Insumos */}
      <InsumoWizard
        isOpen={showWizard}
        onClose={handleWizardClose}
        insumoEditando={editingInsumo}
        onGuardar={handleWizardSave}
      />
    </div>
  );
};

export default InsumosManagement;
