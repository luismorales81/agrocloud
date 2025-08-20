import React from 'react';
import { useOnlineStatus } from '../hooks/useResponsive';

interface OfflineIndicatorProps {
  showDetails?: boolean;
}

const OfflineIndicator: React.FC<OfflineIndicatorProps> = ({ showDetails = false }) => {
  const isOnline = useOnlineStatus();

  if (isOnline) {
    return null; // No mostrar nada si está online
  }

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      background: '#ef4444',
      color: 'white',
      padding: '8px 16px',
      fontSize: '0.875rem',
      textAlign: 'center',
      zIndex: 9999,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      gap: '8px',
      boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
    }}>
      <div style={{
        width: '8px',
        height: '8px',
        borderRadius: '50%',
        backgroundColor: 'white',
        animation: 'pulse 2s infinite'
      }} />
      
      <span style={{ fontWeight: '500' }}>
        Sin conexión a internet
      </span>
      
      {showDetails && (
        <span style={{ fontSize: '0.75rem', opacity: 0.9 }}>
          - Algunas funciones están disponibles offline
        </span>
      )}
      
      <style jsx>{`
        @keyframes pulse {
          0% { opacity: 1; }
          50% { opacity: 0.5; }
          100% { opacity: 1; }
        }
      `}</style>
    </div>
  );
};

export default OfflineIndicator;
