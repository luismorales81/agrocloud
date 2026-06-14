package com.agrocloud.core.application.port;
import com.agrocloud.core.domain.Empresa;

import java.util.List;
import java.util.Optional;

/**
 * Puerto que permite al módulo Porcinos obtener listado de lotes/corrales
 * sin depender del módulo Cultivos ni de la entidad Plot.
 * La implementación debe estar en el módulo Cultivos (p. ej. PlotService
 * o un adapter) y registrarse como bean en la configuración de la API.
 */
public interface LoteParaPorcinosQuery {

    /**
     * Lista lotes disponibles para uso porcino (tipo_uso = PORCINO)
     * para la empresa del usuario en contexto.
     *
     * @param empresaId ID de la empresa
     * @return lista de lotes mínimos (id, nombre, superficie)
     */
    List<LoteMinimoDTO> listarLotesPorcinosPorEmpresa(Long empresaId);

    /**
     * Obtiene un lote por ID para validaciones y datos mínimos (nombre, superficie).
     *
     * @param loteId ID del lote
     * @return opcional con datos mínimos del lote si existe
     */
    Optional<LoteMinimoDTO> obtenerPorId(Long loteId);
}
