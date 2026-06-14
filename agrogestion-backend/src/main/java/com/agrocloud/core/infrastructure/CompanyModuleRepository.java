package com.agrocloud.core.infrastructure;

import com.agrocloud.core.domain.CompanyModule;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyModuleRepository extends JpaRepository<CompanyModule, Long> {

    Optional<CompanyModule> findByCompanyAndModule(Empresa company, Module module);
    Optional<CompanyModule> findByCompanyIdAndModuleId(Long companyId, Long moduleId);
    List<CompanyModule> findByCompanyId(Long companyId);
    List<CompanyModule> findByCompanyIdAndEnabledTrue(Long companyId);
    List<CompanyModule> findByModuleId(Long moduleId);
    boolean existsByCompanyIdAndModuleId(Long companyId, Long moduleId);
    boolean existsByCompanyIdAndModuleIdAndEnabledTrue(Long companyId, Long moduleId);

    @Query("SELECT cm FROM CompanyModule cm WHERE cm.company.id = :companyId AND cm.enabled = true")
    List<CompanyModule> findEnabledModulesByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT cm.module FROM CompanyModule cm WHERE cm.company.id = :companyId AND cm.enabled = true")
    List<Module> findEnabledModulesForCompany(@Param("companyId") Long companyId);
}
