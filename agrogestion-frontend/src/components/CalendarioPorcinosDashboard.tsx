import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { API_ENDPOINTS } from '../services/apiEndpoints';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useCurrencyUpdate } from '../hooks/useCurrencyUpdate';
import EmpresaSelector from './EmpresaSelector';
import { Icon } from '../core/components/Icon';
import { AccionCalendarioPorcinosModal } from './porcinos/AccionCalendarioPorcinosModal';

interface EventoCalendario {
  id: string;
  tipo: 'PARTO' | 'ECOGRAFIA' | 'DESTETE' | 'CONTROL_CELO' | 'GESTACION_VENCIDA' | 'RECORDATORIO' | 'TAREA_RECURRENTE';
  titulo: string;
  descripcion?: string;
  fecha: string;
  fechaFin?: string;
  estado?: string;
  prioridad?: 'CRITICA' | 'ALTA' | 'MEDIA' | 'BAJA' | 'COMPLETADO';
  color?: string;
  partoId?: number;
  gestacionId?: number;
  servicioId?: number;
  responsable?: string;
  usuarioId?: number;
  usuarioNombre?: string;
  tipoRecordatorio?: string;
  completado?: boolean;
  recordatorioId?: number;
  madreId?: number;
  madreIdentificacion?: string;
  nacidosVivos?: number;
  serieId?: number;
  tipoRepeticion?: string;
  cumplida?: boolean;
}

interface RecordatorioForm {
  titulo: string;
  descripcion: string;
  fecha: string;
  tipo: string;
  partoId?: number;
  reproduccionId?: number;
  sanidadId?: number;
  alimentacionId?: number;
}

