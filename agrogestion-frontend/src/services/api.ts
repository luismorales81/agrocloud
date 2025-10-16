import axios from 'axios';
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

// Configuración base de Axios - Vite
// Soporta tanto VITE_API_URL como VITE_API_BASE_URL para compatibilidad
const API_URL = import.meta.env.VITE_API_URL || import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const BASE_URL = API_URL.endsWith('/api') ? API_URL : `${API_URL}/api`;

console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');
console.log('%c🚀 API SERVICE INITIALIZED - VERSION 2.1', 'color: #00ff00; font-weight: bold; font-size: 16px');
console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');
console.log('%c📡 API_URL:', 'color: #ffaa00; font-weight: bold', API_URL);
console.log('%c📡 BASE_URL:', 'color: #ffaa00; font-weight: bold', BASE_URL);
console.log('%c✅ /api prefix included:', 'color: #00ff00; font-weight: bold', BASE_URL.includes('/api'));
console.log('%c🔧 VITE_API_URL:', 'color: #ffaa00', import.meta.env.VITE_API_URL || 'NOT SET');
console.log('%c🔧 MODE:', 'color: #ffaa00', import.meta.env.MODE);
console.log('%c🌍 ENV:', 'color: #ffaa00', import.meta.env.VITE_ENVIRONMENT || 'development');
console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');

// Crear instancia de Axios
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

  // Interceptor para normalizar URLs y evitar duplicaciones de /api
  api.interceptors.request.use(
  (config) => {
    if (typeof config.url === 'string') {
      let url = config.url;
      
      // Normalizar barras múltiples
      url = url.replace(/\/+/g, '/');
      
      // Detectar y corregir duplicaciones de /api
      if (url.includes('/api/api/')) {
        console.warn('🚨 [API] URL duplicada detectada:', url);
        url = url.replace(/\/api\/api\//g, '/api/');
        console.log('🔧 [API] URL corregida:', url);
      }
      
      // Detectar y corregir /api/v1/api/
      if (url.includes('/api/v1/api/')) {
        console.warn('🚨 [API] URL v1 duplicada detectada:', url);
        url = url.replace(/\/api\/v1\/api\//g, '/api/v1/');
        console.log('🔧 [API] URL v1 corregida:', url);
      }
      
      // Detectar y corregir /api/auth/api/
      if (url.includes('/api/auth/api/')) {
        console.warn('🚨 [API] URL auth duplicada detectada:', url);
        url = url.replace(/\/api\/auth\/api\//g, '/api/auth/');
        console.log('🔧 [API] URL auth corregida:', url);
      }
      
      config.url = url;
    }
    return config;
  },
  (error) => {
    console.error('❌ [API] Error en normalizador de URL:', error);
    return Promise.reject(error);
  }
);

  // Interceptor para agregar token a las peticiones
  api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      console.log('🔧 [API] Agregando token a petición:', config.url);
      config.headers.Authorization = `Bearer ${token}`;
    } else {
      console.log('⚠️ [API] No hay token disponible para:', config.url);
    }
    return config;
  },
  (error) => {
    console.error('❌ [API] Error en interceptor de request:', error);
    return Promise.reject(error);
  }
);

  // Interceptor para manejar respuestas
  api.interceptors.response.use(
  (response) => {
    console.log('✅ [API] Respuesta exitosa:', response.config.url, response.status);
    return response;
  },
  (error) => {
    console.error('❌ [API] Error en respuesta:', {
      url: error.config?.url,
      status: error.response?.status,
      message: error.message,
      data: error.response?.data
    });
    
    if (error.response?.status === 401) {
      console.log('🔧 [API] Token expirado, limpiando localStorage');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    } else if (error.response?.status === 500) {
      console.error('🚨 [API] Error interno del servidor:', error.response?.data);
      // No redirigir en errores 500, solo loggear
    }
    
    return Promise.reject(error);
  }
  );

// Función para mostrar notificaciones
export const showNotification = (message: string, type: 'success' | 'error' | 'info' | 'warning' = 'info') => {
  console.log(`🔔 [Notification] ${type.toUpperCase()}: ${message}`);
  // Aquí puedes implementar tu sistema de notificaciones
  // Por ahora solo mostramos en consola
};

