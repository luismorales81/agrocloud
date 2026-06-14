import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useMadres } from '../../hooks/useMadres';
import { padrillosService } from '../../services/padrillosService';
import type { EstadoMadre, Origen, FiltrosMadres, Padrillo } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

type TabType = 'madres' | 'padrillos';

const ReproductoresScreen: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [tabActiva, setTabActiva] = useState<TabType>('madres');
  
  // Estados para Madres
  const [filtrosMadres, setFiltrosMadres] = useState<FiltrosMadres>({});
  const { madres, loading: loadingMadres, error: errorMadres } = useMadres(filtrosMadres);
  
  // Estados para Padrillos
  const [padrillos, setPadrillos] = useState<Padrillo[]>([]);
  const [loadingPadrillos, setLoadingPadrillos] = useState(true);

  // Detectar si viene de ruta específica
  useEffect(() => {
    if (location.pathname.includes('/padrillos')) {
      setTabActiva('padrillos');
    } else if (location.pathname.includes('/madres')) {
      setTabActiva('madres');
    }
  }, [location.pathname]);

  useEffect(() => {
    if (tabActiva === 'padrillos') {
      cargarPadrillos();
    }
  }, [tabActiva]);

  const cargarPadrillos = async () => {
    setLoadingPadrillos(true);
    try {
      const data = await padrillosService.listar();
      setPadrillos(data);
    } catch (error) {
      console.error('Error al cargar padrillos:', error);
    } finally {
      setLoadingPadrillos(false);
    }
  };

  const handleEliminarPadrillo = async (id: number, identificacion: string) => {
    if (!confirm(`¿Está seguro de que desea eliminar el padrillo "${identificacion}"?`)) {
      return;
    }

    try {
      await padrillosService.eliminar(id);
      cargarPadrillos();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Error al eliminar el padrillo');
      console.error('Error:', error);
    }
  };

  const obtenerColorEstado = (estado: EstadoMadre) => {
    switch (estado) {
      case 'CACHORRA':
        return '#f59e0b';
      case 'ADULTA':
        return '#3b82f6';
      case 'GESTACION':
        return '#8b5cf6';
      case 'LACTANCIA':
        return '#ec4899';
      case 'RECRIA':
        return '#10b981';
      case 'DESCARTE':
        return '#6b7280';
      default:
        return '#6b7280';
    }
  };

  const obtenerNombreEstado = (estado: EstadoMadre) => {
    const nombres: Record<EstadoMadre, string> = {
      CACHORRA: 'Cachorra',
      ADULTA: 'Adulta',
      GESTACION: 'Gestación',
      LACTANCIA: 'Lactancia',
      RECRIA: 'Recría',
      DESCARTE: 'Descarte',
    };
    return nombres[estado] || estado;
  };

  const tabs = [
    { id: 'madres' as TabType, nombre: 'Madres', icono: 'PiggyBank' },
    { id: 'padrillos' as TabType, nombre: 'Padrillos', icono: 'Circle' },
  ];

  const loading = tabActiva === 'madres' ? loadingMadres : loadingPadrillos;
  const error = tabActiva === 'madres' ? errorMadres : null;

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="PiggyBank" size={32} />
          Gestión de Reproductores
        </h1>
        <button
          onClick={() => {
            if (tabActiva === 'madres') {
              navigate('/porcinos/madres/nueva');
            } else {
              navigate('/porcinos/padrillos/nuevo');
            }
          }}
          style={{
            padding: '0.75rem 1.5rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '500',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem'
          }}
        >
          <Icon name="Plus" size={18} />
          {tabActiva === 'madres' ? 'Nueva Madre' : 'Nuevo Padrillo'}
        </button>
      </div>

      {/* Tabs */}
      <div style={{
        display: 'flex',
        gap: '0.5rem',
        marginBottom: '2rem',
        borderBottom: '2px solid #e5e7eb',
        paddingBottom: '1rem'
      }}>
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setTabActiva(tab.id)}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: tabActiva === tab.id ? '#3b82f6' : 'white',
              color: tabActiva === tab.id ? 'white' : '#6b7280',
              border: 'none',
              borderBottom: tabActiva === tab.id ? '3px solid #3b82f6' : '3px solid transparent',
              cursor: 'pointer',
              fontWeight: '600',
              borderRadius: '0.5rem 0.5rem 0 0',
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              transition: 'all 0.2s'
            }}
          >
            <Icon name={tab.icono} size={18} />
            {tab.nombre}
          </button>
        ))}
      </div>

      {/* Contenido de los tabs */}
      {loading && (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
          <SemanticIcon semanticName="pending" size={32} />
          <p>Cargando {tabActiva === 'madres' ? 'madres' : 'padrillos'}...</p>
        </div>
      )}

      {error && (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
          <SemanticIcon semanticName="error" size={32} />
          <p style={{ color: '#ef4444' }}>{error}</p>
        </div>
      )}

      {!loading && !error && (
        <>
          {tabActiva === 'madres' && (
            <>
              {/* Filtros para Madres */}
              <div style={{
                backgroundColor: 'white',
                padding: '1.5rem',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                marginBottom: '1.5rem'
              }}>
                <h2 style={{ fontSize: '1rem', fontWeight: '600', marginBottom: '1rem', color: '#1f2937' }}>
                  <Icon name="Search" size={20} style={{ marginRight: '0.5rem' }} /> Filtros
                </h2>
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Estado
                    </label>
                    <select
                      value={filtrosMadres.estado || ''}
                      onChange={(e) => setFiltrosMadres({ ...filtrosMadres, estado: e.target.value as EstadoMadre || undefined })}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    >
                      <option value="">Todos</option>
                      <option value="CACHORRA">Cachorra</option>
                      <option value="ADULTA">Adulta</option>
                      <option value="GESTACION">Gestación</option>
                      <option value="LACTANCIA">Lactancia</option>
                      <option value="RECRIA">Recría</option>
                      <option value="DESCARTE">Descarte</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Origen
                    </label>
                    <select
                      value={filtrosMadres.origen || ''}
                      onChange={(e) => setFiltrosMadres({ ...filtrosMadres, origen: e.target.value as Origen || undefined })}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    >
                      <option value="">Todos</option>
                      <option value="EXTERNA">Externa</option>
                      <option value="INTERNA">Interna</option>
                    </select>
                  </div>

                  <div>
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Identificación
                    </label>
                    <input
                      type="text"
                      value={filtrosMadres.identificacion || ''}
                      onChange={(e) => setFiltrosMadres({ ...filtrosMadres, identificacion: e.target.value || undefined })}
                      placeholder="Buscar por identificación..."
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
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Fecha Ingreso Desde
                    </label>
                    <input
                      type="date"
                      value={filtrosMadres.fechaIngresoDesde || ''}
                      onChange={(e) => setFiltrosMadres({ ...filtrosMadres, fechaIngresoDesde: e.target.value || undefined })}
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
                    <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                      Fecha Ingreso Hasta
                    </label>
                    <input
                      type="date"
                      value={filtrosMadres.fechaIngresoHasta || ''}
                      onChange={(e) => setFiltrosMadres({ ...filtrosMadres, fechaIngresoHasta: e.target.value || undefined })}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    />
                  </div>
                </div>
              </div>

              {/* Tabla de Madres */}
              <div style={{
                backgroundColor: 'white',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                overflow: 'hidden'
              }}>
                {madres.length === 0 ? (
                  <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
                    <Icon name="PiggyBank" size={48} />
                    <p>No se encontraron madres con los filtros seleccionados</p>
                  </div>
                ) : (
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f3f4f6', borderBottom: '2px solid #e5e7eb' }}>
                        <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#374151' }}>
                          Identificación
                        </th>
                        <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#374151' }}>
                          Estado
                        </th>
                        <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#374151' }}>
                          Último Servicio
                        </th>
                        <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#374151' }}>
                          Fecha Probable Parto
                        </th>
                        <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#374151' }}>
                          Acciones
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {madres.map((madre) => (
                        <tr
                          key={madre.id}
                          style={{
                            borderBottom: '1px solid #e5e7eb',
                            cursor: 'pointer',
                            transition: 'background-color 0.2s'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = '#f9fafb';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'white';
                          }}
                          onClick={() => navigate(`/porcinos/madres/${madre.id}`)}
                        >
                          <td style={{ padding: '0.75rem', fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
                            {madre.identificacion}
                          </td>
                          <td style={{ padding: '0.75rem' }}>
                            <span
                              style={{
                                display: 'inline-block',
                                padding: '0.25rem 0.75rem',
                                borderRadius: '0.25rem',
                                backgroundColor: obtenerColorEstado(madre.estadoActual),
                                color: 'white',
                                fontSize: '0.75rem',
                                fontWeight: '500'
                              }}
                            >
                              {obtenerNombreEstado(madre.estadoActual)}
                            </span>
                          </td>
                          <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#6b7280' }}>
                            {madre.ultimoServicio
                              ? new Date(madre.ultimoServicio.fechaServicio).toLocaleDateString('es-ES')
                              : 'N/A'}
                          </td>
                          <td style={{ padding: '0.75rem', fontSize: '0.875rem', color: '#6b7280' }}>
                            {madre.gestacionActiva
                              ? new Date(madre.gestacionActiva.fechaProbableParto).toLocaleDateString('es-ES')
                              : 'N/A'}
                          </td>
                          <td style={{ padding: '0.75rem' }}>
                            <button
                              onClick={(e) => {
                                e.stopPropagation();
                                navigate(`/porcinos/madres/${madre.id}`);
                              }}
                              style={{
                                padding: '0.25rem 0.75rem',
                                backgroundColor: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.25rem',
                                cursor: 'pointer',
                                fontSize: '0.75rem',
                                fontWeight: '500'
                              }}
                            >
                              Ver Detalle
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}

          {tabActiva === 'padrillos' && (
            <>
              {padrillos.length === 0 ? (
                <div style={{
                  backgroundColor: 'white',
                  padding: '3rem',
                  borderRadius: '0.5rem',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                  textAlign: 'center',
                  color: '#6b7280'
                }}>
                  <Icon name="Circle" size={48} />
                  <p>No hay padrillos registrados</p>
                </div>
              ) : (
                <div style={{ display: 'grid', gap: '1rem' }}>
                  {padrillos.map((padrillo) => (
                    <div
                      key={padrillo.id}
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
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                            <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                              {padrillo.identificacion}
                            </h3>
                            {padrillo.fechaBaja && (
                              <span style={{
                                display: 'inline-block',
                                padding: '0.25rem 0.75rem',
                                borderRadius: '9999px',
                                fontSize: '0.75rem',
                                fontWeight: '500',
                                backgroundColor: '#fee2e2',
                                color: '#991b1b'
                              }}>
                                Baja
                              </span>
                            )}
                            {!padrillo.fechaBaja && (
                              <span style={{
                                display: 'inline-block',
                                padding: '0.25rem 0.75rem',
                                borderRadius: '9999px',
                                fontSize: '0.75rem',
                                fontWeight: '500',
                                backgroundColor: '#10b98120',
                                color: '#10b981'
                              }}>
                                Activo
                              </span>
                            )}
                          </div>
                          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                            <div>
                              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Nacimiento</p>
                              <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                                {new Date(padrillo.fechaNacimiento).toLocaleDateString('es-ES')}
                              </p>
                            </div>
                            <div>
                              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Ingreso</p>
                              <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                                {new Date(padrillo.fechaIngresoGranja).toLocaleDateString('es-ES')}
                              </p>
                            </div>
                            <div>
                              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Origen</p>
                              <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                                {padrillo.origen === 'EXTERNA' ? 'Externa' : 'Interna'}
                              </p>
                            </div>
                            {padrillo.fechaBaja && (
                              <div>
                                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Baja</p>
                                <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                                  {new Date(padrillo.fechaBaja).toLocaleDateString('es-ES')}
                                </p>
                              </div>
                            )}
                            {padrillo.motivoBaja && (
                              <div>
                                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Motivo Baja</p>
                                <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{padrillo.motivoBaja}</p>
                              </div>
                            )}
                          </div>
                          {padrillo.observaciones && (
                            <div style={{
                              padding: '1rem',
                              backgroundColor: '#f9fafb',
                              borderRadius: '0.375rem',
                              marginTop: '1rem'
                            }}>
                              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
                              <p style={{ fontSize: '0.875rem' }}>{padrillo.observaciones}</p>
                            </div>
                          )}
                        </div>
                        <div style={{ display: 'flex', gap: '0.5rem', marginLeft: '1rem' }}>
                          <button
                            onClick={() => navigate(`/porcinos/padrillos/${padrillo.id}/editar`)}
                            style={{
                              padding: '0.5rem 1rem',
                              backgroundColor: '#3b82f6',
                              color: 'white',
                              border: 'none',
                              borderRadius: '0.375rem',
                              cursor: 'pointer',
                              fontSize: '0.875rem',
                              fontWeight: '500',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '0.5rem'
                            }}
                          >
                            <Icon name="Edit" size={16} />
                            Editar
                          </button>
                          <button
                            onClick={() => handleEliminarPadrillo(padrillo.id!, padrillo.identificacion)}
                            style={{
                              padding: '0.5rem 1rem',
                              backgroundColor: '#ef4444',
                              color: 'white',
                              border: 'none',
                              borderRadius: '0.375rem',
                              cursor: 'pointer',
                              fontSize: '0.875rem',
                              fontWeight: '500',
                              display: 'flex',
                              alignItems: 'center',
                              gap: '0.5rem'
                            }}
                          >
                            <Icon name="Trash2" size={16} />
                            Eliminar
                          </button>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          )}
        </>
      )}
    </div>
  );
};

export default ReproductoresScreen;
