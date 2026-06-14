package com.agrocloud.core.application.port;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * DTO mínimo de lote/corral para uso por el módulo Porcinos.
 * Evita que Porcinos dependa de la entidad Plot (Cultivos).
 * La implementación de {@link LoteParaPorcinosQuery} (en el módulo Cultivos)
 * devuelve esta estructura.
 * Se serializa "areaHectareas" para compatibilidad con el frontend.
 */
public record LoteMinimoDTO(
    Long id,
    String nombre,
    @JsonProperty("areaHectareas") Double superficieHa
) implements Serializable {
}
