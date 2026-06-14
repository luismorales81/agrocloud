package com.agrocloud.cultivos.application;

import com.agrocloud.dto.CalcularCantidadRequest;
import com.agrocloud.dto.CalcularCantidadResponse;
import com.agrocloud.dto.DosisAgroquimicoRequest;
import com.agrocloud.dto.DosisAgroquimicoResponse;
import com.agrocloud.core.inventory.application.InventoryService;
import com.agrocloud.core.inventory.domain.InventoryOrigin;
import com.agrocloud.core.inventory.domain.InventoryResult;
import com.agrocloud.core.inventory.domain.Insumo;
import com.agrocloud.cultivos.domain.DosisAgroquimico;
import com.agrocloud.cultivos.domain.Plot;
import com.agrocloud.model.enums.UnidadDosis;
import com.agrocloud.core.inventory.infrastructure.InsumoRepository;
import com.agrocloud.cultivos.infrastructure.DosisAgroquimicoRepository;
import com.agrocloud.cultivos.infrastructure.PlotRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service("dosisAgroquimicoServiceCultivos")
@Transactional
public class DosisAgroquimicoService {

    @Autowired
    private DosisAgroquimicoRepository dosisRepository;

    @Autowired
    @Qualifier("insumoRepositoryInventario")
        private InsumoRepository insumoRepository;

    @Autowired
    private PlotRepository plotRepository;

    @Autowired
    private InventoryService inventoryService;

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
        Long empresaId = insumo.getEmpresa() != null ? insumo.getEmpresa().getId() : null;
        if (empresaId == null) {
            empresaId = inventoryService.obtenerEmpresaIdDeProducto(request.getInsumoId()).orElse(null);
        }
        BigDecimal cantidadReq = BigDecimal.valueOf(cantidadNecesaria);
        Boolean stockSuficiente = empresaId != null && inventoryService.hayStockSuficiente(empresaId, request.getInsumoId(), cantidadReq);
        BigDecimal stockDisponible = empresaId != null ? inventoryService.consultarStock(empresaId, request.getInsumoId()) : null;
        // La unidad se deriva desde el insumo
        UnidadDosis unidad = dosis.getUnidad();
        String unidadTexto = unidad != null ? unidad.name() : insumo.getUnidadMedida();
        String mensajeStock = stockSuficiente
                ? "Stock suficiente"
                : "Stock insuficiente. Disponible: " + (stockDisponible != null ? stockDisponible : "N/A") + " " + unidadTexto
                + ", Necesario: " + cantidadNecesaria + " " + unidadTexto;

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

    // Confirmar aplicación y deducir stock vía módulo de inventario
    @Transactional
    public void confirmarAplicacion(Long insumoId, Double cantidadUtilizada, Long referenciaTareaId) {
        Long empresaId = inventoryService.obtenerEmpresaIdDeProducto(insumoId)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado"));
        BigDecimal cantidad = BigDecimal.valueOf(cantidadUtilizada);
        InventoryResult result = inventoryService.consumir(
                empresaId, insumoId, cantidad, InventoryOrigin.CULTIVOS, referenciaTareaId, null);
        if (!result.exito()) {
            throw new RuntimeException(result.mensaje() != null ? result.mensaje() : "Error al descontar stock");
        }
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
