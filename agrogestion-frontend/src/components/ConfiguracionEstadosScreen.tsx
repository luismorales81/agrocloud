import React, { useState, useEffect } from 'react';
import { useLocation, useSearchParams } from 'react-router-dom';
import { configuracionEstadosService } from '../services/apiServices';
import { useEmpresa } from '../contexts/EmpresaContext';
import { SemanticIcon } from './icons';
import { Icon } from '../core/components/Icon';

interface TipoCultivo {
  id: number;
  nombre: string;
  descripcion?: string;
  esPlantilla: boolean;
  activo: boolean;
}

interface EstadoLoteConfig {
  id?: number;
  nombre: string;
  descripcion?: string;
  color: string;
  icono?: string;
  orden: number;
  esEstadoInicial: boolean;
  esEstadoFinal: boolean;
  activo: boolean;
  tipoCultivoId?: number;
  empresaId?: number;
}

interface TransicionEstadoConfig {
  id?: number;
  estadoOrigenId: number;
  estadoDestinoId: number;
  requiereMotivo: boolean;
  activo: boolean;
  tipoCultivoId?: number;
  empresaId?: number;
}

interface TareaPorEstadoConfig {
  id?: number;
  estadoId: number;
  tipoLabor: string;
  nombreTarea: string;
  descripcion?: string;
  esObligatoria: boolean;
  orden: number;
  activo: boolean;
  tipoCultivoId?: number;
  empresaId?: number;
}

// Función helper para validar y limpiar iconos
const validarIcono = (icono: string | undefined | null): string => {
  if (!icono || !icono.trim()) return '📋';
  
  // Limpiar caracteres no válidos y espacios
  const iconoLimpio = icono.trim();
  
  // Si contiene caracteres de reemplazo (?) o caracteres no válidos, usar fallback
  if (iconoLimpio.includes('?') || iconoLimpio.length === 0) {
    return '📋';
  }
  
  // Limitar a un solo carácter/emoji para evitar problemas
  // Los emojis pueden ser de 1-4 bytes en UTF-8
  return iconoLimpio.length <= 4 ? iconoLimpio : '📋';
};

