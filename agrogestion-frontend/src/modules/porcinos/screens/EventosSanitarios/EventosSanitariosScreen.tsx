import React, { useState, useEffect } from 'react';
import { Icon, SemanticIcon } from '../../../../components/icons';
import { eventosSanitariosService } from '../../services/eventosSanitariosService';
import { madresService } from '../../services/madresService';
import { padrillosService } from '../../services/padrillosService';
import { recriaService } from '../../services/recriaService';
import { insumosService } from '../../../../services/apiServices';
import type { 
  EventoSanitario, 
  EventoSanitarioCreateDTO, 
  FiltrosEventosSanitarios,
  TipoEventoSanitario,
  TipoEntidadEvento,
  CategoriaEventoSanitario
} from '../../types';

const EventosSanitariosScreen: React.FC = () => {
  const [eventos, setEventos] = useState<EventoSanitario[]>([]);
  const [tiposEvento, setTiposEvento] = useState<TipoEventoSanitario[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [mostrarFormulario, setMostrarFormulario] = useState(false);
  const [eventoEditando, setEventoEditando] = useState<EventoSanitario | null>(null);
  const [retirosVencidos, setRetirosVencidos] = useState<EventoSanitario[]>([]);
  const [retirosProximos, setRetirosProximos] = useState<EventoSanitario[]>([]);

  // Filtros
  const [filtros, setFiltros] = useState<FiltrosEventosSanitarios>({});
  const [filtroTipoEntidad, setFiltroTipoEntidad] = useState<TipoEntidadEvento | ''>('');
  const [filtroFechaInicio, setFiltroFechaInicio] = useState('');
  const [filtroFechaFin, setFiltroFechaFin] = useState('');
  const [filtroCategoria, setFiltroCategoria] = useState<CategoriaEventoSanitario | ''>('');

  // Datos para el formulario
  const [formData, setFormData] = useState<EventoSanitarioCreateDTO>({
    tipoEventoSanitarioId: 0,
    insumoId: undefined,
    fecha: new Date().toISOString().split('T')[0],
    tipoEntidad: 'MADRE',
    entidadId: 0,
    dosis: undefined,
    unidadDosis: 'ml',
    loteMedicamento: '',
    profesionalResponsable: '',
    fechaRetiro: undefined,
    observaciones: '',
  });

  // Listas para selección de entidades
  const [madres, setMadres] = useState<any[]>([]);
  const [padrillos, setPadrillos] = useState<any[]>([]);
  const [recrias, setRecrias] = useState<any[]>([]);
  const [insumos, setInsumos] = useState<any[]>([]);

  useEffect(() => {
    cargarDatos();
  }, [filtros]);

  const cargarDatos = async () => {
    setLoading(true);
    setError(null);
    try {
      const [eventosData, tiposData, retirosVencidosData, retirosProximosData] = await Promise.all([
        eventosSanitariosService.listar(filtros),
        eventosSanitariosService.obtenerTiposEventoSanitario(),
        eventosSanitariosService.obtenerRetirosVencidos(),
        eventosSanitariosService.obtenerRetirosProximos(),
      ]);

      setEventos(eventosData || []);
      setTiposEvento(tiposData || []);
      setRetirosVencidos(retirosVencidosData || []);
      setRetirosProximos(retirosProximosData || []);

      // Cargar listas de entidades e insumos
      const [madresData, padrillosData, recriasData, insumosData] = await Promise.all([
        madresService.listar(),
        padrillosService.listar(),
        recriaService.listar(),
        insumosService.listar(),
      ]);

      setMadres(madresData || []);
      setPadrillos(padrillosData || []);
      setRecrias(recriasData || []);
      setInsumos(insumosData || []);
    } catch (err: any) {
      setError(err.response?.data?.mensaje || 'Error al cargar eventos sanitarios');
      console.error('Error:', err);
    } finally {
      setLoading(false);
    }
  };

  const aplicarFiltros = () => {
    const nuevosFiltros: FiltrosEventosSanitarios = {};
    if (filtroTipoEntidad) nuevosFiltros.tipoEntidad = filtroTipoEntidad;
    if (filtroFechaInicio) nuevosFiltros.fechaInicio = filtroFechaInicio;
    if (filtroFechaFin) nuevosFiltros.fechaFin = filtroFechaFin;
    if (filtroCategoria) nuevosFiltros.categoria = filtroCategoria;
    setFiltros(nuevosFiltros);
  };

  const limpiarFiltros = () => {
    setFiltroTipoEntidad('');
    setFiltroFechaInicio('');
    setFiltroFechaFin('');
    setFiltroCategoria('');
    setFiltros({});
  };

  const abrirFormularioNuevo = () => {
    setEventoEditando(null);
    setFormData({
      tipoEventoSanitarioId: 0,
      insumoId: undefined,
      fecha: new Date().toISOString().split('T')[0],
      tipoEntidad: 'MADRE',
      entidadId: 0,
      dosis: undefined,
      unidadDosis: 'ml',
      loteMedicamento: '',
      profesionalResponsable: '',
      fechaRetiro: undefined,
      observaciones: '',
    });
    setMostrarFormulario(true);
  };

  const abrirFormularioEditar = (evento: EventoSanitario) => {
    setEventoEditando(evento);
    setFormData({
      tipoEventoSanitarioId: evento.tipoEventoSanitarioId,
      insumoId: evento.insumoId,
      fecha: evento.fecha,
      tipoEntidad: evento.tipoEntidad,
      entidadId: evento.entidadId,
      dosis: evento.dosis,
      unidadDosis: evento.unidadDosis || 'ml',
      loteMedicamento: evento.loteMedicamento || '',
      profesionalResponsable: evento.profesionalResponsable || '',
      fechaRetiro: evento.fechaRetiro,
      observaciones: evento.observaciones || '',
    });
    setMostrarFormulario(true);
  };

  const guardarEvento = async () => {
    try {
      if (eventoEditando?.id) {
        await eventosSanitariosService.actualizar(eventoEditando.id, formData);
      } else {
        await eventosSanitariosService.crear(formData);
      }
      setMostrarFormulario(false);
      cargarDatos();
    } catch (err: any) {
      setError(err.response?.data?.mensaje || 'Error al guardar el evento sanitario');
    }
  };

  const eliminarEvento = async (id: number) => {
    if (!window.confirm('¿Está seguro de eliminar este evento sanitario?')) return;
    try {
      await eventosSanitariosService.eliminar(id);
      cargarDatos();
    } catch (err: any) {
      setError(err.response?.data?.mensaje || 'Error al eliminar el evento sanitario');
    }
  };

  const marcarRetiroCumplido = async (id: number) => {
    try {
      await eventosSanitariosService.marcarRetiroCumplido(id);
      cargarDatos();
    } catch (err: any) {
      setError(err.response?.data?.mensaje || 'Error al marcar retiro como cumplido');
    }
  };

  const obtenerEntidadesDisponibles = () => {
    switch (formData.tipoEntidad) {
      case 'MADRE':
        return madres.filter(m => m.activo !== false);
      case 'PADRILLO':
        return padrillos.filter(p => p.activo !== false);
      case 'LOTE':
        return recrias.filter(r => r.activo !== false);
      default:
        return [];
    }
  };

  const obtenerNombreEntidad = (evento: EventoSanitario) => {
    if (evento.entidadNombre) return evento.entidadNombre;
    if (evento.entidadCodigo) return evento.entidadCodigo;
    return `ID: ${evento.entidadId}`;
  };

  const obtenerTipoEvento = (tipoId: number) => {
    return tiposEvento.find(t => t.id === tipoId);
  };

  const obtenerColorCategoria = (categoria?: CategoriaEventoSanitario) => {
    switch (categoria) {
      case 'VACUNACION':
        return '#10b981';
      case 'DESPARASITACION':
        return '#3b82f6';
      case 'ANTIBIOTICO':
        return '#ef4444';
      case 'VITAMINA':
        return '#f59e0b';
      case 'TRATAMIENTO':
        return '#8b5cf6';
      case 'CONTROL':
        return '#6366f1';
      default:
        return '#6b7280';
    }
  };

  if (loading && eventos.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center' }}>
        <SemanticIcon semanticName="pending" size={32} />
        <p>Cargando eventos sanitarios...</p>
      </div>
    );
  }

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', color: '#1f2937' }}>
          <Icon name="Syringe" size={32} style={{ marginRight: '0.5rem' }} /> Eventos Sanitarios
        </h1>
        <button
          onClick={abrirFormularioNuevo}
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
          <Icon name="Plus" size={18} style={{ marginRight: '0.5rem' }} /> Nuevo Evento
        </button>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          color: '#991b1b',
          borderRadius: '0.375rem',
          marginBottom: '1.5rem'
        }}>
          {error}
        </div>
      )}

      {/* Alertas de Retiros */}
      {(retirosVencidos.length > 0 || retirosProximos.length > 0) && (
        <div style={{ marginBottom: '1.5rem' }}>
          {retirosVencidos.length > 0 && (
            <div style={{
              padding: '1rem',
              backgroundColor: '#fee2e2',
              borderLeft: '4px solid #ef4444',
              borderRadius: '0.375rem',
              marginBottom: '0.5rem'
            }}>
              <strong style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}><SemanticIcon semanticName="warning" size={20} /> Retiros Vencidos ({retirosVencidos.length})</strong>
              <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.875rem' }}>
                Hay {retirosVencidos.length} evento(s) sanitario(s) con retiro vencido que requieren atención.
              </p>
            </div>
          )}
          {retirosProximos.length > 0 && (
            <div style={{
              padding: '1rem',
              backgroundColor: '#fef3c7',
              borderLeft: '4px solid #f59e0b',
              borderRadius: '0.375rem'
            }}>
              <strong>⏰ Retiros Próximos ({retirosProximos.length})</strong>
              <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.875rem' }}>
                Hay {retirosProximos.length} evento(s) sanitario(s) con retiro próximo a vencer.
              </p>
            </div>
          )}
        </div>
      )}

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
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Tipo de Entidad
            </label>
            <select
              value={filtroTipoEntidad}
              onChange={(e) => setFiltroTipoEntidad(e.target.value as TipoEntidadEvento | '')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todos</option>
              <option value="MADRE">Madre</option>
              <option value="PADRILLO">Padrillo</option>
              <option value="LOTE">Lote</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Categoría
            </label>
            <select
              value={filtroCategoria}
              onChange={(e) => setFiltroCategoria(e.target.value as CategoriaEventoSanitario | '')}
              style={{
                width: '100%',
                padding: '0.5rem',
                border: '1px solid #d1d5db',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              <option value="">Todas</option>
              <option value="VACUNACION">Vacunación</option>
              <option value="DESPARASITACION">Desparasitación</option>
              <option value="ANTIBIOTICO">Antibiótico</option>
              <option value="VITAMINA">Vitamina</option>
              <option value="TRATAMIENTO">Tratamiento</option>
              <option value="CONTROL">Control</option>
              <option value="OTRO">Otro</option>
            </select>
          </div>

          <div>
            <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
              Fecha Inicio
            </label>
            <input
              type="date"
              value={filtroFechaInicio}
              onChange={(e) => setFiltroFechaInicio(e.target.value)}
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
              Fecha Fin
            </label>
            <input
              type="date"
              value={filtroFechaFin}
              onChange={(e) => setFiltroFechaFin(e.target.value)}
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
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          <button
            onClick={aplicarFiltros}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontSize: '0.875rem'
            }}
          >
            Aplicar Filtros
          </button>
          <button
            onClick={limpiarFiltros}
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
            Limpiar
          </button>
        </div>
      </div>

      {/* Tabla de Eventos */}
      <div style={{
        backgroundColor: 'white',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        overflow: 'hidden'
      }}>
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <thead>
            <tr style={{ backgroundColor: '#f9fafb', borderBottom: '2px solid #e5e7eb' }}>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Fecha</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Tipo</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Entidad</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Insumo</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Dosis</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Retiro</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Profesional</th>
              <th style={{ padding: '0.75rem', textAlign: 'left', fontSize: '0.875rem', fontWeight: '600' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {eventos.length === 0 ? (
              <tr>
                <td colSpan={8} style={{ padding: '2rem', textAlign: 'center', color: '#6b7280' }}>
                  No hay eventos sanitarios registrados
                </td>
              </tr>
            ) : (
              eventos.map((evento) => {
                const tipoEvento = obtenerTipoEvento(evento.tipoEventoSanitarioId);
                const retiroVencido = evento.fechaRetiro && !evento.retiroCumplido && new Date(evento.fechaRetiro) < new Date();
                const retiroProximo = evento.fechaRetiro && !evento.retiroCumplido && 
                  new Date(evento.fechaRetiro) >= new Date() && 
                  new Date(evento.fechaRetiro) <= new Date(Date.now() + 7 * 24 * 60 * 60 * 1000);

                return (
                  <tr key={evento.id} style={{ borderBottom: '1px solid #e5e7eb' }}>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {new Date(evento.fecha).toLocaleDateString('es-AR')}
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      <span style={{
                        display: 'inline-block',
                        padding: '0.25rem 0.5rem',
                        borderRadius: '0.25rem',
                        backgroundColor: obtenerColorCategoria(tipoEvento?.categoria) + '20',
                        color: obtenerColorCategoria(tipoEvento?.categoria),
                        fontSize: '0.75rem',
                        fontWeight: '500'
                      }}>
                        {tipoEvento?.nombre || 'N/A'}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      <span style={{ fontWeight: '500' }}>
                        {evento.tipoEntidad}: {obtenerNombreEntidad(evento)}
                      </span>
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {evento.insumo?.nombre || evento.insumoId ? (
                        <span style={{ fontWeight: '500' }}>
                          {evento.insumo?.nombre || `ID: ${evento.insumoId}`}
                        </span>
                      ) : '-'}
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {evento.dosis ? `${evento.dosis} ${evento.unidadDosis || 'ml'}` : '-'}
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {evento.fechaRetiro ? (
                        <div>
                          <div style={{ 
                            color: retiroVencido ? '#ef4444' : retiroProximo ? '#f59e0b' : '#10b981',
                            fontWeight: retiroVencido || retiroProximo ? '600' : 'normal'
                          }}>
                            {new Date(evento.fechaRetiro).toLocaleDateString('es-AR')}
                            {evento.retiroCumplido && ' ✓'}
                          </div>
                          {retiroVencido && !evento.retiroCumplido && (
                            <button
                              onClick={() => marcarRetiroCumplido(evento.id!)}
                              style={{
                                marginTop: '0.25rem',
                                padding: '0.25rem 0.5rem',
                                backgroundColor: '#10b981',
                                color: 'white',
                                border: 'none',
                                borderRadius: '0.25rem',
                                cursor: 'pointer',
                                fontSize: '0.75rem'
                              }}
                            >
                              Marcar Cumplido
                            </button>
                          )}
                        </div>
                      ) : '-'}
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      {evento.profesionalResponsable || '-'}
                    </td>
                    <td style={{ padding: '0.75rem', fontSize: '0.875rem' }}>
                      <div style={{ display: 'flex', gap: '0.5rem' }}>
                        <button
                          onClick={() => abrirFormularioEditar(evento)}
                          style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: '#3b82f6',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.25rem',
                            cursor: 'pointer',
                            fontSize: '0.75rem'
                          }}
                        >
                          <Icon name="Pencil" size={16} />
                        </button>
                        <button
                          onClick={() => eliminarEvento(evento.id!)}
                          style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: '#ef4444',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.25rem',
                            cursor: 'pointer',
                            fontSize: '0.75rem'
                          }}
                        >
                          <Icon name="Trash2" size={16} />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {/* Modal de Formulario */}
      {mostrarFormulario && (
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
            borderRadius: '0.5rem',
            padding: '2rem',
            width: '90%',
            maxWidth: '600px',
            maxHeight: '90vh',
            overflow: 'auto'
          }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              {eventoEditando ? 'Editar Evento Sanitario' : 'Nuevo Evento Sanitario'}
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Tipo de Evento *
                </label>
                <select
                  value={formData.tipoEventoSanitarioId}
                  onChange={(e) => {
                    const tipoId = parseInt(e.target.value);
                    const tipo = tiposEvento.find(t => t.id === tipoId);
                    setFormData({
                      ...formData,
                      tipoEventoSanitarioId: tipoId,
                      fechaRetiro: tipo?.requiereFechaRetiro && formData.fechaRetiro ? formData.fechaRetiro : undefined
                    });
                  }}
                  required
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="0">Seleccione un tipo</option>
                  {tiposEvento.filter(t => t.activo !== false).map(tipo => (
                    <option key={tipo.id} value={tipo.id}>
                      {tipo.nombre} ({tipo.categoria})
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Fecha *
                </label>
                <input
                  type="date"
                  value={formData.fecha}
                  onChange={(e) => setFormData({ ...formData, fecha: e.target.value })}
                  required
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
                  Tipo de Entidad *
                </label>
                <select
                  value={formData.tipoEntidad}
                  onChange={(e) => setFormData({ ...formData, tipoEntidad: e.target.value as TipoEntidadEvento, entidadId: 0 })}
                  required
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="MADRE">Madre</option>
                  <option value="PADRILLO">Padrillo</option>
                  <option value="LOTE">Lote (Recría)</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  {formData.tipoEntidad === 'MADRE' ? 'Madre' : formData.tipoEntidad === 'PADRILLO' ? 'Padrillo' : 'Lote'} *
                </label>
                <select
                  value={formData.entidadId}
                  onChange={(e) => setFormData({ ...formData, entidadId: parseInt(e.target.value) })}
                  required
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="0">Seleccione una {formData.tipoEntidad === 'MADRE' ? 'madre' : formData.tipoEntidad === 'PADRILLO' ? 'padrillo' : 'recría'}</option>
                  {obtenerEntidadesDisponibles().map(entidad => (
                    <option key={entidad.id} value={entidad.id}>
                      {entidad.identificacion || entidad.codigo || `ID: ${entidad.id}`}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Insumo (Vacuna/Medicamento)
                </label>
                <select
                  value={formData.insumoId || ''}
                  onChange={(e) => setFormData({ ...formData, insumoId: e.target.value ? parseInt(e.target.value) : undefined })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="">Sin insumo (opcional)</option>
                  {insumos
                    .filter(i => i.activo !== false && (i.tipo === 'VACUNA' || i.tipo === 'MEDICAMENTO' || i.tipo === 'VITAMINA'))
                    .map(insumo => (
                      <option key={insumo.id} value={insumo.id}>
                        {insumo.nombre} {insumo.stockActual !== undefined ? `(Stock: ${insumo.stockActual} ${insumo.unidadMedida || ''})` : ''}
                      </option>
                    ))}
                </select>
                {formData.insumoId && (
                  <p style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: '#6b7280' }}>
                    Al guardar, se descontará automáticamente la dosis del inventario
                  </p>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Dosis
                  </label>
                  <input
                    type="number"
                    step="0.01"
                    value={formData.dosis || ''}
                    onChange={(e) => setFormData({ ...formData, dosis: e.target.value ? parseFloat(e.target.value) : undefined })}
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
                    Unidad
                  </label>
                  <input
                    type="text"
                    value={formData.unidadDosis || 'ml'}
                    onChange={(e) => setFormData({ ...formData, unidadDosis: e.target.value })}
                    placeholder="ml"
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

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Lote de Medicamento
                </label>
                <input
                  type="text"
                  value={formData.loteMedicamento || ''}
                  onChange={(e) => setFormData({ ...formData, loteMedicamento: e.target.value })}
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
                  Profesional Responsable
                </label>
                <input
                  type="text"
                  value={formData.profesionalResponsable || ''}
                  onChange={(e) => setFormData({ ...formData, profesionalResponsable: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                />
              </div>

              {obtenerTipoEvento(formData.tipoEventoSanitarioId)?.requiereFechaRetiro && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                    Fecha de Retiro
                  </label>
                  <input
                    type="date"
                    value={formData.fechaRetiro || ''}
                    onChange={(e) => {
                      const fecha = e.target.value;
                      const tipo = obtenerTipoEvento(formData.tipoEventoSanitarioId);
                      if (tipo?.diasRetiroDefecto && fecha) {
                        const fechaRetiro = new Date(fecha);
                        fechaRetiro.setDate(fechaRetiro.getDate() + tipo.diasRetiroDefecto);
                        setFormData({ ...formData, fechaRetiro: fechaRetiro.toISOString().split('T')[0] });
                      } else {
                        setFormData({ ...formData, fechaRetiro: fecha || undefined });
                      }
                    }}
                    style={{
                      width: '100%',
                      padding: '0.5rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem'
                    }}
                  />
                  {obtenerTipoEvento(formData.tipoEventoSanitarioId)?.diasRetiroDefecto && (
                    <p style={{ marginTop: '0.25rem', fontSize: '0.75rem', color: '#6b7280' }}>
                      Días de retiro por defecto: {obtenerTipoEvento(formData.tipoEventoSanitarioId)?.diasRetiroDefecto} días
                    </p>
                  )}
                </div>
              )}

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontSize: '0.875rem', fontWeight: '500' }}>
                  Observaciones
                </label>
                <textarea
                  value={formData.observaciones || ''}
                  onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    fontFamily: 'inherit'
                  }}
                />
              </div>
            </div>

            <div style={{ display: 'flex', gap: '0.5rem', marginTop: '1.5rem', justifyContent: 'flex-end' }}>
              <button
                onClick={() => setMostrarFormulario(false)}
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
                Cancelar
              </button>
              <button
                onClick={guardarEvento}
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
                {eventoEditando ? 'Actualizar' : 'Guardar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default EventosSanitariosScreen;

