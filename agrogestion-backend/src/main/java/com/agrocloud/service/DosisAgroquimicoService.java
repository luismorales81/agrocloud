package com.agrocloud.service;

import com.agrocloud.dto.CalcularCantidadRequest;
import com.agrocloud.dto.CalcularCantidadResponse;
import com.agrocloud.dto.DosisAgroquimicoRequest;
import com.agrocloud.dto.DosisAgroquimicoResponse;
import com.agrocloud.model.entity.Insumo;
import com.agrocloud.model.entity.DosisAgroquimico;
import com.agrocloud.model.entity.MovimientoInventario;
import com.agrocloud.model.entity.Plot;
import com.agrocloud.model.enums.TipoMovimiento;
import com.agrocloud.model.enums.UnidadDosis;
import com.agrocloud.repository.InsumoRepository;
import com.agrocloud.repository.DosisAgroquimicoRepository;
import com.agrocloud.repository.MovimientoInventarioRepository;
import com.agrocloud.repository.PlotRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DosisAgroquimicoService {

    @Autowired
    private DosisAgroquimicoRepository dosisRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private MovimientoInventarioRepository movimientoRepository;

    // CRUD para dosis
    @Transactional(readOnly = true)
    public List<DosisAgroquimicoResponse> getDosisByInsumo(Long insumoId) {
        System.out.println("[DOSIS_SERVICE] Obteniendo dosis para insumo ID: " + insumoId);
        List<DosisAgroquimico> dosis = dosisRepository.findByInsumoIdAndActivoTrue(insumoId);
        System.out.println("[DOSIS_SERVICE] Encontradas " + dosis.size() + " dosis activas");
        
        // Inicializar relación lazy antes de convertir a DTO
        dosis.forEach(d -> {
            if (d.getInsumo() != null) {
                d.getInsumo().getId(); // Forzar inicialización
            }
        });
        
        return dosis.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public DosisAgroquimicoResponse createDosis(Long insumoId, @Valid DosisAgroquimicoRequest request) {
        try {
            System.out.println("[DOSIS_SERVICE] Creando dosis para insumo: " + insumoId);
            Insumo insumo = insumoRepository.findById(insumoId)
                    .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
            System.out.println("[DOSIS_SERVICE] Insumo encontrado: " + insumo.getNombre());

            DosisAgroquimico dosis = new DosisAgroquimico();
            dosis.setTipoAplicacion(request.getTipoAplicacion());
            dosis.setFormaAplicacion(request.getFormaAplicacion());
            // La unidad ahora se deriva automáticamente del insumo relacionado
            // Ya no se guarda en la base de datos, se calcula desde insumo.unidadMedida
            dosis.setDosisRecomendadaPorHa(request.getDosisRecomendadaPorHa());
            dosis.setInsumo(insumo);
            dosis.setActivo(true);
            System.out.println("[DOSIS_SERVICE] Dosis creada, guardando...");

            DosisAgroquimico saved = dosisRepository.save(dosis);
            System.out.println("[DOSIS_SERVICE] Dosis guardada con ID: " + saved.getId());
            return convertToResponseDto(saved);
        } catch (Exception e) {
            System.err.println("[DOSIS_SERVICE] ERROR al crear dosis: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public DosisAgroquimicoResponse updateDosis(Long dosisId, @Valid DosisAgroquimicoRequest request) {
        DosisAgroquimico dosis = dosisRepository.findById(dosisId)
                .orElseThrow(() -> new RuntimeException("Dosis no encontrada"));

        dosis.setTipoAplicacion(request.getTipoAplicacion());
        dosis.setFormaAplicacion(request.getFormaAplicacion());
        // La unidad ahora se deriva automáticamente del insumo relacionado
        // Ya no se guarda en la base de datos, se calcula desde insumo.unidadMedida
        dosis.setDosisRecomendadaPorHa(request.getDosisRecomendadaPorHa());

        return convertToResponseDto(dosisRepository.save(dosis));
    }

    public void deleteDosis(Long dosisId) {
        DosisAgroquimico dosis = dosisRepository.findById(dosisId)
                .orElseThrow(() -> new RuntimeException("Dosis no encontrada"));
        dosis.setActivo(false);
        dosisRepository.save(dosis);
    }

    // Métodos para el controlador
    public DosisAgroquimicoResponse crear(DosisAgroquimicoRequest request) {
        return createDosis(request.getInsumoId(), request);
    }

    public List<DosisAgroquimicoResponse> obtenerPorInsumo(Long insumoId) {
        return getDosisByInsumo(insumoId);
    }

    public void eliminar(Long id) {
        deleteDosis(id);
    }

    public void eliminarPorInsumo(Long insumoId) {
        List<DosisAgroquimico> dosis = dosisRepository.findByInsumoIdAndActivoTrue(insumoId);
        
        for (DosisAgroquimico d : dosis) {
            d.setActivo(false);
            dosisRepository.save(d);
        }
    }

    // Cálculo de cantidad necesaria
    public CalcularCantidadResponse calcularCantidadNecesaria(@Valid CalcularCantidadRequest request) {
        // Obtener insumo (agroquímico)
        Insumo insumo = insumoRepository.findById(request.getInsumoId())
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        // Obtener lote
        Plot lote = plotRepository.findById(request.getLoteId())
                .orElseThrow(() -> new RuntimeException("Lote no encontrado"));

        // Buscar configuración de dosis
        DosisAgroquimico dosis = dosisRepository
                .findByInsumoIdAndTipoAplicacionAndFormaAplicacionAndActivoTrue(
                        request.getInsumoId(),
                        request.getTipoAplicacion(),
                        request.getFormaAplicacion()
                )
                .orElseThrow(() -> new RuntimeException("No se encontró configuración de dosis para el tipo y forma de aplicación especificados"));

        // Calcular cantidad necesaria
        Double hectareas = lote.getAreaHectareas().doubleValue();
        Double dosisRecomendada = dosis.getDosisRecomendadaPorHa();
        Double dosisUtilizada = request.getDosisPersonalizada() != null ? 
                request.getDosisPersonalizada() : dosisRecomendada;

        // Validar variación de dosis (±20%)
        if (request.getDosisPersonalizada() != null) {
            Double variacion = Math.abs((dosisUtilizada - dosisRecomendada) / dosisRecomendada) * 100;
            if (variacion > 20) {
                throw new RuntimeException("La dosis personalizada no puede variar más del 20% de la dosis recomendada. Variación actual: " + String.format("%.1f", variacion) + "%");
            }
        }

        Double cantidadNecesaria = hectareas * dosisUtilizada;

        // Verificar stock
        Boolean stockSuficiente = insumo.getStockActual().doubleValue() >= cantidadNecesaria;
        // La unidad se deriva desde el insumo
        UnidadDosis unidad = dosis.getUnidad();
        String unidadTexto = unidad != null ? unidad.name() : insumo.getUnidadMedida();
        String mensajeStock = stockSuficiente ? 
                "Stock suficiente" : 
                "Stock insuficiente. Disponible: " + insumo.getStockActual() + " " + unidadTexto + 
                ", Necesario: " + cantidadNecesaria + " " + unidadTexto;

        // Preparar respuesta
        CalcularCantidadResponse response = new CalcularCantidadResponse();
        response.setCantidadNecesaria(cantidadNecesaria);
        response.setUnidadDeMedida(dosis.getUnidad());
        response.setDosisRecomendadaPorHa(dosisRecomendada);
        response.setDosisUtilizada(dosisUtilizada);
        response.setStockSuficiente(stockSuficiente);
        response.setMensajeStock(mensajeStock);
        response.setDosisModificada(request.getDosisPersonalizada() != null);
        
        if (request.getDosisPersonalizada() != null) {
            Double variacion = ((dosisUtilizada - dosisRecomendada) / dosisRecomendada) * 100;
            response.setVariacionPorcentual(variacion);
            response.setMensajeDosis("Dosis modificada por el usuario. Variación: " + 
                    String.format("%.1f", variacion) + "%");
        }

        return response;
    }

    // Confirmar aplicación y deducir stock
    @Transactional
    public void confirmarAplicacion(Long insumoId, Double cantidadUtilizada, Long referenciaTareaId) {
        Insumo insumo = insumoRepository.findById(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));

        // Verificar stock suficiente
        if (insumo.getStockActual().doubleValue() < cantidadUtilizada) {
            throw new RuntimeException("Stock insuficiente para la aplicación");
        }

        // Deducir stock
        insumo.setStockActual(insumo.getStockActual().subtract(BigDecimal.valueOf(cantidadUtilizada)));
        insumoRepository.save(insumo);

        // Registrar movimiento de inventario
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setFechaMovimiento(LocalDateTime.now());
        movimiento.setTipoMovimiento(TipoMovimiento.SALIDA);
        movimiento.setCantidad(BigDecimal.valueOf(cantidadUtilizada));
        movimiento.setInsumo(insumo);
        // TODO: Si hay labor asociada, usar movimiento.setLabor(labor)
        // Por ahora, guardamos el motivo con la referencia
        movimiento.setMotivo("Aplicación de insumo - Referencia ID: " + referenciaTareaId);
        
        movimientoRepository.save(movimiento);
    }

    private DosisAgroquimicoResponse convertToResponseDto(DosisAgroquimico dosis) {
        DosisAgroquimicoResponse dto = new DosisAgroquimicoResponse();
        dto.setId(dosis.getId());
        dto.setTipoAplicacion(dosis.getTipoAplicacion());
        dto.setFormaAplicacion(dosis.getFormaAplicacion());
        dto.setUnidad(dosis.getUnidad());
        dto.setDosisRecomendadaPorHa(dosis.getDosisRecomendadaPorHa());
        dto.setInsumoId(dosis.getInsumo() != null ? dosis.getInsumo().getId() : null);
        dto.setActivo(dosis.getActivo());
        dto.setFechaCreacion(dosis.getFechaCreacion());
        dto.setFechaActualizacion(dosis.getFechaActualizacion());
        return dto;
    }
}
