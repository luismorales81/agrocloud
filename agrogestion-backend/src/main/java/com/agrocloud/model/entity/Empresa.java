package com.agrocloud.model.entity;

import com.agrocloud.model.enums.EstadoEmpresa;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad que representa una empresa en el sistema multiempresa.
 * 
 * @author AgroGestion Team
 * @version 2.0.0
 */
@Entity
@Table(name = "empresas")
@EntityListeners(AuditingEntityListener.class)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Size(max = 20, message = "El CUIT no puede exceder 20 caracteres")
    @Column(name = "cuit", unique = true, length = 20)
    private String cuit;

    @Email(message = "El email de contacto debe ser válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    @Column(name = "telefono_contacto", length = 20)
    private String telefonoContacto;

    @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;
    
    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;


    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoEmpresa estado = EstadoEmpresa.TRIAL;

    @Column(name = "fecha_inicio_trial")
    private LocalDate fechaInicioTrial;

    @Column(name = "fecha_fin_trial")
    private LocalDate fechaFinTrial;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por")
    @JsonIgnoreProperties({"empresa", "empresas", "creadoPor", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    private User creadoPor;

    @CreatedDate
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Relaciones
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<UsuarioEmpresa> usuariosEmpresas = new HashSet<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Field> campos = new HashSet<>();

    // Relaciones temporalmente comentadas hasta que se implementen en las entidades correspondientes
    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "user", "campo", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Plot> lotes = new HashSet<>();

    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "user", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Insumo> insumos = new HashSet<>();

    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "user", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Maquinaria> maquinarias = new HashSet<>();

    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "user", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Ingreso> ingresos = new HashSet<>();

    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "user", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Egreso> egresos = new HashSet<>();

    // @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // @JsonIgnoreProperties({"empresa", "usuario", "password", "resetPasswordToken", "resetPasswordTokenExpiry", "verificationToken"})
    // private Set<Labor> labores = new HashSet<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Cultivo> cultivos = new HashSet<>();

    // Constructors
    public Empresa() {}

    public Empresa(String nombre, String cuit, String emailContacto) {
        this.nombre = nombre;
        this.cuit = cuit;
        this.emailContacto = emailContacto;
        this.estado = EstadoEmpresa.TRIAL;
        this.activo = true;
        this.fechaInicioTrial = LocalDate.now();
        this.fechaFinTrial = LocalDate.now().plusDays(30);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getEmailContacto() {
        return emailContacto;
    }

    public void setEmailContacto(String emailContacto) {
        this.emailContacto = emailContacto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }


    public EstadoEmpresa getEstado() {
        return estado;
    }

    public void setEstado(EstadoEmpresa estado) {
        this.estado = estado;
    }

    public LocalDate getFechaInicioTrial() {
        return fechaInicioTrial;
    }

    public void setFechaInicioTrial(LocalDate fechaInicioTrial) {
        this.fechaInicioTrial = fechaInicioTrial;
    }

    public LocalDate getFechaFinTrial() {
        return fechaFinTrial;
    }

    public void setFechaFinTrial(LocalDate fechaFinTrial) {
        this.fechaFinTrial = fechaFinTrial;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public User getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(User creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Set<UsuarioEmpresa> getUsuariosEmpresas() {
        return usuariosEmpresas;
    }

    public void setUsuariosEmpresas(Set<UsuarioEmpresa> usuariosEmpresas) {
        this.usuariosEmpresas = usuariosEmpresas;
    }

    public Set<Field> getCampos() {
        return campos;
    }

    public void setCampos(Set<Field> campos) {
        this.campos = campos;
    }

    // Getters y setters temporalmente comentados hasta que se implementen las relaciones
    // public Set<Plot> getLotes() {
    //     return lotes;
    // }

    // public void setLotes(Set<Plot> lotes) {
    //     this.lotes = lotes;
    // }

    // public Set<Insumo> getInsumos() {
    //     return insumos;
    // }

    // public void setInsumos(Set<Insumo> insumos) {
    //     this.insumos = insumos;
    // }

    // public Set<Maquinaria> getMaquinarias() {
    //     return maquinarias;
    // }

    // public void setMaquinarias(Set<Maquinaria> maquinarias) {
    //     this.maquinarias = maquinarias;
    // }

    // public Set<Ingreso> getIngresos() {
    //     return ingresos;
    // }

    // public void setIngresos(Set<Ingreso> ingresos) {
    //     this.ingresos = ingresos;
    // }

    // public Set<Egreso> getEgresos() {
    //     return egresos;
    // }

    // public void setEgresos(Set<Egreso> egresos) {
    //     this.egresos = egresos;
    // }

    // public Set<Labor> getLabores() {
    //     return labores;
    // }

    // public void setLabores(Set<Labor> labores) {
    //     this.labores = labores;
    // }

    public Set<Cultivo> getCultivos() {
        return cultivos;
    }

    public void setCultivos(Set<Cultivo> cultivos) {
        this.cultivos = cultivos;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Métodos de utilidad
    public boolean isTrialActivo() {
        return estado == EstadoEmpresa.TRIAL && 
               fechaFinTrial != null && 
               fechaFinTrial.isAfter(LocalDate.now());
    }

    public boolean isActiva() {
        return activo && (estado == EstadoEmpresa.ACTIVO || isTrialActivo());
    }

    public int getDiasRestantesTrial() {
        if (estado != EstadoEmpresa.TRIAL || fechaFinTrial == null) {
            return 0;
        }
        return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), fechaFinTrial);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", cuit='" + cuit + '\'' +
                ", estado=" + estado +
                ", activo=" + activo +
                '}';
    }
}
