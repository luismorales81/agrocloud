package com.agrocloud.core.domain;

import com.agrocloud.core.domain.CompanyModule;
import com.agrocloud.core.domain.ModuleRolePermission;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Core que representa un módulo del sistema (feature flag).
 */
@Entity
@Table(name = "modules", uniqueConstraints = @UniqueConstraint(columnNames = {"code"}))
@EntityListeners(AuditingEntityListener.class)
public class Module {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del módulo es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "El código del módulo es obligatorio")
    @Size(min = 2, max = 50, message = "El código debe tener entre 2 y 50 caracteres")
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CompanyModule> companyModules = new ArrayList<>();

    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ModuleRolePermission> moduleRolePermissions = new ArrayList<>();

    public Module() {}

    public Module(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.active = true;
    }

    public Module(String name, String code) {
        this.name = name;
        this.code = code;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<CompanyModule> getCompanyModules() { return companyModules; }
    public void setCompanyModules(List<CompanyModule> companyModules) { this.companyModules = companyModules; }
    public List<ModuleRolePermission> getModuleRolePermissions() { return moduleRolePermissions; }
    public void setModuleRolePermissions(List<ModuleRolePermission> moduleRolePermissions) { this.moduleRolePermissions = moduleRolePermissions; }

    @Override
    public String toString() {
        return "Module{id=" + id + ", name='" + name + "', code='" + code + "', active=" + active + "}";
    }
}
