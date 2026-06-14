import React, { useState } from 'react';
import api from '../../services/api';
import { API_ENDPOINTS } from '../../services/apiEndpoints';

type TipoAccion = 'control_celo' | 'chequeo_gestacion' | 'destete' | 'parto';

interface EventoAccion {
  tipo: string;
  servicioId?: number;
  gestacionId?: number;
  partoId?: number;
  madreId?: number;
  madreIdentificacion?: string;
  nacidosVivos?: number;
  recordatorioId?: number;
}

interface AccionCalendarioPorcinosModalProps {
  tipo: TipoAccion;
  evento: EventoAccion;
  recordatorioId?: number | null;
  onSuccess: () => void;
  onCancel: () => void;
}

export const AccionCalendarioPorcinosModal: React.FC<AccionCalendarioPorcinosModalProps> = ({
  tipo,
  evento,
  recordatorioId,
  onSuccess,
  onCancel
}) => {
  const [enviando, setEnviando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Control de celo
  const [preñada, setPreñada] = useState<boolean>(true);
  const [observacionesCelo, setObservacionesCelo] = useState('');

  // Chequeo gestación
  const [fechaChequeo, setFechaChequeo] = useState(() => new Date().toISOString().slice(0, 10));
  const [metodoChequeo, setMetodoChequeo] = useState<'ECO' | 'PALPACION' | 'OBSERVACION'>('ECO');
  const [resultadoChequeo, setResultadoChequeo] = useState<'POSITIVO' | 'NEGATIVO'>('POSITIVO');
  const [observacionesChequeo, setObservacionesChequeo] = useState('');

  // Destete
  const [fechaDestete, setFechaDestete] = useState(() => new Date().toISOString().slice(0, 10));
  const [cantidadDestetados, setCantidadDestetados] = useState(evento.nacidosVivos ?? 0);
  const [pesoPromedioDestete, setPesoPromedioDestete] = useState('');
  const [diasLactancia, setDiasLactancia] = useState('');
  const [observacionesDestete, setObservacionesDestete] = useState('');

  // Parto
  const [nacidosVivos, setNacidosVivos] = useState(0);
  const [nacidosMuertos, setNacidosMuertos] = useState(0);
  const [momias, setMomias] = useState(0);
  const [pesoPromedioNacimiento, setPesoPromedioNacimiento] = useState('');
  const [observacionesParto, setObservacionesParto] = useState('');

  const marcarRecordatorioCompletado = async () => {
    if (recordatorioId) {
      await api.patch(`/recordatorios/${recordatorioId}/completar`);
    }
  };

  const handleSubmitControlCelo = async (e: React.FormEvent) => {
    e.preventDefault();
    if (evento.servicioId == null) return;
    setEnviando(true);
    setError(null);
    try {
      await api.post(API_ENDPOINTS.PORCINOS_SERVICIOS.CONTROL_CELO(evento.servicioId), {
        preñada,
        observaciones: observacionesCelo || undefined
      });
      await marcarRecordatorioCompletado();
      onSuccess();
    } catch (err: any) {
      setError(err?.response?.data?.error || err?.message || 'Error al registrar control de celo');
    } finally {
      setEnviando(false);
    }
  };

  const handleSubmitChequeoGestacion = async (e: React.FormEvent) => {
    e.preventDefault();
    const gid = evento.gestacionId;
    if (gid == null) return;
    setEnviando(true);
    setError(null);
    try {
      await api.post(API_ENDPOINTS.PORCINOS_GESTACION.CHEQUEOS(gid), {
        fecha: fechaChequeo,
        metodo: metodoChequeo,
        resultado: resultadoChequeo,
        observaciones: observacionesChequeo || undefined
      });
      await marcarRecordatorioCompletado();
      onSuccess();
    } catch (err: any) {
      setError(err?.response?.data?.error || err?.message || 'Error al registrar chequeo');
    } finally {
      setEnviando(false);
    }
  };

  const handleSubmitDestete = async (e: React.FormEvent) => {
    e.preventDefault();
    if (evento.partoId == null) return;
    setEnviando(true);
    setError(null);
    try {
      const body: Record<string, unknown> = {
        partoId: evento.partoId,
        fechaDestete,
        cantidadDestetados: Number(cantidadDestetados)
      };
      if (pesoPromedioDestete) body.pesoPromedioDestete = Number(pesoPromedioDestete);
      if (diasLactancia) body.diasLactancia = Number(diasLactancia);
      if (observacionesDestete) body.observaciones = observacionesDestete;
      await api.post(API_ENDPOINTS.PORCINOS_DESTETES.CREAR, body);
      await marcarRecordatorioCompletado();
      onSuccess();
    } catch (err: any) {
      setError(err?.response?.data?.error || err?.message || 'Error al registrar destete');
    } finally {
      setEnviando(false);
    }
  };

  const handleSubmitParto = async (e: React.FormEvent) => {
    e.preventDefault();
    const madreId = evento.madreId;
    if (madreId == null) return;
    setEnviando(true);
    setError(null);
    try {
      const body: Record<string, unknown> = {
        madreId,
        nacidosVivos: Number(nacidosVivos)
      };
      if (nacidosMuertos) body.nacidosMuertos = Number(nacidosMuertos);
      if (momias) body.momias = Number(momias);
      if (pesoPromedioNacimiento) body.pesoPromedioNacimiento = Number(pesoPromedioNacimiento);
      if (observacionesParto) body.observaciones = observacionesParto;
      await api.post(API_ENDPOINTS.PORCINOS_PARTOS.CREAR, body);
      await marcarRecordatorioCompletado();
      onSuccess();
    } catch (err: any) {
      setError(err?.response?.data?.error || err?.message || 'Error al registrar parto');
    } finally {
      setEnviando(false);
    }
  };

  const titulos: Record<TipoAccion, string> = {
    control_celo: 'Registrar control de celo',
    chequeo_gestacion: 'Registrar chequeo de preñez',
    destete: 'Registrar destete',
    parto: 'Registrar parto'
  };

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1100,
        padding: '1rem'
      }}
      onClick={onCancel}
    >
      <div
        style={{
          backgroundColor: 'white',
          borderRadius: '0.75rem',
          maxWidth: '480px',
          width: '100%',
          maxHeight: '90vh',
          overflow: 'auto',
          boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1)'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ padding: '1.5rem 1.75rem', borderBottom: '1px solid #e5e7eb' }}>
          <h2 style={{ margin: 0, fontSize: '1.25rem', fontWeight: 'bold', color: '#1f2937' }}>
            {titulos[tipo]}
          </h2>
          {evento.madreIdentificacion && (
            <p style={{ margin: '0.25rem 0 0', fontSize: '0.875rem', color: '#6b7280' }}>
              Madre: {evento.madreIdentificacion}
            </p>
          )}
        </div>

        <form
          onSubmit={
            tipo === 'control_celo'
              ? handleSubmitControlCelo
              : tipo === 'chequeo_gestacion'
                ? handleSubmitChequeoGestacion
                : tipo === 'destete'
                  ? handleSubmitDestete
                  : handleSubmitParto
          }
          style={{ padding: '1.5rem 1.75rem' }}
        >
          {error && (
            <div
              style={{
                marginBottom: '1rem',
                padding: '0.75rem',
                backgroundColor: '#fef2f2',
                color: '#b91c1c',
                borderRadius: '0.375rem',
                fontSize: '0.875rem'
              }}
            >
              {error}
            </div>
          )}

          {tipo === 'control_celo' && (
            <>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  ¿Preñada?
                </label>
                <select
                  value={preñada ? 'si' : 'no'}
                  onChange={(e) => setPreñada(e.target.value === 'si')}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                >
                  <option value="si">Sí</option>
                  <option value="no">No</option>
                </select>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Observaciones (opcional)
                </label>
                <textarea
                  value={observacionesCelo}
                  onChange={(e) => setObservacionesCelo(e.target.value)}
                  rows={2}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
            </>
          )}

          {tipo === 'chequeo_gestacion' && (
            <>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Fecha
                </label>
                <input
                  type="date"
                  value={fechaChequeo}
                  onChange={(e) => setFechaChequeo(e.target.value)}
                  required
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Método
                </label>
                <select
                  value={metodoChequeo}
                  onChange={(e) => setMetodoChequeo(e.target.value as 'ECO' | 'PALPACION' | 'OBSERVACION')}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                >
                  <option value="ECO">Ecografía</option>
                  <option value="PALPACION">Palpación</option>
                  <option value="OBSERVACION">Observación</option>
                </select>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Resultado
                </label>
                <select
                  value={resultadoChequeo}
                  onChange={(e) => setResultadoChequeo(e.target.value as 'POSITIVO' | 'NEGATIVO')}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                >
                  <option value="POSITIVO">Positivo</option>
                  <option value="NEGATIVO">Negativo</option>
                </select>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Observaciones (opcional)
                </label>
                <textarea
                  value={observacionesChequeo}
                  onChange={(e) => setObservacionesChequeo(e.target.value)}
                  rows={2}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
            </>
          )}

          {tipo === 'destete' && (
            <>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Fecha de destete
                </label>
                <input
                  type="date"
                  value={fechaDestete}
                  onChange={(e) => setFechaDestete(e.target.value)}
                  required
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Cantidad destetados
                </label>
                <input
                  type="number"
                  min={0}
                  value={cantidadDestetados}
                  onChange={(e) => setCantidadDestetados(Number(e.target.value))}
                  required
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Peso promedio (kg, opcional)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min={0}
                  value={pesoPromedioDestete}
                  onChange={(e) => setPesoPromedioDestete(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Días de lactancia (opcional)
                </label>
                <input
                  type="number"
                  min={0}
                  value={diasLactancia}
                  onChange={(e) => setDiasLactancia(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Observaciones (opcional)
                </label>
                <textarea
                  value={observacionesDestete}
                  onChange={(e) => setObservacionesDestete(e.target.value)}
                  rows={2}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
            </>
          )}

          {tipo === 'parto' && (
            <>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Nacidos vivos
                </label>
                <input
                  type="number"
                  min={0}
                  value={nacidosVivos}
                  onChange={(e) => setNacidosVivos(Number(e.target.value))}
                  required
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Nacidos muertos (opcional)
                </label>
                <input
                  type="number"
                  min={0}
                  value={nacidosMuertos}
                  onChange={(e) => setNacidosMuertos(Number(e.target.value))}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Momias (opcional)
                </label>
                <input
                  type="number"
                  min={0}
                  value={momias}
                  onChange={(e) => setMomias(Number(e.target.value))}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Peso promedio al nacer (kg, opcional)
                </label>
                <input
                  type="number"
                  step="0.01"
                  min={0}
                  value={pesoPromedioNacimiento}
                  onChange={(e) => setPesoPromedioNacimiento(e.target.value)}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label style={{ display: 'block', marginBottom: '0.375rem', fontWeight: '500', fontSize: '0.875rem' }}>
                  Observaciones (opcional)
                </label>
                <textarea
                  value={observacionesParto}
                  onChange={(e) => setObservacionesParto(e.target.value)}
                  rows={2}
                  style={{ width: '100%', padding: '0.5rem', borderRadius: '0.375rem', border: '1px solid #d1d5db' }}
                />
              </div>
            </>
          )}

          <div style={{ display: 'flex', gap: '0.75rem', marginTop: '1.25rem' }}>
            <button
              type="button"
              onClick={onCancel}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#e5e7eb',
                color: '#374151',
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
              disabled={enviando}
              style={{
                padding: '0.5rem 1rem',
                backgroundColor: '#10b981',
                color: 'white',
                border: 'none',
                borderRadius: '0.375rem',
                cursor: enviando ? 'not-allowed' : 'pointer',
                fontWeight: '500'
              }}
            >
              {enviando ? 'Guardando…' : 'Guardar y marcar hecho'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
