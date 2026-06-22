import React, { useEffect, useState } from 'react';
import { estadosLoteService, ProgresoEstadoLote } from '../services/domain/estadosLoteService';
import { useEmpresa } from '../contexts/EmpresaContext';

interface ProgresoEstadoLotePanelProps {
  loteId: number | null;
  onRecargar?: () => void;
}

const ProgresoEstadoLotePanel: React.FC<ProgresoEstadoLotePanelProps> = ({ loteId }) => {
  const { empresaId } = useEmpresa();
  const [progreso, setProgreso] = useState<ProgresoEstadoLote | null>(null);
  const [cargando, setCargando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!loteId || loteId <= 0) {
      setProgreso(null);
      return;
    }
    let cancelado = false;
    const cargar = async () => {
      setCargando(true);
      setError(null);
      try {
        const data = await estadosLoteService.obtenerProgreso(loteId, empresaId ?? undefined);
        if (!cancelado) setProgreso(data);
      } catch {
        if (!cancelado) setError('No se pudo cargar el progreso del lote.');
      } finally {
        if (!cancelado) setCargando(false);
      }
    };
    cargar();
    return () => { cancelado = true; };
  }, [loteId, empresaId]);

  if (!loteId || loteId <= 0) return null;

  if (cargando) {
    return (
      <div style={{ padding: '12px', background: '#f9fafb', borderRadius: '8px', fontSize: '13px', color: '#6b7280' }}>
        Cargando camino del lote...
      </div>
    );
  }

  if (error || !progreso) {
    return null;
  }

  return (
    <div style={{
      marginTop: '12px',
      padding: '14px',
      background: 'linear-gradient(135deg, #f0fdf4 0%, #ecfdf5 100%)',
      border: '1px solid #bbf7d0',
      borderRadius: '10px',
    }}>
      <div style={{ fontWeight: 700, fontSize: '14px', color: '#166534', marginBottom: '10px' }}>
        Camino del lote
        {progreso.estadoActual && (
          <span style={{ fontWeight: 500, color: '#15803d', marginLeft: '8px' }}>
            — {progreso.estadoActual.icono} {progreso.estadoActual.nombre}
          </span>
        )}
      </div>

      {progreso.usaConfiguracion && progreso.caminoEstados.length > 0 && (
        <div style={{
          display: 'flex',
          flexWrap: 'wrap',
          gap: '6px',
          alignItems: 'center',
          marginBottom: '12px',
        }}>
          {progreso.caminoEstados.map((paso, idx) => (
            <React.Fragment key={paso.id}>
              <span style={{
                padding: '4px 10px',
                borderRadius: '999px',
                fontSize: '12px',
                fontWeight: paso.actual ? 700 : 500,
                background: paso.actual ? (paso.color || '#10b981') : paso.completado ? '#d1fae5' : '#fff',
                color: paso.actual ? '#fff' : paso.completado ? '#065f46' : '#374151',
                border: paso.actual ? 'none' : '1px solid #d1d5db',
                opacity: !paso.actual && !paso.completado ? 0.75 : 1,
              }}>
                {paso.icono} {paso.nombre}
              </span>
              {idx < progreso.caminoEstados.length - 1 && (
                <span style={{ color: '#9ca3af', fontSize: '12px' }}>→</span>
              )}
            </React.Fragment>
          ))}
        </div>
      )}

      {progreso.proximoEstado && (
        <div style={{ fontSize: '13px', color: '#374151', marginBottom: '8px' }}>
          <strong>Próximo:</strong> {progreso.proximoEstado.nombre}
          {progreso.diasParaProximoEstado != null && progreso.diasParaProximoEstado > 0 && (
            <span style={{ color: '#6b7280' }}> (≈ {progreso.diasParaProximoEstado} días desde siembra)</span>
          )}
        </div>
      )}

      {progreso.tareas.length > 0 && (
        <div style={{ marginBottom: '8px' }}>
          <div style={{ fontSize: '12px', fontWeight: 600, color: '#374151', marginBottom: '4px' }}>Tareas del estado</div>
          <ul style={{ margin: 0, paddingLeft: '18px', fontSize: '12px' }}>
            {progreso.tareas.map((t) => (
              <li key={t.tipoLabor} style={{ color: t.completada ? '#059669' : '#b45309' }}>
                {t.completada ? '✓' : '○'} {t.nombreTarea}
                {t.esObligatoria && !t.completada && ' (obligatoria)'}
              </li>
            ))}
          </ul>
        </div>
      )}

      <div style={{
        fontSize: '12px',
        color: '#4b5563',
        background: '#fff',
        padding: '8px 10px',
        borderRadius: '6px',
        border: '1px solid #e5e7eb',
      }}>
        {progreso.mensajeAvance}
      </div>
    </div>
  );
};

export default ProgresoEstadoLotePanel;
