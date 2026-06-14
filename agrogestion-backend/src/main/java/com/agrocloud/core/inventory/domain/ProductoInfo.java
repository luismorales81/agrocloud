package com.agrocloud.core.inventory.domain;

/**
 * Información mínima de un producto para trazabilidad (nombre, unidad).
 */
public record ProductoInfo(String nombre, String unidadMedida) {}
