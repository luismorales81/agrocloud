import axios from 'axios';
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

// Configuración base de Axios - Vite
const BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

console.log('🔧 [API] Configurando servicio con URL:', BASE_URL);
console.log('🔧 [API] Variables de entorno disponibles:', {
  VITE_API_BASE_URL: import.meta.env.VITE_API_BASE_URL,
  MODE: import.meta.env.MODE
});

// Crear instancia de Axios
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

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
      console.log('🔧 [AuthService] Enviando petición a /api/auth/login');
      const response = await api.post('/api/auth/login', { email: username, password });
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
      const response = await api.post('/api/auth/request-password-reset', { email });
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
      const response = await api.post('/api/auth/reset-password', { token, newPassword });
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
      const response = await api.post('/api/auth/change-password', { 
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
      const response = await api.get('/api/v1/campos');
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
      const response = await api.post('/api/v1/campos', fieldData);
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
      const response = await api.get('/api/v1/lotes');
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
      const response = await api.get('/api/v1/insumos');
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
      const response = await api.get('/api/v1/maquinaria');
      console.log('✅ [MachineryService] Maquinaria obtenida:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [MachineryService] Error obteniendo maquinaria:', error);
      throw error;
    }
  }
};

export default api;
