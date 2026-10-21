package com.agrocloud.controller;

import com.agrocloud.dto.LoginRequest;
import com.agrocloud.dto.LoginResponse;
import com.agrocloud.dto.RegistroPublicoSolicitud;
import com.agrocloud.exception.CorreoNoVerificadoException;
import com.agrocloud.exception.EulaNoAceptadoException;
import com.agrocloud.exception.LimiteTasaExcedidoException;
import com.agrocloud.core.security.DireccionIpCliente;
import com.agrocloud.core.application.AuthService;
import com.agrocloud.core.security.ServicioLimiteTasaAuth;
import com.agrocloud.config.ConfiguracionCookieJwt;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    @Qualifier("authServiceCore")
    private AuthService authService;

    @Autowired
    private ServicioLimiteTasaAuth servicioLimiteTasaAuth;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSegura;

    @Value("${jwt.cookie.same-site:Strict}")
    private String cookieSameSite;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest solicitud) {
        try {
            servicioLimiteTasaAuth.verificarLogin(DireccionIpCliente.obtener(solicitud));
            LoginResponse response = authService.login(loginRequest);
            ResponseCookie cookie = ResponseCookie.from(ConfiguracionCookieJwt.NOMBRE_COOKIE, response.getToken())
                    .httpOnly(true)
                    .secure(cookieSegura)
                    .path("/")
                    .sameSite(cookieSameSite)
                    .maxAge(jwtExpirationMs / 1000)
                    .build();
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        } catch (EulaNoAceptadoException | CorreoNoVerificadoException e) {
            throw e;
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw e;
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("Usuario inactivo")) {
                throw e;
            }
            logger.error("Error en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Credenciales incorrectas");
        } catch (Exception e) {
            logger.error("Error inesperado en login: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al iniciar sesión");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroPublicoSolicitud solicitud, HttpServletRequest request) {
        try {
            servicioLimiteTasaAuth.verificarRegistro(DireccionIpCliente.obtener(request));
            authService.registrarUsuarioPublico(solicitud);
            return ResponseEntity.ok(java.util.Map.of(
                    "mensaje",
                    "Si los datos son válidos, enviamos un correo para confirmar la cuenta."));
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body("No se pudo completar el registro. Verificá los datos e intentá de nuevo.");
        }
    }

    @PostMapping("/verificar-email")
    public ResponseEntity<?> verificarEmail(@RequestBody com.agrocloud.dto.PasswordResetRequest request,
                                             HttpServletRequest solicitud) {
        try {
            servicioLimiteTasaAuth.verificarRecupero(DireccionIpCliente.obtener(solicitud), null);
            authService.verifyEmail(request.getToken());
            return ResponseEntity.ok(java.util.Map.of("mensaje", "Correo confirmado. Ya podés ingresar."));
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception e) {
            logger.warn("Verificación de correo rechazada");
            return ResponseEntity.badRequest().body("No se pudo confirmar el correo. El enlace puede haber vencido.");
        }
    }

    @PostMapping("/reenviar-verificacion")
    public ResponseEntity<?> reenviarVerificacion(@RequestBody com.agrocloud.dto.PasswordResetRequest request,
                                                   HttpServletRequest solicitud) {
        String mensaje = "Si el correo está pendiente de confirmación, enviamos un nuevo enlace.";
        try {
            servicioLimiteTasaAuth.verificarRecupero(DireccionIpCliente.obtener(solicitud), request.getEmail());
            authService.reenviarVerificacion(request.getEmail());
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error al reenviar verificación: {}", e.getMessage());
        }
        return ResponseEntity.ok(java.util.Map.of("mensaje", mensaje));
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestBody com.agrocloud.dto.PasswordResetRequest request,
                                                   HttpServletRequest solicitud) {
        try {
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El email es requerido");
            }
            servicioLimiteTasaAuth.verificarRecupero(DireccionIpCliente.obtener(solicitud), request.getEmail());
            authService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok().body(
                "Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en recupero de contraseña: {}", e.getMessage());
            return ResponseEntity.ok().body(
                "Si el email existe en nuestro sistema, recibirás un correo con las instrucciones para recuperar tu contraseña");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.agrocloud.dto.PasswordResetRequest request,
                                            HttpServletRequest solicitud) {
        try {
            servicioLimiteTasaAuth.verificarRecupero(DireccionIpCliente.obtener(solicitud), null);
            authService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().body("Contraseña restablecida exitosamente");
        } catch (LimiteTasaExcedidoException e) {
            return ResponseEntity.status(429).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("No se pudo restablecer la contraseña. El enlace puede haber expirado.");
        } catch (Exception e) {
            logger.error("Error inesperado en reset de contraseña: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Error al restablecer la contraseña");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(ConfiguracionCookieJwt.NOMBRE_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSegura)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(0)
                .build();
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

}
