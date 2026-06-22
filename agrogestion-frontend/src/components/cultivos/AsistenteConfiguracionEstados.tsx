import React, { useState } from 'react';
import DiagramaEstadosConfig, { EstadoDiagrama, TransicionDiagrama, TareaDiagrama } from './DiagramaEstadosConfig';

interface AsistenteConfiguracionEstadosProps {
  abierto: boolean;
  onCerrar: () => void;
  estados: EstadoDiagrama[];
  transiciones: TransicionDiagrama[];
  tareasPorEstado: Map<number, TareaDiagrama[]>;
  onIrATab: (tab: 'estados' | 'transiciones' | 'tareas') => void;
  onAbrirModalEstado: () => void;
  onAbrirModalTransicion: () => void;
  onAbrirModalTarea: (estadoId: number) => void;
}

const PASOS = [
  { id: 1, titulo: 'Estados del ciclo', descripcion: 'Defina el orden fenológico u operativo del cultivo.' },
  { id: 2, titulo: 'Transiciones', descripcion: 'Conecte los estados indicando cómo puede avanzar el lote.' },
  { id: 3, titulo: 'Tareas por estado', descripcion: 'Asigne las labores permitidas y marque las obligatorias.' },
];

const AsistenteConfiguracionEstados: React.FC<AsistenteConfiguracionEstadosProps> = ({
  abierto,
  onCerrar,
  estados,
  transiciones,
  tareasPorEstado,
  onIrATab,
  onAbrirModalEstado,
  onAbrirModalTransicion,
  onAbrirModalTarea,
}) => {
  const [paso, setPaso] = useState(1);

  if (!abierto) return null;

  const estadosOrdenados = [...estados].sort((a, b) => a.orden - b.orden);

  return (
    <div style={{
      position: 'fixed',
      inset: 0,
      background: 'rgba(0,0,0,0.6)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 2000,
      padding: '16px',
    }}>
      <div style={{
        background: '#fff',
        borderRadius: '16px',
        maxWidth: '720px',
        width: '100%',
        maxHeight: '90vh',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
      }}>
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid #e5e7eb',
          background: 'linear-gradient(135deg, #059669 0%, #10b981 100%)',
          color: '#fff',
        }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ margin: 0, fontSize: '20px' }}>Asistente de configuración</h2>
            <button onClick={onCerrar} style={{ background: 'rgba(255,255,255,0.2)', border: 'none', borderRadius: '50%', width: 32, height: 32, cursor: 'pointer', color: '#fff' }}>✕</button>
          </div>
          <div style={{ display: 'flex', gap: '8px', marginTop: '16px' }}>
            {PASOS.map((p) => (
              <div
                key={p.id}
                style={{
                  flex: 1,
                  padding: '8px',
                  borderRadius: '8px',
                  background: paso === p.id ? 'rgba(255,255,255,0.25)' : 'rgba(255,255,255,0.1)',
                  fontSize: '12px',
                  textAlign: 'center',
                }}
              >
                Paso {p.id}
              </div>
            ))}
          </div>
        </div>

        <div style={{ flex: 1, overflowY: 'auto', padding: '24px' }}>
          <h3 style={{ margin: '0 0 8px', fontSize: '18px' }}>{PASOS[paso - 1].titulo}</h3>
          <p style={{ margin: '0 0 20px', color: '#6b7280', fontSize: '14px' }}>{PASOS[paso - 1].descripcion}</p>

          {paso === 1 && (
            <>
              <DiagramaEstadosConfig estados={estados} transiciones={transiciones} tareasPorEstado={tareasPorEstado} />
              <ul style={{ marginTop: '16px', paddingLeft: '20px', fontSize: '14px', color: '#374151' }}>
                {estadosOrdenados.length === 0 && <li>No hay estados. Agregue al menos Disponible y Sembrado.</li>}
                {estadosOrdenados.map((e) => (
                  <li key={e.id}>{e.nombre} (orden {e.orden}){e.diasMinimos ? ` — ${e.diasMinimos} días` : ''}</li>
                ))}
              </ul>
              <button
                onClick={() => { onAbrirModalEstado(); onIrATab('estados'); }}
                style={{ marginTop: '12px', padding: '8px 16px', background: '#10b981', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer' }}
              >
                + Agregar estado
              </button>
            </>
          )}

          {paso === 2 && (
            <>
              <p style={{ fontSize: '14px' }}>Transiciones configuradas: <strong>{transiciones.length}</strong></p>
              {transiciones.length === 0 && (
                <p style={{ color: '#b45309', fontSize: '14px' }}>Sin transiciones el lote no puede avanzar manualmente entre estados.</p>
              )}
              <ul style={{ fontSize: '14px' }}>
                {transiciones.map((t) => (
                  <li key={t.id}>{t.estadoOrigenNombre || t.estadoOrigenId} → {t.estadoDestinoNombre || t.estadoDestinoId}</li>
                ))}
              </ul>
              <button
                onClick={() => { onAbrirModalTransicion(); onIrATab('transiciones'); }}
                style={{ marginTop: '12px', padding: '8px 16px', background: '#3b82f6', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer' }}
              >
                + Agregar transición
              </button>
            </>
          )}

          {paso === 3 && (
            <>
              {estadosOrdenados.map((estado) => {
                const tareas = estado.id ? (tareasPorEstado.get(estado.id) || []) : [];
                return (
                  <div key={estado.id} style={{ marginBottom: '16px', padding: '12px', background: '#f9fafb', borderRadius: '8px' }}>
                    <div style={{ fontWeight: 600, marginBottom: '8px' }}>{estado.icono} {estado.nombre}</div>
                    {tareas.length === 0 ? (
                      <span style={{ fontSize: '13px', color: '#6b7280' }}>Sin tareas</span>
                    ) : (
                      <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '13px' }}>
                        {tareas.map((t, i) => (
                          <li key={i}>{t.nombreTarea}{t.esObligatoria === true ? ' (obligatoria)' : ''}</li>
                        ))}
                      </ul>
                    )}
                    {estado.id && (
                      <button
                        onClick={() => { onAbrirModalTarea(estado.id!); onIrATab('tareas'); }}
                        style={{ marginTop: '8px', padding: '4px 10px', fontSize: '12px', background: '#fff', border: '1px solid #d1d5db', borderRadius: '6px', cursor: 'pointer' }}
                      >
                        + Tarea
                      </button>
                    )}
                  </div>
                );
              })}
            </>
          )}
        </div>

        <div style={{ padding: '16px 24px', borderTop: '1px solid #e5e7eb', display: 'flex', justifyContent: 'space-between' }}>
          <button
            onClick={() => setPaso((p) => Math.max(1, p - 1))}
            disabled={paso === 1}
            style={{ padding: '8px 16px', background: '#e5e7eb', border: 'none', borderRadius: '8px', cursor: paso === 1 ? 'not-allowed' : 'pointer', opacity: paso === 1 ? 0.5 : 1 }}
          >
            Anterior
          </button>
          {paso < 3 ? (
            <button
              onClick={() => setPaso((p) => p + 1)}
              style={{ padding: '8px 20px', background: '#10b981', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}
            >
              Siguiente
            </button>
          ) : (
            <button
              onClick={onCerrar}
              style={{ padding: '8px 20px', background: '#059669', color: '#fff', border: 'none', borderRadius: '8px', cursor: 'pointer', fontWeight: 600 }}
            >
              Finalizar
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default AsistenteConfiguracionEstados;
