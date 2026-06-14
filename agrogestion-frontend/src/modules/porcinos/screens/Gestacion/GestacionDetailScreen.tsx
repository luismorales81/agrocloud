import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { gestacionService } from '../../services/gestacionService';
import { chequeoGestacionService } from '../../services/chequeoGestacionService';
import { parametrosService } from '../../services/parametrosService';
import type { Gestacion, ChequeoGestacion } from '../../types';

const GestacionDetailScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [gestacion, setGestacion] = useState<Gestacion | null>(null);
  const [chequeos, setChequeos] = useState<ChequeoGestacion[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [diasGestacion, setDiasGestacion] = useState<number>(115); // Valor por defecto
  const [showModalAborto, setShowModalAborto] = useState(false);
  const [showModalChequeo, setShowModalChequeo] = useState(false);
  const [formAborto, setFormAborto] = useState({ fechaAborto: '', causaAborto: '' });
  const [formChequeo, setFormChequeo] = useState({
    fecha: new Date().toISOString().split('T')[0],
    metodo: 'ECO' as 'ECO' | 'PALPACION' | 'OBSERVACION',
    resultado: 'POSITIVO' as 'POSITIVO' | 'NEGATIVO',
    observaciones: '',
  });

  useEffect(() => {
    if (id) {
      cargarDatos();
    }
  }, [id]);

  const cargarDatos = async () => {
    setLoading(true);
    setError(null);
    try {
      const [gestacionData, chequeosData, parametrosData] = await Promise.all([
        gestacionService.obtener(parseInt(id!)),
        chequeoGestacionService.obtenerPorGestacion(parseInt(id!)).catch(() => []), // Si falla cargar chequeos, usar array vacío
        parametrosService.obtenerParametrosProductivos().catch(() => null)
      ]);
      setGestacion(gestacionData);
      setChequeos(chequeosData || []);
      if (parametrosData?.diasPromedioGestacion) {
        setDiasGestacion(parametrosData.diasPromedioGestacion);
      }
    } catch (error: any) {
      console.error('Error:', error);
      if (error.response?.status === 404) {
        setError('Gestación no encontrada');
      } else {
        setError('Error al cargar los datos de la gestación');
      }
    } finally {
      setLoading(false);
    }
  };

  const calcularDiasRestantes = (fechaProbableParto: string) => {
    const hoy = new Date();
    const fechaParto = new Date(fechaProbableParto);
    const diff = Math.ceil((fechaParto.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const calcularDiasTranscurridos = (fechaInicio: string) => {
    const hoy = new Date();
    const inicio = new Date(fechaInicio);
    const diff = Math.floor((hoy.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const handleRegistrarAborto = async () => {
    try {
      await gestacionService.registrarAborto(parseInt(id!), formAborto.fechaAborto, formAborto.causaAborto);
      setShowModalAborto(false);
      cargarDatos();
    } catch (error) {
      console.error('Error al registrar aborto:', error);
      alert('Error al registrar aborto');
    }
  };

  const handleRegistrarChequeo = async () => {
    try {
      await chequeoGestacionService.crear(parseInt(id!), formChequeo);
      setShowModalChequeo(false);
      setFormChequeo({
        fecha: new Date().toISOString().split('T')[0],
        metodo: 'ECO',
        resultado: 'POSITIVO',
        observaciones: '',
      });
      cargarDatos();
    } catch (error) {
      console.error('Error al registrar chequeo:', error);
      alert('Error al registrar chequeo');
    }
  };


  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando gestación...</p>
      </div>
    );
  }

  if (!gestacion || error) {
    return (
      <div style={{ padding: '2rem', maxWidth: '600px', margin: '0 auto' }}>
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          border: '1px solid #fecaca',
          borderRadius: '0.5rem',
          marginBottom: '1.5rem',
          color: '#991b1b',
          textAlign: 'center'
        }}>
          <SemanticIcon semanticName="error" size={32} style={{ marginBottom: '0.5rem' }} />
          <p style={{ fontWeight: '500', marginBottom: '0.5rem' }}>
            {error || 'Gestación no encontrada'}
          </p>
          <p style={{ fontSize: '0.875rem' }}>
            La gestación solicitada no existe o no tienes permisos para verla.
          </p>
        </div>
        <div style={{ textAlign: 'center' }}>
          <button
            onClick={() => navigate('/porcinos/gestacion')}
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
            Volver a Gestaciones
          </button>
        </div>
      </div>
    );
  }

  const diasRestantes = calcularDiasRestantes(gestacion.fechaProbableParto);
  const diasTranscurridos = calcularDiasTranscurridos(gestacion.fechaInicio);
  const porcentaje = Math.min(100, Math.max(0, (diasTranscurridos / diasGestacion) * 100));

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/gestacion')}
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
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Detalle de Gestación
        </h1>
      </div>

      {/* Información Principal */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Madre</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{gestacion.madreIdentificacion || `ID: ${gestacion.madreId}`}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Estado</p>
            <span style={{
              display: 'inline-block',
              padding: '0.25rem 0.75rem',
              borderRadius: '9999px',
              fontSize: '0.75rem',
              fontWeight: '500',
              backgroundColor: gestacion.estado === 'EN_CURSO' ? '#10b98120' : gestacion.estado === 'ABORTO' ? '#ef444420' : '#6b728020',
              color: gestacion.estado === 'EN_CURSO' ? '#10b981' : gestacion.estado === 'ABORTO' ? '#ef4444' : '#6b7280'
            }}>
              {gestacion.estado === 'EN_CURSO' ? 'En Curso' : gestacion.estado === 'ABORTO' ? 'Aborto' : 'Finalizada'}
            </span>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Inicio</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(gestacion.fechaInicio).toLocaleDateString('es-ES')}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Probable Parto</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(gestacion.fechaProbableParto).toLocaleDateString('es-ES')}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días Transcurridos</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{diasTranscurridos} días</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días Restantes</p>
            <p style={{
              fontSize: '1rem',
              fontWeight: '500',
              color: diasRestantes < 0 ? '#ef4444' : diasRestantes <= 7 ? '#f59e0b' : '#10b981'
            }}>
              {diasRestantes < 0 ? `${Math.abs(diasRestantes)} días pasados` : `${diasRestantes} días`}
            </p>
          </div>
        </div>

        {/* Barra de Progreso */}
        <div style={{ marginTop: '1rem' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
            <span>Progreso de gestación</span>
            <span>{Math.round(porcentaje)}%</span>
          </div>
          <div style={{
            width: '100%',
            height: '12px',
            backgroundColor: '#e5e7eb',
            borderRadius: '9999px',
            overflow: 'hidden'
          }}>
            <div style={{
              width: `${porcentaje}%`,
              height: '100%',
              backgroundColor: diasRestantes <= 7 ? '#f59e0b' : '#10b981',
              transition: 'width 0.3s'
            }} />
          </div>
        </div>

        {/* Información adicional */}
        {gestacion.fechaAborto && (
          <div style={{
            marginTop: '1.5rem',
            padding: '1rem',
            backgroundColor: '#fee2e2',
            borderRadius: '0.375rem',
            border: '1px solid #fecaca'
          }}>
            <p style={{ fontSize: '0.875rem', fontWeight: '500', color: '#991b1b', marginBottom: '0.5rem' }}>
              <SemanticIcon semanticName="warning" size={20} style={{ marginRight: '0.5rem' }} /> Aborto registrado
            </p>
            <p style={{ fontSize: '0.75rem', color: '#991b1b' }}>
              Fecha: {new Date(gestacion.fechaAborto).toLocaleDateString('es-ES')}
            </p>
            {gestacion.causaAborto && (
              <p style={{ fontSize: '0.75rem', color: '#991b1b' }}>
                Causa: {gestacion.causaAborto}
              </p>
            )}
          </div>
        )}


        {/* Acciones */}
        {gestacion.estado === 'EN_CURSO' && (
          <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem', flexWrap: 'wrap' }}>
            <button
              onClick={() => setShowModalChequeo(true)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#3b82f6',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Chequeo
            </button>
            <button
              onClick={() => navigate(`/porcinos/partos/nuevo?gestacionId=${gestacion.id}&madreId=${gestacion.madreId}`)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#ec4899',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              👶 Registrar Parto
            </button>
            <button
              onClick={() => setShowModalAborto(true)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#ef4444',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <SemanticIcon semanticName="warning" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Aborto
            </button>
          </div>
        )}
      </div>

      {/* Chequeos */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937' }}>
          <Icon name="Search" size={24} style={{ marginRight: '0.5rem' }} /> Chequeos de Gestación
        </h2>
        {chequeos.length === 0 ? (
          <p style={{ color: '#6b7280' }}>No hay chequeos registrados</p>
        ) : (
          <div style={{ display: 'grid', gap: '1rem' }}>
            {chequeos.map((chequeo) => (
              <div
                key={chequeo.id}
                style={{
                  padding: '1rem',
                  backgroundColor: '#f9fafb',
                  borderRadius: '0.375rem',
                  border: '1px solid #e5e7eb'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <p style={{ fontSize: '0.875rem', fontWeight: '500', marginBottom: '0.5rem' }}>
                      {new Date(chequeo.fecha).toLocaleDateString('es-ES')}
                    </p>
                    <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: '#6b7280' }}>
                      <span>Método: {chequeo.metodo === 'ECO' ? 'Ecografía' : chequeo.metodo === 'PALPACION' ? 'Palpación' : 'Observación'}</span>
                      <span style={{
                        color: chequeo.resultado === 'POSITIVO' ? '#10b981' : '#ef4444',
                        fontWeight: '500'
                      }}>
                        Resultado: {chequeo.resultado === 'POSITIVO' ? 'Positivo' : 'Negativo'}
                      </span>
                    </div>
                    {chequeo.observaciones && (
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
                        {chequeo.observaciones}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Modal Registrar Aborto */}
      {showModalAborto && (
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
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%'
          }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              <SemanticIcon semanticName="warning" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Aborto
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha del Aborto *
                </label>
                <input
                  type="date"
                  value={formAborto.fechaAborto}
                  onChange={(e) => setFormAborto({...formAborto, fechaAborto: e.target.value})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Causa del Aborto *
                </label>
                <input
                  type="text"
                  value={formAborto.causaAborto}
                  onChange={(e) => setFormAborto({...formAborto, causaAborto: e.target.value})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  placeholder="Describa la causa del aborto"
                  required
                />
              </div>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button
                  onClick={() => setShowModalAborto(false)}
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
                  onClick={handleRegistrarAborto}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#ef4444',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '500'
                  }}
                >
                  Registrar Aborto
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Registrar Chequeo */}
      {showModalChequeo && (
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
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%'
          }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Chequeo
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha *
                </label>
                <input
                  type="date"
                  value={formChequeo.fecha}
                  onChange={(e) => setFormChequeo({...formChequeo, fecha: e.target.value})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Método *
                </label>
                <select
                  value={formChequeo.metodo}
                  onChange={(e) => setFormChequeo({...formChequeo, metodo: e.target.value as any})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="ECO">Ecografía</option>
                  <option value="PALPACION">Palpación</option>
                  <option value="OBSERVACION">Observación</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Resultado *
                </label>
                <select
                  value={formChequeo.resultado}
                  onChange={(e) => setFormChequeo({...formChequeo, resultado: e.target.value as any})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="POSITIVO">Positivo</option>
                  <option value="NEGATIVO">Negativo</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formChequeo.observaciones}
                  onChange={(e) => setFormChequeo({...formChequeo, observaciones: e.target.value})}
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    fontFamily: 'inherit'
                  }}
                />
              </div>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                <button
                  onClick={() => setShowModalChequeo(false)}
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
                  onClick={handleRegistrarChequeo}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#3b82f6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '500'
                  }}
                >
                  Registrar Chequeo
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default GestacionDetailScreen;