const ConfiguracionEstadosScreen: React.FC = () => {
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const { empresaId } = useEmpresa();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  // Estados principales
  const [tiposCultivo, setTiposCultivo] = useState<TipoCultivo[]>([]);
  const [tipoCultivoSeleccionado, setTipoCultivoSeleccionado] = useState<number | null>(null);
  const [estados, setEstados] = useState<EstadoLoteConfig[]>([]);
  const [transiciones, setTransiciones] = useState<TransicionEstadoConfig[]>([]);
  const [tareas, setTareas] = useState<Map<number, TareaPorEstadoConfig[]>>(new Map());

  // Estados para modales
  const [showModalEstado, setShowModalEstado] = useState(false);
  const [showModalTransicion, setShowModalTransicion] = useState(false);
  const [showModalTarea, setShowModalTarea] = useState(false);
  const [estadoEditando, setEstadoEditando] = useState<EstadoLoteConfig | null>(null);
  const [transicionEditando, setTransicionEditando] = useState<TransicionEstadoConfig | null>(null);
  const [tareaEditando, setTareaEditando] = useState<TareaPorEstadoConfig | null>(null);
  const [estadoSeleccionadoParaTarea, setEstadoSeleccionadoParaTarea] = useState<number | null>(null);

  // Formularios
  const [formEstado, setFormEstado] = useState<EstadoLoteConfig>({
    nombre: '',
    descripcion: '',
    color: '#10b981',
    icono: '📋',
    orden: 0,
    esEstadoInicial: false,
    esEstadoFinal: false,
    activo: true,
  });

  const [formTransicion, setFormTransicion] = useState<TransicionEstadoConfig>({
    estadoOrigenId: 0,
    estadoDestinoId: 0,
    requiereMotivo: false,
    activo: true,
  });

  const [formTarea, setFormTarea] = useState<TareaPorEstadoConfig>({
    estadoId: 0,
    tipoLabor: '',
    nombreTarea: '',
    descripcion: '',
    esObligatoria: false,
    orden: 0,
    activo: true,
  });

  // Tab activa
  const [tabActiva, setTabActiva] = useState<'estados' | 'transiciones' | 'tareas'>('estados');

  // Importación Excel
  const [showModalImportar, setShowModalImportar] = useState(false);
  const [archivoImportar, setArchivoImportar] = useState<File | null>(null);
  const [nombreTipoImportar, setNombreTipoImportar] = useState('');
  const [importarEnExistente, setImportarEnExistente] = useState(false);
  const [importando, setImportando] = useState(false);

  useEffect(() => {
    cargarTiposCultivo();
  }, [location.key, searchParams.toString()]);

  useEffect(() => {
    if (tipoCultivoSeleccionado) {
      cargarConfiguracion();
    }
  }, [tipoCultivoSeleccionado, empresaId]);

  const obtenerIdTipoPreferido = (): number | null => {
    const st = location.state as { tipoCultivoId?: number } | null | undefined;
    if (st?.tipoCultivoId != null && Number.isFinite(Number(st.tipoCultivoId))) {
      return Number(st.tipoCultivoId);
    }
    const q = searchParams.get('tipoCultivoId');
    if (q != null && q !== '' && !Number.isNaN(Number(q))) {
      return Number(q);
    }
    return null;
  };

  const cargarTiposCultivo = async () => {
    try {
      setLoading(true);
      const data = await configuracionEstadosService.obtenerTiposCultivo();
      setTiposCultivo(data);
      const preferido = obtenerIdTipoPreferido();
      setTipoCultivoSeleccionado((prev) => {
        if (preferido != null && data.some((t: TipoCultivo) => t.id === preferido)) {
          return preferido;
        }
        if (prev != null && data.some((t: TipoCultivo) => t.id === prev)) {
          return prev;
        }
        return data.length > 0 ? data[0].id : null;
      });
    } catch (err: any) {
      setError('Error al cargar tipos de cultivo: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const cargarConfiguracion = async () => {
    if (!tipoCultivoSeleccionado) return;

    try {
      setLoading(true);
      setError(null);

      const [estadosData, transicionesData] = await Promise.all([
        configuracionEstadosService.obtenerEstados(tipoCultivoSeleccionado, empresaId || undefined),
        configuracionEstadosService.obtenerTransiciones(tipoCultivoSeleccionado, empresaId || undefined),
      ]);

      setEstados(estadosData);
      setTransiciones(transicionesData);

      // Cargar tareas para cada estado
      const tareasMap = new Map<number, TareaPorEstadoConfig[]>();
      for (const estado of estadosData) {
        if (estado.id) {
          const tareasData = await configuracionEstadosService.obtenerTareas(estado.id, empresaId || undefined);
          tareasMap.set(estado.id, tareasData);
        }
      }
      setTareas(tareasMap);
    } catch (err: any) {
      setError('Error al cargar configuración: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleCrearTipoCultivo = async () => {
    const nombre = prompt('Ingrese el nombre del nuevo tipo de cultivo:');
    if (!nombre) return;

    try {
      setLoading(true);
      await configuracionEstadosService.crearTipoCultivo({
        nombre,
        descripcion: '',
        esPlantilla: false,
        activo: true,
      });
      setSuccess('Tipo de cultivo creado exitosamente');
      await cargarTiposCultivo();
    } catch (err: any) {
      setError('Error al crear tipo de cultivo: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleCopiarPlantilla = async () => {
    if (!tipoCultivoSeleccionado) return;
    if (!confirm('¿Desea copiar la plantilla global a su empresa? Esto creará una copia personalizable.')) return;

    try {
      setLoading(true);
      await configuracionEstadosService.copiarPlantillaAEmpresa(tipoCultivoSeleccionado, empresaId || undefined);
      setSuccess('Plantilla copiada exitosamente');
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al copiar plantilla: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleDescargarPlantilla = async () => {
    try {
      const blob = await configuracionEstadosService.descargarPlantillaExcel();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'plantilla_configuracion_estados.xlsx';
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
      setSuccess('Plantilla descargada correctamente');
    } catch (err: any) {
      setError('Error al descargar plantilla: ' + (err.message || 'Error desconocido'));
    }
  };

  const handleImportarExcel = async () => {
    if (!archivoImportar) {
      setError('Seleccione un archivo Excel');
      return;
    }
    if (!importarEnExistente && !nombreTipoImportar.trim()) {
      setError('Ingrese el nombre del tipo de cultivo para crear uno nuevo');
      return;
    }
    if (importarEnExistente && !tipoCultivoSeleccionado) {
      setError('Seleccione un tipo de cultivo para actualizar');
      return;
    }

    try {
      setImportando(true);
      setError(null);
      const resultado = await configuracionEstadosService.importarDesdeExcel(
        archivoImportar,
        importarEnExistente ? undefined : nombreTipoImportar.trim(),
        importarEnExistente ? tipoCultivoSeleccionado ?? undefined : undefined,
        empresaId || undefined
      );

      if (resultado.exito) {
        setSuccess(
          `Importación exitosa: ${resultado.estadosCreados} estados, ${resultado.transicionesCreadas} transiciones, ${resultado.tareasCreadas} tareas`
        );
        setShowModalImportar(false);
        setArchivoImportar(null);
        setNombreTipoImportar('');
        await cargarTiposCultivo();
        if (resultado.tipoCultivoId) {
          setTipoCultivoSeleccionado(resultado.tipoCultivoId);
        }
      } else if (resultado.errores && resultado.errores.length > 0) {
        setError(
          resultado.errores.map((e: { pestana: string; fila: number; mensaje: string }) =>
            `[${e.pestana} fila ${e.fila}]: ${e.mensaje}`
          ).join('; ')
        );
      }
    } catch (err: any) {
      const data = err.response?.data;
      if (data?.errores && Array.isArray(data.errores)) {
        setError(
          data.errores.map((e: { pestana: string; fila: number; mensaje: string }) =>
            `[${e.pestana} fila ${e.fila}]: ${e.mensaje}`
          ).join('; ')
        );
      } else {
        setError('Error al importar: ' + (data?.message || err.message || 'Error desconocido'));
      }
    } finally {
      setImportando(false);
    }
  };

  const handleAbrirModalEstado = (estado?: EstadoLoteConfig) => {
    if (estado) {
      setEstadoEditando(estado);
      setFormEstado(estado);
    } else {
      setEstadoEditando(null);
      setFormEstado({
        nombre: '',
        descripcion: '',
        color: '#10b981',
        icono: '📋',
        orden: estados.length + 1,
        esEstadoInicial: false,
        esEstadoFinal: false,
        activo: true,
      });
    }
    setShowModalEstado(true);
  };

  const handleGuardarEstado = async () => {
    if (!tipoCultivoSeleccionado) return;
    if (!formEstado.nombre.trim()) {
      setError('El nombre del estado es obligatorio');
      return;
    }

    try {
      setLoading(true);
      if (estadoEditando?.id) {
        await configuracionEstadosService.actualizarEstado(estadoEditando.id, formEstado);
        setSuccess('Estado actualizado exitosamente');
      } else {
        await configuracionEstadosService.crearEstado(formEstado, tipoCultivoSeleccionado, empresaId || undefined);
        setSuccess('Estado creado exitosamente');
      }
      setShowModalEstado(false);
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al guardar estado: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleEliminarEstado = async (id: number) => {
    if (!confirm('¿Está seguro de eliminar este estado?')) return;

    try {
      setLoading(true);
      await configuracionEstadosService.eliminarEstado(id);
      setSuccess('Estado eliminado exitosamente');
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al eliminar estado: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleReordenarEstados = async (direccion: 'arriba' | 'abajo', index: number) => {
    if (!estados[index]) return;

    const nuevoOrden = [...estados];
    if (direccion === 'arriba' && index > 0) {
      [nuevoOrden[index], nuevoOrden[index - 1]] = [nuevoOrden[index - 1], nuevoOrden[index]];
    } else if (direccion === 'abajo' && index < nuevoOrden.length - 1) {
      [nuevoOrden[index], nuevoOrden[index + 1]] = [nuevoOrden[index + 1], nuevoOrden[index]];
    }

    // Actualizar orden
    const estadosReordenados = nuevoOrden.map((estado, idx) => ({
      ...estado,
      orden: idx + 1,
    }));

    try {
      setLoading(true);
      await configuracionEstadosService.reordenarEstados(estadosReordenados.map(e => e.id!).filter(Boolean) as number[]);
      setEstados(estadosReordenados);
      setSuccess('Estados reordenados exitosamente');
    } catch (err: any) {
      setError('Error al reordenar estados: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirModalTransicion = (transicion?: TransicionEstadoConfig) => {
    if (transicion) {
      setTransicionEditando(transicion);
      setFormTransicion(transicion);
    } else {
      setTransicionEditando(null);
      setFormTransicion({
        estadoOrigenId: estados[0]?.id || 0,
        estadoDestinoId: estados[1]?.id || 0,
        requiereMotivo: false,
        activo: true,
      });
    }
    setShowModalTransicion(true);
  };

  const handleGuardarTransicion = async () => {
    if (!tipoCultivoSeleccionado) return;
    if (formTransicion.estadoOrigenId === formTransicion.estadoDestinoId) {
      setError('El estado origen y destino no pueden ser el mismo');
      return;
    }

    try {
      setLoading(true);
      if (transicionEditando?.id) {
        // Actualizar transición (si el backend lo soporta)
        setError('La actualización de transiciones aún no está implementada');
      } else {
        await configuracionEstadosService.crearTransicion(formTransicion, tipoCultivoSeleccionado, empresaId || undefined);
        setSuccess('Transición creada exitosamente');
      }
      setShowModalTransicion(false);
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al guardar transición: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleEliminarTransicion = async (id: number) => {
    if (!confirm('¿Está seguro de eliminar esta transición?')) return;

    try {
      setLoading(true);
      await configuracionEstadosService.eliminarTransicion(id);
      setSuccess('Transición eliminada exitosamente');
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al eliminar transición: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleAbrirModalTarea = (estadoId: number, tarea?: TareaPorEstadoConfig) => {
    if (tarea) {
      setTareaEditando(tarea);
      setFormTarea(tarea);
    } else {
      setTareaEditando(null);
      setFormTarea({
        estadoId,
        tipoLabor: '',
        nombreTarea: '',
        descripcion: '',
        esObligatoria: false,
        orden: (tareas.get(estadoId)?.length || 0) + 1,
        activo: true,
      });
    }
    setEstadoSeleccionadoParaTarea(estadoId);
    setShowModalTarea(true);
  };

  const handleGuardarTarea = async () => {
    if (!tipoCultivoSeleccionado) return;
    if (!formTarea.tipoLabor.trim() || !formTarea.nombreTarea.trim()) {
      setError('El tipo de labor y el nombre de la tarea son obligatorios');
      return;
    }

    try {
      setLoading(true);
      if (tareaEditando?.id) {
        await configuracionEstadosService.actualizarTarea(tareaEditando.id, formTarea);
        setSuccess('Tarea actualizada exitosamente');
      } else {
        await configuracionEstadosService.crearTarea(formTarea, tipoCultivoSeleccionado, empresaId || undefined);
        setSuccess('Tarea creada exitosamente');
      }
      setShowModalTarea(false);
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al guardar tarea: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const handleEliminarTarea = async (id: number) => {
    if (!confirm('¿Está seguro de eliminar esta tarea?')) return;

    try {
      setLoading(true);
      await configuracionEstadosService.eliminarTarea(id);
      setSuccess('Tarea eliminada exitosamente');
      await cargarConfiguracion();
    } catch (err: any) {
      setError('Error al eliminar tarea: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  const tiposLaborDisponibles = [
    'SIEMBRA',
    'FERTILIZACION',
    'APLICACION_AGROQUIMICO',
    'RIEGO',
    'COSECHA',
    'LABRANZA',
    'DESMALEZADO',
    'CONTROL_PLAGAS',
    'OTRO',
  ];

  return (
    <div style={{ padding: '2rem', maxWidth: '1400px', margin: '0 auto' }}>
      <div style={{ marginBottom: '2rem' }}>
        <h1 style={{ fontSize: '2rem', fontWeight: 'bold', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <SemanticIcon semanticName="settings" size={32} />
          Configuración de Estados y Tareas
        </h1>
        <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>
          Configure los estados de lotes, transiciones y tareas disponibles para cada tipo de cultivo.
          Puede usar plantillas globales o personalizar por empresa.
        </p>
      </div>

      {error && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#fee2e2',
          color: '#dc2626',
          borderRadius: '0.5rem',
          marginBottom: '1rem',
        }}>
          {error}
          <button
            onClick={() => setError(null)}
            style={{ marginLeft: '1rem', color: '#dc2626', textDecoration: 'underline' }}
          >
            ✕
          </button>
        </div>
      )}

      {success && (
        <div style={{
          padding: '1rem',
          backgroundColor: '#d1fae5',
          color: '#059669',
          borderRadius: '0.5rem',
          marginBottom: '1rem',
        }}>
          {success}
          <button
            onClick={() => setSuccess(null)}
            style={{ marginLeft: '1rem', color: '#059669', textDecoration: 'underline' }}
          >
            ✕
          </button>
        </div>
      )}

      {/* Selector de tipo de cultivo */}
      <div style={{
        padding: '1.5rem',
        backgroundColor: '#f9fafb',
        borderRadius: '0.5rem',
        marginBottom: '2rem',
        display: 'flex',
        gap: '1rem',
        alignItems: 'center',
        flexWrap: 'wrap',
      }}>
        <label style={{ fontWeight: '600' }}>Tipo de Cultivo:</label>
        <select
          value={tipoCultivoSeleccionado || ''}
          onChange={(e) => setTipoCultivoSeleccionado(Number(e.target.value))}
          style={{
            padding: '0.5rem 1rem',
            borderRadius: '0.375rem',
            border: '1px solid #d1d5db',
            fontSize: '1rem',
            minWidth: '200px',
          }}
        >
          <option value="">Seleccione un tipo de cultivo</option>
          {tiposCultivo.map(tipo => (
            <option key={tipo.id} value={tipo.id}>
              {tipo.nombre} {tipo.esPlantilla ? '(Plantilla)' : '(Personalizado)'}
            </option>
          ))}
        </select>
        <button
          onClick={handleCrearTipoCultivo}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#10b981',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '600',
          }}
        >
          + Nuevo Tipo
        </button>
        {tipoCultivoSeleccionado && (
          <button
            onClick={handleCopiarPlantilla}
            style={{
              padding: '0.5rem 1rem',
              backgroundColor: '#3b82f6',
              color: 'white',
              border: 'none',
              borderRadius: '0.375rem',
              cursor: 'pointer',
              fontWeight: '600',
            }}
          >
            <Icon name="Copy" size={16} style={{ marginRight: '4px' }} /> Copiar Plantilla
          </button>
        )}
        <button
          onClick={handleDescargarPlantilla}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#8b5cf6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '600',
          }}
        >
          <Icon name="Download" size={16} style={{ marginRight: '4px' }} /> Descargar plantilla Excel
        </button>
        <button
          onClick={() => {
            setShowModalImportar(true);
            setArchivoImportar(null);
            setNombreTipoImportar('');
            setImportarEnExistente(false);
          }}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#059669',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontWeight: '600',
          }}
        >
          <Icon name="Upload" size={16} style={{ marginRight: '4px' }} /> Importar desde Excel
        </button>
      </div>

      {/* Modal Importar Excel */}
      {showModalImportar && (
        <div style={{
          position: 'fixed',
          inset: 0,
          backgroundColor: 'rgba(0,0,0,0.5)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 1000,
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '480px',
            width: '90%',
            boxShadow: '0 10px 40px rgba(0,0,0,0.2)',
          }}>
            <h3 style={{ marginBottom: '1.5rem', fontSize: '1.25rem' }}>Importar configuración desde Excel</h3>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Archivo Excel (.xlsx)</label>
              <input
                type="file"
                accept=".xlsx"
                onChange={(e) => setArchivoImportar(e.target.files?.[0] || null)}
                style={{ width: '100%', padding: '0.5rem' }}
              />
            </div>
            <div style={{ marginBottom: '1rem' }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={importarEnExistente}
                  onChange={(e) => setImportarEnExistente(e.target.checked)}
                />
                Actualizar tipo de cultivo existente
              </label>
            </div>
            {!importarEnExistente ? (
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Nombre del nuevo tipo de cultivo</label>
                <input
                  type="text"
                  value={nombreTipoImportar}
                  onChange={(e) => setNombreTipoImportar(e.target.value)}
                  placeholder="Ej: Soja, Maíz..."
                  style={{ width: '100%', padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
                />
              </div>
            ) : (
              <p style={{ marginBottom: '1rem', color: '#6b7280', fontSize: '0.875rem' }}>
                Se reemplazará la configuración de: {tiposCultivo.find(t => t.id === tipoCultivoSeleccionado)?.nombre || 'Seleccionado'}
              </p>
            )}
            <div style={{ display: 'flex', gap: '0.5rem', justifyContent: 'flex-end', marginTop: '1.5rem' }}>
              <button
                onClick={() => setShowModalImportar(false)}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: '#e5e7eb',
                  color: '#374151',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                }}
              >
                Cancelar
              </button>
              <button
                onClick={handleImportarExcel}
                disabled={importando || !archivoImportar}
                style={{
                  padding: '0.5rem 1rem',
                  backgroundColor: importando ? '#9ca3af' : '#059669',
                  color: 'white',
                  border: 'none',
                  borderRadius: '0.375rem',
                  cursor: importando ? 'not-allowed' : 'pointer',
                }}
              >
                {importando ? 'Importando...' : 'Importar'}
              </button>
            </div>
          </div>
        </div>
      )}

      {loading && (
        <div style={{ textAlign: 'center', padding: '2rem' }}>
          <Icon name="Loader" size={32} style={{ marginBottom: '1rem' }} />
          <p>Cargando...</p>
        </div>
      )}

      {!loading && tipoCultivoSeleccionado && (
        <>
          {/* Tabs */}
          <div style={{
            display: 'flex',
            gap: '0.5rem',
            marginBottom: '2rem',
            borderBottom: '2px solid #e5e7eb',
          }}>
            <button
              onClick={() => setTabActiva('estados')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: tabActiva === 'estados' ? '#3b82f6' : 'transparent',
                color: tabActiva === 'estados' ? 'white' : '#6b7280',
                border: 'none',
                borderBottom: tabActiva === 'estados' ? '2px solid #3b82f6' : '2px solid transparent',
                cursor: 'pointer',
                fontWeight: '600',
                borderRadius: '0.5rem 0.5rem 0 0',
              }}
            >
              <Icon name="Clipboard" size={16} style={{ marginRight: '4px' }} /> Estados ({estados.length})
            </button>
            <button
              onClick={() => setTabActiva('transiciones')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: tabActiva === 'transiciones' ? '#3b82f6' : 'transparent',
                color: tabActiva === 'transiciones' ? 'white' : '#6b7280',
                border: 'none',
                borderBottom: tabActiva === 'transiciones' ? '2px solid #3b82f6' : '2px solid transparent',
                cursor: 'pointer',
                fontWeight: '600',
                borderRadius: '0.5rem 0.5rem 0 0',
              }}
            >
              <Icon name="RefreshCcw" size={16} style={{ marginRight: '4px' }} /> Transiciones ({transiciones.length})
            </button>
            <button
              onClick={() => setTabActiva('tareas')}
              style={{
                padding: '0.75rem 1.5rem',
                backgroundColor: tabActiva === 'tareas' ? '#3b82f6' : 'transparent',
                color: tabActiva === 'tareas' ? 'white' : '#6b7280',
                border: 'none',
                borderBottom: tabActiva === 'tareas' ? '2px solid #3b82f6' : '2px solid transparent',
                cursor: 'pointer',
                fontWeight: '600',
                borderRadius: '0.5rem 0.5rem 0 0',
              }}
            >
              <Icon name="CheckCircle" size={16} style={{ marginRight: '4px' }} /> Tareas
            </button>
          </div>

          {/* Tab Estados */}
          {tabActiva === 'estados' && (
            <div>
              <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Estados del Lote</h2>
                <button
                  onClick={() => handleAbrirModalEstado()}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  + Agregar Estado
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {estados.map((estado, index) => (
                  <div
                    key={estado.id}
                    style={{
                      padding: '1rem',
                      backgroundColor: 'white',
                      border: '1px solid #e5e7eb',
                      borderRadius: '0.5rem',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '1rem',
                    }}
                  >
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', flex: 1 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span style={{ fontSize: '1.5rem' }}>{validarIcono(estado.icono)}</span>
                        <span style={{
                          fontWeight: '600',
                          fontSize: '1.1rem',
                        }}>
                          {estado.nombre}
                        </span>
                        {estado.esEstadoInicial && (
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: '#dbeafe',
                            color: '#1e40af',
                            borderRadius: '0.25rem',
                            fontSize: '0.75rem',
                            fontWeight: '600',
                          }}>
                            Inicial
                          </span>
                        )}
                        {estado.esEstadoFinal && (
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: '#fce7f3',
                            color: '#9f1239',
                            borderRadius: '0.25rem',
                            fontSize: '0.75rem',
                            fontWeight: '600',
                          }}>
                            Final
                          </span>
                        )}
                      </div>
                      {estado.descripcion && (
                        <p style={{ color: '#6b7280', fontSize: '0.875rem' }}>{estado.descripcion}</p>
                      )}
                    </div>
                    <div style={{
                      width: '30px',
                      height: '30px',
                      borderRadius: '50%',
                      backgroundColor: estado.color || '#10b981',
                    }} />
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                      <button
                        onClick={() => handleReordenarEstados('arriba', index)}
                        disabled={index === 0}
                        style={{
                          padding: '0.25rem 0.5rem',
                          backgroundColor: index === 0 ? '#e5e7eb' : '#3b82f6',
                          color: index === 0 ? '#9ca3af' : 'white',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: index === 0 ? 'not-allowed' : 'pointer',
                        }}
                      >
                        ↑
                      </button>
                      <button
                        onClick={() => handleReordenarEstados('abajo', index)}
                        disabled={index === estados.length - 1}
                        style={{
                          padding: '0.25rem 0.5rem',
                          backgroundColor: index === estados.length - 1 ? '#e5e7eb' : '#3b82f6',
                          color: index === estados.length - 1 ? '#9ca3af' : 'white',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: index === estados.length - 1 ? 'not-allowed' : 'pointer',
                        }}
                      >
                        ↓
                      </button>
                      <button
                        onClick={() => handleAbrirModalEstado(estado)}
                        style={{
                          padding: '0.25rem 0.5rem',
                          backgroundColor: '#f59e0b',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                        }}
                      >
                        <Icon name="Pencil" size={16} />
                      </button>
                      <button
                        onClick={() => estado.id && handleEliminarEstado(estado.id)}
                        style={{
                          padding: '0.25rem 0.5rem',
                          backgroundColor: '#ef4444',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                        }}
                      >
                        <Icon name="Trash2" size={16} />
                      </button>
                    </div>
                  </div>
                ))}
                {estados.length === 0 && (
                  <div style={{
                    padding: '2rem',
                    textAlign: 'center',
                    color: '#6b7280',
                    backgroundColor: '#f9fafb',
                    borderRadius: '0.5rem',
                  }}>
                    No hay estados configurados. Agregue el primer estado.
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Tab Transiciones */}
          {tabActiva === 'transiciones' && (
            <div>
              <div style={{ marginBottom: '1rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Transiciones entre Estados</h2>
                <button
                  onClick={() => handleAbrirModalTransicion()}
                  style={{
                    padding: '0.5rem 1rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  + Agregar Transición
                </button>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                {transiciones.map((transicion) => {
                  const estadoOrigen = estados.find(e => e.id === transicion.estadoOrigenId);
                  const estadoDestino = estados.find(e => e.id === transicion.estadoDestinoId);
                  return (
                    <div
                      key={transicion.id}
                      style={{
                        padding: '1rem',
                        backgroundColor: 'white',
                        border: '1px solid #e5e7eb',
                        borderRadius: '0.5rem',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <span style={{ fontWeight: '600' }}>{estadoOrigen?.nombre || 'Estado origen'}</span>
                        <span>→</span>
                        <span style={{ fontWeight: '600' }}>{estadoDestino?.nombre || 'Estado destino'}</span>
                        {transicion.requiereMotivo && (
                          <span style={{
                            padding: '0.25rem 0.5rem',
                            backgroundColor: '#fef3c7',
                            color: '#92400e',
                            borderRadius: '0.25rem',
                            fontSize: '0.75rem',
                          }}>
                            Requiere motivo
                          </span>
                        )}
                      </div>
                      <button
                        onClick={() => transicion.id && handleEliminarTransicion(transicion.id)}
                        style={{
                          padding: '0.25rem 0.5rem',
                          backgroundColor: '#ef4444',
                          color: 'white',
                          border: 'none',
                          borderRadius: '0.25rem',
                          cursor: 'pointer',
                        }}
                      >
                        <Icon name="Trash2" size={16} />
                      </button>
                    </div>
                  );
                })}
                {transiciones.length === 0 && (
                  <div style={{
                    padding: '2rem',
                    textAlign: 'center',
                    color: '#6b7280',
                    backgroundColor: '#f9fafb',
                    borderRadius: '0.5rem',
                  }}>
                    No hay transiciones configuradas. Agregue la primera transición.
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Tab Tareas */}
          {tabActiva === 'tareas' && (
            <div>
              <div style={{ marginBottom: '1rem' }}>
                <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>Tareas por Estado</h2>
                <p style={{ color: '#6b7280', fontSize: '0.875rem' }}>
                  Configure las tareas disponibles para cada estado del lote.
                </p>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
                {estados.map((estado) => {
                  const tareasEstado = tareas.get(estado.id!) || [];
                  return (
                    <div
                      key={estado.id}
                      style={{
                        padding: '1.5rem',
                        backgroundColor: 'white',
                        border: '1px solid #e5e7eb',
                        borderRadius: '0.5rem',
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                          <span style={{ fontSize: '1.5rem' }}>
                            {estado.icono && estado.icono.trim() && /[\u{1F300}-\u{1F9FF}]|[\u{2600}-\u{26FF}]|[\u{2700}-\u{27BF}]/u.test(estado.icono) 
                              ? estado.icono 
                              : '📋'}
                          </span>
                          <h3 style={{ fontSize: '1.25rem', fontWeight: 'bold' }}>{estado.nombre}</h3>
                          <span style={{ color: '#6b7280' }}>({tareasEstado.length} tareas)</span>
                        </div>
                        <button
                          onClick={() => estado.id && handleAbrirModalTarea(estado.id)}
                          style={{
                            padding: '0.5rem 1rem',
                            backgroundColor: '#10b981',
                            color: 'white',
                            border: 'none',
                            borderRadius: '0.375rem',
                            cursor: 'pointer',
                            fontWeight: '600',
                          }}
                        >
                          + Agregar Tarea
                        </button>
                      </div>

                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                        {tareasEstado.map((tarea) => (
                          <div
                            key={tarea.id}
                            style={{
                              padding: '0.75rem',
                              backgroundColor: '#f9fafb',
                              border: '1px solid #e5e7eb',
                              borderRadius: '0.375rem',
                              display: 'flex',
                              justifyContent: 'space-between',
                              alignItems: 'center',
                            }}
                          >
                            <div>
                              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <span style={{ fontWeight: '600' }}>{tarea.nombreTarea}</span>
                                <span style={{ color: '#6b7280', fontSize: '0.875rem' }}>
                                  ({tarea.tipoLabor})
                                </span>
                                {tarea.esObligatoria && (
                                  <span style={{
                                    padding: '0.25rem 0.5rem',
                                    backgroundColor: '#fee2e2',
                                    color: '#dc2626',
                                    borderRadius: '0.25rem',
                                    fontSize: '0.75rem',
                                    fontWeight: '600',
                                  }}>
                                    Obligatoria
                                  </span>
                                )}
                              </div>
                              {tarea.descripcion && (
                                <p style={{ color: '#6b7280', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                                  {tarea.descripcion}
                                </p>
                              )}
                            </div>
                            <div style={{ display: 'flex', gap: '0.5rem' }}>
                              <button
                                onClick={() => estado.id && handleAbrirModalTarea(estado.id, tarea)}
                                style={{
                                  padding: '0.25rem 0.5rem',
                                  backgroundColor: '#f59e0b',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '0.25rem',
                                  cursor: 'pointer',
                                }}
                              >
                                <Icon name="Pencil" size={16} />
                              </button>
                              <button
                                onClick={() => tarea.id && handleEliminarTarea(tarea.id)}
                                style={{
                                  padding: '0.25rem 0.5rem',
                                  backgroundColor: '#ef4444',
                                  color: 'white',
                                  border: 'none',
                                  borderRadius: '0.25rem',
                                  cursor: 'pointer',
                                }}
                              >
                                <Icon name="Trash2" size={16} />
                              </button>
                            </div>
                          </div>
                        ))}
                        {tareasEstado.length === 0 && (
                          <div style={{
                            padding: '1rem',
                            textAlign: 'center',
                            color: '#6b7280',
                            backgroundColor: '#f9fafb',
                            borderRadius: '0.375rem',
                          }}>
                            No hay tareas configuradas para este estado.
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </>
      )}

      {/* Modal Estado */}
      {showModalEstado && (
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
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%',
            maxHeight: '90vh',
            overflow: 'auto',
          }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              {estadoEditando ? 'Editar Estado' : 'Nuevo Estado'}
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Nombre *</label>
                <input
                  type="text"
                  value={formEstado.nombre}
                  onChange={(e) => setFormEstado({ ...formEstado, nombre: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Descripción</label>
                <textarea
                  value={formEstado.descripcion || ''}
                  onChange={(e) => setFormEstado({ ...formEstado, descripcion: e.target.value })}
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Color</label>
                  <input
                    type="color"
                    value={formEstado.color}
                    onChange={(e) => setFormEstado({ ...formEstado, color: e.target.value })}
                    style={{
                      width: '100%',
                      height: '40px',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                    }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Icono</label>
                  <input
                    type="text"
                    value={formEstado.icono || ''}
                    onChange={(e) => setFormEstado({ ...formEstado, icono: e.target.value })}
                    placeholder="📋"
                    style={{
                      width: '100%',
                      padding: '0.5rem',
                      border: '1px solid #d1d5db',
                      borderRadius: '0.375rem',
                    }}
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: '1rem' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={formEstado.esEstadoInicial}
                    onChange={(e) => setFormEstado({ ...formEstado, esEstadoInicial: e.target.checked })}
                  />
                  <span>Estado Inicial</span>
                </label>
                <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                  <input
                    type="checkbox"
                    checked={formEstado.esEstadoFinal}
                    onChange={(e) => setFormEstado({ ...formEstado, esEstadoFinal: e.target.checked })}
                  />
                  <span>Estado Final</span>
                </label>
              </div>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button
                  onClick={handleGuardarEstado}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Guardar
                </button>
                <button
                  onClick={() => setShowModalEstado(false)}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Transición */}
      {showModalTransicion && (
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
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%',
          }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              Nueva Transición
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Estado Origen</label>
                <select
                  value={formTransicion.estadoOrigenId}
                  onChange={(e) => setFormTransicion({ ...formTransicion, estadoOrigenId: Number(e.target.value) })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                >
                  {estados.map(estado => (
                    <option key={estado.id} value={estado.id}>
                      {estado.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Estado Destino</label>
                <select
                  value={formTransicion.estadoDestinoId}
                  onChange={(e) => setFormTransicion({ ...formTransicion, estadoDestinoId: Number(e.target.value) })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                >
                  {estados.map(estado => (
                    <option key={estado.id} value={estado.id}>
                      {estado.nombre}
                    </option>
                  ))}
                </select>
              </div>

              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formTransicion.requiereMotivo}
                  onChange={(e) => setFormTransicion({ ...formTransicion, requiereMotivo: e.target.checked })}
                />
                <span>Requiere motivo para cambiar</span>
              </label>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button
                  onClick={handleGuardarTransicion}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Guardar
                </button>
                <button
                  onClick={() => setShowModalTransicion(false)}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Modal Tarea */}
      {showModalTarea && (
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
        }}>
          <div style={{
            backgroundColor: 'white',
            padding: '2rem',
            borderRadius: '0.5rem',
            maxWidth: '500px',
            width: '90%',
          }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 'bold', marginBottom: '1.5rem' }}>
              {tareaEditando ? 'Editar Tarea' : 'Nueva Tarea'}
            </h2>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Tipo de Labor *</label>
                <select
                  value={formTarea.tipoLabor}
                  onChange={(e) => setFormTarea({ ...formTarea, tipoLabor: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                >
                  <option value="">Seleccione un tipo</option>
                  {tiposLaborDisponibles.map(tipo => (
                    <option key={tipo} value={tipo}>{tipo}</option>
                  ))}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Nombre de la Tarea *</label>
                <input
                  type="text"
                  value={formTarea.nombreTarea}
                  onChange={(e) => setFormTarea({ ...formTarea, nombreTarea: e.target.value })}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                />
              </div>

              <div>
                <label style={{ display: 'block', marginBottom: '0.5rem', fontWeight: '600' }}>Descripción</label>
                <textarea
                  value={formTarea.descripcion || ''}
                  onChange={(e) => setFormTarea({ ...formTarea, descripcion: e.target.value })}
                  rows={3}
                  style={{
                    width: '100%',
                    padding: '0.5rem',
                    border: '1px solid #d1d5db',
                    borderRadius: '0.375rem',
                  }}
                />
              </div>

              <label style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formTarea.esObligatoria}
                  onChange={(e) => setFormTarea({ ...formTarea, esObligatoria: e.target.checked })}
                />
                <span>Tarea obligatoria</span>
              </label>

              <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                <button
                  onClick={handleGuardarTarea}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#10b981',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Guardar
                </button>
                <button
                  onClick={() => setShowModalTarea(false)}
                  style={{
                    flex: 1,
                    padding: '0.75rem',
                    backgroundColor: '#6b7280',
                    color: 'white',
                    border: 'none',
                    borderRadius: '0.375rem',
                    cursor: 'pointer',
                    fontWeight: '600',
                  }}
                >
                  Cancelar
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ConfiguracionEstadosScreen;




