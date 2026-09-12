package com.agrocloud.chatia.controller;

import com.agrocloud.chatia.dto.*;
import com.agrocloud.chatia.service.ServicioConfiguracionIaUsuario;
import com.agrocloud.chatia.service.ServicioOrquestadorChatIa;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat-ia")
public class ChatIaController {

    private final ServicioConfiguracionIaUsuario servicioConfiguracion;
    private final ServicioOrquestadorChatIa servicioOrquestador;
    private final ServicioSeguridadContexto servicioSeguridadContexto;

    public ChatIaController(
            ServicioConfiguracionIaUsuario servicioConfiguracion,
            ServicioOrquestadorChatIa servicioOrquestador,
            ServicioSeguridadContexto servicioSeguridadContexto) {
        this.servicioConfiguracion = servicioConfiguracion;
        this.servicioOrquestador = servicioOrquestador;
        this.servicioSeguridadContexto = servicioSeguridadContexto;
    }

    @GetMapping("/estado")
    public ResponseEntity<ChatIaEstadoRespuesta> obtenerEstado() {
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        return ResponseEntity.ok(servicioConfiguracion.obtenerEstado(usuarioId));
    }

    @GetMapping("/configuracion")
    public ResponseEntity<ChatIaConfiguracionRespuesta> obtenerConfiguracion() {
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        return ResponseEntity.ok(servicioConfiguracion.obtenerConfiguracion(usuarioId));
    }

    @PutMapping("/configuracion")
    public ResponseEntity<ChatIaConfiguracionRespuesta> guardarConfiguracion(
            @Valid @RequestBody ChatIaConfiguracionSolicitud solicitud) {
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        return ResponseEntity.ok(servicioConfiguracion.guardarConfiguracion(usuarioId, solicitud));
    }

    @DeleteMapping("/configuracion")
    public ResponseEntity<Void> eliminarConfiguracion() {
        Long usuarioId = servicioSeguridadContexto.obtenerUsuarioIdActual();
        servicioConfiguracion.eliminarConfiguracion(usuarioId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/mensaje")
    public ResponseEntity<ChatIaMensajeRespuesta> enviarMensaje(
            @Valid @RequestBody ChatIaMensajeSolicitud solicitud) {
        return ResponseEntity.ok(servicioOrquestador.procesarMensaje(solicitud));
    }
}
