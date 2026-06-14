import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { partosService } from '../../services/partosService';
import { madresService } from '../../services/madresService';
import { Autocomplete, AutocompleteOption } from '../../../../components/ui/Autocomplete';
import type { Parto, Madre } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const PartosListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [partos, setPartos] = useState<Parto[]>([]);
  const [madres, setMadres] = useState<Madre[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState<{
    fechaDesde?: string;
    fechaHasta?: string;
    madreId?: number;
  }>({});

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
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
      setLoading(false);
    }
  };

  useEffect(() => {
    if (filtros.fechaDesde || filtros.fechaHasta || filtros.madreId) {
      cargarDatos();
    }
  }, [filtros]);

  const obtenerNombreMadre = (madreId: number) => {
    const madre = madres.find(m => m.id === madreId);
    return madre ? madre.identificacion : `ID: ${madreId}`;
  };

  // Convertir madres a opciones para el Autocomplete (incluyendo "Todas")
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

  // Manejar cambio de filtro de madre
  const handleMadreFilterChange = (value: string | number | undefined, option?: AutocompleteOption<Madre>) => {
    if (value === 'todas' || value === undefined) {
      setFiltros({ ...filtros, madreId: undefined });
    } else {
      setFiltros({ ...filtros, madreId: value as number });
    }
  };

  // Obtener el valor del filtro para el Autocomplete
  const valorFiltroMadre = filtros.madreId !== undefined ? filtros.madreId : 'todas';

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando partos...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Partos
        </h1>
        <button
          onClick={() => navigate('/porcinos/partos/nuevo')}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Parto
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
    </div>
  );
};

export default PartosListScreen;
