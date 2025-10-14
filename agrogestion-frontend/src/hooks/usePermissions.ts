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

  // Debug logs (commented out for cleaner console)
  // console.log('🔍 [usePermissions] Debug info:', {
  //   userRoleName: user?.roleName,
  //   isGlobalAdmin,
  //   isGlobalSuperAdmin,
  //   esAdministrador: esAdministrador(),
  //   isAdmin,
    canAdmin,
    canWrite
  // });

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
    canDeleteFinances: !isGlobalSuperAdmin && (isAdmin || isJefeFinanciero)
  };
};
