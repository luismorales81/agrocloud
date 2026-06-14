import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { gestacionService } from '../../services/gestacionService';
import { partosService } from '../../services/partosService';
import { madresService } from '../../services/madresService';
import { parametrosService } from '../../services/parametrosService';
import { Autocomplete, AutocompleteOption } from '../../../../components/ui/Autocomplete';
import type { Gestacion, Parto, Madre } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

type TabType = 'gestaciones' | 'partos';

const GestacionPartosScreen: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [tabActiva, setTabActiva] = useState<TabType>('gestaciones');
  
  // Estados para Gestaciones
  const [gestaciones, setGestaciones] = useState<Gestacion[]>([]);
  const [loadingGestaciones, setLoadingGestaciones] = useState(true);
  const [diasGestacion, setDiasGestacion] = useState<number>(115);
  
  // Estados para Partos
  const [partos, setPartos] = useState<Parto[]>([]);
  const [madres, setMadres] = useState<Madre[]>([]);
  const [loadingPartos, setLoadingPartos] = useState(true);
  const [filtros, setFiltros] = useState<{
    fechaDesde?: string;
    fechaHasta?: string;
    madreId?: number;
  }>({});

  // Detectar si viene de ruta específica
  useEffect(() => {
    if (location.pathname.includes('/partos')) {
      setTabActiva('partos');
    } else if (location.pathname.includes('/gestacion')) {
      setTabActiva('gestaciones');
    }
  }, [location.pathname]);

  useEffect(() => {
    if (tabActiva === 'gestaciones') {
      cargarGestaciones();
    } else {
      cargarPartos();
    }
  }, [tabActiva]);

  useEffect(() => {
    if (tabActiva === 'partos' && (filtros.fechaDesde || filtros.fechaHasta || filtros.madreId)) {
      cargarPartos();
    }
  }, [filtros]);

  const cargarGestaciones = async () => {
    setLoadingGestaciones(true);
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
      setLoadingGestaciones(false);
    }
  };

  const cargarPartos = async () => {
    setLoadingPartos(true);
    try {
      const [partosData, madresData] = await Promise.all([
        partosService.listar(filtros),
        madresService.listar(),
      ]);
      setPartos(partosData);
      setMadres(madresData);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoadingPartos(false);
    }
  };

  const calcularDiasRestantes = (fechaProbableParto: string) => {
    const hoy = new Date();
    const fechaParto = new Date(fechaProbableParto);
    const diff = Math.ceil((fechaParto.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const obtenerColorDias = (dias: number) => {
    if (dias < 0) return '#ef4444';
    if (dias <= 7) return '#f59e0b';
    if (dias <= 14) return '#3b82f6';
    return '#10b981';
  };

  const calcularDiasTranscurridos = (fechaInicio: string) => {
    const hoy = new Date();
    const inicio = new Date(fechaInicio);
    const diff = Math.floor((hoy.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  const obtenerNombreMadre = (madreId: number) => {
    const madre = madres.find(m => m.id === madreId);
    return madre ? madre.identificacion : `ID: ${madreId}`;
  };

  const opcionesMadres: AutocompleteOption<Madre>[] = useMemo(() => {
    const opciones: AutocompleteOption<Madre>[] = [
      { value: 'todas', label: 'Todas', data: undefined }
    ];
    return opciones.concat(
      madres.map(madre => ({
        value: madre.id!,
        label: madre.identificacion,
        data: madre,
      }))
    );
  }, [madres]);

  const handleMadreFilterChange = (value: string | number | undefined, option?: AutocompleteOption<Madre>) => {
    if (value === 'todas' || value === undefined) {
      setFiltros({ ...filtros, madreId: undefined });
    } else {
      setFiltros({ ...filtros, madreId: value as number });
    }
  };

  const valorFiltroMadre = filtros.madreId !== undefined ? filtros.madreId : 'todas';

  const tabs = [
    { id: 'gestaciones' as TabType, nombre: 'Gestaciones', icono: 'Baby' },
    { id: 'partos' as TabType, nombre: 'Partos', icono: 'Baby' },
  ];

  const loading = tabActiva === 'gestaciones' ? loadingGestaciones : loadingPartos;

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Baby" size={32} />
          Gestación y Partos
        </h1>
        {tabActiva === 'partos' && (
          <button
            onClick={() => navigate('/porcinos/partos/nuevo')}
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
            Registrar Parto
          </button>
        )}
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
            {tab.id === 'gestaciones' && gestaciones.length > 0 && (
              <span style={{
                marginLeft: '0.5rem',
                padding: '0.125rem 0.5rem',
                borderRadius: '9999px',
                backgroundColor: tabActiva === tab.id ? 'rgba(255,255,255,0.3)' : '#e5e7eb',
                fontSize: '0.75rem',
                fontWeight: '600'
              }}>
                {gestaciones.length}
              </span>
            )}
          </button>
        ))}
      </div>

      {/* Contenido de los tabs */}
      {loading && (
        <div style={{ padding: '2rem', textAlign: 'center' }}>
          <SemanticIcon semanticName="pending" size={32} />
          <p>Cargando {tabActiva === 'gestaciones' ? 'gestaciones' : 'partos'}...</p>
        </div>
      )}

      {!loading && (
        <>
          {tabActiva === 'gestaciones' && (
            <>
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
                          <div style={{ display: 'flex', gap: '0.5rem', marginLeft: '1rem', flexDirection: 'column' }}>
                            <button
                              onClick={() => navigate(`/porcinos/partos/nuevo?gestacionId=${gestacion.id}&madreId=${gestacion.madreId}`)}
                              style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: '#ec4899',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontWeight: '500',
                                fontSize: '0.875rem',
                                whiteSpace: 'nowrap'
                              }}
                              title="Registrar Parto"
                            >
                              👶 Registrar Parto
                            </button>
                            <button
                              onClick={() => navigate(`/porcinos/gestacion/${gestacion.id}`)}
                              style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontWeight: '500',
                                fontSize: '0.875rem',
                                whiteSpace: 'nowrap'
                              }}
                            >
                              Ver Detalle
                            </button>
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </>
          )}

          {tabActiva === 'partos' && (
            <>
              {/* Filtros */}
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
                      Fecha Desde
                    </label>
                    <input
                      type="date"
                      value={filtros.fechaDesde || ''}
                      onChange={(e) => setFiltros({...filtros, fechaDesde: e.target.value || undefined})}
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
                      Fecha Hasta
                    </label>
                    <input
                      type="date"
                      value={filtros.fechaHasta || ''}
                      onChange={(e) => setFiltros({...filtros, fechaHasta: e.target.value || undefined})}
                      style={{
                        width: '100%',
                        padding: '0.5rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        fontSize: '0.875rem'
                      }}
                    />
                  </div>
                  <Autocomplete
                    options={opcionesMadres}
                    value={valorFiltroMadre}
                    onChange={handleMadreFilterChange}
                    placeholder="Buscar madre por identificación..."
                    label="Madre"
                    emptyMessage="No se encontraron madres"
                    maxHeight={200}
                    style={{ marginBottom: 0 }}
                  />
                  <div style={{ display: 'flex', alignItems: 'flex-end' }}>
                    <button
                      onClick={() => setFiltros({})}
                      style={{
                        padding: '0.5rem 1rem',
                        backgroundColor: '#6b7280',
                        color: 'white',
                        border: 'none',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                        fontWeight: '500',
                        width: '100%'
                      }}
                    >
                      Limpiar Filtros
                    </button>
                  </div>
                </div>
              </div>

              {/* Estadísticas */}
              {partos.length > 0 && (
                <div style={{
                  backgroundColor: 'white',
                  padding: '1.5rem',
                  borderRadius: '0.5rem',
                  boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                  marginBottom: '1.5rem',
                  display: 'grid',
                  gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))',
                  gap: '1rem'
                }}>
                  <div>
                    <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Partos</p>
                    <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>{partos.length}</p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Nacidos Vivos</p>
                    <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
                      {partos.reduce((sum, p) => sum + (p.nacidosVivos || 0), 0)}
                    </p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Nacidos Muertos</p>
                    <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>
                      {partos.reduce((sum, p) => sum + (p.nacidosMuertos || 0), 0)}
                    </p>
                  </div>
                  <div>
                    <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Nacidos</p>
                    <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
                      {partos.reduce((sum, p) => sum + (p.totalNacidos || 0), 0)}
                    </p>
                  </div>
                </div>
              )}

              {/* Lista de Partos */}
              <div style={{
                backgroundColor: 'white',
                borderRadius: '0.5rem',
                boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
                overflow: 'hidden'
              }}>
                {partos.length === 0 ? (
                  <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
                    <Icon name="Baby" size={48} />
                    <p>No hay partos registrados</p>
                  </div>
                ) : (
                  <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                        <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Fecha
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Madre
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Nacidos Vivos
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Nacidos Muertos
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Momias
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Total
                        </th>
                        <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                          Acciones
                        </th>
                      </tr>
                    </thead>
                    <tbody>
                      {partos.map((parto) => (
                        <tr
                          key={parto.id}
                          style={{
                            borderBottom: '1px solid #e5e7eb',
                            transition: 'background-color 0.15s'
                          }}
                          onMouseEnter={(e) => {
                            e.currentTarget.style.backgroundColor = '#f9fafb';
                          }}
                          onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'white';
                          }}
                        >
                          <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                            {new Date(parto.fechaInicio).toLocaleDateString('es-ES')}
                          </td>
                          <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                            {obtenerNombreMadre(parto.madreId)}
                          </td>
                          <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', color: '#10b981', fontWeight: '500' }}>
                            {parto.nacidosVivos}
                          </td>
                          <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', color: '#ef4444', fontWeight: '500' }}>
                            {parto.nacidosMuertos}
                          </td>
                          <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center' }}>
                            {parto.momias}
                          </td>
                          <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', fontWeight: '500' }}>
                            {parto.totalNacidos}
                          </td>
                          <td style={{ padding: '1rem', textAlign: 'center' }}>
                            <button
                              onClick={() => navigate(`/porcinos/partos/${parto.id}`)}
                              style={{
                                padding: '0.5rem 1rem',
                                backgroundColor: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.375rem',
                                cursor: 'pointer',
                                fontSize: '0.875rem',
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
        </>
      )}
    </div>
  );
};

export default GestacionPartosScreen;
