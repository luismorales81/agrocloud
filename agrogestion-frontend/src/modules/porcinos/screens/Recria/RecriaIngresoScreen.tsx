import React, { useState, useEffect, useMemo } from 'react';
import { Icon } from '../../../../components/icons';
import { useNavigate } from 'react-router-dom';
import { recriaService } from '../../services/recriaService';
import api from '../../../../services/api';
import { API_ENDPOINTS } from '../../../../services/apiEndpoints';
import type { RecriaIngresoDTO, Sexo } from '../../types';

interface Lote {
  id: number;
  nombre: string;
  areaHectareas?: number;
  superficie?: number;
  campo?: {
    id: number;
    nombre: string;
  };
  campoId?: number;
  estado?: string;
  cultivoActual?: string;
}

const RecriaIngresoScreen: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<RecriaIngresoDTO>({
    loteId: 0,
    fechaIngreso: new Date().toISOString().split('T')[0],
    pesoPromedio: 0,
    cantidadAnimales: 0,
    sexo: 'HEMBRA',
    observaciones: '',
    // Este formulario representa ingresos de recría desde compras/ingresos externos
    origen: 'EXTERNO',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [lotes, setLotes] = useState<Lote[]>([]);
  const [loteSeleccionado, setLoteSeleccionado] = useState<Lote | null>(null);
  const [busquedaLote, setBusquedaLote] = useState('');
  const [mostrarSelectorLotes, setMostrarSelectorLotes] = useState(false);
  const [cargandoLotes, setCargandoLotes] = useState(false);

  useEffect(() => {
    // Cargar lotes cuando se necesite mostrar el selector
    if (mostrarSelectorLotes && lotes.length === 0) {
      cargarLotes();
    }
  }, [mostrarSelectorLotes]);

  const cargarLotes = async () => {
    setCargandoLotes(true);
    try {
      const response = await api.get(API_ENDPOINTS.PORCINOS_CATALOGOS.LOTES);
      setLotes(response.data || []);
    } catch (error) {
      console.error('Error al cargar lotes porcinos:', error);
      setError('Error al cargar los corrales/lotes disponibles');
    } finally {
      setCargandoLotes(false);
    }
  };

  // Filtrar lotes por búsqueda
  const lotesFiltrados = useMemo(() => {
    if (!busquedaLote.trim()) return lotes;
    const busqueda = busquedaLote.toLowerCase();
    return lotes.filter(l => 
      l.id?.toString().includes(busqueda) ||
      l.nombre?.toLowerCase().includes(busqueda) ||
      l.campo?.nombre?.toLowerCase().includes(busqueda) ||
      l.estado?.toLowerCase().includes(busqueda)
    );
  }, [lotes, busquedaLote]);

  // Manejar selección de lote
  const handleSeleccionarLote = (lote: Lote) => {
    setLoteSeleccionado(lote);
    setFormData(prev => ({ ...prev, loteId: lote.id! }));
    setMostrarSelectorLotes(false);
    setBusquedaLote('');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.loteId || formData.loteId === 0 || !loteSeleccionado) {
      setError('Debe seleccionar un lote');
      return;
    }

    if (!formData.cantidadAnimales || formData.cantidadAnimales <= 0) {
      setError('La cantidad de animales debe ser mayor a cero');
      return;
    }

    if (!formData.pesoPromedio || formData.pesoPromedio <= 0) {
      setError('El peso promedio debe ser mayor a cero');
      return;
    }

    setLoading(true);
    try {
      await recriaService.crear(formData);
      navigate('/porcinos/recria');
    } catch (error: any) {
      setError(error.response?.data?.message || 'Error al registrar ingreso a recría');
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/recria')}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6b7280',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            marginBottom: '1rem',
            fontSize: '0.875rem'
          }}
        >
          ← Volver
        </button>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Plus" size={32} />
          Ingreso a Recría (compra / ingreso externo)
        </h1>
        <p style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#4b5563', maxWidth: '52rem' }}>
          Use este formulario para dar de alta lotes de recría que provienen de una <strong>compra externa</strong> o un ingreso manual.
          Los lotes que provienen de un <strong>destete</strong> se crean automáticamente desde el módulo de destete y quedarán marcados como
          origen "Destete" en la lista de recrías.
        </p>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          border: '1px solid #fecaca',
          borderRadius: '0.375rem',
          marginBottom: '1.5rem',
          color: '#991b1b'
        }}>
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Información de origen del lote */}
          <div style={{
            padding: '0.75rem 1rem',
            borderRadius: '0.375rem',
            backgroundColor: '#ecfdf3',
            border: '1px solid #bbf7d0',
            color: '#166534',
            fontSize: '0.875rem'
          }}>
            <strong style={{ display: 'block', marginBottom: '0.25rem' }}>Origen del lote</strong>
            Este formulario está pensado para ingresar cerdos que <strong>provienen de una compra o ingreso externo</strong>.
            Los lotes que vienen de un <strong>destete</strong> se crean automáticamente desde el módulo de partos/destetes.
          </div>
          <div style={{ position: 'relative' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Lote *
            </label>
            {loteSeleccionado ? (
              <div style={{
                padding: '0.75rem',
                border: '1px solid #3b82f6',
                borderRadius: '0.375rem',
                backgroundColor: '#eff6ff',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}>
                <div style={{ flex: 1 }}>
                  <div style={{ fontWeight: '500', color: '#1f2937', marginBottom: '0.25rem' }}>
                    #{loteSeleccionado.id} - {loteSeleccionado.nombre}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                    {loteSeleccionado.campo?.nombre && `Campo: ${loteSeleccionado.campo.nombre} | `}
                    {loteSeleccionado.areaHectareas && `Superficie: ${loteSeleccionado.areaHectareas} ha`}
                    {loteSeleccionado.superficie && `Superficie: ${loteSeleccionado.superficie} ha`}
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setLoteSeleccionado(null);
                    setFormData(prev => ({ ...prev, loteId: 0 }));
                  }}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    padding: '0.25rem',
                    marginLeft: '0.5rem',
                    color: '#6b7280'
                  }}
                >
                  <Icon name="X" size={18} />
                </button>
              </div>
            ) : (
              <button
                type="button"
                onClick={() => setMostrarSelectorLotes(true)}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem',
                  backgroundColor: 'white',
                  cursor: 'pointer',
                  textAlign: 'left',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  color: '#6b7280'
                }}
              >
                <span>Seleccione un lote...</span>
                <Icon name="ChevronDown" size={18} />
              </button>
            )}
            
            {mostrarSelectorLotes && (
              <div style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 1000
              }} onClick={() => setMostrarSelectorLotes(false)}>
                <div style={{
                  backgroundColor: 'white',
                  borderRadius: '0.5rem',
                  maxWidth: '800px',
                  width: '90%',
                  maxHeight: '80vh',
                  display: 'flex',
                  flexDirection: 'column',
                  boxShadow: '0 10px 25px rgba(0, 0, 0, 0.2)'
                }} onClick={(e) => e.stopPropagation()}>
                  <div style={{
                    padding: '1.5rem',
                    borderBottom: '1px solid #e5e7eb',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center'
                  }}>
                    <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                      Seleccionar Lote
                    </h2>
                    <button
                      type="button"
                      onClick={() => setMostrarSelectorLotes(false)}
                      style={{
                        background: 'none',
                        border: 'none',
                        cursor: 'pointer',
                        padding: '0.25rem',
                        color: '#6b7280'
                      }}
                    >
                      <Icon name="X" size={24} />
                    </button>
                  </div>
                  
                  <div style={{ padding: '1.5rem', borderBottom: '1px solid #e5e7eb' }}>
                    <div style={{ position: 'relative' }}>
                      <Icon 
                        name="Search" 
                        size={18} 
                        style={{ 
                          position: 'absolute', 
                          left: '0.75rem', 
                          top: '50%', 
                          transform: 'translateY(-50%)',
                          color: '#9ca3af'
                        }} 
                      />
                      <input
                        type="text"
                        placeholder="Buscar por ID, nombre, campo o estado..."
                        value={busquedaLote}
                        onChange={(e) => setBusquedaLote(e.target.value)}
                        style={{
                          width: '100%',
                          padding: '0.75rem',
                          paddingLeft: '2.5rem',
                          border: '1px solid #d1d5db',
                          borderRadius: '0.375rem',
                          fontSize: '0.875rem'
                        }}
                        autoFocus
                      />
                    </div>
                  </div>

                  <div style={{ 
                    flex: 1, 
                    overflowY: 'auto',
                    maxHeight: '400px'
                  }}>
                    {cargandoLotes ? (
                      <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
                        Cargando corrales...
                      </div>
                    ) : lotesFiltrados.length === 0 ? (
                      <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
                        {busquedaLote ? 'No se encontraron corrales que coincidan con la búsqueda' : 'No hay corrales/lotes de porcinos configurados. Configure lotes de uso porcino en el módulo de cultivos (tipo Porcino) o use el destete para crear recrías desde partos.'}
                      </div>
                    ) : (
                      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                          <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>ID</th>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Nombre</th>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Campo</th>
                            <th style={{ padding: '0.75rem', textAlign: 'right', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Superficie</th>
                            <th style={{ padding: '0.75rem', textAlign: 'center', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Acción</th>
                          </tr>
                        </thead>
                        <tbody>
                          {lotesFiltrados.map((lote, index) => (
                            <tr
                              key={lote.id}
                              style={{
                                borderBottom: '1px solid #f3f4f6',
                                transition: 'background-color 0.15s',
                                cursor: 'pointer'
                              }}
                              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                            >
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937', fontWeight: '500' }}>
                                #{lote.id}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937' }}>
                                {lote.nombre}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937' }}>
                                {lote.campo?.nombre || '-'}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', textAlign: 'right', color: '#1f2937' }}>
                                {(lote.areaHectareas || lote.superficie || 0).toFixed(2)} ha
                              </td>
                              <td style={{ padding: '0.75rem', textAlign: 'center' }}>
                                <button
                                  type="button"
                                  onClick={() => handleSeleccionarLote(lote)}
                                  style={{
                                    padding: '0.5rem 1rem',
                                    backgroundColor: '#3b82f6',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '0.375rem',
                                    cursor: 'pointer',
                                    fontSize: '0.75rem',
                                    fontWeight: '500'
                                  }}
                                >
                                  Seleccionar
                                </button>
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    )}
                  </div>
                  
                  <div style={{
                    padding: '1rem',
                    borderTop: '1px solid #e5e7eb',
                    backgroundColor: '#f9fafb',
                    fontSize: '0.75rem',
                    color: '#6b7280',
                    textAlign: 'center'
                  }}>
                    {lotesFiltrados.length} lote{lotesFiltrados.length !== 1 ? 's' : ''} disponible{lotesFiltrados.length !== 1 ? 's' : ''}
                  </div>
                </div>
              </div>
            )}
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Fecha de Ingreso *
            </label>
            <input
              type="date"
              value={formData.fechaIngreso}
              onChange={(e) => setFormData({...formData, fechaIngreso: e.target.value})}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '1rem' }}>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Cantidad de Animales *
              </label>
              <input
                type="number"
                min="1"
                value={formData.cantidadAnimales || ''}
                onChange={(e) => setFormData({...formData, cantidadAnimales: parseInt(e.target.value) || 0})}
                required
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
            </div>
            <div>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                Peso Promedio (kg) *
              </label>
              <input
                type="number"
                step="0.01"
                min="0"
                value={formData.pesoPromedio || ''}
                onChange={(e) => setFormData({...formData, pesoPromedio: parseFloat(e.target.value) || 0})}
                required
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  border: '1px solid #d1d5db',
                  borderRadius: '0.375rem',
                  fontSize: '0.875rem'
                }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Sexo *
            </label>
            <select
              value={formData.sexo}
              onChange={(e) => setFormData({...formData, sexo: e.target.value as Sexo})}
              required
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="MACHO">Macho</option>
              <option value="HEMBRA">Hembra</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Observaciones
            </label>
            <textarea
              value={formData.observaciones || ''}
              onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
              rows={4}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem',
                fontFamily: 'inherit'
              }}
              placeholder="Observaciones adicionales sobre el ingreso a recría..."
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
            <button
              type="button"
              onClick={() => navigate('/porcinos/recria')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={loading}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: loading ? '#9ca3af' : '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: loading ? 'not-allowed' : 'pointer',
                fontWeight: '500'
              }}
            >
              {loading ? 'Guardando...' : 'Registrar Ingreso'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default RecriaIngresoScreen;
