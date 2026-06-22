import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { recriaService } from '../../services/recriaService';
import type { Recria } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';
import FiltroDelPeriodoActivo from '../../../../components/FiltroDelPeriodoActivo';
import { useCampana } from '../../../../contexts/CampanaContext';

const RecriaListScreen: React.FC = () => {
  const navigate = useNavigate();
  const { campanaActiva } = useCampana();
  const [recrias, setRecrias] = useState<Recria[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroActivas, setFiltroActivas] = useState(true);
  const [filtroDelPeriodo, setFiltroDelPeriodo] = useState(false);

  useEffect(() => {
    setFiltroDelPeriodo(!filtroActivas);
  }, [filtroActivas]);

  useEffect(() => {
    cargarRecrias();
  }, [filtroActivas, filtroDelPeriodo, campanaActiva?.id]);

  const cargarRecrias = async () => {
    setLoading(true);
    try {
      const data = await recriaService.listar({
        activas: filtroActivas,
        delPeriodoActivo: filtroDelPeriodo,
      });
      setRecrias(data);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const calcularDiasEnRecria = (fechaIngreso: string) => {
    const hoy = new Date();
    const ingreso = new Date(fechaIngreso);
    const diff = Math.floor((hoy.getTime() - ingreso.getTime()) / (1000 * 60 * 60 * 24));
    return diff;
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando recrías...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', margin: 0 }}>
            <Icon name="Circle" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Recría
          </h1>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem', marginBottom: 0 }}>
            Unidad operativa por lote (batch). El período de gestión activo agrupa ventas y consumos al crear cada ingreso.
          </p>
        </div>
        <button
          onClick={() => navigate('/porcinos/recria/ingreso')}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Ingreso a Recría
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
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <label style={{ fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
            Mostrar:
          </label>
          <button
            onClick={() => setFiltroActivas(true)}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: filtroActivas ? '#3b82f6' : '#e5e7eb',
              color: filtroActivas ? 'white' : '#1f2937',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontSize: '0.875rem',
              fontWeight: '500'
            }}
          >
            Activas
          </button>
          <button
            onClick={() => setFiltroActivas(false)}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: !filtroActivas ? '#3b82f6' : '#e5e7eb',
              color: !filtroActivas ? 'white' : '#1f2937',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontSize: '0.875rem',
              fontWeight: '500'
            }}
          >
            Histórico
          </button>
          <FiltroDelPeriodoActivo
            activo={filtroDelPeriodo}
            onChange={setFiltroDelPeriodo}
          />
        </div>
      </div>

      {/* Estadísticas */}
      {recrias.length > 0 && (
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
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Lotes</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>{recrias.length}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Animales</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
              {recrias.reduce((sum, r) => sum + (r.cantidadAnimales || 0), 0)}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
              {recrias.length > 0
                ? (recrias.reduce((sum, r) => sum + (r.pesoPromedio || 0), 0) / recrias.length).toFixed(1)
                : '0'
              } kg
            </p>
          </div>
        </div>
      )}

      {/* Lista de Recrías */}
      {recrias.length === 0 ? (
        <div style={{
          backgroundColor: 'white',
          padding: '3rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          textAlign: 'center',
          color: '#6b7280'
        }}>
          <Icon name="Circle" size={48} />
          <p>No hay recrías registradas</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {recrias.map((recria) => {
            const diasEnRecria = calcularDiasEnRecria(recria.fechaIngreso);
            const tieneMuertes = recria.historialMuertes && recria.historialMuertes.length > 0;
            const totalMuertes = recria.historialMuertes
              ? recria.historialMuertes.reduce((sum, m) => sum + (m.cantidad || 0), 0)
              : 0;

            return (
              <div
                key={recria.id}
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
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem', flexWrap: 'wrap' }}>
                      <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                        Lote: {recria.loteNombre || `ID: ${recria.loteId}`}
                      </h3>

                      {/* Origen de la recría: se muestra siempre de forma explícita */}
                      <span
                        style={{
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '0.25rem',
                          padding: '0.25rem 0.75rem',
                          borderRadius: '9999px',
                          fontSize: '0.75rem',
                          fontWeight: 500,
                          backgroundColor: recria.origen === 'DESTETE' ? '#eff6ff' : '#fef3c7',
                          color: recria.origen === 'DESTETE' ? '#1d4ed8' : '#92400e',
                          border: `1px solid ${recria.origen === 'DESTETE' ? '#bfdbfe' : '#fed7aa'}`
                        }}
                      >
                        <Icon
                          name={recria.origen === 'DESTETE' ? 'Baby' : 'ShoppingCart'}
                          size={14}
                        />
                        {recria.origen === 'DESTETE'
                          ? 'Origen: Destete (lote interno)'
                          : 'Origen: Ingreso externo / compra'}
                      </span>

                      {!recria.fechaSalida && (
                        <span style={{
                          display: 'inline-block',
                          padding: '0.25rem 0.75rem',
                          borderRadius: '9999px',
                          fontSize: '0.75rem',
                          fontWeight: '500',
                          backgroundColor: '#10b98120',
                          color: '#10b981'
                        }}>
                          Activa
                        </span>
                      )}
                    </div>
                    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Animales</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{recria.cantidadAnimales}</p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{recria.pesoPromedio} kg</p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Sexo</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{recria.sexo}</p>
                      </div>
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Ingreso</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                          {new Date(recria.fechaIngreso).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                      {recria.fechaSalida && (
                        <div>
                          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Salida</p>
                          <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                            {new Date(recria.fechaSalida).toLocaleDateString('es-ES')}
                          </p>
                        </div>
                      )}
                      {recria.destino && (
                        <div>
                          <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Destino</p>
                          <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{recria.destino}</p>
                        </div>
                      )}
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días en Recría</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{diasEnRecria} días</p>
                      </div>
                    </div>
                    {tieneMuertes && (
                      <div style={{
                        padding: '0.75rem',
                        backgroundColor: '#fee2e2',
                        borderRadius: '0.375rem',
                        marginTop: '0.5rem'
                      }}>
                        <p style={{ fontSize: '0.75rem', color: '#991b1b', fontWeight: '500', display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                          <SemanticIcon semanticName="warning" size={16} />
                          Total muertes registradas: {totalMuertes}
                        </p>
                      </div>
                    )}
                  </div>
                  <button
                    onClick={() => navigate(`/porcinos/recria/${recria.id}`)}
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

export default RecriaListScreen;
