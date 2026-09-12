import React, { useState } from 'react';
import type { AxiosError } from 'axios';
import api from '../services/api';
import { useCampana } from '../contexts/CampanaContext';
import { useEmpresa } from '../contexts/EmpresaContext';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';
import { Icon } from './icons';
import { normalizarCampana } from '../core/utils/campanaApi';

interface Props {
  /** Si true, omite título principal (uso dentro de Configuración unificada). */
  incrustado?: boolean;
}

const extraerMensajeError = (error: unknown, prefijo: string): string => {
  const axiosErr = error as AxiosError<{ error?: string; message?: string; mensaje?: string }>;
  const detalle =
    axiosErr.response?.data?.error ||
    axiosErr.response?.data?.message ||
    axiosErr.response?.data?.mensaje;
  return prefijo + (detalle || (error instanceof Error ? error.message : 'Error desconocido'));
};

const etiquetaEstado = (estado: string) => {
  switch (estado) {
    case 'ACTIVA':
      return { texto: 'Activa', fondo: '#d1fae5', color: '#065f46' };
    case 'CERRADA':
      return { texto: 'Cerrada', fondo: '#e5e7eb', color: '#374151' };
    default:
      return { texto: 'Borrador', fondo: '#fef3c7', color: '#92400e' };
  }
};

