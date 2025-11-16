/**
 * Servicios centralizados de API
 * Esta es la capa de abstracción entre los componentes y las llamadas API
 * Todos los componentes deben usar estos servicios en lugar de llamar api.get/post directamente
 */

import api from './api';
import { API_ENDPOINTS } from './apiEndpoints';

// ============================================================================
// SERVICIO DE AUTENTICACIÓN
// ============================================================================
export const authService = {
  async login(email: string, password: string) {
    const response = await api.post(API_ENDPOINTS.AUTH.LOGIN, { email, password });
    return response.data;
  },

  async register(userData: any) {
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
    return !!localStorage.getItem('token');
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

  // Métodos adicionales para UsersManagement
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

// ============================================================================
// SERVICIO DE EULA
// ============================================================================
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
    console.log('📄 [EulaService] Aceptando EULA para:', email);
    try {
      const response = await api.post(`/eula/aceptar/${email}`, {
        aceptado,
        ipAddress: '', // Se obtiene del servidor
        userAgent: navigator.userAgent,
      });
      console.log('✅ [EulaService] EULA aceptado, respuesta:', response.data);
      return response.data;
    } catch (error) {
      console.error('❌ [EulaService] Error en aceptarEula:', error);
      throw error;
    }
  },

  async obtenerTexto() {
    const response = await api.get('/eula/texto');
    return response.data;
  },

  async descargarPdf(userId: number) {
    const response = await api.get(`/eula/pdf/${userId}`, {
      responseType: 'blob',
    });
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE USUARIOS
// ============================================================================
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
    const response = await api.patch(API_ENDPOINTS.USUARIOS.CAMBIAR_ESTADO(id), null, {
      params: { estado },
    });
    return response.data;
  },

  async cambiarActivo(id: number, activo: boolean) {
    const response = await api.patch(API_ENDPOINTS.USUARIOS.CAMBIAR_ACTIVO(id), null, {
      params: { activo },
    });
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

// ============================================================================
// SERVICIO DE CULTIVOS
// ============================================================================
export const cultivosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.OBTENER(id));
    return response.data;
  },

  async crear(cultivoData: any) {
    const response = await api.post(API_ENDPOINTS.CULTIVOS.CREAR, cultivoData);
    return response.data;
  },

  async actualizar(id: number, cultivoData: any) {
    const response = await api.put(API_ENDPOINTS.CULTIVOS.ACTUALIZAR(id), cultivoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.CULTIVOS.ELIMINAR(id));
    return response.data;
  },

  async eliminarFisico(id: number) {
    const response = await api.delete(API_ENDPOINTS.CULTIVOS.ELIMINAR_FISICO(id));
    return response.data;
  },

  async restaurar(id: number) {
    const response = await api.put(API_ENDPOINTS.CULTIVOS.RESTAURAR(id), {});
    return response.data;
  },

  async buscar(nombre: string) {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.BUSCAR, {
      params: { nombre },
    });
    return response.data;
  },

  async obtenerEliminados() {
    const response = await api.get(API_ENDPOINTS.CULTIVOS.ELIMINADOS);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE LOTES
// ============================================================================
export const lotesService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.LOTES.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.LOTES.OBTENER(id));
    return response.data;
  },

  async crear(loteData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.CREAR, loteData);
    return response.data;
  },

  async actualizar(id: number, loteData: any) {
    const response = await api.put(API_ENDPOINTS.LOTES.ACTUALIZAR(id), loteData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.LOTES.ELIMINAR(id));
    return response.data;
  },

  async sembrar(id: number, siembraData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.SEMBRAR(id), siembraData);
    return response.data;
  },

  async cosechar(id: number, cosechaData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.COSECHAR(id), cosechaData);
    return response.data;
  },

  async obtenerInfoCosecha(id: number) {
    const response = await api.get(API_ENDPOINTS.LOTES.INFO_COSECHA(id));
    return response.data;
  },

  async abandonar(id: number, motivo: string) {
    const response = await api.post(API_ENDPOINTS.LOTES.ABANDONAR(id), { motivo });
    return response.data;
  },

  async limpiar(id: number, motivo: string) {
    const response = await api.post(API_ENDPOINTS.LOTES.LIMPIAR(id), { motivo });
    return response.data;
  },

  async convertirForraje(id: number, cosechaData: any) {
    const response = await api.post(API_ENDPOINTS.LOTES.CONVERTIR_FORRAJE(id), cosechaData);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE INSUMOS
// ============================================================================
export const insumosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.INSUMOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.INSUMOS.OBTENER(id));
    return response.data;
  },

  async crear(insumoData: any) {
    const response = await api.post(API_ENDPOINTS.INSUMOS.CREAR, insumoData);
    return response.data;
  },

  async crearConDosis(insumoData: any) {
    const response = await api.post(API_ENDPOINTS.INSUMOS.CREAR_CON_DOSIS, insumoData);
    return response.data;
  },

  async actualizar(id: number, insumoData: any) {
    const response = await api.put(API_ENDPOINTS.INSUMOS.ACTUALIZAR(id), insumoData);
    return response.data;
  },

  async actualizarConDosis(id: number, insumoData: any) {
    const response = await api.put(API_ENDPOINTS.INSUMOS.ACTUALIZAR_CON_DOSIS(id), insumoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.INSUMOS.ELIMINAR(id));
    return response.data;
  },

  async obtenerConDosis(id: number) {
    const response = await api.get(API_ENDPOINTS.INSUMOS.CON_DOSIS(id));
    return response.data;
  },

  // Métodos para endpoint público
  async listarPublicos() {
    const response = await api.get('/public/insumos');
    return response.data;
  },

  async actualizarPublico(id: number, insumoData: any) {
    const response = await api.put(`/public/insumos/${id}`, insumoData);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE DOSIS DE AGROQUÍMICOS
// ============================================================================
export const dosisAgroquimicosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.DOSIS_AGROQUIMICOS.LISTAR);
    return response.data;
  },

  async obtenerPorInsumo(insumoId: number) {
    const response = await api.get(API_ENDPOINTS.DOSIS_AGROQUIMICOS.POR_INSUMO(insumoId));
    return response.data;
  },

  async crear(dosisData: any) {
    const response = await api.post(API_ENDPOINTS.DOSIS_AGROQUIMICOS.CREAR, dosisData);
    return response.data;
  },

  async actualizar(id: number, dosisData: any) {
    const response = await api.put(API_ENDPOINTS.DOSIS_AGROQUIMICOS.ACTUALIZAR(id), dosisData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.DOSIS_AGROQUIMICOS.ELIMINAR(id));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE LABORES
// ============================================================================
export const laboresService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.LABORES.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.LABORES.OBTENER(id));
    return response.data;
  },

  async crear(laborData: any) {
    const response = await api.post(API_ENDPOINTS.LABORES.CREAR, laborData);
    return response.data;
  },

  async actualizar(id: number, laborData: any) {
    const response = await api.put(API_ENDPOINTS.LABORES.ACTUALIZAR(id), laborData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.LABORES.ELIMINAR(id));
    return response.data;
  },

  async anular(id: number, anulacionData: any) {
    const response = await api.post(API_ENDPOINTS.LABORES.ANULAR(id), anulacionData);
    return response.data;
  },

  async actualizarCosto(id: number) {
    const response = await api.post(API_ENDPOINTS.LABORES.ACTUALIZAR_COSTO(id));
    return response.data;
  },

  async obtenerTareasDisponibles(estadoLote: string) {
    const response = await api.get(API_ENDPOINTS.LABORES.TAREAS_DISPONIBLES(estadoLote));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE CAMPOS
// ============================================================================
export const camposService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.CAMPOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.CAMPOS.OBTENER(id));
    return response.data;
  },

  async crear(campoData: any) {
    const response = await api.post(API_ENDPOINTS.CAMPOS.CREAR, campoData);
    return response.data;
  },

  async actualizar(id: number, campoData: any) {
    const response = await api.put(API_ENDPOINTS.CAMPOS.ACTUALIZAR(id), campoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.CAMPOS.ELIMINAR(id));
    return response.data;
  },

  // Métodos para endpoint público
  async listarPublicos() {
    const response = await api.get('/public/campos');
    return response.data;
  },

  // Métodos para v1
  async listarV1() {
    const response = await api.get('/v1/campos');
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE MAQUINARIA
// ============================================================================
export const maquinariaService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.MAQUINARIA.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.MAQUINARIA.OBTENER(id));
    return response.data;
  },

  async crear(maquinariaData: any) {
    const response = await api.post(API_ENDPOINTS.MAQUINARIA.CREAR, maquinariaData);
    return response.data;
  },

  async actualizar(id: number, maquinariaData: any) {
    const response = await api.put(API_ENDPOINTS.MAQUINARIA.ACTUALIZAR(id), maquinariaData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.MAQUINARIA.ELIMINAR(id));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE INGRESOS
// ============================================================================
export const ingresosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.INGRESOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.INGRESOS.OBTENER(id));
    return response.data;
  },

  async crear(ingresoData: any) {
    const response = await api.post(API_ENDPOINTS.INGRESOS.CREAR, ingresoData);
    return response.data;
  },

  async actualizar(id: number, ingresoData: any) {
    const response = await api.put(API_ENDPOINTS.INGRESOS.ACTUALIZAR(id), ingresoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.INGRESOS.ELIMINAR(id));
    return response.data;
  },

  // Métodos para v1
  async listarV1() {
    const response = await api.get(API_ENDPOINTS.INGRESOS.V1.LISTAR);
    return response.data;
  },

  async crearV1(ingresoData: any) {
    const response = await api.post(API_ENDPOINTS.INGRESOS.V1.CREAR, ingresoData);
    return response.data;
  },

  async actualizarV1(id: number, ingresoData: any) {
    const response = await api.put(API_ENDPOINTS.INGRESOS.V1.ACTUALIZAR(id), ingresoData);
    return response.data;
  },

  async eliminarV1(id: number) {
    const response = await api.delete(API_ENDPOINTS.INGRESOS.V1.ELIMINAR(id));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE EGRESOS
// ============================================================================
export const egresosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.EGRESOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.EGRESOS.OBTENER(id));
    return response.data;
  },

  async crear(egresoData: any) {
    const response = await api.post(API_ENDPOINTS.EGRESOS.CREAR, egresoData);
    return response.data;
  },

  async actualizar(id: number, egresoData: any) {
    const response = await api.put(API_ENDPOINTS.EGRESOS.ACTUALIZAR(id), egresoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.EGRESOS.ELIMINAR(id));
    return response.data;
  },

  // Métodos para v1
  async listarV1() {
    const response = await api.get(API_ENDPOINTS.EGRESOS.V1.LISTAR);
    return response.data;
  },

  async crearIntegrado(egresoData: any) {
    const response = await api.post(API_ENDPOINTS.EGRESOS.V1.INTEGRADO, egresoData);
    return response.data;
  },

  async eliminarV1(id: number) {
    const response = await api.delete(API_ENDPOINTS.EGRESOS.V1.ELIMINAR(id));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE ROLES
// ============================================================================
export const rolesService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.ROLES.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.ROLES.OBTENER(id));
    return response.data;
  },

  async crear(roleData: any) {
    const response = await api.post(API_ENDPOINTS.ROLES.CREAR, roleData);
    return response.data;
  },

  async actualizar(id: number, roleData: any) {
    const response = await api.put(API_ENDPOINTS.ROLES.ACTUALIZAR(id), roleData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.ROLES.ELIMINAR(id));
    return response.data;
  },

  async agregarPermiso(id: number, permission: string) {
    const response = await api.post(API_ENDPOINTS.ROLES.AGREGAR_PERMISO(id), { permission });
    return response.data;
  },

  async eliminarPermiso(id: number, permission: string) {
    const response = await api.delete(API_ENDPOINTS.ROLES.ELIMINAR_PERMISO(id), {
      data: { permission },
    });
    return response.data;
  },

  async obtenerPermisosDisponibles() {
    const response = await api.get(API_ENDPOINTS.ROLES.PERMISOS_DISPONIBLES);
    return response.data;
  },

  async obtenerEstadisticas() {
    const response = await api.get(API_ENDPOINTS.ROLES.ESTADISTICAS);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE APLICACIONES AGROQUÍMICAS
// ============================================================================
export const aplicacionesAgroquimicasService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.OBTENER(id));
    return response.data;
  },

  async crear(aplicacionData: any) {
    const response = await api.post(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.CREAR, aplicacionData);
    return response.data;
  },

  async actualizar(id: number, aplicacionData: any) {
    const response = await api.put(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.ACTUALIZAR(id), aplicacionData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.ELIMINAR(id));
    return response.data;
  },

  async obtenerPorLabor(laborId: number) {
    const response = await api.get(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.POR_LABOR(laborId));
    return response.data;
  },

  async obtenerDosisSugerida(insumoId: number, tipoAplicacion: string) {
    const response = await api.get(API_ENDPOINTS.APLICACIONES_AGROQUIMICAS.DOSIS_SUGERIR(insumoId, tipoAplicacion));
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE AGROQUÍMICOS INTEGRADOS
// ============================================================================
export const agroquimicosIntegradosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.OBTENER(id));
    return response.data;
  },

  async crear(agroquimicoData: any) {
    const response = await api.post(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.CREAR, agroquimicoData);
    return response.data;
  },

  async actualizar(id: number, agroquimicoData: any) {
    const response = await api.put(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.ACTUALIZAR(id), agroquimicoData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.ELIMINAR(id));
    return response.data;
  },

  async convertirInsumo(insumoId: number, datosAgroquimico: any) {
    const response = await api.post(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.CONVERTIR_INSUMO(insumoId), datosAgroquimico);
    return response.data;
  },

  async obtenerDosisRecomendada(agroquimicoId: number, tipoAplicacion: string) {
    const response = await api.get(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.DOSIS_RECOMENDADA(agroquimicoId, tipoAplicacion));
    return response.data;
  },

  async sugerirDosis(datosSugerencia: any) {
    const response = await api.post(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.SUGERIR_DOSIS, datosSugerencia);
    return response.data;
  },

  async planificarAplicacion(datosAplicacion: any) {
    const response = await api.post(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.PLANIFICAR_APLICACION, datosAplicacion);
    return response.data;
  },

  async obtenerPorTipoAplicacion(tipoAplicacion: string) {
    const response = await api.get(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.POR_TIPO_APLICACION(tipoAplicacion));
    return response.data;
  },

  async obtenerCondicionesRecomendadas() {
    const response = await api.get(API_ENDPOINTS.AGROQUIMICOS_INTEGRADOS.CONDICIONES_RECOMENDADAS);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE COSECHAS
// ============================================================================
export const cosechasService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.COSECHAS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.COSECHAS.OBTENER(id));
    return response.data;
  },

  async crear(cosechaData: any) {
    const response = await api.post(API_ENDPOINTS.COSECHAS.CREAR, cosechaData);
    return response.data;
  },

  async actualizar(id: number, cosechaData: any) {
    const response = await api.put(API_ENDPOINTS.COSECHAS.ACTUALIZAR(id), cosechaData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.COSECHAS.ELIMINAR(id));
    return response.data;
  },

  async liberarLote(loteId: number) {
    const response = await api.put(API_ENDPOINTS.HISTORIAL_COSECHAS.LIBERAR(loteId));
    return response.data;
  },

  async liberarLoteForzado(loteId: number, justificacion: string) {
    const response = await api.put(API_ENDPOINTS.HISTORIAL_COSECHAS.LIBERAR_FORZADO(loteId), { justificacion });
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE REPORTES
// ============================================================================
export const reportesService = {
  async obtenerRendimiento(params?: any) {
    const response = await api.get(API_ENDPOINTS.REPORTES.RENDIMIENTO, { params });
    return response.data;
  },

  async obtenerEstadisticasProduccion(params?: any) {
    const response = await api.get(API_ENDPOINTS.REPORTES.ESTADISTICAS_PRODUCCION, { params });
    return response.data;
  },

  async obtenerCosechas(params?: any) {
    const response = await api.get(API_ENDPOINTS.REPORTES.COSECHAS, { params });
    return response.data;
  },

  async obtenerRentabilidad(params?: any) {
    const response = await api.get(API_ENDPOINTS.REPORTES.RENTABILIDAD, { params });
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE INVENTARIO DE GRANOS
// ============================================================================
export const inventarioGranosService = {
  async listar() {
    const response = await api.get(API_ENDPOINTS.INVENTARIO_GRANOS.LISTAR);
    return response.data;
  },

  async obtener(id: number) {
    const response = await api.get(API_ENDPOINTS.INVENTARIO_GRANOS.OBTENER(id));
    return response.data;
  },

  async crear(inventarioData: any) {
    const response = await api.post(API_ENDPOINTS.INVENTARIO_GRANOS.CREAR, inventarioData);
    return response.data;
  },

  async actualizar(id: number, inventarioData: any) {
    const response = await api.put(API_ENDPOINTS.INVENTARIO_GRANOS.ACTUALIZAR(id), inventarioData);
    return response.data;
  },

  async eliminar(id: number) {
    const response = await api.delete(API_ENDPOINTS.INVENTARIO_GRANOS.ELIMINAR(id));
    return response.data;
  },

  async vender(ventaData: any) {
    const response = await api.post(API_ENDPOINTS.INVENTARIO_GRANOS.VENDER, ventaData);
    return response.data;
  },
};

// ============================================================================
// SERVICIO DE BALANCE
// ============================================================================
export const balanceService = {
  async obtenerGeneral(fechaInicio: string, fechaFin: string) {
    const response = await api.get(API_ENDPOINTS.BALANCE.GENERAL(fechaInicio, fechaFin));
    return response.data;
  },

  async obtenerPorLote(loteId: number, fechaInicio: string, fechaFin: string) {
    const response = await api.get(API_ENDPOINTS.BALANCE.LOTE(loteId, fechaInicio, fechaFin));
    return response.data;
  },

  async obtenerMesActual() {
    const response = await api.get(API_ENDPOINTS.BALANCE.MES_ACTUAL);
    return response.data;
  },

  async obtenerAñoActual() {
    const response = await api.get(API_ENDPOINTS.BALANCE.AÑO_ACTUAL);
    return response.data;
  },
};

// Exportar todos los servicios como un objeto único para facilitar imports
export default {
  auth: authService,
  usuarios: usuariosService,
  cultivos: cultivosService,
  lotes: lotesService,
  insumos: insumosService,
  dosisAgroquimicos: dosisAgroquimicosService,
  labores: laboresService,
  campos: camposService,
  maquinaria: maquinariaService,
  ingresos: ingresosService,
  egresos: egresosService,
  roles: rolesService,
  cosechas: cosechasService,
  reportes: reportesService,
  inventarioGranos: inventarioGranosService,
  aplicacionesAgroquimicas: aplicacionesAgroquimicasService,
  agroquimicosIntegrados: agroquimicosIntegradosService,
  balance: balanceService,
};

