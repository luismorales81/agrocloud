interface OfflineData {
  id?: number;
  key: string;
  data: any;
  timestamp: number;
  synced: boolean;
}

interface PendingSync {
  id?: number;
  url: string;
  method: string;
  headers: any;
  body?: any;
  timestamp: number;
}

class OfflineService {
  private db: IDBDatabase | null = null;
  private readonly DB_NAME = 'AgroCloudDB';
  private readonly DB_VERSION = 1;

  // Inicializar la base de datos
  async init(): Promise<void> {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.DB_NAME, this.DB_VERSION);

      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve();
      };

      request.onupgradeneeded = (event) => {
        const db = (event.target as IDBOpenDBRequest).result;

        // Store para datos offline
        if (!db.objectStoreNames.contains('offlineData')) {
          const store = db.createObjectStore('offlineData', { keyPath: 'id', autoIncrement: true });
          store.createIndex('key', 'key', { unique: false });
          store.createIndex('timestamp', 'timestamp', { unique: false });
          store.createIndex('synced', 'synced', { unique: false });
        }

        // Store para datos pendientes de sincronización
        if (!db.objectStoreNames.contains('pendingSync')) {
          const store = db.createObjectStore('pendingSync', { keyPath: 'id', autoIncrement: true });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }

        // Store para cache de datos
        if (!db.objectStoreNames.contains('dataCache')) {
          const store = db.createObjectStore('dataCache', { keyPath: 'key' });
          store.createIndex('timestamp', 'timestamp', { unique: false });
        }
      };
    });
  }

  // Guardar datos offline
  async saveOfflineData(key: string, data: any): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readwrite');
      const store = transaction.objectStore('offlineData');

      const offlineData: OfflineData = {
        key,
        data,
        timestamp: Date.now(),
        synced: false
      };

      const request = store.add(offlineData);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener datos offline
  async getOfflineData(key: string): Promise<any | null> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readonly');
      const store = transaction.objectStore('offlineData');
      const index = store.index('key');

      const request = index.get(key);

      request.onsuccess = () => {
        const result = request.result;
        resolve(result ? result.data : null);
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener todos los datos offline
  async getAllOfflineData(): Promise<OfflineData[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readonly');
      const store = transaction.objectStore('offlineData');

      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Marcar datos como sincronizados
  async markAsSynced(id: number): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['offlineData'], 'readwrite');
      const store = transaction.objectStore('offlineData');

      const getRequest = store.get(id);
      getRequest.onsuccess = () => {
        const data = getRequest.result;
        if (data) {
          data.synced = true;
          const updateRequest = store.put(data);
          updateRequest.onsuccess = () => resolve();
          updateRequest.onerror = () => reject(updateRequest.error);
        } else {
          resolve();
        }
      };
      getRequest.onerror = () => reject(getRequest.error);
    });
  }

  // Guardar petición pendiente de sincronización
  async savePendingSync(url: string, method: string, headers: any, body?: any): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pendingSync'], 'readwrite');
      const store = transaction.objectStore('pendingSync');

      const pendingSync: PendingSync = {
        url,
        method,
        headers,
        body,
        timestamp: Date.now()
      };

      const request = store.add(pendingSync);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener todas las peticiones pendientes
  async getPendingSyncs(): Promise<PendingSync[]> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pendingSync'], 'readonly');
      const store = transaction.objectStore('pendingSync');

      const request = store.getAll();

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  }

  // Eliminar petición sincronizada
  async removePendingSync(id: number): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['pendingSync'], 'readwrite');
      const store = transaction.objectStore('pendingSync');

      const request = store.delete(id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Guardar en cache
  async saveToCache(key: string, data: any): Promise<void> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['dataCache'], 'readwrite');
      const store = transaction.objectStore('dataCache');

      const cacheData = {
        key,
        data,
        timestamp: Date.now()
      };

      const request = store.put(cacheData);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }

  // Obtener del cache
  async getFromCache(key: string): Promise<any | null> {
    if (!this.db) await this.init();

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['dataCache'], 'readonly');
      const store = transaction.objectStore('dataCache');

      const request = store.get(key);

      request.onsuccess = () => {
        const result = request.result;
        resolve(result ? result.data : null);
      };
      request.onerror = () => reject(request.error);
    });
  }

  // Limpiar cache antiguo (más de 7 días)
  async cleanOldCache(): Promise<void> {
    if (!this.db) await this.init();

    const sevenDaysAgo = Date.now() - (7 * 24 * 60 * 60 * 1000);

    return new Promise((resolve, reject) => {
      const transaction = this.db!.transaction(['dataCache'], 'readwrite');
      const store = transaction.objectStore('dataCache');
      const index = store.index('timestamp');

      const request = index.openCursor(IDBKeyRange.upperBound(sevenDaysAgo));

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

  // Verificar si hay conexión
  async isOnline(): Promise<boolean> {
    try {
      const response = await fetch('/api/health', { 
        method: 'HEAD',
        cache: 'no-cache'
      });
      return response.ok;
    } catch {
      return false;
    }
  }

  // Sincronizar datos pendientes
  async syncPendingData(): Promise<void> {
    const isOnline = await this.isOnline();
    if (!isOnline) return;

    try {
      // Sincronizar datos offline
      const offlineData = await this.getAllOfflineData();
      const unsyncedData = offlineData.filter(data => !data.synced);

      for (const data of unsyncedData) {
        try {
          // Aquí se implementaría la lógica específica de sincronización
          // Por ejemplo, enviar datos al servidor
          await this.markAsSynced(data.id!);
        } catch (error) {
          console.error('Error sincronizando dato:', error);
        }
      }

      // Sincronizar peticiones pendientes
      const pendingSyncs = await this.getPendingSyncs();

      for (const sync of pendingSyncs) {
        try {
          const response = await fetch(sync.url, {
            method: sync.method,
            headers: sync.headers,
            body: sync.body
          });

          if (response.ok) {
            await this.removePendingSync(sync.id!);
          }
        } catch (error) {
          console.error('Error sincronizando petición:', error);
        }
      }
    } catch (error) {
      console.error('Error en sincronización:', error);
    }
  }

  // Registrar el service worker
  async registerServiceWorker(): Promise<void> {
    if ('serviceWorker' in navigator) {
      try {
        const registration = await navigator.serviceWorker.register('/sw.js');
        console.log('Service Worker registrado:', registration);

        // Solicitar sincronización en background
        if ('sync' in registration && typeof (registration as any).sync?.register === 'function') {
          await (registration as any).sync.register('background-sync');
        }
      } catch (error) {
        console.error('Error registrando Service Worker:', error);
      }
    }
  }

  // Instalar PWA
  async installPWA(): Promise<void> {
    if ('serviceWorker' in navigator && 'PushManager' in window) {
      try {
        const registration = await navigator.serviceWorker.ready;
        
        // Solicitar permisos de notificaciones
        const permission = await Notification.requestPermission();
        if (permission === 'granted') {
          console.log('Permisos de notificación concedidos');
        }
      } catch (error) {
        console.error('Error instalando PWA:', error);
      }
    }
  }

  // Mostrar banner de instalación
  showInstallBanner(): void {
    let deferredPrompt: any;
    let bannerShown = false;

    // Verificar si ya se mostró el banner en esta sesión
    if (sessionStorage.getItem('installBannerDismissed') === 'true') {
      return;
    }

    window.addEventListener('beforeinstallprompt', (e) => {
      e.preventDefault();
      deferredPrompt = e;

      // Evitar mostrar múltiples banners
      if (bannerShown) return;
      bannerShown = true;

      // Crear el banner usando createElement para mejor control
      const bannerContainer = document.createElement('div');
      bannerContainer.id = 'install-banner';
      bannerContainer.style.cssText = `
        position: fixed;
        bottom: 20px;
        left: 20px;
        right: 20px;
        background: #2563eb;
        color: white;
        padding: 16px;
        border-radius: 8px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-family: Arial, sans-serif;
      `;

      // Contenido de texto
      const textContent = document.createElement('div');
      textContent.innerHTML = `
        <strong>Instalar AgroCloud</strong>
        <div style="font-size: 0.875rem; opacity: 0.9; margin-top: 4px;">
          Accede más rápido desde tu pantalla de inicio
        </div>
      `;

      // Contenedor de botones
      const buttonContainer = document.createElement('div');
      buttonContainer.style.cssText = 'display: flex; gap: 8px;';

      // Botón de instalar
      const installButton = document.createElement('button');
      installButton.textContent = 'Instalar';
      installButton.style.cssText = `
        background: white;
        color: #2563eb;
        border: none;
        padding: 8px 16px;
        border-radius: 4px;
        font-weight: 500;
        cursor: pointer;
        font-size: 14px;
      `;

      // Botón de descartar
      const dismissButton = document.createElement('button');
      dismissButton.textContent = 'Ahora no';
      dismissButton.style.cssText = `
        background: transparent;
        color: white;
        border: 1px solid white;
        padding: 8px 16px;
        border-radius: 4px;
        cursor: pointer;
        font-size: 14px;
      `;

      // Agregar botones al contenedor
      buttonContainer.appendChild(installButton);
      buttonContainer.appendChild(dismissButton);

      // Agregar contenido al banner
      bannerContainer.appendChild(textContent);
      bannerContainer.appendChild(buttonContainer);

      // Función para remover el banner
      const removeBanner = () => {
        if (bannerContainer && bannerContainer.parentNode) {
          bannerContainer.parentNode.removeChild(bannerContainer);
          sessionStorage.setItem('installBannerDismissed', 'true');
          console.log('Banner de instalación removido');
        }
      };

      // Event listeners usando addEventListener directamente en los elementos
      installButton.addEventListener('click', async (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log('Botón instalar clickeado');
        
        if (deferredPrompt) {
          deferredPrompt.prompt();
          const { outcome } = await deferredPrompt.userChoice;
          if (outcome === 'accepted') {
            console.log('PWA instalada');
          }
          deferredPrompt = null;
        }
        removeBanner();
      });

      dismissButton.addEventListener('click', (event) => {
        event.preventDefault();
        event.stopPropagation();
        console.log('Botón descartar clickeado');
        removeBanner();
      });

      // Cerrar banner al hacer clic fuera de él
      bannerContainer.addEventListener('click', (event) => {
        if (event.target === bannerContainer) {
          console.log('Clic fuera del banner');
          removeBanner();
        }
      });

      // Agregar tecla Escape para cerrar
      const handleEscape = (event: KeyboardEvent) => {
        if (event.key === 'Escape') {
          console.log('Tecla Escape presionada');
          removeBanner();
          document.removeEventListener('keydown', handleEscape);
        }
      };
      document.addEventListener('keydown', handleEscape);

      // Agregar el banner al DOM
      document.body.appendChild(bannerContainer);
      console.log('Banner de instalación mostrado');
    });
  }
}

export const offlineService = new OfflineService();
export default offlineService;
