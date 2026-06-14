package com.agrocloud.config;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.application.CompanyModuleService;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.application.ModulePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que valida el acceso a módulos antes de ejecutar endpoints.
 * 
 * Valida:
 * 1. Que el usuario pertenezca a la empresa
 * 2. Que la empresa tenga el módulo habilitado
 * 3. Que el usuario tenga el rol con permisos sobre el módulo
 * 4. Que el superadmin y el administrador de la empresa (en el contexto X-Company-Id) accedan sin matriz rol-módulo
 * <p>
 * El código de módulo ({@code @RequiresModule("…")}) debe existir en {@code modules.code} (p. ej. {@code AVICOLA_CARNE},
 * {@code AVICOLA_PONEDORAS}); no hay lista rígida en este interceptor: se consulta {@code company_modules} solo para el
 * código de esa petición (otro endpoint con otro código no interfiere).
 * <p>
 * {@code AVICOLA_CARNE} y {@code AVICOLA_PONEDORAS} coexisten sin conflicto: son filas distintas en {@code modules} y
 * relaciones distintas en {@code company_modules}; una empresa puede tener contratado uno, ambos o ninguno.
 * </p>
 * 
 * @author AgroGestion Team
 * @version 1.0.0
 */
@Component
public class ModuleAccessInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ModuleAccessInterceptor.class);

    @Autowired
    private CompanyModuleService companyModuleService;

    @Autowired
    private ModulePermissionService modulePermissionService;

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Solo procesar métodos anotados
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresModule annotation = handlerMethod.getMethodAnnotation(RequiresModule.class);
        
        // Si no tiene la anotación, permitir acceso
        if (annotation == null) {
            // Verificar también a nivel de clase
            annotation = handlerMethod.getBeanType().getAnnotation(RequiresModule.class);
            if (annotation == null) {
                return true;
            }
        }

        String moduleCode = annotation.value();
        String requiredPermission = annotation.permission();
        // allowSuperAdmin: funcionalidad futura - por ahora no se usa

        logger.debug("Validando acceso al módulo: {} con permiso: {}", moduleCode, requiredPermission);

        // Obtener usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Usuario no autenticado intentando acceder al módulo: {}", moduleCode);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Usuario no autenticado\"}");
            return false;
        }

        String email = authentication.getName();
        User user;
        try {
            user = userService.findByEmailWithAllRelationsCombined(email);
        } catch (RuntimeException ex) {
            logger.warn("Usuario no encontrado: {}", email);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"Usuario no encontrado\"}");
            return false;
        }

        // IMPORTANTE: Los superadmins siempre tienen acceso completo sin validación de módulos
        // El sistema de módulos es solo para controlar acceso de usuarios normales
        if (isSuperAdmin(user)) {
            logger.debug("Superadmin detectado, permitiendo acceso sin validación de módulo");
            return true;
        }

        // Obtener empresa del contexto (debe estar en el request o header)
        Long companyId = getCompanyIdFromRequest(request);
        if (companyId == null) {
            logger.warn("No se pudo determinar la empresa del usuario");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"BAD_REQUEST\",\"message\":\"No se pudo determinar la empresa\"}");
            return false;
        }

        // Verificar que el usuario pertenece a la empresa
        if (!user.perteneceAEmpresa(companyId)) {
            logger.warn("Usuario {} no pertenece a la empresa {}", user.getEmail(), companyId);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"Usuario no pertenece a la empresa\"}");
            return false;
        }

        // Verificar que la empresa tiene el módulo habilitado
        if (!companyModuleService.hasModuleEnabled(companyId, moduleCode)) {
            logger.warn("Empresa {} no tiene el módulo {} habilitado", companyId, moduleCode);
            response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED); // 402
            response.getWriter().write("{\"error\":\"MODULE_NOT_ENABLED\",\"message\":\"La empresa no tiene este módulo contratado\"}");
            return false;
        }

        // Administrador de la empresa en el contexto actual: acceso a módulos contratados sin depender de
        // la tabla intermedia rol ↔ módulo (user_roles + module_role_permissions), que a menudo no está poblada para ADMINISTRADOR.
        if (user.esAdministradorEmpresa(companyId)) {
            logger.debug("Usuario administrador de empresa {} — acceso {} al módulo {}", companyId, requiredPermission, moduleCode);
            return true;
        }

        // Verificar permisos del usuario sobre el módulo
        boolean hasPermission = false;
        switch (requiredPermission.toLowerCase()) {
            case "read":
                hasPermission = modulePermissionService.hasReadPermission(user.getId(), moduleCode);
                break;
            case "write":
                hasPermission = modulePermissionService.hasWritePermission(user.getId(), moduleCode);
                break;
            case "manage":
                hasPermission = modulePermissionService.hasManagePermission(user.getId(), moduleCode);
                break;
            default:
                hasPermission = modulePermissionService.hasReadPermission(user.getId(), moduleCode);
        }

        if (!hasPermission) {
            logger.warn("Usuario {} no tiene permiso {} sobre el módulo {}", user.getEmail(), requiredPermission, moduleCode);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"No tiene permisos suficientes sobre este módulo\"}");
            return false;
        }

        logger.debug("Acceso al módulo {} permitido para usuario {}", moduleCode, user.getEmail());
        return true;
    }

    /**
     * Verifica si el usuario es superadmin
     */
    private boolean isSuperAdmin(User user) {
        return user.isSuperAdmin();
    }

    /**
     * Obtiene el ID de la empresa desde el request
     * Puede venir en header, parámetro, o path variable
     */
    private Long getCompanyIdFromRequest(HttpServletRequest request) {
        // Intentar obtener del header
        String companyIdHeader = request.getHeader("X-Company-Id");
        if (companyIdHeader != null && !companyIdHeader.isEmpty()) {
            try {
                return Long.parseLong(companyIdHeader);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido de X-Company-Id: {}", companyIdHeader);
            }
        }

        // Intentar obtener del parámetro
        String companyIdParam = request.getParameter("companyId");
        if (companyIdParam != null && !companyIdParam.isEmpty()) {
            try {
                return Long.parseLong(companyIdParam);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido de companyId: {}", companyIdParam);
            }
        }

        // Intentar obtener del path (ej: /api/companies/{id}/...)
        // Aquí podrías implementar lógica para extraer el ID del path si es necesario
        // Por ahora, se requiere que el frontend envíe el header X-Company-Id

        return null;
    }
}

