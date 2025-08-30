// Servicio para manejo de almacenamiento offline con IndexedDB
export interface OfflineAction {
  id: string;
  timestamp: number;
  method: 'GET' | 'POST' | 'PUT' | 'DELETE';
  url: string;
  body?: any;
  headers?: Record<string, string>;
  retryCount: number;
  maxRetries: number;
}

export interface OfflineData {
  id: string;
  timestamp: number;
  type: 'field' | 'plot' | 'crop' | 'input' | 'machinery' | 'labor' | 'report';
  data: any;
  action: 'create' | 'update' | 'delete';
}

class OfflineStorageService {
  private dbName = 'AgroCloudOfflineDB';
  private version = 1;
  private db: IDBDatabase | null = null;

  // Inicializar la base de datos
  async init(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.version);

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error abriendo IndexedDB:', request.error);
        reject(request.error);
      };

      request.onsuccess = () => {
        this.db = request.result;
        console.log('✅ [OfflineStorage] IndexedDB inicializada correctamente');
        resolve();
      };

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;
        
        // Store para acciones offline
        if (!db.objectStoreNames.contains('offlineActions')) {
          const actionsStore = db.createObjectStore('offlineActions', { keyPath: 'id' });
          actionsStore.createIndex('timestamp', 'timestamp', { unique: false });
          actionsStore.createIndex('method', 'method', { unique: false });
        }

        // Store para datos offline
        if (!db.objectStoreNames.contains('offlineData')) {
          const dataStore = db.createObjectStore('offlineData', { keyPath: 'id' });
          dataStore.createIndex('timestamp', 'timestamp', { unique: false });
          dataStore.createIndex('type', 'type', { unique: false });
          dataStore.createIndex('action', 'action', { unique: false });
        }

        // Store para cache de API
        if (!db.objectStoreNames.contains('apiCache')) {
          const cacheStore = db.createObjectStore('apiCache', { keyPath: 'url' });
          cacheStore.createIndex('timestamp', 'timestamp', { unique: false });
        }

        console.log('🔧 [OfflineStorage] Base de datos actualizada');
      };
    });
  }

  // Guardar una acción offline
  async saveOfflineAction(action: Omit<OfflineAction, 'id' | 'timestamp'>): Promise<string> {
    if (!this.db) await this.init();

    const id = `action_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const offlineAction: OfflineAction = {
      ...action,
      id,
      timestamp: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readwrite');
      const store = transaction.objectStore('offlineActions');
      const request = store.add(offlineAction);

      request.onsuccess = () => {
        console.log('💾 [OfflineStorage] Acción offline guardada:', id);
        resolve(id);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error guardando acción offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Obtener todas las acciones offline
  async getOfflineActions(): Promise<OfflineAction[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readonly');
      const store = transaction.objectStore('offlineActions');
      const request = store.getAll();

      request.onsuccess = () => {
        const actions = request.result.sort((a, b) => a.timestamp - b.timestamp);
        console.log('📋 [OfflineStorage] Acciones offline obtenidas:', actions.length);
        resolve(actions);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error obteniendo acciones offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Eliminar una acción offline
  async removeOfflineAction(id: string): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readwrite');
      const store = transaction.objectStore('offlineActions');
      const request = store.delete(id);

      request.onsuccess = () => {
        console.log('🗑️ [OfflineStorage] Acción offline eliminada:', id);
        resolve();
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error eliminando acción offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Guardar datos offline
  async saveOfflineData(data: Omit<OfflineData, 'id' | 'timestamp'>): Promise<string> {
    if (!this.db) await this.init();

    const id = `data_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
    const offlineData: OfflineData = {
      ...data,
      id,
      timestamp: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readwrite');
      const store = transaction.objectStore('offlineData');
      const request = store.add(offlineData);

      request.onsuccess = () => {
        console.log('💾 [OfflineStorage] Datos offline guardados:', id);
        resolve(id);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error guardando datos offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Obtener datos offline por tipo
  async getOfflineData(type?: string): Promise<OfflineData[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readonly');
      const store = transaction.objectStore('offlineData');
      const request = type ? store.index('type').getAll(type) : store.getAll();

      request.onsuccess = () => {
        const data = request.result.sort((a, b) => a.timestamp - b.timestamp);
        console.log('📋 [OfflineStorage] Datos offline obtenidos:', data.length);
        resolve(data);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error obteniendo datos offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Eliminar datos offline
  async removeOfflineData(id: string): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readwrite');
      const store = transaction.objectStore('offlineData');
      const request = store.delete(id);

      request.onsuccess = () => {
        console.log('🗑️ [OfflineStorage] Datos offline eliminados:', id);
        resolve();
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error eliminando datos offline:', request.error);
        reject(request.error);
      };
    });
  }

  // Guardar cache de API
  async saveApiCache(url: string, data: any, ttl: number = 3600000): Promise<void> {
    if (!this.db) await this.init();

    const cacheEntry = {
      url,
      data,
      timestamp: Date.now(),
      ttl
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const request = store.put(cacheEntry);

      request.onsuccess = () => {
        console.log('💾 [OfflineStorage] Cache de API guardado:', url);
        resolve();
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error guardando cache de API:', request.error);
        reject(request.error);
      };
    });
  }

  // Obtener cache de API
  async getApiCache(url: string): Promise<any | null> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readonly');
      const store = transaction.objectStore('apiCache');
      const request = store.get(url);

      request.onsuccess = () => {
        const cacheEntry = request.result;
        if (!cacheEntry) {
          resolve(null);
          return;
        }

        // Verificar si el cache ha expirado
        const now = Date.now();
        if (now - cacheEntry.timestamp > cacheEntry.ttl) {
          console.log('⏰ [OfflineStorage] Cache expirado:', url);
          this.removeApiCache(url);
          resolve(null);
          return;
        }

        console.log('📦 [OfflineStorage] Cache de API obtenido:', url);
        resolve(cacheEntry.data);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error obteniendo cache de API:', request.error);
        reject(request.error);
      };
    });
  }

  // Eliminar cache de API
  async removeApiCache(url: string): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const request = store.delete(url);

      request.onsuccess = () => {
        console.log('🗑️ [OfflineStorage] Cache de API eliminado:', url);
        resolve();
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error eliminando cache de API:', request.error);
        reject(request.error);
      };
    });
  }

  // Limpiar cache expirado
  async cleanExpiredCache(): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const request = store.getAll();

      request.onsuccess = () => {
        const now = Date.now();
        const expiredEntries = request.result.filter(
          entry => now - entry.timestamp > entry.ttl
        );

        if (expiredEntries.length === 0) {
          resolve();
          return;
        }

        const deletePromises = expiredEntries.map(entry => 
          this.removeApiCache(entry.url)
        );

        Promise.all(deletePromises)
          .then(() => {
            console.log('🧹 [OfflineStorage] Cache expirado limpiado:', expiredEntries.length);
            resolve();
          })
          .catch(reject);
      };

      request.onerror = () => {
        console.error('❌ [OfflineStorage] Error limpiando cache expirado:', request.error);
        reject(request.error);
      };
    });
  }

  // Obtener estadísticas del almacenamiento
  async getStorageStats(): Promise<{
    offlineActionsCount: number;
    offlineDataCount: number;
    apiCacheCount: number;
    totalSize: number;
  }> {
    if (!this.db) await this.init();

    const [actions, data, cache] = await Promise.all([
      this.getOfflineActions(),
      this.getOfflineData(),
      this.getApiCacheStats()
    ]);

    const totalSize = this.estimateStorageSize(actions, data, cache);

    return {
      offlineActionsCount: actions.length,
      offlineDataCount: data.length,
      apiCacheCount: cache.length,
      totalSize
    };
  }

  // Obtener estadísticas del cache de API
  private async getApiCacheStats(): Promise<any[]> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readonly');
      const store = transaction.objectStore('apiCache');
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Estimar el tamaño del almacenamiento
  private estimateStorageSize(actions: OfflineAction[], data: OfflineData[], cache: any[]): number {
    const actionsSize = JSON.stringify(actions).length;
    const dataSize = JSON.stringify(data).length;
    const cacheSize = JSON.stringify(cache).length;
    
    return actionsSize + dataSize + cacheSize;
  }

  // Limpiar todo el almacenamiento
  async clearAll(): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(
        ['offlineActions', 'offlineData', 'apiCache'], 
        'readwrite'
      );

      const actionsStore = transaction.objectStore('offlineActions');
      const dataStore = transaction.objectStore('offlineData');
      const cacheStore = transaction.objectStore('apiCache');

      const actionsRequest = actionsStore.clear();
      const dataRequest = dataStore.clear();
      const cacheRequest = cacheStore.clear();

      Promise.all([
        new Promise((res, rej) => {
          actionsRequest.onsuccess = res;
          actionsRequest.onerror = rej;
        }),
        new Promise((res, rej) => {
          dataRequest.onsuccess = res;
          dataRequest.onerror = rej;
        }),
        new Promise((res, rej) => {
          cacheRequest.onsuccess = res;
          cacheRequest.onerror = rej;
        })
      ])
        .then(() => {
          console.log('🗑️ [OfflineStorage] Todo el almacenamiento limpiado');
          resolve();
        })
        .catch(reject);
    });
  }
}

// Instancia singleton del servicio
export const offlineStorage = new OfflineStorageService();

// Inicializar automáticamente
offlineStorage.init().catch(console.error);
