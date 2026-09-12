import axios from 'axios';
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

function resolverUrlBaseApi(): string {
  const viteBase = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.trim();
  const viteUrl = (import.meta.env.VITE_API_URL as string | undefined)?.trim();
  const candidato = viteBase || viteUrl;
  if (candidato) {
    const sinBarra = candidato.replace(/\/$/, '');
    return sinBarra.includes('/api') ? sinBarra : `${sinBarra}/api`;
  }
  if (import.meta.env.DEV) {
    return 'http://localhost:8080/api';
  }
  return '/api';
}

const VITE_API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
const VITE_API_URL = import.meta.env.VITE_API_URL;
const BASE_URL = resolverUrlBaseApi();

const esDesarrollo = import.meta.env.DEV;

if (esDesarrollo) {
console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');
console.log('%c🚀 API SERVICE INITIALIZED - VERSION 2.3 (timeout 30s, login 60s)', 'color: #00ff00; font-weight: bold; font-size: 16px');
console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');
console.log('%c📡 VITE_API_URL:', 'color: #ffaa00; font-weight: bold', VITE_API_URL || 'NOT SET');
console.log('%c📡 VITE_API_BASE_URL:', 'color: #ffaa00; font-weight: bold', VITE_API_BASE_URL || 'NOT SET');
console.log('%c🌐 Hostname:', 'color: #ffaa00; font-weight: bold', typeof window !== 'undefined' ? window.location.hostname : 'N/A');
console.log('%c📡 BASE_URL final:', 'color: #ffaa00; font-weight: bold', BASE_URL);
console.log('%c✅ /api prefix included:', 'color: #00ff00; font-weight: bold', BASE_URL.includes('/api'));
console.log('%c🔍 URL Analysis:', 'color: #00ff00; font-weight: bold');
console.log('  - VITE_API_URL includes /api:', VITE_API_URL?.includes('/api') || false);
console.log('  - VITE_API_BASE_URL includes /api:', VITE_API_BASE_URL?.includes('/api') || false);
console.log('  - BASE_URL final:', BASE_URL);
console.log('%c🔧 MODE:', 'color: #ffaa00', import.meta.env.MODE);
console.log('%c🌍 ENV:', 'color: #ffaa00', import.meta.env.VITE_ENVIRONMENT || 'development');
console.log('%c════════════════════════════════════════════════════════', 'color: #00ff00; font-weight: bold');
}

/** URL base usada por Axios (útil para mensajes de error en login). */
export const URL_BASE_API = BASE_URL;

/** Mensaje claro cuando no hay respuesta del servidor (backend caído, CORS, URL incorrecta). */
export function mensajeErrorConexionApi(error: AxiosError): string {
  const destino = URL_BASE_API.startsWith('/') ? `${window.location.origin}${URL_BASE_API}` : URL_BASE_API;
  if (error.code === 'ECONNABORTED') {
    return `Tiempo de espera agotado al contactar ${destino}. Verificá que el backend esté en ejecución (puerto 8080).`;
  }
  return `No se pudo conectar con el servidor (${destino}). En local: ejecutá el backend en el puerto 8080 y el frontend con "npm run dev".`;
}

/** Sesión válida: usuario en localStorage (JWT en cookie httpOnly vía withCredentials). */
export function haySesionActiva(): boolean {
  return !!localStorage.getItem('user');
}

