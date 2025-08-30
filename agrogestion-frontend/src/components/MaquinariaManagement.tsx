import React, { useState, useEffect } from 'react';
import { useCurrency } from '../hooks/useCurrency';

interface Maquinaria {
  id?: number;
  nombre: string;
  tipo: string;
  fecha_compra: string;
  kilometros_uso: number;
  estado: string;
  costo_por_hora: number;
  kilometros_mantenimiento_intervalo: number;
  ultimo_mantenimiento_kilometros: number;
  rendimiento_combustible?: number;
  unidad_rendimiento?: string;
  costo_combustible_por_litro?: number;
  valor_actual?: number;
  notas: string;
}

const MaquinariaManagement: React.FC = () => {
  const { formatCurrency } = useCurrency();
  const [maquinaria, setMaquinaria] = useState<Maquinaria[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [editingMaquinaria, setEditingMaquinaria] = useState<Maquinaria | null>(null);
  const [formData, setFormData] = useState<Maquinaria>({
    nombre: '',
    tipo: 'tractor',
    fecha_compra: '',
    kilometros_uso: 0,
    estado: 'activo',
    costo_por_hora: 0,
    kilometros_mantenimiento_intervalo: 5000,
    ultimo_mantenimiento_kilometros: 0,
    rendimiento_combustible: 0,
    unidad_rendimiento: 'km/L',
    costo_combustible_por_litro: 0,
    valor_actual: 0,
    notas: ''
  });

  const tiposMaquinaria = ['tractor', 'cosechadora', 'pulverizadora', 'sembradora', 'arado', 'rastra', 'otro'];
  const estadosMaquinaria = ['activo', 'mantenimiento', 'inactivo', 'retirado'];
  const unidadesRendimiento = ['km/L', 'L/hora', 'L/km'];

  // Cargar datos mock
  useEffect(() => {
    const mockData: Maquinaria[] = [
      {
        id: 1,
        nombre: 'Tractor John Deere 5075E',
        tipo: 'tractor',
        fecha_compra: '2020-03-15',
        kilometros_uso: 12500.50,
        estado: 'activo',
        costo_por_hora: 45.00,
        kilometros_mantenimiento_intervalo: 5000,
        ultimo_mantenimiento_kilometros: 12000.00,
        rendimiento_combustible: 12.5,
        unidad_rendimiento: 'km/L',
        costo_combustible_por_litro: 450.00,
        valor_actual: 8500000.00,
        notas: 'Tractor principal para labores generales'
      },
      {
        id: 2,
        nombre: 'Cosechadora New Holland CR8.90',
        tipo: 'cosechadora',
        fecha_compra: '2019-11-20',
        kilometros_uso: 8500.25,
        estado: 'activo',
        costo_por_hora: 120.00,
        kilometros_mantenimiento_intervalo: 3000,
        ultimo_mantenimiento_kilometros: 8000.00,
        rendimiento_combustible: 8.2,
        unidad_rendimiento: 'km/L',
        costo_combustible_por_litro: 450.00,
        valor_actual: 45000000.00,
        notas: 'Cosechadora de alta capacidad'
      }
    ];
    setMaquinaria(mockData);
  }, []);

  const saveMaquinaria = () => {
    if (editingMaquinaria) {
      setMaquinaria(prev => prev.map(m => m.id === editingMaquinaria.id ? { ...formData, id: m.id } : m));
    } else {
      const newId = Math.max(...maquinaria.map(m => m.id || 0)) + 1;
      setMaquinaria(prev => [...prev, { ...formData, id: newId }]);
    }
    resetForm();
  };

  const editMaquinaria = (maq: Maquinaria) => {
    setEditingMaquinaria(maq);
    setFormData(maq);
    setShowForm(true);
  };

  const deleteMaquinaria = (id: number) => {
    if (window.confirm('¿Está seguro de eliminar esta maquinaria?')) {
      setMaquinaria(prev => prev.filter(m => m.id !== id));
    }
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      tipo: 'tractor',
      fecha_compra: '',
      kilometros_uso: 0,
      estado: 'activo',
      costo_por_hora: 0,
      kilometros_mantenimiento_intervalo: 5000,
      ultimo_mantenimiento_kilometros: 0,
      rendimiento_combustible: 0,
      unidad_rendimiento: 'km/L',
      costo_combustible_por_litro: 0,
      valor_actual: 0,
      notas: ''
    });
    setEditingMaquinaria(null);
    setShowForm(false);
  };

  const getEstadoMantenimiento = (maq: Maquinaria) => {
    const kilometrosDesdeMantenimiento = maq.kilometros_uso - maq.ultimo_mantenimiento_kilometros;
    if (kilometrosDesdeMantenimiento >= maq.kilometros_mantenimiento_intervalo) {
      return { estado: 'MANTENIMIENTO REQUERIDO', color: '#ef4444' };
    } else if (kilometrosDesdeMantenimiento >= maq.kilometros_mantenimiento_intervalo * 0.8) {
      return { estado: 'MANTENIMIENTO PRÓXIMO', color: '#f59e0b' };
    }
    return { estado: 'OK', color: '#10b981' };
  };

  const filteredMaquinaria = maquinaria.filter(maq => 
    maq.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const estadisticas = {
    total: maquinaria.length,
    activas: maquinaria.filter(m => m.estado === 'activo').length,
    alertas: 0
  };

  return (
    <div style={{ padding: '1rem', maxWidth: '1200px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white',
        padding: '1.5rem',
        borderRadius: '0.5rem',
        marginBottom: '1.5rem'
      }}>
        <h1 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>🚜 Gestión de Maquinaria</h1>
        <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9 }}>Administra tu flota de maquinaria agrícola</p>
      </div>

      {/* Estadísticas */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
        gap: '1rem',
        marginBottom: '1.5rem'
      }}>
        <div style={{ background: '#f3f4f6', padding: '1rem', borderRadius: '0.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#374151' }}>{estadisticas.total}</div>
          <div style={{ color: '#6b7280' }}>Total Maquinaria</div>
        </div>
        <div style={{ background: '#f3f4f6', padding: '1rem', borderRadius: '0.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#10b981' }}>{estadisticas.activas}</div>
          <div style={{ color: '#6b7280' }}>Activas</div>
        </div>
        <div style={{ background: '#f3f4f6', padding: '1rem', borderRadius: '0.5rem', textAlign: 'center' }}>
          <div style={{ fontSize: '2rem', fontWeight: 'bold', color: '#ef4444' }}>{estadisticas.alertas}</div>
          <div style={{ color: '#6b7280' }}>En Mantenimiento</div>
        </div>
      </div>

      {/* Botones de acción */}
      <div style={{ marginBottom: '1.5rem' }}>
        <button
          onClick={() => setShowForm(true)}
          style={{
            background: '#2563eb',
            color: 'white',
            border: 'none',
            padding: '0.5rem 1rem',
            borderRadius: '0.25rem',
            cursor: 'pointer',
            fontSize: '0.875rem'
          }}
        >
          ➕ Nueva Maquinaria
        </button>
      </div>

      {/* Buscador */}
      <div style={{ marginBottom: '1.5rem' }}>
        <input
          type="text"
          placeholder="Buscar maquinaria..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{
            padding: '0.5rem',
            border: '1px solid #d1d5db',
            borderRadius: '0.25rem',
            width: '300px'
          }}
        />
      </div>

      {/* Lista de maquinaria */}
      <div style={{ background: 'white', borderRadius: '0.5rem', overflow: 'hidden', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ background: '#f9fafb' }}>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Maquinaria</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Tipo</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Estado</th>
                                 <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Kilómetros</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Mantenimiento</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Costo/Hora (ARS)</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Rendimiento</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {filteredMaquinaria.map(maq => {
                const mantenimiento = getEstadoMantenimiento(maq);
                return (
                  <tr key={maq.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                    <td style={{ padding: '0.75rem' }}>
                      <div style={{ fontWeight: 'bold' }}>{maq.nombre}</div>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                        Compra: {new Date(maq.fecha_compra).toLocaleDateString()}
                      </div>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        background: '#e5e7eb',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        fontSize: '0.75rem'
                      }}>
                        {maq.tipo}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        background: maq.estado === 'activo' ? '#d1fae5' : '#fef3c7',
                        color: maq.estado === 'activo' ? '#065f46' : '#92400e',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        fontSize: '0.75rem'
                      }}>
                        {maq.estado}
                      </span>
                    </td>
                                         <td style={{ padding: '0.75rem' }}>
                       <div>{maq.kilometros_uso.toFixed(1)} km</div>
                       <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                         Último: {maq.ultimo_mantenimiento_kilometros.toFixed(1)} km
                       </div>
                     </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        background: mantenimiento.color === '#ef4444' ? '#fee2e2' : '#d1fae5',
                        color: mantenimiento.color,
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        fontSize: '0.75rem',
                        fontWeight: 'bold'
                      }}>
                        {mantenimiento.estado}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      {formatCurrency(maq.costo_por_hora)}/hs
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div>{maq.rendimiento_combustible || 0} {maq.unidad_rendimiento || 'km/L'}</div>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                        {formatCurrency(maq.costo_combustible_por_litro || 0)}/L
                      </div>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div style={{ display: 'flex', gap: '0.25rem' }}>
                        <button
                          onClick={() => editMaquinaria(maq)}
                          style={{
                            background: '#3b82f6',
                            color: 'white',
                            border: 'none',
                            padding: '0.25rem 0.5rem',
                            borderRadius: '0.25rem',
                            cursor: 'pointer',
                            fontSize: '0.75rem'
                          }}
                        >
                          ✏️
                        </button>
                        <button
                          onClick={() => deleteMaquinaria(maq.id!)}
                          style={{
                            background: '#ef4444',
                            color: 'white',
                            border: 'none',
                            padding: '0.25rem 0.5rem',
                            borderRadius: '0.25rem',
                            cursor: 'pointer',
                            fontSize: '0.75rem'
                          }}
                        >
                          🗑️
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Formulario de maquinaria */}
      {showForm && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          background: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            background: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '600px',
            width: '95%',
            maxHeight: '90vh',
            overflowY: 'auto'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
              <h2 style={{ margin: 0, color: '#374151' }}>
                {editingMaquinaria ? '✏️ Editar' : '🚜 Nueva'} Maquinaria
              </h2>
              <button
                onClick={resetForm}
                style={{
                  background: 'none',
                  border: 'none',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  color: '#6b7280'
                }}
              >
                ✕
              </button>
            </div>
            
            <div style={{ display: 'grid', gap: '1rem' }}>
              {/* Nombre */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  📝 Nombre de la Maquinaria *
                </label>
                <input
                  type="text"
                  placeholder="Ej: Tractor John Deere 5075E"
                  value={formData.nombre}
                  onChange={(e) => setFormData({...formData, nombre: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Nombre descriptivo que identifique la maquinaria
                </small>
              </div>
              
              {/* Tipo */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  🚜 Tipo de Maquinaria *
                </label>
                <select
                  value={formData.tipo}
                  onChange={(e) => setFormData({...formData, tipo: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                >
                  {tiposMaquinaria.map(tipo => (
                    <option key={tipo} value={tipo}>{tipo.charAt(0).toUpperCase() + tipo.slice(1)}</option>
                  ))}
                </select>
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Categoría principal de la maquinaria
                </small>
              </div>
              
              {/* Fecha de Compra */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  📅 Fecha de Compra *
                </label>
                <input
                  type="date"
                  value={formData.fecha_compra}
                  onChange={(e) => setFormData({...formData, fecha_compra: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Fecha cuando se adquirió la maquinaria
                </small>
              </div>
              
                             {/* Kilómetros de Uso */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   🚗 Kilómetros de Uso Actual *
                 </label>
                 <input
                   type="number"
                   placeholder="0.0"
                   value={formData.kilometros_uso}
                   onChange={(e) => setFormData({...formData, kilometros_uso: parseFloat(e.target.value) || 0})}
                   style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                   step="0.1"
                 />
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Total de kilómetros acumulados en el odómetro de la maquinaria
                 </small>
               </div>
              
              {/* Estado */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  🔧 Estado Actual *
                </label>
                <select
                  value={formData.estado}
                  onChange={(e) => setFormData({...formData, estado: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                >
                  {estadosMaquinaria.map(estado => (
                    <option key={estado} value={estado}>{estado.charAt(0).toUpperCase() + estado.slice(1)}</option>
                  ))}
                </select>
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Estado operativo actual de la maquinaria
                </small>
              </div>
              
              {/* Costo por Hora */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  💰 Costo por Hora de Operación
                </label>
                <input
                  type="number"
                  placeholder="0.00"
                  value={formData.costo_por_hora}
                  onChange={(e) => setFormData({...formData, costo_por_hora: parseFloat(e.target.value) || 0})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                  step="0.01"
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Costo estimado por hora de uso (combustible, mantenimiento, etc.)
                </small>
              </div>
              
                             {/* Intervalo de Mantenimiento */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   🔧 Intervalo de Mantenimiento (kilómetros)
                 </label>
                 <input
                   type="number"
                   placeholder="5000"
                   value={formData.kilometros_mantenimiento_intervalo}
                   onChange={(e) => setFormData({...formData, kilometros_mantenimiento_intervalo: parseInt(e.target.value) || 5000})}
                   style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                 />
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Cada cuántos kilómetros se debe realizar mantenimiento preventivo
                 </small>
               </div>
               
               {/* Último Mantenimiento */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   ⏰ Kilómetros del Último Mantenimiento
                 </label>
                 <input
                   type="number"
                   placeholder="0.0"
                   value={formData.ultimo_mantenimiento_kilometros}
                   onChange={(e) => setFormData({...formData, ultimo_mantenimiento_kilometros: parseFloat(e.target.value) || 0})}
                   style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                   step="0.1"
                 />
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Kilómetros en el odómetro cuando se realizó el último mantenimiento
                 </small>
               </div>

               {/* Rendimiento de Combustible */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   ⛽ Rendimiento de Combustible
                 </label>
                 <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '0.5rem' }}>
                   <input
                     type="number"
                     placeholder="0.0"
                     value={formData.rendimiento_combustible || 0}
                     onChange={(e) => setFormData({...formData, rendimiento_combustible: parseFloat(e.target.value) || 0})}
                     style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem' }}
                     step="0.1"
                   />
                   <select
                     value={formData.unidad_rendimiento || 'km/L'}
                     onChange={(e) => setFormData({...formData, unidad_rendimiento: e.target.value})}
                     style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem' }}
                   >
                     {unidadesRendimiento.map(unidad => (
                       <option key={unidad} value={unidad}>{unidad}</option>
                     ))}
                   </select>
                 </div>
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Rendimiento de combustible (ej: 12.5 km/L, 15 L/hora)
                 </small>
               </div>

               {/* Costo de Combustible */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   💰 Costo de Combustible por Litro (ARS)
                 </label>
                 <input
                   type="number"
                   placeholder="0.00"
                   value={formData.costo_combustible_por_litro || 0}
                   onChange={(e) => setFormData({...formData, costo_combustible_por_litro: parseFloat(e.target.value) || 0})}
                   style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                   step="0.01"
                 />
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Costo actual del combustible por litro en pesos argentinos
                 </small>
               </div>

               {/* Valor Actual */}
               <div>
                 <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                   💵 Valor Actual de la Maquinaria (ARS)
                 </label>
                 <input
                   type="number"
                   placeholder="0.00"
                   value={formData.valor_actual || 0}
                   onChange={(e) => setFormData({...formData, valor_actual: parseFloat(e.target.value) || 0})}
                   style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                   step="0.01"
                 />
                 <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                   Valor actual de mercado de la maquinaria en pesos argentinos
                 </small>
               </div>
              
              {/* Notas */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  📝 Notas Adicionales
                </label>
                <textarea
                  placeholder="Información adicional, características especiales, observaciones..."
                  value={formData.notas}
                  onChange={(e) => setFormData({...formData, notas: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', minHeight: '80px', width: '100%', resize: 'vertical' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Información adicional relevante sobre la maquinaria
                </small>
              </div>
            </div>
            
            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem', justifyContent: 'flex-end' }}>
              <button
                onClick={resetForm}
                style={{
                  background: '#6b7280',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.25rem',
                  cursor: 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500'
                }}
              >
                ❌ Cancelar
              </button>
              <button
                onClick={saveMaquinaria}
                disabled={!formData.nombre || !formData.fecha_compra}
                style={{
                  background: !formData.nombre || !formData.fecha_compra ? '#9ca3af' : '#2563eb',
                  color: 'white',
                  border: 'none',
                  padding: '0.75rem 1.5rem',
                  borderRadius: '0.25rem',
                  cursor: !formData.nombre || !formData.fecha_compra ? 'not-allowed' : 'pointer',
                  fontSize: '0.875rem',
                  fontWeight: '500'
                }}
              >
                {editingMaquinaria ? '💾 Actualizar Maquinaria' : '💾 Guardar Maquinaria'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MaquinariaManagement;
