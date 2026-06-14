import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { gestacionService } from '../../services/gestacionService';
import { parametrosService } from '../../services/parametrosService';
import type { Gestacion } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const GestacionListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [gestaciones, setGestaciones] = useState<Gestacion[]>([]);
  const [loading, setLoading] = useState(true);
  const [diasGestacion, setDiasGestacion] = useState<number>(115); // Valor por defecto

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [gestacionesData, parametrosData] = await Promise.all([
        gestacionService.obtenerActivas(),
        parametrosService.obtenerParametrosProductivos().catch(() => null)
      ]);
      setGestaciones(gestacionesData);
      if (parametrosData?.diasPromedioGestacion) {
        setDiasGestacion(parametrosData.diasPromedioGestacion);
      }
    } catch (error) {
      console.error('Error:', error);
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

  const obtenerColorDias = (dias: number) => {
    if (dias < 0) return '#ef4444'; // Pasado
    if (dias <= 7) return '#f59e0b'; // Próximo (7 días)
    if (dias <= 14) return '#3b82f6'; // Próximo (14 días)
    return '#10b981'; // Normal
  };

  const calcularDiasTranscurridos = (fechaInicio: string) => {
    const hoy = new Date();
    const inicio = new Date(fechaInicio);
    const diff = Math.floor((hoy.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando gestaciones...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Gestaciones Activas
        </h1>
        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
          Total: {gestaciones.length} gestación{gestaciones.length !== 1 ? 'es' : ''}
        </div>
      </div>

      {gestaciones.length === 0 ? (
        <div style={{
          backgroundColor: 'white',
          padding: '3rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          textAlign: 'center',
          color: '#6b7280'
        }}>
          <Icon name="Baby" size={48} />
          <p>No hay gestaciones activas</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {gestaciones.map((gestacion) => {
            const diasRestantes = calcularDiasRestantes(gestacion.fechaProbableParto);
            const diasTranscurridos = calcularDiasTranscurridos(gestacion.fechaInicio);
            const porcentaje = Math.min(100, Math.max(0, (diasTranscurridos / diasGestacion) * 100));

            return (
              <div
                key={gestacion.id}
                style={{
                  backgroundColor: 'white',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                  transition: 'box-shadow 0.15s'
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.boxShadow = '0 4px 6px rgba(0, 0, 0, 0.1)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.boxShadow = '0 1px 3px rgba(0, 0, 0, 0.1)';
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1rem' }}>
                  <div style={{ flex: 1 }}>
                    <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '0.5rem', color: '#1f2937' }}>
                      {gestacion.madreIdentificacion || `Madre ID: ${gestacion.madreId}`}
                    </h3>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Inicio</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                          {new Date(gestacion.fechaInicio).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Probable Parto</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                          {new Date(gestacion.fechaProbableParto).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días Transcurridos</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{diasTranscurridos} días</p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días Restantes</p>
                        <p style={{
                          fontSize: '0.875rem',
                          fontWeight: '500',
                          color: obtenerColorDias(diasRestantes)
                        }}>
                          {diasRestantes < 0 ? `${Math.abs(diasRestantes)} días pasados` : `${diasRestantes} días`}
                        </p>
                      </div>
                    </div>
                    <div style={{ marginTop: '1rem' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.75rem', color: '#6b7280' }}>
                        <span>Progreso de gestación</span>
                        <span>{Math.round(porcentaje)}%</span>
                      </div>
                      <div style={{
                        width: '100%',
                        height: '8px',
                        backgroundColor: '#e5e7eb',
                        borderRadius: '9999px',
                        overflow: 'hidden'
                      }}>
                        <div style={{
                          width: `${porcentaje}%`,
                          height: '100%',
                          backgroundColor: obtenerColorDias(diasRestantes),
                          transition: 'width 0.3s'
                        }} />
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => navigate(`/porcinos/gestacion/${gestacion.id}`)}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#3b82f6',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '500',
                      fontSize: '0.875rem',
                      marginLeft: '1rem'
                    }}
                  >
                    Ver Detalle
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default GestacionListScreen;
