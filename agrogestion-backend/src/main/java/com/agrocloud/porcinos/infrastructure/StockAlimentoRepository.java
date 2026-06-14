package com.agrocloud.porcinos.infrastructure;

import com.agrocloud.core.domain.Empresa;
import com.agrocloud.porcinos.domain.StockAlimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockAlimentoRepository extends JpaRepository<StockAlimento, Long> {

    List<StockAlimento> findByEmpresa(Empresa empresa);

    Optional<StockAlimento> findByInsumoIdAndEmpresa(Long insumoId, Empresa empresa);

    Optional<StockAlimento> findByCultivoIdAndEmpresa(Long cultivoId, Empresa empresa);

    Optional<StockAlimento> findByNombreAndEmpresa(String nombre, Empresa empresa);
}
