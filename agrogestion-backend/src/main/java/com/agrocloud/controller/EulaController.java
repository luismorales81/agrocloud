package com.agrocloud.controller;

import com.agrocloud.dto.AceptarEulaRequest;
import com.agrocloud.dto.EulaEstadoResponse;
import com.agrocloud.exception.EulaNoAceptadoException;
import com.agrocloud.model.entity.User;
import com.agrocloud.repository.UserRepository;
import com.agrocloud.service.EulaService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para manejar el EULA (End User License Agreement)
 */
@RestController
@RequestMapping("/api/eula")
@CrossOrigin(origins = "*")
public class EulaController {
    
    private static final Logger logger = LoggerFactory.getLogger(EulaController.class);
    
    @Autowired
    private EulaService eulaService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Obtiene el estado del EULA para el usuario autenticado
     */
    @GetMapping("/estado")
    public ResponseEntity<EulaEstadoResponse> obtenerEstado(Authentication authentication) {
        try {
            String email = authentication.getName();
            Optional<User> usuarioOpt = userRepository.findByEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User usuario = usuarioOpt.get();
            EulaEstadoResponse response = new EulaEstadoResponse();
            response.setAceptado(usuario.getEulaAceptado());
            response.setFechaAceptacion(usuario.getEulaFechaAceptacion());
            response.setVersion(usuario.getEulaVersion());
            
            if (usuario.getEulaPdfPath() != null && !usuario.getEulaPdfPath().isEmpty()) {
                response.setPdfUrl("/api/eula/pdf/" + usuario.getId());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo estado EULA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene el estado del EULA por email (para verificación antes de login)
     */
    @GetMapping("/estado/{email}")
    public ResponseEntity<EulaEstadoResponse> obtenerEstadoPorEmail(@PathVariable String email) {
        try {
            Optional<User> usuarioOpt = userRepository.findByEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User usuario = usuarioOpt.get();
            EulaEstadoResponse response = new EulaEstadoResponse();
            response.setAceptado(usuario.getEulaAceptado());
            response.setFechaAceptacion(usuario.getEulaFechaAceptacion());
            response.setVersion(usuario.getEulaVersion());
            
            if (usuario.getEulaPdfPath() != null && !usuario.getEulaPdfPath().isEmpty()) {
                response.setPdfUrl("/api/eula/pdf/" + usuario.getId());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo estado EULA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Acepta el EULA para el usuario autenticado
     */
    @PostMapping("/aceptar")
    public ResponseEntity<?> aceptarEula(
            @RequestBody AceptarEulaRequest request,
            HttpServletRequest httpRequest,
            Authentication authentication) {
        try {
            if (request.getAceptado() == null || !request.getAceptado()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debe aceptar el EULA para continuar"));
            }
            
            String email = authentication.getName();
            Optional<User> usuarioOpt = userRepository.findByEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User usuario = usuarioOpt.get();
            
            // Obtener IP y User Agent
            String ipAddress = obtenerIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Si no se proporcionaron en el request, usar los de la petición HTTP
            if (request.getIpAddress() == null || request.getIpAddress().isEmpty()) {
                request.setIpAddress(ipAddress);
            }
            if (request.getUserAgent() == null || request.getUserAgent().isEmpty()) {
                request.setUserAgent(userAgent);
            }
            
            // Aceptar EULA
            eulaService.aceptarEula(usuario, request.getIpAddress(), request.getUserAgent());
            
            logger.info("EULA aceptado por usuario: {}", email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "EULA aceptado correctamente");
            response.put("aceptado", true);
            response.put("fechaAceptacion", usuario.getEulaFechaAceptacion());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error aceptando EULA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al aceptar el EULA: " + e.getMessage()));
        }
    }
    
    /**
     * Acepta el EULA por email (para uso antes del login)
     */
    @PostMapping("/aceptar/{email}")
    public ResponseEntity<?> aceptarEulaPorEmail(
            @PathVariable String email,
            @RequestBody AceptarEulaRequest request,
            HttpServletRequest httpRequest) {
        try {
            if (request.getAceptado() == null || !request.getAceptado()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Debe aceptar el EULA para continuar"));
            }
            
            Optional<User> usuarioOpt = userRepository.findByEmail(email);
            
            if (usuarioOpt.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Usuario no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            User usuario = usuarioOpt.get();
            
            // Obtener IP y User Agent
            String ipAddress = obtenerIpAddress(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            // Si no se proporcionaron en el request, usar los de la petición HTTP
            if (request.getIpAddress() == null || request.getIpAddress().isEmpty()) {
                request.setIpAddress(ipAddress);
            }
            if (request.getUserAgent() == null || request.getUserAgent().isEmpty()) {
                request.setUserAgent(userAgent);
            }
            
            // Aceptar EULA
            eulaService.aceptarEula(usuario, request.getIpAddress(), request.getUserAgent());
            
            logger.info("EULA aceptado por usuario: {}", email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "EULA aceptado correctamente");
            response.put("aceptado", true);
            response.put("fechaAceptacion", usuario.getEulaFechaAceptacion());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error aceptando EULA: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error al aceptar el EULA: " + e.getMessage()));
        }
    }
    
    /**
     * Descarga el PDF del EULA firmado por un usuario
     */
    @GetMapping("/pdf/{userId}")
    public ResponseEntity<Resource> descargarPdfEula(@PathVariable Long userId) {
        try {
            Optional<User> usuarioOpt = userRepository.findById(userId);
            
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User usuario = usuarioOpt.get();
            
            if (usuario.getEulaPdfPath() == null || usuario.getEulaPdfPath().isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = eulaService.obtenerPdfEula(usuario);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"EULA_" + usuario.getEmail() + ".pdf\"")
                .body(resource);
        } catch (Exception e) {
            logger.error("Error descargando PDF EULA: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Obtiene el texto del EULA
     */
    @GetMapping("/texto")
    public ResponseEntity<Map<String, String>> obtenerTextoEula() {
        Map<String, String> response = new HashMap<>();
        response.put("version", "1.0");
        response.put("texto", obtenerTextoCompletoEula());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Retorna el texto completo del EULA
     */
    private String obtenerTextoCompletoEula() {
        return "EULA – Acuerdo de Licencia de Usuario Final (Agrocloud)\n\n" +
               "Versión 1.0\n\n" +
               "Este Acuerdo de Licencia de Usuario Final (\"EULA\") establece los términos y condiciones que rigen el uso del software Agrocloud. " +
               "Al registrarse, acceder o utilizar Agrocloud, el Usuario acepta íntegramente este acuerdo. Si no está de acuerdo, no debe utilizar el sistema.\n\n" +
               "1. Objeto\n" +
               "Agrocloud es un software de gestión para el sector agropecuario. Este EULA regula el uso del sistema, incluyendo su acceso, funciones, actualizaciones y soporte.\n\n" +
               "2. Otorgamiento de Licencia\n" +
               "El titular de Agrocloud otorga al Usuario una licencia personal, limitada, no exclusiva, intransferible y revocable para utilizar el sistema conforme a este EULA. " +
               "El Usuario no adquiere derechos de propiedad sobre el software.\n\n" +
               "3. Propiedad Intelectual\n" +
               "Agrocloud, su código fuente, diseño, bases de datos, contenidos, marcas y documentación son propiedad exclusiva de su titular. " +
               "Los datos ingresados por el Usuario son de su exclusiva propiedad; Agrocloud solo los utiliza para la operación del sistema.\n\n" +
               "4. Restricciones\n" +
               "El Usuario no podrá, en ningún caso: Copiar, modificar, desarmar, descompilar o realizar ingeniería inversa del software. " +
               "Transferir, sublicenciar, revender o ceder el acceso a terceros. Intentar obtener acceso no autorizado a funciones, servidores o bases de datos. " +
               "Interferir con la operación del servicio o utilizarlo con fines ilegales o incompatibles con normativas vigentes (incluyendo SENASA u otros organismos).\n\n" +
               "5. Protección de Datos Personales\n" +
               "Agrocloud cumple con la Ley 25.326 (Argentina). El Usuario autoriza el tratamiento de sus datos únicamente para: " +
               "Operación normal del sistema, Soporte, mantenimiento y mejoras, Cumplimiento de obligaciones legales. " +
               "Los datos no se comercializan ni se comparten con terceros sin autorización del Usuario, salvo exigencia legal.\n\n" +
               "6. Disponibilidad del Servicio\n" +
               "Agrocloud procurará mantener el sistema operativo de forma continua, pero no garantiza la disponibilidad permanente debido a: " +
               "Actualizaciones, Mantenimiento, Fallas de red o servicios de terceros. " +
               "El titular no será responsable por interrupciones, pérdida de datos o daños derivados de causas fuera de su control.\n\n" +
               "7. Limitación de Responsabilidad\n" +
               "El Usuario acepta que: Agrocloud no es responsable por decisiones de producción o comerciales basadas en la información generada por el sistema. " +
               "El titular del software no garantiza resultados productivos, financieros ni técnicos. " +
               "La responsabilidad total por cualquier reclamo no superará el monto efectivamente abonado por el Usuario durante los últimos tres (3) meses previos al reclamo.\n\n" +
               "8. Planes, Pagos y Renovaciones\n" +
               "Los precios, planes y modalidades de contratación se detallan en la web o dentro del sistema. " +
               "Agrocloud podrá realizar cambios en las tarifas con notificación previa. " +
               "La falta de pago podrá derivar en suspensión o finalización del acceso.\n\n" +
               "9. Cancelación\n" +
               "El Usuario puede cancelar su cuenta en cualquier momento. Agrocloud podrá suspender o finalizar el acceso si detecta: " +
               "Incumplimiento del EULA, Uso indebido del sistema, Actividades fraudulentas o ilegales. " +
               "Los datos podrán conservarse por un plazo máximo de 90 días, salvo obligación legal en contrario.\n\n" +
               "10. Actualizaciones\n" +
               "Agrocloud podrá implementar mejoras, cambios, nuevas funciones o modificaciones sin previo aviso. " +
               "El Usuario acepta que dichas actualizaciones forman parte del servicio y quedan cubiertas por este EULA.\n\n" +
               "11. Jurisdicción y Ley Aplicable\n" +
               "Este EULA se rige por las leyes de la República Argentina. " +
               "Las partes se someten a los tribunales con competencia en el domicilio del titular del software, renunciando a cualquier otra jurisdicción.\n\n" +
               "12. Aceptación\n" +
               "Al presionar \"Aceptar\", \"Crear cuenta\", \"Ingresar\" o al utilizar el sistema, el Usuario declara haber leído, comprendido y aceptado la totalidad de este EULA.";
    }
    
    /**
     * Obtiene la dirección IP del cliente desde la petición HTTP
     */
    private String obtenerIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        
        // Si hay múltiples IPs (proxies), tomar la primera
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        
        return ipAddress;
    }
}

