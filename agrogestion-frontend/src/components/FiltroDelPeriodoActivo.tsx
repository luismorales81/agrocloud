import React from 'react';
import { useCampana } from '../contexts/CampanaContext';

interface Props {
  activo: boolean;
  onChange: (valor: boolean) => void;
  /** Si false, no muestra el control (p. ej. vista solo operativa del día). */
  visible?: boolean;
}

/** Checkbox reutilizable: filtrar listados por campana_id del período seleccionado en la barra global. */
const FiltroDelPeriodoActivo: React.FC<Props> = ({ activo, onChange, visible = true }) => {
  const { campanaActiva } = useCampana();

  if (!visible) {
    return null;
  }

  return (
    <label
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        gap: '0.5rem',
        fontSize: '0.875rem',
        color: '#374151',
        cursor: 'pointer',
        userSelect: 'none',
      }}
      title={
        campanaActiva
          ? `Solo registros del período ${campanaActiva.nombre} (${campanaActiva.fechaInicio} — ${campanaActiva.fechaFin})`
          : 'Solo registros del período activo'
      }
    >
      <input
        type="checkbox"
        checked={activo}
        onChange={(e) => onChange(e.target.checked)}
      />
      <span>
        Solo del período activo
        {campanaActiva && (
          <span style={{ color: '#6b7280', marginLeft: '0.25rem' }}>({campanaActiva.codigo})</span>
        )}
      </span>
    </label>
  );
};

export default FiltroDelPeriodoActivo;
