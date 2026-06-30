package com.agrocloud.lecheria.repository;

import com.agrocloud.lecheria.model.entity.LecheriaParametroEspecie;
import com.agrocloud.lecheria.model.enums.LecheriaEspecie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LecheriaParametroEspecieRepository extends JpaRepository<LecheriaParametroEspecie, LecheriaEspecie> {
}
