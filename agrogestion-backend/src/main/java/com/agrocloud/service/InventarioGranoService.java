package com.agrocloud.service;

import com.agrocloud.dto.InventarioGranoDTO;
import com.agrocloud.dto.VentaGranoRequest;
import com.agrocloud.model.entity.*;
import com.agrocloud.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar el inventario de granos cosechados.
 */
@Service
@Transactional
public class InventarioGranoService {

    @Autowired
    private InventarioGranoRepository inventarioRepository;

    @Autowired
    private MovimientoInventarioGranoRepository movimientoRepository;

    @Autowired
    private IngresoRepository ingresoRepository;

    /**
     * Crea un registro de inventario automáticamente al cosechar.
     */
    public InventarioGrano crearInventarioDesdeCosecha(HistorialCosecha cosecha, User usuario) {
        System.out.println("[INVENTARIO_SERVICE] Creando inventario desde cosecha ID: " + cosecha.getId());
        
        // Verificar si ya existe inventario para esta cosecha
        if (inventarioRepository.findByCosechaId(cosecha.getId()).isPresent()) {
            System.out.println("[INVENTARIO_SERVICE] Ya existe inventario para esta cosecha");
            return inventarioRepository.findByCosechaId(cosecha.getId()).get();
        }

        InventarioGrano inventario = new InventarioGrano(
            cosecha,
            cosecha.getCultivo(),
            cosecha.getLote(),
            usuario,
            cosecha.getCantidadCosechada(),
            cosecha.getUnidadCosecha(),
            cosecha.getCostoTotalProduccion() != null ? cosecha.getCostoTotalProduccion() : BigDecimal.ZERO,
            cosecha.getFechaCosecha()
        );

        inventario.setVariedad(cosecha.getVariedadSemilla());
        inventario.setEstado("DISPONIBLE");

        inventario = inventarioRepository.save(inventario);
        System.out.println("[INVENTARIO_SERVICE] Inventario creado con ID: " + inventario.getId());

        // Registrar movimiento de entrada
        MovimientoInventarioGrano movimiento = new MovimientoInventarioGrano(
            inventario,
            usuario,
            MovimientoInventarioGrano.TipoMovimiento.ENTRADA_COSECHA,
            cosecha.getCantidadCosechada(),
            cosecha.getFechaCosecha()
        );
        movimiento.setObservaciones("Entrada automática desde cosecha ID " + cosecha.getId());
        movimientoRepository.save(movimiento);

        return inventario;
    }

    /**
     * Procesa una venta de grano desde el inventario.
     */
    public Long venderGrano(VentaGranoRequest request, User usuario) {
        System.out.println("[INVENTARIO_SERVICE] Procesando venta de grano");
        
        // Obtener inventario
        InventarioGrano inventario = inventarioRepository.findById(request.getInventarioId())
            .orElseThrow(() -> new RuntimeException("Inventario no encontrado"));

        // Validar cantidad disponible
        if (inventario.getCantidadDisponible().compareTo(request.getCantidadVender()) < 0) {
            throw new RuntimeException("Cantidad insuficiente en inventario. Disponible: " + 
                inventario.getCantidadDisponible() + " " + inventario.getUnidadMedida());
        }

        // Calcular monto total
        BigDecimal montoTotal = request.getCantidadVender().multiply(request.getPrecioVentaUnitario());

        // Reducir cantidad disponible
        BigDecimal nuevaCantidad = inventario.getCantidadDisponible().subtract(request.getCantidadVender());
        inventario.setCantidadDisponible(nuevaCantidad);
        
        // Actualizar estado si se agotó
        if (nuevaCantidad.compareTo(BigDecimal.ZERO) <= 0) {
            inventario.setEstado("AGOTADO");
        }
        
        inventarioRepository.save(inventario);

        // Registrar movimiento de salida
        MovimientoInventarioGrano movimiento = new MovimientoInventarioGrano(
            inventario,
            usuario,
            MovimientoInventarioGrano.TipoMovimiento.SALIDA_VENTA,
            request.getCantidadVender(),
            request.getFechaVenta()
        );
        movimiento.setPrecioUnitario(request.getPrecioVentaUnitario());
        movimiento.setMontoTotal(montoTotal);
        movimiento.setClienteDestino(request.getClienteComprador());
        movimiento.setObservaciones(request.getObservaciones());

        movimiento = movimientoRepository.save(movimiento);

        // Crear ingreso en finanzas
        Ingreso ingreso = new Ingreso();
        ingreso.setConcepto("Venta " + request.getCantidadVender() + " " + inventario.getUnidadMedida() + 
                           " de " + inventario.getCultivo().getNombre() + " - Lote " + inventario.getLote().getNombre());
        ingreso.setDescripcion("Venta desde inventario ID " + inventario.getId());
        ingreso.setTipoIngreso(Ingreso.TipoIngreso.VENTA_CULTIVO);
        ingreso.setMonto(montoTotal);
        ingreso.setFecha(request.getFechaVenta());
        ingreso.setClienteComprador(request.getClienteComprador());
        ingreso.setEstado(Ingreso.EstadoIngreso.PAGADO);
        ingreso.setLote(inventario.getLote());
        ingreso.setUsuario(usuario);
        
        Ingreso ingresoGuardado = ingresoRepository.save(ingreso);
        System.out.println("[INVENTARIO_SERVICE] Ingreso creado con ID: " + ingresoGuardado.getId());

        // Actualizar movimiento con referencia al ingreso
        movimiento.setReferenciaTipo("INGRESO");
        movimiento.setReferenciaId(ingresoGuardado.getId());
        movimientoRepository.save(movimiento);

        System.out.println("[INVENTARIO_SERVICE] Venta procesada exitosamente. Movimiento ID: " + movimiento.getId());
        return ingresoGuardado.getId();
    }

