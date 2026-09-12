package com.agrocloud.trazabilidad.api;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.domain.Empresa;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.application.UserService;
import com.agrocloud.trazabilidad.application.ReporteTrazabilidadService;
import com.agrocloud.trazabilidad.application.TrazabilidadComercialOrquestador;
import com.agrocloud.trazabilidad.application.TrazabilidadExpedienteOrquestador;
import com.agrocloud.trazabilidad.dto.SolicitudGeneracionExpedienteTrazabilidad;
import com.agrocloud.trazabilidad.domain.TrazabilidadReporte;
import com.agrocloud.trazabilidad.dto.SolicitudGeneracionReporteTrazabilidad;
import com.agrocloud.trazabilidad.dto.TrazabilidadReporteListado;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/trazabilidad")
public class TrazabilidadComercialController {

    @Autowired
    private TrazabilidadComercialOrquestador orquestador;
    @Autowired
    private TrazabilidadExpedienteOrquestador expedienteOrquestador;
    @Autowired
    private ReporteTrazabilidadService reporteTrazabilidadService;
    @Autowired
    private UserService userService;
    @Autowired
    private EmpresaContextService empresaContextService;

    @GetMapping("/reportes")
    public ResponseEntity<List<TrazabilidadReporteListado>> listarReportes(
            @AuthenticationPrincipal UserDetails detalles) {
        User u = obtenerUsuario(detalles);
        Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(u.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario sin empresa asociada"));
        return ResponseEntity.ok(reporteTrazabilidadService.listarResumenesPorEmpresa(empresa));
    }

    @PostMapping("/expedientes")
    public ResponseEntity<Map<String, Object>> generarExpediente(
            @Valid @RequestBody SolicitudGeneracionExpedienteTrazabilidad solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User u = obtenerUsuario(detalles);
        Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(u.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario sin empresa asociada"));
        TrazabilidadReporte r = expedienteOrquestador.solicitarExpediente(solicitud, u, empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", r.getId(),
                "resultado", r.getResultado().name(),
                "certificacion", r.getCertificacionCodigo(),
                "entidadTipo", r.getEntidadTipo().name(),
                "entidadId", r.getEntidadId(),
                "hashSnapshot", r.getHashSnapshot(),
                "hashPdf", Optional.ofNullable(r.getHashPdf()).orElse(""),
                "versionMotor", r.getVersionMotor()
        ));
    }

    @PostMapping("/reportes")
    public ResponseEntity<Map<String, Object>> generar(
            @Valid @RequestBody SolicitudGeneracionReporteTrazabilidad solicitud,
            @AuthenticationPrincipal UserDetails detalles) {
        User u = obtenerUsuario(detalles);
        Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(u.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario sin empresa asociada"));
        TrazabilidadReporte r = orquestador.solicitarReporte(solicitud, u, empresa);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", r.getId(),
                "resultado", r.getResultado().name(),
                "certificacion", r.getCertificacionCodigo(),
                "hashSnapshot", r.getHashSnapshot(),
                "hashPdf", Optional.ofNullable(r.getHashPdf()).orElse(""),
                "versionMotor", r.getVersionMotor()
        ));
    }

    @GetMapping(value = "/reportes/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(
            @PathVariable long id,
            @AuthenticationPrincipal UserDetails detalles) {
        User u = obtenerUsuario(detalles);
        Empresa empresa = empresaContextService.obtenerEmpresaPrincipalDelUsuario(u.getId())
                .orElseThrow(() -> new IllegalStateException("Usuario sin empresa asociada"));
        byte[] pdf = reporteTrazabilidadService.obtenerPdf(id, empresa);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=trazabilidad-TRC-" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private User obtenerUsuario(UserDetails detalles) {
        if (detalles == null) {
            throw new AuthenticationCredentialsNotFoundException("Usuario no autenticado");
        }
        User usuario = userService.findByEmailWithAllRelations(detalles.getUsername());
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        return usuario;
    }
}
