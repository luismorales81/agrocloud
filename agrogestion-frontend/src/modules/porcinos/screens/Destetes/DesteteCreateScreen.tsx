import React, { useState, useEffect, useMemo } from 'react';
import { Icon } from '../../../../components/icons';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { desteteService } from '../../services/desteteService';
import { partosService } from '../../services/partosService';
import { parametrosService } from '../../services/parametrosService';
import type { Destete, Parto, ParametrosProductivosPorcino } from '../../types';

const DesteteCreateScreen: React.FC = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const partoIdParam = searchParams.get('partoId');

  const [partos, setPartos] = useState<Parto[]>([]);
  const [parametros, setParametros] = useState<ParametrosProductivosPorcino | null>(null);
  const [formData, setFormData] = useState<Partial<Destete>>({
    partoId: partoIdParam ? parseInt(partoIdParam) : 0,
    fechaDestete: new Date().toISOString().split('T')[0],
    cantidadDestetados: 0,
    pesoPromedioDestete: 0,
    diasLactancia: undefined,
    observaciones: '',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [partoSeleccionado, setPartoSeleccionado] = useState<Parto | null>(null);
  const [busquedaParto, setBusquedaParto] = useState('');
  const [mostrarSelectorPartos, setMostrarSelectorPartos] = useState(false);

  useEffect(() => {
    cargarDatos();
  }, [partoIdParam]);

  useEffect(() => {
    if (formData.partoId && formData.partoId > 0) {
      const parto = partos.find(p => p.id === formData.partoId);
      setPartoSeleccionado(parto || null);
      if (parto && formData.fechaDestete) {
        calcularDiasLactancia(parto, formData.fechaDestete);
      }
    }
  }, [formData.partoId, formData.fechaDestete, partos]);

  const cargarDatos = async () => {
    try {
      const [partosData, parametrosData] = await Promise.all([
        partosService.listar({}),
        parametrosService.obtenerParametrosProductivos().catch(() => null),
      ]);
      
      // Filtrar solo partos sin destete y sin fecha fin
      const partosDisponibles = partosData.filter((p: Parto) => 
        !p.fechaFin && p.nacidosVivos > 0
      );
      setPartos(partosDisponibles);
      setParametros(parametrosData);
      
      if (partoIdParam) {
        const parto = partosDisponibles.find((p: Parto) => p.id === parseInt(partoIdParam));
        if (parto) {
          setPartoSeleccionado(parto);
          setFormData(prev => ({ ...prev, partoId: parto.id }));
        }
      }
    } catch (error) {
      console.error('Error al cargar datos:', error);
    }
  };

  const calcularDiasLactancia = (parto: Parto, fechaDestete: string) => {
    const inicio = new Date(parto.fechaInicio);
    const desteteFecha = new Date(fechaDestete);
    const dias = Math.floor((desteteFecha.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
    setFormData(prev => ({ ...prev, diasLactancia: dias }));
  };

  const handleFechaDesteteChange = (fecha: string) => {
    setFormData(prev => ({ ...prev, fechaDestete: fecha }));
    if (partoSeleccionado) {
      calcularDiasLactancia(partoSeleccionado, fecha);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.partoId || formData.partoId === 0 || !partoSeleccionado) {
      setError('Debe seleccionar un parto');
      return;
    }

    if (!formData.cantidadDestetados || formData.cantidadDestetados <= 0) {
      setError('La cantidad de destetados debe ser mayor a cero');
      return;
    }

    if (!formData.pesoPromedioDestete || formData.pesoPromedioDestete <= 0) {
      setError('El peso promedio debe ser mayor a cero');
      return;
    }

    if (partoSeleccionado && formData.cantidadDestetados > partoSeleccionado.nacidosVivos) {
      setError(`La cantidad destetada no puede ser mayor a los nacidos vivos (${partoSeleccionado.nacidosVivos})`);
      return;
    }

    setLoading(true);
    try {
      await desteteService.crear(formData.partoId!, formData);
      navigate('/porcinos/destetes');
    } catch (error: any) {
      setError(error.response?.data?.message || 'Error al registrar el destete');
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const diasLactanciaEsperados = parametros?.diasLactancia || 21;

  // Filtrar partos por búsqueda
  const partosFiltrados = useMemo(() => {
    if (!busquedaParto.trim()) return partos;
    const busqueda = busquedaParto.toLowerCase();
    return partos.filter(p => 
      p.id?.toString().includes(busqueda) ||
      p.madreIdentificacion?.toLowerCase().includes(busqueda) ||
      new Date(p.fechaInicio).toLocaleDateString('es-ES').toLowerCase().includes(busqueda) ||
      p.madreId.toString().includes(busqueda)
    );
  }, [partos, busquedaParto]);

  // Manejar selección de parto
  const handleSeleccionarParto = (parto: Parto) => {
    setPartoSeleccionado(parto);
    setFormData(prev => ({ ...prev, partoId: parto.id! }));
    setMostrarSelectorPartos(false);
    setBusquedaParto('');
    if (formData.fechaDestete) {
      calcularDiasLactancia(parto, formData.fechaDestete);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: '800px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/destetes')}
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
          <Icon name="Baby" size={32} />
          Registrar Destete
        </h1>
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

      {parametros && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#eff6ff',
          border: '1px solid #bfdbfe',
          borderRadius: '0.375rem',
          marginBottom: '1.5rem',
          fontSize: '0.875rem',
          color: '#1e40af'
        }}>
          <strong>Configuración:</strong> Días de lactancia esperados: {diasLactanciaEsperados} días
        </div>
      )}

      <form onSubmit={handleSubmit} style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          <div style={{ position: 'relative' }}>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Parto *
            </label>
            {partoSeleccionado ? (
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
                    Parto #{partoSeleccionado.id} - {new Date(partoSeleccionado.fechaInicio).toLocaleDateString('es-ES')}
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>
                    Madre: {partoSeleccionado.madreIdentificacion || `Madre nº ${partoSeleccionado.madreId}`} · 
                    Nacidos vivos: {partoSeleccionado.nacidosVivos}
                  </div>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    setPartoSeleccionado(null);
                    setFormData(prev => ({ ...prev, partoId: 0 }));
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
                onClick={() => setMostrarSelectorPartos(true)}
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
                <span>Seleccione un parto...</span>
                <Icon name="ChevronDown" size={18} />
              </button>
            )}
            
            {mostrarSelectorPartos && (
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
              }} onClick={() => setMostrarSelectorPartos(false)}>
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
                      Seleccionar Parto
                    </h2>
                    <button
                      type="button"
                      onClick={() => setMostrarSelectorPartos(false)}
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
                        placeholder="Buscar por ID, madre o fecha..."
                        value={busquedaParto}
                        onChange={(e) => setBusquedaParto(e.target.value)}
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
                    {partosFiltrados.length === 0 ? (
                      <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
                        {busquedaParto ? 'No se encontraron partos que coincidan con la búsqueda' : 'No hay partos disponibles'}
                      </div>
                    ) : (
                      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                        <thead>
                          <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>ID</th>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Fecha</th>
                            <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Madre</th>
                            <th style={{ padding: '0.75rem', textAlign: 'center', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Nacidos Vivos</th>
                            <th style={{ padding: '0.75rem', textAlign: 'center', fontSize: '0.75rem', fontWeight: '600', color: '#6b7280' }}>Acción</th>
                          </tr>
                        </thead>
                        <tbody>
                          {partosFiltrados.map((parto, index) => (
                            <tr
                              key={parto.id}
                              style={{
                                borderBottom: '1px solid #f3f4f6',
                                transition: 'background-color 0.15s',
                                cursor: 'pointer'
                              }}
                              onMouseEnter={(e) => e.currentTarget.style.backgroundColor = '#f9fafb'}
                              onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'white'}
                            >
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937', fontWeight: '500' }}>
                                #{parto.id}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937' }}>
                                {new Date(parto.fechaInicio).toLocaleDateString('es-ES')}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#1f2937' }}>
                                {parto.madreIdentificacion || `ID: ${parto.madreId}`}
                              </td>
                              <td style={{ padding: '0.75rem', fontSize: '0.875rem', textAlign: 'center', color: '#10b981', fontWeight: '500' }}>
                                {parto.nacidosVivos}
                              </td>
                              <td style={{ padding: '0.75rem', textAlign: 'center' }}>
                                <button
                                  type="button"
                                  onClick={() => handleSeleccionarParto(parto)}
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
                    {partosFiltrados.length} parto{partosFiltrados.length !== 1 ? 's' : ''} disponible{partosFiltrados.length !== 1 ? 's' : ''}
                  </div>
                </div>
              </div>
            )}
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Fecha Destete *
            </label>
            <input
              type="date"
              value={formData.fechaDestete || ''}
              onChange={(e) => handleFechaDesteteChange(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Cantidad Destetados *
            </label>
            <input
              type="number"
              min="1"
              max={partoSeleccionado?.nacidosVivos || 999}
              value={formData.cantidadDestetados || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, cantidadDestetados: parseInt(e.target.value) || 0 }))}
              required
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
            {partoSeleccionado && (
              <div style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
                Máximo disponible: {partoSeleccionado.nacidosVivos}
              </div>
            )}
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Peso Promedio al Destete (kg) *
            </label>
            <input
              type="number"
              step="0.01"
              min="0.01"
              value={formData.pesoPromedioDestete || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, pesoPromedioDestete: parseFloat(e.target.value) || 0 }))}
              required
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
            {parametros?.pesoDesteteObjetivo && (
              <div style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
                Peso objetivo configurado: {parametros.pesoDesteteObjetivo} kg
              </div>
            )}
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Días de Lactancia
            </label>
            <input
              type="number"
              min="0"
              value={formData.diasLactancia || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, diasLactancia: parseInt(e.target.value) || undefined }))}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem',
                backgroundColor: '#f9fafb'
              }}
              readOnly
            />
            <div style={{ marginTop: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
              Calculado automáticamente desde la fecha del parto hasta la fecha de destete
            </div>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
              Observaciones
            </label>
            <textarea
              value={formData.observaciones || ''}
              onChange={(e) => setFormData(prev => ({ ...prev, observaciones: e.target.value }))}
              rows={4}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem',
                fontFamily: 'inherit'
              }}
            />
          </div>

          <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end' }}>
            <button
              type="button"
              onClick={() => navigate('/porcinos/destetes')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#6b7280',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontSize: '0.875rem',
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
                fontSize: '0.875rem',
                fontWeight: '500'
              }}
            >
              {loading ? 'Registrando...' : 'Registrar Destete'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};

export default DesteteCreateScreen;

