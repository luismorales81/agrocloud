import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate } from 'react-router-dom';
import { ventaPorcinoService } from '../../services/ventaPorcinoService';
import { recriaService } from '../../services/recriaService';
import type { VentaPorcino, Recria } from '../../types';

const FaenaListScreen: React.FC = () => {
  const navigate = useNavigate();
  const [faenas, setFaenas] = useState<VentaPorcino[]>([]);
  const [recrias, setRecrias] = useState<Recria[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtroRecria, setFiltroRecria] = useState<number | null>(null);

  useEffect(() => {
    cargarDatos();
  }, [filtroRecria]);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [ventasData, recriasData] = await Promise.all([
        ventaPorcinoService.listar(),
        recriaService.listar({ activas: false })
      ]);
      // Filtrar solo las ventas de tipo FAENA
      const faenasData = Array.isArray(ventasData) 
        ? ventasData.filter((v: VentaPorcino) => v.tipo === 'FAENA')
        : [];
      setFaenas(faenasData);
      setRecrias(recriasData);
    } catch (error) {
      console.error('Error al cargar datos:', error);
    } finally {
      setLoading(false);
    }
  };

  const faenasFiltradas = filtroRecria
    ? faenas.filter(f => f.recriaId === filtroRecria)
    : faenas;

  const obtenerNombreRecria = (recriaId: number) => {
    const recria = recrias.find(r => r.id === recriaId);
    return recria ? `Lote: ${recria.loteNombre || recria.loteId}` : `ID: ${recriaId}`;
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando faenas...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Scissors" size={32} style={{ marginRight: '0.5rem' }} /> Gestión de Faena
        </h1>
      </div>

      {/* Filtros */}
      <div style={{
        backgroundColor: 'white',
        padding: '1.5rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
          <label style={{ fontSize: '0.875rem', fontWeight: '500', color: '#1f2937' }}>
            Filtrar por Recría:
          </label>
          <select
            value={filtroRecria || ''}
            onChange={(e) => setFiltroRecria(e.target.value ? parseInt(e.target.value) : null)}
            style={{
              padding: '0.5rem 1rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              fontSize: '0.875rem'
            }}
          >
            <option value="">Todas</option>
            {recrias.map(r => (
              <option key={r.id} value={r.id}>
                {r.loteNombre || `Lote ${r.loteId}`}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Estadísticas */}
      {faenasFiltradas.length > 0 && (
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
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Faenas</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#1f2937' }}>{faenasFiltradas.length}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Ingresos Totales</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
              ${faenasFiltradas.reduce((sum, f) => sum + (f.ingresoTotal || 0), 0).toLocaleString('es-AR')}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Total Enviado</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>
              {faenasFiltradas.reduce((sum, f) => sum + (f.pesoEnvio ?? 0), 0).toFixed(2)} kg
            </p>
          </div>
        </div>
      )}

      {/* Lista de Faenas */}
      {faenasFiltradas.length === 0 ? (
        <div style={{
          backgroundColor: 'white',
          padding: '3rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          textAlign: 'center',
          color: '#6b7280'
        }}>
          <Icon name="Scissors" size={48} />
          <p>No hay faenas registradas</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gap: '1rem' }}>
          {faenasFiltradas.map((faena) => (
            <div
              key={faena.id}
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
                  <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '1rem' }}>
                    {faena.recriaId != null ? obtenerNombreRecria(faena.recriaId) : '—'}
                  </h3>
                  <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Envío</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                        {faena.fechaEnvio ? new Date(faena.fechaEnvio).toLocaleDateString('es-ES') : '-'}
                      </p>
                    </div>
                    {faena.fechaFaena && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Faena</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>
                          {new Date(faena.fechaFaena).toLocaleDateString('es-ES')}
                        </p>
                      </div>
                    )}
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Envío</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{faena.pesoEnvio || '-'} kg</p>
                    </div>
                    {faena.pesoFaena && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Faena</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{faena.pesoFaena} kg</p>
                      </div>
                    )}
                    {faena.rendimiento && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Rendimiento</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{faena.rendimiento.toFixed(2)}%</p>
                      </div>
                    )}
                    {faena.precioKg && (
                      <div>
                        <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Precio/kg</p>
                        <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>${faena.precioKg.toLocaleString('es-AR')}</p>
                      </div>
                    )}
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Ingreso Total</p>
                      <p style={{ fontSize: '1rem', fontWeight: 'bold', color: '#10b981' }}>
                        ${(faena.ingresoTotal || 0).toLocaleString('es-AR')}
                      </p>
                    </div>
                    <div>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Animales</p>
                      <p style={{ fontSize: '0.875rem', fontWeight: '500' }}>{faena.cantidad}</p>
                    </div>
                  </div>
                  {faena.observaciones && (
                    <div style={{
                      padding: '1rem',
                      backgroundColor: '#f9fafb',
                      borderRadius: '0.375rem',
                      marginTop: '1rem'
                    }}>
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
                      <p style={{ fontSize: '0.875rem' }}>{faena.observaciones}</p>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FaenaListScreen;







