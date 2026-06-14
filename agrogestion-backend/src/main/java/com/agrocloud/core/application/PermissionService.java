package com.agrocloud.core.application;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Servicio Core para permisos por rol.
 */
@Service("permissionServiceCore")
public class PermissionService {

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
                break;
        }

        return permissions;
    }

    private Set<String> getAdminPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("canViewFields");
        permissions.add("canCreateFields");
        permissions.add("canUpdateFields");
        permissions.add("canDeleteFields");
        permissions.add("canViewLotes");
        permissions.add("canCreateLotes");
        permissions.add("canUpdateLotes");
        permissions.add("canDeleteLotes");
        permissions.add("canViewCultivos");
        permissions.add("canCreateCultivos");
        permissions.add("canUpdateCultivos");
        permissions.add("canDeleteCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canCreateCosechas");
        permissions.add("canUpdateCosechas");
        permissions.add("canDeleteCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canCreateInsumos");
        permissions.add("canUpdateInsumos");
        permissions.add("canDeleteInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canCreateMaquinaria");
        permissions.add("canUpdateMaquinaria");
        permissions.add("canDeleteMaquinaria");
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        permissions.add("canDeleteLabores");
        permissions.add("canViewFinances");
        permissions.add("canCreateFinances");
        permissions.add("canUpdateFinances");
        permissions.add("canDeleteFinances");
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        permissions.add("canManageUsers");
        permissions.add("canManageCompanies");
        permissions.add("canViewAdminPanel");
        return permissions;
    }

    private Set<String> getJefeCampoPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("canViewFields");
        permissions.add("canCreateFields");
        permissions.add("canUpdateFields");
        permissions.add("canDeleteFields");
        permissions.add("canViewLotes");
        permissions.add("canCreateLotes");
        permissions.add("canUpdateLotes");
        permissions.add("canDeleteLotes");
        permissions.add("canViewCultivos");
        permissions.add("canCreateCultivos");
        permissions.add("canUpdateCultivos");
        permissions.add("canDeleteCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canCreateCosechas");
        permissions.add("canUpdateCosechas");
        permissions.add("canDeleteCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canCreateInsumos");
        permissions.add("canUpdateInsumos");
        permissions.add("canDeleteInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canCreateMaquinaria");
        permissions.add("canUpdateMaquinaria");
        permissions.add("canDeleteMaquinaria");
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        permissions.add("canDeleteLabores");
        permissions.add("canViewReports");
        return permissions;
    }

    private Set<String> getOperarioPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canViewLabores");
        permissions.add("canCreateLabores");
        permissions.add("canUpdateLabores");
        return permissions;
    }

    private Set<String> getJefeFinancieroPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canViewLabores");
        permissions.add("canViewFinances");
        permissions.add("canCreateFinances");
        permissions.add("canUpdateFinances");
        permissions.add("canDeleteFinances");
        permissions.add("canViewFinancialReports");
        permissions.add("canViewReports");
        permissions.add("canExportReports");
        return permissions;
    }

    private Set<String> getConsultorExternoPermissions() {
        Set<String> permissions = new HashSet<>();
        permissions.add("canViewFields");
        permissions.add("canViewLotes");
        permissions.add("canViewCultivos");
        permissions.add("canViewCosechas");
        permissions.add("canViewInsumos");
        permissions.add("canViewMaquinaria");
        permissions.add("canViewLabores");
        permissions.add("canViewReports");
        permissions.add("canExportReports");
        return permissions;
    }
}
