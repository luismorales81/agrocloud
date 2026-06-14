package com.agrocloud.annotation;

import com.agrocloud.model.enums.ModuloSistema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca controladores o métodos cuyo acceso depende de un módulo contratado por la empresa.
 * El valor debe coincidir con {@code modules.code} y con {@link ModuloSistema#getCodigo()}.
 * <p>
 * Ejemplos: {@code @RequiresModule("AVICOLA_CARNE")}, {@code @RequiresModule("AVICOLA_PONEDORAS")}.
 * </p>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresModule {
    
    /** Código del módulo (p. ej. {@link ModuloSistema#AVICOLA_PONEDORAS}). */
    String value();

    /**
     * Permiso requerido: {@code read}, {@code write} o {@code manage}.
     * Por defecto solo lectura.
     */
    String permission() default "read";

    /**
     * Si es verdadero, el superadmin puede omitir la validación de módulo (reservado; el interceptor ya omite superadmin).
     */
    boolean allowSuperAdmin() default true;
}

