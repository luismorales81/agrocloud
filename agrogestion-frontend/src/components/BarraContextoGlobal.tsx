import React from 'react';
import EmpresaSelector from './EmpresaSelector';
import CampanaSelector from './CampanaSelector';
import CurrencySelector from './CurrencySelector';
import useConceptoTemporalModulo from '../core/hooks/useConceptoTemporalModulo';
import { useAuth } from '../contexts/AuthContext';

/** Barra global: empresa, período/campaña, moneda y sesión (todos los módulos). */
const BarraContextoGlobal: React.FC = () => {
  const { concepto } = useConceptoTemporalModulo();
  const { logout } = useAuth();

  const cerrarSesion = () => {
    logout();
    window.location.href = '/login';
  };

  return (
    <div
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 900,
        display: 'flex',
        flexWrap: 'wrap',
        alignItems: 'center',
        justifyContent: 'space-between',
        gap: '0.75rem',
        padding: '0.5rem 1rem',
        backgroundColor: '#f9fafb',
        borderBottom: '1px solid #e5e7eb',
      }}
    >
      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          gap: '0.75rem',
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

      <div
        style={{
          display: 'flex',
          flexWrap: 'wrap',
          alignItems: 'center',
          gap: '0.75rem',
          marginLeft: 'auto',
        }}
      >
        <CurrencySelector inline />
        <button
          type="button"
          onClick={cerrarSesion}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#ef4444',
            color: 'white',
            border: 'none',
            borderRadius: '0.375rem',
            cursor: 'pointer',
            fontSize: '0.875rem',
            fontWeight: 500,
            whiteSpace: 'nowrap',
          }}
        >
          Cerrar sesión
        </button>
      </div>
    </div>
  );
};

export default BarraContextoGlobal;
