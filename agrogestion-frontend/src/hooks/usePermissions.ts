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
    esAsesor, 
    esOperario, 
    esContador, 
    esTecnico, 
    esSoloLectura,
    tienePermisoEscritura,
    tienePermisoAdministracion,
    tienePermisoFinanciero
  } = useEmpresa();

  // Verificar roles globales y de empresa
  const isGlobalAdmin = user?.roleName === 'ADMINISTRADOR';
  const isGlobalSuperAdmin = user?.roleName === 'SUPERADMIN';
  
  // SUPERADMIN tiene acceso limitado: solo dashboard, reportes y admin global
  const isAdmin = esAdministrador() || isGlobalAdmin;
  const isAsesor = esAsesor();
  const isOperario = esOperario();
  const isContador = esContador();
  const isTecnico = esTecnico();
  const isSoloLectura = esSoloLectura();
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
    canCreateFields: !isGlobalSuperAdmin && canWrite && !isSoloLectura,
    canEditFields: !isGlobalSuperAdmin && canWrite && !isSoloLectura,
    canDeleteFields: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de lotes - SUPERADMIN NO tiene acceso
    canViewLotes: !isGlobalSuperAdmin,
    canCreateLotes: !isGlobalSuperAdmin && canWrite && !isSoloLectura,
    canEditLotes: !isGlobalSuperAdmin && canWrite && !isSoloLectura,
    canDeleteLotes: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de cultivos - SUPERADMIN NO tiene acceso
    canViewCultivos: !isGlobalSuperAdmin,
    canCreateCultivos: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canEditCultivos: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canDeleteCultivos: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de cosechas - SUPERADMIN NO tiene acceso
    canViewCosechas: !isGlobalSuperAdmin,
    canCreateCosechas: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canEditCosechas: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canDeleteCosechas: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de insumos - SUPERADMIN NO tiene acceso
    canViewInsumos: !isGlobalSuperAdmin,
    canCreateInsumos: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canEditInsumos: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canDeleteInsumos: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de maquinaria - SUPERADMIN NO tiene acceso
    canViewMaquinaria: !isGlobalSuperAdmin,
    canCreateMaquinaria: !isGlobalSuperAdmin && canWrite && (isTecnico || isAsesor || isAdmin),
    canEditMaquinaria: !isGlobalSuperAdmin && canWrite && (isTecnico || isAsesor || isAdmin),
    canDeleteMaquinaria: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
    // Gestión de labores - SUPERADMIN NO tiene acceso
    canViewLabores: !isGlobalSuperAdmin,
    canCreateLabores: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canEditLabores: !isGlobalSuperAdmin && canWrite && (isOperario || isTecnico || isAsesor || isAdmin),
    canDeleteLabores: !isGlobalSuperAdmin && (isAdmin || isAsesor),
    
          // Reportes - SUPERADMIN NO ve reportes operativos, solo los del admin global
          canViewReports: !isGlobalSuperAdmin, // SUPERADMIN no ve reportes operativos
          canViewFinancialReports: !isGlobalSuperAdmin && canFinance, // SUPERADMIN no ve reportes financieros operativos
          canExportReports: !isGlobalSuperAdmin && (canWrite && !isSoloLectura),
    
    // Administración - SUPERADMIN tiene acceso completo a administración
    canManageUsers: isGlobalAdmin || isGlobalSuperAdmin || isAdmin,
    canManageCompanies: isGlobalAdmin || isGlobalSuperAdmin || isAdmin,
    canViewAdminPanel: isGlobalAdmin || isGlobalSuperAdmin || canAdmin,
    
    // Finanzas - SUPERADMIN NO tiene acceso a gestión financiera operativa
    canViewFinances: !isGlobalSuperAdmin && canFinance,
    canCreateFinances: !isGlobalSuperAdmin && canFinance && canWrite,
    canEditFinances: !isGlobalSuperAdmin && canFinance && canWrite,
    canDeleteFinances: !isGlobalSuperAdmin && (isAdmin || isAsesor)
  };
};
