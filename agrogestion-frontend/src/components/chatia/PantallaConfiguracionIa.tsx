import React, { useEffect, useState } from 'react';
import { chatIaService } from '../../services/chatIaService';

interface PantallaConfiguracionIaProps {
  abierto: boolean;
  onCerrar: () => void;
  onGuardado?: () => void;
}

const MODELOS_DEFECTO = ['gemini-flash-latest', 'gemini-3.6-flash', 'gemini-3.1-pro-preview', 'gemini-pro-latest'];

const PantallaConfiguracionIa: React.FC<PantallaConfiguracionIaProps> = ({
  abierto,
  onCerrar,
  onGuardado,
}) => {
  const [claveApi, setClaveApi] = useState('');
  const [modelo, setModelo] = useState('gemini-flash-latest');
  const [modelos, setModelos] = useState<string[]>(MODELOS_DEFECTO);
  const [claveEnmascarada, setClaveEnmascarada] = useState<string | null>(null);
  const [claveConfigurada, setClaveConfigurada] = useState(false);
  const [claveInvalida, setClaveInvalida] = useState(false);
  const [guardando, setGuardando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [exito, setExito] = useState<string | null>(null);

  useEffect(() => {
    if (!abierto) return;
    setError(null);
    setExito(null);
    setClaveApi('');
    chatIaService
      .obtenerEstado()
      .then((estado) => {
        setModelo(estado.modelo || 'gemini-flash-latest');
        setClaveConfigurada(estado.claveConfigurada === true);
        setClaveInvalida(estado.claveInvalida === true);
        if (estado.modelosDisponibles?.length) {
          setModelos(estado.modelosDisponibles);
        }
      })
      .catch(() => undefined);
    chatIaService
      .obtenerConfiguracion()
      .then((config) => {
        setModelo(config.modelo || 'gemini-flash-latest');
        setClaveEnmascarada(config.claveEnmascarada || null);
        setClaveConfigurada(config.claveConfigurada);
        setClaveInvalida(config.claveInvalida);
      })
      .catch(() => undefined);
  }, [abierto]);

  if (!abierto) return null;

  const handleGuardar = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!claveConfigurada && !claveApi.trim()) {
      setError('Ingresá tu clave API de Gemini para activar el asistente.');
      return;
    }
    setGuardando(true);
    setError(null);
    setExito(null);
    try {
      const config = await chatIaService.guardarConfiguracion(claveApi.trim(), modelo);
      setClaveEnmascarada(config.claveEnmascarada || null);
      setClaveConfigurada(true);
      setClaveInvalida(false);
      setClaveApi('');
      setExito(
        claveApi.trim()
          ? 'Configuración guardada correctamente.'
          : 'Modelo actualizado correctamente.'
      );
      onGuardado?.();
    } catch (err: unknown) {
      const msg =
        err && typeof err === 'object' && 'response' in err
          ? (err as { response?: { data?: { message?: string } } }).response?.data?.message
          : null;
      setError(msg || 'No se pudo validar la configuración.');
    } finally {
      setGuardando(false);
    }
  };

  const handleEliminar = async () => {
    if (!window.confirm('¿Eliminar la configuración de IA? El chat quedará deshabilitado.')) {
      return;
    }
    setGuardando(true);
    try {
      await chatIaService.eliminarConfiguracion();
      setClaveEnmascarada(null);
      setClaveConfigurada(false);
      setClaveInvalida(false);
      setClaveApi('');
      setExito('Configuración eliminada.');
      onGuardado?.();
    } catch {
      setError('No se pudo eliminar la configuración.');
    } finally {
      setGuardando(false);
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        zIndex: 1100,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '1rem',
      }}
      onClick={onCerrar}
    >
      <div
        style={{
          backgroundColor: 'white',
          borderRadius: '0.75rem',
          padding: '1.5rem',
          maxWidth: '480px',
          width: '100%',
          boxShadow: '0 20px 40px rgba(0,0,0,0.2)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 style={{ margin: '0 0 0.5rem', fontSize: '1.25rem', color: '#111827' }}>
          Configuración del asistente IA
        </h2>
        <p style={{ margin: '0 0 1rem', fontSize: '0.875rem', color: '#6b7280' }}>
          Usá tu propia cuenta de Google Gemini (BYOK). La clave se guarda cifrada en el servidor y
          solo se usa para tus consultas sobre datos productivos de tu establecimiento.
        </p>

        {claveConfigurada && claveEnmascarada && (
          <p style={{ fontSize: '0.8rem', color: '#374151', marginBottom: '0.75rem' }}>
            Clave actual: <strong>{claveEnmascarada}</strong>
          </p>
        )}

        {claveInvalida && (
          <p
            style={{
              fontSize: '0.8rem',
              color: '#b91c1c',
              marginBottom: '0.75rem',
              backgroundColor: '#fef2f2',
              padding: '0.5rem',
              borderRadius: '0.375rem',
            }}
          >
            La última consulta falló por un problema con la clave. Guardá de nuevo (solo modelo o
            clave nueva) para reintentar.
          </p>
        )}

        <form onSubmit={handleGuardar}>
          <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
            Clave API de Gemini {claveConfigurada ? '(opcional)' : ''}
          </label>
          <input
            type="password"
            value={claveApi}
            onChange={(e) => setClaveApi(e.target.value)}
            placeholder={claveConfigurada ? 'Dejá vacío para mantener la clave actual' : 'AIza...'}
            style={{
              width: '100%',
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              marginBottom: '1rem',
              boxSizing: 'border-box',
            }}
          />

          <label style={{ display: 'block', fontSize: '0.875rem', marginBottom: '0.25rem' }}>
            Modelo
          </label>
          <select
            value={modelo}
            onChange={(e) => setModelo(e.target.value)}
            style={{
              width: '100%',
              padding: '0.5rem',
              border: '1px solid #d1d5db',
              borderRadius: '0.375rem',
              marginBottom: '1rem',
            }}
          >
            {modelos.map((m) => (
              <option key={m} value={m}>
                {m}
              </option>
            ))}
          </select>

          {error && (
            <p style={{ color: '#dc2626', fontSize: '0.875rem', marginBottom: '0.75rem' }}>{error}</p>
          )}
          {exito && (
            <p style={{ color: '#059669', fontSize: '0.875rem', marginBottom: '0.75rem' }}>{exito}</p>
          )}

          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <button
              type="submit"
              disabled={guardando}
              style={{
                flex: 1,
                padding: '0.625rem',
                backgroundColor: '#2563eb',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: guardando ? 'wait' : 'pointer',
              }}
            >
              {guardando ? 'Validando...' : claveConfigurada ? 'Actualizar' : 'Guardar'}
            </button>
            {claveConfigurada && (
              <button
                type="button"
                onClick={handleEliminar}
                disabled={guardando}
                style={{
                  padding: '0.625rem 1rem',
                  backgroundColor: '#fef2f2',
                  color: '#dc2626',
                  border: '1px solid #fecaca',
                  borderRadius: '0.375rem',
                  cursor: 'pointer',
                }}
              >
                Eliminar
              </button>
            )}
            <button
              type="button"
              onClick={onCerrar}
              style={{
                padding: '0.625rem 1rem',
                backgroundColor: '#f3f4f6',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: 'pointer',
              }}
            >
              Cerrar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default PantallaConfiguracionIa;
