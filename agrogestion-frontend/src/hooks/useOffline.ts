import { useState, useEffect, useCallback } from 'react';
import { offlineStorage, OfflineAction, OfflineData } from '../services/OfflineStorage';

export interface OfflineStats {
  offlineActionsCount: number;
  offlineDataCount: number;
  apiCacheCount: number;
  totalSize: number;
}

export const useOffline = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [offlineStats, setOfflineStats] = useState<OfflineStats>({
    offlineActionsCount: 0,
    offlineDataCount: 0,
    apiCacheCount: 0,
    totalSize: 0
  });
  const [syncInProgress, setSyncInProgress] = useState(false);

  // Actualizar estadísticas del almacenamiento
  const updateStats = useCallback(async () => {
    try {
      const stats = await offlineStorage.getStorageStats();
      setOfflineStats(stats);
    } catch (error) {
      console.error('Error actualizando estadísticas offline:', error);
    }
  }, []);

  useEffect(() => {
    // Event listeners para cambios de conectividad
    const handleOnline = () => {
      setIsOnline(true);
      // Sincronizar automáticamente cuando vuelve la conexión
      setTimeout(() => {
        syncOfflineData();
      }, 1000);
    };

    const handleOffline = () => {
      setIsOnline(false);
    };

    // Agregar event listeners
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Actualizar estadísticas iniciales
    updateStats();

    // Limpiar cache expirado periódicamente
    const cleanCacheInterval = setInterval(() => {
      offlineStorage.cleanExpiredCache().catch(console.error);
    }, 300000); // Cada 5 minutos

    // Limpiar event listeners
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(cleanCacheInterval);
    };
  }, [updateStats]);

  // Sincronizar datos offline
  const syncOfflineData = async () => {
    if (!isOnline || syncInProgress) {
      return;
    }

    setSyncInProgress(true);
    try {
      // Obtener acciones offline
      const offlineActions = await offlineStorage.getOfflineActions();
      
      if (offlineActions.length === 0) {
        await updateStats();
        return;
      }

      // Procesar cada acción offline
      for (const action of offlineActions) {
        try {
          await processOfflineAction(action);
          await offlineStorage.removeOfflineAction(action.id);
        } catch (error) {
          console.error('Error sincronizando acción offline:', action.id, error);
          
          // Incrementar contador de reintentos
          if (action.retryCount < action.maxRetries) {
            const updatedAction = {
              ...action,
              retryCount: action.retryCount + 1
            };
            await offlineStorage.removeOfflineAction(action.id);
            await offlineStorage.saveOfflineAction(updatedAction);
          }
        }
      }

      await updateStats();
      
    } catch (error) {
      console.error('Error sincronizando datos offline:', error);
    } finally {
      setSyncInProgress(false);
    }
  };

  // Procesar una acción offline
  const processOfflineAction = async (action: OfflineAction) => {
    const response = await fetch(action.url, {
      method: action.method,
      headers: action.headers || {
        'Content-Type': 'application/json'
      },
      body: action.body ? JSON.stringify(action.body) : undefined
    });

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return response;
  };

  // Guardar acción offline
  const saveOfflineAction = async (action: Omit<OfflineAction, 'id' | 'timestamp'>) => {
    try {
      const id = await offlineStorage.saveOfflineAction(action);
      await updateStats();
      return id;
    } catch (error) {
      console.error('Error guardando acción offline:', error);
      throw error;
    }
  };

  // Guardar datos offline
  const saveOfflineData = async (data: Omit<OfflineData, 'id' | 'timestamp'>) => {
    try {
      const id = await offlineStorage.saveOfflineData(data);
      await updateStats();
      return id;
    } catch (error) {
      console.error('Error guardando datos offline:', error);
      throw error;
    }
  };

  // Obtener datos offline
  const getOfflineData = async (type?: string) => {
    try {
      const data = await offlineStorage.getOfflineData(type);
      return data;
    } catch (error) {
      console.error('Error obteniendo datos offline:', error);
      throw error;
    }
  };

  // Limpiar cache
  const cleanCache = async () => {
    try {
      await offlineStorage.cleanExpiredCache();
      await updateStats();
    } catch (error) {
      console.error('Error limpiando cache offline:', error);
    }
  };

  // Limpiar todos los datos offline
  const clearAllOfflineData = async () => {
    try {
      await offlineStorage.clearAll();
      await updateStats();
    } catch (error) {
      console.error('Error limpiando datos offline:', error);
    }
  };

  // Verificar si hay datos pendientes de sincronización
  const hasPendingData = offlineStats.offlineActionsCount > 0;

  // Obtener estado de sincronización
  const getSyncStatus = () => {
    if (syncInProgress) return 'sincronizando';
    if (hasPendingData) return 'pendiente';
    return 'actualizado';
  };


  return {
    // Estado
    isOnline,
    offlineStats,
    syncInProgress,
    hasPendingData,
    
    // Métodos
    syncOfflineData,
    saveOfflineAction,
    saveOfflineData,
    getOfflineData,
    cleanCache,
    clearAllOfflineData,
    updateStats,
    
    // Utilidades
    getSyncStatus
  };
};
