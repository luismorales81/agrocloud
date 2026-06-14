package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.porcinos.domain.HistorialEstadoMadre;
import com.agrocloud.porcinos.domain.Madre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoMadreRepository extends JpaRepository<HistorialEstadoMadre, Long> {

    List<HistorialEstadoMadre> findByMadre(Madre madre);

    List<HistorialEstadoMadre> findByMadreAndFechaFinIsNull(Madre madre);
}
