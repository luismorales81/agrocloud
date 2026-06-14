import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { transferenciasService } from '../../services/transferenciasService';
import { partosService } from '../../services/partosService';
import { madresService } from '../../services/madresService';
import type { TransferenciaLechon, Parto, Madre } from '../../types';
import { Icon, SemanticIcon } from '../../../../components/icons';

const TransferenciasScreen: React.FC = () => {
  const navigate = useNavigate();
  const [transferencias, setTransferencias] = useState<TransferenciaLechon[]>([]);
  const [partos, setPartos] = useState<Parto[]>([]);
  const [madres, setMadres] = useState<Madre[]>([]);
  const [loading, setLoading] = useState(true);
  const [filtros, setFiltros] = useState<{
    fechaDesde?: string;
    fechaHasta?: string;
    madreOrigenId?: number;
    madreDestinoId?: number;
  }>({});

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [transferenciasData, partosData, madresData] = await Promise.all([
        transferenciasService.listar(),
        partosService.listar({}),
        madresService.listar(),
      ]);
      setTransferencias(transferenciasData);
      setPartos(partosData);
      setMadres(madresData);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const obtenerParto = (partoId: number) => {
    return partos.find(p => p.id === partoId);
  };

  const obtenerMadre = (madreId: number) => {
    return madres.find(m => m.id === madreId);
  };

  const obtenerNombreMadre = (madreId: number) => {
    const madre = obtenerMadre(madreId);
    return madre ? madre.identificacion : `ID: ${madreId}`;
  };

  // Filtrar transferencias
  const transferenciasFiltradas = transferencias.filter(t => {
    if (filtros.fechaDesde) {
      const fechaTransferencia = new Date(t.fechaTransferencia);
      const fechaDesde = new Date(filtros.fechaDesde);
      if (fechaTransferencia < fechaDesde) return false;
    }
    if (filtros.fechaHasta) {
      const fechaTransferencia = new Date(t.fechaTransferencia);
      const fechaHasta = new Date(filtros.fechaHasta);
      fechaHasta.setHours(23, 59, 59, 999);
      if (fechaTransferencia > fechaHasta) return false;
    }
    if (filtros.madreOrigenId && t.madreOrigenId !== filtros.madreOrigenId) {
      return false;
    }
    if (filtros.madreDestinoId && t.madreDestinoId !== filtros.madreDestinoId) {
      return false;
    }
    return true;
  });

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando transferencias...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="RefreshCw" size={32} style={{ marginRight: '0.5rem' }} /> Transferencias de Lechones
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
                padding: '0.75rem',
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
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Madre Origen
            </label>
            <select
              value={filtros.madreOrigenId || ''}
              onChange={(e) => setFiltros({...filtros, madreOrigenId: e.target.value ? parseInt(e.target.value) : undefined})}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todas</option>
              {madres.map(madre => (
                <option key={madre.id} value={madre.id}>
                  {madre.identificacion}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Madre Destino
            </label>
            <select
              value={filtros.madreDestinoId || ''}
              onChange={(e) => setFiltros({...filtros, madreDestinoId: e.target.value ? parseInt(e.target.value) : undefined})}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todas</option>
              {madres.map(madre => (
                <option key={madre.id} value={madre.id}>
                  {madre.identificacion}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div style={{ marginTop: '1rem' }}>
          <button
            onClick={() => setFiltros({})}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#6b7280',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontSize: '0.875rem'
            }}
          >
            Limpiar Filtros
          </button>
        </div>
      </div>

      {/* Lista de Transferencias */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflowX: 'auto',
        overflowY: 'visible'
      }}>
        {transferenciasFiltradas.length === 0 ? (
          <div style={{ padding: '3rem', textAlign: 'center', color: '#6b7280' }}>
            <Icon name="RefreshCw" size={48} />
            <p>No hay transferencias registradas</p>
          </div>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', minWidth: '800px' }}>
            <thead>
              <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Fecha
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Madre Origen
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Parto Origen
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Madre Destino
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Parto Destino
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Cantidad
                </th>
                <th style={{ padding: '1rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Motivo
                </th>
                <th style={{ padding: '1rem', textAlign: 'center', fontSize: '0.875rem', fontWeight: '600', color: '#1f2937' }}>
                  Acción
                </th>
              </tr>
            </thead>
            <tbody>
              {transferenciasFiltradas.map((transferencia) => {
                const partoOrigen = obtenerParto(transferencia.partoOrigenId);
                const partoDestino = obtenerParto(transferencia.partoDestinoId);
                return (
                  <tr
                    key={transferencia.id}
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
                      {transferencia.fechaTransferencia ? new Date(transferencia.fechaTransferencia).toLocaleDateString('es-ES') : '-'}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      <strong>{obtenerNombreMadre(transferencia.madreOrigenId)}</strong>
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      {partoOrigen ? (
                        <span
                          onClick={() => navigate(`/porcinos/partos/${partoOrigen.id}`)}
                          style={{
                            color: '#3b82f6',
                            cursor: 'pointer',
                            textDecoration: 'underline'
                          }}
                        >
                          Parto #{partoOrigen.id}
                        </span>
                      ) : (
                        `Parto #${transferencia.partoOrigenId}`
                      )}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      <strong>{obtenerNombreMadre(transferencia.madreDestinoId)}</strong>
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      {partoDestino ? (
                        <span
                          onClick={() => navigate(`/porcinos/partos/${partoDestino.id}`)}
                          style={{
                            color: '#3b82f6',
                            cursor: 'pointer',
                            textDecoration: 'underline'
                          }}
                        >
                          Parto #{partoDestino.id}
                        </span>
                      ) : (
                        `Parto #${transferencia.partoDestinoId}`
                      )}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem', textAlign: 'center', color: '#10b981', fontWeight: '500' }}>
                      {transferencia.cantidad}
                    </td>
                    <td style={{ padding: '1rem', fontSize: '0.875rem' }}>
                      {transferencia.motivo || '-'}
                    </td>
                    <td style={{ padding: '1rem', textAlign: 'center' }}>
                      <button
                        onClick={() => {
                          // Navegar al detalle del parto origen o destino
                          if (partoOrigen) {
                            navigate(`/porcinos/partos/${partoOrigen.id}`);
                          } else if (partoDestino) {
                            navigate(`/porcinos/partos/${partoDestino.id}`);
                          }
                        }}
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
                        <Icon name="Eye" size={16} style={{ marginRight: '0.25rem' }} /> Ver Detalle
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>

      {/* Resumen */}
      {transferenciasFiltradas.length > 0 && (
        <div style={{
          marginTop: '1.5rem',
          padding: '1rem',
          backgroundColor: '#f0f9ff',
          borderRadius: '0.5rem',
          border: '1px solid #bae6fd'
        }}>
          <p style={{ fontSize: '0.875rem', color: '#1e40af', fontWeight: '500' }}>
            Total de transferencias: <strong>{transferenciasFiltradas.length}</strong> | 
            Total de lechones transferidos: <strong>{transferenciasFiltradas.reduce((sum, t) => sum + (t.cantidad || 0), 0)}</strong>
          </p>
        </div>
      )}
    </div>
  );
};

export default TransferenciasScreen;
