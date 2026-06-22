import React, { useMemo } from 'react';

export interface EstadoDiagrama {
  id?: number;
  nombre: string;
  color?: string;
  icono?: string;
  orden: number;
  esEstadoInicial?: boolean;
  esEstadoFinal?: boolean;
  diasMinimos?: number | null;
  modoAvance?: string;
}

export interface TransicionDiagrama {
  id?: number;
  estadoOrigenId: number;
  estadoDestinoId: number;
  estadoOrigenNombre?: string;
  estadoDestinoNombre?: string;
  requiereMotivo?: boolean;
}

export interface TareaDiagrama {
  nombreTarea: string;
  esObligatoria?: boolean;
}

interface DiagramaEstadosConfigProps {
  estados: EstadoDiagrama[];
  transiciones: TransicionDiagrama[];
  tareasPorEstado: Map<number, TareaDiagrama[]>;
  onSeleccionarEstado?: (estadoId: number) => void;
}

const DiagramaEstadosConfig: React.FC<DiagramaEstadosConfigProps> = ({
  estados,
  transiciones,
  tareasPorEstado,
  onSeleccionarEstado,
}) => {
  const estadosOrdenados = useMemo(
    () => [...estados].sort((a, b) => a.orden - b.orden),
    [estados]
  );

  const transicionesPorOrigen = useMemo(() => {
    const mapa = new Map<number, TransicionDiagrama[]>();
    transiciones.forEach((t) => {
      const lista = mapa.get(t.estadoOrigenId) || [];
      lista.push(t);
      mapa.set(t.estadoOrigenId, lista);
    });
    return mapa;
  }, [transiciones]);

  if (estadosOrdenados.length === 0) {
    return (
      <div style={{ padding: '2rem', textAlign: 'center', color: '#6b7280', background: '#f9fafb', borderRadius: '8px' }}>
        Agregue estados para ver el diagrama del ciclo.
      </div>
    );
  }

  return (
    <div style={{ overflowX: 'auto', paddingBottom: '8px' }}>
      <div style={{
        display: 'flex',
        alignItems: 'flex-start',
        gap: '8px',
        minWidth: 'min-content',
        padding: '16px 8px',
      }}>
        {estadosOrdenados.map((estado, index) => {
          const salidas = transicionesPorOrigen.get(estado.id!) || [];
          const tareas = estado.id ? (tareasPorEstado.get(estado.id) || []) : [];
          const obligatorias = tareas.filter((t) => t.esObligatoria).length;

          return (
            <React.Fragment key={estado.id ?? index}>
              <div
                onClick={() => estado.id && onSeleccionarEstado?.(estado.id)}
                style={{
                  minWidth: '140px',
                  maxWidth: '180px',
                  background: '#fff',
                  border: `2px solid ${estado.color || '#10b981'}`,
                  borderRadius: '12px',
                  padding: '12px',
                  cursor: onSeleccionarEstado ? 'pointer' : 'default',
                  boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
                  transition: 'transform 0.15s',
                }}
                onMouseEnter={(e) => { if (onSeleccionarEstado) e.currentTarget.style.transform = 'translateY(-2px)'; }}
                onMouseLeave={(e) => { e.currentTarget.style.transform = 'translateY(0)'; }}
              >
                <div style={{ fontSize: '24px', marginBottom: '4px' }}>{estado.icono || '📋'}</div>
                <div style={{ fontWeight: 700, fontSize: '13px', color: '#111827', marginBottom: '6px' }}>
                  {estado.nombre}
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px', marginBottom: '6px' }}>
                  {estado.esEstadoInicial && (
                    <span style={{ fontSize: '10px', background: '#dbeafe', color: '#1e40af', padding: '2px 6px', borderRadius: '4px' }}>Inicial</span>
                  )}
                  {estado.esEstadoFinal && (
                    <span style={{ fontSize: '10px', background: '#fce7f3', color: '#9d174d', padding: '2px 6px', borderRadius: '4px' }}>Final</span>
                  )}
                  {estado.diasMinimos != null && estado.diasMinimos > 0 && (
                    <span style={{ fontSize: '10px', background: '#fef3c7', color: '#92400e', padding: '2px 6px', borderRadius: '4px' }}>
                      {estado.diasMinimos}d
                    </span>
                  )}
                </div>
                {tareas.length > 0 && (
                  <div style={{ fontSize: '11px', color: '#6b7280' }}>
                    {tareas.length} tarea{tareas.length !== 1 ? 's' : ''}
                    {obligatorias > 0 && ` (${obligatorias} oblig.)`}
                  </div>
                )}
                {salidas.length > 0 && (
                  <div style={{ marginTop: '8px', fontSize: '10px', color: '#4b5563' }}>
                    → {salidas.map((s) => s.estadoDestinoNombre || `#${s.estadoDestinoId}`).join(', ')}
                  </div>
                )}
              </div>
              {index < estadosOrdenados.length - 1 && (
                <div style={{
                  alignSelf: 'center',
                  color: '#9ca3af',
                  fontSize: '20px',
                  fontWeight: 300,
                  padding: '0 2px',
                }}>
                  →
                </div>
              )}
            </React.Fragment>
          );
        })}
      </div>
      <p style={{ fontSize: '12px', color: '#9ca3af', margin: '8px 0 0', paddingLeft: '8px' }}>
        Flujo principal por orden. Las flechas dentro de cada tarjeta muestran transiciones adicionales configuradas.
      </p>
    </div>
  );
};

export default DiagramaEstadosConfig;
