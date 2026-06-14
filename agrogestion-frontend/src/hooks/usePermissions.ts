import { useEmpresa } from '../contexts/EmpresaContext';
import { useAuth } from '../contexts/AuthContext';

export interface Permissions {
  // Gestión de campos
  canViewFields: boolean;
  canCreateFields: boolean;
  canEditFields: boolean;
  canDeleteFields: boolean;
  
  // Gestión de lotes
  canViewLotes: boolean;
  canCreateLotes: boolean;
  canEditLotes: boolean;
  canDeleteLotes: boolean;
  
  // Gestión de cultivos
  canViewCultivos: boolean;
  canCreateCultivos: boolean;
  canEditCultivos: boolean;
  canDeleteCultivos: boolean;
  
  // Gestión de cosechas
  canViewCosechas: boolean;
  canCreateCosechas: boolean;
  canEditCosechas: boolean;
  canDeleteCosechas: boolean;
  
  // Gestión de insumos
  canViewInsumos: boolean;
  canCreateInsumos: boolean;
  canEditInsumos: boolean;
  canDeleteInsumos: boolean;
  
  // Gestión de maquinaria
  canViewMaquinaria: boolean;
  canCreateMaquinaria: boolean;
  canEditMaquinaria: boolean;
  canDeleteMaquinaria: boolean;
  
  // Gestión de labores
  canViewLabores: boolean;
  canCreateLabores: boolean;
  canEditLabores: boolean;
  canDeleteLabores: boolean;
  
  // Reportes
  canViewReports: boolean;
  canViewFinancialReports: boolean;
  canExportReports: boolean;
  
  // Administración
  canManageUsers: boolean;
  canManageCompanies: boolean;
  canViewAdminPanel: boolean;
  
  // Finanzas
  canViewFinances: boolean;
  canCreateFinances: boolean;
  canEditFinances: boolean;
  canDeleteFinances: boolean;
  
  // Inventario
  canViewInventario: boolean;
  
  // Porcinos - Permisos granulares
  canViewPorcinos: boolean;
  
  // Madres
  canViewMadres: boolean;
  canCreateMadres: boolean;
  canEditMadres: boolean;
  canDeleteMadres: boolean;
  
  // Servicios
  canViewServicios: boolean;
  canCreateServicios: boolean;
  canEditServicios: boolean;
  canDeleteServicios: boolean;
  
  // Gestación
  canViewGestacion: boolean;
  canCreateGestacion: boolean;
  canEditGestacion: boolean;
  canDeleteGestacion: boolean;
  
  // Partos
  canViewPartos: boolean;
  canCreatePartos: boolean;
  canEditPartos: boolean;
  canDeletePartos: boolean;
  
  // Destetes
  canViewDestetes: boolean;
  canCreateDestetes: boolean;
  canEditDestetes: boolean;
  canDeleteDestetes: boolean;
  
  // Recría
  canViewRecria: boolean;
  canCreateRecria: boolean;
  canEditRecria: boolean;
  canDeleteRecria: boolean;
  
  // Padrillos
  canViewPadrillos: boolean;
  canCreatePadrillos: boolean;
  canEditPadrillos: boolean;
  canDeletePadrillos: boolean;
  
  // Ventas
  canViewVentas: boolean;
  canCreateVentas: boolean;
  canEditVentas: boolean;
  canDeleteVentas: boolean;
  
  // Eventos Sanitarios
  canViewEventosSanitarios: boolean;
  canCreateEventosSanitarios: boolean;
  canEditEventosSanitarios: boolean;
  canDeleteEventosSanitarios: boolean;
  
  // Alimentación
  canViewAlimentacion: boolean;
  canCreateAlimentacion: boolean;
  canEditAlimentacion: boolean;
  canDeleteAlimentacion: boolean;
  
  // Inventario Porcinos
  canViewInventarioPorcinos: boolean;
  
  // Configuraciones Porcinos
  canManageConfiguracionesPorcinos: boolean;
  
  // Faena
  canViewFaena: boolean;
  canCreateFaena: boolean;
  canEditFaena: boolean;
  canDeleteFaena: boolean;
  
  // Reportes Porcinos
  canViewReportesPorcinos: boolean;
  canExportReportesPorcinos: boolean;
}

