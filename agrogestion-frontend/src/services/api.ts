import axios from 'axios';
import type { AxiosInstance, AxiosResponse, AxiosError } from 'axios';

// Configuración base de Axios
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://agrocloud-production.up.railway.app/api';

// Crear instancia de Axios
const api: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para agregar token JWT a las peticiones
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas y errores
api.interceptors.response.use(
  (response: AxiosResponse) => {
    return response;
  },
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    // Mostrar notificación de error
    const errorMessage = (error.response?.data as any)?.message || error.message || 'Error de conexión';
    showNotification(errorMessage, 'error');
    
    return Promise.reject(error);
  }
);

// Función para mostrar notificaciones
export const showNotification = (message: string, type: 'success' | 'error' | 'warning' | 'info' = 'info') => {
  // Crear elemento de notificación
  const notification = document.createElement('div');
  notification.className = `fixed top-4 right-4 z-50 p-4 rounded-lg shadow-lg max-w-sm transform transition-all duration-300 translate-x-full`;
  
  // Configurar colores según el tipo
  switch (type) {
    case 'success':
      notification.className += ' bg-success-500 text-white';
      break;
    case 'error':
      notification.className += ' bg-danger-500 text-white';
      break;
    case 'warning':
      notification.className += ' bg-warning-500 text-white';
      break;
    default:
      notification.className += ' bg-primary-500 text-white';
  }
  
  notification.innerHTML = `
    <div class="flex items-center justify-between">
      <span>${message}</span>
      <button onclick="this.parentElement.parentElement.remove()" class="ml-2 text-white hover:text-gray-200">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
        </svg>
      </button>
    </div>
  `;
  
  document.body.appendChild(notification);
  
  // Animar entrada
  setTimeout(() => {
    notification.classList.remove('translate-x-full');
  }, 100);
  
  // Auto-remover después de 5 segundos
  setTimeout(() => {
    notification.classList.add('translate-x-full');
    setTimeout(() => {
      if (notification.parentElement) {
        notification.remove();
      }
    }, 300);
  }, 5000);
};

// Servicios de autenticación
export const authService = {
  login: async (username: string, password: string) => {
    const response = await api.post('/auth/login', { username, password });
    return response.data;
  },
  
  logout: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  
  getCurrentUser: () => {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  },
  
  isAuthenticated: () => {
    return !!localStorage.getItem('token');
  }
};

// Servicios de usuarios
export const userService = {
  getAll: async () => {
    const response = await api.get('/users');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/users/${id}`);
    return response.data;
  },
  
  create: async (userData: any) => {
    const response = await api.post('/users', userData);
    return response.data;
  },
  
  update: async (id: number, userData: any) => {
    const response = await api.put(`/users/${id}`, userData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/users/${id}`);
    return response.data;
  }
};

// Servicios de campos
export const fieldService = {
  getAll: async () => {
    const response = await api.get('/fields');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/fields/${id}`);
    return response.data;
  },
  
  create: async (fieldData: any) => {
    const response = await api.post('/fields', fieldData);
    return response.data;
  },
  
  update: async (id: number, fieldData: any) => {
    const response = await api.put(`/fields/${id}`, fieldData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/fields/${id}`);
    return response.data;
  }
};

// Servicios de lotes
export const plotService = {
  getAll: async () => {
    const response = await api.get('/plots');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/plots/${id}`);
    return response.data;
  },
  
  create: async (plotData: any) => {
    const response = await api.post('/plots', plotData);
    return response.data;
  },
  
  update: async (id: number, plotData: any) => {
    const response = await api.put(`/plots/${id}`, plotData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/plots/${id}`);
    return response.data;
  }
};

// Servicios de cultivos
export const cropService = {
  getAll: async () => {
    const response = await api.get('/crops');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/crops/${id}`);
    return response.data;
  },
  
  create: async (cropData: any) => {
    const response = await api.post('/crops', cropData);
    return response.data;
  },
  
  update: async (id: number, cropData: any) => {
    const response = await api.put(`/crops/${id}`, cropData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/crops/${id}`);
    return response.data;
  }
};

// Servicios de insumos
export const inputService = {
  getAll: async () => {
    const response = await api.get('/inputs');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/inputs/${id}`);
    return response.data;
  },
  
  create: async (inputData: any) => {
    const response = await api.post('/inputs', inputData);
    return response.data;
  },
  
  update: async (id: number, inputData: any) => {
    const response = await api.put(`/inputs/${id}`, inputData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/inputs/${id}`);
    return response.data;
  }
};

// Servicios de movimientos de insumos
export const inputMovementService = {
  getAll: async () => {
    const response = await api.get('/input-movements');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/input-movements/${id}`);
    return response.data;
  },
  
  create: async (movementData: any) => {
    const response = await api.post('/input-movements', movementData);
    return response.data;
  },
  
  update: async (id: number, movementData: any) => {
    const response = await api.put(`/input-movements/${id}`, movementData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/input-movements/${id}`);
    return response.data;
  }
};

// Servicios de labores
export const laborService = {
  getAll: async () => {
    const response = await api.get('/labors');
    return response.data;
  },
  
  getById: async (id: number) => {
    const response = await api.get(`/labors/${id}`);
    return response.data;
  },
  
  create: async (laborData: any) => {
    const response = await api.post('/labors', laborData);
    return response.data;
  },
  
  update: async (id: number, laborData: any) => {
    const response = await api.put(`/labors/${id}`, laborData);
    return response.data;
  },
  
  delete: async (id: number) => {
    const response = await api.delete(`/labors/${id}`);
    return response.data;
  }
};

// Servicios de reportes
export const reportService = {
  getProductionReport: async () => {
    const response = await api.get('/reports/production');
    return response.data;
  },
  
  getStockReport: async () => {
    const response = await api.get('/reports/stock');
    return response.data;
  },
  
  getCostsReport: async () => {
    const response = await api.get('/reports/costs');
    return response.data;
  },
  
  getGeneralReport: async () => {
    const response = await api.get('/reports/general');
    return response.data;
  }
};

export default api;
