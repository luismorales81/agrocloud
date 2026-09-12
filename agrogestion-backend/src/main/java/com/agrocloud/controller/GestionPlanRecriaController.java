package com.agrocloud.controller;

import com.agrocloud.annotation.RequiresModule;
import com.agrocloud.core.application.UserService;
import com.agrocloud.core.security.ServicioSeguridadContexto;
import com.agrocloud.dto.porcinos.PlanRecriaEdicionRespuestaDto;
import com.agrocloud.dto.porcinos.PlanRecriaResumenDto;
import com.agrocloud.dto.wizard.WizardPlanRecriaPropuestaDto;
import com.agrocloud.porcinos.application.ServicioWizardPlanRecriaPersistencia;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CRUD de planes de recrÃ­a guardados.
 */
@RestController
@RequestMapping("/api/v1/porcinos/planes-recria")
@RequiresModule("pigs")
public class GestionPlanRecriaController {

    private final ServicioWizardPlanRecriaPersistencia servicioWizardPlanRecriaPersistencia;
    private final UserService userService;
    private final ServicioSeguridadContexto servicioSeguridadContexto;

    public GestionPlanRecriaController(ServicioWizardPlanRecriaPersistencia servicioWizardPlanRecriaPersistencia,
                                       UserService userService,
                                       ServicioSeguridadContexto servicioSeguridadContexto) {
        this.servicioWizardPlanRecriaPersistencia = servicioWizardPlanRecriaPersistencia;
        this.userService = userService;
        this.servicioSeguridadContexto = servicioSeguridadContexto;
    }

    @GetMapping
    public ResponseEntity<List<PlanRecriaResumenDto>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {
        validarUsuario(userDetails);
        return ResponseEntity.ok(servicioWizardPlanRecriaPersistencia.listarResumenes(servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<PlanRecriaEdicionRespuestaDto> obtener(
            @PathVariable long planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        validarUsuario(userDetails);
        return ResponseEntity.ok(servicioWizardPlanRecriaPersistencia.obtenerParaEdicion(planId, servicioSeguridadContexto.obtenerEmpresaIdActual()));
    }

    @PutMapping("/{planId}")
    @RequiresModule(value = "pigs", permission = "write")
    public ResponseEntity<Map<String, Object>> actualizar(
            @PathVariable long planId,
            @RequestBody WizardPlanRecriaPropuestaDto propuesta,
            @AuthenticationPrincipal UserDetails userDetails) {
        validarUsuario(userDetails);
        servicioWizardPlanRecriaPersistencia.actualizarPlan(planId, servicioSeguridadContexto.obtenerEmpresaIdActual(), propuesta);
        Map<String, Object> body = new HashMap<>();
        body.put("planRecriaId", planId);
        body.put("mensaje", "Plan actualizado");
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{planId}")
    @RequiresModule(value = "pigs", permission = "write")
    public ResponseEntity<Map<String, String>> desactivar(
            @PathVariable long planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        validarUsuario(userDetails);
        servicioWizardPlanRecriaPersistencia.desactivarPlan(planId, servicioSeguridadContexto.obtenerEmpresaIdActual());
        return ResponseEntity.ok(Map.of("mensaje", "Plan desactivado"));
    }

    private void validarUsuario(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }
        userService.findByEmailWithAllRelations(userDetails.getUsername());
    }
}
