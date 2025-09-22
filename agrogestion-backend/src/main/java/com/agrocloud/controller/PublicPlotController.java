package com.agrocloud.controller;

import com.agrocloud.model.entity.Plot;
import com.agrocloud.dto.PlotDTO;
import com.agrocloud.repository.PlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/lotes")
@CrossOrigin(origins = "*")
public class PublicPlotController {

    @Autowired
    private PlotRepository plotRepository;

    /**
     * Obtiene todos los lotes (público para el balance)
     */
    @GetMapping
    public ResponseEntity<List<PlotDTO>> getAllPlots() {
        try {
            List<Plot> plots = plotRepository.findByActivoTrue();
            List<PlotDTO> plotDTOs = plots.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(plotDTOs);
        } catch (Exception e) {
            System.err.println("Error en getAllPlots: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Obtiene lotes por usuario ID (público para el balance)
     */
    @GetMapping("/usuario/{userId}")
    public ResponseEntity<List<PlotDTO>> getPlotsByUser(@PathVariable Long userId) {
        try {
            List<Plot> plots = plotRepository.findByUserIdAndActivoTrue(userId);
            List<PlotDTO> plotDTOs = plots.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(plotDTOs);
        } catch (Exception e) {
            System.err.println("Error en getPlotsByUser: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Endpoint de prueba
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PublicPlotController funcionando correctamente");
    }
    
    /**
     * Convierte una entidad Plot a PlotDTO
     */
    private PlotDTO convertToDTO(Plot plot) {
        return new PlotDTO(
                plot.getId(),
                plot.getNombre(),
                plot.getDescripcion(),
                plot.getAreaHectareas(),
                plot.getEstado().name(),
                plot.getTipoSuelo(),
                plot.getActivo(),
                plot.getFechaCreacion(),
                plot.getFechaActualizacion(),
                plot.getCampo() != null ? plot.getCampo().getId() : null,
                plot.getUser() != null ? plot.getUser().getId() : null
        );
    }
}
