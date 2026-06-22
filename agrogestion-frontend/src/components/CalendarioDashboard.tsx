/* @refresh reset */
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { useAuth } from '../contexts/AuthContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import { useCurrencyContext } from '../contexts/CurrencyContext';
import { useCurrencyUpdate } from '../hooks/useCurrencyUpdate';
import EmpresaSelector from './EmpresaSelector';
import { laboresService } from '../services/apiServices';
import { Icon } from './icons';

interface EventoCalendario {
  id: string;
  tipo: 'LABOR' | 'COSECHA' | 'RECORDATORIO' | 'TAREA_RECURRENTE';
  titulo: string;
  descripcion?: string;
  fecha: string;
  fechaFin?: string;
  estado?: string;
  /** Spec SDD: labor vencida (fecha planificada &lt; hoy y planificada). */
  overdue?: boolean;
  fechaRealizacion?: string;
  laborId?: number;
  loteId?: number;
  loteNombre?: string;
  responsable?: string;
  usuarioId?: number;
  usuarioNombre?: string;
  tipoRecordatorio?: string;
  completado?: boolean;
  recordatorioId?: number;
  cultivo?: string;
  superficie?: number;
  /** Tareas recurrentes (SPEC calendario). */
  serieId?: number;
  tipoRepeticion?: string;
  cumplida?: boolean;
}

interface CalendarioDashboardProps {
  /** Calendario general (cultivos), avícola huevos o feedlot. */
  modoCalendario?: 'general' | 'avicolaHuevos' | 'feedlot';
}

interface RecordatorioForm {
  titulo: string;
  descripcion: string;
  fecha: string;
  tipo: string;
  laborId?: number;
  loteId?: number;
  loteAvicolaHuevoId?: number | '';
}

interface BotonVerEnLaboresProps {
  laborId?: number;
  onCerrar: () => void;
  variante: 'cabecera' | 'contenido';
}

/** Aísla useNavigate para evitar fallos de HMR en el componente principal. */
const BotonVerEnLabores: React.FC<BotonVerEnLaboresProps> = ({ laborId, onCerrar, variante }) => {
  const navigate = useNavigate();

  const irALabores = () => {
    onCerrar();
    navigate('/cultivos/labores', {
      state: laborId != null ? { abrirLaborId: laborId } : {},
    });
  };

  if (variante === 'cabecera') {
    return (
      <button
        type="button"
        onClick={irALabores}
        style={{
          marginTop: '0.75rem',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '0.35rem',
          padding: '0.4rem 0.75rem',
          backgroundColor: 'rgba(255, 255, 255, 0.25)',
          color: 'white',
          border: '1px solid rgba(255, 255, 255, 0.5)',
          borderRadius: '0.375rem',
          cursor: 'pointer',
          fontSize: '0.8125rem',
          fontWeight: '500',
        }}
      >
        <Icon name="ExternalLink" size={14} /> Ver detalle en Labores
      </button>
    );
  }

  return (
    <button
      type="button"
      onClick={irALabores}
      style={{
        marginTop: '0.5rem',
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.35rem',
        padding: '0.35rem 0.65rem',
        backgroundColor: '#f3f4f6',
        color: '#374151',
        border: '1px solid #e5e7eb',
        borderRadius: '0.375rem',
        cursor: 'pointer',
        fontSize: '0.8125rem',
        fontWeight: '500',
      }}
    >
      <Icon name="ExternalLink" size={14} /> Ver en Labores
    </button>
  );
};

