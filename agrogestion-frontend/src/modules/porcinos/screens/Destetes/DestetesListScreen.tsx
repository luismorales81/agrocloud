import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { desteteService } from '../../services/desteteService';
import { partosService } from '../../services/partosService';
import type { Destete, Parto } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const DestetesListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [destetes, setDestetes] = useState<Destete[]>([]);
  const [partos, setPartos] = useState<Parto[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState<{
    fechaDesde?: string;
    fechaHasta?: string;
    partoId?: number;
  }>({});

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [destetesData, partosData] = await Promise.all([
        desteteService.listar(),
        partosService.listar({}),
      ]);
      setDestetes(destetesData);
      setPartos(partosData);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (filtros.fechaDesde || filtros.fechaHasta || filtros.partoId) {
      cargarDatos();
    }
  }, [filtros]);

  const obtenerParto = (partoId: number) => {
    return partos.find(p => p.id === partoId);
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando destetes...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Destetes
        </h1>
        <button
          onClick={() => navigate('/porcinos/destetes/nuevo')}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Destete
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
      {destetes.length > 0 && (
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
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Destetes</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>{destetes.length}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Lechones Destetados</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
              {destetes.reduce((sum, d) => sum + (d.cantidadDestetados || 0), 0)}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
              {destetes.length > 0 
                ? (destetes.reduce((sum, d) => sum + (d.pesoPromedioDestete || 0), 0) / destetes.length).toFixed(2)
                : '0.00'} kg
            </p>
          </div>
        </div>
      )}

      {/* Lista de Destetes */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflow: 'hidden'
      }}>
        {destetes.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            <Icon name="Baby" size={48} />
            <p>No hay destetes registrados</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Fecha Destete
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Parto
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Cantidad Destetados
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Peso Promedio (kg)
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Días Lactancia
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody>
              {destetes.map((destete) => {
                const parto = obtenerParto(destete.partoId);
                return (
                  <tr
                    key={destete.id}
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
                      {destete.fechaDestete ? new Date(destete.fechaDestete).toLocaleDateString('es-ES') : '-'}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      {parto ? `Parto #${parto.id} - ${new Date(parto.fechaInicio).toLocaleDateString('es-ES')}` : `Parto #${destete.partoId}`}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', color: '#10b981', fontWeight: '500' }}>
                      {destete.cantidadDestetados}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', fontWeight: '500' }}>
                      {destete.pesoPromedioDestete?.toFixed(2) || '0.00'}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center' }}>
                      {destete.diasLactancia || '-'}
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'center' }}>
                      <button
                        onClick={() => navigate(`/porcinos/destetes/${destete.id}`)}
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
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default DestetesListScreen;

