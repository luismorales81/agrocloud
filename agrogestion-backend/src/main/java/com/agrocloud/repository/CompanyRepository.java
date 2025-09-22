package com.agrocloud.repository;

import com.agrocloud.model.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Empresa, Long> {
    
    Optional<Empresa> findByCuit(String cuit);
    
    List<Empresa> findByActivoTrue();
    
    List<Empresa> findByActivoFalse();
    
    @Query("SELECT e FROM Empresa e WHERE e.nombre LIKE %:nombre%")
    List<Empresa> findByNombreContaining(@Param("nombre") String nombre);
    
    boolean existsByCuit(String cuit);
    
    boolean existsByNombre(String nombre);
}
