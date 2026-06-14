package com.agrocloud.porcinos.application;
import com.agrocloud.core.domain.User;
import com.agrocloud.core.domain.Empresa;

import com.agrocloud.core.application.EmpresaContextService;
import com.agrocloud.core.application.port.LoteParaPorcinosQuery;

import com.agrocloud.porcinos.domain.MovimientoEtapa;
import com.agrocloud.porcinos.domain.Recria;
import com.agrocloud.porcinos.infrastructure.MovimientoEtapaRepository;
import com.agrocloud.porcinos.infrastructure.RecriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MovimientoEtapaService {

    @Autowired
    private MovimientoEtapaRepository movimientoRepository;

    @Autowired
    private RecriaRepository recriaRepository;

    @Autowired
    private LoteParaPorcinosQuery loteParaPorcinosQuery;

    @Autowired
    private EmpresaContextService empresaContextService;

    @Autowired
    private RecriaStockService recriaStockService;

    @Transactional
    public MovimientoEtapa moverEntreEtapas(Long recriaOrigenId, MovimientoEtapa.EtapaRecria etapaDestino,
                                            Long loteDestinoId, Integer cantidadAnimales, 
                                            BigDecimal pesoPromedio, String observaciones, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            throw new IllegalArgumentException("Usuario no tiene empresa activa");
        }

        Optional<Recria> recriaOrigen = recriaRepository.findByIdAndActivoTrue(recriaOrigenId);
        if (recriaOrigen.isEmpty() || !recriaOrigen.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            throw new IllegalArgumentException("Recría origen no encontrada");
        }

        Recria recria = recriaOrigen.get();

        // VALIDACIÓN: No se puede pasar a engorde si no se cerró recría
        if (etapaDestino == MovimientoEtapa.EtapaRecria.TERMINACION && recria.getFechaSalida() == null) {
            throw new IllegalArgumentException("Debe cerrar la recría antes de pasar a engorde");
        }

        // VALIDACIÓN: No se puede vender/faenar si no tiene peso registrado
        if (pesoPromedio == null || pesoPromedio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debe registrar peso promedio para el movimiento");
        }

        if (cantidadAnimales == null || cantidadAnimales <= 0) {
            throw new IllegalArgumentException("La cantidad de animales debe ser mayor a cero");
        }

        // Fórmula oficial: disponibles = cantidadAnimales - Σ(muertes)
        int animalesDisponibles = recriaStockService.animalesDisponibles(recria);

        if (cantidadAnimales > animalesDisponibles) {
            throw new IllegalArgumentException(
                String.format("La cantidad a mover (%d) excede los animales disponibles (%d)", 
                    cantidadAnimales, animalesDisponibles));
        }

        MovimientoEtapa movimiento = new MovimientoEtapa();
        movimiento.setRecriaOrigen(recria);
        
        // Convertir etapa de Recria a MovimientoEtapa
        Recria.EtapaRecria etapaOrigenRecria = recria.getEtapa();
        MovimientoEtapa.EtapaRecria etapaOrigen = etapaOrigenRecria != null 
            ? MovimientoEtapa.EtapaRecria.valueOf(etapaOrigenRecria.name())
            : MovimientoEtapa.EtapaRecria.F1;
        
        movimiento.setEtapaOrigen(etapaOrigen);
        movimiento.setEtapaDestino(etapaDestino);
        movimiento.setFechaMovimiento(java.time.LocalDate.now());
        movimiento.setCantidadAnimales(cantidadAnimales);
        movimiento.setPesoPromedio(pesoPromedio);
        movimiento.setObservaciones(observaciones);
        movimiento.setEmpresa(empresaActiva.get());
        movimiento.setUsuario(user);

        // Si se especifica lote destino, validar que exista y crear/actualizar recría destino
        if (loteDestinoId != null) {
            if (loteParaPorcinosQuery.obtenerPorId(loteDestinoId).isEmpty()) {
                throw new IllegalArgumentException("El lote destino no existe");
            }
            movimiento.setLoteDestinoId(loteDestinoId);
            crearOActualizarRecriaDestino(loteDestinoId, recria, etapaDestino, cantidadAnimales, pesoPromedio, user);
        }

        // Actualizar cantidad de animales en recría origen
        int nuevaCantidad = recria.getCantidadAnimales() - cantidadAnimales;
        if (nuevaCantidad == 0) {
            // Cerrar recría origen si no quedan animales
            recria.setFechaSalida(java.time.LocalDate.now());
            recria.setDestino(Recria.DestinoRecria.ENGORDE);
        }
        recria.setCantidadAnimales(nuevaCantidad);
        recria.setEtapa(convertirEtapaMovimientoARecria(etapaDestino));
        recriaRepository.save(recria);

        return movimientoRepository.save(movimiento);
    }

    /**
     * Crear o actualizar recría destino
     */
    private void crearOActualizarRecriaDestino(Long loteDestinoId, Recria recriaOrigen, 
                                               MovimientoEtapa.EtapaRecria etapaDestino,
                                               Integer cantidadAnimales, 
                                               BigDecimal pesoPromedio, User user) {
        Recria.EtapaRecria etapaRecriaDestino = convertirEtapaMovimientoARecria(etapaDestino);
        List<Recria> recriasExistentes = recriaRepository.findByLoteIdAndActivoTrue(loteDestinoId);
        Optional<Recria> recriaDestinoOpt = recriasExistentes.stream()
            .filter(r -> r.getEtapa() == etapaRecriaDestino && r.getFechaSalida() == null)
            .findFirst();

        if (recriaDestinoOpt.isPresent()) {
            Recria recriaDestino = recriaDestinoOpt.get();
            int nuevaCantidad = recriaDestino.getCantidadAnimales() + cantidadAnimales;
            BigDecimal pesoPromedioActual = recriaDestino.getPesoPromedio();
            BigDecimal pesoPromedioNuevo = pesoPromedio;
            int cantidadActual = recriaDestino.getCantidadAnimales();
            BigDecimal pesoTotalActual = pesoPromedioActual.multiply(BigDecimal.valueOf(cantidadActual));
            BigDecimal pesoTotalNuevo = pesoPromedioNuevo.multiply(BigDecimal.valueOf(cantidadAnimales));
            BigDecimal pesoPromedioPonderado = pesoTotalActual.add(pesoTotalNuevo)
                .divide(BigDecimal.valueOf(nuevaCantidad), 2, java.math.RoundingMode.HALF_UP);
            recriaDestino.setCantidadAnimales(nuevaCantidad);
            recriaDestino.setPesoPromedio(pesoPromedioPonderado);
            recriaRepository.save(recriaDestino);
        } else {
            Recria nuevaRecria = new Recria();
            nuevaRecria.setLoteId(loteDestinoId);
            nuevaRecria.setFechaIngreso(java.time.LocalDate.now());
            nuevaRecria.setCantidadAnimales(cantidadAnimales);
            nuevaRecria.setPesoPromedio(pesoPromedio);
            nuevaRecria.setEtapa(etapaRecriaDestino);
            nuevaRecria.setEmpresa(recriaOrigen.getEmpresa());
            nuevaRecria.setUsuario(user);
            nuevaRecria.setActivo(true);
            recriaRepository.save(nuevaRecria);
        }
    }

    /**
     * Convertir etapa de MovimientoEtapa a Recria.EtapaRecria
     */
    private Recria.EtapaRecria convertirEtapaMovimientoARecria(MovimientoEtapa.EtapaRecria etapa) {
        return Recria.EtapaRecria.valueOf(etapa.name());
    }

    public List<MovimientoEtapa> obtenerMovimientosPorRecria(Long recriaId, User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }

        Optional<Recria> recria = recriaRepository.findById(recriaId);
        if (recria.isEmpty() || !recria.get().getEmpresa().getId().equals(empresaActiva.get().getId())) {
            return List.of();
        }

        List<MovimientoEtapa> movimientos = movimientoRepository.findByRecriaOrigen(recria.get());
        movimientos.addAll(movimientoRepository.findByRecriaDestino(recria.get()));
        return movimientos;
    }

    public List<MovimientoEtapa> obtenerTodosLosMovimientos(User user) {
        Optional<Empresa> empresaActiva = empresaContextService.obtenerEmpresaPrincipalDelUsuario(user.getId());
        if (empresaActiva.isEmpty()) {
            return List.of();
        }
        return movimientoRepository.findByEmpresa(empresaActiva.get());
    }
}

