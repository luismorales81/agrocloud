package com.agrocloud.exception;

/**
 * El cliente superó el límite de intentos de un endpoint público.
 */
public class LimiteTasaExcedidoException extends RuntimeException {

    public LimiteTasaExcedidoException(String mensaje) {
        super(mensaje);
    }
}
