package com.agrocloud.feedlot.model.entity;

import com.agrocloud.feedlot.model.enums.FeedlotMetodoCloseout;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedlot_configuracion_empresa")
@EntityListeners(AuditingEntityListener.class)
public class FeedlotConfiguracionEmpresa {

    @Id
    @Column(name = "empresa_id")
    private Long empresaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_closeout", nullable = false, length = 20)
    private FeedlotMetodoCloseout metodoCloseout = FeedlotMetodoCloseout.DEADS_IN;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public FeedlotMetodoCloseout getMetodoCloseout() {
        return metodoCloseout;
    }

    public void setMetodoCloseout(FeedlotMetodoCloseout metodoCloseout) {
        this.metodoCloseout = metodoCloseout;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
