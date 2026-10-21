package com.agrocloud.exception;

/**
 * Excepción lanzada cuando un usuario intenta acceder al sistema sin haber aceptado el EULA
 */
public class EulaNoAceptadoException extends RuntimeException {

    private final String ticket;

    public EulaNoAceptadoException(String message) {
        super(message);
        this.ticket = null;
    }

    public EulaNoAceptadoException(String message, String ticket) {
        super(message);
        this.ticket = ticket;
    }

    public EulaNoAceptadoException(String message, Throwable cause) {
        super(message, cause);
        this.ticket = null;
    }

    public String getTicket() {
        return ticket;
    }
}

