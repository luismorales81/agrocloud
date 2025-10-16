import { showNotification } from './api';
import api from './api';

// Tipos para el caché offline
interface CacheEntry<T> {
  data: T;
  timestamp: number;
  expiresAt?: number;
}

interface OfflineAction {
  id: string;
  type: 'CREATE' | 'UPDATE' | 'DELETE';
  endpoint: string;
  data: any;
  timestamp: number;
}

class OfflineService {
  private static instance: OfflineService;
  private cache: Map<string, CacheEntry<any>> = new Map();
  private pendingActions: OfflineAction[] = [];
  private isOnline: boolean = navigator.onLine;
  private syncInProgress: boolean = false;

  private constructor() {
    this.setupEventListeners();
    this.loadFromStorage();
  }

  public static getInstance(): OfflineService {
    if (!OfflineService.instance) {
      OfflineService.instance = new OfflineService();
    }
    return OfflineService.instance;
  }

  private setupEventListeners(): void {
    // Escuchar cambios de conectividad
    window.addEventListener('online', () => {
      this.isOnline = true;
      this.syncPendingActions();
      showNotification('Conexión restaurada. Sincronizando datos...', 'success');
    });

    window.addEventListener('offline', () => {
      this.isOnline = false;
      showNotification('Sin conexión. Trabajando en modo offline', 'warning');
    });

    // Escuchar antes de cerrar la página para guardar datos
    window.addEventListener('beforeunload', () => {
      this.saveToStorage();
    });
  }

  private loadFromStorage(): void {
    try {
      const cacheData = localStorage.getItem('agrocloud_cache');
      const actionsData = localStorage.getItem('agrocloud_pending_actions');
      
      if (cacheData) {
        const parsedCache = JSON.parse(cacheData);
        this.cache = new Map(Object.entries(parsedCache));
      }

      if (actionsData) {
        this.pendingActions = JSON.parse(actionsData);
      }
    } catch (error) {
      console.error('❌ [OfflineService] Error cargando desde localStorage:', error);
    }
  }

  private saveToStorage(): void {
    try {
      const cacheObj = Object.fromEntries(this.cache);
      localStorage.setItem('agrocloud_cache', JSON.stringify(cacheObj));
      localStorage.setItem('agrocloud_pending_actions', JSON.stringify(this.pendingActions));
    } catch (error) {
      console.error('❌ [OfflineService] Error guardando en localStorage:', error);
    }
  }

  // Métodos públicos
  public isOnlineMode(): boolean {
    return this.isOnline;
  }

  public async get<T>(key: string, fetcher?: () => Promise<T>, ttl?: number): Promise<T | null> {
    const cached = this.cache.get(key);
    
    if (cached && (!cached.expiresAt || cached.expiresAt > Date.now())) {
      return cached.data;
    }

    if (this.isOnline && fetcher) {
      try {
        const data = await fetcher();
        this.set(key, data, ttl);
        return data;
      } catch (error) {
        console.error('❌ [OfflineService] Error obteniendo datos online:', error);
        // Si hay error online pero tenemos datos en caché, devolverlos
        if (cached) {
          return cached.data;
        }
        throw error;
      }
    }

    // Si no hay conexión y no hay caché, devolver null
    if (cached) {
      return cached.data;
    }

    return null;
  }

  public set<T>(key: string, data: T, ttl?: number): void {
    const expiresAt = ttl ? Date.now() + ttl : undefined;
    this.cache.set(key, {
        data,
      timestamp: Date.now(),
      expiresAt
    });
    this.saveToStorage();
  }

  public remove(key: string): void {
    this.cache.delete(key);
    this.saveToStorage();
  }

  public clear(): void {
    this.cache.clear();
    this.pendingActions = [];
    this.saveToStorage();
  }

