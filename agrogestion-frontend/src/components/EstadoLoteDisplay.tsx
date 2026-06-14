import React from 'react';

interface EstadoLoteDisplayProps {
  estado: string;
  estadoConfigurado?: {
    id: number;
    nombre: string;
    color: string;
    icono?: string;
  };
}

/**
 * Componente para mostrar el estado de un lote
 * Si hay estado configurado, lo muestra con su color e icono personalizado
 * Si no, muestra el estado del enum tradicional
 */
const EstadoLoteDisplay: React.FC<EstadoLoteDisplayProps> = ({ estado, estadoConfigurado }) => {
  if (estadoConfigurado) {
    return (
      <span
        style={{
          backgroundColor: estadoConfigurado.color || '#6b7280',
          color: 'white',
          padding: '4px 8px',
          borderRadius: '4px',
          display: 'inline-flex',
          alignItems: 'center',
          gap: '4px',
          fontSize: '12px',
          fontWeight: '500'
        }}
      >
        {estadoConfigurado.icono && <span>{estadoConfigurado.icono}</span>}
        <span>{estadoConfigurado.nombre}</span>
      </span>
    );
  }

  // Fallback al estado tradicional
  const getColorEstado = (estado: string): string => {
    const estadoUpper = estado?.toUpperCase() || '';
    switch (estadoUpper) {
      case 'DISPONIBLE': return '#10b981';
      case 'PREPARADO': return '#f59e0b';
      case 'SEMBRADO': return '#3b82f6';
      case 'EN_CRECIMIENTO': return '#8b5cf6';
      case 'EN_FLORACION': return '#ec4899';
      case 'EN_FRUTIFICACION': return '#f97316';
      case 'LISTO_PARA_COSECHA': return '#22c55e';
      case 'EN_COSECHA': return '#eab308';
      case 'COSECHADO': return '#14b8a6';
      case 'EN_DESCANSO': return '#64748b';
      case 'EN_PREPARACION': return '#6366f1';
      case 'ENFERMO': return '#ef4444';
      case 'ABANDONADO': return '#6b7280';
      default: return '#6b7280';
    }
  };

  const getIconoEstado = (estado: string): string => {
    const estadoUpper = estado?.toUpperCase() || '';
    switch (estadoUpper) {
      case 'DISPONIBLE': return '🟢';
      case 'PREPARADO': return '🟡';
      case 'SEMBRADO': return '🌱';
      case 'EN_CRECIMIENTO': return '🌿';
      case 'EN_FLORACION': return '🌸';
      case 'EN_FRUTIFICACION': return '🍎';
      case 'LISTO_PARA_COSECHA': return '📦';
      case 'EN_COSECHA': return '⚙️';
      case 'COSECHADO': return '✅';
      case 'EN_DESCANSO': return '😴';
      case 'EN_PREPARACION': return '🔧';
      case 'ENFERMO': return '⚠️';
      case 'ABANDONADO': return '❌';
      default: return '📋';
    }
  };

  return (
    <span
      style={{
        backgroundColor: getColorEstado(estado),
        color: 'white',
        padding: '4px 8px',
        borderRadius: '4px',
        display: 'inline-flex',
        alignItems: 'center',
        gap: '4px',
        fontSize: '12px',
        fontWeight: '500'
      }}
    >
      <span>{getIconoEstado(estado)}</span>
      <span>{estado}</span>
    </span>
  );
};

export default EstadoLoteDisplay;

