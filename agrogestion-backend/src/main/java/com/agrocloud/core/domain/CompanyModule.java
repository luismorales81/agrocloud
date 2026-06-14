package com.agrocloud.core.domain;
import com.agrocloud.core.domain.Empresa;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "company_modules", uniqueConstraints = @UniqueConstraint(columnNames = {"company_id", "module_id"}))
@EntityListeners(AuditingEntityListener.class)
public class CompanyModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @JsonIgnoreProperties({"companyModules", "usuariosEmpresas", "campos", "cultivos"})
    private Empresa company;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnoreProperties({"companyModules", "moduleRolePermissions"})
    private Module module;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CompanyModule() {}

    public CompanyModule(Empresa company, Module module) {
        this.company = company;
        this.module = module;
        this.enabled = true;
    }

    public CompanyModule(Empresa company, Module module, Boolean enabled) {
        this.company = company;
        this.module = module;
        this.enabled = enabled != null ? enabled : true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Empresa getCompany() { return company; }
    public void setCompany(Empresa company) { this.company = company; }
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
