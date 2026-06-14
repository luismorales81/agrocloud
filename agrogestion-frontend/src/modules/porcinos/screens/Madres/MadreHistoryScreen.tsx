import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { madresService } from '../../services/madresService';
import { partosService } from '../../services/partosService';
import { serviciosService } from '../../services/serviciosService';
import { gestacionService } from '../../services/gestacionService';
import type { Parto, Servicio, Gestacion } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const MadreHistoryScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [historial, setHistorial] = useState<any[]>([]);
  const [partos, setPartos] = useState<Parto[]>([]);
  const [servicios, setServicios] = useState<Servicio[]>([]);
  const [gestaciones, setGestaciones] = useState<Gestacion[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      cargarHistorial();
    }
  }, [id]);

  const cargarHistorial = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [historialData, partosData, serviciosData, gestacionesData] = await Promise.all([
        madresService.obtenerHistorial(parseInt(id)).catch(() => []),
        partosService.obtenerPorMadre(parseInt(id)).catch(() => []),
        serviciosService.obtenerPorMadre(parseInt(id)).catch(() => []),
        gestacionService.obtenerPorMadre(parseInt(id)).catch(() => []),
      ]);
      setHistorial(historialData);
      setPartos(partosData);
      setServicios(serviciosData);
      setGestaciones(gestacionesData);
    } catch (error) {
      console.error('Error cargando historial:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          📜 Historial Completo
        </h1>
        <button onClick={() => navigate(`/porcinos/madres/${id}`)} style={{
          padding: '0.5rem 1rem',
          backgroundColor: '#6b7280',
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          cursor: 'pointer'
        }}>
          ← Volver
        </button>
      </div>

      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}><SemanticIcon semanticName="pending" size={32} /> Cargando historial...</div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
          {/* Historial de Partos */}
          <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem', color: '#1f2937' }}>
              <Icon name="Baby" size={24} style={{ marginRight: '0.5rem' }} /> Historial de Partos
            </h2>
            {partos.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#6b7280', padding: '2rem' }}>No hay partos registrados</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {partos.map((parto) => (
                  <div
                    key={parto.id}
                    onClick={() => navigate(`/porcinos/partos/${parto.id}`)}
                    style={{
                      padding: '1rem',
                      borderLeft: '4px solid #ec4899',
                      backgroundColor: '#f9fafb',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      transition: 'background-color 0.15s'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = '#f3f4f6';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = '#f9fafb';
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.5rem' }}>
                      <div style={{ fontWeight: '600', fontSize: '1rem' }}>
                        Parto del {new Date(parto.fechaInicio).toLocaleDateString('es-ES')}
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/porcinos/partos/${parto.id}`);
                        }}
                        style={{
                          padding: '0.25rem 0.75rem',
                          backgroundColor: '#ec4899',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem'
                        }}
                      >
                        Ver Detalle
                      </button>
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '1rem', marginTop: '0.5rem' }}>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>Nacidos Vivos:</span>
                        <span style={{ fontSize: '1rem', fontWeight: '600', color: '#10b981', marginLeft: '0.5rem' }}>{parto.nacidosVivos}</span>
                      </div>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>Nacidos Muertos:</span>
                        <span style={{ fontSize: '1rem', fontWeight: '600', color: '#ef4444', marginLeft: '0.5rem' }}>{parto.nacidosMuertos}</span>
                      </div>
                      <div>
                        <span style={{ fontSize: '0.75rem', color: '#6b7280' }}>Momias:</span>
                        <span style={{ fontSize: '1rem', fontWeight: '600', marginLeft: '0.5rem' }}>{parto.momias}</span>
                      </div>
                    </div>
                    {parto.pesoPromedioNacimiento && (
                      <div style={{ marginTop: '0.5rem', fontSize: '0.875rem', color: '#6b7280' }}>
                        Peso promedio: {parto.pesoPromedioNacimiento} kg
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Historial de Servicios */}
          <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem', color: '#1f2937' }}>
              <Icon name="Heart" size={24} style={{ marginRight: '0.5rem' }} /> Historial de Servicios
            </h2>
            {servicios.length === 0 ? (
              <p style={{ textAlign: 'center', color: '#6b7280', padding: '2rem' }}>No hay servicios registrados</p>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {servicios.map((servicio) => (
                  <div
                    key={servicio.id}
                    onClick={() => navigate(`/porcinos/servicios/${servicio.id}`)}
                    style={{
                      padding: '1rem',
                      borderLeft: '4px solid #8b5cf6',
                      backgroundColor: '#f9fafb',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      transition: 'background-color 0.15s'
                    }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor = '#f3f4f6';
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = '#f9fafb';
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <div>
                        <div style={{ fontWeight: '600', marginBottom: '0.25rem' }}>
                          {servicio.tipo === 'MONTA_NATURAL' ? 'Monta Natural' : 'Inseminación Artificial'}
                        </div>
                        <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                          {new Date(servicio.fechaServicio).toLocaleDateString('es-ES')}
                        </div>
                        <div style={{ marginTop: '0.5rem' }}>
                          <span style={{
                            display: 'inline-block',
                            padding: '0.25rem 0.75rem',
                            borderRadius: '9999px',
                            fontSize: '0.75rem',
                            fontWeight: '500',
                            backgroundColor: servicio.estadoServicio === 'PREÑEZ_CONFIRMADA' ? '#10b98120' : servicio.estadoServicio === 'FALLIDO' ? '#ef444420' : '#f59e0b20',
                            color: servicio.estadoServicio === 'PREÑEZ_CONFIRMADA' ? '#10b981' : servicio.estadoServicio === 'FALLIDO' ? '#ef4444' : '#f59e0b'
                          }}>
                            {servicio.estadoServicio === 'PREÑEZ_CONFIRMADA' ? 'Preñez Confirmada' : servicio.estadoServicio === 'FALLIDO' ? 'Fallido' : 'Pendiente Control'}
                          </span>
                        </div>
                      </div>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/porcinos/servicios/${servicio.id}`);
                        }}
                        style={{
                          padding: '0.25rem 0.75rem',
                          backgroundColor: '#8b5cf6',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem'
                        }}
                      >
                        Ver Detalle
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Historial General */}
          {historial.length > 0 && (
            <div style={{ backgroundColor: 'white', padding: '1.5rem', borderRadius: '0.5rem', boxShadow: '0 1px 3px rgba(0,0,0,0.1)' }}>
              <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem', color: '#1f2937' }}>
                📜 Historial General
              </h2>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                {historial.map((item, index) => (
                  <div key={index} style={{
                    padding: '1rem',
                    borderLeft: '4px solid #3b82f6',
                    backgroundColor: '#f9fafb',
                    borderRadius: '0.375rem'
                  }}>
                    <div style={{ fontWeight: '600', marginBottom: '0.5rem' }}>{item.tipo}</div>
                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                      {new Date(item.fecha).toLocaleDateString('es-ES')}
                    </div>
                    {item.descripcion && (
                      <div style={{ marginTop: '0.5rem', fontSize: '0.875rem' }}>{item.descripcion}</div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MadreHistoryScreen;

