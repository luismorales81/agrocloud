import React, { useCallback, useEffect, useState } from 'react';
import { Icon } from '../icons';
import {
  trazabilidadExpedienteService,
  type ReporteTrazabilidadResumen,
  type TipoEntidadExpediente,
} from '../../services/trazabilidadExpedienteService';
import type { ConfiguracionExpedienteModulo, OpcionEntidad } from './configuracionesExpediente';

interface Props {
  configuracion: ConfiguracionExpedienteModulo;
}

const TrazabilidadExpedienteScreen: React.FC<Props> = ({ configuracion }) => {
  const tipoInicial = configuracion.tiposEntidad[0]?.valor ?? 'LOTE';
  const [tipoEntidad, setTipoEntidad] = useState<TipoEntidadExpediente>(tipoInicial);
  const [entidades, setEntidades] = useState<OpcionEntidad[]>([]);
  const [entidadId, setEntidadId] = useState<number | ''>('');
  const [cargandoEntidades, setCargandoEntidades] = useState(false);
  const [generando, setGenerando] = useState(false);
  const [mensaje, setMensaje] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [ultimoReporteId, setUltimoReporteId] = useState<number | null>(null);
  const [reportes, setReportes] = useState<ReporteTrazabilidadResumen[]>([]);

  const cargarEntidades = useCallback(async () => {
    setCargandoEntidades(true);
    setError(null);
    try {
      const lista = await configuracion.cargarEntidades(tipoEntidad);
      setEntidades(lista);
      setEntidadId(lista.length > 0 ? lista[0].id : '');
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudieron cargar las entidades');
      setEntidades([]);
      setEntidadId('');
    } finally {
      setCargandoEntidades(false);
    }
  }, [configuracion, tipoEntidad]);

  const cargarReportes = useCallback(async () => {
    try {
      const lista = await trazabilidadExpedienteService.listarReportes();
      const expedientes = lista.filter((r) => r.certificacionCodigo === 'EXPEDIENTE_CICLO_VIDA');
      setReportes(expedientes);
    } catch {
      setReportes([]);
    }
  }, []);

  useEffect(() => {
    cargarEntidades();
  }, [cargarEntidades]);

  useEffect(() => {
    cargarReportes();
  }, [cargarReportes]);

  const generarExpediente = async () => {
    if (entidadId === '') {
      setError('Seleccione una entidad');
      return;
    }
    setGenerando(true);
    setError(null);
    setMensaje(null);
    try {
      const resp = await trazabilidadExpedienteService.generarExpediente(tipoEntidad, entidadId);
      setUltimoReporteId(resp.id);
      setMensaje(`Expediente generado (TRC-${resp.id}). Hash: ${resp.hashSnapshot.slice(0, 16)}…`);
      await cargarReportes();
    } catch (e) {
      const msg =
        typeof e === 'object' && e !== null && 'response' in e
          ? String((e as { response?: { data?: { message?: string } } }).response?.data?.message ?? 'Error al generar')
          : e instanceof Error
            ? e.message
            : 'Error al generar expediente';
      setError(msg);
    } finally {
      setGenerando(false);
    }
  };

  const descargarPdf = async (id: number) => {
    try {
      const blob = await trazabilidadExpedienteService.descargarPdf(id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `expediente-TRC-${id}.pdf`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'No se pudo descargar el PDF');
    }
  };

  return (
    <div style={{ padding: '1.5rem', maxWidth: 960, margin: '0 auto' }}>
      <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: '#1f2937', marginBottom: '0.5rem' }}>
        {configuracion.tituloPagina}
      </h1>
      <p style={{ color: '#6b7280', marginBottom: '1.5rem' }}>{configuracion.descripcion}</p>

      <div
        style={{
          background: '#fff',
          border: '1px solid #e5e7eb',
          borderRadius: 12,
          padding: '1.25rem',
          marginBottom: '1.5rem',
        }}
      >
        {configuracion.tiposEntidad.length > 1 && (
          <label style={{ display: 'block', marginBottom: '1rem' }}>
            <span style={{ display: 'block', fontWeight: 600, marginBottom: 6 }}>Tipo de alcance</span>
            <select
              value={tipoEntidad}
              onChange={(e) => setTipoEntidad(e.target.value as TipoEntidadExpediente)}
              style={{ width: '100%', padding: '0.5rem', borderRadius: 8, border: '1px solid #d1d5db' }}
            >
              {configuracion.tiposEntidad.map((t) => (
                <option key={t.valor} value={t.valor}>
                  {t.etiqueta}
                </option>
              ))}
            </select>
          </label>
        )}

        <label style={{ display: 'block', marginBottom: '1rem' }}>
          <span style={{ display: 'block', fontWeight: 600, marginBottom: 6 }}>Entidad</span>
          <select
            value={entidadId}
            onChange={(e) => setEntidadId(e.target.value ? Number(e.target.value) : '')}
            disabled={cargandoEntidades || entidades.length === 0}
            style={{ width: '100%', padding: '0.5rem', borderRadius: 8, border: '1px solid #d1d5db' }}
          >
            {entidades.length === 0 ? (
              <option value="">Sin registros</option>
            ) : (
              entidades.map((e) => (
                <option key={e.id} value={e.id}>
                  {e.etiqueta}
                </option>
              ))
            )}
          </select>
        </label>

        <button
          type="button"
          onClick={generarExpediente}
          disabled={generando || entidadId === ''}
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 8,
            padding: '0.6rem 1.2rem',
            background: generando ? '#9ca3af' : '#059669',
            color: '#fff',
            border: 'none',
            borderRadius: 8,
            fontWeight: 600,
            cursor: generando ? 'wait' : 'pointer',
          }}
        >
          <Icon name="FileText" size={18} />
          {generando ? 'Generando…' : 'Generar expediente PDF'}
        </button>

        {ultimoReporteId != null && (
          <button
            type="button"
            onClick={() => descargarPdf(ultimoReporteId)}
            style={{
              marginLeft: 12,
              padding: '0.6rem 1rem',
              background: '#fff',
              border: '1px solid #059669',
              color: '#059669',
              borderRadius: 8,
              fontWeight: 600,
              cursor: 'pointer',
            }}
          >
            Descargar último PDF
          </button>
        )}
      </div>

      {mensaje && (
        <p style={{ color: '#059669', marginBottom: '1rem', padding: '0.75rem', background: '#ecfdf5', borderRadius: 8 }}>
          {mensaje}
        </p>
      )}
      {error && (
        <p style={{ color: '#dc2626', marginBottom: '1rem', padding: '0.75rem', background: '#fef2f2', borderRadius: 8 }}>
          {error}
        </p>
      )}

      <h2 style={{ fontSize: '1.15rem', fontWeight: 600, marginBottom: '0.75rem' }}>Expedientes recientes</h2>
      {reportes.length === 0 ? (
        <p style={{ color: '#9ca3af' }}>Aún no hay expedientes generados en esta empresa.</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.9rem' }}>
          <thead>
            <tr style={{ background: '#f9fafb', textAlign: 'left' }}>
              <th style={{ padding: '0.5rem', borderBottom: '1px solid #e5e7eb' }}>Código</th>
              <th style={{ padding: '0.5rem', borderBottom: '1px solid #e5e7eb' }}>Alcance</th>
              <th style={{ padding: '0.5rem', borderBottom: '1px solid #e5e7eb' }}>Fecha</th>
              <th style={{ padding: '0.5rem', borderBottom: '1px solid #e5e7eb' }}></th>
            </tr>
          </thead>
          <tbody>
            {reportes.slice(0, 15).map((r) => (
              <tr key={r.id}>
                <td style={{ padding: '0.5rem', borderBottom: '1px solid #f3f4f6' }}>TRC-{r.id}</td>
                <td style={{ padding: '0.5rem', borderBottom: '1px solid #f3f4f6' }}>
                  {r.entidadTipo} #{r.entidadId}
                </td>
                <td style={{ padding: '0.5rem', borderBottom: '1px solid #f3f4f6' }}>
                  {r.generadoEn ? new Date(r.generadoEn).toLocaleString('es-AR') : '-'}
                </td>
                <td style={{ padding: '0.5rem', borderBottom: '1px solid #f3f4f6' }}>
                  <button
                    type="button"
                    onClick={() => descargarPdf(r.id)}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#2563eb',
                      cursor: 'pointer',
                      textDecoration: 'underline',
                    }}
                  >
                    PDF
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default TrazabilidadExpedienteScreen;
