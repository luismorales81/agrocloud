package com.agrocloud.controller;

import com.agrocloud.core.application.ServicioLibroEmpresa;
import com.agrocloud.core.domain.Egreso;
import com.agrocloud.core.domain.Ingreso;
import com.agrocloud.dto.SolicitudCompraInsumo;
import com.agrocloud.dto.SolicitudLineaLibro;
import com.agrocloud.exception.ResourceConflictException;
import com.agrocloud.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/economia")
public class EconomiaController {

    private final ServicioLibroEmpresa libro;

    public EconomiaController(ServicioLibroEmpresa libro) {
        this.libro = libro;
    }

    @GetMapping("/egresos")
    public List<Egreso> listarEgresos(
            @RequestParam(required = false) String modulo,
            @RequestParam(defaultValue = "false") boolean incluirCompartidas) {
        return libro.listarEgresos(modulo, incluirCompartidas);
    }

    @GetMapping("/ingresos")
    public List<Ingreso> listarIngresos(@RequestParam(required = false) String modulo) {
        return libro.listarIngresos(modulo);
    }

    @PostMapping("/compras")
    public ResponseEntity<Egreso> comprar(@RequestBody SolicitudCompraInsumo solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libro.registrarCompraInsumo(solicitud));
    }

    @PostMapping("/compras/{id}/anulacion")
    public Egreso anularCompra(@PathVariable Long id) {
        return libro.anularCompra(id);
    }

    @PostMapping("/egresos")
    public ResponseEntity<Egreso> crearEgreso(@RequestBody SolicitudLineaLibro solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libro.registrarEgresoManual(solicitud));
    }

    @PostMapping("/ingresos")
    public ResponseEntity<Ingreso> crearIngreso(@RequestBody SolicitudLineaLibro solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libro.registrarIngresoManual(solicitud));
    }

    @PostMapping("/ingresos/{id}/anulacion")
    public Ingreso anularIngreso(@PathVariable Long id) {
        return libro.anularIngreso(id);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumento(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> estado(IllegalStateException e) {
        return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Map<String, String>> conflicto(ResourceConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("mensaje", e.getMessage()));
    }
}
