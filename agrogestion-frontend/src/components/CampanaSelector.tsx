import React, { useState } from 'react';
import { useCampana } from '../contexts/CampanaContext';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';

const CampanaSelector: React.FC = () => {
  const { campanaActiva, campanas, cambiarCampana, loading, error } = useCampana();
  const { concepto } = useConceptoTemporalModulo();
  const [abierto, setAbierto] = useState(false);

  const estadoColor = (estado: string) => {
    switch (estado) {
      case 'ACTIVA': return 'text-green-700 bg-green-100';
      case 'CERRADA': return 'text-gray-700 bg-gray-200';
      default: return 'text-amber-700 bg-amber-100';
    }
  };

  if (loading) {
    return <span className="text-sm text-gray-500">{concepto.etiquetaPeriodo}…</span>;
  }
  if (error) {
    return <span className="text-sm text-red-600">{error}</span>;
  }
  if (!campanaActiva) {
    return <span className="text-sm text-gray-500">Sin período</span>;
  }

  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setAbierto(!abierto)}
        className="flex items-center gap-2 px-3 py-2 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-gray-50 min-w-[200px]"
        title={concepto.ayuda}
      >
        <span className="text-lg">📅</span>
        <div className="text-left flex-1 min-w-0">
          <div className="text-[10px] uppercase tracking-wide text-gray-500 truncate">
            {concepto.etiquetaPeriodo}
          </div>
          <div className="text-sm font-medium text-gray-900 truncate">{campanaActiva.nombre}</div>
          <div className="text-xs text-gray-500">{campanaActiva.codigo}</div>
        </div>
        <svg className={`w-4 h-4 text-gray-400 shrink-0 ${abierto ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
        </svg>
      </button>
      {abierto && (
        <>
          <div className="fixed inset-0 z-40" onClick={() => setAbierto(false)} />
          <div className="absolute top-full left-0 mt-1 w-80 bg-white border border-gray-200 rounded-md shadow-lg z-50 max-h-72 overflow-y-auto">
            <div className="px-3 py-2 bg-gray-50 border-b text-xs text-gray-600">
              {concepto.ayuda}
            </div>
            {campanas.map((c) => (
              <button
                key={c.id}
                type="button"
                onClick={async () => {
                  await cambiarCampana(c.id);
                  setAbierto(false);
                }}
                className={`w-full px-3 py-2 text-left hover:bg-gray-50 border-b border-gray-100 ${
                  campanaActiva.id === c.id ? 'bg-blue-50' : ''
                }`}
              >
                <div className="flex justify-between items-center">
                  <span className="text-sm font-medium">{c.nombre}</span>
                  <span className={`text-xs px-2 py-0.5 rounded-full ${estadoColor(c.estado)}`}>{c.estado}</span>
                </div>
                <div className="text-xs text-gray-500">{c.fechaInicio} — {c.fechaFin}</div>
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
};

export default CampanaSelector;
