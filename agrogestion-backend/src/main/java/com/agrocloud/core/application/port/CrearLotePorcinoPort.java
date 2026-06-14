package com.agrocloud.core.application.port;

/**
 * Puerto que permite al módulo Porcinos solicitar la creación o obtención
 * de un lote/corral porcino sin depender del módulo Cultivos.
 * La implementación en Cultivos crea Field/Plot si es necesario.
 */
public interface CrearLotePorcinoPort {

    /**
     * Obtiene un lote existente por nombre sugerido para el usuario, o crea uno nuevo.
     *
     * @param empresaId     ID de la empresa
     * @param userId        ID del usuario propietario
     * @param nombreSugerido Nombre deseado del lote (p. ej. "Madre - dd/MM/yyyy")
     * @return ID del lote (existente o recién creado)
     * @throws RuntimeException si no se puede crear ni encontrar un lote
     */
    Long crearOObtenerLotePorcino(Long empresaId, Long userId, String nombreSugerido);
}
