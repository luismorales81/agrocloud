import React, { createContext, useContext, useState, useEffect } from 'react';
import type { ReactNode } from 'react';
import { authService, showNotification } from '../services/api';

interface User {
  id: number;
  username: string;
  name: string;
  email: string;
  roleName: string;
  permissions: string[];
  active: boolean;
  emailVerified: boolean;
  lastLogin?: string;
  createdAt?: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  login: (username: string, password: string, rememberMe?: boolean) => Promise<boolean>;
  logout: () => void;
  isAuthenticated: () => boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const initializeAuth = () => {
      try {
        const currentUser = authService.getCurrentUser();
        if (currentUser && authService.isAuthenticated()) {
          setUser(currentUser);
          console.log('✅ [AuthContext] Usuario restaurado desde localStorage');
        }
      } catch (error) {
        console.error('Error initializing auth:', error);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (username: string, password: string, rememberMe: boolean = true): Promise<boolean> => {
    try {
      console.log('🔧 [AuthContext] Iniciando login para:', username, 'rememberMe:', rememberMe);
      setLoading(true);
      
      console.log('🔧 [AuthContext] Llamando a authService.login...');
      const response = await authService.login(username, password);
      
      console.log('✅ [AuthContext] Login exitoso, respuesta:', response);
      
      // Guardar datos de autenticación
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(response.user));
      
      // Si rememberMe es true, marcar la sesión como persistente
      if (rememberMe) {
        localStorage.setItem('rememberMe', 'true');
        localStorage.setItem('loginTimestamp', Date.now().toString());
        console.log('💾 [AuthContext] Sesión marcada como persistente');
      } else {
        localStorage.removeItem('rememberMe');
        localStorage.removeItem('loginTimestamp');
      }
      
      console.log('💾 [AuthContext] Datos guardados en localStorage');
      
      setUser(response.user);
      showNotification('Inicio de sesión exitoso', 'success');
      
      console.log('✅ [AuthContext] Login completado exitosamente');
      return true;
    } catch (error: any) {
      // Verificar si es un error de EULA esperado (no es realmente un error, es parte del flujo)
      const isEulaError = 
        (error as any).isEulaError ||
        (error.response?.status === 403 && error.response?.data?.error === 'EULA_NO_ACEPTADO');
      
      if (isEulaError) {
        // Log mínimo y re-lanzar para que Login.tsx lo maneje
        console.log('📄 [AuthContext] EULA no aceptado, re-lanzando para Login');
        throw error;
      }
      
      // Log completo para otros errores reales
      console.error('❌ [AuthContext] Error en login:', error);
      
      // Manejar diferentes tipos de errores de autenticación
      if (error.response?.status === 401) {
        const errorData = error.response.data;
        
        if (errorData?.error === 'Credenciales inválidas') {
          showNotification('Email o contraseña incorrectos. Por favor, verifica tus credenciales.', 'error');
        } else if (errorData?.error === 'Usuario no encontrado') {
          showNotification('El email proporcionado no está registrado en el sistema.', 'error');
        } else if (errorData?.error === 'Usuario inactivo') {
          showNotification('Tu cuenta está desactivada. Contacta al administrador.', 'error');
        } else {
          showNotification('Credenciales inválidas. Por favor, verifica tu email y contraseña.', 'error');
        }
      } else if (error.response?.status === 403) {
        showNotification('Tu cuenta está desactivada. Contacta al administrador.', 'error');
        return false;
      } else if (error.response?.status === 500) {
        showNotification('Error interno del servidor. Inténtalo de nuevo más tarde.', 'error');
        return false;
      } else if (error.code === 'NETWORK_ERROR' || !error.response) {
        showNotification('Error de conexión. Verifica tu conexión a internet e inténtalo de nuevo.', 'error');
        return false;
      } else {
        showNotification('Error en el inicio de sesión. Inténtalo de nuevo.', 'error');
        return false;
      }
    } finally {
      setLoading(false);
    }
  };

  const logout = (): void => {
    authService.logout();
    setUser(null);
    showNotification('Sesión cerrada', 'info');
  };

  const isAuthenticated = (): boolean => {
    const isAuth = authService.isAuthenticated();
    const rememberMe = localStorage.getItem('rememberMe');
    
    // Si hay rememberMe, mantener la sesión activa indefinidamente
    if (isAuth && rememberMe === 'true') {
      return true;
    }
    
    return isAuth;
  };

  const contextValue: AuthContextType = {
    user,
    loading,
    login,
    logout,
    isAuthenticated,
  };

  return (
    <AuthContext.Provider value={contextValue}>
      {children}
    </AuthContext.Provider>
  );
};
