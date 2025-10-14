import React, { useState, useEffect } from 'react';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import api from '../services/api';
import PermissionGate from './PermissionGate';

interface Maquinaria {
  id?: number;
  nombre: string;
  tipo: string;
  marca?: string;
  modelo?: string;
  anio_fabricacion?: number | null;
  numero_serie?: string;
  fecha_compra: string;
  kilometros_uso: number | null;
  estado: string;
  costo_por_hora: number | null;
  kilometros_mantenimiento_intervalo: number | null;
  ultimo_mantenimiento_kilometros: number | null;
  rendimiento_combustible?: number | null;
  unidad_rendimiento?: string;
  costo_combustible_por_litro?: number | null;
  valor_actual?: number | null;
  notas: string;
}

const MaquinariaManagement: React.FC = () => {
  const { formatCurrency } = useCurrencyContext();
  const { user } = useAuth();
  const { rolUsuario } = useEmpresa();
  const [maquinaria, setMaquinaria] = useState<Maquinaria[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [editingMaquinaria, setEditingMaquinaria] = useState<Maquinaria | null>(null);
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState<Maquinaria>({
    nombre: '',
    tipo: 'tractor',
    marca: '',
    modelo: '',
    anio_fabricacion: null,
    numero_serie: '',
    fecha_compra: '',
    kilometros_uso: null,
    estado: 'ACTIVA',
    costo_por_hora: null,
    kilometros_mantenimiento_intervalo: null,
    ultimo_mantenimiento_kilometros: null,
    rendimiento_combustible: null,
    unidad_rendimiento: 'km/L',
    costo_combustible_por_litro: null,
    valor_actual: null,
    notas: ''
  });

  // Verificar si el usuario puede modificar maquinaria
  const puedeModificarMaquinaria = () => {
    if (!rolUsuario) return false;
    // Solo OPERARIO, INVITADO y CONSULTOR_EXTERNO NO pueden modificar (solo lectura)
    return rolUsuario !== 'OPERARIO' && 
           rolUsuario !== 'INVITADO' && 
           rolUsuario !== 'CONSULTOR_EXTERNO' && 
           rolUsuario !== 'LECTURA';
  };

  const tiposMaquinaria = ['tractor', 'cosechadora', 'pulverizadora', 'sembradora', 'arado', 'rastra', 'otro'];
  const estadosMaquinaria = ['ACTIVA', 'OPERATIVA', 'EN_MANTENIMIENTO', 'FUERA_DE_SERVICIO', 'RETIRADA'];
  const unidadesRendimiento = ['km/L', 'L/hora', 'L/km'];

  // Cargar datos desde la API
  const loadMaquinaria = async () => {
    try {
      setLoading(true);
        const response = await api.get('/api/maquinaria');
      
      // Mapear datos del backend al formato esperado por el frontend
      const maquinariaMapeada = response.data.map((maq: any) => ({
        id: maq.id,
        nombre: maq.nombre || '',
        tipo: maq.tipo || 'No especificado',
        marca: maq.marca || '',
        modelo: maq.modelo || '',
        anio_fabricacion: maq.anioFabricacion || null,
        numero_serie: maq.numeroSerie || '',
        fecha_compra: maq.fechaCompra ? new Date(maq.fechaCompra).toISOString().split('T')[0] : '',
        kilometros_uso: maq.kilometrosUso || null,
        estado: maq.estado || 'ACTIVA',
        costo_por_hora: maq.costoPorHora || null,
        kilometros_mantenimiento_intervalo: maq.kilometrosMantenimientoIntervalo || null,
        ultimo_mantenimiento_kilometros: maq.ultimoMantenimientoKilometros || null,
        rendimiento_combustible: maq.rendimientoCombustible || null,
        unidad_rendimiento: maq.unidadRendimiento || 'km/L',
        costo_combustible_por_litro: maq.costoCombustiblePorLitro || null,
        valor_actual: maq.valorActual || null,
        notas: maq.descripcion || ''
      }));
      
      console.log('🔧 [Maquinaria] Datos mapeados:', maquinariaMapeada);
      console.log('🔧 [Maquinaria] Total cargado:', maquinariaMapeada.length);
      setMaquinaria(maquinariaMapeada);
    } catch (error) {
      console.error('Error cargando maquinaria:', error);
      setMaquinaria([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadMaquinaria();
  }, []);

  const saveMaquinaria = async () => {
    try {
      // Preparar datos para envío, mapeando a camelCase para el backend
      const datosParaEnvio = {
        nombre: formData.nombre,
        tipo: formData.tipo,
        marca: formData.marca,
        modelo: formData.modelo,
        anioFabricacion: formData.anio_fabricacion || null,
        numeroSerie: formData.numero_serie,
        fechaCompra: formData.fecha_compra,
        kilometrosUso: formData.kilometros_uso || 0,
        estado: formData.estado,
        costoPorHora: formData.costo_por_hora || 0,
        kilometrosMantenimientoIntervalo: formData.kilometros_mantenimiento_intervalo || 5000,
        ultimoMantenimientoKilometros: formData.ultimo_mantenimiento_kilometros || 0,
        rendimientoCombustible: formData.rendimiento_combustible || 0,
        unidadRendimiento: formData.unidad_rendimiento,
        costoCombustiblePorLitro: formData.costo_combustible_por_litro || 0,
        valorActual: formData.valor_actual || 0,
        descripcion: formData.notas
      };

      if (editingMaquinaria) {
        // Actualizar maquinaria existente
        await api.put(`/maquinaria/${editingMaquinaria.id}`, datosParaEnvio);
        setMaquinaria(prev => prev.map(m => m.id === editingMaquinaria.id ? { ...formData, id: m.id } : m));
      } else {
        // Crear nueva maquinaria
        const response = await api.post('/maquinaria', datosParaEnvio);
        setMaquinaria(prev => [...prev, { ...formData, id: response.data.id }]);
      }
      resetForm();
    } catch (error) {
      console.error('Error guardando maquinaria:', error);
      alert('Error al guardar la maquinaria. Por favor, intente nuevamente.');
    }
  };

  // Mostrar formulario con scroll
  const showFormWithScroll = () => {
    setShowForm(true);
    setTimeout(() => {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }, 100);
  };

  const editMaquinaria = (maq: Maquinaria) => {
    setEditingMaquinaria(maq);
    setFormData(maq);
    showFormWithScroll();
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
      marca: '',
      modelo: '',
      anio_fabricacion: null,
      numero_serie: '',
      fecha_compra: '',
      kilometros_uso: null,
      estado: 'ACTIVA',
      costo_por_hora: null,
      kilometros_mantenimiento_intervalo: null,
      ultimo_mantenimiento_kilometros: null,
      rendimiento_combustible: null,
      unidad_rendimiento: 'km/L',
      costo_combustible_por_litro: null,
      valor_actual: null,
      notas: ''
    });
    setEditingMaquinaria(null);
    setShowForm(false);
  };

  const getEstadoMantenimiento = (maq: Maquinaria) => {
    const kilometrosUso = maq.kilometros_uso || 0;
    const ultimoMantenimiento = maq.ultimo_mantenimiento_kilometros || 0;
    const intervaloMantenimiento = maq.kilometros_mantenimiento_intervalo || 1000;
    
    const kilometrosDesdeMantenimiento = kilometrosUso - ultimoMantenimiento;
    if (kilometrosDesdeMantenimiento >= intervaloMantenimiento) {
      return { estado: 'MANTENIMIENTO REQUERIDO', color: '#ef4444' };
    } else if (kilometrosDesdeMantenimiento >= intervaloMantenimiento * 0.8) {
      return { estado: 'MANTENIMIENTO PRÓXIMO', color: '#f59e0b' };
    }
    return { estado: 'OK', color: '#10b981' };
  };

  const filteredMaquinaria = maquinaria.filter(maq => 
    maq.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const estadisticas = {
    total: maquinaria.length,
    activas: maquinaria.filter(m => m.estado === 'ACTIVA' || m.estado === 'OPERATIVA').length,
    alertas: 0
  };

  // Debug: Log de estadísticas
  console.log('🔧 [Maquinaria] Estadísticas calculadas:', estadisticas);
  console.log('🔧 [Maquinaria] Estados encontrados:', maquinaria.map(m => m.estado));

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
        <PermissionGate permission="canCreateMaquinaria">
          <button
            onClick={showFormWithScroll}
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
        </PermissionGate>
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
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Marca/Modelo</th>
                <th style={{ padding: '0.75rem', textAlign: 'left', borderBottom: '1px solid #e5e7eb' }}>Año</th>
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
                        {maq.tipo || 'No especificado'} • Compra: {maq.fecha_compra ? new Date(maq.fecha_compra).toLocaleDateString() : 'No especificada'}
                      </div>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div style={{ fontWeight: 'bold' }}>{maq.marca || 'Sin marca'}</div>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                        {maq.modelo || 'Sin modelo'}
                      </div>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        background: '#e5e7eb',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        fontSize: '0.75rem'
                      }}>
                        {maq.anio_fabricacion || 'N/A'}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <span style={{
                        background: (maq.estado === 'ACTIVA' || maq.estado === 'OPERATIVA') ? '#d1fae5' : '#fef3c7',
                        color: (maq.estado === 'ACTIVA' || maq.estado === 'OPERATIVA') ? '#065f46' : '#92400e',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        fontSize: '0.75rem'
                      }}>
                        {maq.estado}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div>{(maq.kilometros_uso || 0).toFixed(1)} km</div>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                        Último: {(maq.ultimo_mantenimiento_kilometros || 0).toFixed(1)} km
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
                      {formatCurrency(maq.costo_por_hora || 0)}/hs
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div>{maq.rendimiento_combustible || 0} {maq.unidad_rendimiento || 'km/L'}</div>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                        {formatCurrency(maq.costo_combustible_por_litro || 0)}/L
                      </div>
                    </td>
                    <td style={{ padding: '0.75rem' }}>
                      <div style={{ display: 'flex', gap: '0.25rem' }}>
                        {puedeModificarMaquinaria() ? (
                          <>
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
                          </>
                        ) : (
                          <span style={{ 
                            color: '#6b7280',
                            fontSize: '0.75rem',
                            fontStyle: 'italic'
                          }}>
                            Solo lectura
                          </span>
                        )}
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
      {showForm && puedeModificarMaquinaria() && (
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

              {/* Marca */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  🏭 Marca
                </label>
                <input
                  type="text"
                  placeholder="Ej: John Deere, Case IH, New Holland"
                  value={formData.marca || ''}
                  onChange={(e) => setFormData({...formData, marca: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Marca o fabricante de la maquinaria
                </small>
              </div>

              {/* Modelo */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  🔧 Modelo
                </label>
                <input
                  type="text"
                  placeholder="Ej: 5075E, Axial Flow 2388, T6.180"
                  value={formData.modelo || ''}
                  onChange={(e) => setFormData({...formData, modelo: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Modelo específico de la maquinaria
                </small>
              </div>

              {/* Año de Fabricación */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  📅 Año de Fabricación
                </label>
                <input
                  type="number"
                  placeholder="2020"
                  value={formData.anio_fabricacion || ''}
                  onChange={(e) => setFormData({...formData, anio_fabricacion: e.target.value ? parseInt(e.target.value) : null})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                  min="1900"
                  max="2030"
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Año en que fue fabricada la maquinaria
                </small>
              </div>

              {/* Número de Serie */}
              <div>
                <label style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 'bold', color: '#374151' }}>
                  🔢 Número de Serie
                </label>
                <input
                  type="text"
                  placeholder="Ej: JD5075E123456"
                  value={formData.numero_serie || ''}
                  onChange={(e) => setFormData({...formData, numero_serie: e.target.value})}
                  style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.25rem', width: '100%' }}
                />
                <small style={{ color: '#6b7280', fontSize: '0.75rem' }}>
                  Número de serie único del fabricante
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
                   value={formData.kilometros_uso || ''}
                   onChange={(e) => setFormData({...formData, kilometros_uso: e.target.value ? parseFloat(e.target.value) : null})}
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
                    <option key={estado} value={estado}>{estado.replace(/_/g, ' ')}</option>
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
                  value={formData.costo_por_hora || ''}
                  onChange={(e) => setFormData({...formData, costo_por_hora: e.target.value ? parseFloat(e.target.value) : null})}
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
                   value={formData.kilometros_mantenimiento_intervalo || ''}
                   onChange={(e) => setFormData({...formData, kilometros_mantenimiento_intervalo: e.target.value ? parseInt(e.target.value) : null})}
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
                   value={formData.ultimo_mantenimiento_kilometros || ''}
                   onChange={(e) => setFormData({...formData, ultimo_mantenimiento_kilometros: e.target.value ? parseFloat(e.target.value) : null})}
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
                     value={formData.rendimiento_combustible || ''}
                     onChange={(e) => setFormData({...formData, rendimiento_combustible: e.target.value ? parseFloat(e.target.value) : null})}
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
                   value={formData.costo_combustible_por_litro || ''}
                   onChange={(e) => setFormData({...formData, costo_combustible_por_litro: e.target.value ? parseFloat(e.target.value) : null})}
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
                   value={formData.valor_actual || ''}
                   onChange={(e) => setFormData({...formData, valor_actual: e.target.value ? parseFloat(e.target.value) : null})}
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
