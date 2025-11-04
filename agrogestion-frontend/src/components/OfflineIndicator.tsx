import React, { useState } from 'react';
import { useOffline } from '../hooks/useOffline';

interface OfflineIndicatorProps {
  variant?: 'floating' | 'inline';
}

const OfflineIndicator: React.FC<OfflineIndicatorProps> = ({ variant = 'floating' }) => {
  const {
    isOnline,
    offlineStats,
    syncInProgress,
    hasPendingData,
    syncOfflineData,
    cleanCache,
    clearAllOfflineData,
    getSyncStatus
  } = useOffline();

  const [showDetails, setShowDetails] = useState(false);

  // Obtener color y texto según el estado
  const getStatusInfo = () => {
    if (syncInProgress) {
      return {
        color: 'bg-blue-500',
        text: 'Sincronizando...',
        icon: '🔄'
      };
    }
    
    if (!isOnline) {
      return {
        color: 'bg-red-500',
        text: 'Sin conexión',
        icon: '📡'
      };
    }
    
    if (hasPendingData) {
      return {
        color: 'bg-yellow-500',
        text: 'Datos pendientes',
        icon: '⏳'
      };
    }
    
    return {
      color: 'bg-green-500',
      text: 'Conectado',
      icon: '🌐'
    };
  };

  const statusInfo = getStatusInfo();

  // Formatear tamaño de datos
  const formatDataSize = (bytes: number) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  // Si es variante inline, renderizar versión simplificada
  if (variant === 'inline') {
    const statusColor = statusInfo.color === 'bg-green-500' ? '#10b981' :
                        statusInfo.color === 'bg-red-500' ? '#ef4444' :
                        statusInfo.color === 'bg-yellow-500' ? '#f59e0b' :
                        statusInfo.color === 'bg-blue-500' ? '#3b82f6' : '#6b7280';
    
    return (
      <div style={{ marginBottom: '1rem' }}>
        <div style={{
          width: '100%',
          padding: '0.5rem',
          backgroundColor: statusColor,
          color: 'white',
          border: 'none',
          borderRadius: '0.375rem',
          fontSize: '0.75rem',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '0.25rem',
          fontWeight: '500'
        }}>
          <span>{statusInfo.icon}</span>
          <span>{statusInfo.text}</span>
          {hasPendingData && (
            <span style={{
              backgroundColor: 'white',
              color: '#ef4444',
              borderRadius: '50%',
              width: '18px',
              height: '18px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '0.625rem',
              fontWeight: 'bold',
              marginLeft: '0.25rem'
            }}>
              {offlineStats.offlineActionsCount}
            </span>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="fixed bottom-4 right-4 z-50">
      {/* Indicador principal */}
      <div className="flex items-center space-x-2">
        <button
          onClick={() => setShowDetails(!showDetails)}
          className={`
            flex items-center space-x-2 px-3 py-2 rounded-lg text-white text-sm font-medium
            shadow-lg hover:shadow-xl transition-all duration-200
            ${statusInfo.color}
          `}
        >
          <span className="text-lg">{statusInfo.icon}</span>
          <span>{statusInfo.text}</span>
          {hasPendingData && (
            <span className="bg-white text-red-500 rounded-full w-5 h-5 flex items-center justify-center text-xs font-bold">
              {offlineStats.offlineActionsCount}
            </span>
          )}
        </button>
      </div>

      {/* Panel de detalles */}
      {showDetails && (
        <div className="absolute bottom-full right-0 mb-2 w-80 bg-white rounded-lg shadow-xl border border-gray-200 p-4">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-semibold text-gray-800">Estado Offline</h3>
            <button
              onClick={() => setShowDetails(false)}
              className="text-gray-400 hover:text-gray-600"
            >
              ✕
            </button>
          </div>

          {/* Estado de conexión */}
          <div className="mb-4">
            <div className="flex items-center space-x-2 mb-2">
              <span className={`w-3 h-3 rounded-full ${statusInfo.color}`}></span>
              <span className="text-sm font-medium text-gray-700">
                {statusInfo.text}
              </span>
            </div>
            <p className="text-xs text-gray-500">
              Última actualización: {new Date().toLocaleTimeString()}
            </p>
          </div>

          {/* Estadísticas */}
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-700 mb-2">Almacenamiento Local</h4>
            <div className="space-y-2">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Acciones pendientes:</span>
                <span className="font-medium">{offlineStats.offlineActionsCount}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Datos guardados:</span>
                <span className="font-medium">{offlineStats.offlineDataCount}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Cache API:</span>
                <span className="font-medium">{offlineStats.apiCacheCount}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">Tamaño total:</span>
                <span className="font-medium">{formatDataSize(offlineStats.totalSize)}</span>
              </div>
            </div>
          </div>

          {/* Acciones */}
          <div className="space-y-2">
            {hasPendingData && isOnline && (
              <button
                onClick={syncOfflineData}
                disabled={syncInProgress}
                className={`
                  w-full px-3 py-2 rounded-md text-sm font-medium transition-colors
                  ${syncInProgress 
                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed' 
                    : 'bg-blue-500 text-white hover:bg-blue-600'
                  }
                `}
              >
                {syncInProgress ? 'Sincronizando...' : '🔄 Sincronizar Ahora'}
              </button>
            )}

            <button
              onClick={cleanCache}
              className="w-full px-3 py-2 bg-yellow-500 text-white rounded-md text-sm font-medium hover:bg-yellow-600 transition-colors"
            >
              🧹 Limpiar Cache
            </button>

            <button
              onClick={clearAllOfflineData}
              className="w-full px-3 py-2 bg-red-500 text-white rounded-md text-sm font-medium hover:bg-red-600 transition-colors"
            >
              🗑️ Limpiar Todo
            </button>
          </div>

          {/* Información adicional */}
          <div className="mt-4 pt-4 border-t border-gray-200">
            <p className="text-xs text-gray-500">
              Los datos se sincronizan automáticamente cuando vuelve la conexión.
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default OfflineIndicator;
