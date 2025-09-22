import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import api from '../services/api';

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

const InsumosManagement: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingInsumo, setEditingInsumo] = useState<Insumo | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('todos');
  const [loading, setLoading] = useState(false);
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD' | 'EUR'>('ARS');
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

  // Cargar configuración de moneda
  useEffect(() => {
    // La configuración se maneja ahora a través del contexto
  }, []);

  // Cargar datos desde la API
    const loadInsumos = async () => {
      try {
        setLoading(true);
        const response = await api.get('/api/v1/insumos');
        
        
        // Mapear datos del backend al frontend con valores por defecto
        const insumosMapeados = response.data.map((insumo: any) => ({
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
      const datosParaEnvio = {
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

      if (editingInsumo) {
        // Editar insumo existente
        const response = await api.put(`/insumos/${editingInsumo.id}`, datosParaEnvio);
        const updatedInsumos = insumos.map(insumo => 
          insumo.id === editingInsumo.id ? response.data : insumo
        );
        setInsumos(updatedInsumos);
        alert('Insumo actualizado exitosamente');
      } else {
        // Crear nuevo insumo
        const response = await api.post('/insumos', datosParaEnvio);
        setInsumos(prev => [...prev, response.data]);
        alert('Insumo creado exitosamente');
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
    setFormData(insumo);
    showFormWithScroll();
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

        const response = await fetch(`http://localhost:8080/api/insumos/${id}`, {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          }
        });

        if (response.ok || response.status === 204) {
          // Eliminar del estado local solo si la API confirma la eliminación
          setInsumos(prev => prev.filter(insumo => insumo.id !== id));
          alert('Insumo eliminado exitosamente');
        } else {
          console.error('Error al eliminar el insumo:', response.status, response.statusText);
          alert('Error al eliminar el insumo. Por favor, inténtalo de nuevo.');
        }
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
  };

  // Filtrar insumos
  const filteredInsumos = insumos.filter(insumo => {
    const matchesSearch = insumo.nombre.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         insumo.descripcion.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         insumo.proveedor.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesType = filterType === 'todos' || insumo.categoria === filterType;
    
    return matchesSearch && matchesType;
  });

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
        <button
          onClick={showFormWithScroll}
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
      {showForm && (
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
        ) : filteredInsumos.length === 0 ? (
          <div style={{ padding: '40px', textAlign: 'center', color: '#666' }}>
            {searchTerm || filterType !== 'todos' ? 'No se encontraron insumos que coincidan con los filtros' : 'No hay insumos registrados'}
          </div>
        ) : (
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
                {filteredInsumos.map(insumo => (
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
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default InsumosManagement;
