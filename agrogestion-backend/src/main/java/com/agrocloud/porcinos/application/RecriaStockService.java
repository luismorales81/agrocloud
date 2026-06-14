package com.agrocloud.porcinos.application;

import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.MuerteRecriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Servicio de dominio: única fuente de verdad para el cálculo de animales disponibles en recría.
 * Fórmula oficial: disponibles = cantidadAnimales - Σ(muertes activas).
 * Las ventas/faenas ya están reflejadas en cantidadAnimales (no se restan).
 */
@Service
public class RecriaStockService {

    @Autowired
    private MuerteRecriaRepository muerteRecriaRepository;

    /**
     * Calcula los animales disponibles en la recría según la fórmula oficial.
     * Usado por VentaPorcinoService, MuerteRecriaService, MovimientoEtapaService, RecriaService,
     * ConsumoDiarioAutomaticoService.
     */
    public int animalesDisponibles(Recria recria) {
        int cantidad = recria.getCantidadAnimales() != null ? recria.getCantidadAnimales() : 0;
        int muertes = muerteRecriaRepository.findByRecriaAndActivoTrue(recria).stream()
            .mapToInt(m -> m.getCantidad() != null ? m.getCantidad() : 0)
            .sum();
        return Math.max(0, cantidad - muertes);
    }
}
