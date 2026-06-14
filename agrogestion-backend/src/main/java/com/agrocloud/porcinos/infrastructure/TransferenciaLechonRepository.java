package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.Madre;
import com.agrocloud.porcinos.domain.Parto;
import com.agrocloud.porcinos.domain.TransferenciaLechon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransferenciaLechonRepository extends JpaRepository<TransferenciaLechon, Long> {

    List<TransferenciaLechon> findByEmpresa(Empresa empresa);

    List<TransferenciaLechon> findByPartoOrigen(Parto parto);

    List<TransferenciaLechon> findByPartoDestino(Parto parto);

    List<TransferenciaLechon> findByMadreOrigen(Madre madre);

    List<TransferenciaLechon> findByMadreDestino(Madre madre);

    @Query("SELECT t FROM TransferenciaLechon t " +
           "LEFT JOIN FETCH t.partoOrigen " +
           "LEFT JOIN FETCH t.madreOrigen " +
           "LEFT JOIN FETCH t.partoDestino " +
           "LEFT JOIN FETCH t.madreDestino " +
           "WHERE t.partoOrigen = :parto")
    List<TransferenciaLechon> findByPartoOrigenWithRelations(@Param("parto") Parto parto);

    @Query("SELECT t FROM TransferenciaLechon t " +
           "LEFT JOIN FETCH t.partoOrigen " +
           "LEFT JOIN FETCH t.madreOrigen " +
           "LEFT JOIN FETCH t.partoDestino " +
           "LEFT JOIN FETCH t.madreDestino " +
           "WHERE t.partoDestino = :parto")
    List<TransferenciaLechon> findByPartoDestinoWithRelations(@Param("parto") Parto parto);

    @Query("SELECT t FROM TransferenciaLechon t " +
           "LEFT JOIN FETCH t.partoOrigen " +
           "LEFT JOIN FETCH t.madreOrigen " +
           "LEFT JOIN FETCH t.partoDestino " +
           "LEFT JOIN FETCH t.madreDestino " +
           "WHERE t.empresa = :empresa")
    List<TransferenciaLechon> findByEmpresaWithRelations(@Param("empresa") Empresa empresa);
}
