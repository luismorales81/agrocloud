package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.ChequeoGestacion;
import com.agrocloud.porcinos.domain.Gestacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChequeoGestacionRepository extends JpaRepository<ChequeoGestacion, Long> {

    @Query("SELECT c FROM ChequeoGestacion c LEFT JOIN FETCH c.gestacion LEFT JOIN FETCH c.empresa WHERE c.gestacion = :gestacion AND c.activo = true")
    List<ChequeoGestacion> findByGestacionAndActivoTrue(@Param("gestacion") Gestacion gestacion);

    List<ChequeoGestacion> findByEmpresaAndActivoTrue(Empresa empresa);
}
