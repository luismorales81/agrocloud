/**
 * Servicios de autenticación, EULA y usuarios.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

export const authService = {
  async login(email: string, password: string) {
    const response = await api.post(API_ENDPOINTS.AUTH.LOGIN, { email, password });
    return response.data;
  },

  async register(userData: { name: string; email: string; password: string }) {
    const response = await api.post(API_ENDPOINTS.AUTH.REGISTER, userData);
    return response.data;
  },

  async resetPassword(token: string, newPassword: string) {
    const response = await api.post(API_ENDPOINTS.AUTH.RESET_PASSWORD, { token, newPassword });
    return response.data;
  },

  async requestPasswordReset(email: string) {
    const response = await api.post(API_ENDPOINTS.AUTH.REQUEST_RESET, { email });
    return response.data;
  },

  async changePassword(currentPassword: string, newPassword: string, confirmPassword: string) {
    const response = await api.post(API_ENDPOINTS.AUTH.CHANGE_PASSWORD, {
      currentPassword,
      newPassword,
      confirmPassword,
    });
    return response.data;
  },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('user');
  },

  getCurrentUser() {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      try {
        return JSON.parse(userStr);
      } catch {
        return null;
      }
    }
    return null;
  },

  async obtenerUsuarios() {
    const response = await api.get(API_ENDPOINTS.AUTH.USERS);
    return response.data;
  },

  async obtenerRoles() {
    const response = await api.get(API_ENDPOINTS.AUTH.ROLES);
    return response.data;
  },

  async obtenerEstadisticas() {
    const response = await api.get(API_ENDPOINTS.AUTH.STATS);
    return response.data;
  },

  async actualizarUsuario(id: number, usuarioData: any) {
    const response = await api.put(API_ENDPOINTS.AUTH.USER(id), usuarioData);
    return response.data;
  },

  async toggleEstadoUsuario(id: number) {
    const response = await api.patch(API_ENDPOINTS.AUTH.USER_TOGGLE_STATUS(id));
    return response.data;
  },

  async eliminarUsuario(id: number) {
    const response = await api.delete(API_ENDPOINTS.AUTH.USER(id));
    return response.data;
  },
};

export const eulaService = {
  async obtenerEstado(email?: string) {
    if (email) {
      const response = await api.get(`/eula/estado/${email}`);
      return response.data;
    } else {
      const response = await api.get('/eula/estado');
      return response.data;
    }
  },

  async aceptarEula(email: string, aceptado: boolean) {
    try {
      const response = await api.post(`/eula/aceptar/${email}`, {
        aceptado,
        ipAddress: '',
        userAgent: navigator.userAgent,
      });
      return response.data;
    } catch (error) {
      console.error('[EulaService] Error en aceptarEula:', error);
      throw error;
    }
  },

  async obtenerTexto() {
    const response = await api.get('/eula/texto');
    return response.data;
  },

  async descargarPdf(userId: number) {
    const response = await api.get(`/eula/pdf/${userId}`, { responseType: 'blob' });
    return response.data;
  },
};

export const usuariosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.USUARIOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.USUARIOS.OBTENER(id));
    return response.data;
  },

  async crear(usuarioData: any) {
    const response = await api.post(API_ENDPOINTS.USUARIOS.CREAR, usuarioData);
    return response.data;
  },

  async actualizar(id: number, usuarioData: any) {
    const response = await api.put(API_ENDPOINTS.USUARIOS.ACTUALIZAR(id), usuarioData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.USUARIOS.ELIMINAR(id));
    return response.data;
  },

  async cambiarEstado(id: number, estado: string) {
    const response = await api.patch(API_ENDPOINTS.USUARIOS.CAMBIAR_ESTADO(id), null, { params: { estado } });
    return response.data;
  },

  async cambiarActivo(id: number, activo: boolean) {
    const response = await api.patch(API_ENDPOINTS.USUARIOS.CAMBIAR_ACTIVO(id), null, { params: { activo } });
    return response.data;
  },

  async resetPassword(id: number, nuevaPassword: string) {
    const response = await api.patch(API_ENDPOINTS.USUARIOS.RESET_PASSWORD(id), null, {
      params: { nuevaContraseña: nuevaPassword },
    });
    return response.data;
  },

  async obtenerEstadisticas() {
    const response = await api.get(API_ENDPOINTS.USUARIOS.ESTADISTICAS);
    return response.data;
  },

  async obtenerRoles() {
    const response = await api.get(API_ENDPOINTS.USUARIOS.ROLES);
    return response.data;
  },
};
