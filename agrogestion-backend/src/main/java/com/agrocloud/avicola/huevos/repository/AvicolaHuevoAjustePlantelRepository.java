package com.agrocloud.avicola.huevos.repository;

import com.agrocloud.avicola.huevos.model.entity.AvicolaHuevoAjustePlantel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvicolaHuevoAjustePlantelRepository extends JpaRepository<AvicolaHuevoAjustePlantel, Long> {

    @Query("SELECT a FROM AvicolaHuevoAjustePlantel a WHERE a.lote.id = :loteId AND a.empresaId = :empresaId ORDER BY a.fechaHora DESC, a.id DESC")
    List<AvicolaHuevoAjustePlantel> listarPorLoteYEmpresa(@Param("loteId") Long loteId, @Param("empresaId") Long empresaId);
}
