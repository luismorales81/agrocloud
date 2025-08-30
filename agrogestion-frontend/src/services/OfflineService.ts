// Servicio para manejo offline de AgroCloud
export interface OfflineAction {
  id?: number;
  url: string;
  method: string;
  headers: Record<string, string>;
  body?: string;
  timestamp?: number;
}

export interface OfflineData {
  key: string;
  data: any;
  timestamp: number;
}

class OfflineService {
  private db: IDBDatabase | null = null;
  private isOnline = navigator.onLine;
  private syncInProgress = false;

  constructor() {
    this.initDatabase();
    this.setupEventListeners();
  }

  // Inicializar base de datos IndexedDB
  private async initDatabase(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open('AgroCloudOffline', 1);

      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;

        // Store para acciones offline
        if (!db.objectStoreNames.contains('offlineActions')) {
          const store = db.createObjectStore('offlineActions', { 
            keyPath: 'id', 
            autoIncrement: true 
          });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }

        // Store para datos offline
        if (!db.objectStoreNames.contains('offlineData')) {
          const store = db.createObjectStore('offlineData', { keyPath: 'key' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }

        // Store para cache de API
        if (!db.objectStoreNames.contains('apiCache')) {
          const store = db.createObjectStore('apiCache', { keyPath: 'url' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }
      };
    });
  }

  // Configurar event listeners
  private setupEventListeners(): void {
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.syncOfflineData();
      this.notifyStatusChange('online');
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      this.notifyStatusChange('offline');
    });

