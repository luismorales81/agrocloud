import React from 'react';
import { NavLink } from 'react-router-dom';
import { Icon } from '../../../components/icons';

const estiloEnlaceBase: React.CSSProperties = {
  display: 'inline-flex',
  alignItems: 'center',
  gap: '0.35rem',
  padding: '0.5rem 0.85rem',
  borderRadius: '0.375rem',
  fontSize: '0.875rem',
  fontWeight: 500,
  textDecoration: 'none',
  color: '#4b5563',
  border: '1px solid transparent',
};

const estiloActivo: React.CSSProperties = {
  backgroundColor: '#eff6ff',
  color: '#1d4ed8',
  borderColor: '#bfdbfe',
};

/**
 * Enlaces comunes entre pantallas del módulo Alimentación (porcinos).
 */
const BarraSubnavegacionAlimentacionPorcinos: React.FC = () => {
  return (
    <nav
      aria-label="Secciones de alimentación porcinos"
      style={{
        display: 'flex',
        flexWrap: 'wrap',
        gap: '0.35rem',
        marginBottom: '1.25rem',
        padding: '0.5rem',
        backgroundColor: '#f9fafb',
        borderRadius: '0.5rem',
        border: '1px solid #e5e7eb',
      }}
    >
      <NavLink
        to="/porcinos/alimentacion/insumos-compuestos"
        end
        style={({ isActive }) => ({
          ...estiloEnlaceBase,
          ...(isActive ? estiloActivo : {}),
        })}
      >
        <Icon name="Utensils" size={16} />
        Recetas e insumos
      </NavLink>
      <NavLink
        to="/porcinos/alimentacion/formulas"
        style={({ isActive }) => ({
          ...estiloEnlaceBase,
          ...(isActive ? estiloActivo : {}),
        })}
      >
        <Icon name="Wheat" size={16} />
        Fórmulas
      </NavLink>
      <NavLink
        to="/porcinos/alimentacion/consumos"
        end
        style={({ isActive }) => ({
          ...estiloEnlaceBase,
          ...(isActive ? estiloActivo : {}),
        })}
      >
        <Icon name="List" size={16} />
        Consumos manuales
      </NavLink>
      <NavLink
        to="/porcinos/alimentacion/historial-raciones"
        end
        style={({ isActive }) => ({
          ...estiloEnlaceBase,
          ...(isActive ? estiloActivo : {}),
        })}
      >
        <Icon name="BarChart" size={16} />
        Historial por fórmula
      </NavLink>
      <NavLink
        to="/porcinos/alimentacion/calendario"
        end
        style={({ isActive }) => ({
          ...estiloEnlaceBase,
          ...(isActive ? estiloActivo : {}),
        })}
      >
        <Icon name="Calendar" size={16} />
        Calendario diario
      </NavLink>
    </nav>
  );
};

export default BarraSubnavegacionAlimentacionPorcinos;