const GestionCampanasScreen: React.FC<Props> = ({ incrustado = false }) => {
  const { campanas, campanaActiva, recargarCampanas, cambiarCampana, registrarCampanaCreada, loading } = useCampana();
  const { esAdministrador } = useEmpresa();
  const { concepto } = useConceptoTemporalModulo();

  const [codigo, setCodigo] = useState('');
  const [nombre, setNombre] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [mensaje, setMensaje] = useState<{ tipo: 'exito' | 'error'; texto: string } | null>(null);
  const [guardando, setGuardando] = useState(false);

  const tituloPeriodo = concepto.etiquetaPeriodo;

  const crear = async () => {
    if (!codigo.trim() || !nombre.trim() || !fechaInicio || !fechaFin) {
      setMensaje({ tipo: 'error', texto: 'Complete código, nombre y fechas del período.' });
      return;
    }
    if (fechaFin < fechaInicio) {
      setMensaje({ tipo: 'error', texto: 'La fecha de fin debe ser posterior a la de inicio.' });
      return;
    }

    try {
      setGuardando(true);
      setMensaje(null);
      const { data } = await api.post('/v1/campanas', {
        codigo: codigo.trim(),
        nombre: nombre.trim(),
        fechaInicio,
        fechaFin,
      });
      const creada = normalizarCampana(data);
      if (creada) {
        registrarCampanaCreada(creada);
      }
      await recargarCampanas();
      setMensaje({
        tipo: 'exito',
        texto: `${tituloPeriodo} creada. Actívela para usarla en el selector superior.`,
      });
      setCodigo('');
      setNombre('');
      setFechaInicio('');
      setFechaFin('');
      await recargarCampanas();
    } catch (e: unknown) {
      setMensaje({ tipo: 'error', texto: extraerMensajeError(e, 'Error al crear: ') });
    } finally {
      setGuardando(false);
    }
  };

  const activar = async (id: number) => {
    try {
      setMensaje(null);
      await api.post(`/v1/campanas/${id}/activar`);
      await recargarCampanas();
      await cambiarCampana(id);
      setMensaje({ tipo: 'exito', texto: 'Período activado. Las operaciones nuevas se imputarán a este período.' });
    } catch (e: unknown) {
      setMensaje({ tipo: 'error', texto: extraerMensajeError(e, 'Error al activar: ') });
    }
  };

  const cerrar = async (id: number, nombrePeriodo: string) => {
    if (!confirm(`¿Cerrar "${nombrePeriodo}"? Las operaciones quedarán en solo lectura para este período.`)) {
      return;
    }
    try {
      setMensaje(null);
      await api.post(`/v1/campanas/${id}/cerrar`);
      await recargarCampanas();
      setMensaje({ tipo: 'exito', texto: 'Período cerrado.' });
    } catch (e: unknown) {
      setMensaje({ tipo: 'error', texto: extraerMensajeError(e, 'Error al cerrar: ') });
    }
  };

  if (!esAdministrador()) {
    return (
      <p style={{ padding: '1rem', color: '#6b7280' }}>
        Solo administradores pueden gestionar {tituloPeriodo.toLowerCase()}s.
      </p>
    );
  }

  return (
    <div style={{ maxWidth: incrustado ? '100%' : '900px', margin: incrustado ? 0 : '0 auto' }}>
      {!incrustado && (
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: '0.5rem' }}>
          {tituloPeriodo}s
        </h1>
      )}

      <p style={{ color: '#6b7280', fontSize: '0.9rem', marginBottom: '1.25rem', maxWidth: '720px' }}>
        {concepto.etiquetaPeriodo === 'Campaña agrícola' ? (
          <>
            La <strong>campaña agrícola</strong> agrupa ciclos de cultivo, labores, costos y reportes de la empresa.
            Cada lote puede tener un ciclo siembra→cosecha por campaña. Solo puede haber una campaña <strong>activa</strong> a la vez.
          </>
        ) : (
          <>
            El <strong>período de gestión</strong> consolida reportes y movimientos económicos del módulo.
            Unidad operativa: <strong>{concepto.unidadOperativa}</strong>. {concepto.ayuda}
          </>
        )}
      </p>

      {campanaActiva && (
        <div
          style={{
            padding: '0.75rem 1rem',
            marginBottom: '1rem',
            backgroundColor: '#eff6ff',
            border: '1px solid #bfdbfe',
            borderRadius: '0.5rem',
            display: 'flex',
            alignItems: 'center',
            gap: '0.5rem',
            flexWrap: 'wrap',
          }}
        >
          <Icon name="CalendarDays" size={18} />
          <span>
            <strong>Período en uso:</strong> {campanaActiva.nombre} ({campanaActiva.codigo}) —{' '}
            {campanaActiva.fechaInicio} a {campanaActiva.fechaFin}
          </span>
        </div>
      )}

      {mensaje && (
        <div
          style={{
            padding: '0.75rem 1rem',
            marginBottom: '1rem',
            borderRadius: '0.5rem',
            backgroundColor: mensaje.tipo === 'exito' ? '#d1fae5' : '#fee2e2',
            color: mensaje.tipo === 'exito' ? '#065f46' : '#dc2626',
          }}
        >
          {mensaje.texto}
        </div>
      )}

      <div
        style={{
          border: '1px solid #e5e7eb',
          borderRadius: '0.5rem',
          padding: '1.25rem',
          marginBottom: '1.5rem',
          backgroundColor: 'white',
        }}
      >
        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '1rem' }}>
          Nueva {tituloPeriodo.toLowerCase()}
        </h2>
        <div style={{ display: 'grid', gap: '0.75rem', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
          <input
            className="border px-2 py-1"
            style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
            placeholder="Código (ej. 2025-26)"
            value={codigo}
            onChange={(e) => setCodigo(e.target.value)}
          />
          <input
            style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
            placeholder="Nombre"
            value={nombre}
            onChange={(e) => setNombre(e.target.value)}
          />
          <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.85rem' }}>
            Inicio
            <input
              type="date"
              style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
              value={fechaInicio}
              onChange={(e) => setFechaInicio(e.target.value)}
            />
          </label>
          <label style={{ display: 'flex', flexDirection: 'column', gap: '0.25rem', fontSize: '0.85rem' }}>
            Fin
            <input
              type="date"
              style={{ padding: '0.5rem', border: '1px solid #d1d5db', borderRadius: '0.375rem' }}
              value={fechaFin}
              onChange={(e) => setFechaFin(e.target.value)}
            />
          </label>
        </div>
        <button
          type="button"
          disabled={guardando}
          onClick={crear}
          style={{
            marginTop: '1rem',
            padding: '0.5rem 1.25rem',
            backgroundColor: '#3b82f6',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: guardando ? 'not-allowed' : 'pointer',
            fontWeight: 600,
            opacity: guardando ? 0.7 : 1,
          }}
        >
          {guardando ? 'Creando…' : 'Crear período'}
        </button>
      </div>

      <h2 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.75rem' }}>
        Períodos configurados {loading ? '(cargando…)' : `(${campanas.length})`}
      </h2>

      {campanas.length === 0 && !loading ? (
        <p style={{ color: '#6b7280', padding: '1rem', backgroundColor: '#f9fafb', borderRadius: '0.5rem' }}>
          No hay períodos. Cree uno para comenzar a operar.
        </p>
      ) : (
        <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
          {campanas.map((c) => {
            const est = etiquetaEstado(c.estado);
            const esSeleccionada = campanaActiva?.id === c.id;
            return (
              <li
                key={c.id}
                style={{
                  border: esSeleccionada ? '2px solid #3b82f6' : '1px solid #e5e7eb',
                  borderRadius: '0.5rem',
                  padding: '1rem',
                  backgroundColor: 'white',
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  gap: '1rem',
                  flexWrap: 'wrap',
                }}
              >
                <div>
                  <div style={{ fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                    {c.nombre} <span style={{ color: '#6b7280', fontWeight: 400 }}>({c.codigo})</span>
                    <span
                      style={{
                        fontSize: '0.75rem',
                        padding: '0.15rem 0.5rem',
                        borderRadius: '9999px',
                        backgroundColor: est.fondo,
                        color: est.color,
                      }}
                    >
                      {est.texto}
                    </span>
                    {esSeleccionada && (
                      <span style={{ fontSize: '0.75rem', color: '#2563eb' }}>· En uso en el selector</span>
                    )}
                  </div>
                  <div style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.25rem' }}>
                    {c.fechaInicio} — {c.fechaFin}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  {c.estado !== 'ACTIVA' && c.estado !== 'CERRADA' && (
                    <button
                      type="button"
                      onClick={() => activar(c.id)}
                      style={{
                        padding: '0.35rem 0.75rem',
                        border: '1px solid #d1d5db',
                        borderRadius: '0.375rem',
                        backgroundColor: 'white',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                    >
                      Activar
                    </button>
                  )}
                  {c.estado === 'ACTIVA' && !esSeleccionada && (
                    <button
                      type="button"
                      onClick={() => activar(c.id)}
                      style={{
                        padding: '0.35rem 0.75rem',
                        border: '1px solid #3b82f6',
                        borderRadius: '0.375rem',
                        backgroundColor: '#eff6ff',
                        color: '#1d4ed8',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                    >
                      Usar en selector
                    </button>
                  )}
                  {c.estado !== 'CERRADA' && (
                    <button
                      type="button"
                      onClick={() => cerrar(c.id, c.nombre)}
                      style={{
                        padding: '0.35rem 0.75rem',
                        border: '1px solid #fecaca',
                        borderRadius: '0.375rem',
                        backgroundColor: '#fef2f2',
                        color: '#b91c1c',
                        cursor: 'pointer',
                        fontSize: '0.875rem',
                      }}
                    >
                      Cerrar
                    </button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
};

export default GestionCampanasScreen;
