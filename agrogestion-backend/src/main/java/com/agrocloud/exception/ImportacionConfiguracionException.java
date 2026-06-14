package com.agrocloud.exception;

import com.agrocloud.cultivos.application.ImportacionConfiguracionEstadosService;

/**
 * Excepción lanzada cuando la importación de configuración desde Excel tiene errores.
 * Permite devolver el resultado detallado al cliente y provocar rollback transaccional.
 */
public class ImportacionConfiguracionException extends RuntimeException {

    private final ImportacionConfiguracionEstadosService.ResultadoImportacion resultado;

    public ImportacionConfiguracionException(ImportacionConfiguracionEstadosService.ResultadoImportacion resultado) {
        super("Errores en la importación: " + (resultado.getErrores().isEmpty() ? "desconocido" : resultado.getErrores().get(0).getMensaje()));
        this.resultado = resultado;
    }

    public ImportacionConfiguracionEstadosService.ResultadoImportacion getResultado() {
        return resultado;
    }
}
