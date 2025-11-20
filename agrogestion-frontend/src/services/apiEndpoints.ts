/**
 * Servicio centralizado de endpoints de API
 * Todas las rutas de la API deben definirse aquí para mantener consistencia
 * 
 * IMPORTANTE: NO incluir el prefijo /api/ ya que el interceptor lo agrega automáticamente
 */

export const API_ENDPOINTS = {
  // Autenticación
  AUTH: {
    LOGIN: '/auth/login',
    REGISTER: '/auth/register',
    LOGOUT: '/auth/logout',
    RESET_PASSWORD: '/auth/reset-password',
    REQUEST_RESET: '/auth/request-password-reset',
    CHANGE_PASSWORD: '/auth/change-password',
    VERIFY_EMAIL: '/auth/verify-email',
    USERS: '/auth/users',
    USER: (id: number) => `/auth/users/${id}`,
    USER_TOGGLE_STATUS: (id: number) => `/auth/users/${id}/toggle-status`,
    ROLES: '/auth/roles',
    STATS: '/auth/stats',
  },

  // Usuarios
  USUARIOS: {
    BASE: '/admin/usuarios',
    LISTAR: '/admin/usuarios',
    OBTENER: (id: number) => `/admin/usuarios/${id}`,
    CREAR: '/admin/usuarios',
    ACTUALIZAR: (id: number) => `/admin/usuarios/${id}`,
    ELIMINAR: (id: number) => `/admin/usuarios/${id}`,
    CAMBIAR_ESTADO: (id: number) => `/admin/usuarios/${id}/estado`,
    CAMBIAR_ACTIVO: (id: number) => `/admin/usuarios/${id}/activo`,
    RESET_PASSWORD: (id: number) => `/admin/usuarios/${id}/reset-password`,
    ESTADISTICAS: '/admin/usuarios/estadisticas',
    ROLES: '/admin/usuarios/roles',
  },

  // Roles
  ROLES: {
    BASE: '/roles',
    LISTAR: '/roles',
    OBTENER: (id: number) => `/roles/${id}`,
    CREAR: '/roles',
    ACTUALIZAR: (id: number) => `/roles/${id}`,
    ELIMINAR: (id: number) => `/roles/${id}`,
    PERMISOS: (id: number) => `/roles/${id}/permissions`,
    AGREGAR_PERMISO: (id: number) => `/roles/${id}/permissions`,
    ELIMINAR_PERMISO: (id: number) => `/roles/${id}/permissions`,
    PERMISOS_DISPONIBLES: '/roles/permissions/available',
    ESTADISTICAS: '/roles/stats',
  },

  // Campos
  CAMPOS: {
    BASE: '/campos',
    LISTAR: '/campos',
    OBTENER: (id: number) => `/campos/${id}`,
    CREAR: '/campos',
    ACTUALIZAR: (id: number) => `/campos/${id}`,
    ELIMINAR: (id: number) => `/campos/${id}`,
  },

  // Lotes
  LOTES: {
    BASE: '/v1/lotes',
    LISTAR: '/v1/lotes',
    OBTENER: (id: number) => `/v1/lotes/${id}`,
    CREAR: '/v1/lotes',
    ACTUALIZAR: (id: number) => `/v1/lotes/${id}`,
    ELIMINAR: (id: number) => `/v1/lotes/${id}`,
    SEMBRAR: (id: number) => `/v1/lotes/${id}/sembrar`,
    COSECHAR: (id: number) => `/v1/lotes/${id}/cosechar`,
    INFO_COSECHA: (id: number) => `/v1/lotes/${id}/info-cosecha`,
    ABANDONAR: (id: number) => `/v1/lotes/${id}/abandonar`,
    LIMPIAR: (id: number) => `/v1/lotes/${id}/limpiar`,
    CONVERTIR_FORRAJE: (id: number) => `/v1/lotes/${id}/convertir-forraje`,
  },

  // Cultivos
  CULTIVOS: {
    BASE: '/v1/cultivos',
    LISTAR: '/v1/cultivos',
    OBTENER: (id: number) => `/v1/cultivos/${id}`,
    CREAR: '/v1/cultivos',
    ACTUALIZAR: (id: number) => `/v1/cultivos/${id}`,
    ELIMINAR: (id: number) => `/v1/cultivos/${id}`,
    ELIMINAR_FISICO: (id: number) => `/v1/cultivos/${id}/fisico`,
    RESTAURAR: (id: number) => `/v1/cultivos/${id}/restaurar`,
    BUSCAR: '/v1/cultivos/buscar',
    ELIMINADOS: '/v1/cultivos/eliminados',
  },

  // Insumos
  INSUMOS: {
    BASE: '/insumos',
    LISTAR: '/insumos',
    OBTENER: (id: number) => `/insumos/${id}`,
    CREAR: '/insumos',
    CREAR_CON_DOSIS: '/insumos/con-dosis',
    ACTUALIZAR: (id: number) => `/insumos/${id}`,
    ACTUALIZAR_CON_DOSIS: (id: number) => `/insumos/${id}/con-dosis`,
    ELIMINAR: (id: number) => `/insumos/${id}`,
    CON_DOSIS: (id: number) => `/insumos/${id}/con-dosis`,
  },

  // Dosis de Agroquímicos
  DOSIS_AGROQUIMICOS: {
    BASE: '/dosis-agroquimicos',
    LISTAR: '/dosis-agroquimicos',
    POR_INSUMO: (insumoId: number) => `/dosis-agroquimicos/insumo/${insumoId}`,
    CREAR: '/dosis-agroquimicos',
    ACTUALIZAR: (id: number) => `/dosis-agroquimicos/${id}`,
    ELIMINAR: (id: number) => `/dosis-agroquimicos/${id}`,
  },

  // Labores
  LABORES: {
    BASE: '/labores',
    LISTAR: '/labores',
    OBTENER: (id: number) => `/labores/${id}`,
    CREAR: '/labores',
    ACTUALIZAR: (id: number) => `/labores/${id}`,
    ELIMINAR: (id: number) => `/labores/${id}`,
    ANULAR: (id: number) => `/labores/${id}/anular`,
    ACTUALIZAR_COSTO: (id: number) => `/labores/${id}/actualizar-costo`,
    TAREAS_DISPONIBLES: (estadoLote: string) => `/labores/tareas-disponibles/${estadoLote}`,
  },

  // Maquinaria
  MAQUINARIA: {
    BASE: '/maquinaria',
    LISTAR: '/maquinaria',
    OBTENER: (id: number) => `/maquinaria/${id}`,
    CREAR: '/maquinaria',
    ACTUALIZAR: (id: number) => `/maquinaria/${id}`,
    ELIMINAR: (id: number) => `/maquinaria/${id}`,
  },

  // Ingresos
  INGRESOS: {
    BASE: '/public/ingresos',
    LISTAR: '/public/ingresos',
    OBTENER: (id: number) => `/public/ingresos/${id}`,
    CREAR: '/public/ingresos',
    ACTUALIZAR: (id: number) => `/public/ingresos/${id}`,
    ELIMINAR: (id: number) => `/public/ingresos/${id}`,
    V1: {
      BASE: '/v1/ingresos',
      LISTAR: '/v1/ingresos',
      OBTENER: (id: number) => `/v1/ingresos/${id}`,
      CREAR: '/v1/ingresos',
      ACTUALIZAR: (id: number) => `/v1/ingresos/${id}`,
      ELIMINAR: (id: number) => `/v1/ingresos/${id}`,
    },
  },

  // Egresos
  EGRESOS: {
    BASE: '/public/egresos',
    LISTAR: '/public/egresos',
    OBTENER: (id: number) => `/public/egresos/${id}`,
    CREAR: '/public/egresos',
    ACTUALIZAR: (id: number) => `/public/egresos/${id}`,
    ELIMINAR: (id: number) => `/public/egresos/${id}`,
    V1: {
      BASE: '/v1/egresos',
      LISTAR: '/v1/egresos',
      OBTENER: (id: number) => `/v1/egresos/${id}`,
      CREAR: '/v1/egresos',
      ACTUALIZAR: (id: number) => `/v1/egresos/${id}`,
      ELIMINAR: (id: number) => `/v1/egresos/${id}`,
      INTEGRADO: '/v1/egresos/integrado',
    },
  },

  // Cosechas
  COSECHAS: {
    BASE: '/cosechas',
    LISTAR: '/v1/cosechas',
    OBTENER: (id: number) => `/cosechas/${id}`,
    CREAR: '/cosechas',
    ACTUALIZAR: (id: number) => `/cosechas/${id}`,
    ELIMINAR: (id: number) => `/cosechas/${id}`,
  },

  // Historial Cosechas
  HISTORIAL_COSECHAS: {
    LIBERAR: (loteId: number) => `/historial-cosechas/lote/${loteId}/liberar`,
    LIBERAR_FORZADO: (loteId: number) => `/historial-cosechas/lote/${loteId}/liberar-forzado`,
  },

  // Aplicaciones Agroquímicas
  APLICACIONES_AGROQUIMICAS: {
    BASE: '/aplicaciones-agroquimicas',
    LISTAR: '/aplicaciones-agroquimicas',
    OBTENER: (id: number) => `/aplicaciones-agroquimicas/${id}`,
    CREAR: '/aplicaciones-agroquimicas',
    ACTUALIZAR: (id: number) => `/aplicaciones-agroquimicas/${id}`,
    ELIMINAR: (id: number) => `/aplicaciones-agroquimicas/${id}`,
    POR_LABOR: (laborId: number) => `/aplicaciones-agroquimicas/labor/${laborId}`,
    DOSIS_SUGERIR: (insumoId: number, tipoAplicacion: string) => `/aplicaciones-agroquimicas/dosis/sugerir?insumoId=${insumoId}&tipoAplicacion=${tipoAplicacion}`,
  },

  // Agroquímicos Integrados
  AGROQUIMICOS_INTEGRADOS: {
    BASE: '/v1/agroquimicos-integrados',
    LISTAR: '/v1/agroquimicos-integrados',
    OBTENER: (id: number) => `/v1/agroquimicos-integrados/${id}`,
    CREAR: '/v1/agroquimicos-integrados',
    ACTUALIZAR: (id: number) => `/v1/agroquimicos-integrados/${id}`,
    ELIMINAR: (id: number) => `/v1/agroquimicos-integrados/${id}`,
    CONVERTIR_INSUMO: (insumoId: number) => `/v1/agroquimicos-integrados/convertir-insumo/${insumoId}`,
    DOSIS_RECOMENDADA: (agroquimicoId: number, tipoAplicacion: string) => `/v1/agroquimicos-integrados/agroquimicos/${agroquimicoId}/dosis-recomendada?tipoAplicacion=${tipoAplicacion}`,
    SUGERIR_DOSIS: '/v1/agroquimicos-integrados/sugerir-dosis',
    PLANIFICAR_APLICACION: '/v1/agroquimicos-integrados/planificar-aplicacion',
    POR_TIPO_APLICACION: (tipoAplicacion: string) => `/v1/agroquimicos-integrados/tipo-aplicacion/${tipoAplicacion}`,
    CONDICIONES_RECOMENDADAS: '/v1/agroquimicos-integrados/condiciones-recomendadas',
  },

  // Inventario Granos
  INVENTARIO_GRANOS: {
    BASE: '/v1/inventario-granos',
    LISTAR: '/v1/inventario-granos',
    OBTENER: (id: number) => `/v1/inventario-granos/${id}`,
    CREAR: '/v1/inventario-granos',
    ACTUALIZAR: (id: number) => `/v1/inventario-granos/${id}`,
    ELIMINAR: (id: number) => `/v1/inventario-granos/${id}`,
    VENDER: '/v1/inventario-granos/vender',
  },

  // Reportes
  REPORTES: {
    BASE: '/v1/reportes',
    RENDIMIENTO: '/v1/reportes/rendimiento',
    ESTADISTICAS_PRODUCCION: '/v1/reportes/estadisticas-produccion',
    COSECHAS: '/v1/reportes/cosechas',
    RENTABILIDAD: '/v1/reportes/rentabilidad',
  },

  // Balance
  BALANCE: {
    BASE: '/v1/balance',
    GENERAL: (fechaInicio: string, fechaFin: string) => `/v1/balance/general?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`,
    LOTE: (loteId: number, fechaInicio: string, fechaFin: string) => `/v1/balance/lote/${loteId}?fechaInicio=${fechaInicio}&fechaFin=${fechaFin}`,
    MES_ACTUAL: '/v1/balance/mes-actual',
    AÑO_ACTUAL: '/v1/balance/año-actual',
  },

  // Dashboard
  DASHBOARD: {
    BASE: '/dashboard',
    ESTADISTICAS: '/dashboard/estadisticas',
    RESUMEN: '/dashboard/resumen',
  },

  // Admin Dashboard
  ADMIN_DASHBOARD: {
    RESUMEN: '/admin/dashboard/resumen',
    USUARIOS_LISTA: '/admin/dashboard/usuarios/lista',
    USO_SISTEMA: '/admin/dashboard/uso-sistema',
    AUDITORIA: '/admin/dashboard/auditoria',
    REPORTES: '/admin/dashboard/reportes',
  },

  // Admin Global
  ADMIN_GLOBAL: {
    BASE: '/admin-global',
    DASHBOARD_SIMPLE: '/admin-global/dashboard-simple',
    ESTADISTICAS: '/admin-global/estadisticas',
    ESTADISTICAS_USO: '/admin-global/estadisticas-uso',
    ESTADISTICAS_CLIMA: '/admin-global/estadisticas-clima',
    EMPRESAS: '/admin-global/empresas',
    EMPRESAS_BASIC: '/admin-global/empresas-basic',
    USUARIOS_BASIC: '/admin-global/usuarios-basic',
  },

  // Empresas
  EMPRESAS: {
    BASE: '/v1/empresas',
    MIS_EMPRESAS: '/v1/empresas/mis-empresas',
  },

  // Empresa Usuario
  EMPRESA_USUARIO: {
    ROLES_EMPRESA: '/roles-empresa',
    TODAS_RELACIONES: '/empresa-usuario/todas-relaciones',
    USUARIOS_EMPRESA: (empresaId: number) => `/empresa-usuario/empresa/${empresaId}/usuarios`,
    ASIGNAR: '/empresa-usuario/asignar',
    CAMBIAR_ROL: '/empresa-usuario/cambiar-rol',
    ASIGNAR_ROLES: '/empresa-usuario/asignar-roles',
    ROLES_USUARIO: '/empresa-usuario/roles-usuario',
    REMOVER: (usuarioId: number, empresaId: number) => `/empresa-usuario/remover/${usuarioId}/${empresaId}`,
  },

  // Usuarios (endpoint básico)
  USUARIOS_BASIC: {
    LISTAR: '/admin/usuarios/basic',
  },
} as const;

// Exportar un helper para obtener endpoints de forma segura
export const getEndpoint = (endpoint: string | ((...args: any[]) => string), ...args: any[]): string => {
  if (typeof endpoint === 'function') {
    return endpoint(...args);
  }
  return endpoint;
};

