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
            case "ADMIN":
            case "ADMINISTRADOR":
                permissions.addAll(getAdminPermissions());
                break;
            case "PRODUCTOR":
                permissions.addAll(getProductorPermissions());
                break;
            case "INGENIERO_AGRONOMO":
                permissions.addAll(getIngenieroAgronomoPermissions());
                break;
            case "TECNICO":
                permissions.addAll(getTecnicoPermissions());
                break;
            case "OPERARIO":
                permissions.addAll(getOperarioPermissions());
                break;
            case "CONTADOR":
                permissions.addAll(getContadorPermissions());
                break;
            case "ASESOR":
                permissions.addAll(getAsesorPermissions());
                break;
            case "INVITADO":
                permissions.addAll(getInvitadoPermissions());
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
     * Permisos para PRODUCTOR - Acceso completo a operaciones
     */
    private Set<String> getProductorPermissions() {
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
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        
        return permissions;
    }

    /**
     * Permisos para INGENIERO_AGRONOMO - Acceso técnico completo
     */
    private Set<String> getIngenieroAgronomoPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Campos
        permissions.add("canViewFields");
        permissions.add("canCreateFields");
        permissions.add("canUpdateFields");
        
        // Lotes
        permissions.add("canViewLotes");
        permissions.add("canCreateLotes");
        permissions.add("canUpdateLotes");
        
        // Cultivos
        permissions.add("canViewCultivos");
        permissions.add("canCreateCultivos");
        permissions.add("canUpdateCultivos");
        
        // Cosechas
        permissions.add("canViewCosechas");
        permissions.add("canCreateCosechas");
        permissions.add("canUpdateCosechas");
        
        // Insumos
        permissions.add("canViewInsumos");
        permissions.add("canCreateInsumos");
        permissions.add("canUpdateInsumos");
        
        // Maquinaria
        permissions.add("canViewMaquinaria");
        permissions.add("canCreateMaquinaria");
        permissions.add("canUpdateMaquinaria");
        
        // Labores
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        
        return permissions;
    }

    /**
     * Permisos para TECNICO - Solo lectura y operaciones básicas
     */
    private Set<String> getTecnicoPermissions() {
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
        
        // Reportes
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
     * Permisos para CONTADOR - Solo finanzas y reportes
     */
    private Set<String> getContadorPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Finanzas
        permissions.add("canViewFinances");
        permissions.add("canCreateFinances");
        permissions.add("canUpdateFinances");
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        
        // Cosechas (para análisis de costos)
        permissions.add("canViewCosechas");
        
        return permissions;
    }

    /**
     * Permisos para ASESOR - Solo lectura para asesoramiento
     */
    private Set<String> getAsesorPermissions() {
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
        
        // Reportes
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        
        return permissions;
    }

    /**
     * Permisos para INVITADO - Acceso muy limitado
     */
    private Set<String> getInvitadoPermissions() {
        Set<String> permissions = new HashSet<>();
        
        // Solo lectura básica
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        
        return permissions;
    }
}
