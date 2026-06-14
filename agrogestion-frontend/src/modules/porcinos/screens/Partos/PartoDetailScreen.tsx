import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { partosService } from '../../services/partosService';
import { desteteService } from '../../services/desteteService';
import { transferenciasService } from '../../services/transferenciasService';
import { partosService as partosServiceForTransfer } from '../../services/partosService';
import type { Parto, Destete, TransferenciaLechon } from '../../types';

const PartoDetailScreen: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [parto, setParto] = useState<Parto | null>(null);
  const [destete, setDestete] = useState<Destete | null>(null);
  const [transferencias, setTransferencias] = useState<TransferenciaLechon[]>([]);
  const [partosDisponibles, setPartosDisponibles] = useState<Parto[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModalDestete, setShowModalDestete] = useState(false);
  const [showModalTransferencia, setShowModalTransferencia] = useState(false);
  const [formDestete, setFormDestete] = useState({
    fechaDestete: new Date().toISOString().split('T')[0],
    cantidadDestetados: 0,
    pesoPromedioDestete: 0,
    observaciones: '',
  });
  const [formTransferencia, setFormTransferencia] = useState<Partial<TransferenciaLechon>>({
    partoDestinoId: 0,
    cantidad: 0,
    motivo: '',
    observaciones: '',
  });

  useEffect(() => {
    if (id) {
      cargarDatos();
    }
  }, [id]);

  const cargarDatos = async () => {
    setLoading(true);
    try {
      const [partoData, desteteData, transferenciasData, partosData] = await Promise.all([
        partosService.obtener(parseInt(id!)),
        desteteService.obtenerPorParto(parseInt(id!)).catch(() => null),
        transferenciasService.obtenerPorParto(parseInt(id!)).catch(() => []),
        partosService.listar().catch(() => []),
      ]);
      setParto(partoData);
      setDestete(desteteData);
      setTransferencias(transferenciasData);
      // Filtrar partos disponibles (excluir el actual y los que ya no tienen lechones disponibles)
      setPartosDisponibles(partosData.filter((p: Parto) => 
        p.id !== partoData.id && p.nacidosVivos > 0
      ));
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setLoading(false);
    }
  };

  const calcularDiasLactancia = () => {
    if (!parto || !destete) return null;
    const inicio = new Date(parto.fechaInicio);
    const desteteFecha = new Date(destete.fechaDestete);
    return Math.floor((desteteFecha.getTime() - inicio.getTime()) / (1000 * 60 * 60 * 24));
  };

  const handleRegistrarDestete = async () => {
    if (!formDestete.cantidadDestetados || formDestete.cantidadDestetados <= 0) {
      alert('La cantidad de destetados debe ser mayor a cero');
      return;
    }
    if (!formDestete.pesoPromedioDestete || formDestete.pesoPromedioDestete <= 0) {
      alert('El peso promedio debe ser mayor a cero');
      return;
    }
    try {
      await desteteService.crear(parseInt(id!), formDestete);
      setShowModalDestete(false);
      cargarDatos();
    } catch (error) {
      console.error('Error al registrar destete:', error);
      alert('Error al registrar destete');
    }
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando parto...</p>
      </div>
    );
  }

  if (!parto) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <p>Parto no encontrado</p>
        <button onClick={() => navigate('/porcinos/partos')}>Volver</button>
      </div>
    );
  }

  const diasLactancia = calcularDiasLactancia();

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/partos')}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#6b7280',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            marginBottom: '1rem',
            fontSize: '0.875rem'
          }}
        >
          ← Volver
        </button>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Baby" size={32} style={{ marginRight: '0.5rem' }} /> Detalle de Parto
        </h1>
      </div>

      {/* Información Principal */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '1.5rem', marginBottom: '2rem' }}>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Madre</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{parto.madreIdentificacion || `ID: ${parto.madreId}`}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Inicio</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(parto.fechaInicio).toLocaleDateString('es-ES')}</p>
          </div>
          {parto.fechaFin && (
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Fin</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(parto.fechaFin).toLocaleDateString('es-ES')}</p>
            </div>
          )}
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Nacidos Vivos</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>{parto.nacidosVivos}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Nacidos Muertos</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>{parto.nacidosMuertos}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Momias</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>{parto.momias}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Total Nacidos</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>{parto.totalNacidos}</p>
          </div>
          {parto.pesoPromedioNacimiento && (
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio Nacimiento</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{parto.pesoPromedioNacimiento} kg</p>
            </div>
          )}
        </div>

        {parto.observaciones && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#f9fafb',
            borderRadius: '0.375rem',
            marginTop: '1rem'
          }}>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
            <p style={{ fontSize: '0.875rem' }}>{parto.observaciones}</p>
          </div>
        )}
      </div>

      {/* Destete */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
            🍼 Información de Destete
          </h2>
          {!destete && (
            <button
              onClick={() => {
                setFormDestete({
                  ...formDestete,
                  cantidadDestetados: parto.nacidosVivos,
                });
                setShowModalDestete(true);
              }}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Destete
            </button>
          )}
        </div>

        {destete ? (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1.5rem' }}>
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Destete</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(destete.fechaDestete).toLocaleDateString('es-ES')}</p>
            </div>
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Destetados</p>
              <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>{destete.cantidadDestetados}</p>
            </div>
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio Destete</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{destete.pesoPromedioDestete} kg</p>
            </div>
            {diasLactancia !== null && (
              <div>
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días de Lactancia</p>
                <p style={{ fontSize: '1rem', fontWeight: '500' }}>{diasLactancia} días</p>
              </div>
            )}
            {destete.observaciones && (
              <div style={{ gridColumn: '1 / -1' }}>
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
                <p style={{ fontSize: '0.875rem' }}>{destete.observaciones}</p>
              </div>
            )}
          </div>
        ) : (
          <p style={{ color: '#6b7280' }}>No se ha registrado destete para este parto</p>
        )}
      </div>

      {/* Transferencias de Lechones */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem' }}>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937', marginBottom: '0.5rem' }}>
              <Icon name="RefreshCw" size={24} style={{ marginRight: '0.5rem' }} /> Transferencias de Lechones
            </h2>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', fontStyle: 'italic' }}>
              Transferencias relacionadas con este parto (origen o destino)
            </p>
          </div>
          {parto.nacidosVivos > 0 && partosDisponibles.length > 0 && (
            <button
              onClick={() => {
                setFormTransferencia({
                  partoDestinoId: 0,
                  cantidad: 0,
                  motivo: '',
                  observaciones: '',
                });
                setShowModalTransferencia(true);
              }}
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
              <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Transferencia
            </button>
          )}
        </div>

        {transferencias.length === 0 ? (
          <p style={{ color: '#6b7280' }}>No hay transferencias registradas para este parto</p>
        ) : (
          <div style={{ display: 'grid', gap: '1rem' }}>
            {transferencias.map((transferencia, index) => (
              <div
                key={index}
                style={{
                  padding: '1rem',
                  backgroundColor: '#f9fafb',
                  borderRadius: '0.375rem',
                  border: '1px solid #e5e7eb'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div>
                    <p style={{ fontSize: '0.875rem', fontWeight: '500', marginBottom: '0.5rem' }}>
                      {new Date(transferencia.fechaTransferencia).toLocaleDateString('es-ES')}
                    </p>
                    <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: '#6b7280' }}>
                      <span>De: <strong>Madre {transferencia.madreOrigenId}</strong></span>
                      <span>→</span>
                      <span>A: <strong>Madre {transferencia.madreDestinoId}</strong></span>
                      <span>Cantidad: <strong>{transferencia.cantidad}</strong></span>
                    </div>
                    {transferencia.motivo && (
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
                        Motivo: {transferencia.motivo}
                      </p>
                    )}
                    {transferencia.observaciones && (
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
                        {transferencia.observaciones}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        {transferencias.length > 0 && (
          <div style={{ marginTop: '1rem', paddingTop: '1rem', borderTop: '1px solid #e5e7eb' }}>
            <button
              onClick={() => navigate('/porcinos/transferencias')}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#f3f4f6',
                color: '#374151',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontSize: '0.875rem'
              }}
            >
              Ver todas las transferencias
            </button>
          </div>
        )}
      </div>

      {/* Modal Registrar Transferencia */}
      {showModalTransferencia && (
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
              <Icon name="RefreshCw" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Transferencia de Lechones
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Parto Destino *
                </label>
                <select
                  value={formTransferencia.partoDestinoId || ''}
                  onChange={(e) => setFormTransferencia({...formTransferencia, partoDestinoId: parseInt(e.target.value) || 0})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                >
                  <option value="">Seleccione un parto destino</option>
                  {partosDisponibles.map(p => (
                    <option key={p.id} value={p.id}>
                      Madre {p.madreIdentificacion || p.madreId} - {p.nacidosVivos} lechones disponibles
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Cantidad *
                </label>
                <input
                  type="number"
                  min="1"
                  max={parto.nacidosVivos}
                  value={formTransferencia.cantidad || ''}
                  onChange={(e) => setFormTransferencia({...formTransferencia, cantidad: parseInt(e.target.value) || 0})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
                  Máximo: {parto.nacidosVivos} lechones disponibles
                </p>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Motivo
                </label>
                <input
                  type="text"
                  value={formTransferencia.motivo || ''}
                  onChange={(e) => setFormTransferencia({...formTransferencia, motivo: e.target.value})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  placeholder="Ej: Madre con pocos lechones"
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formTransferencia.observaciones || ''}
                  onChange={(e) => setFormTransferencia({...formTransferencia, observaciones: e.target.value})}
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
                  onClick={() => setShowModalTransferencia(false)}
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
                  onClick={async () => {
                    if (!formTransferencia.partoDestinoId || !formTransferencia.cantidad) {
                      alert('Debe completar todos los campos obligatorios');
                      return;
                    }
                    try {
                      await transferenciasService.crear({
                        partoOrigenId: parto.id!,
                        madreOrigenId: parto.madreId,
                        partoDestinoId: formTransferencia.partoDestinoId,
                        madreDestinoId: partosDisponibles.find(p => p.id === formTransferencia.partoDestinoId)?.madreId || 0,
                        fechaTransferencia: new Date().toISOString().split('T')[0],
                        cantidad: formTransferencia.cantidad,
                        motivo: formTransferencia.motivo,
                        observaciones: formTransferencia.observaciones,
                      });
                      setShowModalTransferencia(false);
                      setFormTransferencia({
                        partoDestinoId: 0,
                        cantidad: 0,
                        motivo: '',
                        observaciones: '',
                      });
                      cargarDatos();
                    } catch (error) {
                      console.error('Error al registrar transferencia:', error);
                      alert('Error al registrar transferencia');
                    }
                  }}
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
                  Registrar Transferencia
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Registrar Destete */}
      {showModalDestete && (
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
              🍼 Registrar Destete
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha Destete *
                </label>
                <input
                  type="date"
                  value={formDestete.fechaDestete}
                  onChange={(e) => setFormDestete({...formDestete, fechaDestete: e.target.value})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Cantidad Destetados *
                </label>
                <input
                  type="number"
                  min="1"
                  max={parto.nacidosVivos}
                  value={formDestete.cantidadDestetados || ''}
                  onChange={(e) => setFormDestete({...formDestete, cantidadDestetados: parseInt(e.target.value) || 0})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>
                  Máximo: {parto.nacidosVivos} nacidos vivos
                </p>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Peso Promedio Destete (kg) *
                </label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={formDestete.pesoPromedioDestete || ''}
                  onChange={(e) => setFormDestete({...formDestete, pesoPromedioDestete: parseFloat(e.target.value) || 0})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formDestete.observaciones}
                  onChange={(e) => setFormDestete({...formDestete, observaciones: e.target.value})}
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
                  onClick={() => setShowModalDestete(false)}
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
                  onClick={handleRegistrarDestete}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '500'
                  }}
                >
                  Registrar Destete
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PartoDetailScreen;

