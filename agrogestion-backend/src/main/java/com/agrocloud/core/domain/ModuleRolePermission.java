package com.agrocloud.core.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_permissions", uniqueConstraints = @UniqueConstraint(columnNames = {"role_id", "module_id"}))
@EntityListeners(AuditingEntityListener.class)
public class ModuleRolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    @JsonIgnoreProperties({"moduleRolePermissions", "userCompanyRoles", "rolePermissions"})
    private Role role;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    @JsonIgnoreProperties({"moduleRolePermissions", "companyModules"})
    private Module module;

    @Column(name = "read_permission", nullable = false)
    private Boolean read = false;

    @Column(name = "write_permission", nullable = false)
    private Boolean write = false;

    @Column(name = "manage_permission", nullable = false)
    private Boolean manage = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public ModuleRolePermission() {}

    public ModuleRolePermission(Role role, Module module) {
        this.role = role;
        this.module = module;
    }

    public ModuleRolePermission(Role role, Module module, Boolean read, Boolean write, Boolean manage) {
        this.role = role;
        this.module = module;
        this.read = read != null ? read : false;
        this.write = write != null ? write : false;
        this.manage = manage != null ? manage : false;
    }

    public boolean hasReadPermission() { return read != null && read; }
    public boolean hasWritePermission() { return write != null && write; }
    public boolean hasManagePermission() { return manage != null && manage; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }
    public Boolean getWrite() { return write; }
    public void setWrite(Boolean write) { this.write = write; }
    public Boolean getManage() { return manage; }
    public void setManage(Boolean manage) { this.manage = manage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
