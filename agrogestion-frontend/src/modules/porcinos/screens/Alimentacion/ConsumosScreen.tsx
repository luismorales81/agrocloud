import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { consumoAlimentoService } from '../../services/consumoAlimentoService';
import type { ConsumoAlimento } from '../../types';
import BarraSubnavegacionAlimentacionPorcinos from '../../components/BarraSubnavegacionAlimentacionPorcinos';

const ConsumosScreen: React.FC = () => {
  const navigate = useNavigate();
  const [consumos, setConsumos] = useState<ConsumoAlimento[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [filtros, setFiltros] = useState<{ fechaDesde?: string; fechaHasta?: string }>({});
  const [formData, setFormData] = useState<Partial<ConsumoAlimento>>({
    categoria: 'MADRES',
    fecha: new Date().toISOString().split('T')[0],
    cantidadKg: 0,
    tipoAlimento: 'BALANCEADO',
    observaciones: '',
  });

  useEffect(() => {
    cargarConsumos();
  }, [filtros]);

  const cargarConsumos = async () => {
    setLoading(true);
    try {
      const data = await consumoAlimentoService.listar(filtros.fechaDesde, filtros.fechaHasta);
      setConsumos(data);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await consumoAlimentoService.crear(formData);
      setShowModal(false);
      setFormData({
        categoria: 'MADRES',
        fecha: new Date().toISOString().split('T')[0],
        cantidadKg: 0,
        tipoAlimento: 'BALANCEADO',
        observaciones: '',
      });
      cargarConsumos();
    } catch (error) {
      console.error('Error al registrar consumo:', error);
      alert('Error al registrar consumo');
    }
  };

  const obtenerNombreCategoria = (categoria: string) => {
    const nombres: Record<string, string> = {
      MADRES: 'Madres',
      PADRILLOS: 'Padrillos',
      RECRIA: 'Recría',
      ENGORDE: 'Engorde',
      LECHONES: 'Lechones',
    };
    return nombres[categoria] || categoria;
  };

  const obtenerNombreTipoAlimento = (tipo: string) => {
    const nombres: Record<string, string> = {
      BALANCEADO: 'Balanceado',
      MAIZ: 'Maíz',
      GRANO_PROPIO: 'Grano Propio',
    };
    return nombres[tipo] || tipo;
  };

  const totalConsumo = consumos.reduce((sum, c) => sum + (c.cantidadKg || 0), 0);

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando consumos...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <BarraSubnavegacionAlimentacionPorcinos />
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '0.75rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Wheat" size={32} style={{ marginRight: '0.5rem' }} /> Historial de Consumos
        </h1>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.5rem' }}>
          <button
            type="button"
            onClick={() => navigate('/porcinos/alimentacion/calendario')}
            style={{
              padding: '0.75rem 1.5rem',
              backgroundColor: '#ffffff',
              color: '#1f2937',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontWeight: '500'
            }}
          >
            <Icon name="Calendar" size={18} style={{ marginRight: '0.5rem' }} /> Calendario diario
          </button>
          <button
            onClick={() => setShowModal(true)}
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
            <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Consumo
          </button>
        </div>
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
      {consumos.length > 0 && (
        <div style={{
          backgroundColor: 'white',
          padding: '1.5rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          marginBottom: '1.5rem'
        }}>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.5rem' }}>Total Consumido</p>
          <p style={{ fontSize: '2rem', fontWeight: 'bold', color: '#3b82f6' }}>{totalConsumo.toFixed(2)} kg</p>
        </div>
      )}

      {/* Lista de Consumos */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflow: 'hidden'
      }}>
        {consumos.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            <Icon name="Wheat" size={48} />
            <p>No hay consumos registrados</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Fecha
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Categoría
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Tipo Alimento
                </th>
                <th style={{ padding: '1rem', textAlign: 'right', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Cantidad (kg)
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Cultivo Relacionado
                </th>
              </tr>
            </thead>
            <tbody>
              {consumos.map((consumo) => (
                <tr
                  key={consumo.id}
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
                    {new Date(consumo.fecha).toLocaleDateString('es-ES')}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {obtenerNombreCategoria(consumo.categoria)}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                    {obtenerNombreTipoAlimento(consumo.tipoAlimento)}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'right', fontWeight: '500' }}>
                    {consumo.cantidadKg.toFixed(2)}
                  </td>
                  <td style={{ padding: '1rem', fontSize: '0.875rem', color: '#6b7280' }}>
                    {consumo.cultivoRelacionadoNombre || '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal Registrar Consumo */}
      {showModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%'
          }}>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Consumo
            </h2>
            <form onSubmit={handleSubmit}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Categoría *
                  </label>
                  <select
                    value={formData.categoria}
                    onChange={(e) => setFormData({...formData, categoria: e.target.value as any})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  >
                    <option value="MADRES">Madres</option>
                    <option value="PADRILLOS">Padrillos</option>
                    <option value="RECRIA">Recría</option>
                    <option value="ENGORDE">Engorde</option>
                    <option value="LECHONES">Lechones</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Fecha *
                  </label>
                  <input
                    type="date"
                    value={formData.fecha}
                    onChange={(e) => setFormData({...formData, fecha: e.target.value})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Tipo de Alimento *
                  </label>
                  <select
                    value={formData.tipoAlimento}
                    onChange={(e) => setFormData({...formData, tipoAlimento: e.target.value as any})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  >
                    <option value="BALANCEADO">Balanceado</option>
                    <option value="MAIZ">Maíz</option>
                    <option value="GRANO_PROPIO">Grano Propio</option>
                  </select>
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Cantidad (kg) *
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    min="0"
                    value={formData.cantidadKg || ''}
                    onChange={(e) => setFormData({...formData, cantidadKg: parseFloat(e.target.value) || 0})}
                    required
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Observaciones
                  </label>
                  <textarea
                    value={formData.observaciones || ''}
                    onChange={(e) => setFormData({...formData, observaciones: e.target.value})}
                    rows={3}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem',
                      fontFamily: 'inherit'
                    }}
                  />
                </div>
                <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '1rem' }}>
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    style={{
                      padding: '0.75rem 1.5rem',
                      backgroundColor: '#6b7280',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '500'
                    }}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
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
                    Registrar Consumo
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ConsumosScreen;







