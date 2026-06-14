import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useMadres } from '../../hooks/useMadres';
import type { EstadoMadre, Origen, FiltrosMadres } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const MadresListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [filtros, setFiltros] = useState<FiltrosMadres>({});
  const { madres, loading, error } = useMadres(filtros);

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

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando madres...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="error" size={32} />
        <p style={{ color: '#ef4444' }}>{error}</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="PiggyBank" size={32} />
          Gestión de Madres
        </h1>
        <button
          onClick={() => navigate('/porcinos/madres/nueva')}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nueva Madre
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
              value={filtros.estado || ''}
              onChange={(e) => setFiltros({ ...filtros, estado: e.target.value as EstadoMadre || undefined })}
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
              value={filtros.origen || ''}
              onChange={(e) => setFiltros({ ...filtros, origen: e.target.value as Origen || undefined })}
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
              value={filtros.identificacion || ''}
              onChange={(e) => setFiltros({ ...filtros, identificacion: e.target.value || undefined })}
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
              value={filtros.fechaIngresoDesde || ''}
              onChange={(e) => setFiltros({ ...filtros, fechaIngresoDesde: e.target.value || undefined })}
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
              value={filtros.fechaIngresoHasta || ''}
              onChange={(e) => setFiltros({ ...filtros, fechaIngresoHasta: e.target.value || undefined })}
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

      {/* Tabla de madres */}
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
    </div>
  );
};

export default MadresListScreen;

