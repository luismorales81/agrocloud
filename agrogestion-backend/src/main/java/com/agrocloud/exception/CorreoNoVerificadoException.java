package com.agrocloud.exception;

/**
 * La contraseña es correcta, pero el correo del registro público todavía no fue confirmado.
 */
public class CorreoNoVerificadoException extends RuntimeException {

    public CorreoNoVerificadoException() {
        super("Confirmá tu correo antes de ingresar. Revisá la bandeja de entrada.");
    }
}
