package com.agrocloud.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio para manejar permisos por rol
 */
@Service
public class PermissionService {

    /**
     * Obtener permisos según el rol del usuario
     */
    public Set<String> getPermissionsByRole(String roleName) {
        Set<String> permissions = new HashSet<>();
        
        if (roleName == null) {
            return permissions;
        }
        
        switch (roleName.toUpperCase()) {
            case "SUPERADMIN":
            case "ADMINISTRADOR":
                permissions.addAll(getAdminPermissions());
                break;
            case "JEFE_CAMPO":
                permissions.addAll(getJefeCampoPermissions());
                break;
            case "JEFE_FINANCIERO":
                permissions.addAll(getJefeFinancieroPermissions());
                break;
            case "OPERARIO":
                permissions.addAll(getOperarioPermissions());
                break;
            case "CONSULTOR_EXTERNO":
                permissions.addAll(getConsultorExternoPermissions());
                break;
            // Mantener compatibilidad con roles antiguos
            case "PRODUCTOR":
            case "ASESOR":
            case "TECNICO":
                permissions.addAll(getJefeCampoPermissions());
                break;
            case "CONTADOR":
                permissions.addAll(getJefeFinancieroPermissions());
                break;
            case "LECTURA":
            case "INVITADO":
                permissions.addAll(getConsultorExternoPermissions());
                break;
            default:
                // Sin permisos para roles no reconocidos
                break;
        }
        
        return permissions;
    }

    /**
     * Permisos para ADMINISTRADOR - Acceso completo
     */
    private Set<String> getAdminPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Campos
        permissions.add("canViewFields");
        permissions.add("canCreateFields");
        permissions.add("canUpdateFields");
        permissions.add("canDeleteFields");
        
        // Lotes
        permissions.add("canViewLotes");
        permissions.add("canCreateLotes");
        permissions.add("canUpdateLotes");
        permissions.add("canDeleteLotes");
        
        // Cultivos
        permissions.add("canViewCultivos");
        permissions.add("canCreateCultivos");
        permissions.add("canUpdateCultivos");
        permissions.add("canDeleteCultivos");
        
        // Cosechas
        permissions.add("canViewCosechas");
        permissions.add("canCreateCosechas");
        permissions.add("canUpdateCosechas");
        permissions.add("canDeleteCosechas");
        
        // Insumos
        permissions.add("canViewInsumos");
        permissions.add("canCreateInsumos");
        permissions.add("canUpdateInsumos");
        permissions.add("canDeleteInsumos");
        
        // Maquinaria
        permissions.add("canViewMaquinaria");
        permissions.add("canCreateMaquinaria");
        permissions.add("canUpdateMaquinaria");
        permissions.add("canDeleteMaquinaria");
        
        // Labores
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        permissions.add("canDeleteLabores");
        
        // Finanzas
        permissions.add("canViewFinances");
        permissions.add("canCreateFinances");
        permissions.add("canUpdateFinances");
        permissions.add("canDeleteFinances");
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        
        // Usuarios y empresas
        permissions.add("canManageUsers");
        permissions.add("canManageCompanies");
        permissions.add("canViewAdminPanel");
        
        return permissions;
    }

    /**
     * Permisos para JEFE_CAMPO - Gestión completa de operaciones de campo
     * (Fusión de PRODUCTOR, ASESOR, TECNICO)
     */
    private Set<String> getJefeCampoPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Campos
        permissions.add("canViewFields");
        permissions.add("canCreateFields");
        permissions.add("canUpdateFields");
        permissions.add("canDeleteFields");
        
        // Lotes
        permissions.add("canViewLotes");
        permissions.add("canCreateLotes");
        permissions.add("canUpdateLotes");
        permissions.add("canDeleteLotes");
        
        // Cultivos
        permissions.add("canViewCultivos");
        permissions.add("canCreateCultivos");
        permissions.add("canUpdateCultivos");
        permissions.add("canDeleteCultivos");
        
        // Cosechas
        permissions.add("canViewCosechas");
        permissions.add("canCreateCosechas");
        permissions.add("canUpdateCosechas");
        permissions.add("canDeleteCosechas");
        
        // Insumos
        permissions.add("canViewInsumos");
        permissions.add("canCreateInsumos");
        permissions.add("canUpdateInsumos");
        permissions.add("canDeleteInsumos");
        
        // Maquinaria
        permissions.add("canViewMaquinaria");
        permissions.add("canCreateMaquinaria");
        permissions.add("canUpdateMaquinaria");
        permissions.add("canDeleteMaquinaria");
        
        // Labores
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        permissions.add("canDeleteLabores");
        
        // Reportes (sin acceso financiero)
        permissions.add("canViewReports");
        
        return permissions;
    }

    /**
     * Permisos para OPERARIO - Solo lectura y labores
     */
    private Set<String> getOperarioPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Campos
        permissions.add("canViewFields");
        
        // Lotes
        permissions.add("canViewLotes");
        
        // Cultivos
        permissions.add("canViewCultivos");
        
        // Cosechas
        permissions.add("canViewCosechas");
        
        // Insumos
        permissions.add("canViewInsumos");
        
        // Maquinaria
        permissions.add("canViewMaquinaria");
        
        // Labores
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        
        return permissions;
    }

    /**
     * Permisos para JEFE_FINANCIERO - Gestión financiera y contable
     * (Reemplaza a CONTADOR)
     */
    private Set<String> getJefeFinancieroPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Lectura de operaciones (para contexto financiero)
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canViewLabores");
        
        // Finanzas (gestión completa)
        permissions.add("canViewFinances");
        permissions.add("canCreateFinances");
        permissions.add("canUpdateFinances");
        permissions.add("canDeleteFinances");
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        permissions.add("canExportReports");
        
        return permissions;
    }

    /**
     * Permisos para CONSULTOR_EXTERNO - Solo lectura completa
     * (Reemplaza a ASESOR, LECTURA, INVITADO)
     */
    private Set<String> getConsultorExternoPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Solo lectura de todo
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canViewLabores");
        
        // Reportes (sin acceso a finanzas)
        permissions.add("canViewReports");
        permissions.add("canExportReports");
        
        return permissions;
    }
}
