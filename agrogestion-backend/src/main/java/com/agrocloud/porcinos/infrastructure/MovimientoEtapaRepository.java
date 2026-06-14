package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.MovimientoEtapa;
import com.agrocloud.porcinos.domain.Recria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoEtapaRepository extends JpaRepository<MovimientoEtapa, Long> {

    List<MovimientoEtapa> findByEmpresa(Empresa empresa);

    List<MovimientoEtapa> findByRecriaOrigen(Recria recria);

    List<MovimientoEtapa> findByRecriaDestino(Recria recria);
}
