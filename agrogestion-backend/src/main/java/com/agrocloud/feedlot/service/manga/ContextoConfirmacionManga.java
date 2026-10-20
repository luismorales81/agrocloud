package com.agrocloud.feedlot.service.manga;

import com.agrocloud.core.domain.User;
import com.agrocloud.feedlot.model.dto.FeedlotConfirmarSesionSolicitud;

public class ContextoConfirmacionManga {

    private final User usuario;
    private final FeedlotConfirmarSesionSolicitud solicitud;

    public ContextoConfirmacionManga(User usuario) {
        this(usuario, null);
    }

    public ContextoConfirmacionManga(User usuario, FeedlotConfirmarSesionSolicitud solicitud) {
        this.usuario = usuario;
        this.solicitud = solicitud;
    }

    public User getUsuario() {
        return usuario;
    }

    public FeedlotConfirmarSesionSolicitud getSolicitud() {
        return solicitud;
    }
}
