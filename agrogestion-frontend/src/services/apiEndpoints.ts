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
    /** Solo lotes de uso cultivo (excluye porcinos). Para labores, lotes y calendario. */
    LISTAR_CULTIVO: '/v1/lotes/cultivo',
    OBTENER: (id: number) => `/v1/lotes/${id}`,
    CREAR: '/v1/lotes',
    ACTUALIZAR: (id: number) => `/v1/lotes/${id}`,
    ELIMINAR: (id: number) => `/v1/lotes/${id}`,
    SEMBRAR: (id: number) => `/v1/lotes/${id}/sembrar`,
    COSECHAR: (id: number) => `/v1/lotes/${id}/cosechar`,
    INFO_COSECHA: (id: number) => `/v1/lotes/${id}/info-cosecha`,
    ABANDONAR: (id: number) => `/v1/lotes/${id}/abandonar`,
    CONVERTIR_FORRAJE: (id: number) => `/v1/lotes/${id}/convertir-forraje`,
    RESETEAR: (id: number) => `/v1/lotes/${id}/resetear`,
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

  // Labores (spec SDD: calendario y tareas derivan de labores)
  LABORES: {
    BASE: '/labores',
    LISTAR: '/labores',
    /** GET con query: fecha_desde, fecha_hasta, lote_id, estado, overdue */
    LISTAR_CON_FILTROS: '/labores',
    OBTENER: (id: number) => `/labores/${id}`,
    CREAR: '/labores',
    ACTUALIZAR: (id: number) => `/labores/${id}`,
    /** PATCH parcial: estado, fecha_planificada, fecha_realizacion, observaciones */
    ACTUALIZAR_PARCIAL: (id: number) => `/labores/${id}`,
    ELIMINAR: (id: number) => `/labores/${id}`,
    ANULAR: (id: number) => `/labores/${id}/anular`,
    ACTUALIZAR_COSTO: (id: number) => `/labores/${id}/actualizar-costo`,
    TAREAS_DISPONIBLES: (estadoLote: string) => `/labores/tareas-disponibles/${estadoLote}`,
    TAREAS_DISPONIBLES_POR_LOTE: (loteId: number) => `/labores/tareas-disponibles/lote/${loteId}`,
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

  // Porcinos - Madres
  PORCINOS_MADRES: {
    BASE: '/v1/porcinos/madres',
    LISTAR: '/v1/porcinos/madres',
    OBTENER: (id: number) => `/v1/porcinos/madres/${id}`,
    CREAR: '/v1/porcinos/madres',
    ACTUALIZAR: (id: number) => `/v1/porcinos/madres/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/madres/${id}`,
    HISTORIAL: (id: number) => `/v1/porcinos/madres/${id}/historial`,
    CAMBIAR_ESTADO: (id: number) => `/v1/porcinos/madres/${id}/cambiar-estado`,
    REGISTRAR_MUERTE: (id: number) => `/v1/porcinos/madres/${id}/muerte`,
    MUERTES: (id: number) => `/v1/porcinos/madres/${id}/muertes`,
    TODAS_MUERTES: '/v1/porcinos/madres/muertes',
  },

  // Porcinos - Servicios
  PORCINOS_SERVICIOS: {
    BASE: '/v1/porcinos/servicios',
    LISTAR: '/v1/porcinos/servicios',
    OBTENER: (id: number) => `/v1/porcinos/servicios/${id}`,
    CREAR: '/v1/porcinos/servicios',
    ACTUALIZAR: (id: number) => `/v1/porcinos/servicios/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/servicios/${id}`,
    POR_MADRE: (madreId: number) => `/v1/porcinos/servicios/madre/${madreId}`,
    PENDIENTES_CONTROL: '/v1/porcinos/servicios/pendientes-control',
    CONTROL_CELO: (id: number) => `/v1/porcinos/servicios/${id}/control-celo`,
    GESTACION_POR_SERVICIO: (id: number) => `/v1/porcinos/servicios/${id}/gestacion`,
  },

  // Porcinos - Gestación
  PORCINOS_GESTACION: {
    BASE: '/v1/porcinos/gestacion',
    LISTAR: '/v1/porcinos/gestacion',
    OBTENER: (id: number) => `/v1/porcinos/gestacion/${id}`,
    CREAR: '/v1/porcinos/gestacion',
    ACTUALIZAR: (id: number) => `/v1/porcinos/gestacion/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/gestacion/${id}`,
    ACTIVAS: '/v1/porcinos/gestacion/activas',
    PROXIMOS_PARTOS: '/v1/porcinos/gestacion/proximos-partos',
    POR_MADRE: (madreId: number) => `/v1/porcinos/gestacion/madre/${madreId}`,
    REGISTRAR_ABORTO: (id: number) => `/v1/porcinos/gestacion/${id}/aborto`,
    FINALIZAR: (id: number) => `/v1/porcinos/gestacion/${id}/finalizar`,
    CHEQUEOS: (id: number) => `/v1/porcinos/gestacion/${id}/chequeos`,
  },

  // Porcinos - Partos
  PORCINOS_PARTOS: {
    BASE: '/v1/porcinos/partos',
    LISTAR: '/v1/porcinos/partos',
    OBTENER: (id: number) => `/v1/porcinos/partos/${id}`,
    CREAR: '/v1/porcinos/partos',
    ACTUALIZAR: (id: number) => `/v1/porcinos/partos/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/partos/${id}`,
    POR_MADRE: (madreId: number) => `/v1/porcinos/partos/madre/${madreId}`,
    PROXIMOS: '/v1/porcinos/partos/proximos',
    MUERTES: (id: number) => `/v1/porcinos/partos/${id}/muertes`,
    REGISTRAR_MUERTE: (id: number) => `/v1/porcinos/partos/${id}/muertes`,
    DESTETE: (id: number) => `/v1/porcinos/partos/${id}/destete`,
    REGISTRAR_DESTETE: (id: number) => `/v1/porcinos/partos/${id}/destete`,
    DESTETES: '/v1/porcinos/partos/destetes',
  },

  // Porcinos - Destetes
  PORCINOS_DESTETES: {
    BASE: '/v1/porcinos/destetes',
    LISTAR: '/v1/porcinos/destetes',
    OBTENER: (id: number) => `/v1/porcinos/destetes/${id}`,
    CREAR: '/v1/porcinos/destetes',
    POR_PARTO: (partoId: number) => `/v1/porcinos/destetes/por-parto/${partoId}`,
  },

  // Porcinos - Recría
  PORCINOS_RECRIA: {
    BASE: '/v1/porcinos/recria',
    LISTAR: '/v1/porcinos/recria',
    OBTENER: (id: number) => `/v1/porcinos/recria/${id}`,
    CREAR: '/v1/porcinos/recria',
    ACTUALIZAR: (id: number) => `/v1/porcinos/recria/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/recria/${id}`,
    POR_LOTE: (loteId: number) => `/v1/porcinos/recria/lote/${loteId}`,
    REGISTRAR_MUERTE: (id: number) => `/v1/porcinos/recria/${id}/muertes`,
    MUERTES: (id: number) => `/v1/porcinos/recria/${id}/muertes`,
    TODAS_MUERTES: '/v1/porcinos/recria/muertes',
    REGISTRAR_EGRESO: (id: number) => `/v1/porcinos/recria/${id}/egreso`,
  },

  /** Planes de recría guardados (listado, consulta, actualización, desactivación) */
  PORCINOS_PLANES_RECRIA: {
    LISTAR: '/v1/porcinos/planes-recria',
    OBTENER: (id: number) => `/v1/porcinos/planes-recria/${id}`,
    ACTUALIZAR: (id: number) => `/v1/porcinos/planes-recria/${id}`,
    DESACTIVAR: (id: number) => `/v1/porcinos/planes-recria/${id}`,
  },

  // Porcinos - Alimentación
  PORCINOS_FORMULAS: {
    BASE: '/v1/porcinos/formulas',
    LISTAR: '/v1/porcinos/formulas',
    OBTENER: (id: number) => `/v1/porcinos/formulas/${id}`,
    CREAR: '/v1/porcinos/formulas',
    ACTUALIZAR: (id: number) => `/v1/porcinos/formulas/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/formulas/${id}`,
    POR_ETAPA: (etapa: string) => `/v1/porcinos/formulas/etapa/${etapa}`,
    ACTIVAR: (id: number) => `/v1/porcinos/formulas/${id}/activar`,
    DESACTIVAR: (id: number) => `/v1/porcinos/formulas/${id}/desactivar`,
  },

  PORCINOS_CONSUMOS: {
    BASE: '/v1/porcinos/consumos',
    LISTAR: '/v1/porcinos/consumos',
    OBTENER: (id: number) => `/v1/porcinos/consumos/${id}`,
    CREAR: '/v1/porcinos/consumos',
    ACTUALIZAR: (id: number) => `/v1/porcinos/consumos/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/consumos/${id}`,
    POR_LOTE: (loteId: number) => `/v1/porcinos/consumos/lote/${loteId}`,
    POR_MADRE: (madreId: number) => `/v1/porcinos/consumos/madre/${madreId}`,
    HISTORIAL: '/v1/porcinos/consumos/historial',
  },

  // Porcinos - Alimentación (Consumos de Alimento)
  PORCINOS_ALIMENTACION: {
    BASE: '/v1/porcinos/alimentacion',
    CONSUMOS: '/v1/porcinos/alimentacion/consumos',
    REGISTRAR_CONSUMO: '/v1/porcinos/alimentacion/consumos',
    CONSUMO_TOTAL_CATEGORIA: '/v1/porcinos/alimentacion/consumos/total-categoria',
  },

  /**
   * Calendario de alimentación automática (controlador bajo /api/porcinos/calendario-alimentacion, sin /v1).
   */
  PORCINOS_CALENDARIO_ALIMENTACION: {
    BASE: '/porcinos/calendario-alimentacion',
    MENSUAL: (ano: number, mes: number) =>
      `/porcinos/calendario-alimentacion/mensual?ano=${ano}&mes=${mes}`,
    DETALLE_DIA: (fechaIso: string) => `/porcinos/calendario-alimentacion/dia/${fechaIso}`,
    REGISTRAR_CANTIDAD_REAL: (consumoId: number) =>
      `/porcinos/calendario-alimentacion/consumo/${consumoId}/cantidad-real`,
    CONFIRMAR_DIA: (fechaIso: string) =>
      `/porcinos/calendario-alimentacion/dia/${fechaIso}/confirmar`,
  },

  // Porcinos - Ventas
  PORCINOS_VENTAS: {
    BASE: '/v1/porcinos/ventas',
    LISTAR: '/v1/porcinos/ventas',
    CREAR: '/v1/porcinos/ventas',
    INGRESOS_TOTALES: '/v1/porcinos/ventas/ingresos-totales',
  },

  // Porcinos - Dashboard
  PORCINOS_DASHBOARD: {
    KPIS: '/v1/porcinos/dashboard/kpis',
    ALERTAS: '/v1/porcinos/dashboard/alertas',
  },

  // Porcinos - Configuraciones
  PORCINOS_CONFIGURACIONES: {
    BASE: '/v1/porcinos/configuraciones',
    LISTAR: '/v1/porcinos/configuraciones',
    OBTENER: (clave: string) => `/v1/porcinos/configuraciones/${clave}`,
    POR_CATEGORIA: (categoria: string) => `/v1/porcinos/configuraciones/categoria/${categoria}`,
    GUARDAR: '/v1/porcinos/configuraciones',
  },

  // Porcinos - Padrillos
  PORCINOS_PADRILLOS: {
    BASE: '/v1/porcinos/padrillos',
    LISTAR: '/v1/porcinos/padrillos',
    OBTENER: (id: number) => `/v1/porcinos/padrillos/${id}`,
    CREAR: '/v1/porcinos/padrillos',
    ACTUALIZAR: (id: number) => `/v1/porcinos/padrillos/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/padrillos/${id}`,
  },

  // Porcinos - Transferencias de Lechones
  PORCINOS_TRANSFERENCIAS: {
    BASE: '/v1/porcinos/partos/transferencias',
    CREAR: '/v1/porcinos/partos/transferencias',
    POR_PARTO: (partoId: number) => `/v1/porcinos/partos/${partoId}/transferencias`,
    LISTAR: '/v1/porcinos/partos/transferencias',
  },

  // Porcinos - Registro de Pesos
  PORCINOS_PESOS: {
    REGISTRAR: (recriaId: number) => `/v1/porcinos/recria/${recriaId}/pesos`,
    POR_RECRIA: (recriaId: number) => `/v1/porcinos/recria/${recriaId}/pesos`,
    GDP: (recriaId: number) => `/v1/porcinos/recria/${recriaId}/gdp`,
  },

  // Porcinos - Movimientos entre Etapas
  PORCINOS_MOVIMIENTOS: {
    CREAR: (recriaId: number) => `/v1/porcinos/recria/${recriaId}/mover-etapa`,
    POR_RECRIA: (recriaId: number) => `/v1/porcinos/recria/${recriaId}/movimientos`,
    LISTAR: '/v1/porcinos/recria/movimientos',
  },

  // Porcinos - Faena
  PORCINOS_FAENA: {
    BASE: '/v1/porcinos/faena',
    REGISTRAR: (recriaId: number) => `/v1/porcinos/faena/${recriaId}`,
    POR_RECRIA: (recriaId: number) => `/v1/porcinos/faena/${recriaId}`,
    LISTAR: '/v1/porcinos/faena',
    INGRESOS_TOTALES: '/v1/porcinos/faena/ingresos-totales',
  },

  // Porcinos - Catálogos Configurables
  PORCINOS_CATALOGOS: {
    BASE: '/v1/porcinos/catalogos',
    LOTES: '/v1/porcinos/catalogos/lotes',
    // Razas
    RAZAS: '/v1/porcinos/catalogos/razas',
    RAZAS_POR_TIPO: (tipo: string) => `/v1/porcinos/catalogos/razas/tipo/${tipo}`,
    // Tipos de Alimento (ELIMINADO - REDUNDANTE)
    // TIPOS_ALIMENTO: '/v1/porcinos/catalogos/tipos-alimento',
    // Tipos de Servicio
    TIPOS_SERVICIO: '/v1/porcinos/catalogos/tipos-servicio',
    // Causas de Mortalidad
    CAUSAS_MORTALIDAD: '/v1/porcinos/catalogos/causas-mortalidad',
    // Motivos de Baja
    MOTIVOS_BAJA: '/v1/porcinos/catalogos/motivos-baja',
    // Esquemas Sanitarios
    ESQUEMAS_SANITARIOS: '/v1/porcinos/catalogos/esquemas-sanitarios',
    // Tipos de Parto
    TIPOS_PARTO: '/v1/porcinos/catalogos/tipos-parto',
    // Ubicaciones Internas
    UBICACIONES_INTERNAS: '/v1/porcinos/catalogos/ubicaciones-internas',
    UBICACIONES_POR_NIVEL: (nivel: string) => `/v1/porcinos/catalogos/ubicaciones-internas/nivel/${nivel}`,
    UBICACIONES_HIJAS: (padreId: number) => `/v1/porcinos/catalogos/ubicaciones-internas/padre/${padreId}`,
    // Tipos de Evento Sanitario
    TIPOS_EVENTO_SANITARIO: '/v1/porcinos/catalogos/tipos-evento-sanitario',
    TIPOS_EVENTO_SANITARIO_POR_CATEGORIA: (categoria: string) => `/v1/porcinos/catalogos/tipos-evento-sanitario/categoria/${categoria}`,
  },

  // Porcinos - Eventos Sanitarios
  PORCINOS_EVENTOS_SANITARIOS: {
    BASE: '/v1/porcinos/eventos-sanitarios',
    LISTAR: '/v1/porcinos/eventos-sanitarios',
    OBTENER: (id: number) => `/v1/porcinos/eventos-sanitarios/${id}`,
    CREAR: '/v1/porcinos/eventos-sanitarios',
    ACTUALIZAR: (id: number) => `/v1/porcinos/eventos-sanitarios/${id}`,
    ELIMINAR: (id: number) => `/v1/porcinos/eventos-sanitarios/${id}`,
    POR_ENTIDAD: (tipoEntidad: string, entidadId: number) => `/v1/porcinos/eventos-sanitarios/entidad/${tipoEntidad}/${entidadId}`,
    POR_RANGO: '/v1/porcinos/eventos-sanitarios/rango',
    MARCAR_RETIRO_CUMPLIDO: (id: number) => `/v1/porcinos/eventos-sanitarios/${id}/marcar-retiro-cumplido`,
    RETIROS_VENCIDOS: '/v1/porcinos/eventos-sanitarios/retiros/vencidos',
    RETIROS_PROXIMOS: '/v1/porcinos/eventos-sanitarios/retiros/proximos',
  },

  // Porcinos - Parámetros
  PORCINOS_PARAMETROS: {
    BASE: '/v1/porcinos/parametros',
    ESTABLECIMIENTO: '/v1/porcinos/parametros/establecimiento',
    PRODUCTIVOS: '/v1/porcinos/parametros/productivos',
    ECONOMICOS: '/v1/porcinos/parametros/economicos',
  },

  // Usuarios (endpoint básico)
  USUARIOS_BASIC: {
    LISTAR: '/admin/usuarios/basic',
  },

  // Módulos (Sistema de Feature Flags - Solo SuperAdmin)
  MODULOS: {
    BASE: '/admin/modules',
    LISTAR: '/admin/modules',
    OBTENER: (id: number) => `/admin/modules/${id}`,
    CREAR: '/admin/modules',
    ACTUALIZAR: (id: number) => `/admin/modules/${id}`,
    ELIMINAR: (id: number) => `/admin/modules/${id}`,
  },

  // Módulos por Empresa
  COMPANY_MODULES: {
    BASE: '/admin/companies',
    HABILITAR: (companyId: number, moduleId: number) => `/admin/companies/${companyId}/modules/${moduleId}/enable`,
    DESHABILITAR: (companyId: number, moduleId: number) => `/admin/companies/${companyId}/modules/${moduleId}/disable`,
    LISTAR: (companyId: number) => `/admin/companies/${companyId}/modules`,
  },

  // Trazabilidad comercial y expedientes
  TRAZABILIDAD: {
    EXPEDIENTES: '/trazabilidad/expedientes',
    REPORTES: '/trazabilidad/reportes',
    PDF: (id: number) => `/trazabilidad/reportes/${id}/pdf`,
  },

  CONFIGURACION_ESTADOS: {
    BASE: '/v1/configuracion-estados',
    // Tipos de cultivo
    TIPOS_CULTIVO: '/v1/configuracion-estados/tipos-cultivo',
    TIPOS_CULTIVO_PLANTILLAS: '/v1/configuracion-estados/tipos-cultivo/plantillas',
    TIPO_CULTIVO: (id: number) => `/v1/configuracion-estados/tipos-cultivo/${id}`,
    // Estados
    ESTADOS: '/v1/configuracion-estados/estados',
    ESTADO: (id: number) => `/v1/configuracion-estados/estados/${id}`,
    ESTADOS_REORDENAR: '/v1/configuracion-estados/estados/reordenar',
    ESTADOS_COPIAR_PLANTILLA: '/v1/configuracion-estados/estados/copiar-plantilla',
    // Transiciones
    TRANSICIONES: '/v1/configuracion-estados/transiciones',
    TRANSICIONES_DESDE_ESTADO: (estadoOrigenId: number) => `/v1/configuracion-estados/transiciones/desde-estado/${estadoOrigenId}`,
    TRANSICIONES_VALIDAR: '/v1/configuracion-estados/transiciones/validar',
    TRANSICION: (id: number) => `/v1/configuracion-estados/transiciones/${id}`,
    // Tareas
    TAREAS: '/v1/configuracion-estados/tareas',
    TAREA: (id: number) => `/v1/configuracion-estados/tareas/${id}`,
    TAREAS_REORDENAR: '/v1/configuracion-estados/tareas/reordenar',
    TAREAS_VALIDAR: '/v1/configuracion-estados/tareas/validar',
    // Importación Excel
    IMPORTAR: '/v1/configuracion-estados/importar',
    PLANTILLA_EXCEL: '/v1/configuracion-estados/plantilla-excel',
    VALIDACION_COMPLETA: '/v1/configuracion-estados/validacion-completa',
  },

  ESTADOS_LOTES: {
    PROGRESO: (loteId: number) => `/estados-lotes/lote/${loteId}/progreso`,
    RECALCULAR_TODOS: '/estados-lotes/recalcular-todos',
  },
} as const;

// Exportar un helper para obtener endpoints de forma segura
export const getEndpoint = (endpoint: string | ((...args: any[]) => string), ...args: any[]): string => {
  if (typeof endpoint === 'function') {
    return endpoint(...args);
  }
  return endpoint;
};