const CalendarioPorcinosDashboard: React.FC = () => {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { empresaActiva, rolUsuario } = useEmpresa();
  const { formatCurrency, selectedCurrency, exchangeType, realRates, changeCurrency, changeExchangeType } = useCurrencyContext();
  useCurrencyUpdate();

  const [fechaActual, setFechaActual] = useState(new Date());
  const [eventos, setEventos] = useState<EventoCalendario[]>([]);
  const [loading, setLoading] = useState(false);
  const [mostrarModalRecordatorio, setMostrarModalRecordatorio] = useState(false);
  const [mostrarModalDetalleRecordatorio, setMostrarModalDetalleRecordatorio] = useState(false);
  const [eventoSeleccionado, setEventoSeleccionado] = useState<any>(null);
  const [recordatorioSeleccionado, setRecordatorioSeleccionado] = useState<any>(null);
  const [loadingEvento, setLoadingEvento] = useState(false);
  const [loadingRecordatorio, setLoadingRecordatorio] = useState(false);
  const [fechaSeleccionada, setFechaSeleccionada] = useState<string>('');
  const [formRecordatorio, setFormRecordatorio] = useState<RecordatorioForm>({
    titulo: '',
    descripcion: '',
    fecha: '',
    tipo: 'GENERAL'
  });
  const [eliminandoRecordatorio, setEliminandoRecordatorio] = useState(false);
  const [modalAccion, setModalAccion] = useState<'control_celo' | 'chequeo_gestacion' | 'destete' | 'parto' | null>(null);
  const [eventoParaModal, setEventoParaModal] = useState<EventoCalendario | null>(null);

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  // Obtener primer día del mes y último día del mes
  const primerDiaMes = new Date(fechaActual.getFullYear(), fechaActual.getMonth(), 1);
  const ultimoDiaMes = new Date(fechaActual.getFullYear(), fechaActual.getMonth() + 1, 0);
  const primerDiaSemana = primerDiaMes.getDay();
  const diasEnMes = ultimoDiaMes.getDate();

  // Cargar eventos del calendario
  useEffect(() => {
    cargarEventos();
  }, [fechaActual]);

  const cargarEventos = async () => {
    setLoading(true);
    const fechaInicio = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-01`;
    const fechaFin = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-${String(diasEnMes).padStart(2, '0')}`;
    try {
      const response = await api.get(`/calendario/porcinos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`);
      let eventosUnicos: EventoCalendario[] = [];
      if (response.data && response.data.eventos) {
        const eventosFormateados: EventoCalendario[] = response.data.eventos.map((e: any) => ({
          id: e.id,
          tipo: e.tipo,
          titulo: e.titulo,
          descripcion: e.descripcion,
          fecha: e.fecha,
          prioridad: e.prioridad,
          color: e.color,
          partoId: e.partoId,
          gestacionId: e.gestacionId,
          servicioId: e.servicioId,
          tipoRecordatorio: e.tipoRecordatorio,
          completado: e.completado,
          recordatorioId: e.recordatorioId,
          madreId: e.madreId,
          madreIdentificacion: e.madreIdentificacion,
          nacidosVivos: e.nacidosVivos,
        }));
        eventosUnicos = eventosFormateados.filter(
          (evento, index, self) => index === self.findIndex((ev) => ev.id === evento.id)
        );
        eventosUnicos = eventosUnicos.filter((e) => e.tipo !== 'TAREA_RECURRENTE');
      }
      setEventos(eventosUnicos);
    } catch (error) {
      console.error('Error cargando eventos:', error);
      try {
        const response = await api.get(`/calendario/eventos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`);
        let lista: EventoCalendario[] = [];
        if (response.data && response.data.recordatorios) {
          lista = response.data.recordatorios.map((r: any) => ({
            id: r.id,
            tipo: 'RECORDATORIO' as const,
            titulo: r.titulo,
            descripcion: r.descripcion,
            fecha: r.fecha,
            tipoRecordatorio: r.tipoRecordatorio,
            completado: r.completado,
            recordatorioId: r.recordatorioId || r.id?.replace('recordatorio_', ''),
          }));
        }
        lista = lista.filter((e) => e.tipo !== 'TAREA_RECURRENTE');
        setEventos(lista);
      } catch {
        setEventos([]);
      }
    } finally {
      setLoading(false);
    }
  };

  const mesAnterior = () => {
    setFechaActual(new Date(fechaActual.getFullYear(), fechaActual.getMonth() - 1, 1));
  };

  const mesSiguiente = () => {
    setFechaActual(new Date(fechaActual.getFullYear(), fechaActual.getMonth() + 1, 1));
  };

  const irAHoy = () => {
    setFechaActual(new Date());
  };

  const obtenerEventosDia = (dia: number): EventoCalendario[] => {
    const fechaCompleta = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    return eventos.filter(evento => evento.fecha === fechaCompleta);
  };

  const obtenerColorEvento = (evento: EventoCalendario): string => {
    // Si el evento tiene color definido, usarlo
    if (evento.color) return evento.color;
    
    // Colores por tipo
    switch (evento.tipo) {
      case 'PARTO':
        return '#ec4899'; // Rosa
      case 'ECOGRAFIA':
        return '#8b5cf6'; // Morado
      case 'DESTETE':
        return '#10b981'; // Verde
      case 'CONTROL_CELO':
        return '#6366f1'; // Indigo
      case 'GESTACION_VENCIDA':
        return '#ef4444'; // Rojo
      case 'RECORDATORIO':
        if (evento.completado) return '#6b7280'; // Gris si está completado
        return '#ec4899'; // Rosa
      default:
        return '#6b7280';
    }
  };

  const obtenerIconoEvento = (tipo: string): React.ReactNode => {
    switch (tipo) {
      case 'PARTO': return <Icon name="PiggyBank" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      case 'ECOGRAFIA': return <Icon name="Activity" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      case 'DESTETE': return <Icon name="Baby" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      case 'CONTROL_CELO': return <Icon name="Eye" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      case 'GESTACION_VENCIDA': return <Icon name="AlertTriangle" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      case 'RECORDATORIO': return <Icon name="Pin" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
      default: return <Icon name="Calendar" size={14} style={{ display: 'inline', verticalAlign: 'middle' }} />;
    }
  };

  const obtenerNombreTipo = (tipo: string): string => {
    switch (tipo) {
      case 'PARTO': return 'Parto';
      case 'ECOGRAFIA': return 'Ecografía';
      case 'DESTETE': return 'Destete';
      case 'CONTROL_CELO': return 'Control Celo';
      case 'GESTACION_VENCIDA': return 'Gestación Vencida';
      case 'RECORDATORIO': return 'Recordatorio';
      default: return tipo;
    }
  };

  const mensajeErrorApi = (error: unknown): string => {
    const data = error && typeof error === 'object' && 'response' in error
      ? (error as { response?: { data?: { error?: string; mensaje?: string } } }).response?.data
      : undefined;
    if (data?.error && typeof data.error === 'string') return data.error;
    if (data?.mensaje && typeof data.mensaje === 'string') return data.mensaje;
    return 'Ocurrió un error. Intente de nuevo.';
  };

  const eliminarRecordatorioCalendario = async (recordatorioId: number) => {
    if (!window.confirm('¿Eliminar este recordatorio? No se puede deshacer.')) return;
    setEliminandoRecordatorio(true);
    try {
      await api.delete(`/recordatorios/${recordatorioId}`);
      setMostrarModalDetalleRecordatorio(false);
      setRecordatorioSeleccionado(null);
      if (eventoSeleccionado?.recordatorioId === recordatorioId) {
        setEventoSeleccionado(null);
      }
      await cargarEventos();
    } catch (error) {
      console.error('Error eliminando recordatorio:', error);
      alert(mensajeErrorApi(error));
    } finally {
      setEliminandoRecordatorio(false);
    }
  };

  const abrirModalRecordatorio = (dia: number) => {
    const fechaCompleta = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    setFechaSeleccionada(fechaCompleta);
    setFormRecordatorio({
      titulo: '',
      descripcion: '',
      fecha: fechaCompleta,
      tipo: 'GENERAL'
    });
    setMostrarModalRecordatorio(true);
  };

  const guardarRecordatorio = async () => {
    try {
      await api.post('/recordatorios', {
        titulo: formRecordatorio.titulo,
        descripcion: formRecordatorio.descripcion,
        fecha: formRecordatorio.fecha,
        tipo: formRecordatorio.tipo,
        partoId: formRecordatorio.partoId || null,
        reproduccionId: formRecordatorio.reproduccionId || null,
        sanidadId: formRecordatorio.sanidadId || null,
        alimentacionId: formRecordatorio.alimentacionId || null
      });
      setMostrarModalRecordatorio(false);
      cargarEventos();
    } catch (error) {
      console.error('Error guardando recordatorio:', error);
      alert('Error al guardar el recordatorio');
    }
  };

  const marcarRecordatorioCompletado = async (recordatorioId: number) => {
    try {
      await api.patch(`/recordatorios/${recordatorioId}/completar`);
      cargarEventos();
    } catch (error) {
      console.error('Error marcando recordatorio:', error);
    }
  };

  const abrirModalDetalleRecordatorio = async (recordatorioId: number) => {
    setLoadingRecordatorio(true);
    setMostrarModalDetalleRecordatorio(true);
    try {
      const response = await api.get(`/recordatorios/${recordatorioId}`);
      setRecordatorioSeleccionado(response.data);
    } catch (error) {
      console.error('Error cargando recordatorio:', error);
      alert('Error al cargar los detalles del recordatorio');
      setMostrarModalDetalleRecordatorio(false);
    } finally {
      setLoadingRecordatorio(false);
    }
  };

  const obtenerNombreTipoRecordatorio = (tipo: string): string => {
    const tipos: { [key: string]: string } = {
      'GENERAL': 'General',
      'PARTO': 'Parto',
      'REPRODUCCION': 'Reproducción',
      'SANIDAD': 'Sanidad',
      'ALIMENTACION': 'Alimentación',
      'MANTENIMIENTO': 'Mantenimiento',
      'REUNION': 'Reunión',
      'OTRO': 'Otro'
    };
    return tipos[tipo] || tipo;
  };

  const nombresMeses = [
    'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
    'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
  ];

  const diasSemana = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  const esHoy = (dia: number): boolean => {
    const hoy = new Date();
    return dia === hoy.getDate() &&
           fechaActual.getMonth() === hoy.getMonth() &&
           fechaActual.getFullYear() === hoy.getFullYear();
  };

  return (
    <div style={{ padding: '2rem' }}>
      {/* Cabecera con controles */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '2rem'
      }}>
        <h1 style={{ 
          fontSize: '2rem', 
          fontWeight: 'bold', 
          color: '#1f2937'
        }}>
          <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Icon name="Calendar" size={24} /> Calendario (porcinos y tareas)
          </span>
        </h1>
        
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '1rem'
        }}>
          {/* Selector de Empresa */}
          {empresaActiva && (
            <div style={{
              backgroundColor: 'white',
              borderRadius: '0.375rem',
              boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
              border: '1px solid #e5e7eb',
              padding: '0.5rem'
            }}>
              <EmpresaSelector />
            </div>
          )}
          
          {/* Selector de Moneda */}
          <div style={{
            backgroundColor: 'white',
            borderRadius: '0.375rem',
            boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
            border: '1px solid #e5e7eb',
            padding: '0.5rem'
          }}>
            <select
              value={selectedCurrency === 'ARS' ? 'ARS' : exchangeType}
              onChange={(event) => {
                const value = event.target.value;
                if (value === 'ARS') {
                  changeCurrency('ARS');
                } else if (value === 'oficial' || value === 'blue') {
                  changeCurrency('USD');
                  changeExchangeType(value);
                }
                setTimeout(() => {
                  window.dispatchEvent(new Event('currencyUpdate'));
                }, 100);
              }}
              style={{
                border: 'none',
                outline: 'none',
                fontSize: '0.875rem',
                fontWeight: '500',
                color: '#374151',
                backgroundColor: 'transparent',
                cursor: 'pointer',
                minWidth: '180px'
              }}
            >
              <option value="ARS">ARS (Pesos Argentinos)</option>
              <option value="oficial">
                USD Oficial {realRates?.oficial ? `($${realRates.oficial.toFixed(2)})` : ''}
              </option>
              <option value="blue">
                USD Blue {realRates?.blue ? `($${realRates.blue.toFixed(2)})` : ''}
              </option>
            </select>
          </div>
          
          {/* Botón Cerrar Sesión */}
          <button
            onClick={handleLogout}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#ef4444',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer'
            }}
          >
            Cerrar sesión
          </button>
        </div>
      </div>

      {/* Encabezado del calendario con navegación */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'center',
        marginBottom: '2rem',
        backgroundColor: 'white',
        padding: '1.5rem',
        borderRadius: '0.5rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)'
      }}>
        <div style={{ 
          fontSize: '1.25rem', 
          fontWeight: 'bold',
          color: '#1f2937',
          minWidth: '200px',
          textAlign: 'center'
        }}>
          {nombresMeses[fechaActual.getMonth()]} {fechaActual.getFullYear()}
        </div>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', flexWrap: 'wrap', justifyContent: 'center' }}>
          <button
            onClick={mesAnterior}
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
            ← Anterior
          </button>
          <button
            onClick={irAHoy}
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
            Hoy
          </button>
          <button
            onClick={mesSiguiente}
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
            Siguiente →
          </button>
          <button
            type="button"
            onClick={() => {
              const hoyIso = new Date().toISOString().slice(0, 10);
              setFechaSeleccionada(hoyIso);
              setFormRecordatorio({
                titulo: '',
                descripcion: '',
                fecha: hoyIso,
                tipo: 'GENERAL',
              });
              setMostrarModalRecordatorio(true);
            }}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#ec4899',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontSize: '0.875rem',
            }}
          >
            Nuevo recordatorio
          </button>
        </div>
      </div>

      {/* Leyenda */}
      <div style={{
        backgroundColor: 'white',
        padding: '1rem',
        borderRadius: '0.5rem',
        marginBottom: '1rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        gap: '1.5rem',
        flexWrap: 'wrap'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#ec4899', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="PiggyBank" size={14} /> Partos
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#8b5cf6', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Activity" size={14} /> Ecografías
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#10b981', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Baby" size={14} /> Destetes
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#f59e0b', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Home" size={14} /> Maternidad
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#6366f1', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Eye" size={14} /> Control Celo
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#ef4444', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="AlertTriangle" size={14} /> Vencidas
          </span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#ec4899', borderRadius: '4px', border: '2px dashed #9ca3af' }}></div>
          <span style={{ fontSize: '0.875rem', display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Icon name="Pin" size={14} /> Recordatorios
          </span>
        </div>
      </div>

      {/* Calendario */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div style={{ fontSize: '2rem', marginBottom: '1rem', display: 'flex', justifyContent: 'center' }}>
            <Icon name="Loader" size={32} />
          </div>
          <p>Cargando eventos...</p>
        </div>
      ) : (
        <div style={{
          backgroundColor: 'white',
          borderRadius: '0.5rem',
          boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
          overflow: 'hidden'
        }}>
          {/* Días de la semana */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            backgroundColor: '#f3f4f6',
            borderBottom: '2px solid #e5e7eb'
          }}>
            {diasSemana.map(dia => (
              <div key={dia} style={{
                padding: '0.75rem',
                textAlign: 'center',
                fontWeight: 'bold',
                color: '#374151',
                fontSize: '0.875rem'
              }}>
                {dia}
              </div>
            ))}
          </div>

          {/* Días del mes */}
          <div style={{
            display: 'grid',
            gridTemplateColumns: 'repeat(7, 1fr)',
            gap: '1px',
            backgroundColor: '#e5e7eb'
          }}>
            {/* Espacios vacíos antes del primer día */}
            {Array.from({ length: primerDiaSemana }).map((_, index) => (
              <div key={`empty-${index}`} style={{ backgroundColor: 'white', minHeight: '120px' }}></div>
            ))}

            {/* Días del mes */}
            {Array.from({ length: diasEnMes }).map((_, index) => {
              const dia = index + 1;
              const eventosDia = obtenerEventosDia(dia);
              const esDiaHoy = esHoy(dia);

              return (
                <div
                  key={dia}
                  style={{
                    backgroundColor: 'white',
                    minHeight: '120px',
                    padding: '0.5rem',
                    border: esDiaHoy ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                    cursor: 'pointer',
                    position: 'relative'
                  }}
                  onClick={() => abrirModalRecordatorio(dia)}
                >
                  <div style={{
                    fontWeight: esDiaHoy ? 'bold' : 'normal',
                    color: esDiaHoy ? '#3b82f6' : '#1f2937',
                    marginBottom: '0.5rem',
                    fontSize: '0.875rem'
                  }}>
                    {dia}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem' }}>
                    {eventosDia.slice(0, 3).map((evento, idx) => (
                      <div
                        key={`${evento.id}_${evento.fecha}_${idx}`}
                        style={{
                          backgroundColor: obtenerColorEvento(evento),
                          color: 'white',
                          padding: '0.25rem 0.5rem',
                          borderRadius: '0.25rem',
                          fontSize: '0.7rem',
                          cursor: 'pointer',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap',
                          border: evento.prioridad === 'CRITICA' ? '2px solid #fef08a' : 'none',
                          animation: evento.prioridad === 'CRITICA' ? 'pulse 2s infinite' : 'none'
                        }}
                        title={`${obtenerIconoEvento(evento.tipo)} ${evento.titulo}${evento.descripcion ? '\n' + evento.descripcion : ''}`}
                        onClick={(e) => {
                          e.stopPropagation();
                          if (evento.tipo === 'RECORDATORIO' && evento.recordatorioId) {
                            abrirModalDetalleRecordatorio(evento.recordatorioId);
                          } else {
                            setEventoSeleccionado(evento);
                          }
                        }}
                        onDoubleClick={(e) => {
                          // Doble click: comportamiento consistente, siempre mostrar resumen del evento
                          e.stopPropagation();
                          if (evento.tipo === 'RECORDATORIO' && evento.recordatorioId) {
                            abrirModalDetalleRecordatorio(evento.recordatorioId);
                          } else {
                            setEventoSeleccionado(evento);
                          }
                        }}
                      >
                        {obtenerIconoEvento(evento.tipo)} {evento.titulo.length > 12 ? evento.titulo.substring(0, 12) + '...' : evento.titulo}
                      </div>
                    ))}
                    {eventosDia.length > 3 && (
                      <div style={{
                        fontSize: '0.75rem',
                        color: '#6b7280',
                        textAlign: 'center',
                        padding: '0.25rem'
                      }}>
                        +{eventosDia.length - 3} más
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Modal para agregar recordatorio */}
      {mostrarModalRecordatorio && (
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
            width: '90%',
            maxWidth: '500px'
          }}>
            <h2 style={{ marginTop: 0, marginBottom: '0.5rem' }}>Agregar Recordatorio</h2>
            <p style={{ fontSize: '0.8rem', color: '#6b7280', marginTop: 0, marginBottom: '1rem' }}>
              Un recordatorio es <strong>una sola fecha</strong> en el calendario (avisos de sanidad, reproducción, etc.).
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                  Título *
                </label>
                <input
                  type="text"
                  value={formRecordatorio.titulo}
                  onChange={(e) => setFormRecordatorio({ ...formRecordatorio, titulo: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                  placeholder="Ej: Vacunación de lechones"
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                  Descripción
                </label>
                <textarea
                  value={formRecordatorio.descripcion}
                  onChange={(e) => setFormRecordatorio({ ...formRecordatorio, descripcion: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem',
                    minHeight: '80px'
                  }}
                  placeholder="Detalles adicionales..."
                />
              </div>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                  Tipo
                </label>
                <select
                  value={formRecordatorio.tipo}
                  onChange={(e) => setFormRecordatorio({ ...formRecordatorio, tipo: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                    fontSize: '0.875rem'
                  }}
                >
                  <option value="GENERAL">General</option>
                  <option value="PARTO">Parto</option>
                  <option value="REPRODUCCION">Reproducción</option>
                  <option value="SANIDAD">Sanidad</option>
                  <option value="ALIMENTACION">Alimentación</option>
                  <option value="MANTENIMIENTO">Mantenimiento</option>
                  <option value="REUNION">Reunión</option>
                  <option value="OTRO">Otro</option>
                </select>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end' }}>
                <button
                  onClick={() => setMostrarModalRecordatorio(false)}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer'
                  }}
                >
                  Cancelar
                </button>
                <button
                  onClick={guardarRecordatorio}
                  disabled={!formRecordatorio.titulo}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: formRecordatorio.titulo ? '#3b82f6' : '#9ca3af',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: formRecordatorio.titulo ? 'pointer' : 'not-allowed'
                  }}
                >
                  Guardar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal de Detalle de Evento */}
      {eventoSeleccionado && eventoSeleccionado.tipo !== 'RECORDATORIO' && (
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
          zIndex: 1000,
          padding: '1rem'
        }}
        onClick={() => setEventoSeleccionado(null)}
        >
          <div style={{
            backgroundColor: 'white',
            borderRadius: '0.75rem',
            maxWidth: '500px',
            width: '100%',
            maxHeight: '90vh',
            overflow: 'auto',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
          }}
          onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div style={{
              background: `linear-gradient(135deg, ${obtenerColorEvento(eventoSeleccionado)} 0%, ${obtenerColorEvento(eventoSeleccionado)}dd 100%)`,
              color: 'white',
              padding: '1.5rem 2rem',
              borderRadius: '0.75rem 0.75rem 0 0',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center'
            }}>
              <div>
                <h2 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 'bold' }}>
                  {obtenerIconoEvento(eventoSeleccionado.tipo)} {obtenerNombreTipo(eventoSeleccionado.tipo)}
                </h2>
                <p style={{ margin: '0.25rem 0 0 0', opacity: 0.9, fontSize: '0.875rem' }}>
                  {eventoSeleccionado.madreIdentificacion && `Madre: ${eventoSeleccionado.madreIdentificacion}`}
                </p>
              </div>
              <button
                onClick={() => setEventoSeleccionado(null)}
                style={{
                  background: 'rgba(255, 255, 255, 0.2)',
                  border: 'none',
                  color: 'white',
                  fontSize: '1.5rem',
                  cursor: 'pointer',
                  padding: '0.25rem 0.75rem',
                  borderRadius: '0.375rem'
                }}
              >
                ✕
              </button>
            </div>

            <div style={{ padding: '1.5rem' }}>
              {/* Prioridad */}
              {eventoSeleccionado.prioridad && (
                <div style={{
                  backgroundColor: eventoSeleccionado.prioridad === 'CRITICA' ? '#fef2f2' : 
                                   eventoSeleccionado.prioridad === 'ALTA' ? '#fff7ed' : '#f0fdf4',
                  padding: '1rem',
                  borderRadius: '0.5rem',
                  marginBottom: '1rem',
                  border: `2px solid ${eventoSeleccionado.prioridad === 'CRITICA' ? '#ef4444' : 
                                       eventoSeleccionado.prioridad === 'ALTA' ? '#f59e0b' : '#10b981'}`
                }}>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.25rem' }}>
                    Prioridad
                  </div>
                  <div style={{
                    fontSize: '1rem',
                    fontWeight: 'bold',
                    color: eventoSeleccionado.prioridad === 'CRITICA' ? '#ef4444' : 
                           eventoSeleccionado.prioridad === 'ALTA' ? '#f59e0b' : '#10b981'
                  }}>
                    {eventoSeleccionado.prioridad === 'CRITICA' ? '🚨 CRÍTICA' :
                     eventoSeleccionado.prioridad === 'ALTA' ? '⚠️ Alta' :
                     eventoSeleccionado.prioridad === 'MEDIA' ? '📋 Media' : '📌 Baja'}
                  </div>
                </div>
              )}

              {/* Fecha */}
              <div style={{
                backgroundColor: '#f9fafb',
                padding: '1rem',
                borderRadius: '0.5rem',
                marginBottom: '1rem'
              }}>
                <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.25rem' }}>
                  📅 Fecha Programada
                </div>
                <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                  {new Date(eventoSeleccionado.fecha).toLocaleDateString('es-ES', { 
                    weekday: 'long', 
                    year: 'numeric', 
                    month: 'long', 
                    day: 'numeric' 
                  })}
                </div>
              </div>

              {/* Descripción */}
              {eventoSeleccionado.descripcion && (
                <div style={{
                  backgroundColor: '#f9fafb',
                  padding: '1rem',
                  borderRadius: '0.5rem',
                  marginBottom: '1rem',
                  borderLeft: `4px solid ${obtenerColorEvento(eventoSeleccionado)}`
                }}>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                    📝 Descripción
                  </div>
                  <p style={{ margin: 0, color: '#374151', lineHeight: '1.5' }}>
                    {eventoSeleccionado.descripcion}
                  </p>
                </div>
              )}

              {/* Información adicional */}
              {eventoSeleccionado.nacidosVivos && (
                <div style={{
                  backgroundColor: '#ecfdf5',
                  padding: '1rem',
                  borderRadius: '0.5rem',
                  marginBottom: '1rem'
                }}>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.25rem' }}>
                    🐷 Lechones Nacidos Vivos
                  </div>
                  <div style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#10b981' }}>
                    {eventoSeleccionado.nacidosVivos}
                  </div>
                </div>
              )}

              {/* Botones de Acción: abren modal para registrar la acción en el ciclo de vida y marcar recordatorio */}
              <div style={{ marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                {eventoSeleccionado.tipo === 'DESTETE' && eventoSeleccionado.partoId && (
                  <button
                    onClick={() => { setEventoParaModal(eventoSeleccionado); setModalAccion('destete'); }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      backgroundColor: '#10b981',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '600',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <span>🍼</span>
                    <span>Registrar destete</span>
                  </button>
                )}

                {(eventoSeleccionado.tipo === 'CONTROL_CELO' && eventoSeleccionado.servicioId) && (
                  <button
                    onClick={() => { setEventoParaModal(eventoSeleccionado); setModalAccion('control_celo'); }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      backgroundColor: '#3b82f6',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '600',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <span>🔍</span>
                    <span>Registrar control de celo</span>
                  </button>
                )}

                {eventoSeleccionado.tipo === 'ECOGRAFIA' && eventoSeleccionado.servicioId && (
                  <button
                    onClick={async () => {
                      try {
                        const res = await api.get(API_ENDPOINTS.PORCINOS_SERVICIOS.GESTACION_POR_SERVICIO(eventoSeleccionado.servicioId));
                        const gestacionId = res.data?.gestacionId;
                        setEventoParaModal({ ...eventoSeleccionado, gestacionId });
                        setModalAccion(gestacionId ? 'chequeo_gestacion' : 'control_celo');
                      } catch {
                        setEventoParaModal(eventoSeleccionado);
                        setModalAccion('control_celo');
                      }
                    }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      backgroundColor: '#8b5cf6',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '600',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <span>🔬</span>
                    <span>Registrar chequeo de preñez</span>
                  </button>
                )}

                {eventoSeleccionado.tipo === 'PARTO' && (eventoSeleccionado.gestacionId || eventoSeleccionado.madreId) && (
                  <button
                    onClick={() => { setEventoParaModal(eventoSeleccionado); setModalAccion('parto'); }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      backgroundColor: '#ec4899',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '600',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <span>👶</span>
                    <span>Registrar parto</span>
                  </button>
                )}

                {/* Solo marcar completado cuando no hay acción de ciclo de vida (recordatorio genérico) */}
                {eventoSeleccionado.recordatorioId && !eventoSeleccionado.completado &&
                  !(eventoSeleccionado.tipo === 'DESTETE' && eventoSeleccionado.partoId) &&
                  !(eventoSeleccionado.tipo === 'CONTROL_CELO' && eventoSeleccionado.servicioId) &&
                  !(eventoSeleccionado.tipo === 'ECOGRAFIA' && eventoSeleccionado.servicioId) &&
                  !(eventoSeleccionado.tipo === 'PARTO' && (eventoSeleccionado.gestacionId || eventoSeleccionado.madreId)) && (
                  <button
                    onClick={async () => {
                      try {
                        await api.patch(`/recordatorios/${eventoSeleccionado.recordatorioId}/completar`);
                        setEventoSeleccionado(null);
                        cargarEventos();
                      } catch (error) {
                        console.error('Error marcando recordatorio:', error);
                        alert('Error al marcar como completado');
                      }
                    }}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      backgroundColor: '#6b7280',
                      color: 'white',
                      border: 'none',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontWeight: '500',
                      fontSize: '0.875rem',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      gap: '0.5rem'
                    }}
                  >
                    <span>✓</span>
                    <span>Marcar como completado</span>
                  </button>
                )}

                {eventoSeleccionado.tipo === 'RECORDATORIO' && eventoSeleccionado.recordatorioId && (
                  <button
                    type="button"
                    onClick={() => eliminarRecordatorioCalendario(eventoSeleccionado.recordatorioId!)}
                    disabled={eliminandoRecordatorio}
                    style={{
                      width: '100%',
                      padding: '0.75rem',
                      marginTop: '0.5rem',
                      backgroundColor: '#fef2f2',
                      color: '#b91c1c',
                      border: '1px solid #fecaca',
                      borderRadius: '0.375rem',
                      cursor: eliminandoRecordatorio ? 'wait' : 'pointer',
                      fontWeight: '500',
                      fontSize: '0.875rem',
                    }}
                  >
                    {eliminandoRecordatorio ? 'Eliminando…' : 'Eliminar recordatorio'}
                  </button>
                )}
              </div>

              {/* Botón cerrar */}
              <button
                onClick={() => setEventoSeleccionado(null)}
                style={{
                  width: '100%',
                  padding: '0.75rem',
                  backgroundColor: '#6b7280',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                  fontWeight: '500',
                  marginTop: '1rem'
                }}
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Modal de acción desde calendario (registrar en ciclo de vida y marcar recordatorio) */}
      {modalAccion && (eventoParaModal || eventoSeleccionado) && (
        <AccionCalendarioPorcinosModal
          tipo={modalAccion}
          evento={(eventoParaModal ?? eventoSeleccionado) as any}
          recordatorioId={(eventoParaModal ?? eventoSeleccionado)?.recordatorioId ?? undefined}
          onSuccess={() => {
            setModalAccion(null);
            setEventoParaModal(null);
            setEventoSeleccionado(null);
            cargarEventos();
          }}
          onCancel={() => { setModalAccion(null); setEventoParaModal(null); }}
        />
      )}

      {/* Modal de Detalle de Recordatorio */}
      {mostrarModalDetalleRecordatorio && (
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
          zIndex: 1000,
          padding: '1rem'
        }}
        onClick={() => setMostrarModalDetalleRecordatorio(false)}
        >
          <div style={{
            backgroundColor: 'white',
            borderRadius: '0.75rem',
            maxWidth: '600px',
            width: '100%',
            maxHeight: '90vh',
            overflow: 'auto',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
          }}
          onClick={(e) => e.stopPropagation()}
          >
            {loadingRecordatorio ? (
              <div style={{ textAlign: 'center', padding: '3rem' }}>
                <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>⏳</div>
                <p style={{ color: '#6b7280', fontSize: '1rem' }}>Cargando detalles del recordatorio...</p>
              </div>
            ) : recordatorioSeleccionado ? (
              <>
                {/* Header del modal */}
                <div style={{
                  background: 'linear-gradient(135deg, #ec4899 0%, #be185d 100%)',
                  color: 'white',
                  padding: '1.5rem 2rem',
                  borderRadius: '0.75rem 0.75rem 0 0',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center'
                }}>
                  <div>
                    <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                      📌 {recordatorioSeleccionado.titulo || 'Recordatorio'}
                    </h2>
                    <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9, fontSize: '0.875rem' }}>
                      {obtenerNombreTipoRecordatorio(recordatorioSeleccionado.tipo || 'GENERAL')}
                    </p>
                  </div>
                  <button
                    onClick={() => setMostrarModalDetalleRecordatorio(false)}
                    style={{
                      background: 'rgba(255, 255, 255, 0.2)',
                      border: 'none',
                      color: 'white',
                      fontSize: '1.5rem',
                      cursor: 'pointer',
                      padding: '0.25rem 0.75rem',
                      borderRadius: '0.375rem',
                      fontWeight: 'bold'
                    }}
                  >
                    ✕
                  </button>
                </div>

                <div style={{ padding: '2rem' }}>
                  {/* Estado */}
                  <div style={{
                    backgroundColor: '#f9fafb',
                    padding: '1.25rem',
                    borderRadius: '0.5rem',
                    marginBottom: '1.5rem',
                    border: '2px solid',
                    borderColor: recordatorioSeleccionado.completado ? '#10b981' : '#f59e0b'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                      Estado
                    </div>
                    <div style={{
                      fontSize: '1.125rem',
                      fontWeight: 'bold',
                      color: recordatorioSeleccionado.completado ? '#10b981' : '#f59e0b'
                    }}>
                      {recordatorioSeleccionado.completado ? '✅ Completado' : '⏳ Pendiente'}
                    </div>
                  </div>

                  {/* Fecha */}
                  <div style={{
                    backgroundColor: '#f9fafb',
                    padding: '1.25rem',
                    borderRadius: '0.5rem',
                    marginBottom: '1.5rem',
                    border: '1px solid #e5e7eb'
                  }}>
                    <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                      📅 Fecha
                    </div>
                    <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                      {recordatorioSeleccionado.fecha ? new Date(recordatorioSeleccionado.fecha).toLocaleDateString('es-ES', { 
                        weekday: 'long', 
                        year: 'numeric', 
                        month: 'long', 
                        day: 'numeric' 
                      }) : 'N/A'}
                    </div>
                  </div>

                  {/* Descripción */}
                  {recordatorioSeleccionado.descripcion && (
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem',
                      borderLeft: '4px solid #ec4899'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '0.75rem', 
                        color: '#1f2937',
                        fontSize: '1rem',
                        fontWeight: '600'
                      }}>
                        📝 Descripción
                      </h3>
                      <p style={{ 
                        margin: 0, 
                        color: '#374151', 
                        lineHeight: '1.6',
                        fontSize: '0.9375rem'
                      }}>
                        {recordatorioSeleccionado.descripcion}
                      </p>
                    </div>
                  )}

                  {/* Información adicional */}
                  {(recordatorioSeleccionado.partoId || recordatorioSeleccionado.reproduccionId || recordatorioSeleccionado.sanidadId || recordatorioSeleccionado.alimentacionId) && (
                    <div style={{
                      backgroundColor: '#eff6ff',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem',
                      borderLeft: '4px solid #3b82f6'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '1rem', 
                        color: '#1f2937',
                        fontSize: '1rem',
                        fontWeight: '600'
                      }}>
                        🔗 Relacionado con
                      </h3>
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {recordatorioSeleccionado.partoId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Parto ID:</strong> {recordatorioSeleccionado.partoId}
                          </div>
                        )}
                        {recordatorioSeleccionado.reproduccionId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Reproducción ID:</strong> {recordatorioSeleccionado.reproduccionId}
                          </div>
                        )}
                        {recordatorioSeleccionado.sanidadId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Sanidad ID:</strong> {recordatorioSeleccionado.sanidadId}
                          </div>
                        )}
                        {recordatorioSeleccionado.alimentacionId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Alimentación ID:</strong> {recordatorioSeleccionado.alimentacionId}
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Botones de acción */}
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: '1rem', marginTop: '2rem' }}>
                    {!recordatorioSeleccionado.completado && (
                      <button
                        onClick={async () => {
                          try {
                            await api.patch(`/recordatorios/${recordatorioSeleccionado.id}/completar`);
                            setRecordatorioSeleccionado({ ...recordatorioSeleccionado, completado: true });
                            cargarEventos();
                            alert('Recordatorio marcado como completado');
                          } catch (error) {
                            console.error('Error marcando recordatorio:', error);
                            alert('Error al marcar el recordatorio como completado');
                          }
                        }}
                        style={{
                          flex: '1 1 140px',
                          padding: '0.75rem 1.5rem',
                          backgroundColor: '#10b981',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.375rem',
                          cursor: 'pointer',
                          fontWeight: '500',
                          fontSize: '0.875rem'
                        }}
                      >
                        ✅ Marcar como Completado
                      </button>
                    )}
                    <button
                      type="button"
                      onClick={() => eliminarRecordatorioCalendario(recordatorioSeleccionado.id)}
                      disabled={eliminandoRecordatorio}
                      style={{
                        flex: '1 1 140px',
                        padding: '0.75rem 1.5rem',
                        backgroundColor: '#fef2f2',
                        color: '#b91c1c',
                        border: '1px solid #fecaca',
                        borderRadius: '0.375rem',
                        cursor: eliminandoRecordatorio ? 'wait' : 'pointer',
                        fontWeight: '500',
                        fontSize: '0.875rem'
                      }}
                    >
                      {eliminandoRecordatorio ? 'Eliminando…' : 'Eliminar recordatorio'}
                    </button>
                    <button
                      onClick={() => setMostrarModalDetalleRecordatorio(false)}
                      style={{
                        flex: '1 1 140px',
                        padding: '0.75rem 1.5rem',
                        backgroundColor: '#6b7280',
                        color: 'white',
                        border: 'none',
                        borderRadius: '0.375rem',
                        cursor: 'pointer',
                        fontWeight: '500',
                        fontSize: '0.875rem'
                      }}
                    >
                      Cerrar
                    </button>
                  </div>
                </div>
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '3rem' }}>
                <div style={{ fontSize: '3rem', marginBottom: '1rem' }}>❌</div>
                <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>No se pudieron cargar los detalles del recordatorio</p>
                <button
                  onClick={() => setMostrarModalDetalleRecordatorio(false)}
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
                  Cerrar
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default CalendarioPorcinosDashboard;

// Inyectar estilos de animación
if (typeof document !== 'undefined' && !document.getElementById('calendario-porcinos-estilos')) {
  const style = document.createElement('style');
  style.id = 'calendario-porcinos-estilos';
  style.textContent = `
    @keyframes pulse {
      0%, 100% { opacity: 1; }
      50% { opacity: 0.7; }
    }
  `;
  document.head.appendChild(style);
}

