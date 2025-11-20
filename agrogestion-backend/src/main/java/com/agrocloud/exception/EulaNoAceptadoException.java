package com.agrocloud.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder al sistema sin haber aceptado el EULA
 */
public class EulaNoAceptadoException extends RuntimeException {
    
    public EulaNoAceptadoException(String message) {
        super(message);
    }
    
    public EulaNoAceptadoException(String message, Throwable cause) {
        super(message, cause);
    }
}

