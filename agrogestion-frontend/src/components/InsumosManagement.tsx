import React, { useState, useEffect } from 'react';
import currencyService from '../services/CurrencyService';

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
  const [insumos, setInsumos] = useState<Insumo[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingInsumo, setEditingInsumo] = useState<Insumo | null>(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('todos');
  const [loading, setLoading] = useState(false);
  const [selectedCurrency, setSelectedCurrency] = useState<'ARS' | 'USD'>('ARS');
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

  // Tipos de insumos disponibles
  const tiposInsumo = [
    'semilla',
    'fertilizante',
    'herbicida',
    'fungicida',
    'insecticida',
    'combustible',
    'lubricante',
    'repuesto',
    'herramienta',
    'otro'
  ];

  // Categorías de insumos
  const categorias = [
    'Semillas',
    'Fertilizantes',
    'Agroquímicos',
    'Combustibles',
    'Repuestos',
    'Herramientas',
    'Otros'
  ];

  // Unidades de medida
  const unidadesMedida = [
    'kg',
    'L',
    'unidad',
    'm²',
    'm³',
    'tonelada',
    'litro',
    'metro',
    'caja',
    'bolsa'
  ];

  // Cargar configuración de moneda
  useEffect(() => {
    const config = currencyService.getCurrencyConfig();
    setSelectedCurrency(config.currency);
  }, []);

  const formatCurrency = (amount: number): string => {
    return currencyService.formatCurrency(amount, selectedCurrency);
  };

  // Cargar datos mock
  const loadInsumos = async () => {
    setLoading(true);
    
    // Simulación de carga
    setTimeout(() => {
      const mockInsumos: Insumo[] = [
        {
          id: 1,
          nombre: 'Semilla Soja DM 53i54',
          tipo: 'semilla',
          descripcion: 'Semilla de soja de ciclo corto, resistente a sequía',
          unidad_medida: 'kg',
          precio_unitario: 8500,
          stock_actual: 2500,
          stock_minimo: 500,
          proveedor: 'Don Mario Semillas',
          fecha_vencimiento: '2025-06-30',
          estado: 'activo',
          categoria: 'Semillas'
        },
        {
          id: 2,
          nombre: 'Fertilizante Urea 46%',
          tipo: 'fertilizante',
          descripcion: 'Fertilizante nitrogenado de alta concentración',
          unidad_medida: 'kg',
          precio_unitario: 450,
          stock_actual: 8000,
          stock_minimo: 1000,
          proveedor: 'Profertil S.A.',
          fecha_vencimiento: '2024-12-31',
          estado: 'activo',
          categoria: 'Fertilizantes'
        },
        {
          id: 3,
          nombre: 'Glifosato 48%',
          tipo: 'herbicida',
          descripcion: 'Herbicida sistémico de amplio espectro',
          unidad_medida: 'L',
          precio_unitario: 2800,
          stock_actual: 150,
          stock_minimo: 50,
          proveedor: 'Syngenta',
          fecha_vencimiento: '2025-03-15',
          estado: 'activo',
          categoria: 'Agroquímicos'
        },
        {
          id: 4,
          nombre: 'Aceite de Motor 15W40',
          tipo: 'lubricante',
          descripcion: 'Aceite lubricante para motores diesel',
          unidad_medida: 'L',
          precio_unitario: 1200,
          stock_actual: 80,
          stock_minimo: 20,
          proveedor: 'YPF',
          fecha_vencimiento: '2026-01-01',
          estado: 'activo',
          categoria: 'Combustibles'
        },
        {
          id: 5,
          nombre: 'Filtro de Aire',
          tipo: 'repuesto',
          descripcion: 'Filtro de aire para tractor Massey Ferguson',
          unidad_medida: 'unidad',
          precio_unitario: 850,
          stock_actual: 5,
          stock_minimo: 2,
          proveedor: 'Massey Ferguson',
          fecha_vencimiento: '2027-01-01',
          estado: 'activo',
          categoria: 'Repuestos'
        }
      ];
      
      setInsumos(mockInsumos);
      setLoading(false);
    }, 1000);
  };

  useEffect(() => {
    loadInsumos();
  }, []);

  // Guardar insumo
  const saveInsumo = async () => {
    try {
      setLoading(true);
      
      if (!formData.nombre || !formData.tipo || !formData.unidad_medida) {
        alert('Por favor complete todos los campos obligatorios');
        return;
      }

      if (editingInsumo) {
        // Editar insumo existente
        const updatedInsumos = insumos.map(insumo => 
          insumo.id === editingInsumo.id ? { ...formData, id: insumo.id } : insumo
        );
        setInsumos(updatedInsumos);
        alert('Insumo actualizado exitosamente');
      } else {
        // Crear nuevo insumo
        const newInsumo: Insumo = {
          ...formData,
          id: insumos.length + 1
        };
        setInsumos(prev => [...prev, newInsumo]);
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

  // Editar insumo
  const editInsumo = (insumo: Insumo) => {
    setEditingInsumo(insumo);
    setFormData(insumo);
    setShowForm(true);
  };

  // Eliminar insumo
  const deleteInsumo = (id: number) => {
    if (confirm('¿Está seguro de que desea eliminar este insumo?')) {
      setInsumos(prev => prev.filter(insumo => insumo.id !== id));
      alert('Insumo eliminado exitosamente');
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
    
    const matchesType = filterType === 'todos' || insumo.tipo === filterType;
    
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
          onClick={() => setShowForm(true)}
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
            <option value="todos">Todos los tipos</option>
            {tiposInsumo.map(tipo => (
              <option key={tipo} value={tipo}>
                {tipo.charAt(0).toUpperCase() + tipo.slice(1)}
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

            {/* Tipo */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Tipo *:</label>
              <select
                value={formData.tipo}
                onChange={(e) => setFormData(prev => ({ ...prev, tipo: e.target.value }))}
                style={{
                  width: '100%',
                  padding: '10px',
                  border: '1px solid #ddd',
                  borderRadius: '5px',
                  fontSize: '14px'
                }}
              >
                <option value="">Seleccionar tipo</option>
                {tiposInsumo.map(tipo => (
                  <option key={tipo} value={tipo}>
                    {tipo.charAt(0).toUpperCase() + tipo.slice(1)}
                  </option>
                ))}
              </select>
            </div>

            {/* Categoría */}
            <div>
              <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>Categoría:</label>
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
                  <option key={unidad} value={unidad}>{unidad.toUpperCase()}</option>
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
                        {formatCurrency(insumo.precio_unitario)}
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
