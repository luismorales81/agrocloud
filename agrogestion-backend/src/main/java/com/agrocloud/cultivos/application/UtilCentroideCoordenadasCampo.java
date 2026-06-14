package com.agrocloud.cultivos.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

/**
 * Calcula un punto representativo (centroide simple) a partir del JSON de coordenadas del campo.
 */
public final class UtilCentroideCoordenadasCampo {

    private UtilCentroideCoordenadasCampo() {}

    /**
     * @return par [latitud, longitud] o vacío si no hay datos válidos
     */
    public static Optional<double[]> calcularCentroide(String coordenadasJson, ObjectMapper mapper) {
        if (coordenadasJson == null || coordenadasJson.isBlank() || mapper == null) {
            return Optional.empty();
        }
        try {
            JsonNode raiz = mapper.readTree(coordenadasJson);
            if (!raiz.isArray() || raiz.isEmpty()) {
                return Optional.empty();
            }
            double sumaLat = 0;
            double sumaLon = 0;
            int n = 0;
            for (JsonNode punto : raiz) {
                if (punto.hasNonNull("lat") && punto.hasNonNull("lng")) {
                    sumaLat += punto.get("lat").asDouble();
                    sumaLon += punto.get("lng").asDouble();
                    n++;
                }
            }
            if (n == 0) {
                return Optional.empty();
            }
            return Optional.of(new double[]{sumaLat / n, sumaLon / n});
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
