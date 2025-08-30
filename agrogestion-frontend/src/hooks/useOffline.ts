import { useState, useEffect, useCallback } from 'react';
import { offlineStorage, OfflineAction, OfflineData } from '../services/OfflineStorage';

export interface OfflineStats {
  offlineActionsCount: number;
  offlineDataCount: number;
  apiCacheCount: number;
  totalSize: number;
}

export const useOffline = () => {
  console.log('🔧 [useOffline] Inicializando hook...');
  
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [offlineStats, setOfflineStats] = useState<OfflineStats>({
    offlineActionsCount: 0,
    offlineDataCount: 0,
    apiCacheCount: 0,
    totalSize: 0
  });
  const [syncInProgress, setSyncInProgress] = useState(false);

  console.log('🔧 [useOffline] Estado inicial - Online:', isOnline);

  // Actualizar estadísticas del almacenamiento
  const updateStats = useCallback(async () => {
    try {
      const stats = await offlineStorage.getStorageStats();
      setOfflineStats(stats);
      console.log('📊 [useOffline] Estadísticas actualizadas:', stats);
    } catch (error) {
      console.error('❌ [useOffline] Error actualizando estadísticas:', error);
    }
  }, []);

  useEffect(() => {
    console.log('🔧 [useOffline] Configurando event listeners...');
    
    // Event listeners para cambios de conectividad
    const handleOnline = () => {
      console.log('🌐 [useOffline] Conexión restaurada');
      setIsOnline(true);
      // Sincronizar automáticamente cuando vuelve la conexión
      setTimeout(() => {
        syncOfflineData();
      }, 1000);
    };

    const handleOffline = () => {
      console.log('📡 [useOffline] Conexión perdida');
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

    console.log('🔧 [useOffline] Event listeners configurados');

    // Limpiar event listeners
    return () => {
      console.log('🔧 [useOffline] Limpiando event listeners');
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
      clearInterval(cleanCacheInterval);
    };
  }, [updateStats]);

  // Sincronizar datos offline
  const syncOfflineData = async () => {
    console.log('🔄 [useOffline] Iniciando sincronización...');
    if (!isOnline || syncInProgress) {
      console.log('⚠️ [useOffline] Sincronización cancelada - Online:', isOnline, 'Sync en progreso:', syncInProgress);
      return;
    }

    setSyncInProgress(true);
    try {
      console.log('⏳ [useOffline] Sincronizando datos offline...');
      
      // Obtener acciones offline
      const offlineActions = await offlineStorage.getOfflineActions();
      
      if (offlineActions.length === 0) {
        console.log('✅ [useOffline] No hay datos para sincronizar');
        await updateStats();
        return;
      }

      // Procesar cada acción offline
      for (const action of offlineActions) {
        try {
          await processOfflineAction(action);
          await offlineStorage.removeOfflineAction(action.id);
          console.log('✅ [useOffline] Acción sincronizada:', action.id);
        } catch (error) {
          console.error('❌ [useOffline] Error sincronizando acción:', action.id, error);
          
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
      console.log('✅ [useOffline] Sincronización completada');
      
    } catch (error) {
      console.error('❌ [useOffline] Error sincronizando datos:', error);
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
      console.log('💾 [useOffline] Acción offline guardada:', id);
      return id;
    } catch (error) {
      console.error('❌ [useOffline] Error guardando acción offline:', error);
      throw error;
    }
  };

  // Guardar datos offline
  const saveOfflineData = async (data: Omit<OfflineData, 'id' | 'timestamp'>) => {
    try {
      const id = await offlineStorage.saveOfflineData(data);
      await updateStats();
      console.log('💾 [useOffline] Datos offline guardados:', id);
      return id;
    } catch (error) {
      console.error('❌ [useOffline] Error guardando datos offline:', error);
      throw error;
    }
  };

  // Obtener datos offline
  const getOfflineData = async (type?: string) => {
    try {
      const data = await offlineStorage.getOfflineData(type);
      console.log('📋 [useOffline] Datos offline obtenidos:', data.length);
      return data;
    } catch (error) {
      console.error('❌ [useOffline] Error obteniendo datos offline:', error);
      throw error;
    }
  };

  // Limpiar cache
  const cleanCache = async () => {
    console.log('🗑️ [useOffline] Limpiando cache...');
    try {
      await offlineStorage.cleanExpiredCache();
      await updateStats();
      console.log('✅ [useOffline] Cache limpiado');
    } catch (error) {
      console.error('❌ [useOffline] Error limpiando cache:', error);
    }
  };

  // Limpiar todos los datos offline
  const clearAllOfflineData = async () => {
    console.log('🗑️ [useOffline] Limpiando todos los datos offline...');
    try {
      await offlineStorage.clearAll();
      await updateStats();
      console.log('✅ [useOffline] Todos los datos offline limpiados');
    } catch (error) {
      console.error('❌ [useOffline] Error limpiando datos offline:', error);
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

  console.log('🔧 [useOffline] Hook renderizado - Online:', isOnline, 'Pending:', hasPendingData);

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
