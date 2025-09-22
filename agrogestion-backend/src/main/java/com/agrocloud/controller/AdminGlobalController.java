package com.agrocloud.controller;

import com.agrocloud.model.entity.Empresa;
import com.agrocloud.model.entity.User;
import com.agrocloud.model.enums.EstadoEmpresa;
import com.agrocloud.service.AdminGlobalService;
import com.agrocloud.service.EmpresaService;
import com.agrocloud.service.UserService;
import com.agrocloud.service.FieldService;
import com.agrocloud.service.InsumoService;
import com.agrocloud.service.MaquinariaService;
import com.agrocloud.service.EgresoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador para la administración global del sistema multiempresa.
 * Este controlador es utilizado por el SuperAdmin para gestionar todas las empresas.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@RestController
@RequestMapping("/api/admin-global")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:3001", "http://127.0.0.1:5173"})
public class AdminGlobalController {

    @Autowired
    private AdminGlobalService adminGlobalService;

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UserService userService;

    @Autowired
    private FieldService fieldService;

    @Autowired
    private InsumoService insumoService;

    @Autowired
    private MaquinariaService maquinariaService;

    @Autowired
    private EgresoService egresoService;

    /**
     * Obtiene el dashboard del administrador global
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('SUPERADMIN') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerDashboardGlobal() {
        try {
            Map<String, Object> dashboard = adminGlobalService.obtenerDashboardGlobal();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene el dashboard del administrador global (endpoint temporal sin autenticación para pruebas)
     */
    @GetMapping("/dashboard-test")
    public ResponseEntity<Map<String, Object>> obtenerDashboardGlobalTest() {
        try {
            Map<String, Object> dashboard = adminGlobalService.obtenerDashboardGlobal();
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint simple para probar conectividad
     */
    @GetMapping("/test-simple")
    public ResponseEntity<Map<String, Object>> testSimple() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "AdminGlobalController funcionando");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard simplificado con datos reales de la base de datos
     */
    @GetMapping("/dashboard-simple")
    public ResponseEntity<Map<String, Object>> obtenerDashboardSimple() {
        Map<String, Object> dashboard = new HashMap<>();
        
        try {
            // Consultar datos reales de la base de datos usando métodos disponibles
            long totalEmpresas = empresaService.obtenerTodasLasEmpresas().size();
            long empresasActivas = empresaService.obtenerTodasLasEmpresas().stream().filter(e -> e.getActivo() != null && e.getActivo()).count();
            long totalUsuarios = userService.findAll().size();
            long usuariosActivos = userService.findAll().stream().filter(u -> u.getActivo() != null && u.getActivo()).count();
            
            // Consultar estadísticas de entidades principales
            long totalCampos = fieldService.getAllFields().size();
            long totalInsumos = insumoService.getAllInsumos().size();
            long totalMaquinaria = maquinariaService.getAllMaquinaria().size();
            
            // Balance financiero removido del dashboard
            
            // Construir respuesta con datos reales
            dashboard.put("totalEmpresas", totalEmpresas);
            dashboard.put("empresasActivas", empresasActivas);
            dashboard.put("empresasTrial", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("empresasSuspendidas", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("totalUsuarios", totalUsuarios);
            dashboard.put("usuariosActivos", usuariosActivos);
            dashboard.put("usuariosPendientes", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("usuariosSuspendidos", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("totalCampos", totalCampos);
            dashboard.put("totalLotes", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("totalCultivos", 0L); // Por ahora 0, se puede implementar después
            dashboard.put("totalInsumos", totalInsumos);
            dashboard.put("totalMaquinaria", totalMaquinaria);
            dashboard.put("totalLabores", 0L); // Por ahora 0, se puede implementar después
            // Balance financiero removido del dashboard
            dashboard.put("fechaConsulta", java.time.LocalDateTime.now().toString());
            dashboard.put("status", "success");
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    /**
     * Obtiene todas las empresas con paginación
     */
    @GetMapping("/empresas")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Page<Empresa>> obtenerTodasLasEmpresas(Pageable pageable) {
        try {
            Page<Empresa> empresas = adminGlobalService.obtenerTodasLasEmpresas(pageable);
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene empresas básicas con datos reales sin referencias circulares
     */
    @GetMapping("/empresas-basic")
    public ResponseEntity<List<Map<String, Object>>> obtenerEmpresasBasicas() {
        try {
            List<Empresa> empresas = empresaService.obtenerTodasLasEmpresas();
            List<Map<String, Object>> empresasBasicas = new java.util.ArrayList<>();
            
            for (Empresa empresa : empresas) {
                Map<String, Object> empresaBasica = new HashMap<>();
                empresaBasica.put("id", empresa.getId());
                empresaBasica.put("nombre", empresa.getNombre());
                empresaBasica.put("cuit", empresa.getCuit());
                empresaBasica.put("emailContacto", empresa.getEmailContacto());
                empresaBasica.put("telefonoContacto", empresa.getTelefonoContacto());
                empresaBasica.put("direccion", empresa.getDireccion());
                empresaBasica.put("estado", empresa.getEstado() != null ? empresa.getEstado().name() : "ACTIVO");
                empresaBasica.put("activo", empresa.getActivo());
                empresaBasica.put("fechaCreacion", empresa.getFechaCreacion());
                empresaBasica.put("fechaActualizacion", empresa.getFechaActualizacion());
                
                empresasBasicas.add(empresaBasica);
            }
            
            return ResponseEntity.ok(empresasBasicas);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene todos los usuarios del sistema
     */
    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<User>> obtenerTodosLosUsuarios() {
        try {
            List<User> usuarios = adminGlobalService.obtenerTodosLosUsuarios();
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene usuarios básicos con datos reales sin referencias circulares
     */
    @GetMapping("/usuarios-basic")
    public ResponseEntity<List<Map<String, Object>>> obtenerUsuariosBasicos() {
        try {
            List<User> usuarios = userService.findAll();
            List<Map<String, Object>> usuariosBasicos = new java.util.ArrayList<>();
            
            for (User usuario : usuarios) {
                Map<String, Object> usuarioBasico = new HashMap<>();
                usuarioBasico.put("id", usuario.getId());
                usuarioBasico.put("username", usuario.getUsername());
                usuarioBasico.put("firstName", usuario.getFirstName());
                usuarioBasico.put("lastName", usuario.getLastName());
                usuarioBasico.put("email", usuario.getEmail());
                usuarioBasico.put("activo", usuario.getActivo());
                usuarioBasico.put("estado", usuario.getEstado() != null ? usuario.getEstado().name() : "ACTIVO");
                usuarioBasico.put("emailVerified", usuario.getEmailVerified());
                usuarioBasico.put("fechaCreacion", usuario.getFechaCreacion());
                usuarioBasico.put("fechaActualizacion", usuario.getFechaActualizacion());
                
                // Agregar roles sin referencias circulares
                List<Map<String, String>> rolesBasicos = new java.util.ArrayList<>();
                if (usuario.getRoles() != null) {
                    for (var rol : usuario.getRoles()) {
                        Map<String, String> rolBasico = new HashMap<>();
                        rolBasico.put("name", rol.getNombre());
                        rolesBasicos.add(rolBasico);
                    }
                }
                usuarioBasico.put("roles", rolesBasicos);
                
                usuariosBasicos.add(usuarioBasico);
            }
            
            return ResponseEntity.ok(usuariosBasicos);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Busca empresas con filtros
     */
    @GetMapping("/empresas/buscar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Page<Empresa>> buscarEmpresas(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) EstadoEmpresa estado,
            @RequestParam(required = false) Boolean activo,
            Pageable pageable) {
        try {
            Page<Empresa> empresas = adminGlobalService.buscarEmpresas(nombre, estado, activo, pageable);
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene una empresa por ID
     */
    @GetMapping("/empresas/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> obtenerEmpresaPorId(@PathVariable Long id) {
        try {
            Optional<Empresa> empresa = adminGlobalService.obtenerEmpresaPorId(id);
            return empresa.map(ResponseEntity::ok)
                         .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Crea una nueva empresa desde el panel de administración global
     */
    @PostMapping("/empresas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> crearEmpresa(@RequestBody CrearEmpresaRequest request) {
        try {
            Empresa empresa = adminGlobalService.crearEmpresaDesdeAdmin(
                    request.getNombre(),
                    request.getCuit(),
                    request.getEmailContacto(),
                    request.getTelefonoContacto(),
                    request.getDireccion(),
                    request.getAdminUsername(),
                    request.getAdminEmail(),
                    request.getAdminPassword(),
                    request.getAdminFirstName(),
                    request.getAdminLastName()
            );
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza una empresa desde el panel de administración global
     */
    @PutMapping("/empresas/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> actualizarEmpresa(@PathVariable Long id, @RequestBody ActualizarEmpresaRequest request) {
        try {
            Empresa empresa = adminGlobalService.actualizarEmpresaDesdeAdmin(
                    id,
                    request.getNombre(),
                    request.getCuit(),
                    request.getEmailContacto(),
                    request.getTelefonoContacto(),
                    request.getDireccion(),
                    request.getEstado(),
                    request.getActivo()
            );
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Activa una empresa
     */
    @PostMapping("/empresas/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> activarEmpresa(@PathVariable Long id) {
        try {
            Empresa empresa = adminGlobalService.activarEmpresa(id);
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Suspende una empresa
     */
    @PostMapping("/empresas/{id}/suspender")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> suspenderEmpresa(@PathVariable Long id) {
        try {
            Empresa empresa = adminGlobalService.suspenderEmpresa(id);
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    /**
     * Extiende el trial de una empresa
     */
    @PostMapping("/empresas/{id}/extender-trial")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Empresa> extenderTrialEmpresa(@PathVariable Long id, @RequestBody ExtenderTrialRequest request) {
        try {
            Empresa empresa = adminGlobalService.extenderTrialEmpresa(id, request.getDiasAdicionales());
            return ResponseEntity.ok(empresa);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene usuarios de una empresa específica
     */
    @GetMapping("/empresas/{id}/usuarios")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<com.agrocloud.model.entity.UsuarioEmpresa>> obtenerUsuariosDeEmpresa(@PathVariable Long id) {
        try {
            List<com.agrocloud.model.entity.UsuarioEmpresa> usuarios = adminGlobalService.obtenerUsuariosDeEmpresa(id);
            return ResponseEntity.ok(usuarios);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene reporte de uso del sistema por empresa
     */
    @GetMapping("/empresas/{id}/reporte-uso")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Map<String, Object>> obtenerReporteUsoEmpresa(@PathVariable Long id) {
        try {
            Map<String, Object> reporte = adminGlobalService.obtenerReporteUsoEmpresa(id);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Resetea la contraseña de un usuario
     */
    @PostMapping("/usuarios/{id}/resetear-password")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> resetearPasswordUsuario(@PathVariable Long id, @RequestBody ResetearPasswordRequest request) {
        try {
            adminGlobalService.resetearPasswordUsuario(id, request.getNuevaPassword());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Desactiva un usuario
     */
    @PostMapping("/usuarios/{id}/desactivar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<User> desactivarUsuario(@PathVariable Long id) {
        try {
            User usuario = adminGlobalService.desactivarUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Activa un usuario
     */
    @PostMapping("/usuarios/{id}/activar")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<User> activarUsuario(@PathVariable Long id) {
        try {
            User usuario = adminGlobalService.activarUsuario(id);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene empresas con trial próximo a vencer
     */
    @GetMapping("/empresas/trial-proximo-vencer")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<Empresa>> obtenerEmpresasTrialProximoVencer(@RequestParam(defaultValue = "7") int dias) {
        try {
            List<Empresa> empresas = adminGlobalService.obtenerEmpresasTrialProximoVencer(dias);
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene empresas con trial vencido
     */
    @GetMapping("/empresas/trial-vencido")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<Empresa>> obtenerEmpresasTrialVencido() {
        try {
            List<Empresa> empresas = adminGlobalService.obtenerEmpresasTrialVencido();
            return ResponseEntity.ok(empresas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene reporte de uso por usuario específico
     */
    @GetMapping("/usuarios/{id}/reporte-uso")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerReporteUsoPorUsuario(@PathVariable Long id) {
        try {
            Map<String, Object> reporte = adminGlobalService.obtenerReporteUsoPorUsuario(id);
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene reporte de actividad del sistema
     */
    @GetMapping("/reporte-actividad")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerReporteActividadSistema() {
        try {
            Map<String, Object> reporte = adminGlobalService.obtenerReporteActividadSistema();
            return ResponseEntity.ok(reporte);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Obtiene estadísticas de uso del sistema
     */
    @GetMapping("/estadisticas-uso")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasUsoSistema() {
        try {
            Map<String, Object> estadisticas = adminGlobalService.obtenerEstadisticasUsoSistema();
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Clases DTO para las requests
    public static class CrearEmpresaRequest {
        private String nombre;
        private String cuit;
        private String emailContacto;
        private String telefonoContacto;
        private String direccion;
        private String adminUsername;
        private String adminEmail;
        private String adminPassword;
        private String adminFirstName;
        private String adminLastName;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCuit() { return cuit; }
        public void setCuit(String cuit) { this.cuit = cuit; }
        public String getEmailContacto() { return emailContacto; }
        public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
        public String getTelefonoContacto() { return telefonoContacto; }
        public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public String getAdminUsername() { return adminUsername; }
        public void setAdminUsername(String adminUsername) { this.adminUsername = adminUsername; }
        public String getAdminEmail() { return adminEmail; }
        public void setAdminEmail(String adminEmail) { this.adminEmail = adminEmail; }
        public String getAdminPassword() { return adminPassword; }
        public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }
        public String getAdminFirstName() { return adminFirstName; }
        public void setAdminFirstName(String adminFirstName) { this.adminFirstName = adminFirstName; }
        public String getAdminLastName() { return adminLastName; }
        public void setAdminLastName(String adminLastName) { this.adminLastName = adminLastName; }
    }

    public static class ActualizarEmpresaRequest {
        private String nombre;
        private String cuit;
        private String emailContacto;
        private String telefonoContacto;
        private String direccion;
        private EstadoEmpresa estado;
        private Boolean activo;

        // Getters and Setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getCuit() { return cuit; }
        public void setCuit(String cuit) { this.cuit = cuit; }
        public String getEmailContacto() { return emailContacto; }
        public void setEmailContacto(String emailContacto) { this.emailContacto = emailContacto; }
        public String getTelefonoContacto() { return telefonoContacto; }
        public void setTelefonoContacto(String telefonoContacto) { this.telefonoContacto = telefonoContacto; }
        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
        public EstadoEmpresa getEstado() { return estado; }
        public void setEstado(EstadoEmpresa estado) { this.estado = estado; }
        public Boolean getActivo() { return activo; }
        public void setActivo(Boolean activo) { this.activo = activo; }
    }


    public static class ExtenderTrialRequest {
        private int diasAdicionales;

        public int getDiasAdicionales() { return diasAdicionales; }
        public void setDiasAdicionales(int diasAdicionales) { this.diasAdicionales = diasAdicionales; }
    }

    public static class ResetearPasswordRequest {
        private String nuevaPassword;

        public String getNuevaPassword() { return nuevaPassword; }
        public void setNuevaPassword(String nuevaPassword) { this.nuevaPassword = nuevaPassword; }
    }
}
