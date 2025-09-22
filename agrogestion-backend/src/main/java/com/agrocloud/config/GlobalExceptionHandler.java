package com.agrocloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para la aplicación AgroCloud.
 * Evita que el sistema se caiga por excepciones no controladas.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Maneja excepciones de credenciales incorrectas (login fallido)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        logger.warn("🔐 [GlobalExceptionHandler] Credenciales incorrectas: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Credenciales inválidas");
        response.put("message", "Email o contraseña incorrectos. Por favor, verifica tus credenciales.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Maneja excepciones de usuario no encontrado
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameNotFound(UsernameNotFoundException ex, WebRequest request) {
        logger.warn("👤 [GlobalExceptionHandler] Usuario no encontrado: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Usuario no encontrado");
        response.put("message", "El email proporcionado no está registrado en el sistema.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Maneja excepciones de usuario inactivo
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, WebRequest request) {
        // Verificar si es un error de usuario inactivo
        if (ex.getMessage() != null && ex.getMessage().contains("Usuario inactivo")) {
            logger.warn("🚫 [GlobalExceptionHandler] Usuario inactivo: {}", ex.getMessage());
            
            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("status", HttpStatus.FORBIDDEN.value());
            response.put("error", "Usuario inactivo");
            response.put("message", "Tu cuenta está desactivada. Contacta al administrador para reactivarla.");
            response.put("path", request.getDescription(false).replace("uri=", ""));
            
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // Para otras RuntimeExceptions, manejar como error interno
        logger.error("❌ [GlobalExceptionHandler] Error interno: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Error interno del servidor");
        response.put("message", "Ha ocurrido un error inesperado. Inténtalo de nuevo.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Maneja excepciones de base de datos
     */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Map<String, Object>> handleSQLException(SQLException ex, WebRequest request) {
        logger.error("🗄️ [GlobalExceptionHandler] Error de base de datos: {} - SQLState: {} - ErrorCode: {}", 
                   ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        response.put("error", "Error de conexión a base de datos");
        response.put("message", "Problema temporal con la base de datos. Inténtalo de nuevo en unos momentos.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("sqlState", ex.getSQLState());
        response.put("errorCode", ex.getErrorCode());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
    
    /**
     * Maneja excepciones de timeout de conexión
     */
    @ExceptionHandler(java.sql.SQLTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleSQLTimeoutException(java.sql.SQLTimeoutException ex, WebRequest request) {
        logger.error("⏰ [GlobalExceptionHandler] Timeout de conexión a BD: {}", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.REQUEST_TIMEOUT.value());
        response.put("error", "Timeout de conexión");
        response.put("message", "La operación tardó demasiado tiempo. Inténtalo de nuevo.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(response);
    }
    
    /**
     * Maneja todas las demás excepciones no controladas
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, WebRequest request) {
        logger.error("💥 [GlobalExceptionHandler] Error no controlado: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Error interno del servidor");
        response.put("message", "Ha ocurrido un error inesperado. Inténtalo de nuevo.");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