    /**
     * Obtiene todo el inventario de un usuario.
     */
    @Transactional(readOnly = true)
    public List<InventarioGranoDTO> obtenerInventarioPorUsuario(Long usuarioId) {
        List<InventarioGrano> inventarios = inventarioRepository.findByUsuarioIdOrderByFechaIngresoDesc(usuarioId);
        return inventarios.stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene inventario disponible por usuario.
     */
    @Transactional(readOnly = true)
    public List<InventarioGranoDTO> obtenerInventarioDisponible(Long usuarioId) {
        List<InventarioGrano> inventarios = inventarioRepository
            .findByUsuarioIdAndEstadoOrderByFechaIngresoDesc(usuarioId, "DISPONIBLE");
        return inventarios.stream()
            .filter(i -> i.getCantidadDisponible().compareTo(BigDecimal.ZERO) > 0)
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    /**
     * Convierte InventarioGrano a DTO.
     */
    private InventarioGranoDTO convertirADTO(InventarioGrano inventario) {
        InventarioGranoDTO dto = new InventarioGranoDTO();
        dto.setId(inventario.getId());
        dto.setCosechaId(inventario.getCosecha().getId());
        dto.setCultivoId(inventario.getCultivo().getId());
        dto.setCultivoNombre(inventario.getCultivo().getNombre());
        dto.setLoteId(inventario.getLote().getId());
        dto.setLoteNombre(inventario.getLote().getNombre());
        dto.setCantidadInicial(inventario.getCantidadInicial());
        dto.setCantidadDisponible(inventario.getCantidadDisponible());
        dto.setUnidadMedida(inventario.getUnidadMedida());
        dto.setCostoProduccionTotal(inventario.getCostoProduccionTotal());
        dto.setCostoUnitario(inventario.getCostoUnitario());
        dto.setFechaIngreso(inventario.getFechaIngreso());
        dto.setEstado(inventario.getEstado());
        dto.setVariedad(inventario.getVariedad());
        dto.setCalidad(inventario.getCalidad());
        dto.setUbicacionAlmacenamiento(inventario.getUbicacionAlmacenamiento());
        dto.setObservaciones(inventario.getObservaciones());

        // Calcular campos derivados
        dto.setValorTotal(inventario.getCantidadDisponible().multiply(inventario.getCostoUnitario()));
        dto.setCantidadVendida(inventario.getCantidadInicial().subtract(inventario.getCantidadDisponible()));
        
        if (inventario.getCantidadInicial().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal porcentaje = inventario.getCantidadDisponible()
                .divide(inventario.getCantidadInicial(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
            dto.setPorcentajeDisponible(porcentaje);
        } else {
            dto.setPorcentajeDisponible(BigDecimal.ZERO);
        }

        return dto;
    }
}