  // Gestión de acciones pendientes
  public addPendingAction(action: Omit<OfflineAction, 'id' | 'timestamp'>): void {
    const pendingAction: OfflineAction = {
      ...action,
      id: `action_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
      timestamp: Date.now()
    };

    this.pendingActions.push(pendingAction);
    this.saveToStorage();
  }

  public async syncPendingActions(): Promise<void> {
    if (this.syncInProgress || !this.isOnline || this.pendingActions.length === 0) {
      return;
    }

    this.syncInProgress = true;
    
    const actionsToSync = [...this.pendingActions];
    const successfulActions: string[] = [];
    const failedActions: OfflineAction[] = [];
      
    for (const action of actionsToSync) {
        try {
        await this.executeAction(action);
        successfulActions.push(action.id);
        } catch (error) {
        console.error('❌ [OfflineService] Error sincronizando acción:', action.id, error);
        failedActions.push(action);
      }
    }

    // Remover acciones exitosas
    this.pendingActions = failedActions;
    this.saveToStorage();

      this.syncInProgress = false;

    if (successfulActions.length > 0) {
      showNotification(`${successfulActions.length} acciones sincronizadas exitosamente`, 'success');
    }

    if (failedActions.length > 0) {
      showNotification(`${failedActions.length} acciones fallaron al sincronizar`, 'error');
    }
  }

  private async executeAction(action: OfflineAction): Promise<void> {
    const { type, endpoint, data } = action;
    
    // Importar dinámicamente para evitar dependencias circulares
    const { default: axios } = await import('axios');
    const token = localStorage.getItem('token');
    
    const config = {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    };

    const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
    const fullURL = `${baseURL}/api${endpoint}`;

    switch (type) {
      case 'CREATE':
        await axios.post(fullURL, data, config);
        break;
      case 'UPDATE':
        await axios.put(fullURL, data, config);
        break;
      case 'DELETE':
        await axios.delete(fullURL, config);
        break;
    }
  }

  // Métodos específicos para diferentes entidades
  public async getFields(): Promise<any[]> {
    return this.get('fields', async () => {
      const { fieldService } = await import('./api');
      return fieldService.getAll();
    }, 5 * 60 * 1000); // 5 minutos TTL
  }

  public async getLotes(): Promise<any[]> {
    return this.get('lotes', async () => {
      const { plotService } = await import('./api');
      return plotService.getAll();
    }, 5 * 60 * 1000);
  }

  public async getLabores(): Promise<any[]> {
    return this.get('labores', async () => {
      const response = await api.get('/labores');
      return response.data;
    }, 2 * 60 * 1000); // 2 minutos TTL (más frecuente)
  }

  public async getInsumos(): Promise<any[]> {
    return this.get('insumos', async () => {
      const response = await api.get('/insumos');
      return response.data;
    }, 10 * 60 * 1000); // 10 minutos TTL
  }

  public async getMaquinaria(): Promise<any[]> {
    return this.get('maquinaria', async () => {
      const response = await api.get('/maquinaria');
      return response.data;
    }, 10 * 60 * 1000);
  }

  // Métodos para agregar acciones pendientes
  public addFieldAction(type: 'CREATE' | 'UPDATE' | 'DELETE', data: any, id?: number): void {
    const endpoint = type === 'CREATE' ? '/fields' : `/fields/${id}`;
    this.addPendingAction({ type, endpoint, data });
  }

  public addLoteAction(type: 'CREATE' | 'UPDATE' | 'DELETE', data: any, id?: number): void {
    const endpoint = type === 'CREATE' ? '/lotes' : `/lotes/${id}`;
    this.addPendingAction({ type, endpoint, data });
  }

  public addLaborAction(type: 'CREATE' | 'UPDATE' | 'DELETE', data: any, id?: number): void {
    const endpoint = type === 'CREATE' ? '/labores' : `/labores/${id}`;
    this.addPendingAction({ type, endpoint, data });
  }

  // Estadísticas del caché
  public getCacheStats(): { size: number; pendingActions: number; isOnline: boolean } {
    return {
      size: this.cache.size,
      pendingActions: this.pendingActions.length,
      isOnline: this.isOnline
    };
  }
}

export const offlineService = OfflineService.getInstance();
export default offlineService;