// Servicio de autenticación
export const authService = {
  async login(username: string, password: string) {
    console.log('🔧 [AuthService] Intentando login con:', { username, password: '***' });
    try {
      // Usar el endpoint real de autenticación
      console.log('🔧 [AuthService] Enviando petición a /auth/login');
      const response = await api.post('/auth/login', { email: username, password });
      console.log('✅ [AuthService] Login exitoso:', response.data);
      
      return response.data;
    } catch (error) {
      console.error('❌ [AuthService] Error en login:', error);
      throw error;
    }
  },

  async requestPasswordReset(email: string) {
    console.log('🔧 [AuthService] Solicitando reset de contraseña...');
    try {
      const response = await api.post('/auth/request-password-reset', { email });
      console.log('✅ [AuthService] Reset solicitado exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [AuthService] Error solicitando reset:', error);
      throw error;
    }
  },

  async resetPassword(token: string, newPassword: string) {
    console.log('🔧 [AuthService] Reseteando contraseña...');
    try {
      const response = await api.post('/auth/reset-password', { token, newPassword });
      console.log('✅ [AuthService] Contraseña reseteada exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [AuthService] Error reseteando contraseña:', error);
      throw error;
    }
  },

  async changePassword(currentPassword: string, newPassword: string, confirmPassword: string) {
    console.log('🔧 [AuthService] Cambiando contraseña...');
    try {
      const response = await api.post('/auth/change-password', { 
        currentPassword, 
        newPassword, 
        confirmPassword 
      });
      console.log('✅ [AuthService] Contraseña cambiada exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [AuthService] Error cambiando contraseña:', error);
      throw error;
    }
  },

  logout() {
    console.log('🔧 [AuthService] Cerrando sesión...');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    console.log('✅ [AuthService] Sesión cerrada');
  },

  isAuthenticated() {
    const token = localStorage.getItem('token');
    const isAuth = !!token;
    console.log('🔧 [AuthService] Verificando autenticación:', isAuth);
    return isAuth;
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        const user = JSON.parse(userStr);
        console.log('🔧 [AuthService] Usuario actual:', user.email);
        return user;
      } catch (error) {
        console.error('❌ [AuthService] Error parseando usuario:', error);
        return null;
      }
    }
    console.log('ℹ️ [AuthService] No hay usuario en localStorage');
    return null;
  }
};

// Servicios para diferentes entidades
export const fieldService = {
  async getAll() {
    console.log('🔧 [FieldService] Obteniendo campos...');
    try {
      const response = await api.get('/campos');
      console.log('✅ [FieldService] Campos obtenidos:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [FieldService] Error obteniendo campos:', error);
      throw error;
    }
  },

  async create(fieldData: any) {
    console.log('🔧 [FieldService] Creando campo...');
    try {
      const response = await api.post('/campos', fieldData);
      console.log('✅ [FieldService] Campo creado:', response.data.id);
      return response.data;
    } catch (error) {
      console.error('❌ [FieldService] Error creando campo:', error);
      throw error;
    }
  }
};

export const plotService = {
  async getAll() {
    console.log('🔧 [PlotService] Obteniendo lotes...');
    try {
      const response = await api.get('/v1/lotes');
      console.log('✅ [PlotService] Lotes obtenidos:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [PlotService] Error obteniendo lotes:', error);
      throw error;
    }
  }
};

export const inputService = {
  async getAll() {
    console.log('🔧 [InputService] Obteniendo insumos...');
    try {
      const response = await api.get('/insumos');
      console.log('✅ [InputService] Insumos obtenidos:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [InputService] Error obteniendo insumos:', error);
      throw error;
    }
  }
};

export const machineryService = {
  async getAll() {
    console.log('🔧 [MachineryService] Obteniendo maquinaria...');
    try {
      const response = await api.get('/maquinaria');
      console.log('✅ [MachineryService] Maquinaria obtenida:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [MachineryService] Error obteniendo maquinaria:', error);
      throw error;
    }
  }
};

export default api;