// Crear instancia de Axios
const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
});

  // Interceptor para normalizar URLs y evitar duplicaciones de /api
  api.interceptors.request.use(
  (config) => {
    if (typeof config.url === 'string') {
      let url = config.url;
      
      if (esDesarrollo) {
      console.log('🔍 [API Interceptor] URL original:', url);
      console.log('🔍 [API Interceptor] BASE_URL:', BASE_URL);
      }
      
      // CORRECCIÓN AGRESIVA: Eliminar TODAS las duplicaciones de /api
      // Patrón 1: /api/api/ -> /api/
      url = url.replace(/\/api\/api\//g, '/api/');
      
      // Patrón 2: /api/v1/api/ -> /api/v1/
      url = url.replace(/\/api\/v1\/api\//g, '/api/v1/');
      
      // Patrón 3: /api/auth/api/ -> /api/auth/
      url = url.replace(/\/api\/auth\/api\//g, '/api/auth/');
      
      // Patrón 4: Cualquier duplicación restante
      while (url.includes('/api/api/')) {
        url = url.replace(/\/api\/api\//g, '/api/');
        console.warn('🚨 [API] Duplicación adicional corregida:', url);
      }
      
      // Normalizar barras múltiples
      url = url.replace(/\/+/g, '/');
      
      // Log de corrección
      if (config.url !== url) {
        console.warn('🚨 [API] URL corregida de:', config.url);
        console.warn('🚨 [API] URL corregida a:', url);
      }
      
      config.url = url;
      if (esDesarrollo) {
      console.log('✅ [API Interceptor] URL final:', url);
      }
    }
    return config;
  },
  (error) => {
    console.error('❌ [API] Error en normalizador de URL:', error);
    return Promise.reject(error);
  }
);

  // Interceptor para agregar token y empresa activa a las peticiones
  api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    const url = config.url ?? '';
    const esRutaAuth = url.includes('/auth/') || url.includes('/eula/');

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // Sin sesión no enviar contexto de empresa/campaña (evita ruido en login y estados stale)
    if (!token && esRutaAuth) {
      return config;
    }

    const haySesion = token || localStorage.getItem('user');
    if (!haySesion && !esRutaAuth) {
      return config;
    }

    // Solo enviar empresa validada contra mis-empresas (evita 500 por X-Company-Id obsoleto)
    const empresaIdValidada = localStorage.getItem('empresaIdValidada');
    if (empresaIdValidada && /^\d+$/.test(empresaIdValidada)) {
      config.headers['X-Company-Id'] = empresaIdValidada;
    }
    const rawCampana = localStorage.getItem('campanaActiva');
    const esRutaCampanas = url.includes('/v1/campanas');
    if (rawCampana && !esRutaCampanas) {
      try {
        const campana = JSON.parse(rawCampana);
        if (campana?.id != null && campana.estado !== 'CERRADA') {
          config.headers['X-Campaign-Id'] = String(campana.id);
        }
      } catch {
        // ignorar
      }
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
    // Solo loggear respuestas exitosas si no son de EULA (para reducir ruido)
    if (!response.config.url?.includes('/eula/') && esDesarrollo) {
      console.log('✅ [API] Respuesta exitosa:', response.config.url, response.status);
    }
    return response;
  },
  (error) => {
    // Verificar si es un error de EULA esperado (no es realmente un error, es parte del flujo)
    const isEulaError = error.response?.status === 403 && 
                       error.response?.data?.error === 'EULA_NO_ACEPTADO';
    
    if (isEulaError) {
      // Log mínimo para errores de EULA (flujo esperado)
      console.log('📄 [API] EULA no aceptado detectado (flujo normal)');
      error.isEulaError = true;
      error.eulaError = error.response.data;
    } else {
      // Log completo para otros errores
      const esTimeout = error.code === 'ECONNABORTED';
      if (esTimeout) {
        console.warn('⏱️ [API] Tiempo de espera agotado:', error.config?.url);
      } else {
        console.error('❌ [API] Error en respuesta:', {
          url: error.config?.url,
          status: error.response?.status,
          message: error.message,
          data: error.response?.data
        });
      }
    }
    
    if (error.response?.status === 401) {
      const isEulaEndpoint = error.config?.url?.includes('/eula/');
      const teniaSesion = Boolean(localStorage.getItem('user'));
      const enLogin = window.location.pathname === '/login' || window.location.pathname === '/';
      if (!isEulaEndpoint && teniaSesion && !enLogin) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        localStorage.removeItem('empresaActiva');
        localStorage.removeItem('empresaIdValidada');
        localStorage.removeItem('campanaActiva');
        window.location.href = '/login';
      }
    } else if (
      error.response?.status === 403 &&
      !isEulaError &&
      Boolean(localStorage.getItem('user')) &&
      (error.config?.url ?? '').includes('/mis-empresas')
    ) {
      // Sesión fantasma: user en localStorage sin cookie JWT válida → Spring responde 403
      console.warn('⚠️ [API] Sesión inválida al cargar empresas; redirigiendo a login');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      localStorage.removeItem('rememberMe');
      localStorage.removeItem('loginTimestamp');
      localStorage.removeItem('empresaActiva');
      localStorage.removeItem('empresaIdValidada');
      localStorage.removeItem('rolUsuario');
      localStorage.removeItem('campanaActiva');
      localStorage.removeItem('currentModule');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
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
    try {
      // Comprobar que el backend responde antes de intentar login (fallo rápido si no está listo)
      try {
        await api.get('/health', { timeout: 8000 });
      } catch (healthError: any) {
        console.warn('⚠️ [AuthService] Backend no disponible en /health:', healthError?.message);
        throw healthError;
      }

      const response = await api.post('/auth/login', { email: username, password }, { timeout: 60000 });
      console.log('✅ [AuthService] Login exitoso');
      
      return response.data;
    } catch (error: any) {
      // Solo loggear si NO es un error de EULA esperado (flujo normal)
      const isEulaError = 
        (error as any).isEulaError ||
        (error.response?.status === 403 && error.response?.data?.error === 'EULA_NO_ACEPTADO');
      
      if (!isEulaError) {
        console.error('❌ [AuthService] Error en login:', error);
      }
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
    api.post('/auth/logout').catch(() => undefined);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('rememberMe');
    localStorage.removeItem('loginTimestamp');
    localStorage.removeItem('empresaActiva');
    localStorage.removeItem('empresaIdValidada');
    localStorage.removeItem('rolUsuario');
    localStorage.removeItem('campanaActiva');
    localStorage.removeItem('currentModule');
  },

  isAuthenticated() {
    return !!localStorage.getItem('user');
  },

  /** Comprueba que la cookie JWT siga válida (no basta con user en localStorage). */
  async validarSesion(): Promise<boolean> {
    try {
      await api.get('/v1/empresas/mis-empresas', { timeout: 15000 });
      return true;
    } catch {
      return false;
    }
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

// Servicio para el wizard de insumos
export const insumoWizardService = {
  async crearInsumo(datosInsumo: any) {
    console.log('🔧 [InsumoWizardService] Creando insumo...');
    console.log('🔧 [InsumoWizardService] URL que se usará:', '/insumos');
    try {
      const response = await api.post('/insumos', datosInsumo);
      console.log('✅ [InsumoWizardService] Insumo creado exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error creando insumo:', error);
      throw error;
    }
  },

  // Método eliminado - usar solo crearInsumo para todos los tipos

  async actualizarInsumo(id: number, datosInsumo: any) {
    console.log('🔧 [InsumoWizardService] Actualizando insumo...');
    console.log('🔧 [InsumoWizardService] ID:', id);
    console.log('🔧 [InsumoWizardService] URL que se usará:', `/insumos/${id}`);
    try {
      const response = await api.put(`/insumos/${id}`, datosInsumo);
      console.log('✅ [InsumoWizardService] Insumo actualizado exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error actualizando insumo:', error);
      throw error;
    }
  },

  // Método eliminado - usar solo actualizarInsumo para todos los tipos

  async eliminarInsumo(id: number) {
    console.log('🔧 [InsumoWizardService] Eliminando insumo...');
    console.log('🔧 [InsumoWizardService] ID:', id);
    console.log('🔧 [InsumoWizardService] URL que se usará:', `/insumos/${id}`);
    try {
      const response = await api.delete(`/insumos/${id}`);
      console.log('✅ [InsumoWizardService] Insumo eliminado exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error eliminando insumo:', error);
      throw error;
    }
  },

  // Método eliminado - usar solo eliminarInsumo para todos los tipos

  // Métodos para manejar dosis de agroquímicos
  async obtenerDosisPorInsumo(insumoId: number) {
    console.log('🔧 [InsumoWizardService] Obteniendo dosis del insumo...');
    try {
      const response = await api.get(`/dosis-agroquimicos/insumo/${insumoId}`);
      console.log('✅ [InsumoWizardService] Dosis obtenidas:', response.data?.length ?? 0);
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error obteniendo dosis:', error);
      throw error;
    }
  },
  async crearDosisAgroquimico(insumoId: number, datosDosis: any) {
    console.log('🔧 [InsumoWizardService] Creando dosis de agroquímico...');
    try {
      const response = await api.post(`/dosis-agroquimicos`, {
        insumoId,
        ...datosDosis
      });
      console.log('✅ [InsumoWizardService] Dosis creada exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error creando dosis:', error);
      throw error;
    }
  },

  async eliminarDosisAgroquimico(insumoId: number) {
    console.log('🔧 [InsumoWizardService] Eliminando dosis de insumo...');
    try {
      const response = await api.delete(`/dosis-agroquimicos/insumo/${insumoId}`);
      console.log('✅ [InsumoWizardService] Dosis eliminadas exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [InsumoWizardService] Error eliminando dosis:', error);
      throw error;
    }
  }
};

// Servicio para agroquímicos integrados
export const agroquimicoIntegradoService = {
  async getAllAgroquimicos() {
    console.log('🔧 [AgroquimicoIntegradoService] Obteniendo agroquímicos...');
    try {
      const response = await api.get('/v1/agroquimicos-integrados/agroquimicos');
      console.log('✅ [AgroquimicoIntegradoService] Agroquímicos obtenidos:', response.data.length);
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error obteniendo agroquímicos:', error);
      throw error;
    }
  },

  async convertirInsumoAAgroquimico(insumoId: number, datosAgroquimico: any) {
    console.log('🔧 [AgroquimicoIntegradoService] Convirtiendo insumo a agroquímico...');
    try {
      const response = await api.post(`/v1/agroquimicos-integrados/${insumoId}/convertir-agroquimico`, datosAgroquimico);
      console.log('✅ [AgroquimicoIntegradoService] Insumo convertido exitosamente');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error convirtiendo insumo:', error);
      throw error;
    }
  },

  async sugerirDosis(datosSugerencia: any) {
    console.log('🔧 [AgroquimicoIntegradoService] Obteniendo sugerencias de dosis...');
    try {
      const response = await api.post('/v1/agroquimicos-integrados/sugerir-dosis', datosSugerencia);
      console.log('✅ [AgroquimicoIntegradoService] Sugerencias obtenidas');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error obteniendo sugerencias:', error);
      throw error;
    }
  },

  async planificarAplicacion(datosAplicacion: any) {
    console.log('🔧 [AgroquimicoIntegradoService] Planificando aplicación...');
    try {
      const response = await api.post('/v1/agroquimicos-integrados/planificar-aplicacion', datosAplicacion);
      console.log('✅ [AgroquimicoIntegradoService] Aplicación planificada');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error planificando aplicación:', error);
      throw error;
    }
  },

  async ejecutarLabor(laborId: number, datosEjecucion: any) {
    console.log('🔧 [AgroquimicoIntegradoService] Ejecutando labor...');
    try {
      const response = await api.post(`/v1/agroquimicos-integrados/ejecutar-labor/${laborId}`, datosEjecucion);
      console.log('✅ [AgroquimicoIntegradoService] Labor ejecutada');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error ejecutando labor:', error);
      throw error;
    }
  },

  async getAgroquimicosPorTipoAplicacion(tipoAplicacion: string) {
    console.log('🔧 [AgroquimicoIntegradoService] Obteniendo agroquímicos por tipo de aplicación...');
    try {
      const response = await api.get(`/v1/agroquimicos-integrados/agroquimicos/tipo-aplicacion/${tipoAplicacion}`);
      console.log('✅ [AgroquimicoIntegradoService] Agroquímicos obtenidos por tipo');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error obteniendo agroquímicos por tipo:', error);
      throw error;
    }
  },

  async getCondicionesRecomendadas() {
    console.log('🔧 [AgroquimicoIntegradoService] Obteniendo condiciones recomendadas...');
    try {
      const response = await api.get('/v1/agroquimicos-integrados/condiciones-recomendadas');
      console.log('✅ [AgroquimicoIntegradoService] Condiciones recomendadas obtenidas');
      return response.data;
    } catch (error) {
      console.error('❌ [AgroquimicoIntegradoService] Error obteniendo condiciones:', error);
      throw error;
    }
  }
};

export default api;
