import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { serviciosService } from '../../services/serviciosService';
import { madresService } from '../../services/madresService';
import type { Servicio, EstadoServicio, TipoServicio, Madre } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const ServiciosListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [servicios, setServicios] = useState<Servicio[]>([]);
  const [madres, setMadres] = useState<Madre[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState<{
    estado?: EstadoServicio;
    tipo?: TipoServicio;
    madreId?: number;
  }>({ estado: 'PENDIENTE_CONTROL' }); // Por defecto mostrar solo pendientes de control

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [serviciosData, madresData] = await Promise.all([
        serviciosService.listar(),
        madresService.listar(),
      ]);
      setServicios(serviciosData);
      setMadres(madresData);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };


  const obtenerNombreMadre = (madreId: number) => {
    const madre = madres.find(m => m.id === madreId);
    return madre ? madre.identificacion : `ID: ${madreId}`;
  };

  const obtenerColorEstado = (estado: EstadoServicio) => {
    switch (estado) {
      case 'PENDIENTE_CONTROL':
        return '#f59e0b';
      case 'PREÑEZ_CONFIRMADA':
        return '#10b981';
      case 'FALLIDO':
        return '#ef4444';
      default:
        return '#6b7280';
    }
  };

  const obtenerNombreEstado = (estado: EstadoServicio) => {
    const nombres: Record<EstadoServicio, string> = {
      PENDIENTE_CONTROL: 'Pendiente Control',
      PREÑEZ_CONFIRMADA: 'Preñez Confirmada',
      FALLIDO: 'Fallido',
    };
    return nombres[estado] || estado;
  };

  const serviciosFiltrados = servicios.filter(s => {
    if (filtros.estado && s.estadoServicio !== filtros.estado) return false;
    if (filtros.tipo && s.tipo !== filtros.tipo) return false;
    if (filtros.madreId && s.madreId !== filtros.madreId) return false;
    return true;
  });

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando servicios...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Heart" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Servicios
        </h1>
        <button
          onClick={() => navigate('/porcinos/servicios/nuevo')}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nuevo Servicio
        </button>
      </div>

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
              Estado
            </label>
            <select
              value={filtros.estado || 'PENDIENTE_CONTROL'}
              onChange={(e) => setFiltros({ ...filtros, estado: e.target.value as EstadoServicio || undefined })}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todos</option>
              <option value="PENDIENTE_CONTROL">Pendiente Control</option>
              <option value="PREÑEZ_CONFIRMADA">Preñez Confirmada</option>
              <option value="FALLIDO">Fallido</option>
            </select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Tipo
            </label>
            <select
              value={filtros.tipo || ''}
              onChange={(e) => setFiltros({ ...filtros, tipo: e.target.value as TipoServicio || undefined })}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todos</option>
              <option value="MONTA_NATURAL">Monta Natural</option>
              <option value="IA">Inseminación Artificial</option>
            </select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Madre
            </label>
            <select
              value={filtros.madreId || ''}
              onChange={(e) => setFiltros({ ...filtros, madreId: e.target.value ? parseInt(e.target.value) : undefined })}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todas</option>
              {madres.map(m => (
                <option key={m.id} value={m.id}>{m.identificacion}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Lista de Servicios */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflowX: 'auto',
        overflowY: 'visible'
      }}>
        {serviciosFiltrados.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            <Icon name="Clipboard" size={48} />
            <p>No hay servicios registrados</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '800px' }}>
            <thead>
              <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Madre
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Tipo
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Fecha Servicio
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Control Celo
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Estado
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Intento #
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Acción
                </th>
              </tr>
            </thead>
            <tbody>
              {serviciosFiltrados.map((servicio) => (
                <tr
                  key={servicio.id}
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
                    {obtenerNombreMadre(servicio.madreId)}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {servicio.tipo === 'MONTA_NATURAL' ? 'Monta Natural' : 'Inseminación Artificial'}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {new Date(servicio.fechaServicio).toLocaleDateString('es-ES')}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {new Date(servicio.fechaControlCelo).toLocaleDateString('es-ES')}
                  </td>
                  <td style={{ padding: '1rem' }}>
                    <span
                      style={{
                        display: 'inline-block',
                        padding: '0.25rem 0.75rem',
                        borderRadius: '9999px',
                        fontSize: '0.75rem',
                        fontWeight: '500',
                        backgroundColor: obtenerColorEstado(servicio.estadoServicio) + '20',
                        color: obtenerColorEstado(servicio.estadoServicio)
                      }}
                    >
                      {obtenerNombreEstado(servicio.estadoServicio)}
                    </span>
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center' }}>
                    {servicio.numeroIntento}
                  </td>
                  <td style={{ padding: '1rem', textAlign: 'center', whiteSpace: 'nowrap' }}>
                    {servicio.estadoServicio === 'PENDIENTE_CONTROL' && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/porcinos/servicios/control-celo/${servicio.id}`);
                        }}
                        style={{
                          padding: '0.5rem 1rem',
                          backgroundColor: '#f59e0b',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem',
                          fontWeight: '500'
                        }}
                      >
                        Control Celo
                      </button>
                    )}
                    {servicio.estadoServicio === 'FALLIDO' && servicio.madreId && (
                      <button
                        type="button"
                        onClick={(e) => {
                          e.stopPropagation();
                          navigate(`/porcinos/servicios/nuevo?madreId=${servicio.madreId}`);
                        }}
                        style={{
                          padding: '0.5rem 1rem',
                          backgroundColor: '#3b82f6',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontSize: '0.875rem',
                          fontWeight: '500',
                          marginLeft: '0.25rem'
                        }}
                        title="Cargar nuevo servicio para la misma madre (Intento # se calcula automáticamente)"
                      >
                        Nuevo intento
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default ServiciosListScreen;
