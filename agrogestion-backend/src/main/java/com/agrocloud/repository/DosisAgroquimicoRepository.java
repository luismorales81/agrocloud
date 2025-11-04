package com.agrocloud.repository;

import com.agrocloud.model.entity.DosisAgroquimico;
import com.agrocloud.model.enums.TipoAplicacion;
import com.agrocloud.model.enums.FormaAplicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DosisAgroquimicoRepository extends JpaRepository<DosisAgroquimico, Long> {
    
    List<DosisAgroquimico> findByInsumoIdAndActivoTrue(Long insumoId);
    
    Optional<DosisAgroquimico> findByInsumoIdAndTipoAplicacionAndFormaAplicacionAndActivoTrue(
        Long insumoId,
        TipoAplicacion tipoAplicacion,
        FormaAplicacion formaAplicacion
    );
    
    List<DosisAgroquimico> findByTipoAplicacionAndFormaAplicacionAndActivoTrue(
        TipoAplicacion tipoAplicacion, 
        FormaAplicacion formaAplicacion
    );
}