export const usePermissions = (): Permissions => {
  const { user } = useAuth();
  const { 
    esAdministrador, 
    esJefeCampo,
    esJefeFinanciero,
    esOperario,
    esConsultorExterno,
    // Legacy
    esAsesor, 
    esContador, 
    esTecnico, 
    esSoloLectura,
    esProductor,
    tienePermisoEscritura,
    tienePermisoAdministracion,
    tienePermisoFinanciero
  } = useEmpresa();

  // Verificar roles globales y de empresa
  const isGlobalAdmin = user?.roleName === 'ADMINISTRADOR';
  const isGlobalSuperAdmin = user?.roleName === 'SUPERADMIN';
  
  // Nuevos roles
  const isAdmin = esAdministrador() || isGlobalAdmin;
  const isJefeCampo = esJefeCampo();
  const isJefeFinanciero = esJefeFinanciero();
  const isOperario = esOperario();
  const isConsultorExterno = esConsultorExterno();
  
  const canWrite = tienePermisoEscritura() || isGlobalAdmin;
  const canAdmin = tienePermisoAdministracion() || isGlobalAdmin;
  const canFinance = tienePermisoFinanciero() || isGlobalAdmin;

  // Debug: descomentar para inspeccionar permisos
  // console.log('🔍 [usePermissions]', { userRoleName: user?.roleName, canAdmin, canWrite });

  return {
    // Gestión de campos - SUPERADMIN NO tiene acceso a funcionalidades operativas
    canViewFields: !isGlobalSuperAdmin, // SUPERADMIN no puede ver campos
    canCreateFields: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditFields: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteFields: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de lotes - SUPERADMIN NO tiene acceso
    canViewLotes: !isGlobalSuperAdmin,
    canCreateLotes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditLotes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteLotes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de cultivos - SUPERADMIN NO tiene acceso
    canViewCultivos: !isGlobalSuperAdmin,
    canCreateCultivos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditCultivos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteCultivos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de cosechas - SUPERADMIN NO tiene acceso
    canViewCosechas: !isGlobalSuperAdmin,
    canCreateCosechas: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditCosechas: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteCosechas: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de insumos - SUPERADMIN NO tiene acceso
    canViewInsumos: !isGlobalSuperAdmin,
    canCreateInsumos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditInsumos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteInsumos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de maquinaria - SUPERADMIN NO tiene acceso
    canViewMaquinaria: !isGlobalSuperAdmin,
    canCreateMaquinaria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditMaquinaria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteMaquinaria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestión de labores - SUPERADMIN NO tiene acceso
    canViewLabores: !isGlobalSuperAdmin,
    canCreateLabores: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditLabores: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteLabores: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Reportes - SUPERADMIN NO ve reportes operativos, solo los del admin global
    canViewReports: !isGlobalSuperAdmin, // SUPERADMIN no ve reportes operativos
    canViewFinancialReports: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero), // Solo roles con acceso financiero
    canExportReports: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isJefeFinanciero),
    
    // Administración - SUPERADMIN tiene acceso completo a administración
    canManageUsers: isGlobalAdmin || isGlobalSuperAdmin || isAdmin,
    canManageCompanies: isGlobalAdmin || isGlobalSuperAdmin || isAdmin,
    canViewAdminPanel: isGlobalAdmin || isGlobalSuperAdmin || canAdmin,
    
    // Finanzas - SUPERADMIN NO tiene acceso a gestión financiera operativa
    canViewFinances: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    canCreateFinances: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    canEditFinances: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    canDeleteFinances: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    
    // Inventario
    canViewInventario: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    
    // Porcinos - Permiso general (para acceso al módulo)
    canViewPorcinos: !isGlobalSuperAdmin, // Todos los usuarios excepto SUPERADMIN pueden ver porcinos
    
    // Madres - SUPERADMIN NO tiene acceso
    canViewMadres: !isGlobalSuperAdmin,
    canCreateMadres: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditMadres: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeleteMadres: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Servicios - SUPERADMIN NO tiene acceso
    canViewServicios: !isGlobalSuperAdmin,
    canCreateServicios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditServicios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteServicios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Gestación - SUPERADMIN NO tiene acceso
    canViewGestacion: !isGlobalSuperAdmin,
    canCreateGestacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditGestacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteGestacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Partos - SUPERADMIN NO tiene acceso
    canViewPartos: !isGlobalSuperAdmin,
    canCreatePartos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditPartos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeletePartos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Destetes - SUPERADMIN NO tiene acceso
    canViewDestetes: !isGlobalSuperAdmin,
    canCreateDestetes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditDestetes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteDestetes: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Recría - SUPERADMIN NO tiene acceso
    canViewRecria: !isGlobalSuperAdmin,
    canCreateRecria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditRecria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteRecria: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Padrillos - SUPERADMIN NO tiene acceso
    canViewPadrillos: !isGlobalSuperAdmin,
    canCreatePadrillos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canEditPadrillos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    canDeletePadrillos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Ventas - SUPERADMIN NO tiene acceso, solo Admin y Jefe Financiero
    canViewVentas: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canCreateVentas: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canEditVentas: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canDeleteVentas: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    
    // Eventos Sanitarios - SUPERADMIN NO tiene acceso
    canViewEventosSanitarios: !isGlobalSuperAdmin,
    canCreateEventosSanitarios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditEventosSanitarios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteEventosSanitarios: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Alimentación - SUPERADMIN NO tiene acceso
    canViewAlimentacion: !isGlobalSuperAdmin,
    canCreateAlimentacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canEditAlimentacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    canDeleteAlimentacion: !isGlobalSuperAdmin && (isAdmin || isJefeCampo),
    
    // Inventario Porcinos - SUPERADMIN NO tiene acceso
    canViewInventarioPorcinos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isOperario),
    
    // Configuraciones Porcinos - Solo Admin
    canManageConfiguracionesPorcinos: !isGlobalSuperAdmin && isAdmin,
    
    // Faena - SUPERADMIN NO tiene acceso
    canViewFaena: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canCreateFaena: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canEditFaena: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero || isJefeCampo),
    canDeleteFaena: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero),
    
    // Reportes Porcinos - SUPERADMIN NO tiene acceso
    canViewReportesPorcinos: !isGlobalSuperAdmin,
    canExportReportesPorcinos: !isGlobalSuperAdmin && (isAdmin || isJefeCampo || isJefeFinanciero)
  };
};
