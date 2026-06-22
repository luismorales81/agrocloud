import React from 'react';
import EmpresaSelector from './EmpresaSelector';
import CampanaSelector from './CampanaSelector';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';

/** Barra global: empresa + período/campaña según el módulo activo. */
const BarraContextoGlobal: React.FC = () => {
  const { concepto } = useConceptoTemporalModulo();

  return (
    <div
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 900,
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        gap: '0.75rem',
        padding: '0.5rem 1rem',
        backgroundColor: '#f9fafb',
        borderBottom: '1px solid #e5e7eb',
      }}
    >
      <EmpresaSelector />
      <CampanaSelector />
      <span
        style={{
          fontSize: '0.75rem',
          color: '#6b7280',
          padding: '0.25rem 0.5rem',
          backgroundColor: '#fff',
          border: '1px solid #e5e7eb',
          borderRadius: '0.375rem',
        }}
        title={concepto.ayuda}
      >
        Unidad operativa: <strong style={{ color: '#374151' }}>{concepto.unidadOperativa}</strong>
      </span>
    </div>
  );
};

export default BarraContextoGlobal;
