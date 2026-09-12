import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCampana } from '../contexts/CampanaContext';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';
import { useModule } from '../core/hooks/useModule';

const CampanaSelector: React.FC = () => {
  const { campanaActiva, campanas, cambiarCampana, loading, error } = useCampana();
  const { concepto } = useConceptoTemporalModulo();
  const { currentModule } = useModule();
  const navigate = useNavigate();
  const [abierto, setAbierto] = useState(false);

  const estadoColor = (estado: string) => {
    switch (estado) {
      case 'ACTIVA': return { fondo: '#d1fae5', color: '#065f46' };
      case 'CERRADA': return { fondo: '#e5e7eb', color: '#374151' };
      default: return { fondo: '#fef3c7', color: '#92400e' };
    }
  };

  const irAGestionPeriodos = () => {
    if (!currentModule) return;
    const ruta =
      currentModule === 'cultivos'
        ? `/${currentModule}/configuracion?tab=periodos`
        : `/${currentModule}/configuracion/periodos`;
    navigate(ruta);
    setAbierto(false);
  };

  const periodoVisible = campanaActiva ?? campanas[0] ?? null;

  if (loading) {
    return <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>{concepto.etiquetaPeriodo}…</span>;
  }
  if (error) {
    return <span style={{ fontSize: '0.875rem', color: '#dc2626' }}>{error}</span>;
  }
  if (!periodoVisible) {
    return (
      <button
        type="button"
        onClick={irAGestionPeriodos}
        style={{
          fontSize: '0.875rem',
          color: '#6b7280',
          background: '#fff',
          border: '1px dashed #d1d5db',
          borderRadius: '0.375rem',
          padding: '0.5rem 0.75rem',
          cursor: 'pointer',
        }}
        title="Configurar período de gestión"
      >
        Sin período — configurar
      </button>
    );
  }

  return (
    <div style={{ position: 'relative' }}>
      <button
        type="button"
        onClick={() => setAbierto(!abierto)}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '0.5rem',
          padding: '0.5rem 0.75rem',
          backgroundColor: '#fff',
          border: '1px solid #d1d5db',
          borderRadius: '0.375rem',
          boxShadow: '0 1px 2px rgba(0,0,0,0.05)',
          minWidth: '200px',
          cursor: 'pointer',
        }}
        title={concepto.ayuda}
      >
        <span style={{ fontSize: '1.125rem' }}>📅</span>
        <div style={{ textAlign: 'left', flex: 1, minWidth: 0 }}>
          <div style={{ fontSize: '10px', textTransform: 'uppercase', letterSpacing: '0.05em', color: '#6b7280' }}>
            {concepto.etiquetaPeriodo}
          </div>
          <div style={{ fontSize: '0.875rem', fontWeight: 500, color: '#111827', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {periodoVisible.nombre}
          </div>
          <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>{periodoVisible.codigo}</div>
        </div>
        <span style={{ color: '#9ca3af', fontSize: '0.75rem' }}>{abierto ? '▲' : '▼'}</span>
      </button>
      {abierto && (
        <>
          <div
            style={{ position: 'fixed', inset: 0, zIndex: 40 }}
            onClick={() => setAbierto(false)}
          />
          <div
            style={{
              position: 'absolute',
              top: '100%',
              left: 0,
              marginTop: '0.25rem',
              width: '20rem',
              backgroundColor: '#fff',
              border: '1px solid #e5e7eb',
              borderRadius: '0.375rem',
              boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
              zIndex: 50,
              maxHeight: '18rem',
              overflowY: 'auto',
            }}
          >
            <div style={{ padding: '0.5rem 0.75rem', backgroundColor: '#f9fafb', borderBottom: '1px solid #e5e7eb', fontSize: '0.75rem', color: '#4b5563' }}>
              {concepto.ayuda}
            </div>
            {campanas.map((c) => {
              const estilo = estadoColor(c.estado);
              return (
                <button
                  key={c.id}
                  type="button"
                  onClick={async () => {
                    await cambiarCampana(c.id);
                    setAbierto(false);
                  }}
                  style={{
                    width: '100%',
                    padding: '0.5rem 0.75rem',
                    textAlign: 'left',
                    border: 'none',
                    borderBottom: '1px solid #f3f4f6',
                    backgroundColor: periodoVisible.id === c.id ? '#eff6ff' : '#fff',
                    cursor: 'pointer',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span style={{ fontSize: '0.875rem', fontWeight: 500 }}>{c.nombre}</span>
                    <span style={{ fontSize: '0.75rem', padding: '0.125rem 0.5rem', borderRadius: '9999px', backgroundColor: estilo.fondo, color: estilo.color }}>
                      {c.estado}
                    </span>
                  </div>
                  <div style={{ fontSize: '0.75rem', color: '#6b7280' }}>{c.fechaInicio} — {c.fechaFin}</div>
                </button>
              );
            })}
            <button
              type="button"
              onClick={irAGestionPeriodos}
              style={{
                width: '100%',
                padding: '0.75rem',
                border: 'none',
                borderTop: '1px solid #e5e7eb',
                backgroundColor: '#f9fafb',
                color: '#2563eb',
                fontSize: '0.875rem',
                fontWeight: 500,
                cursor: 'pointer',
              }}
            >
              Administrar períodos →
            </button>
          </div>
        </>
      )}
    </div>
  );
};

export default CampanaSelector;
