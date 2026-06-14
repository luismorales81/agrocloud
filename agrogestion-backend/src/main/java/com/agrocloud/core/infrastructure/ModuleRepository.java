package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {

    Optional<Module> findByCode(String code);

    List<Module> findByActiveTrue();

    List<Module> findByActive(Boolean active);

    boolean existsByCode(String code);

    @Query("SELECT m FROM Module m WHERE m.active = true ORDER BY m.name")
    List<Module> findAllActiveModules();
}