const CalendarioDashboard: React.FC<CalendarioDashboardProps> = ({ modoCalendario = 'general' }) => {
  const { user, logout } = useAuth();
  const { empresaActiva, rolUsuario } = useEmpresa();
  const { formatCurrency, selectedCurrency, exchangeType, realRates, changeCurrency, changeExchangeType } = useCurrencyContext();
  useCurrencyUpdate(); // Forzar actualización cuando cambie la moneda

  const [fechaActual, setFechaActual] = useState(new Date());
  const [eventos, setEventos] = useState<EventoCalendario[]>([]);
  const [loading, setLoading] = useState(false);
  const [mostrarModalRecordatorio, setMostrarModalRecordatorio] = useState(false);
  const [mostrarModalLabor, setMostrarModalLabor] = useState(false);
  const [mostrarModalDetalleRecordatorio, setMostrarModalDetalleRecordatorio] = useState(false);
  const [eventoSeleccionado, setEventoSeleccionado] = useState<EventoCalendario | null>(null);
  const [laborSeleccionada, setLaborSeleccionada] = useState<any>(null);
  const [recordatorioSeleccionado, setRecordatorioSeleccionado] = useState<any>(null);
  const [loadingLabor, setLoadingLabor] = useState(false);
  const [loadingRecordatorio, setLoadingRecordatorio] = useState(false);
  const [fechaSeleccionada, setFechaSeleccionada] = useState<string>('');
  const [errorCalendario, setErrorCalendario] = useState<string | null>(null);
  const [actualizandoLabor, setActualizandoLabor] = useState(false);
  const [reprogramarFecha, setReprogramarFecha] = useState<string>('');
  const [formRecordatorio, setFormRecordatorio] = useState<RecordatorioForm>({
    titulo: '',
    descripcion: '',
    fecha: '',
    tipo: 'GENERAL',
    loteAvicolaHuevoId: '',
  });
  const [eliminandoRecordatorio, setEliminandoRecordatorio] = useState(false);
  const [eliminandoSerieRecurrente, setEliminandoSerieRecurrente] = useState(false);
  const [lotesHuevosOpciones, setLotesHuevosOpciones] = useState<{ id: number; nombre: string }[]>([]);
  const [mostrarModalTareaRecurrente, setMostrarModalTareaRecurrente] = useState(false);
  const [guardandoTareaRecurrente, setGuardandoTareaRecurrente] = useState(false);
  const [formTareaRecurrente, setFormTareaRecurrente] = useState({
    titulo: '',
    descripcion: '',
    fechaInicio: '',
    tipoRepeticion: 'SEMANAL',
  });

  const handleLogout = () => {
    logout();
    window.location.href = '/login';
  };

  // Obtener primer día del mes y último día del mes
  const primerDiaMes = new Date(fechaActual.getFullYear(), fechaActual.getMonth(), 1);
  const ultimoDiaMes = new Date(fechaActual.getFullYear(), fechaActual.getMonth() + 1, 0);
  const primerDiaSemana = primerDiaMes.getDay(); // 0 = Domingo, 1 = Lunes, etc.
  const diasEnMes = ultimoDiaMes.getDate();

  // Cargar eventos del calendario
  useEffect(() => {
    cargarEventos();
  }, [fechaActual, modoCalendario]);

  useEffect(() => {
    if (modoCalendario !== 'avicolaHuevos') {
      setLotesHuevosOpciones([]);
      return;
    }
    (async () => {
      try {
        const { data } = await api.get<Array<{ id: number; nombre: string }>>('/avicola-huevos/lotes');
        setLotesHuevosOpciones(Array.isArray(data) ? data.map((l) => ({ id: l.id, nombre: l.nombre })) : []);
      } catch {
        setLotesHuevosOpciones([]);
      }
    })();
  }, [modoCalendario]);

  const cargarEventos = async () => {
    setLoading(true);
    setErrorCalendario(null);
    try {
      const fechaInicio = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-01`;
      const fechaFin = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-${String(diasEnMes).padStart(2, '0')}`;

      const ruta =
        modoCalendario === 'avicolaHuevos'
          ? `/calendario/avicola-huevos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`
          : modoCalendario === 'feedlot'
          ? `/calendario/feedlot?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`
          : `/calendario/eventos?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`;

      const response = await api.get(ruta);
      if (response.data && response.data.todos) {
        const lista: EventoCalendario[] = response.data.todos;
        setEventos(lista);
      }
    } catch (error: unknown) {
      console.error('Error cargando eventos:', error);
      const status = error && typeof error === 'object' && 'response' in error
        ? (error as { response?: { status?: number } }).response?.status
        : null;
      const es503 = status === 503;
      const esRed = error && typeof error === 'object' && 'message' in error &&
        typeof (error as { message: string }).message === 'string' &&
        ((error as { message: string }).message.includes('Network') || (error as { message: string }).message.includes('Failed to fetch'));
      setErrorCalendario(
        es503 || esRed
          ? 'No se pudo conectar con el servidor. Compruebe que el backend esté en ejecución (puerto 8080).'
          : 'No se pudieron cargar los eventos del calendario. Intente de nuevo.'
      );
      setEventos([]);
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

  const obtenerColorEvento = (
    tipo: string,
    estado?: string,
    completado?: boolean,
    overdue?: boolean
  ): string => {
    if (tipo === 'LABOR') {
      if (overdue) return '#dc2626'; // Rojo: labor vencida (spec SDD)
      switch (estado) {
        case 'PLANIFICADA': return '#3b82f6'; // Azul
        case 'EN_PROGRESO': return '#f59e0b'; // Naranja
        case 'COMPLETADA': return '#10b981'; // Verde
        case 'CANCELADA': return '#6b7280'; // Gris
        case 'ANULADA': return '#ef4444'; // Rojo
        default: return '#3b82f6';
      }
    } else if (tipo === 'COSECHA') {
      return '#8b5cf6'; // Morado
    } else if (tipo === 'RECORDATORIO') {
      if (completado) return '#6b7280'; // Gris si está completado
      return '#ec4899'; // Rosa
    } else if (tipo === 'TAREA_RECURRENTE') {
      return completado ? '#6b7280' : '#d97706';
    }
    return '#6b7280';
  };

  const obtenerIconoEvento = (tipo: string): React.ReactNode => {
    switch (tipo) {
      case 'LABOR': return <Icon name="Wrench" size={16} />;
      case 'COSECHA': return <Icon name="Wheat" size={16} />;
      case 'RECORDATORIO': return <Icon name="Pin" size={16} />;
      case 'TAREA_RECURRENTE': return <Icon name="Calendar" size={16} />;
      default: return <Icon name="CalendarDays" size={16} />;
    }
  };

  const abrirModalRecordatorio = (dia: number) => {
    const fechaCompleta = `${fechaActual.getFullYear()}-${String(fechaActual.getMonth() + 1).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
    setFechaSeleccionada(fechaCompleta);
    setFormRecordatorio({
      titulo: '',
      descripcion: '',
      fecha: fechaCompleta,
      tipo: 'GENERAL',
      loteAvicolaHuevoId: '',
    });
    setMostrarModalRecordatorio(true);
  };

  const guardarRecordatorio = async () => {
    try {
      const cuerpo: Record<string, unknown> = {
        titulo: formRecordatorio.titulo,
        descripcion: formRecordatorio.descripcion,
        fecha: formRecordatorio.fecha,
        tipo: formRecordatorio.tipo,
        laborId: formRecordatorio.laborId || null,
        loteId: formRecordatorio.loteId || null,
      };
      if (modoCalendario === 'avicolaHuevos') {
        if (formRecordatorio.loteAvicolaHuevoId === '') {
          alert('Seleccioná un lote de postura para este recordatorio');
          return;
        }
        cuerpo.loteAvicolaHuevoId = formRecordatorio.loteAvicolaHuevoId;
      }
      await api.post('/recordatorios', cuerpo);
      setMostrarModalRecordatorio(false);
      cargarEventos();
    } catch (error) {
      console.error('Error guardando recordatorio:', error);
      alert('Error al guardar el recordatorio');
    }
  };

  const guardarTareaRecurrenteModulo = async () => {
    if (!formTareaRecurrente.titulo.trim() || !formTareaRecurrente.fechaInicio) {
      alert('Título y fecha de inicio son obligatorios');
      return;
    }
    setGuardandoTareaRecurrente(true);
    try {
      await api.post('/calendario/tareas-recurrentes', {
        titulo: formTareaRecurrente.titulo.trim(),
        descripcion: formTareaRecurrente.descripcion.trim() || null,
        fechaInicio: formTareaRecurrente.fechaInicio,
        fechaFin: null,
        tipoRepeticion: formTareaRecurrente.tipoRepeticion,
        ambitoCalendario:
          modoCalendario === 'avicolaHuevos'
            ? 'AVICOLA_HUEVOS'
            : modoCalendario === 'feedlot'
            ? 'FEEDLOT'
            : 'GENERAL',
      });
      setMostrarModalTareaRecurrente(false);
      setFormTareaRecurrente({ titulo: '', descripcion: '', fechaInicio: '', tipoRepeticion: 'SEMANAL' });
      cargarEventos();
    } catch (e) {
      console.error(e);
      alert('No se pudo crear la tarea recurrente');
    } finally {
      setGuardandoTareaRecurrente(false);
    }
  };

  const alternarCumplimientoTareaRecurrente = async () => {
    if (!eventoSeleccionado?.serieId || !eventoSeleccionado?.fecha) return;
    try {
      const cumplida = !eventoSeleccionado.cumplida;
      await api.patch(
        `/calendario/tareas-recurrentes/${eventoSeleccionado.serieId}/cumplimiento?fecha=${eventoSeleccionado.fecha}`,
        { cumplida }
      );
      setEventoSeleccionado(null);
      cargarEventos();
    } catch (e) {
      console.error(e);
      alert('No se pudo actualizar el cumplimiento');
    }
  };

  /** Elimina la serie completa: desaparecen todas las ocurrencias (pasadas y futuras) de esta tarea recurrente. */
  const eliminarSerieTareaRecurrente = async () => {
    if (!eventoSeleccionado?.serieId) return;
    if (
      !window.confirm(
        '¿Eliminar toda esta tarea recurrente? Se quitarán todas las fechas similares vinculadas a la misma serie. No se puede deshacer.'
      )
    ) {
      return;
    }
    setEliminandoSerieRecurrente(true);
    try {
      await api.delete(`/calendario/tareas-recurrentes/${eventoSeleccionado.serieId}`);
      setEventoSeleccionado(null);
      await cargarEventos();
    } catch (e) {
      console.error(e);
      alert(mensajeErrorApi(e));
    } finally {
      setEliminandoSerieRecurrente(false);
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

  const eliminarLaborCalendario = async (laborId: number) => {
    if (
      !window.confirm(
        '¿Eliminar esta labor del calendario? Si está planificada, se cancelará y se revertirán reservas de insumos cuando corresponda. Las labores ya ejecutadas pueden requerir anulación formal desde Cultivos.'
      )
    ) {
      return;
    }
    setActualizandoLabor(true);
    try {
      await laboresService.eliminar(laborId);
      setMostrarModalLabor(false);
      setLaborSeleccionada(null);
      if (eventoSeleccionado?.laborId === laborId) {
        setEventoSeleccionado(null);
      }
      await cargarEventos();
    } catch (error) {
      console.error('Error eliminando labor:', error);
      alert(mensajeErrorApi(error));
    } finally {
      setActualizandoLabor(false);
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

  const abrirModalLabor = async (laborId: number) => {
    setLoadingLabor(true);
    setMostrarModalLabor(true);
    setReprogramarFecha('');
    try {
      const labor = await laboresService.obtener(laborId);
      setLaborSeleccionada(labor);
    } catch (error) {
      console.error('Error cargando labor:', error);
      alert('Error al cargar los detalles de la labor');
      setMostrarModalLabor(false);
    } finally {
      setLoadingLabor(false);
    }
  };

  /** Spec SDD: marcar labor como realizada (PATCH). */
  const marcarLaborRealizada = async () => {
    if (!laborSeleccionada?.id) return;
    setActualizandoLabor(true);
    try {
      const hoy = new Date().toISOString().slice(0, 10);
      await laboresService.actualizarParcial(laborSeleccionada.id, { estado: 'realizada', fecha_realizacion: hoy });
      const labor = await laboresService.obtener(laborSeleccionada.id);
      setLaborSeleccionada(labor);
      cargarEventos();
    } catch (e) {
      console.error(e);
      alert('Error al marcar la labor como realizada');
    } finally {
      setActualizandoLabor(false);
    }
  };

  /** Spec SDD: reprogramar labor (PATCH fecha_planificada). */
  const reprogramarLabor = async () => {
    if (!laborSeleccionada?.id || !reprogramarFecha) return;
    setActualizandoLabor(true);
    try {
      await laboresService.actualizarParcial(laborSeleccionada.id, { fecha_planificada: reprogramarFecha });
      const labor = await laboresService.obtener(laborSeleccionada.id);
      setLaborSeleccionada(labor);
      setReprogramarFecha('');
      cargarEventos();
    } catch (e) {
      console.error(e);
      alert('Error al reprogramar la labor');
    } finally {
      setActualizandoLabor(false);
    }
  };

  /** Spec SDD: cancelar labor (PATCH estado=cancelada). */
  const cancelarLabor = async () => {
    if (!laborSeleccionada?.id) return;
    if (!window.confirm('¿Cancelar esta labor? Ya no aparecerá como pendiente en el calendario.')) return;
    setActualizandoLabor(true);
    try {
      await laboresService.actualizarParcial(laborSeleccionada.id, { estado: 'cancelada' });
      setMostrarModalLabor(false);
      setLaborSeleccionada(null);
      cargarEventos();
    } catch (e) {
      console.error(e);
      alert('Error al cancelar la labor');
    } finally {
      setActualizandoLabor(false);
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

  const obtenerNombreEstado = (estado: string): string => {
    const estados: { [key: string]: string } = {
      'PLANIFICADA': 'Planificada',
      'EN_PROGRESO': 'En Progreso',
      'COMPLETADA': 'Completada',
      'CANCELADA': 'Cancelada',
      'ANULADA': 'Anulada'
    };
    return estados[estado] || estado;
  };

  const obtenerNombreTipoRecordatorio = (tipo: string): string => {
    const tipos: { [key: string]: string } = {
      'GENERAL': 'General',
      'LABOR': 'Labor',
      'COSECHA': 'Cosecha',
      'MANTENIMIENTO': 'Mantenimiento',
      'INSUMO': 'Insumo',
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
          <Icon name="CalendarDays" size={32} style={{ marginRight: '0.5rem', display: 'inline-block', verticalAlign: 'middle' }} />{' '}
          {modoCalendario === 'avicolaHuevos'
            ? 'Calendario avícola huevos'
            : modoCalendario === 'feedlot'
            ? 'Calendario feedlot'
            : 'Calendario de labores y tareas'}
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
                // Forzar actualización inmediata
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
              <option value="ARS">💰 ARS (Pesos Argentinos)</option>
              <option value="oficial">
                💵 USD Oficial {realRates?.oficial ? `($${realRates.oficial.toFixed(2)})` : ''}
              </option>
              <option value="blue">
                💙 USD Blue {realRates?.blue ? `($${realRates.blue.toFixed(2)})` : ''}
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
              fontSize: '0.875rem'
            }}
          >
            Nuevo recordatorio
          </button>
          <button
            type="button"
            onClick={() => {
              const hoy = new Date().toISOString().slice(0, 10);
              setFormTareaRecurrente((f) => ({ ...f, fechaInicio: hoy }));
              setMostrarModalTareaRecurrente(true);
            }}
            style={{
              padding: '0.5rem 1rem',
              borderRadius: '0.375rem',
              border: '1px solid #d97706',
              backgroundColor: '#fffbeb',
              color: '#b45309',
              cursor: 'pointer',
              fontWeight: 500,
              fontSize: '0.875rem',
            }}
          >
            Nueva tarea recurrente
          </button>
        </div>
      </div>

      {/* Leyenda: el calendario es vista derivada de labores (spec SDD). Las tareas son las labores. */}
      <div style={{
        backgroundColor: 'white',
        padding: '1rem',
        borderRadius: '0.5rem',
        marginBottom: '1rem',
        boxShadow: '0 1px 3px rgba(0, 0, 0, 0.1)',
        display: 'flex',
        gap: '2rem',
        flexWrap: 'wrap'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#3b82f6', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>Labores / Tareas planificadas</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#f59e0b', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>En progreso</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#10b981', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>Labores realizadas</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#dc2626', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>Vencidas (pendientes)</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#8b5cf6', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>Cosechas estimadas</span>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <div style={{ width: '20px', height: '20px', backgroundColor: '#ec4899', borderRadius: '4px' }}></div>
          <span style={{ fontSize: '0.875rem' }}>Recordatorios generales</span>
        </div>
      </div>

      {/* Calendario */}
      {errorCalendario && (
        <div style={{
          padding: '1rem 1.25rem',
          marginBottom: '1rem',
          backgroundColor: '#fef2f2',
          border: '1px solid #fecaca',
          borderRadius: '0.5rem',
          color: '#b91c1c',
          fontSize: '0.875rem'
        }}>
          {errorCalendario}
        </div>
      )}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <Icon name="Loader2" size={32} className="animate-spin" />
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
                        key={evento.id}
                        style={{
                          backgroundColor: obtenerColorEvento(
                            evento.tipo,
                            evento.estado,
                            Boolean(evento.completado || evento.cumplida),
                            evento.overdue
                          ),
                          color: 'white',
                          padding: '0.25rem 0.5rem',
                          borderRadius: '0.25rem',
                          fontSize: '0.75rem',
                          cursor: 'pointer',
                          overflow: 'hidden',
                          textOverflow: 'ellipsis',
                          whiteSpace: 'nowrap'
                        }}
                        title={evento.titulo}
                        onClick={(e) => {
                          e.stopPropagation();
                          if (evento.tipo === 'LABOR' && evento.laborId) {
                            // Click simple: ir directo al detalle completo de la labor
                            abrirModalLabor(evento.laborId);
                          } else if (evento.tipo === 'RECORDATORIO' && evento.recordatorioId) {
                            // Click simple: ver detalle completo del recordatorio
                            abrirModalDetalleRecordatorio(evento.recordatorioId);
                          } else {
                            // Otros tipos: al menos mostrar un resumen básico
                            setEventoSeleccionado(evento);
                          }
                        }}
                        onDoubleClick={(e) => {
                          // Doble click: siempre mostrar un resumen del evento, sin importar el tipo
                          e.stopPropagation();
                          setEventoSeleccionado(evento);
                        }}
                      >
                        <span style={{ display: 'inline-flex', alignItems: 'center', marginRight: '0.25rem' }}>{obtenerIconoEvento(evento.tipo)}</span> {evento.titulo.length > 15 ? evento.titulo.substring(0, 15) + '...' : evento.titulo}
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

      {/* Modal de resumen rápido de evento (para doble click) */}
      {eventoSeleccionado && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1100,
            padding: '1rem'
          }}
          onClick={() => setEventoSeleccionado(null)}
        >
          <div
            style={{
              backgroundColor: 'white',
              borderRadius: '0.75rem',
              maxWidth: '480px',
              width: '100%',
              maxHeight: '80vh',
              overflow: 'auto',
              boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <div
              style={{
                padding: '1rem 1.5rem',
                borderBottom: '1px solid #e5e7eb',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}
            >
              <div>
                <h2
                  style={{
                    margin: 0,
                    fontSize: '1.1rem',
                    fontWeight: 600,
                    display: 'flex',
                    alignItems: 'center',
                    gap: '0.5rem'
                  }}
                >
                  {obtenerIconoEvento(eventoSeleccionado.tipo)}{' '}
                  {eventoSeleccionado.titulo}
                </h2>
                <p
                  style={{
                    margin: '0.25rem 0 0 0',
                    fontSize: '0.8rem',
                    color: '#6b7280'
                  }}
                >
                  {eventoSeleccionado.fecha}
                  {eventoSeleccionado.fechaFin
                    ? ` → ${eventoSeleccionado.fechaFin}`
                    : ''}
                </p>
              </div>
              <button
                onClick={() => setEventoSeleccionado(null)}
                style={{
                  border: 'none',
                  backgroundColor: 'transparent',
                  cursor: 'pointer',
                  fontSize: '1.25rem',
                  color: '#6b7280'
                }}
              >
                ×
              </button>
            </div>

            <div style={{ padding: '1.25rem 1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {eventoSeleccionado.descripcion && (
                <div>
                  <div
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      color: '#6b7280',
                      marginBottom: '0.25rem'
                    }}
                  >
                    Descripción
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#111827' }}>
                    {eventoSeleccionado.descripcion}
                  </div>
                </div>
              )}

              {eventoSeleccionado.loteNombre && (
                <div>
                  <div
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      color: '#6b7280',
                      marginBottom: '0.25rem'
                    }}
                  >
                    Lote
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#111827' }}>
                    {eventoSeleccionado.loteNombre}
                  </div>
                </div>
              )}

              {eventoSeleccionado.cultivo && (
                <div>
                  <div
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      color: '#6b7280',
                      marginBottom: '0.25rem'
                    }}
                  >
                    Cultivo
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#111827' }}>
                    {eventoSeleccionado.cultivo}
                    {eventoSeleccionado.superficie
                      ? ` • ${eventoSeleccionado.superficie} ha`
                      : ''}
                  </div>
                </div>
              )}

              {eventoSeleccionado.estado && (
                <div>
                  <div
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      color: '#6b7280',
                      marginBottom: '0.25rem'
                    }}
                  >
                    Estado
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#111827' }}>
                    {obtenerNombreEstado(eventoSeleccionado.estado)}
                  </div>
                </div>
              )}

              {eventoSeleccionado.tipo === 'TAREA_RECURRENTE' && eventoSeleccionado.serieId != null && (
                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '0.75rem', marginTop: '0.25rem', display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                  <button
                    type="button"
                    onClick={alternarCumplimientoTareaRecurrente}
                    style={{
                      width: '100%',
                      padding: '0.5rem 0.75rem',
                      fontSize: '0.875rem',
                      color: '#92400e',
                      backgroundColor: '#fffbeb',
                      border: '1px solid #fcd34d',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                    }}
                  >
                    {eventoSeleccionado.cumplida ? 'Marcar como pendiente' : 'Marcar como cumplida'}
                  </button>
                  <button
                    type="button"
                    onClick={eliminarSerieTareaRecurrente}
                    disabled={eliminandoSerieRecurrente}
                    style={{
                      width: '100%',
                      padding: '0.5rem 0.75rem',
                      fontSize: '0.875rem',
                      color: '#b91c1c',
                      backgroundColor: '#fef2f2',
                      border: '1px solid #fecaca',
                      borderRadius: '0.375rem',
                      cursor: eliminandoSerieRecurrente ? 'wait' : 'pointer',
                    }}
                  >
                    {eliminandoSerieRecurrente ? 'Eliminando…' : 'Eliminar toda la serie recurrente'}
                  </button>
                </div>
              )}

              {eventoSeleccionado.tipo === 'RECORDATORIO' && eventoSeleccionado.recordatorioId != null && (
                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '0.75rem', marginTop: '0.25rem' }}>
                  <button
                    type="button"
                    onClick={() => eliminarRecordatorioCalendario(eventoSeleccionado.recordatorioId!)}
                    disabled={eliminandoRecordatorio}
                    style={{
                      width: '100%',
                      padding: '0.5rem 0.75rem',
                      fontSize: '0.875rem',
                      color: '#b91c1c',
                      backgroundColor: '#fef2f2',
                      border: '1px solid #fecaca',
                      borderRadius: '0.375rem',
                      cursor: eliminandoRecordatorio ? 'wait' : 'pointer',
                    }}
                  >
                    {eliminandoRecordatorio ? 'Eliminando…' : 'Eliminar recordatorio'}
                  </button>
                </div>
              )}

              {eventoSeleccionado.tipo === 'LABOR' &&
                eventoSeleccionado.laborId != null &&
                eventoSeleccionado.estado === 'PLANIFICADA' && (
                <div style={{ borderTop: '1px solid #e5e7eb', paddingTop: '0.75rem', marginTop: '0.25rem' }}>
                  <button
                    type="button"
                    onClick={() => eliminarLaborCalendario(eventoSeleccionado.laborId!)}
                    disabled={actualizandoLabor}
                    style={{
                      width: '100%',
                      padding: '0.5rem 0.75rem',
                      fontSize: '0.875rem',
                      color: '#b91c1c',
                      backgroundColor: '#fef2f2',
                      border: '1px solid #fecaca',
                      borderRadius: '0.375rem',
                      cursor: actualizandoLabor ? 'wait' : 'pointer',
                    }}
                  >
                    {actualizandoLabor ? 'Eliminando…' : 'Eliminar labor'}
                  </button>
                </div>
              )}

              {eventoSeleccionado.responsable && (
                <div>
                  <div
                    style={{
                      fontSize: '0.75rem',
                      fontWeight: 600,
                      textTransform: 'uppercase',
                      color: '#6b7280',
                      marginBottom: '0.25rem'
                    }}
                  >
                    Responsable
                  </div>
                  <div style={{ fontSize: '0.9rem', color: '#111827' }}>
                    {eventoSeleccionado.responsable}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {mostrarModalTareaRecurrente && (
        <div
          style={{
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
          }}
          onClick={() => !guardandoTareaRecurrente && setMostrarModalTareaRecurrente(false)}
        >
          <div
            style={{
              backgroundColor: 'white',
              padding: '2rem',
              borderRadius: '0.5rem',
              width: '90%',
              maxWidth: '480px',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h2 style={{ marginTop: 0 }}>Nueva tarea recurrente</h2>
            <p style={{ fontSize: '0.85rem', color: '#6b7280' }}>
              {modoCalendario === 'avicolaHuevos'
                ? 'Visible solo en el calendario de avícola huevos.'
                : modoCalendario === 'feedlot'
                ? 'Visible solo en el calendario de feedlot.'
                : 'Visible en el calendario general.'}
            </p>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <label style={{ fontWeight: 500 }}>
                Título *
                <input
                  type="text"
                  value={formTareaRecurrente.titulo}
                  onChange={(e) => setFormTareaRecurrente({ ...formTareaRecurrente, titulo: e.target.value })}
                  style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </label>
              <label style={{ fontWeight: 500 }}>
                Descripción
                <textarea
                  value={formTareaRecurrente.descripcion}
                  onChange={(e) => setFormTareaRecurrente({ ...formTareaRecurrente, descripcion: e.target.value })}
                  style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', borderRadius: '0.375rem', border: '1px solid #d1d5db', minHeight: '64px' }}
                />
              </label>
              <label style={{ fontWeight: 500 }}>
                Primera fecha *
                <input
                  type="date"
                  value={formTareaRecurrente.fechaInicio}
                  onChange={(e) => setFormTareaRecurrente({ ...formTareaRecurrente, fechaInicio: e.target.value })}
                  style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </label>
              <label style={{ fontWeight: 500 }}>
                Repetición
                <select
                  value={formTareaRecurrente.tipoRepeticion}
                  onChange={(e) => setFormTareaRecurrente({ ...formTareaRecurrente, tipoRepeticion: e.target.value })}
                  style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                >
                  <option value="DIARIA">Diaria</option>
                  <option value="SEMANAL">Semanal</option>
                  <option value="MENSUAL">Mensual</option>
                  <option value="ANUAL">Anual</option>
                </select>
              </label>
              <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '0.5rem' }}>
                <button
                  type="button"
                  onClick={() => setMostrarModalTareaRecurrente(false)}
                  disabled={guardandoTareaRecurrente}
                  style={{ padding: '0.5rem 1rem', backgroundColor: '#6b7280', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer' }}
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  onClick={guardarTareaRecurrenteModulo}
                  disabled={guardandoTareaRecurrente}
                  style={{ padding: '0.5rem 1rem', backgroundColor: '#d97706', color: 'white', border: 'none', borderRadius: '0.375rem', cursor: 'pointer' }}
                >
                  {guardandoTareaRecurrente ? 'Guardando…' : 'Crear'}
                </button>
              </div>
            </div>
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
              Un recordatorio es <strong>una sola fecha</strong> en el calendario (avisos generales o vinculados a labores y lotes).
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
                  placeholder="Ej: Revisar riego del lote 5"
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
                  <option value="LABOR">Labor</option>
                  <option value="COSECHA">Cosecha</option>
                  <option value="MANTENIMIENTO">Mantenimiento</option>
                  <option value="INSUMO">Insumo</option>
                  <option value="REUNION">Reunión</option>
                  <option value="OTRO">Otro</option>
                </select>
              </div>
              {modoCalendario === 'avicolaHuevos' && (
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '500' }}>
                    Lote de postura *
                  </label>
                  <select
                    value={formRecordatorio.loteAvicolaHuevoId === '' ? '' : String(formRecordatorio.loteAvicolaHuevoId)}
                    onChange={(e) =>
                      setFormRecordatorio({
                        ...formRecordatorio,
                        loteAvicolaHuevoId: e.target.value === '' ? '' : Number(e.target.value),
                      })
                    }
                    style={{
                      width: '100%',
                      padding: '0.5rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                      fontSize: '0.875rem',
                    }}
                  >
                    <option value="">Seleccionar lote…</option>
                    {lotesHuevosOpciones.map((l) => (
                      <option key={l.id} value={l.id}>
                        {l.nombre}
                      </option>
                    ))}
                  </select>
                </div>
              )}
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

      {/* Modal de detalles de labor */}
      {mostrarModalLabor && (
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
        onClick={(e) => {
          if (e.target === e.currentTarget) {
            setMostrarModalLabor(false);
          }
        }}
        >
          <div style={{
            backgroundColor: 'white',
            borderRadius: '0.75rem',
            width: '100%',
            maxWidth: '900px',
            maxHeight: '90vh',
            overflowY: 'auto',
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)'
          }}>
            {loadingLabor ? (
              <div style={{ textAlign: 'center', padding: '3rem' }}>
                <Icon name="Loader2" size={48} className="animate-spin" />
                <p style={{ color: '#6b7280', fontSize: '1rem' }}>Cargando detalles de la labor...</p>
              </div>
            ) : laborSeleccionada ? (
              <>
                {/* Header del modal */}
                <div style={{
                  background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                  color: 'white',
                  padding: '1.5rem 2rem',
                  borderRadius: '0.75rem 0.75rem 0 0',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  flexWrap: 'wrap',
                  gap: '0.75rem'
                }}>
                  <div>
                    <h2 style={{ margin: 0, fontSize: '1.5rem', fontWeight: 'bold' }}>
                      <Icon name="Wrench" size={24} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> {laborSeleccionada.tipo || laborSeleccionada.tipoLabor || 'Labor'}
                    </h2>
                    <p style={{ margin: '0.5rem 0 0 0', opacity: 0.9, fontSize: '0.875rem' }}>
                      {laborSeleccionada.loteNombre || 'Sin lote asignado'}
                    </p>
                    <BotonVerEnLabores
                      laborId={laborSeleccionada?.id}
                      onCerrar={() => setMostrarModalLabor(false)}
                      variante="cabecera"
                    />
                  </div>
                  <button
                    onClick={() => setMostrarModalLabor(false)}
                    style={{
                      backgroundColor: 'rgba(255, 255, 255, 0.2)',
                      color: 'white',
                      border: 'none',
                      padding: '0.5rem',
                      borderRadius: '0.375rem',
                      cursor: 'pointer',
                      fontSize: '1.25rem',
                      width: '36px',
                      height: '36px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      transition: 'background-color 0.2s'
                    }}
                    onMouseEnter={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.3)'}
                    onMouseLeave={(e) => e.currentTarget.style.backgroundColor = 'rgba(255, 255, 255, 0.2)'}
                  >
                    <Icon name="X" size={24} />
                  </button>
                </div>

                <div style={{ padding: '2rem' }}>
                  {/* Información del Lote - Destacada - Siempre visible si hay información */}
                  {(laborSeleccionada.loteId || laborSeleccionada.loteNombre || laborSeleccionada.loteCampo || laborSeleccionada.loteSuperficie || laborSeleccionada.loteCultivo) && (
                    <div style={{
                      background: 'linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%)',
                      color: 'white',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '2rem',
                      boxShadow: '0 4px 6px rgba(0, 0, 0, 0.1)'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '1rem', 
                        fontSize: '1.125rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="Square" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Información del Lote
                      </h3>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '1rem' }}>
                        <div>
                          <div style={{ fontSize: '0.75rem', opacity: 0.9, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                            Nombre del Lote
                          </div>
                          <div style={{ fontSize: '1rem', fontWeight: 'bold' }}>
                            {laborSeleccionada.loteNombre || 'Sin asignar'}
                          </div>
                        </div>
                        {laborSeleccionada.loteCampo ? (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.9, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Campo
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500' }}>
                              {laborSeleccionada.loteCampo}
                            </div>
                          </div>
                        ) : (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.7, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Campo
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500', opacity: 0.7 }}>
                              No disponible
                            </div>
                          </div>
                        )}
                        {laborSeleccionada.loteSuperficie ? (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.9, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Superficie
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500' }}>
                              {laborSeleccionada.loteSuperficie} ha
                            </div>
                          </div>
                        ) : (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.7, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Superficie
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500', opacity: 0.7 }}>
                              No disponible
                            </div>
                          </div>
                        )}
                        {laborSeleccionada.loteCultivo ? (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.9, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Cultivo Actual
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500' }}>
                              {laborSeleccionada.loteCultivo}
                            </div>
                          </div>
                        ) : (
                          <div>
                            <div style={{ fontSize: '0.75rem', opacity: 0.7, marginBottom: '0.5rem', textTransform: 'uppercase', fontWeight: '600' }}>
                              Cultivo Actual
                            </div>
                            <div style={{ fontSize: '1rem', fontWeight: '500', opacity: 0.7 }}>
                              No disponible
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}

                  {/* Tarjetas principales en grid - adaptativo según elementos disponibles */}
                  <div style={{
                    display: 'grid',
                    gridTemplateColumns: `repeat(${laborSeleccionada.fechaFin ? 4 : 3}, 1fr)`,
                    gap: '1rem',
                    marginBottom: '2rem'
                  }}>
                    {/* Estado y vencida (spec SDD) */}
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.25rem',
                      borderRadius: '0.5rem',
                      border: '2px solid',
                      borderColor: obtenerColorEvento('LABOR', laborSeleccionada.estado, undefined, laborSeleccionada.overdue)
                    }}>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                        Estado
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                        <span style={{
                          fontSize: '1.125rem',
                          fontWeight: 'bold',
                          color: obtenerColorEvento('LABOR', laborSeleccionada.estado, undefined, laborSeleccionada.overdue)
                        }}>
                          {obtenerNombreEstado(laborSeleccionada.estado)}
                        </span>
                        {laborSeleccionada.overdue && (
                          <span style={{
                            fontSize: '0.75rem',
                            fontWeight: '600',
                            color: '#fff',
                            backgroundColor: '#dc2626',
                            padding: '0.2rem 0.5rem',
                            borderRadius: '0.25rem'
                          }}>
                            Vencida
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Responsable */}
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.25rem',
                      borderRadius: '0.5rem',
                      border: '1px solid #e5e7eb'
                    }}>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                        <Icon name="User" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Responsable
                      </div>
                      <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                        {laborSeleccionada.responsable || 'No asignado'}
                      </div>
                    </div>

                    {/* Fecha Inicio */}
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.25rem',
                      borderRadius: '0.5rem',
                      border: '1px solid #e5e7eb'
                    }}>
                      <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                        <Icon name="CalendarDays" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Fecha Inicio
                      </div>
                      <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                        {laborSeleccionada.fechaInicio ? new Date(laborSeleccionada.fechaInicio).toLocaleDateString('es-ES', { 
                          weekday: 'long', 
                          year: 'numeric', 
                          month: 'long', 
                          day: 'numeric' 
                        }) : 'N/A'}
                      </div>
                    </div>

                    {/* Fecha Fin */}
                    {laborSeleccionada.fechaFin && (
                      <div style={{
                        backgroundColor: '#f9fafb',
                        padding: '1.25rem',
                        borderRadius: '0.5rem',
                        border: '1px solid #e5e7eb'
                      }}>
                        <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                          <Icon name="CheckCircle" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Fecha Fin
                        </div>
                        <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                          {new Date(laborSeleccionada.fechaFin).toLocaleDateString('es-ES', {
                            weekday: 'long',
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          })}
                        </div>
                      </div>
                    )}

                    {/* Fecha realización (spec SDD) */}
                    {laborSeleccionada.fechaRealizacion && (
                      <div style={{
                        backgroundColor: '#ecfdf5',
                        padding: '1.25rem',
                        borderRadius: '0.5rem',
                        border: '1px solid #a7f3d0'
                      }}>
                        <div style={{ fontSize: '0.75rem', color: '#065f46', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                          <Icon name="CheckCircle" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Fecha realización
                        </div>
                        <div style={{ fontSize: '1rem', fontWeight: '500', color: '#047857' }}>
                          {new Date(laborSeleccionada.fechaRealizacion).toLocaleDateString('es-ES', {
                            weekday: 'long',
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          })}
                        </div>
                      </div>
                    )}

                    {/* Horas de Trabajo: solo se muestra si hay horas registradas (> 0) */}
                    {laborSeleccionada.horasTrabajo != null && Number(laborSeleccionada.horasTrabajo) > 0 && (
                      <div style={{
                        backgroundColor: '#f9fafb',
                        padding: '1.25rem',
                        borderRadius: '0.5rem',
                        border: '1px solid #e5e7eb'
                      }}>
                        <div style={{ fontSize: '0.75rem', color: '#6b7280', textTransform: 'uppercase', fontWeight: '600', marginBottom: '0.5rem' }}>
                          <Icon name="Clock" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Horas Trabajo
                        </div>
                        <div style={{ fontSize: '1rem', fontWeight: '500', color: '#1f2937' }}>
                          {laborSeleccionada.horasTrabajo} horas
                        </div>
                      </div>
                    )}
                  </div>

                  {/* Acciones rápidas (spec SDD): solo si la labor está planificada */}
                  {laborSeleccionada.estado === 'PLANIFICADA' && (
                    <div style={{
                      backgroundColor: '#eff6ff',
                      padding: '1.25rem',
                      borderRadius: '0.5rem',
                      marginBottom: '2rem',
                      border: '1px solid #bfdbfe'
                    }}>
                      <div style={{ fontSize: '0.875rem', fontWeight: '600', color: '#1e40af', marginBottom: '0.75rem' }}>
                        Acciones
                      </div>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '0.75rem', alignItems: 'center' }}>
                        <button
                          type="button"
                          onClick={marcarLaborRealizada}
                          disabled={actualizandoLabor}
                          style={{
                            padding: '0.5rem 1rem',
                            backgroundColor: '#10b981',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.375rem',
                            cursor: actualizandoLabor ? 'not-allowed' : 'pointer',
                            fontSize: '0.875rem',
                            fontWeight: '500'
                          }}
                        >
                          {actualizandoLabor ? '...' : 'Marcar como realizada'}
                        </button>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <input
                            type="date"
                            value={reprogramarFecha}
                            onChange={(e) => setReprogramarFecha(e.target.value)}
                            style={{
                              padding: '0.5rem',
                              border: '1px solid #d1d5db',
                              borderRadius: '0.375rem',
                              fontSize: '0.875rem'
                            }}
                          />
                          <button
                            type="button"
                            onClick={reprogramarLabor}
                            disabled={actualizandoLabor || !reprogramarFecha}
                            style={{
                              padding: '0.5rem 1rem',
                              backgroundColor: '#3b82f6',
                              color: 'white',
                              border: 'none',
                              borderRadius: '0.375rem',
                              cursor: actualizandoLabor || !reprogramarFecha ? 'not-allowed' : 'pointer',
                              fontSize: '0.875rem',
                              fontWeight: '500'
                            }}
                          >
                            Reprogramar
                          </button>
                        </div>
                        <button
                          type="button"
                          onClick={cancelarLabor}
                          disabled={actualizandoLabor}
                          style={{
                            padding: '0.5rem 1rem',
                            backgroundColor: '#6b7280',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.375rem',
                            cursor: actualizandoLabor ? 'not-allowed' : 'pointer',
                            fontSize: '0.875rem',
                            fontWeight: '500'
                          }}
                        >
                          Cancelar labor
                        </button>
                        <button
                          type="button"
                          onClick={() => laborSeleccionada?.id != null && eliminarLaborCalendario(laborSeleccionada.id)}
                          disabled={actualizandoLabor}
                          style={{
                            padding: '0.5rem 1rem',
                            backgroundColor: '#fef2f2',
                            color: '#b91c1c',
                            border: '1px solid #fecaca',
                            borderRadius: '0.375rem',
                            cursor: actualizandoLabor ? 'not-allowed' : 'pointer',
                            fontSize: '0.875rem',
                            fontWeight: '500'
                          }}
                        >
                          Eliminar labor
                        </button>
                      </div>
                    </div>
                  )}

                  {/* Costo Total destacado: solo se muestra si hay costo (> 0) */}
                  {laborSeleccionada.costoTotal != null && Number(laborSeleccionada.costoTotal) > 0 && (
                    <div style={{
                      background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
                      color: 'white',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '2rem',
                      textAlign: 'center'
                    }}>
                      <div style={{ fontSize: '0.875rem', opacity: 0.9, marginBottom: '0.5rem' }}>
                        Costo Total de la Labor
                      </div>
                      <div style={{ fontSize: '2rem', fontWeight: 'bold' }}>
                        {formatCurrency(laborSeleccionada.costoTotal)}
                      </div>
                    </div>
                  )}

                  {/* Descripción */}
                  {laborSeleccionada.descripcion && (
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem',
                      borderLeft: '4px solid #3b82f6'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '0.75rem', 
                        color: '#1f2937',
                        fontSize: '1rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="FileText" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Descripción
                      </h3>
                      <p style={{ 
                        margin: 0, 
                        color: '#374151', 
                        lineHeight: '1.6',
                        fontSize: '0.9375rem'
                      }}>
                        {laborSeleccionada.descripcion}
                      </p>
                    </div>
                  )}

                  {/* Observaciones */}
                  {laborSeleccionada.observaciones && (
                    <div style={{
                      backgroundColor: '#fffbeb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem',
                      borderLeft: '4px solid #f59e0b'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '0.75rem', 
                        color: '#1f2937',
                        fontSize: '1rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="Lightbulb" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Observaciones
                      </h3>
                      <p style={{ 
                        margin: 0, 
                        color: '#374151', 
                        lineHeight: '1.6',
                        fontSize: '0.9375rem'
                      }}>
                        {laborSeleccionada.observaciones}
                      </p>
                    </div>
                  )}

                  {/* Desglose de costos */}
                  {(laborSeleccionada.costoInsumos || laborSeleccionada.costoMaquinaria || laborSeleccionada.costoManoObra) && (
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '1rem', 
                        color: '#1f2937',
                        fontSize: '1.125rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="DollarSign" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Desglose de Costos
                      </h3>
                      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '1rem' }}>
                        {laborSeleccionada.costoInsumos && (
                          <div style={{
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '0.375rem',
                            border: '1px solid #e5e7eb'
                          }}>
                            <div style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Insumos</div>
                            <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                              {formatCurrency(laborSeleccionada.costoInsumos)}
                            </div>
                          </div>
                        )}
                        {laborSeleccionada.costoMaquinaria && (
                          <div style={{
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '0.375rem',
                            border: '1px solid #e5e7eb'
                          }}>
                            <div style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Maquinaria</div>
                            <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                              {formatCurrency(laborSeleccionada.costoMaquinaria)}
                            </div>
                          </div>
                        )}
                        {laborSeleccionada.costoManoObra && (
                          <div style={{
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '0.375rem',
                            border: '1px solid #e5e7eb'
                          }}>
                            <div style={{ fontSize: '0.75rem', color: '#6b7280', marginBottom: '0.5rem' }}>Mano de Obra</div>
                            <div style={{ fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
                              {formatCurrency(laborSeleccionada.costoManoObra)}
                            </div>
                          </div>
                        )}
                      </div>
                    </div>
                  )}


                  {/* Insumos Usados */}
                  {laborSeleccionada.insumosUsados && laborSeleccionada.insumosUsados.length > 0 && (
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem',
                      marginBottom: '1.5rem'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '1rem', 
                        color: '#1f2937',
                        fontSize: '1.125rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="Flask" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Insumos Utilizados ({laborSeleccionada.insumosUsados.length})
                      </h3>
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {laborSeleccionada.insumosUsados.map((insumo: any, index: number) => (
                          <div key={index} style={{
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '0.5rem',
                            border: '1px solid #e5e7eb',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}>
                            <div style={{ flex: 1 }}>
                              <div style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.25rem', fontSize: '1rem' }}>
                                {insumo.insumoNombre || insumo.nombre || `Insumo #${insumo.idInsumo || index + 1}`}
                              </div>
                              <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                {insumo.insumoTipo && <span style={{ marginRight: '0.5rem' }}>{insumo.insumoTipo}</span>}
                                <span>
                                  Cantidad: {insumo.cantidadUsada || insumo.cantidad || '0'} {insumo.unidadMedida || ''}
                                  {insumo.costoUnitario && ` • Unitario: ${formatCurrency(insumo.costoUnitario)}`}
                                </span>
                              </div>
                              {insumo.observaciones && (
                                <div style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem', fontStyle: 'italic' }}>
                                  {insumo.observaciones}
                                </div>
                              )}
                            </div>
                            {insumo.costoTotal && (
                              <div style={{ fontSize: '1.125rem', fontWeight: '600', color: '#059669', marginLeft: '1rem', minWidth: '120px', textAlign: 'right' }}>
                                {formatCurrency(insumo.costoTotal)}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Maquinaria - Separada por tipo - Siempre mostrar */}
                  <div style={{
                    backgroundColor: '#f9fafb',
                    padding: '1.5rem',
                    borderRadius: '0.5rem',
                    marginBottom: '1.5rem'
                  }}>
                    <h3 style={{ 
                      marginTop: 0, 
                      marginBottom: '1rem', 
                      color: '#1f2937',
                      fontSize: '1.125rem',
                      fontWeight: '600'
                    }}>
                      <Icon name="Tractor" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Maquinaria Utilizada {laborSeleccionada.maquinarias && laborSeleccionada.maquinarias.length > 0 ? `(${laborSeleccionada.maquinarias.length})` : ''}
                    </h3>
                    
                    {laborSeleccionada.maquinarias && laborSeleccionada.maquinarias.length > 0 ? (
                      <>
                        {/* Maquinaria Propia */}
                        {laborSeleccionada.maquinarias.filter((maq: any) => maq.tipo === 'PROPIA' || !maq.tipo || maq.tipo === null).length > 0 && (
                          <div style={{ marginBottom: '1.5rem' }}>
                            <div style={{ fontSize: '0.875rem', color: '#6b7280', fontWeight: '600', marginBottom: '0.75rem', textTransform: 'uppercase' }}>
                              <Icon name="Home" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Maquinaria Propia
                            </div>
                            <div style={{ display: 'grid', gap: '0.75rem' }}>
                              {laborSeleccionada.maquinarias
                                .filter((maq: any) => maq.tipo === 'PROPIA' || !maq.tipo || maq.tipo === null)
                                .map((maq: any, index: number) => (
                                <div key={index} style={{
                                  backgroundColor: 'white',
                                  padding: '1rem',
                                  borderRadius: '0.5rem',
                                  border: '1px solid #e5e7eb',
                                  display: 'flex',
                                  justifyContent: 'space-between',
                                  alignItems: 'center'
                                }}>
                                  <div style={{ flex: 1 }}>
                                    <div style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.25rem', fontSize: '1rem' }}>
                                      {maq.descripcion || maq.maquinariaNombre || 'Maquinaria'}
                                    </div>
                                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                      {maq.horasUso && `Horas de uso: ${maq.horasUso}`}
                                      {maq.horasUso && maq.kilometrosRecorridos && ' • '}
                                      {maq.kilometrosRecorridos && `Kilómetros: ${maq.kilometrosRecorridos} km`}
                                      {(!maq.horasUso && !maq.kilometrosRecorridos) && <span style={{ fontStyle: 'italic', color: '#9ca3af' }}>Sin detalles adicionales</span>}
                                    </div>
                                    {maq.observaciones && (
                                      <div style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem', fontStyle: 'italic' }}>
                                        {maq.observaciones}
                                      </div>
                                    )}
                                  </div>
                                  {maq.costo && (
                                    <div style={{ fontSize: '1.125rem', fontWeight: '600', color: '#059669', marginLeft: '1rem', minWidth: '120px', textAlign: 'right' }}>
                                      {formatCurrency(maq.costo)}
                                    </div>
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        )}

                        {/* Maquinaria Alquilada */}
                        {laborSeleccionada.maquinarias.filter((maq: any) => maq.tipo === 'ALQUILADA').length > 0 && (
                          <div>
                            <div style={{ fontSize: '0.875rem', color: '#6b7280', fontWeight: '600', marginBottom: '0.75rem', textTransform: 'uppercase' }}>
                              <Icon name="Building2" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Maquinaria Alquilada
                            </div>
                            <div style={{ display: 'grid', gap: '0.75rem' }}>
                              {laborSeleccionada.maquinarias
                                .filter((maq: any) => maq.tipo === 'ALQUILADA')
                                .map((maq: any, index: number) => (
                                <div key={index} style={{
                                  backgroundColor: 'white',
                                  padding: '1rem',
                                  borderRadius: '0.5rem',
                                  border: '1px solid #e5e7eb',
                                  display: 'flex',
                                  justifyContent: 'space-between',
                                  alignItems: 'center'
                                }}>
                                  <div style={{ flex: 1 }}>
                                    <div style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.25rem', fontSize: '1rem' }}>
                                      {maq.descripcion || maq.maquinariaNombre || 'Maquinaria'}
                                    </div>
                                    <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                      {maq.proveedor && <span style={{ fontWeight: '500' }}>Proveedor: {maq.proveedor}</span>}
                                      {maq.proveedor && maq.horasUso && ' • '}
                                      {maq.horasUso && `Horas: ${maq.horasUso}`}
                                      {maq.kilometrosRecorridos && (maq.proveedor || maq.horasUso) && ' • '}
                                      {maq.kilometrosRecorridos && `${maq.kilometrosRecorridos} km`}
                                      {(!maq.proveedor && !maq.horasUso && !maq.kilometrosRecorridos) && <span style={{ fontStyle: 'italic', color: '#9ca3af' }}>Sin detalles adicionales</span>}
                                    </div>
                                    {maq.observaciones && (
                                      <div style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem', fontStyle: 'italic' }}>
                                        {maq.observaciones}
                                      </div>
                                    )}
                                  </div>
                                  {maq.costo && (
                                    <div style={{ fontSize: '1.125rem', fontWeight: '600', color: '#059669', marginLeft: '1rem', minWidth: '120px', textAlign: 'right' }}>
                                      {formatCurrency(maq.costo)}
                                    </div>
                                  )}
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </>
                    ) : (
                      <div style={{ margin: 0 }}>
                        <p style={{ margin: 0, color: '#9ca3af', fontSize: '0.875rem', fontStyle: 'italic' }}>
                          No se registró maquinaria para esta labor.
                        </p>
                        <p style={{ margin: '0.5rem 0 0 0', fontSize: '0.8125rem', color: '#6b7280' }}>
                          Puede ver y editar el detalle completo de esta labor (insumos, costos, fechas) en el módulo Labores.
                        </p>
                        <BotonVerEnLabores
                          laborId={laborSeleccionada?.id}
                          onCerrar={() => setMostrarModalLabor(false)}
                          variante="contenido"
                        />
                      </div>
                    )}
                  </div>

                  {/* Mano de Obra */}
                  {laborSeleccionada.manoObra && laborSeleccionada.manoObra.length > 0 && (
                    <div style={{
                      backgroundColor: '#f9fafb',
                      padding: '1.5rem',
                      borderRadius: '0.5rem'
                    }}>
                      <h3 style={{ 
                        marginTop: 0, 
                        marginBottom: '1rem', 
                        color: '#1f2937',
                        fontSize: '1.125rem',
                        fontWeight: '600'
                      }}>
                        <Icon name="Users" size={20} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> Mano de Obra ({laborSeleccionada.manoObra.length})
                      </h3>
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {laborSeleccionada.manoObra.map((mo: any, index: number) => (
                          <div key={index} style={{
                            backgroundColor: 'white',
                            padding: '1rem',
                            borderRadius: '0.5rem',
                            border: '1px solid #e5e7eb',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}>
                            <div>
                              <div style={{ fontWeight: '600', color: '#1f2937', marginBottom: '0.25rem' }}>
                                {mo.descripcion}
                              </div>
                              <div style={{ fontSize: '0.875rem', color: '#6b7280' }}>
                                {mo.cantidadPersonas && `${mo.cantidadPersonas} persona(s)`}
                                {mo.horasTrabajo && ` • ${mo.horasTrabajo} hora(s)`}
                                {mo.proveedor && ` • ${mo.proveedor}`}
                              </div>
                            </div>
                            {mo.costoTotal && (
                              <div style={{ fontSize: '1rem', fontWeight: '600', color: '#059669' }}>
                                {formatCurrency(mo.costoTotal)}
                              </div>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div style={{ textAlign: 'center', padding: '3rem' }}>
                <Icon name="XCircle" size={48} />
                <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>No se pudieron cargar los detalles de la labor</p>
                <button
                  onClick={() => setMostrarModalLabor(false)}
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
                <Icon name="Loader2" size={48} className="animate-spin" />
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
                      <Icon name="Pin" size={24} style={{ marginRight: '0.5rem', display: 'inline-block' }} /> {recordatorioSeleccionado.titulo || 'Recordatorio'}
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
                    <Icon name="X" size={24} />
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
                      {recordatorioSeleccionado.completado ? <><Icon name="CheckCircle" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Completado</> : <><Icon name="Clock" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Pendiente</>}
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
                      <Icon name="CalendarDays" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Fecha
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
                        <Icon name="FileText" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Descripción
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
                  {(recordatorioSeleccionado.laborId || recordatorioSeleccionado.loteId) && (
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
                        <Icon name="Link" size={16} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Relacionado con
                      </h3>
                      <div style={{ display: 'grid', gap: '0.75rem' }}>
                        {recordatorioSeleccionado.laborId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Labor ID:</strong> {recordatorioSeleccionado.laborId}
                          </div>
                        )}
                        {recordatorioSeleccionado.loteId && (
                          <div style={{ fontSize: '0.875rem', color: '#374151' }}>
                            <strong>Lote ID:</strong> {recordatorioSeleccionado.loteId}
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
                        <Icon name="CheckCircle" size={18} style={{ marginRight: '0.25rem', display: 'inline-block' }} /> Marcar como Completado
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
                <Icon name="XCircle" size={48} />
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

export default CalendarioDashboard;

