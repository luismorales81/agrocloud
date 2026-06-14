/**
 * Servicios de roles, reportes, aplicaciones agroquímicas, agroquímicos integrados,
 * cosechas, inventario granos y configuración de estados.
 */

import api from '../api';
import { API_ENDPOINTS } from '../apiEndpoints';

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
    const response = await api.delete(API_ENDPOINTS.ROLES.ELIMINAR_PERMISO(id), { data: { permission } });
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

export const configuracionEstadosService = {
  async obtenerTiposCultivo() {
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPOS_CULTIVO);
    return response.data;
  },

  async obtenerPlantillas() {
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPOS_CULTIVO_PLANTILLAS);
    return response.data;
  },

  async obtenerTipoCultivo(id: number) {
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPO_CULTIVO(id));
    return response.data;
  },

  async crearTipoCultivo(data: any) {
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPOS_CULTIVO, data);
    return response.data;
  },

  async actualizarTipoCultivo(id: number, data: any) {
    const response = await api.put(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPO_CULTIVO(id), data);
    return response.data;
  },

  async eliminarTipoCultivo(id: number) {
    const response = await api.delete(API_ENDPOINTS.CONFIGURACION_ESTADOS.TIPO_CULTIVO(id));
    return response.data;
  },

  async obtenerEstados(tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADOS, { params });
    return response.data;
  },

  async obtenerEstado(id: number) {
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADO(id));
    return response.data;
  },

  async crearEstado(data: any, tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADOS, data, { params });
    return response.data;
  },

  async actualizarEstado(id: number, data: any) {
    const response = await api.put(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADO(id), data);
    return response.data;
  },

  async reordenarEstados(estadoIds: number[]) {
    const response = await api.put(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADOS_REORDENAR, estadoIds);
    return response.data;
  },

  async eliminarEstado(id: number) {
    const response = await api.delete(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADO(id));
    return response.data;
  },

  async copiarPlantillaAEmpresa(tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.ESTADOS_COPIAR_PLANTILLA, null, { params });
    return response.data;
  },

  async obtenerTransiciones(tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TRANSICIONES, { params });
    return response.data;
  },

  async obtenerTransicionesDesdeEstado(estadoOrigenId: number, empresaId?: number) {
    const params = empresaId ? { empresaId } : {};
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TRANSICIONES_DESDE_ESTADO(estadoOrigenId), { params });
    return response.data;
  },

  async crearTransicion(data: any, tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.TRANSICIONES, data, { params });
    return response.data;
  },

  async eliminarTransicion(id: number) {
    const response = await api.delete(API_ENDPOINTS.CONFIGURACION_ESTADOS.TRANSICION(id));
    return response.data;
  },

  async validarTransicion(estadoOrigenId: number, estadoDestinoId: number, empresaId?: number) {
    const params = empresaId ? { estadoOrigenId, estadoDestinoId, empresaId } : { estadoOrigenId, estadoDestinoId };
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TRANSICIONES_VALIDAR, { params });
    return response.data;
  },

  async descargarPlantillaExcel(): Promise<Blob> {
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.PLANTILLA_EXCEL, {
      responseType: 'blob',
    });
    return response.data;
  },

  async importarDesdeExcel(
    archivo: File,
    nombreTipoCultivo?: string,
    tipoCultivoId?: number,
    empresaId?: number
  ) {
    const formData = new FormData();
    formData.append('archivo', archivo);
    if (nombreTipoCultivo) formData.append('nombreTipoCultivo', nombreTipoCultivo);
    if (tipoCultivoId) formData.append('tipoCultivoId', String(tipoCultivoId));
    if (empresaId) formData.append('empresaId', String(empresaId));
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.IMPORTAR, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
    return response.data;
  },

  async obtenerTareas(estadoId: number, empresaId?: number) {
    const params = empresaId ? { estadoId, empresaId } : { estadoId };
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREAS, { params });
    return response.data;
  },

  async crearTarea(data: any, tipoCultivoId: number, empresaId?: number) {
    const params = empresaId ? { tipoCultivoId, empresaId } : { tipoCultivoId };
    const response = await api.post(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREAS, data, { params });
    return response.data;
  },

  async actualizarTarea(id: number, data: any) {
    const response = await api.put(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREA(id), data);
    return response.data;
  },

  async reordenarTareas(estadoId: number, tareaIds: number[]) {
    const params = { estadoId };
    const response = await api.put(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREAS_REORDENAR, tareaIds, { params });
    return response.data;
  },

  async eliminarTarea(id: number) {
    const response = await api.delete(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREA(id));
    return response.data;
  },

  async validarTarea(estadoId: number, tipoLabor: string, empresaId?: number) {
    const params = empresaId ? { estadoId, tipoLabor, empresaId } : { estadoId, tipoLabor };
    const response = await api.get(API_ENDPOINTS.CONFIGURACION_ESTADOS.TAREAS_VALIDAR, { params });
    return response.data;
  },
};