    // Escuchar mensajes del Service Worker
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.addEventListener('message', (event) => {
        const { type, data } = event.data;
        
        switch (type) {
          case 'SYNC_COMPLETE':
            this.handleSyncComplete(data);
            break;
          case 'OFFLINE_STATUS':
            this.handleOfflineStatus(data);
            break;
        }
      });
    }
  }

  // Verificar si está online
  public isOnlineStatus(): boolean {
    return this.isOnline;
  }

  // Almacenar acción offline
  public async storeOfflineAction(action: Omit<OfflineAction, 'id' | 'timestamp'>): Promise<void> {
    if (!this.db) await this.initDatabase();

    const offlineAction: OfflineAction = {
      ...action,
      timestamp: Date.now()
    };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readwrite');
      const store = transaction.objectStore('offlineActions');
      const request = store.add(offlineAction);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Almacenar datos offline
  public async storeOfflineData(key: string, data: any): Promise<void> {
    if (!this.db) await this.initDatabase();

      const offlineData: OfflineData = {
        key,
        data,
      timestamp: Date.now()
      };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readwrite');
      const store = transaction.objectStore('offlineData');
      const request = store.put(offlineData);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener datos offline
  public async getOfflineData(key: string): Promise<any | null> {
    if (!this.db) await this.initDatabase();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readonly');
      const store = transaction.objectStore('offlineData');
      const request = store.get(key);

      request.onsuccess = () => resolve(request.result?.data || null);
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener todas las acciones offline
  public async getOfflineActions(): Promise<OfflineAction[]> {
    if (!this.db) await this.initDatabase();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readonly');
      const store = transaction.objectStore('offlineActions');
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Eliminar acción offline
  public async removeOfflineAction(id: number): Promise<void> {
    if (!this.db) await this.initDatabase();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineActions'], 'readwrite');
      const store = transaction.objectStore('offlineActions');
      const request = store.delete(id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Sincronizar datos offline
  public async syncOfflineData(): Promise<void> {
    if (!this.isOnline || this.syncInProgress) return;

    this.syncInProgress = true;
    
    try {
      const actions = await this.getOfflineActions();
      
      for (const action of actions) {
        try {
          await this.syncAction(action);
          await this.removeOfflineAction(action.id!);
        } catch (error) {
          console.error('Error sincronizando acción:', error);
        }
      }

      this.notifySyncComplete(actions.length);
    } catch (error) {
      console.error('Error en sincronización:', error);
    } finally {
      this.syncInProgress = false;
    }
  }

  // Sincronizar una acción específica
  private async syncAction(action: OfflineAction): Promise<void> {
    const response = await fetch(action.url, {
      method: action.method,
      headers: action.headers,
      body: action.body
    });

    if (!response.ok) {
      throw new Error(`Error ${response.status}: ${response.statusText}`);
    }

    return response.json();
  }

  // Cache de API
  public async cacheApiResponse(url: string, data: any): Promise<void> {
    if (!this.db) await this.initDatabase();

    const cacheEntry = {
      url,
        data,
        timestamp: Date.now()
      };

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const request = store.put(cacheEntry);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener cache de API
  public async getApiCache(url: string): Promise<any | null> {
    if (!this.db) await this.initDatabase();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readonly');
      const store = transaction.objectStore('apiCache');
      const request = store.get(url);

      request.onsuccess = () => {
        const result = request.result;
        if (result && Date.now() - result.timestamp < 24 * 60 * 60 * 1000) {
          resolve(result.data);
        } else {
          resolve(null);
        }
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Limpiar cache antiguo
  public async cleanOldCache(): Promise<void> {
    if (!this.db) await this.initDatabase();

    const oneDayAgo = Date.now() - 24 * 60 * 60 * 1000;

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readwrite');
      const store = transaction.objectStore('apiCache');
      const index = store.index('timestamp');
      const request = index.openCursor(IDBKeyRange.upperBound(oneDayAgo));

      request.onsuccess = () => {
        const cursor = request.result;
        if (cursor) {
          cursor.delete();
          cursor.continue();
        } else {
          resolve();
        }
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener estadísticas offline
  public async getOfflineStats(): Promise<{
    offlineActionsCount: number;
    offlineDataCount: number;
    cacheSize: number;
  }> {
    if (!this.db) await this.initDatabase();

    const [actions, data, cache] = await Promise.all([
      this.getOfflineActions(),
      this.getAllOfflineData(),
      this.getAllApiCache()
    ]);

    return {
      offlineActionsCount: actions.length,
      offlineDataCount: data.length,
      cacheSize: cache.length
    };
  }

  // Obtener todos los datos offline
  private async getAllOfflineData(): Promise<OfflineData[]> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readonly');
      const store = transaction.objectStore('offlineData');
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener todo el cache de API
  private async getAllApiCache(): Promise<any[]> {
    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['apiCache'], 'readonly');
      const store = transaction.objectStore('apiCache');
      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Notificar cambio de estado
  private notifyStatusChange(status: 'online' | 'offline'): void {
    const event = new CustomEvent('offlineStatusChange', { 
      detail: { status } 
    });
    window.dispatchEvent(event);
  }

  // Notificar sincronización completa
  private notifySyncComplete(syncedCount: number): void {
    const event = new CustomEvent('offlineSyncComplete', { 
      detail: { syncedCount } 
    });
    window.dispatchEvent(event);
  }

  // Manejar sincronización completa
  private handleSyncComplete(data: { syncedCount: number }): void {
    this.notifySyncComplete(data.syncedCount);
  }

  // Manejar estado offline
  private handleOfflineStatus(data: any): void {
    // Implementar lógica adicional si es necesario
    console.log('Estado offline:', data);
  }

  // Limpiar todos los datos offline
  public async clearAllOfflineData(): Promise<void> {
    if (!this.db) await this.initDatabase();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(
        ['offlineActions', 'offlineData', 'apiCache'], 
        'readwrite'
      );

      const actionsStore = transaction.objectStore('offlineActions');
      const dataStore = transaction.objectStore('offlineData');
      const cacheStore = transaction.objectStore('apiCache');

      const clearActions = new Promise<void>((res, rej) => {
        const req = actionsStore.clear();
        req.onsuccess = () => res();
        req.onerror = () => rej(req.error);
      });

      const clearData = new Promise<void>((res, rej) => {
        const req = dataStore.clear();
        req.onsuccess = () => res();
        req.onerror = () => rej(req.error);
      });

      const clearCache = new Promise<void>((res, rej) => {
        const req = cacheStore.clear();
        req.onsuccess = () => res();
        req.onerror = () => rej(req.error);
      });

      Promise.all([clearActions, clearData, clearCache])
        .then(() => resolve())
        .catch(reject);
    });
  }
}

// Instancia singleton
export const offlineService = new OfflineService();
export default offlineService;
