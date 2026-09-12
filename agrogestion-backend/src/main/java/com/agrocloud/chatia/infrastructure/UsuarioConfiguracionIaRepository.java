package com.agrocloud.chatia.infrastructure;

import com.agrocloud.chatia.domain.UsuarioConfiguracionIa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioConfiguracionIaRepository extends JpaRepository<UsuarioConfiguracionIa, Long> {

    Optional<UsuarioConfiguracionIa> findByUsuarioId(Long usuarioId);
}
