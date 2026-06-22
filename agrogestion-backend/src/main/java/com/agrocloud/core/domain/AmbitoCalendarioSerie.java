package com.agrocloud.core.domain;

/**
 * Ámbito de una serie de tareas recurrentes: dónde se muestra y gestiona en la UI.
 */
public enum AmbitoCalendarioSerie {
    /** Calendario principal (cultivos / vista general). */
    GENERAL,
    /** Calendario del módulo avícola producción de huevos (independiente en datos y listado). */
    AVICOLA_HUEVOS,
    /** Calendario del módulo feedlot (engorde a corral). */
    FEEDLOT
}
