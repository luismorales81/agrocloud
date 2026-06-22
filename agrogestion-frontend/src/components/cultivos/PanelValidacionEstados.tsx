import React from 'react';

export interface AvisoValidacion {
  tipo: 'ERROR' | 'ADVERTENCIA' | 'INFO';
  codigo: string;
  mensaje: string;
  estadoId?: number;
  estadoNombre?: string;
}

export interface ValidacionConfiguracion {
  /** Jackson serializa isValida() como "valida" */
  valida?: boolean;
  totalEstados: number;
  totalTransiciones: number;
  totalTareas: number;
  avisos: AvisoValidacion[];
}

interface PanelValidacionEstadosProps {
  validacion: ValidacionConfiguracion | null;
  cargando?: boolean;
}

const colorPorTipo: Record<string, { bg: string; border: string; text: string }> = {
  ERROR: { bg: '#fef2f2', border: '#fecaca', text: '#991b1b' },
  ADVERTENCIA: { bg: '#fffbeb', border: '#fde68a', text: '#92400e' },
  INFO: { bg: '#eff6ff', border: '#bfdbfe', text: '#1e40af' },
};

const PanelValidacionEstados: React.FC<PanelValidacionEstadosProps> = ({ validacion, cargando }) => {
  if (cargando) {
    return <div style={{ padding: '12px', color: '#6b7280', fontSize: '13px' }}>Validando configuración...</div>;
  }
  if (!validacion) return null;

  const esValida = validacion.valida === true;
  const errores = validacion.avisos.filter((a) => a.tipo === 'ERROR');
  const advertencias = validacion.avisos.filter((a) => a.tipo === 'ADVERTENCIA');

  return (
    <div style={{ marginTop: '16px' }}>
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: '12px',
        marginBottom: '12px',
        flexWrap: 'wrap',
      }}>
        <span style={{
          padding: '6px 12px',
          borderRadius: '8px',
          fontWeight: 600,
          fontSize: '13px',
          background: esValida ? '#d1fae5' : '#fee2e2',
          color: esValida ? '#065f46' : '#991b1b',
        }}>
          {esValida ? '✓ Configuración válida' : '✗ Requiere correcciones'}
        </span>
        <span style={{ fontSize: '12px', color: '#6b7280' }}>
          {validacion.totalEstados} estados · {validacion.totalTransiciones} transiciones · {validacion.totalTareas} tareas
        </span>
      </div>

      {validacion.avisos.length === 0 && (
        <div style={{ padding: '12px', background: '#f0fdf4', borderRadius: '8px', fontSize: '13px', color: '#166534' }}>
          No se detectaron problemas en la configuración.
        </div>
      )}

      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {[...errores, ...advertencias, ...validacion.avisos.filter((a) => a.tipo === 'INFO')].map((aviso, i) => {
          const colores = colorPorTipo[aviso.tipo] || colorPorTipo.INFO;
          return (
            <div
              key={`${aviso.codigo}-${i}`}
              style={{
                padding: '10px 12px',
                background: colores.bg,
                border: `1px solid ${colores.border}`,
                borderRadius: '8px',
                fontSize: '13px',
                color: colores.text,
              }}
            >
              <strong>{aviso.tipo}:</strong> {aviso.mensaje}
              {aviso.estadoNombre && (
                <span style={{ opacity: 0.85 }}> ({aviso.estadoNombre})</span>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default PanelValidacionEstados;
