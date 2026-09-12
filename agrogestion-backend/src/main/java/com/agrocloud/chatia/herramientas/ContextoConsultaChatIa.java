package com.agrocloud.chatia.herramientas;

import com.agrocloud.core.domain.User;

import java.util.Set;

/**
 * Contexto de ejecución acotado a empresa, campaña y módulos contratados.
 */
public class ContextoConsultaChatIa {

    private final Long empresaId;
    private final Long campanaId;
    private final Long usuarioId;
    private final User usuario;
    private final Set<String> modulosActivos;
    private final String moduloActivo;

    public ContextoConsultaChatIa(
            Long empresaId,
            Long campanaId,
            Long usuarioId,
            User usuario,
            Set<String> modulosActivos,
            String moduloActivo) {
        this.empresaId = empresaId;
        this.campanaId = campanaId;
        this.usuarioId = usuarioId;
        this.usuario = usuario;
        this.modulosActivos = modulosActivos;
        this.moduloActivo = moduloActivo;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public Long getCampanaId() {
        return campanaId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public User getUsuario() {
        return usuario;
    }

    public Set<String> getModulosActivos() {
        return modulosActivos;
    }

    public String getModuloActivo() {
        return moduloActivo;
    }
}
