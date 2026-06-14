import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { useParams, useNavigate } from 'react-router-dom';
import { recriaService } from '../../services/recriaService';
import { pesosService } from '../../services/pesosService';
import { movimientosService } from '../../services/movimientosService';
import type { MuerteRecria, CausaMuerte, DestinoRecria, Recria, RegistroPeso, MovimientoEtapa, EtapaRecria } from '../../types';

const RecriaDetailScreen: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [recria, setRecria] = useState<Recria | null>(null);
  const [registrosPeso, setRegistrosPeso] = useState<RegistroPeso[]>([]);
  const [movimientos, setMovimientos] = useState<MovimientoEtapa[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModalMuerte, setShowModalMuerte] = useState(false);
  const [showModalEgreso, setShowModalEgreso] = useState(false);
  const [showModalPeso, setShowModalPeso] = useState(false);
  const [showModalMovimiento, setShowModalMovimiento] = useState(false);
  const [formMuerte, setFormMuerte] = useState<Partial<MuerteRecria>>({
    fecha: new Date().toISOString().split('T')[0],
    causa: 'APLASTAMIENTO',
    cantidad: 0,
    observaciones: '',
  });
  const [formEgreso, setFormEgreso] = useState({
    fechaSalida: new Date().toISOString().split('T')[0],
    destino: 'VENTA' as DestinoRecria,
    observaciones: '',
  });
  const [formPeso, setFormPeso] = useState<Partial<RegistroPeso>>({
    fechaPesaje: new Date().toISOString().split('T')[0],
    pesoPromedio: 0,
    cantidadAnimales: 0,
    metodo: 'BALANZA',
    observaciones: '',
  });
  const [guardandoPeso, setGuardandoPeso] = useState(false);
  const [errorPeso, setErrorPeso] = useState<string | null>(null);
  const [formMovimiento, setFormMovimiento] = useState<Partial<MovimientoEtapa>>({
    etapaDestino: 'F2' as EtapaRecria,
    cantidadAnimales: 0,
    pesoPromedio: 0,
    observaciones: '',
  });

  useEffect(() => {
    if (id) {
      cargarRecria();
    }
  }, [id]);

  const cargarRecria = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [data, pesos, movimientosData] = await Promise.all([
        recriaService.obtener(parseInt(id)),
        pesosService.obtenerPorRecria(parseInt(id)).catch(() => []),
        movimientosService.obtenerPorRecria(parseInt(id)).catch(() => []),
      ]);
      setRecria(data);
      setRegistrosPeso(pesos);
      setMovimientos(movimientosData);
      if (data.cantidadAnimales) {
        setFormPeso(prev => ({ ...prev, cantidadAnimales: data.cantidadAnimales }));
      }
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

  const handleRegistrarMuerte = async () => {
    if (!formMuerte.cantidad || formMuerte.cantidad <= 0) {
      alert('La cantidad debe ser mayor a cero');
      return;
    }
    if (!recria) return;
    const totalMuertesActuales = recria.historialMuertes
      ? recria.historialMuertes.reduce((sum, m) => sum + (m.cantidad || 0), 0)
      : 0;
    const disponibles = recria.cantidadAnimales - totalMuertesActuales;
    if (formMuerte.cantidad > disponibles) {
      alert(`La cantidad de muertes no puede superar los animales disponibles (${disponibles}). Disponibles = cantidad en lote − muertes ya registradas.`);
      return;
    }
    try {
      await recriaService.registrarMuerte(parseInt(id!), formMuerte as MuerteRecria);
      setShowModalMuerte(false);
      setFormMuerte({
        fecha: new Date().toISOString().split('T')[0],
        causa: 'APLASTAMIENTO',
        cantidad: 0,
        observaciones: '',
      });
      cargarRecria();
    } catch (error) {
      console.error('Error al registrar muerte:', error);
      alert('Error al registrar muerte');
    }
  };

  const handleRegistrarEgreso = async () => {
    if (!formEgreso.fechaSalida) {
      alert('Debe seleccionar una fecha de salida');
      return;
    }
    try {
      await recriaService.registrarEgreso(
        parseInt(id!),
        formEgreso.fechaSalida,
        formEgreso.destino,
        formEgreso.observaciones
      );
      setShowModalEgreso(false);
      setFormEgreso({
        fechaSalida: new Date().toISOString().split('T')[0],
        destino: 'VENTA',
        observaciones: '',
      });
      cargarRecria();
    } catch (error) {
      console.error('Error al registrar egreso:', error);
      alert('Error al registrar egreso');
    }
  };

  const handleRegistrarPeso = async () => {
    if (!formPeso.pesoPromedio || formPeso.pesoPromedio <= 0) {
      setErrorPeso('El peso promedio debe ser mayor a cero');
      return;
    }
    if (!formPeso.fechaPesaje) {
      setErrorPeso('Debe indicar la fecha de pesada');
      return;
    }
    const fechaPesada = new Date(formPeso.fechaPesaje);
    if (fechaPesada > new Date()) {
      setErrorPeso('La fecha de pesada no puede ser futura');
      return;
    }
    setErrorPeso(null);
    setGuardandoPeso(true);
    try {
      await pesosService.registrar(parseInt(id!), {
        fechaPesaje: formPeso.fechaPesaje,
        pesoPromedio: formPeso.pesoPromedio,
        cantidadAnimales: formPeso.cantidadAnimales ?? recria?.cantidadAnimales ?? 0,
        metodo: formPeso.metodo,
        observaciones: formPeso.observaciones,
      });
      setShowModalPeso(false);
      setFormPeso({
        fechaPesaje: new Date().toISOString().split('T')[0],
        pesoPromedio: 0,
        cantidadAnimales: recria?.cantidadAnimales ?? 0,
        metodo: 'BALANZA',
        observaciones: '',
      });
      cargarRecria();
    } catch (error: unknown) {
      console.error('Error al registrar pesada:', error);
      const msg = (error as { response?: { data?: { message?: string }; status?: number } })?.response?.data?.message;
      setErrorPeso(msg || 'Error al registrar la pesada');
    } finally {
      setGuardandoPeso(false);
    }
  };

  const obtenerNombreCausa = (causa: string) => {
    const nombres: Record<string, string> = {
      APLASTAMIENTO: 'Aplastamiento',
      DIARREA: 'Diarrea',
      MALFORMACION: 'Malformación',
      DEBILIDAD: 'Debilidad',
      ENFERMEDAD: 'Enfermedad',
      ACCIDENTE: 'Accidente',
      OTRA: 'Otra',
    };
    return nombres[causa] || causa;
  };

  const obtenerNombreDestino = (destino: string) => {
    const nombres: Record<string, string> = {
      VENTA: 'Venta',
      FUTURA_MADRE: 'Futura Madre',
      ENGORDE: 'Engorde',
    };
    return nombres[destino] || destino;
  };

  if (loading) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando recría...</p>
      </div>
    );
  }

  if (!recria) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <p>Recría no encontrada</p>
        <button onClick={() => navigate('/porcinos/recria')}>Volver</button>
      </div>
    );
  }

  const diasEnRecria = calcularDiasEnRecria(recria.fechaIngreso);
  const totalMuertes = recria.historialMuertes
    ? recria.historialMuertes.reduce((sum, m) => sum + (m.cantidad || 0), 0)
    : 0;
  const animalesRestantes = recria.cantidadAnimales - totalMuertes;

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ marginBottom: '2rem' }}>
        <button
          onClick={() => navigate('/porcinos/recria')}
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
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <Icon name="Circle" size={32} /> Detalle de Recría
          {/* Origen visible también en el detalle */}
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
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Lote</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>
              {/* Para lotes provenientes de destete mostramos Madre + fecha, para los externos el nombre del lote */}
              {recria.origen === 'DESTETE' && recria.madreNombre && recria.fechaIngreso
                ? `${recria.madreNombre} - ${new Date(recria.fechaIngreso).toLocaleDateString('es-ES')}`
                : recria.loteNombre || `ID: ${recria.loteId}`}
            </p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Cantidad Animales</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#3b82f6' }}>{recria.cantidadAnimales}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Animales Restantes</p>
            <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>{animalesRestantes}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Peso Promedio</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{recria.pesoPromedio} kg</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Sexo</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{recria.sexo === 'MACHO' ? 'Macho' : 'Hembra'}</p>
          </div>
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Ingreso</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(recria.fechaIngreso).toLocaleDateString('es-ES')}</p>
          </div>
          {recria.fechaSalida && (
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Fecha Salida</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{new Date(recria.fechaSalida).toLocaleDateString('es-ES')}</p>
            </div>
          )}
          {recria.destino && (
            <div>
              <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Destino</p>
              <p style={{ fontSize: '1rem', fontWeight: '500' }}>{obtenerNombreDestino(recria.destino)}</p>
            </div>
          )}
          <div>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.25rem' }}>Días en Recría</p>
            <p style={{ fontSize: '1rem', fontWeight: '500' }}>{diasEnRecria} días</p>
          </div>
        </div>

        {recria.observaciones && (
          <div style={{
            padding: '1rem',
            backgroundColor: '#f9fafb',
            borderRadius: '0.375rem',
            marginTop: '1rem'
          }}>
            <p style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Observaciones</p>
            <p style={{ fontSize: '0.875rem' }}>{recria.observaciones}</p>
          </div>
        )}

        {/* Acciones */}
        {!recria.fechaSalida && (
          <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem', flexWrap: 'wrap' }}>
            <button
              onClick={() => { setShowModalPeso(true); setErrorPeso(null); setFormPeso(prev => ({ ...prev, cantidadAnimales: recria.cantidadAnimales, pesoPromedio: recria.pesoPromedio || 0 })); }}
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
              <Icon name="Scale" size={18} style={{ marginRight: '0.5rem' }} /> Registrar pesada
            </button>
            <button
              onClick={() => setShowModalMuerte(true)}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: '#ef4444',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
                fontWeight: '500'
              }}
            >
              <SemanticIcon semanticName="warning" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Muerte
            </button>
            <button
              onClick={() => setShowModalEgreso(true)}
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
              <SemanticIcon semanticName="success" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Egreso
            </button>
          </div>
        )}
      </div>

      {/* Historial de pesadas */}
      <div style={{
        backgroundColor: 'white',
        padding: '2rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        marginBottom: '1.5rem'
      }}>
        <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem', color: '#1f2937', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <Icon name="Scale" size={24} /> Historial de pesadas
        </h2>
        {registrosPeso.length === 0 ? (
          <p style={{ fontSize: '0.875rem', color: '#6b7280' }}>No hay pesadas registradas. Use «Registrar pesada» para cargar el peso promedio del lote.</p>
        ) : (
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.875rem' }}>
              <thead>
                <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Fecha</th>
                  <th style={{ padding: '0.75rem', textAlign: 'right' }}>Peso prom. (kg)</th>
                  <th style={{ padding: '0.75rem', textAlign: 'center' }}>Cantidad</th>
                  <th style={{ padding: '0.75rem', textAlign: 'left' }}>Método</th>
                  {registrosPeso.some(r => r.observaciones) && <th style={{ padding: '0.75rem', textAlign: 'left' }}>Observaciones</th>}
                </tr>
              </thead>
              <tbody>
                {registrosPeso.map((r) => (
                  <tr key={r.id} style={{ borderBottom: '1px solid #f3f4f6' }}>
                    <td style={{ padding: '0.75rem' }}>{new Date(r.fechaPesaje).toLocaleDateString('es-ES')}</td>
                    <td style={{ padding: '0.75rem', textAlign: 'right', fontWeight: '500' }}>{Number(r.pesoPromedio).toFixed(2)}</td>
                    <td style={{ padding: '0.75rem', textAlign: 'center' }}>{r.cantidadAnimales}</td>
                    <td style={{ padding: '0.75rem' }}>{r.metodo === 'MUESTREO' ? 'Muestreo' : r.metodo === 'ESTIMADO' ? 'Estimado' : 'Balanza'}</td>
                    {registrosPeso.some(x => x.observaciones) && <td style={{ padding: '0.75rem', maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }} title={r.observaciones}>{r.observaciones || '—'}</td>}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Historial de Muertes */}
      {recria.historialMuertes && recria.historialMuertes.length > 0 && (
        <div style={{
          backgroundColor: 'white',
          padding: '2rem',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          marginBottom: '1.5rem'
        }}>
          <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1.5rem', color: '#1f2937' }}>
            <SemanticIcon semanticName="warning" size={24} style={{ marginRight: '0.5rem' }} /> Historial de Muertes
          </h2>
          <div style={{
            padding: '1rem',
            backgroundColor: '#fee2e2',
            borderRadius: '0.375rem',
            marginBottom: '1rem',
            border: '1px solid #fecaca'
          }}>
            <p style={{ fontSize: '0.875rem', fontWeight: '500', color: '#991b1b', marginBottom: '0.25rem' }}>
              Total de muertes registradas: {totalMuertes}
            </p>
            <p style={{ fontSize: '0.75rem', color: '#991b1b' }}>
              Mortalidad: {recria.cantidadAnimales > 0 ? ((totalMuertes / recria.cantidadAnimales) * 100).toFixed(2) : 0}%
            </p>
          </div>
          <div style={{ display: 'grid', gap: '1rem' }}>
            {recria.historialMuertes.map((muerte, index) => (
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
                      {new Date(muerte.fecha).toLocaleDateString('es-ES')}
                    </p>
                    <div style={{ display: 'flex', gap: '1rem', fontSize: '0.75rem', color: '#6b7280' }}>
                      <span>Causa: <strong>{obtenerNombreCausa(muerte.causa)}</strong></span>
                      <span>Cantidad: <strong style={{ color: '#ef4444' }}>{muerte.cantidad}</strong></span>
                    </div>
                    {muerte.observaciones && (
                      <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.5rem' }}>
                        {muerte.observaciones}
                      </p>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Modal Registrar Muerte */}
      {showModalMuerte && (
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
              <SemanticIcon semanticName="warning" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Muerte
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha *
                </label>
                <input
                  type="date"
                  value={formMuerte.fecha}
                  onChange={(e) => setFormMuerte({...formMuerte, fecha: e.target.value})}
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
                  Causa *
                </label>
                <select
                  value={formMuerte.causa}
                  onChange={(e) => setFormMuerte({...formMuerte, causa: e.target.value as CausaMuerte})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="APLASTAMIENTO">Aplastamiento</option>
                  <option value="DIARREA">Diarrea</option>
                  <option value="MALFORMACION">Malformación</option>
                  <option value="DEBILIDAD">Debilidad</option>
                  <option value="ENFERMEDAD">Enfermedad</option>
                  <option value="ACCIDENTE">Accidente</option>
                  <option value="OTRA">Otra</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Cantidad *
                </label>
                <input
                  type="number"
                  min="1"
                  max={animalesRestantes}
                  value={formMuerte.cantidad || ''}
                  onChange={(e) => setFormMuerte({...formMuerte, cantidad: parseInt(e.target.value) || 0})}
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
                  Máximo: {animalesRestantes} animales restantes
                </p>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formMuerte.observaciones || ''}
                  onChange={(e) => setFormMuerte({...formMuerte, observaciones: e.target.value})}
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
                  onClick={() => setShowModalMuerte(false)}
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
                  onClick={handleRegistrarMuerte}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: '#ef4444',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '500'
                  }}
                >
                  Registrar Muerte
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Registrar Egreso */}
      {showModalEgreso && (
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
              <SemanticIcon semanticName="success" size={18} style={{ marginRight: '0.5rem' }} /> Registrar Egreso
            </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha de Salida *
                </label>
                <input
                  type="date"
                  value={formEgreso.fechaSalida}
                  onChange={(e) => setFormEgreso({...formEgreso, fechaSalida: e.target.value})}
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
                  Destino *
                </label>
                <select
                  value={formEgreso.destino}
                  onChange={(e) => setFormEgreso({...formEgreso, destino: e.target.value as DestinoRecria})}
                  style={{
                    width: '100%',
                    padding: '0.75rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="VENTA">Venta</option>
                  <option value="FUTURA_MADRE">Futura Madre</option>
                  <option value="ENGORDE">Engorde</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formEgreso.observaciones}
                  onChange={(e) => setFormEgreso({...formEgreso, observaciones: e.target.value})}
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
                  onClick={() => setShowModalEgreso(false)}
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
                  onClick={handleRegistrarEgreso}
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
                  Registrar Egreso
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Registrar pesada */}
      {showModalPeso && (
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
            <h2 style={{ fontSize: '1.25rem', fontWeight: 'bold', marginBottom: '1rem' }}>
              <Icon name="Scale" size={18} style={{ marginRight: '0.5rem', verticalAlign: 'middle' }} /> Registrar pesada
            </h2>
            <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '1rem' }}>
              El peso actual del lote se actualizará con esta pesada. Se guarda el historial; no se borran pesadas anteriores.
            </p>
            {errorPeso && (
              <div style={{ padding: '0.75rem', backgroundColor: '#fee2e2', border: '1px solid #fecaca', borderRadius: '0.375rem', marginBottom: '1rem', color: '#991b1b', fontSize: '0.875rem' }}>
                {errorPeso}
              </div>
            )}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Fecha de pesada *</label>
                <input
                  type="date"
                  value={formPeso.fechaPesaje || ''}
                  onChange={(e) => setFormPeso(prev => ({ ...prev, fechaPesaje: e.target.value }))}
                  max={new Date().toISOString().split('T')[0]}
                  style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Peso promedio (kg) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={formPeso.pesoPromedio || ''}
                  onChange={(e) => setFormPeso(prev => ({ ...prev, pesoPromedio: parseFloat(e.target.value) || 0 }))}
                  style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                  placeholder="Ej. 12.5"
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Cantidad de animales</label>
                <input
                  type="number"
                  min="1"
                  value={formPeso.cantidadAnimales ?? ''}
                  onChange={(e) => setFormPeso(prev => ({ ...prev, cantidadAnimales: parseInt(e.target.value) || undefined }))}
                  style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                />
                <p style={{ fontSize: '0.75rem', color: '#6b7280', marginTop: '0.25rem' }}>Por defecto: {recria.cantidadAnimales} del lote</p>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Método</label>
                <select
                  value={formPeso.metodo || 'BALANZA'}
                  onChange={(e) => setFormPeso(prev => ({ ...prev, metodo: e.target.value as 'BALANZA' | 'MUESTREO' | 'ESTIMADO' }))}
                  style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem' }}
                >
                  <option value="BALANZA">Balanza</option>
                  <option value="MUESTREO">Muestreo</option>
                  <option value="ESTIMADO">Estimado</option>
                </select>
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>Observaciones</label>
                <textarea
                  value={formPeso.observaciones || ''}
                  onChange={(e) => setFormPeso(prev => ({ ...prev, observaciones: e.target.value }))}
                  rows={2}
                  style={{ width: '100%', padding: '0.75rem', border: '1px solid #d1d5db', borderRadius: '0.375rem', fontSize: '0.875rem', fontFamily: 'inherit' }}
                  placeholder="Opcional"
                />
              </div>
              <div style={{ display: 'flex', gap: '1rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => { setShowModalPeso(false); setErrorPeso(null); }}
                  style={{ padding: '0.75rem 1.5rem', backgroundColor: '#6b7280', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer', fontWeight: '500' }}
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  onClick={handleRegistrarPeso}
                  disabled={guardandoPeso || !formPeso.pesoPromedio || formPeso.pesoPromedio <= 0}
                  style={{
                    padding: '0.75rem 1.5rem',
                    backgroundColor: guardandoPeso || !formPeso.pesoPromedio || formPeso.pesoPromedio <= 0 ? '#9ca3af' : '#3b82f6',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: guardandoPeso ? 'not-allowed' : 'pointer',
                    fontWeight: '500'
                  }}
                >
                  {guardandoPeso ? 'Guardando...' : 'Registrar pesada'}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RecriaDetailScreen;
